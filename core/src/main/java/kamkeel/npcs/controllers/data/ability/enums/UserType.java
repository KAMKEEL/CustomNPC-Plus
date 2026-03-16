package kamkeel.npcs.controllers.data.ability.enums;


import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

/**
 * Defines which IEntity types can use an ability.
 */
public enum UserType {
    NPC_ONLY,
    PLAYER_ONLY,
    BOTH,
    NONE;

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
