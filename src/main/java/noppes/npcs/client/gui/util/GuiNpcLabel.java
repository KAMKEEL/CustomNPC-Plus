package noppes.npcs.client.gui.util;

import kamkeel.npcs.util.TextSplitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiScreen;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.StatCollector;
import noppes.npcs.client.CustomNpcResourceListener;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.Iterator;
import java.util.List;

public class GuiNpcLabel extends Gui {
    public String label;
    public int x, y, color;
    public boolean enabled = true;
    public int id;
    public boolean drawShadow = false;

    // Hover text support
    public String hoverableText = "";
    private boolean wasHovered = false;
    private int hoverCount = 0;

    public GuiNpcLabel(int id, Object label, int x, int y, int color) {
        this.id = id;
        this.label = StatCollector.translateToLocal(label.toString());
        this.x = x;
        this.y = y;
        this.color = color;
    }

    public GuiNpcLabel(int id, Object label, int x, int y) {
        this(id, label, x, y, CustomNpcResourceListener.DefaultTextColor);
    }

    public void setHoverText(String text) {
        this.hoverableText = text;
    }

    public void drawLabel(GuiScreen gui, FontRenderer fontRenderer) {
        if (enabled)
            fontRenderer.drawString(label, x, y, color, drawShadow);
    }

    public void center(int width) {
        int size = Minecraft.getMinecraft().fontRenderer.getStringWidth(label);
        x += (width - size) / 2;
    }

    public boolean isMouseOver(int mouseX, int mouseY, FontRenderer fr) {
        int width = fr.getStringWidth(label);
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + 10;
    }

    /**
     * Draws hover tooltip if the mouse is over this label.
     *
     * @param localMouseX  Mouse X in scroll-local coordinates (for hit testing)
     * @param localMouseY  Mouse Y in scroll-local coordinates (for hit testing)
     * @param screenMouseX Mouse X in screen coordinates (for tooltip positioning)
     * @param screenMouseY Mouse Y in screen coordinates (for tooltip positioning)
     * @param hasSubGui    Whether a sub-gui is open
     * @param fr           FontRenderer for measuring label width
     */
    public void drawHover(int localMouseX, int localMouseY, int screenMouseX, int screenMouseY, boolean hasSubGui, FontRenderer fr) {
        if (hasSubGui || hoverableText.isEmpty()) return;

        boolean hovered = isMouseOver(localMouseX, localMouseY, fr);
        if (!hovered) {
            wasHovered = false;
            hoverCount = 0;
            return;
        }
        if (!wasHovered) {
            wasHovered = true;
            hoverCount = 0;
        }
        if (hoverCount < 65) hoverCount++;

        if (hoverCount > 60) {
            GL11.glPushMatrix();
            Minecraft mc = Minecraft.getMinecraft();
            String displayString = StatCollector.translateToLocal(hoverableText);
            GL11.glColor4f(1, 1, 1, 1);
            List<String> lines = TextSplitter.splitText(displayString, 30);
            drawHoveringText(lines, screenMouseX, screenMouseY, mc);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    @SuppressWarnings("rawtypes")
    protected void drawHoveringText(List textLines, int x, int y, Minecraft mc) {
        if (mc.fontRenderer == null || textLines.isEmpty())
            return;
        GL11.glDisable(GL12.GL_RESCALE_NORMAL);
        RenderHelper.disableStandardItemLighting();
        GL11.glDisable(GL11.GL_LIGHTING);
        GL11.glDisable(GL11.GL_DEPTH_TEST);

        int maxWidth = 0;
        Iterator iterator = textLines.iterator();
        while (iterator.hasNext()) {
            String s = (String) iterator.next();
            int lineWidth = mc.fontRenderer.getStringWidth(s);
            if (lineWidth > maxWidth) {
                maxWidth = lineWidth;
            }
        }

        int j2 = x + 12;
        int k2 = y - 12;
        int maxHeight = 8;

        if (textLines.size() > 1) {
            maxHeight += 2 + (textLines.size() - 1) * 10;
        }

        ScaledResolution scaledRes = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int screenWidth = scaledRes.getScaledWidth();
        int screenHeight = scaledRes.getScaledHeight();

        if (j2 + maxWidth + 6 > screenWidth) {
            j2 = x - maxWidth - 16;
        }
        if (j2 < 4) {
            j2 = 4;
        }
        if (k2 + maxHeight + 6 > screenHeight) {
            k2 = screenHeight - maxHeight - 6;
        }
        if (k2 < 4) {
            k2 = 4;
        }

        this.zLevel = 300.0F;
        int j1 = -267386864;
        this.drawGradientRect(j2 - 3, k2 - 4, j2 + maxWidth + 3, k2 - 3, j1, j1);
        this.drawGradientRect(j2 - 3, k2 + maxHeight + 3, j2 + maxWidth + 3, k2 + maxHeight + 4, j1, j1);
        this.drawGradientRect(j2 - 3, k2 - 3, j2 + maxWidth + 3, k2 + maxHeight + 3, j1, j1);
        this.drawGradientRect(j2 - 4, k2 - 3, j2 - 3, k2 + maxHeight + 3, j1, j1);
        this.drawGradientRect(j2 + maxWidth + 3, k2 - 3, j2 + maxWidth + 4, k2 + maxHeight + 3, j1, j1);
        int k1 = 1347420415;
        int l1 = (k1 & 16711422) >> 1 | k1 & -16777216;
        this.drawGradientRect(j2 - 3, k2 - 3 + 1, j2 - 3 + 1, k2 + maxHeight + 3 - 1, k1, l1);
        this.drawGradientRect(j2 + maxWidth + 2, k2 - 3 + 1, j2 + maxWidth + 3, k2 + maxHeight + 3 - 1, k1, l1);
        this.drawGradientRect(j2 - 3, k2 - 3, j2 + maxWidth + 3, k2 - 3 + 1, k1, k1);
        this.drawGradientRect(j2 - 3, k2 + maxHeight + 2, j2 + maxWidth + 3, k2 + maxHeight + 3, l1, l1);

        for (int i2 = 0; i2 < textLines.size(); ++i2) {
            String s1 = (String) textLines.get(i2);
            mc.fontRenderer.drawStringWithShadow(s1, j2, k2, -1);
            if (i2 == 0) {
                k2 += 2;
            }
            k2 += 10;
        }

        this.zLevel = 0.0F;
        GL11.glEnable(GL11.GL_LIGHTING);
        GL11.glEnable(GL11.GL_DEPTH_TEST);
        RenderHelper.enableStandardItemLighting();
        GL11.glEnable(GL12.GL_RESCALE_NORMAL);
    }
}
