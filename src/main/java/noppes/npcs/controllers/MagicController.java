package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.api.handler.data.IMagic;
import noppes.npcs.controllers.data.Magic;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.zip.GZIPInputStream;

public class MagicController {
    public HashMap<Integer, Magic> magicSync = new HashMap<Integer,Magic>();
	public HashMap<Integer,Magic> magics;

	private static MagicController instance = new MagicController();

	private int lastUsedID = 0;

	public MagicController(){
		instance = this;
		magics = new HashMap<Integer, Magic>();
	}

	public static MagicController getInstance(){
		return instance;
	}

	public Magic getMagic(int magic) {
		return magics.get(magic);
	}

	public void load(){
        magics = new HashMap<Integer, Magic>();
        lastUsedID = 0;
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if(saveDir == null){
			return;
		}
		try {
			File file = new File(saveDir, "magic.dat");
			if(file.exists()){
				loadMagicFile(file);
			}
		} catch (Exception e) {
			try {
				File file = new File(saveDir, "magic.dat_old");
				if(file.exists()){
					loadMagicFile(file);
				}

			} catch (Exception ee) {
			}
		}

        if(magics.isEmpty()){
            magics.put(0,new Magic(0,"Nature", 0x00DD00));
            magics.put(1,new Magic(1,"Arcane", 0xF2DD00));
            magics.put(2,new Magic(2,"Ice", 0xDD0000));
            magics.put(3,new Magic(3,"Fire", 0xDD0000));
            magics.put(4,new Magic(4,"Dark", 0xDD0000));
            magics.put(5,new Magic(5,"Holy", 0xDD0000));
        }
	}

	private void loadMagicFile(File file) throws IOException{
		DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		loadMagic(var1);
		var1.close();
	}

	public void loadMagic(DataInputStream stream) throws IOException{
		HashMap<Integer,Magic> magic = new HashMap<Integer,Magic>();
		NBTTagCompound nbttagcompound1 = CompressedStreamTools.read(stream);
		lastUsedID = nbttagcompound1.getInteger("lastID");
		NBTTagList list = nbttagcompound1.getTagList("NPCMagic", 10);

		if(list != null){
			for(int i = 0; i < list.tagCount(); i++)
			{
				NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
				Magic loadMagic = new Magic();
				loadMagic.readNBT(nbttagcompound);
				magic.put(loadMagic.id,loadMagic);
			}
		}
		this.magics = magic;
	}
	public NBTTagCompound getNBT(){
		NBTTagList list = new NBTTagList();
		for(int slot : magics.keySet()){
			Magic mag = magics.get(slot);
			NBTTagCompound nbtfactions = new NBTTagCompound();
			mag.writeNBT(nbtfactions);
			list.appendTag(nbtfactions);
		}
		NBTTagCompound nbttagcompound = new NBTTagCompound();
		nbttagcompound.setInteger("lastID", lastUsedID);
		nbttagcompound.setTag("NPCFactions", list);
		return nbttagcompound;
	}
	public void saveFactions(){
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
			File file = new File(saveDir, "magic.dat_new");
			File file1 = new File(saveDir, "magic.dat_old");
			File file2 = new File(saveDir, "magic.dat");
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

	public Magic get(int magicID) {
		return magics.get(magicID);
	}

	public List<IMagic> list() {
		return new ArrayList(this.magics.values());
	}

	public void saveMagic(Magic mag) {

		if(mag.id < 0){
			mag.id = getUnusedId();
			while(hasName(mag.name))
				mag.name += "_";
		}
		else{
			Magic existing = magics.get(mag.id);
			if(existing != null && !existing.name.equals(mag.name))
				while(hasName(mag.name))
					mag.name += "_";
		}
		magics.remove(mag.id);
		magics.put(mag.id, mag);

        NBTTagCompound facCompound = new NBTTagCompound();
        mag.writeNBT(facCompound);
        // Server.sendToAll(EnumPacketClient.SYNC_UPDATE, SyncType.MAGIC, facCompound);
		saveFactions();
	}

	public int getUnusedId(){
		if(lastUsedID == 0){
			for(int catid : magics.keySet())
				if(catid > lastUsedID)
					lastUsedID = catid;
		}
		lastUsedID++;
		return lastUsedID;
	}

	public boolean hasName(String newName) {
		if(newName.trim().isEmpty())
			return true;
		for(Magic mag : magics.values())
			if(mag.name.equals(newName))
				return true;
		return false;
	}

	public Magic getMagicFromName(String magicName){
		for (Map.Entry<Integer,Magic> entryMag: MagicController.getInstance().magics.entrySet()){
			if (entryMag.getValue().name.equalsIgnoreCase(magicName)){
				return entryMag.getValue();
			}
		}
		return null;
	}

	public String[] getNames() {
		String[] names = new String[magics.size()];
		int i = 0;
		for(Magic mag : magics.values()){
			names[i] = mag.name.toLowerCase();
			i++;
		}
		return names;
	}
}
