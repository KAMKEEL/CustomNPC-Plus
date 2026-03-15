package noppes.npcs.constants;

import net.minecraft.util.StatCollector;

public enum EnumClaimType {
    ITEM("auction.claim.item"),
    CURRENCY("auction.claim.currency"),
    REFUND("auction.claim.refund");

    private final String langKey;

    EnumClaimType(String langKey) {
        this.langKey = langKey;
    }

    public String getLangKey() {
        return langKey;
    }

    public String getDisplayName() {
        return StatCollector.translateToLocal(langKey);
    }

    public boolean isItem() {
        return this == ITEM;
    }

    public boolean isCurrency() {
        return this == CURRENCY || this == REFUND;
    }

    public static EnumClaimType fromOrdinal(int ordinal) {
        if (ordinal >= 0 && ordinal < values().length) {
            return values()[ordinal];
        }
        return ITEM;
    }
}
