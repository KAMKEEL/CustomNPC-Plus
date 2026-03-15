/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability
 */

/**
 * Interface for an NPC's ability data manager.
 * Provides access to the NPC's abilities and execution state.
  * @javaFqn noppes.npcs.api.ability.IDataAbilities
*/
export interface IDataAbilities {
    /**
     * Check if the ability system is enabled for this NPC.
     *
     * @return true if enabled
     */
    isEnabled(): import('./boolean').boolean;
    /**
     * Enable or disable the ability system for this NPC.
     *
     * @param enabled whether to enable
     */
    setEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Get all abilities assigned to this NPC (resolved from slots).
     * Returns safe deep copies. Use {@link #getSourceAbility(String)} for live references.
     *
     * @return array of abilities
     */
    getAbilities(): import('./IAbility').IAbility[];
    /**
     * Add an inline ability to this NPC.
     *
     * @param ability the ability to add
     */
    addAbility(ability: import('./IAbility').IAbility): import('./void').void;
    /**
     * Add a reference ability by key (built-in name or custom UUID).
     *
     * @param key the ability reference key
     */
    addAbilityReference(key: String): import('./void').void;
    /**
     * Remove an ability by ID.
     *
     * @param abilityId the ability ID to remove
     */
    removeAbility(abilityId: String): import('./void').void;
    /**
     * Get an ability by ID.
     * Returns a safe deep copy. Use {@link #getSourceAbility(String)} for the live reference.
     *
     * @param abilityId the ability ID
     * @return the ability, or null
     */
    getAbility(abilityId: String): import('./IAbility').IAbility;
    /**
     * Check if the NPC has an ability with the given ID.
     *
     * @param abilityId the ability ID
     * @return true if found
     */
    hasAbility(abilityId: String): import('./boolean').boolean;
    /**
     * Check if a slot is a reference (by ability ID).
     *
     * @param abilityId the ability ID
     * @return true if reference
     */
    isAbilityReference(abilityId: String): import('./boolean').boolean;
    /**
     * Convert a reference slot to inline (by ability ID).
     * Returns false if the reference cannot be resolved.
     *
     * @param abilityId the ability ID
     * @return true if converted successfully
     */
    convertToInline(abilityId: String): import('./boolean').boolean;
    /**
     * Clear all abilities from this NPC.
     */
    clearAbilities(): import('./void').void;
    /**
     * Get the currently executing ability, or null if none.
     * Returns a safe deep copy. Use {@link #getSourceCurrentAbility()} for the live reference.
     *
     * @return the current ability, or null
     */
    getCurrentAbility(): import('./IAbility').IAbility;
    /**
     * Check if any ability is currently executing.
     *
     * @return true if executing
     */
    isExecutingAbility(): import('./boolean').boolean;
    /**
     * Interrupt the current ability if one is executing.
     */
    interruptCurrentAbility(): import('./void').void;
    /**
     * Signal the current ability to complete immediately.
     */
    completeCurrentAbility(): import('./void').void;
    /**
     * Get the global cooldown timer in ticks.
     *
     * @return cooldown in ticks
     */
    getGlobalCooldown(): import('./int').int;
    /**
     * Set the global cooldown timer in ticks.
     *
     * @param ticks cooldown in ticks
     */
    setGlobalCooldown(ticks: import('./int').int): import('./void').void;
    /**
     * Reset all ability cooldowns.
     */
    resetCooldowns(): import('./void').void;
    /**
     * Force start an ability on this NPC.
     * If an ability is currently executing, it will be cancelled.
     *
     * @param abilityId The ID of the ability to start
     * @return true if the ability was started successfully
     */
    forceStartAbility(abilityId: String): import('./boolean').boolean;
    /**
     * Force start an ability on this NPC with a specific target.
     * If an ability is currently executing, it will be cancelled.
     *
     * @param abilityId The ID of the ability to start
     * @param target    The target entity (can be null for self-targeted abilities)
     * @return true if the ability was started successfully
     */
    forceStartAbility(abilityId: String, target: Object): import('./boolean').boolean;
    /**
     * Execute an ability on this NPC by key (built-in name or custom UUID).
     * The NPC does NOT need to have this ability assigned.
     * If an ability is currently executing, it will be cancelled.
     *
     * @param key The ability key (built-in name or custom UUID)
     * @return true if the ability was started successfully
     */
    executeAbility(key: String): import('./boolean').boolean;
    /**
     * Execute an ability on this NPC with a specific target.
     * The NPC does NOT need to have this ability assigned.
     * If an ability is currently executing, it will be cancelled.
     *
     * @param key    The ability key (built-in name or custom UUID)
     * @param target The target entity (can be null for self-targeted abilities)
     * @return true if the ability was started successfully
     */
    executeAbility(key: String, target: Object): import('./boolean').boolean;
    /**
     * Create a new ability instance by type ID.
     * The ability is not assigned to this NPC - use addAbility() to assign it.
     *
     * @param typeId The type ID (e.g., "cnpc:slam", "cnpc:projectile")
     * @return The new ability, or null if the type is unknown
     */
    createAbility(typeId: String): import('./IAbility').IAbility;
    /**
     * Get the live source reference of an ability by ID.
     * Unlike {@link #getAbility(String)} which returns a safe copy,
     * this returns the actual ability object. Modifications will permanently
     * affect the NPC's ability configuration.
     *
     * @param abilityId The ID of the ability
     * @return The live ability reference, or null if not found
     */
    getSourceAbility(abilityId: String): import('./IAbility').IAbility;
    /**
     * Get the live source reference of the currently executing ability.
     * Unlike {@link #getCurrentAbility()} which returns a safe copy,
     * this returns the actual ability object. Modifications will permanently
     * affect the NPC's ability configuration.
     *
     * @return The live ability reference, or null if no ability is executing
     */
    getSourceCurrentAbility(): import('./IAbility').IAbility;
}
