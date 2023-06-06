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

import java.io.IOException;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;

public class ObservableProgress implements Progress {

	public interface Listener {
		void message(Progress progress, Level level, String message, Object... args);

		void newJob(Progress progress, Progress newJob, String name, Object... args);

		void progressed(Progress progress, Optional<Integer> percent, Optional<String> message, Object... args);

		void closed(Progress progress);
	}

	private final Progress delegate;
	private final Optional<ObservableProgress> root;;
	private final List<Listener> listeners = new CopyOnWriteArrayList<>();
	private final List<Progress> jobs = new CopyOnWriteArrayList<>();

	ObservableProgress(Progress delegate) {
		this.delegate = delegate;
		this.root = Optional.empty();
	}

	ObservableProgress(ObservableProgress root, Progress delegate) {
		this.root = Optional.of(root);
		this.delegate = delegate;
	}

	public ObservableProgress addListener(Listener listener) {
		if (root.isPresent())
			throw new IllegalStateException("Can only add a listener to the root progress.");
		this.listeners.add(listener);
		return this;
	}

	public ObservableProgress removeListener(Listener listener) {
		if (root.isPresent())
			throw new IllegalStateException("Can only remove a listener from the root progress.");
		this.listeners.remove(listener);
		return this;
	}

	@Override
	public void close() throws IOException {
		delegate.close();
		if (root.isEmpty()) {
			doClose(this);
		} else {
			root.get().doClose(this);
		}
	}

	void doClose(Progress progress) throws IOException {
		for (var l : listeners)
			l.closed(progress);
	}

	@Override
	public boolean isCancelled() {
		return delegate.isCancelled();
	}

	@Override
	public List<Progress> jobs() {
		return jobs;
	}

	@Override
	public Progress newJob(String name, Object... args) {
		var newJob = new ObservableProgress(this, delegate.newJob(name, args)) {
			@Override
			public void close() throws IOException {
				jobs.remove(this);
				super.close();
			}
		};
		jobs.add(newJob);
		if (root.isPresent()) {
			root.get().doNewJob(this, newJob, name, args);
		} else {
			doNewJob(this, newJob, name, args);
		}
		return newJob;
	}

	void doNewJob(Progress progress, Progress newJob, String name, Object... args) {
		for (var l : listeners) {
			l.newJob(progress, newJob, name, args);
		}
	}

	@Override
	public void progressed(Optional<Integer> percent, Optional<String> message, Object... args) {
		delegate.progressed(percent, message, args);
		if (root.isPresent()) {
			root.get().doProgressed(this, percent, message, args);
		} else {
			doProgressed(this, percent, message, args);
		}
	}

	void doProgressed(Progress progress, Optional<Integer> percent, Optional<String> message, Object... args) {
		for (var l : listeners) {
			l.progressed(progress, percent, message, args);
		}
	}

	@Override
	public void message(Level level, String message, Object... args) {
		delegate.message(level, message, args);
		if (root.isPresent()) {
			root.get().doMessage(this, level, message, args);
		} else {
			doMessage(this, level, message, args);
		}
	}

	void doMessage(Progress progress, Level level, String message, Object... args) {
		for (var l : listeners) {
			l.message(progress, level, message, args);
		}
	}

	public static ObservableProgress of(Progress delegate) {
		return new ObservableProgress(delegate);
	}

}
