package noppes.npcs.controllers.data;

import kamkeel.npcs.addon.DBCAddon;
import kamkeel.npcs.controllers.ProfileController;
import kamkeel.npcs.controllers.data.profile.Profile;
import kamkeel.npcs.network.packets.data.AchievementPacket;
import kamkeel.npcs.network.packets.data.ChatAlertPacket;
import kamkeel.npcs.network.packets.request.party.PartyInvitePacket;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.ability.IPlayerAbilityData;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.handler.IPlayerBankData;
import noppes.npcs.api.handler.IPlayerTradeData;
import noppes.npcs.api.handler.IPlayerData;
import noppes.npcs.api.handler.IPlayerDialogData;
import noppes.npcs.api.handler.IPlayerFactionData;
import noppes.npcs.api.handler.IPlayerItemGiverData;
import noppes.npcs.api.handler.IPlayerMailData;
import noppes.npcs.api.handler.IPlayerQuestData;
import noppes.npcs.api.handler.IPlayerTransportData;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.action.ActionManager;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataSkinOverlays;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScreenSize;
import noppes.npcs.util.CustomNPCsThreader;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.HashSet;
import java.util.UUID;

import static noppes.npcs.util.CustomNPCsThreader.customNPCThread;

public class PlayerData implements IExtendedEntityProperties, IPlayerData {
    public PlayerDialogData dialogData = new PlayerDialogData(this);
    public PlayerBankData bankData = new PlayerBankData(this);
    public PlayerQuestData questData = new PlayerQuestData(this);
    public PlayerTransportData transportData = new PlayerTransportData(this);
    public PlayerFactionData factionData = new PlayerFactionData(this);
    public PlayerItemGiverData itemgiverData = new PlayerItemGiverData(this);
    public PlayerMailData mailData = new PlayerMailData(this);
    public AnimationData animationData = new AnimationData(this);
    public PlayerEffectData effectData = new PlayerEffectData(this);
    public DataTimers timers = new DataTimers(this);
    public DataSkinOverlays skinOverlays = new DataSkinOverlays(this);
    public MagicData magicData = new MagicData();

    // Trade data (currency + auction claims) - shared across all profile slots
    public PlayerTradeData tradeData = new PlayerTradeData(this);

    public PlayerAbilityData abilityData = new PlayerAbilityData(this);
    public ActionManager actionManager = new ActionManager();
    public PlayerDataScript scriptData;

    public EntityNPCInterface editingNpc;
    public NBTTagCompound cloned;

    public UUID partyUUID = null;
    private final HashSet<UUID> partyInvites = new HashSet<>();

    public EntityPlayer player;

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
        // Continue playing animation for self when re-logging
        AnimationData animationData = this.animationData;
        if (animationData != null && animationData.isClientAnimating()) {
            Animation currentAnimation = animationData.currentClientAnimation;
            NBTTagCompound compound = currentAnimation.writeToNBT();
            animationData.viewAnimation(currentAnimation, animationData, compound,
                animationData.isClientAnimating(), currentAnimation.currentFrame, currentAnimation.currentFrameTime);
        }

        CustomEffectController controller = CustomEffectController.getInstance();
        UUID playerID = player.getPersistentID();
        // Only add if there are saved effects and none are registered yet.
        if (!controller.playerEffects.containsKey(playerID)) {
            controller.playerEffects.put(playerID, effectData.getEffects());
        }

        // Sync player ability data to client
        abilityData.syncToClient();
    }

    public void onLogout() {
        this.partyInvites.clear();
        this.actionManager.clear();
    }

    @Override
    public void saveNBTData(NBTTagCompound nbtTagCompound) {
    }

    @Override
    public void loadNBTData(NBTTagCompound compound) {
    }

    public void setNBT(NBTTagCompound data) {
        dialogData.loadNBTData(data);
        bankData.loadNBTData(data);
        questData.loadNBTData(data);
        transportData.loadNBTData(data);
        factionData.loadNBTData(data);
        itemgiverData.loadNBTData(data);
        mailData.loadNBTData(data);
        timers.readFromNBT(data);
        skinOverlays.readFromNBT(data);
        animationData.readFromNBT(data);
        effectData.readFromNBT(data);
        magicData.readToNBT(data);
        tradeData.readFromNBT(data);
        abilityData.readFromNBT(data);

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

    public NBTTagCompound getNBT() {
        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        }
        NBTTagCompound compound = new NBTTagCompound();
        dialogData.saveNBTData(compound);
        bankData.saveNBTData(compound);
        questData.saveNBTData(compound);
        transportData.saveNBTData(compound);
        factionData.saveNBTData(compound);
        itemgiverData.saveNBTData(compound);
        mailData.saveNBTData(compound);
        timers.writeToNBT(compound);
        skinOverlays.writeToNBT(compound);
        animationData.writeToNBT(compound);
        effectData.writeToNBT(compound);
        magicData.writeToNBT(compound);
        tradeData.writeToNBT(compound);
        abilityData.writeToNBT(compound);

        compound.setString("PlayerName", playername);
        compound.setString("UUID", uuid);
        compound.setInteger("PlayerCompanionId", companionID);
        compound.setBoolean("isGUIOpen", isGUIOpen);
        compound.setInteger("ProfileSlot", profileSlot);

        if (hasCompanion()) {
            NBTTagCompound nbt = new NBTTagCompound();
            if (activeCompanion.writeToNBTOptional(nbt))
                compound.setTag("PlayerCompanion", nbt);
        }
        DBCAddon.instance.writeToNBT(this, compound);
        return compound;
    }

    public NBTTagCompound getSyncNBT() {
        NBTTagCompound compound = new NBTTagCompound();
        dialogData.saveNBTData(compound);
        questData.saveNBTData(compound);
        factionData.saveNBTData(compound);
        return compound;
    }

    public void setSyncNBT(NBTTagCompound data) {
        dialogData.loadNBTData(data);
        questData.loadNBTData(data);
        factionData.loadNBTData(data);
    }

    public NBTTagCompound getPlayerEffects() {
        NBTTagCompound compound = new NBTTagCompound();
        effectData.writeToNBT(compound);
        return compound;
    }

    public void setPlayerEffects(NBTTagCompound data) {
        effectData.readFromNBT(data);
    }

    public NBTTagCompound getSyncNBTFull() {
        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        }
        NBTTagCompound compound = new NBTTagCompound();
        dialogData.saveNBTData(compound);
        bankData.saveNBTData(compound);
        questData.saveNBTData(compound);
        transportData.saveNBTData(compound);
        factionData.saveNBTData(compound);
        mailData.saveNBTData(compound);
        tradeData.writeToNBT(compound);
        compound.setString("PlayerName", playername);
        compound.setString("UUID", uuid);
        DBCAddon.instance.writeToNBT(this, compound);
        return compound;
    }

    public void setSyncNBTFull(NBTTagCompound data) {
        dialogData.loadNBTData(data);
        bankData.loadNBTData(data);
        questData.loadNBTData(data);
        transportData.loadNBTData(data);
        factionData.loadNBTData(data);
        mailData.loadNBTData(data);
        tradeData.readFromNBT(data);
        if (player != null) {
            playername = player.getCommandSenderName();
            uuid = player.getPersistentID().toString();
        } else {
            playername = data.getString("PlayerName");
            uuid = data.getString("UUID");
        }
        DBCAddon.instance.readFromNBT(this, data);
    }

    public void getDBCSync(NBTTagCompound compound) {
        DBCAddon.instance.writeToNBT(this, compound);
    }

    public void setDBCSync(NBTTagCompound data) {
        DBCAddon.instance.readFromNBT(this, data);
    }

    @Override
    public void init(Entity entity, World world) {

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

    public void updateCompanion(World world) {
        if (!hasCompanion() || world == activeCompanion.worldObj)
            return;
        RoleCompanion role = (RoleCompanion) activeCompanion.roleInterface;
        role.owner = player;
        if (!role.isFollowing())
            return;
        NBTTagCompound nbt = new NBTTagCompound();
        activeCompanion.writeToNBTOptional(nbt);
        activeCompanion.isDead = true;

        EntityCustomNpc npc = new EntityCustomNpc(world);
        npc.readEntityFromNBT(nbt);
        npc.setPosition(player.posX, player.posY, player.posZ);
        setCompanion(npc);
        ((RoleCompanion) npc.roleInterface).setSitting(false);
        world.spawnEntityInWorld(npc);
    }

    public void inviteToParty(Party party) {
        if (party != null && this.partyUUID == null && !this.partyInvites.contains(party.getPartyUUID())) {
            this.partyInvites.add(party.getPartyUUID());

            AchievementPacket.sendAchievement((EntityPlayerMP) player, true, "party.inviteAlert", party.getPartyLeader().getCommandSenderName());
            ChatAlertPacket.sendChatAlert((EntityPlayerMP) player, "\u00A7a", "party.inviteChat", " ", party.getPartyLeader().getCommandSenderName(), "!");
        }
    }

    public void ignoreInvite(UUID uuid) {
        if (uuid != null) {
            this.partyInvites.remove(uuid);
            PartyInvitePacket.sendInviteData((EntityPlayerMP) player);
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

        final NBTTagCompound compound = getNBT();
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
                    CompressedStreamTools.writeCompressed(compound, new FileOutputStream(file));
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
        NBTTagCompound data = PlayerDataController.Instance.loadPlayerData(player.getPersistentID().toString());
        if (data.hasNoTags()) {
            data = PlayerDataController.Instance.loadPlayerDataOld(player.getCommandSenderName());
        }
        if (data.hasNoTags()) {
            data = getNBT();
        }
        setNBT(data);
    }

    public static PlayerData get(EntityPlayer player) {
        if (player.worldObj.isRemote)
            return CustomNpcs.proxy.getPlayerData(player);

        return PlayerDataController.Instance.getPlayerData(player);
    }
}
