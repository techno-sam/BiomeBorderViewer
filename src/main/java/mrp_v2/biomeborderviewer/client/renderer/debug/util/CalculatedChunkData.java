package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;

import java.util.ArrayList;

public class CalculatedChunkData
{
    private final BorderData[] borders;

    public CalculatedChunkData(Int3 pos, World world)
    {
        int xOrigin = pos.getX() * 16, yOrigin = pos.getY() * 16, zOrigin = pos.getZ() * 16;
        int x, z, y;
        Int3 mainPos;
        Biome mainBiome, neighborBiome;
        Int3[] neighbors = new Int3[6];
        ArrayList<BorderData> borderList = new ArrayList<>();
        boolean similar;
        for (y = yOrigin; y < yOrigin + 16; y++)
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
                            similar = Math.abs(mainBiome.getTemperature() - neighborBiome.getTemperature()) < 0.1f;
                            borderList.add(new BorderData(similar, mainPos, neighborPos));
                        }
                    }
                }
            }
        }
        simplifyBorders(borderList);
        this.borders = borderList.toArray(new BorderData[0]);
    }

    private static void combineVerticalBorders(ArrayList<BorderData> borders)
    {
        boolean didSomething = false;
        BorderData borderA, borderB;
        Loop1:
        for (int i1 = 0; i1 < borders.size() - 1; i1++)
        {
            borderA = borders.get(i1);
            for (int i2 = i1 + 1; i2 < borders.size(); i2++)
            {
                borderB = borders.get(i2);
                if (borderA.canMergeOnAxis(borderB, Direction.Axis.Y))
                {
                    borders.remove(i2);
                    borders.remove(i1);
                    borders.add(i1, BorderData.merge(borderA, borderB));
                    didSomething = true;
                    continue Loop1;
                }
            }
        }
        if (didSomething)
        {
            combineVerticalBorders(borders);
        }
    }

    private static void simplifyBorders(ArrayList<BorderData> borders)
    {
        combineVerticalBorders(borders);
        boolean didSomething = false;
        BorderData borderA, borderB;
        Loop1:
        for (int i1 = 0; i1 < borders.size() - 1; i1++)
        {
            borderA = borders.get(i1);
            for (int i2 = i1 + 1; i2 < borders.size(); i2++)
            {
                borderB = borders.get(i2);
                if (borderA.canMerge(borderB))
                {
                    borders.remove(i2);
                    borders.remove(i1);
                    borders.add(i1, BorderData.merge(borderA, borderB));
                    didSomething = true;
                    continue Loop1;
                }
            }
        }
        if (didSomething)
        {
            simplifyBorders(borders);
        }
    }

    public void updateColors()
    {
        for (BorderData borderData : this.borders)
        {
            borderData.updateColor();
        }
    }

    public void draw(Matrix4f matrix, IVertexBuilder builder)
    {
        for (BorderData lineData : borders)
        {
            lineData.draw(matrix, builder);
        }
    }
}
