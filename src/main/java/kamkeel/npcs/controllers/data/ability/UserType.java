package kamkeel.npcs.controllers.data.ability;

/**
 * Defines which entity types can use an ability.
 */
public enum UserType {
    NPC_ONLY,
    PLAYER_ONLY,
    BOTH;

    public boolean allowsNpc() {
        return this == NPC_ONLY || this == BOTH;
    }

    public boolean allowsPlayer() {
        return this == PLAYER_ONLY || this == BOTH;
    }

    public static UserType fromOrdinal(int ordinal) {
        UserType[] values = values();
        if (ordinal >= 0 && ordinal < values.length) {
            return values[ordinal];
        }
        return BOTH;
    }
}
