package noppes.npcs.client.gui.builder;

import cpw.mods.fml.relauncher.Side;
import cpw.mods.fml.relauncher.SideOnly;

/**
 * Callback interface for ModernFieldPanel events.
 * Implemented by the parent GUI to handle external selection and dirty tracking.
 */
@SideOnly(Side.CLIENT)
public interface ModernFieldPanelListener {

    /**
     * Called when an external selector action is requested (quest, dialog, faction, sound, mail).
     * @param action The action type (e.g. "quest", "dialog", "faction", "sound", "mail")
     * @param slot The slot identifier for the request
     */
    void onSelectAction(String action, int slot);

    /**
     * Called when a color picker is requested.
     * @param slot The color slot identifier
     * @param currentColor The current color value
     */
    void onColorSelect(int slot, int currentColor);

    /**
     * Called when any field value changes (for dirty tracking).
     */
    void onFieldChanged();
}
