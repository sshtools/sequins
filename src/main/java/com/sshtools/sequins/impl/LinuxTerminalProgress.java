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

import com.sshtools.sequins.DefaultConsoleProgress;
import com.sshtools.sequins.Sequence.Color;

public class LinuxTerminalProgress extends DefaultConsoleProgress {

	private final LinuxTerminal terminal;

	public LinuxTerminalProgress(LinuxTerminal terminal, String name, Object... args) {
		super(name, args);
		this.terminal = terminal;
		super.postConstruct();
	}


	protected LinuxTerminalProgress(LinuxTerminal terminal, Object lock, int indent, String name, Object... args) {
		super(lock, indent, name, args);
		this.terminal = terminal;
		super.postConstruct();
	}


	@Override
	protected void postConstruct() {
		setSpinnerChars("🕐🕐🕒🕓🕓🕓🕖🕗🕘🕙🕚🕛");
	}

	@Override
	protected DefaultConsoleProgress createNewJob(Object lock, String name, Object... args) {
		return new LinuxTerminalProgress(terminal, lock, indent() + 1, name, args);
	}

	@Override
	protected void printIndent() {
		var wrt = terminal.getWriter();
		if (indent() > 1)
			wrt.print("○ ");
		else
			wrt.print("● ");
		wrt.flush();
	}


	@Override
	protected void printName() {
		var seq = terminal.createSequence();
		if(indent() == 0)
			seq.fg(Color.YELLOW);
		seq.msg(name().pattern(), name().args());
		if(indent() == 0)
			seq.defaultFg();
		var wrt = terminal.getWriter();
		wrt.print(seq);
		wrt.flush();
	}

	@Override
	protected void printMessage() {
		var seq = terminal.createSequence();
		switch(message().level().orElse(Level.NORMAL)) {
		case ERROR:
			seq.fg(Color.RED);
			break;
		case WARNING:
			seq.fg(Color.BRIGHT_YELLOW);
			break;
		case INFO:
			seq.fg(Color.BRIGHT_BLUE);
			break;
		case VERBOSE:
			seq.boldOn();
			break;
		default:
			break;
		}
		seq.msg(message().pattern(), message().args());
		switch(message().level().orElse(Level.NORMAL)) {
		case ERROR:
			seq.defaultFg();
			break;
		case WARNING:
			seq.defaultFg();
			break;
		case INFO:
			seq.defaultFg();
			break;
		case VERBOSE:
			seq.boldOff();
			break;
		default:
			break;
		}
		var wrt = terminal.getWriter();
		wrt.print(seq);
		wrt.flush();
	}

}
