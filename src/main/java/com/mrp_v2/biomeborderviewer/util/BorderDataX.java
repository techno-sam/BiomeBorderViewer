package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Matrix4f;

public class BorderDataX extends BorderDataBase {

	private final float x1, x2, y1, y2, z1, z2;

	private final int hashCode;

	public BorderDataX(Int3 a, Int3 b, boolean similarBiome) {
		super(similarBiome);
		if (a.getX() < b.getX()) {
			x1 = b.getX() - offset;
			x2 = b.getX() + offset;
		} else {
			x1 = a.getX() - offset;
			x2 = a.getX() + offset;
		}
		y1 = a.getY();
		y2 = y1 + 1;
		z1 = a.getZ();
		z2 = z1 + 1;
		// hash code
		final int prime = 31;
		int result = super.hashCode();
		result = prime * result + Float.floatToIntBits(x1);
		result = prime * result + Float.floatToIntBits(x2);
		result = prime * result + Float.floatToIntBits(y1);
		result = prime * result + Float.floatToIntBits(y2);
		result = prime * result + Float.floatToIntBits(z1);
		result = prime * result + Float.floatToIntBits(z2);
		hashCode = result;
	}

	@Override
	public void draw(Matrix4f matrix, IVertexBuilder builder) {
		Drawer drawer = getDrawer(matrix, builder);
		// -x side
		drawer.DrawSegment(x1, y1, z1);
		drawer.DrawSegment(x1, y1, z2);
		drawer.DrawSegment(x1, y2, z2);
		drawer.DrawSegment(x1, y2, z1);
		// +x side
		drawer.DrawSegment(x2, y1, z1);
		drawer.DrawSegment(x2, y2, z1);
		drawer.DrawSegment(x2, y2, z2);
		drawer.DrawSegment(x2, y1, z2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof BorderDataX)) {
			return false;
		}
		BorderDataX other = (BorderDataX) obj;
		if (Float.floatToIntBits(x1) != Float.floatToIntBits(other.x1)) {
			return false;
		}
		if (Float.floatToIntBits(x2) != Float.floatToIntBits(other.x2)) {
			return false;
		}
		if (Float.floatToIntBits(y1) != Float.floatToIntBits(other.y1)) {
			return false;
		}
		if (Float.floatToIntBits(y2) != Float.floatToIntBits(other.y2)) {
			return false;
		}
		if (Float.floatToIntBits(z1) != Float.floatToIntBits(other.z1)) {
			return false;
		}
		if (Float.floatToIntBits(z2) != Float.floatToIntBits(other.z2)) {
			return false;
		}
		return true;
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
