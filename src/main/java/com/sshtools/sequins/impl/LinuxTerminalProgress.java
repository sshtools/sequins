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

import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Terminal;
import com.sshtools.sequins.Sequence.Color;

public class LinuxTerminalProgress extends FallbackConsoleProgress {

	LinuxTerminalProgress(Terminal terminal, boolean showSpinner, boolean percentageText, String name,
			Object... args) {
		this(terminal, showSpinner, percentageText, new Object(), 0, name, args);
	}

	protected LinuxTerminalProgress(Terminal terminal, boolean showSpinner, boolean percentageText, Object lock,
			int indent, String name, Object... args) {
		super(terminal, showSpinner, percentageText, new Object(), indent, name, "🕐🕐🕒🕓🕓🕓🕖🕗🕘🕙🕚🕛".codePoints().toArray(), args);
	}


	@Override
	protected FallbackConsoleProgress createNewJob(Object lock, String name, Object... args) {
		return new LinuxTerminalProgress((LinuxTerminal) terminal, indeterminate, percentageText, lock, indent() + 1, name, args);
	}

	@Override
	protected void printIndent(Sequence seq) {
		if (indent() > 1)
			seq.str("○ ");
		else
			seq.str("● ");
	}

	@Override
	protected void printMessage(Sequence seq, int availableWidth) {
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
	}

}
