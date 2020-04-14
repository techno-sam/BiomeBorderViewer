package com.mrp_v2.biomeborderviewer.util;

import net.minecraft.world.IWorld;
import net.minecraft.world.chunk.IChunk;

public class QueuedChunkData {
	private final IChunk chunk;
	private final IWorld world;

	public QueuedChunkData(IChunk chunk, IWorld world) {
		this.chunk = chunk;
		this.world = world;
	}

	public IChunk getChunk() {
		return chunk;
	}

	public IWorld getWorld() {
		return world;
	}
}
