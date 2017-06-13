package org.lastmilehealth.collect.android.summary.calc;

public class AverageFunction extends BaseFunction{
	
	public AverageFunction() {
	}
	
	public AverageFunction(Function ...functions) {
		super(functions);
	}

	@Override
	public double evaluate() {
		if (parameters.size() == 0){ 
			return 0.0;
		}
		double sum = 0;
		for (Function parameter : parameters) {
			sum += parameter.evaluate();
		}
		return sum/parameters.size();
	}
	
}
