package noppes.npcs.controllers.data;

import java.util.HashMap;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import noppes.npcs.EventHooks;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.FactionEvent;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.handler.IPlayerFactionData;

public class PlayerFactionData implements IPlayerFactionData {
	private final PlayerData parent;
	public HashMap<Integer,Integer> factionData = new HashMap<Integer,Integer>();

	public PlayerFactionData() {
		parent = null;
	}

	public PlayerFactionData(PlayerData parent) {
		this.parent = parent;
	}

	public void loadNBTData(NBTTagCompound compound) {
		HashMap<Integer,Integer> factionData = new HashMap<Integer,Integer>();
		if(compound == null)
			return;
        NBTTagList list = compound.getTagList("FactionData", 10);
        if(list == null){
        	return;
        }

        for(int i = 0; i < list.tagCount(); i++)
        {
            NBTTagCompound nbttagcompound = list.getCompoundTagAt(i);
            factionData.put(nbttagcompound.getInteger("Faction"),nbttagcompound.getInteger("Points"));
        }
        this.factionData = factionData;
	}

	public void saveNBTData(NBTTagCompound compound) {
		NBTTagList list = new NBTTagList();
		for(int faction : factionData.keySet()){
			NBTTagCompound nbttagcompound = new NBTTagCompound();
			nbttagcompound.setInteger("Faction", faction);
			nbttagcompound.setInteger("Points", factionData.get(faction));
			list.appendTag(nbttagcompound);
		}
		
		compound.setTag("FactionData", list);
	}

	public int getFactionPoints(int id) {
		if(!factionData.containsKey(id)){
			Faction faction = FactionController.getInstance().get(id);
			factionData.put(id, faction == null? -1 : faction.defaultPoints);
		}
		return factionData.get(id);
	}

	public void increasePoints(int factionId, int points, EntityPlayer player) {
		if (EventHooks.onFactionPoints(new FactionEvent.FactionPoints((IPlayer) NpcAPI.Instance().getIEntity((EntityPlayerMP) player), FactionController.getInstance().get(factionId), points < 0, points)))
			return;

		if(!factionData.containsKey(factionId)){
			Faction faction = FactionController.getInstance().get(factionId);
			factionData.put(factionId, faction == null? -1 : faction.defaultPoints);
		}
		factionData.put(factionId, factionData.get(factionId) + points);
	}
	
	public NBTTagCompound getPlayerGuiData(){
		NBTTagCompound compound = new NBTTagCompound();
		saveNBTData(compound);
		
		NBTTagList list = new NBTTagList();
		for(int id : factionData.keySet()){
			Faction faction = FactionController.getInstance().get(id);
			if(faction == null || faction.hideFaction)
				continue;
			NBTTagCompound com = new NBTTagCompound();
			faction.writeNBT(com);
			list.appendTag(com);
		}
		compound.setTag("FactionList", list);
		
		return compound;
	}

	public int getPoints(int id) {
		return this.getFactionPoints(id);
	}

	public void addPoints(int id, int points) {
		if (parent != null) {
			this.increasePoints(id, points, parent.player);
		}
	}

	public void setPoints(int id, int points) {
		if (parent != null) {
			increasePoints(id, points-getFactionPoints(id), parent.player);
		}
	}
}
