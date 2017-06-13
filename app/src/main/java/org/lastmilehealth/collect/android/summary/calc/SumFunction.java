package org.lastmilehealth.collect.android.summary.calc;

public class SumFunction extends BaseFunction {
	
	public SumFunction() {
		
	}
	
	public SumFunction(Function ...functions) {
		super(functions);
	}

	@Override
	public double evaluate() {
		double sum = 0;
		if (parameters.size() > 0) {
			for (Function parameter : parameters) {
				sum += parameter.evaluate();
			}
		}
		return sum;
	}

}
