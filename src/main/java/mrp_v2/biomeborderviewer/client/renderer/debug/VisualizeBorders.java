package mrp_v2.biomeborderviewer.client.renderer.debug;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mojang.blaze3d.systems.RenderSystem;
import mrp_v2.biomeborderviewer.BiomeBorderViewer;
import mrp_v2.biomeborderviewer.client.renderer.BiomeBorderRenderType;
import mrp_v2.biomeborderviewer.client.renderer.debug.util.BiomeBorderDataCollection;
import mrp_v2.biomeborderviewer.client.renderer.debug.util.Int3;
import mrp_v2.biomeborderviewer.util.Util;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.render.*;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.client.util.math.Vector3d;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.text.LiteralText;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Matrix4f;
import net.minecraft.world.WorldAccess;
import net.minecraft.world.chunk.ChunkStatus;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

import java.awt.*;

public class VisualizeBorders
{
    private static Color COLOR_A;
    private static Color COLOR_B;
    private static boolean showingBorders;
    private static int horizontalViewRange, verticalViewRange;
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

    public static void chunkLoad(WorldAccess world, ChunkPos chunkPos)
    {
        if (!(world instanceof ClientWorld))
        {
            return;
        }
        if (world.getChunk(chunkPos.x, chunkPos.z).getStatus() != ChunkStatus.FULL)
        {
            return;
        }
        for (int y = 0; y < 16; y++)
        {
            biomeBorderData.chunkLoaded(new Int3(chunkPos.x, y, chunkPos.z));
        }
    }

    public static void chunkUnload(WorldAccess world, ChunkPos chunkPos)
    {
        if (!(world instanceof ClientWorld))
        {
            return;
        }
        for (int y = 0; y < 16; y++)
        {
            biomeBorderData.chunkUnloaded(new Int3(chunkPos.x, y, chunkPos.z));
        }
    }

    public static void bordersKeyPressed()
    {
        if (biomeBorderData.areNoChunksLoaded())
        {
            return;
        }
        showingBorders = !showingBorders;
        LogManager.getLogger().debug("Show Borders hotkey pressed, showingBorders is now " + showingBorders);
        if (MinecraftClient.getInstance().player != null) {
            MinecraftClient.getInstance().player
                    .sendMessage(new LiteralText("Showing borders is now " + showingBorders), true);
        }
    }

    public static void loadConfigSettings()
    {
        horizontalViewRange = BiomeBorderViewer.config.horizontalViewRange;
        verticalViewRange = BiomeBorderViewer.config.verticalViewRange;
        COLOR_A = new Color(BiomeBorderViewer.config.borderAColor.toInt(), true);
        COLOR_B = new Color(BiomeBorderViewer.config.borderBColor.toInt(), true);
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
        Entity player = MinecraftClient.getInstance().getCameraEntity();
        if (player != null) {
            double cameraX = player.prevX + (player.getX() - player.prevX) * (double) partialTicks;
            double cameraY = player.prevY + (player.getY() - player.prevY) * (double) partialTicks +
                    player.getEyeHeight(player.getPose());
            double cameraZ = player.prevZ + (player.getZ() - player.prevZ) * (double) partialTicks;
            Vector3d playerPos = new Vector3d(cameraX, cameraY, cameraZ);

            //Setup
            RenderSystem.enableDepthTest();
            RenderSystem.setShader(GameRenderer::getPositionColorShader);
            //RenderSystem.setShaderColor(1f, 1f, 1f, 1f);
            RenderSystem.depthMask(true);
            RenderSystem.enableBlend();
            RenderSystem.blendFuncSeparate(GlStateManager.SrcFactor.SRC_ALPHA, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA, GlStateManager.SrcFactor.ONE, GlStateManager.DstFactor.ONE_MINUS_SRC_ALPHA);
            RenderSystem.disableTexture();

            //Render
            stack.push();
            Tessellator tessellator = Tessellator.getInstance();
            BufferBuilder buffer = tessellator.getBuffer();
            buffer.begin(VertexFormat.DrawMode.QUADS, VertexFormats.POSITION_COLOR);

            RenderSystem.applyModelViewMatrix();
            stack.translate(-playerPos.x, -playerPos.y, -playerPos.z);

            //Push Data
            Matrix4f matrix = stack.peek().getModel();
            biomeBorderData.renderBorders(Util.getChunkColumn(horizontalViewRange, verticalViewRange,
                    new Int3((int) (playerPos.x / 16), (int) (playerPos.y / 16), (int) (playerPos.z / 16))), matrix,
                    buffer, player.world);

            //Draw
            tessellator.draw();
            stack.pop();
            RenderSystem.applyModelViewMatrix();

            //Cleanup
            RenderSystem.enableBlend();
            RenderSystem.enableTexture();
            //RenderSystem.defaultBlendFunc();
            RenderSystem.depthMask(true);
        }
    }

    public static void worldUnload(WorldAccess world)
    {
        if (!(world instanceof ClientWorld))
        {
            return;
        }
        biomeBorderData.worldUnloaded();
        biomeBorderData = new BiomeBorderDataCollection();
    }
}
