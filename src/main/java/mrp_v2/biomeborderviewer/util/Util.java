package mrp_v2.biomeborderviewer.util;

import mrp_v2.biomeborderviewer.client.renderer.debug.util.Int3;

public class Util
{
    public static Int3[] getNeighborChunks(Int3 chunk)
    {
        return new Int3[]{new Int3(chunk.getX() + 1, chunk.getY(), chunk.getZ()),
                new Int3(chunk.getX() - 1, chunk.getY(), chunk.getZ()),
                new Int3(chunk.getX(), chunk.getY() + 1, chunk.getZ()),
                new Int3(chunk.getX(), chunk.getY() - 1, chunk.getZ()),
                new Int3(chunk.getX(), chunk.getY(), chunk.getZ() + 1),
                new Int3(chunk.getX(), chunk.getY(), chunk.getZ() - 1)};
    }

    public static Int3[] getChunkSquare(int radius, Int3 center)
    {
        int squareSideLength = radius * 2 + 1;
        Int3[] result = new Int3[squareSideLength * squareSideLength * squareSideLength];
        for (int y = 0; y < squareSideLength; y++)
        {
            for (int x = 0; x < squareSideLength; x++)
            {
                for (int z = 0; z < squareSideLength; z++)
                {
                    result[z + x * squareSideLength + y * squareSideLength * squareSideLength] =
                            new Int3(center.getX() - radius + x, center.getY() - radius + y,
                                    center.getZ() - radius + z);
                }
            }
        }
        return result;
    }

    public static String join(String a, String b)
    {
        return a + "." + b;
    }
}
