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
import java.util.Optional;

public interface Progress extends Closeable {
	
	public enum Level {
		INFO, WARNING, ERROR, VERBOSE, NORMAL
	}

	Progress newJob(String name, Object... args);

	void progressed(Optional<Integer> percent);
	
	void message(Level level, String message, Object... args);
	
	static Progress sink() {
		return new Progress() {
			@Override
			public void close() throws IOException {
			}
			
			@Override
			public void progressed(Optional<Integer> percent) {
			}
			
			@Override
			public Progress newJob(String name, Object... args) {
				return sink();
			}

			@Override
			public void message(Level level, String message, Object... args) {
			}
		};
	}
}
