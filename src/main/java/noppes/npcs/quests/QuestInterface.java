package noppes.npcs.quests;

import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.scripted.handler.data.IQuestObjective;

public abstract class QuestInterface {
	public int questId;
	public abstract void writeEntityToNBT(NBTTagCompound compound);
	public abstract void readEntityFromNBT(NBTTagCompound compound);
	public abstract boolean isCompleted(EntityPlayer player);
	public abstract void handleComplete(EntityPlayer player);
	public abstract Vector<String> getQuestLogStatus(EntityPlayer player);
	public abstract IQuestObjective[] getObjectives(EntityPlayer var1);
}
