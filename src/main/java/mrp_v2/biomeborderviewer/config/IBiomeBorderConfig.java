package mrp_v2.biomeborderviewer.config;

import com.google.gson.Gson;
import com.google.gson.JsonObject;

public interface IBiomeBorderConfig {
    public JsonObject save();
    public void load(JsonObject object);
}
