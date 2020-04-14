package com.mrp_v2.biomeborderviewer.util;

import java.util.ArrayList;

public class ChunkBiomeBorderData {
	private ArrayList<LineData> lines;
	private ArrayList<CornerData> corners;

	public ChunkBiomeBorderData(ArrayList<LineData> lines) {
		this.lines = lines;
		corners = null;
	}

	public ChunkBiomeBorderData(ArrayList<LineData> lines, ArrayList<CornerData> corners) {
		this.lines = lines;
		this.corners = corners;
	}

	public ArrayList<LineData> getLines() {
		return lines;
	}

	public ArrayList<CornerData> getCorners() {
		return corners;
	}
}
