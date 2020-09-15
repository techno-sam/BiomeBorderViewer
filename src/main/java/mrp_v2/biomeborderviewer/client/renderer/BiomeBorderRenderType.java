package mrp_v2.biomeborderviewer.client.renderer;

import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;

public abstract class BiomeBorderRenderType extends RenderType
{
    private static RenderType BIOME_BORDER = null;

    private BiomeBorderRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
            boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn)
    {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static void initBiomeBorderRenderType()
    {
        BIOME_BORDER = RenderType.makeType("biome_border", DefaultVertexFormats.POSITION_COLOR, 7, 262144, false, true,
                State.getBuilder().transparency(TRANSLUCENT_TRANSPARENCY).writeMask(COLOR_DEPTH_WRITE).build(false));
    }

    public static RenderType getBiomeBorder()
    {
        return BIOME_BORDER;
    }
}
