package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.ModernColors;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.ScaledResolution;
import org.lwjgl.opengl.GL11;

import java.util.ArrayList;
import java.util.List;

/**
 * Modern styled dropdown/select component.
 * Allows selection from a list of options.
 */
public class ModernDropdown extends Gui {

    // Static: track the currently expanded dropdown to ensure only one is open
    protected static ModernDropdown activeDropdown = null;

    // Identity
    protected int id;

    // Position/Size
    protected int x, y, width, height;

    // Options
    protected List<String> options = new ArrayList<>();
    protected int selectedIndex = 0;

    // State
    protected boolean expanded = false;
    protected boolean enabled = true;
    protected boolean hovered = false;
    protected int hoveredOption = -1;

    // Colors
    protected int bgColor = ModernColors.INPUT_BG;
    protected int bgColorHover = ModernColors.INPUT_BG_FOCUSED;
    protected int borderColor = ModernColors.INPUT_BORDER;
    protected int borderColorFocused = ModernColors.INPUT_BORDER_FOCUSED;
    protected int textColor = ModernColors.TEXT_LIGHT;
    protected int textColorDisabled = ModernColors.TEXT_DARK;
    protected int dropdownBg = ModernColors.PANEL_BG_SOLID;
    protected int optionHover = ModernColors.SELECTION_BG;
    protected int arrowColor = ModernColors.TEXT_GRAY;

    // Layout
    protected int paddingX = 4;
    protected int maxDropdownHeight = 120;

    // Screen-space position for overlay rendering (to handle scroll offsets)
    protected int screenX, screenY;

    public ModernDropdown(int id, int x, int y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // === Drawing ===

    /**
     * Draw the dropdown. Use drawBase() + drawOverlay() for z-order control.
     */
    public void draw(int mouseX, int mouseY) {
        drawBase(mouseX, mouseY);
        if (expanded) {
            drawOverlay(mouseX, mouseY);
        }
    }

    /**
     * Draw only the base button (not the expanded dropdown).
     * Call this first, then call drawOverlay() later for proper z-order.
     */
    public void drawBase(int mouseX, int mouseY) {
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        // Check hover state
        hovered = enabled && isInsideMain(mouseX, mouseY);

        // Draw main button
        int bg = !enabled ? ModernColors.INPUT_BG_DISABLED : (hovered || expanded ? bgColorHover : bgColor);
        int border = expanded ? borderColorFocused : borderColor;

        drawRect(x - 1, y - 1, x + width + 1, y + height + 1, border);
        drawRect(x, y, x + width, y + height, bg);

        // Draw selected text
        String displayText = selectedIndex >= 0 && selectedIndex < options.size() ?
                            options.get(selectedIndex) : "";
        displayText = fr.trimStringToWidth(displayText, width - paddingX * 2 - 12);
        int textY = y + (height - fr.FONT_HEIGHT) / 2;
        fr.drawString(displayText, x + paddingX, textY, enabled ? textColor : textColorDisabled);

        // Draw arrow
        String arrow = expanded ? "\u25B2" : "\u25BC"; // ▲ or ▼
        int arrowX = x + width - paddingX - fr.getStringWidth(arrow);
        fr.drawString(arrow, arrowX, textY, arrowColor);
    }

    /**
     * Draw the expanded dropdown overlay. Call this AFTER all other components
     * to ensure it renders on top.
     */
    public void drawOverlay(int mouseX, int mouseY) {
        if (!expanded) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        drawDropdown(mouseX, mouseY, fr);
    }

    protected void drawDropdown(int mouseX, int mouseY, FontRenderer fr) {
        int optionHeight = fr.FONT_HEIGHT + 4;
        int dropdownHeight = Math.min(options.size() * optionHeight, maxDropdownHeight);
        int dropdownY = y + height;

        // Use scissor for scrolling if needed
        Minecraft mc = Minecraft.getMinecraft();
        ScaledResolution sr = new ScaledResolution(mc, mc.displayWidth, mc.displayHeight);
        int scale = sr.getScaleFactor();

        // Draw dropdown background with border
        drawRect(x - 1, dropdownY, x + width + 1, dropdownY + dropdownHeight + 1, borderColorFocused);
        drawRect(x, dropdownY, x + width, dropdownY + dropdownHeight, dropdownBg);

        GL11.glEnable(GL11.GL_SCISSOR_TEST);
        GL11.glScissor(x * scale, mc.displayHeight - (dropdownY + dropdownHeight) * scale,
                      width * scale, dropdownHeight * scale);

        // Draw options
        hoveredOption = -1;
        for (int i = 0; i < options.size(); i++) {
            int optY = dropdownY + i * optionHeight;

            // Check hover
            if (mouseX >= x && mouseX < x + width &&
                mouseY >= optY && mouseY < optY + optionHeight) {
                hoveredOption = i;
                drawRect(x, optY, x + width, optY + optionHeight, optionHover);
            }

            // Draw text
            String text = fr.trimStringToWidth(options.get(i), width - paddingX * 2);
            int textCol = (i == selectedIndex) ? ModernColors.ACCENT_BLUE : textColor;
            fr.drawString(text, x + paddingX, optY + 2, textCol);
        }

        GL11.glDisable(GL11.GL_SCISSOR_TEST);
    }

    // === Interaction ===

    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        if (button != 0) return false;

        if (!enabled) return false;

        if (expanded) {
            // Check if clicked on an option
            if (hoveredOption >= 0) {
                selectedIndex = hoveredOption;
                expanded = false;
                if (activeDropdown == this) activeDropdown = null;
                return true;
            }
            // Clicked outside - close
            expanded = false;
            if (activeDropdown == this) activeDropdown = null;
            return isInsideMain(mouseX, mouseY);
        } else {
            // Check if clicked on main button
            if (isInsideMain(mouseX, mouseY)) {
                // Close any other open dropdown first
                if (activeDropdown != null && activeDropdown != this) {
                    activeDropdown.close();
                }
                activeDropdown = this;
                expanded = true;
                return true;
            }
        }

        return false;
    }

    public boolean isInsideMain(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width &&
               mouseY >= y && mouseY < y + height;
    }

    public boolean isInsideDropdown(int mouseX, int mouseY) {
        if (!expanded) return false;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int optionHeight = fr.FONT_HEIGHT + 4;
        int dropdownHeight = Math.min(options.size() * optionHeight, maxDropdownHeight);
        int dropdownY = y + height;

        return mouseX >= x && mouseX < x + width &&
               mouseY >= dropdownY && mouseY < dropdownY + dropdownHeight;
    }

    public boolean isInside(int mouseX, int mouseY) {
        return isInsideMain(mouseX, mouseY) || isInsideDropdown(mouseX, mouseY);
    }

    public void close() {
        expanded = false;
        if (activeDropdown == this) activeDropdown = null;
    }

    /**
     * Close any currently active dropdown. Call this when clicking elsewhere
     * in the UI to dismiss open dropdowns.
     */
    public static void closeActiveDropdown() {
        if (activeDropdown != null) {
            activeDropdown.expanded = false;
            activeDropdown = null;
        }
    }

    /**
     * Check if any dropdown is currently expanded.
     */
    public static boolean hasActiveDropdown() {
        return activeDropdown != null && activeDropdown.expanded;
    }

    // === Getters/Setters ===

    public int getId() { return id; }

    public int getSelectedIndex() { return selectedIndex; }

    public void setSelectedIndex(int index) {
        this.selectedIndex = Math.max(0, Math.min(index, options.size() - 1));
    }

    public String getSelectedValue() {
        if (selectedIndex >= 0 && selectedIndex < options.size()) {
            return options.get(selectedIndex);
        }
        return "";
    }

    public void setSelectedValue(String value) {
        int idx = options.indexOf(value);
        if (idx >= 0) {
            selectedIndex = idx;
        }
    }

    public List<String> getOptions() { return options; }

    public void setOptions(List<String> options) {
        this.options = options != null ? options : new ArrayList<>();
        if (selectedIndex >= this.options.size()) {
            selectedIndex = this.options.isEmpty() ? -1 : 0;
        }
    }

    public void addOption(String option) {
        options.add(option);
    }

    public void clearOptions() {
        options.clear();
        selectedIndex = -1;
    }

    public boolean isExpanded() { return expanded; }

    public boolean isEnabled() { return enabled; }

    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        if (!enabled) expanded = false;
    }

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

    /**
     * Set screen-space position for overlay rendering (handles scroll offsets).
     */
    public void setScreenPosition(int screenX, int screenY) {
        this.screenX = screenX;
        this.screenY = screenY;
    }

    /**
     * Draw the expanded dropdown overlay in screen-space coordinates.
     * Call this AFTER disabling scissor test and popping scroll translation.
     */
    public void drawOverlayScreenSpace(int mouseX, int mouseY) {
        if (!expanded) return;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;

        int optionHeight = fr.FONT_HEIGHT + 4;
        int dropdownHeight = Math.min(options.size() * optionHeight, maxDropdownHeight);
        int dropdownY = screenY + height;

        // Draw dropdown background with border at screen position
        drawRect(screenX - 1, dropdownY, screenX + width + 1, dropdownY + dropdownHeight + 1, borderColorFocused);
        drawRect(screenX, dropdownY, screenX + width, dropdownY + dropdownHeight, dropdownBg);

        // Draw options with screen-space hit detection
        hoveredOption = -1;
        for (int i = 0; i < options.size(); i++) {
            int optY = dropdownY + i * optionHeight;

            // Check hover using screen coordinates
            if (mouseX >= screenX && mouseX < screenX + width &&
                mouseY >= optY && mouseY < optY + optionHeight) {
                hoveredOption = i;
                drawRect(screenX, optY, screenX + width, optY + optionHeight, optionHover);
            }

            // Draw text
            String text = fr.trimStringToWidth(options.get(i), width - paddingX * 2);
            int textCol = (i == selectedIndex) ? ModernColors.ACCENT_BLUE : textColor;
            fr.drawString(text, screenX + paddingX, optY + 2, textCol);
        }
    }

    /**
     * Handle mouse click using screen-space coordinates for expanded dropdown.
     */
    public boolean mouseClickedScreenSpace(int mouseX, int mouseY, int button) {
        if (button != 0 || !enabled) return false;

        if (expanded) {
            // Check if clicked on an option using screen coordinates
            if (hoveredOption >= 0) {
                selectedIndex = hoveredOption;
                expanded = false;
                if (activeDropdown == this) activeDropdown = null;
                return true;
            }
            // Clicked outside - close
            expanded = false;
            if (activeDropdown == this) activeDropdown = null;
            return isInsideMainScreenSpace(mouseX, mouseY);
        } else {
            // Check if clicked on main button using screen coordinates
            if (isInsideMainScreenSpace(mouseX, mouseY)) {
                // Close any other open dropdown first
                if (activeDropdown != null && activeDropdown != this) {
                    activeDropdown.close();
                }
                activeDropdown = this;
                expanded = true;
                return true;
            }
        }
        return false;
    }

    /**
     * Check if mouse is inside the main dropdown button using screen coordinates.
     */
    protected boolean isInsideMainScreenSpace(int mouseX, int mouseY) {
        return mouseX >= screenX && mouseX < screenX + width &&
               mouseY >= screenY && mouseY < screenY + height;
    }

    public int getX() { return x; }
    public int getY() { return y; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Get the height when expanded (for z-ordering).
     */
    public int getExpandedHeight() {
        if (!expanded) return height;
        FontRenderer fr = Minecraft.getMinecraft().fontRenderer;
        int optionHeight = fr.FONT_HEIGHT + 4;
        return height + Math.min(options.size() * optionHeight, maxDropdownHeight);
    }
}
