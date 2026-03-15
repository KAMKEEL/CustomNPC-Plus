package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for Teleport abilities.
 * Instant repositioning with various modes.
 */
public interface IAbilityTeleport extends IAbility {

    /** @return Teleport mode ordinal (0=BLINK, 1=BEHIND, 2=SINGLE). */
    int getMode();

    /** @param mode Teleport mode ordinal (0=BLINK, 1=BEHIND, 2=SINGLE). */
    void setMode(int mode);

    /** @return Number of rapid blink teleports in BLINK mode. */
    int getBlinkCount();

    /** @param count Number of blink teleports. */
    void setBlinkCount(int count);

    /** @return Delay in ticks between blink teleports. */
    int getBlinkDelayTicks();

    /** @param ticks Delay in ticks between blinks. */
    void setBlinkDelayTicks(int ticks);

    /** @return Maximum random offset radius for blink teleports in blocks. */
    float getBlinkRadius();

    /** @param radius Blink offset radius in blocks. */
    void setBlinkRadius(float radius);

    /** @return Distance behind the target to teleport to in BEHIND mode. */
    float getBehindDistance();

    /** @param distance Behind-target distance in blocks. */
    void setBehindDistance(float distance);

    /** @return Whether the teleport requires clear line of sight to the destination. */
    boolean isRequireLineOfSight();

    /** @param require Whether line of sight is required. */
    void setRequireLineOfSight(boolean require);

    /** @return Whether damage is dealt at the departure location. */
    boolean isDamageAtStart();

    /** @param damage Whether to deal damage at departure. */
    void setDamageAtStart(boolean damage);

    /** @return Whether damage is dealt at the arrival location. */
    boolean isDamageAtEnd();

    /** @param damage Whether to deal damage at arrival. */
    void setDamageAtEnd(boolean damage);

    /** @return Damage dealt at teleport locations. */
    float getDamage();

    /** @param damage Teleport damage amount. */
    void setDamage(float damage);

    /** @return Radius of the damage area at teleport locations in blocks. */
    float getDamageRadius();

    /** @param radius Damage area radius in blocks. */
    void setDamageRadius(float radius);
}
