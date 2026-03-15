package kamkeel.npcs.controllers.data.ability.conditions;

public enum ConditionFilter {
    CASTER,
    TARGET,
    BOTH;

    public boolean checksCaster() {
        return this == CASTER || this == BOTH;
    }

    public boolean checksTarget() {
        return this == TARGET || this == BOTH;
    }

    public static ConditionFilter fromOrdinal(int ordinal) {
        ConditionFilter[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return BOTH;
    }

    @Override
    public String toString() {
        switch (this) {
            case BOTH:
                return "condition.filter.both";
            case CASTER:
                return "condition.filter.caster";
            case TARGET:
                return "condition.filter.target";
            default:
                return name();
        }
    }
}
