package com.mrp_v2.biomeborderviewer.util;

public class Color {
	public int r, g, b;

	public Color() {
		this(0, 0, 0);
	}

	public Color(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public void setColor(int r, int g, int b) {
		this.r = r;
		this.g = g;
		this.b = b;
	}

	public int hashCode() {
		return r * g * b;
	}

	public boolean equals(Color color) {
		return r == color.r && g == color.g && b == color.b;
	}

	public boolean equals(Object obj) {
		if (obj instanceof Color) {
			return equals((Color) obj);
		}
		return false;
	}
}
