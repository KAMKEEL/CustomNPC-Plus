package noppes.npcs.api.handler;

import noppes.npcs.api.ability.IChainedAbility;

/**
 * Handler for ability management. Allows scripts to query ability types,
 * built-in abilities, and custom ability presets.
 * <p>
 * Access via API.getAbilities()
 */
public interface IAbilityHandler {

    /**
     * Get all registered ability type IDs.
     *
     * @return array of all registered ability type IDs
     */
    String[] getTypes();

    /**
     * Check if an ability type is registered.
     *
     * @param typeId the ability type ID to check
     * @return true if the type is registered
     */
    boolean hasType(String typeId);

    /**
     * Get all built-in ability names.
     *
     * @return array of all registered ability names (built-in and custom)
     */
    String[] getAbilityNameArray();

    /**
     * Check if a built-in ability exists by name.
     *
     * @param name the ability name to check
     * @return true if an ability with this name exists
     */
    boolean hasAbilityName(String name);

    /**
     * Get all custom ability preset names.
     *
     * @return array of all custom (user-created) ability names
     */
    String[] getCustomAbilityNameArray();

    /**
     * Check if a custom ability preset exists by name.
     *
     * @param name the custom ability name to check
     * @return true if a custom ability with this name exists
     */
    boolean hasCustomAbilityName(String name);

    /**
     * Delete a custom ability preset by name.
     *
     * @param name the custom ability name to delete
     * @return true if deleted successfully
     */
    boolean deleteCustomAbilityByName(String name);

    /**
     * Get all chained ability names.
     *
     * @return array of all chained ability names
     */
    String[] getChainedAbilityNames();

    /**
     * Check if a chained ability exists by name.
     *
     * @param name the chained ability name to check
     * @return true if a chained ability with this name exists
     */
    boolean hasChainedAbilityName(String name);

    /**
     * Delete a chained ability by name.
     *
     * @param name the chained ability name to delete
     * @return true if deleted successfully
     */
    boolean deleteChainedAbilityByName(String name);

    /**
     * Get a chained ability by name.
     *
     * @param name the chained ability name
     * @return the chained ability, or null if not found
     */
    IChainedAbility getChainedAbility(String name);

    /**
     * Save a chained ability. If a chain with the same name exists, it is overwritten.
     *
     * @param chain the chained ability to save
     * @return true if saved successfully
     */
    boolean saveChainedAbility(IChainedAbility chain);
}
