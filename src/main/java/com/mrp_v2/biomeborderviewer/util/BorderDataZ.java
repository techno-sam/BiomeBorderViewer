package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;

import net.minecraft.client.renderer.Matrix4f;

public class BorderDataZ extends BorderDataBase {

	private final float x1, x2, y1, y2, z1, z2;

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
}
