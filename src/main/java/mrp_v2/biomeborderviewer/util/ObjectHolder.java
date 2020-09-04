package mrp_v2.biomeborderviewer.util;

import mrp_v2.biomeborderviewer.BiomeBorderViewer;
import net.minecraft.client.settings.KeyBinding;
import org.lwjgl.glfw.GLFW;

public class ObjectHolder
{
    public static final KeyBinding SHOW_BORDERS =
            new KeyBinding(BiomeBorderViewer.ID + ".key.showBorders", GLFW.GLFW_KEY_B,
                    BiomeBorderViewer.ID + ".key.categories");
}
