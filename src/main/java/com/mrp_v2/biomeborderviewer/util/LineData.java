package com.mrp_v2.biomeborderviewer.util;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

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

	public void drawLine(IWorld world, Vec3d playerPos) {
		float ay = VisualizeBorders.heightForPos(a.getX(), a.getZ(), world, playerPos);
		float by = VisualizeBorders.heightForPos(b.getX(), b.getZ(), world, playerPos);
		Color color = VisualizeBorders.borderColor(similarTemperature);
		GlStateManager.color4f(color.r, color.g, color.b, color.a);
		GlStateManager.begin(GL11.GL_QUADS);
		if (a.getX() == b.getX()) {
			// top
			GlStateManager.vertex3f(a.getX() + VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() - VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			// bottom
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() - VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(a.getX() + VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			// -x side
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() - VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() - VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			// +x side
			GlStateManager.vertex3f(a.getX() + VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(a.getX() + VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
		} else {
			// top
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() + VisualizeBorders.radius);
			// bottom
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			// -z side
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() - VisualizeBorders.radius);
			// +z side
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay + VisualizeBorders.radius,
					a.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by + VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(b.getX() + VisualizeBorders.radius, by - VisualizeBorders.radius,
					b.getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f(a.getX() - VisualizeBorders.radius, ay - VisualizeBorders.radius,
					a.getZ() + VisualizeBorders.radius);
			GlStateManager.end();
		}
	}

	private static final float minWallHeight = 0;
	private static final float maxWallHeight = 255;
	private static final float wallOffsetDivisor = 1f / 0b11111111;

	public void drawWall() {
		Color color = VisualizeBorders.borderColor(similarTemperature);
		GlStateManager.color4f(color.r, color.g, color.b, color.a);
		GlStateManager.begin(GL11.GL_QUADS);
		if (a.getX() == b.getX()) {
			// -x side
			GlStateManager.vertex3f(a.getX() + wallOffsetDivisor, minWallHeight, a.getZ());
			GlStateManager.vertex3f(b.getX() + wallOffsetDivisor, minWallHeight, b.getZ());
			GlStateManager.vertex3f(b.getX() + wallOffsetDivisor, maxWallHeight, b.getZ());
			GlStateManager.vertex3f(a.getX() + wallOffsetDivisor, maxWallHeight, a.getZ());
			// +x side
			GlStateManager.vertex3f(a.getX() - wallOffsetDivisor, maxWallHeight, a.getZ());
			GlStateManager.vertex3f(b.getX() - wallOffsetDivisor, maxWallHeight, b.getZ());
			GlStateManager.vertex3f(b.getX() - wallOffsetDivisor, minWallHeight, b.getZ());
			GlStateManager.vertex3f(a.getX() - wallOffsetDivisor, minWallHeight, a.getZ());
		} else {
			// -z side
			GlStateManager.vertex3f(a.getX(), minWallHeight, a.getZ() - wallOffsetDivisor);
			GlStateManager.vertex3f(b.getX(), minWallHeight, b.getZ() - wallOffsetDivisor);
			GlStateManager.vertex3f(b.getX(), maxWallHeight, b.getZ() - wallOffsetDivisor);
			GlStateManager.vertex3f(a.getX(), maxWallHeight, a.getZ() - wallOffsetDivisor);
			// +z side
			GlStateManager.vertex3f(a.getX(), maxWallHeight, a.getZ() + wallOffsetDivisor);
			GlStateManager.vertex3f(b.getX(), maxWallHeight, b.getZ() + wallOffsetDivisor);
			GlStateManager.vertex3f(b.getX(), minWallHeight, b.getZ() + wallOffsetDivisor);
			GlStateManager.vertex3f(a.getX(), minWallHeight, a.getZ() + wallOffsetDivisor);
		}
		GlStateManager.end();
	}
}
