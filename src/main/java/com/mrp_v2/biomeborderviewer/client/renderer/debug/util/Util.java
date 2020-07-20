package com.mrp_v2.biomeborderviewer.client.renderer.debug.util;

import net.minecraft.util.math.ChunkPos;

public class Util {

	public static ChunkPos[] getNeighborChunks(ChunkPos chunk) {
		return new ChunkPos[] { new ChunkPos(chunk.x + 1, chunk.z), new ChunkPos(chunk.x - 1, chunk.z),
				new ChunkPos(chunk.x, chunk.z + 1), new ChunkPos(chunk.x, chunk.z - 1),
				new ChunkPos(chunk.x + 1, chunk.z + 1), new ChunkPos(chunk.x - 1, chunk.z - 1),
				new ChunkPos(chunk.x - 1, chunk.z + 1), new ChunkPos(chunk.x + 1, chunk.z - 1) };
	}
}
