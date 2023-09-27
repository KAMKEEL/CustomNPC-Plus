package noppes.npcs.scripted.scoreboard;

import net.minecraft.scoreboard.*;
import net.minecraft.server.MinecraftServer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.scoreboard.IScoreboard;
import noppes.npcs.api.scoreboard.IScoreboardObjective;
import noppes.npcs.api.scoreboard.IScoreboardTeam;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Karel
 */
public class ScriptScoreboard implements IScoreboard {
	private Scoreboard board;

	public ScriptScoreboard(){
		board = MinecraftServer.getServer().worldServerForDimension(0).getScoreboard();
	}
	
	/**
	 * @return Returns an array with all ScoreboardObjectives
	 */
	public IScoreboardObjective[] getObjectives(){
        List<ScoreObjective> collection = new ArrayList<ScoreObjective>(board.getScoreObjectives());
		IScoreboardObjective[] objectives = new IScoreboardObjective[collection.size()];
        for(int i = 0; i < collection.size(); i++){
        	objectives[i] = new ScriptScoreboardObjective(collection.get(i));
        }
        return objectives;
	}
	
	/**
	 * @param name Name of the objective
	 * @return Returns the ScoreboardObjective
	 */
	public IScoreboardObjective getObjective(String name){
		ScoreObjective obj = board.getObjective(name);
		if(obj == null)
			return null;
		return new ScriptScoreboardObjective(obj);
	}
	
	/**
	 * @param objective Objective to check
	 * @return Returns whether or not the objective exists
	 */
	public boolean hasObjective(String objective){
        return board.getObjective(objective) != null;
	}
	
	/**
	 * @param objective Objective to remove
	 */
	public void removeObjective(String objective){
		ScoreObjective obj = board.getObjective(objective); 
		if(obj != null)
			board.func_96519_k(obj);
	}
	
	/**
	 * @version 1.8
	 * @param objective Scoreboard objective name (1-16 chars)
	 * @param criteria The criteria see http://minecraft.wiki/w/Scoreboard#Objectives
	 * @return Returns the created ScoreboardObjective, returns null if it failed to create
	 */
	public IScoreboardObjective addObjective(String objective, String criteria){
		if(hasObjective(objective))
			return null;
        
		IScoreObjectiveCriteria icriteria = (IScoreObjectiveCriteria)IScoreObjectiveCriteria.field_96643_a.get(criteria);
        if(icriteria == null)
        	return null;
        
        if(objective.length() == 0 || objective.length() > 16)
        	return null;
        
        ScoreObjective obj = board.addScoreObjective(objective, icriteria);
        return new ScriptScoreboardObjective(obj);
	}

	/**
	 * @since 1.7.10c
	 * @param player Name of a player or non existing thing
	 * @param objective Scoreboard objective name
	 * @param score Score as integer between -2,147,483,648 and 2,147,483,647
	 * @param datatag Optional tag to specify type, can be left empty
	 */
	public void setPlayerScore(String player, String objective, int score, String datatag){
		ScoreObjective objec = board.getObjective(objective);
		if(objec == null || objec.getCriteria().isReadOnly() || 
				score < Integer.MIN_VALUE || score > Integer.MAX_VALUE)
			return;

        Score sco = board.func_96529_a(player, objec);
        sco.setScorePoints(score);
	}

	public void setPlayerScore(IPlayer player, String objective, int score, String datatag){
		this.setPlayerScore(player.getName(),objective,score,datatag);
	}

	/**
	 * @since 1.7.10c
	 * @param player Name of a player or non existing thing
	 * @param objective Scoreboard objective name
	 * @param datatag Optional tag to specify type, can be left empty
	 * @return Returns score value
	 */
	public int getPlayerScore(String player, String objective, String datatag){
		ScoreObjective objec = board.getObjective(objective);
		if(objec == null || objec.getCriteria().isReadOnly())
			return 0;

        return board.func_96529_a(player, objec).getScorePoints(); 
	}

	public int getPlayerScore(IPlayer player, String objective, String datatag){
		return this.getPlayerScore(player.getName(),objective,datatag);
	}

	/**
	 * @version 1.8
	 * @param player Name of a player or non existing thing
	 * @param objective Scoreboard objective name
	 * @param datatag Optional tag to specify type, can be left empty
	 * @return Returns whether or not the player has the objective
	 */
	public boolean hasPlayerObjective(String player, String objective, String datatag){
		ScoreObjective objec = board.getObjective(objective);
		if(objec == null)
			return false;
		
		return board.func_96510_d(player).get(objec) != null;
	}

	public boolean hasPlayerObjective(IPlayer player, String objective, String datatag){
		return this.hasPlayerObjective(player.getName(),objective,datatag);
	}
	
	/**
	 * @version 1.8
	 * @param player Name of a player or non existing thing
	 * @param objective Scoreboard objective name
	 * @param datatag Optional tag to specify type, can be left empty
	 */
	public void deletePlayerScore(String player, String objective, String datatag){
		ScoreObjective objec = board.getObjective(objective);
		if(objec == null)
			return;

        if(board.func_96510_d(player).remove(objec) != null)
        	board.func_96516_a(player);
	}

	public void deletePlayerScore(IPlayer player, String objective, String datatag){
		this.deletePlayerScore(player.getName(),objective,datatag);
	}
	
	public IScoreboardTeam[] getTeams(){
		List<ScorePlayerTeam> list = new ArrayList<ScorePlayerTeam>(board.getTeams());
		IScoreboardTeam[] teams = new IScoreboardTeam[list.size()];
		for(int i = 0; i < list.size(); i++){
			teams[i] = new ScriptScoreboardTeam(list.get(i), board);
		}
		return teams;
	}
	
	public IScoreboardTeam getTeamByName(String name) {
		IScoreboardTeam[] teams = getTeams();
		for (int i = 0; i < teams.length; i++) {
			if (teams[i].getName().equals(name)) {
				return teams[i];
			}
		}
		return null;
	}
	
	public boolean hasTeam(String name){
		return board.getPlayersTeam(name) != null;
	}
	
	public IScoreboardTeam addTeam(String name){
		if(hasTeam(name))
			return null;
		return new ScriptScoreboardTeam(board.createTeam(name), board);
	}
	
	public IScoreboardTeam getTeam(String name){
		ScorePlayerTeam team = board.getPlayersTeam(name);
		if(team == null)
			return null;
		return new ScriptScoreboardTeam(team, board);
	}
	
	public void removeTeam(String name){
		ScorePlayerTeam team = board.getPlayersTeam(name);
		if(team != null)
			board.removeTeam(team);
	}
}
