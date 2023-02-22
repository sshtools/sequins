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
