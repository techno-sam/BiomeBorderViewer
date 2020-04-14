package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.Vec3d;

public class LineData {
	public Vec3d a, b;
	public Color color;

	public LineData(Vec3d a, Vec3d b) {
		this.a = a;
		this.b = b;
	}

	public LineData(Vec3d a, Vec3d b, Color color) {
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

	public LineData clone() {
		return new LineData(UtilityMethods.clone(a), UtilityMethods.clone(b), color.clone());
	}
}
