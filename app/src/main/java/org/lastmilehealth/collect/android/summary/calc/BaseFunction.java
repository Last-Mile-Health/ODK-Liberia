package org.lastmilehealth.collect.android.summary.calc;

import java.util.ArrayList;
import java.util.List;

public abstract class BaseFunction implements Function {
	protected List<Function> parameters = new ArrayList<Function>();
	
	public BaseFunction() {
		
	}
	
	public BaseFunction(Function ...functions) {
		if (functions != null) {
			for (Function func : functions) {
				if (func != null) {
					parameters.add(func);
				}
			}
		}
	}
	
	
	@Override
	public void addParameter(Function parameter) {
		parameters.add(parameter);
	}
}
