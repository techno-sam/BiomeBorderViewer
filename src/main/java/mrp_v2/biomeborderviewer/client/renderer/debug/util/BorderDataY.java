package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Matrix4f;

public class BorderDataY extends BorderDataBase
{
    private BorderDataY(boolean similarBiome, float x1, float x2, float y1, float y2, float z1, float z2)
    {
        super(similarBiome, x1, x2, y1, y2, z1, z2);
    }

    static public BorderDataY merge(BorderDataY a, BorderDataY b)
    {
        if (a.x1 == b.x1 && a.x2 == b.x2)
        {
            if (a.z1 == b.z2)
            {
                return new BorderDataY(a.similarBiome, a.x1, a.x2, a.y1, a.y2, b.z1, a.z2);
            } else
            {
                return new BorderDataY(a.similarBiome, a.x1, a.x2, a.y1, a.y2, a.z1, b.z2);
            }
        } else
        {
            if (a.x1 == b.x2)
            {
                return new BorderDataY(a.similarBiome, b.x1, a.x2, a.y1, a.y2, a.z1, a.z2);
            } else
            {
                return new BorderDataY(a.similarBiome, a.x1, b.x2, a.y1, a.y2, a.z1, a.z2);
            }
        }
    }

    static public BorderDataY newBorder(Int3 a, Int3 b, boolean similarBiome)
    {
        if (a.getY() < b.getY())
        {
            return new BorderDataY(similarBiome, a.getX(), a.getX() + 1, b.getY() - offset, b.getY() + offset, a.getZ(),
                    a.getZ() + 1);
        } else
        {
            return new BorderDataY(similarBiome, a.getX(), a.getX() + 1, a.getY() - offset, a.getY() + offset, a.getZ(),
                    a.getZ() + 1);
        }
    }

    @Override public boolean canNotMerge(BorderDataBase border)
    {
        if (super.canNotMerge(border))
        {
            return true;
        }
        if (!(border instanceof BorderDataY))
        {
            return true;
        }
        BorderDataY other = (BorderDataY) border;
        if (equals(border))
        {
            return false;
        }
        if (y1 != other.y1)
        {
            return true;
        }
        if (y2 != other.y2)
        {
            return true;
        }
        if (x1 == other.x1 && x2 == other.x2)
        {
            return z1 != other.z2 && z2 != other.z1;
        } else if (z1 == other.z1 && z2 == other.z2)
        {
            return x1 != other.x2 && x2 != other.x1;
        }
        return true;
    }

    @Override public void draw(Matrix4f matrix, IVertexBuilder builder)
    {
        Drawer drawer = getDrawer(matrix, builder);
        // -y side
        drawer.DrawSegment(x1, y1, z1);
        drawer.DrawSegment(x2, y1, z1);
        drawer.DrawSegment(x2, y1, z2);
        drawer.DrawSegment(x1, y1, z2);
        // +y side
        drawer.DrawSegment(x1, y2, z1);
        drawer.DrawSegment(x1, y2, z2);
        drawer.DrawSegment(x2, y2, z2);
        drawer.DrawSegment(x2, y2, z1);
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
        if (!(obj instanceof BorderDataY))
        {
            return false;
        }
        return true;
    }
}
