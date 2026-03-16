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
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.IPlayerItemGiverData;
import noppes.npcs.api.jobs.IJobItemGiver;
import noppes.npcs.roles.JobItemGiver;

import java.util.HashMap;

public class PlayerItemGiverData implements IPlayerItemGiverData {
    private final PlayerData parent;
    private HashMap<Integer, Long> itemgivers = new HashMap<Integer, Long>();
    private HashMap<Integer, Integer> chained = new HashMap<Integer, Integer>();

    public PlayerItemGiverData(PlayerData parent) {
        this.parent = parent;
    }

    public void loadNBTData(INbt compound) {
        chained = NBTTags.getIntegerIntegerMap(compound.getTagList("ItemGiverChained", 10));
        itemgivers = NBTTags.getIntegerLongMap(compound.getTagList("ItemGiversList", 10));
    }

    public void saveNBTData(INbt compound) {
        compound.setTag("ItemGiverChained", NBTTags.nbtIntegerIntegerMap(chained));
        compound.setTag("ItemGiversList", NBTTags.nbtIntegerLongMap(itemgivers));
    }

    public boolean hasInteractedBefore(JobItemGiver jobItemGiver) {
        return itemgivers.containsKey(jobItemGiver.itemGiverId);
    }

    public long getTime(JobItemGiver jobItemGiver) {
        return itemgivers.get(jobItemGiver.itemGiverId);
    }

    public void setTime(JobItemGiver jobItemGiver, long day) {
        itemgivers.put(jobItemGiver.itemGiverId, day);
    }

    public int getItemIndex(JobItemGiver jobItemGiver) {
        if (chained.containsKey(jobItemGiver.itemGiverId))
            return chained.get(jobItemGiver.itemGiverId);
        return 0;
    }

    public void setItemIndex(JobItemGiver jobItemGiver, int i) {
        chained.put(jobItemGiver.itemGiverId, i);
    }

    public long getTime(IJobItemGiver jobItemGiver) {
        return itemgivers.get(((JobItemGiver) jobItemGiver).itemGiverId);
    }

    public void setTime(IJobItemGiver jobItemGiver, long day) {
        itemgivers.put(((JobItemGiver) jobItemGiver).itemGiverId, day);
    }

    public boolean hasInteractedBefore(IJobItemGiver jobItemGiver) {
        return itemgivers.containsKey(((JobItemGiver) jobItemGiver).itemGiverId);
    }
}
