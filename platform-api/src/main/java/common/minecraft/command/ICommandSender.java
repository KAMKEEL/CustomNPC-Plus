package common.minecraft.command;

import common.minecraft.util.ITextComponent;
import noppes.npcs.api.IWorld;

/**
 * Platform abstraction for Minecraft's command sender.
 * MC 1.7.10: net.minecraft.command.ICommandSender
 * MC 1.12+: ICommandSender / CommandSource (1.13+)
 */
public interface ICommandSender {
    String getCommandSenderName();
    IWorld getEntityWorld();
    void addChatMessage(ITextComponent component);
    boolean canCommandSenderUseCommand(int permLevel, String commandName);
}
