package com.sshtools.sequins;

import java.io.IOException;
import java.io.UncheckedIOException;

public interface Twidget {
	
	public abstract class AbstractTwidget implements Twidget {
		
		private final Terminal terminal;
		
		AbstractTwidget(Terminal terminal) {
			this.terminal = terminal;
		}

		@Override
		public final Terminal getTerminal() {
			return terminal;
		}
	}
	
	Terminal getTerminal();

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
