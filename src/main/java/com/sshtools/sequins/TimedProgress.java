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
import java.util.List;
import java.util.Optional;

public class TimedProgress implements Progress {
	
	private final Progress delegate;
	private long started;

	TimedProgress(Progress delegate) {
		this.delegate = delegate;
		started = System.currentTimeMillis();
	}

	@Override
	public void close() throws IOException {
		try {
			var now = System.currentTimeMillis();
			message(Level.INFO, "Task took {0}s", String.format("%f", (double)(now - started) / 1000.0d));
		}
		finally {
			delegate.close();
		}
		
	}

	@Override
	public boolean isCancelled() {
		return delegate.isCancelled();
	}

	@Override
	public List<Progress> jobs() {
		return delegate.jobs();
	}

	@Override
	public Progress newJob(String name, Object... args) {
		return delegate.newJob(name, args);
	}

	@Override
	public void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		delegate.progressed(percent, message, args);
	}

	@Override
	public void message(Level level, String message, Object... args) {
		delegate.message(level, message, args);
	}
	
	public static TimedProgress of(Progress delegate) {
		return new TimedProgress(delegate);
	}

}
