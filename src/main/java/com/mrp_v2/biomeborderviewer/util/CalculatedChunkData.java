package com.mrp_v2.biomeborderviewer.util;

import java.util.ArrayList;

import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

public class CalculatedChunkData {
	private final ArrayList<LineData> lines;
	private final ArrayList<CornerData> corners;
	private final IWorld world;

	public CalculatedChunkData(ArrayList<LineData> lines, ArrayList<CornerData> corners, IWorld world) {
		this.lines = lines;
		this.corners = corners;
		this.world = world;
	}

	public CalculatedChunkData(QueuedChunkData data) {
		world = data.getWorld();
		lines = new ArrayList<LineData>();
		corners = new ArrayList<CornerData>();
		int xOrigin = data.getChunkPos().getXStart(), zOrigin = data.getChunkPos().getZStart();
		// Declarations to avoid reallocation
		int x, z;
		Int3 mainPos, mainPosCopy;
		Biome mainBiome, neighborBiome;
		Int3[] neighbors = new Int3[4];
		LineData lineData;
		CornerData cornerDataA, cornerDataB;
		//
		for (x = xOrigin; x < xOrigin + 16; x++) {
			for (z = zOrigin; z < zOrigin + 16; z += 2) {
				if (z == zOrigin && Math.abs((xOrigin + x) % 2) == 1) {
					z++;
				}
				mainPos = new Int3(x, 64, z);
				mainBiome = world.getBiome(mainPos.toBlockPos());
				neighbors[0] = mainPos.add(1, 0, 0);
				neighbors[1] = mainPos.add(-1, 0, 0);
				neighbors[2] = mainPos.add(0, 0, 1);
				neighbors[3] = mainPos.add(0, 0, -1);
				for (Int3 neighborPos : neighbors) {
					neighborBiome = world.getBiome(neighborPos.toBlockPos());
					if (!neighborBiome.equals(mainBiome)) {
						mainPosCopy = new Int3(mainPos);
						if (mainPosCopy.getZ() == neighborPos.getZ()) {// if they have the same z and different x
							mainPosCopy = mainPosCopy.add(0, 0, 1);
							if (mainPosCopy.getX() > neighborPos.getX()) {
								neighborPos = neighborPos.add(1, 0, 0);
							} else {
								mainPosCopy = mainPosCopy.add(1, 0, 0);
							}
						} else {// if they have the same x and different z
							mainPosCopy = mainPosCopy.add(1, 0, 0);
							if (mainPosCopy.getZ() > neighborPos.getZ()) {
								neighborPos = neighborPos.add(0, 0, 1);
							} else {
								mainPosCopy = mainPosCopy.add(0, 0, 1);
							}
						}
						lineData = new LineData(mainPosCopy, neighborPos);
						lineData.similarTemperature = mainBiome.getTempCategory() == neighborBiome.getTempCategory();
						lines.add(lineData);
						cornerDataA = new CornerData(mainPosCopy);
						cornerDataB = new CornerData(neighborPos);
						if (mainPosCopy.getX() == neighborPos.getX()) {
							cornerDataA.showMinusZ = false;
							cornerDataB.showPlusZ = false;
						} else {
							cornerDataA.showMinusX = false;
							cornerDataB.showPlusX = false;
						}
						cornerDataA.similarTemperature = lineData.similarTemperature;
						cornerDataB.similarTemperature = lineData.similarTemperature;
						if (!corners.contains(cornerDataA)) {
							corners.add(cornerDataA);
						} else {
							corners.get(corners.indexOf(cornerDataA)).combine(cornerDataA);
						}
						if (!corners.contains(cornerDataB)) {
							corners.add(cornerDataB);
						} else {
							corners.get(corners.indexOf(cornerDataB)).combine(cornerDataB);
						}
					}
				}
			}
		}
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

	public void draw(Vec3d playerPos) {
		switch (VisualizeBorders.renderMode) {
		case WALL:
			for (LineData lineData : lines) {
				lineData.drawWall();
			}
			break;
		default:
			for (LineData lineData : lines) {
				lineData.drawLine(world, playerPos);
			}
			for (CornerData cornerData : corners) {
				cornerData.drawCorner(world, playerPos);
			}
			break;
		}
	}
}
