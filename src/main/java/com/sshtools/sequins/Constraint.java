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

public final class Constraint {
	private int x;
	private int y;
	private int width;
	private int height;

	private Constraint() {
	}

	public static Constraint at(int x, int y) {
		return new Constraint().x(x).y(y);
	}

	public static Constraint of(int width) {
		return of(width, Integer.MAX_VALUE);
	}

	public static Constraint of(int width, int height) {
		return new Constraint().width(width).height(height);
	}

	public static Constraint bound(int x, int y, int width, int height) {
		return new Constraint().x(x).y(y).width(width).height(height);
	}

	public Constraint x(int x) {
		this.x = x;
		return this;
	}

	public int x() {
		return x;
	}

	public Constraint y(int y) {
		this.y = y;
		return this;
	}

	public int y() {
		return y;
	}

	public Constraint width(int width) {
		this.width = width;
		return this;
	}

	public int width() {
		return width;
	}

	public Constraint height(int height) {
		this.height = height;
		return this;
	}

	public int height() {
		return height;
	}
}