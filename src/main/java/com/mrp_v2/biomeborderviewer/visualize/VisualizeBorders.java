package com.mrp_v2.biomeborderviewer.visualize;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.BiomeBorderViewer;
import com.mrp_v2.biomeborderviewer.config.ConfigOptions;
import com.mrp_v2.biomeborderviewer.util.Color;
import com.mrp_v2.biomeborderviewer.util.CornerData;
import com.mrp_v2.biomeborderviewer.util.LineData;
import com.mrp_v2.biomeborderviewer.util.Vector3Float;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Matrix4f;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.biome.Biome;
import net.minecraft.world.gen.Heightmap.Type;
import net.minecraftforge.client.event.InputEvent.KeyInputEvent;
import net.minecraftforge.client.event.RenderWorldLastEvent;
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

	@SubscribeEvent
	public static void keyPressed(KeyInputEvent event) {
		if (BiomeBorderViewer.showBorders.isPressed()) {
			showingBorders = !showingBorders;
			LogManager.getLogger().debug("Show Borders hotkey pressed. showingBorders is now " + showingBorders);
		}
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		if (showingBorders) {
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			// calculate lines and corners
			Vector3Float playerNetPos = new Vector3Float();
			Vec3d tempPlayerPos = player.getPositionVec();
			playerNetPos.x = (float) (player.prevPosX + (tempPlayerPos.x - player.prevPosX) * event.getPartialTicks());
			playerNetPos.y = (float) (player.prevPosY + (tempPlayerPos.y - player.prevPosY) * event.getPartialTicks());
			playerNetPos.z = (float) (player.prevPosZ + (tempPlayerPos.z - player.prevPosZ) * event.getPartialTicks());
			playerNetPos.y += player.getEyeHeight(player.getPose());
			World world = player.getEntityWorld();
			ArrayList<LineData> lines = new ArrayList<LineData>();
			ArrayList<CornerData> corners = new ArrayList<CornerData>();
			for (int x = (int) (playerNetPos.x - viewRange); x <= (int) (playerNetPos.x + viewRange); x++) {
				for (int z = (int) (playerNetPos.z - viewRange); z <= (int) (playerNetPos.z + viewRange); z++) {
					if (x % 2 == 0 && z == (int) (playerNetPos.z - viewRange)) {
						z++;
					}
					BlockPos mainPos = new BlockPos(x, 10, z);				
					Biome mainBiome = world.getBiome(mainPos);
					BlockPos[] neighbors = new BlockPos[] { new BlockPos(x + 1, 10, z), new BlockPos(x - 1, 10, z),
							new BlockPos(x, 10, z + 1), new BlockPos(x, 10, z - 1) };
					for (BlockPos neighborPos : neighbors) {
						Biome neighborBiome = world.getBiome(neighborPos);
						if (!neighborBiome.equals(mainBiome)) {
							LineData lineData = new LineData(Vector3Float.fromBlockPos(mainPos),
									Vector3Float.fromBlockPos(neighborPos));
							CornerData cornerDataA = new CornerData(), cornerDataB = new CornerData();
							if (lineData.a.x != lineData.b.x) {// if they have the same z and different x
								lineData.a.z += 1 - radius;
								lineData.b.z += radius;
								if (lineData.a.x > lineData.b.x) {
									lineData.b.x += 1;
								} else {
									lineData.a.x += 1;
								}
								cornerDataA.showMinusZ = false;
								cornerDataB.showPlusZ = false;
							} else {// if they have the same x and different z
								lineData.a.x += 1 - radius;
								lineData.b.x += radius;
								if (lineData.a.z > lineData.b.z) {
									lineData.b.z += 1;
								} else {
									lineData.a.z += 1;
								}
								cornerDataA.showMinusX = false;
								cornerDataB.showPlusX = false;
							}
							lineData.a.y = heightForPos(lineData.a.x, lineData.a.z, world, playerNetPos);
							lineData.b.y = heightForPos(lineData.b.x, lineData.b.z, world, playerNetPos);
							cornerDataA.pos = lineData.a.roundedXAndZ();
							cornerDataB.pos = lineData.b.roundedXAndZ();
							lineData.color = borderColor(mainBiome, neighborBiome);
							cornerDataA.color = lineData.color;
							cornerDataB.color = lineData.color;
							if (!lines.contains(lineData)) {
								lines.add(lineData);
							}
							if (!corners.contains(cornerDataA)) {
								corners.add(cornerDataA);
							} else {
								corners.get(corners.indexOf(cornerDataA)).ignoreSides(cornerDataA);
							}
							if (!corners.contains(cornerDataB)) {
								corners.add(cornerDataB);
							} else {
								corners.get(corners.indexOf(cornerDataB)).ignoreSides(cornerDataB);
							}
						}
					}
				}
			}
			// draw lines and corners
			draw(playerNetPos, lines, corners, event.getMatrixStack());
		}
	}

	private static void draw(Vector3Float playerPos, ArrayList<LineData> lines, ArrayList<CornerData> corners, MatrixStack matrixStack) {
		IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
		RenderType myRenderType = RenderType.getLightning();
		IVertexBuilder builder = buffer.getBuffer(myRenderType);
		
		matrixStack.push();
		matrixStack.translate(-playerPos.x, -playerPos.y, -playerPos.z);
		Matrix4f matrix = matrixStack.getLast().getMatrix();
		
		drawLines(lines, matrix, builder);
		drawCorners(corners, matrix, builder);

		matrixStack.pop();
		buffer.finish(myRenderType);
	}

	private static void drawLines(ArrayList<LineData> lines, Matrix4f matrix, IVertexBuilder builder) {
		for (LineData lineData : lines) {
			drawLine(lineData, matrix, builder);
		}
	}
	
	private static void drawLine(LineData lineData, Matrix4f matrix, IVertexBuilder builder) {
		if (lineData.a.x == lineData.b.x) {
			// top
			builder.pos(matrix, lineData.a.x + radius, lineData.a.y + radius, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y + radius, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - radius, lineData.b.y + radius, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y + radius, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// bottom
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y - radius, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - radius, lineData.b.y - radius, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y - radius, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x + radius, lineData.a.y - radius, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// -x side
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y + radius, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - radius, lineData.b.y + radius, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - radius, lineData.b.y - radius, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y - radius, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +x side
			builder.pos(matrix, lineData.a.x + radius, lineData.a.y - radius, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y - radius, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y + radius, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x + radius, lineData.a.y + radius, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
		} else {
			// top
			builder.pos(matrix, lineData.a.x, lineData.a.y + radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, lineData.b.y + radius, lineData.b.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, lineData.b.y + radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x, lineData.a.y + radius, lineData.a.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// bottom
			builder.pos(matrix, lineData.a.x, lineData.a.y - radius, lineData.a.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, lineData.b.y - radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, lineData.b.y - radius, lineData.b.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x, lineData.a.y - radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// -z side
			builder.pos(matrix, lineData.a.x, lineData.a.y - radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, lineData.b.y - radius, lineData.b.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, lineData.b.y + radius, lineData.b.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x, lineData.a.y + radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +z side
			builder.pos(matrix, lineData.a.x, lineData.a.y + radius, lineData.a.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, lineData.b.y + radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, lineData.b.y - radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x, lineData.a.y - radius, lineData.a.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
		}
	}

	private static void drawCorners(ArrayList<CornerData> corners, Matrix4f matrix, IVertexBuilder builder) {
		for (CornerData cornerData : corners) {
			drawCorner(cornerData, matrix, builder);
		}
	}

	private static void drawCorner(CornerData cornerData, Matrix4f matrix, IVertexBuilder builder) {
		if (cornerData.showPlusX) {// +x side
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showMinusX) {// -x side
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showPlusZ) {// +z side
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showMinusZ) {// -z side
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		//top
		builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		//bottom
		builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
	}

	private static Color borderColor(Biome a, Biome b) {
		if (Similar(a, b)) {
			return colorA;
		} else {
			return colorB;
		}
	}

	private static float heightForPos(float x, float z, World world, Vector3Float playerPos) {
		double height = 0;
		switch (renderMode) {
		case FIXED_HEIGHT:
			height = fixedHeight;
			break;
		case FOLLOW_PLAYER_HEIGHT:
			height = playerBasedHeight(playerPos);
			break;
		case FOLLOW_PLAYER_IF_HIGHER_THAN_TERRAIN:
			double playerBasedHeight = playerBasedHeight(playerPos);
			double terrainBasedHeight = terrainBasedHeight(x, z, world);
			if (playerBasedHeight >= terrainBasedHeight) {
				height = playerBasedHeight;
			} else {
				height = terrainBasedHeight;
			}
			break;
		case MATCH_TERRAIN:
			height = terrainBasedHeight(x, z, world);
			break;
		default:
			height = 64.0;
			break;
		}
		if (height < 0) {
			height = 0;
		} else if (height > 256) {
			height = 256;
		}
		return (float) height;
	}

	private static float playerBasedHeight(Vector3Float playerPos) {
		return (float) (playerPos.y + playerHeightOffset);
	}

	private static float terrainBasedHeight(float xf, float zf, World world) {
		int x = Math.round(xf);
		int z = Math.round(zf);
		float height = 0;
		for (int tempX = x - 1; tempX <= x; tempX++) {
			for (int tempZ = z - 1; tempZ <= z; tempZ++) {
				int y = world.getHeight(Type.WORLD_SURFACE, tempX, tempZ);
				if (y > height) {
					height = y;
				}
			}
		}
		return (float) (height + terrainHeightOffset);
	}

	private static boolean Similar(Biome a, Biome b) {
		return a.getTempCategory() == b.getTempCategory();
	}

	public static void loadConfigSettings() {
		LogManager.getLogger().debug("Loading config settings for border lines.");
		viewRange = ConfigOptions.viewRange.get();
		playerHeightOffset = ConfigOptions.playerHeightOffset.get();
		terrainHeightOffset = ConfigOptions.terrainHeightOffset.get();
		fixedHeight = ConfigOptions.fixedHeight.get();
		renderMode = ConfigOptions.baseLineHeight.get();
		colorA = new Color(ConfigOptions.lineAR.get(), ConfigOptions.lineAG.get(), ConfigOptions.lineAB.get(), ConfigOptions.lineAA.get());
		colorB = new Color(ConfigOptions.lineBR.get(), ConfigOptions.lineBG.get(), ConfigOptions.lineBB.get(), ConfigOptions.lineBA.get());
		radius = ConfigOptions.lineWidth.get().floatValue() / 2;
	}
}
