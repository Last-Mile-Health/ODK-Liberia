package org.lastmilehealth.collect.android.summary.calc;

import java.text.DecimalFormat;

/**
 * Created by Anton Donchev on 13.06.2017.
 */

public class RoundingFunctionEvaluator implements FunctionEvaluator {
    public static final int MAX_DECIMAL_PLACES = 3;
    private Function function;
    private int decimalPlaces = MAX_DECIMAL_PLACES;

    @Override
    public String evaluate() {
        if (function == null) {
            return null;
        } else {
            try {
                double functionResult = function.evaluate();
                DecimalFormat format = createDecimalFormat();
                return format.format(functionResult);
            } catch (Exception e) {
                return "0";
            }
        }
    }

    public void setFunction(Function function) {
        this.function = function;
    }

    public void setDecimalPlaces(int decimalPlaces) {
        if (decimalPlaces < 0) {
            this.decimalPlaces = 0;
        } else {
            this.decimalPlaces = Math.min(decimalPlaces, MAX_DECIMAL_PLACES);
        }
    }

    private DecimalFormat createDecimalFormat() {
        StringBuilder builder = new StringBuilder("#");
        if (decimalPlaces > 0) {
            builder.append(".");
            for (int i = 0; i < decimalPlaces; i++) {
                builder.append("#");
            }
        }
        return new DecimalFormat(builder.toString());
    }
}
