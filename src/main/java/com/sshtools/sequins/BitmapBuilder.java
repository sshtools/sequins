package com.sshtools.sequins;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.UncheckedIOException;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Optional;

import com.sshtools.sequins.Bitmap.ImageDisplayMethod;
import com.sshtools.sequins.Bitmap.ImageSourceFormat;

public abstract class BitmapBuilder {
	private Optional<ImageSourceFormat> format = Optional.empty();
	
	protected Optional<Integer> width = Optional.empty();
	protected Optional<Integer> height = Optional.empty();
	protected Optional<ImageDisplayMethod> display = Optional.empty();
	protected boolean failIfUnspported;
	
	public BitmapBuilder withFailIfUnsupported() {
		return withFailIfUnsupported(true);
	}
	
	public BitmapBuilder withFailIfUnsupported(boolean failIfUnsupported) {
		this.failIfUnspported = failIfUnsupported;
		return this;
	}

	public BitmapBuilder withDisplay(ImageDisplayMethod display) {
		this.display = Optional.of(display);
		return this;
	}

	public BitmapBuilder withSourceWidth(int width) {
		this.width = Optional.of(width);
		return this;
	}

	public BitmapBuilder withFormat(ImageSourceFormat format) {
		this.format = Optional.of(format);
		return this;
	}

	public BitmapBuilder withSourceHeight(int height) {
		this.height = Optional.of(height);
		return this;
	}

	public BitmapBuilder withSourceSize(int width, int height) {
		return withSourceWidth(width).withSourceHeight(height);
	}

	public Bitmap build(String resource) {
		var cl = Thread.currentThread().getContextClassLoader();
		return build(resource, cl == null ? this.getClass().getClassLoader() : cl);
	}

	public Bitmap build(String resource, Class<?> base) {
		var res = base.getResource(resource);
		if (res == null)
			throw new UncheckedIOException(new FileNotFoundException(resource));
		return build(res);
	}

	public Bitmap build(String resource, ClassLoader loader) {
		var res = loader.getResource(resource);
		if (res == null)
			throw new UncheckedIOException(new FileNotFoundException(resource));
		return build(res);
	}

	public Bitmap build(URL url) {
		try {
			var conx = url.openConnection();
			if (conx == null)
				throw new FileNotFoundException(url.toString());
			try (var in = conx.getInputStream()) {
				return build(in, determineFormatFromMime(conx.getContentType())
						.orElse(determineFormat(url.getFile()).orElseThrow(() -> new IllegalStateException(
								"Image format could not be determined automatically so it must be speified."))));
			}
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public Bitmap build(File file) {
		return build(file.toPath());
	}

	public Bitmap build(Path file) {
		try (var in = Files.newInputStream(file)) {
			return build(in, format.orElseGet(() -> determineFormat(file).orElseThrow(() -> new IllegalStateException(
					"Image format could not be determined automatically so it must be speified."))));
		} catch (IOException ioe) {
			throw new UncheckedIOException(ioe);
		}
	}

	public Bitmap build(InputStream in) {
		return build(in, format
				.orElseThrow(() -> new IllegalStateException("Format must be specified when just using a stream.")));
	}

	private Optional<ImageSourceFormat> determineFormatFromMime(String mime) {
		if (mime != null && mime.equalsIgnoreCase("image/png"))
			return Optional.of(ImageSourceFormat.PNG);
		else
			return Optional.empty();
	}

	private Optional<ImageSourceFormat> determineFormat(Path path) {
		return determineFormat(path.getFileName().toString());
	}

	private Optional<ImageSourceFormat> determineFormat(String filenameWithExtension) {
		if (filenameWithExtension != null && filenameWithExtension.toLowerCase().endsWith(".png"))
			return Optional.of(ImageSourceFormat.PNG);
		else
			return Optional.empty();
	}

	protected abstract Bitmap build(InputStream in, ImageSourceFormat format);
}
