package mrp_v2.biomeborderviewer.client.renderer.debug;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import mrp_v2.biomeborderviewer.client.renderer.debug.util.BiomeBorderDataCollection;
import mrp_v2.biomeborderviewer.client.renderer.debug.util.Color;
import mrp_v2.biomeborderviewer.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import org.apache.logging.log4j.LogManager;

import java.util.UUID;

public class VisualizeBorders
{
    private static final Color COLOR_A = new Color();
    private static final Color COLOR_B = new Color();
    private static boolean showingBorders;
    private static int horizontalViewRange;
    private static int verticalViewRange;
    private static BiomeBorderDataCollection biomeBorderData = new BiomeBorderDataCollection();

    public static Color borderColor(boolean isSimilar)
    {
        if (isSimilar)
        {
            return COLOR_A;
        } else
        {
            return COLOR_B;
        }
    }

    public static void chunkLoad(IWorld world, ChunkPos chunkPos)
    {
        if (!(world instanceof ClientWorld))
        {
            return;
        }
        biomeBorderData.chunkLoaded(chunkPos);
    }

    public static void chunkUnload(IWorld world, ChunkPos chunkPos)
    {
        if (!(world instanceof ClientWorld))
        {
            return;
        }
        biomeBorderData.chunkUnloaded(chunkPos);
    }

    public static int getVerticalViewRange()
    {
        return verticalViewRange;
    }

    @SuppressWarnings("resource") public static void bordersKeyPressed()
    {
        if (biomeBorderData.areNoChunksLoaded())
        {
            return;
        }
        showingBorders = !showingBorders;
        LogManager.getLogger().debug("Show Borders hotkey pressed, showingBorders is now " + showingBorders);
        Minecraft.getInstance().player.sendMessage(new StringTextComponent("Showing borders is now " + showingBorders),
                UUID.randomUUID());
    }

    public static void loadConfigSettings()
    {
        horizontalViewRange = mrp_v2.biomeborderviewer.config.ClientConfig.CLIENT.horizontalViewRange.get();
        verticalViewRange = mrp_v2.biomeborderviewer.config.ClientConfig.CLIENT.verticalViewRange.get();
        COLOR_A.set(mrp_v2.biomeborderviewer.config.ClientConfig.getColorA());
        COLOR_B.set(mrp_v2.biomeborderviewer.config.ClientConfig.getColorB());
    }

    public static void renderEvent(float partialTicks, MatrixStack matrixStack)
    {
        if (showingBorders)
        {
            renderBorders(partialTicks, matrixStack);
        }
    }

    private static void renderBorders(float partialTicks, MatrixStack stack)
    {
        Entity player = Minecraft.getInstance().getRenderViewEntity();
        double cameraX = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * (double) partialTicks;
        double cameraY = player.lastTickPosY +
                (player.getPosY() - player.lastTickPosY) * (double) partialTicks +
                player.getEyeHeight(player.getPose());
        double cameraZ = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * (double) partialTicks;
        Vector3d playerPos = new Vector3d(cameraX, cameraY, cameraZ);
        IVertexBuilder builder = Minecraft.getInstance()
                .getRenderTypeBuffers()
                .getBufferSource()
                .getBuffer(BiomeBorderRenderType.getBiomeBorder());
        stack.push();
        stack.translate(-playerPos.x, -playerPos.y, -playerPos.z);
        Matrix4f matrix = stack.getLast().getMatrix();
        int playerY = ((int) playerPos.getY()) >> 4;
        biomeBorderData.renderBorders(Util.getChunkSquare(horizontalViewRange, playerPos), matrix, builder, playerY,
                player.world);
        stack.pop();
        Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(BiomeBorderRenderType.getBiomeBorder());
    }

    public static void worldUnload(IWorld world)
    {
        if (!(world instanceof ClientWorld))
        {
            return;
        }
        biomeBorderData.worldUnloaded();
        biomeBorderData = new BiomeBorderDataCollection();
    }

    public abstract static class BiomeBorderRenderType extends RenderType
    {
        private static RenderType BIOME_BORDER = null;

        private BiomeBorderRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
                boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn)
        {
            super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
        }

        public static void initBiomeBorderRenderType()
        {
            BIOME_BORDER =
                    RenderType.makeType("biome_border", DefaultVertexFormats.POSITION_COLOR, 7, 262144, false, true,
                            RenderType.State.getBuilder()
                                    .transparency(TRANSLUCENT_TRANSPARENCY)
                                    .writeMask(COLOR_DEPTH_WRITE)
                                    .build(false));
        }

        public static RenderType getBiomeBorder()
        {
            return BIOME_BORDER;
        }
    }
}
