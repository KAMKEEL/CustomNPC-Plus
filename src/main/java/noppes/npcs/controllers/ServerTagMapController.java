package noppes.npcs.controllers;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.TagMap;

import java.io.*;
import java.util.zip.GZIPInputStream;

public class ServerTagMapController {
	public static ServerTagMapController Instance;
	public TagMap tagMap;

	public ServerTagMapController(){}

	public File getDir(){
		File dir = new File(CustomNpcs.getWorldSaveDirectory(), "clones");
		if(!dir.exists())
			dir.mkdir();
		return dir;
	}

	public File getCloneTabDir(int tab){
		File dir = new File(getDir(), tab + "");
		if(!dir.exists())
			dir.mkdir();
		return dir;
	}

	public TagMap getTagMap(int tab){
		this.tagMap = new TagMap(tab);
		try {
			File file = new File(getCloneTabDir(tab), "_____tag_map.dat");
			if(file.exists()){
				loadTagMapFile(file);
			}
		} catch (Exception e) {
			try {
				File file = new File(getCloneTabDir(tab), "_____tag_map.dat_old");
				if(file.exists()){
					loadTagMapFile(file);
				}

			} catch (Exception ee) {
			}
		}
		return this.tagMap;
	}

	private void loadTagMapFile(File file) throws IOException {
		DataInputStream var1 = new DataInputStream(new BufferedInputStream(new GZIPInputStream(new FileInputStream(file))));
		loadTagMaps(var1);
		var1.close();
	}

	public void loadTagMaps(DataInputStream stream) throws IOException{
		NBTTagCompound nbtCompound = CompressedStreamTools.read(stream);
		this.tagMap.readNBT(nbtCompound);
	}

}
