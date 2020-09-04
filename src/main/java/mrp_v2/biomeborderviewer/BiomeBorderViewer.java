package mrp_v2.biomeborderviewer;

import mrp_v2.biomeborderviewer.config.ClientConfig;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;

@Mod(BiomeBorderViewer.ID) public class BiomeBorderViewer
{
    public static final String ID = "biome" + "border" + "viewer";
    public static final String DISPLAY_NAME = "Biome Border Viewer";

    public BiomeBorderViewer()
    {
        ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, ClientConfig.clientSpec);
    }
}
