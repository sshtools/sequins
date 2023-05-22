/**
 * Copyright © 2023 JAdaptive Limited (support@jadaptive.com)
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sshtools.sequins;

import java.time.Duration;
import java.util.Optional;

public abstract class ProgressBuilder {

	protected boolean indeterminate;
	protected boolean percentageText;
	protected Optional<Long> rateLimit = Optional.empty();
	protected boolean interruptable;
	protected boolean timing = !System.getProperty("sequins.timeProgress", "false").equals("false");
	protected String message;
	protected Object[] args;
	protected Duration spinnerStartDelay = Duration.ofSeconds(1);
	protected boolean hideCursor;
	
	
	public ProgressBuilder withMessage(String message, Object... args) {
		this.message = message;
		this.args = args;
		return this;
	}

	public ProgressBuilder withSpinnerStartDelay(Duration spinnerStartDelay) {
		this.spinnerStartDelay = spinnerStartDelay;
		return this;
	}

	public ProgressBuilder withIndeterminate() {
		this.indeterminate = true;
		return this;
	}

	public ProgressBuilder withPercentageText() {
		this.percentageText = true;
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

	public ProgressBuilder withHideCursor() {
		return withHideCursor(true);
	}

	public ProgressBuilder withHideCursor(boolean hideCursor) {
		this.hideCursor = hideCursor;
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

	public final boolean hideCursor() {
		return hideCursor;
	}

	public final Duration spinnerStartDelay() {
		return spinnerStartDelay;
	}

	public final boolean indeterminate() {
		return indeterminate;
	}

	public final boolean percentageText() {
		return percentageText;
	}

	public final Optional<Long> rateLimit() {
		return rateLimit;
	}

	public final boolean interruptable() {
		return interruptable;
	}

	public final boolean timing() {
		return timing;
	}

	public final String message() {
		return message;
	}

	public final Object[] args() {
		return args;
	}

	protected abstract Progress buildImpl();
}
