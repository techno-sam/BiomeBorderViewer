package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import mrp_v2.biomeborderviewer.client.renderer.debug.VisualizeBorders;
import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Matrix4f;

import java.awt.*;
import java.util.Objects;

public class BorderData
{
    private static final float offset = 1f / 0b11111111;
    private final Float3 min, max;
    private final boolean similarBiome;
    private final Direction.Axis axis;
    private final Direction.Axis[] otherAxes;
    private Color color;

    private BorderData(Float3 min, Float3 max, boolean similarBiome, Direction.Axis axis, Direction.Axis[] otherAxes)
    {
        this.min = min;
        this.max = max;
        this.similarBiome = similarBiome;
        this.axis = axis;
        this.otherAxes = otherAxes;
        updateColor();
    }

    public void updateColor()
    {
        this.color = VisualizeBorders.borderColor(this.similarBiome);
    }

    public BorderData(boolean similarBiome, Int3 a, Int3 b)
    {
        Int3 min = Int3.min(a, b);
        Int3 max = Int3.max(a, b);
        this.similarBiome = similarBiome;
        if (min.getX() != max.getX())
        {
            if (min.getY() != max.getY() || min.getZ() != max.getZ())
            {
                throw new IllegalArgumentException("Incorrect arguments for border data!");
            }
            this.axis = Direction.Axis.X;
            this.otherAxes = new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.Z};
        } else if (min.getY() != max.getY())
        {
            if (min.getZ() != max.getZ())
            {
                throw new IllegalArgumentException("Incorrect arguments for border data!");
            }
            this.axis = Direction.Axis.Y;
            this.otherAxes = new Direction.Axis[]{Direction.Axis.Z, Direction.Axis.X};
        } else if (min.getZ() != max.getZ())
        {
            this.axis = Direction.Axis.Z;
            this.otherAxes = new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Y};
        } else
        {
            throw new IllegalArgumentException("Incorrect arguments for border data!");
        }
        Float3 float3 = new Float3(max.getX(), max.getY(), max.getZ());
        this.min = float3.addOnAxis(-offset, this.axis);
        this.max = float3.addOnAxis(offset, this.axis).addOnOtherAxes(1.0F, this.axis);
        updateColor();
    }

    /**
     * Assumes borders can be merged, ensure {@link BorderData#canMerge(BorderData)} is true before calling.
     */
    public static BorderData merge(BorderData a, BorderData b)
    {
        return new BorderData(Float3.min(a.min, b.min), Float3.max(a.max, b.max), a.similarBiome, a.axis, a.otherAxes);
    }

    @Override public int hashCode()
    {
        return Objects.hash(min, max, similarBiome, axis);
    }

    @Override public boolean equals(Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (!(o instanceof BorderData))
        {
            return false;
        }
        BorderData that = (BorderData) o;
        return similarBiome == that.similarBiome && min.equals(that.min) && max.equals(that.max) && axis == that.axis;
    }

    public boolean canMergeOnAxis(BorderData other, Direction.Axis mergeAxis)
    {
        if (this.axis == mergeAxis)
        {
            return false;
        }
        if (other.axis == mergeAxis)
        {
            return false;
        }
        if (this.min.getValueOnAxis(mergeAxis) == other.min.getValueOnAxis(mergeAxis))
        {
            return false;
        }
        return canMerge(other);
    }

    public boolean canMerge(BorderData other)
    {
        if (this.similarBiome != other.similarBiome)
        {
            return false;
        }
        if (!this.axis.equals(other.axis))
        {
            return false;
        }
        if (this.min.getValueOnAxis(this.axis) != other.min.getValueOnAxis(this.axis))
        {
            return false;
        }
        if (this.max.getValueOnAxis(this.axis) != other.max.getValueOnAxis(this.axis))
        {
            return false;
        }
        Direction.Axis otherAx1 = this.otherAxes[0];
        Direction.Axis otherAx2 = this.otherAxes[1];
        if (this.min.areValuesOnAxisEqual(other.min, otherAx1) && this.max.areValuesOnAxisEqual(other.max, otherAx1))
        {
            return this.min.areValuesOnAxisEqual(other.max, otherAx2) ||
                    this.max.areValuesOnAxisEqual(other.min, otherAx2);
        } else if (this.min.areValuesOnAxisEqual(other.min, otherAx2) &&
                this.max.areValuesOnAxisEqual(other.max, otherAx2))
        {
            return this.min.areValuesOnAxisEqual(other.max, otherAx1) ||
                    this.max.areValuesOnAxisEqual(other.min, otherAx1);
        }
        return false;
    }

    public void draw(Matrix4f matrix, IVertexBuilder builder)
    {
        Drawer drawer = getDrawer(matrix, builder);
        switch (this.axis)
        {
            case X:
                // -x side
                drawer.drawSegment(this.min.getX(), this.min.getY(), this.min.getZ());
                drawer.drawSegment(this.min.getX(), this.min.getY(), this.max.getZ());
                drawer.drawSegment(this.min.getX(), this.max.getY(), this.max.getZ());
                drawer.drawSegment(this.min.getX(), this.max.getY(), this.min.getZ());
                // +x side
                drawer.drawSegment(this.max.getX(), this.min.getY(), this.min.getZ());
                drawer.drawSegment(this.max.getX(), this.max.getY(), this.min.getZ());
                drawer.drawSegment(this.max.getX(), this.max.getY(), this.max.getZ());
                drawer.drawSegment(this.max.getX(), this.min.getY(), this.max.getZ());
                break;
            case Y:
                // -y side
                drawer.drawSegment(this.min.getX(), this.min.getY(), this.min.getZ());
                drawer.drawSegment(this.max.getX(), this.min.getY(), this.min.getZ());
                drawer.drawSegment(this.max.getX(), this.min.getY(), this.max.getZ());
                drawer.drawSegment(this.min.getX(), this.min.getY(), this.max.getZ());
                // +y side
                drawer.drawSegment(this.min.getX(), this.max.getY(), this.min.getZ());
                drawer.drawSegment(this.min.getX(), this.max.getY(), this.max.getZ());
                drawer.drawSegment(this.max.getX(), this.max.getY(), this.max.getZ());
                drawer.drawSegment(this.max.getX(), this.max.getY(), this.min.getZ());
                break;
            case Z:
                // -z side
                drawer.drawSegment(this.min.getX(), this.min.getY(), this.min.getZ());
                drawer.drawSegment(this.min.getX(), this.max.getY(), this.min.getZ());
                drawer.drawSegment(this.max.getX(), this.max.getY(), this.min.getZ());
                drawer.drawSegment(this.max.getX(), this.min.getY(), this.min.getZ());
                // +z side
                drawer.drawSegment(this.min.getX(), this.min.getY(), this.max.getZ());
                drawer.drawSegment(this.max.getX(), this.min.getY(), this.max.getZ());
                drawer.drawSegment(this.max.getX(), this.max.getY(), this.max.getZ());
                drawer.drawSegment(this.min.getX(), this.max.getY(), this.max.getZ());
                break;
            default:
                throw new IllegalStateException("Cannot have axis '" + this.axis + "'");
        }
    }

    private Drawer getDrawer(Matrix4f matrix, IVertexBuilder builder)
    {
        return new Drawer(matrix, builder);
    }

    private class Drawer
    {
        private final Matrix4f matrix;
        private final IVertexBuilder builder;

        public Drawer(Matrix4f matrix, IVertexBuilder builder)
        {
            this.matrix = matrix;
            this.builder = builder;
        }

        public void drawSegment(float x, float y, float z)
        {
            builder.pos(matrix, x, y, z).color(color.getRed(), color.getGreen(), color.getBlue(), color.getAlpha())
                    .endVertex();
        }
    }
}
