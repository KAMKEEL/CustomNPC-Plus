package noppes.npcs.api.ability.type;

/**
 * API interface for Disc abilities.
 * Flat spinning disc projectile with optional boomerang.
 */
public interface IAbilityDisc extends IAbilityEnergyProjectile {

    /** @return Travel speed of the disc in blocks per tick. */
    float getSpeed();

    /** @param speed Travel speed in blocks per tick. */
    void setSpeed(float speed);

    /** @return Visual radius of the disc in blocks. */
    float getDiscRadius();

    /** @param radius Disc radius in blocks. */
    void setDiscRadius(float radius);

    /** @return Thickness of the disc in blocks. */
    float getDiscThickness();

    /** @param thickness Disc thickness in blocks. */
    void setDiscThickness(float thickness);

    /** @return Whether the disc spins on a vertical axis (blade-like). */
    boolean isVertical();

    /** @param vertical Whether the disc spins vertically. */
    void setVertical(boolean vertical);

    /** @return Whether the disc returns to the caster after reaching max distance. */
    boolean isBoomerang();

    /** @param boomerang Whether the disc returns to the caster. */
    void setBoomerang(boolean boomerang);

    /** @return Delay in ticks before the disc begins returning. */
    int getBoomerangDelay();

    /** @param ticks Boomerang return delay in ticks. */
    void setBoomerangDelay(int ticks);

    /** @return Whether the disc tracks its target. */
    boolean isHoming();

    /** @param homing Whether to track the target. */
    void setHoming(boolean homing);

    /** @return Homing turn strength (higher = tighter turns). */
    float getHomingStrength();

    /** @param strength Homing turn strength. */
    void setHomingStrength(float strength);

    /** @return Maximum distance at which homing activates, in blocks. */
    float getHomingRange();

    /** @param range Maximum homing activation range in blocks. */
    void setHomingRange(float range);

    /** @return Visual rotation speed of the disc in degrees per tick. */
    float getRotationSpeed();

    /** @param speed Rotation speed in degrees per tick. */
    void setRotationSpeed(float speed);

    /** @return Number of disc projectiles fired per use. */
    int getProjectileCount();

    /** @param count Number of projectiles per use. */
    void setProjectileCount(int count);

    /** @return Delay in ticks between each projectile in a multi-projectile volley. */
    int getFireDelay();

    /** @param delay Delay in ticks between projectiles. */
    void setFireDelay(int delay);

    // Indexed accessors (per-projectile overrides)

    /**
     * @param projectileIndex Projectile index.
     * @return Inner color as a packed RGB integer.
     */
    int getInnerColor(int projectileIndex);

    /**
     * @param projectileIndex Projectile index.
     * @param color Inner color as a packed RGB integer.
     */
    void setInnerColor(int projectileIndex, int color);

    /**
     * @param projectileIndex Projectile index.
     * @return Outer color as a packed RGB integer.
     */
    int getOuterColor(int projectileIndex);

    /**
     * @param projectileIndex Projectile index.
     * @param color Outer color as a packed RGB integer.
     */
    void setOuterColor(int projectileIndex, int color);

    /**
     * @param projectileIndex Projectile index.
     * @return Anchor point ordinal.
     */
    int getAnchorPoint(int projectileIndex);

    /**
     * @param projectileIndex Projectile index.
     * @param point Anchor point ordinal.
     */
    void setAnchorPoint(int projectileIndex, int point);
}
