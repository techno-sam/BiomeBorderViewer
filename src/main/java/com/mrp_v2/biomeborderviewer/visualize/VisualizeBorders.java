package com.mrp_v2.biomeborderviewer.visualize;

import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.BiomeBorderViewer;
import com.mrp_v2.biomeborderviewer.config.BiomeBorderViewerConfig;
import com.mrp_v2.biomeborderviewer.util.CalculatedChunkData;
import com.mrp_v2.biomeborderviewer.util.Color;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.text.StringTextComponent;
import net.minecraft.world.World;
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
				DefaultVertexFormats.POSITION_COLOR, 7, 256, true, true,
				RenderType.State.getBuilder().writeMask(COLOR_DEPTH_WRITE).transparency(TRANSLUCENT_TRANSPARENCY)
						.target(field_239236_S_).shadeModel(SHADE_ENABLED).build(false));

		public BiomeBorderRenderType(String nameIn, VertexFormat formatIn, int drawModeIn, int bufferSizeIn,
				boolean useDelegateIn, boolean needsSortingIn, Runnable setupTaskIn, Runnable clearTaskIn) {
			super(nameIn, formatIn, drawModeIn, bufferSizeIn, useDelegateIn, needsSortingIn, setupTaskIn, clearTaskIn);
		}

		public static RenderType getBiomeBorder() {
			return BIOME_BORDER;
		}
	}

	private static class ChunkCalculator implements Runnable {

		private final ChunkPos pos;
		private final World world;
		private final Set<ChunkPos> queuedSet;
		private final Map<ChunkPos, CalculatedChunkData> resultMap;

		public ChunkCalculator(ChunkPos pos, World world, Set<ChunkPos> queuedSet,
				Map<ChunkPos, CalculatedChunkData> resultMap) {
			this.pos = pos;
			this.world = world;
			this.queuedSet = queuedSet;
			this.resultMap = resultMap;
		}

		public void run() {
			CalculatedChunkData calculatedData = new CalculatedChunkData(pos, world);
			synchronized (THREAD_LOCK) {
				queuedSet.remove(pos);
				resultMap.put(pos, calculatedData);
			}
		}
	}

	private static final ExecutorService THREAD_POOL = Executors.newFixedThreadPool(4);

	private static boolean showingBorders;

	private static int horizontalViewRange;
	private static int verticalViewRange;

	private static Color COLOR_A = new Color();
	private static Color COLOR_B = new Color();

	// all events are on same thread
	private static final HashMap<ChunkPos, CalculatedChunkData> calculatedChunks = new HashMap<ChunkPos, CalculatedChunkData>();

	private static HashMap<ChunkPos, CalculatedChunkData> calculatedChunksToAdd = new HashMap<ChunkPos, CalculatedChunkData>();
	private static Set<ChunkPos> chunksQueuedForCalculation = new HashSet<ChunkPos>();
	private static final Object THREAD_LOCK = new Object();

	// all events are on same thread
	private static HashSet<ChunkPos> loadedChunks = new HashSet<ChunkPos>();

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
			if (loadedChunks.isEmpty()) {
				return;
			}
			showingBorders = !showingBorders;
			LogManager.getLogger().debug("Show Borders hotkey pressed, showingBorders is now " + showingBorders);
			Minecraft.getInstance().player.sendMessage(
					new StringTextComponent("Showing borders is now " + showingBorders), UUID.randomUUID());
		}
	}

	public static void loadConfigSettings() {
		LogManager.getLogger().debug("Loading config settings for Biome Border Viewer.");
		horizontalViewRange = BiomeBorderViewerConfig.CLIENT.horizontalViewRange.get();
		verticalViewRange = BiomeBorderViewerConfig.CLIENT.verticalViewRange.get();
		COLOR_A.set(BiomeBorderViewerConfig.getColorA());
		COLOR_B.set(BiomeBorderViewerConfig.getColorB());
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		synchronized (THREAD_LOCK) {
			if (calculatedChunksToAdd.size() > 0) {
				calculatedChunks.putAll(calculatedChunksToAdd);
				calculatedChunksToAdd.clear();
			}
			if (showingBorders) {
				@SuppressWarnings("resource")
				PlayerEntity player = Minecraft.getInstance().player;
				Vector3d playerPos = player.getEyePosition(event.getPartialTicks());
				IVertexBuilder builder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource()
						.getBuffer(BiomeBorderRenderType.getBiomeBorder());
				event.getMatrixStack().push();
				event.getMatrixStack().translate(-playerPos.x, -playerPos.y, -playerPos.z);
				Matrix4f matrix = event.getMatrixStack().getLast().getMatrix();
				int playerY = (int) playerPos.getY();
				HashSet<ChunkPos> chunksToQueue = new HashSet<ChunkPos>();
				for (ChunkPos pos : getChunkSquare(horizontalViewRange,
						new ChunkPos(new BlockPos(player.getPositionVec())))) {
					if (calculatedChunks.containsKey(pos)) {
						calculatedChunks.get(pos).draw(matrix, builder, playerY);
					} else if (chunkReadyForCalculations(pos)) {
						chunksToQueue.add(pos);
					}
				}
				event.getMatrixStack().pop();
				Minecraft.getInstance().getRenderTypeBuffers().getBufferSource()
						.finish(BiomeBorderRenderType.getBiomeBorder());
				chunksToQueue.removeAll(chunksQueuedForCalculation);
				startChunkCalculations(chunksToQueue, player.world);
			}
		}
	}

	private static void startChunkCalculations(Set<ChunkPos> data, World world) {
		for (ChunkPos pos : data) {
			chunksQueuedForCalculation.add(pos);
			THREAD_POOL.execute(new ChunkCalculator(pos, world, chunksQueuedForCalculation, calculatedChunksToAdd));
		}
	}

	@SubscribeEvent(priority = EventPriority.LOW)
	public static void worldUnload(WorldEvent.Unload event) {
		if (event.getWorld() == null || !(event.getWorld() instanceof ClientWorld)) {
			return;
		}
		synchronized (THREAD_LOCK) {
			chunksQueuedForCalculation = new HashSet<ChunkPos>();
			calculatedChunksToAdd = new HashMap<ChunkPos, CalculatedChunkData>();
		}
		calculatedChunks.clear();
		loadedChunks.clear();
	}
}
