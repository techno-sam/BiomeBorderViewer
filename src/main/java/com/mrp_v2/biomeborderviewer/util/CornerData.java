package com.mrp_v2.biomeborderviewer.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;

public class CornerData extends Vec3d {
	public boolean showPlusX = true, showMinusX = true, showPlusZ = true, showMinusZ = true;
	public boolean similarTemperature;

	public CornerData(Vec3d position) {
		this(position, true);
	}

	public CornerData(Vec3d position, boolean similarTemperature) {
		super(position.x, position.y, position.z);
		this.similarTemperature = similarTemperature;
	}

	public boolean equals(Object obj) {
		if (!(obj instanceof CornerData)) {
			return false;
		}
		return super.equals(obj);
	}

	public void combine(CornerData other) {
		if (other.showMinusX == false) {
			showMinusX = false;
		}
		if (other.showPlusX == false) {
			showPlusX = false;
		}
		if (other.showPlusZ == false) {
			showPlusZ = false;
		}
		if (other.showMinusZ == false) {
			showMinusZ = false;
		}
	}

	public void drawCorner(Matrix4f matrix, IVertexBuilder builder, IWorld world, Vec3d playerPos) {
		float y = VisualizeBorders.heightForPos(x, z, world, playerPos);
		Color color = VisualizeBorders.borderColor(similarTemperature);
		if (showPlusX) {
			// +x side
			builder.pos(matrix, (float) x + VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x + VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x + VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x + VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		}
		if (showMinusX) {
			// -x side
			builder.pos(matrix, (float) x - VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x - VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x - VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x - VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		}
		if (showPlusZ) {
			// +z side
			builder.pos(matrix, (float) x + VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x + VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x - VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x - VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		}
		if (showMinusZ) {
			// -z side
			builder.pos(matrix, (float) x - VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x - VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x + VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
			builder.pos(matrix, (float) x + VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		}
		// top
		builder.pos(matrix, (float) x + VisualizeBorders.radius, y + VisualizeBorders.radius,
				(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		builder.pos(matrix, (float) x + VisualizeBorders.radius, y + VisualizeBorders.radius,
				(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		builder.pos(matrix, (float) x - VisualizeBorders.radius, y + VisualizeBorders.radius,
				(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		builder.pos(matrix, (float) x - VisualizeBorders.radius, y + VisualizeBorders.radius,
				(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		// bottom
		builder.pos(matrix, (float) x - VisualizeBorders.radius, y - VisualizeBorders.radius,
				(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		builder.pos(matrix, (float) x - VisualizeBorders.radius, y - VisualizeBorders.radius,
				(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		builder.pos(matrix, (float) x + VisualizeBorders.radius, y - VisualizeBorders.radius,
				(float) z - VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
		builder.pos(matrix, (float) x + VisualizeBorders.radius, y - VisualizeBorders.radius,
				(float) z + VisualizeBorders.radius).color(color.r, color.g, color.b, color.a).endVertex();
	}
}
