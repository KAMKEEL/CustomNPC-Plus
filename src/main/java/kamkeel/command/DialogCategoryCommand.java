package kamkeel.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Dialog;
import noppes.npcs.controllers.data.DialogCategory;
import noppes.npcs.controllers.data.PlayerData;

import java.util.Collection;
import java.util.List;

public class DialogCategoryCommand extends CommandKamkeelBase {

	@Override
	public String getCommandName() {
		return "dialogcat";
	}

	@Override
	public String getDescription() {
		return "Dialog Category operations";
	}

    @SubCommand(
            desc = "Find dialog category id number by its name",
            usage = "<dialog cat name>"
    )
    public void id(ICommandSender sender, String args[]) throws CommandException {
        if(args.length == 0){
            sendError(sender, "Please provide a name for the dialog category");
            return;
        }

        String catName = String.join(" ", args).toLowerCase();
        final Collection<DialogCategory> dialogCats = DialogController.Instance.categories.values();
        int count = 0;
        for(DialogCategory cat : dialogCats){
            if(cat.getName().toLowerCase().contains(catName)){
                sendResult(sender, String.format("Dialog Cat \u00A7e%d\u00A77 - \u00A7c'%s'", cat.id, cat.getName()));
                count++;
            }
        }
        if(count == 0){
            sendResult(sender, String.format("No Dialog Cat found with name: \u00A7c'%s'", catName));
        }
    }


    @SubCommand(
            desc = "Read a dialog category for a player",
            usage = "<player> <dialogcatid>"
    )
    public void read(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int dialogCatId;
        try {
        	dialogCatId = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
        	sendError(sender, "DialogCatID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
        	sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }

        DialogCategory dialogCategory = DialogController.Instance.categories.get(dialogCatId);
        if (dialogCategory == null){
        	sendError(sender, "Unknown DialogCatID: " + dialogCatId);
            return;
        }

        int count = 0;
        for(PlayerData playerdata : data){
            for(Dialog dialog : dialogCategory.dialogs.values()){
                playerdata.dialogData.dialogsRead.add(dialog.id);
                count++;
            }

            playerdata.save();
            playerdata.updateClient = true;
            sendResult(sender, String.format("Read Dialog Cat \u00A7c'%s' \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", dialogCategory.getName(), dialogCatId, playerdata.playername));
            sendResult(sender, String.format("Read a total of \u00A7b%d \u00A77dialogs", count));
        }
    }


    @SubCommand(
            desc = "Unread a dialog category for a player",
            usage = "<player> <dialogcatid>"
    )
    public void unread(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int dialogCatId;
        try {
            dialogCatId = Integer.parseInt(args[1]);
        }
        catch (NumberFormatException ex) {
            sendError(sender, "DialogCatID must be an integer: " + args[1]);
            return;
        }

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, String.format("Unknown player '%s'", playername));
            return;
        }

        DialogCategory dialogCategory = DialogController.Instance.categories.get(dialogCatId);
        if (dialogCategory == null){
            sendError(sender, "Unknown DialogCatID: " + dialogCatId);
            return;
        }

        int count = 0;
        for(PlayerData playerdata : data){
            for(Dialog dialog : dialogCategory.dialogs.values()){
                playerdata.dialogData.dialogsRead.remove(dialog.id);
                count++;
            }

            playerdata.save();
            playerdata.updateClient = true;
            sendResult(sender, String.format("Unread Dialog Cat \u00A7c'%s' \u00A7e%d\u00A77 for Player '\u00A7b%s\u00A77'", dialogCategory.getName(), dialogCatId, playerdata.playername));
            sendResult(sender, String.format("Unread a total of \u00A7b%d \u00A77dialogs", count));
        }
    }
}

