package kamkeel.npcs.controllers.data.ability;

/**
 * Defines anchor points for ability charging effects.
 * Determines where the charging visual (orb, disc, etc.) appears relative to the entity.
 */
public enum AnchorPoint {
    FRONT("Front"),           // In front of entity face (default)
    CENTER("Center"),         // Entity center
    RIGHT_HAND("Right Hand"), // Right arm/hand position
    LEFT_HAND("Left Hand"),   // Left arm/hand position
    ABOVE_HEAD("Above Head"), // Above the entity
    CHEST("Chest"),           // Chest level, centered
    EYE("Eye");           // Eye level, centered

    private final String displayName;

    AnchorPoint(String displayName) {
        this.displayName = displayName;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get AnchorPoint from ordinal value.
     * Returns FRONT if ordinal is out of bounds.
     */
    public static AnchorPoint fromOrdinal(int ordinal) {
        AnchorPoint[] vals = values();
        if (ordinal < 0 || ordinal >= vals.length) {
            return FRONT;
        }
        return vals[ordinal];
    }

    /**
     * Get all display names for GUI dropdowns.
     */
    public static String[] getDisplayNames() {
        AnchorPoint[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].displayName;
        }
        return names;
    }

    @Override
    public String toString() {
        switch (this) {
            case FRONT: return "ability.anchor.front";
            case CENTER: return "ability.anchor.center";
            case RIGHT_HAND: return "ability.anchor.rightHand";
            case LEFT_HAND: return "ability.anchor.leftHand";
            case ABOVE_HEAD: return "ability.anchor.aboveHead";
            case CHEST: return "ability.anchor.chest";
            case EYE: return "ability.anchor.eye";
            default: return name();
        }
    }
}
