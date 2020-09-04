package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import mrp_v2.biomeborderviewer.config.ClientConfig;
import mrp_v2.biomeborderviewer.util.Util;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BiomeBorderDataCollection
{
    /**
     * Does not need synchronization
     */
    private final HashMap<ChunkPos, CalculatedChunkData> calculatedChunks;
    /**
     * Does not need synchronization
     */
    private final HashSet<ChunkPos> loadedChunks;
    /**
     * Needs synchronization
     */
    private final HashMap<ChunkPos, CalculatedChunkData> calculatedChunksToAdd;
    /**
     * Needs synchronization
     */
    private final HashSet<ChunkPos> chunksQueuedForCalculation;
    private final Object threadLock;
    @Nullable private ExecutorService threadPool;

    public BiomeBorderDataCollection()
    {
        this.calculatedChunks = new HashMap<>();
        this.calculatedChunksToAdd = new HashMap<>();
        this.chunksQueuedForCalculation = new HashSet<>();
        this.loadedChunks = new HashSet<>();
        this.threadLock = new Object();
        this.threadPool = null;
    }

    public void chunkLoaded(ChunkPos pos)
    {
        loadedChunks.add(pos);
    }

    public void chunkUnloaded(ChunkPos pos)
    {
        loadedChunks.remove(pos);
        calculatedChunks.remove(pos);
    }

    public void chunkCalculated(ChunkPos pos, CalculatedChunkData data)
    {
        synchronized (threadLock)
        {
            calculatedChunksToAdd.put(pos, data);
            chunksQueuedForCalculation.remove(pos);
        }
    }

    public boolean areNoChunksLoaded()
    {
        return loadedChunks.isEmpty();
    }

    public void renderBorders(ChunkPos[] chunksToRender, Matrix4f matrix, IVertexBuilder bufferBuilder, int playerY,
            World world)
    {
        HashSet<ChunkPos> chunksToQueue = new HashSet<>();
        for (ChunkPos pos : chunksToRender)
        {
            CalculatedChunkData data = calculatedChunks.get(pos);
            if (data != null)
            {
                data.draw(matrix, bufferBuilder, playerY);
            } else if (chunkReadyForCalculations(pos))
            {
                chunksToQueue.add(pos);
            }
        }
        updateCalculatedChunks(chunksToQueue, world);
    }

    private boolean chunkReadyForCalculations(ChunkPos pos)
    {
        if (!loadedChunks.contains(pos))
        {
            return false;
        }
        for (ChunkPos neighbor : Util.getNeighborChunks(pos))
        {
            if (!loadedChunks.contains(neighbor))
            {
                return false;
            }
        }
        return true;
    }

    /**
     * Updates loaded chunks, and returns a list of chunks waiting for calculations.
     */
    private void updateCalculatedChunks(Set<ChunkPos> chunksToQueueForCalculation, World world)
    {
        if (threadPool == null)
        {
            threadPool = Executors.newFixedThreadPool(ClientConfig.CLIENT.borderCalculationThreads.get());
        }
        synchronized (threadLock)
        {
            if (calculatedChunksToAdd.size() > 0)
            {
                calculatedChunks.putAll(calculatedChunksToAdd);
                calculatedChunksToAdd.clear();
            }
            chunksToQueueForCalculation.removeAll(chunksQueuedForCalculation);
            for (ChunkPos pos : chunksToQueueForCalculation)
            {
                chunksQueuedForCalculation.add(pos);
                threadPool.execute(new ChunkBiomeBorderCalculator(pos, world, this));
            }
        }
    }

    public void worldUnloaded()
    {
        if (threadPool == null)
        {
            return;
        }
        threadPool.shutdownNow();
    }
}
