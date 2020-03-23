package com.mrp_v2.biomeborderviewer.util;

public class CornerData {
	public Vector3Float pos;
	public Color color;
	
	public boolean showPlusX = true, showMinusX = true, showPlusZ = true, showMinusZ = true;

	public CornerData() {
		this(null, null);
	}

	public CornerData(Vector3Float position, Color color) {
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
	
	public void ignoreSides(CornerData other) {
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
