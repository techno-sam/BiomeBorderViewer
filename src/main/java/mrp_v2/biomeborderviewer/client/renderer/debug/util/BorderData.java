package mrp_v2.biomeborderviewer.client.renderer.debug.util;

import net.minecraft.util.Direction;

import java.util.Objects;

public abstract class BorderData
{
    private static final float offset = 1f / 0b11111111;
    protected final Float3 min, max;

    protected BorderData(Float3 min, Float3 max)
    {
        this.min = min;
        this.max = max;
    }

    public static BorderData from(Int3 a, Int3 b)
    {
        Int3 min = Int3.min(a, b);
        Int3 max = Int3.max(a, b);
        if (min.getX() != max.getX())
        {
            if (min.getY() != max.getY() || min.getZ() != max.getZ())
            {
                throw new IllegalArgumentException("Incorrect arguments for border data!");
            }
            return new X(new Float3(max.getX(), max.getY(), max.getZ()));
        } else if (min.getY() != max.getY())
        {
            if (min.getZ() != max.getZ())
            {
                throw new IllegalArgumentException("Incorrect arguments for border data!");
            }
            return new Y(new Float3(max.getX(), max.getY(), max.getZ()));
        } else if (min.getZ() != max.getZ())
        {
            return new Z(new Float3(max.getX(), max.getY(), max.getZ()));
        } else
        {
            throw new IllegalArgumentException("Incorrect arguments for border data!");
        }
    }

    /**
     * Assumes borders can be merged, ensure {@link BorderData#canMerge(BorderData)} is true before calling.
     */
    public static BorderData merge(BorderData a, BorderData b)
    {
        switch (a.getAxis())
        {
            case X:
                return new X(Float3.min(a.min, b.min), Float3.max(a.max, b.max));
            case Y:
                return new Y(Float3.min(a.min, b.min), Float3.max(a.max, b.max));
            case Z:
                return new Z(Float3.min(a.min, b.min), Float3.max(a.max, b.max));
            default:
                throw new IllegalArgumentException("Unknown axis '" + a.getAxis() + "'");
        }
    }

    public abstract Direction.Axis getAxis();

    @Override public int hashCode()
    {
        return Objects.hash(min, max);
    }

    @Override public abstract boolean equals(Object o);

    public boolean equals(BorderData o)
    {
        return this.min.equals(o.min) && this.max.equals(o.max);
    }

    public boolean canMergeOnAxis(BorderData other, Direction.Axis mergeAxis)
    {
        if (this.getAxis() == mergeAxis)
        {
            return false;
        }
        if (other.getAxis() == mergeAxis)
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
        if (!this.getAxis().equals(other.getAxis()))
        {
            return false;
        }
        if (this.min.getValueOnAxis(this.getAxis()) != other.min.getValueOnAxis(this.getAxis()))
        {
            return false;
        }
        if (this.max.getValueOnAxis(this.getAxis()) != other.max.getValueOnAxis(this.getAxis()))
        {
            return false;
        }
        Direction.Axis otherAx1 = this.getOtherAxes()[0];
        Direction.Axis otherAx2 = this.getOtherAxes()[1];
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

    public abstract Direction.Axis[] getOtherAxes();

    public static class X extends BorderData
    {
        private static final Direction.Axis[] otherAxes = new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.Z};

        protected X(Float3 float3)
        {
            super(float3.addOnAxis(-offset, Direction.Axis.X),
                    float3.addOnAxis(offset, Direction.Axis.X).addOnOtherAxes(1.0F, Direction.Axis.X));
        }

        public X(Float3 min, Float3 max)
        {
            super(min, max);
        }

        @Override public Direction.Axis getAxis()
        {
            return Direction.Axis.X;
        }

        @Override public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }
            if (!(o instanceof X))
            {
                return false;
            }
            X other = (X) o;
            return super.equals(other);
        }

        @Override public Direction.Axis[] getOtherAxes()
        {
            return otherAxes;
        }
    }

    public static class Y extends BorderData
    {
        private static final Direction.Axis[] otherAxes = new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z};

        protected Y(Float3 float3)
        {
            super(float3.addOnAxis(-offset, Direction.Axis.Y),
                    float3.addOnAxis(offset, Direction.Axis.Y).addOnOtherAxes(1.0F, Direction.Axis.Y));
        }

        public Y(Float3 min, Float3 max)
        {
            super(min, max);
        }

        @Override public Direction.Axis getAxis()
        {
            return Direction.Axis.Y;
        }

        @Override public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }
            if (!(o instanceof Y))
            {
                return false;
            }
            Y other = (Y) o;
            return super.equals(other);
        }

        @Override public Direction.Axis[] getOtherAxes()
        {
            return otherAxes;
        }
    }

    public static class Z extends BorderData
    {
        private static final Direction.Axis[] otherAxes = new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Y};

        protected Z(Float3 float3)
        {
            super(float3.addOnAxis(-offset, Direction.Axis.Z),
                    float3.addOnAxis(offset, Direction.Axis.Z).addOnOtherAxes(1.0F, Direction.Axis.Z));
        }

        public Z(Float3 min, Float3 max)
        {
            super(min, max);
        }

        @Override public Direction.Axis getAxis()
        {
            return Direction.Axis.Z;
        }

        @Override public boolean equals(Object o)
        {
            if (o == this)
            {
                return true;
            }
            if (!(o instanceof Z))
            {
                return false;
            }
            Z other = (Z) o;
            return super.equals(other);
        }

        @Override public Direction.Axis[] getOtherAxes()
        {
            return otherAxes;
        }
    }
}
