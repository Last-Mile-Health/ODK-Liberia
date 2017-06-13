package org.lastmilehealth.collect.android.summary.calc;

import android.text.TextUtils;

import org.lastmilehealth.collect.android.parser.InstanceElement;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DefaultFunctionParser implements FunctionParser {
    private static final String REGEX_FUNCTION = "^([A-Z]+)\\((.*)\\)$";
    private static final String REGEX_VARIABLE = "^[A-Za-z0-9\\\\/]+$";
    private final FunctionFactory functionFactory;
    private final RoundingFunctionEvaluator functionEvaluator = new RoundingFunctionEvaluator();
    private final Collection<InstanceElement> instances = new ArrayList<>();
    private Function rootFunc = null;

    public DefaultFunctionParser(FunctionFactory factory) {
        this.functionFactory = factory;
    }

    @Override
    public void setInstances(Collection<InstanceElement> elements) {
        this.instances.addAll(elements);
    }

    @Override
    public FunctionEvaluator parse(String expression) {
        if (TextUtils.isEmpty(expression)) {
            throw new ParsingException("Expression is empty");
        }

        try {
            parseRecursive(expression.replaceAll("\\s", ""), null);
        } catch (Exception e) {
            rootFunc = null;
        }

        if (rootFunc == null) {
            throw new ParsingException("Cannot parse expression (" + expression + ")");
        }

        functionEvaluator.setFunction(rootFunc);

        return functionEvaluator;
    }

    private Function parseRecursive(String expression,
                                    Function parentFunc) {
        // first check if this is a number
        Function func = null;
        boolean isNumber = tryParsingNumber(expression, parentFunc);

        if (!isNumber) {
            // check if it is a variable parameter
            if (tryParsingVariable(expression, parentFunc)) {
                return null;
            } else {
                // try to parse as a function
                func = tryParsingFunction(expression, parentFunc);
            }
        } else {
            return null;
        }
        return func;
    }

    private List<String> parseFunctionParameters(String expression) {
        int brackets = 0;
        List<String> parsedParameters = new ArrayList<>();
        char[] expressionAsCharArray = expression.toCharArray();
        StringBuilder processedParameter = new StringBuilder();
        for (int i = 0; i < expressionAsCharArray.length; i++) {
            char c = expressionAsCharArray[i];
            switch (c) {
                case '(':
                    brackets++;
                    processedParameter.append(c);
                    break;

                case ')':
                    brackets--;
                    processedParameter.append(c);
                    break;

                case ',':
                    if (brackets == 0) {
                        if (processedParameter.length() > 0) {
                            parsedParameters.add(processedParameter.toString());
                            processedParameter.setLength(0);
                        }
                    } else {
                        processedParameter.append(c);
                    }
                    break;

                default:
                    processedParameter.append(c);
                    break;
            }
        }

        if (brackets != 0) {
            throw new ParsingException("Brackets doesn't match");
        } else if (processedParameter.length() > 0) {
            parsedParameters.add(processedParameter.toString());
        }
        return parsedParameters;
    }

    private boolean tryParsingNumber(String expression,
                                     Function parentFunc) {
        double number;
        try {
            number = Double.parseDouble(expression);
        } catch (Exception e) {
            return false;
        }
        if (parentFunc == null) {
            throw new ParsingException("A function must be the root");
        }
        Function param = new NumberParameter(number);
        parentFunc.addParameter(param);
        return true;
    }

    private boolean tryParsingVariable(String expression,
                                       Function parentFunc) {
        Matcher matcher = Pattern.compile(REGEX_VARIABLE).matcher(expression);
        // TODO parse the variable through all the instances.
        if (matcher.find()) {
            if (parentFunc == null) {
                throw new ParsingException("A function must be the root");
            }
            for (InstanceElement element : instances) {
                VariableParameter parameter = new VariableParameter(expression, element);
                parentFunc.addParameter(parameter);
            }
            return true;
        }
        return false;
    }

    private Function tryParsingFunction(String expression,
                                        Function parentFunc) {
        Function func = null;
        Pattern pattern = Pattern.compile(REGEX_FUNCTION);
        Matcher matcher = pattern.matcher(expression);
        if (matcher.find()) {
            String functionName = matcher.group(1);
            if (functionName.equalsIgnoreCase(FunctionType.ROUND)) {
                List<String> parameters = parseFunctionParameters(matcher.group(2));
                functionEvaluator.setDecimalPlaces(Integer.parseInt(parameters.get(1)));
                parseRecursive(parameters.get(0), parentFunc);
            } else {
                func = functionFactory.createFunction(functionName);
                if (func != null) {
                    // The parent func should be null only when the root func is parsed.
                    if (parentFunc != null) {
                        parentFunc.addParameter(func);
                    } else {
                        rootFunc = func;
                    }
                    List<String> parameters = parseFunctionParameters(matcher.group(2));
                    for (String param : parameters) {
                        parseRecursive(param, func);
                    }
                } else {
                    throw new ParsingException("Unknown function type " + functionName);
                }
            }

        }
        return func;
    }
}
