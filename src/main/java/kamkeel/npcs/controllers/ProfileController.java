package kamkeel.npcs.controllers;

import kamkeel.npcs.controllers.data.IProfileData;
import kamkeel.npcs.controllers.data.Profile;
import kamkeel.npcs.controllers.data.ProfileOperation;
import kamkeel.npcs.controllers.data.Slot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.util.CustomNPCsThreader;
import noppes.npcs.util.NBTJsonUtil;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.*;

public class ProfileController {

    public static HashMap<String, IProfileData> profileTypes;
    public static HashMap<UUID, Profile> activeProfiles;
    public static String profile_directory = "profiles";

    public ProfileController(){
        profileTypes = new HashMap<>();
        activeProfiles = new HashMap<>();
    }

    // ---------- Registration & I/O ----------
    public static boolean registerProfileType(IProfileData type){
        if(profileTypes.containsKey(type.getTagName()))
            return false;
        profileTypes.put(type.getTagName(), type);
        return true;
    }

    public static File getProfileDir(){
        try {
            File file = new File(CustomNpcs.getWorldSaveDirectory(), profile_directory);
            if(!file.exists())
                file.mkdir();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // Returns the backup folder for profiles.
    public static File getBackupDir() {
        File base = getProfileDir();
        File backup = new File(base, "backup");
        if(!backup.exists()){
            backup.mkdir();
        }
        return backup;
    }

    // Login for online players.
    // If no slots exist, create default slot 0 and save the player's current IProfileData into it.
    public static synchronized void login(EntityPlayer player){
        if(player == null)
            return;
        Profile profile;
        if(activeProfiles.containsKey(player.getUniqueID())){
            profile = activeProfiles.get(player.getUniqueID());
            profile.player = player;
        } else {
            NBTTagCompound compound = load(player);
            profile = new Profile(player, compound);
            // If no slots exist, create default slot 0.
            if(profile.slots.isEmpty()){
                Slot defaultSlot = new Slot(0, "Default Slot");
                defaultSlot.setCompound(new NBTTagCompound());
                profile.slots.put(0, defaultSlot);
                profile.currentID = 0;
                saveSlotData(player);
                save(player, profile);
            }
            activeProfiles.put(player.getUniqueID(), profile);
        }
    }

    public static synchronized NBTTagCompound load(EntityPlayer player) {
        File saveDir = getProfileDir();
        String filename = player.getUniqueID().toString() + ".dat";
        try {
            File file = new File(saveDir, filename);
            if(file.exists()){
                return NBTJsonUtil.loadNBTData(file);
            }
        } catch (Exception e) {
            LogWriter.error("Error loading profile file: " + filename, e);
        }
        return new NBTTagCompound();
    }

    public static synchronized NBTTagCompound load(UUID uuid) {
        File saveDir = getProfileDir();
        String filename = uuid.toString() + ".dat";
        try {
            File file = new File(saveDir, filename);
            if(file.exists()){
                return NBTJsonUtil.loadNBTData(file);
            }
        } catch (Exception e) {
            LogWriter.error("Error loading profile file: " + filename, e);
        }
        return new NBTTagCompound();
    }

    // Offline save (synchronous) when no EntityPlayer is available.
    public static synchronized void saveOffline(Profile profile, UUID uuid) {
        final NBTTagCompound compound = profile.writeToNBT();
        final String filename = uuid.toString() + ".dat";
        try {
            File saveDir = getProfileDir();
            File fileNew = new File(saveDir, filename + "_new");
            File fileOld = new File(saveDir, filename);
            CompressedStreamTools.writeCompressed(compound, new FileOutputStream(fileNew));
            if(fileOld.exists()){
                fileOld.delete();
            }
            fileNew.renameTo(fileOld);
            // Also backup if allowed.
            backupProfile(uuid, compound);
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    // Save for online players (asynchronous).
    public static synchronized void save(EntityPlayer player, Profile profile) {
        profile.locked = true;
        CustomNPCsThreader.customNPCThread.execute(() -> {
            final NBTTagCompound compound = profile.writeToNBT();
            final String filename = player.getUniqueID() + ".dat";
            try {
                File saveDir = getProfileDir();
                File fileNew = new File(saveDir, filename + "_new");
                File fileOld = new File(saveDir, filename);
                CompressedStreamTools.writeCompressed(compound, new FileOutputStream(fileNew));
                if(fileOld.exists()){
                    fileOld.delete();
                }
                fileNew.renameTo(fileOld);
                // Backup if enabled.
                if(ConfigMain.AllowProfileBackups) {
                    backupProfile(player.getUniqueID(), compound);
                }
            } catch (Exception e) {
                LogWriter.except(e);
            } finally {
                profile.locked = false;
            }
        });
    }

    // Backup the profile by saving a copy to profiles/backup/<uuid>/<date>.dat.
    private static void backupProfile(UUID uuid, NBTTagCompound compound) {
        try {
            File backupDir = new File(getBackupDir(), uuid.toString());
            if(!backupDir.exists()){
                backupDir.mkdirs();
            }
            String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupFile = new File(backupDir, dateStr + ".dat");
            CompressedStreamTools.writeCompressed(compound, new FileOutputStream(backupFile));
            // Now enforce backup amount limit.
            File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".dat"));
            if(backups != null && backups.length > ConfigMain.ProfileBackupAmount) {
                // Sort backups by last modified ascending (oldest first)
                Arrays.sort(backups, Comparator.comparingLong(File::lastModified));
                for(int i = 0; i < backups.length - ConfigMain.ProfileBackupAmount; i++){
                    backups[i].delete();
                }
            }
        } catch(Exception e) {
            LogWriter.except(e);
        }
    }

    // Rollback a player's profile from a backup file.
    // Returns true if successful.
    public static boolean rollbackProfile(String username, File backupFile) {
        UUID uuid = getUUIDFromUsername(username);
        if(uuid == null) return false;
        try (FileInputStream fis = new FileInputStream(backupFile)) {
            NBTTagCompound compound = CompressedStreamTools.readCompressed(fis);
            // Overwrite the main profile file.
            File saveDir = getProfileDir();
            File mainFile = new File(saveDir, uuid.toString() + ".dat");
            File fileNew = new File(saveDir, uuid.toString() + "_new");
            CompressedStreamTools.writeCompressed(compound, new FileOutputStream(fileNew));
            if(mainFile.exists()){
                mainFile.delete();
            }
            fileNew.renameTo(mainFile);

            // Update the in-memory profile.
            EntityPlayerMP player = getPlayer(username);
            Profile newProfile;
            if(player != null) {
                newProfile = new Profile(player, compound);
                activeProfiles.put(uuid, newProfile);
                // Load the rolled-back data into the player's IProfileData.
                loadSlotData(player);
            } else {
                newProfile = new Profile(null, compound);
                activeProfiles.put(uuid, newProfile);
            }
            return true;
        } catch(Exception e) {
            LogWriter.except(e);
            return false;
        }
    }

    // Logout removes an active profile.
    public static synchronized void logout(EntityPlayer player) {
        if(player != null) {
            activeProfiles.remove(player.getUniqueID());
        }
    }

    // ---------- Profile Retrieval Helpers ----------
    public static Profile getProfile(EntityPlayer player) {
        if(player == null) return null;
        if(!activeProfiles.containsKey(player.getUniqueID()))
            login(player);
        return activeProfiles.get(player.getUniqueID());
    }

    public static Profile getProfile(UUID uuid) {
        if(activeProfiles.containsKey(uuid))
            return activeProfiles.get(uuid);
        NBTTagCompound compound = load(uuid);
        return new Profile(null, compound);
    }

    public static Profile getProfile(String username) {
        EntityPlayer player = getPlayer(username);
        if(player != null) {
            return getProfile(player);
        } else {
            UUID uuid = getUUIDFromUsername(username);
            if(uuid == null) return null;
            return getProfile(uuid);
        }
    }

    // ---------- Mojang API UUID Lookup ----------
    public static UUID getUUIDFromUsername(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder temp = new StringBuilder();
            String line;
            while((line = in.readLine()) != null){
                temp.append(line);
            }
            in.close();
            con.disconnect();
            String response = temp.toString();
            if(response.contains("\"id\"")) {
                int idIndex = response.indexOf("\"id\"");
                int colonIndex = response.indexOf(":", idIndex);
                int quoteStart = response.indexOf("\"", colonIndex);
                int quoteEnd = response.indexOf("\"", quoteStart + 1);
                String idString = response.substring(quoteStart + 1, quoteEnd);
                idString = idString.replaceFirst(
                    "(\\p{XDigit}{8})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}{4})(\\p{XDigit}+)",
                    "$1-$2-$3-$4-$5"
                );
                return UUID.fromString(idString);
            }
        } catch (IOException e) {
            LogWriter.error("Error retrieving UUID for " + username, e);
        }
        return null;
    }

    // ---------- Internal Slot Operation Helpers ----------
    // Returns the next available temporary (negative) slot id (starting at -1)
    private static int getNextAvailableTempSlot(Profile profile) {
        int id = -1;
        while(profile.slots.containsKey(id)) {
            id--;
        }
        return id;
    }

    // Clone a slot within a Profile.
    // If temporary==true, destinationSlotId is computed automatically.
    private static ProfileOperation cloneSlotInternal(Profile profile, int sourceSlotId, int destinationSlotId, boolean temporary) {
        if(profile.locked) {
            LogWriter.error("Profile is locked; cannot clone slot.");
            return ProfileOperation.LOCKED;
        }
        // Run verification checks on all IProfileData in priority order.
        if(profile.player != null) {
            List<IProfileData> dataList = new ArrayList<>(profileTypes.values());
            dataList.sort(Comparator.comparingInt(IProfileData::getSwitchPriority));
            for(IProfileData pd : dataList) {
                if(!pd.verifySwitch(profile.player)) {
                    LogWriter.error("Verification check failed for profile type: " + pd.getTagName());
                    return ProfileOperation.VERIFICATION_FAILED;
                }
            }
        }
        if(!profile.slots.containsKey(sourceSlotId)) {
            LogWriter.error("Source slot " + sourceSlotId + " does not exist.");
            return ProfileOperation.ERROR;
        }
        if(destinationSlotId == profile.currentID) {
            LogWriter.error("Cannot clone to the current active slot (" + profile.currentID + ").");
            return ProfileOperation.ERROR;
        }
        if(temporary) {
            destinationSlotId = getNextAvailableTempSlot(profile);
        } else {
            if(destinationSlotId <= 0) {
                LogWriter.error("Invalid destination slot id for a non-temporary clone.");
                return ProfileOperation.ERROR;
            }
        }
        Slot sourceSlot = profile.slots.get(sourceSlotId);
        NBTTagCompound clonedData = (NBTTagCompound) sourceSlot.getCompound().copy();
        Slot clonedSlot = new Slot(destinationSlotId, "Cloned Slot " + destinationSlotId, clonedData, System.currentTimeMillis(), temporary);
        profile.slots.put(destinationSlotId, clonedSlot);
        return ProfileOperation.SUCCESS;
    }

    // Remove a slot from a Profile.
    private static ProfileOperation removeSlotInternal(Profile profile, int slotId) {
        if(profile.locked) {
            LogWriter.error("Profile is locked; cannot remove slot.");
            return ProfileOperation.LOCKED;
        }
        if(slotId == profile.currentID) {
            LogWriter.error("Cannot remove the currently active slot (" + profile.currentID + ").");
            return ProfileOperation.ERROR;
        }
        if(!profile.slots.containsKey(slotId)) {
            LogWriter.error("Slot " + slotId + " does not exist.");
            return ProfileOperation.ERROR;
        }
        profile.slots.remove(slotId);
        return ProfileOperation.SUCCESS;
    }

    // Change the active slot in a Profile.
    // If the new slot doesn't exist, it is created and NEW_SLOT_CREATED is returned.
    // Saves current slot data before switching.
    private static ProfileOperation changeSlotInternal(Profile profile, int newSlotId) {
        if(profile.locked) {
            LogWriter.error("Profile is locked; cannot change slot.");
            return ProfileOperation.LOCKED;
        }
        // Disallow switching to the same active slot.
        if(profile.currentID == newSlotId) {
            LogWriter.error("Slot " + newSlotId + " is already active.");
            return ProfileOperation.ALREADY_ACTIVE;
        }
        // Run verification checks from all IProfileData in order of priority.
        if(profile.player != null) {
            List<IProfileData> dataList = new ArrayList<>(profileTypes.values());
            dataList.sort(Comparator.comparingInt(IProfileData::getSwitchPriority));
            for(IProfileData pd : dataList) {
                if(!pd.verifySwitch(profile.player)) {
                    LogWriter.error("Verification check failed for profile type: " + pd.getTagName());
                    return ProfileOperation.VERIFICATION_FAILED;
                }
            }
        }
        if(profile.player != null) {
            saveSlotData(profile.player);
        }
        boolean createdNewSlot = false;
        // If the new slot doesn't exist, create it.
        if(!profile.slots.containsKey(newSlotId)) {
            Slot newSlot = new Slot(newSlotId, "Slot " + newSlotId);
            newSlot.setCompound(new NBTTagCompound());
            profile.slots.put(newSlotId, newSlot);
            createdNewSlot = true;
        }
        profile.currentID = newSlotId;
        if(profile.player != null) {
            loadSlotData(profile.player);
        }
        // Update PlayerData so that PlayerData.get(player).profileSlot matches Profile.currentID.
        if(profile.player != null) {
            PlayerData pdata = PlayerData.get(profile.player);
            pdata.profileSlot = newSlotId;
            pdata.save();
        }
        return createdNewSlot ? ProfileOperation.NEW_SLOT_CREATED : ProfileOperation.SUCCESS;
    }

    // ---------- Public Operations (Overloaded for EntityPlayer, UUID, or username) ----------
    // ----- Clone Slot -----
    public static ProfileOperation cloneSlot(EntityPlayer player, int sourceSlotId, int destinationSlotId, boolean temporary) {
        Profile profile = getProfile(player);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = cloneSlotInternal(profile, sourceSlotId, destinationSlotId, temporary);
        if(result == ProfileOperation.SUCCESS && player != null)
            save(player, profile);
        return result;
    }

    public static ProfileOperation cloneSlot(UUID uuid, int sourceSlotId, int destinationSlotId, boolean temporary) {
        Profile profile = getProfile(uuid);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = cloneSlotInternal(profile, sourceSlotId, destinationSlotId, temporary);
        if(result == ProfileOperation.SUCCESS) {
            if(profile.player != null)
                save(profile.player, profile);
            else
                saveOffline(profile, uuid);
        }
        return result;
    }

    public static ProfileOperation cloneSlot(String username, int sourceSlotId, int destinationSlotId, boolean temporary) {
        Profile profile = getProfile(username);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = cloneSlotInternal(profile, sourceSlotId, destinationSlotId, temporary);
        if(result == ProfileOperation.SUCCESS) {
            if(profile.player != null)
                save(profile.player, profile);
            else {
                UUID uuid = getUUIDFromUsername(username);
                if(uuid != null)
                    saveOffline(profile, uuid);
            }
        }
        return result;
    }

    // ----- Remove Slot -----
    public static ProfileOperation removeSlot(EntityPlayer player, int slotId) {
        Profile profile = getProfile(player);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = removeSlotInternal(profile, slotId);
        if(result == ProfileOperation.SUCCESS && player != null)
            save(player, profile);
        return result;
    }

    public static ProfileOperation removeSlot(UUID uuid, int slotId) {
        Profile profile = getProfile(uuid);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = removeSlotInternal(profile, slotId);
        if(result == ProfileOperation.SUCCESS) {
            if(profile.player != null)
                save(profile.player, profile);
            else
                saveOffline(profile, uuid);
        }
        return result;
    }

    public static ProfileOperation removeSlot(String username, int slotId) {
        Profile profile = getProfile(username);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = removeSlotInternal(profile, slotId);
        if(result == ProfileOperation.SUCCESS) {
            if(profile.player != null)
                save(profile.player, profile);
            else {
                UUID uuid = getUUIDFromUsername(username);
                if(uuid != null)
                    saveOffline(profile, uuid);
            }
        }
        return result;
    }

    // ----- Change Slot -----
    public static ProfileOperation changeSlot(EntityPlayer player, int newSlotId) {
        Profile profile = getProfile(player);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = changeSlotInternal(profile, newSlotId);
        if((result == ProfileOperation.SUCCESS || result == ProfileOperation.NEW_SLOT_CREATED) && player != null)
            save(player, profile);
        return result;
    }

    public static ProfileOperation changeSlot(UUID uuid, int newSlotId) {
        Profile profile = getProfile(uuid);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = changeSlotInternal(profile, newSlotId);
        if(result == ProfileOperation.SUCCESS || result == ProfileOperation.NEW_SLOT_CREATED) {
            if(profile.player != null)
                save(profile.player, profile);
            else
                saveOffline(profile, uuid);
        }
        return result;
    }

    public static ProfileOperation changeSlot(String username, int newSlotId) {
        Profile profile = getProfile(username);
        if(profile == null) return ProfileOperation.PLAYER_NOT_FOUND;
        ProfileOperation result = changeSlotInternal(profile, newSlotId);
        if(result == ProfileOperation.SUCCESS || result == ProfileOperation.NEW_SLOT_CREATED) {
            if(profile.player != null)
                save(profile.player, profile);
            else {
                UUID uuid = getUUIDFromUsername(username);
                if(uuid != null)
                    saveOffline(profile, uuid);
            }
        }
        return result;
    }

    // ---------- Saving current slot data (used before switching) ----------
    public static void saveSlotData(EntityPlayer player){
        if(player == null || !activeProfiles.containsKey(player.getUniqueID()))
            return;
        Profile profile = activeProfiles.get(player.getUniqueID());
        if(profile.locked) {
            LogWriter.error("Profile is locked; cannot save slot data.");
            return;
        }
        NBTTagCompound dataCompound;
        Slot slot;
        if(!profile.slots.isEmpty() && profile.slots.containsKey(profile.currentID)){
            slot = profile.slots.get(profile.currentID);
            dataCompound = (NBTTagCompound) slot.getCompound().copy();
        } else {
            slot = new Slot(profile.currentID, "Slot " + profile.currentID);
            dataCompound = new NBTTagCompound();
            slot.setCompound(dataCompound);
        }
        List<IProfileData> dataList = new ArrayList<>(profileTypes.values());
        dataList.sort(Comparator.comparingInt(IProfileData::getSwitchPriority));
        for(IProfileData profileData : dataList){
            NBTTagCompound cloned = (NBTTagCompound) profileData.getCurrentNBT(player).copy();
            dataCompound.setTag(profileData.getTagName(), cloned);
        }
        slot.setCompound(dataCompound);
    }

    public static void loadSlotData(EntityPlayer player) {
        if(player == null || !activeProfiles.containsKey(player.getUniqueID()))
            return;
        Profile profile = activeProfiles.get(player.getUniqueID());
        if(profile.slots.isEmpty() || !profile.slots.containsKey(profile.currentID))
            return;
        NBTTagCompound slotCompound = (NBTTagCompound) profile.slots.get(profile.currentID).getCompound().copy();
        List<IProfileData> dataList = new ArrayList<>(profileTypes.values());
        dataList.sort(Comparator.comparingInt(IProfileData::getSwitchPriority));
        for(IProfileData profileData : dataList){
            NBTTagCompound data;
            if(slotCompound.hasKey(profileData.getTagName()))
                data = slotCompound.getCompoundTag(profileData.getTagName());
            else
                data = new NBTTagCompound();
            profileData.setNBT(player, data);
        }
        for(IProfileData profileData : dataList){
            profileData.save(player);
        }
    }

    public static EntityPlayerMP getPlayer(String username){
        return MinecraftServer.getServer().getConfigurationManager().func_152612_a(username);
    }
}
