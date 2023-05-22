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

import java.text.MessageFormat;
import java.time.Duration;
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
		final boolean delay;

		Spinner(boolean delay) {
			super("ProgressSpinner");
			setDaemon(true);
			this.delay = delay;
		}

		public void run() {
			try {
				if(delay)
					Thread.sleep(spinnerStartDelay.toMillis());
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
	private boolean cancelled;
	private List<Progress> jobs = new ArrayList<>();
	private Optional<Runnable> onClose = Optional.empty();
	private DumbConsoleProgress parent;

	protected Duration spinnerStartDelay = Duration.ofSeconds(1);
	protected Object lock;
	protected Formattable message;
	protected boolean newlineNeededForNewMessage;
	protected StringBuilder indentStr = new StringBuilder();
	protected boolean firstMessage = true;
	protected boolean startOfLineNeeded;
	protected Spinner spinner;

	protected final Terminal terminal;
	protected final boolean indeterminate;
	protected final boolean percentageText;
	protected final int[] spinnerChars;

	private Optional<String> lastMessage = Optional.empty();
	private Optional<Integer> lastPercent = Optional.empty();
	private Object[] lastArgs = new Object[0];
	private int cursorY;
	private int absCursorY;

	DumbConsoleProgress(DumbConsoleProgress parent, Terminal terminal, boolean showSpinner, Duration spinnerStartDelay, boolean percentageText,
			String name, int[] spinnerChars, Object... args) {
		this(parent, terminal, showSpinner, spinnerStartDelay, percentageText, new Object(), 0, name, spinnerChars, args);
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

	protected DumbConsoleProgress(DumbConsoleProgress parent, Terminal terminal, boolean indeterminate, Duration spinnerStartDelay,
			boolean percentageText, Object lock, int indent, String name, int[] spinnerChars, Object... args) {
		cursorY = parent == null ? 0 : ((DumbConsoleProgress)parent.root()).absCursorY;
		this.parent = parent;
		this.terminal = terminal;
		this.lock = lock;
		this.spinnerChars = spinnerChars;
		this.indeterminate = indeterminate;
		this.percentageText = percentageText;
		this.message = name == null ? null : new Formattable(name, args);
		this.indent = indent;
		for (int i = 0; i < indent; i++)
			indentStr.append("   ");

		if (message == null)
			firstMessage = false;

		postConstruct();
	}

	protected void postConstruct() {
		if (message != null) {
			printJob();
			newlineNeededForNewMessage = true;
			message = null;
		}
		startSpinner(true);
	}

	public final int indent() {
		return indent;
	}

	@Override
	public final void close() {
		checkCursor();
		printNewlineIfNeeded();
		onClose.ifPresent(r -> r.run());
		onClosed();
	}

	protected void printNewlineIfNeeded() {
		stopSpinner(new ArrayList<>());
		synchronized (lock) {
			if (newlineNeededForNewMessage)
				printNewline();
		}
	}

	@Override
	public final void message(Level level, String message, Object... args) {
		var stopped = stopSpinner(new ArrayList<>());
		synchronized (lock) {
			this.message = new Formattable(Optional.of(level), message, args);
			try {
				if (newlineNeededForNewMessage)
					printNewline();
				checkFirstMessage();
				if (stopped)
					startSpinner(true);
				printJob();
				newlineNeededForNewMessage = true;
				if(!indeterminate)
					printNewline();
			} finally {
				this.message = null;
			}
		}
	}

	@Override
	public final Progress newJob(String name, Object... args) {
		var previousJob = jobs.isEmpty() ? null : jobs.get(jobs.size() - 1);
		stopSpinner(new ArrayList<>());
		if(previousJob instanceof DumbConsoleProgress && ((DumbConsoleProgress)previousJob).newlineNeededForNewMessage) {
			newlineNeededForNewMessage = true;
		}
		if (newlineNeededForNewMessage)
			printNewline();
		checkFirstMessage();
		var j = createNewJob(lock, name, args);
		jobs.add(j);
		return j;
	}

	@Override
	public final Progress newJob() {
		return newJob(null);
	}

	protected void startOfLine() {
		printNewline();
		startOfLineNeeded = false;
	}

	private void checkFirstMessage() {
		if (firstMessage) {
			indent++;
			indentStr.append("   ");
			firstMessage = false;
		}
	}

	@Override
	public final void progressPercentage(Optional<Integer> percent) {
		stopSpinner(new ArrayList<>());
		synchronized (lock) {
			checkCursor();
			doProgressed(percent, lastMessage, lastArgs);
		}
	}

	@Override
	public final void progressMessage(Optional<String> message, Object... args) {
		stopSpinner(new ArrayList<>());
		synchronized (lock) {
			checkCursor();
			doProgressed(lastPercent, message, args);
		}
	}

	@Override
	public final void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		stopSpinner(new ArrayList<>());
		synchronized (lock) {
			checkCursor();
			doProgressed(percent, message, args);
		}
	}

	@Override
	public Progress parent() {
		return parent;
	}

	void doProgressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		if (startOfLineNeeded) {
			if (message.isPresent() || (percentageText && percent.isPresent())) {
				startOfLine();
			}
		} else if (newlineNeededForNewMessage) {
			printNewline();
			
		}
		checkFirstMessage();
		this.percent = percent;
		if (message.isPresent()) {
			this.message = new Formattable(this.message == null ? Optional.of(Level.NORMAL) : this.message.level,
					message.get(), args);
		} else {
			if (!percentageText && !indeterminate) {
				return;
			}
		}

		try {
			printJob();
			startOfLineNeeded = newlineNeededForNewMessage = true;
		}
		finally {
			this.lastMessage = message;
			this.lastPercent = percent;
			this.lastArgs = args;
			this.message = null;
			this.percent = Optional.empty();
			startSpinner(false);
		}
	}

	void checkCursor() {
		if(terminal.capabilities().contains(Capability.CURSOR_MOVEMENT) && cursorY != ((DumbConsoleProgress)root()).absCursorY) {
			@SuppressWarnings("resource")
			var diff = cursorY - (((DumbConsoleProgress)root()).absCursorY);
			if(diff < 0) {
				terminal.getWriter().print(terminal.createSequence().cuu(-diff).cr().toString());
			}
			else if(diff > 0) {
				terminal.getWriter().print(terminal.createSequence().cud(diff).cr().toString());
			}
			((DumbConsoleProgress)root()).absCursorY = cursorY;
		}
	}

	protected void printPercentage(Sequence seq) {
		seq.fmt("%3d%%", percent.get());
	}

	protected void printSpinner(Sequence seq) {
		if (spinner != null) {
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
		return new DumbConsoleProgress(this, terminal, indeterminate, spinnerStartDelay, percentageText, lock, indent, name,
				spinnerChars, args);
	}

	protected void printJob() {
		synchronized (lock) {
			var width = terminal.getWidth();
			Sequence seq = terminal.createSequence();

			if (message == null) {
				if (percentageText && percent.isPresent()) {
					seq.str(indentStr);
					if (indent > 0) {
						printIndent(seq);
					}
					printPercentage(seq);
					if(indeterminate)
						seq.ch(' ');
				} else if (indeterminate) {
					printSpinner(seq);
				}
			} else {

				var tailSeq = terminal.createSequence();

				if (percentageText && percent.isPresent()) {
					tailSeq.ch(' ');
					printPercentage(tailSeq);
					if(indeterminate)
						tailSeq.ch(' ');
				} else if (indeterminate) {
					tailSeq.ch(' ');
					printSpinner(tailSeq);
				}

				seq = terminal.createSequence();
				seq.str(indentStr);
				if (indent > 0) {
					printIndent(seq);
				}
				printMessage(seq, width - tailSeq.textLength() - seq.textLength());
				seq.seq(tailSeq);
			}

			var wrt = terminal.getWriter();
			wrt.print(seq);
			wrt.flush();
		}
	}

	final void printNewline() {
		terminal.getWriter().println();
		cursorY++;
		((DumbConsoleProgress)root()).absCursorY = cursorY;
		stopSpinner(new ArrayList<>());
		startOfLineNeeded = newlineNeededForNewMessage = false;
	}

	final void startSpinner(boolean delay) {
		synchronized (lock) {
//			System.out.println("[Start Spinner]");
			if (spinner == null && indeterminate) {
				spinner = new Spinner(delay);
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
		if (newlineNeededForNewMessage) {
			printNewline();
		}
	}

	final boolean stopSpinner(List<DumbConsoleProgress> stopped) {
		if(this != root())
			return ((DumbConsoleProgress)root()).doStopSpinner(stopped);
		return doStopSpinner(stopped);
	}

	final boolean doStopSpinner(List<DumbConsoleProgress> stopped) {
//		try {
//			throw new Exception();
//		}
//		catch(Exception e) {
//			e.printStackTrace(System.out);
//			System.out.println("[Stop Spinner]");
//		}
		for (var j : jobs) {
			((DumbConsoleProgress) j).doStopSpinner(stopped);
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
//				if (message != null)
					printJob();
				return true;
			}
			return false;
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
	
	protected void onClosed() {
	}

}
