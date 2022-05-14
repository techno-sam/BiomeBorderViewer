package mrp_v2.biomeborderviewer;

import com.google.gson.Gson;
import com.google.gson.JsonObject;
import com.google.gson.JsonSyntaxException;
import me.shedaniel.clothconfig2.api.ConfigBuilder;
import me.shedaniel.clothconfig2.api.ConfigCategory;
import me.shedaniel.clothconfig2.api.ConfigEntryBuilder;
import me.shedaniel.math.Color;
import mrp_v2.biomeborderviewer.client.renderer.BiomeBorderRenderType;
import mrp_v2.biomeborderviewer.client.renderer.debug.VisualizeBorders;
import mrp_v2.biomeborderviewer.config.BiomeBorderViewerConfig;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientChunkEvents;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.Screen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.LiteralText;
import net.minecraft.text.TranslatableText;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.lwjgl.glfw.GLFW;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class BiomeBorderViewer implements ClientModInitializer
{
    public static final String ID = "biome" + "border" + "viewer";
    public static final String DISPLAY_NAME = "Biome Border Viewer";
    public static final Logger LOGGER = LogManager.getLogger(ID);

    private static final Gson GSON = new Gson();

    private static final KeyBinding CONFIG_BIND = new KeyBinding(
            "biomeborderviewer.key.config",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_SEMICOLON,
            "biomeborderviewer.title.keys"
    );

    private static final KeyBinding BORDERS_BIND = new KeyBinding(
            "biomeborderviewer.key.showBorders",
            InputUtil.Type.KEYSYM,
            GLFW.GLFW_KEY_BACKSLASH,
            "biomeborderviewer.title.keys"
    );

    public static BiomeBorderViewerConfig config = new BiomeBorderViewerConfig();

    public BiomeBorderViewer() {}

    @Override
    public void onInitializeClient() {
        // This code runs as soon as Minecraft is in a mod-load-ready state.
        // However, some things (like resources) may still be uninitialized.
        // Proceed with mild caution.

        LOGGER.info("Hello Fabric world!");

        KeyBindingHelper.registerKeyBinding(CONFIG_BIND);
        KeyBindingHelper.registerKeyBinding(BORDERS_BIND);

        BiomeBorderRenderType.initBiomeBorderRenderType();

        loadConfig();
        VisualizeBorders.loadConfigSettings();

        ClientTickEvents.END_CLIENT_TICK.register(this::onEndTick);
        ClientChunkEvents.CHUNK_UNLOAD.register((clientWorld, chunk) -> VisualizeBorders.chunkUnload(clientWorld, chunk.getPos()));
        ClientChunkEvents.CHUNK_LOAD.register((clientWorld, chunk) -> VisualizeBorders.chunkLoad(clientWorld, chunk.getPos()));
        WorldRenderEvents.AFTER_ENTITIES.register((context) ->
        {
            VisualizeBorders.renderEvent(context.tickDelta(), context.matrixStack());
        });

    }

    private static Path getConfigPath() {
        return FabricLoader.getInstance().getConfigDir().resolve("eagleeyed.json");
    }

    public static void saveConfig() {
        JsonObject config = BiomeBorderViewer.config.save();

        try {
            Files.write(getConfigPath(), GSON.toJson(config).getBytes());
        }
        catch (IOException ex) {
            LOGGER.error("Failed to save BiomeBorderViewer config");
        }
    }

    private void resetEspConfigs() {
        config = new BiomeBorderViewerConfig();
    }

    private void loadConfig() {
        resetEspConfigs();
        try {
            JsonObject config = GSON.fromJson(new String(Files.readAllBytes(getConfigPath())), JsonObject.class);
            BiomeBorderViewer.config.load(config);
        }
        catch (IOException | JsonSyntaxException ex) {
            LOGGER.error("Failed to load BiomeBorderViewer config");
        }
    }

    private void onEndTick(MinecraftClient client) {
        if (BORDERS_BIND.wasPressed()) {
            VisualizeBorders.bordersKeyPressed();
        }
        if (CONFIG_BIND.isPressed()) {
            ConfigBuilder builder = ConfigBuilder.create()
                    .setParentScreen(MinecraftClient.getInstance().currentScreen)
                    .setTitle(new TranslatableText("biomeborderviewer.title.config"))
                    .setSavingRunnable(BiomeBorderViewer::saveConfig);

            ConfigEntryBuilder entryBuilder = builder.entryBuilder();

            ConfigCategory general = builder.getOrCreateCategory(new TranslatableText("biomeborderviewer.title.category.general"));

            general.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("biomeborderviewer.config.gui.border_a"),
                    Color.ofRGBA(config.borderAColor.red, config.borderAColor.green, config.borderAColor.blue, config.borderAColor.alpha))
                    .setDefaultValue(Color.ofRGBA(0, 255, 0, 64).getColor())
                    .setSaveConsumer((newColor) -> {
                        Color color = Color.ofTransparent(newColor);
                        config.borderAColor.setRed(color.getRed());
                        config.borderAColor.setGreen(color.getGreen());
                        config.borderAColor.setBlue(color.getBlue());
                        config.borderAColor.setAlpha(color.getAlpha());
                        VisualizeBorders.loadConfigSettings();
                    })
                    .setTooltip(new LiteralText("Color when the 2 biomes have similar temperatures."))
                    .setAlphaMode(true)
                    .build());

            general.addEntry(entryBuilder.startAlphaColorField(new TranslatableText("biomeborderviewer.config.gui.border_b"),
                    Color.ofRGBA(config.borderBColor.red, config.borderBColor.green, config.borderBColor.blue, config.borderBColor.alpha))
                    .setDefaultValue(Color.ofRGBA(255, 0, 0, 64).getColor())
                    .setSaveConsumer((newColor) -> {
                        Color color = Color.ofTransparent(newColor);
                        config.borderBColor.setRed(color.getRed());
                        config.borderBColor.setGreen(color.getGreen());
                        config.borderBColor.setBlue(color.getBlue());
                        config.borderBColor.setAlpha(color.getAlpha());
                        VisualizeBorders.loadConfigSettings();
                    })
                    .setTooltip(new LiteralText("Color when the 2 biomes have different temperatures."))
                    .setAlphaMode(true)
                    .build());

            general.addEntry(entryBuilder.startIntSlider(new TranslatableText("biomeborderviewer.config.gui.horizontalViewRange"),
                    config.horizontalViewRange,
                    0,
                    32)
                    .setDefaultValue(3)
                    .setSaveConsumer((newDistance) -> {
                        config.horizontalViewRange = newDistance;
                        VisualizeBorders.loadConfigSettings();
                    })
                    .setTooltip(new LiteralText("The horizontal distance to show biome borders around the player. Like render distance, but for the biome border. High values may impact performance."))
                    .build());

            general.addEntry(entryBuilder.startIntSlider(new TranslatableText("biomeborderviewer.config.gui.verticalViewRange"),
                    config.verticalViewRange,
                    0,
                    16)
                    .setDefaultValue(1)
                    .setSaveConsumer((newDistance) -> {
                        config.verticalViewRange = newDistance;
                        VisualizeBorders.loadConfigSettings();
                    })
                    .setTooltip(new LiteralText("The vertical distance to show biome borders above and below the player. High values may impact performance."))
                    .build());

            general.addEntry(entryBuilder.startIntSlider(new TranslatableText("biomeborderviewer.config.gui.borderCalculationThreads"),
                    config.borderCalculationThreads,
                    1,
                    16)
                    .setDefaultValue(1)
                    .setSaveConsumer((newNumber) -> {
                        config.borderCalculationThreads = newNumber;
                    })
                    .setTooltip(new LiteralText("How many threads to use to calculate the biome borders. Only change this if you know what you are doing!"))
                    .build());

            general.addEntry(entryBuilder.startBooleanToggle(new TranslatableText("biomeborderviewer.config.gui.force2dOverworld"),
                    config.force2dOverworld)
                    .setDefaultValue(true)
                    .setSaveConsumer((newBool) -> {
                        config.force2dOverworld = newBool;
                    })
                    .setTooltip(new LiteralText("If true, checks overworld biomes based on biome at Y0."))
                    .build());

            Screen screen = builder.build();
            client.setScreen(screen);
        }
    }
}
