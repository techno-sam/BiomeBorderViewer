package mrp_v2.biomeborderviewer.util;

import mrp_v2.biomeborderviewer.BiomeBorderViewer;
import mrp_v2.biomeborderviewer.client.renderer.debug.VisualizeBorders;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;

@Mod.EventBusSubscriber(value = Dist.CLIENT, modid = BiomeBorderViewer.ID) public class EventHandler
{
    @SubscribeEvent public static void clientSetup(FMLClientSetupEvent event)
    {
        ClientRegistry.registerKeyBinding(ObjectHolder.SHOW_BORDERS);
        VisualizeBorders.BiomeBorderRenderType.initBiomeBorderRenderType();
    }

    @SubscribeEvent(priority = EventPriority.LOW) public static void chunkLoad(ChunkEvent.Load event)
    {
        VisualizeBorders.chunkLoad(event.getWorld(), event.getChunk().getPos());
    }

    @SubscribeEvent(priority = EventPriority.LOW) public static void chunkUnload(ChunkEvent.Unload event)
    {
        VisualizeBorders.chunkUnload(event.getWorld(), event.getChunk().getPos());
    }

    @SubscribeEvent public static void keyPressed(InputEvent.KeyInputEvent event)
    {
        if (ObjectHolder.SHOW_BORDERS.isPressed())
        {
            VisualizeBorders.bordersKeyPressed();
        }
    }

    @SubscribeEvent public static void renderEvent(RenderWorldLastEvent event)
    {
        VisualizeBorders.renderEvent(event.getPartialTicks(), event.getMatrixStack());
    }

    @SubscribeEvent(priority = EventPriority.LOW) public static void worldUnload(WorldEvent.Unload event)
    {
        VisualizeBorders.worldUnload(event.getWorld());
    }
}
