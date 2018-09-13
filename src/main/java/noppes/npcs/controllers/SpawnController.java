package noppes.npcs.controllers;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.WeightedRandom;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;

public class SpawnController {
	public HashMap<String,List<SpawnData>> biomes = new HashMap<String, List<SpawnData>>() ;
	public ArrayList<SpawnData> data = new ArrayList<SpawnData>();
	public Random random = new Random();
	
	public static SpawnController instance;

	private int lastUsedID = 0;
	
	public SpawnController(){
		instance = this;
		loadData();
		
	}

	private void loadData(){

		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if(saveDir == null){
			return;
		}
		try {
	        File file = new File(saveDir, "spawns.dat");
	        if(file.exists()){
	        	loadDataFile(file);
	        }
		} catch (Exception e) {
			try {
		        File file = new File(saveDir, "spawns.dat_old");
		        if(file.exists()){
		        	loadDataFile(file);
		        }
		        
			} catch (Exception ee) {
			}
		}
	}

	private void loadDataFile(File file) throws IOException{
        DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		loadData(var1);
		var1.close();
	}

	public void loadData(DataInputStream stream) throws IOException{
		ArrayList<SpawnData> data = new ArrayList<SpawnData>();
        NBTTagCompound nbttagcompound1 = CompressedStreamTools.read(stream);
        lastUsedID = nbttagcompound1.getInteger("lastID");
        NBTTagList nbtlist = nbttagcompound1.getTagList("NPCSpawnData", 10);

	    if(nbtlist != null){
	        for(int i = 0; i < nbtlist.tagCount(); i++)
	        {
	            NBTTagCompound nbttagcompound = nbtlist.getCompoundTagAt(i);
	            SpawnData spawn = new SpawnData();
	            spawn.readNBT(nbttagcompound);
	            data.add(spawn);
	        }
	    }
	    this.data = data;
		fillBiomeData();
	}
	public NBTTagCompound getNBT(){
        NBTTagList list = new NBTTagList();
        for(SpawnData spawn : data){
            NBTTagCompound nbtfactions = new NBTTagCompound();
            spawn.writeNBT(nbtfactions);
        	list.appendTag(nbtfactions);
        }
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setInteger("lastID", lastUsedID);
        nbttagcompound.setTag("NPCSpawnData", list);
        return nbttagcompound;
	}
	public void saveData(){
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
            File file = new File(saveDir, "spawns.dat_new");
            File file1 = new File(saveDir, "spawns.dat_old");
            File file2 = new File(saveDir, "spawns.dat");
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

	public SpawnData getSpawnData(int id) {
		for(SpawnData spawn : data)
			if(spawn.id == id)
				return spawn;
		return null;
	}

	public void saveSpawnData(SpawnData spawn) {
		if(spawn.id < 0)
			spawn.id = getUnusedId();
		SpawnData original = getSpawnData(spawn.id);
		if(original == null)
			data.add(spawn);
		else{
			original.readNBT(spawn.writeNBT(new NBTTagCompound()));
		}
		fillBiomeData();
		saveData();
	}
	
	private void fillBiomeData(){
		HashMap<String,List<SpawnData>> biomes = new HashMap<String, List<SpawnData>>() ;
		for(SpawnData spawn : data){
	        for(String s : spawn.biomes){
	        	List<SpawnData> list = biomes.get(s);
	        	if(list == null)
	        		biomes.put(s, (list = new ArrayList<SpawnData>()));
	        	list.add(spawn);
	        }
		}
        this.biomes = biomes;
	}
	
	public int getUnusedId(){
		lastUsedID++;
		return lastUsedID;
	}
	public void removeSpawnData(int id) {
		ArrayList<SpawnData> data = new ArrayList<SpawnData>();
		
		for(SpawnData spawn : this.data){
			if(spawn.id == id)
				continue;
            data.add(spawn);
		}
		this.data = data;

		fillBiomeData();
		saveData();
	}
	
	public List<SpawnData> getSpawnList(String biome) {
		return biomes.get(biome);
	}

	public SpawnData getRandomSpawnData(String biome, boolean isAir) {
		List<SpawnData> list = getSpawnList(biome);
		if(list == null || list.isEmpty())
			return null;
		return (SpawnData) WeightedRandom.getRandomItem(this.random, list);
	}

	public Map<String,Integer> getScroll() {
		Map<String,Integer> map = new HashMap<String,Integer>();
		for(SpawnData spawn : data){
			map.put(spawn.name, spawn.id);
		}
		return map;
	}
}
