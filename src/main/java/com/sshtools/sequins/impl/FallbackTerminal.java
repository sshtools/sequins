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

import java.io.PrintWriter;

import com.sshtools.sequins.Progress;
import com.sshtools.sequins.ProgressBuilder;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Terminal;

public class FallbackTerminal implements Terminal {
	private final PrintWriter writer;
	private final PrintWriter errWriter;

	public FallbackTerminal() {
		writer = new PrintWriter(System.out, true);
		errWriter = new PrintWriter(System.err, true);
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
	public int getWidth() {
		return 132;
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
	public ProgressBuilder progressBuilder() {
		return new ProgressBuilder() {
			@Override
			protected Progress buildImpl() {
				return new DefaultConsoleProgress(FallbackTerminal.this, indeterminate, percentageText, message, args);
			}
		};
	}
}
