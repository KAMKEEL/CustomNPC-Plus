package noppes.npcs.controllers;

import net.minecraft.nbt.NBTTagCompound;

public class QuestData {
	public Quest quest;
	public boolean isCompleted;
	public NBTTagCompound extraData = new NBTTagCompound();
	public QuestData(Quest quest){
		this.quest = quest;
	}
	public void writeEntityToNBT(NBTTagCompound nbttagcompound){
		nbttagcompound.setBoolean("QuestCompleted", isCompleted);
		nbttagcompound.setTag("ExtraData", extraData);
	}
	public void readEntityFromNBT(NBTTagCompound nbttagcompound){
		isCompleted = nbttagcompound.getBoolean("QuestCompleted");
		extraData = nbttagcompound.getCompoundTag("ExtraData");
	}
}
