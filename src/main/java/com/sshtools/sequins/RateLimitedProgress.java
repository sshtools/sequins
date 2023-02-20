package com.sshtools.sequins;

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RateLimitedProgress implements Progress {

	private final long ms;
	private final ScheduledExecutorService executor;
	private final Progress delegate;
	private final boolean root;
	private ScheduledFuture<?> task;
	private Optional<String> message;
	private Optional<Integer> percent;
	private Object[] args;
	private final Object lock = new Object();

	RateLimitedProgress(Progress delegate, long ms) {
		this.delegate = delegate;
		this.ms = ms;
		this.root = true;
		executor = Executors.newScheduledThreadPool(1);
	}

	RateLimitedProgress(RateLimitedProgress root, Progress delegate) {
		this.delegate = delegate;
		this.executor = root.executor;
		this.ms = root.ms;
		this.root = false;
	}

	@Override
	public void close() throws IOException {
		finishTask();
		try {
			delegate.close();
		}
		finally {
			if(root) {
				executor.shutdown();
			}
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
		finishTask();
		return new RateLimitedProgress(this, delegate.newJob(name, args));
	}

	@Override
	public void progressed(Optional<String> message, Optional<Integer> percent, Object... args) {
		synchronized (lock) {
			this.message = message;
			this.percent = percent;
			this.args = args;
		}
		if (task == null || task.isDone()) {
			task = executor.schedule(() -> {
				Optional<String> fmsg;
				Optional<Integer> fpc;
				Object[] fargs;
				synchronized (lock) {
					fmsg = RateLimitedProgress.this.message;
					fpc = RateLimitedProgress.this.percent;
					fargs = RateLimitedProgress.this.args;
				}
				delegate.progressed(fmsg, fpc, fargs);
			}, ms, TimeUnit.MILLISECONDS);
		}

	}

	@Override
	public void message(Level level, String message, Object... args) {
		finishTask();
		delegate.message(level, message, args);
	}
	
	public static RateLimitedProgress of(Progress delegate, long ms) {
		return new RateLimitedProgress(delegate, ms);
	}

	private void finishTask() {
		if (task != null) {
			try {
				task.get();
			} catch (InterruptedException | ExecutionException e) {
			}
		}
	}

}
