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

import com.sshtools.sequins.Table.Alignment;

public class Cell<T extends Object> {
	private T value;
	private Alignment alignment;
	private boolean strong;
	private int maxWidth = -1;
	private int minWidth = 01;

	public Cell(T value) {
		this.value = value;
	}

	public boolean strong() {
		return strong;
	}

	public Cell<T> strong(boolean strong) {
		this.strong = strong;
		return this;
	}

	public Alignment alignment() {
		return alignment;
	}

	public Cell<T> alignment(Alignment alignment) {
		this.alignment = alignment;
		return this;
	}

	public T value() {
		return value;
	}

	public Cell<T> value(T value) {
		this.value = value;
		return this;
	}

	public Cell<T> maxWidth(int maxWidth) {
		this.maxWidth = maxWidth;
		return this;
	}
	
	public int maxWidth() {
		return maxWidth;
	}

	public Cell<T> minWidth(int minWidth) {
		this.minWidth = minWidth;
		return this;
	}
	
	public int minWidth() {
		return minWidth;
	}

}