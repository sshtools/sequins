package com.sshtools.sequins;

public abstract class ProgressBuilder {

	protected boolean indeterminate;
	protected boolean percentageText;
	protected String message;
	protected Object[] args;
	
	public ProgressBuilder withMessage(String message, Object... args) {
		this.message = message;
		this.args = args;
		return this;
	}
	
	public ProgressBuilder withIndeterminate() {
		this.indeterminate = true;
		return this;
	}
	
	public ProgressBuilder withPercentageText() {
		this.indeterminate = true;
		return this;
	}
	
	public abstract Progress build();
}
