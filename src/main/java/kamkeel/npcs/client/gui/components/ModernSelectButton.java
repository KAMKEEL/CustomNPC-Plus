package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * A button that displays selected item text and triggers selection on click.
 * The parent handles opening the actual selection GUI.
 */
public class ModernSelectButton extends Gui {

    // Identity
    protected int id;
    protected int slot = 0; // For multi-slot selections

    // Position/Size
    protected int x, y, width, height;

    // Display
    protected String displayText = "";
    protected String placeholder = "Select...";
    protected int selectedId = -1;

    // State
    protected boolean enabled = true;
    protected boolean hovered = false;

    // Colors
    protected int bgColor = ModernColors.BUTTON_BG;
    protected int bgColorHover = ModernColors.BUTTON_BG_HOVER;
    protected int bgColorDisabled = ModernColors.BUTTON_BG_DISABLED;
    protected int borderColor = ModernColors.BUTTON_BORDER;
    protected int borderColorHover = ModernColors.BUTTON_BORDER_HOVER;
    protected int textColor = ModernColors.TEXT_LIGHT;
    protected int textColorDisabled = ModernColors.TEXT_DARK;
    protected int placeholderColor = ModernColors.TEXT_DARK;

    // Padding
    protected int paddingX = 4;

    public ModernSelectButton(int id, int x, int y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public ModernSelectButton(int id, int x, int y, int width, int height, String placeholder) {
        this(id, x, y, width, height);
        this.placeholder = placeholder;
    }

    // === Drawing ===

    public void draw(int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        hovered = enabled && isInside(mouseX, mouseY);

        // Background
        int bg = !enabled ? bgColorDisabled : (hovered ? bgColorHover : bgColor);
        int border = hovered ? borderColorHover : borderColor;

        // Draw border
        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, border);
        // Draw background
        drawRect(x, y, x + width, y + height, bg);

        // Draw text
        String text = (displayText != null && !displayText.isEmpty()) ? displayText : placeholder;
        text = fr.trimStringToWidth(text, width - paddingX * 2);
        int textY = y + (height - fr.FONT_HEIGHT) / 2;

        int textCol;
        if (!enabled) {
            textCol = textColorDisabled;
        } else if (displayText != null && !displayText.isEmpty()) {
            textCol = textColor;
        } else {
            textCol = placeholderColor;
        }

        fr.drawString(text, x + paddingX, textY, textCol);
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

    // === Selection Management ===

    public void setSelected(int id, String name) {
        this.selectedId = id;
        this.displayText = name;
    }

    public void clearSelection() {
        this.selectedId = -1;
        this.displayText = "";
    }

    public boolean hasSelection() {
        return selectedId >= 0;
    }

    // === Getters/Setters ===

    public int getId() { return id; }

    public int getSlot() { return slot; }

    public void setSlot(int slot) { this.slot = slot; }

    public int getSelectedId() { return selectedId; }

    public String getDisplayText() { return displayText; }

    public void setDisplayText(String text) {
        this.displayText = text;
    }

    public String getPlaceholder() { return placeholder; }

    public void setPlaceholder(String placeholder) {
        this.placeholder = placeholder;
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
