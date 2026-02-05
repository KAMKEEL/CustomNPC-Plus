package kamkeel.npcs.client.gui.components;

import net.minecraft.client.Minecraft;
import noppes.npcs.client.gui.util.GuiNpcTextField;

/**
 * Modern styled text field extending GuiNpcTextField.
 * Provides additional convenience methods like setBounds() for dynamic positioning.
 * Designed for use in panels that manage their own rendering/input.
 */
public class ModernTextField extends GuiNpcTextField {

    protected String placeholder = "";

    /**
     * Create a text field with position and size.
     */
    public ModernTextField(int id, int x, int y, int width, int height) {
        super(id, null, Minecraft.getMinecraft().fontRenderer, x, y, width, height, "");
    }

    /**
     * Create a text field with initial text.
     */
    public ModernTextField(int id, int x, int y, int width, int height, String text) {
        super(id, null, Minecraft.getMinecraft().fontRenderer, x, y, width, height, text != null ? text : "");
    }

    // === Convenience Methods ===

    /**
     * Set bounds (position and size) for dynamic layout.
     */
    public void setBounds(int x, int y, int width, int height) {
        this.xPosition = x;
        this.yPosition = y;
        this.width = width;
        this.height = height;
    }

    /**
     * Set position only.
     */
    public void setPosition(int x, int y) {
        this.xPosition = x;
        this.yPosition = y;
    }

    /**
     * Draw the text field. Alias for drawTextBox() for API compatibility.
     */
    public void draw(int mouseX, int mouseY) {
        drawTextBox();
    }

    /**
     * Handle key typed. Alias for textboxKeyTyped() for API compatibility.
     */
    public boolean keyTyped(char c, int keyCode) {
        return textboxKeyTyped(c, keyCode);
    }

    /**
     * Handle mouse click with boolean return.
     * GuiNpcTextField.mouseClicked returns void, so we wrap it.
     */
    public boolean handleClick(int mouseX, int mouseY, int button) {
        boolean wasInside = isInside(mouseX, mouseY);
        super.mouseClicked(mouseX, mouseY, button);
        return wasInside && button == 0;
    }

    /**
     * Set max length. Alias for setMaxStringLength() for API compatibility.
     */
    public ModernTextField setMaxLength(int max) {
        setMaxStringLength(max);
        return this;
    }

    /**
     * Set placeholder text (note: GuiNpcTextField doesn't display placeholders,
     * but we keep the method for API compatibility).
     */
    public ModernTextField setPlaceholder(String placeholder) {
        this.placeholder = placeholder != null ? placeholder : "";
        return this;
    }

    /**
     * Get placeholder text.
     */
    public String getPlaceholder() {
        return placeholder;
    }

    // === Position/Size Getters ===

    public int getX() { return xPosition; }
    public int getY() { return yPosition; }
    public int getWidth() { return width; }
    public int getHeight() { return height; }

    /**
     * Check if a point is inside this field.
     */
    public boolean isInside(int mouseX, int mouseY) {
        return mouseX >= xPosition && mouseX < xPosition + width &&
               mouseY >= yPosition && mouseY < yPosition + height;
    }

    /**
     * Set enabled state.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Set colors (no-op for compatibility, uses default MC styling).
     */
    public ModernTextField setColors(int bg, int border, int text) {
        // GuiNpcTextField uses default styling
        return this;
    }

    /**
     * Set multi-line mode (no-op, use ModernTextArea for multi-line).
     */
    public ModernTextField setMultiLine(boolean multi) {
        // Use ModernTextArea for multi-line
        return this;
    }
}
