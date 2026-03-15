package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Sweeper abilities.
 * Low sweeping beam that rotates around the NPC.
 */
public interface IAbilitySweeper extends IAbility {

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

    /** @return Damage dealt per hit to entities caught in the beam. */
    float getDamage();

    /** @param damage Damage per hit. */
    void setDamage(float damage);

    /** @return Interval in ticks between damage applications to the same target. */
    int getDamageInterval();

    /** @param interval Damage interval in ticks. */
    void setDamageInterval(int interval);

    /** @return Whether the beam can hit through multiple targets. */
    boolean isPiercing();

    /** @param piercing Whether the beam pierces through targets. */
    void setPiercing(boolean piercing);

    /** @return Rotation speed of the sweep in degrees per tick. */
    float getSweepSpeed();

    /** @param speed Sweep rotation speed in degrees per tick. */
    void setSweepSpeed(float speed);

    /** @return Number of full rotations the beam completes before stopping. */
    int getNumberOfRotations();

    /** @param rotations Number of full rotations. */
    void setNumberOfRotations(int rotations);

    /** @return Whether the beam pivot tracks the target instead of rotating freely. */
    boolean isLockOnTarget();

    /** @param lock Whether the beam locks on to the target. */
    void setLockOnTarget(boolean lock);

    /** @return Inner (core) color of the beam as a packed RGB integer. */
    int getInnerColor();

    /** @param color Inner color as a packed RGB integer. */
    void setInnerColor(int color);

    /** @return Outer (glow) color of the beam as a packed RGB integer. */
    int getOuterColor();

    /** @param color Outer color as a packed RGB integer. */
    void setOuterColor(int color);

    /** @return Width of the outer color layer relative to the beam width. */
    float getOuterColorWidth();

    /** @param width Outer color layer width. */
    void setOuterColorWidth(float width);

    /** @return Whether the outer color layer is rendered. */
    boolean isOuterColorEnabled();

    /** @param enabled Whether to render the outer color layer. */
    void setOuterColorEnabled(boolean enabled);
}
