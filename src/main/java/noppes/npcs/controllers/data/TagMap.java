package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

public class TagMap {
    public int cloneTab;
    public HashMap<String, HashSet<UUID>> tagMap;

    public TagMap(int tab) {
        this.cloneTab = tab;
        this.tagMap = new HashMap<String, HashSet<UUID>>();
    }

    public void readNBT(NBTTagCompound compound) {
        this.tagMap = new HashMap<String, HashSet<UUID>>();
        NBTTagList list = compound.getTagList("TagMap", 10);
        if (list != null) {
            for (int i = 0; i < list.tagCount(); i++) {
                NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
                String cloneName = nbttagcompound.getString("Clone");

                HashSet<UUID> uuids = new HashSet<UUID>();
                NBTTagList nbtTagList = nbttagcompound.getTagList("TagUUIDs", 8);
                for (int j = 0; j < nbtTagList.tagCount(); j++) {
                    String uuid = nbtTagList.getStringTagAt(j);
                    if (!uuid.isEmpty()) {
                        uuids.add(UUID.fromString(uuid));
                    }
                }
                tagMap.put(cloneName, uuids);
            }
        }
    }

    public NBTTagCompound writeNBT() {
        NBTTagCompound nbt = new NBTTagCompound();
        NBTTagList cloneList = new NBTTagList();
        for (String key : tagMap.keySet()) {
            HashSet<UUID> uuidSet = tagMap.get(key);
            if (uuidSet.size() > 0) {
                NBTTagCompound cloneCompound = new NBTTagCompound();
                cloneCompound.setString("Clone", key);
                NBTTagList nbtTagList = new NBTTagList();
                for (UUID uuid : uuidSet) {
                    nbtTagList.appendTag(new NBTTagString(uuid.toString()));
                }
                cloneCompound.setTag("TagUUIDs", nbtTagList);
                cloneList.appendTag(cloneCompound);
            }
        }
        nbt.setTag("TagMap", cloneList);
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
