package noppes.npcs.constants;

import noppes.npcs.CustomNpcsPermissions;

public enum EnumPacketServer {
	SpawnMob(CustomNpcsPermissions.SPAWNER_MOB),
	MobSpawner(CustomNpcsPermissions.SPAWNER_CREATE),

	ClonePreSave(CustomNpcsPermissions.NPC_CLONE),
    CloneSave(CustomNpcsPermissions.NPC_CLONE),
    CloneRemove(CustomNpcsPermissions.NPC_CLONE),
    CloneList,
    CloneTagList,
    CloneAllTags,
    CloneAllTagsShort,

	ScriptGlobalGuiDataSave(CustomNpcsPermissions.SCRIPT_GLOBAL, false),
	ScriptGlobalGuiDataGet(false),

	ScriptPlayerSave(CustomNpcsPermissions.SCRIPT_PLAYER, false),
	ScriptPlayerGet(false),

	ScriptForgeSave(CustomNpcsPermissions.SCRIPT_FORGE, false),
	ScriptForgeGet(false),

	ScriptGlobalNPCSave(CustomNpcsPermissions.SCRIPT_GLOBAL, false),
	ScriptGlobalNPCGet(false),

	ScriptItemDataSave(CustomNpcsPermissions.SCRIPT_ITEM, false),
	ScriptItemDataGet(false),

    ScriptBlockDataSave(CustomNpcsPermissions.SCRIPT_BLOCK, false),
	ScriptBlockDataGet(false),

    ScriptDataSave(CustomNpcsPermissions.SCRIPT_NPC, true),
    ScriptDataGet(true),
	EventScriptDataSave(CustomNpcsPermissions.SCRIPT_NPC, true),
    EventScriptDataGet(true),

    DialogCategoriesGet,
    DialogsGetFromDialog,
	DialogsGet,
    DialogGet,
	DialogRemove(CustomNpcsPermissions.GLOBAL_DIALOG),

	FactionsGet,
    FactionGet,
    TagGet,
	QuestCategoriesGet,
	QuestsGetFromQuest, QuestsGet,
	MerchantUpdate(CustomNpcsPermissions.EDIT_VILLAGER),
	PlayerRider(CustomNpcsPermissions.TOOL_MOUNTER),
	SpawnRider(CustomNpcsPermissions.TOOL_MOUNTER),
	MovingPathSave(CustomNpcsPermissions.TOOL_PATHER,true),
    MovingPathGet(true),

    QuestGet,
	DimensionTeleport;

	public CustomNpcsPermissions.Permission permission = null;
	public boolean needsNpc = false;

	EnumPacketServer() {}

	EnumPacketServer(CustomNpcsPermissions.Permission permission, boolean npc) {
		this(permission);
	}
	EnumPacketServer(boolean npc) {
		needsNpc = npc;
	}
	EnumPacketServer(CustomNpcsPermissions.Permission permission) {
		this.permission = permission;
	}
	public boolean hasPermission() {
		return permission != null;
	}
}
