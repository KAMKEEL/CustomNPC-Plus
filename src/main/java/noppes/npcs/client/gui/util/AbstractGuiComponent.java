package noppes.npcs.client.gui.util;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;

/**
 * Abstract base class for standalone GUI components.
 * Provides common functionality for modern UI elements that don't extend
 * Minecraft's built-in widget classes.
 *
 * Components extending this class:
 * - ModernCheckbox
 * - ModernToggle
 * - ModernDropdown
 * - ModernLabel
 * - ModernSelectButton
 * - CollapsibleSection
 * - ScrollPanel
 *
 * Components that wrap Minecraft classes (ModernButton extends GuiNpcButton)
 * should NOT extend this class.
 */
public abstract class AbstractGuiComponent extends Gui {

    // === Identity ===
    protected int id;

    // === Position & Size ===
    protected int x, y;
    protected int width, height;

    // === State ===
    protected boolean enabled = true;
    protected boolean visible = true;
    protected boolean hovered = false;

    // === Minecraft reference ===
    protected Minecraft mc = Minecraft.getMinecraft();

    // === Constructors ===

    protected AbstractGuiComponent() {
        this(0);
    }

    protected AbstractGuiComponent(int id) {
        this.id = id;
    }

    protected AbstractGuiComponent(int id, int x, int y, int width, int height) {
        this.id = id;
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    // === Abstract Methods ===

    /**
     * Draw this component.
     * @param mouseX Current mouse X position
     * @param mouseY Current mouse Y position
     */
    public abstract void draw(int mouseX, int mouseY);

    // === Optional Override Methods ===

    /**
     * Handle mouse click event.
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param button Mouse button (0=left, 1=right, 2=middle)
     * @return true if the click was handled
     */
    public boolean mouseClicked(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Handle mouse release event.
     * @param mouseX Mouse X position
     * @param mouseY Mouse Y position
     * @param button Mouse button
     * @return true if the release was handled
     */
    public boolean mouseReleased(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Handle key typed event.
     * @param c Character typed
     * @param keyCode Key code
     * @return true if the key was handled
     */
    public boolean keyTyped(char c, int keyCode) {
        return false;
    }

    /**
     * Handle mouse scroll event.
     * @param delta Scroll delta (positive = up, negative = down)
     * @return true if the scroll was handled
     */
    public boolean mouseScrolled(int delta) {
        return false;
    }

    /**
     * Called each tick to update animation states.
     */
    public void updateScreen() {
        // Override if animation needed
    }

    // === Utility Methods ===

    /**
     * Check if the mouse is inside this component's bounds.
     */
    public boolean isInside(int mouseX, int mouseY) {
        return mouseX >= x && mouseX < x + width &&
               mouseY >= y && mouseY < y + height;
    }

    /**
     * Update hover state based on mouse position.
     * Call this at the start of draw() for proper hover detection.
     */
    protected void updateHoverState(int mouseX, int mouseY) {
        hovered = enabled && isInside(mouseX, mouseY);
    }

    /**
     * Get the font renderer.
     */
    protected FontRenderer getFontRenderer() {
        return mc.fontRenderer;
    }

    /**
     * Draw a string with the default font renderer.
     */
    protected void drawText(String text, int x, int y, int color) {
        getFontRenderer().drawString(text, x, y, color);
    }

    /**
     * Draw a string with shadow.
     */
    protected void drawTextWithShadow(String text, int x, int y, int color) {
        getFontRenderer().drawStringWithShadow(text, x, y, color);
    }

    /**
     * Get the width of a string in pixels.
     */
    protected int getTextWidth(String text) {
        return getFontRenderer().getStringWidth(text);
    }

    /**
     * Get the height of text in pixels.
     */
    protected int getTextHeight() {
        return getFontRenderer().FONT_HEIGHT;
    }

    /**
     * Trim a string to fit within a given width.
     */
    protected String trimToWidth(String text, int maxWidth) {
        return getFontRenderer().trimStringToWidth(text, maxWidth);
    }

    // === Getters & Setters ===

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }

    public int getX() { return x; }
    public void setX(int x) { this.x = x; }

    public int getY() { return y; }
    public void setY(int y) { this.y = y; }

    public int getWidth() { return width; }
    public void setWidth(int width) { this.width = width; }

    public int getHeight() { return height; }
    public void setHeight(int height) { this.height = height; }

    public void setPosition(int x, int y) {
        this.x = x;
        this.y = y;
    }

    public void setSize(int width, int height) {
        this.width = width;
        this.height = height;
    }

    public void setBounds(int x, int y, int width, int height) {
        this.x = x;
        this.y = y;
        this.width = width;
        this.height = height;
    }

    public boolean isEnabled() { return enabled; }
    public void setEnabled(boolean enabled) { this.enabled = enabled; }

    public boolean isVisible() { return visible; }
    public void setVisible(boolean visible) { this.visible = visible; }

    public boolean isHovered() { return hovered; }
}
