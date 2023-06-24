package com.sshtools.sequins.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;

import org.jline.reader.Completer;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;

import com.sshtools.sequins.Constraint;
import com.sshtools.sequins.ProgressBuilder;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Sequins;

public class JLineTerminal extends AbstractTerminal implements Sequins {

	private org.jline.terminal.Terminal nativeTerm;
	private PrintWriter errWriter;

	public JLineTerminal() throws IOException {
		this(TerminalBuilder.builder().build());
	}

	public JLineTerminal(org.jline.terminal.Terminal nativeTerm) {
		this.nativeTerm = nativeTerm;
		errWriter = new PrintWriter(System.err, true);
	}

	@Override
	public Constraint constraint() {
		if(isDumb())
			return Constraint.of(132, 24);
		else
			return Constraint.of(nativeTerm.getWidth(), nativeTerm.getHeight());
	}

	boolean isDumb() {
		return nativeTerm.getType().equals(Terminal.TYPE_DUMB) || nativeTerm.getType().equals(Terminal.TYPE_DUMB_COLOR);
	}

	@Override
	public Sequence createSequence() {
		if(isDumb()) {
			return new Sequence() {
				@Override
				public Sequence cub(int repeat) {
					return this;
				}

				@Override
				public Sequence eraseLine() {
					return cr().ch(getWidth(), ' ').cr();
				}

				@Override
				public Sequence newSeq() {
					return createSequence();
				}
			};
		}
		else {
			return new Sequence() {

				@Override
				public String toString() {
					return buffer == null ? super.toString() : buffer.toAnsi(nativeTerm);
				}
	
				@Override
				public Sequence box(BoxChar ch) {
					switch (ch) {
					case BOX_TOP_LEFT:
						return cp(0x6c);
					case BOX_TOP:
						return cp(0x71);
					case BOX_TOP_MIDDLE:
						return cp(0x77);
					case BOX_TOP_RIGHT:
						return cp(0x6b);
					case BOX_MIDDLE_LEFT:
						return cp(0x74);
					case BOX_MIDDLE:
						return cp(0x71);
					case BOX_MIDDLE_MIDDLE:
						return cp(0x6e);
					case BOX_MIDDLE_RIGHT:
						return cp(0x75);
					case BOX_BOTTOM_LEFT:
						return cp(0x6d);
					case BOX_BOTTOM:
						return cp(0x71);
					case BOX_BOTTOM_MIDDLE:
						return cp(0x76);
					case BOX_BOTTOM_RIGHT:
						return cp(0x6a);
					case BOX_LEFT:
						return cp(0x78);
					case BOX_RIGHT:
						return cp(0x78);
					case BOX_CENTER:
						return cp(0x78);
					default:
						return ch('*');
					}
				}
	
				@Override
				public Sequence cub(int repeat) {
					return csi().num(repeat).ch('D');
				}
	
				@Override
				public Sequence eraseLine() {
					return csi().num(2).ch('K');
				}
	
				@Override
				public Sequence gr(boolean gr) {
					return esc().ch('(').ch(gr ? '0' : 'B');
				}
	
				@Override
				public Sequence newSeq() {
					return createSequence();
				}
	
				@Override
				public Sequence shade(int repeat, Shade ch) {
					switch (ch) {
					case DARK_SHADE:
						return ch(repeat, '▓');
					case MEDIUM_SHADE:
						return ch(repeat, '▒');
					default:
						return ch(repeat, '░');
					}
				}
			};
		}
	}

	@Override
	public PrintWriter getErrorWriter() {
		return errWriter;
	}

	@Override
	public PrintWriter getWriter() {
		return nativeTerm.writer();
	}

	@Override
	public final char[] password(PromptContext context, String fmt, Object... args) {
		return interruptSpinner(() -> passwordImpl(context, fmt, args));
	}

	@Override
	public final String prompt() {
		return interruptSpinner(() -> promptImpl());
	}

	@Override
	public final char[] password() {
		return interruptSpinner(() -> passwordImpl());
	}

	@Override
	public final String prompt(PromptContext context, String fmt, Object... args) {
		return interruptSpinner(() -> promptImpl(context, fmt, args));
	}

	@Override
	public Terminal terminal() {
		return nativeTerm;
	}

	@Override
	protected DumbConsoleProgress createProgress(ProgressBuilder builder) {
		if(isDumb()) {
			return new DumbConsoleProgress(this, builder.indeterminate(), builder.spinnerStartDelay(),
					builder.percentageText(), builder.message(), new int[] { '.' }, builder.args());
		}
		else {
			return new JLineTerminalProgress(JLineTerminal.this, builder.indeterminate(), builder.spinnerStartDelay(),
					builder.percentageText(), builder.message(), new int[] { '.' }, builder.args());
		}
	}

	private char[] passwordImpl() {
		try {
			return createLineReaderBuilder().build().readLine('*').toCharArray();
		} catch (Exception e) {
			return null;
		}
	}

	private char[] passwordImpl(PromptContext context, String fmt, Object... args) {
		try {
			var writer = getWriter();
			writer.print(createSequence().msg(fmt, args).str(": ").toString());
			writer.flush();
			return createLineReaderBuilder().build().readLine('*').toCharArray();
		} catch (Exception e) {
			return null;
		}
	}

	private String promptImpl() {
		return createLineReaderBuilder().build().readLine();
	}

	private String promptImpl(PromptContext context, String fmt, Object... args) {
		var bldr = createLineReaderBuilder();

		Optional<Completer> completer = context.attr(Completer.class.getName());
		completer.ifPresent(c -> bldr.completer(c));

		var reader = bldr.build();

		return reader.readLine(createSequence().msg(fmt, args).toString() + " ");

	}

	private LineReaderBuilder createLineReaderBuilder() {
		var bldr = LineReaderBuilder.builder().terminal(nativeTerm);
		return bldr;
	}
}
