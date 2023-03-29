package noppes.npcs.client.controllers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.ServerCloneController;
import noppes.npcs.controllers.ServerTagMapController;
import noppes.npcs.controllers.data.TagMap;

import java.io.File;
import java.util.HashSet;
import java.util.UUID;

public class ClientCloneController extends ServerCloneController{
	public static ClientCloneController Instance;

	@Override
	public File getDir(){
		File dir = new File(CustomNpcs.Dir,"clones");
		if(!dir.exists())
			dir.mkdir();
		return dir;
	}

	@Override
	public boolean addToTagMap(NBTTagCompound nbttagcompound, String name, int tab){
		HashSet<UUID> tagUUIDs = new HashSet<UUID>();
		if(nbttagcompound.hasKey("TagUUIDs")){
			NBTTagList nbtTagList = nbttagcompound.getTagList("TagUUIDs",8);
			for (int i = 0; i < nbtTagList.tagCount(); i++) {
				tagUUIDs.add(UUID.fromString(nbtTagList.getStringTagAt(i)));
			}
		}
		if(tagUUIDs.size() > 0){
			TagMap tagMap = ClientTagMapController.Instance.getTagMap(tab);
			tagMap.putClone(name, tagUUIDs);
			ClientTagMapController.Instance.saveTagMap(tagMap);
			return true;
		}
		return false;
	}

	@Override
	public boolean removeFromTagMap(String name, int tab){
		TagMap tagMap = ClientTagMapController.Instance.getTagMap(tab);
		if(tagMap.removeClone(name)){
			ClientTagMapController.Instance.saveTagMap(tagMap);
			return true;
		}
		return false;
	}

}
