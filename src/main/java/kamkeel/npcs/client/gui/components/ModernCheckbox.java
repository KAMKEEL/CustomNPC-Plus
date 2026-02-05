package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * Modern styled checkbox component.
 * Square box: hollow border when OFF, filled green when ON.
 * Single click to toggle.
 */
public class ModernCheckbox extends Gui {

    // Identity
    protected int id;
    protected String label;

    // State
    protected boolean value = false;
    protected boolean enabled = true;
    protected boolean hovered = false;

    // Position/Size
    protected int x, y;
    protected int size = 14;
    protected int labelGap = 6;

    // Colors
    protected int borderColor = ModernColors.INPUT_BORDER;
    protected int borderColorHover = ModernColors.INPUT_BORDER_FOCUSED;
    protected int fillOff = ModernColors.INPUT_BG;
    protected int fillOn = ModernColors.ACCENT_GREEN;
    protected int labelColor = ModernColors.TEXT_LIGHT;
    protected int labelColorDisabled = ModernColors.TEXT_DARK;

    public ModernCheckbox(int id, int x, int y, String label, boolean value) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.label = label;
        this.value = value;
    }

    public ModernCheckbox(int id, int x, int y, boolean value) {
        this(id, x, y, null, value);
    }

    // === Drawing ===

    public void draw(int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        // Check hover on checkbox
        hovered = enabled && mouseX >= x && mouseX < x + size &&
                  mouseY >= y && mouseY < y + size;

        int border = !enabled ? ModernColors.INPUT_BORDER :
                    (hovered ? borderColorHover : borderColor);

        // Draw border (hollow box outline)
        drawRect(x, y, x + size, y + 1, border);                    // top
        drawRect(x, y + size - 1, x + size, y + size, border);      // bottom
        drawRect(x, y, x + 1, y + size, border);                    // left
        drawRect(x + size - 1, y, x + size, y + size, border);      // right

        // Fill interior
        if (value) {
            // Filled green when ON
            int fill = enabled ? fillOn : ModernColors.darken(fillOn, 0.4f);
            drawRect(x + 2, y + 2, x + size - 2, y + size - 2, fill);
        } else {
            // Dark background when OFF
            int fill = enabled ? fillOff : ModernColors.INPUT_BG_DISABLED;
            drawRect(x + 1, y + 1, x + size - 1, y + size - 1, fill);
        }

        // Draw label to the right
        if (label != null && !label.isEmpty()) {
            int labelY = y + (size - fr.FONT_HEIGHT) / 2;
            int labelX = x + size + labelGap;
            fr.drawString(label, labelX, labelY, enabled ? labelColor : labelColorDisabled);
        }
    }

    // === Interaction ===

    /**
     * Handle mouse click. Returns true if handled.
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || !enabled) return false;

        // Check if checkbox was clicked
        if (mouseX >= x && mouseX < x + size &&
            mouseY >= y && mouseY < y + size) {
            toggle();
            return true;
        }

        return false;
    }

    /**
     * Toggle the value.
     */
    public void toggle() {
        value = !value;
    }

    // === Getters/Setters ===

    public int getId() { return id; }
    public boolean getValue() { return value; }

    public void setValue(boolean value) {
        this.value = value;
    }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    public String getLabel() { return label; }

    public void setLabel(String label) {
        this.label = label;
    }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getSize() { return size; }

    public ModernCheckbox setSize(int size) {
        this.size = size;
        return this;
    }

    /**
     * Get total width including label.
     */
    public int getTotalWidth(FontRenderer fr) {
        if (label == null || label.isEmpty()) {
            return size;
        }
        return size + labelGap + fr.getStringWidth(label);
    }

    public ModernCheckbox setColors(int borderColor, int fillOn) {
        this.borderColor = borderColor;
        this.fillOn = fillOn;
        return this;
    }
}
