package kamkeel.npcs.network.enums;


import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;

// Packets to the Client
public enum EnumDataPacket {
    // Synchronization
    SYNC,
    SYNC_EFFECTS,
    CONFIG_COMMAND,

    // Messages
    CHAT_ALERT,
    ACHIEVEMENT,
    CHATBUBBLE,
    SOUND,
    DIALOG,
    QUEST_COMPLETION,

    // NPC
    EDIT_NPC,
    UPDATE_NPC,
    ROLE_NPC,
    DELETE_NPC,
    CLONE_NPC,
    WEAPON_NPC,

    // Data
    SCROLL_LIST,
    SCROLL_DATA,
    SCROLL_GROUP,
    SCROLL_SELECTED,
    PARTY_DATA,

    // GUI
    GUI_OPEN,
    GUI_REDSTONE,
    GUI_WAYPOINT,
    GUI_TELEPORTER,
    GUI_BOOK,
    GUI_DATA,
    PLAYER_DATA,
    GUI_ERROR,
    GUI_CLOSE,
    ISGUIOPEN,

    // Visual
    SCRIPTED_PARTICLE,
    PARTICLE,
    PLAYER_UPDATE_SKIN_OVERLAYS,
    UPDATE_ANIMATIONS,
    SCRIPT_OVERLAY_DATA,
    SCRIPT_OVERLAY_CLOSE,
    OVERLAY_QUEST_TRACKING,
    MARK_DATA,

    // Other
    VILLAGER_LIST,
    SWING_PLAYER_ARM,
    DISABLE_MOUSE_INPUT,
    LOGIN,

    // Ability System
    TELEGRAPH_SPAWN,
    TELEGRAPH_REMOVE,
    PLAYER_ABILITY_SYNC,
    PLAYER_ABILITY_STATE,
    ABILITY_HOTBAR_SYNC,
    ABILITY_COOLDOWN_SYNC,

    // Profile System
    PROFILE_SHARED_QUEST,

    // Auction System
    AUCTION_DATA,

    // Energy Charge Preview
    ENERGY_CHARGE_SPAWN,
    ENERGY_CHARGE_REMOVE,

    // Energy IExplosion Preview
    ENERGY_IExplosion_SPAWN,

    // Energy Projectile Reflection Sync
    PROJECTILE_REFLECT,

    // Energy Projectile Client Sync (position, motion, visual, and movement properties)
    PROJECTILE_CLIENT_SYNC,

    // Energy Barrier Client Sync (visual properties and barrier-specific data)
    BARRIER_CLIENT_SYNC,
}
