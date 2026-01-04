package noppes.npcs.constants;

/**
 * Stock reset types for Trader NPCs.
 * Mirrors EnumQuestRepeat pattern for consistency.
 */
public enum EnumStockReset {
    NONE,       // Never resets
    MCDAILY,    // Every 24000 MC ticks (1 MC day)
    MCWEEKLY,   // Every 168000 MC ticks (7 MC days)
    MCCUSTOM,   // Custom MC tick interval
    RLDAILY,    // Every 24 real hours
    RLWEEKLY,   // Every 7 real days
    RLCUSTOM;   // Custom real-time interval

    /**
     * Get reset interval in milliseconds for real-time types
     * or ticks for MC-time types
     */
    public long getDefaultInterval() {
        switch (this) {
            case MCDAILY:
                return 24000;  // MC ticks
            case MCWEEKLY:
                return 168000; // MC ticks (7 days)
            case RLDAILY:
                return 86400000L;  // 24 hours in ms
            case RLWEEKLY:
                return 604800000L; // 7 days in ms
            default:
                return 0;
        }
    }

    /**
     * Check if this reset type uses real-world time
     */
    public boolean isRealTime() {
        return this == RLDAILY || this == RLWEEKLY || this == RLCUSTOM;
    }

    /**
     * Check if this reset type uses Minecraft time
     */
    public boolean isMinecraftTime() {
        return this == MCDAILY || this == MCWEEKLY || this == MCCUSTOM;
    }

    /**
     * Get display name for GUI
     */
    public String getDisplayName() {
        switch (this) {
            case NONE: return "Never";
            case MCDAILY: return "MC Daily";
            case MCWEEKLY: return "MC Weekly";
            case MCCUSTOM: return "MC Custom";
            case RLDAILY: return "Real Daily";
            case RLWEEKLY: return "Real Weekly";
            case RLCUSTOM: return "Real Custom";
            default: return "Unknown";
        }
    }
}
