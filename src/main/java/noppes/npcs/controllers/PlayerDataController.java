package noppes.npcs.controllers;

import java.io.EOFException;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.util.CustomNPCsScheduler;
import noppes.npcs.util.NBTJsonUtil;

public class PlayerDataController {		
	public static PlayerDataController instance;
	public Map<String, String> nameUUIDs;
	
	public PlayerDataController(){
		instance = this;
		nameUUIDs = getUsernameData();
	}
	public File getSaveDir(){
		try{
			File file = new File(CustomNpcs.getWorldSaveDirectory(),"playerdata");
			if(!file.exists())
				file.mkdir();
			return file;
		}
		catch (Exception e) {
			e.printStackTrace();
			return null;
		}
	}

	public NBTTagCompound loadPlayerDataOld(String player){
		File saveDir = getSaveDir();
		String filename = player;
		if(filename.isEmpty())
			filename = "noplayername";
		filename += ".dat";
		try {
	        File file = new File(saveDir, filename);
	        if(file.exists()){
	        	NBTTagCompound comp = CompressedStreamTools.readCompressed(new FileInputStream(file));
	        	file.delete();
		        file = new File(saveDir, filename+"_old");
		        if(file.exists())
		        	file.delete();
	        	return comp;
	        }
		} catch (Exception e) {
			LogWriter.except(e);
		}
		try {
	        File file = new File(saveDir, filename+"_old");
	        if(file.exists()){
	        	return CompressedStreamTools.readCompressed(new FileInputStream(file));
	        }
	        
		} catch (Exception e) {
			LogWriter.except(e);
		}

		return new NBTTagCompound();
	}

	public NBTTagCompound loadPlayerData(String player){
		File saveDir = getSaveDir();
		String filename = player;
		if(filename.isEmpty())
			filename = "noplayername";
		try {
	        File file = new File(saveDir, filename + ".json");
	        if(file.exists()){
	        	return NBTJsonUtil.LoadFile(file);
	        }
		} catch (Exception e) {
			LogWriter.error("Error loading: " + filename, e);
		}

		return new NBTTagCompound();
	}
	
	public void savePlayerData(PlayerData data){
		final NBTTagCompound compound = data.getNBT();
		final String filename = data.uuid + ".json";
		nameUUIDs.put(data.playername, data.uuid);
		CustomNPCsScheduler.runTack(new Runnable(){
			@Override
			public void run() {
				try {
					File saveDir = getSaveDir();
		            File file = new File(saveDir, filename+"_new");
		            File file1 = new File(saveDir, filename);
		            NBTJsonUtil.SaveFile(file, compound);
		            if(file1.exists()){
		                file1.delete();
		            }
		            file.renameTo(file1);
				} catch (Exception e) {
					LogWriter.except(e);
				}
			}
		});
	}
	
	public PlayerBankData getBankData(EntityPlayer player, int bankId) {
		Bank bank = BankController.getInstance().getBank(bankId);
		PlayerBankData data = getPlayerData(player).bankData;
		if(!data.hasBank(bank.id)){
			data.loadNew(bank.id);
		}
		return data;
	}
	
	public PlayerData getPlayerData(EntityPlayer player){
		PlayerData data = (PlayerData) player.getExtendedProperties("CustomNpcsData");
		if(data == null){
			player.registerExtendedProperties("CustomNpcsData", data = new PlayerData());
			data.player = player;
			data.loadNBTData(null);
		}
		data.player = player;
		return data;
	}
	public String hasPlayer(String username) {
        for(String name : nameUUIDs.keySet()){
        	if(name.equalsIgnoreCase(username))
        		return name;
        }
		
		return "";
	}
	
	public PlayerData getDataFromUsername(String username){
		EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
		PlayerData data = null;
		if(player == null){
			for(String name : nameUUIDs.keySet()){
				if(name.equalsIgnoreCase(username)){
					data = new PlayerData();
					data.setNBT(loadPlayerData(nameUUIDs.get(name)));
					break;
				}
			}
		}
		else
			data = getPlayerData(player);
		
		return data;
	}
	
	public void addPlayerMessage(String username, PlayerMail mail) {
		mail.time = System.currentTimeMillis();
		
		EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
		PlayerData data = getDataFromUsername(username);
		data.mailData.playermail.add(mail.copy());
		savePlayerData(data);
	}
	
	private Map<String, String> getUsernameData(){
		Map<String, String> map = new HashMap<String, String>();
        for(File file : getSaveDir().listFiles()){
        	if(file.isDirectory() || !file.getName().endsWith(".json"))
        		continue;
			try {
				NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
	        	if(compound.hasKey("PlayerName")){
	        		map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 5));
	        	}
			} catch (Exception e) {
				LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			}
        }
		return map;
	}
	
	public boolean hasMail(EntityPlayer player) {
		return getPlayerData(player).mailData.hasMail();
	}
}
