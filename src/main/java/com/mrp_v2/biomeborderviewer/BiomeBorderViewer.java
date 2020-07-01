package com.mrp_v2.biomeborderviewer;

import org.lwjgl.glfw.GLFW;

import com.mrp_v2.biomeborderviewer.config.BiomeBorderViewerConfig;

import net.minecraft.client.settings.KeyBinding;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.client.registry.ClientRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(BiomeBorderViewer.MODID)
public class BiomeBorderViewer {

	public static final String MODID = "biomeborderviewer";

	public static final KeyBinding SHOW_BORDERS = new KeyBinding(MODID + ".key.showBorders", GLFW.GLFW_KEY_B,
			MODID + ".key.categories");

	static {
		ClientRegistry.registerKeyBinding(SHOW_BORDERS);
	}

	public BiomeBorderViewer() {
		final IEventBus modEventBus = FMLJavaModLoadingContext.get().getModEventBus();
		ModLoadingContext.get().registerConfig(ModConfig.Type.CLIENT, BiomeBorderViewerConfig.clientSpec);
		modEventBus.register(BiomeBorderViewerConfig.class);
	}
}
