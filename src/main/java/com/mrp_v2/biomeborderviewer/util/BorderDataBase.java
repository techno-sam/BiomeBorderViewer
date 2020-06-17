package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.client.renderer.Matrix4f;

public abstract class BorderDataBase {
	protected static final float offset = 1f / 0b11111111;

	private final boolean similarBiome;

	protected BorderDataBase(boolean similarBiome) {
		this.similarBiome = similarBiome;
	}

	protected Drawer getDrawer(Matrix4f matrix, IVertexBuilder builder) {
		return new Drawer(matrix, builder, VisualizeBorders.borderColor(similarBiome));
	}

	abstract public void draw(Matrix4f matrix, IVertexBuilder builder);

	protected class Drawer {
		private final Matrix4f matrix;
		private final IVertexBuilder builder;
		private final Color color;

		public Drawer(Matrix4f matrix, IVertexBuilder builder, Color color) {
			this.matrix = matrix;
			this.builder = builder;
			this.color = color;
		}

		public void DrawSegment(float x, float y, float z) {
			builder.pos(matrix, x, y, z).color(color.r, color.g, color.b, color.a).endVertex();
		}
	}
}
