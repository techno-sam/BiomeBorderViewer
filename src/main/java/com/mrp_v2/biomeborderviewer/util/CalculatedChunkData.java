package com.mrp_v2.biomeborderviewer.util;

import java.util.ArrayList;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.world.biome.Biome;

public class CalculatedChunkData {
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
								borderData = new BorderDataX(mainPos, neighborPos,
										mainBiome.getTempCategory() == neighborBiome.getTempCategory());
							} else if (mainPos.getZ() != neighborPos.getZ()) {
								borderData = new BorderDataZ(mainPos, neighborPos,
										mainBiome.getTempCategory() == neighborBiome.getTempCategory());
							} else {
								borderData = new BorderDataY(mainPos, neighborPos,
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

	private class CalculatedSubChunkData {
		private final int subChunkHeight;
		private final ArrayList<BorderDataBase> borders;

		public CalculatedSubChunkData(ArrayList<BorderDataBase> borders, int subChunkHeight) {
			this.borders = borders;
			this.subChunkHeight = subChunkHeight;
		}

		public void draw(Matrix4f matrix, IVertexBuilder builder, int playerY) {
			if (Math.abs(((subChunkHeight * 16) + 8) - playerY) < VisualizeBorders.GetVerticalViewRange() * 16) {
				for (BorderDataBase lineData : borders) {
					lineData.draw(matrix, builder);
				}
			}
		}
	}
}
