package noppes.npcs.controllers.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import noppes.npcs.LogWriter;
import noppes.npcs.Server;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.handler.*;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.PlayerDataController;
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

public class PlayerData implements IExtendedEntityProperties, IPlayerData {
	public PlayerDialogData dialogData = new PlayerDialogData(this);
	public PlayerBankData bankData = new PlayerBankData(this);
	public PlayerQuestData questData = new PlayerQuestData(this);
	public PlayerTransportData transportData = new PlayerTransportData(this);
	public PlayerFactionData factionData = new PlayerFactionData(this);
	public PlayerItemGiverData itemgiverData = new PlayerItemGiverData(this);
	public PlayerMailData mailData = new PlayerMailData(this);
	public AnimationData animationData = new AnimationData(this);

	public DataTimers timers = new DataTimers(this);
	public DataSkinOverlays skinOverlays = new DataSkinOverlays(this);

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

	public ScreenSize screenSize = new ScreenSize(-1,-1);

	public void onLogin() {

	}

	public void onLogout() {
		this.partyUUID = null;
		this.partyInvites.clear();
	}

	@Override
	public void saveNBTData(NBTTagCompound nbtTagCompound) {
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
	}

	public void setNBT(NBTTagCompound data){
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

		if(player != null){
			playername = player.getCommandSenderName();
			uuid = player.getPersistentID().toString();
		}
		else{
			playername = data.getString("PlayerName");
			uuid = data.getString("UUID");
		}
		companionID = data.getInteger("PlayerCompanionId");
		if(data.hasKey("PlayerCompanion") && !hasCompanion()){
			EntityCustomNpc npc = new EntityCustomNpc(player.worldObj);
			npc.readEntityFromNBT(data.getCompoundTag("PlayerCompanion"));
			npc.setPosition(player.posX, player.posY, player.posZ);
			if(npc.advanced.role == EnumRoleType.Companion){
				setCompanion(npc);
				((RoleCompanion)npc.roleInterface).setSitting(false);
				player.worldObj.spawnEntityInWorld(npc);
			}
		}
		isGUIOpen = data.getBoolean("isGUIOpen");
	}
	public NBTTagCompound getNBT() {
		if(player != null){
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

		compound.setString("PlayerName", playername);
		compound.setString("UUID", uuid);
		compound.setInteger("PlayerCompanionId", companionID);
		compound.setBoolean("isGUIOpen",isGUIOpen);
		
		if(hasCompanion()){
			NBTTagCompound nbt = new NBTTagCompound();
			if(activeCompanion.writeToNBTOptional(nbt))
				compound.setTag("PlayerCompanion", nbt);
		}
		return compound;
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

	public ScreenSize getScreenSize(){
		return screenSize;
	}

	public void setScreenSize(ScreenSize size){
		screenSize = size;
	}

	public boolean hasCompanion(){
		return activeCompanion != null && !activeCompanion.isDead;
	}

	public void setCompanion(EntityNPCInterface npc) {
		if(npc != null && npc.advanced.role != EnumRoleType.Companion)//shouldnt happen
			return;
		companionID++;
		activeCompanion = npc;
		if(npc != null)
			((RoleCompanion)npc.roleInterface).companionID = companionID;
		save();
	}

	public void updateCompanion(World world) {
		if(!hasCompanion() || world == activeCompanion.worldObj)
			return;
		RoleCompanion role = (RoleCompanion) activeCompanion.roleInterface;
		role.owner = player;
		if(!role.isFollowing())
			return;
		NBTTagCompound nbt = new NBTTagCompound();
		activeCompanion.writeToNBTOptional(nbt);
		activeCompanion.isDead = true;

		EntityCustomNpc npc = new EntityCustomNpc(world);
		npc.readEntityFromNBT(nbt);
		npc.setPosition(player.posX, player.posY, player.posZ);
		setCompanion(npc);
		((RoleCompanion)npc.roleInterface).setSitting(false);
		world.spawnEntityInWorld(npc);
	}

	public void inviteToParty(Party party) {
		if (party != null && this.partyUUID == null && !this.partyInvites.contains(party.getPartyUUID())) {
			this.partyInvites.add(party.getPartyUUID());
			Server.sendData((EntityPlayerMP)this.player, EnumPacketClient.PARTY_MESSAGE, "party.inviteAlert", party.getPartyLeader().getCommandSenderName());
			Server.sendData((EntityPlayerMP)this.player, EnumPacketClient.CHAT, "party.inviteChat", " ", party.getPartyLeader().getCommandSenderName(), "!");
		}
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

	public synchronized void save() {
		final NBTTagCompound compound = getNBT();
		final String filename;
		if(ConfigMain.DatFormat){
			filename = uuid + ".dat";
		} else {
			filename = uuid + ".json";
		}
		PlayerDataController.Instance.putPlayerMap(playername, uuid);
		PlayerDataController.Instance.putPlayerDataCache(uuid, this);
		CustomNPCsThreader.playerDataThread.execute(() -> {
			try {
				File saveDir = PlayerDataController.Instance.getSaveDir();
				File file = new File(saveDir, filename + "_new");
				File file1 = new File(saveDir, filename);
				if(ConfigMain.DatFormat){
					CompressedStreamTools.writeCompressed(compound, new FileOutputStream(file));
				} else {
					NBTJsonUtil.SaveFile(file, compound);
				}
				if(file1.exists()){
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
		if(data.hasNoTags()){
			data = PlayerDataController.Instance.loadPlayerDataOld(player.getCommandSenderName());
		}
		setNBT(data);
	}
}
