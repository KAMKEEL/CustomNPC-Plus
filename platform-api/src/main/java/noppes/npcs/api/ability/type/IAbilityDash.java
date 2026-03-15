package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Dash abilities.
 * Quick evasive sidestep movement.
 */
public interface IAbilityDash extends IAbility {

    /** @return Dash mode ordinal (0=AGGRESSIVE, 1=DEFENSIVE, 2=DIRECTIONAL). */
    int getDashMode();

    /** @param mode Dash mode ordinal (0=AGGRESSIVE, 1=DEFENSIVE, 2=DIRECTIONAL). */
    void setDashMode(int mode);

    /** @return Distance covered by the dash in blocks. */
    float getDashDistance();

    /** @param distance Dash distance in blocks. */
    void setDashDistance(float distance);

    /** @return Speed of the dash movement. */
    float getDashSpeed();

    /** @param speed Dash movement speed. */
    void setDashSpeed(float speed);

    /** @return Angle offset of the dash direction in degrees. */
    float getDashAngle();

    /** @param dashAngle Direction angle offset in degrees. */
    void setDashAngle(float dashAngle);

    /** @return Dash direction ordinal (determines lateral movement direction). */
    int getDashDirection();

    /** @param mode Dash direction ordinal. */
    void setDashDirection(int mode);
}
