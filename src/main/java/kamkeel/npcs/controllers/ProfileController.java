package kamkeel.npcs.controllers;

import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.controllers.data.profile.CNPCData;
import kamkeel.npcs.controllers.data.profile.EnumProfileOperation;
import kamkeel.npcs.controllers.data.profile.IProfileData;
import kamkeel.npcs.controllers.data.profile.Profile;
import kamkeel.npcs.controllers.data.profile.ProfileInfoEntry;
import kamkeel.npcs.controllers.data.profile.ProfileOperation;
import kamkeel.npcs.controllers.data.profile.Slot;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.CustomNpcsPermissions;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.IPlayerData;
import noppes.npcs.api.handler.IProfileHandler;
import noppes.npcs.api.handler.data.IProfile;
import noppes.npcs.api.handler.data.ISlot;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.controllers.data.PlayerQuestData;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.util.CustomNPCsThreader;
import noppes.npcs.util.NBTJsonUtil;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static noppes.npcs.CustomNpcsPermissions.PROFILE_MAX;
import static noppes.npcs.CustomNpcsPermissions.PROFILE_REGION_BYPASS;
import static noppes.npcs.CustomNpcsPermissions.hasCustomPermission;

public class ProfileController implements IProfileHandler {

    // Message constants
    private static final String MSG_PLAYER_NOT_FOUND = "Player not found.";
    private static final String MSG_PROFILE_LOCKED_CLONE = "Profile is locked; cannot clone slot.";
    private static final String MSG_SOURCE_SLOT_NOT_EXIST = "Source slot does not exist.";
    private static final String MSG_CANNOT_CLONE_CURRENT = "Cannot clone to the current active slot.";
    private static final String MSG_INVALID_DEST_SLOT = "Invalid destination slot id.";
    private static final String MSG_CLONE_SUCCESS = "Slot cloned successfully.";
    private static final String MSG_PROFILE_LOCKED_REMOVE = "Profile is locked; cannot remove slot.";
    private static final String MSG_CANNOT_REMOVE_ACTIVE = "Cannot remove the currently active slot.";
    private static final String MSG_SLOT_NOT_EXIST = "Slot does not exist.";
    private static final String MSG_REMOVE_SUCCESS = "Slot removed successfully.";
    private static final String MSG_PROFILE_LOCKED_CREATE = "Profile is locked; cannot create slot.";
    private static final String MSG_MAX_SLOTS_REACHED = "Maximum allowed slots reached.";
    private static final String MSG_NEW_SLOT_CREATED = "New slot created successfully.";
    private static final String MSG_PROFILE_LOCKED_CHANGE = "Profile is locked; cannot change slot.";
    private static final String MSG_SLOT_ALREADY_ACTIVE = "Slot is already active.";
    private static final String MSG_REGION_NOT_ALLOWED = "Profile switching not allowed from your current location.";
    private static final String MSG_CHANGE_SUCCESS = "Slot changed successfully.";
    private static final String MSG_CANCELLED = "Operation cancelled.";

    public static Map<String, IProfileData> profileTypes = new HashMap<>();
    public static Map<UUID, Profile> activeProfiles = new HashMap<>();
    public static String profile_directory = "profiles";

    public static ProfileController Instance;

    public ProfileController() {
        Instance = this;
        profileTypes = new HashMap<>();
        activeProfiles = new HashMap<>();
    }

    public static boolean registerProfileType(IProfileData type) {
        if (profileTypes.containsKey(type.getTagName()))
            return false;
        profileTypes.put(type.getTagName(), type);
        return true;
    }

    public static File getProfileDir() {
        try {
            File file = new File(CustomNpcs.getWorldSaveDirectory(), profile_directory);
            if (!file.exists())
                file.mkdir();
            return file;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public static File getBackupDir() {
        File base = getProfileDir();
        File backup = new File(base, "backup");
        if (!backup.exists()) {
            backup.mkdir();
        }
        return backup;
    }

    public synchronized void login(EntityPlayer player) {
        if (!ConfigMain.ProfilesEnabled)
            return;

        if (player == null)
            return;
        Profile profile;
        if (activeProfiles.containsKey(player.getUniqueID())) {
            profile = activeProfiles.get(player.getUniqueID());
            profile.player = player;
        } else {
            NBTTagCompound compound = load(player);
            profile = new Profile(player, compound);
            if (profile.getSlots().isEmpty()) {
                Slot defaultSlot = new Slot(0, "Default Slot");
                defaultSlot.setLastLoaded(System.currentTimeMillis());
                profile.getSlots().put(0, defaultSlot);
                profile.currentSlotId = 0;
                saveSlotData(player);
            }
            if (!profile.getSlots().containsKey(profile.currentSlotId)) {
                profile.currentSlotId = 0;
            }
            profile.player = player;
            activeProfiles.put(player.getUniqueID(), profile);
            loadSlotData(player);
            verifySlotQuests(profile.player);
        }
    }

    public synchronized NBTTagCompound load(EntityPlayer player) {
        File saveDir = getProfileDir();
        String filename = player.getUniqueID().toString() + ".dat";
        try {
            File file = new File(saveDir, filename);
            if (file.exists()) {
                return NBTJsonUtil.loadNBTData(file);
            }
        } catch (Exception e) {
            LogWriter.error("Error loading profile file: " + filename, e);
        }
        return new NBTTagCompound();
    }

    public synchronized NBTTagCompound load(UUID uuid) {
        File saveDir = getProfileDir();
        String filename = uuid.toString() + ".dat";
        try {
            File file = new File(saveDir, filename);
            if (file.exists()) {
                return NBTJsonUtil.loadNBTData(file);
            }
        } catch (Exception e) {
            LogWriter.error("Error loading profile file: " + filename, e);
        }
        return new NBTTagCompound();
    }

    public synchronized void saveOffline(Profile profile, UUID uuid) {
        profile.setLocked(true);
        final NBTTagCompound compound = profile.writeToNBT();
        final String filename = uuid.toString() + ".dat";
        CustomNPCsThreader.customNPCThread.execute(() -> {
            try {
                File saveDir = getProfileDir();
                File fileNew = new File(saveDir, filename + "_new");
                File fileOld = new File(saveDir, filename);
                CompressedStreamTools.writeCompressed(compound, new FileOutputStream(fileNew));
                if (fileOld.exists()) {
                    fileOld.delete();
                }
                fileNew.renameTo(fileOld);
                backupProfile(uuid, compound);
            } catch (Exception e) {
                LogWriter.except(e);
            } finally {
                profile.setLocked(false);
            }
        });
    }

    public synchronized void save(EntityPlayer player, Profile profile) {
        profile.setLocked(true);
        final NBTTagCompound compound = profile.writeToNBT();
        final String filename = player.getUniqueID() + ".dat";
        CustomNPCsThreader.customNPCThread.execute(() -> {
            try {
                File saveDir = getProfileDir();
                File fileNew = new File(saveDir, filename + "_new");
                File fileOld = new File(saveDir, filename);
                CompressedStreamTools.writeCompressed(compound, new FileOutputStream(fileNew));
                if (fileOld.exists()) {
                    fileOld.delete();
                }
                fileNew.renameTo(fileOld);
                if (ConfigMain.AllowProfileBackups) {
                    backupProfile(player.getUniqueID(), compound);
                }
            } catch (Exception e) {
                LogWriter.except(e);
            } finally {
                profile.setLocked(false);
            }
        });
    }

    private void backupProfile(UUID uuid, NBTTagCompound compound) {
        try {
            File backupDir = new File(getBackupDir(), uuid.toString());
            if (!backupDir.exists()) {
                backupDir.mkdirs();
            }
            String dateStr = new SimpleDateFormat("yyyyMMdd_HHmmss").format(new Date());
            File backupFile = new File(backupDir, dateStr + ".dat");
            CompressedStreamTools.writeCompressed(compound, new FileOutputStream(backupFile));
            File[] backups = backupDir.listFiles((dir, name) -> name.endsWith(".dat"));
            if (backups != null && backups.length > ConfigMain.ProfileBackupAmount) {
                Arrays.sort(backups, Comparator.comparingLong(File::lastModified));
                for (int i = 0; i < backups.length - ConfigMain.ProfileBackupAmount; i++) {
                    backups[i].delete();
                }
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public boolean rollbackProfile(String username, File backupFile) {
        UUID uuid = getUUIDFromUsername(username);
        if (uuid == null)
            return false;
        try (FileInputStream fis = new FileInputStream(backupFile)) {
            NBTTagCompound compound = CompressedStreamTools.readCompressed(fis);
            File saveDir = getProfileDir();
            File mainFile = new File(saveDir, uuid.toString() + ".dat");
            File fileNew = new File(saveDir, uuid.toString() + "_new");
            CompressedStreamTools.writeCompressed(compound, new FileOutputStream(fileNew));
            if (mainFile.exists()) {
                mainFile.delete();
            }
            fileNew.renameTo(mainFile);
            EntityPlayer player = NoppesUtilServer.getPlayerByName(username);
            Profile newProfile;
            if (player != null) {
                newProfile = new Profile(player, compound);
                activeProfiles.put(uuid, newProfile);
                loadSlotData(player);
                return true;
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
        return false;
    }

    public synchronized void logout(EntityPlayer player) {
        if (player != null && activeProfiles.containsKey(player.getUniqueID())) {
            saveSlotData(player);
            Profile profile = activeProfiles.get(player.getUniqueID());
            save(player, profile);
        }
    }

    public Profile getProfile(EntityPlayer player) {
        if (player == null)
            return null;
        if (!activeProfiles.containsKey(player.getUniqueID()))
            login(player);
        return activeProfiles.get(player.getUniqueID());
    }

    public Profile getProfile(UUID uuid) {
        if (activeProfiles.containsKey(uuid))
            return activeProfiles.get(uuid);
        NBTTagCompound compound = load(uuid);
        return new Profile(null, compound);
    }

    public Profile getProfile(String username) {
        EntityPlayer player = NoppesUtilServer.getPlayerByName(username);
        if (player != null) {
            return getProfile(player);
        } else {
            UUID uuid = getUUIDFromUsername(username);
            if (uuid == null)
                return null;
            return getProfile(uuid);
        }
    }

    public UUID getUUIDFromUsername(String username) {
        try {
            URL url = new URL("https://api.mojang.com/users/profiles/minecraft/" + username);
            HttpURLConnection con = (HttpURLConnection) url.openConnection();
            con.setRequestMethod("GET");
            con.setConnectTimeout(5000);
            con.setReadTimeout(5000);
            BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
            StringBuilder temp = new StringBuilder();
            String line;
            while ((line = in.readLine()) != null) {
                temp.append(line);
            }
            in.close();
            con.disconnect();
            String response = temp.toString();
            if (response.contains("\"id\"")) {
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

    private int getNextAvailableTempSlot(Profile profile) {
        int id = -1;
        while (profile.getSlots().containsKey(id)) {
            id--;
        }
        return id;
    }

    private ProfileOperation cloneSlotInternal(Profile profile, int sourceSlotId, int destinationSlotId, boolean temporary) {
        if (profile.isLocked()) {
            return ProfileOperation.locked(MSG_PROFILE_LOCKED_CLONE);
        }
        if (profile.player != null) {
            List<IProfileData> dataList = new ArrayList<>(profileTypes.values());
            dataList.sort(Comparator.comparingInt(IProfileData::getSwitchPriority));
            for (IProfileData pd : dataList) {
                if (pd.verifySwitch(profile.player).getResult() != EnumProfileOperation.SUCCESS) {
                    return pd.verifySwitch(profile.player);
                }
            }
        }
        if (!profile.getSlots().containsKey(sourceSlotId)) {
            return ProfileOperation.error(MSG_SOURCE_SLOT_NOT_EXIST);
        }
        if (destinationSlotId == profile.getCurrentSlotId()) {
            return ProfileOperation.error(MSG_CANNOT_CLONE_CURRENT);
        }
        if (temporary) {
            destinationSlotId = getNextAvailableTempSlot(profile);
        } else {
            if (destinationSlotId <= 0) {
                return ProfileOperation.error(MSG_INVALID_DEST_SLOT);
            }
        }
        ISlot sourceSlot = profile.getSlots().get(sourceSlotId);
        Map<String, NBTTagCompound> newComponents = new HashMap<>();
        for (String key : sourceSlot.getComponents().keySet()) {
            newComponents.put(key, (NBTTagCompound) sourceSlot.getComponentData(key).copy());
        }
        Slot clonedSlot = new Slot(destinationSlotId, "Cloned Slot " + destinationSlotId, System.currentTimeMillis(), temporary, newComponents);
        profile.getSlots().put(destinationSlotId, clonedSlot);
        return ProfileOperation.success(MSG_CLONE_SUCCESS);
    }

    private ProfileOperation removeSlotInternal(Profile profile, int slotId) {
        if (profile.isLocked()) {
            return ProfileOperation.locked(MSG_PROFILE_LOCKED_REMOVE);
        }
        if (slotId == profile.getCurrentSlotId()) {
            return ProfileOperation.error(MSG_CANNOT_REMOVE_ACTIVE);
        }
        if (!profile.getSlots().containsKey(slotId)) {
            return ProfileOperation.error(MSG_SLOT_NOT_EXIST);
        }

        if (profile.player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(profile.player);
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(profile.player);
            if (EventHooks.onProfileRemove(handler, scriptPlayer, profile, slotId, false))
                return ProfileOperation.error(MSG_CANCELLED);
        }

        profile.getSlots().remove(slotId);

        if (profile.player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(profile.player);
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(profile.player);
            EventHooks.onProfileRemove(handler, scriptPlayer, profile, slotId, true);
        }

        return ProfileOperation.success(MSG_REMOVE_SUCCESS);
    }

    public ProfileOperation createSlotInternal(Profile profile) {
        if (profile.isLocked()) {
            return ProfileOperation.locked(MSG_PROFILE_LOCKED_CREATE);
        }
        int newSlotId = 0;
        while (profile.getSlots().containsKey(newSlotId)) {
            newSlotId++;
        }
        if (!allowSlotPermission(profile.player)) {
            return ProfileOperation.error(MSG_MAX_SLOTS_REACHED);
        }
        if (profile.player != null) {
            List<IProfileData> dataList = new ArrayList<>(profileTypes.values());
            dataList.sort(Comparator.comparingInt(IProfileData::getSwitchPriority));
            for (IProfileData pd : dataList) {
                if (pd.verifySwitch(profile.player).getResult() != EnumProfileOperation.SUCCESS) {
                    return pd.verifySwitch(profile.player);
                }
            }
        }

        if (profile.player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(profile.player);
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(profile.player);
            if (EventHooks.onProfileCreate(handler, scriptPlayer, profile, newSlotId, false))
                return ProfileOperation.error(MSG_CANCELLED);
        }

        Slot newSlot = new Slot(newSlotId, "Slot " + newSlotId);
        newSlot.setLastLoaded(System.currentTimeMillis());
        profile.getSlots().put(newSlotId, newSlot);
        if (profile.player != null) {
            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(profile.player);
            verifySlotQuests(profile.player);
            save(profile.player, profile);

            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(profile.player);
            EventHooks.onProfileCreate(handler, scriptPlayer, profile, newSlotId, true);
        }
        return ProfileOperation.success(MSG_NEW_SLOT_CREATED);
    }

    private ProfileOperation changeSlotInternal(Profile profile, int newSlotId) {
        if (profile.isLocked()) {
            return ProfileOperation.locked(MSG_PROFILE_LOCKED_CHANGE);
        }
        if (profile.getCurrentSlotId() == newSlotId) {
            return ProfileOperation.error(MSG_SLOT_ALREADY_ACTIVE);
        }
        if (!profile.getSlots().containsKey(newSlotId)) {
            return ProfileOperation.error(MSG_SLOT_NOT_EXIST);
        }
        if (profile.player != null) {
            if (ConfigMain.RegionProfileSwitching) {
                boolean allowed = hasCustomPermission(profile.player, PROFILE_REGION_BYPASS.name);
                if (!allowed) {
                    int playerDim = profile.player.dimension;
                    int playerX = (int) profile.player.posX;
                    int playerY = (int) profile.player.posY;
                    int playerZ = (int) profile.player.posZ;
                    for (List<Integer> region : ConfigMain.RestrictedProfileRegions) {
                        if (region.size() == 7) {
                            int dim = region.get(0);
                            int x1 = Math.min(region.get(1), region.get(4));
                            int y1 = Math.min(region.get(2), region.get(5));
                            int z1 = Math.min(region.get(3), region.get(6));
                            int x2 = Math.max(region.get(1), region.get(4));
                            int y2 = Math.max(region.get(2), region.get(5));
                            int z2 = Math.max(region.get(3), region.get(6));
                            if (playerDim == dim &&
                                playerX >= x1 && playerX <= x2 &&
                                playerY >= y1 && playerY <= y2 &&
                                playerZ >= z1 && playerZ <= z2) {
                                allowed = true;
                                break;
                            }
                        }
                    }
                }
                if (!allowed) {
                    return ProfileOperation.error(MSG_REGION_NOT_ALLOWED);
                }
            }

            List<IProfileData> dataList = new ArrayList<>(profileTypes.values());
            dataList.sort(Comparator.comparingInt(IProfileData::getSwitchPriority));
            for (IProfileData pd : dataList) {
                if (pd.verifySwitch(profile.player).getResult() != EnumProfileOperation.SUCCESS) {
                    return pd.verifySwitch(profile.player);
                }
            }

            int prevSlot = profile.getCurrentSlotId();

            PlayerDataScript handler = ScriptController.Instance.getPlayerScripts(profile.player);
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(profile.player);
            if (EventHooks.onProfileChange(handler, scriptPlayer, profile, newSlotId, prevSlot, false))
                return ProfileOperation.error(MSG_CANCELLED);


            saveSlotData(profile.player);
            profile.currentSlotId = newSlotId;
            loadSlotData(profile.player);

            EventHooks.onProfileChange(handler, scriptPlayer, profile, newSlotId, prevSlot, true);
        } else {
            return ProfileOperation.error(MSG_PLAYER_NOT_FOUND);
        }
        return ProfileOperation.success(MSG_CHANGE_SUCCESS);
    }

    public ProfileOperation cloneSlot(EntityPlayer player, int sourceSlotId, int destinationSlotId, boolean temporary) {
        Profile profile = getProfile(player);
        if (profile == null)
            return ProfileOperation.error(MSG_PLAYER_NOT_FOUND);
        ProfileOperation result = cloneSlotInternal(profile, sourceSlotId, destinationSlotId, temporary);
        if (result.getResult() == EnumProfileOperation.SUCCESS && player != null) {
            save(player, profile);
        }
        return result;
    }

    public ProfileOperation cloneSlot(UUID uuid, int sourceSlotId, int destinationSlotId, boolean temporary) {
        Profile profile = getProfile(uuid);
        if (profile == null)
            return ProfileOperation.error(MSG_PLAYER_NOT_FOUND);
        ProfileOperation result = cloneSlotInternal(profile, sourceSlotId, destinationSlotId, temporary);
        if (result.getResult() == EnumProfileOperation.SUCCESS) {
            if (profile.player != null)
                save(profile.player, profile);
            else
                saveOffline(profile, uuid);
        }
        return result;
    }

    public ProfileOperation cloneSlot(String username, int sourceSlotId, int destinationSlotId, boolean temporary) {
        Profile profile = getProfile(username);
        if (profile == null)
            return ProfileOperation.error(MSG_PLAYER_NOT_FOUND);
        ProfileOperation result = cloneSlotInternal(profile, sourceSlotId, destinationSlotId, temporary);
        if (result.getResult() == EnumProfileOperation.SUCCESS) {
            if (profile.player != null)
                save(profile.player, profile);
            else {
                UUID uuid = getUUIDFromUsername(username);
                if (uuid != null)
                    saveOffline(profile, uuid);
            }
        }
        return result;
    }

    public ProfileOperation removeSlot(EntityPlayer player, int slotId) {
        Profile profile = getProfile(player);
        if (profile == null)
            return ProfileOperation.error(MSG_PLAYER_NOT_FOUND);
        ProfileOperation result = removeSlotInternal(profile, slotId);
        if (result.getResult() == EnumProfileOperation.SUCCESS && player != null) {
            save(player, profile);
        }
        return result;
    }

    public ProfileOperation removeSlot(String username, int slotId) {
        Profile profile = getProfile(username);
        if (profile == null)
            return ProfileOperation.error(MSG_PLAYER_NOT_FOUND);
        ProfileOperation result = removeSlotInternal(profile, slotId);
        if (result.getResult() == EnumProfileOperation.SUCCESS) {
            if (profile.player != null)
                save(profile.player, profile);
            else {
                UUID uuid = getUUIDFromUsername(username);
                if (uuid != null)
                    saveOffline(profile, uuid);
            }
        }
        return result;
    }

    public ProfileOperation changeSlot(EntityPlayer player, int newSlotId) {
        Profile profile = getProfile(player);
        if (profile == null)
            return ProfileOperation.error(MSG_PLAYER_NOT_FOUND);
        ProfileOperation result = changeSlotInternal(profile, newSlotId);
        if (result.getResult() == EnumProfileOperation.SUCCESS && player != null)
            save(player, profile);
        return result;
    }

    public ProfileOperation changeSlot(String username, int newSlotId) {
        Profile profile = getProfile(username);
        if (profile == null)
            return ProfileOperation.error(MSG_PLAYER_NOT_FOUND);
        ProfileOperation result = changeSlotInternal(profile, newSlotId);
        if (result.getResult() == EnumProfileOperation.SUCCESS) {
            if (profile.player != null)
                save(profile.player, profile);
            else {
                UUID uuid = getUUIDFromUsername(username);
                if (uuid != null)
                    saveOffline(profile, uuid);
            }
        }
        return result;
    }

    public void saveSlotData(EntityPlayer player) {
        if (player == null || !activeProfiles.containsKey(player.getUniqueID()))
            return;
        Profile profile = activeProfiles.get(player.getUniqueID());
        if (profile.isLocked()) {
            return;
        }
        ISlot slot = profile.getSlots().get(profile.getCurrentSlotId());
        if (slot == null) {
            slot = new Slot(profile.getCurrentSlotId(), "Slot " + profile.getCurrentSlotId());
            profile.getSlots().put(profile.getCurrentSlotId(), slot);
        }
        for (IProfileData profileData : profileTypes.values()) {
            NBTTagCompound cloned = (NBTTagCompound) profileData.getCurrentNBT(player).copy();
            slot.setComponentData(profileData.getTagName(), cloned);
        }
        slot.setLastLoaded(System.currentTimeMillis());
    }

    public void loadSlotData(EntityPlayer player) {
        if (player == null || !activeProfiles.containsKey(player.getUniqueID()))
            return;
        Profile profile = activeProfiles.get(player.getUniqueID());
        ISlot slot = profile.getSlots().get(profile.getCurrentSlotId());
        if (slot == null)
            return;

        for (IProfileData profileData : profileTypes.values()) {
            NBTTagCompound data;
            if (slot.getComponents().containsKey(profileData.getTagName()))
                data = (NBTTagCompound) slot.getComponentData(profileData.getTagName()).copy();
            else
                data = new NBTTagCompound();
            profileData.setNBT(player, data);
        }
        for (IProfileData profileData : profileTypes.values()) {
            profileData.save(player);
        }

        PlayerData pdata = PlayerData.get(player);
        pdata.profileSlot = profile.getCurrentSlotId();
        pdata.save();

        if (ConfigMain.AttributesEnabled)
            AttributeController.getTracker(player).recalcAttributes(player);
    }

    public List<ProfileInfoEntry> getProfileInfo(EntityPlayer player, int slotId) {
        List<ProfileInfoEntry> infoList = new ArrayList<>();
        Profile profile = getProfile(player);
        if (profile == null)
            return infoList;

        List<IProfileData> dataList = new ArrayList<>(profileTypes.values());
        dataList.sort(Comparator.comparingInt(IProfileData::getSwitchPriority));
        if (slotId == profile.getCurrentSlotId()) {
            for (IProfileData pd : dataList) {
                NBTTagCompound currentNBT = pd.getCurrentNBT(player);
                List<ProfileInfoEntry> subInfo = pd.getInfo(player, currentNBT);
                infoList.addAll(subInfo);
            }
        } else {
            if (!profile.getSlots().containsKey(slotId))
                return infoList;
            ISlot slot = profile.getSlots().get(slotId);
            for (IProfileData pd : dataList) {
                if (slot.getComponents().containsKey(pd.getTagName())) {
                    NBTTagCompound sub = (NBTTagCompound) slot.getComponentData(pd.getTagName()).copy();
                    List<ProfileInfoEntry> subInfo = pd.getInfo(player, sub);
                    infoList.addAll(subInfo);
                }
            }
        }
        return infoList;
    }

    public boolean allowSlotPermission(EntityPlayer player) {
        Profile profile = getProfile(player);
        int currentSlots = profile.getSlots().size();
        if (CustomNpcsPermissions.hasCustomPermission(player, PROFILE_MAX.name)) {
            return true;
        }
        int highestAllowed = 0;
        for (int i = 1; i <= 50; i++) {
            String perm = "customnpcs.profile.max." + i;
            if (CustomNpcsPermissions.hasCustomPermission(player, perm)) {
                highestAllowed = i;
            }
        }
        if (highestAllowed == 0 || highestAllowed < ConfigMain.DefaultProfileSlots) {
            highestAllowed = ConfigMain.DefaultProfileSlots;
        }
        return currentSlots < highestAllowed;
    }

    @Override
    public IProfile getProfile(IPlayer player) {
        if (player == null || player.getMCEntity() == null)
            return null;
        return getProfile((EntityPlayer) player.getMCEntity());
    }

    @Override
    public boolean changeSlot(IPlayer player, int slotID) {
        if (player == null || player.getMCEntity() == null)
            return false;

        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        ProfileOperation profileOperation = changeSlot(entityPlayer, slotID);
        return profileOperation.getResult() == EnumProfileOperation.SUCCESS;
    }

    @Override
    public boolean hasSlot(IPlayer player, int slotID) {
        if (player == null || player.getMCEntity() == null)
            return false;

        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        Profile profile = getProfile(entityPlayer);
        if (profile == null)
            return false;
        return profile.getSlots().containsKey(slotID);
    }

    @Override
    public boolean removeSlot(IPlayer player, int slotID) {
        if (player == null || player.getMCEntity() == null)
            return false;

        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        ProfileOperation profileOperation = removeSlot(entityPlayer, slotID);
        return profileOperation.getResult() == EnumProfileOperation.SUCCESS;
    }

    @Override
    public IPlayerData getSlotPlayerData(IPlayer player, int slotID) {
        PlayerData playerData;
        if (player == null || player.getMCEntity() == null)
            return null;

        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        Profile profile = getProfile(entityPlayer);
        if (profile == null)
            return null;
        return getSlotPlayerData(entityPlayer, slotID);
    }

    @Override
    public void saveSlotData(IPlayer player) {
        if (player == null || player.getMCEntity() == null)
            return;

        EntityPlayer entityPlayer = (EntityPlayer) player.getMCEntity();
        saveSlotData(entityPlayer);
    }

    public IPlayerData getSlotPlayerData(EntityPlayer player, int slotID) {
        PlayerData playerData;
        Profile profile = getProfile(player);
        if (profile == null)
            return null;

        if (profile.currentSlotId == slotID) {
            playerData = PlayerData.get(player);
        } else {
            Slot slot = (Slot) profile.getSlots().get(slotID);
            playerData = getSlotPlayerData(player, slot);
        }
        return playerData;
    }

    public PlayerData getSlotPlayerData(EntityPlayer player, Slot slot) {
        if (slot == null)
            return null;

        PlayerData playerData = new PlayerData();
        playerData.player = player;

        NBTTagCompound compound = slot.getComponentData(new CNPCData().getTagName());
        if (compound == null)
            compound = new NBTTagCompound();
        else
            compound = (NBTTagCompound) compound.copy();
        playerData.setNBT(compound);
        return playerData;
    }

    public void verifySlotQuests(EntityPlayer player) {
        Profile profile = getProfile(player);
        if (profile == null) {
            return;
        }

        Map<Integer, Long> universalFinished = new HashMap<>();
        for (Integer questId : QuestController.Instance.sharedQuests.keySet()) {
            long maxTime = 0;
            for (ISlot slot : profile.getSlots().values()) {
                IPlayerData data = getSlotPlayerData(player, slot.getId());
                if (data == null) {
                    continue;
                }
                PlayerQuestData questData = (PlayerQuestData) data.getQuestData();
                Long t = questData.finishedQuests.get(questId);
                if (t != null && t > maxTime) {
                    maxTime = t;
                }
            }
            if (maxTime > 0) {
                universalFinished.put(questId, maxTime);
            }
        }

        // Push the universal shared completion times into every slot.
        for (ISlot slot : profile.getSlots().values()) {
            IPlayerData data = getSlotPlayerData(player, slot.getId());
            if (data != null) {
                PlayerData playerData = (PlayerData) data;
                PlayerQuestData questData = (PlayerQuestData) data.getQuestData();
                questData.finishedQuests.putAll(universalFinished);
                slot.setComponentData(new CNPCData().getTagName(), playerData.getNBT());
            }
        }
    }

    public void shareQuestCompletion(EntityPlayer player, int questId, long completeTime) {
        Profile profile = getProfile(player);
        if (profile == null) {
            return;
        }
        for (ISlot slot : profile.getSlots().values()) {
            IPlayerData playerData = getSlotPlayerData(player, slot.getId());
            if (playerData != null) {
                PlayerQuestData questData = (PlayerQuestData) playerData.getQuestData();
                Long existing = questData.finishedQuests.get(questId);
                if (existing == null || completeTime > existing) {
                    questData.finishedQuests.put(questId, completeTime);
                }
                playerData.save();
            }
        }
        save(player, profile);
    }
}
