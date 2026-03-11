package noppes.npcs.constants;

import net.minecraft.util.StatCollector;

/**
 * Stock reset types for Trader NPCs.
 * Mirrors EnumQuestRepeat pattern for consistency.
 */
public enum EnumStockReset {
    NONE("stock.reset.never"),
    MCDAILY("stock.reset.mcdaily"),
    MCWEEKLY("stock.reset.mcweekly"),
    MCCUSTOM("stock.reset.mccustom"),
    RLDAILY("stock.reset.rldaily"),
    RLWEEKLY("stock.reset.rlweekly"),
    RLCUSTOM("stock.reset.rlcustom");

    private final String langKey;

    EnumStockReset(String langKey) {
        this.langKey = langKey;
    }

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
     * Get display name for GUI (translated)
     */
    public String getDisplayName() {
        return StatCollector.translateToLocal(langKey);
    }

    /**
     * Get all display names for dropdown menus
     */
    public static String[] getDisplayNames() {
        EnumStockReset[] values = values();
        String[] names = new String[values.length];
        for (int i = 0; i < values.length; i++) {
            names[i] = values[i].getDisplayName();
        }
        return names;
    }
}
