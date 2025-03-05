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
     * Draws a 2-pixel thick yellow border if selected.
     */
    protected void renderSelectionOutline() {
        if (!selected)
            return;
        // Top
        drawRect(posX - 2, posY - 2, posX + width + 2, posY - 1, 0xFFFFFF00);
        // Bottom
        drawRect(posX - 2, posY + height + 1, posX + width + 2, posY + height + 2, 0xFFFFFF00);
        // Left
        drawRect(posX - 2, posY - 2, posX - 1, posY + height + 2, 0xFFFFFF00);
        // Right
        drawRect(posX + width + 1, posY - 2, posX + width + 2, posY + height + 2, 0xFFFFFF00);
    }

    @Override
    public void update() { }
}
