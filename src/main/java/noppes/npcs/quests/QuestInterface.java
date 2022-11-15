package noppes.npcs.quests;

import java.util.Vector;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.Server;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.api.handler.data.IQuestInterface;
import noppes.npcs.api.handler.data.IQuestObjective;

public abstract class QuestInterface implements IQuestInterface {
	public int questId;
	public abstract void writeEntityToNBT(NBTTagCompound compound);
	public abstract void readEntityFromNBT(NBTTagCompound compound);
	public abstract boolean isCompleted(PlayerData player);
	public void handleComplete(EntityPlayer player) {
		if (QuestController.instance.get(questId).equals(PlayerDataController.instance.getPlayerData(player).questData.trackedQuest)) {
			PlayerDataController.instance.getPlayerData(player).questData.trackedQuest = null;
			Server.sendData((EntityPlayerMP) player, EnumPacketClient.OVERLAY_QUEST_TRACKING);
		}
	}
	public abstract Vector<String> getQuestLogStatus(EntityPlayer player);
	public abstract IQuestObjective[] getObjectives(EntityPlayer var1);
}
