package noppes.npcs.client.gui.util;

import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;

/**
 * Callback for when a chained ability is saved from the config SubGui.
 */
public interface IChainedAbilityConfigCallback {
    void onChainedAbilitySaved(ChainedAbility chain);
}
