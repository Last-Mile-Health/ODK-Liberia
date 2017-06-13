package org.lastmilehealth.collect.android.summary.calc;

public interface Function {
	
	void addParameter(Function parameter);
	
	double evaluate();
}
