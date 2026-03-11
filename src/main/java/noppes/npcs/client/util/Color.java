package noppes.npcs.client.util;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;
import net.minecraft.nbt.NBTTagCompound;
import org.lwjgl.opengl.GL11;

public class Color {
    public int color;
    public float alpha;

    public Color(int color) {
        setColor(color, 1);
    }

    public Color(int color, float alpha) {
        setColor(color, alpha);
    }

    public Color(int red, int green, int blue, float alpha) {
        setColor(red, green, blue, alpha);
    }

    public void setColor(int color, float alpha) {
        this.color = color;
        this.alpha = alpha;
    }

    public void setColor(int red, int green, int blue, float alpha) {
        this.color = (red << 16) + (green << 8) + blue;
        this.alpha = alpha;
    }

    public static Color lerpRGBA(Color color1, Color color2, float fraction) {
        fraction = Math.min(fraction, 1f);
        int red = (int) (color1.getRed() + ((color2.getRed() - color1.getRed()) * fraction));
        int green = (int) (color1.getGreen() + ((color2.getGreen() - color1.getGreen()) * fraction));
        int blue = (int) (color1.getBlue() + ((color2.getBlue() - color1.getBlue()) * fraction));
        float newAlpha = color1.alpha + ((color2.alpha - color1.alpha) * fraction);
        int newColor = (red << 16) + (green << 8) + blue;
        return new Color(newColor, newAlpha);
    }

    public Color lerpRGBA(Color color2, float fraction) {
        return Color.lerpRGBA(this, color2, fraction);
    }

    public Color multiply(float multi) {
        if (multi == 1)
            return this.clone();

        int r = (int) ((getRed() * multi));
        int g = (int) ((getGreen() * multi));
        int b = (int) ((getBlue() * multi));
        float a = alpha * multi;
        return new Color((r << 16) + (g << 8) + b, a);
    }

    @SideOnly(Side.CLIENT)
    public void glColor() {
        GL11.glColor4f(getRedF(), getGreenF(), getBlueF(), alpha);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound, String name) {
        compound.setInteger(name + "Color", color);
        compound.setFloat(name + "Alpha", alpha);

        return compound;
    }

    public void readFromNBT(NBTTagCompound compound, String name) {
        setColor(compound.getInteger(name + "Color"), compound.getFloat(name + "Alpha"));
    }

    public int getRed() {
        return (color >> 16 & 0xFF);
    }

    public float getRedF() {
        return (float) (color >> 16 & 0xFF) / 255f;
    }

    public int getGreen() {
        return (color >> 8 & 0xFF);
    }

    public float getGreenF() {
        return (float) (color >> 8 & 0xFF) / 255f;
    }

    public int getBlue() {
        return (color & 0xFF);
    }

    public float getBlueF() {
        return (float) (color & 0xFF) / 255f;
    }

    public static String getColor(int color) {
        String str;
        for (str = Integer.toHexString(color); str.length() < 6; str = "0" + str) {
        }
        return str;
    }

    public static String getColor(int color, float alpha) {
        String str = getColor(color);
        return str + " | " + (int) (alpha * 255);
    }

    public Color clone() {
        return new Color(color, alpha);
    }
}
