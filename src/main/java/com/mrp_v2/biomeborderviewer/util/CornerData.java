package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.Vec3d;

public class CornerData {
	public Vec3d pos;
	public Color color;
	
	public boolean showPlusX = true, showMinusX = true, showPlusZ = true, showMinusZ = true;

	public CornerData() {
		this(null, null);
	}

	public CornerData(Vec3d position, Color color) {
		this.pos = position;
		this.color = color;
	}

	/*
	 * only factors position
	 */
	public int hashCode() {
		return pos.hashCode();
	}

	/*
	 * Only factors position
	 */
	public boolean equals(CornerData cornerData) {
		return pos.equals(cornerData.pos);
	}

	public boolean equals(Object obj) {
		if (obj instanceof CornerData) {
			return equals((CornerData) obj);
		}
		return false;
	}
	
	public void combine(CornerData other) {
		if (other.showMinusX == false) {
			showMinusX = false;
		}
		if (other.showPlusX == false) {
			showPlusX = false;
		}
		if (other.showPlusZ == false) {
			showPlusZ = false;
		}
		if (other.showMinusZ == false) {
			showMinusZ = false;
		}
	}
}
