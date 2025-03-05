package kamkeel.npcs.editorgui;

import net.minecraft.client.gui.Gui;

/**
 * AbstractEditorComponent provides common position/size fields and a method to draw
 * a yellow selection outline when the component is selected.
 */
public abstract class AbstractEditorComponent extends Gui implements IEditorComponent {
    // These fields are public for easy access.
    public int posX, posY, width, height;
    protected boolean selected = false;

    public AbstractEditorComponent(int posX, int posY, int width, int height) {
        this.posX = posX;
        this.posY = posY;
        this.width = width;
        this.height = height;
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
     * Draws a 2–pixel thick yellow border if this component is selected.
     */
    protected void renderSelectionOutline() {
        if (!selected)
            return;
        // Top edge
        drawRect(posX - 2, posY - 2, posX + width + 2, posY - 1, 0xFFFFFF00);
        // Bottom edge
        drawRect(posX - 2, posY + height + 1, posX + width + 2, posY + height + 2, 0xFFFFFF00);
        // Left edge
        drawRect(posX - 2, posY - 2, posX - 1, posY + height + 2, 0xFFFFFF00);
        // Right edge
        drawRect(posX + width + 1, posY - 2, posX + width + 2, posY + height + 2, 0xFFFFFF00);
    }

    @Override
    public void update() { }
}
