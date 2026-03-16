package noppes.npcs.controllers;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.data.Bank;
import noppes.npcs.controllers.data.PlayerBankData;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.util.CacheHashMap;
import noppes.npcs.util.NBTJsonUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.zip.GZIPInputStream;

import static noppes.npcs.util.CustomNPCsThreader.customNPCThread;

public class PlayerDataController {
    public static PlayerDataController Instance;
    public HashMap<String, String> nameUUIDs;
    private final CacheHashMap<String, CacheHashMap.CachedObject<PlayerData>> playerDataCache = new CacheHashMap<>(60 * 60 * 1000);

    public PlayerDataController() {
        Instance = this;
        File dir = getSaveDir();

        LogWriter.info("Loading PlayerData...");
        // If no PlayerData MAP - Load PlayerData to create PlayerData Map
        if (!getPlayerDataMap()) {
            LogWriter.info("Generating PlayerData map file...");
            File[] files = dir.listFiles(); // Get an array of all files in the directory
            HashMap<String, String> map = new HashMap<String, String>();
            if (files != null) {
                int length = files.length;
                if (length != 0) {
                    if (length > 100) {
                        LogWriter.info("Found " + length + " PlayerData files... This may take a few minutes");
                    }
                    int tenPercent = (int) ((double) length * 0.1);
                    int progress = 0;
                    // Load the files in parallel using a stream
                    for (int i = 0; i < length; i++) {
                        File file = files[i];
                        if (file.isDirectory() || (!file.getName().endsWith(".json") && !file.getName().endsWith(".dat")))
                            continue;
                        try {
                            if (file.getName().endsWith(".json")) {
                                INbt compound = NBTJsonUtil.LoadFile(file);
                                if (compound.hasKey("PlayerName")) {
                                    map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 5));
                                }
                            }
                            if (file.getName().endsWith(".dat")) {
                                INbt compound = NBTJsonUtil.loadNBTData(file);
                                if (compound.hasKey("PlayerName")) {
                                    map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 4));
                                }
                            }
                        } catch (Exception e) {
                            LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                        }
                        if (tenPercent != 0) {
                            if (progress != 100) {
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

    public boolean getPlayerDataMap() {
        try {
            File file = new File(CustomNpcs.getWorldSaveDirectory(), "playerdatamap.dat");
            if (file.exists()) {
                loadPlayerDataMap(file);
                return true;
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
        try {
            File file = new File(CustomNpcs.getWorldSaveDirectory(), "playerdatamap.dat_old");
            if (file.exists()) {
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


    public void loadPlayerDatasMap(DataInputStream stream) throws IOException {
        INbt INbt = CompressedStreamTools.read(stream);
        readNBT(INbt);
    }

    public synchronized void savePlayerDataMap() {
        customNPCThread.execute(() -> {
            try {
                File saveDir = CustomNpcs.getWorldSaveDirectory();
                File file = new File(saveDir, "playerdatamap.dat_new");
                File file1 = new File(saveDir, "playerdatamap.dat_old");
                File file2 = new File(saveDir, "playerdatamap.dat");
                NBTIO.writeCompressed(writeNBT(), new FileOutputStream(file));
                if (file1.exists()) {
                    file1.delete();
                }
                file2.renameTo(file1);
                if (file2.exists()) {
                    file2.delete();
                }
                file.renameTo(file2);
                if (file.exists()) {
                    file.delete();
                }
            } catch (Exception e) {
                LogWriter.except(e);
            }
        });
    }

    public File getSaveDir() {
        try {
            File file = new File(CustomNpcs.getWorldSaveDirectory(), "playerdata");
            if (!file.exists())
                file.mkdir();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public File getNewSaveDir() {
        try {
            File file = new File(CustomNpcs.getWorldSaveDirectory(), "playerdata_new");
            if (file.exists()) {
                return null;
            }
            file.mkdir();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public INbt loadPlayerDataOld(String player) {
        File saveDir = getSaveDir();
        String filename = player;
        if (filename.isEmpty())
            filename = "noplayername";
        filename += ".dat";
        try {
            File file = new File(saveDir, filename);
            if (file.exists()) {
                INbt comp;
                try (FileInputStream fis = new FileInputStream(file)) {
                    comp = NBTIO.readCompressed(fis);
                }
                file.delete();
                file = new File(saveDir, filename + "_old");
                if (file.exists())
                    file.delete();
                return comp;
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
        try {
            File file = new File(saveDir, filename + "_old");
            if (file.exists()) {
                try (FileInputStream fis = new FileInputStream(file)) {
                    return NBTIO.readCompressed(fis);
                }
            }

        } catch (Exception e) {
            LogWriter.except(e);
        }

        return new INbt();
    }

    public INbt loadPlayerData(String player) {
        File saveDir = getSaveDir();
        String filename = player;
        if (filename.isEmpty())
            filename = "noplayername";

        if (ConfigMain.DatFormat) {
            filename += ".dat";
        } else {
            filename += ".json";
        }
        try {
            File file = new File(saveDir, filename);
            if (file.exists()) {
                if (ConfigMain.DatFormat) {
                    return NBTJsonUtil.loadNBTData(file);
                } else {
                    return NBTJsonUtil.LoadFile(file);
                }
            }
        } catch (Exception e) {
            LogWriter.error("Error loading: " + filename, e);
        }

        return new INbt();
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

    public PlayerBankData getBankData(IPlayer player, int bankId) {
        Bank bank = BankController.getInstance().getBank(bankId);
        PlayerBankData data = PlayerData.get(player).bankData;
        if (!data.hasBank(bank.id)) {
            data.loadNew(bank.id);
        }
        return data;
    }

    public ArrayList<PlayerData> getAllPlayerData() {
        ArrayList<PlayerData> playerDataList = new ArrayList<>();
        List<?> list = PlatformServiceHolder.get().getServer().getConfigurationManager().playerEntityList;
        for (Object o : list) {
            if (o instanceof IPlayer) {
                playerDataList.add(PlayerData.get((IPlayer) o));
            }
        }
        return playerDataList;
    }

    public PlayerData getPlayerData(IPlayer player) {
        PlayerData data = getPlayerDataCache(player.getUniqueID().toString());
        if (data != null) {
            data.player = player;
            return data;
        }

        data = (PlayerData) player.getExtendedProperties("CustomNpcsData");
        if (data == null) {
            player.registerExtendedProperties("CustomNpcsData", data = new PlayerData());
            data.player = player;
            data.scriptData = new PlayerDataScript(player);
            data.load();
        }

        data.player = player;
        return data;
    }

    public static IPlayer getPlayerFromUUID(UUID uuid) {
        MinecraftServer server = PlatformServiceHolder.get().getServer();
        if (server != null) {
            for (Object playerObj : server.getConfigurationManager().playerEntityList) {
                if (playerObj instanceof IPlayer) {
                    IPlayer player = (IPlayer) playerObj;
                    if (player.getUniqueID().equals(uuid)) {
                        return player;
                    }
                }
            }
        }
        return null;
    }

    public String hasPlayer(String username) {
        for (String name : nameUUIDs.keySet()) {
            if (name.equalsIgnoreCase(username))
                return name;
        }

        return "";
    }

    public String getPlayerUUIDFromName(String username) {
        for (String name : nameUUIDs.keySet()) {
            if (name.equalsIgnoreCase(username))
                return nameUUIDs.get(name);
        }

        return "";
    }

    public PlayerData getDataFromUsername(String username) {
        IPlayer player = PlatformServiceHolder.get().getServer().getConfigurationManager().func_152612_a(username);
        PlayerData data = null;
        if (player == null) {
            for (String name : nameUUIDs.keySet()) {
                if (name.equalsIgnoreCase(username)) {
                    data = new PlayerData();
                    data.setNBT(PlayerDataController.Instance.loadPlayerData(nameUUIDs.get(name)));
                    break;
                }
            }
        } else
            data = PlayerData.get(player);

        return data;
    }

    public PlayerData getData(UUID uuid) {
        IPlayer player = getPlayerFromUUID(uuid);
        PlayerData data;
        if (player == null) {
            data = new PlayerData();
            data.setNBT(PlayerDataController.Instance.loadPlayerData(uuid.toString()));
        } else
            data = PlayerData.get(player);
        return data;
    }

    public void addPlayerMessage(String username, PlayerMail mail) {
        mail.time = System.currentTimeMillis();

        IPlayer player = PlatformServiceHolder.get().getServer().getConfigurationManager().func_152612_a(username);
        PlayerData data = getDataFromUsername(username);
        data.mailData.playermail.add(mail.copy());
        data.save();
    }

    public List<PlayerData> getPlayersData(ICommandSender sender, String username) {
        ArrayList<PlayerData> list = new ArrayList<PlayerData>();
        IPlayer[] players = PlayerSelector.matchPlayers(sender, username);
        if (players == null || players.length == 0) {
            PlayerData data = PlayerDataController.Instance.getDataFromUsername(username);
            if (data != null)
                list.add(data);
        } else {
            for (IPlayer player : players) {
                list.add(PlayerData.get(player));
            }
        }

        return list;
    }

    public void putPlayerMap(String playerName, String uuid) {
        nameUUIDs.put(playerName, uuid);
        savePlayerDataMap();
    }

    public boolean hasMail(IPlayer player) {
        return PlayerData.get(player).mailData.hasMail();
    }

    public void readNBT(INbt compound) {
        this.nameUUIDs = new HashMap<String, String>();
        INbtList list = compound.getTagList("PlayerDataMap", 10);
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                INbt INbt = list.getCompoundTagAt(i);
                String playerName = INbt.getString("Name");
                String uuid = INbt.getString("UUID");
                nameUUIDs.put(playerName, uuid);
            }
        }
    }

    public INbt writeNBT() {
        INbt nbt = new INbt();
        INbtList playerList = new INbtList();
        for (String key : nameUUIDs.keySet()) {
            INbt playerCompound = new INbt();
            playerCompound.setString("Name", key);
            playerCompound.setString("UUID", nameUUIDs.get(key));
            playerList.appendTag(playerCompound);

        }
        nbt.setTag("PlayerDataMap", playerList);
        return nbt;
    }

    public void generatePlayerMap(IPlayer sender) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            if (sender != null) {
                LogWriter.info("PlayerMap regeneration queued by " + sender.getCommandSenderName());
                sender.addChatMessage(new ITextComponent("You have initiated PlayerMap regeneration"));
            }
            nameUUIDs.clear();

            File dir = getSaveDir();
            LogWriter.info("Generating PlayerData map file...");
            File[] files = dir.listFiles(); // Get an array of all files in the directory
            HashMap<String, String> map = new HashMap<String, String>();
            if (files != null) {
                int length = files.length;
                if (length != 0) {
                    if (length > 100) {
                        LogWriter.info("Found " + length + " PlayerData files... This may take a few minutes");
                    }
                    int tenPercent = (int) ((double) length * 0.1);
                    int progress = 0;
                    // Load the files in parallel using a stream
                    for (int i = 0; i < length; i++) {
                        File file = files[i];
                        if (file.isDirectory() || (!file.getName().endsWith(".json") && !file.getName().endsWith(".dat")))
                            continue;
                        try {
                            if (file.getName().endsWith(".json")) {
                                INbt compound = NBTJsonUtil.LoadFile(file);
                                if (compound.hasKey("PlayerName")) {
                                    map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 5));
                                }
                            }
                            if (file.getName().endsWith(".dat")) {
                                INbt compound = NBTJsonUtil.loadNBTData(file);
                                if (compound.hasKey("PlayerName")) {
                                    map.put(compound.getString("PlayerName"), file.getName().substring(0, file.getName().length() - 4));
                                }
                            }
                        } catch (Exception e) {
                            LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                        }
                        if (tenPercent != 0) {
                            if (progress != 100) {
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
            if (sender != null) {
                sender.addChatMessage(new ITextComponent("PlayerMap regeneration complete"));
            }
        });

        executor.shutdown();
    }

    public void convertPlayerFiles(final IPlayer sender, final boolean type) {
        ExecutorService executor = Executors.newSingleThreadExecutor();
        executor.execute(() -> {
            String fileType;
            if (type) {
                fileType = ".dat";
            } else {
                fileType = ".json";
            }

            if (sender != null) {
                LogWriter.info("PlayerData Conversion queued by " + sender.getCommandSenderName());
                sender.addChatMessage(new ITextComponent("PlayerData Conversion to " + fileType + " format"));
            }

            File dir = getSaveDir();
            LogWriter.info("Converting PlayerData to " + fileType + " format");
            File[] files = dir.listFiles(); // Get an array of all files in the directory
            if (files != null) {
                int length = files.length;
                if (length != 0) {
                    if (length > 100) {
                        LogWriter.info("Found " + length + " PlayerData files... This may take a few minutes");
                    }
                    int tenPercent = (int) ((double) length * 0.1);
                    int progress = 0;
                    File saveDir = PlayerDataController.Instance.getNewSaveDir();
                    if (saveDir == null) {
                        if (sender != null) {
                            sender.addChatMessage(new ITextComponent("playerdata_new folder already exists please delete it or rename it"));
                        }
                        LogWriter.error("playerdata_new folder already exists please delete it or rename it");
                        return;
                    }
                    // Load the files in parallel using a stream
                    for (int i = 0; i < length; i++) {
                        File file = files[i];
                        if (file.isDirectory() || (!file.getName().endsWith(".json") && !file.getName().endsWith(".dat")))
                            continue;
                        try {
                            String filename = "error";
                            boolean valid = false;
                            INbt compound = new INbt();
                            if (type) {
                                if (file.getName().endsWith(".json")) {
                                    compound = NBTJsonUtil.LoadFile(file);
                                    if (compound.hasKey("PlayerName")) {
                                        filename = file.getName().substring(0, file.getName().length() - 5);
                                        valid = true;
                                    }
                                }
                            } else {
                                if (file.getName().endsWith(".dat")) {
                                    compound = NBTJsonUtil.loadNBTData(file);
                                    if (compound.hasKey("PlayerName")) {
                                        filename = file.getName().substring(0, file.getName().length() - 4);
                                        valid = true;
                                    }
                                }
                            }
                            if (valid) {
                                try {
                                    File newFile = new File(saveDir, filename + "_new" + fileType);
                                    File oldFile = new File(saveDir, filename + fileType);
                                    if (type) {
                                        NBTIO.writeCompressed(compound, new FileOutputStream(newFile));
                                    } else {
                                        NBTJsonUtil.SaveFile(newFile, compound);
                                    }
                                    if (oldFile.exists()) {
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
                        if (tenPercent != 0) {
                            if (progress != 100) {
                                if (i % tenPercent == 0) {
                                    progress += 10;
                                    LogWriter.info("Converting PlayerData: Progress: " + progress + "%");
                                }
                            }
                        }
                    }
                }
            }
            if (sender != null) {
                sender.addChatMessage(new ITextComponent("PlayerData Conversion complete"));
            }
            LogWriter.info("PlayerData Converted - Please rename the playerdata_new folder to playerdata");
        });

        executor.shutdown();
    }
}
