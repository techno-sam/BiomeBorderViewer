package com.mrp_v2.biomeborderviewer.client.renderer.debug;

import java.util.UUID;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.BiomeBorderViewer;
import com.mrp_v2.biomeborderviewer.client.renderer.debug.util.BiomeBorderDataCollection;
import com.mrp_v2.biomeborderviewer.client.renderer.debug.util.Color;
import com.mrp_v2.biomeborderviewer.config.Config;
import com.mrp_v2.biomeborderviewer.util.Util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.Entity;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod.EventBusSubscriber;

@EventBusSubscriber(value = Dist.CLIENT)
public class VisualizeBorders {

	private abstract static class BiomeBorderRenderType extends RenderType {

		private static final RenderType BIOME_BORDER = RenderType.makeType("biome_border",
				DefaultVertexFormats.POSITION_COLOR, 7, 262144, false, true,
				RenderType.State.getBuilder()
						.transparency(TRANSLUCENT_TRANSPARENCY)
						.writeMask(COLOR_DEPTH_WRITE)
						.build(false));

		public BiomeBorderRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
				boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
			super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
		}

		public static RenderType getBiomeBorder() {
			return BIOME_BORDER;
		}
	}

	private static boolean showingBorders;

	private static int horizontalViewRange;
	private static int verticalViewRange;

	private static Color COLOR_A = new Color();
	private static Color COLOR_B = new Color();

	private static BiomeBorderDataCollection biomeBorderData = new BiomeBorderDataCollection();

	public static Color borderColor(boolean isSimilar) {
		if (isSimilar) {
			return COLOR_A;
		} else {
			return COLOR_B;
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void chunkLoad(ChunkEvent.Load event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		biomeBorderData.chunkLoaded(event.getChunk().getPos());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void chunkUnload(ChunkEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		biomeBorderData.chunkUnloaded(event.getChunk().getPos());
	}

	public static int getVerticalViewRange() {
		return verticalViewRange;
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void keyPressed(KeyInputEvent event) {
		if (BiomeBorderViewer.SHOW_BORDERS.isPressed()) {
			if (biomeBorderData.areAnyChunksLoaded()) {
				return;
			}
			showingBorders = !showingBorders;
			LogManager.getLogger().debug("Show Borders hotkey pressed, showingBorders is now " + showingBorders);
			Minecraft.getInstance().player.sendMessage(
					new StringTextComponent("Showing borders is now " + showingBorders), UUID.randomUUID());
		}
	}

	public static void loadConfigSettings() {
		horizontalViewRange = Config.CLIENT.horizontalViewRange.get();
		verticalViewRange = Config.CLIENT.verticalViewRange.get();
		COLOR_A.set(Config.getColorA());
		COLOR_B.set(Config.getColorB());
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		if (showingBorders) {
			renderBorders(event.getPartialTicks(), event.getMatrixStack());
		}
	}

	private static void renderBorders(float partialTicks, MatrixStack stack) {
		Entity player = Minecraft.getInstance().getRenderViewEntity();
		double cameraX = player.lastTickPosX + (player.getPosX() - player.lastTickPosX) * (double) partialTicks;
		double cameraY = player.lastTickPosY + (player.getPosY() - player.lastTickPosY) * (double) partialTicks
				+ player.getEyeHeight(player.getPose());
		double cameraZ = player.lastTickPosZ + (player.getPosZ() - player.lastTickPosZ) * (double) partialTicks;
		Vector3d playerPos = new Vector3d(cameraX, cameraY, cameraZ);
		IVertexBuilder builder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource()
				.getBuffer(BiomeBorderRenderType.getBiomeBorder());
		stack.push();
		stack.translate(-playerPos.x, -playerPos.y, -playerPos.z);
		Matrix4f matrix = stack.getLast().getMatrix();
		int playerY = ((int) playerPos.getY()) >> 4;
		biomeBorderData.renderBorders(Util.getChunkSquare(horizontalViewRange, playerPos), matrix, builder, playerY,
				player.world);
		// test draw
		builder.pos(matrix, 25, 80, 40).color(255, 0, 0, 255).endVertex();
		builder.pos(matrix, 25, 80, 41).color(255, 0, 0, 255).endVertex();
		builder.pos(matrix, 26, 80, 41).color(255, 0, 0, 255).endVertex();
		builder.pos(matrix, 26, 80, 40).color(255, 0, 0, 255).endVertex();
		// test draw
		builder.pos(matrix, 26, 80, 40).color(0, 0, 255, 128).endVertex();
		builder.pos(matrix, 26, 80, 41).color(0, 0, 255, 128).endVertex();
		builder.pos(matrix, 27, 80, 41).color(0, 0, 255, 128).endVertex();
		builder.pos(matrix, 27, 80, 40).color(0, 0, 255, 128).endVertex();
		// test draw
		builder.pos(matrix, 27, 80, 40).color(0, 255, 0, 64).endVertex();
		builder.pos(matrix, 27, 80, 41).color(0, 255, 0, 64).endVertex();
		builder.pos(matrix, 28, 80, 41).color(0, 255, 0, 64).endVertex();
		builder.pos(matrix, 28, 80, 40).color(0, 255, 0, 64).endVertex();
		// end test draw
		stack.pop();
		Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(BiomeBorderRenderType.getBiomeBorder());
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void worldUnload(WorldEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		biomeBorderData.worldUnloaded();
		biomeBorderData = new BiomeBorderDataCollection();
	}
}
