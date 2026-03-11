package noppes.npcs.client.gui.util;

import kamkeel.npcs.controllers.data.ability.Ability;

/**
 * Callback interface for SubGuiAbilityConfig.
 * Implemented by GUIs that open the ability config dialog.
 */
public interface IAbilityConfigCallback {
    /**
     * Called when an ability has been edited and saved.
     *
     * @param ability The edited ability
     */
    void onAbilitySaved(Ability ability);
}
