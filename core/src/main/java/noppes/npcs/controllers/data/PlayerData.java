package noppes.npcs.controllers.data;

import noppes.npcs.constants.ClientOnly;
import kamkeel.npcs.util.IVector3;
import kamkeel.npcs.controllers.data.ability.preview.PreviewEntityHandler;
import kamkeel.npcs.controllers.data.ability.gui.SubGuiAbilityConfig;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityConfigCallback;
import kamkeel.npcs.controllers.data.ability.gui.FieldDef;
import kamkeel.npcs.controllers.data.ability.gui.IChainedAbilityFieldProvider;
import kamkeel.npcs.controllers.data.ability.gui.IAbilityFieldProvider;
import noppes.npcs.entity.EntityNPCInterface;
import kamkeel.npcs.entity.EntityEnergyDome;
import kamkeel.npcs.entity.EntityEnergyBarrier;
import kamkeel.npcs.entity.EntityEnergyPanel;
import kamkeel.npcs.entity.EntityAbilityOrb;
import kamkeel.npcs.entity.EntityAbilityLaser;
import kamkeel.npcs.entity.EntityAbilityDisc;
import kamkeel.npcs.entity.EntityAbilityBeam;
import kamkeel.npcs.entity.EntityEnergyProjectile;
import kamkeel.npcs.util.ByteBufUtils;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ability.IPlayerAbilityData;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.handler.IPlayerBankData;
import noppes.npcs.api.handler.IPlayerData;
import noppes.npcs.api.handler.IPlayerDialogData;
import noppes.npcs.api.handler.IPlayerFactionData;
import noppes.npcs.api.handler.IPlayerItemGiverData;
import noppes.npcs.api.handler.IPlayerMailData;
import noppes.npcs.api.handler.IPlayerQuestData;
import noppes.npcs.api.handler.IPlayerTradeData;
import noppes.npcs.api.handler.IPlayerTransportData;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.action.ActionManager;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.util.CustomNPCsThreader;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.UUID;

public class PlayerData implements IExtendedEntityProperties, IPlayerData {
    public PlayerDialogData dialogData = new PlayerDialogData();
    public PlayerBankData bankData = new PlayerBankData(this);
    public PlayerQuestData questData = new PlayerQuestData(this);
    public PlayerTransportData transportData = new PlayerTransportData();
    public PlayerFactionData factionData = new PlayerFactionData(this);
    public PlayerItemGiverData itemgiverData = new PlayerItemGiverData(this);
    public PlayerMailData mailData = new PlayerMailData(this);
    public AnimationData animationData = new AnimationData(this);
    public PlayerEffectData effectData = new PlayerEffectData();
    public DataTimers timers = new DataTimers(this);
    public DataSkinOverlays skinOverlays = new DataSkinOverlays(this);
    public MagicData magicData = new MagicData();

    // Trade data (currency + auction claims) - shared across all profile slots
    public PlayerTradeData tradeData = new PlayerTradeData(this);

    public PlayerAbilityData abilityData = new PlayerAbilityData(this);
    public PlayerAbilityHotbarData hotbarData = new PlayerAbilityHotbarData();
    public ActionManager actionManager = new ActionManager();
    public PlayerDataScript scriptData;

    public EntityNPCInterface editingNpc;
    public INbt cloned;

    public UUID partyUUID = null;
    private final HashSet<UUID> partyInvites = new HashSet<>();

    public IPlayer player;

    public String playername = "";
    public String uuid = "";

    private EntityNPCInterface activeCompanion = null;
    public int companionID = 0;

    public boolean isGUIOpen = false;
    public boolean hadInteract = true;

    public boolean updateClient = false;

    public ScreenSize screenSize = new ScreenSize(-1, -1);

    public int profileSlot = 0;
    private boolean specialKeyDown = false;

    public void onLogin() {
        // Clear any stale transient ability state that leaked via the PlayerData cache.
        // Must happen before the animation check since it may clear ability animation state.
        abilityData.resetOnLogin();

        // Handle animation state from previous session
        AnimationData animationData = this.animationData;
        if (animationData != null && animationData.isClientAnimating()) {
            if (abilityData.isPlayingAbilityAnimation()) {
                // Ability animation was playing when player disconnected.
                // Ability state is transient (lost on disconnect), so this animation
                // is orphaned - clear it instead of replaying a stuck animation.
                abilityData.clearOrphanedAbilityAnimation();
            } else {
                // Non-ability animation (e.g. script-driven) - continue playing
                Animation currentAnimation = animationData.currentClientAnimation;
                INbt compound = currentAnimation.writeToNBT();
                animationData.viewAnimation(currentAnimation, animationData, compound,
                    animationData.isClientAnimating(), currentAnimation.currentFrame, currentAnimation.currentFrameTime);
            }
        }

        CustomEffectController controller = CustomEffectController.getInstance();
        UUID playerID = player.getPersistentID();
        // Only add if there are saved effects and none are registered yet.
        if (!controller.playerEffects.containsKey(playerID)) {
            controller.playerEffects.put(playerID, effectData.getEffects());
        }
    }

    public void onLogout() {
        // Interrupt executing ability (fires events, rolls cooldown), then clear
        // all transient state so nothing leaks via the PlayerData cache.
        abilityData.resetOnDisconnect();
        this.partyInvites.clear();
        this.actionManager.clear();
    }

    @Override
    public void saveNBTData(INbt INbt) {
    }

    @Override
    public void loadNBTData(INbt compound) {
    }

    public void setNBT(INbt data) {
        dialogData.loadNBTData(new NBTWrapper(data));
        bankData.loadNBTData(data);
        questData.loadNBTData(data);
        transportData.loadNBTData(new NBTWrapper(data));
        factionData.loadNBTData(data);
        itemgiverData.loadNBTData(data);
        mailData.loadNBTData(data);
        timers.readFromNBT(data);
        skinOverlays.readFromNBT(data);
        animationData.readFromNBT(data);
        effectData.readFromNBT(new NBTWrapper(data));
        magicData.readToNBT(new NBTWrapper(data));
        tradeData.readFromNBT(data);
        abilityData.readFromNBT(data);
        hotbarData.readFromNBT(data);

        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        } else {
            playername = data.getString("PlayerName");
            uuid = data.getString("UUID");
        }
        companionID = data.getInteger("PlayerCompanionId");
        profileSlot = data.getInteger("ProfileSlot");
        if (data.hasKey("PlayerCompanion") && !hasCompanion()) {
            EntityCustomNpc npc = new EntityCustomNpc(player.worldObj);
            npc.readEntityFromNBT(data.getCompoundTag("PlayerCompanion"));
            npc.setPosition(player.posX, player.posY, player.posZ);
            if (npc.advanced.role == EnumRoleType.Companion) {
                setCompanion(npc);
                ((RoleCompanion) npc.roleInterface).setSitting(false);
                player.worldObj.spawnEntityInWorld(npc);
            }
        }
        isGUIOpen = data.getBoolean("isGUIOpen");
        DBCAddon.instance.readFromNBT(this, data);
    }

    public INbt getNBT() {
        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        }
        INbt compound = new INbt();
        dialogData.saveNBTData(new NBTWrapper(compound));
        bankData.saveNBTData(compound);
        questData.saveNBTData(compound);
        transportData.saveNBTData(new NBTWrapper(compound));
        factionData.saveNBTData(compound);
        itemgiverData.saveNBTData(compound);
        mailData.saveNBTData(compound);
        timers.writeToNBT(compound);
        skinOverlays.writeToNBT(compound);
        animationData.writeToNBT(compound);
        effectData.writeToNBT(new NBTWrapper(compound));
        magicData.writeToNBT(new NBTWrapper(compound));
        tradeData.writeToNBT(compound);
        abilityData.writeToNBT(compound);
        hotbarData.writeToNBT(compound);

        compound.setString("PlayerName", playername);
        compound.setString("UUID", uuid);
        compound.setInteger("PlayerCompanionId", companionID);
        compound.setBoolean("isGUIOpen", isGUIOpen);
        compound.setInteger("ProfileSlot", profileSlot);

        if (hasCompanion()) {
            INbt nbt = new INbt();
            if (activeCompanion.writeToNBTOptional(nbt))
                compound.setTag("PlayerCompanion", nbt);
        }
        DBCAddon.instance.writeToNBT(this, compound);
        return compound;
    }

    public INbt getSyncNBT() {
        INbt compound = new INbt();
        dialogData.saveNBTData(new NBTWrapper(compound));
        questData.saveNBTData(compound);
        factionData.saveNBTData(compound);
        return compound;
    }

    public void setSyncNBT(INbt data) {
        dialogData.loadNBTData(new NBTWrapper(data));
        questData.loadNBTData(data);
        factionData.loadNBTData(data);
    }

    public INbt getPlayerEffects() {
        INbt compound = new INbt();
        effectData.writeToNBT(new NBTWrapper(compound));
        return compound;
    }

    public void setPlayerEffects(INbt data) {
        effectData.readFromNBT(new NBTWrapper(data));
    }

    public INbt getSyncNBTFull() {
        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        }
        INbt compound = new INbt();
        dialogData.saveNBTData(new NBTWrapper(compound));
        bankData.saveNBTData(compound);
        questData.saveNBTData(compound);
        transportData.saveNBTData(new NBTWrapper(compound));
        factionData.saveNBTData(compound);
        mailData.saveNBTData(compound);
        tradeData.writeToNBT(compound);
        abilityData.writeToNBT(compound);
        hotbarData.writeToNBT(compound);
        compound.setString("PlayerName", playername);
        compound.setString("UUID", uuid);
        DBCAddon.instance.writeToNBT(this, compound);
        return compound;
    }

    public void setSyncNBTFull(INbt data) {
        dialogData.loadNBTData(new NBTWrapper(data));
        bankData.loadNBTData(data);
        questData.loadNBTData(data);
        transportData.loadNBTData(new NBTWrapper(data));
        factionData.loadNBTData(data);
        mailData.loadNBTData(data);
        tradeData.readFromNBT(data);
        abilityData.readFromNBT(data);
        hotbarData.readFromNBT(data);
        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        } else {
            playername = data.getString("PlayerName");
            uuid = data.getString("UUID");
        }
        DBCAddon.instance.readFromNBT(this, data);
    }

    public void getDBCSync(INbt compound) {
        DBCAddon.instance.writeToNBT(this, compound);
    }

    public void setDBCSync(INbt data) {
        DBCAddon.instance.readFromNBT(this, data);
    }

    @Override
    public void init(IEntity IEntity, IWorld IWorld) {

    }

    public void setGUIOpen(boolean bool) {
        this.isGUIOpen = bool;
    }

    public boolean getGUIOpen() {
        return this.isGUIOpen;
    }

    public boolean isSpecialKeyDown() {
        return specialKeyDown;
    }

    public void setSpecialKeyDown(boolean specialKeyDown) {
        this.specialKeyDown = specialKeyDown;
    }

    public ScreenSize getScreenSize() {
        return screenSize;
    }

    public void setScreenSize(ScreenSize size) {
        screenSize = size;
    }

    public boolean hasCompanion() {
        return activeCompanion != null && !activeCompanion.isDead;
    }

    public void setCompanion(EntityNPCInterface npc) {
        if (npc != null && npc.advanced.role != EnumRoleType.Companion)//shouldnt happen
            return;
        companionID++;
        activeCompanion = npc;
        if (npc != null)
            ((RoleCompanion) npc.roleInterface).companionID = companionID;
        save();
    }

    public void updateCompanion(IWorld IWorld) {
        if (!hasCompanion() || IWorld == activeCompanion.worldObj)
            return;
        RoleCompanion role = (RoleCompanion) activeCompanion.roleInterface;
        role.owner = player;
        if (!role.isFollowing())
            return;
        INbt nbt = new INbt();
        activeCompanion.writeToNBTOptional(nbt);
        activeCompanion.isDead = true;

        EntityCustomNpc npc = new EntityCustomNpc(IWorld);
        npc.readEntityFromNBT(nbt);
        npc.setPosition(player.posX, player.posY, player.posZ);
        setCompanion(npc);
        ((RoleCompanion) npc.roleInterface).setSitting(false);
        IWorld.spawnEntityInWorld(npc);
    }

    public void inviteToParty(Party party) {
        if (party != null && this.partyUUID == null && !this.partyInvites.contains(party.getPartyUUID())) {
            this.partyInvites.add(party.getPartyUUID());

            AchievementPacket.sendAchievement((IPlayer) player, true, "party.inviteAlert", party.getPartyLeader().getCommandSenderName());
            ChatAlertPacket.sendChatAlert((IPlayer) player, "\u00A7a", "party.inviteChat", " ", party.getPartyLeader().getCommandSenderName(), "!");
        }
    }

    public void ignoreInvite(UUID uuid) {
        if (uuid != null) {
            this.partyInvites.remove(uuid);
            PartyInvitePacket.sendInviteData((IPlayer) player);
        }
    }

    public void acceptInvite(UUID uuid) {
        if (uuid != null) {
            this.partyInvites.remove(uuid);
            Party party = PartyController.Instance().getParty(uuid);
            if (party != null) {
                if (!party.getIsLocked()) {
                    party.addPlayer(player);
                    PartyController.Instance().pingPartyUpdate(party);
                }
            }
        }
    }

    public Party getPlayerParty() {
        if (partyUUID != null) {
            return PartyController.Instance().getParty(partyUUID);
        }
        return null;
    }

    public HashSet<UUID> getPartyInvites() {
        return (HashSet<UUID>) this.partyInvites.clone();
    }

    public void setCompanion(ICustomNpc npc) {
        this.setCompanion((EntityNPCInterface) npc.getMCEntity());
    }

    public ICustomNpc getCompanion() {
        return (ICustomNpc) NpcAPI.Instance().getIEntity(activeCompanion);
    }

    public int getCompanionID() {
        return companionID;
    }

    public IPlayerDialogData getDialogData() {
        return dialogData;
    }

    public IPlayerBankData getBankData() {
        return bankData;
    }

    public IPlayerQuestData getQuestData() {
        return questData;
    }

    public IPlayerTransportData getTransportData() {
        return transportData;
    }

    public IPlayerFactionData getFactionData() {
        return factionData;
    }

    public IPlayerItemGiverData getItemGiverData() {
        return itemgiverData;
    }

    public IPlayerMailData getMailData() {
        return mailData;
    }

    public IPlayerTradeData getTradeData() {
        return tradeData;
    }

    public IPlayerAbilityData getAbilityData() {
        return abilityData;
    }

    public synchronized void save() {
        // Don't Save this is a Modification of a Profile's PlayerData
        Profile profile = ProfileController.Instance.getProfile(UUID.fromString(uuid));
        if (profile != null && profile.currentSlotId != this.profileSlot) {
            ProfileController.Instance.saveOffline(profile, UUID.fromString(uuid));
            return;
        }

        final INbt compound = getNBT();
        final String filename;
        if (ConfigMain.DatFormat) {
            filename = uuid + ".dat";
        } else {
            filename = uuid + ".json";
        }
        PlayerDataController.Instance.putPlayerMap(playername, uuid);
        PlayerDataController.Instance.putPlayerDataCache(uuid, this);
        CustomNPCsThreader.customNPCThread.execute(() -> {
            try {
                File saveDir = PlayerDataController.Instance.getSaveDir();
                File file = new File(saveDir, filename + "_new");
                File file1 = new File(saveDir, filename);
                if (ConfigMain.DatFormat) {
                    NBTIO.writeCompressed(compound, new FileOutputStream(file));
                } else {
                    NBTJsonUtil.SaveFile(file, compound);
                }
                if (file1.exists()) {
                    file1.delete();
                }
                file.renameTo(file1);
            } catch (Exception e) {
                LogWriter.except(e);
            }
        });
    }

    public void load() {
        INbt data = PlayerDataController.Instance.loadPlayerData(player.getPersistentID().toString());
        if (data.hasNoTags()) {
            data = PlayerDataController.Instance.loadPlayerDataOld(player.getCommandSenderName());
        }
        if (data.hasNoTags()) {
            data = getNBT();
        }
        setNBT(data);
    }

    public static PlayerData get(IPlayer player) {
        if (player.worldObj.isRemote)
            return CustomNpcs.proxy.getPlayerData(player);

        return PlayerDataController.Instance.getPlayerData(player);
    }
}
