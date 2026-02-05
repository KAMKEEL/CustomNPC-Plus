package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * A button that displays a color swatch and hex value.
 * Clicking opens a color picker (handled by parent).
 */
public class ModernColorButton extends Gui {

    // Identity
    protected int id;

    // Position/Size
    protected int x, y, width, height;

    // Color
    protected int color = 0xFFFFFF;

    // State
    protected boolean enabled = true;
    protected boolean hovered = false;

    // Colors
    protected int borderColor = ModernColors.INPUT_BORDER;
    protected int borderColorHover = ModernColors.INPUT_BORDER_FOCUSED;
    protected int disabledOverlay = 0x80000000;

    public ModernColorButton(int id, int x, int y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ModernColorButton(int id, int x, int y, int width, int height, int color) {
        this(id, x, y, width, height);
        this.color = color;
    }

    // === Drawing ===

    public void draw(int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        hovered = enabled && isInside(mouseX, mouseY);

        // Draw border
        int border = hovered ? borderColorHover : borderColor;
        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, border);

        // Draw color swatch
        drawRect(x, y, x + width, y + height, 0xFF000000 | color);

        // Draw disabled overlay
        if (!enabled) {
            drawRect(x, y, x + width, y + height, disabledOverlay);
        }

        // Draw hex text
        String hex = String.format("%06X", color & 0xFFFFFF);
        int textColor = getContrastColor(color);
        int textX = x + (width - fr.getStringWidth(hex)) / 2;
        int textY = y + (height - fr.FONT_HEIGHT) / 2;
        fr.drawString(hex, textX, textY, textColor);
    }

    /**
     * Get a contrasting text color (black or white) based on background brightness.
     */
    protected int getContrastColor(int bgColor) {
        int r = (bgColor >> 16) & 0xFF;
        int g = (bgColor >> 8) & 0xFF;
        int b = bgColor & 0xFF;
        // Calculate relative luminance
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        return luminance > 0.5 ? 0x000000 : 0xFFFFFF;
    }

    // === Interaction ===

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || !enabled) return false;
        return isInside(mouseX, mouseY);
    }

    public boolean isInside(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width &&
               mouseY >= y && mouseY < y + height;
    }

    // === Getters/Setters ===

    public int getId() { return id; }

    public int getColor() { return color; }

    public void setColor(int color) {
        this.color = color & 0xFFFFFF;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public boolean isHovered() { return hovered; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
