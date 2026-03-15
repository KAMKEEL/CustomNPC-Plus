package noppes.npcs.api.ability;

/**
 * Interface for managing player abilities.
 * Players reference abilities by key (built-in names or custom UUIDs).
 * <p>
 * Access via IPlayerData.getAbilityData()
 */
public interface IPlayerAbilityData {

    /**
     * Get all unlocked ability keys.
     *
     * @return array of ability keys
     */
    String[] getUnlockedAbilities();

    /**
     * Unlock an ability for this player.
     *
     * @param key The ability key (built-in name or custom UUID)
     */
    void unlockAbility(String key);

    /**
     * Lock (remove) an ability from this player.
     *
     * @param key the ability key
     */
    void lockAbility(String key);

    /**
     * Check if the player has unlocked a specific ability.
     *
     * @param key the ability key
     * @return true if unlocked
     */
    boolean hasUnlockedAbility(String key);

    /**
     * Get the currently selected ability index.
     *
     * @return the selected index
     */
    int getSelectedIndex();

    /**
     * Set the selected ability index.
     *
     * @param index the ability index
     */
    void setSelectedIndex(int index);

    /**
     * Get the key of the currently selected ability.
     *
     * @return the selected ability key
     */
    String getSelectedAbilityKey();

    /**
     * Select the next ability in the list.
     */
    void selectNext();

    /**
     * Select the previous ability in the list.
     */
    void selectPrevious();

    /**
     * Check if the player is currently executing an ability.
     *
     * @return true if executing
     */
    boolean isExecutingAbility();

    /**
     * Get the currently executing ability.
     *
     * @return the current ability, or null
     */
    IAbility getCurrentAbility();

    /**
     * Interrupt the currently executing ability.
     */
    void interruptCurrentAbility();

    /**
     * Signal the current ability to complete immediately.
     */
    void completeCurrentAbility();

    /**
     * Check if the player is on universal cooldown.
     *
     * @return true if on cooldown
     */
    boolean isOnCooldown();

    /**
     * Check if a specific ability is on cooldown.
     *
     * @param key The ability key
     * @return true if on cooldown
     */
    boolean isOnCooldown(String key);

    /**
     * Reset the universal cooldown.
     */
    void resetCooldown();

    /**
     * Reset cooldown for a specific ability key.
     *
     * @param key The ability key
     */
    void resetCooldown(String key);

    /**
     * Reset all cooldowns.
     */
    void resetAllCooldowns();

    /**
     * Activate the currently selected ability.
     *
     * @return true if activated
     */
    boolean activateAbility();

    /**
     * Activate a specific ability by key.
     *
     * @param key the ability key
     * @return true if activated
     */
    boolean activateAbility(String key);

    // Toggle system

    /**
     * Cycle a toggle ability to its next state.
     * Off -&gt; State 1 -&gt; State 2 -&gt; ... -&gt; State N -&gt; Off
     * @param key the ability key
     * @return The new state (0 = off, 1+ = active state number)
     */
    int toggleAbility(String key);

    /**
     * Get the current toggle state for an ability.
     *
     * @param key the ability key
     * @return 0 if not active, 1+ for active state
     */
    int getToggleState(String key);

    /**
     * Set a toggle to a specific state. 0 = deactivate, 1+ = specific state.
     *
     * @param key the ability key
     * @param state the toggle state
     */
    void setToggleState(String key, int state);

    /**
     * Check if a toggle ability is currently active (any state &gt; 0).
     *
     * @param key the ability key
     * @return true if toggled on
     */
    boolean isAbilityToggled(String key);
}
