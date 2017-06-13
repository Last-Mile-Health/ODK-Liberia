package org.lastmilehealth.collect.android.summary.calc;

public class MaxFunction extends BaseFunction {
	
	public MaxFunction() {
	}
	
	public MaxFunction(Function ...functions) {
		super(functions);
	}

	@Override
	public double evaluate() {
		if (parameters.size() == 0){ 
			return 0;
		}
		double max = Double.MIN_VALUE;
		for (Function parameter : parameters) {
			double parameterValue = parameter.evaluate();
			if (parameterValue > max) {
				max = parameterValue;
			}
		}
		return max;
	}

}
