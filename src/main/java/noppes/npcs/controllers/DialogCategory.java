package noppes.npcs.controllers;

import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;

public class DialogCategory {

	public int id = -1;
	public String title = "";
	public HashMap<Integer,Dialog> dialogs;
	
	public DialogCategory(){
		dialogs = new HashMap<Integer, Dialog>();
	}
	
	public void readNBT(NBTTagCompound compound){
        id = compound.getInteger("Slot");
        title = compound.getString("Title");
        
        NBTTagList dialogsList = compound.getTagList("Dialogs", 10);
        if(dialogsList != null){
            for(int ii = 0; ii < dialogsList.tagCount(); ii++)
            {
            	Dialog dialog = new Dialog();
            	dialog.category = this;
            	NBTTagCompound comp = dialogsList.getCompoundTagAt(ii);
            	dialog.readNBT(comp);
            	dialog.id = comp.getInteger("DialogId");
            	dialogs.put(dialog.id, dialog);
            }
        }
	}

	public NBTTagCompound writeNBT(NBTTagCompound nbtfactions) {
        nbtfactions.setInteger("Slot", id);
        nbtfactions.setString("Title", title);
        NBTTagList dialogs = new NBTTagList();
        for(Dialog dialog : this.dialogs.values()){
        	dialogs.appendTag(dialog.writeToNBT(new NBTTagCompound()));
        }
        nbtfactions.setTag("Dialogs", dialogs);
        return nbtfactions;
	}
}
