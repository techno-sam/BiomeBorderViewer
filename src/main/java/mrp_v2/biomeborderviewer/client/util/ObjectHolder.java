package mrp_v2.biomeborderviewer.client.util;

import net.minecraft.util.math.Direction;

import java.util.LinkedHashMap;
import java.util.Map;

public class ObjectHolder
{
    public static final Map<Direction.Axis, Direction.Axis[]> AXIS_TO_OTHER_AXES_MAP = new LinkedHashMap<>();

    static
    {
        AXIS_TO_OTHER_AXES_MAP.put(Direction.Axis.X, new Direction.Axis[]{Direction.Axis.Y, Direction.Axis.Z});
        AXIS_TO_OTHER_AXES_MAP.put(Direction.Axis.Y, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Z});
        AXIS_TO_OTHER_AXES_MAP.put(Direction.Axis.Z, new Direction.Axis[]{Direction.Axis.X, Direction.Axis.Y});
    }
}
