package com.mrp_v2.biomeborderviewer.visualize;

import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.BiomeBorderViewer;
import com.mrp_v2.biomeborderviewer.config.ConfigOptions;
import com.mrp_v2.biomeborderviewer.util.CalculatedChunkData;
import com.mrp_v2.biomeborderviewer.util.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.EventPriority;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VisualizeBorders {

	private static class ChunkCalculator implements Runnable {

		private final ChunkPos pos;
		private final World world;

		public ChunkCalculator(ChunkPos pos, World world) {
			this.pos = pos;
			this.world = world;
		}

		public void run() {
			CalculatedChunkData calculatedData = new CalculatedChunkData(pos, world);
			chunkCalculated(pos, calculatedData);
		}
	}

	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);

	private static boolean showingBorders;

	private static int horizontalViewRange;
	private static int verticalViewRange;

	private static Color colorA = new Color();
	private static Color colorB = new Color();

	// all events are on same thread
	private static final HashMap<ChunkPos, CalculatedChunkData> CALCULATED_CHUNKS = new HashMap<ChunkPos, CalculatedChunkData>(
			128);

	private static final ConcurrentHashMap<ChunkPos, CalculatedChunkData> CALCULATED_CHUNKS_TO_ADD = new ConcurrentHashMap<ChunkPos, CalculatedChunkData>(
			128);

	private static final Set<ChunkPos> CHUNKS_QUEUED_FOR_CALCULATION = Collections
			.synchronizedSet(new HashSet<ChunkPos>());

	// all events are on same thread
	private static HashSet<ChunkPos> loadedChunks = new HashSet<ChunkPos>();

	public static Color borderColor(boolean isSimilar) {
		if (isSimilar) {
			return colorA;
		} else {
			return colorB;
		}
	}

	private static void chunkCalculated(ChunkPos pos, CalculatedChunkData data) {
		CALCULATED_CHUNKS_TO_ADD.putIfAbsent(pos, data);
		CHUNKS_QUEUED_FOR_CALCULATION.remove(pos);
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
		CALCULATED_CHUNKS.remove(event.getChunk().getPos());
		loadedChunks.remove(event.getChunk().getPos());
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
		if (BiomeBorderViewer.SHOW_BORDERS.isPressed()) {
			showingBorders = !showingBorders;
			LogManager.getLogger().debug("Show Borders hotkey pressed, showingBorders is now " + showingBorders);
			Minecraft.getInstance().player.sendMessage(
					new StringTextComponent("Showing borders is now " + showingBorders), UUID.randomUUID());
		}
	}

	public static void loadConfigSettings() {
		LogManager.getLogger().debug("Loading config settings for Biome Border Viewer.");
		horizontalViewRange = ConfigOptions.CLIENT.horizontalViewRange.get();
		verticalViewRange = ConfigOptions.CLIENT.verticalViewRange.get();
		colorA.set(ConfigOptions.getColorA());
		colorB.set(ConfigOptions.getColorB());
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		if (CALCULATED_CHUNKS_TO_ADD.size() > 0) {
			CALCULATED_CHUNKS.putAll(CALCULATED_CHUNKS_TO_ADD);
			CALCULATED_CHUNKS_TO_ADD.clear();
		}
		if (showingBorders) {
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			Vector3d playerPos = player.getEyePosition(event.getPartialTicks());
			IVertexBuilder builder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource()
					.getBuffer(RenderType.getLightning());
			event.getMatrixStack().push();
			event.getMatrixStack().translate(-playerPos.x, -playerPos.y, -playerPos.z);
			Matrix4f matrix = event.getMatrixStack().getLast().getMatrix();
			int playerY = (int) playerPos.getY();
			HashSet<ChunkPos> chunksToQueue = new HashSet<ChunkPos>();
			for (ChunkPos pos : getChunkSquare(horizontalViewRange,
					new ChunkPos(new BlockPos(player.getPositionVec())))) {
				if (CALCULATED_CHUNKS.containsKey(pos)) {
					CALCULATED_CHUNKS.get(pos).draw(matrix, builder, playerY);
				} else if (chunkReadyForCalculations(pos)) {
					chunksToQueue.add(pos);
				}
			}
			event.getMatrixStack().pop();
			Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(RenderType.getLightning());
			chunksToQueue.removeAll(CHUNKS_QUEUED_FOR_CALCULATION);
			startChunkCalculations(chunksToQueue, player.world);
		}
	}

	private static void startChunkCalculations(Set<ChunkPos> data, World world) {
		for (ChunkPos pos : data) {
			CHUNKS_QUEUED_FOR_CALCULATION.add(pos);
			THREAD_POOL.execute(new ChunkCalculator(pos, world));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void worldUnload(WorldEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		CALCULATED_CHUNKS.clear();
		loadedChunks.clear();
		THREAD_POOL.shutdownNow();
	}
}
