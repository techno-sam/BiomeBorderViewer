package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.client.renderer.Matrix4f;

public abstract class BorderDataBase {
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

	protected static final float offset = 1f / 0b11111111;

	protected final float x1, x2, y1, y2, z1, z2;

	private final int hashCode;

	protected final boolean similarBiome;

	protected BorderDataBase(boolean similarBiome, float x1, float x2, float y1, float y2, float z1, float z2) {
		this.similarBiome = similarBiome;
		this.x1 = x1;
		this.x2 = x2;
		this.y1 = y1;
		this.y2 = y2;
		this.z1 = z1;
		this.z2 = z2;
		// hash code
		final int prime = 31;
		int result = 1;
		result = prime * result + (similarBiome ? 1231 : 1237);
		result = prime * result + Float.floatToIntBits(x1);
		result = prime * result + Float.floatToIntBits(x2);
		result = prime * result + Float.floatToIntBits(y1);
		result = prime * result + Float.floatToIntBits(y2);
		result = prime * result + Float.floatToIntBits(z1);
		result = prime * result + Float.floatToIntBits(z2);
		hashCode = result;
	}

	public boolean canMerge(BorderDataBase border) {
		return similarBiome == border.similarBiome;
	}

	abstract public void draw(Matrix4f matrix, IVertexBuilder builder);

	@Override
	public boolean equals(Object obj) {
		if (this == obj) {
			return true;
		}
		if (!(obj instanceof BorderDataBase)) {
			return false;
		}
		BorderDataBase other = (BorderDataBase) obj;
		if (similarBiome != other.similarBiome) {
			return false;
		}
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

	protected Drawer getDrawer(Matrix4f matrix, IVertexBuilder builder) {
		return new Drawer(matrix, builder, VisualizeBorders.borderColor(similarBiome));
	}

	@Override
	public int hashCode() {
		return hashCode;
	}
}
