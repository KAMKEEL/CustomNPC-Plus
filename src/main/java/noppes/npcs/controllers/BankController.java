package noppes.npcs.controllers;

import io.netty.buffer.ByteBufInputStream;

import java.io.BufferedInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.HashMap;
import java.util.zip.GZIPInputStream;

import net.minecraft.nbt.CompressedStreamTools;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.CustomNpcs;

public class BankController {
	public HashMap<Integer,Bank> banks;
	private String filePath = "";

	private static BankController instance;
	
	public BankController(){
		instance = this;
		banks = new HashMap<Integer, Bank>();
		loadBanks();

		if(banks.isEmpty())
		{
			Bank bank = new Bank();
			bank.id = 0;
			bank.name = "Default Bank";
			for(int i = 0;i < 6;i++)
				bank.slotTypes.put(i, 0);
			
			banks.put(bank.id, bank);
		}
	}
	public static BankController getInstance(){
		if(newInstance()){
			instance = new BankController();
		}
		return instance;
	}
	
	private static boolean newInstance() {
		if(instance == null)
			return true;
		File file = CustomNpcs.getWorldSaveDirectory();
		if(file == null)
			return false;
		if(!instance.filePath.equals(file.getAbsolutePath())){
			return true;
		}
		
		return false;
	}
	
	private void loadBanks(){
		File saveDir = CustomNpcs.getWorldSaveDirectory();
		if(saveDir == null)
			return;

		filePath = saveDir.getAbsolutePath();
		
		try {
	        File file = new File(saveDir, "bank.dat");
	        if(file.exists()){
		        loadBanks(file);
	        }
		} catch (Exception e) {
			try {
		        File file = new File(saveDir, "bank.dat_old");
		        if(file.exists()){
		        	loadBanks(file);
		        }
		        
			} catch (Exception ee) {
			}
		}
	}
	private void loadBanks(File file) throws IOException{
		loadBanks(CompressedStreamTools.readCompressed(new FileInputStream(file)));
	}
	
	public void loadBanks(NBTTagCompound nbttagcompound1) throws IOException{
		HashMap<Integer,Bank> banks = new HashMap<Integer,Bank>();
        NBTTagList list = nbttagcompound1.getTagList("Data", 10);
        if(list != null){
	        for(int i = 0; i < list.tagCount(); i++)
	        {
	            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
	            Bank bank = new Bank();
	            bank.readEntityFromNBT(nbttagcompound);
	            banks.put(bank.id,bank);
	        }
        }
		this.banks = banks;
	}

	public NBTTagCompound getNBT(){

        NBTTagList list = new NBTTagList();
        for(Bank bank : banks.values()){
            NBTTagCompound nbtfactions = new NBTTagCompound();
            bank.writeEntityToNBT(nbtfactions);
        	list.appendTag(nbtfactions);
        }
        NBTTagCompound nbttagcompound = new NBTTagCompound();
        nbttagcompound.setTag("Data", list);
        return nbttagcompound;
	}
	public Bank getBank(int bankId) {
		Bank bank = banks.get(bankId);
		if(bank != null)
			return bank;
		return banks.values().iterator().next();
	}
	public void saveBanks(){
		try {
			File saveDir = CustomNpcs.getWorldSaveDirectory();
            File file = new File(saveDir, "bank.dat_new");
            File file1 = new File(saveDir, "bank.dat_old");
            File file2 = new File(saveDir, "bank.dat");
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
			e.printStackTrace();
		}
	}
	public void saveBank(Bank bank) {
		if(bank.id < 0){
			bank.id = getUnusedId();
		}
		banks.put(bank.id, bank);
		saveBanks();
	}
	
	public int getUnusedId(){
		int id = 0;
		while(true){
			if(!banks.containsKey(id))
				return id;
			id++;
		}
	}
	public void removeBank(int bank) {
		if(bank < 0 || banks.size() <= 1)
			return;
		banks.remove(bank);
		saveBanks();
	}
}
