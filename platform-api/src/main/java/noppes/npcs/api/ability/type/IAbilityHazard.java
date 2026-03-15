package noppes.npcs.api.ability.type;

/**
 * API interface for Hazard abilities.
 * Persistent ground effect zones placed around the caster.
 */
public interface IAbilityHazard extends IAbilityZone {

    /** @return Damage radius of the hazard zone in blocks. */
    float getRadius();

    /** @param radius Damage radius in blocks. */
    void setRadius(float radius);

    /** @return Damage dealt per damage interval tick. */
    float getDamagePerSecond();

    /** @param damage Damage per interval. */
    void setDamagePerSecond(float damage);

    /** @return Interval in ticks between damage applications. */
    int getDamageInterval();

    /** @param interval Damage interval in ticks. */
    void setDamageInterval(int interval);

    /** @return Whether the hazard also damages the caster. */
    boolean isAffectsCaster();

    /** @param affects Whether the hazard affects the caster. */
    void setAffectsCaster(boolean affects);
}
