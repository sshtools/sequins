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

import java.io.PrintWriter;

public interface Terminal extends Prompter, DrawContext {
	
	@Deprecated
	static Terminal create() {
		return Sequins.create();
	}
	
	static boolean isYes(String str, boolean defaultIfNull) {
		return (str == null) ? defaultIfNull : str.toLowerCase().startsWith("y");
	}
	
	default boolean yesNo() {
		return isYes(prompt(createSequence().ch('[').boldOn().ch('Y').boldOff().str("]es,[N]o: ").toString()), true);
	}

	default boolean yesNo(String fmt, Object... args) {
		return yesNo(PromptContext.empty(), fmt, args);
	}

	default boolean noYes(String fmt, Object... args) {
		return noYes(PromptContext.empty(), fmt, args);
	}

	default String prompt(String fmt, Object... args) {
		return prompt(PromptContext.empty(), fmt, args);
	}

	default char[] password(String fmt, Object... args) {
		return password(PromptContext.empty(), fmt, args);
	}

	default boolean yesNo(PromptContext context, String fmt, Object... args) {
		return isYes(prompt(context, 
						createSequence().msg(fmt, args).str(" [").boldOn().ch('Y').boldOff().str("]es,[N]o: ").toString()), true);
	}
	
	default boolean noYes() {
		return isYes(prompt(createSequence().str("[Y]es,[").boldOn().ch('N').boldOff().str("]o: ").toString()), false);
	}

	default boolean noYes(PromptContext context, String fmt, Object... args) {
		return isYes(prompt(context,
						createSequence().msg(fmt, args).str(" [Y]es,[").boldOn().ch('N').boldOff().str("]o: ").toString()), false);
	}

	String prompt();

	String prompt(PromptContext context, String fmt, Object... args);

	char[] password();

	char[] password(PromptContext context, String fmt, Object... args);

	PrintWriter getWriter();

	PrintWriter getErrorWriter();

	Sequence createSequence();

	ProgressBuilder progressBuilder();
	
	default Terminal print(String text) {
		var wrt = getWriter();
		wrt.print(text);
		wrt.flush();
		return this;
	}
	
	default Terminal println() {
		var wrt = getWriter();
		wrt.println();
		wrt.flush();
		return this;
	}
	
	default Terminal println(String text) {
		var wrt = getWriter();
		wrt.println(text);
		wrt.flush();
		return this;
	}
	
	default Terminal newline() {
		var wrt = getWriter();
		wrt.println();
		wrt.flush();
		return this;
	}
	
	default Terminal message(String message, Object... args) {
		var seq = createSequence();
		seq.msg(message, args);
		var wrt = getWriter();
		wrt.print(seq);
		wrt.flush();
		return this;
	}
	
	default Terminal messageln(String message, Object... args) {
		var seq = createSequence();
		seq.msg(message, args);
		var wrt = getWriter();
		wrt.println(seq);
		wrt.flush();
		return this;
	}

	default Terminal error(Throwable exception) {
		return error(null, exception);
	}

	default Terminal error(String message, Throwable exception, Object... args) {
		return error(false, message, exception, args);
	}
	
	default Terminal error(boolean showTrace, String message, Throwable exception, Object... args) {
		if(message != null) {
			errorln(message, args);
		}
		if(exception != null) {
			var seq = createSequence();
			seq.exception(exception, showTrace);
			var wrt = getErrorWriter();
			wrt.print(seq.toString());
			wrt.flush();
		}
		return this;
	}
	
	default Terminal error(String message, Object... args) {
		var seq = createSequence();
		seq.msg(message, args);
		var wrt = getErrorWriter();
		wrt.print(seq);
		wrt.flush();
		return this;
	}
	
	default Terminal errorln(String message, Object... args) {
		var seq = createSequence();
		seq.msg(message, args);
		var wrt = getErrorWriter();
		wrt.println(seq);
		return this;
	}

	default ProgressBuilder progressBuilder(String title, Object... args) {
		return progressBuilder().withMessage(title, args);
	}

}
