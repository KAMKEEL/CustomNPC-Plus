package kamkeel;

import java.util.Arrays;
import java.util.List;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;

public class CmdFaction extends CommandKamkeelBase {

    public Faction selectedFaction;
    public List<PlayerData> data;


	@Override
	public String getCommandName() {
		return "faction";
	}

	@Override
	public String getDescription() {
		return "Faction operations";
	}

	@Override
	public String getUsage() {
		return "<player> <faction> <command>";
	}

	@Override
	public boolean runSubCommands(){
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String playername = args[0];
        String factionname = args[1];

        data = PlayerDataController.instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            throw new CommandException("Unknown player: " + playername);
        } 
        
        try{
            selectedFaction = FactionController.getInstance().getFaction(Integer.parseInt(factionname));
        }
        catch(NumberFormatException e){
            selectedFaction = FactionController.getInstance().getFactionFromName(factionname);
        }
        
        if (selectedFaction == null) {
            throw new CommandException("Unknown faction: " + factionname);
        }
        
        processSubCommand(sender, args[2], Arrays.copyOfRange(args, 3, args.length));
        
        for(PlayerData playerdata : data){
        	playerdata.saveNBTData(null);
        }
	}

    @SubCommand(
            desc = "Add points",
            usage = "<points>"
    )
    public void add(ICommandSender sender, String[] args) throws CommandException {
        int points;
        try {
            points = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
        	throw new CommandException("Must be an integer: " + args[0]);
        }
        int factionid = this.selectedFaction.id;

        for(PlayerData playerdata : data){
	        PlayerFactionData playerfactiondata = playerdata.factionData;
	        playerfactiondata.increasePoints(factionid, points, playerdata.player);
        }
    }

    @SubCommand(
            desc = "Substract points",
            usage = "<points>"
    )
    public void subtract(ICommandSender sender, String[] args) throws CommandException {
        int points;
        try {
            points = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
        	throw new CommandException("Must be an integer: " + args[0]);
        }
        int factionid = this.selectedFaction.id;
        for(PlayerData playerdata : data){
        	PlayerFactionData playerfactiondata = playerdata.factionData;
        	playerfactiondata.increasePoints(factionid, -points, playerdata.player);
        }
    }

    @SubCommand(desc = "Reset points to default")
    public void reset(ICommandSender sender, String[] args) {
        for(PlayerData playerdata : data){
        	playerdata.factionData.factionData.put(this.selectedFaction.id, this.selectedFaction.defaultPoints);
        }
    }

    @SubCommand(
            desc = "Set points",
            usage = "<points>"
    )
    public void set(ICommandSender sender, String[] args) throws CommandException {
        int points;
        try {
            points = Integer.parseInt(args[0]);
        } catch (NumberFormatException ex) {
        	throw new CommandException("Must be an integer: " + args[0]);
        }
        for(PlayerData playerdata : data){
        	PlayerFactionData playerfactiondata = playerdata.factionData;
        	playerfactiondata.factionData.put(this.selectedFaction.id,points);
        }
    }
    
    @SubCommand(desc = "Drop relationship")
    public void drop(ICommandSender sender, String[] args){
        for(PlayerData playerdata : data){
        	playerdata.factionData.factionData.remove(this.selectedFaction.id);
        }
    }
    
    @Override
	public List addTabCompletionOptions(ICommandSender par1, String[] args) {
		if(args.length == 3){
			return CommandBase.getListOfStringsMatchingLastWord(args, new String[]{"add", "subtract", "set", "reset", "drop", "create"});
		}
    	return null;
    }
}
