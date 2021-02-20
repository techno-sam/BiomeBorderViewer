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

    public static Int3[] getChunkColumn(int horizontalRadius, int verticalRadius, Int3 center)
    {
        int squareSideLength = horizontalRadius * 2 + 1;
        int columnHeight = verticalRadius * 2 + 1;
        Int3[] result = new Int3[squareSideLength * squareSideLength * columnHeight];
        int currentIndex = 0;
        for (int y = 0; y < columnHeight; y++)
        {
            for (int x = 0; x < squareSideLength; x++)
            {
                for (int z = 0; z < squareSideLength; z++)
                {
                    result[currentIndex++] =
                            new Int3(center.getX() - horizontalRadius + x, center.getY() - verticalRadius + y,
                                    center.getZ() - horizontalRadius + z);
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
