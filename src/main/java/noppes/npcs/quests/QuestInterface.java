package noppes.npcs.quests;

import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.scripted.interfaces.handler.data.IQuestInterface;
import noppes.npcs.scripted.interfaces.handler.data.IQuestObjective;

public abstract class QuestInterface implements IQuestInterface {
	public int questId;
	public abstract void writeEntityToNBT(NBTTagCompound compound);
	public abstract void readEntityFromNBT(NBTTagCompound compound);
	public abstract boolean isCompleted(PlayerData player);
	public abstract void handleComplete(EntityPlayer player);
	public abstract Vector<String> getQuestLogStatus(EntityPlayer player);
	public abstract IQuestObjective[] getObjectives(EntityPlayer var1);
}
