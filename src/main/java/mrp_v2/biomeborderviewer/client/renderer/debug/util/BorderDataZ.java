package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Matrix4f;

public class BorderDataZ extends BorderDataBase
{
    private BorderDataZ(boolean similarBiome, float x1, float x2, float y1, float y2, float z1, float z2)
    {
        super(similarBiome, x1, x2, y1, y2, z1, z2);
    }

    static public BorderDataZ merge(BorderDataZ a, BorderDataZ b)
    {
        if (a.y1 == b.y1 && a.y2 == b.y2)
        {
            if (a.x1 == b.x2)
            {
                return new BorderDataZ(a.similarBiome, b.x1, a.x2, a.y1, a.y2, a.z1, a.z2);
            } else
            {
                return new BorderDataZ(a.similarBiome, a.x1, b.x2, a.y1, a.y2, a.z1, a.z2);
            }
        } else
        {
            if (a.y1 == b.y2)
            {
                return new BorderDataZ(a.similarBiome, a.x1, a.x2, b.y1, a.y2, a.z1, a.z2);
            } else
            {
                return new BorderDataZ(a.similarBiome, a.x1, a.x2, a.y1, b.y2, a.z1, a.z2);
            }
        }
    }

    static public BorderDataZ newBorder(Int3 a, Int3 b, boolean similarBiome)
    {
        if (a.getZ() < b.getZ())
        {
            return new BorderDataZ(similarBiome, a.getX(), a.getX() + 1, a.getY(), a.getY() + 1, b.getZ() - offset,
                    b.getZ() + offset);
        } else
        {
            return new BorderDataZ(similarBiome, a.getX(), a.getX() + 1, a.getY(), a.getY() + 1, a.getZ() - offset,
                    a.getZ() + offset);
        }
    }

    @Override public boolean canNotMerge(BorderDataBase border)
    {
        if (super.canNotMerge(border))
        {
            return true;
        }
        if (!(border instanceof BorderDataZ))
        {
            return true;
        }
        BorderDataZ other = (BorderDataZ) border;
        if (equals(border))
        {
            return false;
        }
        if (z1 != other.z1)
        {
            return true;
        }
        if (z2 != other.z2)
        {
            return true;
        }
        if (y1 == other.y1 && y2 == other.y2)
        {
            return x1 != other.x2 && x2 != other.x1;
        } else if (x1 == other.x1 && x2 == other.x2)
        {
            return y1 != other.y2 && y2 != other.y1;
        }
        return true;
    }

    @Override public void draw(Matrix4f matrix, IVertexBuilder builder)
    {
        Drawer drawer = getDrawer(matrix, builder);
        // -z side
        drawer.DrawSegment(x1, y1, z1);
        drawer.DrawSegment(x1, y2, z1);
        drawer.DrawSegment(x2, y2, z1);
        drawer.DrawSegment(x2, y1, z1);
        // +z side
        drawer.DrawSegment(x1, y1, z2);
        drawer.DrawSegment(x2, y1, z2);
        drawer.DrawSegment(x2, y2, z2);
        drawer.DrawSegment(x1, y2, z2);
    }

    @Override public boolean equals(Object obj)
    {
        if (this == obj)
        {
            return true;
        }
        if (!super.equals(obj))
        {
            return false;
        }
        if (!(obj instanceof BorderDataZ))
        {
            return false;
        }
        return true;
    }
}
