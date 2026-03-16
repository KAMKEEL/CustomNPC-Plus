package noppes.npcs.controllers;


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
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.ICloneHandler;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.IPos;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.IWorld;
import noppes.npcs.controllers.data.CloneFolder;
import noppes.npcs.controllers.data.TagMap;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.util.NBTJsonUtil;

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
                Map<Integer, Map<String, INbt>> clones = loadOldClones(file);
                file.delete();
                file = new File(dir, "clonednpcs.dat_old");
                if (file.exists())
                    file.delete();

                for (int tab : clones.keySet()) {
                    Map<String, INbt> map = clones.get(tab);
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

    private Map<Integer, Map<String, INbt>> loadOldClones(File file) throws Exception {
        Map<Integer, Map<String, INbt>> clones = new HashMap<Integer, Map<String, INbt>>();
        INbt nbttagcompound1;
        try (FileInputStream fis = new FileInputStream(file)) {
            nbttagcompound1 = NBTIO.readCompressed(fis);
        }
        INbtList list = nbttagcompound1.getTagList("Data", 10);
        if (list == null) {
            return clones;
        }
        for (int i = 0; i < list.tagCount(); i++) {
            INbt compound = list.getCompoundTagAt(i);
            if (!compound.hasKey("ClonedTab")) {
                compound.setInteger("ClonedTab", 1);
            }

            Map<String, INbt> tab = clones.get(compound.getInteger("ClonedTab"));
            if (tab == null)
                clones.put(compound.getInteger("ClonedTab"), tab = new HashMap<String, INbt>());

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

    public INbt getCloneData(ICommandSender player, String name, int tab) {
        File file = new File(new File(getDir(), tab + ""), name + ".json");
        if (!file.exists()) {
            if (player != null)
                player.addChatMessage(new ITextComponent("Could not find clone file"));
            return null;
        }
        try {
            return NBTJsonUtil.LoadFile(file);
        } catch (Exception e) {
            LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
            if (player != null)
                player.addChatMessage(new ITextComponent(e.getMessage()));
        }
        return null;
    }

    public void saveClone(int tab, String name, INbt compound) {
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

    public String addClone(INbt INbt, String name, int tab) {
        cleanTags(INbt);
        saveClone(tab, name, INbt);
        return name;
    }

    public String addClone(INbt INbt, String name, int tab, INbt tempTags) {
        cleanTagList(INbt, tempTags);
        cleanTags(INbt);
        saveClone(tab, name, INbt);
        return name;
    }

    // ==================== Folder Registry ====================

    public void loadFolders() {
        folders.clear();
        File dir = getDir();
        File[] files = dir.listFiles();
        if (files != null) {
            List<File> folderDirs = new ArrayList<>();
            for (File f : files) {
                if (!f.isDirectory()) continue;
                String name = f.getName();
                if (name.startsWith("___")) continue;
                try {
                    Integer.parseInt(name);
                    continue;
                } catch (NumberFormatException ignored) {
                }
                folderDirs.add(f);
            }
            folderDirs.sort(Comparator.comparing(File::getName));
            for (File f : folderDirs) {
                CloneFolder folder = new CloneFolder(f.getName());
                folder.createdDate = f.lastModified();
                folders.put(f.getName(), folder);
            }
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
        return folder;
    }

    public boolean renameFolder(String oldName, String newName) {
        if (!folders.containsKey(oldName) || !CloneFolder.isValidName(newName) || folders.containsKey(newName)) {
            return false;
        }
        CloneFolder folder = folders.get(oldName);
        File oldDir = getFolderDir(oldName);
        File newDir = new File(getDir(), newName);
        if (!oldDir.renameTo(newDir)) {
            return false;
        }
        folders.remove(oldName);
        folder.name = newName;
        folders.put(newName, folder);
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
        return true;
    }

    // ==================== Folder-Based Clone Operations ====================

    public File getFolderDir(String folderName) {
        return new File(getDir(), folderName);
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

    public INbt getCloneData(ICommandSender player, String name, String folderName) {
        File file = new File(getFolderDir(folderName), name + ".json");
        if (!file.exists()) {
            if (player != null)
                player.addChatMessage(new ITextComponent("Could not find clone file"));
            return null;
        }
        try {
            return NBTJsonUtil.LoadFile(file);
        } catch (Exception e) {
            LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
            if (player != null)
                player.addChatMessage(new ITextComponent(e.getMessage()));
        }
        return null;
    }

    public void saveClone(String folderName, String name, INbt compound) {
        try {
            File dir = getFolderDir(folderName);
            if (!dir.exists()) dir.mkdir();
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

    public String addClone(INbt INbt, String name, String folderName) {
        cleanTags(INbt);
        saveClone(folderName, name, INbt);
        return name;
    }

    public String addClone(INbt INbt, String name, String folderName, INbt tempTags) {
        cleanTagList(INbt, tempTags);
        cleanTags(INbt);
        saveClone(folderName, name, INbt);
        return name;
    }

    // ==================== Move Operations ====================

    public boolean moveClone(String cloneName, int fromTab, String toFolder) {
        INbt data = getCloneData(null, cloneName, fromTab);
        if (data == null) return false;

        HashSet<UUID> tagUUIDs = getTagUUIDsFromTab(cloneName, fromTab);

        saveClone(toFolder, cloneName, data);
        setTagUUIDsForFolder(cloneName, toFolder, tagUUIDs);

        removeClone(cloneName, fromTab);
        return true;
    }

    public boolean moveClone(String cloneName, String fromFolder, int toTab) {
        INbt data = getCloneData(null, cloneName, fromFolder);
        if (data == null) return false;

        HashSet<UUID> tagUUIDs = getTagUUIDsFromFolder(cloneName, fromFolder);

        saveClone(toTab, cloneName, data);
        setTagUUIDsForTab(cloneName, toTab, tagUUIDs);

        removeClone(cloneName, fromFolder);
        return true;
    }

    public boolean moveClone(String cloneName, String fromFolder, String toFolder) {
        INbt data = getCloneData(null, cloneName, fromFolder);
        if (data == null) return false;

        HashSet<UUID> tagUUIDs = getTagUUIDsFromFolder(cloneName, fromFolder);

        saveClone(toFolder, cloneName, data);
        setTagUUIDsForFolder(cloneName, toFolder, tagUUIDs);

        removeClone(cloneName, fromFolder);
        return true;
    }

    public boolean moveClone(String cloneName, int fromTab, int toTab) {
        INbt data = getCloneData(null, cloneName, fromTab);
        if (data == null) return false;

        HashSet<UUID> tagUUIDs = getTagUUIDsFromTab(cloneName, fromTab);

        saveClone(toTab, cloneName, data);
        setTagUUIDsForTab(cloneName, toTab, tagUUIDs);

        removeClone(cloneName, fromTab);
        return true;
    }

    // ==================== Tag Utilities ====================

    public INbt cleanTagList(INbt INbt, INbt tempTags) {
        HashSet<UUID> tagUUIDs = new HashSet<UUID>();
        if (INbt.hasKey("TagUUIDs")) {
            INbtList INbtList = INbt.getTagList("TagUUIDs", 8);
            for (int i = 0; i < INbtList.tagCount(); i++) {
                tagUUIDs.add(UUID.fromString(INbtList.getStringTagAt(i)));
            }

            INbt.removeTag("TagUUIDs");
        }
        if (tempTags.hasKey("TempTagUUIDs")) {
            INbtList INbtList = tempTags.getTagList("TempTagUUIDs", 8);
            for (int i = 0; i < INbtList.tagCount(); i++) {
                tagUUIDs.add(UUID.fromString(INbtList.getStringTagAt(i)));
            }

            tempTags.removeTag("TempTagUUIDs");
        }

        if (tagUUIDs.size() > 0) {
            INbtList INbtList = new INbtList();
            for (UUID uuid : tagUUIDs) {
                INbtList.appendTag(new NBTTagString(uuid.toString()));
            }
            INbt.setTag("TagUUIDs", INbtList);
        }

        return INbt;
    }

    public boolean addToTagMap(INbt INbt, String name, int tab) {
        HashSet<UUID> tagUUIDs = new HashSet<UUID>();
        if (INbt.hasKey("TagUUIDs")) {
            INbtList INbtList = INbt.getTagList("TagUUIDs", 8);
            for (int i = 0; i < INbtList.tagCount(); i++) {
                tagUUIDs.add(UUID.fromString(INbtList.getStringTagAt(i)));
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

    public boolean addToTagMap(INbt INbt, String name, String folderName) {
        HashSet<UUID> tagUUIDs = new HashSet<>();
        if (INbt.hasKey("TagUUIDs")) {
            INbtList INbtList = INbt.getTagList("TagUUIDs", 8);
            for (int i = 0; i < INbtList.tagCount(); i++) {
                tagUUIDs.add(UUID.fromString(INbtList.getStringTagAt(i)));
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

    public void cleanTags(INbt INbt) {
        if (INbt.hasKey("ItemGiverId"))
            INbt.setInteger("ItemGiverId", 0);
        if (INbt.hasKey("TransporterId"))
            INbt.setInteger("TransporterId", -1);

        INbt.removeTag("StartPosNew");
        INbt.removeTag("StartPos");
        INbt.removeTag("MovingPathNew");
        INbt.removeTag("Pos");
        INbt.removeTag("Riding");

        if (!INbt.hasKey("ModRev"))
            INbt.setInteger("ModRev", 1);

        if (INbt.hasKey("TransformRole")) {
            INbt adv = INbt.getCompoundTag("TransformRole");
            adv.setInteger("TransporterId", -1);
            INbt.setTag("TransformRole", adv);
        }

        if (INbt.hasKey("TransformJob")) {
            INbt adv = INbt.getCompoundTag("TransformJob");
            adv.setInteger("ItemGiverId", 0);
            INbt.setTag("TransformJob", adv);
        }

        if (INbt.hasKey("TransformAI")) {
            INbt adv = INbt.getCompoundTag("TransformAI");
            adv.removeTag("StartPosNew");
            adv.removeTag("StartPos");
            adv.removeTag("MovingPathNew");
            INbt.setTag("TransformAI", adv);
        }
    }

    // ==================== API Methods (ICloneHandler) ====================

    public IEntity spawn(double x, double y, double z, int tab, String name, IWorld IWorld, boolean ignoreProtection) {
        INbt compound = this.getCloneData((ICommandSender) null, name, tab);
        if (compound == null) {
            throw new CustomNPCsException("Unknown clone tab:" + tab + " name:" + name, new Object[0]);
        } else {
            IEntity IEntity;
            if (!ignoreProtection) {
                IEntity = NoppesUtilServer.spawnCloneWithProtection(compound, (int) x, (int) y, (int) z, IWorld.getMCWorld());
            } else {
                IEntity = NoppesUtilServer.spawnClone(compound, (int) x, (int) y, (int) z, IWorld.getMCWorld());
            }
            return IEntity == null ? null : NpcAPI.Instance().getIEntity(IEntity);
        }
    }

    public IEntity spawn(IPos pos, int tab, String name, IWorld IWorld, boolean ignoreProtection) {
        return this.spawn(pos.getX(), pos.getY(), pos.getZ(), tab, name, IWorld, ignoreProtection);
    }

    public IEntity spawn(double x, double y, double z, int tab, String name, IWorld IWorld) {
        return spawn(x, y, z, tab, name, IWorld, true);
    }

    public IEntity spawn(IPos pos, int tab, String name, IWorld IWorld) {
        return this.spawn(pos.getX(), pos.getY(), pos.getZ(), tab, name, IWorld);
    }

    public IEntity[] getTab(int tab, IWorld IWorld) {
        File dir = new File(getDir(), tab + "");
        if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null) {
            return new IEntity[]{};
        }

        ArrayList<IEntity> arrayList = new ArrayList<>();

        try {
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    INbt compound = NBTJsonUtil.LoadFile(file);
                    cleanTags(compound);
                    IEntity IEntity = EntityList.createEntityFromNBT(compound, IWorld.getMCWorld());
                    arrayList.add(IEntity == null ? null : NpcAPI.Instance().getIEntity(IEntity));
                }
            }
        } catch (Exception ignored) {
        }

        return arrayList.toArray(new IEntity[]{});
    }

    public IEntity get(int tab, String name, IWorld IWorld) {
        INbt compound = this.getCloneData((ICommandSender) null, name, tab);
        if (compound == null) {
            throw new CustomNPCsException("Unknown clone tab:" + tab + " name:" + name, new Object[0]);
        } else {
            cleanTags(compound);
            IEntity IEntity = EntityList.createEntityFromNBT(compound, IWorld.getMCWorld());
            return IEntity == null ? null : NpcAPI.Instance().getIEntity(IEntity);
        }
    }

    public boolean has(int tab, String name) {
        INbt compound = this.getCloneData((ICommandSender) null, name, tab);
        return compound != null;
    }

    public void set(int tab, String name, IEntity IEntity) {
        INbt compound = new INbt();
        if (!IEntity.getMCEntity().writeMountToNBT(compound))
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

    public IEntity spawn(double x, double y, double z, String folderName, String name, IWorld IWorld, boolean ignoreProtection) {
        INbt compound = getCloneData(null, name, folderName);
        if (compound == null) {
            throw new CustomNPCsException("Unknown clone folder:" + folderName + " name:" + name, new Object[0]);
        }
        IEntity IEntity;
        if (!ignoreProtection) {
            IEntity = NoppesUtilServer.spawnCloneWithProtection(compound, (int) x, (int) y, (int) z, IWorld.getMCWorld());
        } else {
            IEntity = NoppesUtilServer.spawnClone(compound, (int) x, (int) y, (int) z, IWorld.getMCWorld());
        }
        return IEntity == null ? null : NpcAPI.Instance().getIEntity(IEntity);
    }

    public IEntity spawn(IPos pos, String folderName, String name, IWorld IWorld, boolean ignoreProtection) {
        return this.spawn(pos.getX(), pos.getY(), pos.getZ(), folderName, name, IWorld, ignoreProtection);
    }

    public IEntity spawn(double x, double y, double z, String folderName, String name, IWorld IWorld) {
        return spawn(x, y, z, folderName, name, IWorld, true);
    }

    public IEntity spawn(IPos pos, String folderName, String name, IWorld IWorld) {
        return this.spawn(pos.getX(), pos.getY(), pos.getZ(), folderName, name, IWorld);
    }

    public IEntity[] getFolder(String folderName, IWorld IWorld) {
        File dir = getFolderDir(folderName);
        if (!dir.exists() || !dir.isDirectory() || dir.listFiles() == null) {
            return new IEntity[]{};
        }

        ArrayList<IEntity> arrayList = new ArrayList<>();
        try {
            for (File file : dir.listFiles()) {
                if (file.getName().endsWith(".json")) {
                    INbt compound = NBTJsonUtil.LoadFile(file);
                    cleanTags(compound);
                    IEntity IEntity = EntityList.createEntityFromNBT(compound, IWorld.getMCWorld());
                    arrayList.add(IEntity == null ? null : NpcAPI.Instance().getIEntity(IEntity));
                }
            }
        } catch (Exception ignored) {
        }
        return arrayList.toArray(new IEntity[]{});
    }

    public IEntity get(String folderName, String name, IWorld IWorld) {
        INbt compound = getCloneData(null, name, folderName);
        if (compound == null) {
            throw new CustomNPCsException("Unknown clone folder:" + folderName + " name:" + name, new Object[0]);
        }
        cleanTags(compound);
        IEntity IEntity = EntityList.createEntityFromNBT(compound, IWorld.getMCWorld());
        return IEntity == null ? null : NpcAPI.Instance().getIEntity(IEntity);
    }

    public boolean has(String folderName, String name) {
        INbt compound = getCloneData(null, name, folderName);
        return compound != null;
    }

    public void set(String folderName, String name, IEntity IEntity) {
        INbt compound = new INbt();
        if (!IEntity.getMCEntity().writeMountToNBT(compound))
            throw new CustomNPCsException("Cannot save dead entities", new Object[0]);

        this.cleanTags(compound);
        this.saveClone(folderName, name, compound);
    }

    public void remove(String folderName, String name) {
        this.removeClone(name, folderName);
    }
}
