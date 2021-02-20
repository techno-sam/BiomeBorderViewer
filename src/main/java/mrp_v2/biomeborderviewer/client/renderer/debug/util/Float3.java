package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import net.minecraft.util.Direction;
import net.minecraft.util.math.vector.Vector3f;

public class Float3 extends Vector3f
{
    public Float3(float x, float y, float z)
    {
        super(x, y, z);
    }

    public static Float3 min(Float3 a, Float3 b)
    {
        return new Float3(Math.min(a.getX(), b.getX()), Math.min(a.getY(), b.getY()), Math.min(a.getZ(), b.getZ()));
    }

    public static Float3 max(Float3 a, Float3 b)
    {
        return new Float3(Math.max(a.getX(), b.getX()), Math.max(a.getY(), b.getY()), Math.max(a.getZ(), b.getZ()));
    }

    public boolean areValuesOnAxisEqual(Float3 other, Direction.Axis axis)
    {
        return this.getValueOnAxis(axis) == other.getValueOnAxis(axis);
    }

    public float getValueOnAxis(Direction.Axis axis)
    {
        switch (axis)
        {
            case X:
                return this.getX();
            case Y:
                return this.getY();
            case Z:
                return this.getZ();
            default:
                throw new IllegalArgumentException("Can not get the value of axis '" + axis + "'");
        }
    }

    public Float3 addOnOtherAxes(float f, Direction.Axis axis)
    {
        switch (axis)
        {
            case X:
                return new Float3(this.getX(), this.getY() + f, this.getZ() + f);
            case Y:
                return new Float3(this.getX() + f, this.getY(), this.getZ() + f);
            case Z:
                return new Float3(this.getX() + f, this.getY() + f, this.getZ());
            default:
                throw new IllegalArgumentException("Can not get the value of axis '" + axis + "'");
        }
    }

    public Float3 addOnAxis(float f, Direction.Axis axis)
    {
        switch (axis)
        {
            case X:
                return new Float3(this.getX() + f, this.getY(), this.getZ());
            case Y:
                return new Float3(this.getX(), this.getY() + f, this.getZ());
            case Z:
                return new Float3(this.getX(), this.getY(), this.getZ() + f);
            default:
                throw new IllegalArgumentException("Can not get the value of axis '" + axis + "'");
        }
    }
}
