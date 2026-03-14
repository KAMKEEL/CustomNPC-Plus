package noppes.npcs.controllers;

import noppes.npcs.core.NBT;
import noppes.npcs.controllers.data.TransportCategory;
import noppes.npcs.controllers.data.TransportLocation;
import kamkeel.npcs.platform.PlatformServiceHolder;
import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;

import java.io.File;
import java.util.HashMap;

public class TransportController {
    private HashMap<Integer, TransportLocation> locations = new HashMap<Integer, TransportLocation>();
    public HashMap<Integer, TransportCategory> categories = new HashMap<Integer, TransportCategory>();

    private int lastUsedID = 0;

    // TODO: mc1710 version implements ITransportHandler and adds:
    // OLD: public TransportLocation saveLocation(int categoryId, NBTTagCompound compound, EntityNPCInterface npc)
    //   - This overload uses EntityNPCInterface, RoleTransporter, EnumRoleType
    // OLD: public ITransportCategory[] categories()
    // OLD: public void createCategory(String title)
    // OLD: public ITransportCategory getCategory(String title)
    // OLD: public void removeCategory(String title)

    private static TransportController instance;

    public TransportController() {
        instance = this;
        loadCategories();
        if (categories.isEmpty()) {
            TransportCategory cat = new TransportCategory();
            cat.id = 1;
            cat.title = "Default";
            categories.put(cat.id, cat);
        }
    }

    private void loadCategories() {
        File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
        if (saveDir == null)
            return;
        try {
            File file = new File(saveDir, "transport.dat");
            if (!file.exists()) {
                return;
            }
            loadCategories(file);
        } catch (Exception e) {
            try {
                File file = new File(saveDir, "transport.dat_old");
                if (!file.exists()) {
                    return;
                }
                loadCategories(file);

            } catch (Exception ee) {
            }
        }
    }

    public void loadCategories(File file) throws Exception {
        HashMap<Integer, TransportLocation> locations = new HashMap<Integer, TransportLocation>();
        HashMap<Integer, TransportCategory> categories = new HashMap<Integer, TransportCategory>();
        // OLD: NBTTagCompound nbttagcompound1;
        // OLD: try (FileInputStream fis = new FileInputStream(file)) {
        // OLD:     nbttagcompound1 = CompressedStreamTools.readCompressed(fis);
        // OLD: }
        INbt nbttagcompound1 = PlatformServiceHolder.get().readCompressedNBT(file);
        lastUsedID = nbttagcompound1.getInteger("lastID");
        INbtList list = nbttagcompound1.getTagList("NPCTransportCategories", 10);
        if (list == null) {
            return;
        }
        for (int i = 0; i < list.size(); i++) {
            TransportCategory category = new TransportCategory();
            INbt compound = list.getCompound(i);
            // OLD: category.readNBT(new NBTWrapper(compound));
            category.readNBT(compound);

            for (TransportLocation location : category.locations.values())
                locations.put(location.id, location);

            categories.put(category.id, category);
        }
        this.locations = locations;
        this.categories = categories;
    }

    public INbt getNBT() {
        INbtList list = NBT.list();
        for (TransportCategory category : categories.values()) {
            INbt compound = NBT.compound();
            category.writeNBT(compound);
            list.addCompound(compound);
        }
        INbt nbttagcompound = NBT.compound();
        nbttagcompound.setInteger("lastID", lastUsedID);
        nbttagcompound.setTagList("NPCTransportCategories", list);
        return nbttagcompound;
    }

    public void saveCategories() {
        try {
            File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
            File file = new File(saveDir, "transport.dat_new");
            File file1 = new File(saveDir, "transport.dat_old");
            File file2 = new File(saveDir, "transport.dat");
            // OLD: CompressedStreamTools.writeCompressed(getNBT(), new FileOutputStream(file));
            PlatformServiceHolder.get().writeCompressedNBT(getNBT(), file);
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
            // OLD: LogWriter.except(e);
            PlatformServiceHolder.get().logError("Error saving transport data", e);
        }
    }

    public TransportLocation getTransport(int transportId) {
        return locations.get(transportId);
    }

    public TransportLocation getTransport(String name) {
        for (TransportLocation loc : locations.values()) {
            if (loc.name.equals(name))
                return loc;
        }

        return null;
    }

    public int getUniqueIdLocation() {
        if (lastUsedID == 0) {
            for (int catid : locations.keySet())
                if (catid > lastUsedID)
                    lastUsedID = catid;
        }
        lastUsedID++;
        return lastUsedID;
    }

    private int getUniqueIdCategory() {
        int id = 0;
        for (int catid : categories.keySet())
            if (catid > id)
                id = catid;
        id++;
        return id;
    }

    public void setLocation(TransportLocation location) {
        if (locations.containsKey(location.id)) {
            for (TransportCategory cat : categories.values())
                cat.locations.remove(location.id);
        }
        locations.put(location.id, location);
        location.category.locations.put(location.id, location);
    }

    public TransportLocation removeLocation(int location) {
        TransportLocation loc = locations.get(location);
        if (loc == null)
            return null;
        loc.category.locations.remove(location);
        locations.remove(location);

        saveCategories();
        return loc;
    }

    private boolean containsCategoryName(String name) {
        name = name.toLowerCase();
        for (TransportCategory cat : categories.values()) {
            if (cat.title.toLowerCase().equals(name))
                return true;
        }
        return false;
    }

    public void saveCategory(String name, int id) {
        if (id < 0) {
            id = getUniqueIdCategory();
        }
        if (categories.containsKey(id)) {
            TransportCategory category = categories.get(id);
            if (!category.title.equals(name)) {
                while (containsCategoryName(name))
                    name += "_";
                categories.get(id).title = name;
            }
        } else {
            while (containsCategoryName(name))
                name += "_";
            TransportCategory category = new TransportCategory();
            category.id = id;
            category.title = name;
            categories.put(id, category);
        }
        saveCategories();
    }

    public void removeCategory(int id) {
        if (categories.size() == 1)
            return;
        TransportCategory cat = categories.get(id);
        if (cat == null)
            return;
        for (int i : cat.locations.keySet())
            locations.remove(i);
        categories.remove(id);

        saveCategories();
    }

    public boolean containsLocationName(String name) {
        name = name.toLowerCase();
        for (TransportLocation loc : locations.values()) {
            if (loc.name.toLowerCase().equals(name))
                return true;
        }
        return false;
    }

    public static TransportController getInstance() {
        return instance;
    }

    public TransportLocation saveLocation(int categoryId, TransportLocation location) {
        TransportCategory category = categories.get(categoryId);
        if (category == null)
            return null;
        location.category = category;
        if (location.id < 0 || !locations.get(location.id).name.equals(location.name)) {
            while (containsLocationName(location.name))
                location.name += "_";
        }
        if (location.id < 0)
            location.id = getUniqueIdLocation();

        category.locations.put(location.id, location);
        locations.put(location.id, location);
        saveCategories();

        return location;
    }
}
