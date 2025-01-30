package kamkeel.npcs.network.enums;

// Packets to the Client
public enum EnumClientPacket {
    // Synchronization
    SYNC,
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
}
