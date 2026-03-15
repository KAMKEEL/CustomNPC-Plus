package noppes.npcs.client.gui.util;

import kamkeel.npcs.util.TextSplitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.RenderHelper;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.StatCollector;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL12;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.atomic.AtomicBoolean;

public class GuiNpcButton extends GuiButton {

    protected String[] display;
    private int displayValue = 0;
    public int id;
    public String hoverableText = "";
    public int oldHover;
    public int hoverCount = 0;
    boolean hasSubGUI = false;

    // Fields for icon texture and its dimensions.
    protected ResourceLocation iconTexture = null;
    protected int iconWidth = 16;
    protected int iconHeight = 16;

    protected int iconPosX = 0;
    protected int iconPosY = 0;

    public boolean rightClickable;

    public GuiNpcButton(int i, int j, int k, String s) {
        super(i, j, k, StatCollector.translateToLocal(s));
        id = i;
    }

    public GuiNpcButton(int i, int j, int k, String[] display, int val) {
        this(i, j, k, display[val]);
        this.display = display;
        this.displayValue = val;

        if (display.length > 1)
            rightClickable = true;
    }

    public GuiNpcButton(int i, int j, int k, int l, int m, String string) {
        super(i, j, k, l, m, StatCollector.translateToLocal(string));
        id = i;
    }

    public GuiNpcButton(int i, int j, int k, int l, int m, String[] display, int val) {
        this(i, j, k, l, m, display.length == 0 ? "" : display[val % display.length]);
        this.display = display;
        this.displayValue = display.length == 0 ? 0 : val % display.length;

        if (display.length > 1)
            rightClickable = true;
    }

    public GuiNpcButton(int i, int j, int k, int l, int m, Enum<?>[] displayEnums, int val) {
        this(i, j, k, l, m, displayEnums.length == 0 ? "" : displayEnums[val % displayEnums.length].toString());
        ArrayList<String> strings = new ArrayList<>();
        for (Enum<?> e : displayEnums) {
            strings.add(e.toString());
        }
        this.display = strings.toArray(new String[0]);
        this.displayValue = display.length == 0 ? 0 : val % display.length;

        if (display.length > 1)
            rightClickable = true;
    }

    public void setDisplayText(String text) {
        this.displayString = StatCollector.translateToLocal(text);
    }

    public void setHoverText(String text) {
        String translated = StatCollector.translateToLocal(text);
        this.hoverableText = translated == null ? "" : translated.replace("\\n", "\n");
    }

    public int getValue() {
        return displayValue;
    }

    public void setEnabled(boolean bo) {
        this.enabled = bo;
    }

    public void setVisible(boolean b) {
        this.visible = b;
    }

    public boolean getVisible() {
        return visible;
    }

    public void setDisplay(int value) {
        this.displayValue = value;
        this.setDisplayText(display[value]);
    }

    public void setTextColor(int color) {
        this.packedFGColour = color;
    }

    /**
     * Sets the icon texture for this button.
     * The texture should be 16x16 by default, unless you adjust the icon dimensions.
     */
    public GuiNpcButton setIconTexture(ResourceLocation texture) {
        this.iconTexture = texture;
        return this;
    }

    public GuiNpcButton setIconPos(int width, int height, int x, int y) {
        this.iconWidth = width;
        this.iconHeight = height;
        this.iconPosX = x;
        this.iconPosY = y;
        return this;
    }

    public AtomicBoolean rightClicked = new AtomicBoolean();

    @Override
    public boolean mousePressed(Minecraft minecraft, int i, int j) {
        boolean bo = super.mousePressed(minecraft, i, j);
        if (bo && display != null && display.length != 0) {
            if (rightClicked.get()) {
                if (displayValue <= 0)
                    displayValue = display.length;
                displayValue--;
            } else
                displayValue = (displayValue + 1) % display.length;
            this.setDisplayText(display[displayValue]);
        }
        return bo;
    }

    public int getWidth() {
        return width;
    }

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible)
            return;
        // Auto-truncate display text if wider than button
        String original = this.displayString;
        FontRenderer fr = mc.fontRenderer;
        if (fr != null) {
            int maxTextWidth = this.width - 6;
            if (fr.getStringWidth(original) > maxTextWidth) {
                String ellipsis = "...";
                int targetWidth = maxTextWidth - fr.getStringWidth(ellipsis);
                if (targetWidth > 0) {
                    this.displayString = fr.trimStringToWidth(original, targetWidth) + ellipsis;
                }
            }
        }
        // Wide buttons (>396px) break the vanilla 200px button texture.
        // Draw them with a three-patch approach instead.
        if (this.width > 396) {
            drawWideButton(mc, mouseX, mouseY);
        } else {
            super.drawButton(mc, mouseX, mouseY);
        }
        // Restore original display string
        this.displayString = original;
        // Then, if an icon texture is set, draw it.
        if (iconTexture != null) {
            mc.getTextureManager().bindTexture(iconTexture);
            GL11.glPushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            int iconX = this.xPosition + (this.width - iconWidth) / 2;
            int iconY = this.yPosition + (this.height - iconHeight) / 2;
            this.drawTexturedModalRect(iconX, iconY, iconPosX, iconPosY, iconWidth, iconHeight);
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
        }
    }

    /**
     * Draws a button wider than the vanilla 200px texture allows.
     * Uses three-patch rendering: left cap, tiled center, right cap.
     */
    private void drawWideButton(Minecraft mc, int mouseX, int mouseY) {
        FontRenderer fontrenderer = mc.fontRenderer;
        mc.getTextureManager().bindTexture(buttonTextures);
        GL11.glColor4f(1.0F, 1.0F, 1.0F, 1.0F);
        this.field_146123_n = mouseX >= this.xPosition && mouseY >= this.yPosition
            && mouseX < this.xPosition + this.width && mouseY < this.yPosition + this.height;
        int k = this.getHoverState(this.field_146123_n);
        GL11.glEnable(GL11.GL_BLEND);
        OpenGlHelper.glBlendFunc(770, 771, 1, 0);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);

        int texY = 46 + k * 20;
        int cap = 100;

        // Left cap (first 100px of texture)
        this.drawTexturedModalRect(this.xPosition, this.yPosition, 0, texY, cap, this.height);
        // Right cap (last 100px of texture)
        this.drawTexturedModalRect(this.xPosition + this.width - cap, this.yPosition, 200 - cap, texY, cap, this.height);
        // Tile the center column to fill the gap
        int gapStart = this.xPosition + cap;
        int gapEnd = this.xPosition + this.width - cap;
        for (int x = gapStart; x < gapEnd; x += 2) {
            int drawW = Math.min(2, gapEnd - x);
            this.drawTexturedModalRect(x, this.yPosition, 99, texY, drawW, this.height);
        }

        this.mouseDragged(mc, mouseX, mouseY);

        int l = 14737632;
        if (packedFGColour != 0) {
            l = packedFGColour;
        } else if (!this.enabled) {
            l = 10526880;
        } else if (this.field_146123_n) {
            l = 16777120;
        }
        this.drawCenteredString(fontrenderer, this.displayString,
            this.xPosition + this.width / 2, this.yPosition + (this.height - 8) / 2, l);
    }

    public void drawHover(int i, int j, boolean hasSubGui) {
        if (hasSubGui || !visible || hoverableText.isEmpty())
            return;
        int hoverState = this.getHoverState(this.field_146123_n);
        if (oldHover != hoverState) {
            oldHover = hoverState;
            hoverCount = 0;
        } else {
            if (hoverCount < 65)
                hoverCount++;
        }
        if (hoverState == 2 && hoverCount > 60) {
            GL11.glPushMatrix();
            Minecraft mc = Minecraft.getMinecraft();
            String displayString = StatCollector.translateToLocal(hoverableText);
            GL11.glColor4f(1, 1, 1, 1);
            List<String> lines = splitHoverText(displayString, 30);
            drawHoveringText(lines, i, j, mc);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
    }

    private List<String> splitHoverText(String text, int maxLineLength) {
        List<String> lines = new ArrayList<String>();
        if (text == null || text.isEmpty())
            return lines;

        String normalized = text.replace("\\n", "\n");
        String[] explicitLines = normalized.split("\\r?\\n", -1);
        for (String line : explicitLines) {
            if (line.isEmpty()) {
                lines.add("");
                continue;
            }
            lines.addAll(TextSplitter.splitText(line, maxLineLength));
        }
        return lines;
    }

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

        // Get scaled screen dimensions
        ScaledResolution scaledRes = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int screenWidth = scaledRes.getScaledWidth();
        int screenHeight = scaledRes.getScaledHeight();

        // If tooltip would go off right edge, draw on left side of mouse
        if (j2 + maxWidth + 6 > screenWidth) {
            j2 = x - maxWidth - 16;
        }

        // Keep tooltip on screen horizontally
        if (j2 < 4) {
            j2 = 4;
        }

        // Keep tooltip on screen vertically
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

    @Override
    public int getHoverState(boolean mouseOver) {
        byte b0 = 1;
        if (!this.enabled) {
            b0 = 0;
        } else if (mouseOver && !this.hasSubGUI) {
            b0 = 2;
        }
        return b0;
    }

    public void updateSubGUI(boolean hasSubGUI) {
        this.hasSubGUI = hasSubGUI;
    }

    public boolean getHasSubGUI() {
        return this.hasSubGUI;
    }
}
