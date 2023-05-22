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
import java.util.ArrayList;
import java.util.List;

import com.sshtools.sequins.Twidget.AbstractTwidget;

public class Composition extends AbstractTwidget {

	private final static class TwidgetWrapper {
		TwidgetWrapper(Twidget widget, Constraint constraint) {
			this.widget = widget;
			this.constraint = constraint;
		}

		Twidget widget;
		Constraint constraint;
	}

	private final List<TwidgetWrapper> widgets = new ArrayList<>();

	public Composition(Terminal terminal) {
		super(terminal);
	}

	public Composition add(Twidget twidget, Constraint constraint) {
		widgets.add(new TwidgetWrapper(twidget, constraint));
		return this;
	}

	@Override
	public Sequence draw(DrawContext context, Sequence seq) throws IOException {
		for(var w : widgets) {
			w.widget.draw(new DrawContext() {
				@Override
				public Constraint constraint() {
					return w.constraint;
				}
			}, seq);
		}
		return seq;
	}

}
