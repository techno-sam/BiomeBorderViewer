package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import net.minecraft.util.math.ChunkPos;
import net.minecraft.world.World;

public class ChunkBiomeBorderCalculator implements Runnable
{
    private final ChunkPos pos;
    private final World world;
    private final BiomeBorderDataCollection resultRecipient;

    public ChunkBiomeBorderCalculator(ChunkPos pos, World world, BiomeBorderDataCollection resultRecipient)
    {
        this.pos = pos;
        this.world = world;
        this.resultRecipient = resultRecipient;
    }

    public void run()
    {
        CalculatedChunkData data = new CalculatedChunkData(pos, world);
        resultRecipient.chunkCalculated(pos, data);
    }
}
