package noppes.npcs.scripted;

import noppes.npcs.controllers.Faction;

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
	
	public boolean isFriendlyToPlayer(ScriptPlayer player){
		return faction.isFriendlyToPlayer(player.player);
	}
	
	public boolean isNeutralToPlayer(ScriptPlayer player){
		return faction.isNeutralToPlayer(player.player);
	}
	
	public boolean isAggressiveToPlayer(ScriptPlayer player){
		return faction.isAggressiveToPlayer(player.player);
	}
	
	public boolean isAggressiveToNpc(ScriptNpc npc){
		return faction.isAggressiveToNpc(npc.npc);
	}
}
