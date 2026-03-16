package noppes.npcs.constants;


import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

public enum EnumPotionType {
    None("gui.none", -1),
    Fire("tile.fire.name", -1),
    Poison("IPotionType.poison", IPotionType.poison.id),
    Hunger("IPotionType.hunger", IPotionType.hunger.id),
    Weakness("IPotionType.weakness", IPotionType.weakness.id),
    Slowness("IPotionType.moveSlowdown", IPotionType.moveSlowdown.id),
    Nausea("IPotionType.confusion", IPotionType.confusion.id),
    Blindness("IPotionType.blindness", IPotionType.blindness.id),
    Wither("IPotionType.wither", IPotionType.wither.id),
    MiningFatigue("IPotionType.digSlowDown", IPotionType.digSlowdown.id),
    Manual("effect.manual", -1);

    private final String langKey;
    private final int IPotionTypeId;

    EnumPotionType(String langKey, int IPotionTypeId) {
        this.langKey = langKey;
        this.IPotionTypeId = IPotionTypeId;
    }

    public String getLangKey() {
        return langKey;
    }

    public int getIPotionTypeId() {
        return IPotionTypeId;
    }

    /**
     * Returns the actual IPotionType ID to apply.
     * For Manual type, returns the provided manualId.
     * For all others, returns the mapped IPotionTypeId.
     */
    public int getResolvedIPotionTypeId(int manualId) {
        if (this == Manual) return manualId;
        return IPotionTypeId;
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
     * Checks if the given IPotionType ID is valid and registered in the Minecraft IPotionType registry.
     */
    public static boolean isValidIPotionTypeId(int id) {
        return id >= 0 && id < IPotionType.IPotionTypeTypes.length && IPotionType.IPotionTypeTypes[id] != null;
    }
}
