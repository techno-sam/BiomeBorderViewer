package mrp_v2.biomeborderviewer.config;

import com.google.gson.JsonObject;

public class BiomeBorderViewerConfig implements IBiomeBorderConfig {

    public Color borderAColor;
    public Color borderBColor;
    public int horizontalViewRange;
    public int verticalViewRange;
    public int borderCalculationThreads;
    public boolean force2dOverworld;

    public BiomeBorderViewerConfig() {
        this(new Color(), new Color(), 3, 1, 1, true);
    }

    public BiomeBorderViewerConfig(Color borderAColor, Color borderBColor, int horizontalViewRange, int verticalViewRange, int borderCalculationThreads, boolean force2dOverworld) {
        this.borderAColor = borderAColor;
        this.borderBColor = borderBColor;
        this.horizontalViewRange = horizontalViewRange;
        this.verticalViewRange = verticalViewRange;
        this.borderCalculationThreads = borderCalculationThreads;
        this.force2dOverworld = true;
    }

    @Override
    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.add("borderAColor", borderAColor.save());
        object.add("borderBColor", borderBColor.save());
        object.addProperty("horizontalViewRange", horizontalViewRange);
        object.addProperty("verticalViewRange", verticalViewRange);
        object.addProperty("borderCalculationThreads", borderCalculationThreads);
        object.addProperty("force2dOverWorld", force2dOverworld);
        return object;
    }

    @Override
    public void load(JsonObject object) {
        borderAColor = new Color();
        borderAColor.load(object.getAsJsonObject("borderAColor"));

        borderBColor = new Color();
        borderBColor.load(object.getAsJsonObject("borderBColor"));

        if (object.has("horizontalViewRange")) {
            horizontalViewRange = object.get("horizontalViewRange").getAsInt();
        }

        if (object.has("verticalViewRange")) {
            verticalViewRange = object.get("verticalViewRange").getAsInt();
        }

        if (object.has("borderCalculationThreads")) {
            borderCalculationThreads = object.get("borderCalculationThreads").getAsInt();
        }

        if (object.has("force2dOverworld")) {
            force2dOverworld = object.get("force2dOverWorld").getAsBoolean();
        }
    }
}
