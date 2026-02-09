package kamkeel.npcs.controllers.data.ability;

/**
 * Defines what an ability locks when Lock Position is enabled.
 */
public enum LockType {
    MOVEMENT,
    ROTATION,
    BOTH;

    public boolean locksMovement() {
        return this == MOVEMENT || this == BOTH;
    }

    public boolean locksRotation() {
        return this == ROTATION || this == BOTH;
    }

    public String getDisplayKey() {
        switch (this) {
            case MOVEMENT:
                return "ability.lockType.movement";
            case ROTATION:
                return "ability.lockType.rotation";
            case BOTH:
                return "ability.lockType.both";
            default:
                return "ability.lockType.both";
        }
    }

    public static String[] getDisplayKeys() {
        return new String[]{
            "ability.lockType.movement",
            "ability.lockType.rotation",
            "ability.lockType.both"
        };
    }

    public static LockType fromOrdinal(int ordinal) {
        LockType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return BOTH;
    }
}
