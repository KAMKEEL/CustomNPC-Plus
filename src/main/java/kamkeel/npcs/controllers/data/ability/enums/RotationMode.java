package kamkeel.npcs.controllers.data.ability.enums;

/**
 * Defines what happens to NPC rotation during ability phases.
 * Used with a LockMode to control WHEN it applies.
 */
public enum RotationMode {
    /**
     * NPC rotates normally via AI look helper.
     */
    FREE,

    /**
     * Freeze rotation at the moment the phase starts.
     * NPC stays facing the direction it was looking when the phase began.
     */
    LOCKED,

    /**
     * Force NPC to continuously face its target (hit scan).
     * Overrides AI look helper every tick for precise aiming.
     */
    TRACK;

    public String getDisplayKey() {
        switch (this) {
            case FREE:
                return "ability.rotation.free";
            case LOCKED:
                return "ability.rotation.locked";
            case TRACK:
                return "ability.rotation.track";
            default:
                return "ability.rotation.free";
        }
    }

    public static String[] getDisplayKeys() {
        return new String[]{
            "ability.rotation.free",
            "ability.rotation.locked",
            "ability.rotation.track"
        };
    }

    public static RotationMode fromOrdinal(int ordinal) {
        RotationMode[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return FREE;
    }
}
