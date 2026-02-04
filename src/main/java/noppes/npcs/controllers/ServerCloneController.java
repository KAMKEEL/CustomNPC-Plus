package noppes.npcs.controllers;

import net.minecraft.command.ICommandSender;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.util.ChatComponentText;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.IPos;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.controllers.data.CloneFolder;
import noppes.npcs.controllers.data.TagMap;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.util.NBTJsonUtil;

import java.io.File;
import java.io.FileInputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class ServerCloneController implements ICloneHandler {
    public static ServerCloneController Instance;

    protected Map<String, CloneFolder> folders = new LinkedHashMap<>();

    public ServerCloneController() {
        loadClones();
        loadFolders();
    }

    private void loadClones() {
        try {
            File dir = new File(getDir(), "..");
            File file = new File(dir, "clonednpcs.dat");
            if (file.exists()) {
                Map<Integer, Map<String, NBTTagCompound>> clones = loadOldClones(file);
                file.delete();
                file = new File(dir, "clonednpcs.dat_old");
                if (file.exists())
                    file.delete();

                for (int tab : clones.keySet()) {
                    Map<String, NBTTagCompound> map = clones.get(tab);
                    for (String name : map.keySet()) {
                        saveClone(tab, name, map.get(name));
                    }
                }
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public File getDir() {
        File dir = new File(CustomNpcs.getWorldSaveDirectory(), "clones");
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    private Map<Integer, Map<String, NBTTagCompound>> loadOldClones(File file) throws Exception {
        Map<Integer, Map<String, NBTTagCompound>> clones = new HashMap<Integer, Map<String, NBTTagCompound>>();
        NBTTagCompound nbttagcompound1;
        try (FileInputStream fis = new FileInputStream(file)) {
            nbttagcompound1 = CompressedStreamTools.readCompressed(fis);
        }
        NBTTagList list = nbttagcompound1.getTagList("Data", 10);
        if (list == null) {
            return clones;
        }
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            if (!compound.hasKey("ClonedTab")) {
                compound.setInteger("ClonedTab", 1);
            }

            Map<String, NBTTagCompound> tab = clones.get(compound.getInteger("ClonedTab"));
            if (tab == null)
                clones.put(compound.getInteger("ClonedTab"), tab = new HashMap<String, NBTTagCompound>());

            String name = compound.getString("ClonedName");
            int number = 1;
            while (tab.containsKey(name)) {
                number++;
                name = String.format("%s%s", compound.getString("ClonedName"), number);
            }
            compound.removeTag("ClonedName");
            compound.removeTag("ClonedTab");
            compound.removeTag("ClonedDate");
            cleanTags(compound);
            tab.put(name, compound);
        }
        return clones;
    }

    // ==================== Tab-Based Clone Operations ====================

    public NBTTagCompound getCloneData(ICommandSender player, String name, int tab) {
        File file = new File(new File(getDir(), tab + ""), name + ".json");
        if (!file.exists()) {
            if (player != null)
                player.addChatMessage(new ChatComponentText("Could not find clone file"));
            return null;
        }
        try {
            return NBTJsonUtil.LoadFile(file);
        } catch (Exception e) {
            LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
            if (player != null)
                player.addChatMessage(new ChatComponentText(e.getMessage()));
        }
        return null;
    }

    public void saveClone(int tab, String name, NBTTagCompound compound) {
        try {
            File dir = new File(getDir(), tab + "");
            if (!dir.exists())
                dir.mkdir();
            String filename = name + ".json";

            File file = new File(dir, filename + "_new");
            File file2 = new File(dir, filename);
            NBTJsonUtil.SaveFile(file, compound);
            addToTagMap(compound, name, tab);
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public List<String> getClones(int tab) {
        List<String> list = new ArrayList<String>();
        File dir = new File(getDir(), tab + "");
        if (!dir.exists() || !dir.isDirectory())
            return list;
        for (String file : dir.list()) {
            if (file.endsWith(".json"))
                list.add(file.substring(0, file.length() - 5));
        }
        return list;
    }

    public List<String> getClonesDate(int tab) {
        List<String> list = new ArrayList<String>();
        File dir = new File(getDir(), tab + "");
        if (!dir.exists() || !dir.isDirectory())
            return list;
        File[] files = dir.listFiles();
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });

        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".json"))
                list.add(fileName.substring(0, fileName.length() - 5));
        }
        return list;
    }

    public boolean removeClone(String name, int tab) {
        File file = new File(new File(getDir(), tab + ""), name + ".json");
        if (!file.exists())
            return false;
        file.delete();
        removeFromTagMap(name, tab);
        return true;
    }

    public String addClone(NBTTagCompound nbttagcompound, String name, int tab) {
        cleanTags(nbttagcompound);
        saveClone(tab, name, nbttagcompound);
        return name;
    }

    public String addClone(NBTTagCompound nbttagcompound, String name, int tab, NBTTagCompound tempTags) {
        cleanTagList(nbttagcompound, tempTags);
        cleanTags(nbttagcompound);
        saveClone(tab, name, nbttagcompound);
        return name;
    }

    // ==================== Folder Registry ====================

    public void loadFolders() {
        folders.clear();
        try {
            File file = new File(getDir(), "___folders.json");
            if (file.exists()) {
                NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
                NBTTagList list = compound.getTagList("Folders", 10);
                for (int i = 0; i < list.tagCount(); i++) {
                    CloneFolder folder = new CloneFolder();
                    folder.readNBT(list.getCompoundTagAt(i));
                    if (CloneFolder.isValidName(folder.name)) {
                        folders.put(folder.name, folder);
                    }
                }
            }
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public void saveFolders() {
        try {
            NBTTagCompound compound = new NBTTagCompound();
            NBTTagList list = new NBTTagList();
            for (CloneFolder folder : folders.values()) {
                list.appendTag(folder.writeNBT(new NBTTagCompound()));
            }
            compound.setTag("Folders", list);

            File file = new File(getDir(), "___folders.json_new");
            File file2 = new File(getDir(), "___folders.json");
            NBTJsonUtil.SaveFile(file, compound);
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public List<CloneFolder> getFolderList() {
        return new ArrayList<>(folders.values());
    }

    public List<String> getFolderNames() {
        return new ArrayList<>(folders.keySet());
    }

    public boolean hasFolder(String name) {
        return folders.containsKey(name);
    }

    // ==================== Folder CRUD ====================

    public CloneFolder createFolder(String name) {
        if (!CloneFolder.isValidName(name) || folders.containsKey(name)) {
            return null;
        }
        CloneFolder folder = new CloneFolder(name);
        File dir = getFolderDir(name);
        if (!dir.exists()) {
            dir.mkdir();
        }
        folders.put(name, folder);
        saveFolders();
        return folder;
    }

    public boolean renameFolder(String oldName, String newName) {
        if (!folders.containsKey(oldName) || !CloneFolder.isValidName(newName) || folders.containsKey(newName)) {
            return false;
        }
        CloneFolder folder = folders.get(oldName);
        File oldDir = getFolderDir(oldName);
        File newDir = new File(getDir(), "folder_" + newName);
        if (!oldDir.renameTo(newDir)) {
            return false;
        }
        folders.remove(oldName);
        folder.name = newName;
        folders.put(newName, folder);
        saveFolders();
        return true;
    }

    public boolean deleteFolder(String name) {
        if (!folders.containsKey(name)) {
            return false;
        }
        List<String> clones = getClones(name);
        if (!clones.isEmpty()) {
            return false;
        }
        File dir = getFolderDir(name);
        if (dir.exists()) {
            File tagMapFile = new File(dir, "___tagmap.dat");
            if (tagMapFile.exists()) tagMapFile.delete();
            File tagMapOld = new File(dir, "___tagmap.dat_old");
            if (tagMapOld.exists()) tagMapOld.delete();
            File tagMapNew = new File(dir, "___tagmap.dat_new");
            if (tagMapNew.exists()) tagMapNew.delete();
            dir.delete();
        }
        folders.remove(name);
        saveFolders();
        return true;
    }

    // ==================== Folder-Based Clone Operations ====================

    public File getFolderDir(String folderName) {
        File dir = new File(getDir(), "folder_" + folderName);
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    public List<String> getClones(String folderName) {
        List<String> list = new ArrayList<>();
        File dir = getFolderDir(folderName);
        if (!dir.exists() || !dir.isDirectory())
            return list;
        for (String file : dir.list()) {
            if (file.endsWith(".json"))
                list.add(file.substring(0, file.length() - 5));
        }
        return list;
    }

    public List<String> getClonesDate(String folderName) {
        List<String> list = new ArrayList<>();
        File dir = getFolderDir(folderName);
        if (!dir.exists() || !dir.isDirectory())
            return list;
        File[] files = dir.listFiles();
        if (files == null) return list;
        Arrays.sort(files, new Comparator<File>() {
            public int compare(File f1, File f2) {
                return Long.compare(f1.lastModified(), f2.lastModified());
            }
        });
        for (File file : files) {
            String fileName = file.getName();
            if (fileName.endsWith(".json"))
                list.add(fileName.substring(0, fileName.length() - 5));
        }
        return list;
    }

    public NBTTagCompound getCloneData(ICommandSender player, String name, String folderName) {
        File file = new File(getFolderDir(folderName), name + ".json");
        if (!file.exists()) {
            if (player != null)
                player.addChatMessage(new ChatComponentText("Could not find clone file"));
            return null;
        }
        try {
            return NBTJsonUtil.LoadFile(file);
        } catch (Exception e) {
            LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
            if (player != null)
                player.addChatMessage(new ChatComponentText(e.getMessage()));
        }
        return null;
    }

    public void saveClone(String folderName, String name, NBTTagCompound compound) {
        try {
            File dir = getFolderDir(folderName);
            String filename = name + ".json";
            File file = new File(dir, filename + "_new");
            File file2 = new File(dir, filename);
            NBTJsonUtil.SaveFile(file, compound);
            addToTagMap(compound, name, folderName);
            if (file2.exists()) {
                file2.delete();
            }
            file.renameTo(file2);
        } catch (Exception e) {
            LogWriter.except(e);
        }
    }

    public boolean removeClone(String name, String folderName) {
        File file = new File(getFolderDir(folderName), name + ".json");
        if (!file.exists())
            return false;
        file.delete();
        removeFromTagMap(name, folderName);
        return true;
    }

    public String addClone(NBTTagCompound nbttagcompound, String name, String folderName) {
        cleanTags(nbttagcompound);
        saveClone(folderName, name, nbttagcompound);
        return name;
    }

    public String addClone(NBTTagCompound nbttagcompound, String name, String folderName, NBTTagCompound tempTags) {
        cleanTagList(nbttagcompound, tempTags);
        cleanTags(nbttagcompound);
        saveClone(folderName, name, nbttagcompound);
        return name;
    }

    // ==================== Move Operations ====================

    public boolean moveClone(String cloneName, int fromTab, String toFolder) {
        NBTTagCompound data = getCloneData(null, cloneName, fromTab);
        if (data == null) return false;

        HashSet<UUID> tagUUIDs = getTagUUIDsFromTab(cloneName, fromTab);

        saveClone(toFolder, cloneName, data);
        setTagUUIDsForFolder(cloneName, toFolder, tagUUIDs);

        removeClone(cloneName, fromTab);
        return true;
    }

    public boolean moveClone(String cloneName, String fromFolder, int toTab) {
        NBTTagCompound data = getCloneData(null, cloneName, fromFolder);
        if (data == null) return false;

        HashSet<UUID> tagUUIDs = getTagUUIDsFromFolder(cloneName, fromFolder);

        saveClone(toTab, cloneName, data);
        setTagUUIDsForTab(cloneName, toTab, tagUUIDs);

        removeClone(cloneName, fromFolder);
        return true;
    }

    public boolean moveClone(String cloneName, String fromFolder, String toFolder) {
        NBTTagCompound data = getCloneData(null, cloneName, fromFolder);
        if (data == null) return false;

        HashSet<UUID> tagUUIDs = getTagUUIDsFromFolder(cloneName, fromFolder);

        saveClone(toFolder, cloneName, data);
        setTagUUIDsForFolder(cloneName, toFolder, tagUUIDs);

        removeClone(cloneName, fromFolder);
        return true;
    }

    public boolean moveClone(String cloneName, int fromTab, int toTab) {
        NBTTagCompound data = getCloneData(null, cloneName, fromTab);
        if (data == null) return false;

        HashSet<UUID> tagUUIDs = getTagUUIDsFromTab(cloneName, fromTab);

        saveClone(toTab, cloneName, data);
        setTagUUIDsForTab(cloneName, toTab, tagUUIDs);

        removeClone(cloneName, fromTab);
        return true;
    }

    // ==================== Tag Utilities ====================

    public NBTTagCompound cleanTagList(NBTTagCompound nbttagcompound, NBTTagCompound tempTags) {
        HashSet<UUID> tagUUIDs = new HashSet<UUID>();
        if (nbttagcompound.hasKey("TagUUIDs")) {
            NBTTagList nbtTagList = nbttagcompound.getTagList("TagUUIDs", 8);
            for (int i = 0; i < nbtTagList.tagCount(); i++) {
                tagUUIDs.add(UUID.fromString(nbtTagList.getStringTagAt(i)));
            }

            nbttagcompound.removeTag("TagUUIDs");
        }
        if (tempTags.hasKey("TempTagUUIDs")) {
            NBTTagList nbtTagList = tempTags.getTagList("TempTagUUIDs", 8);
            for (int i = 0; i < nbtTagList.tagCount(); i++) {
                tagUUIDs.add(UUID.fromString(nbtTagList.getStringTagAt(i)));
            }

            tempTags.removeTag("TempTagUUIDs");
        }

        if (tagUUIDs.size() > 0) {
            NBTTagList nbtTagList = new NBTTagList();
            for (UUID uuid : tagUUIDs) {
                nbtTagList.appendTag(new NBTTagString(uuid.toString()));
            }
            nbttagcompound.setTag("TagUUIDs", nbtTagList);
        }

        return nbttagcompound;
    }

    public boolean addToTagMap(NBTTagCompound nbttagcompound, String name, int tab) {
        HashSet<UUID> tagUUIDs = new HashSet<UUID>();
        if (nbttagcompound.hasKey("TagUUIDs")) {
            NBTTagList nbtTagList = nbttagcompound.getTagList("TagUUIDs", 8);
            for (int i = 0; i < nbtTagList.tagCount(); i++) {
                tagUUIDs.add(UUID.fromString(nbtTagList.getStringTagAt(i)));
            }
        }

        TagMap tagMap = ServerTagMapController.Instance.getTagMap(tab);
        if (!tagUUIDs.isEmpty()) {
            tagMap.putClone(name, tagUUIDs);
        } else {
            tagMap.removeClone(name);
        }
        ServerTagMapController.Instance.saveTagMap(tagMap);
        return true;
    }

    public boolean addToTagMap(NBTTagCompound nbttagcompound, String name, String folderName) {
        HashSet<UUID> tagUUIDs = new HashSet<>();
        if (nbttagcompound.hasKey("TagUUIDs")) {
            NBTTagList nbtTagList = nbttagcompound.getTagList("TagUUIDs", 8);
            for (int i = 0; i < nbtTagList.tagCount(); i++) {
                tagUUIDs.add(UUID.fromString(nbtTagList.getStringTagAt(i)));
            }
        }

        TagMap tagMap = ServerTagMapController.Instance.getTagMap(folderName);
        if (!tagUUIDs.isEmpty()) {
            tagMap.putClone(name, tagUUIDs);
        } else {
            tagMap.removeClone(name);
        }
        ServerTagMapController.Instance.saveTagMap(tagMap);
        return true;
    }

    public boolean removeFromTagMap(String name, int tab) {
        TagMap tagMap = ServerTagMapController.Instance.getTagMap(tab);
        if (tagMap.removeClone(name)) {
            ServerTagMapController.Instance.saveTagMap(tagMap);
            return true;
        }
        return false;
    }

    public boolean removeFromTagMap(String name, String folderName) {
        TagMap tagMap = ServerTagMapController.Instance.getTagMap(folderName);
        if (tagMap.removeClone(name)) {
            ServerTagMapController.Instance.saveTagMap(tagMap);
            return true;
        }
        return false;
    }

    protected HashSet<UUID> getTagUUIDsFromTab(String cloneName, int tab) {
        TagMap tagMap = ServerTagMapController.Instance.getTagMap(tab);
        HashSet<UUID> uuids = tagMap.getUUIDs(cloneName);
        return uuids != null ? new HashSet<>(uuids) : new HashSet<>();
    }

    protected HashSet<UUID> getTagUUIDsFromFolder(String cloneName, String folderName) {
        TagMap tagMap = ServerTagMapController.Instance.getTagMap(folderName);
        HashSet<UUID> uuids = tagMap.getUUIDs(cloneName);
        return uuids != null ? new HashSet<>(uuids) : new HashSet<>();
    }

    protected void setTagUUIDsForTab(String cloneName, int tab, HashSet<UUID> tagUUIDs) {
        if (tagUUIDs == null || tagUUIDs.isEmpty()) return;
        TagMap tagMap = ServerTagMapController.Instance.getTagMap(tab);
        tagMap.putClone(cloneName, tagUUIDs);
        ServerTagMapController.Instance.saveTagMap(tagMap);
    }

    protected void setTagUUIDsForFolder(String cloneName, String folderName, HashSet<UUID> tagUUIDs) {
        if (tagUUIDs == null || tagUUIDs.isEmpty()) return;
        TagMap tagMap = ServerTagMapController.Instance.getTagMap(folderName);
        tagMap.putClone(cloneName, tagUUIDs);
        ServerTagMapController.Instance.saveTagMap(tagMap);
    }

    public void cleanTags(NBTTagCompound nbttagcompound) {
        if (nbttagcompound.hasKey("ItemGiverId"))
            nbttagcompound.setInteger("ItemGiverId", 0);
        if (nbttagcompound.hasKey("TransporterId"))
            nbttagcompound.setInteger("TransporterId", -1);

        nbttagcompound.removeTag("StartPosNew");
        nbttagcompound.removeTag("StartPos");
        nbttagcompound.removeTag("MovingPathNew");
        nbttagcompound.removeTag("Pos");
        nbttagcompound.removeTag("Riding");

        if (!nbttagcompound.hasKey("ModRev"))
            nbttagcompound.setInteger("ModRev", 1);

        if (nbttagcompound.hasKey("TransformRole")) {
            NBTTagCompound adv = nbttagcompound.getCompoundTag("TransformRole");
            adv.setInteger("TransporterId", -1);
            nbttagcompound.setTag("TransformRole", adv);
        }

        if (nbttagcompound.hasKey("TransformJob")) {
            NBTTagCompound adv = nbttagcompound.getCompoundTag("TransformJob");
            adv.setInteger("ItemGiverId", 0);
            nbttagcompound.setTag("TransformJob", adv);
        }

        if (nbttagcompound.hasKey("TransformAI")) {
            NBTTagCompound adv = nbttagcompound.getCompoundTag("TransformAI");
            adv.removeTag("StartPosNew");
            adv.removeTag("StartPos");
            adv.removeTag("MovingPathNew");
            nbttagcompound.setTag("TransformAI", adv);
        }
    }

    // ==================== API Methods (ICloneHandler) ====================

    public IEntity spawn(double x, double y, double z, int tab, String name, IWorld world, boolean ignoreProtection) {
        NBTTagCompound compound = this.getCloneData((ICommandSender) null, name, tab);
        if (compound == null) {
            throw new CustomNPCsException("Unknown clone tab:" + tab + " name:" + name, new Object[0]);
        } else {
            Entity entity;
            if (!ignoreProtection) {
                entity = NoppesUtilServer.spawnCloneWithProtection(compound, (int) x, (int) y, (int) z, world.getMCWorld());
            } else {
                entity = NoppesUtilServer.spawnClone(compound, (int) x, (int) y, (int) z, world.getMCWorld());
            }
            return entity == null ? null : NpcAPI.Instance().getIEntity(entity);
        }
    }

    public IEntity spawn(IPos pos, int tab, String name, IWorld world, boolean ignoreProtection) {
        return this.spawn(pos.getX(), pos.getY(), pos.getZ(), tab, name, world, ignoreProtection);
    }

    public IEntity spawn(double x, double y, double z, int tab, String name, IWorld world) {
        return spawn(x, y, z, tab, name, world, true);
    }

    public IEntity spawn(IPos pos, int tab, String name, IWorld world) {
        return this.spawn(pos.getX(), pos.getY(), pos.getZ(), tab, name, world);
    }

    public IEntity[] getTab(int tab, IWorld world) {
        File dir = new File(getDir(), tab + "");
        if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null) {
            return new IEntity[]{};
        }

        ArrayList<IEntity> arrayList = new ArrayList<>();

        try {
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
                    cleanTags(compound);
                    Entity entity = EntityList.createEntityFromNBT(compound, world.getMCWorld());
                    arrayList.add(entity == null ? null : NpcAPI.Instance().getIEntity(entity));
                }
            }
        } catch (Exception ignored) {
        }

        return arrayList.toArray(new IEntity[]{});
    }

    public IEntity get(int tab, String name, IWorld world) {
        NBTTagCompound compound = this.getCloneData((ICommandSender) null, name, tab);
        if (compound == null) {
            throw new CustomNPCsException("Unknown clone tab:" + tab + " name:" + name, new Object[0]);
        } else {
            cleanTags(compound);
            Entity entity = EntityList.createEntityFromNBT(compound, world.getMCWorld());
            return entity == null ? null : NpcAPI.Instance().getIEntity(entity);
        }
    }

    public boolean has(int tab, String name) {
        NBTTagCompound compound = this.getCloneData((ICommandSender) null, name, tab);
        return compound != null;
    }

    public void set(int tab, String name, IEntity entity) {
        NBTTagCompound compound = new NBTTagCompound();
        if (!entity.getMCEntity().writeMountToNBT(compound))
            throw new CustomNPCsException("Cannot save dead entities", new Object[0]);

        this.cleanTags(compound);
        this.saveClone(tab, name, compound);
    }

    public void remove(int tab, String name) {
        this.removeClone(name, tab);
    }

    // --- Folder API Methods ---

    public String[] getFolders() {
        return getFolderNames().toArray(new String[0]);
    }

    public IEntity spawn(double x, double y, double z, String folderName, String name, IWorld world, boolean ignoreProtection) {
        NBTTagCompound compound = getCloneData(null, name, folderName);
        if (compound == null) {
            throw new CustomNPCsException("Unknown clone folder:" + folderName + " name:" + name, new Object[0]);
        }
        Entity entity;
        if (!ignoreProtection) {
            entity = NoppesUtilServer.spawnCloneWithProtection(compound, (int) x, (int) y, (int) z, world.getMCWorld());
        } else {
            entity = NoppesUtilServer.spawnClone(compound, (int) x, (int) y, (int) z, world.getMCWorld());
        }
        return entity == null ? null : NpcAPI.Instance().getIEntity(entity);
    }

    public IEntity spawn(IPos pos, String folderName, String name, IWorld world, boolean ignoreProtection) {
        return this.spawn(pos.getX(), pos.getY(), pos.getZ(), folderName, name, world, ignoreProtection);
    }

    public IEntity spawn(double x, double y, double z, String folderName, String name, IWorld world) {
        return spawn(x, y, z, folderName, name, world, true);
    }

    public IEntity spawn(IPos pos, String folderName, String name, IWorld world) {
        return this.spawn(pos.getX(), pos.getY(), pos.getZ(), folderName, name, world);
    }

    public IEntity[] getFolder(String folderName, IWorld world) {
        File dir = getFolderDir(folderName);
        if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null) {
            return new IEntity[]{};
        }

        ArrayList<IEntity> arrayList = new ArrayList<>();
        try {
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    NBTTagCompound compound = NBTJsonUtil.LoadFile(file);
                    cleanTags(compound);
                    Entity entity = EntityList.createEntityFromNBT(compound, world.getMCWorld());
                    arrayList.add(entity == null ? null : NpcAPI.Instance().getIEntity(entity));
                }
            }
        } catch (Exception ignored) {
        }
        return arrayList.toArray(new IEntity[]{});
    }

    public IEntity get(String folderName, String name, IWorld world) {
        NBTTagCompound compound = getCloneData(null, name, folderName);
        if (compound == null) {
            throw new CustomNPCsException("Unknown clone folder:" + folderName + " name:" + name, new Object[0]);
        }
        cleanTags(compound);
        Entity entity = EntityList.createEntityFromNBT(compound, world.getMCWorld());
        return entity == null ? null : NpcAPI.Instance().getIEntity(entity);
    }

    public boolean has(String folderName, String name) {
        NBTTagCompound compound = getCloneData(null, name, folderName);
        return compound != null;
    }

    public void set(String folderName, String name, IEntity entity) {
        NBTTagCompound compound = new NBTTagCompound();
        if (!entity.getMCEntity().writeMountToNBT(compound))
            throw new CustomNPCsException("Cannot save dead entities", new Object[0]);

        this.cleanTags(compound);
        this.saveClone(folderName, name, compound);
    }

    public void remove(String folderName, String name) {
        this.removeClone(name, folderName);
    }
}
