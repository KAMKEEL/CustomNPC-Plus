package noppes.npcs.scripted;

import java.util.ArrayList;
import java.util.List;

import net.minecraft.scoreboard.ScorePlayerTeam;
import net.minecraft.scoreboard.Scoreboard;
import net.minecraft.util.EnumChatFormatting;

public class ScriptScoreboardTeam {
	private ScorePlayerTeam team;
	private Scoreboard board;
	protected ScriptScoreboardTeam(ScorePlayerTeam team, Scoreboard board){
		this.team = team;
		this.board = board;
	}
	
	public String getName(){
		return team.getRegisteredName();
	}
	
	public String getDisplayName(){
		return team.func_96669_c();
	}
	
	/**
	 * @param name Name used as display (1-32 chars)
	 */
	public void setDisplayName(String name){
		if(name.length() > 0 && name.length() <= 32)
			team.setTeamName(name);
	}
	
	public void addPlayer(String player){
		board.func_151392_a(player, getName());
	}
	
	public void removePlayer(String player){
		board.removePlayerFromTeam(player, team);
	}
	
	public String[] getPlayers(){
		List<String> list = new ArrayList<String>(team.getMembershipCollection());
		return list.toArray(new String[list.size()]);
	}
	
	public void clearPlayers(){
        List<String> list = new ArrayList<String>(team.getMembershipCollection());
        for(String player : list){
        	board.removePlayerFromTeam(player, team);
        }
	}
	
	public boolean getFriendlyFire(){
		return team.getAllowFriendlyFire();
	}
	
	public void setFriendlyFire(boolean bo){
		team.setAllowFriendlyFire(bo);
	}
	
	/**
	 * @param color Valid color values are "black", "dark_blue", "dark_green", "dark_aqua", "dark_red", "dark_purple", "gold", "gray", "dark_gray", "blue", "green", "aqua", "red", "light_purple", "yellow", and "white". Or "reset" if you want default 
	 */
	public void setColor(String color){
        EnumChatFormatting enumchatformatting = EnumChatFormatting.getValueByName(color);

        if (enumchatformatting == null || enumchatformatting.isFancyStyling())
        	return;

        team.setNamePrefix(enumchatformatting.toString());
        team.setNameSuffix(EnumChatFormatting.RESET.toString());
	}
	
	/**
	 * @return Returns color string. Returns null if no color was set
	 */
	public String getColor(){
		String prefix = team.getColorPrefix();
		if(prefix == null || prefix.isEmpty())
			return null;
		for(EnumChatFormatting format : EnumChatFormatting.values()){
			if(prefix.equals(format.toString()) && format != EnumChatFormatting.RESET)
				return format.getFriendlyName();
		}
		return null;
	}
	
	public void setSeeInvisibleTeamPlayers(boolean bo){
		team.setSeeFriendlyInvisiblesEnabled(bo);
	}
	
	public boolean getSeeInvisibleTeamPlayers(){
		return team.func_98297_h();
	}
}
