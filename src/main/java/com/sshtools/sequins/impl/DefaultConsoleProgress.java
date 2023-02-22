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
package com.sshtools.sequins.impl;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import com.sshtools.sequins.Progress;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Terminal;

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
	private boolean newlineNeeded;
	private Optional<Integer> percent = Optional.empty();
	private Spinner spinner;
	private int[] spinnerChars = SPINNER_CHARS;
	private boolean cancelled;
	private List<Progress> jobs = new ArrayList<>();
	
	protected final Terminal terminal;	
	protected final boolean indeterminate;
	protected final boolean percentageText;

	DefaultConsoleProgress(Terminal terminal, boolean showSpinner, boolean percentageText, String name, Object... args) {
		this(terminal, showSpinner, percentageText, new Object(), 0, name, args);
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

	protected DefaultConsoleProgress(Terminal terminal, boolean showSpinner, boolean percentageText, Object lock, int indent, String name, Object... args) {
		this.terminal = terminal;
		this.lock = lock;
		this.indeterminate = showSpinner;
		this.percentageText = percentageText;
		this.message = name == null ? null :  new Formattable(name, args);
		this.indent = indent;
		for (int i = 0; i < indent; i++)
			indentStr.append("   ");
		postConstruct();
	}
	
	protected void postConstruct() {
		if (message != null) {
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
			var wasNlNeeded = newlineNeeded;
			printJob();
			if (wasNlNeeded)
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
		if (newlineNeeded)
			printNewline();
		var j = createNewJob(lock, name, args);
		jobs.add(j);
		return j;
	}

	@Override
	public final void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		synchronized (lock) {
			if (percent.isPresent()) {
				stopSpinner(); // Now have actual progress
			}
			this.percent = percent;
			if(message.isPresent()) {
				this.message = new Formattable(this.message == null ? Optional.of(Level.NORMAL) : this.message.level, message.get(), args);
			}
			printJob();
		}
	}

	protected void printSpinner(Sequence seq) {
		if(indeterminate) {
			if(spinner == null) {
				if(percentageText) {
					seq.fmt("%3d%%", percent.get());
				}
			}
			else {
				if(percentageText) {
					seq.msg("{0} {1}", Character.toString(spinnerChars[spinner.index]), String.format("%3d%%", percent.get()));
				}
				else {
					seq.msg(" {0}", Character.toString(spinnerChars[spinner.index]));
				}
			}
		}
		else {
			if(percentageText) {
				seq.fmt("%3d%%", percent.get());
			}
		}
	}

	protected void printIndent(Sequence seq) {
		if (indent > 1)
			seq.str("o ");
		else
			seq.str("* ");
	}

	protected void printMessage(Sequence seq, int availableWidth) {
		seq.str(message);
	}

	protected DefaultConsoleProgress createNewJob(Object lock, String name,  Object... args) {
		return new DefaultConsoleProgress(terminal, indeterminate, percentageText, lock, indent + 1, name, args);
	}

	void printJob() {
		synchronized (lock) {
			newlineNeeded = true;
			var width = terminal.getWidth();
			var seq = terminal.createSequence();
			seq.str(indentStr);
			if (indent > 0) {
				printIndent(seq);
			}
			var indentLen = seq.textLength();
			var spinnerSeq = terminal.createSequence();
			printSpinner(spinnerSeq);
			var len = spinnerSeq.textLength();
			if (message != null) {
				printMessage(seq, width - len - indentLen + (len > 0 ? 1 : 0));
			}
			if(len > 0)
				seq.ch(' ');
			seq.seq(spinnerSeq);
			seq.cr();
			var wrt = terminal.getWriter();
			wrt.print(seq);
			wrt.flush();
		}
	}

	void printNewline() {
		terminal.getWriter().println();
		stopSpinner();
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

	@Override
	public List<Progress> jobs() {
		return jobs;
	}

}
