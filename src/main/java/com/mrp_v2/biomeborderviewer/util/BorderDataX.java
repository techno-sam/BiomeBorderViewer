package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Matrix4f;

public class BorderDataX extends BorderDataBase {

	private final float x1, x2, y1, y2, z1, z2;

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
}
