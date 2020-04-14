package com.mrp_v2.biomeborderviewer.util;

public class Color {
	public int r, g, b, a;

	public Color() {
		this(0, 0, 0, 255);
	}

	public Color(int r, int g, int b, int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}

	public int hashCode() {
		return r * g * b * a;
	}

	public boolean equals(Color color) {
		return r == color.r && g == color.g && b == color.b && a == color.a;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Color) {
			return equals((Color) obj);
		}
		return false;
	}

	public void set(int r, int g, int b, int a) {
		this.r = r;
		this.g = g;
		this.b = b;
		this.a = a;
	}
}
