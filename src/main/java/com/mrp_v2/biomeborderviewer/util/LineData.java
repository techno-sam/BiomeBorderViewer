package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.Vec3d;

public class LineData {
	public final Vec3d a, b;
	public boolean similarTemperature;

	public LineData(Vec3d a, Vec3d b) {
		this.a = a;
		this.b = b;
	}

	public LineData(Vec3d a, Vec3d b, boolean similarTemperature) {
		this(a, b);
		this.similarTemperature = similarTemperature;
	}

	public int hashCode() {
		return a.hashCode() * b.hashCode();
	}

	public boolean equals(LineData lineData) {
		return a.equals(lineData.a) && b.equals(lineData.b) && similarTemperature == lineData.similarTemperature;
	}

	public boolean equals(Object obj) {
		if (obj instanceof LineData) {
			return equals((LineData) obj);
		}
		return false;
	}
}
