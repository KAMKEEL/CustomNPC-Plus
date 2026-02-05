package kamkeel.npcs.client.gui.components;

import noppes.npcs.client.gui.util.GuiNpcTextArea;

/**
 * Modern styled text area extending GuiNpcTextArea.
 * Provides convenience methods like setBounds() for dynamic positioning.
 * Designed for use in panels that manage their own rendering/input.
 */
public class ModernTextArea extends GuiNpcTextArea {

    /**
     * Create a text area with position and size.
     */
    public ModernTextArea(int id, int x, int y, int width, int height) {
        super(id, null, x, y, width, height, "");
    }

    /**
     * Create a text area with initial text.
     */
    public ModernTextArea(int id, int x, int y, int width, int height, String text) {
        super(id, null, x, y, width, height, text != null ? text : "");
    }

    // === Convenience Methods ===

    /**
     * Draw the text area. Alias for drawTextBox(mouseX, mouseY).
     */
    public void draw(int mouseX, int mouseY) {
        drawTextBox(mouseX, mouseY);
    }

    /**
     * Handle key typed. Alias for textboxKeyTyped().
     */
    public boolean keyTyped(char c, int keyCode) {
        return textboxKeyTyped(c, keyCode);
    }

    /**
     * Handle mouse click with boolean return.
     * GuiNpcTextArea.mouseClicked returns void, so we wrap it.
     */
    public boolean handleClick(int mouseX, int mouseY, int button) {
        boolean wasInside = isInside(mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY, button);
        return wasInside && button == 0;
    }

    /**
     * Set max length. Alias for setMaxStringLength().
     */
    public ModernTextArea setMaxLength(int max) {
        setMaxStringLength(max);
        return this;
    }

    /**
     * Set enabled state.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
        this.canEdit = enabled;
    }

    /**
     * Mouse released - no-op, scrollbar handled internally by drawTextBox.
     */
    public void mouseReleased(int mouseX, int mouseY) {
        // GuiNpcTextArea handles scrollbar internally
    }

    /**
     * Mouse dragged - no-op, scrollbar handled internally by drawTextBox.
     */
    public void mouseDragged(int mouseX, int mouseY) {
        // GuiNpcTextArea handles scrollbar internally
    }

    /**
     * Check if a point is inside this text area.
     */
    public boolean isInside(int mouseX, int mouseY) {
        return mouseX >= posX && mouseX < posX + width &&
               mouseY >= posY && mouseY < posY + height;
    }

    // === Position Getters ===

    public int getX() { return posX; }
    public int getY() { return posY; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }
}
