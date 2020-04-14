package com.mrp_v2.biomeborderviewer.config;

import org.apache.commons.lang3.tuple.Pair;

import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.DoubleValue;
import net.minecraftforge.common.ForgeConfigSpec.EnumValue;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigOptions {

	private static final String translationKey = "mrp_v2.biomeborderviewer.configgui.";

	public enum RenderModes {
		LINE_FOLLOW_PLAYER_HEIGHT, LINE_MATCH_TERRAIN, LINE_FOLLOW_PLAYER_IF_HIGHER_THAN_TERRAIN, LINE_FIXED_HEIGHT, WALL
	}

	public static EnumValue<RenderModes> renderMode;

	public static IntValue lineAR;
	public static IntValue lineAG;
	public static IntValue lineAB;
	public static IntValue lineAA;

	public static IntValue lineBR;
	public static IntValue lineBG;
	public static IntValue lineBB;
	public static IntValue lineBA;

	public static DoubleValue playerHeightOffset;
	public static DoubleValue terrainHeightOffset;
	public static DoubleValue fixedHeight;

	public static DoubleValue lineWidth;

	public static IntValue viewRange;

	public static class Client {

		Client(ForgeConfigSpec.Builder builder) {
			builder.comment("biome border viewer client settings").push("client");

			renderMode = builder.comment("What the reference point for the border-line height is."
					+ "\n LINE_FOLLOW_PLAYER_HEIGHT - The line follows the height of the player + playerHeightOffset"
					+ "\n LINE_MATCH_TERRAIN - The line follows the height of the highest block + terrainHeightOffset"
					+ "\n LINE_FOLLOW_PLAYER_IF_HIGHER_THAN_TERRAIN - The line follows the player height, unless the terrain height is higher."
					+ "\n LINE_FIXED_HEIGHT - The height of the line is fixed at fixedHeight"
					+ "\n WALL - Makes a wall going from y 0-255. It is recommened to make the line more transparent (decrease the alpha value) in this scenario.")
					.translation(translationKey + "renderMode").defineEnum("renderMode", RenderModes.WALL);

			lineAR = builder.comment("The red value of the line's color when the 2 biomes have the same temperature.")
					.translation(translationKey + "lineAR").defineInRange("lineAR", 0, 0, 255);

			lineAG = builder.comment("The green value of the line's color when the 2 biomes have the same temperature.")
					.translation(translationKey + "lineAG").defineInRange("lineAG", 255, 0, 255);

			lineAB = builder.comment("The blue value of the line's color when the 2 biomes have the same temperature.")
					.translation(translationKey + "lineAB").defineInRange("lineAB", 0, 0, 255);

			lineAA = builder.comment(
					"The alpha (transparency) value of the line's color when the 2 biomes have the same temperature.")
					.translation(translationKey + "lineAA").defineInRange("lineAA", 128, 0, 255);

			lineBR = builder.comment("The red value of the line's color when the 2 biomes have different temperatures.")
					.translation(translationKey + "lineBR").defineInRange("lineBR", 255, 0, 255);

			lineBG = builder
					.comment("The green value of the line's color when the 2 biomes have different temperatures.")
					.translation(translationKey + "lineBG").defineInRange("lineBG", 0, 0, 255);

			lineBB = builder
					.comment("The blue value of the line's color when the 2 biomes have different temperatures.")
					.translation(translationKey + "lineBB").defineInRange("lineBB", 0, 0, 255);

			lineBA = builder.comment(
					"The alpha (transparency) value of the line's color when the 2 biomes have the same temperature.")
					.translation(translationKey + "lineBA").defineInRange("lineBA", 128, 0, 255);

			playerHeightOffset = builder
					.comment("The height offset from the player's eyes that the lines are drawn at.")
					.translation(translationKey + "playerHeightOffset")
					.defineInRange("playerHeightOffset", 1.0, -256.0, 256.0);

			terrainHeightOffset = builder.comment("The height offset from the terrain that the lines are drawn at.")
					.translation(translationKey + "terrainHeightOffset")
					.defineInRange("terrainHeightOffset", 1.0, -256.0, 256.0);

			fixedHeight = builder.comment("The height that the lines are drawn at.")
					.translation(translationKey + "fixedHeight").defineInRange("fixedHeight", 64.0, 0.0, 256.0);

			lineWidth = builder.comment("The width of the line").translation(translationKey + "lineWidth")
					.defineInRange("lineWidth", .0625, 0.01, 0.4);

			viewRange = builder
					.comment("The radius of the square of chunks around the player to show the border.\n"
							+ "Similar to render distance, but for the biome border.\n"
							+ "High values may impact performance.")
					.translation(translationKey + "viewRange").defineInRange("viewRange", 4, 1, 32);
		}
	}

	public static final ForgeConfigSpec clientSpec;
	public static final Client CLIENT;
	static {
		final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
		clientSpec = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		VisualizeBorders.loadConfigSettings();
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.Reloading configEvent) {
		VisualizeBorders.loadConfigSettings();
	}
}
