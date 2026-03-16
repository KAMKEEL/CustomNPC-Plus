package common.minecraft.util;

/**
 * Platform abstraction for Minecraft's chat formatting style.
 * MC 1.7.10: ChatStyle
 * MC 1.12+: Style
 */
public interface ITextStyle {
    boolean getBold();
    boolean getItalic();
    boolean getUnderlined();
    boolean getStrikethrough();
    boolean getObfuscated();
    String getColor();
}
