package noppes.npcs.scripted;

import net.minecraft.scoreboard.ScoreObjective;

public class ScriptScoreboardObjective {
	private ScoreObjective objective;
	
	protected ScriptScoreboardObjective(ScoreObjective objective){
		this.objective = objective;
	}
	
	/**
	 * @return Returns objective name
	 */
	public String getName(){
		return objective.getName();
	}
	
	/**
	 * @return Returns display name
	 */
	public String getDisplayName(){
		return objective.getDisplayName();
	}
	
	/**
	 * @since 1.7.10c
	 * @param name Name used for display (1-32 chars)
	 */
	public void setDisplayName(String name){
		if(name.length() > 0 && name.length() <= 32)
			objective.setDisplayName(name);
	}
	
	/**
	 * @since 1.7.10c
	 * @return Returns the criteria string
	 */
	public String getCriteria(){
		return objective.getCriteria().func_96636_a();
	}
	
	/**
	 * @return Return whether or not the objective value can be changed. E.g. player health can't be changed
	 */
	public boolean isReadyOnly(){
		return objective.getCriteria().isReadOnly();
	}
	
}
