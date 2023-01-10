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
import java.util.Optional;

public class DefaultConsoleProgress implements Progress {

	public class Formattable {
		Object[] args;
		Optional<Level> level;
		String pattern;

		public Formattable(Optional<Level> level, String pattern, Object... args) {
			super();
			this.pattern = pattern;
			this.args = args;
			this.level = level;
		}

		public Formattable(String pattern, Object... args) {
			this(Optional.empty(), pattern, args);
		}

		public Object[] args() {
			return args;
		}
		
		public Optional<Level> level() {
			return level;
		}

		public String pattern() {
			return pattern;
		}

		public String toString() {
			return MessageFormat.format(pattern, args);
		}

	}

	class Spinner extends Thread {

		boolean active = true;
		int index;

		Spinner() {
			super("ProgressSpinner");
			setDaemon(true);
		}

		public void run() {
			try {
				while (active) {
					synchronized (lock) {
						printJob();
					}
					index++;
					if (index == spinnerChars.length)
						index = 0;
					Thread.sleep(100);
				}
			} catch (InterruptedException ie) {
			}
		}
	}

	static int[] SPINNER_CHARS = new int[] { '|', '/', '-', '\\', '|', '/', '-', '\\' };

	private int indent = 0;
	private StringBuilder indentStr = new StringBuilder();
	private Object lock;
	private Formattable message;
	private Formattable name;
	private boolean newlineNeeded;
	private Optional<Integer> percent = Optional.empty();
	private Spinner spinner;
	private int[] spinnerChars = SPINNER_CHARS;

	public DefaultConsoleProgress(String name, Object... args) {
		this(new Object(), 0, name, args);
	}
	
	public Formattable name() {
		return name;
	}
	
	public Formattable message() {
		return message;
	}

	public int[] getSpinnerChars() {
		return spinnerChars;
	}

	public void setSpinnerChars(int[] spinnerChars) {
		this.spinnerChars = spinnerChars;
	}

	public void setSpinnerChars(String spinnerChars) {
		this.spinnerChars = spinnerChars.codePoints().toArray();
	}

	protected DefaultConsoleProgress(Object lock, int indent, String name, Object... args) {
		this.lock = lock;
		this.name = new Formattable(name, args);
		this.indent = indent;
		for (int i = 0; i < indent; i++)
			indentStr.append("   ");
		postConstruct();
	}
	
	protected void postConstruct() {
		if (name != null) {
			printJob();
		}
		startSpinner();
	}

	public int indent() {
		return indent;
	}

	@Override
	public final void close() throws IOException {
		synchronized (lock) {
			stopSpinner();
			printJob();
			if (newlineNeeded)
				printNewline();
		}
	}

	@Override
	public final void message(Level level, String message, Object... args) {
		synchronized (lock) {
			stopSpinner();
			this.message = new Formattable(Optional.of(level), message, args);
			try {
				printJob();
				printNewline();
			} finally {
				this.message = null;
			}
		}
	}

	@Override
	public final Progress newJob(String name, Object... args) {
		stopSpinner();
		clear();
		if (newlineNeeded)
			printNewline();
		return createNewJob(lock, name, args);
	}

	@Override
	public final void progressed(Optional<Integer> percent) {
		synchronized (lock) {
			if (percent.isPresent()) {
				stopSpinner(); // Now have actual progress
			}
			this.percent = percent;
			printJob();
		}
	}

	protected void printName() {
		System.out.print(name);
	}

	protected void printSpinner() {
		if (spinner != null) {
			System.out.print(": " + Character.toString(spinnerChars[spinner.index]));
		} else if (percent.isPresent()) {
			System.out.print(": " + percent.get() + "%");
		} else {
			System.out.print("        ");
		}
	}

	protected void printIndent() {
		if (indent > 1)
			System.out.print("o ");
		else
			System.out.print("* ");
	}

	protected void printMessage() {
		System.out.print(message);
	}

	protected DefaultConsoleProgress createNewJob(Object lock, String name,  Object... args) {
		return new DefaultConsoleProgress(lock, indent + 1, name, args);
	}

	void clear() {
		name = null;
	}

	void printJob() {
		synchronized (lock) {
			newlineNeeded = true;
			System.out.print(indentStr);
			if (indent > 0) {
				printIndent();
			}
			if (message == null) {
				if (name != null) {
					printName();
					printSpinner();
					System.out.print("\r");
				}
			} else {
				printMessage();
			}
		}
	}

	void printNewline() {
		stopSpinner();
		System.out.println();
		newlineNeeded = false;
	}

	void startSpinner() {
		synchronized (lock) {
			if (spinner == null) {
				spinner = new Spinner();
				spinner.start();
			}
		}
	}

	void stopSpinner() {
		synchronized (lock) {
			if (spinner != null) {
				spinner.active = false;
				spinner.interrupt();
				try {
					spinner.join();
				} catch (InterruptedException e) {
					throw new IllegalStateException("Interrupted.", e);
				} finally {
					spinner = null;
				}
				printJob();
			}
		}
	}

	String trimAndFillTo(String str, int size, char fillWith) {
		if (str.length() > size)
			str = str.substring(0, size);
		while (str.length() < size) {
			str = str.concat(String.valueOf(fillWith));
		}
		return str;
	}

}
