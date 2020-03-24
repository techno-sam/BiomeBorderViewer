package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.BlockPos;

public class Float3 {
	public float x, y, z;

	public Float3(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Float3() {
	}
	
	public Float3 roundedXAndZ() {
		return new Float3(Math.round(x), y, Math.round(z));
	}
	
	public Float3(BlockPos pos) {
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
	}

	/**
	 * Ignores the y value
	 */
	public int hashCode() {
		return (int) (x * z);
	}

	/**
	 * Ignores the y value
	 */
	public boolean equals(Float3 vector) {
		return x == vector.x && z == vector.z;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Float3) {
			return equals((Float3)obj);
		}
		return false;
	}
}
