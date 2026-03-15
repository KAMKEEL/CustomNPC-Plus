package noppes.npcs.api.ability;

/**
 * Handler for ability registration and creation.
 * Use this to register custom ability types from external mods.
 */
public interface IAbilityHandler {

    /**
     * Register a custom ability type.
     * Call during FMLInitializationEvent.
     *
     * @param typeId       The unique type ID (e.g., "mymod:custom_ability")
     * @param abilityClass The class that implements the ability
     */
    void registerType(String typeId, Class<? extends IAbility> abilityClass);

    /**
     * Check if an ability type is registered.
     *
     * @param typeId the type ID to check
     * @return true if registered
     */
    boolean hasType(String typeId);

    /**
     * Get all registered ability type IDs.
     *
     * @return array of registered type IDs
     */
    String[] getTypes();

    /**
     * Create a new ability of the given type.
     * Returns null if the type is not registered.
     *
     * @param typeId the type ID
     * @return the new ability, or null
     */
    IAbility create(String typeId);

    /**
     * Register a pre-configured ability by name.
     * These abilities are resolved by name and can be referenced by NPCs and players.
     * Call during FMLInitializationEvent.
     *
     * @param name    The unique name for the ability
     * @param ability The ability instance
     */
    void registerAbility(String name, IAbility ability);

    /**
     * Check if a registered ability exists by name.
     *
     * @param name The ability name
     * @return true if the ability exists
     */
    boolean hasAbility(String name);
}
