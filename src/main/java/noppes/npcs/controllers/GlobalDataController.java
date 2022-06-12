package noppes.npcs.controllers;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.HashMap;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;
import noppes.npcs.roles.JobItemGiver;

public class GlobalDataController {
	public static GlobalDataController instance;
	
	private int itemGiverId = 0;
	public HashMap<Integer, JobItemGiver> itemGivers = new HashMap<>();
	
	public GlobalDataController(){
		instance = this;
		load();
	}
	
	private void load(){
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		try {
	        File file = new File(saveDir, "global.dat");
	        if(file.exists()){
	        	loadData(file);
	        }
		} catch (Exception e) {
			try {
		        File file = new File(saveDir, "global.dat_old");
		        if(file.exists()){
		        	loadData(file);
		        }
		        
			} catch (Exception ee) {
				ee.printStackTrace();
			}
		}
	}
	private void loadData(File file) throws Exception {
        NBTTagCompound nbttagcompound1 = CompressedStreamTools.readCompressed(new FileInputStream(file));
        itemGiverId = nbttagcompound1.getInteger("itemGiverId");

		NBTTagList jobList = nbttagcompound1.getTagList("ItemGivers", 10);
		for (int i = 0; i < jobList.tagCount(); i++) {
			NBTTagCompound compound = jobList.getCompoundTagAt(i);
			JobItemGiver jobItemGiver = new JobItemGiver();
			jobItemGiver.readFromNBT(compound);
			itemGivers.put(jobItemGiver.itemGiverId, jobItemGiver);
		}
	}
	public void saveData(){
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();

	        NBTTagCompound nbttagcompound = new NBTTagCompound();
	        nbttagcompound.setInteger("itemGiverId", itemGiverId);

			NBTTagList jobList = new NBTTagList();
			for (JobItemGiver jobItemGiver : itemGivers.values()) {
				NBTTagCompound compound = new NBTTagCompound();
				jobItemGiver.writeToNBT(compound);
				jobList.appendTag(compound);
			}
			nbttagcompound.setTag("ItemGivers", jobList);
	        
            File file = new File(saveDir, "global.dat_new");
            File file1 = new File(saveDir, "global.dat_old");
            File file2 = new File(saveDir, "global.dat");
            CompressedStreamTools.writeCompressed(nbttagcompound, new FileOutputStream(file));
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
			e.printStackTrace();
		}
	}
	public int incrementItemGiverId(){
		itemGiverId++;
		return itemGiverId;
	}
}
