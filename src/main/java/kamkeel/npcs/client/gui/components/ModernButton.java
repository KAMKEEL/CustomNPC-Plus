package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import noppes.npcs.client.gui.util.GuiNpcButton;

/**
 * Modern styled button extending GuiNpcButton.
 * Flat design matching the GuiModernScreen aesthetic with modern colors.
 * Inherits hover text, icon support, and multi-value cycling from GuiNpcButton.
 */
public class ModernButton extends GuiNpcButton {

    // Styling (can be customized per-button)
    protected int bgColor = ModernColors.BUTTON_BG;
    protected int bgColorHover = ModernColors.BUTTON_BG_HOVER;
    protected int bgColorPressed = ModernColors.BUTTON_BG_PRESSED;
    protected int bgColorDisabled = ModernColors.BUTTON_BG_DISABLED;
    protected int borderColor = ModernColors.BUTTON_BORDER;
    protected int borderColorHover = ModernColors.BUTTON_BORDER_HOVER;
    protected int textColor = ModernColors.TEXT_WHITE;
    protected int textColorDisabled = ModernColors.TEXT_DARK;

    // Layout
    protected int paddingX = 6;
    protected int paddingY = 2;
    protected int minWidth = 20;
    protected int minHeight = 16;

    // State
    protected boolean pressed = false;
    protected boolean hovered = false;

    // Color button mode
    protected boolean colorMode = false;
    protected int displayColor = 0xFFFFFF;

    // Multi-value support (like GuiNpcButton)
    protected String[] values;
    protected int valueIndex = 0;

    /**
     * Create a simple button with text.
     */
    public ModernButton(int id, int x, int y, int width, int height, String text) {
        super(id, x, y, width, height, text);
    }

    /**
     * Create a button with minimum size based on text.
     */
    public ModernButton(int id, int x, int y, String text) {
        super(id, x, y, 60, 20, text);
        fitToText(Minecraft.getMinecraft().fontRenderer);
    }

    /**
     * Create a multi-value button (uses parent's display array).
     */
    public ModernButton(int id, int x, int y, int width, int height, String[] values, int startIndex) {
        super(id, x, y, width, height, values, startIndex);
        // Also store in local values for modern methods
        this.values = values;
        this.valueIndex = startIndex;
    }

    // === Size Methods ===

    /**
     * Set button size.
     */
    public ModernButton setSize(int w, int h) {
        this.width = Math.max(minWidth, w);
        this.height = Math.max(minHeight, h);
        return this;
    }

    /**
     * Set button bounds (position and size).
     */
    public ModernButton setBounds(int x, int y, int w, int h) {
        this.xPosition = x;
        this.yPosition = y;
        return setSize(w, h);
    }

    /**
     * Auto-size button based on text width.
     */
    public ModernButton fitToText(FontRenderer fr) {
        if (fr == null) fr = Minecraft.getMinecraft().fontRenderer;
        int textWidth = fr.getStringWidth(displayString);
        this.width = Math.max(minWidth, textWidth + paddingX * 2);
        this.height = Math.max(minHeight, fr.FONT_HEIGHT + paddingY * 2);
        return this;
    }

    /**
     * Set minimum dimensions.
     */
    public ModernButton setMinSize(int minW, int minH) {
        this.minWidth = minW;
        this.minHeight = minH;
        // Enforce minimums on current size
        if (width < minWidth) width = minWidth;
        if (height < minHeight) height = minHeight;
        return this;
    }

    // === Style Methods ===

    /**
     * Set background color for normal state.
     */
    public ModernButton setBackgroundColor(int color) {
        this.bgColor = color;
        return this;
    }

    /**
     * Set all background colors at once.
     */
    public ModernButton setBackgroundColors(int normal, int hover, int pressed, int disabled) {
        this.bgColor = normal;
        this.bgColorHover = hover;
        this.bgColorPressed = pressed;
        this.bgColorDisabled = disabled;
        return this;
    }

    /**
     * Set border color.
     */
    public ModernButton setBorderColor(int color) {
        this.borderColor = color;
        this.borderColorHover = ModernColors.lighten(color, 0.2f);
        return this;
    }

    /**
     * Set text colors.
     */
    public ModernButton setTextColors(int normal, int disabled) {
        this.textColor = normal;
        this.textColorDisabled = disabled;
        return this;
    }

    /**
     * Set padding around text.
     */
    public ModernButton setPadding(int px, int py) {
        this.paddingX = px;
        this.paddingY = py;
        return this;
    }

    // === Multi-Value Support (delegates to parent where appropriate) ===

    /**
     * Get current value index (for multi-value buttons).
     * Uses parent's getValue() for consistency.
     */
    @Override
    public int getValue() {
        return super.getValue();
    }

    /**
     * Set current value index.
     */
    public ModernButton setValue(int index) {
        setDisplay(index);
        if (values != null && index >= 0 && index < values.length) {
            this.valueIndex = index;
        }
        return this;
    }

    /**
     * Cycle to next value.
     */
    public void nextValue() {
        if (values != null && values.length > 0) {
            valueIndex = (valueIndex + 1) % values.length;
            setDisplay(valueIndex);
        }
    }

    /**
     * Cycle to previous value.
     */
    public void prevValue() {
        if (values != null && values.length > 0) {
            valueIndex = (valueIndex - 1 + values.length) % values.length;
            setDisplay(valueIndex);
        }
    }

    // === Color Button Mode ===

    public ModernButton setColorMode(int color) {
        this.colorMode = true;
        this.displayColor = color & 0xFFFFFF;
        return this;
    }

    public void setColor(int color) {
        this.displayColor = color & 0xFFFFFF;
    }

    public int getColor() {
        return displayColor;
    }

    protected int getContrastColor(int bgColor) {
        int r = (bgColor >> 16) & 0xFF;
        int g = (bgColor >> 8) & 0xFF;
        int b = bgColor & 0xFF;
        double luminance = (0.299 * r + 0.587 * g + 0.114 * b) / 255.0;
        return luminance > 0.5 ? 0x000000 : 0xFFFFFF;
    }

    // === Rendering ===

    @Override
    public void drawButton(Minecraft mc, int mouseX, int mouseY) {
        if (!this.visible) return;

        FontRenderer fr = mc.fontRenderer;
        hovered = mouseX >= xPosition && mouseY >= yPosition &&
                  mouseX < xPosition + width && mouseY < yPosition + height;

        if (colorMode) {
            drawColorButton(fr, mouseX, mouseY);
            return;
        }

        // Determine colors based on state
        int bg, border, text;
        if (!enabled) {
            bg = bgColorDisabled;
            border = borderColor;
            text = textColorDisabled;
        } else if (pressed && hovered) {
            bg = bgColorPressed;
            border = borderColorHover;
            text = textColor;
        } else if (hovered) {
            bg = bgColorHover;
            border = borderColorHover;
            text = textColor;
        } else {
            bg = bgColor;
            border = borderColor;
            text = textColor;
        }

        // Draw background
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, bg);

        // Draw border (1px)
        drawHorizontalLine(xPosition, xPosition + width - 1, yPosition, border);
        drawHorizontalLine(xPosition, xPosition + width - 1, yPosition + height - 1, border);
        drawVerticalLine(xPosition, yPosition, yPosition + height - 1, border);
        drawVerticalLine(xPosition + width - 1, yPosition, yPosition + height - 1, border);

        // Draw text centered
        String displayText = displayString;
        int maxTextWidth = width - paddingX * 2;
        if (fr.getStringWidth(displayText) > maxTextWidth) {
            // Truncate with ellipsis
            displayText = trimToWidth(fr, displayText, maxTextWidth - fr.getStringWidth("..")) + "..";
        }

        int textX = xPosition + (width - fr.getStringWidth(displayText)) / 2;
        int textY = yPosition + (height - fr.FONT_HEIGHT) / 2;

        // Draw shadow first for better readability
        fr.drawString(displayText, textX + 1, textY + 1, ModernColors.darken(text, 0.6f));
        fr.drawString(displayText, textX, textY, text);
    }

    /**
     * Draw horizontal line (like Gui.drawHorizontalLine but public).
     */
    protected void drawHorizontalLine(int x1, int x2, int y, int color) {
        if (x2 < x1) {
            int temp = x1;
            x1 = x2;
            x2 = temp;
        }
        drawRect(x1, y, x2 + 1, y + 1, color);
    }

    /**
     * Draw vertical line.
     */
    protected void drawVerticalLine(int x, int y1, int y2, int color) {
        if (y2 < y1) {
            int temp = y1;
            y1 = y2;
            y2 = temp;
        }
        drawRect(x, y1, x + 1, y2 + 1, color);
    }

    /**
     * Trim text to fit within width.
     */
    protected String trimToWidth(FontRenderer fr, String text, int maxWidth) {
        if (fr.getStringWidth(text) <= maxWidth) return text;
        StringBuilder sb = new StringBuilder();
        for (char c : text.toCharArray()) {
            if (fr.getStringWidth(sb.toString() + c) > maxWidth) break;
            sb.append(c);
        }
        return sb.toString();
    }

    protected void drawColorButton(FontRenderer fr, int mouseX, int mouseY) {
        int border = hovered ? ModernColors.INPUT_BORDER_FOCUSED : ModernColors.INPUT_BORDER;
        drawRect(xPosition - 1, yPosition - 1, xPosition + width + 1, yPosition + height + 1, border);
        drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0xFF000000 | displayColor);
        if (!enabled) {
            drawRect(xPosition, yPosition, xPosition + width, yPosition + height, 0x80000000);
        }
        String hex = String.format("%06X", displayColor & 0xFFFFFF);
        int textColor = getContrastColor(displayColor);
        int textX = xPosition + (width - fr.getStringWidth(hex)) / 2;
        int textY = yPosition + (height - fr.FONT_HEIGHT) / 2;
        fr.drawString(hex, textX, textY, textColor);
    }

    // === Mouse Handling ===

    @Override
    public boolean mousePressed(Minecraft mc, int mouseX, int mouseY) {
        boolean result = super.mousePressed(mc, mouseX, mouseY);
        if (result) {
            pressed = true;
            // Cycle value for multi-value buttons
            if (values != null) {
                nextValue();
            }
        }
        return result;
    }

    @Override
    public void mouseReleased(int mouseX, int mouseY) {
        pressed = false;
        super.mouseReleased(mouseX, mouseY);
    }

    /**
     * Handle right-click for reverse cycling (multi-value buttons).
     */
    public boolean mouseRightPressed(Minecraft mc, int mouseX, int mouseY) {
        if (enabled && visible && hovered && values != null) {
            prevValue();
            return true;
        }
        return false;
    }

    // === State Accessors ===

    public boolean isHovered() {
        return hovered;
    }

    public boolean isPressed() {
        return pressed;
    }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }
}
