package foxz.commandhelper;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;

public class ChMcLogger extends AbstractCommandHelper {

    public ChMcLogger(Object sender) {
        super(sender);
    }

    public void sendmessage(String msg) {
        ICommandSender sender = (ICommandSender) pcParam;
        sender.addChatMessage(new ChatComponentText(msg));
    }

    @Override
    public void help(String cmd, String desc, String usa) {
    	if(usa.isEmpty())
    		sendmessage(String.format("%s = %s", cmd, desc));
    	else
    		sendmessage(String.format("%s %s = %s", cmd, usa, desc));
    }

    @Override
    public void cmdError(String cmd) {
        sendmessage(String.format("Unknow command '%s'", cmd));
    }

    @Override
    public void error(String err) {
        sendmessage(String.format("Error: %s", err));
    }

}
