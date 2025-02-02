package noppes.npcs.constants;

import noppes.npcs.CustomNpcsPermissions;

public enum EnumPacketServer {
	NPCDelete(CustomNpcsPermissions.NPC_DELETE, true),
	NpcMenuClose(CustomNpcsPermissions.NPC_GUI, true),
	RemoteReset(CustomNpcsPermissions.NPC_RESET),
	SpawnMob(CustomNpcsPermissions.SPAWNER_MOB),
	MobSpawner(CustomNpcsPermissions.SPAWNER_CREATE),

	ModelDataSave(CustomNpcsPermissions.NPC_DISPLAY, true),

    TransformSave(CustomNpcsPermissions.NPC_ADVANCED, true), TransformGet(true),
	TransformLoad(CustomNpcsPermissions.NPC_ADVANCED, true),
	TraderMarketSave(CustomNpcsPermissions.NPC_ADVANCED, true),
	JobSave(CustomNpcsPermissions.NPC_ADVANCED, true), JobGet(true),
	RoleSave(CustomNpcsPermissions.NPC_ADVANCED, true), RoleGet(true),
	JobSpawnerAdd(CustomNpcsPermissions.NPC_ADVANCED, true), JobSpawnerRemove(CustomNpcsPermissions.NPC_ADVANCED, true),
	RoleCompanionUpdate(CustomNpcsPermissions.NPC_ADVANCED, true),

	ClonePreSave(CustomNpcsPermissions.NPC_CLONE), CloneSave(CustomNpcsPermissions.NPC_CLONE), CloneRemove(CustomNpcsPermissions.NPC_CLONE), CloneList, CloneTagList, CloneAllTags, CloneAllTagsShort,

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

    ScriptDataSave(CustomNpcsPermissions.SCRIPT_NPC, true), ScriptDataGet(true),
	EventScriptDataSave(CustomNpcsPermissions.SCRIPT_NPC, true), EventScriptDataGet(true),

    DialogCategoriesGet, DialogsGetFromDialog,
	DialogsGet, DialogGet,
	DialogRemove(CustomNpcsPermissions.GLOBAL_DIALOG),

	FactionsGet,
    FactionGet,
	TagsGet,
    TagGet,
    NpcTagsGet,
	QuestCategoriesGet,
	QuestRemove(CustomNpcsPermissions.GLOBAL_QUEST),
	QuestRewardSave(CustomNpcsPermissions.GLOBAL_QUEST),
	QuestSave(CustomNpcsPermissions.GLOBAL_QUEST), QuestsGetFromQuest, QuestsGet,
	MerchantUpdate(CustomNpcsPermissions.EDIT_VILLAGER),
	PlayerRider(CustomNpcsPermissions.TOOL_MOUNTER),
	SpawnRider(CustomNpcsPermissions.TOOL_MOUNTER),
	MovingPathSave(CustomNpcsPermissions.TOOL_PATHER,true), MovingPathGet(true),

	AnimationsGet, AnimationGet,
    AnimationRemove(CustomNpcsPermissions.GLOBAL_ANIMATION), AnimationSave(CustomNpcsPermissions.GLOBAL_ANIMATION),

    QuestOpenGui, QuestLogToServer, PartyLogToServer, UntrackQuest,

    QuestGet, QuestCategoryGet,
	SaveTileEntity, MailOpenSetup,
	DimensionTeleport, GetTileEntity, IsGuiOpen,

	SavePartyData, CreateParty, GetPartyData, DisbandParty, KickPlayer, LeavePlayer, PartyInvite, GetPartyInviteList, AcceptInvite,
	IgnoreInvite, SetPartyLeader, SetPartyQuest,

    ServerUpdateSkinOverlays;

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
