package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;

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

	public Int3(float x, float y, float z) {
		this(Math.round(x), Math.round(y), Math.round(z));
	}

	public Int3(Vec3d pos) {
		this(pos.x, pos.y, pos.z);
	}

	public Int3(double x, double y, double z) {
		this((float) x, (float) y, (float) z);
	}

	public Int3 add(int xIn, int yIn, int zIn) {
		if (xIn == 0 && yIn == 0 && zIn == 0) {
			return this;
		} else {
			return new Int3(x + xIn, y + yIn, z + zIn);
		}
	}

	public BlockPos toBlockPos() {
		return new BlockPos(x, y, z);
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

	public int axisDistance(Int3 other) {
		return Math.max(Math.max(Math.abs(x - other.x), Math.abs(y - other.y)), Math.abs(z - other.z));
	}
}
