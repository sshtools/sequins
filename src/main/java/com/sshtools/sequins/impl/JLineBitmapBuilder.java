package com.sshtools.sequins.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.util.Base64;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.atomic.AtomicInteger;

import com.sshtools.jsixel.lib.bitmap.Bitmap2Sixel;
import com.sshtools.jsixel.lib.bitmap.FormatType;
import com.sshtools.jsixel.lib.bitmap.PixelFormat;
import com.sshtools.jsixel.lib.bitmap.BitmapLoader.ImageType;
import com.sshtools.jsixel.lib.bitmap.Bitmap2Sixel.Bitmap2SixelBuilder;
import com.sshtools.jsixel.lib.bitmap.RawBitmap.RawBitmapBuilder;
import com.sshtools.sequins.Bitmap;
import com.sshtools.sequins.Bitmap.ImageSourceFormat;
import com.sshtools.sequins.BitmapBuilder;
import com.sshtools.sequins.DrawContext;
import com.sshtools.sequins.Sequence;
import com.sshtools.sequins.Sequins;

public final class JLineBitmapBuilder extends BitmapBuilder {

	static void packet(Sequence seq, Map<String, Object> parms) {
		packet(seq, parms, null, 0, 0);
	}

	static void packet(Sequence seq, Map<String, Object> parms, byte[] data, int off, int len) {
		seq.esc();
		seq.str("_G");
		var idx = new AtomicInteger();
		parms.forEach((k, v) -> {
			if (idx.getAndIncrement() > 0)
				seq.ch(',');
			seq.str(k);
			seq.ch('=');
			seq.str(v);
		});
		if (data != null) {
			seq.ch(';');
			if (len == data.length && off == 0)
				seq.rawStr(Base64.getEncoder().encodeToString(data));
			else {
				var arr = new byte[len];
				System.arraycopy(data, off, arr, 0, len);
				seq.rawStr(Base64.getEncoder().encodeToString(arr));
			}
		}
		seq.esc();
		seq.ch('\\');
	}

	private class KittyBitmap implements Bitmap {

		private final JLineSequins terminal;
		private final ImageSourceFormat format;
		private final byte[] data;
		private Optional<Integer> height;
		private Optional<Integer> width;

		KittyBitmap(JLineSequins terminal, InputStream in, ImageSourceFormat format, Optional<Integer> width,
				Optional<Integer> height) throws IOException {
			this.terminal = terminal;
			this.format = format;
			this.width = width;
			this.height = height;

			var bout = new ByteArrayOutputStream();
			try {
				in.transferTo(bout);
			} finally {
				in.close();
			}
			data = bout.toByteArray();
		}

		@Override
		public Sequins getTerminal() {
			return terminal;
		}

		@Override
		public Sequence draw(DrawContext context, Sequence seq) throws IOException {
			var pos = 0;
			while (pos < data.length) {
				var len = Math.min(4096, data.length - pos);
				var prms = new LinkedHashMap<String, Object>();

				if (pos == 0) {
					prms.put("a", 'T'); // TODO re-using placement
					switch (format) {
					case PNG:
						prms.put("f", 100);
						break;
					case RGB:
						if (!width.isPresent() || !height.isPresent())
							throw new IllegalStateException("Width and height must be specified for RGB images.");
						prms.put("f", 24);
						break;
					case RGBA:
						if (!width.isPresent() || !height.isPresent())
							throw new IllegalStateException("Width and height must be specified for RGBA images.");
						prms.put("f", 32);
						break;
					default:
						throw new UnsupportedOperationException();
					}
					width.ifPresent(w -> prms.put("s", w));
					height.ifPresent(w -> prms.put("v", w));
				}

				if (data.length - pos > 4096)
					prms.put("m", 1);
				else
					prms.put("m", 0);

				packet(seq, prms, data, pos, len);

				pos += len;
			}
			return seq;
		}

	}
	private class JSixelBitmap implements Bitmap {

		private final JLineSequins terminal;
		private final Bitmap2Sixel converter;

		JSixelBitmap(JLineSequins terminal, InputStream in, ImageSourceFormat format, Optional<Integer> width,
				Optional<Integer> height) throws IOException {
			this.terminal = terminal;
			
			switch (format) {
			case RGB:
				var bpp = 24;
				var fmt = PixelFormat.RGB888;
			case RGBA: 
				bpp = 32;
				fmt = PixelFormat.RGBA8888;
				converter = new Bitmap2SixelBuilder().
						fromBitmap(new RawBitmapBuilder().
								fromStream(in).
								withHeight(height.orElseThrow(() -> new IllegalStateException("Height must be supplied."))).
								withWidth(height.orElseThrow(() -> new IllegalStateException("Width must be supplied."))).
								withBitsPerPixel(bpp).
								withPixelFormat(fmt).
								withFormatType(FormatType.COLOR).
								build()).
						build();
				break;
			default:
				try {
					converter = new Bitmap2SixelBuilder().
							withType(ImageType.valueOf(format.name())).
							fromStream(in).
							build();
				} finally {
					in.close();
				}
				break;
			}
		}

		@Override
		public Sequins getTerminal() {
			return terminal;
		}

		@Override
		public Sequence draw(DrawContext context, Sequence seq) throws IOException {
			converter.write(seq.channel());
			return seq;
		}

	}

	private class EmptyBitmap implements Bitmap {

		private final JLineSequins terminal;

		EmptyBitmap(JLineSequins terminal) {
			this.terminal = terminal;
		}

		@Override
		public Sequins getTerminal() {
			return terminal;
		}

		@Override
		public Sequence draw(DrawContext context, Sequence seq) throws IOException {
			return seq;
		}

	}

	private JLineSequins terminal;

	JLineBitmapBuilder(JLineSequins terminal) {
		this.terminal = terminal;
	}

	@Override
	public Bitmap build(InputStream in, ImageSourceFormat format) {
		var display = this.display.or(() -> terminal.imageDisplayMethod());
		if (display.isEmpty()) {
			if (failIfUnspported)
				throw new IllegalStateException(
						"This device cannot positively be identified as supporting Kitty or Sixel graphics protocols.");
		} else {
			switch (display.get()) {
			case KITTY:
				return kittyImage(in, format);
			case SIXEL:
				return sixelImage(in, format);
			}
		}
		return new EmptyBitmap(terminal);
	}

	private Bitmap kittyImage(InputStream in, ImageSourceFormat format) {
		try {
			return new KittyBitmap(terminal, in, format, width, height);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

	private Bitmap sixelImage(InputStream in, ImageSourceFormat format) {
		try {
			//return new SixelBitmap(terminal, in, format, width, height);
			return new JSixelBitmap(terminal, in, format, width, height);
		} catch (IOException e) {
			throw new UncheckedIOException(e);
		}
	}

}
