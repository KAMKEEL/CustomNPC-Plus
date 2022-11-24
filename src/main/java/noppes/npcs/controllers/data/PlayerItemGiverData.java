package noppes.npcs.controllers.data;

import java.util.ArrayList;
import java.util.HashMap;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.GlobalDataController;
import noppes.npcs.roles.JobItemGiver;
import noppes.npcs.api.handler.IPlayerItemGiverData;
import noppes.npcs.api.jobs.IJobItemGiver;
import noppes.npcs.scripted.roles.ScriptJobItemGiver;

public class PlayerItemGiverData implements IPlayerItemGiverData {
	private final PlayerData parent;
	private HashMap<Integer, Long> itemgivers = new HashMap<Integer,Long>();
	private HashMap<Integer, Integer> chained = new HashMap<Integer,Integer>();

	public PlayerItemGiverData(PlayerData parent) {
		this.parent = parent;
	}

	public void loadNBTData(NBTTagCompound compound) {
		chained = NBTTags.getIntegerIntegerMap(compound.getTagList("ItemGiverChained", 10));
		itemgivers = NBTTags.getIntegerLongMap(compound.getTagList("ItemGiversList", 10));
	}

	public void saveNBTData(NBTTagCompound compound) {
		compound.setTag("ItemGiverChained", NBTTags.nbtIntegerIntegerMap(chained));
		compound.setTag("ItemGiversList", NBTTags.nbtIntegerLongMap(itemgivers));
	}
	public boolean hasInteractedBefore(JobItemGiver jobItemGiver) {
		return itemgivers.containsKey(jobItemGiver.itemGiverId);
	}
	
	public long getTime(JobItemGiver jobItemGiver){
		return itemgivers.get(jobItemGiver.itemGiverId);
	}
	public void setTime(JobItemGiver jobItemGiver, long day) {
		itemgivers.put(jobItemGiver.itemGiverId, day);
	}
	public int getItemIndex(JobItemGiver jobItemGiver) {
		if(chained.containsKey(jobItemGiver.itemGiverId))
			return chained.get(jobItemGiver.itemGiverId);
		return 0;
	}
	public void setItemIndex(JobItemGiver jobItemGiver, int i) {
		chained.put(jobItemGiver.itemGiverId, i);
	}

	public long getTime(IJobItemGiver jobItemGiver){
		return itemgivers.get(((JobItemGiver) jobItemGiver).itemGiverId);
	}

	public void setTime(IJobItemGiver jobItemGiver, long day) {
		itemgivers.put(((JobItemGiver) jobItemGiver).itemGiverId, day);
	}

	public boolean hasInteractedBefore(IJobItemGiver jobItemGiver) {
		return itemgivers.containsKey(((JobItemGiver) jobItemGiver).itemGiverId);
	}

	public IJobItemGiver[] getItemGivers() {
		ArrayList<IJobItemGiver> list = new ArrayList<>();
		for (JobItemGiver jobItemGiver : GlobalDataController.instance.itemGivers.values()) {
			if (jobItemGiver.npc != null) {
				list.add(new ScriptJobItemGiver(jobItemGiver.npc));
			} else {
				list.add(new ScriptJobItemGiver(jobItemGiver));
			}
		}

		return list.toArray(new IJobItemGiver[0]);
	}
}
