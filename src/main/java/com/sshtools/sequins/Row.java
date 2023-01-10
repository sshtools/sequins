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

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.stream.Collectors;

import com.sshtools.sequins.Table.Alignment;

public class Row extends ArrayList<Cell<?>> {
	private static final long serialVersionUID = 1L;

	private Alignment defaultAlignment = Alignment.LEFT;
	private boolean strong;

	public Row(String... cells) {
		this(Arrays.asList(cells).stream().map(c -> new Cell<String>(c)).collect(Collectors.toList()));
	}

	public Row(Cell<?>... cells) {
		this(Arrays.asList(cells));
	}

	public Row(int cells) {
		for (int i = 0; i < cells; i++) {
			add(new Cell<String>(""));
		}
	}

	public Row(Collection<Cell<?>> cells) {
		addAll(cells);
	}
	

	public boolean strong() {
		return strong;
	}

	public Row strong(boolean strong) {
		this.strong = strong;
		return this;
	}

	public Alignment defaultAlignment() {
		return defaultAlignment;
	}

	public Row defaultAlignment(Alignment defaultAlignment) {
		this.defaultAlignment = defaultAlignment;
		return this;
	}
}