package org.lastmilehealth.collect.android.summary.calc;

public class EvaluationException extends RuntimeException {
	
	public EvaluationException(String message) {
		super(message);
	}
	
	public EvaluationException(String message, Throwable exception) {
		super (message, exception);
	}
}
