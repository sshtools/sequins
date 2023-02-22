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

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.ServiceLoader;

public interface Terminal extends Prompter, DrawContext {
	
	static Terminal create() {
		var l = new ArrayList<TerminalFactory>();
		for(var srv : ServiceLoader.load(TerminalFactory.class)) {
			if(srv.isAvailable()) {
				l.add(srv);
			}
		}
		Collections.sort(l, (a, b) -> Integer.valueOf(a.getWeight()).compareTo(b.getWeight()));
		if(l.isEmpty())
			throw new IllegalStateException("No providers.");
		return l.get(0).create();
	}
	
	static boolean isYes(String str, boolean defaultIfNull) {
		return (str == null) ? defaultIfNull : str.toLowerCase().startsWith("y");
	}
	
	default boolean yesNo() {
		return isYes(prompt(createSequence().ch('[').boldOn().ch('Y').boldOff().str("]es,[N]o: ").toString()), true);
	}

	default boolean yesNo(String fmt, Object... args) {
		return isYes(prompt(
						createSequence().fmt(fmt, args).str(" [").boldOn().ch('Y').boldOff().str("]es,[N]o: ").toString()), true);
	}
	default boolean noYes() {
		return isYes(prompt(createSequence().str("[Y]es,[").boldOn().ch('N').boldOff().str("]o: ").toString()), false);
	}

	default boolean noYes(String fmt, Object... args) {
		return isYes(prompt(
						createSequence().fmt(fmt, args).str(" [Y]es,[").boldOn().ch('N').boldOff().str("]o: ").toString()), false);
	}

	default String prompt() {
		var console = System.console();
		if (console == null) {
			try {
				return new BufferedReader(new InputStreamReader(System.in)).readLine();
			} catch (IOException e) {
				return null;
			}
		}
		return console.readLine();
	}

	default String prompt(String fmt, Object... args) {
		var console = System.console();
		if (console == null) {
			try {
				var writer = getWriter();
				writer.print(createSequence().msg(fmt, args).toString());
				writer.flush();
				return new BufferedReader(new InputStreamReader(System.in)).readLine();
			} catch (IOException e) {
				return null;
			}
		}
		return console.readLine(createSequence().msg(fmt, args).toString(), args);
	}

	default char[] password() {
		var console = System.console();
		if (console == null) {
			try {
				return new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
			} catch (IOException e) {
				return null;
			}
		}
		return console.readPassword();
	}

	default char[] password(String fmt, Object... args) {
		var console = System.console();
		if (console == null) {
			try {
				var writer = getWriter();
				writer.print(createSequence().msg(fmt, args).toString());
				writer.flush();
				return new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
			} catch (IOException e) {
				return null;
			}
		}
		return console.readPassword(createSequence().msg(fmt, args).toString());
	}

	PrintWriter getWriter();

	PrintWriter getErrorWriter();

	Sequence createSequence();

	ProgressBuilder progressBuilder();
	
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
			error(message, args);
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
