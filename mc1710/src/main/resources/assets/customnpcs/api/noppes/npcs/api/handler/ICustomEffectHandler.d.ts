/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.handler
 */

/**
 * Handler interface for managing custom effects.
 * Provides methods to create, retrieve, apply, remove, and save custom effects.
  * @javaFqn noppes.npcs.api.handler.ICustomEffectHandler
*/
export interface ICustomEffectHandler {
    /**
     * Creates a new custom effect with the specified name.
     * If an effect with the given name already exists, the existing effect is returned.
     *
     * @param name the name of the custom effect to create
     * @return the newly created or existing custom effect
     */
    createEffect(name: String): import('./data/ICustomEffect').ICustomEffect;
    /**
     * Retrieves the custom effect with the specified name.
     *
     * @param name the name of the custom effect to retrieve
     * @return the custom effect with the given name, or null if not found
     */
    getEffect(name: String): import('./data/ICustomEffect').ICustomEffect;
    /**
     * Deletes the custom effect with the specified name.
     *
     * @param name the name of the custom effect to delete
     */
    deleteEffect(name: String): import('./void').void;
    /**
     * Checks if the specified player currently has an effect with the given ID.
     *
     * @param player the player to check
     * @param id     the ID of the status effect
     * @return true if the player has the effect, false otherwise
     */
    hasEffect(player: import('../entity/IPlayer').IPlayer, id: import('./int').int): import('./boolean').boolean;
    /**
     * Checks if the specified player currently has the given custom effect.
     *
     * @param player the player to check
     * @param effect the custom effect to check for
     * @return true if the player has the effect, false otherwise
     */
    hasEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect): import('./boolean').boolean;
    /**
     * Retrieves the remaining duration of the effect with the specified ID for the given player.
     * <p>
     * Return values:
     * <ul>
     *     <li>-1 if the player does not have the effect</li>
     *     <li>-100 if the effect has an infinite duration</li>
     * </ul>
     *
     * @param player the player whose effect duration is queried
     * @param id     the ID of the status effect
     * @return the remaining effect time in seconds
     */
    getEffectDuration(player: import('../entity/IPlayer').IPlayer, id: import('./int').int): import('./int').int;
    /**
     * Retrieves the remaining duration of the given custom effect for the specified player.
     * <p>
     * Return values:
     * <ul>
     *     <li>-1 if the player does not have the effect</li>
     *     <li>-100 if the effect has an infinite duration</li>
     * </ul>
     *
     * @param player the player whose effect duration is queried
     * @param effect the custom effect to check
     * @return the remaining effect time in seconds
     */
    getEffectDuration(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect): import('./int').int;
    /**
     * Applies the effect with the specified ID to the player.
     *
     * @param player   the player to apply the effect to
     * @param id       the ID of the status effect
     * @param duration the duration of the effect in seconds (-100 for infinite duration)
     * @param level    the level of the effect (some effects may ignore this value)
     */
    applyEffect(player: import('../entity/IPlayer').IPlayer, id: import('./int').int, duration: import('./int').int, level: import('./byte').byte): import('./void').void;
    /**
     * Applies the specified custom effect to the player.
     *
     * @param player   the player to apply the effect to
     * @param effect   the custom effect to apply
     * @param duration the duration of the effect in seconds (-100 for infinite duration)
     * @param level    the level of the effect (some effects may ignore this value)
     */
    applyEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect, duration: import('./int').int, level: import('./byte').byte): import('./void').void;
    /**
     * Removes the effect with the specified ID from the player.
     *
     * @param player the player to remove the effect from
     * @param id     the ID of the status effect to remove
     */
    removeEffect(player: import('../entity/IPlayer').IPlayer, id: import('./int').int): import('./void').void;
    /**
     * Removes the specified custom effect from the player.
     *
     * @param player the player to remove the effect from
     * @param effect the custom effect to remove
     */
    removeEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect): import('./void').void;
    /**
     * Clears all custom effects from the specified player.
     *
     * @param player the player whose effects should be cleared
     */
    clearEffects(player: import('../entity/IPlayer').IPlayer): import('./void').void;
    /**
     * Applies the effect with the specified ID and index to the player.
     * The index parameter allows for multiple variants of the same effect.
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param player   the player to apply the effect to
     * @param id       the ID of the status effect
     * @param duration the duration of the effect in seconds (-100 for infinite duration)
     * @param level    the level of the effect
     * @param index    the index to differentiate between variants of the effect (INDEX: 0: CNPC+, 1: DBC Addon)
     */
    applyEffect(player: import('../entity/IPlayer').IPlayer, id: import('./int').int, duration: import('./int').int, level: import('./byte').byte, index: import('./int').int): import('./void').void;
    /**
     * Applies the specified custom effect (with the given index) to the player.
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param player   the player to apply the effect to
     * @param effect   the custom effect to apply
     * @param duration the duration of the effect in seconds (-100 for infinite duration)
     * @param level    the level of the effect
     * @param index    the index to differentiate between variants of the effect (INDEX: 0: CNPC+, 1: DBC Addon)
     */
    applyEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect, duration: import('./int').int, level: import('./byte').byte, index: import('./int').int): import('./void').void;
    /**
     * Removes the effect with the specified ID and index from the player.
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param player the player to remove the effect from
     * @param id     the ID of the status effect to remove
     * @param index  the index of the effect variant to remove (INDEX: 0: CNPC+, 1: DBC Addon)
     */
    removeEffect(player: import('../entity/IPlayer').IPlayer, id: import('./int').int, index: import('./int').int): import('./void').void;
    /**
     * Removes the specified custom effect (with the given index) from the player.
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param player the player to remove the effect from
     * @param effect the custom effect to remove
     * @param index  the index of the effect variant to remove (INDEX: 0: CNPC+, 1: DBC Addon)
     */
    removeEffect(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect, index: import('./int').int): import('./void').void;
    /**
     * Clears all custom effects with the specified index from the player.
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param player the player whose effects should be cleared
     * @param index  the index of the effect variants to clear (INDEX: 0: CNPC+, 1: DBC Addon)
     */
    clearEffects(player: import('../entity/IPlayer').IPlayer, index: import('./int').int): import('./void').void;
    /**
     * Retrieves the remaining duration of the effect with the specified ID and index for the given player.
     * <p>
     * Return values:
     * <ul>
     *     <li>-1 if the player does not have the effect</li>
     *     <li>-100 if the effect has an infinite duration</li>
     * </ul>
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param player the player whose effect duration is queried
     * @param id     the ID of the status effect
     * @param index  the index of the effect variant (INDEX: 0: CNPC+, 1: DBC Addon)
     * @return the remaining effect time in seconds
     */
    getEffectDuration(player: import('../entity/IPlayer').IPlayer, id: import('./int').int, index: import('./int').int): import('./int').int;
    /**
     * Retrieves the remaining duration of the specified custom effect (with the given index) for the player.
     * <p>
     * Return values:
     * <ul>
     *     <li>-1 if the player does not have the effect</li>
     *     <li>-100 if the effect has an infinite duration</li>
     * </ul>
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param player the player whose effect duration is queried
     * @param effect the custom effect to check
     * @param index  the index of the effect variant (INDEX: 0: CNPC+, 1: DBC Addon)
     * @return the remaining effect time in seconds
     */
    getEffectDuration(player: import('../entity/IPlayer').IPlayer, effect: import('./data/ICustomEffect').ICustomEffect, index: import('./int').int): import('./int').int;
    /**
     * Retrieves the custom effect with the specified name and index.
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param name  the name of the custom effect
     * @param index the index of the effect variant (INDEX: 0: CNPC+, 1: DBC Addon)
     * @return the custom effect matching the name and index, or null if not found
     */
    getEffect(name: String, index: import('./int').int): import('./data/ICustomEffect').ICustomEffect;
    /**
     * Retrieves the custom effect with the specified ID and index.
     * <p>
     * Note: INDEX: 0: CNPC+, 1: DBC Addon
     *
     * @param id    the ID of the custom effect
     * @param index the index of the effect variant (INDEX: 0: CNPC+, 1: DBC Addon)
     * @return the custom effect matching the ID and index, or null if not found
     */
    getEffect(id: import('./int').int, index: import('./int').int): import('./data/ICustomEffect').ICustomEffect;
    /**
     * Persists the given custom effect.
     * This method saves the effect and returns the saved instance, potentially with an updated ID or name.
     *
     * @param customEffect the custom effect to save
     * @return the saved custom effect instance
     */
    saveEffect(customEffect: import('./data/ICustomEffect').ICustomEffect): import('./data/ICustomEffect').ICustomEffect;
}
