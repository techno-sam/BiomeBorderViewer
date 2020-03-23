package com.mrp_v2.biomeborderviewer.util;

public class LineData {
	public Vector3Float a, b;
	public Color color;

	public LineData(Vector3Float a, Vector3Float b) {
		this(a, b, null);
	}

	public LineData(Vector3Float a, Vector3Float b, Color color) {
		this.a = a;
		this.b = b;
		this.color = color;
	}

	public int hashCode() {
		return a.hashCode() * b.hashCode() - color.hashCode();
	}

	public boolean equals(LineData lineData) {
		return a.equals(lineData.a) && b.equals(lineData.b) && color.equals(lineData.color);
	}

	public boolean equals(Object obj) {
		if (obj instanceof LineData) {
			return equals((LineData) obj);
		}
		return false;
	}
}
