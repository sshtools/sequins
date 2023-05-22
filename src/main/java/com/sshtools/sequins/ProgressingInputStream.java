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
		progress.progressed((int) ((((double) bytes) / (double) length) * (double) 100));
	}

}
