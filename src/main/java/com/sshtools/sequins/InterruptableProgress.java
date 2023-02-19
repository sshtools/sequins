package com.sshtools.sequins;

import java.io.IOException;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterruptableProgress {

	private static final class IntrProgressImpl implements Progress {

		private Thread thread;
		private boolean cancelled;
		private final Progress delegate;
		private List<Progress> jobs = new ArrayList<>();

		IntrProgressImpl(Progress delegate) {
			this.delegate = delegate;
		}

		@Override
		public void error(String message, Object... args) {
			delegate.error(message, args);
		}

		@Override
		public void error(String message, Throwable exception, Object... args) {
			delegate.error(message, exception, args);
		}

		@Override
		public void close() throws IOException {
			delegate.close();
		}

		@Override
		public void progressed(int percent) {
			delegate.progressed(percent);
		}

		@Override
		public void progressed(String message, Object... args) {
			delegate.progressed(message, args);
		}

		@Override
		public void progressed(Optional<String> message, Optional<Integer> percent, Object... args) {
			delegate.progressed(message, percent, args);
		}

		@Override
		public Progress newJob(String name, Object... args) {
			var j = new IntrProgressImpl(delegate.newJob(name, args));
			jobs.add(j);
			return j;
		}

		@Override
		public void message(Level level, String message, Object... args) {
			delegate.message(level, message, args);
		}

		@Override
		public boolean isCancelled() {
			return cancelled;
		}

		@Override
		public final void cancel() {
			cancelled = true;
			try {
				delegate.cancel();
			}
			finally {
				try {
					Progress.super.cancel();
				}
				finally {
					if (thread != null) {
						thread.interrupt();
					}
				}
			}
		}

		@Override
		public List<Progress> jobs() {
			return jobs;
		}
	}

	public static void attachThread(Progress progress) {
		attachThread(progress, Thread.currentThread());
	}

	public static void attachThread(Progress progress, Thread thread) {
		if (progress instanceof IntrProgressImpl) {
			var ip = (IntrProgressImpl) progress;
			ip.thread = thread;
		} else
			throw new IllegalArgumentException(
					MessageFormat.format("{0} implementation is not a {1}. Use {2}.of() to wrap it first.",
							Progress.class.getName(), IntrProgressImpl.class.getName(), Progress.class.getName()));
	}

	public static Progress of(Progress progress) {
		return new IntrProgressImpl(progress);
	}
}
