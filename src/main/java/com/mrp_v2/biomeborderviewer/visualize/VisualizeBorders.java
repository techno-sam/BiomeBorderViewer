package com.mrp_v2.biomeborderviewer.visualize;

import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.platform.GlStateManager;
import com.mrp_v2.biomeborderviewer.BiomeBorderViewer;
import com.mrp_v2.biomeborderviewer.config.ConfigOptions;
import com.mrp_v2.biomeborderviewer.util.CalculatedChunkData;
import com.mrp_v2.biomeborderviewer.util.Color;
import com.mrp_v2.biomeborderviewer.util.QueuedChunkData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.IWorld;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VisualizeBorders {

	private static boolean showingBorders;

	private static int viewRange;

	private static double playerHeightOffset;
	private static double terrainHeightOffset;
	private static double fixedHeight;

	public static float radius;

	private static Color colorA = new Color();
	private static Color colorB = new Color();

	public static ConfigOptions.RenderModes renderMode;

	private static ConcurrentHashMap<ChunkPos, CalculatedChunkData> calculatedChunks = new ConcurrentHashMap<ChunkPos, CalculatedChunkData>(
			128);

	private static ConcurrentHashMap<ChunkPos, QueuedChunkData> queuedChunks = new ConcurrentHashMap<ChunkPos, QueuedChunkData>(
			32);

	private static ArrayList<ChunkPos> loadedChunks = new ArrayList<ChunkPos>();

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
					calculatedChunks.put(pos, new CalculatedChunkData(queuedChunks.get(pos)));
				}
			}
		}
	}

	@SubscribeEvent
	public static void chunkUnload(ChunkEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		calculatedChunks.remove(event.getChunk().getPos());
		queuedChunks.remove(event.getChunk().getPos());
		loadedChunks.remove(event.getChunk().getPos());
	}

	@SubscribeEvent
	public static void worldUnload(WorldEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		calculatedChunks.clear();
		queuedChunks.clear();
		loadedChunks.clear();
	}

	@SuppressWarnings("resource")
	@SubscribeEvent
	public static void keyPressed(KeyInputEvent event) {
		if (BiomeBorderViewer.showBorders.isPressed()) {
			showingBorders = !showingBorders;
			LogManager.getLogger().debug("Show Borders hotkey pressed. showingBorders is now " + showingBorders
					+ ", render mode = " + renderMode.toString());
			Minecraft.getInstance().player
					.sendMessage(new StringTextComponent("Showing borders is now " + showingBorders));
		}
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		if (showingBorders) {
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			ChunkPos playerChunk = new ChunkPos(player.getPosition());
			Vec3d playerPos = player.getEyePosition(event.getPartialTicks());
			GlStateManager.pushMatrix();
			GlStateManager.enableBlend();
			GlStateManager.disableTexture();
			GlStateManager.enableAlphaTest();
			GlStateManager.translated(-playerPos.x, -playerPos.y, -playerPos.z);
			for (ChunkPos pos : calculatedChunks.keySet()) {
				if (chessboardDistance(pos, playerChunk) <= viewRange) {
					calculatedChunks.get(pos).draw(playerPos);
				}
			}
			GlStateManager.disableAlphaTest();
			GlStateManager.disableBlend();
			GlStateManager.enableTexture();
			GlStateManager.popMatrix();
		}
	}

	private static int chessboardDistance(ChunkPos a, ChunkPos b) {
		if (a.x - b.x < a.z - b.z) {
			return Math.abs(a.x - b.x);
		}
		return Math.abs(a.z - b.z);
	}

	private static ChunkPos[] getNeighborChunks(ChunkPos chunk) {
		return new ChunkPos[] { new ChunkPos(chunk.x + 1, chunk.z), new ChunkPos(chunk.x - 1, chunk.z),
				new ChunkPos(chunk.x, chunk.z + 1), new ChunkPos(chunk.x, chunk.z - 1),
				new ChunkPos(chunk.x + 1, chunk.z + 1), new ChunkPos(chunk.x - 1, chunk.z - 1),
				new ChunkPos(chunk.x - 1, chunk.z + 1), new ChunkPos(chunk.x + 1, chunk.z - 1) };
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

	public static Color borderColor(boolean isSimilar) {
		if (isSimilar) {
			return colorA;
		} else {
			return colorB;
		}
	}

	public static float heightForPos(double x, double z, IWorld world, Vec3d playerPos) {
		switch (renderMode) {
		case LINE_FIXED_HEIGHT:
			return (float) fixedHeight;
		case LINE_FOLLOW_PLAYER_HEIGHT:
			return playerBasedHeight(playerPos);
		case LINE_FOLLOW_PLAYER_IF_HIGHER_THAN_TERRAIN:
			float playerBasedHeight = playerBasedHeight(playerPos);
			float terrainBasedHeight = terrainBasedHeight(x, z, world);
			if (playerBasedHeight >= terrainBasedHeight)
				return playerBasedHeight;
			else
				return terrainBasedHeight;
		case LINE_MATCH_TERRAIN:
			return terrainBasedHeight(x, z, world);
		default:
			return 64;
		}
	}

	private static float playerBasedHeight(Vec3d playerPos) {
		float height = (float) (playerPos.y + playerHeightOffset);
		return height;
	}

	private static float terrainBasedHeight(double xf, double zf, IWorld world) {
		int x = (int) Math.round(xf);
		int z = (int) Math.round(zf);
		float height = 0;
		for (int tempX = x - 1; tempX <= x; tempX++) {
			for (int tempZ = z - 1; tempZ <= z; tempZ++) {
				int y = world.getHeight(Heightmap.Type.MOTION_BLOCKING, tempX, tempZ);
				if (y > height) {
					height = y;
				}
			}
		}
		return (float) (height + terrainHeightOffset);
	}

	public static void loadConfigSettings() {
		LogManager.getLogger().debug("Loading config settings for border lines.");
		viewRange = ConfigOptions.viewRange.get();
		playerHeightOffset = ConfigOptions.playerHeightOffset.get();
		terrainHeightOffset = ConfigOptions.terrainHeightOffset.get();
		fixedHeight = ConfigOptions.fixedHeight.get();
		renderMode = ConfigOptions.renderMode.get();
		colorA.set(ConfigOptions.getColorA());
		colorB.set(ConfigOptions.getColorB());
		radius = ConfigOptions.lineWidth.get().floatValue() / 2;
	}
}
