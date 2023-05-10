package noppes.npcs.controllers;

import net.minecraft.command.ICommandSender;
import net.minecraft.command.PlayerSelector;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.*;
import noppes.npcs.util.NBTJsonUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.Executor;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

public class PlayerDataController {
	public static PlayerDataController instance;
	public HashMap<String, String> nameUUIDs;
	private static final Executor playerDataThread = Executors.newSingleThreadExecutor();

	public PlayerDataController(){
		instance = this;
		File dir = getSaveDir();

		LogWriter.info("Loading PlayerData...");
		// If no PlayerData MAP - Load PlayerData to create PlayerData Map
		if(!getPlayerDataMap()){
			LogWriter.info("Generating PlayerData map file...");
			File[] files = dir.listFiles(); // Get an array of all files in the directory
			HashMap<String, String> map = new HashMap<String, String>();
			if(files != null){
				int length = files.length;
				if(length != 0){
					if(length > 100){
						LogWriter.info("Found " + length + " PlayerData files... This may take a few minutes");
					}
					int tenPercent = (int) ((double) length * 0.1);
					int progress = 0;
					// Load the files in parallel using a stream
					for(int i = 0; i < length; i++){
						File file = files[i];
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
						if(tenPercent != 0 ){
							if(progress != 100){
								if (i % tenPercent == 0) {
									progress += 10;
									LogWriter.info("Creating PlayerMap: Progress: " + progress + "%");
								}
							}
						}
					}
				}
			}
			nameUUIDs = map;
			savePlayerDataMap();
		}

		LogWriter.info("Done loading PlayerData");
	}

	public void generatePlayerMap(EntityPlayerMP sender){
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			if(sender != null){
				LogWriter.info("PlayerMap regeneration queued by " + sender.getCommandSenderName());
				sender.addChatMessage(new ChatComponentText("You have initiated PlayerMap regeneration"));
			}
			nameUUIDs.clear();

			File dir = getSaveDir();
			LogWriter.info("Generating PlayerData map file...");
			File[] files = dir.listFiles(); // Get an array of all files in the directory
			HashMap<String, String> map = new HashMap<String, String>();
			if(files != null){
				int length = files.length;
				if(length != 0){
					if(length > 100){
						LogWriter.info("Found " + length + " PlayerData files... This may take a few minutes");
					}
					int tenPercent = (int) ((double) length * 0.1);
					int progress = 0;
					// Load the files in parallel using a stream
					for(int i = 0; i < length; i++){
						File file = files[i];
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
						if(tenPercent != 0 ){
							if(progress != 100){
								if (i % tenPercent == 0) {
									progress += 10;
									LogWriter.info("Creating PlayerMap: Progress: " + progress + "%");
								}
							}
						}
					}
				}
			}
			nameUUIDs = map;
			savePlayerDataMap();

			if(sender != null){
				sender.addChatMessage(new ChatComponentText("PlayerMap regeneration complete"));
			}
		});

		executor.shutdown();
	}

	public boolean getPlayerDataMap(){
		try {
			File file = new File(getSaveDir(), "___playermap.dat");
			if(file.exists()){
				loadPlayerDataMap(file);
				return true;
			}
		} catch (Exception e) {
			try {
				File file = new File(getSaveDir(), "___playermap.dat_old");
				if(file.exists()){
					loadPlayerDataMap(file);
					return true;
				}
			} catch (Exception ignored) {}
		}
		return false;
	}

	private void loadPlayerDataMap(File file) throws IOException {
		DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		loadPlayerDatasMap(var1);
		var1.close();
	}


	public void loadPlayerDatasMap(DataInputStream stream) throws IOException{
		NBTTagCompound nbttagcompound = CompressedStreamTools.read(stream);
		readNBT(nbttagcompound);
	}

	public void savePlayerDataMap(){
		try {
			File saveDir = getSaveDir();
			File file = new File(saveDir, "___playermap.dat_new");
			File file1 = new File(saveDir, "___playermap.dat_old");
			File file2 = new File(saveDir, "___playermap.dat");
			CompressedStreamTools.writeCompressed(writeNBT(), new FileOutputStream(file));
			if(file1.exists())
			{
				file1.delete();
			}
			file2.renameTo(file1);
			if(file2.exists())
			{
				file2.delete();
			}
			file.renameTo(file2);
			if(file.exists())
			{
				file.delete();
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
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
		filename += ".json";
		try {
			File file = new File(saveDir, filename);
			if(file.exists()){
				return NBTJsonUtil.LoadFile(file);
			}
		} catch (Exception e) {
			LogWriter.error("Error loading: " + filename, e);
		}

		return new NBTTagCompound();
	}

	public void savePlayerData(PlayerData data){
		NBTTagCompound compound = data.getNBT();
		playerDataThread.execute(() -> {
			String filename = data.uuid + ".json";
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

			nameUUIDs.put(data.playername, data.uuid);
			savePlayerDataMap();
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
			data.loadPlayerDataFromFile();
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
					data.setNBT(PlayerData.loadPlayerData(nameUUIDs.get(name)));
					break;
				}
			}
		}
		else
			data = PlayerDataController.instance.getPlayerData(player);

		return data;
	}

	public void addPlayerMessage(String username, PlayerMail mail) {
		mail.time = System.currentTimeMillis();

		EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
		PlayerData data = getDataFromUsername(username);
		data.mailData.playermail.add(mail.copy());
		savePlayerData(data);
	}

	public List<PlayerData> getPlayersData(ICommandSender sender, String username){
		ArrayList<PlayerData> list = new ArrayList<PlayerData>();
		EntityPlayerMP[] players = PlayerSelector.matchPlayers(sender, username);
		if(players == null || players.length == 0){
			PlayerData data = PlayerDataController.instance.getDataFromUsername(username);
			if(data != null)
				list.add(data);
		}
		else{
			for(EntityPlayer player : players){
				list.add(PlayerDataController.instance.getPlayerData(player));
			}
		}

		return list;
	}

	public boolean hasMail(EntityPlayer player) {
		return getPlayerData(player).mailData.hasMail();
	}
	public void readNBT(NBTTagCompound compound){
		this.nameUUIDs = new HashMap<String, String>();
		NBTTagList list = compound.getTagList("PlayerDataMap", 10);
		if(list != null){
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
				String playerName = nbttagcompound.getString("Name");
				String uuid = nbttagcompound.getString("UUID");
				nameUUIDs.put(playerName, uuid);
			}
		}
	}

	public NBTTagCompound writeNBT(){
		NBTTagCompound nbt = new NBTTagCompound();
		NBTTagList playerList = new NBTTagList();
		for(String key: nameUUIDs.keySet()){
			NBTTagCompound playerCompound = new NBTTagCompound();
			playerCompound.setString("Name", key);
			playerCompound.setString("UUID", nameUUIDs.get(key));
			playerList.appendTag(playerCompound);

		}
		nbt.setTag("PlayerDataMap", playerList);
		return nbt;
	}
}