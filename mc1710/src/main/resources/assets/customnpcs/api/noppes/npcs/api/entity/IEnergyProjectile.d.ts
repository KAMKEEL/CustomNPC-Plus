/**
 * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10
 * Package: noppes.npcs.api.entity
 */

/**
 * Base interface for all energy projectile entities (Orb, Beam, Disc, Laser, Slicer).
 * <p>
 * Extends {@link IEnergyAbility} for shared display, lightning, owner, and charging methods.
 * <p>
 * <b>Fire methods — launch from current position (no repositioning):</b>
 * <ul>
 *   <li>{@link #fireAt(IEntity)} — toward an entity</li>
 *   <li>{@link #fireAt(double, double, double)} — toward world coordinates</li>
 *   <li>{@link #fireDirection(float, float)} — in a yaw/pitch direction</li>
 * </ul>
 * <b>FireFrom methods — reposition to caster's eye, then launch (look-vector snap):</b>
 * <ul>
 *   <li>{@link #fireFrom(IEntityLivingBase)} — fire along caster's look direction</li>
 *   <li>{@link #fireFrom(IEntityLivingBase, IEntity)} — fire toward a target entity</li>
 * </ul>
  * @javaFqn noppes.npcs.api.entity.IEnergyProjectile
*/
export interface IEnergyProjectile<T extends Entity /* net.minecraft.entity.Entity */> extends import('./IEnergyAbility').IEnergyAbility {
    /**
     * Sets the owner entity (used for homing return, damage attribution, etc.).
     * @param owner the entity that owns this projectile
     */
    setOwner(owner: import('./IEntity').IEntity): import('./void').void;
    /** @return the entity ID of the current homing/tracking target, or -1 if none */
    getTargetEntityId(): import('./int').int;
    /** @return the current homing/tracking target entity, or null if none */
    getTarget(): import('./IEntity').IEntity;
    /**
     * Sets the homing/tracking target entity. Pass null to clear.
     * @param target the entity to home towards
     */
    setTarget(target: import('./IEntity').IEntity): import('./void').void;
    /** @return the projectile's visual and collision size */
    getSize(): import('./float').float;
    /**
     * Sets the projectile's visual and collision size.
     * @param size the projectile size
     */
    setSize(size: import('./float').float): import('./void').void;
    /** @return the visual rotation speed (degrees per tick) */
    getRotationSpeed(): import('./float').float;
    setRotationSpeed(speed: import('./float').float): import('./void').void;
    getInterpolatedRotationX(partialTicks: import('./float').float): import('./float').float;
    getInterpolatedRotationY(partialTicks: import('./float').float): import('./float').float;
    getInterpolatedRotationZ(partialTicks: import('./float').float): import('./float').float;
    getInterpolatedSize(partialTicks: import('./float').float): import('./float').float;
    /** @return maximum travel distance before the projectile despawns (0 = unlimited) */
    getMaxDistance(): import('./float').float;
    setMaxDistance(distance: import('./float').float): import('./void').void;
    /** @return maximum lifetime in ticks before the projectile despawns (0 = unlimited) */
    getMaxLifetime(): import('./int').int;
    setMaxLifetime(ticks: import('./int').int): import('./void').void;
    /** @return base damage dealt on hit */
    getDamage(): import('./float').float;
    setDamage(damage: import('./float').float): import('./void').void;
    /** @return horizontal knockback strength */
    getKnockback(): import('./float').float;
    setKnockback(knockback: import('./float').float): import('./void').void;
    /** @return upward knockback strength */
    getKnockbackUp(): import('./float').float;
    setKnockbackUp(knockbackUp: import('./float').float): import('./void').void;
    /** @return true if the projectile explodes on impact */
    isExplosive(): import('./boolean').boolean;
    setExplosive(explosive: import('./boolean').boolean): import('./void').void;
    /** @return explosion radius (only used if {@link #isExplosive()} is true) */
    getExplosionRadius(): import('./float').float;
    setExplosionRadius(radius: import('./float').float): import('./void').void;
    /** @return damage falloff multiplier over explosion radius (0 = no falloff, 1 = full falloff) */
    getExplosionDamageFalloff(): import('./float').float;
    setExplosionDamageFalloff(falloff: import('./float').float): import('./void').void;
    /** @return hit behavior type (0 = single hit, 1 = pierce, 2 = multi-hit with delay) */
    getHitType(): import('./int').int;
    setHitType(hitType: import('./int').int): import('./void').void;
    /** @return ticks between multi-hit damage ticks on the same entity */
    getMultiHitDelayTicks(): import('./int').int;
    setMultiHitDelayTicks(delayTicks: import('./int').int): import('./void').void;
    /** @return maximum number of entities this projectile can hit (0 = unlimited) */
    getMaxHits(): import('./int').int;
    setMaxHits(maxHits: import('./int').int): import('./void').void;
    /** @return travel speed (blocks per tick) */
    getSpeed(): import('./float').float;
    setSpeed(speed: import('./float').float): import('./void').void;
    /** @return true if the projectile homes toward its target */
    isHoming(): import('./boolean').boolean;
    setHoming(homing: import('./boolean').boolean): import('./void').void;
    /** @return how aggressively the projectile turns toward its target (higher = tighter turns) */
    getHomingStrength(): import('./float').float;
    setHomingStrength(strength: import('./float').float): import('./void').void;
    /** @return maximum range at which homing will acquire/track a target */
    getHomingRange(): import('./float').float;
    setHomingRange(range: import('./float').float): import('./void').void;
    /** @return anchor point type used during the charge/windup phase */
    getAnchor(): import('./int').int;
    getAnchorOffsetX(): import('./float').float;
    getAnchorOffsetY(): import('./float').float;
    getAnchorOffsetZ(): import('./float').float;
    /** @return X coordinate where the projectile was launched from */
    getStartX(): import('./double').double;
    /** @return Y coordinate where the projectile was launched from */
    getStartY(): import('./double').double;
    /** @return Z coordinate where the projectile was launched from */
    getStartZ(): import('./double').double;
    /** @return true if the projectile has hit something */
    hasHit(): import('./boolean').boolean;
    /**
     * Returns the energy projectile sub-type.
     * @return 0=Orb, 1=Beam, 2=Disc, 3=Laser, 4=Slicer
     */
    getEnergyType(): import('./int').int;
    /**
     * Sends all current visual, movement, and position data to tracking clients.
     * <p>
     * Call this <b>after</b> making batch changes to properties like size, speed,
     * homing, colors, or type-specific fields (beam width, disc radius, etc.).
     * This avoids sending a separate packet for every setter call.
     * <p>
     * <b>Note:</b> {@code fireAt()}, {@code fireDirection()}, and {@code fireFrom()}
     * automatically sync motion when called on an already-spawned projectile,
     * so you do not need to call {@code syncClient()} after firing.
     * Use this method when changing properties that affect rendering or movement
     * behavior without re-firing.
     * <p>
     * Example:
     * <pre>
     * orb.setSize(2.0);
     * orb.setSpeed(0.5);
     * orb.setInnerColor(0xFF0000);
     * orb.syncClient(); // one packet for all changes
     * </pre>
     */
    syncClient(): import('./void').void;
    /**
     * Spawns the projectile and launches it from its <b>current position</b> toward the target entity.
     * <p>
     * No look-vector repositioning occurs — the projectile fires from exactly where it was
     * placed by {@code createOrb}/{@code createBeam}/etc. If the target is a living entity,
     * it is also set as the homing target for projectiles with homing enabled.
     * <p>
     * To launch from the owner's eye position with look-vector snapping, use
     * {@link #fireFrom(IEntityLivingBase, IEntity)} instead.
     *
     * @param target the entity to fire toward (can be null for untargeted launch)
     */
    fireAt(target: import('./IEntity').IEntity): import('./void').void;
    /**
     * Spawns the projectile and launches it toward the specified world coordinates.
     * <p>
     * The projectile fires from its <b>current position</b> — no look-vector repositioning occurs.
     * Motion is calculated as a normalized direction vector scaled by the projectile's speed.
     *
     * @param x target X coordinate
     * @param y target Y coordinate
     * @param z target Z coordinate
     */
    fireAt(x: import('./double').double, y: import('./double').double, z: import('./double').double): import('./void').void;
    /**
     * Spawns the projectile and launches it in the specified yaw/pitch direction.
     * <p>
     * The projectile fires from its <b>current position</b> — no look-vector repositioning occurs.
     *
     * @param yaw   horizontal angle in degrees (0 = south, 90 = west, like Minecraft yaw)
     * @param pitch vertical angle in degrees (negative = up, positive = down)
     */
    fireDirection(yaw: import('./float').float, pitch: import('./float').float): import('./void').void;
    /**
     * Sets the owner, positions the projectile at the caster's eye level, and launches it
     * using the entity's full launch sequence ({@code startMoving}/{@code startFiring}).
     * <p>
     * For player-owned projectiles, this <b>snaps the projectile to the owner's look-vector
     * position</b> and fires in the caster's look direction. This is the intended method for
     * "cast from self" behavior. The projectile's position set by {@code createOrb}/etc. is
     * overridden.
     * <p>
     * Use {@link #fireAt(IEntity)} or {@link #fireAt(double, double, double)} instead if you
     * want to launch from a specific custom position.
     *
     * @param caster the entity to fire from
     */
    fireFrom(caster: import('./IEntityLivingBase').IEntityLivingBase): import('./void').void;
    /**
     * Sets the owner, positions the projectile at the caster's eye level, and launches it
     * toward the target entity using the entity's full launch sequence.
     * <p>
     * For player-owned projectiles, this <b>snaps the projectile to the owner's look-vector
     * position</b> before firing toward the target. The projectile's position set by
     * {@code createOrb}/etc. is overridden.
     * <p>
     * Use {@link #fireAt(IEntity)} instead if you want to launch from a specific custom position.
     *
     * @param caster the entity to fire from
     * @param target the entity to fire toward
     */
    fireFrom(caster: import('./IEntityLivingBase').IEntityLivingBase, target: import('./IEntity').IEntity): import('./void').void;
}
