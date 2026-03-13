package noppes.npcs.controllers.data;

import noppes.npcs.platform.nbt.INBTCompound;
import noppes.npcs.platform.nbt.INBTList;
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

    public void readNBT(INBTCompound compound) {
        this.tagMap = new HashMap<String, HashSet<UUID>>();
        INBTList list = compound.getList("TagMap", 10);
        if (list != null) {
            for (int i = 0; i < list.size(); i++) {
                INBTCompound nbttagcompound = list.getCompound(i);
                String cloneName = nbttagcompound.getString("Clone");

                HashSet<UUID> uuids = new HashSet<UUID>();
                INBTList nbtTagList = nbttagcompound.getList("TagUUIDs", 8);
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

    public INBTCompound writeNBT() {
        INBTCompound nbt = NBT.compound();
        INBTList cloneList = NBT.list();
        for (String key : tagMap.keySet()) {
            HashSet<UUID> uuidSet = tagMap.get(key);
            if (uuidSet.size() > 0) {
                INBTCompound cloneCompound = NBT.compound();
                cloneCompound.setString("Clone", key);
                INBTList nbtTagList = NBT.list();
                for (UUID uuid : uuidSet) {
                    nbtTagList.addString(uuid.toString());
                }
                cloneCompound.setList("TagUUIDs", nbtTagList);
                cloneList.addCompound(cloneCompound);
            }
        }
        nbt.setList("TagMap", cloneList);
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
