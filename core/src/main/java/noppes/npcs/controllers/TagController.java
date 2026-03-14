package noppes.npcs.controllers;

import noppes.npcs.core.NBT;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.platform.PlatformServiceHolder;
import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;

import java.io.File;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.UUID;

public class TagController {
    public HashMap<Integer, Tag> tags;
    private static TagController instance;

    private int lastUsedID = 0;

    // TODO: mc1710 version implements ITagHandler and adds:
    // OLD: public List<ITag> list()
    // OLD: public ITag delete(int id)
    // OLD: public static void sendCategoryTagMap(EntityPlayerMP player, HashMap<String, HashSet<UUID>> itemTags) — uses GuiDataPacket

    public TagController() {
        instance = this;
        tags = new HashMap<Integer, Tag>();
        loadTags();
    }

    public static TagController getInstance() {
        return instance;
    }

    private void loadTags() {
        File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
        if (saveDir == null) {
            return;
        }
        try {
            File file = new File(saveDir, "tags.dat");
            if (file.exists()) {
                loadTagsFile(file);
            }
        } catch (Exception e) {
            try {
                File file = new File(saveDir, "tags.dat_old");
                if (file.exists()) {
                    loadTagsFile(file);
                }

            } catch (Exception ee) {
            }
        }
    }

    private void loadTagsFile(File file) throws Exception {
        // OLD: DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
        // OLD: loadTags(var1);
        // OLD: var1.close();
        INBTCompound nbttagcompound1 = PlatformServiceHolder.get().readCompressedNBT(file);
        loadTags(nbttagcompound1);
    }

    public void loadTags(INBTCompound nbttagcompound1) {
        HashMap<Integer, Tag> tags = new HashMap<Integer, Tag>();
        lastUsedID = nbttagcompound1.getInteger("lastID");
        INBTList list = nbttagcompound1.getList("NPCTags", 10);

        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                INBTCompound nbttagcompound = list.getCompound(i);
                Tag tag = new Tag();
                // OLD: tag.readNBT(new NBTWrapper(nbttagcompound));
                tag.readNBT(nbttagcompound);
                tags.put(tag.id, tag);
            }
        }
        this.tags = tags;
    }

    public INBTCompound getNBT() {
        INBTList list = NBT.list();
        for (int slot : tags.keySet()) {
            Tag tag = tags.get(slot);
            INBTCompound nbtfactions = NBT.compound();
            // OLD: tag.writeNBT(new NBTWrapper(nbtfactions));
            tag.writeNBT(nbtfactions);
            list.addCompound(nbtfactions);
        }
        INBTCompound nbttagcompound = NBT.compound();
        nbttagcompound.setInteger("lastID", lastUsedID);
        nbttagcompound.setList("NPCTags", list);
        return nbttagcompound;
    }

    public void saveTags() {
        try {
            File saveDir = PlatformServiceHolder.get().getWorldSaveDirectory();
            File file = new File(saveDir, "tags.dat_new");
            File file1 = new File(saveDir, "tags.dat_old");
            File file2 = new File(saveDir, "tags.dat");
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
            PlatformServiceHolder.get().logError("Error saving tag data", e);
        }
    }

    public Tag get(int tagSlot) {
        return tags.get(tagSlot);
    }

    public void saveTag(Tag tag) {

        if (tag.id < 0) {
            tag.id = getUnusedId();
            while (hasName(tag.name))
                tag.name += "_";
        } else {
            Tag existing = tags.get(tag.id);
            if (existing != null && !existing.name.equals(tag.name))
                while (hasName(tag.name))
                    tag.name += "_";
        }
        tags.remove(tag.id);
        tags.put(tag.id, tag);
        saveTags();
    }

    public Tag create(Tag tag) {
        this.saveTag(tag);
        return tag;
    }

    public Tag create(String name, int color) {
        Tag tag = new Tag();
        tag.name = name;
        tag.color = color;
        this.saveTag(tag);
        return tag;
    }

    public int getUnusedId() {
        if (lastUsedID == 0) {
            for (int catid : tags.keySet())
                if (catid > lastUsedID)
                    lastUsedID = catid;
        }
        lastUsedID++;
        return lastUsedID;
    }

    public void delete(int id) {
        if (id >= 0 && this.tags.size() > 1) {
            Tag tag = this.tags.remove(id);
            if (tag != null) {
                this.saveTags();
                tag.id = -1;
            }
        }
    }

    public boolean hasName(String newName) {
        if (newName.trim().isEmpty())
            return true;
        for (Tag tag : tags.values())
            if (tag.name.equals(newName))
                return true;
        return false;
    }

    public Tag getTagFromName(String tagname) {
        for (Map.Entry<Integer, Tag> entryTag : TagController.getInstance().tags.entrySet()) {
            if (entryTag.getValue().name.equalsIgnoreCase(tagname)) {
                return entryTag.getValue();
            }
        }
        return null;
    }

    public Tag getTagFromUUID(UUID uuid) {
        for (Map.Entry<Integer, Tag> entryTag : TagController.getInstance().tags.entrySet()) {
            if (entryTag.getValue().uuid.equals(uuid)) {
                return entryTag.getValue();
            }
        }
        return null;
    }

    public String[] getNames() {
        String[] names = new String[tags.size()];
        int i = 0;
        for (Tag tag : tags.values()) {
            names[i] = tag.name.toLowerCase();
            i++;
        }
        return names;
    }

    public HashSet<Tag> getAllTags() {
        return new HashSet<Tag>(this.tags.values());
    }

    /**
     * Write a set of tag UUIDs to an NBT compound under the given key.
     */
    public static void writeTagUUIDs(INBTCompound compound, String key, HashSet<UUID> tagUUIDs) {
        if (tagUUIDs != null && !tagUUIDs.isEmpty()) {
            INBTList list = NBT.list();
            for (UUID uuid : tagUUIDs) {
                list.addString(uuid.toString());
            }
            compound.setList(key, list);
        }
    }

    /**
     * Read a set of tag UUIDs from an NBT compound under the given key.
     */
    public static HashSet<UUID> readTagUUIDs(INBTCompound compound, String key) {
        HashSet<UUID> uuids = new HashSet<>();
        if (compound.hasKey(key)) {
            INBTList list = compound.getList(key, 8);
            for (int i = 0; i < list.size(); i++) {
                try {
                    uuids.add(UUID.fromString(list.getString(i)));
                } catch (IllegalArgumentException ignored) {}
            }
        }
        return uuids;
    }

    /**
     * Remove invalid tag UUIDs (those not in the tag registry).
     */
    public static void validateTagUUIDs(HashSet<UUID> tagUUIDs) {
        TagController tc = getInstance();
        if (tc == null) return;
        tagUUIDs.removeIf(uuid -> tc.getTagFromUUID(uuid) == null);
    }
}
