package mrp_v2.biomeborderviewer.util;

import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Vector3d;

public class Util
{
    public static ChunkPos[] getNeighborChunks(ChunkPos chunk)
    {
        return new ChunkPos[]{new ChunkPos(chunk.x + 1, chunk.z), new ChunkPos(chunk.x - 1, chunk.z),
                new ChunkPos(chunk.x, chunk.z + 1), new ChunkPos(chunk.x, chunk.z - 1),
                new ChunkPos(chunk.x + 1, chunk.z + 1), new ChunkPos(chunk.x - 1, chunk.z - 1),
                new ChunkPos(chunk.x - 1, chunk.z + 1), new ChunkPos(chunk.x + 1, chunk.z - 1)};
    }

    public static ChunkPos[] getChunkSquare(int radius, Vector3d playerPos)
    {
        return getChunkSquare(radius, new ChunkPos(new BlockPos(playerPos)));
    }

    public static ChunkPos[] getChunkSquare(int radius, ChunkPos center)
    {
        int squareSideLength = radius * 2 + 1;
        ChunkPos[] result = new ChunkPos[squareSideLength * squareSideLength];
        for (int x = 0; x < squareSideLength; x++)
        {
            for (int z = 0; z < squareSideLength; z++)
            {
                result[z + x * squareSideLength] = new ChunkPos(center.x - radius + x, center.z - radius + z);
            }
        }
        return result;
    }

    public static String join(String a, String b)
    {
        return a + "." + b;
    }
}
