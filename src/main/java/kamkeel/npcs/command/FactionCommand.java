package kamkeel.npcs.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.FactionController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Faction;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;

import java.util.Arrays;
import java.util.List;

public class FactionCommand extends CommandKamkeelBase {

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

        data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        try{
            selectedFaction = FactionController.getInstance().getFaction(Integer.parseInt(factionname));
        }
        catch(NumberFormatException e){
            selectedFaction = FactionController.getInstance().getFactionFromName(factionname);
        }

        if (selectedFaction == null) {
            sendError(sender, "Unknown faction: " + factionname);
            return;
        }

        processSubCommand(sender, args[2], Arrays.copyOfRange(args, 3, args.length));

        for(PlayerData playerdata : data){
            playerdata.save();
            playerdata.updateClient = true;
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
            sendError(sender, "Must be an integer: " + args[0]);
            return;
        }
        int factionid = this.selectedFaction.id;

        for(PlayerData playerdata : data){
	        PlayerFactionData playerfactiondata = playerdata.factionData;
	        playerfactiondata.increasePoints(factionid, points, playerdata.player);
            playerdata.updateClient = true;
            sendResult(sender, String.format("Added Points \u00A7a%d\u00A77, Faction \u00A7e%s (%d)\u00A77 for Player \u00A7b%s\u00A77", points, this.selectedFaction.getName(), this.selectedFaction.id, playerdata.playername));
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
            sendError(sender, "Must be an integer: " + args[0]);
            return;
        }
        int factionid = this.selectedFaction.id;
        for(PlayerData playerdata : data){
        	PlayerFactionData playerfactiondata = playerdata.factionData;
        	playerfactiondata.increasePoints(factionid, -points, playerdata.player);
            playerdata.updateClient = true;
            sendResult(sender, String.format("Subtracted Points \u00A7a%d\u00A77, Faction \u00A7e%s (%d)\u00A77 for Player \u00A7b%s\u00A77", points, this.selectedFaction.getName(), this.selectedFaction.id, playerdata.playername));
        }
    }

    @SubCommand(desc = "Reset points to default")
    public void reset(ICommandSender sender, String[] args) {
        for(PlayerData playerdata : data){
        	playerdata.factionData.factionData.put(this.selectedFaction.id, this.selectedFaction.defaultPoints);
            playerdata.updateClient = true;
            sendResult(sender, String.format("Reset Faction \u00A7e%s (%d)\u00A77 for Player \u00A7b%s\u00A77", this.selectedFaction.getName(), this.selectedFaction.id, playerdata.playername));
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
            sendError(sender, "Must be an integer: " + args[0]);
            return;
        }
        for(PlayerData playerdata : data){
        	PlayerFactionData playerfactiondata = playerdata.factionData;
        	playerfactiondata.factionData.put(this.selectedFaction.id, points);
            playerdata.updateClient = true;
            sendResult(sender, String.format("Set Points \u00A7a%d\u00A77, Faction \u00A7e%s (%d)\u00A77 for Player \u00A7b%s\u00A77", points, this.selectedFaction.getName(), this.selectedFaction.id, playerdata.playername));
        }
    }

    @SubCommand(desc = "Drop relationship")
    public void drop(ICommandSender sender, String[] args){
        for(PlayerData playerdata : data){
        	playerdata.factionData.factionData.remove(this.selectedFaction.id);
            playerdata.updateClient = true;
            sendResult(sender, String.format("Dropped Faction \u00A7e%s (%d)\u00A77 from Player \u00A7b%s\u00A77", this.selectedFaction.getName(), this.selectedFaction.id, playerdata.playername));
        }
    }

    @Override
	public List addTabCompletionOptions(ICommandSender par1, String[] args) {
		if(args.length == 3){
			return getListOfStringsMatchingLastWord(args, new String[]{"add", "subtract", "set", "reset", "drop", "create"});
		}
    	return null;
    }
}
