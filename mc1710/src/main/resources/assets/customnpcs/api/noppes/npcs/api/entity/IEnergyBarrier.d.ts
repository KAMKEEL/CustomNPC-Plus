/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Base interface for energy barrier entities (Dome, Panel).
 * Barriers are defensive structures that block incoming energy projectiles.
 * Extends IEnergyAbility for shared display/lightning/owner/charging methods.
  * @javaFqn noppes.npcs.api.entity.IEnergyBarrier
*/
export interface IEnergyBarrier<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyAbility').IEnergyAbility {
    getCurrentHealth(): import('./float').float;
    setCurrentHealth(health: import('./float').float): import('./void').void;
    getHealthPercent(): import('./float').float;
    getMaxHealth(): import('./float').float;
    setMaxHealth(maxHealth: import('./float').float): import('./void').void;
    isUseHealth(): import('./boolean').boolean;
    setUseHealth(useHealth: import('./boolean').boolean): import('./void').void;
    getDuration(): import('./int').int;
    setDuration(ticks: import('./int').int): import('./void').void;
    isUseDuration(): import('./boolean').boolean;
    setUseDuration(useDuration: import('./boolean').boolean): import('./void').void;
    getTicksAlive(): import('./int').int;
    getDefaultMultiplier(): import('./float').float;
    setDefaultMultiplier(multiplier: import('./float').float): import('./void').void;
    isSolid(): import('./boolean').boolean;
    setSolid(solid: import('./boolean').boolean): import('./void').void;
    isKnockbackEnabled(): import('./boolean').boolean;
    setKnockbackEnabled(enabled: import('./boolean').boolean): import('./void').void;
    getKnockbackStrength(): import('./float').float;
    setKnockbackStrength(strength: import('./float').float): import('./void').void;
    /**
     * Returns whether this barrier absorbs damage on behalf of its owner.
     * When absorbing is enabled, incoming damage to the barrier's owner is redirected
     * to the barrier instead, subject to the absorb radius check.
     *
     * @return {@code true} if the barrier absorbs the owner's incoming damage
     * @see #getAbsorbRadius()
     */
    isAbsorbing(): import('./boolean').boolean;
    /**
     * Sets whether this barrier absorbs damage on behalf of its owner.
     * When absorbing is enabled, incoming damage to the barrier's owner is redirected
     * to the barrier instead, subject to the absorb radius check.
     *
     * @param absorbing {@code true} to enable damage absorption for the owner
     * @see #setAbsorbRadius(float)
     */
    setAbsorbing(absorbing: import('./boolean').boolean): import('./void').void;
    /**
     * Gets the absorb radius for this barrier.
     * The absorb radius determines the maximum distance from the barrier's current
     * position at which the owner's incoming damage will still be redirected to the barrier.
     * This check uses the barrier entity's live position, so it works correctly with
     * moving barriers (following domes, moving walls, shields, etc.).
     *
     * <ul>
     *   <li>{@code -1} = No distance limit. Damage is always absorbed regardless of distance.</li>
     *   <li>{@code 0} = Uses the barrier's own geometric extent as the effective radius.
     *       <ul>
     *         <li>Domes: the dome radius (sphere radius in blocks).</li>
     *         <li>Panels: {@code (max(width, height) * 0.5 + 1.0) * 3}
     *             (i.e. {@code max(width, height) * 1.5 + 3.0} blocks).</li>
     *       </ul>
     *   </li>
     *   <li>Positive values = The owner must be within this many blocks of the barrier entity.</li>
     * </ul>
     *
     * @return the absorb radius in blocks, or {@code -1} for unlimited, or {@code 0} for barrier extent
     */
    getAbsorbRadius(): import('./float').float;
    /**
     * Sets the absorb radius for this barrier.
     * The absorb radius determines the maximum distance from the barrier's current
     * position at which the owner's incoming damage will still be redirected to the barrier.
     * This check uses the barrier entity's live position, so it works correctly with
     * moving barriers (following domes, moving walls, shields, etc.).
     *
     * <ul>
     *   <li>{@code -1} = No distance limit. Damage is always absorbed regardless of distance.</li>
     *   <li>{@code 0} = Uses the barrier's own geometric extent as the effective radius.
     *       <ul>
     *         <li>Domes: the dome radius (sphere radius in blocks).</li>
     *         <li>Panels: {@code (max(width, height) * 0.5 + 1.0) * 3}
     *             (i.e. {@code max(width, height) * 1.5 + 3.0} blocks).</li>
     *       </ul>
     *   </li>
     *   <li>Positive values = The owner must be within this many blocks of the barrier entity.</li>
     * </ul>
     *
     * @param radius the absorb radius in blocks, or {@code -1} for unlimited, or {@code 0} for barrier extent
     */
    setAbsorbRadius(radius: import('./float').float): import('./void').void;
    /**
     * Returns the barrier type: 0=Dome, 1=Panel
     * @return the barrier type ordinal
     */
    getBarrierType(): import('./int').int;
    /**
     * Sends all current visual and barrier-specific data to tracking clients.
     * <p>
     * Call this <b>after</b> making batch changes to properties like colors, alpha,
     * lightning, dome radius, panel dimensions, etc. This sends a single packet
     * instead of one per setter call.
     * <p>
     * Example:
     * <pre>
     * dome.setInnerColor(0xFF0000);
     * dome.setDomeRadius(5.0);
     * dome.syncClient(); // one packet for all changes
     * </pre>
     */
    syncClient(): import('./void').void;
}
