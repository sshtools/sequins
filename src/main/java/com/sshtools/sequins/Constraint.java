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