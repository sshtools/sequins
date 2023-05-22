package com.sshtools.sequins;

import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public class InterruptableProgress implements Progress {

	private Thread thread;
	private boolean cancelled;
	private final Progress delegate;
	private List<Progress> jobs = new ArrayList<>();

	InterruptableProgress(Progress delegate) {
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
	public Progress parent() {
		return delegate.parent();
	}

	@Override
	public Progress newJob() {
		return delegate.newJob();
	}

	@Override
	public void close() {
		delegate.close();
	}

	@Override
	public void progressPercentage(int percent) {
		delegate.progressPercentage(percent);
	}

	@Override
	public void progressed(int percent, String message, Object... args) {
		delegate.progressed(percent, message, args);
	}

	@Override
	public void progressMessage(String message, Object... args) {
		delegate.progressMessage(message, args);
	}

	@Override
	public void progressPercentage(Optional<Integer> percent) {
		delegate.progressPercentage(percent);		
	}

	@Override
	public void progressMessage(Optional<String> message, Object... args) {
		delegate.progressMessage(message, args);		
	}

	@Override
	public void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		delegate.progressed(percent, message, args);
	}

	@Override
	public Progress newJob(String name, Object... args) {
		var j = new InterruptableProgress(delegate.newJob(name, args));
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
		} finally {
			try {
				Progress.super.cancel();
			} finally {
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

	public static void attachThread(Progress progress) {
		attachThread(progress, Thread.currentThread());
	}

	public static void attachThread(Progress progress, Thread thread) {
		if (progress instanceof InterruptableProgress) {
			var ip = (InterruptableProgress) progress;
			ip.thread = thread;
		} else
			throw new IllegalArgumentException(MessageFormat.format(
					"{0} implementation is not a {1}. Use {2}.withInterruptable() to wrap it first.",
					Progress.class.getName(), ProgressBuilder.class.getName(), Progress.class.getName()));
	}
	
	public static InterruptableProgress of(Progress delegate) {
		return new InterruptableProgress(delegate);
	}

}
