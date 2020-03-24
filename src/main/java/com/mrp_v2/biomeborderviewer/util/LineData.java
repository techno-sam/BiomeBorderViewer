package com.mrp_v2.biomeborderviewer.util;

public class LineData {
	public Int2Float1Combo a, b;
	public Color color;

	public LineData(Int2Float1Combo a, Int2Float1Combo b) {
		this.a = a;
		this.b = b;
	}

	public LineData(Int2Float1Combo a, Int2Float1Combo b, Color color) {
		this(a, b);
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
