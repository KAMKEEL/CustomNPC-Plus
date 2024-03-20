package foxz.utils;

import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.ChatComponentText;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.roles.RoleTrader;
import noppes.npcs.util.CacheHashMap;
import noppes.npcs.util.MarketCachedObject;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import static noppes.npcs.util.CustomNPCsThreader.customNPCThread;

public class Market {

    private static final CacheHashMap<String, MarketCachedObject> marketCache = new CacheHashMap<>(5 * 60 * 1000, 1 * 60 * 1000);

    public static void save(RoleTrader r, String name) {
        if(name.isEmpty())
            return;

        NBTTagCompound roleCompound = r.writeNBT(new NBTTagCompound());
        roleCompound.setString("SaveName", name);
        putMarketCache(name, roleCompound);
        if(r.recordHistory)
            return;

        saveFile(roleCompound);
    }

    public static void saveFile(NBTTagCompound compound) {
        String name = compound.getString("SaveName");
        File file = getFile(name + "_new");
        File file1 = getFile(name);
        customNPCThread.execute(() -> {
            try {
                if(ConfigMain.MarketDatFormat){
                    CompressedStreamTools.writeCompressed(compound, new FileOutputStream(file));
                } else {
                    NBTJsonUtil.SaveFile(file, compound);
                }
                if(file1.exists()){
                    file1.delete();
                }
                file.renameTo(file1);
            } catch (Exception e) {

            }
        });
    }

    public static void getMarket(RoleTrader role, String name){
        NBTTagCompound data = getMarketCache(name);
        if(data != null){
            role.readNBT(data);
            return;
        }

        load(role, name);
    }

    public static NBTTagCompound getMarketCache(final String name) {
        synchronized (marketCache) {
            if (!marketCache.containsKey(name)) {
                return null;
            }
            return marketCache.get(name).getObject();
        }
    }

    public static void putMarketCache(final String name, final NBTTagCompound role) {
        synchronized (marketCache) {
            marketCache.put(name, new MarketCachedObject(role));
        }
    }


    static public void load(RoleTrader role, String name){
        if(role.npc.worldObj.isRemote)
            return;
        File file = getFile(name);
        if(!file.exists())
            return;

        try {
            if(ConfigMain.MarketDatFormat){
                NBTTagCompound compound = NBTJsonUtil.loadNBTData(file);
                role.readNBT(compound);
            }
            else {
                role.readNBT(NBTJsonUtil.LoadFile(file));
            }
        } catch (Exception e) {
        }
    }

    public static File getMarketDir(){
        try{
            File file = new File(CustomNpcs.getWorldSaveDirectory(),"markets");
            if(!file.exists())
                file.mkdir();
            return file;
        }
        catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getNewMarketDir(){
        try{
            File file = new File(CustomNpcs.getWorldSaveDirectory(),"markets_new");
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

    private static File getFile(String name){
        String filename = name.toLowerCase();
        if(ConfigMain.MarketDatFormat){
            filename += ".dat";
        } else {
            filename += ".json";
        }

        return new File(getMarketDir(), filename);
    }

    static public void setMarket(EntityNPCInterface npc, String marketName) {
        if(marketName.isEmpty())
            return;
        if(!getFile(marketName).exists())
            Market.save((RoleTrader) npc.roleInterface, marketName);

        Market.getMarket((RoleTrader) npc.roleInterface, marketName);
    }


    public static void convertMarketFiles(final EntityPlayerMP sender, final boolean type){
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
                LogWriter.info("Market Conversion queued by " + sender.getCommandSenderName());
                sender.addChatMessage(new ChatComponentText("Market Conversion to " + fileType + " format"));
            }

            File dir = getMarketDir();
            LogWriter.info("Converting Market to " + fileType + " format");
            File[] files = dir.listFiles(); // Get an array of all files in the directory
            if(files != null){
                int length = files.length;
                if(length != 0){
                    if(length > 100){
                        LogWriter.info("Found " + length + " Market files... This may take a few minutes");
                    }
                    int tenPercent = (int) ((double) length * 0.1);
                    int progress = 0;
                    File saveDir = getNewMarketDir();
                    if(saveDir == null){
                        if(sender != null){
                            sender.addChatMessage(new ChatComponentText("markets_new folder already exists please delete it or rename it"));
                        }
                        LogWriter.error("markets_new folder already exists please delete it or rename it");
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
                                    compound = NBTJsonUtil.loadNBTData(file);
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
                                    LogWriter.info("Converting Market: Progress: " + progress + "%");
                                }
                            }
                        }
                    }
                }
            }
            if(sender != null){
                sender.addChatMessage(new ChatComponentText("Market Conversion complete"));
            }
            LogWriter.info("Market Converted - Please rename the markets_new folder to markets and restart");
        });

        executor.shutdown();
    }

}
