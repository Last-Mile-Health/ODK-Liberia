package org.lastmilehealth.collect.android.summary.calc;

public class DefaultFunctionFactory implements FunctionFactory {

    @Override
    public Function createFunction(String functionName) {
        switch (functionName.toUpperCase()) {
            case FunctionType.SUM:
                return new SumFunction();

            case FunctionType.AVERAGE:
                return new AverageFunction();

            case FunctionType.COUNT:
                return new CountFunction();

            case FunctionType.MAX:
                return new MaxFunction();

            case FunctionType.MIN:
                return new MinFunction();

            case FunctionType.PRODUCT:
                return new ProductFunction();
        }
        return null;
    }

}
