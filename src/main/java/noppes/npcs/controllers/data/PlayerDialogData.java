package noppes.npcs.controllers.data;

import java.util.HashSet;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.api.handler.IPlayerDialogData;

public class PlayerDialogData implements IPlayerDialogData {
	private final PlayerData parent;
	public HashSet<Integer> dialogsRead = new HashSet<Integer>();

	public PlayerDialogData(PlayerData parent) {
		this.parent = parent;
	}
	
	public void loadNBTData(NBTTagCompound compound) {
		HashSet<Integer> dialogsRead = new HashSet<Integer>();
		if(compound == null)
			return;
        NBTTagList list = compound.getTagList("DialogData", 10);
        if(list == null){
        	return;
        }

        for(int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
            dialogsRead.add(nbttagcompound.getInteger("Dialog"));
        }
        this.dialogsRead = dialogsRead;
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for(int dia : dialogsRead){
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Dialog", dia);
			list.appendTag(nbttagcompound);
		}
		
		compound.setTag("DialogData", list);
	}

	public boolean hasReadDialog(int id) {
		return dialogsRead.contains(id);
	}

	public void readDialog(int id) {
		dialogsRead.add(id);
	}

	public void unreadDialog(int id) {
		dialogsRead.remove(id);
	}
}
