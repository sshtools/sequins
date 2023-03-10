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

import com.sshtools.sequins.Capability;
import com.sshtools.sequins.Progress;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Terminal;

public class DumbConsoleProgress implements Progress {

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
		boolean hadFirstSpin;

		Spinner() {
			super("ProgressSpinner");
			setDaemon(true);
		}

		public void run() {
			try {
				Thread.sleep(1000);
				while (active) {
					synchronized (lock) {
						printJob();
					}
					hadFirstSpin = true;
					index++;
					if (index == spinnerChars.length)
						index = 0;
					Thread.sleep(terminal.capabilities().contains(Capability.CURSOR_MOVEMENT) ? 100 : 1000);
				}
			} catch (InterruptedException ie) {
			}
		}
	}

	static int[] SPINNER_CHARS = new int[] { '.' };

	private int indent = 0;
	private Optional<Integer> percent = Optional.empty();
	private Spinner spinner;
	private boolean cancelled;
	private List<Progress> jobs = new ArrayList<>();
	private Optional<Runnable> onClose = Optional.empty();
	
	protected Object lock;
	protected Formattable message;
	protected boolean newlineNeeded;
	protected StringBuilder indentStr = new StringBuilder();

	protected final Terminal terminal;
	protected final boolean indeterminate;
	protected final boolean percentageText;
	protected final int[] spinnerChars;

	DumbConsoleProgress(Terminal terminal, boolean showSpinner, boolean percentageText, String name,int[] spinnerChars, 
			Object... args) {
		this(terminal, showSpinner, percentageText, new Object(), 0, name, spinnerChars, args);
	}
	
	void setOnClose(Runnable onClose) {
		this.onClose = Optional.of(onClose);
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

	protected DumbConsoleProgress(Terminal terminal, boolean showSpinner, boolean percentageText, Object lock,
			int indent, String name, int[] spinnerChars, Object... args) {
		this.terminal = terminal;
		this.lock = lock;
		this.spinnerChars = spinnerChars;
		this.indeterminate = showSpinner;
		this.percentageText = percentageText;
		this.message = name == null ? null : new Formattable(name, args);
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

	public final int indent() {
		return indent;
	}

	@Override
	public final void close() throws IOException {
		stopSpinner(new ArrayList<>());
		synchronized (lock) {
			var wasNlNeeded = newlineNeeded;
			if (wasNlNeeded)
				printNewline();
		}
		onClose.ifPresent(r -> r.run());
	}

	@Override
	public final void message(Level level, String message, Object... args) {
		stopSpinner(new ArrayList<>());
		synchronized (lock) {
			this.message = new Formattable(Optional.of(level), message, args);
			try {
				if (newlineNeeded)
					printNewline();
				printJob();
			} finally {
				this.message = null;
			}
		}
	}

	@Override
	public final Progress newJob(String name, Object... args) {
		stopSpinner(new ArrayList<>());
		if (newlineNeeded)
			printNewline();
		var j = createNewJob(lock, name, args);
		jobs.add(j);
		return j;
	}

	@Override
	public final void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		if (percent.isPresent()) {
			stopSpinner(new ArrayList<>()); // Now have actual progress
		}
		synchronized (lock) {
			this.percent = percent;
			if (message.isPresent()) {
				this.message = new Formattable(this.message == null ? Optional.of(Level.NORMAL) : this.message.level,
						message.get(), args);
			}
			printJob();
		}
	}

	protected void printPercentage(Sequence seq) {
		if (spinner == null) {
			seq.str("    ");
		} else {
			seq.fmt("%3d%%", percent.get());
		}
	}

	protected void printSpinner(Sequence seq) {
		if (spinner == null) {
			seq.ch(' ');
		} else {
			seq.msg("{0}", Character.toString(spinnerChars[spinner.index]));
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

	protected DumbConsoleProgress createNewJob(Object lock, String name, Object... args) {
		return new DumbConsoleProgress(terminal, indeterminate, percentageText, lock, indent + 1, name, spinnerChars, args);
	}

	protected void printJob() {
		synchronized (lock) {
			var width = terminal.getWidth();
			Sequence seq = null;
			
			if(newlineNeeded) {
				seq = terminal.createSequence();
				printSpinner(seq);
			}
			else {
				var tailSeq = terminal.createSequence();
				
				if (percentageText) {
					tailSeq.ch(' ');
					printPercentage(null);
				}
				else if (indeterminate) {
					printSpinner(tailSeq);
				}
	
				if (message == null) {
					seq = tailSeq;
				} else {
					seq = terminal.createSequence();
					seq.str(indentStr);
					if (indent > 0) {
						printIndent(seq);
					}
					printMessage(seq, width - tailSeq.textLength() - seq.textLength());
					seq.seq(tailSeq);
				}
			}

			var wrt = terminal.getWriter();
			wrt.print(seq);
			newlineNeeded = true;
			wrt.flush();
		}
	}

	final void printNewline() {
		terminal.getWriter().println();
		stopSpinner(new ArrayList<>());
		newlineNeeded = false;
	}

	final void startSpinner() {
		synchronized (lock) {
			if (spinner == null) {
				spinner = new Spinner();
				spinner.start();
			}
		}
	}

	final boolean isSpinning() {
		synchronized (lock) {
			if (spinner != null && spinner.isAlive()) {
				return true;
			}
			for (var j : jobs) {
				if (((DumbConsoleProgress) j).isSpinning()) {
					return true;
				}
			}
			return false;
		}
	}

	final void interrupt(List<DumbConsoleProgress> stopped) {

		if (isSpinning()) {
			stopSpinner(stopped);
		}
		for (var j : jobs) {
			((DumbConsoleProgress) j).interrupt(stopped);
		}
		if (newlineNeeded) {
			printNewline();
		}
	}

	final void stopSpinner(List<DumbConsoleProgress> stopped) {
		for (var j : jobs) {
			((DumbConsoleProgress) j).stopSpinner(stopped);
		}
		synchronized (lock) {
			if (spinner != null) {
				spinner.active = false;
				spinner.interrupt();
				try {
					spinner.join();
				} catch (InterruptedException e) {
					throw new IllegalStateException("Interrupted.", e);
				} finally {
					stopped.add(this);
					spinner = null;
				}
				printJob();
			}
		}
	}

	final String trimAndFillTo(String str, int size, char fillWith) {
		if (str.length() > size)
			str = str.substring(0, size);
		while (str.length() < size) {
			str = str.concat(String.valueOf(fillWith));
		}
		return str;
	}

	@Override
	public final List<Progress> jobs() {
		return jobs;
	}

}
