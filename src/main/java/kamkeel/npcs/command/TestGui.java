package kamkeel.npcs.command;

import kamkeel.npcs.controllers.data.ability.Ability;
import net.minecraft.client.Minecraft;
import net.minecraft.command.CommandException;
import net.minecraft.command.ICommandSender;
import somehussar.gui.AnnotatedGui;

public class TestGui extends CommandKamkeelBase {

    @Override
    public void processCommand(ICommandSender sender, String[] args) throws CommandException {
        Minecraft.getMinecraft().displayGuiScreen(new AnnotatedGui(Ability.class));
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public String getCommandName() {
        return "mytestgui";
    }
}
