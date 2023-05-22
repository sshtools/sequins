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
import java.util.function.Consumer;

import com.sshtools.sequins.Twidget.AbstractTwidget;

public class Text<N extends Number> extends AbstractTwidget {
	
	private final Consumer<Sequence> text;

	public Text(Terminal terminal, String text) {
		this(terminal, (seq) -> seq.str(text));
	}

	public Text(Terminal terminal, Consumer<Sequence> text) {
		super(terminal);
		this.text = text;
	}

	@Override
	public Sequence draw(DrawContext context, Sequence seq) throws IOException {
		var iseq = getTerminal().createSequence();
		iseq.maxTextLength(context.constraint().width() - context.constraint().x());
		text.accept(iseq);
		seq.seq(iseq);
		return seq;
	}
}
