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
import java.io.IOException;
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
	
	boolean isCancelled();
	
	List<Progress> jobs();

	Progress newJob(String name, Object... args);

	default void progressed(int percent) {
		progressed(Optional.of(percent), Optional.empty());
	}

	default void progressed(String message, Object... args) {
		progressed(Optional.empty(), Optional.of(message), args);
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
		return new Progress() {
			private boolean cancelled;
			private List<Progress> jobs = new ArrayList<>();

			@Override
			public void close() throws IOException {
			}
			
			@Override
			public void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
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
		};
	}
}
