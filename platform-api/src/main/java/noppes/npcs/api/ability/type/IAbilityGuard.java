package noppes.npcs.api.ability.type;

/**
 * API interface for Guard abilities.
 * Defensive stance that reduces incoming damage.
 */
public interface IAbilityGuard extends IAbilityDefend {

    /** @return Damage reduction multiplier while guarding (0.0 = full block, 1.0 = no reduction). */
    float getDamageReduction();

    /** @param reduction Damage reduction multiplier (0.0 = full block, 1.0 = no reduction). */
    void setDamageReduction(float reduction);
}
