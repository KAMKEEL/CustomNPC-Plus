package kamkeel.npcs.controllers.data.ability.gui;

import kamkeel.npcs.controllers.data.ability.data.ChainedAbility;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

/**
 * External field definition provider for chained abilities.
 * Called during GUI initialization to allow mods to inject custom tabs/fields
 * into the chained ability configuration GUI.
 * <p>
 * Note: Although this interface references FieldDef (client-only), it is safe to
 * register on both sides because the generic type is erased at bytecode level.
 * The addFieldDefinitions method is only ever called from client-side GUI code.
 */
public interface IChainedAbilityFieldProvider {
    /**
     * Add field definitions to the chained ability's GUI.
     * Fields added here should use .tab() to assign them to a custom tab.
     *
     * @param chain The chained ability being configured
     * @param defs  The mutable list of field definitions to add to
     */
    void addFieldDefinitions(ChainedAbility chain, List<FieldDef> defs);
}
