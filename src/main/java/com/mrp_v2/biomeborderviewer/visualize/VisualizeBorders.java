package com.mrp_v2.biomeborderviewer.visualize;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.BiomeBorderViewer;
import com.mrp_v2.biomeborderviewer.config.ConfigOptions;
import com.mrp_v2.biomeborderviewer.util.CalculatedChunkData;
import com.mrp_v2.biomeborderviewer.util.Color;
import com.mrp_v2.biomeborderviewer.util.QueuedChunkData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderGameOverlayEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VisualizeBorders {

	private static class ChunkCalculator implements Runnable {

		private final QueuedChunkData data;

		public ChunkCalculator(QueuedChunkData data) {
			this.data = data;
		}

		public void run() {
			CalculatedChunkData calculatedData = new CalculatedChunkData(data);
			chunkCalculated(data.getChunkPos(), calculatedData);
		}
	}

	private static ExecutorService threadPool = Executors.newFixedThreadPool(4);

	private static boolean showingBorders;

	private static int horizontalViewRange;
	private static int verticalViewRange;

	private static Color colorA = new Color();
	private static Color colorB = new Color();

	private static HashMap<ChunkPos, CalculatedChunkData> calculatedChunks = new HashMap<ChunkPos, CalculatedChunkData>(
			128);

	private static ConcurrentHashMap<ChunkPos, CalculatedChunkData> queuedCalculatedChunks = new ConcurrentHashMap<ChunkPos, CalculatedChunkData>(
			128);

	private static Set<ChunkPos> calculatingChunks = Collections.synchronizedSet(new HashSet<ChunkPos>());

	private static HashSet<ChunkPos> loadedChunks = new HashSet<ChunkPos>();

	public static Color borderColor(boolean isSimilar) {
		if (isSimilar) {
			return colorA;
		} else {
			return colorB;
		}
	}

	private static void chunkCalculated(ChunkPos pos, CalculatedChunkData data) {
		if (calculatingChunks.contains(pos)) {
			calculatingChunks.remove(pos);
			queuedCalculatedChunks.put(pos, data);
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void chunkLoad(ChunkEvent.Load event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		loadedChunks.add(event.getChunk().getPos());
	}

	private static boolean chunkReadyForCalculations(ChunkPos pos) {
		if (!loadedChunks.contains(pos)) {
			return false;
		}
		for (ChunkPos neighbor : getNeighborChunks(pos)) {
			if (!loadedChunks.contains(neighbor)) {
				return false;
			}
		}
		return true;
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void chunkUnload(ChunkEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		calculatedChunks.remove(event.getChunk().getPos());
		loadedChunks.remove(event.getChunk().getPos());
		calculatingChunks.remove(event.getChunk().getPos());
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void debugScreen(RenderGameOverlayEvent.Text event) {
		if (!Minecraft.getInstance().gameSettings.showDebugInfo) {
			return;
		}
		event.getLeft().add("Chunks Waiting for Biome Border Calculations: " + calculatingChunks.size());
	}

	private static ChunkPos[] getChunkSquare(int radius, ChunkPos center) {
		int sqaureSideLength = radius * 2 + 1;
		ChunkPos[] result = new ChunkPos[sqaureSideLength * sqaureSideLength];
		for (int x = 0; x < sqaureSideLength; x++) {
			for (int z = 0; z < sqaureSideLength; z++) {
				result[z + x * sqaureSideLength] = new ChunkPos(center.x - radius + x, center.z - radius + z);
			}
		}
		return result;
	}

	private static ChunkPos[] getNeighborChunks(ChunkPos chunk) {
		return new ChunkPos[] { new ChunkPos(chunk.x + 1, chunk.z), new ChunkPos(chunk.x - 1, chunk.z),
				new ChunkPos(chunk.x, chunk.z + 1), new ChunkPos(chunk.x, chunk.z - 1),
				new ChunkPos(chunk.x + 1, chunk.z + 1), new ChunkPos(chunk.x - 1, chunk.z - 1),
				new ChunkPos(chunk.x - 1, chunk.z + 1), new ChunkPos(chunk.x + 1, chunk.z - 1) };
	}

	public static int GetVerticalViewRange() {
		return verticalViewRange;
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void keyPressed(KeyInputEvent event) {
		if (BiomeBorderViewer.showBorders.isPressed()) {
			showingBorders = !showingBorders;
			LogManager.getLogger().debug("Show Borders hotkey pressed, showingBorders is now " + showingBorders);
			Minecraft.getInstance().player
					.sendMessage(new StringTextComponent("Showing borders is now " + showingBorders));
		}
	}

	public static void loadConfigSettings() {
		LogManager.getLogger().debug("Loading config settings for Biome Border Viewer.");
		horizontalViewRange = ConfigOptions.horizontalViewRange.get();
		verticalViewRange = ConfigOptions.verticalViewRange.get();
		colorA.set(ConfigOptions.getColorA());
		colorB.set(ConfigOptions.getColorB());
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		if (queuedCalculatedChunks.size() > 0) {
			calculatedChunks.putAll(queuedCalculatedChunks);
			queuedCalculatedChunks.clear();
		}
		if (showingBorders) {
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			Vec3d playerPos = player.getEyePosition(event.getPartialTicks());
			IVertexBuilder builder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource()
					.getBuffer(RenderType.getLightning());
			event.getMatrixStack().push();
			event.getMatrixStack().translate(-playerPos.x, -playerPos.y, -playerPos.z);
			Matrix4f matrix = event.getMatrixStack().getLast().getMatrix();
			int playerY = (int) playerPos.getY();
			for (ChunkPos pos : getChunkSquare(horizontalViewRange, new ChunkPos(player.getPosition()))) {
				if (calculatedChunks.containsKey(pos)) {
					calculatedChunks.get(pos).draw(matrix, builder, playerY);
				} else if (chunkReadyForCalculations(pos)) {
					startChunkCalculations(new QueuedChunkData(pos, player.world));
				}
			}
			event.getMatrixStack().pop();
			Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(RenderType.getLightning());
		}
	}

	private static void startChunkCalculations(QueuedChunkData data) {
		ChunkCalculator cc = new ChunkCalculator(data);
		calculatingChunks.add(data.getChunkPos());
		threadPool.execute(cc);
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void worldUnload(WorldEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		calculatedChunks.clear();
		loadedChunks.clear();
		calculatingChunks.clear();
	}
}
