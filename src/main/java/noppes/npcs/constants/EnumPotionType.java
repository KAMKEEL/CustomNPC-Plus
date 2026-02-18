package noppes.npcs.constants;

import net.minecraft.potion.Potion;

public enum EnumPotionType {
    None("gui.none", -1),
    Fire("tile.fire.name", -1),
    Poison("potion.poison", Potion.poison.id),
    Hunger("potion.hunger", Potion.hunger.id),
    Weakness("potion.weakness", Potion.weakness.id),
    Slowness("potion.moveSlowdown", Potion.moveSlowdown.id),
    Nausea("potion.confusion", Potion.confusion.id),
    Blindness("potion.blindness", Potion.blindness.id),
    Wither("potion.wither", Potion.wither.id),
    MiningFatigue("potion.digSlowDown", Potion.digSlowdown.id),
    Manual("effect.manual", -1);

    private final String langKey;
    private final int potionId;

    EnumPotionType(String langKey, int potionId) {
        this.langKey = langKey;
        this.potionId = potionId;
    }

    public String getLangKey() {
        return langKey;
    }

    public int getPotionId() {
        return potionId;
    }

    /**
     * Returns the actual potion ID to apply.
     * For Manual type, returns the provided manualId.
     * For all others, returns the mapped potionId.
     */
    public int getResolvedPotionId(int manualId) {
        if (this == Manual) return manualId;
        return potionId;
    }

    public static String[] getLangKeys() {
        EnumPotionType[] types = values();
        String[] keys = new String[types.length];
        for (int i = 0; i < types.length; i++) keys[i] = types[i].langKey;
        return keys;
    }

    /**
     * Returns lang keys excluding None. For use in effect lists where None is not a valid choice.
     */
    public static String[] getLangKeysNoNone() {
        EnumPotionType[] types = values();
        String[] keys = new String[types.length - 1];
        for (int i = 1; i < types.length; i++) keys[i - 1] = types[i].langKey;
        return keys;
    }

    /**
     * Maps a 0-based index (excluding None) back to the corresponding EnumPotionType.
     * Index 0 = Fire, 1 = Poison, etc.
     */
    public static EnumPotionType fromIndexNoNone(int index) {
        return fromOrdinal(index + 1);
    }

    public static EnumPotionType fromOrdinal(int ordinal) {
        EnumPotionType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return None;
    }

    /**
     * Checks if the given potion ID is valid and registered in the Minecraft potion registry.
     */
    public static boolean isValidPotionId(int id) {
        return id >= 0 && id < Potion.potionTypes.length && Potion.potionTypes[id] != null;
    }
}
