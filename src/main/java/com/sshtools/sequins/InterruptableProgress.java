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

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterruptableProgress implements Progress {

	private Thread thread;
	private boolean cancelled;
	private final Progress delegate;
	private List<Progress> jobs = new ArrayList<>();

	InterruptableProgress(Progress delegate) {
		this.delegate = delegate;
	}

	@Override
	public void error(String message, Object... args) {
		delegate.error(message, args);
	}

	@Override
	public void error(String message, Throwable exception, Object... args) {
		delegate.error(message, exception, args);
	}

	@Override
	public void close() throws IOException {
		delegate.close();
	}

	@Override
	public void progressed(int percent) {
		delegate.progressed(percent);
	}

	@Override
	public void progressed(String message, Object... args) {
		delegate.progressed(message, args);
	}

	@Override
	public void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		delegate.progressed(percent, message, args);
	}

	@Override
	public Progress newJob(String name, Object... args) {
		var j = new InterruptableProgress(delegate.newJob(name, args));
		jobs.add(j);
		return j;
	}

	@Override
	public void message(Level level, String message, Object... args) {
		delegate.message(level, message, args);
	}

	@Override
	public boolean isCancelled() {
		return cancelled;
	}

	@Override
	public final void cancel() {
		cancelled = true;
		try {
			delegate.cancel();
		} finally {
			try {
				Progress.super.cancel();
			} finally {
				if (thread != null) {
					thread.interrupt();
				}
			}
		}
	}

	@Override
	public List<Progress> jobs() {
		return jobs;
	}

	public static void attachThread(Progress progress) {
		attachThread(progress, Thread.currentThread());
	}

	public static void attachThread(Progress progress, Thread thread) {
		if (progress instanceof InterruptableProgress) {
			var ip = (InterruptableProgress) progress;
			ip.thread = thread;
		} else
			throw new IllegalArgumentException(MessageFormat.format(
					"{0} implementation is not a {1}. Use {2}.withInterruptable() to wrap it first.",
					Progress.class.getName(), ProgressBuilder.class.getName(), Progress.class.getName()));
	}
	
	public static InterruptableProgress of(Progress delegate) {
		return new InterruptableProgress(delegate);
	}

}
