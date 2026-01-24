package noppes.npcs.constants;

import net.minecraft.util.StatCollector;

public enum EnumNotificationType {
    AUCTION_WON("auction.notify.won"),
    AUCTION_OUTBID("auction.notify.outbid"),
    AUCTION_SOLD("auction.notify.sold"),
    AUCTION_EXPIRED("auction.notify.expired"),
    CLAIM_READY("auction.notify.claim");

    private final String langKey;

    EnumNotificationType(String langKey) {
        this.langKey = langKey;
    }

    public String getLangKey() {
        return langKey;
    }

    public String getDisplayName() {
        return StatCollector.translateToLocal(langKey);
    }

    public static EnumNotificationType fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return CLAIM_READY;
    }
}
