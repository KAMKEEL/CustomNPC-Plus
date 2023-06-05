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
import net.minecraft.util.ResourceLocation;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.client.renderer.ImageData;
import noppes.npcs.config.ConfigClient;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.data.*;
import noppes.npcs.util.CacheHashMap;
import noppes.npcs.util.CustomNPCsThreader;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.NBTJsonUtil;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.*;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.GZIPInputStream;

import static noppes.npcs.util.CustomNPCsThreader.playerDataThread;

public class PlayerDataController {
	public static PlayerDataController instance;
	public static HashMap<String, String> nameUUIDs;
	private static final CacheHashMap<String, CacheHashMap.CachedObject<PlayerData>> playerDataCache = new CacheHashMap<>(60 * 60 * 1000);

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


	public synchronized void savePlayerDataMap() {
		playerDataThread.execute(() -> {
			try {
				File saveDir = CustomNpcs.getWorldSaveDirectory();
				Path saveDirPath = saveDir.toPath();
				Path newFile = saveDirPath.resolve("playerdatamap.dat_new");
				Path oldFile = saveDirPath.resolve("playerdatamap.dat_old");
				Path currentFile = saveDirPath.resolve("playerdatamap.dat");

				Files.deleteIfExists(oldFile);
				Files.move(currentFile, oldFile, StandardCopyOption.REPLACE_EXISTING);
				Files.move(newFile, currentFile, StandardCopyOption.REPLACE_EXISTING);
				Files.deleteIfExists(newFile);
			} catch (IOException e) {
				LogWriter.except(e);
			}
		});
	}

	public File getSaveDir() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			Path directory = Paths.get(saveDir.getAbsolutePath(), "playerdata");
			Files.createDirectories(directory);
			return directory.toFile();
		} catch (IOException e) {
			e.printStackTrace();
			return null;
		}
	}

	public File getNewSaveDir() {
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			Path directory = Paths.get(saveDir.getAbsolutePath(), "playerdata_new");
			if (Files.exists(directory)) {
				return null;
			}
			Files.createDirectories(directory);
			return directory.toFile();
		} catch (IOException e) {
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
			Path filePath = saveDir.toPath().resolve(filename);
			if (Files.exists(filePath)) {
				NBTTagCompound comp = CompressedStreamTools.readCompressed(Files.newInputStream(filePath));
				Files.deleteIfExists(filePath);
				Path oldFilePath = saveDir.toPath().resolve(filename + "_old");
				Files.deleteIfExists(oldFilePath);
				return comp;
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
		try {
			Path oldFilePath = saveDir.toPath().resolve(filename + "_old");
			if (Files.exists(oldFilePath)) {
				return CompressedStreamTools.readCompressed(Files.newInputStream(oldFilePath));
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
			return CompressedStreamTools.readCompressed(Files.newInputStream(file.toPath()));
		} catch (Exception e) {
			LogWriter.error("Error loading: " + file.getName(), e);
		}
		return new NBTTagCompound();
	}

	public static void putPlayerDataCache(final String uuid, final PlayerData playerCompound) {
		synchronized (playerDataCache) {
			playerDataCache.put(uuid, new CacheHashMap.CachedObject<>(playerCompound));
		}
	}

	public static PlayerData getPlayerDataCache(final String uuid) {
		synchronized (playerDataCache) {
			if (!playerDataCache.containsKey(uuid)) {
				return null;
			}
			return playerDataCache.get(uuid).getObject();
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
					data.setNBT(PlayerDataController.instance.loadPlayerData(nameUUIDs.get(name)));
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
		data.save();
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

	public void convertPlayerFiles(final EntityPlayerMP sender, final boolean type) {
		// Determine the file type
		String fileType = type ? ".dat" : ".json";

		if (sender != null) {
			LogWriter.info("PlayerData Conversion queued by " + sender.getCommandSenderName());
			sender.addChatMessage(new ChatComponentText("PlayerData Conversion to " + fileType + " format"));
		}

		File saveDir = getSaveDir();
		File[] files = saveDir.listFiles();

		if (files != null && files.length > 0) {
			File newSaveDir = PlayerDataController.instance.getNewSaveDir();

			if (newSaveDir == null) {
				if (sender != null) {
					sender.addChatMessage(new ChatComponentText("playerdata_new folder already exists please delete it or rename it"));
				}
				LogWriter.error("playerdata_new folder already exists please delete it or rename it");
				return;
			}

			//Determine the number of threads to be used
			int numThreads = Runtime.getRuntime().availableProcessors();
			ExecutorService executor = Executors.newFixedThreadPool(numThreads);

			//Calculate the number of files to process per thread
			int numFilesPerThread = files.length / numThreads;
			int remainingFiles = files.length % numThreads;

			int startIndex = 0;
			int endIndex = numFilesPerThread;

			// Submit conversion tasks to the thread pool
			for (int i = 0; i < numThreads; i++) {
				if (i == numThreads - 1) {
					endIndex += remainingFiles;
				}

				File[] filesToConvert = Arrays.copyOfRange(files, startIndex, endIndex);
				startIndex = endIndex;
				endIndex += numFilesPerThread;

				executor.execute(() -> {
					convertFiles(filesToConvert, type, fileType, newSaveDir, sender);
				});
			}

			executor.shutdown();
			try {
				executor.awaitTermination(Long.MAX_VALUE, TimeUnit.NANOSECONDS); //Wait for all the threads to complete the task
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}

		if (sender != null) {
			sender.addChatMessage(new ChatComponentText("PlayerData Conversion complete"));
		}
		LogWriter.info("PlayerData Converted - Please rename the playerdata_new folder to playerdata");
	}


	//Check if a file has a valid extension based on the file type
	private boolean isValidFile(File file, boolean type) {
		String extension = type ? ".json" : ".dat";
		return file.getName().toLowerCase().endsWith(extension);
	}

	//Extract the filename without the extension based on the file type
	private String getFilename(String fileName, boolean type) {
		String extension = type ? ".json" : ".dat";
		return fileName.substring(0, fileName.length() - extension.length());
	}
	private void convertFiles(File[] files, boolean type, String fileType, File newSaveDir, EntityPlayerMP sender) {
		int totalFiles = files.length;
		int tenPercent = (int) (totalFiles * 0.1);
		int progress = 0;

		for (File file : files) {
			//Skip thr files with invalid extensions
			if (file.isDirectory() || !isValidFile(file, type)) {
				continue;
			}
			try {
				String filename = getFilename(file.getName(), type);
				NBTTagCompound compound = type ? NBTJsonUtil.LoadFile(file) : loadNBTData(file);
				convertFile(fileType, newSaveDir, type, compound, filename);
			} catch (Exception e) {
				LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			}

			if (tenPercent != 0 && progress != 100 && (progress % tenPercent == 0)) {
				progress += 10;
				LogWriter.info("Converting PlayerData: Progress: " + progress + "%");
			}
		}
	}

	//Convert a file and replace the old file with the new one
	private void convertFile(String fileType, File newSaveDir, boolean type, NBTTagCompound compound, String filename) throws IOException, JsonException {
		File newFile = new File(newSaveDir, filename + "_new" + fileType);
		File oldFile = new File(newSaveDir, filename + fileType);

		Path newFilePath = newFile.toPath();
		if (type) {
			try (OutputStream outputStream = Files.newOutputStream(newFilePath)) {
				CompressedStreamTools.writeCompressed(compound, outputStream);
			}
		} else {
			NBTJsonUtil.SaveFile(newFile, compound);
		}
		Files.move(newFilePath, oldFile.toPath(), StandardCopyOption.REPLACE_EXISTING);
	}
}