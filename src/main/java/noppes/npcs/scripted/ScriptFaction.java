package noppes.npcs.scripted;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.controllers.Faction;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.interfaces.entity.ICustomNpc;
import noppes.npcs.scripted.interfaces.entity.IPlayer;

public class ScriptFaction {
	private Faction faction;
	
	public ScriptFaction(Faction faction){
		this.faction = faction;
	}
	
	/**
	 * @return Returns the factions id
	 */
	public int getId(){
		return faction.id;
	}
	
	/**
	 * @return Return the factions name
	 */
	public String getName(){
		return faction.name;
	}
	
	public int getDefaultPoints(){
		return faction.defaultPoints;
	}
		
	public int getColor(){
		return faction.color;
	}
	
	public boolean isFriendlyToPlayer(IPlayer player){
		return faction.isFriendlyToPlayer((EntityPlayer) player.getMCEntity());
	}
	
	public boolean isNeutralToPlayer(IPlayer player){
		return faction.isNeutralToPlayer((EntityPlayer) player.getMCEntity());
	}
	
	public boolean isAggressiveToPlayer(IPlayer player){
		return faction.isAggressiveToPlayer((EntityPlayer) player.getMCEntity());
	}
	
	public boolean isAggressiveToNpc(ICustomNpc npc){
		return faction.isAggressiveToNpc((EntityNPCInterface) npc.getMCEntity());
	}
}
