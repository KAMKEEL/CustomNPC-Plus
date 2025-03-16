package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;

public class QuestData {
    public Quest quest;
    public boolean isCompleted;
    public boolean sendAlerts = true;
    public NBTTagCompound extraData = new NBTTagCompound();

    public QuestData(Quest quest) {
        this.quest = quest;
    }

    public void writeEntityToNBT(NBTTagCompound nbttagcompound) {
        nbttagcompound.setBoolean("QuestCompleted", isCompleted);
        nbttagcompound.setBoolean("SendAlerts", sendAlerts);
        nbttagcompound.setTag("ExtraData", extraData);
    }

    public void readEntityFromNBT(NBTTagCompound nbttagcompound) {
        isCompleted = nbttagcompound.getBoolean("QuestCompleted");
        sendAlerts = nbttagcompound.getBoolean("SendAlerts");
        extraData = nbttagcompound.getCompoundTag("ExtraData");
    }
}
