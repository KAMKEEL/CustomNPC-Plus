package kamkeel.npcs.controllers.data.ability.enums;

/**
 * Filter for determining which entities an ability should affect in AOE mode.
 */
public enum TargetFilter {
    ALLIES,
    ENEMIES,
    ALL;

    @Override
    public String toString() {
        switch (this) {
            case ALLIES:
                return "ability.targetFilter.allies";
            case ENEMIES:
                return "ability.targetFilter.enemies";
            case ALL:
                return "ability.targetFilter.all";
            default:
                return name();
        }
    }

    public static TargetFilter fromString(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return ALLIES;
        }
    }
}
