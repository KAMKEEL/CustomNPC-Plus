package noppes.npcs.client.gui.hud;

import net.minecraft.client.gui.GuiButton;
import net.minecraft.client.gui.ScaledResolution;
import net.minecraft.nbt.NBTTagCompound;
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
     * The default implementation adds a toggle button for enabling/disabling the component.
     */
    public void addEditorButtons(List<GuiButton> buttonList) {
        // Toggle enabled/disabled button (id 999 reserved for toggle)
        String label = enabled ? "Enabled" : "Disabled";
        buttonList.add(new GuiButton(999, 0, 0, 120, 20, label));
    }

    /**
     * Handles editor button actions specific to this HUD component.
     * The default implementation toggles the enabled state if button id is 999.
     */
    public void onEditorButtonPressed(GuiButton button) {
        if(button.id == 999){
            enabled = !enabled;
            button.displayString = enabled ? "Enabled" : "Disabled";
        }
    }

    /**
     * Computes the effective scale factor for rendering and hitbox calculations.
     * Uses the current resolution (with 1920 as the base width).
     */
    public float getEffectiveScale(ScaledResolution res) {
        return (scale / 100.0F) * (res.getScaledWidth() / 1920.0F);
    }
}
