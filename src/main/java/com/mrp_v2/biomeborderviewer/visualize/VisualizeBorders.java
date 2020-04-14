package com.mrp_v2.biomeborderviewer.visualize;

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.BiomeBorderViewer;
import com.mrp_v2.biomeborderviewer.config.ConfigOptions;
import com.mrp_v2.biomeborderviewer.config.ConfigOptions.RenderModes;
import com.mrp_v2.biomeborderviewer.util.ChunkBiomeBorderData;
import com.mrp_v2.biomeborderviewer.util.Color;
import com.mrp_v2.biomeborderviewer.util.CornerData;
import com.mrp_v2.biomeborderviewer.util.LineData;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.ChunkPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.IWorld;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.chunk.IChunk;
import net.minecraft.world.gen.Heightmap;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber
public class VisualizeBorders {

	private static boolean showingBorders;

	private static int viewRange;

	private static double playerHeightOffset;
	private static double terrainHeightOffset;
	private static double fixedHeight;

	private static float radius;

	private static Color colorA;
	private static Color colorB;

	private static ConfigOptions.RenderModes renderMode;

	private static HashMap<ChunkPos, ChunkBiomeBorderData> calculatedChunks = new HashMap<ChunkPos, ChunkBiomeBorderData>();

	@SubscribeEvent
	public static void chunkLoad(ChunkEvent.Load event) {
		if (event.getWorld().isAreaLoaded(event.getChunk().getPos().asBlockPos().add(7, 0, 7), 10)) {
			calculatedChunks.put(event.getChunk().getPos(), calculateDataForChunk(event.getChunk(), event.getWorld()));
		}
	}

	@SubscribeEvent
	public static void chunkUnload(ChunkEvent.Unload event) {
		calculatedChunks.remove(event.getChunk().getPos());
	}

	@SubscribeEvent
	public static void keyPressed(KeyInputEvent event) {
		if (BiomeBorderViewer.showBorders.isPressed()) {
			showingBorders = !showingBorders;
			LogManager.getLogger().debug("Show Borders hotkey pressed. showingBorders is now " + showingBorders
					+ ", render mode = " + renderMode.toString());
		}
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		if (showingBorders) {
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			// prepare calculations
			Vec3d playerEyePos = player.getEyePosition(event.getPartialTicks());
			// prepare to draw
			IVertexBuilder builder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource()
					.getBuffer(RenderType.getLightning());
			event.getMatrixStack().push();
			event.getMatrixStack().translate(-playerEyePos.x, -playerEyePos.y, -playerEyePos.z);
			Matrix4f matrix = event.getMatrixStack().getLast().getMatrix();
			// draw
			for (ChunkPos pos : calculatedChunks.keySet()){
				for (LineData lineData : calculatedChunks.get(pos).getLines()) {
					switch (renderMode) {
					case WALL:
						drawWall(lineData, matrix, builder);
						break;
					default:
						drawLine(lineData, matrix, builder);
						break;
					}
				}
				if (renderMode != RenderModes.WALL) {
					drawCorners(calculatedChunks.get(pos).getCorners()), matrix, builder);
				}
			}
			// end drawing
			event.getMatrixStack().pop();
			Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(RenderType.getLightning());
		}
	}

	/**
	 * Assumes neighboring chunks are loaded
	 */
	private static ChunkBiomeBorderData calculateDataForChunk(IChunk chunk, IWorld world) {
		ArrayList<LineData> lines = new ArrayList<LineData>();
		ArrayList<CornerData> corners = new ArrayList<CornerData>();
		//
		BlockPos mainPos;
		Biome mainBiome, neighborBiome;
		BlockPos[] neighbors;
		Vec3d a, b;
		LineData lineData;
		CornerData cornerDataA, cornerDataB;
		for (int x = 0; x < 15; x++) {
			for (int z = 0; z < 15; z += 2) {
				if (x % 2 == 1 && z == 0) {
					z++;
				}
				mainPos = new BlockPos(x, 10, z);
				mainBiome = world.getBiome(mainPos);
				neighbors = new BlockPos[] { new BlockPos(x + 1, 10, z), new BlockPos(x - 1, 10, z),
						new BlockPos(x, 10, z + 1), new BlockPos(x, 10, z - 1) };
				for (BlockPos neighborPos : neighbors) {
					neighborBiome = world.getBiome(neighborPos);
					if (!neighborBiome.equals(mainBiome)) {
						a = new Vec3d(mainPos);
						b = new Vec3d(neighborPos);
						if (a.x != b.x) {// if they have the same z and different x
							a = a.add(0, 0, 1);
							if (a.x > b.x) {
								b = b.add(1, 0, 0);
							} else {
								a = a.add(1, 0, 0);
							}
						} else {// if they have the same x and different z
							a = a.add(1, 0, 0);
							if (a.z > b.z) {
								b = b.add(0, 0, 1);
							} else {
								a = a.add(0, 0, 1);
							}
						}
						a = new Vec3d(a.x, fixedHeight, a.z);
						b = new Vec3d(b.x, fixedHeight, b.z);
						lineData = new LineData(a, b);
						lineData.color = borderColor(mainBiome, neighborBiome);
						lines.add(lineData.clone());
						cornerDataA = new CornerData();
						cornerDataB = new CornerData();
						if (a.x == b.x) {
							cornerDataA.showMinusZ = false;
							cornerDataB.showPlusZ = false;
						} else {
							cornerDataA.showMinusX = false;
							cornerDataB.showPlusX = false;
						}
						cornerDataA.pos = a;
						cornerDataB.pos = b;
						cornerDataA.color = lineData.color;
						cornerDataB.color = lineData.color;
						if (!corners.contains(cornerDataA)) {
							corners.add(cornerDataA);
						} else {
							corners.get(corners.indexOf(cornerDataA)).combine(cornerDataA);
						}
						if (!corners.contains(cornerDataB)) {
							corners.add(cornerDataB);
						} else {
							corners.get(corners.indexOf(cornerDataB)).combine(cornerDataB);
						}
					}
				}
			}
		}
		return new ChunkBiomeBorderData(lines, corners);
	}

	private static final float minWallHeight = 0;
	private static final float maxWallHeight = 255;
	private static final float wallOffsetDivisor = 1f / 0b11111111;

	private static void drawWall(LineData lineData, Matrix4f matrix, IVertexBuilder builder) {
		if (lineData.a.x == lineData.b.x) {
			// -x side
			builder.pos(matrix, (float) lineData.a.x + wallOffsetDivisor, minWallHeight, (float) lineData.a.z)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + wallOffsetDivisor, minWallHeight, (float) lineData.b.z)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + wallOffsetDivisor, maxWallHeight, (float) lineData.b.z)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x + wallOffsetDivisor, maxWallHeight, (float) lineData.a.z)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +x side
			builder.pos(matrix, (float) lineData.a.x - wallOffsetDivisor, maxWallHeight, (float) lineData.a.z)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x - wallOffsetDivisor, maxWallHeight, (float) lineData.b.z)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x - wallOffsetDivisor, minWallHeight, (float) lineData.b.z)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x - wallOffsetDivisor, minWallHeight, (float) lineData.a.z)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
		} else {
			// -z side
			builder.pos(matrix, (float) lineData.a.x, minWallHeight, (float) lineData.a.z - wallOffsetDivisor)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x, minWallHeight, (float) lineData.b.z - wallOffsetDivisor)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x, maxWallHeight, (float) lineData.b.z - wallOffsetDivisor)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x, maxWallHeight, (float) lineData.a.z - wallOffsetDivisor)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +z side
			builder.pos(matrix, (float) lineData.a.x, maxWallHeight, (float) lineData.a.z + wallOffsetDivisor)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x, maxWallHeight, (float) lineData.b.z + wallOffsetDivisor)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x, minWallHeight, (float) lineData.b.z + wallOffsetDivisor)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x, minWallHeight, (float) lineData.a.z + wallOffsetDivisor)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
		}
	}

	private static void drawLine(LineData lineData, Matrix4f matrix, IVertexBuilder builder) {
		if (lineData.a.x == lineData.b.x) {
			// top
			builder.pos(matrix, (float) lineData.a.x + radius, (float) lineData.a.y + radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y + radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x - radius, (float) lineData.b.y + radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y + radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// bottom
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y - radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x - radius, (float) lineData.b.y - radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y - radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x + radius, (float) lineData.a.y - radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// -x side
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y + radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x - radius, (float) lineData.b.y + radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x - radius, (float) lineData.b.y - radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y - radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +x side
			builder.pos(matrix, (float) lineData.a.x + radius, (float) lineData.a.y - radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y - radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y + radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x + radius, (float) lineData.a.y + radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
		} else {
			// top
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y + radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y + radius,
					(float) lineData.b.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y + radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y + radius,
					(float) lineData.a.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// bottom
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y - radius,
					(float) lineData.a.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y - radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y - radius,
					(float) lineData.b.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y - radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// -z side
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y - radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y - radius,
					(float) lineData.b.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y + radius,
					(float) lineData.b.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y + radius,
					(float) lineData.a.z - radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +z side
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y + radius,
					(float) lineData.a.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y + radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.b.x + radius, (float) lineData.b.y - radius,
					(float) lineData.b.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, (float) lineData.a.x - radius, (float) lineData.a.y - radius,
					(float) lineData.a.z + radius)
					.color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
		}
	}

	private static void drawCorners(ArrayList<CornerData> corners, Matrix4f matrix, IVertexBuilder builder) {
		for (CornerData cornerData : corners) {
			drawCorner(cornerData, matrix, builder);
		}
	}

	private static void drawCorner(CornerData cornerData, Matrix4f matrix, IVertexBuilder builder) {
		if (cornerData.showPlusX) {
			// +x side
			builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y - radius,
					(float) cornerData.pos.z - radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y + radius,
					(float) cornerData.pos.z - radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y + radius,
					(float) cornerData.pos.z + radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y - radius,
					(float) cornerData.pos.z + radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showMinusX) {
			// -x side
			builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y - radius,
					(float) cornerData.pos.z + radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y + radius,
					(float) cornerData.pos.z + radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y + radius,
					(float) cornerData.pos.z - radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y - radius,
					(float) cornerData.pos.z - radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showPlusZ) {
			// +z side
			builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y - radius,
					(float) cornerData.pos.z + radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y + radius,
					(float) cornerData.pos.z + radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y + radius,
					(float) cornerData.pos.z + radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y - radius,
					(float) cornerData.pos.z + radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showMinusZ) {
			// -z side
			builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y - radius,
					(float) cornerData.pos.z - radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y + radius,
					(float) cornerData.pos.z - radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y + radius,
					(float) cornerData.pos.z - radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y - radius,
					(float) cornerData.pos.z - radius)
					.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		// top
		builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y + radius,
				(float) cornerData.pos.z + radius)
				.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y + radius,
				(float) cornerData.pos.z - radius)
				.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y + radius,
				(float) cornerData.pos.z - radius)
				.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y + radius,
				(float) cornerData.pos.z + radius)
				.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		// bottom
		builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y - radius,
				(float) cornerData.pos.z + radius)
				.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, (float) cornerData.pos.x - radius, (float) cornerData.pos.y - radius,
				(float) cornerData.pos.z - radius)
				.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y - radius,
				(float) cornerData.pos.z - radius)
				.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, (float) cornerData.pos.x + radius, (float) cornerData.pos.y - radius,
				(float) cornerData.pos.z + radius)
				.color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
	}

	private static Color borderColor(Biome a, Biome b) {
		if (similarTemperature(a, b)) {
			return colorA;
		} else {
			return colorB;
		}
	}

	private static float heightForPos(double x, double z, World world, Vec3d playerPos) {
		switch (renderMode) {
		case FIXED_HEIGHT:
			return (float) fixedHeight;
		case FOLLOW_PLAYER_HEIGHT:
			return playerBasedHeight(playerPos);
		case FOLLOW_PLAYER_IF_HIGHER_THAN_TERRAIN:
			float playerBasedHeight = playerBasedHeight(playerPos);
			float terrainBasedHeight = terrainBasedHeight(x, z, world);
			if (playerBasedHeight >= terrainBasedHeight)
				return playerBasedHeight;
			else
				return terrainBasedHeight;
		case MATCH_TERRAIN:
			return terrainBasedHeight(x, z, world);
		default:
			return 64;
		}
	}

	private static float playerBasedHeight(Vec3d playerPos) {
		float height = (float) (playerPos.y + playerHeightOffset);
		return height;
	}

	private static float terrainBasedHeight(double xf, double zf, World world) {
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

	private static boolean similarTemperature(Biome a, Biome b) {
		return a.getTempCategory() == b.getTempCategory();
	}

	public static void loadConfigSettings() {
		LogManager.getLogger().debug("Loading config settings for border lines.");
		viewRange = ConfigOptions.viewRange.get();
		playerHeightOffset = ConfigOptions.playerHeightOffset.get();
		terrainHeightOffset = ConfigOptions.terrainHeightOffset.get();
		fixedHeight = ConfigOptions.fixedHeight.get();
		renderMode = ConfigOptions.renderMode.get();
		colorA = new Color(ConfigOptions.lineAR.get(), ConfigOptions.lineAG.get(), ConfigOptions.lineAB.get(),
				ConfigOptions.lineAA.get());
		colorB = new Color(ConfigOptions.lineBR.get(), ConfigOptions.lineBG.get(), ConfigOptions.lineBB.get(),
				ConfigOptions.lineBA.get());
		radius = ConfigOptions.lineWidth.get().floatValue() / 2;
	}
}
