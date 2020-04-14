package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.Vec3d;

public class UtilityMethods {
	public static Vec3d clone(Vec3d vec) {
		return new Vec3d(vec.x, vec.y, vec.z);
	}
}
