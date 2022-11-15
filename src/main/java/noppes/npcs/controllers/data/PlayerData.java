package noppes.npcs.controllers.data;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.world.World;
import net.minecraftforge.common.IExtendedEntityProperties;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.entity.data.DataSkinOverlays;
import noppes.npcs.entity.data.DataTimers;
import noppes.npcs.roles.RoleCompanion;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.handler.*;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.FileInputStream;

public class PlayerData implements IExtendedEntityProperties, IPlayerData {
	public PlayerDialogData dialogData = new PlayerDialogData(this);
	public PlayerBankData bankData = new PlayerBankData(this);
	public PlayerQuestData questData = new PlayerQuestData(this);
	public PlayerTransportData transportData = new PlayerTransportData(this);
	public PlayerFactionData factionData = new PlayerFactionData(this);
	public PlayerItemGiverData itemgiverData = new PlayerItemGiverData(this);
	public PlayerMailData mailData = new PlayerMailData(this);

	public DataTimers timers = new DataTimers(this);
	public DataSkinOverlays skinOverlays = new DataSkinOverlays(this);

	public EntityNPCInterface editingNpc;
	public NBTTagCompound cloned;
	
	public EntityPlayer player;

	public String playername = "";
	public String uuid = "";

	private EntityNPCInterface activeCompanion = null;
	public int companionID = 0;

	public boolean isGUIOpen = false;

	@Override
	public void saveNBTData(NBTTagCompound nbtTagCompound) {
		//Do Nothing
	}

	public void savePlayerDataOnFile() {
		PlayerDataController.instance.savePlayerData(this);
	}

	@Override
	public void loadNBTData(NBTTagCompound compound) {
		//Do Nothing
	}

	public void loadPlayerDataFromFile() {
		NBTTagCompound data = PlayerDataController.instance.loadPlayerData(player.getPersistentID().toString());
		if(data.hasNoTags()){
			data = PlayerDataController.instance.loadPlayerDataOld(player.getCommandSenderName());
		}
		setNBT(data);
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
		isGUIOpen = bool;
		savePlayerDataOnFile();
	}

	public boolean getGUIOpen() {
		loadPlayerDataFromFile();
		return isGUIOpen;
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
		savePlayerDataOnFile();
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

	public static NBTTagCompound loadPlayerDataOld(String player) {
		File saveDir = CustomNpcs.getWorldSaveDirectory("playerdata");
		String filename = player;
		if (player.isEmpty()) {
			filename = "noplayername";
		}

		filename = filename + ".dat";

		File file;
		try {
			file = new File(saveDir, filename);
			if (file.exists()) {
				NBTTagCompound comp = CompressedStreamTools.readCompressed(new FileInputStream(file));
				file.delete();
				file = new File(saveDir, filename + "_old");
				if (file.exists()) {
					file.delete();
				}

				return comp;
			}
		} catch (Exception var6) {
			LogWriter.except(var6);
		}

		try {
			file = new File(saveDir, filename + "_old");
			if (file.exists()) {
				return CompressedStreamTools.readCompressed(new FileInputStream(file));
			}
		} catch (Exception var5) {
			LogWriter.except(var5);
		}

		return new NBTTagCompound();
	}

	public static NBTTagCompound loadPlayerData(String player) {
		File saveDir = CustomNpcs.getWorldSaveDirectory("playerdata");
		String filename = player;
		if (player.isEmpty()) {
			filename = "noplayername";
		}

		filename = filename + ".json";
		File file = null;

		try {
			file = new File(saveDir, filename);
			if (file.exists()) {
				return NBTJsonUtil.LoadFile(file);
			}
		} catch (Exception var5) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), var5);
		}

		return new NBTTagCompound();
	}

	public static PlayerData get(EntityPlayer player) {
		if(player.worldObj.isRemote) {
			return CustomNpcs.proxy.getPlayerData(player);
		} else {
			PlayerData data = new PlayerData();
			if (data.player == null) {
				data.player = player;
				NBTTagCompound compound = loadPlayerData(player.getPersistentID().toString());
				if (compound.hasNoTags()) {
					compound = loadPlayerDataOld(player.getCommandSenderName());
				}

				data.setNBT(compound);
			}

			return data;
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

	public void save() {
		PlayerDataController.instance.savePlayerData(this);
	}
}
