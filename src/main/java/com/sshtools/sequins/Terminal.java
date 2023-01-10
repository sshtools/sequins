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

public interface Terminal {
	
	static Terminal create() {
		var l = new ArrayList<TerminalFactory>();
		for(var srv : ServiceLoader.load(TerminalFactory.class)) {
			if(srv.isAvailable())
				l.add(srv);
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
				getWriter().print(createSequence().fmt(fmt, args).toString());
				return new BufferedReader(new InputStreamReader(System.in)).readLine();
			} catch (IOException e) {
				return null;
			}
		}
		return console.readLine(createSequence().fmt(fmt, args).toString(), args);
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
				getWriter().print(createSequence().fmt(fmt, args).toString());
				return new BufferedReader(new InputStreamReader(System.in)).readLine().toCharArray();
			} catch (IOException e) {
				return null;
			}
		}
		return console.readPassword(createSequence().fmt(fmt, args).toString());
	}

	PrintWriter getWriter();

	int getWidth();

	Sequence createSequence();

	default Progress createProgress(String title, Object... args) {
		return new DefaultConsoleProgress(title, args);
	}

}
