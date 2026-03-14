package noppes.npcs.controllers.data;

import noppes.npcs.api.INbt;
import noppes.npcs.api.INbtList;
import noppes.npcs.core.NBT;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class TagMap {
    public int cloneTab;
    public String cloneFolder;
    public HashMap<String, HashSet<UUID>> tagMap;

    public TagMap(int tab) {
        this.cloneTab = tab;
        this.cloneFolder = null;
        this.tagMap = new HashMap<String, HashSet<UUID>>();
    }

    public TagMap(String folder) {
        this.cloneTab = -1;
        this.cloneFolder = folder;
        this.tagMap = new HashMap<String, HashSet<UUID>>();
    }

    public void readNBT(INbt compound) {
        this.tagMap = new HashMap<String, HashSet<UUID>>();
        INbtList list = compound.getTagList("TagMap", 10);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                INbt nbttagcompound = list.getCompound(i);
                String cloneName = nbttagcompound.getString("Clone");

                HashSet<UUID> uuids = new HashSet<UUID>();
                INbtList nbtTagList = nbttagcompound.getTagList("TagUUIDs", 8);
                for (int j = 0; j < nbtTagList.size(); j++) {
                    String uuid = nbtTagList.getString(j);
                    if (!uuid.isEmpty()) {
                        uuids.add(UUID.fromString(uuid));
                    }
                }
                tagMap.put(cloneName, uuids);
            }
        }
    }

    public INbt writeNBT() {
        INbt nbt = NBT.compound();
        INbtList cloneList = NBT.list();
        for (String key : tagMap.keySet()) {
            HashSet<UUID> uuidSet = tagMap.get(key);
            if (uuidSet.size() > 0) {
                INbt cloneCompound = NBT.compound();
                cloneCompound.setString("Clone", key);
                INbtList nbtTagList = NBT.list();
                for (UUID uuid : uuidSet) {
                    nbtTagList.addString(uuid.toString());
                }
                cloneCompound.setTagList("TagUUIDs", nbtTagList);
                cloneList.addCompound(cloneCompound);
            }
        }
        nbt.setTagList("TagMap", cloneList);
        return nbt;
    }

    public int getCloneTab() {
        return this.cloneTab;
    }

    public boolean hasClone(String cloneName) {
        return tagMap.containsKey(cloneName);
    }

    public HashSet<UUID> getUUIDs(String cloneName) {
        if (hasClone(cloneName)) {
            return tagMap.get(cloneName);
        }
        return null;
    }

    public List<UUID> getUUIDsList(String cloneName) {
        HashSet<UUID> uuids = getUUIDs(cloneName);
        if (uuids != null) {
            List<UUID> tagUUIDS = new ArrayList<>(uuids);
            Collections.sort(tagUUIDS);
            return tagUUIDS;
        }

        return null;
    }

    public boolean removeClone(String cloneName) {
        return tagMap.remove(cloneName) != null;
    }

    public void putClone(String cloneName, HashSet<UUID> uuids) {
        tagMap.put(cloneName, uuids);
    }

    public boolean hasTag(String cloneName, UUID tag) {
        HashSet<UUID> uuids = getUUIDs(cloneName);
        if (uuids == null) {
            return false;
        }
        return uuids.contains(tag);
    }

    public HashSet<UUID> getAllUUIDs() {
        HashSet<UUID> uuids = new HashSet<UUID>();
        for (HashSet<UUID> uuids1 : tagMap.values()) {
            uuids.addAll(uuids1);
        }
        return uuids;
    }

}
