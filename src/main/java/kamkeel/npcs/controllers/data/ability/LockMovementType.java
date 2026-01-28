package kamkeel.npcs.controllers.data.ability;

/**
 * Defines when an ability locks NPC movement and rotation.
 */
public enum LockMovementType {
    /**
     * No movement lock - NPC can move freely during both phases.
     */
    NO,

    /**
     * Lock movement during WINDUP phase only.
     * NPC can move during ACTIVE phase.
     */
    WINDUP,

    /**
     * Lock movement during ACTIVE phase only.
     * NPC can move during WINDUP phase.
     */
    ACTIVE,

    /**
     * Lock movement during both WINDUP and ACTIVE phases.
     */
    WINDUP_AND_ACTIVE;

    /**
     * Check if movement should be locked during WINDUP phase.
     */
    public boolean locksWindup() {
        return this == WINDUP || this == WINDUP_AND_ACTIVE;
    }

    /**
     * Check if movement should be locked during ACTIVE phase.
     */
    public boolean locksActive() {
        return this == ACTIVE || this == WINDUP_AND_ACTIVE;
    }

    /**
     * Get display name for GUI.
     */
    public String getDisplayKey() {
        switch (this) {
            case NO:
                return "gui.no";
            case WINDUP:
                return "ability.lockMove.windup";
            case ACTIVE:
                return "ability.lockMove.active";
            case WINDUP_AND_ACTIVE:
                return "ability.lockMove.both";
            default:
                return "gui.no";
        }
    }

    /**
     * Get all display keys for GUI dropdown.
     */
    public static String[] getDisplayKeys() {
        return new String[]{
            "gui.no",
            "ability.lockMove.windup",
            "ability.lockMove.active",
            "ability.lockMove.both"
        };
    }

    /**
     * Get LockMovementType from ordinal, with fallback to WINDUP.
     */
    public static LockMovementType fromOrdinal(int ordinal) {
        LockMovementType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return WINDUP;
    }
}
