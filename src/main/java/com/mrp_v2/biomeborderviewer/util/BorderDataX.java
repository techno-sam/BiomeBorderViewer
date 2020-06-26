package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.util.math.vector.Matrix4f;

public class BorderDataX extends BorderDataBase {

	static public BorderDataX merge(BorderDataX a, BorderDataX b) {
		if (a.y1 == b.y1 && a.y2 == b.y2) {
			if (a.z1 == b.z2) {
				return new BorderDataX(a.similarBiome, a.x1, a.x2, a.y1, a.y2, b.z1, a.z2);
			} else {
				return new BorderDataX(a.similarBiome, a.x1, a.x2, a.y1, a.y2, a.z1, b.z2);
			}
		} else {
			if (a.y1 == b.y2) {
				return new BorderDataX(a.similarBiome, a.x1, a.x2, b.y1, a.y2, a.z1, a.z2);
			} else {
				return new BorderDataX(a.similarBiome, a.x1, a.x2, a.y1, b.y2, a.z1, a.z2);
			}
		}
	}

	static public BorderDataX newBorder(Int3 a, Int3 b, boolean similarBiome) {
		if (a.getX() < b.getX()) {
			return new BorderDataX(similarBiome, b.getX() - offset, b.getX() + offset, a.getY(), a.getY() + 1, a.getZ(),
					a.getZ() + 1);
		} else {
			return new BorderDataX(similarBiome, a.getX() - offset, a.getX() + offset, a.getY(), a.getY() + 1, a.getZ(),
					a.getZ() + 1);
		}
	}

	private BorderDataX(boolean similarBiome, float x1, float x2, float y1, float y2, float z1, float z2) {
		super(similarBiome, x1, x2, y1, y2, z1, z2);
	}

	@Override
	public boolean canMerge(BorderDataBase border) {
		if (!super.canMerge(border)) {
			return false;
		}
		if (!(border instanceof BorderDataX)) {
			return false;
		}
		BorderDataX other = (BorderDataX) border;
		if (equals(border)) {
			return true;
		}
		if (x1 != other.x1) {
			return false;
		}
		if (x2 != other.x2) {
			return false;
		}
		if (y1 == other.y1 && y2 == other.y2) {
			if (z1 == other.z2 || z2 == other.z1) {
				return true;
			}
		} else if (z1 == other.z1 && z2 == other.z2) {
			if (y1 == other.y2 || y2 == other.y1) {
				return true;
			}
		}
		return false;
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
		return true;
	}
}
