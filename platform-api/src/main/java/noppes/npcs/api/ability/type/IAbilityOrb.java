package noppes.npcs.api.ability.type;

/**
 * API interface for Orb abilities.
 * Homing projectile sphere.
 */
public interface IAbilityOrb extends IAbilityEnergyProjectile {

    /** @return Travel speed of the orb in blocks per tick. */
    float getOrbSpeed();

    /** @param speed Travel speed in blocks per tick. */
    void setOrbSpeed(float speed);

    /** @return Visual size (radius) of the orb in blocks. */
    float getOrbSize();

    /** @param size Orb size in blocks. */
    void setOrbSize(float size);

    /** @return Whether the orb tracks its target. */
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

    /** @return Visual rotation speed of the orb in degrees per tick. */
    float getRotationSpeed();

    /** @param speed Rotation speed in degrees per tick. */
    void setRotationSpeed(float speed);

    /** @return Number of orb projectiles fired per use. */
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
