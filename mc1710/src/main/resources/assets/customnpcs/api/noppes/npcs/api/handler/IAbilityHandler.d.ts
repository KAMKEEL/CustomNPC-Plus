/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handler for ability management. Allows scripts to query ability types,
 * built-in abilities, and custom ability presets.
 * <p>
 * Access via API.getAbilities()
  * @javaFqn noppes.npcs.api.handler.IAbilityHandler
*/
export interface IAbilityHandler {
    /**
     * Get all registered ability type IDs.
     *
     * @return array of all registered ability type IDs
     */
    getTypes(): String[];
    /**
     * Check if an ability type is registered.
     *
     * @param typeId the ability type ID to check
     * @return true if the type is registered
     */
    hasType(typeId: String): import('./boolean').boolean;
    /**
     * Get all built-in ability names.
     *
     * @return array of all registered ability names (built-in and custom)
     */
    getAbilityNameArray(): String[];
    /**
     * Check if a built-in ability exists by name.
     *
     * @param name the ability name to check
     * @return true if an ability with this name exists
     */
    hasAbilityName(name: String): import('./boolean').boolean;
    /**
     * Get all custom ability preset names.
     *
     * @return array of all custom (user-created) ability names
     */
    getCustomAbilityNameArray(): String[];
    /**
     * Check if a custom ability preset exists by name.
     *
     * @param name the custom ability name to check
     * @return true if a custom ability with this name exists
     */
    hasCustomAbilityName(name: String): import('./boolean').boolean;
    /**
     * Delete a custom ability preset by name.
     *
     * @param name the custom ability name to delete
     * @return true if deleted successfully
     */
    deleteCustomAbilityByName(name: String): import('./boolean').boolean;
    /**
     * Get all chained ability names.
     *
     * @return array of all chained ability names
     */
    getChainedAbilityNames(): String[];
    /**
     * Check if a chained ability exists by name.
     *
     * @param name the chained ability name to check
     * @return true if a chained ability with this name exists
     */
    hasChainedAbilityName(name: String): import('./boolean').boolean;
    /**
     * Delete a chained ability by name.
     *
     * @param name the chained ability name to delete
     * @return true if deleted successfully
     */
    deleteChainedAbilityByName(name: String): import('./boolean').boolean;
    /**
     * Get a chained ability by name.
     *
     * @param name the chained ability name
     * @return the chained ability, or null if not found
     */
    getChainedAbility(name: String): import('../ability/IChainedAbility').IChainedAbility;
    /**
     * Save a chained ability. If a chain with the same name exists, it is overwritten.
     *
     * @param chain the chained ability to save
     * @return true if saved successfully
     */
    saveChainedAbility(chain: import('../ability/IChainedAbility').IChainedAbility): import('./boolean').boolean;
}
