package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.Tessellator;
import org.lwjgl.input.Mouse;
import org.lwjgl.opengl.GL11;

public class GuiUtil {

    public static void setScissorClip(int x, int y, int width, int height) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution res = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = res.getScaleFactor();


        // Adjust position to Top-Left origin (OpenGL window/screen space uses bottom-left origin)
        y = mc.displayHeight - y * scale;
        height *= scale;

        // Set clip
        GL11.glScissor(x * scale, y - height, width * scale, height);
    }

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

    public static void drawRectD(double left, double top, double right, double bottom) {
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

        Tessellator tessellator = Tessellator.instance;
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        tessellator.startDrawingQuads();
        tessellator.addVertex(left, bottom, 0.0D);
        tessellator.addVertex(right, bottom, 0.0D);
        tessellator.addVertex(right, top, 0.0D);
        tessellator.addVertex(left, top, 0.0D);
        tessellator.draw();
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glDisable(GL11.GL_BLEND);
    }


    public static void drawRectD(double left, double top, double right, double bottom, int color) {
        float f3 = (float) (color >> 24 & 255) / 255.0F;
        float f = (float) (color >> 16 & 255) / 255.0F;
        float f1 = (float) (color >> 8 & 255) / 255.0F;
        float f2 = (float) (color & 255) / 255.0F;
        GL11.glColor4f(f, f1, f2, f3);
        drawRectD(left, top, right, bottom);
    }

    public static void drawGradientRect(int left, int top, int right, int bottom, int startColor, int endColor) {
        double zLevel = 0;

        float f = (float) (startColor >> 24 & 255) / 255.0F;
        float f1 = (float) (startColor >> 16 & 255) / 255.0F;
        float f2 = (float) (startColor >> 8 & 255) / 255.0F;
        float f3 = (float) (startColor & 255) / 255.0F;
        float f4 = (float) (endColor >> 24 & 255) / 255.0F;
        float f5 = (float) (endColor >> 16 & 255) / 255.0F;
        float f6 = (float) (endColor >> 8 & 255) / 255.0F;
        float f7 = (float) (endColor & 255) / 255.0F;
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(f1, f2, f3, f);
        tessellator.addVertex(right, top, zLevel);
        tessellator.addVertex(left, top, zLevel);
        tessellator.setColorRGBA_F(f5, f6, f7, f4);
        tessellator.addVertex(left, bottom, zLevel);
        tessellator.addVertex(right, bottom, zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }
    
    public static void drawGradientRectHorizontal(int left, int top, int right, int bottom, int startColor, int endColor) {
        double zLevel = 0;

        float sA = (float) (startColor >> 24 & 255) / 255.0F;
        float sR = (float) (startColor >> 16 & 255) / 255.0F;
        float sG = (float) (startColor >> 8 & 255) / 255.0F;
        float sB = (float) (startColor & 255) / 255.0F;

        float eA = (float) (endColor >> 24 & 255) / 255.0F;
        float eR = (float) (endColor >> 16 & 255) / 255.0F;
        float eG = (float) (endColor >> 8 & 255) / 255.0F;
        float eB = (float) (endColor & 255) / 255.0F;

        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glDisable(GL11.GL_ALPHA_TEST);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glShadeModel(GL11.GL_SMOOTH);
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.setColorRGBA_F(sR, sG, sB, sA);
        tessellator.addVertex(left, top, zLevel);
        tessellator.addVertex(left, bottom, zLevel);
        tessellator.setColorRGBA_F(eR, eG, eB, eA);
        tessellator.addVertex(right, bottom, zLevel);
        tessellator.addVertex(right, top, zLevel);
        tessellator.draw();
        GL11.glShadeModel(GL11.GL_FLAT);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glEnable(GL11.GL_ALPHA_TEST);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
    }

    public static void drawTexturedModalRect(double x, double y, double width, double height, int textureX, int textureY) {
        double zLevel = 0;
        float f = 1 / 256f;
        float f1 = 1 / 256f;
        Tessellator tessellator = Tessellator.instance;
        tessellator.startDrawingQuads();
        tessellator.addVertexWithUV(x + 0, y + height, zLevel, (float) (textureX) * f, (float) (textureY + height) * f1);
        tessellator.addVertexWithUV(x + width, y + height, zLevel, (float) (textureX + width) * f, (float) (textureY + height) * f1);
        tessellator.addVertexWithUV(x + width, y + 0, zLevel, (float) (textureX + width) * f, (float) (textureY) * f1);
        tessellator.addVertexWithUV(x + 0, y + 0, zLevel, (float) (textureX) * f, (float) (textureY) * f1);
        tessellator.draw();
    }

    /**
     * Draw a scaled textured rectangle (for rendering icons).
     */
    public static void drawScaledTexturedRect(int x, int y, int u, int v, int srcWidth, int srcHeight,
                                              int destWidth, int destHeight, int textureWidth, int textureHeight) {
        float uScale = 1.0f / textureWidth;
        float vScale = 1.0f / textureHeight;

        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        GL11.glBegin(GL11.GL_QUADS);
        GL11.glTexCoord2f(u * uScale, v * vScale);
        GL11.glVertex2f(x, y);
        GL11.glTexCoord2f(u * uScale, (v + srcHeight) * vScale);
        GL11.glVertex2f(x, y + destHeight);
        GL11.glTexCoord2f((u + srcWidth) * uScale, (v + srcHeight) * vScale);
        GL11.glVertex2f(x + destWidth, y + destHeight);
        GL11.glTexCoord2f((u + srcWidth) * uScale, v * vScale);
        GL11.glVertex2f(x + destWidth, y);
        GL11.glEnd();

        GL11.glDisable(GL11.GL_BLEND);
    }


    public static void setMouse(int guiX, int guiY) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        int factor = scaledResolution.getScaleFactor();

        Mouse.setCursorPosition(guiX * factor, mc.displayHeight - (guiY * factor));
    }

    public static void setMouseX(int guiX) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        int factor = scaledResolution.getScaleFactor();
        Mouse.setCursorPosition(guiX * factor, Mouse.getY());
    }

    public static void setMouseY(int guiY) {
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution scaledResolution = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);

        int factor = scaledResolution.getScaleFactor();
        Mouse.setCursorPosition(Mouse.getX(), mc.displayHeight - (guiY * factor));
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
