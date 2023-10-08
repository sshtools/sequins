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
package com.sshtools.sequins;

import java.io.IOException;
import java.io.UncheckedIOException;

public interface Twidget {
	
	public abstract class AbstractTwidget implements Twidget {
		
		private final Sequins terminal;
		
		AbstractTwidget(Sequins terminal) {
			this.terminal = terminal;
		}

		@Override
		public final Sequins getTerminal() {
			return terminal;
		}
	}
	
	Sequins getTerminal();

	default void draw() throws IOException {
		var trm = getTerminal();
		var seq = draw(trm, trm.createSequence());
		var wrt = trm.getWriter();
		wrt.write(seq.toString());
		wrt.flush();
	}
	
	default String drawToString(int width) {
		try {
			var trm = getTerminal();
			return draw(new DrawContext() {
				@Override
				public Constraint constraint() {
					return Constraint.of(width);
				}
			}, trm.createSequence()).toString();
		} catch (IOException e) {
			throw new UncheckedIOException(e.getMessage(), e);
		}
	}
	
	Sequence draw(DrawContext context, Sequence seq) throws IOException;
}
