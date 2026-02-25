package kamkeel.npcs.client.command;

import kamkeel.npcs.util.CNPCDebug;
import net.minecraft.command.CommandBase;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

/**
 * Client-only command to toggle debug logging.
 * Registered via ClientCommandHandler so it runs entirely on the client
 * without sending a packet to the server.
 */
public class CommandCNPCDebugger extends CommandBase {

    @Override
    public String getCommandName() {
        return "cnpcdebugger";
    }

    @Override
    public String getCommandUsage(ICommandSender sender) {
        return "/cnpcdebugger <type> - Toggle client debug logging for a type (e.g. energy)";
    }

    @Override
    public int getRequiredPermissionLevel() {
        return 0;
    }

    @Override
    public void processCommand(ICommandSender sender, String[] args) {
        if (args.length == 0) {
            sender.addChatMessage(new ChatComponentText("\u00A7cUsage: /cnpcdebugger <type>"));
            return;
        }

        String type = args[0].toLowerCase();
        boolean newState = CNPCDebug.toggleClient(type);
        String stateText = newState ? "\u00A7aENABLED" : "\u00A7cDISABLED";
        sender.addChatMessage(new ChatComponentText("\u00A77[CNPC+] \u00A7eClient debug '" + type + "' " + stateText));
    }
}
