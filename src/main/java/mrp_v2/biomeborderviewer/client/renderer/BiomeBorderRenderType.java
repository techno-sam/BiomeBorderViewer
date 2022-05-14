package mrp_v2.biomeborderviewer.client.renderer;

import net.minecraft.client.render.RenderLayer;
import net.minecraft.client.render.VertexFormat;
import net.minecraft.client.render.VertexFormats;

public abstract class BiomeBorderRenderType extends RenderLayer
{
    private static RenderLayer BIOME_BORDER = null;

    private BiomeBorderRenderType(String nameIn, VertexFormat formatIn, VertexFormat.DrawMode drawModeIn, int bufferSizeIn,
                                  boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn)
    {
        super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
    }

    public static void initBiomeBorderRenderType()
    {
        BIOME_BORDER = RenderLayer.of("biome_border", VertexFormats.POSITION_COLOR, VertexFormat.DrawMode.QUADS, 256, false, true,
                MultiPhaseParameters.builder().transparency(TRANSLUCENT_TRANSPARENCY).writeMaskState(ALL_MASK)
                        .build(false));
    }

    public static RenderLayer getBiomeBorder()
    {
        return BIOME_BORDER;
    }
}
