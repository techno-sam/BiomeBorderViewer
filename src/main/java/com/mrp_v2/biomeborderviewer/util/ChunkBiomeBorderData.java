package com.mrp_v2.biomeborderviewer.util;

import java.util.ArrayList;

import net.minecraft.world.IWorld;

public class ChunkBiomeBorderData {
	private final ArrayList<LineData> lines;
	private final ArrayList<CornerData> corners;
	private final IWorld world;

	public ChunkBiomeBorderData(ArrayList<LineData> lines, ArrayList<CornerData> corners, IWorld world) {
		this.lines = lines;
		this.corners = corners;
		this.world = world;
	}

	public ArrayList<LineData> getLines() {
		return lines;
	}

	public ArrayList<CornerData> getCorners() {
		return corners;
	}
	
	public IWorld getWorld() {
		return world;
	}
}
