/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability
 */

/**
 * Handler for ability registration and creation.
 * Use this to register custom ability types from external mods.
  * @javaFqn noppes.npcs.api.ability.IAbilityHandler
*/
export interface IAbilityHandler {
    /**
     * Register a custom ability type.
     * Call during FMLInitializationEvent.
     *
     * @param typeId       The unique type ID (e.g., "mymod:custom_ability")
     * @param abilityClass The class that implements the ability
     */
    registerType(typeId: String, abilityClass: Class<import('./IAbility').IAbility>): import('./void').void;
    /**
     * Check if an ability type is registered.
     *
     * @param typeId the type ID to check
     * @return true if registered
     */
    hasType(typeId: String): import('./boolean').boolean;
    /**
     * Get all registered ability type IDs.
     *
     * @return array of registered type IDs
     */
    getTypes(): String[];
    /**
     * Create a new ability of the given type.
     * Returns null if the type is not registered.
     *
     * @param typeId the type ID
     * @return the new ability, or null
     */
    create(typeId: String): import('./IAbility').IAbility;
    /**
     * Register a pre-configured ability by name.
     * These abilities are resolved by name and can be referenced by NPCs and players.
     * Call during FMLInitializationEvent.
     *
     * @param name    The unique name for the ability
     * @param ability The ability instance
     */
    registerAbility(name: String, ability: import('./IAbility').IAbility): import('./void').void;
    /**
     * Check if a registered ability exists by name.
     *
     * @param name The ability name
     * @return true if the ability exists
     */
    hasAbility(name: String): import('./boolean').boolean;
}
