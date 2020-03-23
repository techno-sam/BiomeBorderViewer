package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.BlockPos;

public class Vector3Float {
	public float x, y, z;

	public Vector3Float(float x, float y, float z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Vector3Float() {
		this(0, 0, 0);
	}
	
	public void roundXAndZ() {
		x = Math.round(x);
		z = Math.round(z);
	}
	
	public Vector3Float roundedXAndZ() {
		return new Vector3Float(Math.round(x), y, Math.round(z));
	}
	
	public static Vector3Float fromBlockPos(BlockPos pos) {
		return new Vector3Float(pos.getX(), pos.getY(), pos.getZ());
	}

	/**
	 * Ignores the y value
	 */
	public int hashCode() {
		return (int) (x * z);
	}

	/**
	 * Ignores the y value
	 * 
	 * @param vector
	 * @return
	 */
	public boolean equals(Vector3Float vector) {
		return x == vector.x && z == vector.z;
	}
	
	public boolean equals(Object obj) {
		if (obj instanceof Vector3Float) {
			return equals((Vector3Float)obj);
		}
		return false;
	}
}
