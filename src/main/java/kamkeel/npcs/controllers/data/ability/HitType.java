package kamkeel.npcs.controllers.data.ability;

/**
 * Shared hit behavior for energy projectiles.
 */
public enum HitType {
    SINGLE,
    PIERCE,
    MULTI;

    public static HitType fromOrdinal(int ordinal) {
        HitType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return SINGLE;
    }

    @Override
    public String toString() {
        switch (this) {
            case SINGLE:
                return "ability.hitType.single";
            case PIERCE:
                return "ability.hitType.pierce";
            case MULTI:
                return "ability.hitType.multi";
            default:
                return name();
        }
    }
}
