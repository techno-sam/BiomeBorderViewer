package com.mrp_v2.biomeborderviewer.util;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.biome.Biome;

public class ChunkBiomeBorderData {
	private final ArrayList<LineData> lines;
	private final ArrayList<CornerData> corners;
	private final IWorld world;

	public ChunkBiomeBorderData(ArrayList<LineData> lines, ArrayList<CornerData> corners, IWorld world) {
		this.lines = lines;
		this.corners = corners;
		this.world = world;
	}

	public ChunkBiomeBorderData(QueuedChunkData data) {
		world = data.getWorld();
		lines = new ArrayList<LineData>();
		corners = new ArrayList<CornerData>();
		int xOrigin = data.getChunk().getPos().getXStart(), zOrigin = data.getChunk().getPos().getZStart();
		// Declarations to avoid reallocation
		int x, z;
		Int3 mainPos;
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
				mainBiome = data.getWorld().getBiome(mainPos.toBlockPos());
				neighbors[0] = mainPos.add(1, 0, 0);
				neighbors[1] = mainPos.add(-1, 0, 0);
				neighbors[2] = mainPos.add(0, 0, 1);
				neighbors[3] = mainPos.add(0, 0, -1);
				for (Int3 neighborPos : neighbors) {
					neighborBiome = data.getWorld().getBiome(neighborPos.toBlockPos());
					if (!neighborBiome.equals(mainBiome)) {
						if (mainPos.getX() != neighborPos.getX()) {// if they have the same z and different x
							mainPos = mainPos.add(0, 0, 1);
							if (mainPos.getX() > neighborPos.getX()) {
								neighborPos = neighborPos.add(1, 0, 0);
							} else {
								mainPos = mainPos.add(1, 0, 0);
							}
						} else {// if they have the same x and different z
							mainPos = mainPos.add(1, 0, 0);
							if (mainPos.getZ() > neighborPos.getZ()) {
								neighborPos = neighborPos.add(0, 0, 1);
							} else {
								mainPos = mainPos.add(0, 0, 1);
							}
						}
						lineData = new LineData(mainPos, neighborPos);
						lineData.similarTemperature = mainBiome.getTempCategory() == neighborBiome.getTempCategory();
						lines.add(lineData);
						cornerDataA = new CornerData(mainPos);
						cornerDataB = new CornerData(neighborPos);
						if (mainPos.getX() == neighborPos.getX()) {
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

	public void draw(Matrix4f matrix, IVertexBuilder builder, Vec3d playerPos) {
		switch (VisualizeBorders.renderMode) {
		case WALL:
			for (LineData lineData : lines) {
				lineData.drawWall(matrix, builder);
			}
			break;
		default:
			for (LineData lineData : lines) {
				lineData.drawLine(matrix, builder, world, playerPos);
			}
			for (CornerData cornerData : corners) {
				cornerData.drawCorner(matrix, builder, world, playerPos);
			}
			break;
		}
	}
}
