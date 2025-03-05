package kamkeel.npcs.editorgui;

import net.minecraft.client.gui.Gui;

/**
 * AbstractEditorComponent provides common fields and methods for all editor components.
 */
public abstract class AbstractEditorComponent extends Gui implements IEditorComponent {
    // Store the editor component's ID.
    protected int id;
    // Position and size.
    public int posX, posY, width, height;
    protected boolean selected = false;

    public AbstractEditorComponent(int posX, int posY, int width, int height) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
    }

    /**
     * Returns the ID for this editor component.
     */
    public int getID() {
        return id;
    }

    @Override
    public void setSelected(boolean selected) {
        this.selected = selected;
    }

    @Override
    public boolean isSelected() {
        return selected;
    }

    /**
     * Draws an outline around the component.
     * Yellow if selected; grey if not.
     */
    protected void renderSelectionOutline() {
        int outlineColor = selected ? 0xFFFFFF00 : 0xFF888888;
        // Top border
        drawRect(posX - 1, posY - 1, posX + width + 1, posY, outlineColor);
        // Bottom border
        drawRect(posX - 1, posY + height, posX + width + 1, posY + height + 1, outlineColor);
        // Left border
        drawRect(posX - 1, posY - 1, posX, posY + height + 1, outlineColor);
        // Right border
        drawRect(posX + width, posY - 1, posX + width + 1, posY + height + 1, outlineColor);
    }

    @Override
    public void update() { }
}
