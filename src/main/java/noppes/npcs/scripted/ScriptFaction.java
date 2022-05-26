package noppes.npcs.scripted;

import noppes.npcs.controllers.Faction;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.scripted.entity.ScriptNpc;
import noppes.npcs.scripted.entity.ScriptPlayer;

public class ScriptFaction {
	
	private Faction faction;

	public void saveFaction() {
		FactionController.getInstance().saveFaction(faction);
	}
	
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
	
	public void setName(String name) {
		faction.name = name;
	}
	
	public int getDefaultPoints(){
		return faction.defaultPoints;
	}
	
	public void setDefaultPoints(int p) {
		faction.defaultPoints = p;
	}
	
	public int getFriendlyPoints() {
		return faction.friendlyPoints;
	}
	
	public void setFriendlyPoints(int p) {
		faction.friendlyPoints = p;
	}
	
	public int getNeutralPoints() {
		return faction.neutralPoints;
	}
	
	public void setNeutralPoints(int p) {
		faction.neutralPoints = p;
	}
	
	public boolean getHideFaction() {
		return faction.hideFaction;
	}
	
	public void setHideFaction(boolean b) {
		faction.hideFaction = b;
	}
	
	public boolean getGetsAttacked() {
		return faction.getsAttacked;
	}
	
	public void setGetsAttacked(boolean b) {
		faction.getsAttacked = b;
	}
		
	public int getColor(){
		return faction.color;
	}
	
	public void setColor(int c) {
		faction.color = c;
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
	
	public Integer[] getFactionEnemies() {
		if (faction.attackFactions == null) return new Integer[0];
		return faction.attackFactions.toArray(new Integer[faction.attackFactions.size()]);
	}
	
	public boolean isEnemyFaction(int id) {
		if (faction.attackFactions == null) return false;
		Integer[] enemies = getFactionEnemies();
		for (int i = 0; i < enemies.length; ++i) {
			if (enemies[i] == id) return true;
		}
		return false;
	}
	
	public void addFactionEnemy(int id) {
		if (!faction.attackFactions.contains(id)) faction.attackFactions.add(id);
	}
	
	public void removeFactionEnemy(int id) {
		faction.attackFactions.remove(id);
	}
}
