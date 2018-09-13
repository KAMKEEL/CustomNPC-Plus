package noppes.npcs.controllers;

import java.util.HashSet;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.entity.EntityNPCInterface;

public class Faction {
	public String name = "";
	public int color = Integer.parseInt("FF00", 16);
	
	public HashSet<Integer> attackFactions;
	public int id = -1;
	
	public int neutralPoints = 500;
	public int friendlyPoints = 1500;
		
	public int defaultPoints = 1000;
	
	public boolean hideFaction = false;
	public boolean getsAttacked = false;
	
	public Faction(){
		attackFactions = new HashSet<Integer>();
	}
	
	public Faction(int id, String name,int color, int defaultPoints){
		this.name = name;
		this.color = color;
		this.defaultPoints = defaultPoints;
		this.id = id;
		attackFactions = new HashSet<Integer>();
	}
	public static String formatName(String name){
		name = name.toLowerCase().trim();
		return name.substring(0, 1).toUpperCase() + name.substring(1);
	}
	public void readNBT(NBTTagCompound compound){
        name = compound.getString("Name");
        color = compound.getInteger("Color");
        id = compound.getInteger("Slot");
        
        neutralPoints= compound.getInteger("NeutralPoints");
        friendlyPoints = compound.getInteger("FriendlyPoints");
        defaultPoints = compound.getInteger("DefaultPoints");

        hideFaction = compound.getBoolean("HideFaction");
        getsAttacked = compound.getBoolean("GetsAttacked");
        
        attackFactions = NBTTags.getIntegerSet(compound.getTagList("AttackFactions", 10));
	}
	public void writeNBT(NBTTagCompound compound){
		compound.setInteger("Slot", id);
		compound.setString("Name", name);
		compound.setInteger("Color", color);
		
		compound.setInteger("NeutralPoints", neutralPoints);
		compound.setInteger("FriendlyPoints", friendlyPoints);
		compound.setInteger("DefaultPoints", defaultPoints);

		compound.setBoolean("HideFaction", hideFaction);
		compound.setBoolean("GetsAttacked", getsAttacked);
		
		compound.setTag("AttackFactions", NBTTags.nbtIntegerSet(attackFactions));
	}

	public boolean isFriendlyToPlayer(EntityPlayer player) {
		PlayerFactionData data = PlayerDataController.instance.getPlayerData(player).factionData;		
		return data.getFactionPoints(id) >= friendlyPoints;
	}

	public boolean isAggressiveToPlayer(EntityPlayer player) {
		PlayerFactionData data = PlayerDataController.instance.getPlayerData(player).factionData;		
		return data.getFactionPoints(id) < neutralPoints;
	}
	
	public boolean isNeutralToPlayer(EntityPlayer player) {
		PlayerFactionData data = PlayerDataController.instance.getPlayerData(player).factionData;
		int points = data.getFactionPoints(id);
		return points >= neutralPoints && points < friendlyPoints;
	}

	public boolean isAggressiveToNpc(EntityNPCInterface entity) {
		return attackFactions.contains(entity.faction.id);
	}
}
