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
