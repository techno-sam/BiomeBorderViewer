package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.IWorld;

public class QueuedChunkData {
	private final ChunkPos chunk;
	private final IWorld world;

	public QueuedChunkData(ChunkPos chunk, IWorld world) {
		this.chunk = chunk;
		this.world = world;
	}

	public ChunkPos getChunkPos() {
		return chunk;
	}

	public IWorld getWorld() {
		return world;
	}
}
