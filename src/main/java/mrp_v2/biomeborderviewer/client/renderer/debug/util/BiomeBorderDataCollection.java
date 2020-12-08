package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import mrp_v2.biomeborderviewer.client.Config;
import mrp_v2.biomeborderviewer.util.Util;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;

import javax.annotation.Nullable;
import java.util.HashMap;
import java.util.HashSet;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class BiomeBorderDataCollection
{
    /**
     * Does not need synchronization
     */
    private final HashMap<Int3, CalculatedChunkData> calculatedChunks;
    /**
     * Does not need synchronization
     */
    private final HashSet<Int3> loadedChunks;
    /**
     * Needs synchronization, use this as a lock
     */
    private final HashMap<Int3, CalculatedChunkData> calculatedChunksToAdd;
    /**
     * Needs synchronization, use {@link BiomeBorderDataCollection#calculatedChunksToAdd} as a lock
     */
    private final HashSet<Int3> chunksQueuedForCalculation;
    @Nullable private ExecutorService threadPool;

    public BiomeBorderDataCollection()
    {
        this.calculatedChunks = new HashMap<>();
        this.calculatedChunksToAdd = new HashMap<>();
        this.chunksQueuedForCalculation = new HashSet<>();
        this.loadedChunks = new HashSet<>();
        this.threadPool = null;
    }

    public void updateColors()
    {
        for (CalculatedChunkData chunkData : calculatedChunks.values())
        {
            chunkData.updateColors();
        }
    }

    public void chunkLoaded(Int3 pos)
    {
        loadedChunks.add(pos);
    }

    public void chunkUnloaded(Int3 pos)
    {
        loadedChunks.remove(pos);
        calculatedChunks.remove(pos);
    }

    public void chunkCalculated(Int3 pos, CalculatedChunkData data)
    {
        synchronized (calculatedChunksToAdd)
        {
            calculatedChunksToAdd.put(pos, data);
            chunksQueuedForCalculation.remove(pos);
        }
    }

    public boolean areNoChunksLoaded()
    {
        return loadedChunks.isEmpty();
    }

    public void renderBorders(Int3[] chunksToRender, Matrix4f matrix, IVertexBuilder bufferBuilder, World world)
    {
        HashSet<Int3> chunksToQueue = new HashSet<>();
        for (Int3 pos : chunksToRender)
        {
            CalculatedChunkData data = calculatedChunks.get(pos);
            if (data != null)
            {
                data.draw(matrix, bufferBuilder);
            } else if (chunkReadyForCalculations(pos))
            {
                chunksToQueue.add(pos);
            }
        }
        updateChunkCalculations(chunksToQueue, world);
    }

    private boolean chunkReadyForCalculations(Int3 pos)
    {
        if (!loadedChunks.contains(pos))
        {
            return false;
        }
        for (Int3 neighbor : Util.getNeighborChunks(pos))
        {
            if (!loadedChunks.contains(neighbor))
            {
                if (neighbor.getY() < 0 || neighbor.getY() > 15)
                {
                    continue;
                }
                return false;
            }
        }
        return true;
    }

    /**
     * Updates the calculations of chunks.
     */
    private void updateChunkCalculations(HashSet<Int3> chunksToQueueForCalculation, World world)
    {
        synchronized (calculatedChunksToAdd)
        {
            if (calculatedChunksToAdd.size() > 0)
            {
                calculatedChunks.putAll(calculatedChunksToAdd);
                calculatedChunksToAdd.clear();
            }
            chunksToQueueForCalculation.removeAll(chunksQueuedForCalculation);
            if (chunksToQueueForCalculation.size() > 0)
            {
                if (threadPool == null)
                {
                    threadPool = Executors.newFixedThreadPool(Config.CLIENT.borderCalculationThreads.get());
                }
            }
            for (Int3 pos : chunksToQueueForCalculation)
            {
                chunksQueuedForCalculation.add(pos);
                threadPool.execute(new ChunkBiomeBorderCalculator(pos, world, this));
            }
        }
    }

    public void worldUnloaded()
    {
        if (threadPool != null)
        {
            threadPool.shutdownNow();
        }
    }
}
