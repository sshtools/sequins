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

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.Callable;

import com.sshtools.sequins.Constraint;
import com.sshtools.sequins.Progress;
import com.sshtools.sequins.ProgressBuilder;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Sequins;

public abstract class AbstractTerminal implements Sequins {

	private final List<DumbConsoleProgress> consoleProgress = Collections.synchronizedList(new ArrayList<>());

	public AbstractTerminal() {
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

			@Override
			public Sequence cub(int repeat) {
				return this;
			}

		};
	}

	protected <T> T interruptSpinner(Callable<T> task) {
		var runningSpinners = new ArrayList<DumbConsoleProgress>();
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
				runningSpinners.forEach(p -> p.startSpinner(true));
			}
		}
	}

	@Override
	public final ProgressBuilder progressBuilder() {
		return new ProgressBuilder() {
			@Override
			protected Progress buildImpl() {
				var progress = createProgress(this);
				progress.setOnClose(() -> consoleProgress.remove(progress));
				consoleProgress.add(progress);
				return progress;
			}
		};
	}

	protected abstract DumbConsoleProgress createProgress(ProgressBuilder builder);
}
