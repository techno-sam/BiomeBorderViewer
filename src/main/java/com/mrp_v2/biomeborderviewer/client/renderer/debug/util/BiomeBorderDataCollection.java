package com.mrp_v2.biomeborderviewer.client.renderer.debug.util;

import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import com.google.common.collect.Maps;
import com.google.common.collect.Sets;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class BiomeBorderDataCollection {

	private final Map<ChunkPos, CalculatedChunkData> calculatedChunks;
	private final Map<ChunkPos, CalculatedChunkData> calculatedChunksToAdd;
	private final Set<ChunkPos> chunksQueuedForCalculation;
	private final Set<ChunkPos> loadedChunks;
	private final Object threadLock;
	private final ExecutorService threadPool;

	public BiomeBorderDataCollection() {
		this.calculatedChunks = Maps.newHashMap();
		this.calculatedChunksToAdd = Maps.newHashMap();
		this.chunksQueuedForCalculation = Sets.newHashSet();
		this.loadedChunks = Sets.newHashSet();
		this.threadLock = new Object();
		this.threadPool = Executors.newFixedThreadPool(4);
	}

	public void chunkLoaded(ChunkPos pos) {
		loadedChunks.add(pos);
	}

	public void chunkUnloaded(ChunkPos pos) {
		loadedChunks.remove(pos);
		calculatedChunks.remove(pos);
	}

	public void chunkCalculated(ChunkPos pos, CalculatedChunkData data) {
		synchronized (threadLock) {
			calculatedChunksToAdd.put(pos, data);
			chunksQueuedForCalculation.remove(pos);
		}
	}

	public boolean areAnyChunksLoaded() {
		return loadedChunks.isEmpty();
	}

	public CalculatedChunkData getChunk(ChunkPos pos) {
		return calculatedChunks.get(pos);
	}

	public boolean chunkReadyForCalculations(ChunkPos pos) {
		if (!loadedChunks.contains(pos)) {
			return false;
		}
		for (ChunkPos neighbor : Util.getNeighborChunks(pos)) {
			if (!loadedChunks.contains(neighbor)) {
				return false;
			}
		}
		return true;
	}

	/**
	 * Updates loaded chunks, and returns a list of chunks waiting for calculations.
	 */
	public void updateCalculatedChunks(Set<ChunkPos> chunksToQueueForCalculation, World world) {
		synchronized (threadLock) {
			if (calculatedChunksToAdd.size() > 0) {
				calculatedChunks.putAll(calculatedChunksToAdd);
				calculatedChunksToAdd.clear();
			}
			chunksToQueueForCalculation.removeAll(chunksQueuedForCalculation);
			for (ChunkPos pos : chunksToQueueForCalculation) {
				chunksQueuedForCalculation.add(pos);
				threadPool.execute(new ChunkBiomeBorderCalculator(pos, world, this));
			}
		}
	}

	public void worldUnloaded() {
		threadPool.shutdownNow();
	}
}
