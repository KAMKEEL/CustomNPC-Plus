package kamkeel.npcs.controllers.data.ability;

/**
 * Defines how an ability selects its target(s).
 */
public enum TargetingMode {
    /** Target the current aggro target */
    AGGRO_TARGET,

    /** Target self (for buffs, heals, guards) */
    SELF,

    /** AOE centered on self */
    AOE_SELF,

    /** AOE centered on target */
    AOE_TARGET,

    /** Random nearby enemy */
    RANDOM_ENEMY,

    /** Lowest HP enemy in range */
    LOWEST_HP,

    /** Highest threat enemy */
    HIGHEST_THREAT
}
