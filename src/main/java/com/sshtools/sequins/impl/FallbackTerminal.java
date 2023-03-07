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
import java.io.PrintWriter;
import java.lang.ProcessBuilder.Redirect;
import java.util.ArrayList;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;

import com.sshtools.sequins.Capability;
import com.sshtools.sequins.Constraint;
import com.sshtools.sequins.Progress;
import com.sshtools.sequins.ProgressBuilder;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Terminal;

public class FallbackTerminal implements Terminal {
	private final PrintWriter writer;
	private final PrintWriter errWriter;

	private final List<DefaultConsoleProgress> consoleProgress = Collections.synchronizedList(new ArrayList<>());
	private final boolean tty;

	public FallbackTerminal() {
		writer = new PrintWriter(System.out, true);
		errWriter = new PrintWriter(System.err, true);
		tty = isTty();
	}

	private boolean isTty() {
		try {
			return new ProcessBuilder("tty").redirectInput(Redirect.INHERIT).redirectError(Redirect.DISCARD).redirectOutput(Redirect.DISCARD).start().waitFor() == 0;
		} catch (IOException | InterruptedException e) {
			return false;
		}
	}

	@Override
	public final Set<Capability> capabilities() {
		var caps = new LinkedHashSet<Capability>();
		if(tty)
			caps.add(Capability.CURSOR_MOVEMENT);
		buildCaps(caps);
		return caps;
	}
	
	protected void buildCaps(Set<Capability> caps) {
	}

	@Override
	public PrintWriter getWriter() {
		return writer;
	}

	@Override
	public PrintWriter getErrorWriter() {
		return errWriter;
	}

	@Override
	public Constraint constraint() {
		return Constraint.of(132, 24);
	}

	@Override
	public Sequence createSequence() {
		return new Sequence() {

			@Override
			public Sequence newSeq() {
				return createSequence();
			}

			@Override
			public Sequence eraseLine() {
				return cr().ch(getWidth(), ' ').cr();
			}

		};
	}

	@Override
	public String prompt(String fmt, Object... args) {
		return interruptSpinner(() -> Terminal.super.prompt(fmt, args));
	}

	@Override
	public String prompt(PromptContext context, String fmt, Object... args) {
		return interruptSpinner(() -> Terminal.super.prompt(context, fmt, args));
	}

	@Override
	public char[] password(PromptContext context, String fmt, Object... args) {
		return interruptSpinner(() -> Terminal.super.password(context, fmt, args));
	}

	protected <T> T interruptSpinner(Callable<T> task) {
		var runningSpinners = new ArrayList<DefaultConsoleProgress>();
		synchronized (consoleProgress) {
			consoleProgress.forEach(p -> {
				p.interrupt(runningSpinners);
			});
		}
		try {
			try {
				return task.call();
			} catch (RuntimeException re) {
				throw re;
			} catch (Exception e) {
				throw new IllegalStateException("Failed to interrupt spinner.", e);
			}
		} finally {
			synchronized (consoleProgress) {
				runningSpinners.forEach(p -> p.startSpinner());
			}
		}
	}

	@Override
	public ProgressBuilder progressBuilder() {
		return new ProgressBuilder() {
			@Override
			protected Progress buildImpl() {
				var progress = new DefaultConsoleProgress(FallbackTerminal.this, indeterminate, percentageText, message,
						args) {
					@Override
					protected void onClose() {
						consoleProgress.remove(this);
					}
				};
				if(!tty) {
					progress.setSpinnerChars(new int[] {'.'});
				}
				consoleProgress.add(progress);
				return progress;
			}
		};
	}
}
