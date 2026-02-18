package kamkeel.npcs.controllers.data.ability;

/**
 * Defines how an ability selects its target(s).
 */
public enum TargetingMode {
    /**
     * Target the current aggro target
     */
    AGGRO_TARGET,

    /**
     * Target self (for buffs, heals, guards)
     */
    SELF,

    /**
     * AOE centered on self
     */
    AOE_SELF,

    /**
     * AOE centered on target
     */
    AOE_TARGET;

    @Override
    public String toString() {
        switch (this) {
            case AGGRO_TARGET:
                return "ability.target.aggro_target";
            case SELF:
                return "ability.target.self";
            case AOE_SELF:
                return "ability.target.aoe_self";
            case AOE_TARGET:
                return "ability.target.aoe_target";
            default:
                return name();
        }
    }
}
