package org.lastmilehealth.collect.android.summary.calc;

public class ProductFunction extends BaseFunction {
	
	public ProductFunction() {
	}
	
	public ProductFunction(Function ...functions) {
		super(functions);
	}

	@Override
	public double evaluate() {
		if (parameters.size() == 0){ 
			return 0;
		}
		double product = 1;
		for (Function parameter : parameters) {
			product *= parameter.evaluate();
		}
		return product;
	}

}
