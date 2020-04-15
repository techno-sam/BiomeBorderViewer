package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;

public class LineData {
	public final Vec3d a, b;
	public boolean similarTemperature;

	public LineData(Vec3d a, Vec3d b) {
		this.a = a;
		this.b = b;
	}

	public LineData(Vec3d a, Vec3d b, boolean similarTemperature) {
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
		float ay = VisualizeBorders.heightForPos(a.x, a.z, world, playerPos);
		float by = VisualizeBorders.heightForPos(b.x, b.z, world, playerPos);
		Color color = VisualizeBorders.borderColor(similarTemperature);
		if (a.x == b.x) {
			// top
			builder.pos(matrix, (float) a.x + VisualizeBorders.radius, ay + VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by + VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x - VisualizeBorders.radius, by + VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay + VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// bottom
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay - VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x - VisualizeBorders.radius, by - VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by - VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x + VisualizeBorders.radius, ay - VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// -x side
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay + VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x - VisualizeBorders.radius, by + VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x - VisualizeBorders.radius, by - VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay - VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// +x side
			builder.pos(matrix, (float) a.x + VisualizeBorders.radius, ay - VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by - VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by + VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x + VisualizeBorders.radius, ay + VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
		} else {
			// top
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay + VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by + VisualizeBorders.radius, (float) b.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by + VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay + VisualizeBorders.radius, (float) a.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// bottom
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay - VisualizeBorders.radius, (float) a.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by - VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by - VisualizeBorders.radius, (float) b.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay - VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// -z side
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay - VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by - VisualizeBorders.radius, (float) b.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by + VisualizeBorders.radius, (float) b.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay + VisualizeBorders.radius, (float) a.z - VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// +z side
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay + VisualizeBorders.radius, (float) a.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by + VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + VisualizeBorders.radius, by - VisualizeBorders.radius, (float) b.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x - VisualizeBorders.radius, ay - VisualizeBorders.radius, (float) a.z + VisualizeBorders.radius)
					.color(color.r, color.g, color.b, color.a).endVertex();
		}
	}
	
	private static final float minWallHeight = 0;
	private static final float maxWallHeight = 255;
	private static final float wallOffsetDivisor = 1f / 0b11111111;
	
	public void drawWall(Matrix4f matrix, IVertexBuilder builder) {
		Color color = VisualizeBorders.borderColor(similarTemperature);
		if (a.x == b.x) {
			// -x side
			builder.pos(matrix, (float) a.x + wallOffsetDivisor, minWallHeight, (float) a.z)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + wallOffsetDivisor, minWallHeight, (float) b.z)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x + wallOffsetDivisor, maxWallHeight, (float) b.z)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x + wallOffsetDivisor, maxWallHeight, (float) a.z)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// +x side
			builder.pos(matrix, (float) a.x - wallOffsetDivisor, maxWallHeight, (float) a.z)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x - wallOffsetDivisor, maxWallHeight, (float) b.z)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x - wallOffsetDivisor, minWallHeight, (float) b.z)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x - wallOffsetDivisor, minWallHeight, (float) a.z)
					.color(color.r, color.g, color.b, color.a).endVertex();
		} else {
			// -z side
			builder.pos(matrix, (float) a.x, minWallHeight, (float) a.z - wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x, minWallHeight, (float) b.z - wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x, maxWallHeight, (float) b.z - wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x, maxWallHeight, (float) a.z - wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			// +z side
			builder.pos(matrix, (float) a.x, maxWallHeight, (float) a.z + wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x, maxWallHeight, (float) b.z + wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) b.x, minWallHeight, (float) b.z + wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) a.x, minWallHeight, (float) a.z + wallOffsetDivisor)
					.color(color.r, color.g, color.b, color.a).endVertex();
		}
	}
}
