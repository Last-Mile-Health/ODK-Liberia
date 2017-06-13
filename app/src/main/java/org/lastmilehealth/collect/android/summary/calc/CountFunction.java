package org.lastmilehealth.collect.android.summary.calc;

public class CountFunction extends BaseFunction {
	
	public CountFunction() {
	}
	
	public CountFunction(Function ...functions) {
		super(functions);
	}

	@Override
	public double evaluate() {
		return parameters.size();
	}

}
