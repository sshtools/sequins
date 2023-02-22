package com.sshtools.sequins;

import java.io.IOException;
import java.util.Optional;

import com.sshtools.sequins.Sequence.Shade;
import com.sshtools.sequins.Twidget.AbstractTwidget;

public class ProgressBar<N extends Number> extends AbstractTwidget {
	
	private Optional<N> value = Optional.empty();
	private Optional<N> max = Optional.empty();

	public ProgressBar(Terminal terminal) {
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
