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
    CHEST("Chest");           // Chest level, centered

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
}
