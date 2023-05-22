package com.sshtools.sequins;

import java.io.FilterInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ProgressingInputStream extends FilterInputStream {

	private final long length;
	private final Progress progress;
	private long bytes;

	public ProgressingInputStream(InputStream in, Progress progress, long length) {
		super(in);
		this.progress = progress;
		this.length = length;
	}

	@Override
	public int read() throws IOException {
		var r = super.read();
		if (r != -1) {
			progress(1);
		}
		return r;
	}

	@Override
	public int read(byte[] b, int off, int len) throws IOException {
		var r = super.read(b, off, len);
		if (r != -1) {
			progress(r);
		}
		return r;
	}

	void progress(int amount) throws IOException {
		bytes += amount;
		if(progress.isCancelled())
			throw new IOException("Cancelled.");
		progress.progressPercentage((int) ((((double) bytes) / (double) length) * (double) 100));
	}

}
