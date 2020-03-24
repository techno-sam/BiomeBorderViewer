package com.mrp_v2.biomeborderviewer.visualize;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;

import com.mojang.blaze3d.vertex.IVertexBuilder;
import com.mrp_v2.biomeborderviewer.BiomeBorderViewer;
import com.mrp_v2.biomeborderviewer.config.ConfigOptions;
import com.mrp_v2.biomeborderviewer.config.ConfigOptions.RenderModes;
import com.mrp_v2.biomeborderviewer.util.Color;
import com.mrp_v2.biomeborderviewer.util.CornerData;
import com.mrp_v2.biomeborderviewer.util.Float3;
import com.mrp_v2.biomeborderviewer.util.Int2Float1Combo;
import com.mrp_v2.biomeborderviewer.util.LineData;

import net.minecraft.client.Minecraft;
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
			LogManager.getLogger().debug("Show Borders hotkey pressed. showingBorders is now " + showingBorders + ", render mode = " + renderMode.toString());
		}
	}

	@SubscribeEvent
	public static void renderEvent(RenderWorldLastEvent event) {
		if (showingBorders) {
			@SuppressWarnings("resource")
			PlayerEntity player = Minecraft.getInstance().player;
			//prepare calculations
			Vec3d tempPlayerPos = player.getPositionVec();
			Float3 playerNetPos = new Float3(
					(float) (player.prevPosX + (tempPlayerPos.x - player.prevPosX) * event.getPartialTicks()),
					(float) (player.prevPosY + (tempPlayerPos.y - player.prevPosY) * event.getPartialTicks() + player.getEyeHeight(player.getPose())),
					(float) (player.prevPosZ + (tempPlayerPos.z - player.prevPosZ) * event.getPartialTicks())
					);
			ArrayList<CornerData> corners = new ArrayList<CornerData>();
			//prepare to draw
			IVertexBuilder builder = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().getBuffer(RenderType.getLightning());
			event.getMatrixStack().push();
			event.getMatrixStack().translate(-playerNetPos.x, -playerNetPos.y, -playerNetPos.z);
			Matrix4f matrix = event.getMatrixStack().getLast().getMatrix();
			//draw
			for (int x = (int) (playerNetPos.x - viewRange); x <= (int) (playerNetPos.x + viewRange); x++) {
				
				for (int z = (int) (playerNetPos.z - viewRange); z <= (int) (playerNetPos.z + viewRange); z+= 2) {
					if (x % 2 == 0 && z == (int) (playerNetPos.z - viewRange)) z++;
					BlockPos mainPos = new BlockPos(x, 10, z);				
					Biome mainBiome = player.getEntityWorld().getBiome(mainPos);
					BlockPos[] neighbors = new BlockPos[] { new BlockPos(x + 1, 10, z), new BlockPos(x - 1, 10, z), new BlockPos(x, 10, z + 1), new BlockPos(x, 10, z - 1) };
					for (BlockPos neighborPos : neighbors) {
						Biome neighborBiome = player.getEntityWorld().getBiome(neighborPos);
						if (!neighborBiome.equals(mainBiome)) {
							Int2Float1Combo a = new Int2Float1Combo(mainPos);
							Int2Float1Combo b = new Int2Float1Combo(neighborPos);
							if (a.x != b.x) {// if they have the same z and different x
								a.z += 1;
								if (a.x > b.x) b.x += 1;
								else a.x += 1;
							} else {// if they have the same x and different z
								a.x += 1;
								if (a.z > b.z) b.z += 1;
								else a.z += 1;
							}
							a.y = heightForPos(a.x, a.z, player.getEntityWorld(), playerNetPos);
							b.y = heightForPos(b.x, b.z, player.getEntityWorld(), playerNetPos);
							LineData lineData = new LineData(a, b);
							lineData.color = borderColor(mainBiome, neighborBiome);
							if (renderMode == RenderModes.WALL) {
								drawWall(lineData, matrix, builder);
							} else {
								CornerData cornerDataA = new CornerData(), cornerDataB = new CornerData();
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
								drawLine(lineData, matrix, builder);
								if (!corners.contains(cornerDataA)) corners.add(cornerDataA);
								else corners.get(corners.indexOf(cornerDataA)).ignoreSides(cornerDataA);
								if (!corners.contains(cornerDataB)) corners.add(cornerDataB);
								else corners.get(corners.indexOf(cornerDataB)).ignoreSides(cornerDataB);
							}
							
						}
					}
				}
			}
			// draw lines and corners
			if (renderMode != RenderModes.WALL) {
				drawCorners(corners, matrix, builder);
			}
			//end drawing
			event.getMatrixStack().pop();
			Minecraft.getInstance().getRenderTypeBuffers().getBufferSource().finish(RenderType.getLightning());
		}
	}

	private static final int minWallHeight = 0;
	private static final int maxWallHeight = 255;
	private static final float wallOffsetDivisor = 0b1111111111;
	
	private static void drawWall(LineData lineData, Matrix4f matrix, IVertexBuilder builder) {
		if (lineData.a.x == lineData.b.x) {
			// -x side
			builder.pos(matrix, lineData.a.x + (1 / wallOffsetDivisor), minWallHeight, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + (1 / wallOffsetDivisor), minWallHeight, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + (1 / wallOffsetDivisor), maxWallHeight, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x + (1 / wallOffsetDivisor), maxWallHeight, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +x side
			builder.pos(matrix, lineData.a.x - (1 / wallOffsetDivisor), maxWallHeight, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - (1 / wallOffsetDivisor), maxWallHeight, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - (1 / wallOffsetDivisor), minWallHeight, lineData.b.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - (1 / wallOffsetDivisor), minWallHeight, lineData.a.z).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();			
		} else {
			// -z side
			builder.pos(matrix, lineData.a.x, minWallHeight, lineData.a.z - (1 / wallOffsetDivisor)).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, minWallHeight, lineData.b.z - (1 / wallOffsetDivisor)).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, maxWallHeight, lineData.b.z - (1 / wallOffsetDivisor)).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x, maxWallHeight, lineData.a.z - (1 / wallOffsetDivisor)).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +z side
			builder.pos(matrix, lineData.a.x, maxWallHeight, lineData.a.z + (1 / wallOffsetDivisor)).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, maxWallHeight, lineData.b.z + (1 / wallOffsetDivisor)).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x, minWallHeight, lineData.b.z + (1 / wallOffsetDivisor)).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x, minWallHeight, lineData.a.z + (1 / wallOffsetDivisor)).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();			
		}
	}

	private static void drawLine(LineData lineData, Matrix4f matrix, IVertexBuilder builder) {
		if (lineData.a.x == lineData.b.x) {
			// top
			builder.pos(matrix, lineData.a.x + radius, lineData.a.y + radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y + radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - radius, lineData.b.y + radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y + radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// bottom
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y - radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - radius, lineData.b.y - radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y - radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x + radius, lineData.a.y - radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// -x side
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y + radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - radius, lineData.b.y + radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x - radius, lineData.b.y - radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y - radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +x side
			builder.pos(matrix, lineData.a.x + radius, lineData.a.y - radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y - radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y + radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x + radius, lineData.a.y + radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
		} else {
			// top
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y + radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y + radius, lineData.b.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y + radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y + radius, lineData.a.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// bottom
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y - radius, lineData.a.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y - radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y - radius, lineData.b.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y - radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// -z side
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y - radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y - radius, lineData.b.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y + radius, lineData.b.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y + radius, lineData.a.z - radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			// +z side
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y + radius, lineData.a.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y + radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.b.x + radius, lineData.b.y - radius, lineData.b.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
			builder.pos(matrix, lineData.a.x - radius, lineData.a.y - radius, lineData.a.z + radius).color(lineData.color.r, lineData.color.g, lineData.color.b, lineData.color.a).endVertex();
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
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showMinusX) {
			// -x side
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z - radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showPlusZ) {
			// +z side
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
			builder.pos(matrix, cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z + radius).color(cornerData.color.r, cornerData.color.g, cornerData.color.b, cornerData.color.a).endVertex();
		}
		if (cornerData.showMinusZ) {
			// -z side
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
		if (similarTemperature(a, b)) {
			return colorA;
		} else {
			return colorB;
		}
	}

	private static float heightForPos(float x, float z, World world, Float3 playerPos) {
		switch (renderMode) {
		case FIXED_HEIGHT:
			return (float) fixedHeight;
		case FOLLOW_PLAYER_HEIGHT:
			return playerBasedHeight(playerPos);
		case FOLLOW_PLAYER_IF_HIGHER_THAN_TERRAIN:
			float playerBasedHeight = playerBasedHeight(playerPos);
			float terrainBasedHeight = terrainBasedHeight(x, z, world);
			if (playerBasedHeight >= terrainBasedHeight) return playerBasedHeight;
			else return terrainBasedHeight;
		case MATCH_TERRAIN:
			return terrainBasedHeight(x, z, world);
		default:
			return 64;
		}
	}

	private static float playerBasedHeight(Float3 playerPos) {
		float height = (float) (playerPos.y + playerHeightOffset);
		if (height < 0) return 0;
		if (height > 256) return 256;
		return height;
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

	private static boolean similarTemperature(Biome a, Biome b) {
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
