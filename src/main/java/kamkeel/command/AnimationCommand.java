package kamkeel.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.api.ISkinOverlay;
import noppes.npcs.api.handler.data.IAnimation;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.SkinOverlay;

import java.util.List;

public class AnimationCommand extends CommandKamkeelBase {

	@Override
	public String getCommandName() {
		return "animation";
	}

	@Override
	public String getDescription() {
		return "Animation operations";
	}
    
    @SubCommand(
            desc = "set an animation to a player",
            usage = "<player> <num>"
    )
    public void set(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        int animationId;
        try {
            animationId = Integer.parseInt(args[1]);
        } catch (NumberFormatException ex) {
            sendError(sender, "Animation num must be an integer: " + args[1]);
            return;
        }
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        IAnimation animation = AnimationController.getInstance().get(animationId);
        if(animation == null){
            sendError(sender, "No Animation ID found: " + animationId);
            return;
        }

        for(PlayerData playerdata : data){
            playerdata.animationData.setEnabled(false);
            playerdata.animationData.setAnimation(animation);
            playerdata.animationData.updateClient();
            playerdata.save();
            sendResult(sender, String.format("Animation set to Player '\u00A7b%s\u00A77' on ID \u00A7d%d", playerdata.playername, animationId));
            return;
        }
    }

    @SubCommand(
            desc = "clear an animation from a player",
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
            playerdata.animationData.setEnabled(false);
            playerdata.animationData.setAnimation(null);
            playerdata.animationData.updateClient();
            playerdata.save();
            sendResult(sender, String.format("Animation cleared from Player '\u00A7b%s\u00A77'", playerdata.playername));
            return;
        }
    }

    @SubCommand(
            desc = "enable an animation on a player",
            usage = "<player>"
    )
    public void enable(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for(PlayerData playerdata : data){
            if(playerdata.animationData.getAnimation() == null){
                sendError(sender, String.format("Player '\u00A7b%s\u00A74' does not have an animation set", playerdata.playername));
                return;
            }

            playerdata.animationData.setEnabled(true);
            playerdata.animationData.updateClient();
            playerdata.save();
            sendResult(sender, String.format("Animation Enabled for Player '\u00A7b%s\u00A77'", playerdata.playername));
            return;
        }
    }

    @SubCommand(
            desc = "disable an animation on a player",
            usage = "<player>"
    )
    public void disable(ICommandSender sender, String args[]) throws CommandException{
        String playername=args[0];
        List<PlayerData> data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            sendError(sender, "Unknown player: " + playername);
            return;
        }

        for(PlayerData playerdata : data){
            if(playerdata.animationData.getAnimation() == null){
                sendError(sender, String.format("Player '\u00A7b%s\u00A74' does not have an animation set", playerdata.playername));
                return;
            }

            playerdata.animationData.setEnabled(false);
            playerdata.animationData.updateClient();
            playerdata.save();
            sendResult(sender, String.format("Animation Disabled for Player '\u00A7b%s\u00A77'", playerdata.playername));
            return;
        }
    }

    @SubCommand(
            desc = "reload animations"
    )
    public void reload(ICommandSender sender, String args[]) {
        AnimationController.Instance.load();
        sendResult(sender, "Animations Reloaded");
    }

}
