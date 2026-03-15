/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.ability
 */

/**
 * Interface for NPC abilities.
 * Abilities are special attacks or actions that NPCs can perform during combat.
  * @javaFqn noppes.npcs.api.ability.IAbility
*/
export interface IAbility extends import('./IAbilityAction').IAbilityAction {
    /**
     * Get the unique ID (UUID for custom abilities, registry key for built-in).
     * This is the stable reference that never changes after creation.
     * @return the unique ability ID
     */
    getId(): String;
    /**
     * Set the unique ID of this ability instance.
     * @param id Unique ability ID.
     */
    setId(id: String): import('./void').void;
    /**
     * Get the unique identifier/name of this ability (used for file naming, commands, search).
     */
    getName(): String;
    /**
     * Set the unique identifier/name of this ability.
     * @param name Unique identifier.
     */
    setName(name: String): import('./void').void;
    /**
     * Get the display name (cosmetic). Falls back to getName() if not set.
     * @return the display name
     */
    getDisplayName(): String;
    /**
     * Set the display name (cosmetic). Pass empty string to use getName() as display.
     * @param displayName Display name, or empty string for default.
     */
    setDisplayName(displayName: String): import('./void').void;
    /**
     * Get the type ID of this ability (e.g., "cnpc:slam", "cnpc:beam").
     * @return the type ID
     */
    getTypeId(): String;
    /**
     * Check if this ability is enabled.
     */
    isEnabled(): import('./boolean').boolean;
    /**
     * Enable or disable this ability.
     * @param enabled Whether the ability is enabled.
     */
    setEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /**
     * Get the selection weight for random ability selection.
     * Higher weight = more likely to be selected.
     */
    getWeight(): import('./int').int;
    /**
     * Set the selection weight.
     * @param weight Selection weight (higher = more likely).
     */
    setWeight(weight: import('./int').int): import('./void').void;
    /**
     * Get the minimum range at which this ability can be used.
     */
    getMinRange(): import('./float').float;
    /**
     * Set the minimum range.
     * @param range Minimum range in blocks.
     */
    setMinRange(range: import('./float').float): import('./void').void;
    /**
     * Get the maximum range at which this ability can be used.
     */
    getMaxRange(): import('./float').float;
    /**
     * Set the maximum range.
     * @param range Maximum range in blocks.
     */
    setMaxRange(range: import('./float').float): import('./void').void;
    /**
     * Get the cooldown time in ticks.
     */
    getCooldownTicks(): import('./int').int;
    /**
     * Set the cooldown time in ticks.
     * @param ticks Cooldown duration in ticks.
     */
    setCooldownTicks(ticks: import('./int').int): import('./void').void;
    /**
     * Get the wind-up time in ticks (telegraph phase).
     * @return wind-up time in ticks
     */
    getWindUpTicks(): import('./int').int;
    /**
     * Set the wind-up time in ticks.
     * @param ticks Wind-up duration in ticks.
     */
    setWindUpTicks(ticks: import('./int').int): import('./void').void;
    /**
     * Get the dazed time in ticks (stun after interrupt during WINDUP).
     * @return dazed time in ticks
     */
    getDazedTicks(): import('./int').int;
    /**
     * Set the dazed time in ticks.
     * @param ticks Dazed duration in ticks.
     */
    setDazedTicks(ticks: import('./int').int): import('./void').void;
    /**
     * Check if this ability can be interrupted by damage.
     * @return true if interruptible
     */
    isInterruptible(): import('./boolean').boolean;
    /**
     * Set whether this ability can be interrupted.
     * @param interruptible Whether the ability is interruptible.
     */
    setInterruptible(interruptible: import('./boolean').boolean): import('./void').void;
    /**
     * Check if this ability ignores target invulnerability frames (hurt resistance).
     * @return true if ignoring invulnerability frames
     */
    isIgnoreIFrames(): import('./boolean').boolean;
    /**
     * Set whether this ability ignores target invulnerability frames (hurt resistance).
     * @param ignore Whether to ignore invulnerability frames.
     */
    setIgnoreIFrames(ignore: import('./boolean').boolean): import('./void').void;
    /**
     * Get the lock movement type for this ability.
     *
     * @return 0=NO, 1=WINDUP, 2=ACTIVE, 3=WINDUP_AND_ACTIVE
     */
    getLockMovementType(): import('./int').int;
    /**
     * Set the lock movement type for this ability.
     *
     * @param type 0=NO, 1=WINDUP, 2=ACTIVE, 3=WINDUP_AND_ACTIVE
     */
    setLockMovementType(type: import('./int').int): import('./void').void;
    /**
     * Get the rotation mode for this ability.
     *
     * @return 0=FREE, 1=LOCKED, 2=TRACK
     */
    getRotationModeType(): import('./int').int;
    /**
     * Set the rotation mode for this ability.
     *
     * @param type 0=FREE, 1=LOCKED, 2=TRACK
     */
    setRotationModeType(type: import('./int').int): import('./void').void;
    /**
     * Get the rotation phase for this ability (when rotation mode applies).
     *
     * @return 0=NO, 1=WINDUP, 2=ACTIVE, 3=WINDUP_AND_ACTIVE
     */
    getRotationPhaseType(): import('./int').int;
    /**
     * Set the rotation phase for this ability.
     *
     * @param type 0=NO, 1=WINDUP, 2=ACTIVE, 3=WINDUP_AND_ACTIVE
     */
    setRotationPhaseType(type: import('./int').int): import('./void').void;
    /**
     * Get track speed for TRACK rotation mode (blocks/second).
     * 0 = instant tracking. Comparable to player sprint speed (~5.6 blocks/sec).
     * @return track speed in blocks/second
     */
    getTrackSpeedValue(): import('./float').float;
    /**
     * Set track speed for TRACK rotation mode (blocks/second).
     *
     * @param speed 0 = instant tracking. ~4 = dodgeable while sprinting. ~6 = barely undodgeable.
     */
    setTrackSpeedValue(speed: import('./float').float): import('./void').void;
    /**
     * Check if movement (pathfinding/motion) is locked during the WINDUP phase.
     * @return true if movement is locked during windup
     */
    isMovementLockedDuringWindup(): import('./boolean').boolean;
    /**
     * Check if movement (pathfinding/motion) is locked during the ACTIVE phase.
     * @return true if movement is locked during active phase
     */
    isMovementLockedDuringActive(): import('./boolean').boolean;
    /**
     * Check if rotation (yaw/pitch) is locked during the WINDUP phase.
     * @return true if rotation is locked during windup
     */
    isRotationLockedDuringWindup(): import('./boolean').boolean;
    /**
     * Check if rotation (yaw/pitch) is locked during the ACTIVE phase.
     * @return true if rotation is locked during active phase
     */
    isRotationLockedDuringActive(): import('./boolean').boolean;
    /**
     * Check if this ability is currently being executed.
     * @return true if currently executing
     */
    isExecuting(): import('./boolean').boolean;
    /**
     * Get the current execution phase (0=IDLE, 1=WINDUP, 2=ACTIVE, 3=DAZED, 4=BURST_DELAY).
     * @return the current phase ordinal
     */
    getPhaseInt(): import('./int').int;
    /**
     * Get the current tick within the current phase.
     * @return the current tick within the phase
     */
    getCurrentTick(): import('./int').int;
    /**
     * Get the allowed user type for this ability.
     *
     * @return 0=NPC_ONLY, 1=PLAYER_ONLY, 2=BOTH
     */
    getAllowedByType(): import('./int').int;
    /**
     * Set the allowed user type for this ability.
     *
     * @param type 0=NPC_ONLY, 1=PLAYER_ONLY, 2=BOTH
     */
    setAllowedByType(type: import('./int').int): import('./void').void;
    /**
     * Check if this ability ignores cooldowns.
     * @return true if ignoring cooldowns
     */
    isIgnoreCooldown(): import('./boolean').boolean;
    /**
     * Set whether this ability ignores cooldowns.
     * @param ignore Whether to ignore cooldowns.
     */
    setIgnoreCooldown(ignore: import('./boolean').boolean): import('./void').void;
    /**
     * Check if this ability uses per-ability cooldown instead of global cooldown.
     * When true, using this ability only puts THIS ability on cooldown, not all abilities.
     * @return true if using per-ability cooldown
     */
    isPerAbilityCooldown(): import('./boolean').boolean;
    /**
     * Set whether this ability uses per-ability cooldown.
     * @param perAbility Whether to use per-ability cooldown.
     */
    setPerAbilityCooldown(perAbility: import('./boolean').boolean): import('./void').void;
    /**
     * Get this ability's data as NBT.
     * @return the ability's NBT data
     */
    getNbt(): import('../INbt').INbt;
    /**
     * Set this ability's data from NBT.
     * @param nbt NBT data to load.
     */
    setNbt(nbt: import('../INbt').INbt): import('./void').void;
    /** @return Whether burst firing is enabled (fires multiple instances in rapid succession). */
    isBurstEnabled(): import('./boolean').boolean;
    /** @param enabled Whether burst firing is enabled. */
    setBurstEnabled(enabled: import('./boolean').boolean): import('./void').void;
    /** @return Number of times the ability fires per burst. */
    getBurstAmount(): import('./int').int;
    /** @param amount Number of shots per burst. */
    setBurstAmount(amount: import('./int').int): import('./void').void;
    /** @return Delay in ticks between each burst shot. */
    getBurstDelay(): import('./int').int;
    /** @param delay Delay in ticks between burst shots. */
    setBurstDelay(delay: import('./int').int): import('./void').void;
    /** @return Whether animations replay for each burst shot. */
    isBurstReplayAnimations(): import('./boolean').boolean;
    /** @param replay Whether to replay animations per burst shot. */
    setBurstReplayAnimations(replay: import('./boolean').boolean): import('./void').void;
    /** @return Whether burst shots can overlap (fire before previous shot ends). */
    isBurstOverlap(): import('./boolean').boolean;
    /** @param overlap Whether burst shots can overlap. */
    setBurstOverlap(overlap: import('./boolean').boolean): import('./void').void;
    /**
     * Check if this ability is toggleable (has at least 1 toggle state).
     * @return true if toggleable
     */
    isToggleable(): import('./boolean').boolean;
    /**
     * Get the number of toggle states.
     * 0 = not toggleable, 1 = binary on/off, 2+ = multi-state cycling.
     * @return number of toggle states
     */
    getToggleStates(): import('./int').int;
    /**
     * Set the number of toggle states.
     * 0 = not toggleable, 1 = binary on/off, 2+ = multi-state cycling.
     * @param states Number of toggle states.
     */
    setToggleStates(states: import('./int').int): import('./void').void;
    /**
     * Get the display label for a specific toggle state (1-indexed).
     * @param state Toggle state number (1-indexed).
     * @return the label, or null if none set.
     */
    getToggleStateLabel(state: import('./int').int): String;
    /**
     * Get this ability's magic data. Defines magic types, damage splits,
     * and flat magic damage for this ability. Inherited by spawned energy entities.
     * @return the ability's magic data
     */
    getMagicData(): import('../handler/data/IMagicData').IMagicData;
    onExecute: import('./abstract void').abstract void;
    onActiveTick: import('./abstract void').abstract void;
    writeTypeNBT: import('./abstract void').abstract void;
    readTypeNBT: import('./abstract void').abstract void;
    getDisplayDamage: import('./float').float;
    isDisplayDamageDPS: import('./boolean').boolean;
    getDisplayBarrierHealth: import('./float').float;
    isDisplayReflect: import('./boolean').boolean;
    getDisplayReflectStrength: import('./float').float;
    isDisplayAbsorbing: import('./boolean').boolean;
}
