package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Matrix4f;

public class BorderDataY extends BorderDataBase {

	private final float x1, x2, y1, y2, z1, z2;

	public BorderDataY(Int3 a, Int3 b, boolean similarBiome) {
		super(similarBiome);
		if (a.getY() < b.getY()) {
			y1 = b.getY() - offset;
			y2 = b.getY() + offset;
		} else {
			y1 = a.getY() - offset;
			y2 = a.getY() + offset;
		}
		x1 = a.getX();
		x2 = x1 + 1;
		z1 = a.getZ();
		z2 = z1 + 1;
	}

	@Override
	public void draw(Matrix4f matrix, IVertexBuilder builder) {
		Drawer drawer = getDrawer(matrix, builder);
		// -y side
		drawer.DrawSegment(x1, y1, z1);
		drawer.DrawSegment(x2, y1, z1);
		drawer.DrawSegment(x2, y1, z2);
		drawer.DrawSegment(x1, y1, z2);
		// +y side
		drawer.DrawSegment(x1, y2, z1);
		drawer.DrawSegment(x1, y2, z2);
		drawer.DrawSegment(x2, y2, z2);
		drawer.DrawSegment(x2, y2, z1);
	}
}
