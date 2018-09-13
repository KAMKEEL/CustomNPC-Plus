package noppes.npcs.controllers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class FactionController {
	public HashMap<Integer,Faction> factions;
	
	private static FactionController instance;

	private int lastUsedID = 0;
	
	public FactionController(){
		instance = this;
		factions = new HashMap<Integer, Faction>();
		loadFactions();
		if(factions.isEmpty()){
			factions.put(0,new Faction(0,"Friendly", 0x00DD00, 2000));
			factions.put(1,new Faction(1,"Neutral", 0xF2DD00, 1000));
			factions.put(2,new Faction(2,"Aggressive", 0xDD0000, 0));
		}
		
	}
	
	public static FactionController getInstance(){
		return instance;
	}

	private void loadFactions(){

		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if(saveDir == null){
			return;
		}
		try {
	        File file = new File(saveDir, "factions.dat");
	        if(file.exists()){
	        	loadFactionsFile(file);
	        }
		} catch (Exception e) {
			try {
		        File file = new File(saveDir, "factions.dat_old");
		        if(file.exists()){
		        	loadFactionsFile(file);
		        }
		        
			} catch (Exception ee) {
			}
		}
	}

	private void loadFactionsFile(File file) throws IOException{
        DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		loadFactions(var1);
		var1.close();
	}

	public void loadFactions(DataInputStream stream) throws IOException{
		HashMap<Integer,Faction> factions = new HashMap<Integer,Faction>();
        NBTTagCompound nbttagcompound1 = CompressedStreamTools.read(stream);
        lastUsedID = nbttagcompound1.getInteger("lastID");
        NBTTagList list = nbttagcompound1.getTagList("NPCFactions", 10);

	    if(list != null){
	        for(int i = 0; i < list.tagCount(); i++)
	        {
	            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
	            Faction faction = new Faction();
	            faction.readNBT(nbttagcompound);
	            factions.put(faction.id,faction);
	        }
	    }
    	this.factions = factions;
	}
	public NBTTagCompound getNBT(){
        NBTTagList list = new NBTTagList();
        for(int slot : factions.keySet()){
        	Faction faction = factions.get(slot);
            NBTTagCompound nbtfactions = new NBTTagCompound();
            faction.writeNBT(nbtfactions);
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
            File file = new File(saveDir, "factions.dat_new");
            File file1 = new File(saveDir, "factions.dat_old");
            File file2 = new File(saveDir, "factions.dat");
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

	public Faction getFaction(int faction) {
		return factions.get(faction);
	}

	public void saveFaction(Faction faction) {
		
		if(faction.id < 0){
			faction.id = getUnusedId();
			while(hasName(faction.name))
				faction.name += "_";
		}
		else{
			Faction existing = factions.get(faction.id);
			if(existing != null && !existing.name.equals(faction.name))
				while(hasName(faction.name))
					faction.name += "_";
		}
		factions.remove(faction.id);
		factions.put(faction.id, faction);
		saveFactions();
	}
	
	public int getUnusedId(){
		if(lastUsedID == 0){
			for(int catid : factions.keySet())
				if(catid > lastUsedID)
					lastUsedID = catid;
		}
		lastUsedID++;
		return lastUsedID;
	}
	public void removeFaction(int id) {
		if(id < 0 || factions.size() <= 1)
			return;
		factions.remove(id);
		saveFactions();
	}
	
	public int getFirstFactionId() {
		return factions.keySet().iterator().next();
	}
	public Faction getFirstFaction() {
		return factions.values().iterator().next();
	}
	public boolean hasName(String newName) {
		if(newName.trim().isEmpty())
			return true;
		for(Faction faction : factions.values())
			if(faction.name.equals(newName))
				return true;
		return false;
	}
        public Faction getFactionFromName(String factioname){
            for (Map.Entry<Integer,Faction>entryfaction:FactionController.getInstance().factions.entrySet()){
                if (entryfaction.getValue().name.equalsIgnoreCase(factioname)){
                    return entryfaction.getValue();
                }
            }
            return null;
        }

		public String[] getNames() {
			String[] names = new String[factions.size()];
			int i = 0;
			for(Faction faction : factions.values()){
				names[i] = faction.name.toLowerCase();
				i++;
			}
			return names;
		}
}
