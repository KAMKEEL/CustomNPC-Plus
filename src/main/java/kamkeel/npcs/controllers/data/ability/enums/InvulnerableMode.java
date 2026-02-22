package kamkeel.npcs.controllers.data.ability.enums;

/**
 * Defines when a caster is invulnerable while an ability is executing.
 */
public enum InvulnerableMode {
    NONE,
    WINDUP,
    ACTIVE,
    BOTH;

    public boolean invulnerableDuringWindup() {
        return this == WINDUP || this == BOTH;
    }

    public boolean invulnerableDuringActive() {
        return this == ACTIVE || this == BOTH;
    }

    /**
     * Treat BURST_DELAY as windup-like for interruption/damage immunity checks.
     */
    public boolean isInvulnerableInPhase(AbilityPhase phase) {
        if (phase == null) return false;
        switch (phase) {
            case WINDUP:
            case BURST_DELAY:
                return invulnerableDuringWindup();
            case ACTIVE:
                return invulnerableDuringActive();
            default:
                return false;
        }
    }

    public String getDisplayKey() {
        switch (this) {
            case WINDUP:
                return "ability.invulnerable.windup";
            case ACTIVE:
                return "ability.invulnerable.active";
            case BOTH:
                return "ability.invulnerable.both";
            case NONE:
            default:
                return "ability.invulnerable.none";
        }
    }

    public static String[] getDisplayKeys() {
        return new String[]{
            "ability.invulnerable.none",
            "ability.invulnerable.windup",
            "ability.invulnerable.active",
            "ability.invulnerable.both"
        };
    }

    public static InvulnerableMode fromOrdinal(int ordinal) {
        InvulnerableMode[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return NONE;
    }
}
