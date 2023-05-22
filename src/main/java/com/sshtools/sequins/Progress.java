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

import java.io.Closeable;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public interface Progress extends Closeable {
	
	public static void checkCancel(Progress p) {
		if(p.isCancelled())
			throw new CancelledException();
	}
	
	@SuppressWarnings("serial")
	class CancelledException extends RuntimeException{
	}
	
	public enum Level {
		INFO, WARNING, ERROR, VERBOSE, NORMAL, TRACE
	}
	
	default void cancel() {
		for(var j : jobs()) {
			j.cancel();
		}
	}
	
	Progress parent();
	
	default Progress root() {
		var p = this;
		while (p != null) {
			if (p.parent() == null)
				return p;
			else
				p = p.parent();
		}
		return null;
	}
	
	boolean isCancelled();
	
	List<Progress> jobs();

	Progress newJob();

	Progress newJob(String name, Object... args);
	
	@Override
	void close();

	/**
	 * Set just the progress value (or indeterminate), without changing the progress message.
	 * 
	 * @param percent percentage or -1 for indeterminate
	 */
	default void progressPercentage(int percent) {
		progressed(percent == -1 ? Optional.empty() : Optional.of(percent), Optional.empty());
	}

	/**
	 * Set just the progress value (or indeterminate), without changing the progress message.
	 * 
	 * @param percent optional percentage
	 */
	void progressPercentage(Optional<Integer> percent);

	/**
	 * Set just the progress message, without changing the progress percentage.
	 * 
	 * @param message message or <code>null</code> 
	 * @param args message formatting arguments (if empty, message is unformatted)
	 */
	default void progressMessage(String message, Object... args) {
		progressed(Optional.empty(), Optional.ofNullable(message), args);
	}

	/**
	 * Set just the progress message, without changing the progress percentage.
	 * 
	 * @param message optional message
	 * @param args message formatting arguments (if empty, message is unformatted)
	 */
	void progressMessage(Optional<String> message, Object... args);

	/**
	 * Set the progress percentange and message at the same time.
	 * 
	 * @param percent percentage or -1 for indeterminate
	 * @param message message or <code>null</code> 
	 * @param args message formatting arguments (if empty, message is unformatted)
	 */
	default void progressed(int percent, String message, Object... args) {
		progressed(percent == -1 ? Optional.empty() : Optional.of(percent), Optional.ofNullable(message), args);
	}

	void progressed(Optional<Integer> percent, Optional<String> message, Object... args);
	
	default void error(String message, Object... args) {
		error(message, null, args);
	}
	
	default void error(String message, Throwable exception, Object... args) {
		message(Level.ERROR, message, args);
		if(exception != null) {
			var s = new StringWriter();
			exception.printStackTrace(new PrintWriter(s));
			message(Level.TRACE, s.toString());
		}
	}
	
	void message(Level level, String message, Object... args);

	static Progress sink() {
		return sink(null);
	}
	static Progress sink(Progress parent) {
		return new Progress() {
			private boolean cancelled;
			private List<Progress> jobs = new ArrayList<>();

			@Override
			public void close() {
			}
			
			@Override
			public void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
			}
			
			@Override
			public void progressPercentage(Optional<Integer> percent) {
			}

			@Override
			public void progressMessage(Optional<String> message, Object... args) {
			}

			@Override
			public Progress newJob() {
				var j = sink();
				jobs.add(j);
				return j;
			}

			@Override
			public Progress newJob(String name, Object... args) {
				var j = sink();
				jobs.add(j);
				return j;
			}

			@Override
			public void message(Level level, String message, Object... args) {
			}

			@Override
			public void cancel() {
				cancelled = true;
				Progress.super.cancel();
			}

			@Override
			public boolean isCancelled() {
				return cancelled;
			}

			@Override
			public List<Progress> jobs() {
				return jobs;
			}

			@Override
			public Progress parent() {
				return parent;
			}
		};
	}

	public static Progress console() {
		return Terminal.create().progressBuilder().build();
	}
}
