package common.minecraft.util;

/**
 * Platform abstraction for Minecraft's chat component hierarchy.
 * MC 1.7.10: ChatComponentText, ChatComponentTranslation, IChatComponent
 * MC 1.12+: ITextComponent, TextComponentString, TextComponentTranslation
 */
public interface ITextComponent {
    String getUnformattedText();
    String getFormattedText();
    ITextComponent appendText(String text);
    ITextComponent appendSibling(ITextComponent sibling);
}
