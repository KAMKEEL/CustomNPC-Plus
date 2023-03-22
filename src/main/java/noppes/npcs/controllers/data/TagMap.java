package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

import java.util.*;

public class TagMap {
	public int cloneTab;
	public HashMap<String, HashSet<UUID>> tagMap;

	public TagMap(int tab){
		this.cloneTab = tab;
		this.tagMap = new HashMap<String, HashSet<UUID>>();
	}

	public void readNBT(NBTTagCompound compound){
		NBTTagList list = compound.getTagList("TagMap", 10);
		if(list != null){
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
				String cloneName = nbttagcompound.getString("Clone");
				NBTTagList allUUIDs = compound.getTagList("UUIDs", 10);
				HashSet<UUID> uuids = new HashSet<UUID>();
				if(allUUIDs != null){
					for(int j = 0; j < allUUIDs.tagCount(); j++)
					{
						NBTTagCompound uuidCompound = list.getCompoundTagAt(j);
						String uuid = uuidCompound.getString("UUID");
						if(!uuid.isEmpty()){
							uuids.add(UUID.fromString(uuid));
						}
					}
				}
				tagMap.put(cloneName, uuids);
			}
		}
	}

	public NBTTagCompound writeNBT(){
		NBTTagCompound nbt = new NBTTagCompound();

		NBTTagList cloneList = new NBTTagList();
		for(String key: tagMap.keySet()){
			HashSet<UUID> uuidSet = tagMap.get(key);
			if(uuidSet.size() > 0){
				NBTTagCompound cloneCompound = new NBTTagCompound();
				cloneCompound.setString("Clone", key);
				NBTTagList uuidList = new NBTTagList();
				for(UUID uuid : uuidSet){
					NBTTagCompound uuidCompound = new NBTTagCompound();
					uuidCompound.setString("UUID", uuid.toString());
					uuidList.appendTag(uuidCompound);
				}
				cloneCompound.setTag("UUIDs", uuidList);
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
		if(hasClone(cloneName)){
			return tagMap.get(cloneName);
		}
		return null;
	}

	public void removeClone(String cloneName) {
		tagMap.remove(cloneName);
	}

	public void putClone(String cloneName, HashSet<UUID> uuids) {
		tagMap.put(cloneName, uuids);
	}

	public boolean hasTag(String cloneName, UUID tag) {
		HashSet<UUID> uuids = getUUIDs(cloneName);
		if(uuids == null){
			return false;
		}
		return uuids.contains(tag);
	}

	public HashSet<UUID> getAllUUIDs() {
		HashSet<UUID> uuids = new HashSet<UUID>();
		for(HashSet<UUID> uuids1 : tagMap.values()){
			uuids.addAll(uuids1);
		}
		return uuids;
	}

}
