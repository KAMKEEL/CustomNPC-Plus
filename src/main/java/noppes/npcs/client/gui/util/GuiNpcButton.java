package noppes.npcs.client.gui.util;

import kamkeel.npcs.util.TextSplitter;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiButton;
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
        this.hoverableText = StatCollector.translateToLocal(text);
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
        // First, draw the button normally using the superclass method.
        super.drawButton(mc, mouseX, mouseY);
        // Then, if an icon texture is set, draw it.
        if (iconTexture != null) {
            mc.getTextureManager().bindTexture(iconTexture);
            GL11.glPushMatrix();
            RenderHelper.enableGUIStandardItemLighting();
            // Calculate the icon position so that it's centered in the button.
            // (Assumes the button's (xPosition,yPosition) is its top left.)
            int iconX = this.xPosition + (this.width - iconWidth) / 2;
            int iconY = this.yPosition + (this.height - iconHeight) / 2;
            // Draw the texture; we assume texture coordinates start at (0,0) with the desired size.
            this.drawTexturedModalRect(iconX, iconY, iconPosX, iconPosY, iconWidth, iconHeight);
            RenderHelper.disableStandardItemLighting();
            GL11.glPopMatrix();
        }
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
            List<String> lines = TextSplitter.splitText(displayString, 30);
            drawHoveringText(lines, i, j, mc);
            GL11.glDisable(GL11.GL_LIGHTING);
            GL11.glPopMatrix();
        }
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

        int gameWidth = mc.displayWidth;
        int maxTooltipX = gameWidth - maxWidth - 4;

        if (j2 > maxTooltipX) {
            int diff = j2 - maxTooltipX;
            j2 -= diff;
            GL11.glTranslatef(-300, 0, 0);
        }

        if (k2 + maxHeight + 6 > mc.displayHeight) {
            k2 = mc.displayHeight - maxHeight - 6;
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
