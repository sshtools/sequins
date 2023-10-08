package com.sshtools.sequins.impl;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.Optional;
import java.util.StringTokenizer;
import java.util.concurrent.atomic.AtomicInteger;

import org.jline.reader.Completer;
import org.jline.reader.LineReaderBuilder;
import org.jline.terminal.Cursor;
import org.jline.terminal.Terminal;
import org.jline.terminal.TerminalBuilder;
import org.jline.utils.InfoCmp.Capability;

import com.sshtools.sequins.BitmapBuilder;
import com.sshtools.sequins.Constraint;
import com.sshtools.sequins.ProgressBuilder;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Sequins;
import com.sshtools.sequins.Bitmap.ImageDisplayMethod;

public class JLineSequins extends AbstractTerminal implements Sequins {

	private static Optional<ImageDisplayMethod> cachedImageDisplayMode = null;
	private static Object detectLock = new Object();
	private final Terminal nativeTerm;
	private final PrintWriter errWriter;

	private final JLineSequins parent;
	private final Optional<Constraint> region;

	public JLineSequins() throws IOException {
		this(TerminalBuilder.builder().build());
	}

	public JLineSequins(Terminal nativeTerm) {
		this.nativeTerm = nativeTerm;
		errWriter = new PrintWriter(System.err, true);
		this.parent = null;
		region = Optional.empty();
	}

	private JLineSequins(Terminal nativeTerm, JLineSequins parent, Constraint region) {
		this.nativeTerm = nativeTerm;
		errWriter = new PrintWriter(System.err, true);
		this.parent = parent;
		this.region = Optional.of(region);
	}

	@Override
	public void clear() {
		region.ifPresentOrElse(r -> {
			var p = new AtomicInteger();
			Cursor c = nativeTerm.getCursorPosition(p::set);
			if (c != null) {
				nativeTerm.puts(Capability.cursor_address, constraint().y(), constraint().x());
				for (int i = 0; i < constraint().height(); i++) {
					nativeTerm.puts(Capability.delete_line);
				}
			}
		}, () -> {
			nativeTerm.puts(Capability.clear_screen);
		});
	}

	@Override
	public void close() {
		if (parent != null) {
			var region = parent.constraint();
			var p = new AtomicInteger();
			Cursor c = nativeTerm.getCursorPosition(p::set);
			if (c != null) {
				nativeTerm.puts(Capability.change_scroll_region, region.y(), region.y() + region.height() - 1);
				nativeTerm.puts(Capability.cursor_address, c.getY(), c.getX());
			}
		}
	}

	@Override
	public Constraint constraint() {
		return region.orElseGet(() -> {
			if (isDumb())
				return Constraint.of(132, 24);
			else
				return Constraint.of(nativeTerm.getWidth(), nativeTerm.getHeight());
		});
	}

	@Override
	public BitmapBuilder createBitmap() {
		return new JLineBitmapBuilder(this);
	}

	@Override
	public Sequence createSequence() {
		if (isDumb()) {
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
		} else {
			return new Sequence() {

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

				@Override
				public String toString() {
					return buffer == null ? super.toString() : buffer.toAnsi(nativeTerm);
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
	public Optional<ImageDisplayMethod> imageDisplayMethod() {
		if (cachedImageDisplayMode == null) {
			synchronized (detectLock) {
				if (cachedImageDisplayMode == null) {
					cachedImageDisplayMode = detectImageDisplayMethodImpl();
				}
			}
		}
		return cachedImageDisplayMode;
	}

	@Override
	public final char[] password() {
		return interruptSpinner(() -> passwordImpl());
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
	public final String prompt(PromptContext context, String fmt, Object... args) {
		return interruptSpinner(() -> promptImpl(context, fmt, args));
	}

	@Override
	public Terminal terminal() {
		return nativeTerm;
	}

	@Override
	public Sequins viewport(int height) {
		if (height < 1 || height > constraint().height()) {
			throw new IllegalArgumentException("Viewport must be less than terminal height and > 0.");
		}
		var p = new AtomicInteger();
		nativeTerm.enterRawMode();
		Cursor c = nativeTerm.getCursorPosition(p::set);
		if (c != null) {
			for (int i = 0; i < height - 1; i++) {
				newline();
			}
			nativeTerm.puts(Capability.change_scroll_region, c.getY(), c.getY() + height - 1);
			nativeTerm.puts(Capability.cursor_address, c.getY(), c.getX());
			nativeTerm.flush();
			return new JLineSequins(nativeTerm, this,
					Constraint.bound(constraint().x(), c.getY(), constraint().width(), height));
		}

		return new JLineSequins(nativeTerm, this,
				Constraint.bound(constraint().x(), c.getY(), constraint().width(), height));
	}

	@Override
	protected DumbConsoleProgress createProgress(ProgressBuilder builder) {
		if (isDumb()) {
			return new DumbConsoleProgress(this, builder.indeterminate(), builder.spinnerStartDelay(),
					builder.percentageText(), builder.message(), new int[] { '.' }, builder.args());
		} else {
			return new JLineProgress(JLineSequins.this, builder.indeterminate(), builder.spinnerStartDelay(),
					builder.percentageText(), builder.message(), new int[] { '.' }, builder.args());
		}
	}

	boolean isDumb() {
		return nativeTerm.getType().equals(Terminal.TYPE_DUMB) || nativeTerm.getType().equals(Terminal.TYPE_DUMB_COLOR);
	}

	private LineReaderBuilder createLineReaderBuilder() {
		var bldr = LineReaderBuilder.builder().terminal(nativeTerm);
		return bldr;
	}

	private Optional<ImageDisplayMethod> detectImageDisplayMethodImpl() {
		if (isDumb()) {
			return Optional.empty();
		} else if ("kitty".equals(System.getenv("SEQUINS_IMAGE_MODE"))
				|| "kitty".equals(System.getProperty("sequins.image.mode"))) {
			return Optional.of(ImageDisplayMethod.KITTY);
		} else if ("sixel".equals(System.getenv("SEQUINS_IMAGE_MODE"))
				|| "sixel".equals(System.getProperty("sequins.image.mode"))) {
			return Optional.of(ImageDisplayMethod.SIXEL);
		} else {

			var buf = new char[1024];
			nativeTerm.enterRawMode();
			var seq = createSequence().esc().rawStr("_Gi=31,s=1,v=1,a=q,t=d,f=24;AAAA").st().esc().rawStr("[c");
			print(seq.toString());

			try {
				nativeTerm.reader().readBuffered(buf, 500);
				var str = new String(buf);
				if (str.substring(1).startsWith("_G"))
					return Optional.of(ImageDisplayMethod.KITTY);
				else {
					var st = new StringTokenizer(str, ";");
					while (st.hasMoreTokens()) {
						var t = st.nextToken();
						if (t.equals("4")) {
							return Optional.of(ImageDisplayMethod.SIXEL);
						}
					}
				}
			} catch (Exception e) {
			}
			return Optional.empty();
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
}
