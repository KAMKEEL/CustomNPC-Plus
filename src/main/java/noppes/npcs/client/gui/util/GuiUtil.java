package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiUtil {

    public static void drawHorizontalLine(double y, double startX, double endX, int color) {
        if (endX < startX) {
            double i1 = startX;
            startX = endX;
            endX = i1;
        }

        drawRectD(startX, y, endX + 1, y + 1, color);
    }

    public static void drawVerticalLine(double x, double startY, double endY, int color) {
        if (endY < startY) {
            double i1 = startY;
            startY = endY;
            endY = i1;
        }

        drawRectD(x, startY + 1, x + 1, endY, color);
    }

    public static void drawRectD(double left, double top, double right, double bottom, int color) {
        double j1;

        if (left < right) {
            j1 = left;
            left = right;
            right = j1;
        }

        if (top < bottom) {
            j1 = top;
            top = bottom;
            bottom = j1;
        }

        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glColor4f(f, f1, f2, f3);
        tessellator.startDrawingQuads();
        tessellator.addVertex(left, bottom, 0.0D);
        tessellator.addVertex(right, bottom, 0.0D);
        tessellator.addVertex(right, top, 0.0D);
        tessellator.addVertex(left, top, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }

    public static void drawTexturedModalRect(double x, double y, double width, double height, int textureX, int textureY) {
        double zLevel = 0;
        float f = 0.00390625F;
        float f1 = 0.00390625F;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 0, y + height, zLevel, (float) (textureX) * f, (float) (textureY + height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, zLevel, (float) (textureX + width) * f, (float) (textureY + height) * f1);
        tessellator.addVertexWithUV(x + width, y + 0, zLevel, (float) (textureX + width) * f, (float) (textureY) * f1);
        tessellator.addVertexWithUV(x + 0, y + 0, zLevel, (float) (textureX) * f, (float) (textureY) * f1);
        tessellator.draw();
    }

    public static double preciseMouseX() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        double width = scaledResolution.getScaledWidth();
        return Mouse.getX() * width / mc.displayWidth;
    }

    public static double preciseMouseY() {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        double height = scaledResolution.getScaledHeight();
        return height - Mouse.getY() * height / mc.displayHeight - 1;
    }
}
