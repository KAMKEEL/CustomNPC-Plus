package kamkeel.command;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.NoppesUtilServer;
import noppes.npcs.api.ISkinOverlay;
import noppes.npcs.client.EntityUtil;
import noppes.npcs.constants.EnumAvailabilityDialog;
import noppes.npcs.constants.EnumAvailabilityQuest;
import noppes.npcs.controllers.DialogController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityDialogNpc;

import java.util.Collection;
import java.util.List;

public class OverlayCommand extends CommandKamkeelBase {

	@Override
	public String getCommandName() {
		return "overlay";
	}

	@Override
	public String getDescription() {
		return "Overlay operations";
	}
    
    @SubCommand(
            desc = "set an overlay to a player",
            usage = "<player> <num> <texture>"
    )
    public void set(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int overlayID;
        try {
            overlayID = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Overlay num must be an integer: " + args[1]);
            return;
        }
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }
        for(PlayerData playerdata : data){
            SkinOverlay skinOverlay = new SkinOverlay(args[2]);
            playerdata.skinOverlays.add(overlayID, skinOverlay);
            playerdata.save();
            sendResult(sender, String.format("Overlay added to Player '\u00A7b%s\u00A77' on ID \u00A7d%d", playerdata.playername, overlayID));
            return;
        }
    }

    @SubCommand(
            desc = "remove an overlay from a player",
            usage = "<player> <num>"
    )
    public void remove(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int overlayID;
        try {
            overlayID = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Overlay num must be an integer: " + args[1]);
            return;
        }
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }
        for(PlayerData playerdata : data){
            if(playerdata.skinOverlays.has(overlayID)){
                playerdata.skinOverlays.remove(overlayID);
                playerdata.save();
                sendResult(sender, String.format("Overlay removed to Player '\u00A7b%s\u00A77' on ID \u00A7d%d", playerdata.playername, overlayID));
            }
            else {
                sendError(sender, String.format("No overlay found for Player '\u00A7b%s\u00A74' on ID \u00A7d%d", playerdata.playername, overlayID));
            }
            return;
        }
    }

    @SubCommand(
            desc = "modify an overlay for a player",
            usage = "<player> <num> <blend/glow> <true/false>"
    )
    public void modify(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int overlayID;
        try {
            overlayID = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Overlay num must be an integer: " + args[1]);
            return;
        }
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        if(!args[2].equalsIgnoreCase("blend") && !args[2].equalsIgnoreCase("glow")){
            sendError(sender, "Unknown Entry: " + args[2]);
            return;
        }
        boolean isBlend = args[2].equalsIgnoreCase("blend");

        if(!args[3].equalsIgnoreCase("true") && !args[3].equalsIgnoreCase("false")){
            sendError(sender, "Unknown Bool: " + args[3]);
            return;
        }

        boolean isTrue = args[3].equalsIgnoreCase("true");

        for(PlayerData playerdata : data){
            if(!playerdata.skinOverlays.has(overlayID)){
                sendError(sender, String.format("Player '\u00A7b%s\u00A7c' does not have Overlay ID \u00A7d%d", playerdata.playername, overlayID));
                return;
            }

            ISkinOverlay skinOverlay = playerdata.skinOverlays.get(overlayID);
            if(skinOverlay == null){
                sendError(sender, String.format("Player '\u00A7b%s\u00A7c' does not have Overlay ID \u00A7d%d", playerdata.playername, overlayID));
                return;
            }

            if(isBlend){
                skinOverlay.setBlend(isTrue);
            }
            else{
                skinOverlay.setGlow(isTrue);
            }
            playerdata.skinOverlays.add(overlayID, skinOverlay);
            playerdata.save();
            sendResult(sender, String.format("Overlay ID \u00A7d%d \u00A77Player '\u00A7b%s\u00A77' Updated ", overlayID, playerdata.playername));
            return;
        }
    }

    @SubCommand(
            desc = "clears all overlays from a player",
            usage = "<player>"
    )
    public void clear(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }
        for(PlayerData playerdata : data){
            playerdata.skinOverlays.clear();
            playerdata.save();
            sendResult(sender, String.format("Overlays cleared from Player '\u00A7b%s\u00A77'", playerdata.playername));
            return;
        }
    }

    @SubCommand(
            desc = "List all overlays on a player",
            usage = "<player>"
    )
    public void info(ICommandSender sender, String args[]) throws CommandException {
        String playername=args[0];

        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }
        for(PlayerData playerdata : data){
            sendResult(sender, "--------------------");
            if(playerdata.skinOverlays.size() == 0){
                sendResult(sender, String.format("No Overlays found for Player '\u00A7b%s\u00A77'", playerdata.playername));
            }
            else {
                for(ISkinOverlay overlay : playerdata.skinOverlays.overlayList.values()){
                    if(overlay != null){
                        sendResult(sender, String.format("%s", overlay.getTexture()));
                    }
                }
            }
            sendResult(sender, "--------------------");
            return;
        }
    }
}
