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
import java.util.Optional;

import com.sshtools.sequins.Sequence.Shade;
import com.sshtools.sequins.Twidget.AbstractTwidget;

public class ProgressBar<N extends Number> extends AbstractTwidget {
	
	private Optional<N> value = Optional.empty();
	private Optional<N> max = Optional.empty();

	public ProgressBar(Sequins terminal) {
		super(terminal);
	}

	public Optional<N> getValue() {
		return value;
	}

	public void setValue(N value) {
		setValue(Optional.of(value));
	}

	public void setValue(Optional<N> value) {
		this.value = value;
	}

	public Optional<N> getMax() {
		return max;
	}
	
	public void setMax(N max) {
		setMax(Optional.of(max));
	}

	public void setMax(Optional<N> max) {
		this.max = max;
	}

	@Override
	public Sequence draw(DrawContext context, Sequence seq) throws IOException {
		if(context.getWidth() < 4) {
			seq.ch(context.getWidth(), ' ');
		}
		else {
			seq.ch('[');
			var available = context.getWidth() - 2;
			if(value.isPresent()) {
				var v = this.value.get().doubleValue();
				var m = this.max.orElse(this.value.get()).doubleValue();
				var cells = (int)(Math.round(( v / m ) * (double)available));
				seq.shade(cells, Shade.DARK_SHADE);
				seq.shade(available - cells, Shade.LIGHT_SHADE);
			}
			else {
				seq.shade(available, Shade.MEDIUM_SHADE);
			}
			seq.ch(']');
		}
		return seq;
	}
}
