package mrp_v2.biomeborderviewer;

import mrp_v2.biomeborderviewer.client.Config;
import net.minecraftforge.fml.ExtensionPoint;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.network.FMLNetworkConstants;
import org.apache.commons.lang3.tuple.Pair;

@Mod(BiomeBorderViewer.ID) public class BiomeBorderViewer
{
    public static final String ID = "biome" + "border" + "viewer";
    public static final String DISPLAY_NAME = "Biome Border Viewer";

    public BiomeBorderViewer()
    {
        ModLoadingContext context = ModLoadingContext.get();
        context.registerConfig(ModConfig.Type.CLIENT, Config.clientSpec);
        context.registerExtensionPoint(ExtensionPoint.DISPLAYTEST,
                () -> Pair.of(() -> FMLNetworkConstants.IGNORESERVERONLY, (a, b) -> true));
    }
}
