package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.BlockPos;

public class Int3 {

	private final int x, y, z;

	public Int3(int x, int y, int z) {
		this.x = x;
		this.y = y;
		this.z = z;
	}

	public Int3(Int3 source) {
		x = source.x;
		y = source.y;
		z = source.z;
	}

	public Int3(BlockPos pos) {
		this(pos.getX(), pos.getY(), pos.getZ());
	}

	public Int3 add(int x, int y, int z) {
		if (x == 0 && y == 0 && z == 0) {
			return this;
		} else {
			return new Int3(this.getX() + x, this.getY() + y, this.getZ() + z);
		}
	}

	public BlockPos toBlockPos() {
		return new BlockPos(getX(), getY(), getZ());
	}

	public int getX() {
		return x;
	}

	public int getY() {
		return y;
	}

	public int getZ() {
		return z;
	}
}
