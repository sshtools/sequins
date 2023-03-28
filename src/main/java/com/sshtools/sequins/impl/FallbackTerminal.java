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

import java.util.Set;

import com.sshtools.sequins.Capability;
import com.sshtools.sequins.ProgressBuilder;
import com.sshtools.sequins.Sequence;

public class FallbackTerminal extends DumbTerminal {

	public FallbackTerminal() {
	}

	@Override
	protected void buildCaps(Set<Capability> caps) {
		caps.add(Capability.CURSOR_MOVEMENT);
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
				return noTextAdvance(() -> ch(repeat, '\b'));
			}
		};
	}
	
	protected DumbConsoleProgress createProgress(ProgressBuilder builder) {
		return new FallbackConsoleProgress(this, builder.indeterminate(), builder.spinnerStartDelay(), builder.percentageText(),
				builder.message(), builder.args());
	}

}
