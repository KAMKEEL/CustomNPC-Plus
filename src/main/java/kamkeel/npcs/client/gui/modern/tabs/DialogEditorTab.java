package kamkeel.npcs.client.gui.modern.tabs;

import kamkeel.npcs.client.gui.modern.DialogEditorPanel;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.client.gui.Gui;
import noppes.npcs.controllers.data.Dialog;

import java.util.List;

/**
 * Abstract base class for dialog editor tabs.
 * Each tab handles its own drawing, click handling, and keyboard input.
 */
public abstract class DialogEditorTab extends Gui {

    protected DialogEditorPanel parent;
    protected Dialog dialog;

    public DialogEditorTab(DialogEditorPanel parent) {
        this.parent = parent;
    }

    /**
     * Called when a dialog is set/loaded into the editor.
     */
    public abstract void loadFromDialog(Dialog dialog);

    /**
     * Called when saving the dialog - copy component values back to dialog.
     */
    public abstract void saveToDialog(Dialog dialog);

    /**
     * Draw the tab content.
     * @return The total content height drawn
     */
    public abstract int draw(int contentX, int contentWidth, int startY, int mouseX, int mouseY, FontRenderer fr);

    /**
     * Handle mouse click within this tab.
     * @param mouseX Scroll-adjusted mouse X
     * @param mouseY Scroll-adjusted mouse Y
     * @param button Mouse button
     * @return true if click was handled
     */
    public abstract boolean mouseClicked(int mouseX, int mouseY, int button);

    /**
     * Handle key typed within this tab.
     * @return true if key was handled
     */
    public abstract boolean keyTyped(char c, int keyCode);

    /**
     * Update cursor blink state for text fields.
     */
    public abstract void updateScreen();

    /**
     * Get all dropdowns in this tab for z-order handling.
     */
    public abstract List<kamkeel.npcs.client.gui.components.ModernDropdown> getDropdowns();

    /**
     * Set the current dialog reference.
     */
    public void setDialog(Dialog dialog) {
        this.dialog = dialog;
    }

    /**
     * Handle clicks on expanded dropdowns in screen space.
     * Override in tabs that use ModernFieldPanel.
     */
    public boolean handleExpandedDropdownScreenClick(int mouseX, int mouseY, int button) {
        return false;
    }

    /**
     * Mark the editor as dirty (unsaved changes).
     */
    protected void markDirty() {
        parent.markDirty();
    }
}
