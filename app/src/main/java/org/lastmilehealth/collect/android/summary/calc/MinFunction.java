package org.lastmilehealth.collect.android.summary.calc;

public class MinFunction extends BaseFunction {
	
	public MinFunction() {
	}
	
	public MinFunction(Function ...functions) {
		super(functions);
	}

	@Override
	public double evaluate() {
		if (parameters.size() == 0){ 
			return 0;
		}
		double min = Double.MAX_VALUE;
		for (Function parameter : parameters) {
			double parameterValue = parameter.evaluate();
			if (parameterValue < min) {
				min = parameterValue;
			}
		}
		return min;
	}
	
}
