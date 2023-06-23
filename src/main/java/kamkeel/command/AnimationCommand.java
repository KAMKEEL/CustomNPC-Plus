package kamkeel.command;

import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.QuestController;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.PlayerData;

import java.util.Arrays;
import java.util.List;

public class AnimationCommand extends CommandKamkeelBase {

    public Animation selectedAnimation;
    public List<PlayerData> data;

    @Override
    public String getCommandName() {
        return "animation";
    }

    @Override
    public String getDescription() {
        return "Animation operations";
    }

    @Override
    public String getUsage() {
        return "<player> <subcommand>";
    }

	@Override
	public boolean runSubCommands(){
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String playername = args[0];

        data = PlayerDataController.Instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            throw new CommandException("Unknown player: " + playername);
        }
        
        processSubCommand(sender, args[1], Arrays.copyOfRange(args, 2, args.length));
        data.get(0).animationData.updateClient();
        data.get(0).save();
	}

//    @SubCommand(desc = "enable animation")
//    public void on(ICommandSender sender, String[] args) throws CommandException {
//        data.get(0).animationData.setEnabled(true);
//        sendMessage(sender, String.format("Animations enabled for Player %s", data.get(0).player.getCommandSenderName()));
//
//    }
//
//    @SubCommand(
//            desc = "Set animation",
//            usage = "<animation>"
//    )
//    public void set(ICommandSender sender, String[] args) throws CommandException {
//        if(args.length != 1){
//            throw new CommandException("Please include the animation name:" + "<player> set <animation name>");
//        }
//        String animationName = args[0];
//        selectedAnimation = AnimationController.getInstance().getAnimationFromName(animationName);
//        if (selectedAnimation == null) {
//            throw new CommandException("Unknown animation: " + animationName);
//        }
//
//        data.get(0).animationData.setAnimation(selectedAnimation);
//        sendMessage(sender, String.format("Animation %s set for Player %s", selectedAnimation.getName(), data.get(0).player.getCommandSenderName()));
//    }
//
//    @SubCommand(desc = "disable animation")
//    public void off(ICommandSender sender, String[] args) throws CommandException {
//        data.get(0).animationData.setEnabled(false);
//        sendMessage(sender, String.format("Animations disabled for Player %s", data.get(0).player.getCommandSenderName()));
//    }
//
//    @SubCommand(desc = "play animation")
//    public void clear(ICommandSender sender, String[] args) throws CommandException {
//        data.get(0).animationData.setEnabled(false);
//        data.get(0).animationData.setAnimation(null);
//        sendMessage(sender, String.format("Animations cleared for Player %s", data.get(0).player.getCommandSenderName()));
//
//    }

    @SubCommand(
            desc="reload animations from disk",
            permission = 4
    )
    public void reload(ICommandSender sender, String args[]){
        new AnimationController();
        AnimationController.Instance.load();
        sendResult(sender, "Animations Reloaded");
    }
}
