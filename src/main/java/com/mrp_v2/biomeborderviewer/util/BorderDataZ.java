package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Matrix4f;

public class BorderDataZ extends BorderDataBase {

	private final float x1, x2, y1, y2, z1, z2;

	private final int hashCode;

	public BorderDataZ(Int3 a, Int3 b, boolean similarBiome) {
		super(similarBiome);
		if (a.getZ() < b.getZ()) {
			z1 = b.getZ() - offset;
			z2 = b.getZ() + offset;
		} else {
			z1 = a.getZ() - offset;
			z2 = a.getZ() + offset;
		}
		x1 = a.getX();
		x2 = x1 + 1;
		y1 = a.getY();
		y2 = y1 + 1;
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
		// -z side
		drawer.DrawSegment(x1, y1, z1);
		drawer.DrawSegment(x1, y2, z1);
		drawer.DrawSegment(x2, y2, z1);
		drawer.DrawSegment(x2, y1, z1);
		// +z side
		drawer.DrawSegment(x1, y1, z2);
		drawer.DrawSegment(x2, y1, z2);
		drawer.DrawSegment(x2, y2, z2);
		drawer.DrawSegment(x1, y2, z2);
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!super.equals(obj)) {
			return false;
		}
		if (!(obj instanceof BorderDataZ)) {
			return false;
		}
		BorderDataZ other = (BorderDataZ) obj;
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
