package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.ILinkedItemHandler;
import noppes.npcs.api.handler.data.ILinkedItem;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.IScriptHandler;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.controllers.data.LinkedItemScript;
import noppes.npcs.util.NBTJsonUtil;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.zip.GZIPInputStream;

public class LinkedItemController implements ILinkedItemHandler {
    private static LinkedItemController Instance;
    private int lastUsedID = 0;

    public HashMap<Integer, LinkedItem> linkedItems = new HashMap<>();
    public HashMap<Integer, LinkedItemScript> linkedItemsScripts = new HashMap<>();

    private HashMap<Integer, String> bootOrder;
    public CategoryManager categoryManager = new CategoryManager();

    private LinkedItemController() {
    }

    public static LinkedItemController getInstance() {
        if (Instance == null) {
            Instance = new LinkedItemController();
        }
        return Instance;
    }

    @Override
    public ILinkedItem createItem(String name) {
        return new LinkedItem(name);
    }

    @Override
    public IItemStack createItemStack(int id) {
        LinkedItem linkedItem = (LinkedItem) this.get(id);
        if (linkedItem != null) {
            return linkedItem.createStack();
        }
        return null;
    }

    @Override
    public void add(ILinkedItem linkedItem) {
        if (linkedItem != null) {
            String name = linkedItem.getName();
            if (name != null && !name.isEmpty()) {
                int linkedItemId = linkedItem.getId();
                int id = this.linkedItems.containsKey(linkedItemId) ? linkedItemId : this.getUnusedId();
                this.linkedItems.put(id, (LinkedItem) linkedItem);
                linkedItem.setId(id);
                this.addScript(id);
            }
        }
    }

    private void addScript(int id) {
        this.linkedItemsScripts.put(id, new LinkedItemScript());
    }

    @Override
    public LinkedItem remove(int id) {
        this.removeScript(id);
        return this.linkedItems.remove(id);
    }

    private void removeScript(int id) {
        this.linkedItemsScripts.remove(id);
    }

    @Override
    public LinkedItem get(int id) {
        return (LinkedItem) this.linkedItems.get(id);
    }

    @Override
    public boolean contains(int id) {
        return this.linkedItems.containsKey(id);
    }

    @Override
    public boolean contains(ILinkedItem linkedItem) {
        return this.linkedItems.containsValue((LinkedItem) linkedItem);
    }

    public IScriptHandler getScriptHandler(int id) {
        return this.linkedItemsScripts.get(id);
    }

    /// /////////////////////////////////////////////////////
    /// /////////////////////////////////////////////////////

    public void load() {
        lastUsedID = 0;
        bootOrder = new HashMap<>();
        linkedItems = new HashMap<>();
        LogWriter.info("Loading linked items...");
        readLinkedMap();
        loadLinkedItems();
        LogWriter.info("Done loading linked items.");
    }

    private void loadLinkedItems() {
        linkedItems.clear();

        File dir = getDir();
        if (!dir.exists()) {
            dir.mkdir();
            return;
        }

        categoryManager.loadCategories(dir);

        // Load uncategorized items (root level .json files)
        loadItemsFromDir(dir, CategoryManager.UNCATEGORIZED_ID);

        // Load categorized items (subdirectories)
        for (Map.Entry<Integer, noppes.npcs.controllers.data.Category> entry : categoryManager.getCategories().entrySet()) {
            File catDir = categoryManager.getCategoryDir(entry.getKey());
            loadItemsFromDir(catDir, entry.getKey());
        }

        saveLinkedItemsMap();
    }

    private void loadItemsFromDir(File dir, int catId) {
        File[] files = dir.listFiles();
        if (files == null) return;
        for (File file : files) {
            if (!file.isFile() || !file.getName().endsWith(".json"))
                continue;
            try {
                LinkedItem linkedItem = new LinkedItem();
                linkedItem.readFromNBT(NBTJsonUtil.LoadFile(file));
                linkedItem.name = file.getName().substring(0, file.getName().length() - 5);

                if (linkedItem.id == -1) {
                    linkedItem.id = getUnusedId();
                }

                int originalID = linkedItem.id;
                int setID = linkedItem.id;
                while (bootOrder.containsKey(setID) || linkedItems.containsKey(setID)) {
                    if (bootOrder.containsKey(setID))
                        if (bootOrder.get(setID).equals(linkedItem.name))
                            break;

                    setID++;
                }

                linkedItem.id = setID;
                if (originalID != setID) {
                    LogWriter.info("Found Linked Item ID Mismatch: " + linkedItem.name + ", New ID: " + setID);
                    linkedItem.save();
                }

                linkedItems.put(linkedItem.id, linkedItem);
                categoryManager.registerItem(linkedItem.id, catId);
            } catch (Exception e) {
                LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
            }
        }
    }

    private File getDir() {
        return new File(CustomNpcs.getWorldSaveDirectory(), "linkeditems");
    }

    public int getUnusedId() {
        if (lastUsedID == 0) {
            for (int catid : linkedItems.keySet()) {
                if (catid > lastUsedID)
                    lastUsedID = catid;
            }

        }
        lastUsedID++;
        return lastUsedID;
    }

    public boolean hasName(String newName) {
        if (newName.trim().isEmpty())
            return true;
        for (LinkedItem linkedItem : linkedItems.values())
            if (linkedItem.name.equals(newName))
                return true;
        return false;
    }

    public ILinkedItem saveLinkedItem(ILinkedItem linkedItem) {
        if (linkedItem.getId() < 0) {
            linkedItem.setId(getUnusedId());
            while (hasName(linkedItem.getName()))
                linkedItem.setName(linkedItem.getName() + "_");
        }

        while (hasOther(linkedItem.getName(), linkedItem.getId()))
            linkedItem.setName(linkedItem.getName() + "_");

        TagController.validateTagUUIDs(((LinkedItem) linkedItem).tagUUIDs);
        linkedItems.remove(linkedItem.getId());
        linkedItems.put(linkedItem.getId(), (LinkedItem) linkedItem);
        saveLinkedItemsMap();

        // Save Linked Item File
        File dir = categoryManager.getItemDir(linkedItem.getId());
        if (!dir.exists())
            dir.mkdirs();

        File file = new File(dir, linkedItem.getName() + ".json_new");
        File file2 = new File(dir, linkedItem.getName() + ".json");

        try {
            NBTJsonUtil.SaveFile(file, ((LinkedItem) linkedItem).writeToNBT(true));
            if (file2.exists())
                file2.delete();
            file.renameTo(file2);
        } catch (Exception e) {
            LogWriter.except(e);
        }
        return linkedItems.get(linkedItem.getId());
    }

    public LinkedItem cloneLinkedItem(int originalId) {
        LinkedItem original = linkedItems.get(originalId);
        if (original == null) return null;

        int originalCatId = categoryManager.getItemCategory(originalId);

        NBTTagCompound nbt = original.writeToNBT(true);
        int newId = getUnusedId();
        nbt.setInteger("Id", newId);

        LinkedItem clone = new LinkedItem();
        clone.readFromNBT(nbt);

        String name = clone.getName();
        while (hasName(name)) name += "_";
        clone.name = name;

        if (originalCatId > CategoryManager.UNCATEGORIZED_ID) {
            categoryManager.registerItem(newId, originalCatId);
        }

        saveLinkedItem(clone);
        return clone;
    }

    private boolean hasOther(String name, int id) {
        for (LinkedItem linkedItem : linkedItems.values()) {
            if (linkedItem.getId() != id && linkedItem.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public void delete(int id) {
        LinkedItem linkedItem = (LinkedItem) get(id);
        if (linkedItem != null) {
            LinkedItem foundItem = remove(id);
            if (foundItem != null && foundItem.name != null) {
                File dir = categoryManager.getItemDir(id);
                File file = new File(dir, foundItem.name + ".json");
                if (file.exists()) {
                    file.delete();
                }
                categoryManager.removeItem(id);
                saveLinkedItemsMap();
            }
        }
    }

    public void deleteLinkedItemFile(String prevName) {
        categoryManager.deleteFile(prevName + ".json");
    }


    /// /////////////////////////////////////////////////////
    /// /////////////////////////////////////////////////////
    // LINKED ITEMS MAP
    public File getMapDir() {
        File dir = CustomNpcs.getWorldSaveDirectory();
        if (!dir.exists())
            dir.mkdir();
        return dir;
    }

    public void readLinkedMap() {
        bootOrder.clear();

        try {
            File file = new File(getMapDir(), "linkeditems.dat");
            if (file.exists()) {
                loadLinkedMap(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(getMapDir(), "linkeditems.dat_old");
                if (file.exists()) {
                    loadLinkedMap(file);
                }
            } catch (Exception ignored) {
            }
        }
    }

    public NBTTagCompound writeMapNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList linkedList = new NBTTagList();
        for (Integer key : linkedItems.keySet()) {
            LinkedItem linkedItem = linkedItems.get(key);
            if (!linkedItem.getName().isEmpty()) {
                NBTTagCompound linkedCompound = new NBTTagCompound();
                linkedCompound.setString("Name", linkedItem.getName());
                linkedCompound.setInteger("ID", key);

                linkedList.appendTag(linkedCompound);
            }
        }
        nbt.setTag("LinkedItems", linkedList);
        nbt.setInteger("lastID", lastUsedID);
        return nbt;
    }

    public void readMapNBT(NBTTagCompound compound) {
        lastUsedID = compound.getInteger("lastID");
        NBTTagList list = compound.getTagList("LinkedItems", 10);
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
                String linkedName = nbttagcompound.getString("Name");
                Integer key = nbttagcompound.getInteger("ID");
                bootOrder.put(key, linkedName);
            }
        }
    }

    private void loadLinkedMap(File file) throws IOException {
        DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
        readLinkedMap(var1);
        var1.close();
    }

    public void readLinkedMap(DataInputStream stream) throws IOException {
        NBTTagCompound nbtCompound = CompressedStreamTools.read(stream);
        this.readMapNBT(nbtCompound);
    }

    public void saveLinkedItemsMap() {
        try {
            File saveDir = getMapDir();
            File file = new File(saveDir, "linkeditems.dat_new");
            File file1 = new File(saveDir, "linkeditems.dat_old");
            File file2 = new File(saveDir, "linkeditems.dat");
            CompressedStreamTools.writeCompressed(this.writeMapNBT(), new FileOutputStream(file));
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
    }

    ////////////////////////////////////////////////////////
    // CATEGORY HELPERS
    ////////////////////////////////////////////////////////

    public Map<String, Integer> getCategoryScrollData() {
        return categoryManager.getCategoryScrollData();
    }

    public void moveItemToCategory(int itemId, int catId) {
        LinkedItem item = linkedItems.get(itemId);
        if (item == null) return;
        categoryManager.moveItem(itemId, item.name + ".json", catId);
        saveLinkedItemsMap();
    }

    public Map<String, Integer> getItemsByCategoryScrollData(int catId) {
        Map<String, Integer> map = new HashMap<>();
        List<Integer> itemIds = categoryManager.getItemsInCategory(catId, linkedItems.keySet());
        for (int itemId : itemIds) {
            LinkedItem item = linkedItems.get(itemId);
            if (item != null) {
                map.put(item.name, item.id);
            }
        }
        return map;
    }

    public HashMap<String, HashSet<UUID>> getItemTagMapForCategory(int catId) {
        HashMap<String, HashSet<UUID>> tagMap = new HashMap<>();
        List<Integer> itemIds = categoryManager.getItemsInCategory(catId, linkedItems.keySet());
        for (int itemId : itemIds) {
            LinkedItem item = linkedItems.get(itemId);
            if (item != null && !item.tagUUIDs.isEmpty()) {
                tagMap.put(item.name, item.tagUUIDs);
            }
        }
        return tagMap;
    }
}
