package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;

public class LineData {
	public final Int3 a, b;
	public boolean similarTemperature;

	public LineData(Int3 a, Int3 b) {
		this.a = a;
		this.b = b;
	}

	public LineData(Int3 a, Int3 b, boolean similarTemperature) {
		this(a, b);
		this.similarTemperature = similarTemperature;
	}

	public int hashCode() {
		return a.hashCode() * b.hashCode();
	}

	public boolean equals(LineData lineData) {
		return a.equals(lineData.a) && b.equals(lineData.b) && similarTemperature == lineData.similarTemperature;
	}

	public boolean equals(Object obj) {
		if (obj instanceof LineData) {
			return equals((LineData) obj);
		}
		return false;
	}

	public void drawLine(Matrix4f matrix, IVertexBuilder builder, IWorld world, Vec3d playerPos) {
		float ay = VisualizeBorders.heightForPos(a.getX(), a.getZ(), world, playerPos);
		float by = VisualizeBorders.heightForPos(b.getX(), b.getZ(), world, playerPos);
		Color color = VisualizeBorders.borderColor(similarTemperature);
		if (a.getX() == b.getX()) {
			// top
			builder.pos(matrix, a.getX() + VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() - VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			// bottom
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() - VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() + VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			// -x side
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() - VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() - VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			// +x side
			builder.pos(matrix, a.getX() + VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() + VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		} else {
			// top
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			// bottom
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			// -z side
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			// +z side
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		}
	}

	private static final float minWallHeight = 0;
	private static final float maxWallHeight = 255;
	private static final float wallOffsetDivisor = 1f / 0b11111111;

	public void drawWall(Matrix4f matrix, IVertexBuilder builder) {
		Color color = VisualizeBorders.borderColor(similarTemperature);
		if (a.getX() == b.getX()) {
			// -x side
			builder.pos(matrix, a.getX() + wallOffsetDivisor, minWallHeight, a.getZ())
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + wallOffsetDivisor, minWallHeight, b.getZ())
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() + wallOffsetDivisor, maxWallHeight, b.getZ())
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() + wallOffsetDivisor, maxWallHeight, a.getZ())
					.color(color.r, color.g, color.b, color.a).endVertex();
			// +x side
			builder.pos(matrix, a.getX() - wallOffsetDivisor, maxWallHeight, a.getZ())
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() - wallOffsetDivisor, maxWallHeight, b.getZ())
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX() - wallOffsetDivisor, minWallHeight, b.getZ())
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX() - wallOffsetDivisor, minWallHeight, a.getZ())
					.color(color.r, color.g, color.b, color.a).endVertex();
		} else {
			// -z side
			builder.pos(matrix, a.getX(), minWallHeight, a.getZ() - wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX(), minWallHeight, b.getZ() - wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX(), maxWallHeight, b.getZ() - wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX(), maxWallHeight, a.getZ() - wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// +z side
			builder.pos(matrix, a.getX(), maxWallHeight, a.getZ() + wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX(), maxWallHeight, b.getZ() + wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, b.getX(), minWallHeight, b.getZ() + wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, a.getX(), minWallHeight, a.getZ() + wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
		}
	}
}
