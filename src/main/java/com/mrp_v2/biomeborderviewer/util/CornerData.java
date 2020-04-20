package com.mrp_v2.biomeborderviewer.util;

import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;

public class CornerData extends Int3 {
	public boolean showPlusX = true, showMinusX = true, showPlusZ = true, showMinusZ = true;
	public boolean similarTemperature;

	public CornerData(Int3 position) {
		this(position, true);
	}

	public CornerData(Int3 position, boolean similarTemperature) {
		super(position);
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

	public void drawCorner(IWorld world, Vec3d playerPos) {
		float y = VisualizeBorders.heightForPos(getX(), getZ(), world, playerPos);
		Color color = VisualizeBorders.borderColor(similarTemperature);
		GlStateManager.color4f(color.r / 255f, color.g / 255f, color.b / 255f, color.a / 255f);
		GlStateManager.begin(GL11.GL_QUADS);
		if (showPlusX) {
			// +x side
			GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) getZ() + VisualizeBorders.radius);
		}
		if (showMinusX) {
			// -x side
			GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) getZ() - VisualizeBorders.radius);
		}
		if (showPlusZ) {
			// +z side
			GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) getZ() + VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) getZ() + VisualizeBorders.radius);
		}
		if (showMinusZ) {
			// -z side
			GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y + VisualizeBorders.radius,
					(float) getZ() - VisualizeBorders.radius);
			GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y - VisualizeBorders.radius,
					(float) getZ() - VisualizeBorders.radius);
		}
		// top
		GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y + VisualizeBorders.radius,
				(float) getZ() + VisualizeBorders.radius);
		GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y + VisualizeBorders.radius,
				(float) getZ() - VisualizeBorders.radius);
		GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y + VisualizeBorders.radius,
				(float) getZ() - VisualizeBorders.radius);
		GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y + VisualizeBorders.radius,
				(float) getZ() + VisualizeBorders.radius);
		// bottom
		GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y - VisualizeBorders.radius,
				(float) getZ() + VisualizeBorders.radius);
		GlStateManager.vertex3f((float) getX() - VisualizeBorders.radius, y - VisualizeBorders.radius,
				(float) getZ() - VisualizeBorders.radius);
		GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y - VisualizeBorders.radius,
				(float) getZ() - VisualizeBorders.radius);
		GlStateManager.vertex3f((float) getX() + VisualizeBorders.radius, y - VisualizeBorders.radius,
				(float) getZ() + VisualizeBorders.radius);
		GlStateManager.end();
	}
}
