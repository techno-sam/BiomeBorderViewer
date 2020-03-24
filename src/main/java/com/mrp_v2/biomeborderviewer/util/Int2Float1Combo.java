package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.BlockPos;

public class Int2Float1Combo {
	public int x, z;
	public float y;
	
	public Int2Float1Combo() {
		
	}
	
	public Int2Float1Combo(int x, float y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}
	
	public Int2Float1Combo(BlockPos pos) {
		x = pos.getX();
		y = pos.getY();
		z = pos.getZ();
	}
	
	public int hashCode() {
		return x * z * (int)y;
	}
	
	public boolean equals(Int2Float1Combo other) {
		return x == other.x && y == other.y && z == other.z;
	}
	
	public boolean equals(Object other) {
		if (other instanceof Int2Float1Combo) {
			return equals((Int2Float1Combo)other);
		}
		return false;
	}
}
