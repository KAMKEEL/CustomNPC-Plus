package foxz.commandhelper;

import net.minecraft.command.ICommandSender;

import java.util.List;

public abstract class CommandHelper {

    public class Helper {

        public String name;
        public String usage;
        public String desc;
        public boolean hasEmptyCall;
    }
    public Helper commandHelper = new Helper();
	public List addTabCompletion(ICommandSender par1, String[] args) {
		return null;
	}
}
