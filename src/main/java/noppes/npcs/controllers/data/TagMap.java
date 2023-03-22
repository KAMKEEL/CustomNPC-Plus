package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import java.util.*;

public class TagMap {
	public int cloneTab;
	public HashMap<String, Set<UUID>> tagMap;

	public TagMap(int tab){
		this.cloneTab = tab;
		this.tagMap = new HashMap<String, Set<UUID>>();
	}

	public void readNBT(NBTTagCompound compound){
		NBTTagList list = compound.getTagList(String.valueOf((cloneTab)), 10);
		if(list != null){
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
				String cloneName = nbttagcompound.getString("Clone");
				NBTTagList allUUIDs = compound.getTagList("UUIDs", 10);
				Set<UUID> uuids = new HashSet<UUID>();
				if(list != allUUIDs){
					for(int j = 0; j < allUUIDs.tagCount(); j++)
					{
						NBTTagCompound uuidCompound = list.getCompoundTagAt(j);
						String uuid = nbttagcompound.getString("UUID");
						if(!uuid.isEmpty()){
							uuids.add(UUID.fromString(uuid));
						}
					}
				}
				tagMap.put(cloneName, uuids);
			}
		}
  }
	public void writeNBT(NBTTagCompound compound){

	}

	public int getCloneTab() {
		return this.cloneTab;
	}

}
