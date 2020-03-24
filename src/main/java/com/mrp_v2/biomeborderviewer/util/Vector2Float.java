package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.BlockPos;

public class Vector2Float {
	public float x, z;
	
	public Vector2Float(float x, float z) {
		this.x = x;
		this.z = z;
	}
	
	public Vector2Float() {
		
	}
	
	public static Vector2Float fromBlockPos(BlockPos pos) {
		return new Vector2Float(pos.getX(), pos.getZ());
	}
	
	public int hashCode() {
		return (int)x * (int)z;
	}
	
	public boolean equals(Vector2Float other) {
		return x == other.x && z == other.z;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Vector2Float) {
			return equals((Vector2Float)other);
		}
		return false;
	}
}
