package kamkeel.npcs.controllers.data.ability;

/**
 * Defines anchor points for ability charging effects.
 * Determines where the charging visual (orb, disc, etc.) appears relative to the entity.
 */
public enum AnchorPoint {
    FRONT(0, "Front"),           // In front of entity face (default)
    CENTER(1, "Center"),         // Entity center
    RIGHT_HAND(2, "Right Hand"), // Right arm/hand position
    LEFT_HAND(3, "Left Hand"),   // Left arm/hand position
    ABOVE_HEAD(4, "Above Head"), // Above the entity
    CHEST(5, "Chest");           // Chest level, centered

    private final int id;
    private final String displayName;

    AnchorPoint(int id, String displayName) {
        this.id = id;
        this.displayName = displayName;
    }

    public int getId() {
        return id;
    }

    public String getDisplayName() {
        return displayName;
    }

    /**
     * Get AnchorPoint from NBT id value.
     * Returns FRONT if id is invalid.
     */
    public static AnchorPoint fromId(int id) {
        for (AnchorPoint anchor : values()) {
            if (anchor.id == id) {
                return anchor;
            }
        }
        return FRONT;
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
