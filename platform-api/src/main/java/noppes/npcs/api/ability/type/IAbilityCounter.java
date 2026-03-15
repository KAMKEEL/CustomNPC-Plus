package noppes.npcs.api.ability.type;

/**
 * API interface for Counter abilities.
 * Absorbs incoming damage and counter-attacks the attacker.
 */
public interface IAbilityCounter extends IAbilityDefend {

    /** @return Counter-attack type ordinal (determines how counter damage is calculated). */
    int getCounterType();

    /** @param type Counter-attack type ordinal. */
    void setCounterType(int type);

    /** @return Counter-attack value (meaning depends on counter type, e.g. flat damage or multiplier). */
    float getCounterValue();

    /** @param value Counter-attack value (flat damage or multiplier). */
    void setCounterValue(float value);

    /** @return Animation ID played when the counter-attack triggers, or -1 for none. */
    int getCounterAnimationId();

    /** @param animationId Animation ID for the counter-attack, or -1 for none. */
    void setCounterAnimationId(int animationId);
}
