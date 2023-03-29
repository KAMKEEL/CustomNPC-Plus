package kamkeel;

import net.minecraft.command.CommandBase;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import net.minecraft.entity.monster.EntityEnderman;
import noppes.npcs.controllers.AnimationController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.Animation;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.controllers.data.PlayerFactionData;

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
        return "<player> <animation>";
    }

	@Override
	public boolean runSubCommands(){
		return false;
	}

	@Override
	public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        String playername = args[0];
        String animationName = args[1];

        data = PlayerDataController.instance.getPlayersData(sender, playername);
        if (data.isEmpty()) {
            throw new CommandException("Unknown player: " + playername);
        }

        selectedAnimation = AnimationController.getInstance().getAnimationFromName(animationName);
        
        if (selectedAnimation == null) {
            throw new CommandException("Unknown animation: " + animationName);
        }
        
        processSubCommand(sender, args[2], Arrays.copyOfRange(args, 3, args.length));
	}

    @SubCommand(
            desc = "play animation",
            usage = "<player> <animation> play"
    )
    public void play(ICommandSender sender, String[] args) throws CommandException {

    }
}
