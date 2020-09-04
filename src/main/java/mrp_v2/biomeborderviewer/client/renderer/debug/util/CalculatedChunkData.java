package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import mrp_v2.biomeborderviewer.client.renderer.debug.VisualizeBorders;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

public class CalculatedChunkData
{
    private final CalculatedSubChunkData[] borders;

    public CalculatedChunkData(ChunkPos pos, World world)
    {
        ArrayList<CalculatedSubChunkData> tempBorders = new ArrayList<>();
        int xOrigin = pos.getXStart();
        int zOrigin = pos.getZStart();
        int x, z, y;
        Int3 mainPos;
        Biome mainBiome, neighborBiome;
        Int3[] neighbors = new Int3[6];
        BorderDataBase borderData;
        HashSet<BorderDataBase> subBorders = new HashSet<>();
        boolean similar;
        for (y = 0; y < 256; y++)
        {
            for (x = xOrigin; x < xOrigin + 16; x++)
            {
                for (z = zOrigin; z < zOrigin + 16; z += 2)
                {
                    if (z == zOrigin && Math.abs((xOrigin + x) % 2) == (y % 2))
                    {
                        z++;
                    }
                    mainPos = new Int3(x, y, z);
                    mainBiome = world.getBiome(mainPos.toBlockPos());
                    neighbors[0] = mainPos.add(0, 1, 0);
                    neighbors[1] = mainPos.add(0, -1, 0);
                    neighbors[2] = mainPos.add(1, 0, 0);
                    neighbors[3] = mainPos.add(-1, 0, 0);
                    neighbors[4] = mainPos.add(0, 0, 1);
                    neighbors[5] = mainPos.add(0, 0, -1);
                    for (Int3 neighborPos : neighbors)
                    {
                        if (neighborPos.getY() < 0 || neighborPos.getY() > 255)
                        {
                            continue;
                        }
                        neighborBiome = world.getBiome(neighborPos.toBlockPos());
                        if (!neighborBiome.equals(mainBiome))
                        {
                            similar = Math.abs(mainBiome.func_242445_k() - neighborBiome.func_242445_k()) < 0.1f;
                            if (mainPos.getX() != neighborPos.getX())
                            {
                                borderData = BorderDataX.newBorder(mainPos, neighborPos, similar);
                            } else if (mainPos.getZ() != neighborPos.getZ())
                            {
                                borderData = BorderDataZ.newBorder(mainPos, neighborPos, similar);
                            } else
                            {
                                borderData = BorderDataY.newBorder(mainPos, neighborPos, similar);
                            }
                            subBorders.add(borderData);
                        }
                    }
                }
            }
            if ((y + 1) % 16 == 0)
            {
                if (subBorders.size() > 0)
                {
                    tempBorders.add(new CalculatedSubChunkData(subBorders, (y - 15) / 16));
                    subBorders.clear();
                }
            }
        }
        borders = tempBorders.toArray(new CalculatedSubChunkData[0]);
    }

    public void draw(Matrix4f matrix, IVertexBuilder builder, int playerY)
    {
        for (CalculatedSubChunkData subChunkData : borders)
        {
            subChunkData.draw(matrix, builder, playerY);
        }
    }

    private static class CalculatedSubChunkData
    {
        public final int subChunkHeight;
        public final BorderDataBase[] borders;

        public CalculatedSubChunkData(Set<BorderDataBase> borders, int subChunkHeight)
        {
            this.subChunkHeight = subChunkHeight;
            ArrayList<BorderDataBase> temp = simplifyBorders(borders);
            this.borders = temp.toArray(new BorderDataBase[0]);
        }

        public void draw(Matrix4f matrix, IVertexBuilder builder, int playerY)
        {
            if (Math.abs(subChunkHeight - playerY) <= VisualizeBorders.getVerticalViewRange())
            {
                for (BorderDataBase lineData : borders)
                {
                    lineData.draw(matrix, builder);
                }
            }
        }

        private ArrayList<BorderDataBase> simplifyBorders(Collection<BorderDataBase> datas)
        {
            boolean didSomething = false;
            ArrayList<BorderDataBase> borders = new ArrayList<>(datas);
            BorderDataBase borderA, borderB, merged;
            for (int i1 = 0; i1 < borders.size() - 1; i1++)
            {
                borderA = borders.get(i1);
                for (int i2 = i1 + 1; i2 < borders.size(); i2++)
                {
                    borderB = borders.get(i2);
                    if (borderA.canNotMerge(borderB))
                    {
                        continue;
                    }
                    borders.remove(i2);
                    borders.remove(i1);
                    if (borderA instanceof BorderDataX)
                    {
                        merged = BorderDataX.merge((BorderDataX) borderA, (BorderDataX) borderB);
                    } else if (borderA instanceof BorderDataY)
                    {
                        merged = BorderDataY.merge((BorderDataY) borderA, (BorderDataY) borderB);
                    } else
                    {
                        merged = BorderDataZ.merge((BorderDataZ) borderA, (BorderDataZ) borderB);
                    }
                    borders.add(i1, merged);
                    didSomething = true;
                    i2--;
                    borderA = borders.get(i1);
                }
            }
            if (didSomething)
            {
                return simplifyBorders(new HashSet<>(borders));
            }
            return borders;
        }
    }
}
