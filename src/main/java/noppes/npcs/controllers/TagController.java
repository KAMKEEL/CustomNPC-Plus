package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.ITagHandler;
import noppes.npcs.api.handler.data.ITag;
import noppes.npcs.controllers.data.Tag;
import noppes.npcs.controllers.data.TagMap;
import scala.Int;

import java.io.*;
import java.util.*;
import java.util.zip.GZIPInputStream;

public class TagController implements ITagHandler {
	public HashMap<Integer, Tag> tags;
	public HashMap<UUID, Integer> tagsUUID;
	private static TagController instance;

	private int lastUsedID = 0;

	public TagController(){
		instance = this;
		tags = new HashMap<Integer, Tag>();
		tagsUUID = new HashMap<UUID, Integer>();
		loadTags();
//		if(tags.isEmpty()){
//			// TO-DO
//			tags.put(0,new Faction(0,"Friendly", 0x00DD00, 2000));
//			tags.put(1,new Faction(1,"Neutral", 0xF2DD00, 1000));
//			tags.put(2,new Faction(2,"Aggressive", 0xDD0000, 0));
//		}
	}
	public static TagController getInstance(){
		return instance;
	}

	private void loadTags(){

		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if(saveDir == null){
			return;
		}
		try {
			File file = new File(saveDir, "tags.dat");
			if(file.exists()){
				loadTagsFile(file);
			}
		} catch (Exception e) {
			try {
				File file = new File(saveDir, "tags.dat_old");
				if(file.exists()){
					loadTagsFile(file);
				}

			} catch (Exception ee) {
			}
		}
	}

	private void loadTagsFile(File file) throws IOException{
		DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		loadTags(var1);
		var1.close();
	}

	public void loadTags(DataInputStream stream) throws IOException{
		HashMap<Integer,Tag> tags = new HashMap<Integer,Tag>();
		HashMap<UUID, Integer> tagUUIDs = new HashMap<UUID, Integer>();
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.read(stream);
		lastUsedID = nbttagcompound1.getInteger("lastID");
		NBTTagList list = nbttagcompound1.getTagList("NPCTags", 10);

		if(list != null){
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
				Tag tag = new Tag();
				tag.readNBT(nbttagcompound);
				tags.put(tag.id,tag);
				tagUUIDs.put(tag.uuid, tag.id);
			}
		}
		this.tags = tags;
		this.tagsUUID = tagUUIDs;
	}

	public NBTTagCompound getNBT(){
		NBTTagList list = new NBTTagList();
		for(int slot : tags.keySet()){
			Tag tag = tags.get(slot);
			NBTTagCompound nbtfactions = new NBTTagCompound();
			tag.writeNBT(nbtfactions);
			list.appendTag(nbtfactions);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setInteger("lastID", lastUsedID);
		nbttagcompound.setTag("NPCTags", list);
		return nbttagcompound;
	}

	public void saveTags(){
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "tags.dat_new");
			File file1 = new File(saveDir, "tags.dat_old");
			File file2 = new File(saveDir, "tags.dat");
			CompressedStreamTools.writeCompressed(getNBT(), new FileOutputStream(file));
			if(file1.exists())
			{
				file1.delete();
			}
			file2.renameTo(file1);
			if(file2.exists())
			{
				file2.delete();
			}
			file.renameTo(file2);
			if(file.exists())
			{
				file.delete();
			}
		} catch (Exception e) {
			LogWriter.except(e);
		}
	}

	public Tag get(int tagSlot) {
		return tags.get(tagSlot);
	}

	public Tag get(UUID uuid) {
		Integer id = tagsUUID.get(uuid);
		if(id != null){
			get(id);
		}
		return null;
	}

	public List<ITag> list() {
		return new ArrayList(this.tags.values());
	}

	public void saveTag(Tag tag) {

		if(tag.id < 0){
			tag.id = getUnusedId();
			while(hasName(tag.name))
				tag.name += "_";
		}
		else{
			Tag existing = tags.get(tag.id);
			if(existing != null && !existing.name.equals(tag.name))
				while(hasName(tag.name))
					tag.name += "_";
		}
		tags.remove(tag.id);
		tags.put(tag.id, tag);
		tagsUUID.put(tag.uuid, tag.id);
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

	public int getUnusedId(){
		if(lastUsedID == 0){
			for(int catid : tags.keySet())
				if(catid > lastUsedID)
					lastUsedID = catid;
		}
		lastUsedID++;
		return lastUsedID;
	}

	public ITag delete(int id) {
		if (id >= 0 && this.tags.size() > 1) {
			Tag tag = (Tag)this.tags.remove(id);
			saveTags();
			if (tag == null) {
				return null;
			} else {
				this.tagsUUID.remove(tag.uuid);
				this.saveTags();
				tag.id = -1;
				return tag;
			}
		} else {
			return null;
		}
	}

	public int getFirstTagId() {
		return tags.keySet().iterator().next();
	}

	public Tag getFirstTag() {
		return tags.values().iterator().next();
	}

	public boolean hasName(String newName) {
		if(newName.trim().isEmpty())
			return true;
		for(Tag tag : tags.values())
			if(tag.name.equals(newName))
				return true;
		return false;
	}

	public Tag getTagFromName(String tagname){
		for (Map.Entry<Integer,Tag> entryTag: TagController.getInstance().tags.entrySet()){
			if (entryTag.getValue().name.equalsIgnoreCase(tagname)){
				return entryTag.getValue();
			}
		}
		return null;
	}

	public String[] getNames() {
		String[] names = new String[tags.size()];
		int i = 0;
		for(Tag tag : tags.values()){
			names[i] = tag.name.toLowerCase();
			i++;
		}
		return names;
	}

	public Tag getTagFromUUID(UUID uuid){
		for (Map.Entry<Integer,Tag> entryTag: TagController.getInstance().tags.entrySet()){
			if (entryTag.getValue().uuid.equals(uuid)) {
				return entryTag.getValue();
			}
		}
		return null;
	}

	public HashSet<Tag> getValidTags(TagMap tagMap){
		HashSet<Tag> tags = new HashSet<Tag>();
		for(UUID tagUUID : tagMap.getAllUUIDs()){
			Tag foundTag = get(tagUUID);
			if(foundTag != null){
				tags.add(foundTag);
			}
		}
		return tags;
	}
}
