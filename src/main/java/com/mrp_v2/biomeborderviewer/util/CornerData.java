package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.Vec3d;

public class CornerData extends Vec3d {
	public boolean showPlusX = true, showMinusX = true, showPlusZ = true, showMinusZ = true;
	public boolean similarTemperature;

	public CornerData(Vec3d position) {
		this(position, true);
	}

	public CornerData(Vec3d position, boolean similarTemperature) {
		super(position.x, position.y, position.z);
		this.similarTemperature = similarTemperature;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof CornerData)) {
			return false;
		}
		return super.equals(obj);
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
