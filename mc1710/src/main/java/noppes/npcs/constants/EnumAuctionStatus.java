package noppes.npcs.constants;

import net.minecraft.util.StatCollector;

public enum EnumAuctionStatus {
    ACTIVE("auction.status.active"),
    ENDED("auction.status.ended"),
    CANCELLED("auction.status.cancelled"),
    CLAIMED("auction.status.claimed");

    private final String langKey;

    EnumAuctionStatus(String langKey) {
        this.langKey = langKey;
    }

    public String getLangKey() {
        return langKey;
    }

    public String getDisplayName() {
        return StatCollector.translateToLocal(langKey);
    }

    public boolean isActive() {
        return this == ACTIVE;
    }

    public boolean isEnded() {
        return this == ENDED || this == CANCELLED || this == CLAIMED;
    }

    public boolean canBid() {
        return this == ACTIVE;
    }

    public boolean canCancel() {
        return this == ACTIVE;
    }

    public static EnumAuctionStatus fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return ACTIVE;
    }
}
