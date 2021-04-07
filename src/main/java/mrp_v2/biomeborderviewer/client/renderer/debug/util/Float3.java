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
        return new Float3(Math.min(a.x(), b.x()), Math.min(a.y(), b.y()), Math.min(a.z(), b.z()));
    }

    public static Float3 max(Float3 a, Float3 b)
    {
        return new Float3(Math.max(a.x(), b.x()), Math.max(a.y(), b.y()), Math.max(a.z(), b.z()));
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
                return this.x();
            case Y:
                return this.y();
            case Z:
                return this.z();
            default:
                throw new IllegalArgumentException("Can not get the value of axis '" + axis + "'");
        }
    }

    public Float3 addOnOtherAxes(float f, Direction.Axis axis)
    {
        switch (axis)
        {
            case X:
                return new Float3(this.x(), this.y() + f, this.z() + f);
            case Y:
                return new Float3(this.x() + f, this.y(), this.z() + f);
            case Z:
                return new Float3(this.x() + f, this.y() + f, this.z());
            default:
                throw new IllegalArgumentException("Can not get the value of axis '" + axis + "'");
        }
    }

    public Float3 addOnAxis(float f, Direction.Axis axis)
    {
        switch (axis)
        {
            case X:
                return new Float3(this.x() + f, this.y(), this.z());
            case Y:
                return new Float3(this.x(), this.y() + f, this.z());
            case Z:
                return new Float3(this.x(), this.y(), this.z() + f);
            default:
                throw new IllegalArgumentException("Can not get the value of axis '" + axis + "'");
        }
    }
}
