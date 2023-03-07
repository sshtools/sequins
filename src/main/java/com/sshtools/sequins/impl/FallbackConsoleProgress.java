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

import com.sshtools.sequins.Capability;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Terminal;

public class FallbackConsoleProgress extends DumbConsoleProgress {

	static int[] SPINNER_CHARS = new int[] { '|', '/', '-', '\\', '|', '/', '-', '\\' };

	FallbackConsoleProgress(Terminal terminal, boolean showSpinner, boolean percentageText, String name,
			Object... args) {
		this(terminal, showSpinner, percentageText, new Object(), 0, name, args);
	}

	protected FallbackConsoleProgress(Terminal terminal, boolean showSpinner, boolean percentageText, Object lock,
			int indent, String name, int[] spinnerChars, Object... args) {
		super(terminal, showSpinner, percentageText, lock, indent, name, spinnerChars, args);
	}

	protected FallbackConsoleProgress(Terminal terminal, boolean showSpinner, boolean percentageText, Object lock,
			int indent, String name, Object... args) {
		this(terminal, showSpinner, percentageText, lock, indent, name, SPINNER_CHARS, args);
	}

	protected FallbackConsoleProgress createNewJob(Object lock, String name, Object... args) {
		return new FallbackConsoleProgress(terminal, indeterminate, percentageText, lock, indent() + 1, name, args);
	}

	@Override
	protected final void printJob() {
		synchronized (lock) {
			var width = terminal.getWidth();
			Sequence seq = null;
			var tailSeq = terminal.createSequence();

			if (indeterminate) {
				if (message != null) {
					tailSeq.ch(' ');
				}
				printSpinner(tailSeq);
			}
			if (percentageText) {
				if (tailSeq.textLength() > 0)
					tailSeq.ch(' ');
				printPercentage(null);
			}

			if (message == null) {
				seq = tailSeq;
			} else {
				seq = terminal.createSequence();
				seq.str(indentStr);
				if (indent() > 0) {
					printIndent(seq);
				}
				printMessage(seq, width - tailSeq.textLength() - seq.textLength());
				seq.seq(tailSeq);
			}

			if (terminal.capabilities().contains(Capability.CURSOR_MOVEMENT)) {
				seq.cr();
			}

			var wrt = terminal.getWriter();
			wrt.print(seq);
			newlineNeeded = true;
			wrt.flush();
		}
	}
}
