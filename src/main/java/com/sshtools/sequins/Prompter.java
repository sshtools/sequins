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

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public interface Prompter {
	
	public final static class PromptContextBuilder {
		private PromptContextBuilder() {}
		
		private Optional<String> use = Optional.empty();
		private Map<String, Object> attrs = new HashMap<>();
		
		public final static PromptContextBuilder builder() {
			return new PromptContextBuilder();
		}
		
		public PromptContextBuilder withUse(String use) {
			return withUse(Optional.ofNullable(use));
		}
		
		public PromptContextBuilder withUse(Optional<String> use) {
			this.use = use;
			return this;
		}

		public PromptContextBuilder withAttr(String key, Object val) {
			this.attrs.put(key, val);
			return this;
		}

		public PromptContextBuilder withAttrs(Map<String, Object> attrs) {
			this.attrs.putAll(attrs);
			return this;
		}
		
		public PromptContext build() {
			var a = Collections.unmodifiableMap(new HashMap<>(this.attrs));
			var u = use;
			return new PromptContext() {
				@Override
				public Optional<String> use() {
					return u;
				}
				
				@SuppressWarnings("unchecked")
				@Override
				public <O> Optional<O> attr(String key) {
					var o = a.get(key);
					return (Optional<O>)(o == null ? Optional.empty() : Optional.of(o)); 
				}
			};
		}
	}
	
	public interface PromptContext {
		Optional<String> use();
		<O> Optional<O>  attr(String key);
		
		public static PromptContext empty() {
			return new PromptContext() {
				@Override
				public Optional<String> use() {
					return Optional.empty();
				}
				
				@Override
				public <O> Optional<O>  attr(String key) {
					return Optional.empty();
				}
			};
		}
	}
	
	static boolean isYes(String str, boolean defaultIfNull) {
		return (str == null) ? defaultIfNull : str.toLowerCase().startsWith("y");
	}
	
	boolean yesNo();

	boolean yesNo(String fmt, Object... args);

	default boolean yesNo(PromptContext context, String fmt, Object... args) {
		return yesNo(fmt, args);
	}

	boolean noYes();

	boolean noYes(String fmt, Object... args);

	default boolean noYes(PromptContext context, String fmt, Object... args) {
		return noYes(fmt, args);
	}

	default void pause() {
		prompt();
	}

	default void pause(String fmt, Object... args) {
		prompt(fmt, args);
	}

	default void pause(PromptContext context, String fmt, Object... args) {
		pause(fmt, args);
	}

	String prompt();

	String prompt(String fmt, Object... args);

	default String prompt(PromptContext context, String fmt, Object... args) {
		return prompt(fmt, args);
	}

	char[] password();

	char[] password(String fmt, Object... args);

	default char[] password(PromptContext context, String fmt, Object... args) {
		return password(fmt, args);
	}
}
