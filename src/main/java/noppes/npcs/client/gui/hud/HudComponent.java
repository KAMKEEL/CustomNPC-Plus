package noppes.npcs.client.gui.hud;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.client.gui.GuiButton;
import java.util.List;

public abstract class HudComponent {
    // Stored as percentages (0-100) of the current screen resolution.
    protected int posX = 50, posY = 50, scale = 100, textAlign = 0;

    public boolean enabled = true;
    public boolean hasData = false;
    public boolean isEditting = false;
    public int overlayWidth = 200, overlayHeight = 120;

    /**
     * Loads custom HUD data from an NBT compound.
     */
    public abstract void loadData(NBTTagCompound compound);

    /**
     * Loads HUD settings from the config.
     */
    public abstract void load();

    /**
     * Saves HUD settings to the config.
     */
    public abstract void save();

    /**
     * Renders the HUD as it appears during normal gameplay.
     */
    public abstract void renderOnScreen(float partialTicks);

    /**
     * Renders the HUD in edit mode (with handles, borders, dummy data, etc.).
     */
    public abstract void renderEditing();

    /**
     * Allows the component to add its own editor buttons.
     */
    public void addEditorButtons(List<GuiButton> buttonList) {
        // Default: do nothing.
    }

    /**
     * Handles editor button actions specific to this HUD component.
     */
    public void onEditorButtonPressed(GuiButton button) {
        // Default: do nothing.
    }
}
