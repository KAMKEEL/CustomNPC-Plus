package noppes.npcs.controllers;

import me.luizotavio.compressor.ZstdCompressor;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTSizeTracker;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.NBTJsonUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.StandardOpenOption;
import java.util.HashMap;
import java.util.Map;

import static me.luizotavio.compressor.executor.IOExecutor.IO_EXECUTOR;

public class PlayerDataController {		
	public static PlayerDataController instance;
	
	public PlayerDataController(){
		instance = this;
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
		if(filename.isEmpty()) {
			return new NBTTagCompound();
		}

		if(CustomNpcs.CompressorType.equalsIgnoreCase("zstd")) {
			filename += ".zstd";
		} else {
			filename += ".json";
		}

		try {
			return ensureCompressor(
				new File(saveDir, filename)
			);
		} catch (Exception e) {
			LogWriter.error("Error loading: " + filename, e);
		}

		return new NBTTagCompound();
	}
	
	public void savePlayerData(PlayerData data){
		NBTTagCompound compound = data.getNBT();
		String filename;

		if (CustomNpcs.CompressorType.equalsIgnoreCase("zstd")) {
			filename = data.uuid + ".zstd";
		} else  {
			filename = data.uuid + ".json";
		}

		try {
			File saveDir = getSaveDir();

            File file = new File(saveDir, filename);

			if (file.exists()) {
				file.delete();
			}

			writeCompressor(file, compound);
		} catch (Exception e) {
			LogWriter.except(e);
		}
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
        for(String name : getUsernameData().keySet()){
        	if(name.equalsIgnoreCase(username))
        		return name;
        }
		
		return "";
	}
	
	public PlayerData getDataFromUsername(String username){
		EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
		PlayerData data = null;
		if(player == null){
			Map<String, NBTTagCompound> map = getUsernameData();
			for(String name : map.keySet()){
				if(name.equalsIgnoreCase(username)){
					data = new PlayerData();
					data.setNBT(map.get(name));
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
	
	public Map<String, NBTTagCompound> getUsernameData(){
		Map<String, NBTTagCompound> map = new HashMap<String, NBTTagCompound>();
        for(File file : getSaveDir().listFiles()){
        	if(file.isDirectory() || !file.getName().endsWith(".json"))
        		continue;
			try {
				NBTTagCompound compound = ensureCompressor(file);

	        	if(compound.hasKey("PlayerName")){
	        		map.put(compound.getString("PlayerName"), compound);
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

	private void writeCompressor(File file, NBTTagCompound compound) {
		if (compound == null) {
			throw new IllegalArgumentException("Compound cannot be null!");
		}

		if (CustomNpcs.CompressorType.equalsIgnoreCase("json")) {
			IO_EXECUTOR.submit(() -> {
				try {
					NBTJsonUtil.SaveFile(file, compound);
				} catch (IOException | JsonException e) {
					e.printStackTrace();
				}
			});
		} else if (CustomNpcs.CompressorType.equalsIgnoreCase("zstd")) {
			IO_EXECUTOR.submit(() -> {
				try {
					byte[] compressed = ZstdCompressor.writeCompound(compound);

					Files.write(
						file.toPath(),
						compressed,
						StandardOpenOption.CREATE,
						StandardOpenOption.WRITE
					);
				} catch (Exception e) {
					e.printStackTrace();
				}
			});
		}
	}

	private NBTTagCompound ensureCompressor(File file) throws JsonException, IOException {
		if (!file.exists()) {
			return new NBTTagCompound();
		}

		if (CustomNpcs.CompressorType.equalsIgnoreCase("json")) {
			return NBTJsonUtil.LoadFile(file);
		}

		if (CustomNpcs.CompressorType.equalsIgnoreCase("zstd")) {
			byte[] readCompressed = ZstdCompressor.readCompressed(
				new RandomAccessFile(file, "r")
			);

			try (DataInputStream dataInputStream = new DataInputStream(new ByteArrayInputStream(readCompressed))) {
				return CompressedStreamTools.func_152456_a(dataInputStream, NBTSizeTracker.field_152451_a);
			}
		}

		throw new IOException("Unknown compressor type: " + CustomNpcs.CompressorType);
	}
}
