package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * Modern styled label component.
 * Supports single or multi-line text, alignment, and optional background.
 */
public class ModernLabel extends Gui {

    // Identity
    protected int id;
    protected String text;

    // Position/Size
    protected int x, y;
    protected int width = 0; // 0 = auto-width based on text
    protected int height = 0;

    // Alignment
    public static final int ALIGN_LEFT = 0;
    public static final int ALIGN_CENTER = 1;
    public static final int ALIGN_RIGHT = 2;
    protected int alignment = ALIGN_LEFT;

    // Styling
    protected int textColor = ModernColors.TEXT_LIGHT;
    protected int bgColor = 0; // 0 = no background
    protected int padding = 2;
    protected boolean shadow = false;

    public ModernLabel(int id, int x, int y, String text) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.text = text;
    }

    public ModernLabel(int id, int x, int y, int width, String text) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.text = text;
    }

    // === Drawing ===

    public void draw(int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        int textWidth = fr.getStringWidth(text);
        int actualWidth = width > 0 ? width : textWidth + padding * 2;
        int actualHeight = height > 0 ? height : fr.FONT_HEIGHT + padding * 2;

        // Draw background if set
        if (bgColor != 0) {
            drawRect(x, y, x + actualWidth, y + actualHeight, bgColor);
        }

        // Calculate text position based on alignment
        int textX;
        switch (alignment) {
            case ALIGN_CENTER:
                textX = x + (actualWidth - textWidth) / 2;
                break;
            case ALIGN_RIGHT:
                textX = x + actualWidth - textWidth - padding;
                break;
            default: // ALIGN_LEFT
                textX = x + padding;
        }

        int textY = y + (actualHeight - fr.FONT_HEIGHT) / 2;

        // Draw text
        if (shadow) {
            fr.drawStringWithShadow(text, textX, textY, textColor);
        } else {
            fr.drawString(text, textX, textY, textColor);
        }
    }

    // === Getters/Setters ===

    public int getId() { return id; }
    public String getText() { return text; }

    public void setText(String text) {
        this.text = text;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }

    public int getWidth() {
        if (width > 0) return width;
        return Minecraft.getMinecraft().fontRenderer.getStringWidth(text) + padding * 2;
    }

    public int getHeight() {
        if (height > 0) return height;
        return Minecraft.getMinecraft().fontRenderer.FONT_HEIGHT + padding * 2;
    }

    public ModernLabel setSize(int width, int height) {
        this.width = width;
        this.height = height;
        return this;
    }

    public ModernLabel setAlignment(int align) {
        this.alignment = align;
        return this;
    }

    public ModernLabel setColor(int color) {
        this.textColor = color;
        return this;
    }

    public ModernLabel setBackground(int color) {
        this.bgColor = color;
        return this;
    }

    public ModernLabel setShadow(boolean shadow) {
        this.shadow = shadow;
        return this;
    }

    public ModernLabel setPadding(int padding) {
        this.padding = padding;
        return this;
    }
}
