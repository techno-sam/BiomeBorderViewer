package com.mrp_v2.biomeborderviewer.config;

import org.apache.commons.lang3.tuple.Pair;

import com.mrp_v2.biomeborderviewer.util.Color;
import com.mrp_v2.biomeborderviewer.visualize.VisualizeBorders;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.IntValue;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.config.ModConfig;

public class ConfigOptions {

	public static class Client {

		Client(ForgeConfigSpec.Builder builder) {
			builder.comment("biome border viewer client settings").push("client");

			borderA_R = builder
					.comment("The red value of the line's color when the 2 biomes have similar temperatures.")
					.translation(translationKey + "borderA_R").defineInRange("borderA_R", 0, 0, 255);

			borderA_G = builder
					.comment("The green value of the line's color when the 2 biomes have similar temperatures.")
					.translation(translationKey + "borderA_G").defineInRange("borderA_G", 255, 0, 255);

			borderA_B = builder
					.comment("The blue value of the line's color when the 2 biomes have similar temperatures.")
					.translation(translationKey + "borderA_B").defineInRange("borderA_B", 0, 0, 255);

			borderA_A = builder.comment(
					"The alpha (transparency) value of the line's color when the 2 biomes have similar temperatures.")
					.translation(translationKey + "borderA_A").defineInRange("borderA_A", 128, 0, 255);

			borderB_R = builder
					.comment("The red value of the line's color when the 2 biomes have unsimilar temperatures.")
					.translation(translationKey + "borderB_R").defineInRange("borderB_R", 255, 0, 255);

			borderB_G = builder
					.comment("The green value of the line's color when the 2 biomes have unsimilar temperatures.")
					.translation(translationKey + "borderB_G").defineInRange("borderB_G", 0, 0, 255);

			borderB_B = builder
					.comment("The blue value of the line's color when the 2 biomes have unsimilar temperatures.")
					.translation(translationKey + "borderB_B").defineInRange("borderB_B", 0, 0, 255);

			borderB_A = builder.comment(
					"The alpha (transparency) value of the line's color when the 2 biomes have unsimilar temperatures.")
					.translation(translationKey + "borderB_A").defineInRange("borderB_A", 128, 0, 255);

			horizontalViewRange = builder
					.comment("The horizontal distance to show biome borders around the player.\n"
							+ "Like render distance, but for the biome border.\nHigh values may impact performance.")
					.translation(translationKey + "horizontalViewRange").defineInRange("horizontalViewRange", 4, 1, 32);

			verticalViewRange = builder
					.comment("The vertical distance to show biome borders above and below the player.\n"
							+ "High values may impact performance.")
					.translation(translationKey + "verticalViewRange").defineInRange("verticalViewRange", 4, 1, 16);
		}
	}

	private static final String translationKey = "biomeborderviewer.configgui.";

	public static IntValue borderA_R;
	public static IntValue borderA_G;
	public static IntValue borderA_B;
	public static IntValue borderA_A;

	public static IntValue borderB_R;
	public static IntValue borderB_G;
	public static IntValue borderB_B;
	public static IntValue borderB_A;

	public static IntValue horizontalViewRange;
	public static IntValue verticalViewRange;

	public static final ForgeConfigSpec clientSpec;
	public static final Client CLIENT;

	static {
		final Pair<Client, ForgeConfigSpec> specPair = new ForgeConfigSpec.Builder().configure(Client::new);
		clientSpec = specPair.getRight();
		CLIENT = specPair.getLeft();
	}

	public static Color getColorA() {
		return new Color(borderA_R.get(), borderA_G.get(), borderA_B.get(), borderA_A.get());
	}

	public static Color getColorB() {
		return new Color(borderB_R.get(), borderB_G.get(), borderB_B.get(), borderB_A.get());
	}

	@SubscribeEvent
	public static void onFileChange(final ModConfig.Reloading configEvent) {
		VisualizeBorders.loadConfigSettings();
	}

	@SubscribeEvent
	public static void onLoad(final ModConfig.Loading configEvent) {
		VisualizeBorders.loadConfigSettings();
	}
}
