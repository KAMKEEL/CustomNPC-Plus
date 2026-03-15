package noppes.npcs.api.ability.type;

import noppes.npcs.api.ability.IAbility;

/**
 * API interface for defensive abilities (Guard, Counter, Dodge).
 */
public interface IAbilityDefend extends IAbility {

    /** @return Maximum duration of the defensive stance in ticks. */
    int getDurationTicks();

    /** @param ticks Maximum duration in ticks. */
    void setDurationTicks(int ticks);

    /** @return Maximum number of hits that can be absorbed before the defense breaks. */
    int getMaxHitAmount();

    /** @param amount Maximum hit count before breaking. */
    void setMaxHitAmount(int amount);

    /** @return Whether the entity is currently in a defensive stance. */
    boolean isDefending();
}
