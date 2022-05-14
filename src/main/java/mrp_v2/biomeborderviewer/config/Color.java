package mrp_v2.biomeborderviewer.config;

import com.google.gson.JsonObject;

public class Color implements IBiomeBorderConfig {
    public int red;
    public int green;
    public int blue;
    public int alpha;

    public Color() {
        this(255, 255, 255, 255);
    }

    public Color(int red, int green, int blue) {
        this(red, green, blue, 255);
    }

    public Color(int red, int green, int blue, int alpha) {
        this.red = red;
        this.green = green;
        this.blue = blue;
        this.alpha = alpha;
    }

    private int minMax(int x) {
        return Math.min(255, Math.max(0, x));
    }

    public int getRed() {
        red = minMax(red);
        return red;
    }

    public void setRed(int red) {
        red = minMax(red);
        this.red = red;
    }

    public int getGreen() {
        green = minMax(green);
        return green;
    }

    public void setGreen(int green) {
        green = minMax(green);
        this.green = green;
    }

    public int getBlue() {
        blue = minMax(blue);
        return blue;
    }

    public void setBlue(int blue) {
        blue = minMax(blue);
        this.blue = blue;
    }

    public int getAlpha() {
        alpha = minMax(alpha);
        return alpha;
    }

    public void setAlpha(int alpha) {
        alpha = minMax(alpha);
        this.alpha = alpha;
    }

    @Override
    public JsonObject save() {
        JsonObject object = new JsonObject();
        object.addProperty("red", getRed());
        object.addProperty("green", getGreen());
        object.addProperty("blue", getBlue());
        object.addProperty("alpha", getAlpha());
        return object;
    }

    @Override
    public void load(JsonObject object) {
        setRed(object.get("red").getAsInt());
        setGreen(object.get("green").getAsInt());
        setBlue(object.get("blue").getAsInt());
        setAlpha(object.get("alpha").getAsInt());
    }

    public int toInt() {
        return me.shedaniel.math.Color.ofRGBA(getRed(), getGreen(), getBlue(), getAlpha()).getColor();
    }
}
