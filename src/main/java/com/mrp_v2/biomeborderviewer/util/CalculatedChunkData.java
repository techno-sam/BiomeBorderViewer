package com.mrp_v2.biomeborderviewer.util;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.world.biome.Biome;

public class CalculatedChunkData {

	private class CalculatedSubChunkData {
		private final int subChunkHeight;
		private final HashSet<BorderDataBase> borders;

		public CalculatedSubChunkData(Collection<BorderDataBase> borders, int subChunkHeight) {
			this.borders = new HashSet<BorderDataBase>(borders);
			this.subChunkHeight = subChunkHeight;
			simplifyBorders();
		}

		public void draw(Matrix4f matrix, IVertexBuilder builder, int playerY) {
			if (Math.abs(((subChunkHeight * 16) + 8) - playerY) < VisualizeBorders.GetVerticalViewRange() * 16) {
				for (BorderDataBase lineData : borders) {
					lineData.draw(matrix, builder);
				}
			}
		}
		
		private void simplifyBorders() {
			
		}
	}

	private final ArrayList<CalculatedSubChunkData> borders;

	public CalculatedChunkData(QueuedChunkData data) {
		borders = new ArrayList<CalculatedSubChunkData>();
		int xOrigin = data.getChunkPos().getXStart(), zOrigin = data.getChunkPos().getZStart();
		// Declarations to avoid reallocation
		int x, z, y;
		Int3 mainPos;
		Biome mainBiome, neighborBiome;
		Int3[] neighbors = new Int3[6];
		BorderDataBase borderData;
		ArrayList<BorderDataBase> subBorders = new ArrayList<BorderDataBase>();
		for (y = 0; y < 256; y++) {
			for (x = xOrigin; x < xOrigin + 16; x++) {
				for (z = zOrigin; z < zOrigin + 16; z += 2) {
					if (z == zOrigin && Math.abs((xOrigin + x) % 2) == (y % 2)) {
						z++;
					}
					mainPos = new Int3(x, y, z);
					mainBiome = data.getWorld().getBiome(mainPos.toBlockPos());
					neighbors[0] = mainPos.add(0, 1, 0);
					neighbors[1] = mainPos.add(0, -1, 0);
					neighbors[2] = mainPos.add(1, 0, 0);
					neighbors[3] = mainPos.add(-1, 0, 0);
					neighbors[4] = mainPos.add(0, 0, 1);
					neighbors[5] = mainPos.add(0, 0, -1);
					for (Int3 neighborPos : neighbors) {
						neighborBiome = data.getWorld().getBiome(neighborPos.toBlockPos());
						if (!neighborBiome.equals(mainBiome)) {
							if (mainPos.getX() != neighborPos.getX()) {
								borderData = BorderDataX.newBorder(mainPos, neighborPos,
										mainBiome.getTempCategory() == neighborBiome.getTempCategory());
							} else if (mainPos.getZ() != neighborPos.getZ()) {
								borderData = BorderDataZ.newBorder(mainPos, neighborPos,
										mainBiome.getTempCategory() == neighborBiome.getTempCategory());
							} else {
								borderData = BorderDataY.newBorder(mainPos, neighborPos,
										mainBiome.getTempCategory() == neighborBiome.getTempCategory());
							}
							subBorders.add(borderData);
						}
					}
				}
			}
			if ((y + 1) % 16 == 0) {
				borders.add(new CalculatedSubChunkData(new ArrayList<BorderDataBase>(subBorders), (y - 15) / 16));
				subBorders.clear();
			}
		}
	}

	public void draw(Matrix4f matrix, IVertexBuilder builder, int playerY) {
		for (CalculatedSubChunkData subChunkData : borders) {
			subChunkData.draw(matrix, builder, playerY);
		}
	}
}
