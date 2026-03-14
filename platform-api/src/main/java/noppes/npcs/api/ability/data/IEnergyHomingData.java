package noppes.npcs.api.ability.data;

/**
 * Homing and speed properties for energy ability projectiles.
 * Controls projectile velocity and target-seeking behavior.
 */
public interface IEnergyHomingData {
    /** @return Projectile travel speed in blocks per tick. */
    float getSpeed();

    /** @param speed Travel speed in blocks per tick. */
    void setSpeed(float speed);

    /** @return Whether the projectile tracks its target. */
    boolean isHoming();

    /** @param homing Whether to track the target. */
    void setHoming(boolean homing);

    /** @return Homing turn strength (higher = tighter turns). */
    float getHomingStrength();

    /** @param homingStrength Homing turn strength. */
    void setHomingStrength(float homingStrength);

    /** @return Maximum distance at which homing activates, in blocks. */
    float getHomingRange();

    /** @param homingRange Maximum homing activation range in blocks. */
    void setHomingRange(float homingRange);
}
