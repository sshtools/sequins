package com.sshtools.sequins;

import java.util.Optional;

public abstract class ProgressBuilder {

	protected boolean indeterminate;
	protected boolean percentageText;
	protected Optional<Long> rateLimit;
	protected boolean interruptable;
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

	public ProgressBuilder withRateLimit() {
		return withRateLimit(500);
	}

	public ProgressBuilder withRateLimit(long rateLimit) {
		this.rateLimit = Optional.of(rateLimit);
		return this;
	}

	public final Progress build() {
		var b = buildImpl();
		if (interruptable) {
			b = new InterruptableProgress(b);
			InterruptableProgress.attachThread(b);			
		}
		if (rateLimit.isPresent()) {
			b = new RateLimitedProgress(b, rateLimit.get());
		}
		return b;
	}

	protected abstract Progress buildImpl();
}
