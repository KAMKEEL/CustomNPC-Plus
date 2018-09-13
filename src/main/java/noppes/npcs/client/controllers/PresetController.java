package noppes.npcs.client.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.LogWriter;

public class PresetController {

	public HashMap<String,Preset> presets = new HashMap<String,Preset>();
	
	private File dir;
	public static PresetController instance;
	
	public PresetController(File dir){
		instance = this;
		this.dir = dir;
		load();
	}

	public Preset getPreset(String username) {
		if(presets.isEmpty())
			load();
		return presets.get(username.toLowerCase());
	}
	
	public void load(){
		NBTTagCompound compound = loadPreset();
		HashMap<String,Preset> presets = new HashMap<String, Preset>();
		if(compound != null){
			NBTTagList list = compound.getTagList("Presets", 10);
			for(int i = 0; i < list.tagCount(); i++){
				NBTTagCompound comp = list.getCompoundTagAt(i);
				Preset preset = new Preset();
				preset.readFromNBT(comp);
				presets.put(preset.name.toLowerCase(), preset);
			}
		}
		Preset.FillDefault(presets);
		this.presets = presets;
	}

	private NBTTagCompound loadPreset(){
		String filename = "presets.dat";
		try {
	        File file = new File(dir, filename);
	        if(!file.exists()){
				return null;
	        }
	        return CompressedStreamTools.readCompressed(new FileInputStream(file));
		} catch (Exception e) {
			LogWriter.except(e);
		}
		try {
	        File file = new File(dir, filename+"_old");
	        if(!file.exists()){
				return null;
	        }
	        return CompressedStreamTools.readCompressed(new FileInputStream(file));
	        
		} catch (Exception e) {
			LogWriter.except(e);
		}
		return null;
	}
	
	public void save(){
		NBTTagCompound compound = new NBTTagCompound();
		NBTTagList list = new NBTTagList();
		for(Preset preset : presets.values()){
			list.appendTag(preset.writeToNBT());
		}
		
		compound.setTag("Presets", list);
		savePreset(compound);
	}

	private void savePreset(NBTTagCompound compound){
		String filename = "presets.dat";
		try {
            File file = new File(dir, filename+"_new");
            File file1 = new File(dir, filename+"_old");
            File file2 = new File(dir, filename);
            CompressedStreamTools.writeCompressed(compound, new FileOutputStream(file));
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

	public void addPreset(Preset preset) {
		while(presets.containsKey(preset.name.toLowerCase())){
			preset.name += "_";
		}
		presets.put(preset.name.toLowerCase(), preset);
		save();
	}

	public void removePreset(String preset) {
		if(preset == null)
			return;
		presets.remove(preset.toLowerCase());
		save();
	}
}
