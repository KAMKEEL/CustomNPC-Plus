package kamkeel.npcs.controllers.data.ability.gui;

import kamkeel.npcs.controllers.data.ability.Ability;
import noppes.npcs.client.gui.builder.FieldDef;

import java.util.List;

/**
 * External field definition provider for abilities.
 * Called during getAllDefinitions() to allow mods to inject custom tabs/fields
 * into the ability configuration GUI.
 * <p>
 * Note: Although this interface references FieldDef (client-only), it is safe to
 * register on both sides because the generic type is erased at bytecode level.
 * The addFieldDefinitions method is only ever called from client-side GUI code.
 */
public interface IAbilityFieldProvider {
    /**
     * Add field definitions to the ability's GUI.
     * Fields added here should use .tab() to assign them to a custom tab.
     *
     * @param ability The ability being configured
     * @param defs    The mutable list of field definitions to add to
     */
    void addFieldDefinitions(Ability ability, List<FieldDef> defs);
}
