package foxz.command;

import java.util.List;

import noppes.npcs.controllers.Faction;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.controllers.PlayerFactionData;
import foxz.commandhelper.ChMcLogger;
import foxz.commandhelper.annotations.Command;
import foxz.commandhelper.annotations.SubCommand;
import foxz.commandhelper.permissions.OpOnly;
import foxz.commandhelper.permissions.ParamCheck;

@Command(
        name = "faction",
        desc = "operations about relationship between player and faction"
)
public class CmdFaction extends ChMcLogger {

    public String playername;

    public Faction selectedFaction;
    public List<PlayerData> data;

    public CmdFaction(Object sender) {
        super(sender);
    }

    @SubCommand(
            desc = "Add points",
            usage = "<points>",
            permissions={OpOnly.class, ParamCheck.class}
    )
    public Boolean add(String[] args) {
        int points;
        try {
            points = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sendmessage("Must be an integer");
            return false;
        }
        int factionid = this.selectedFaction.id;

        for(PlayerData playerdata : data){
	        PlayerFactionData playerfactiondata = playerdata.factionData;
	        playerfactiondata.increasePoints(factionid, points);
        }
        return true;
    }

    @SubCommand(
            desc = "Substract points",
            usage = "<points>",
            permissions={OpOnly.class, ParamCheck.class}
    )
    public Boolean subtract(String[] args) {
        int points;
        try {
            points = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sendmessage("Must be an integer");
            return false;
        }
        int factionid = this.selectedFaction.id;
        for(PlayerData playerdata : data){
        	PlayerFactionData playerfactiondata = playerdata.factionData;
        	playerfactiondata.increasePoints(factionid, -points);
        }
        return true;
    }

    @SubCommand(
            desc = "Reset points to default",
            usage = "",
            permissions={OpOnly.class}
    )
    public Boolean reset(String[] args) {
        for(PlayerData playerdata : data){
        	playerdata.factionData.factionData.put(this.selectedFaction.id, this.selectedFaction.defaultPoints);
        }
        return true;
    }

    @SubCommand(
            desc = "Set points",
            usage = "<points>",
            permissions={OpOnly.class, ParamCheck.class}
    )
    public Boolean set(String[] args) {
        int points;
        try {
            points = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
            sendmessage("Must be an integer");
            return false;
        }
        for(PlayerData playerdata : data){
        	PlayerFactionData playerfactiondata = playerdata.factionData;
        	playerfactiondata.factionData.put(this.selectedFaction.id,points);      
        }
        return true;
    }
    
    @SubCommand(
            desc="Drop relationship",
            usage="",
            permissions={OpOnly.class}
    )
    public Boolean drop(String[] args){
        for(PlayerData playerdata : data){
        	playerdata.factionData.factionData.remove(this.selectedFaction.id);
        }
        return true;
    }
}
