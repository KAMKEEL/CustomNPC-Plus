package noppes.npcs.api.entity;


/**
 * Entity-level interface for sweeper beam entities.
 * Created via IEnergyHandler.createSweeper().
 * Configure properties, then call spawn() to place in the world.
 */
public interface IEnergySweeper extends IEnergyAbility {

    // ==================== BEAM DIMENSIONS ====================

    /** @return Length of the sweeping beam in blocks. */
    float getBeamLength();
    /** @param length Beam length in blocks. */
    void setBeamLength(float length);

    /** @return Width of the sweeping beam in blocks. */
    float getBeamWidth();
    /** @param width Beam width in blocks. */
    void setBeamWidth(float width);

    /** @return Height of the sweeping beam in blocks. */
    float getBeamHeight();
    /** @param height Beam height in blocks. */
    void setBeamHeight(float height);

    // ==================== ROTATION ====================

    /** @return Rotation speed of the sweep in degrees per tick. */
    float getSweepSpeed();
    /** @param degreesPerTick Sweep rotation speed in degrees per tick. */
    void setSweepSpeed(float degreesPerTick);

    /** @return Number of full rotations the beam completes before stopping. */
    int getNumberOfRotations();
    /** @param rotations Number of full rotations. */
    void setNumberOfRotations(int rotations);

    /** @return Whether the beam pivot tracks the target instead of rotating freely. */
    boolean isLockOnTarget();
    /** @param lock Whether the beam locks on to the target. */
    void setLockOnTarget(boolean lock);

    // ==================== DAMAGE ====================

    /** @return Damage dealt per hit to entities caught in the beam. */
    float getDamage();
    /** @param damage Damage per hit. */
    void setDamage(float damage);

    /** @return Interval in ticks between damage applications to the same target. */
    int getDamageInterval();
    /** @param ticks Damage interval in ticks. */
    void setDamageInterval(int ticks);

    /** @return Whether the beam can hit through multiple targets. */
    boolean isPiercing();
    /** @param piercing Whether the beam pierces through targets. */
    void setPiercing(boolean piercing);

    // ==================== SPAWNING ====================

    /** Spawn this sweeper entity into the world. */
    void spawn();
}
