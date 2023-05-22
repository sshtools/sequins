package com.sshtools.sequins;

import java.util.List;
import java.util.Optional;

public class TimedProgress implements Progress {
	
	private final Progress delegate;
	private long started;

	TimedProgress(Progress delegate) {
		this.delegate = delegate;
		started = System.currentTimeMillis();
	}

	@Override
	public void close() {
		try {
			var now = System.currentTimeMillis();
			message(Level.INFO, "Task took {0}s", String.format("%f", (double)(now - started) / 1000.0d));
		}
		finally {
			delegate.close();
		}
		
	}

	@Override
	public boolean isCancelled() {
		return delegate.isCancelled();
	}

	@Override
	public List<Progress> jobs() {
		return delegate.jobs();
	}

	@Override
	public Progress newJob(String name, Object... args) {
		return delegate.newJob(name, args);
	}

	@Override
	public Progress newJob() {
		return delegate.newJob();
	}

	@Override
	public Progress parent() {
		return delegate.parent();
	}

	@Override
	public void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		delegate.progressed(percent, message, args);
	}

	@Override
	public void message(Level level, String message, Object... args) {
		delegate.message(level, message, args);
	}
	
	public static TimedProgress of(Progress delegate) {
		return new TimedProgress(delegate);
	}

	@Override
	public void progressPercentage(Optional<Integer> percent) {
		delegate.progressPercentage(percent);
		
	}

	@Override
	public void progressMessage(Optional<String> message, Object... args) {
		delegate.progressMessage(message, args);
	}
}
