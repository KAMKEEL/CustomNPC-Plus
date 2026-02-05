package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * Modern toggle switch component.
 * A sliding on/off switch with animation.
 */
public class ModernToggle extends Gui {

    // Identity
    protected int id;
    protected String label;

    // State
    protected boolean value = false;
    protected boolean enabled = true;
    protected boolean hovered = false;

    // Position
    protected int x, y;
    protected int toggleWidth = 36;
    protected int toggleHeight = 16;
    protected int labelGap = 6;

    // Animation
    protected float animProgress = 0; // 0 = off, 1 = on
    protected float animSpeed = 0.25f;

    // Colors
    protected int trackOff = ModernColors.TOGGLE_OFF;
    protected int trackOn = ModernColors.TOGGLE_ON;
    protected int knobColor = ModernColors.TOGGLE_KNOB;
    protected int labelColor = ModernColors.TEXT_LIGHT;
    protected int labelColorDisabled = ModernColors.TEXT_DARK;

    public ModernToggle(int id, int x, int y, String label, boolean value) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.label = label;
        this.value = value;
        this.animProgress = value ? 1.0f : 0.0f;
    }

    // === Drawing ===

    public void draw(int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        // Update animation
        float target = value ? 1.0f : 0.0f;
        if (Math.abs(animProgress - target) > 0.01f) {
            animProgress += (target - animProgress) * animSpeed;
        } else {
            animProgress = target;
        }

        // Check hover on toggle
        hovered = enabled && mouseX >= x && mouseX < x + toggleWidth &&
                  mouseY >= y && mouseY < y + toggleHeight;

        // Draw label first (to the left)
        if (label != null && !label.isEmpty()) {
            int labelY = y + (toggleHeight - fr.FONT_HEIGHT) / 2;
            int labelX = x - labelGap - fr.getStringWidth(label);
            fr.drawString(label, labelX, labelY, enabled ? labelColor : labelColorDisabled);
        }

        // Interpolate track color
        int trackColor = ModernColors.blend(trackOff, trackOn, animProgress);
        if (!enabled) {
            trackColor = ModernColors.darken(trackColor, 0.4f);
        }

        // Draw track (rounded rectangle approximation)
        int radius = toggleHeight / 2;
        drawRect(x + radius, y, x + toggleWidth - radius, y + toggleHeight, trackColor);
        drawCircle(x + radius, y + radius, radius, trackColor);
        drawCircle(x + toggleWidth - radius, y + radius, radius, trackColor);

        // Calculate knob position
        int knobRadius = radius - 2;
        int knobMinX = x + radius;
        int knobMaxX = x + toggleWidth - radius;
        int knobX = knobMinX + (int)((knobMaxX - knobMinX) * animProgress);

        // Draw knob
        int knob = enabled ? knobColor : ModernColors.darken(knobColor, 0.3f);
        if (hovered) {
            knob = ModernColors.lighten(knob, 0.1f);
        }
        drawCircle(knobX, y + radius, knobRadius, knob);
    }

    /**
     * Draw a filled circle (approximation using rectangles).
     */
    protected void drawCircle(int cx, int cy, int radius, int color) {
        // Simple approximation - draw a series of horizontal lines
        for (int dy = -radius; dy <= radius; dy++) {
            int dx = (int) Math.sqrt(radius * radius - dy * dy);
            drawRect(cx - dx, cy + dy, cx + dx, cy + dy + 1, color);
        }
    }

    // === Interaction ===

    /**
     * Handle mouse click. Returns true if handled.
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0 || !enabled) return false;

        // Check if toggle was clicked
        if (mouseX >= x && mouseX < x + toggleWidth &&
            mouseY >= y && mouseY < y + toggleHeight) {
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
        // Don't reset animation - let it animate to new value
    }

    public void setValueImmediate(boolean value) {
        this.value = value;
        this.animProgress = value ? 1.0f : 0.0f;
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
    public int getWidth() { return toggleWidth; }
    public int getHeight() { return toggleHeight; }

    /**
     * Get total width including label.
     */
    public int getTotalWidth(FontRenderer fr) {
        if (label == null || label.isEmpty()) {
            return toggleWidth;
        }
        return toggleWidth + labelGap + fr.getStringWidth(label);
    }

    public ModernToggle setColors(int trackOff, int trackOn, int knob) {
        this.trackOff = trackOff;
        this.trackOn = trackOn;
        this.knobColor = knob;
        return this;
    }

    public ModernToggle setSize(int width, int height) {
        this.toggleWidth = width;
        this.toggleHeight = height;
        return this;
    }
}
