package noppes.npcs.constants;

import noppes.npcs.CustomNpcsPermissions;

public enum EnumPacketServer {
	Delete(CustomNpcsPermissions.NPC_DELETE, true),
	RemoteMainMenu(CustomNpcsPermissions.NPC_GUI),
    RemoteGlobalMenu(CustomNpcsPermissions.GLOBAL_REMOTE),
	NpcMenuClose(CustomNpcsPermissions.NPC_GUI, true),
	RemoteDelete(CustomNpcsPermissions.NPC_DELETE, true),
	RemoteFreeze(CustomNpcsPermissions.NPC_FREEZE),
	RemoteReset(CustomNpcsPermissions.NPC_RESET),
	SpawnMob(CustomNpcsPermissions.SPAWNER_MOB),
	MobSpawner(CustomNpcsPermissions.SPAWNER_CREATE),

    MainmenuAISave(CustomNpcsPermissions.NPC_AI,true), MainmenuAIGet(true),
	MainmenuInvSave(CustomNpcsPermissions.NPC_INVENTORY, true), MainmenuInvGet(true),
	MainmenuStatsSave(CustomNpcsPermissions.NPC_STATS, true), MainmenuStatsGet(true),

	MainmenuDisplaySave(CustomNpcsPermissions.NPC_DISPLAY, true), MainmenuDisplayGet(true),
	ModelDataSave(CustomNpcsPermissions.NPC_DISPLAY, true),

	MainmenuAdvancedSave(CustomNpcsPermissions.NPC_ADVANCED,true), MainmenuAdvancedGet(true),
	DialogNpcSet(CustomNpcsPermissions.NPC_ADVANCED),
	DialogNpcRemove(CustomNpcsPermissions.NPC_ADVANCED, true),
	FactionSet(CustomNpcsPermissions.NPC_ADVANCED, true),
	TagSet(CustomNpcsPermissions.NPC_ADVANCED, true),
	TransportSave(CustomNpcsPermissions.NPC_ADVANCED, true),
	TransformSave(CustomNpcsPermissions.NPC_ADVANCED, true), TransformGet(true),
	TransformLoad(CustomNpcsPermissions.NPC_ADVANCED, true),
	TraderMarketSave(CustomNpcsPermissions.NPC_ADVANCED, true),
	JobSave(CustomNpcsPermissions.NPC_ADVANCED, true), JobGet(true),
	RoleSave(CustomNpcsPermissions.NPC_ADVANCED, true), RoleGet(true),
	JobSpawnerAdd(CustomNpcsPermissions.NPC_ADVANCED, true), JobSpawnerRemove(CustomNpcsPermissions.NPC_ADVANCED, true),
	RoleCompanionUpdate(CustomNpcsPermissions.NPC_ADVANCED, true),
	LinkedSet(CustomNpcsPermissions.NPC_ADVANCED, true),

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

	LinkedGetAll,
    LinkedRemove(CustomNpcsPermissions.GLOBAL_LINKED), LinkedAdd(CustomNpcsPermissions.GLOBAL_LINKED),

    ScriptDataSave(CustomNpcsPermissions.SCRIPT_NPC, true), ScriptDataGet(true),
	EventScriptDataSave(CustomNpcsPermissions.SCRIPT_NPC, true), EventScriptDataGet(true),

    PlayerDataRemove(CustomNpcsPermissions.GLOBAL_PLAYERDATA),
    PlayerDataMapRegen(CustomNpcsPermissions.GLOBAL_PLAYERDATA),

    BankSave(CustomNpcsPermissions.GLOBAL_BANK), BanksGet, BankGet,
    BankRemove(CustomNpcsPermissions.GLOBAL_BANK),

    DialogCategorySave(CustomNpcsPermissions.GLOBAL_DIALOG), DialogCategoriesGet, DialogsGetFromDialog,
	DialogCategoryRemove(CustomNpcsPermissions.GLOBAL_DIALOG),  DialogCategoryGet,
	DialogSave(CustomNpcsPermissions.GLOBAL_DIALOG),  DialogsGet, DialogGet,
	DialogRemove(CustomNpcsPermissions.GLOBAL_DIALOG),

    TransportCategoryRemove(CustomNpcsPermissions.GLOBAL_TRANSPORT), TransportGetLocation(true),
	TransportRemove(CustomNpcsPermissions.GLOBAL_TRANSPORT), TransportsGet,
	TransportCategorySave(CustomNpcsPermissions.GLOBAL_TRANSPORT), TransportCategoriesGet,
	FactionRemove(CustomNpcsPermissions.GLOBAL_FACTION),
	FactionSave(CustomNpcsPermissions.GLOBAL_FACTION), FactionsGet, FactionGet,
	TagRemove(CustomNpcsPermissions.GLOBAL_TAG),
	TagSave(CustomNpcsPermissions.GLOBAL_TAG), TagsGet, TagGet, NpcTagsGet,
	QuestCategorySave(CustomNpcsPermissions.GLOBAL_QUEST), QuestCategoriesGet,
	QuestRemove(CustomNpcsPermissions.GLOBAL_QUEST),
	QuestCategoryRemove(CustomNpcsPermissions.GLOBAL_QUEST),
	QuestRewardSave(CustomNpcsPermissions.GLOBAL_QUEST),
	QuestSave(CustomNpcsPermissions.GLOBAL_QUEST), QuestsGetFromQuest, QuestsGet,
	QuestDialogGetTitle(CustomNpcsPermissions.GLOBAL_QUEST),
	RecipeSave(CustomNpcsPermissions.GLOBAL_RECIPE),
	RecipeRemove(CustomNpcsPermissions.GLOBAL_RECIPE),
	NaturalSpawnSave(CustomNpcsPermissions.GLOBAL_NATURALSPAWN), NaturalSpawnGet,
	NaturalSpawnRemove(CustomNpcsPermissions.GLOBAL_NATURALSPAWN),
	MerchantUpdate(CustomNpcsPermissions.EDIT_VILLAGER),
	PlayerRider(CustomNpcsPermissions.TOOL_MOUNTER),
	SpawnRider(CustomNpcsPermissions.TOOL_MOUNTER),
	MovingPathSave(CustomNpcsPermissions.TOOL_PATHER,true), MovingPathGet(true),
	DialogNpcGet,

	AnimationsGet, AnimationGet,
    AnimationRemove(CustomNpcsPermissions.GLOBAL_ANIMATION), AnimationSave(CustomNpcsPermissions.GLOBAL_ANIMATION),

	RecipesGet, RecipeGet, QuestOpenGui, PlayerDataGet, QuestLogToServer,

	RemoteNpcsGet(CustomNpcsPermissions.NPC_GUI),
	RemoteTpToNpc(CustomNpcsPermissions.NPC_TELEPORT),
    QuestGet, QuestCategoryGet,
	SaveTileEntity,
	NaturalSpawnGetAll, MailOpenSetup,
	DimensionsGet, DimensionTeleport, GetTileEntity, Gui, IsGuiOpen, CacheAnimation,

	CustomGuiButton,CustomGuiScrollClick,CustomGuiClose,CustomGuiUnfocused,

	SavePartyData, CreateParty, GetPartyData, DisbandParty, KickPlayer, PartyInvite, GetPartyInviteList, AcceptInvite,
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
