package noppes.npcs.api.ability.type;

/**
 * API interface for Dodge abilities.
 * Cancels the incoming attack event entirely and plays a random dodge animation.
 */
public interface IAbilityDodge extends IAbilityDefend {

    /** @return Animation ID for dodge variant 1, or -1 for none. */
    int getDodgeAnimation1Id();

    /** @param animationId Animation ID, or -1 for none. */
    void setDodgeAnimation1Id(int animationId);

    /** @return Animation ID for dodge variant 2, or -1 for none. */
    int getDodgeAnimation2Id();

    /** @param animationId Animation ID, or -1 for none. */
    void setDodgeAnimation2Id(int animationId);

    /** @return Animation ID for dodge variant 3, or -1 for none. */
    int getDodgeAnimation3Id();

    /** @param animationId Animation ID, or -1 for none. */
    void setDodgeAnimation3Id(int animationId);
}
