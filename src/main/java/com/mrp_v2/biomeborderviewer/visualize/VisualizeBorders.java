package com.mrp_v2.biomeborderviewer.visualize;

import java.util.ArrayList;

import org.apache.logging.log4j.LogManager;
import org.lwjgl.opengl.GL11;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.platform.GlStateManager;
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
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
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

	private static ConfigOptions.baseLineHeightTypes baseLineHeight;

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
					Biome mainBiome = world.func_226691_t_(mainPos);
					BlockPos[] neighbors = new BlockPos[] { new BlockPos(x + 1, 10, z), new BlockPos(x - 1, 10, z),
							new BlockPos(x, 10, z + 1), new BlockPos(x, 10, z - 1) };
					for (BlockPos neighborPos : neighbors) {
						Biome neighborBiome = world.func_226691_t_(neighborPos);
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
			//draw(playerNetPos, lines, corners, ms);
			//IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().getRenderTypeBuffers().getBufferSource();
			IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().func_228019_au_().func_228487_b_();
			// !! or !!
			//IRenderTypeBuffer.Impl buffer = Minecraft.getInstance().func_228019_au_().func_228487_c_();
			//IVertexBuilder builder = buffer.getBuffer(RenderType.QUADS);
			IVertexBuilder builder = buffer.getBuffer(RenderType.func_228633_a_("quad", DefaultVertexFormats.POSITION_COLOR, 1, 256, showingBorders, showingBorders, null));
			MatrixStack ms = event.getMatrixStack();
			//ms.push();
			ms.func_227860_a_();
			//ms.translate(-playerNetPos.x, -playerNetPos.y, -playerNetPos.z);
			ms.func_227862_a_(-playerNetPos.x, -playerNetPos.y, -playerNetPos.z);
			//ms.getLast().getMatrix();
			Matrix4f matrix = ms.func_227866_c_().func_227870_a_();
			
		}
	}

	private static void draw(Vector3Float playerPos, ArrayList<LineData> lines, ArrayList<CornerData> corners, MatrixStack ms) {
		GlStateManager.pushMatrix();
		GlStateManager.disableBlend();
		GlStateManager.disableTexture();
		GlStateManager.translatef(-playerPos.x, -playerPos.y, -playerPos.z);
		drawLines(lines);
		drawCorners(corners);
		GlStateManager.enableBlend();
		GlStateManager.enableTexture();
		GlStateManager.popMatrix();
	}

	private static void drawLines(ArrayList<LineData> lines) {
		for (LineData lineData : lines) {
			drawLine(lineData);
		}
	}

	private static void drawLine(LineData lineData) {
		GlStateManager.color3f(lineData.color.r / 255f, lineData.color.g / 255f, lineData.color.b / 255f);
		GlStateManager.begin(GL11.GL_QUADS);
		if (lineData.a.x == lineData.b.x) {
			// top
			GlStateManager.vertex3f(lineData.a.x + radius, lineData.a.y + radius, lineData.a.z);
			GlStateManager.vertex3f(lineData.b.x + radius, lineData.b.y + radius, lineData.b.z);
			GlStateManager.vertex3f(lineData.b.x - radius, lineData.b.y + radius, lineData.b.z);
			GlStateManager.vertex3f(lineData.a.x - radius, lineData.a.y + radius, lineData.a.z);
			// bottom
			GlStateManager.vertex3f(lineData.a.x - radius, lineData.a.y - radius, lineData.a.z);
			GlStateManager.vertex3f(lineData.b.x - radius, lineData.b.y - radius, lineData.b.z);
			GlStateManager.vertex3f(lineData.b.x + radius, lineData.b.y - radius, lineData.b.z);
			GlStateManager.vertex3f(lineData.a.x + radius, lineData.a.y - radius, lineData.a.z);
			// -x side
			GlStateManager.vertex3f(lineData.a.x - radius, lineData.a.y + radius, lineData.a.z);
			GlStateManager.vertex3f(lineData.b.x - radius, lineData.b.y + radius, lineData.b.z);
			GlStateManager.vertex3f(lineData.b.x - radius, lineData.b.y - radius, lineData.b.z);
			GlStateManager.vertex3f(lineData.a.x - radius, lineData.a.y - radius, lineData.a.z);
			// +x side
			GlStateManager.vertex3f(lineData.a.x + radius, lineData.a.y - radius, lineData.a.z);
			GlStateManager.vertex3f(lineData.b.x + radius, lineData.b.y - radius, lineData.b.z);
			GlStateManager.vertex3f(lineData.b.x + radius, lineData.b.y + radius, lineData.b.z);
			GlStateManager.vertex3f(lineData.a.x + radius, lineData.a.y + radius, lineData.a.z);
		} else {
			// top
			GlStateManager.vertex3f(lineData.a.x, lineData.a.y + radius, lineData.a.z - radius);
			GlStateManager.vertex3f(lineData.b.x, lineData.b.y + radius, lineData.b.z - radius);
			GlStateManager.vertex3f(lineData.b.x, lineData.b.y + radius, lineData.b.z + radius);
			GlStateManager.vertex3f(lineData.a.x, lineData.a.y + radius, lineData.a.z + radius);
			// bottom
			GlStateManager.vertex3f(lineData.a.x, lineData.a.y - radius, lineData.a.z + radius);
			GlStateManager.vertex3f(lineData.b.x, lineData.b.y - radius, lineData.b.z + radius);
			GlStateManager.vertex3f(lineData.b.x, lineData.b.y - radius, lineData.b.z - radius);
			GlStateManager.vertex3f(lineData.a.x, lineData.a.y - radius, lineData.a.z - radius);
			// -z side
			GlStateManager.vertex3f(lineData.a.x, lineData.a.y - radius, lineData.a.z - radius);
			GlStateManager.vertex3f(lineData.b.x, lineData.b.y - radius, lineData.b.z - radius);
			GlStateManager.vertex3f(lineData.b.x, lineData.b.y + radius, lineData.b.z - radius);
			GlStateManager.vertex3f(lineData.a.x, lineData.a.y + radius, lineData.a.z - radius);
			// +z side
			GlStateManager.vertex3f(lineData.a.x, lineData.a.y + radius, lineData.a.z + radius);
			GlStateManager.vertex3f(lineData.b.x, lineData.b.y + radius, lineData.b.z + radius);
			GlStateManager.vertex3f(lineData.b.x, lineData.b.y - radius, lineData.b.z + radius);
			GlStateManager.vertex3f(lineData.a.x, lineData.a.y - radius, lineData.a.z + radius);
		}
		GlStateManager.end();
	}

	private static void drawCorners(ArrayList<CornerData> corners) {
		for (CornerData cornerData : corners) {
			drawCorner(cornerData);
		}
	}

	private static void drawCorner(CornerData cornerData) {//x's on wrong sides
		GlStateManager.color3f(cornerData.color.r / 255f, cornerData.color.g / 255f, cornerData.color.b / 255f);
		GlStateManager.begin(GL11.GL_QUADS);
		if (cornerData.showPlusX) {// +x side
			GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z - radius);
			GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z - radius);
			GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z + radius);
			GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z + radius);
		}
		if (cornerData.showMinusX) {// -x side
			GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z + radius);
			GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z + radius);
			GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z - radius);
			GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z - radius);
		}
		if (cornerData.showPlusZ) {// +z side
			GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z + radius);
			GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z + radius);
			GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z + radius);
			GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z + radius);
		}
		if (cornerData.showMinusZ) {// -z side
			GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z - radius);
			GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z - radius);
			GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z - radius);
			GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z - radius);
		}
		//top
		GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z + radius);
		GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y + radius, cornerData.pos.z - radius);
		GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z - radius);
		GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y + radius, cornerData.pos.z + radius);
		//bottom
		GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z + radius);
		GlStateManager.vertex3f(cornerData.pos.x - radius, cornerData.pos.y - radius, cornerData.pos.z - radius);
		GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z - radius);
		GlStateManager.vertex3f(cornerData.pos.x + radius, cornerData.pos.y - radius, cornerData.pos.z + radius);
		GlStateManager.end();
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
		switch (baseLineHeight) {
		case fixed:
			height = fixedHeight;
			break;
		case player:
			height = playerBasedHeight(playerPos);
			break;
		case playerThenTerrain:
			double playerBasedHeight = playerBasedHeight(playerPos);
			double terrainBasedHeight = terrainBasedHeight(x, z, world);
			if (playerBasedHeight >= terrainBasedHeight) {
				height = playerBasedHeight;
			} else {
				height = terrainBasedHeight;
			}
			break;
		case terrain:
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
		baseLineHeight = ConfigOptions.baseLineHeight.get();
		colorA = new Color(ConfigOptions.lineAR.get(), ConfigOptions.lineAG.get(), ConfigOptions.lineAB.get());
		colorB = new Color(ConfigOptions.lineBR.get(), ConfigOptions.lineBG.get(), ConfigOptions.lineBB.get());
		radius = ConfigOptions.lineWidth.get().floatValue() / 2;
	}
}
