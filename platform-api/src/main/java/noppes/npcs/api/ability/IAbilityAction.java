package noppes.npcs.api.ability;

/**
 * Common API interface for all combat actions (abilities and chained abilities).
 * Both {@link IAbility} and {@link IChainedAbility} extend this interface.
 */
public interface IAbilityAction {

    /** @return The unique identifier/name of this action. */
    String getName();

    /** @return Whether this action is enabled. */
    boolean isEnabled();

    /** @return Selection weight for random ability selection (higher = more likely). */
    int getWeight();

    /** @return Cooldown duration in ticks after this action is used. */
    int getCooldownTicks();

    /** @return Minimum range in blocks at which this action can be used. */
    float getMinRange();

    /** @return Maximum range in blocks at which this action can be used. */
    float getMaxRange();

    /** @return Whether this action is a chained ability (sequence) vs an individual ability. */
    boolean isChain();
}
