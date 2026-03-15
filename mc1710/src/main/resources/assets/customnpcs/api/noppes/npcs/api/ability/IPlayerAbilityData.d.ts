/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability
 */

/**
 * Interface for managing player abilities.
 * Players reference abilities by key (built-in names or custom UUIDs).
 * <p>
 * Access via IPlayerData.getAbilityData()
  * @javaFqn noppes.npcs.api.ability.IPlayerAbilityData
*/
export interface IPlayerAbilityData {
    /**
     * Get all unlocked ability keys.
     *
     * @return array of ability keys
     */
    getUnlockedAbilities(): String[];
    /**
     * Unlock an ability for this player.
     *
     * @param key The ability key (built-in name or custom UUID)
     */
    unlockAbility(key: String): import('./void').void;
    /**
     * Lock (remove) an ability from this player.
     *
     * @param key the ability key
     */
    lockAbility(key: String): import('./void').void;
    /**
     * Check if the player has unlocked a specific ability.
     *
     * @param key the ability key
     * @return true if unlocked
     */
    hasUnlockedAbility(key: String): import('./boolean').boolean;
    /**
     * Get the currently selected ability index.
     *
     * @return the selected index
     */
    getSelectedIndex(): import('./int').int;
    /**
     * Set the selected ability index.
     *
     * @param index the ability index
     */
    setSelectedIndex(index: import('./int').int): import('./void').void;
    /**
     * Get the key of the currently selected ability.
     *
     * @return the selected ability key
     */
    getSelectedAbilityKey(): String;
    /**
     * Select the next ability in the list.
     */
    selectNext(): import('./void').void;
    /**
     * Select the previous ability in the list.
     */
    selectPrevious(): import('./void').void;
    /**
     * Check if the player is currently executing an ability.
     *
     * @return true if executing
     */
    isExecutingAbility(): import('./boolean').boolean;
    /**
     * Get the currently executing ability.
     *
     * @return the current ability, or null
     */
    getCurrentAbility(): import('./IAbility').IAbility;
    /**
     * Interrupt the currently executing ability.
     */
    interruptCurrentAbility(): import('./void').void;
    /**
     * Signal the current ability to complete immediately.
     */
    completeCurrentAbility(): import('./void').void;
    /**
     * Check if the player is on universal cooldown.
     *
     * @return true if on cooldown
     */
    isOnCooldown(): import('./boolean').boolean;
    /**
     * Check if a specific ability is on cooldown.
     *
     * @param key The ability key
     * @return true if on cooldown
     */
    isOnCooldown(key: String): import('./boolean').boolean;
    /**
     * Reset the universal cooldown.
     */
    resetCooldown(): import('./void').void;
    /**
     * Reset cooldown for a specific ability key.
     *
     * @param key The ability key
     */
    resetCooldown(key: String): import('./void').void;
    /**
     * Reset all cooldowns.
     */
    resetAllCooldowns(): import('./void').void;
    /**
     * Activate the currently selected ability.
     *
     * @return true if activated
     */
    activateAbility(): import('./boolean').boolean;
    /**
     * Activate a specific ability by key.
     *
     * @param key the ability key
     * @return true if activated
     */
    activateAbility(key: String): import('./boolean').boolean;
    /**
     * Cycle a toggle ability to its next state.
     * Off -&gt; State 1 -&gt; State 2 -&gt; ... -&gt; State N -&gt; Off
     * @param key the ability key
     * @return The new state (0 = off, 1+ = active state number)
     */
    toggleAbility(key: String): import('./int').int;
    /**
     * Get the current toggle state for an ability.
     *
     * @param key the ability key
     * @return 0 if not active, 1+ for active state
     */
    getToggleState(key: String): import('./int').int;
    /**
     * Set a toggle to a specific state. 0 = deactivate, 1+ = specific state.
     *
     * @param key the ability key
     * @param state the toggle state
     */
    setToggleState(key: String, state: import('./int').int): import('./void').void;
    /**
     * Check if a toggle ability is currently active (any state &gt; 0).
     *
     * @param key the ability key
     * @return true if toggled on
     */
    isAbilityToggled(key: String): import('./boolean').boolean;
}
