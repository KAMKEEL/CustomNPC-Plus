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
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.data.*;
import noppes.npcs.util.CacheHashMap;
import noppes.npcs.util.NBTJsonUtil;

import java.io.*;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import static noppes.npcs.util.CustomNPCsThreader.playerDataThread;

public class PlayerDataController {
	public static PlayerDataController Instance;
	public HashMap<String, String> nameUUIDs;
	private final CacheHashMap<String, CacheHashMap.CachedObject<PlayerData>> playerDataCache = new CacheHashMap<>(60 * 60 * 1000);

	public PlayerDataController(){
		Instance = this;
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
						if(file.isDirectory() || (!file.getName().endsWith(".json") && !file.getName().endsWith(".dat")))
							continue;
						try {
							if(file.getName().endsWith(".json")){
								NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
								if(compound.hasKey("PlayerName")){
									map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 5));
								}
							}
							if(file.getName().endsWith(".dat")){
								NBTTagCompound compound = loadNBTData(file);
								if(compound.hasKey("PlayerName")){
									map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 4));
								}
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

	public boolean getPlayerDataMap(){
		try {
			File file = new File(CustomNpcs.getWorldSaveDirectory(), "playerdatamap.dat");
			if(file.exists()){
				loadPlayerDataMap(file);
				return true;
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
		try {
			File file = new File(CustomNpcs.getWorldSaveDirectory(), "playerdatamap.dat_old");
			if(file.exists()){
				loadPlayerDataMap(file);
				return true;
			}
		} catch (Exception e2) {
			LogWriter.except(e2);
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

	public synchronized void savePlayerDataMap(){
		playerDataThread.execute(() -> {
			try {
				File saveDir = CustomNpcs.getWorldSaveDirectory();
				File file = new File(saveDir, "playerdatamap.dat_new");
				File file1 = new File(saveDir, "playerdatamap.dat_old");
				File file2 = new File(saveDir, "playerdatamap.dat");
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
		});
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

	public File getNewSaveDir(){
		try{
			File file = new File(CustomNpcs.getWorldSaveDirectory(),"playerdata_new");
			if(file.exists()){
				return null;
			}
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

		if(ConfigMain.DatFormat){
			filename += ".dat";
		} else {
			filename += ".json";
		}
		try {
			File file = new File(saveDir, filename);
			if(file.exists()){
				if(ConfigMain.DatFormat){
					return loadNBTData(file);
				} else {
					return NBTJsonUtil.LoadFile(file);
				}
			}
		} catch (Exception e) {
			LogWriter.error("Error loading: " + filename, e);
		}

		return new NBTTagCompound();
	}

	public NBTTagCompound loadNBTData(File file){
		try {
			return CompressedStreamTools.readCompressed(new FileInputStream(file));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getName(), e);
		}
		return new NBTTagCompound();
	}

	public void putPlayerDataCache(final String uuid, final PlayerData playerCompound) {
		synchronized (playerDataCache) {
			playerDataCache.put(uuid, new CacheHashMap.CachedObject<>(playerCompound));
		}
	}

	public PlayerData getPlayerDataCache(final String uuid) {
		synchronized (playerDataCache) {
			if (!playerDataCache.containsKey(uuid)) {
				return null;
			}
			return playerDataCache.get(uuid).getObject();
		}
	}

	public void removePlayerDataCache(final String uuid) {
		synchronized (playerDataCache) {
			playerDataCache.remove(uuid);
		}
	}

	public void clearCache() {
		synchronized (playerDataCache) {
			playerDataCache.clear();
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

	public ArrayList<PlayerData> getAllPlayerData() {
		ArrayList<PlayerData> playerDataList = new ArrayList<>();
		List<?> list = MinecraftServer.getServer().getConfigurationManager().playerEntityList;
		for (Object o : list) {
			if (o instanceof EntityPlayer) {
				playerDataList.add(this.getPlayerData((EntityPlayer)o));
			}
		}
		return playerDataList;
	}

	public PlayerData getPlayerData(EntityPlayer player){
		PlayerData data = getPlayerDataCache(player.getUniqueID().toString());
		if(data != null){
			data.player = player;
			return data;
		}

		data = (PlayerData) player.getExtendedProperties("CustomNpcsData");
		if(data == null){
			player.registerExtendedProperties("CustomNpcsData", data = new PlayerData());
			data.player = player;
			data.load();
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
					data.setNBT(PlayerDataController.Instance.loadPlayerData(nameUUIDs.get(name)));
					break;
				}
			}
		}
		else
			data = PlayerDataController.Instance.getPlayerData(player);

		return data;
	}

	public void addPlayerMessage(String username, PlayerMail mail) {
		mail.time = System.currentTimeMillis();

		EntityPlayer player = MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
		PlayerData data = getDataFromUsername(username);
		data.mailData.playermail.add(mail.copy());
		data.save();
	}

	public List<PlayerData> getPlayersData(ICommandSender sender, String username){
		ArrayList<PlayerData> list = new ArrayList<PlayerData>();
		EntityPlayerMP[] players = PlayerSelector.matchPlayers(sender, username);
		if(players == null || players.length == 0){
			PlayerData data = PlayerDataController.Instance.getDataFromUsername(username);
			if(data != null)
				list.add(data);
		}
		else{
			for(EntityPlayer player : players){
				list.add(PlayerDataController.Instance.getPlayerData(player));
			}
		}

		return list;
	}

	public void putPlayerMap(String playerName, String uuid) {
		nameUUIDs.put(playerName, uuid);
		savePlayerDataMap();
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
						if(file.isDirectory() || (!file.getName().endsWith(".json") && !file.getName().endsWith(".dat")))
							continue;
						try {
							if(file.getName().endsWith(".json")){
								NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
								if(compound.hasKey("PlayerName")){
									map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 5));
								}
							}
							if(file.getName().endsWith(".dat")){
								NBTTagCompound compound = loadNBTData(file);
								if(compound.hasKey("PlayerName")){
									map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 4));
								}
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

	public void convertPlayerFiles(final EntityPlayerMP sender, final boolean type){
		ExecutorService executor = Executors.newSingleThreadExecutor();
		executor.execute(() -> {
			String fileType;
			if(type){
				fileType = ".dat";
			}
			else {
				fileType = ".json";
			}

			if(sender != null){
				LogWriter.info("PlayerData Conversion queued by " + sender.getCommandSenderName());
				sender.addChatMessage(new ChatComponentText("PlayerData Conversion to " + fileType + " format"));
			}

			File dir = getSaveDir();
			LogWriter.info("Converting PlayerData to " + fileType + " format");
			File[] files = dir.listFiles(); // Get an array of all files in the directory
			if(files != null){
				int length = files.length;
				if(length != 0){
					if(length > 100){
						LogWriter.info("Found " + length + " PlayerData files... This may take a few minutes");
					}
					int tenPercent = (int) ((double) length * 0.1);
					int progress = 0;
					File saveDir = PlayerDataController.Instance.getNewSaveDir();
					if(saveDir == null){
						if(sender != null){
							sender.addChatMessage(new ChatComponentText("playerdata_new folder already exists please delete it or rename it"));
						}
						LogWriter.error("playerdata_new folder already exists please delete it or rename it");
						return;
					}
					// Load the files in parallel using a stream
					for(int i = 0; i < length; i++){
						File file = files[i];
						if(file.isDirectory() || (!file.getName().endsWith(".json") && !file.getName().endsWith(".dat")))
							continue;
						try {
							String filename = "error";
							boolean valid = false;
							NBTTagCompound compound = new NBTTagCompound();
							if(type){
								if(file.getName().endsWith(".json")){
									compound = NBTJsonUtil.LoadFile(file);
									if(compound.hasKey("PlayerName")) {
										filename = file.getName().substring(0, file.getName().length() - 5);
										valid = true;
									}
								}
							} else {
								if(file.getName().endsWith(".dat")){
									compound = loadNBTData(file);
									if(compound.hasKey("PlayerName")){
										filename = file.getName().substring(0, file.getName().length() - 4);
										valid = true;
									}
								}
							}
							if(valid){
								try {
									File newFile = new File(saveDir, filename + "_new" + fileType);
									File oldFile = new File(saveDir, filename + fileType);
									if(type){
										CompressedStreamTools.writeCompressed(compound, new FileOutputStream(newFile));
									} else {
										NBTJsonUtil.SaveFile(newFile, compound);
									}
									if(oldFile.exists()){
										oldFile.delete();
									}
									newFile.renameTo(oldFile);
								} catch (Exception e) {
									LogWriter.except(e);
								}
							}
						} catch (Exception e) {
							LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
						}
						if(tenPercent != 0 ){
							if(progress != 100){
								if (i % tenPercent == 0) {
									progress += 10;
									LogWriter.info("Converting PlayerData: Progress: " + progress + "%");
								}
							}
						}
					}
				}
			}
			if(sender != null){
				sender.addChatMessage(new ChatComponentText("PlayerData Conversion complete"));
			}
			LogWriter.info("PlayerData Converted - Please rename the playerdata_new folder to playerdata");
		});

		executor.shutdown();
	}
}