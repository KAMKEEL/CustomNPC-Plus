package noppes.npcs.controllers.data;

import noppes.npcs.api.entity.IEntityLiving;
import noppes.npcs.api.IDamageSource;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IEntityLivingBase;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.api.entity.IEntity;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.INbtList;
import noppes.npcs.api.INbt;
public class QuestData {
    public Quest quest;
    public boolean isCompleted;
    public boolean sendAlerts = true;
    public INbt extraData = new INbt();

    public QuestData(Quest quest) {
        this.quest = quest;
    }

    public void writeEntityToNBT(INbt INbt) {
        INbt.setBoolean("QuestCompleted", isCompleted);
        INbt.setBoolean("SendAlerts", sendAlerts);
        INbt.setTag("ExtraData", extraData);
    }

    public void readEntityFromNBT(INbt INbt) {
        isCompleted = INbt.getBoolean("QuestCompleted");
        sendAlerts = INbt.getBoolean("SendAlerts");
        extraData = INbt.getCompoundTag("ExtraData");
    }
}
