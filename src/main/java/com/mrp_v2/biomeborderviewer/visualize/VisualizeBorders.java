package com.mrp_v2.biomeborderviewer.visualize;

import java.util.Collections;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

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
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VisualizeBorders {

	private static class ChunkCalculator extends Thread {

		private final ChunkPos pos;
		private final QueuedChunkData data;

		public ChunkCalculator(ChunkPos pos, QueuedChunkData data) {
			this.setPriority(NORM_PRIORITY - 2);
			this.setName("Biome Border Calculator [ChunkPos: " + pos.toString() + "]");
			this.pos = pos;
			this.data = data;
		}

		public void run() {
			CalculatedChunkData calculatedData = new CalculatedChunkData(data);
			chunkCalculated(pos, calculatedData);
		}
	}

	private static boolean showingBorders;
	private static int horizontalViewRange;

	private static int verticalViewRange;

	public static float radius;
	private static Color colorA = new Color();

	private static Color colorB = new Color();

	private static ConcurrentHashMap<ChunkPos, CalculatedChunkData> calculatedChunks = new ConcurrentHashMap<ChunkPos, CalculatedChunkData>(
			128);

	private static ConcurrentHashMap<ChunkPos, QueuedChunkData> queuedChunks = new ConcurrentHashMap<ChunkPos, QueuedChunkData>(
			32);

	private static Set<ChunkPos> calculatingChunks = Collections.synchronizedSet(new HashSet<ChunkPos>());

	private static Set<ChunkPos> loadedChunks = Collections.synchronizedSet(new HashSet<ChunkPos>());

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
			calculatedChunks.put(pos, data);
		}
	}

	@SubscribeEvent
	public static void chunkLoad(ChunkEvent.Load event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		loadedChunks.add(event.getChunk().getPos());
		if (chunkReadyForCalculations(event.getChunk().getPos())) {
			calculatedChunks.put(event.getChunk().getPos(),
					new CalculatedChunkData(new QueuedChunkData(event.getChunk().getPos(), event.getWorld())));
		} else {
			queuedChunks.put(event.getChunk().getPos(),
					new QueuedChunkData(event.getChunk().getPos(), event.getWorld().getWorld()));
		}
		for (ChunkPos pos : getNeighborChunks(event.getChunk().getPos())) {
			if (queuedChunks.containsKey(pos)) {
				if (chunkReadyForCalculations(pos)) {
					calculatingChunks.add(pos);
					new ChunkCalculator(pos, queuedChunks.get(pos)).start();
					queuedChunks.remove(pos);
				}
			}
		}
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

	@SubscribeEvent
	public static void chunkUnload(ChunkEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		calculatedChunks.remove(event.getChunk().getPos());
		queuedChunks.remove(event.getChunk().getPos());
		loadedChunks.remove(event.getChunk().getPos());
		calculatingChunks.remove(event.getChunk().getPos());
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
			LogManager.getLogger().debug("Show Borders hotkey pressed. showingBorders is now " + showingBorders);
			Minecraft.getInstance().player
					.sendMessage(new StringTextComponent("Showing borders is now " + showingBorders));
		}
	}

	public static void loadConfigSettings() {
		LogManager.getLogger().debug("Loading config settings for border lines.");
		horizontalViewRange = ConfigOptions.horizontalViewRange.get();
		verticalViewRange = ConfigOptions.verticalViewRange.get();
		colorA.set(ConfigOptions.getColorA());
		colorB.set(ConfigOptions.getColorB());
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		if (showingBorders) {
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			ChunkPos playerChunk = new ChunkPos(player.getPosition());
			Vec3d playerPos = player.getEyePosition(event.getPartialTicks());
			IVertexBuilder builder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource()
					.getBuffer(RenderType.getLightning());
			event.getMatrixStack().push();
			event.getMatrixStack().translate(-playerPos.x, -playerPos.y, -playerPos.z);
			Matrix4f matrix = event.getMatrixStack().getLast().getMatrix();
			int playerY = (int) playerPos.getY();
			for (ChunkPos pos : calculatedChunks.keySet()) {
				if (pos.getChessboardDistance(playerChunk) <= horizontalViewRange) {
					calculatedChunks.get(pos).draw(matrix, builder, playerY);
				}
			}
			event.getMatrixStack().pop();
			Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(RenderType.getLightning());
		}
	}

	@SubscribeEvent
	public static void worldUnload(WorldEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		calculatedChunks.clear();
		queuedChunks.clear();
		loadedChunks.clear();
		calculatingChunks.clear();
	}
}
