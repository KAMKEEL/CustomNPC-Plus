package noppes.npcs.api.entity;


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
 */
public interface IEnergyProjectile extends IEnergyAbility {

    // ==================== OWNER & TARGET ====================

    /**
     * Sets the owner entity (used for homing return, damage attribution, etc.).
     * @param owner the entity that owns this projectile
     */
    void setOwner(IEntity owner);

    /** @return the entity ID of the current homing/tracking target, or -1 if none */
    int getTargetEntityId();

    /** @return the current homing/tracking target entity, or null if none */
    IEntity getTarget();

    /**
     * Sets the homing/tracking target entity. Pass null to clear.
     * @param target the entity to home towards
     */
    void setTarget(IEntity target);

    // ==================== SIZE ====================

    /** @return the projectile's visual and collision size */
    float getSize();

    /**
     * Sets the projectile's visual and collision size.
     * @param size the projectile size
     */
    void setSize(float size);

    // ==================== DISPLAY (PROJECTILE-SPECIFIC) ====================

    /** @return the visual rotation speed (degrees per tick) */
    float getRotationSpeed();

    void setRotationSpeed(float speed);

    // ==================== INTERPOLATION (READ-ONLY) ====================

    float getInterpolatedRotationX(float partialTicks);

    float getInterpolatedRotationY(float partialTicks);

    float getInterpolatedRotationZ(float partialTicks);

    float getInterpolatedSize(float partialTicks);

    // ==================== LIFESPAN ====================

    /** @return maximum travel distance before the projectile despawns (0 = unlimited) */
    float getMaxDistance();

    void setMaxDistance(float distance);

    /** @return maximum lifetime in ticks before the projectile despawns (0 = unlimited) */
    int getMaxLifetime();

    void setMaxLifetime(int ticks);

    // ==================== COMBAT ====================

    /** @return base damage dealt on hit */
    float getDamage();

    void setDamage(float damage);

    /** @return horizontal knockback strength */
    float getKnockback();

    void setKnockback(float knockback);

    /** @return upward knockback strength */
    float getKnockbackUp();

    void setKnockbackUp(float knockbackUp);

    /** @return true if the projectile explodes on impact */
    boolean isExplosive();

    void setExplosive(boolean explosive);

    /** @return explosion radius (only used if {@link #isExplosive()} is true) */
    float getExplosionRadius();

    void setExplosionRadius(float radius);

    /** @return damage falloff multiplier over explosion radius (0 = no falloff, 1 = full falloff) */
    float getExplosionDamageFalloff();

    void setExplosionDamageFalloff(float falloff);

    /** @return hit behavior type (0 = single hit, 1 = pierce, 2 = multi-hit with delay) */
    int getHitType();

    void setHitType(int hitType);

    /** @return ticks between multi-hit damage ticks on the same entity */
    int getMultiHitDelayTicks();

    void setMultiHitDelayTicks(int delayTicks);

    /** @return maximum number of entities this projectile can hit (0 = unlimited) */
    int getMaxHits();

    void setMaxHits(int maxHits);

    // ==================== MOVEMENT ====================

    /** @return travel speed (blocks per tick) */
    float getSpeed();

    void setSpeed(float speed);

    /** @return true if the projectile homes toward its target */
    boolean isHoming();

    void setHoming(boolean homing);

    /** @return how aggressively the projectile turns toward its target (higher = tighter turns) */
    float getHomingStrength();

    void setHomingStrength(float strength);

    /** @return maximum range at which homing will acquire/track a target */
    float getHomingRange();

    void setHomingRange(float range);

    // ==================== ANCHOR ====================

    /** @return anchor point type used during the charge/windup phase */
    int getAnchor();

    float getAnchorOffsetX();

    float getAnchorOffsetY();

    float getAnchorOffsetZ();

    // ==================== POSITION ====================

    /** @return X coordinate where the projectile was launched from */
    double getStartX();

    /** @return Y coordinate where the projectile was launched from */
    double getStartY();

    /** @return Z coordinate where the projectile was launched from */
    double getStartZ();

    // ==================== STATE ====================

    /** @return true if the projectile has hit something */
    boolean hasHit();

    /**
     * Returns the energy projectile sub-type.
     * @return 0=Orb, 1=Beam, 2=Disc, 3=Laser, 4=Slicer
     */
    int getEnergyType();

    // ==================== SYNC ====================

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
    void syncClient();

    // ==================== FIRE / LAUNCH ====================

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
    void fireAt(IEntity target);

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
    void fireAt(double x, double y, double z);

    /**
     * Spawns the projectile and launches it in the specified yaw/pitch direction.
     * <p>
     * The projectile fires from its <b>current position</b> — no look-vector repositioning occurs.
     *
     * @param yaw   horizontal angle in degrees (0 = south, 90 = west, like Minecraft yaw)
     * @param pitch vertical angle in degrees (negative = up, positive = down)
     */
    void fireDirection(float yaw, float pitch);

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
    void fireFrom(IEntityLivingBase caster);

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
    void fireFrom(IEntityLivingBase caster, IEntity target);

}
