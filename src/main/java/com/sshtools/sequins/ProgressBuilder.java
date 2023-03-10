package com.sshtools.sequins;

import java.util.Optional;

public abstract class ProgressBuilder {

	protected boolean indeterminate;
	protected boolean percentageText;
	protected Optional<Long> rateLimit = Optional.empty();
	protected boolean interruptable;
	protected boolean timing = !System.getProperty("sequins.timeProgress", "false").equals("false");
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

	public ProgressBuilder withInterruptable() {
		this.interruptable = true;
		return this;
	}

	public ProgressBuilder withTiming() {
		return withTiming(true);
	}

	public ProgressBuilder withTiming(boolean timing) {
		this.timing = timing;
		return this;
	}

	public ProgressBuilder withRateLimit() {
		return withRateLimit(500);
	}

	public ProgressBuilder withRateLimit(long rateLimit) {
		this.rateLimit = Optional.of(rateLimit);
		return this;
	}

	public final Progress build() {
		var b = buildImpl();
		if (rateLimit.isPresent()) {
			b = new RateLimitedProgress(b, rateLimit.get());
		}
		if(timing) {
			b = new TimedProgress(b); 
		}
		if (interruptable) {
			b = new InterruptableProgress(b);
			InterruptableProgress.attachThread(b);			
		}
		return b;
	}

	public boolean indeterminate() {
		return indeterminate;
	}

	public boolean percentageText() {
		return percentageText;
	}

	public Optional<Long> rateLimit() {
		return rateLimit;
	}

	public boolean interruptable() {
		return interruptable;
	}

	public boolean timing() {
		return timing;
	}

	public String message() {
		return message;
	}

	public Object[] args() {
		return args;
	}

	protected abstract Progress buildImpl();
}
