package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.ILinkedItem;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.data.INpcScriptHandler;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.controllers.data.LinkedItemScript;
import noppes.npcs.util.NBTJsonUtil;

import java.io.*;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

public class LinkedItemController {
    private static LinkedItemController Instance;
    private int lastUsedID = 0;

    public HashMap<Integer, LinkedItem> linkedItems = new HashMap<>();
    public HashMap<Integer, LinkedItemScript> linkedItemsScripts = new HashMap<>();

    private HashMap<Integer, String> bootOrder;

    private LinkedItemController() {
    }

    public static LinkedItemController getInstance() {
        if (Instance == null) {
            Instance = new LinkedItemController();
        }
        return Instance;
    }

    public LinkedItem createItem(String name) {
        return new LinkedItem(name);
    }

    public IItemStack createItemStack(int id) {
        LinkedItem linkedItem = this.get(id);
        if (linkedItem != null) {
            return linkedItem.createStack();
        }
        return null;
    }

    public void add(LinkedItem linkedItem) {
        if (linkedItem != null) {
            String name = linkedItem.getName();
            if (name != null && !name.isEmpty()) {
                int linkedItemId = linkedItem.getId();
                int id = this.linkedItems.containsKey(linkedItemId) ? linkedItemId : this.getUnusedId();
                this.linkedItems.put(id, linkedItem);
                linkedItem.setId(id);
                this.addScript(id);
            }
        }
    }

    private void addScript(int id) {
        this.linkedItemsScripts.put(id, new LinkedItemScript());
    }

    public LinkedItem remove(int id) {
        this.removeScript(id);
        return this.linkedItems.remove(id);
    }

    private void removeScript(int id) {
        this.linkedItemsScripts.remove(id);
    }

    public LinkedItem get(int id) {
        return this.linkedItems.get(id);
    }

    public boolean contains(int id) {
        return this.linkedItems.containsKey(id);
    }

    public boolean contains(LinkedItem linkedItem) {
        return this.linkedItems.containsValue(linkedItem);
    }

    public INpcScriptHandler getScriptHandler(int id) {
        return this.linkedItemsScripts.get(id);
    }

    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////

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
        } else {
            for (File file : dir.listFiles()) {
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
                } catch (Exception e) {
                    LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
                }
            }
        }

        saveLinkedItemsMap();
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

        linkedItems.remove(linkedItem.getId());
        linkedItems.put(linkedItem.getId(), (LinkedItem) linkedItem);
        saveLinkedItemsMap();

        // Save Linked Item File
        File dir = this.getDir();
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

    private boolean hasOther(String name, int id) {
        for (LinkedItem linkedItem : linkedItems.values()) {
            if (linkedItem.getId() != id && linkedItem.getName().equalsIgnoreCase(name))
                return true;
        }
        return false;
    }

    public void delete(int id) {
        LinkedItem linkedItem = get(id);
        if (linkedItem != null) {
            LinkedItem foundItem = remove(id);
            if (foundItem != null && foundItem.name != null) {
                File dir = this.getDir();
                for (File file : dir.listFiles()) {
                    if (!file.isFile() || !file.getName().endsWith(".json"))
                        continue;
                    if (file.getName().equalsIgnoreCase(foundItem.name + ".json")) {
                        file.delete();
                        break;
                    }
                }
                saveLinkedItemsMap();
            }
        }
    }

    public void deleteLinkedItemFile(String prevName) {
        File dir = this.getDir();
        if (!dir.exists())
            dir.mkdirs();
        File file2 = new File(dir, prevName + ".json");
        if (file2.exists())
            file2.delete();
    }


    ////////////////////////////////////////////////////////
    ////////////////////////////////////////////////////////
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
    ////////////////////////////////////////////////////////
}
