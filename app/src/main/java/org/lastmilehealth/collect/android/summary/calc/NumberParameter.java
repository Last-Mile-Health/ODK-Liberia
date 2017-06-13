package org.lastmilehealth.collect.android.summary.calc;

public class NumberParameter extends BaseFunction {
	private final double value;
	
	public NumberParameter(double value) {
		this.value = value;
	}
	
	@Override
	public void addParameter(Function parameter) {
		throw new EvaluationException("Cannot add parameters to IntegerParameter");
	}

	@Override
	public double evaluate() {
		return value;
	}

}
