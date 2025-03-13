package kamkeel.npcs.util;

import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import net.minecraft.util.EnumChatFormatting;
import net.minecraft.util.IChatComponent;

public class ColorUtil {


    public static int[] colorTableInts = {
        0xFFFFFF, // White
        0xF2B233, // Light Orange
        0xE680D9, // Pinkish Purple
        0x99B2F2, // Light Blue
        0xE6E633, // Yellow
        0x80CC1A, // Green
        0xF2B2CC, // Light Pink
        0x4D4D4D, // Dark Gray
        0x999999, // Gray
        0x4D99B2, // Teal
        0xB266E6, // Purple
        0x3366CC, // Deep Blue
        0x805D4D, // Brown
        0x668033, // Olive Green
        0xCC4D4D, // Red
        0x1A1A1A   // Black
    };

    public static float[] hexToRGB(int hex) {
        float r = ((hex >> 16) & 0xFF) / 255.0F;
        float g = ((hex >> 8) & 0xFF) / 255.0F;
        float b = (hex & 0xFF) / 255.0F;
        return new float[]{r, g, b};
    }


    /**
     * Helper method that retrieves the EnumChatFormatting associated with a given formatting code character.
     *
     * @param c The formatting code character (e.g., '6' or 'l').
     * @return The matching EnumChatFormatting, or null if not found.
     */
    public static EnumChatFormatting getFormattingByChar(char c) {
        for (EnumChatFormatting format : EnumChatFormatting.values()) {
            if (format.getFormattingCode() == c) {
                return format;
            }
        }
        return null;
    }

    /**
     * Assembles a composite IChatComponent from a raw text string with formatting codes (using §).
     * Each time the formatting changes, a new component is created and appended as a sibling.
     *
     * @param text The raw text with formatting codes.
     * @return A composite IChatComponent reflecting the formatting changes.
     */
    public static IChatComponent assembleComponent(String text) {
        // Final composite component
        ChatComponentText composite = new ChatComponentText("");
        // Accumulates text until a formatting code change
        ChatComponentText current = new ChatComponentText("");
        // Start with a fresh ChatStyle
        ChatStyle currentStyle = new ChatStyle();

        int i = 0;
        while (i < text.length()) {
            char c = text.charAt(i);
            if (c == '\u00A7' && i + 1 < text.length()) {
                // When a formatting code is encountered, first add any accumulated text as a sibling.
                if (current.getUnformattedText().length() > 0) {
                    current.setChatStyle(currentStyle.createDeepCopy());
                    composite.appendSibling(current);
                    current = new ChatComponentText("");
                }
                char code = text.charAt(i + 1);
                EnumChatFormatting formatting = getFormattingByChar(code);
                if (formatting != null) {
                    // If it’s a color code (0-9, a-f), then it resets previous formatting.
                    if ("0123456789abcdef".indexOf(Character.toLowerCase(code)) >= 0) {
                        currentStyle = new ChatStyle().setColor(formatting);
                    } else {
                        // For modifiers (BOLD, ITALIC, etc.)
                        if (formatting == EnumChatFormatting.RESET) {
                            currentStyle = new ChatStyle();
                        } else {
                            switch (formatting) {
                                case BOLD:
                                    currentStyle = currentStyle.setBold(Boolean.TRUE);
                                    break;
                                case ITALIC:
                                    currentStyle = currentStyle.setItalic(Boolean.TRUE);
                                    break;
                                case UNDERLINE:
                                    currentStyle = currentStyle.setUnderlined(Boolean.TRUE);
                                    break;
                                case STRIKETHROUGH:
                                    currentStyle = currentStyle.setStrikethrough(Boolean.TRUE);
                                    break;
                                case OBFUSCATED:
                                    currentStyle = currentStyle.setObfuscated(Boolean.TRUE);
                                    break;
                                default:
                                    break;
                            }
                        }
                    }
                }
                i += 2; // Skip the § and the code character.
            } else {
                // Accumulate normal characters.
                current.appendText(String.valueOf(c));
                i++;
            }
        }
        // Append any trailing text.
        if (current.getUnformattedText().length() > 0) {
            current.setChatStyle(currentStyle.createDeepCopy());
            composite.appendSibling(current);
        }
        return composite;
    }

    private static final String LABEL = "\u00A76[\u00A7eCNPC+\u00A76] ";

    /**
     * Assembles a composite chat component from a raw text string with formatting codes,
     * and immediately sends it to the specified ICommandSender.
     *
     * @param sender The ICommandSender to send the message to.
     * @param text   The raw text (with § codes) to be assembled and sent.
     */
    public static void sendMessage(ICommandSender sender, String text) {
        text = "\u00A77" + text;
        IChatComponent component = assembleComponent(text);
        sender.addChatMessage(component);
    }

    public static void sendMessage(ICommandSender sender, String format, Object... obs) {
        sendMessage(sender, String.format(format, obs));
    }

    public static void sendResult(ICommandSender sender, String text) {
        text = LABEL + "\u00A7a" + text;
        IChatComponent component = assembleComponent(text);
        sender.addChatMessage(component);
    }

    public static void sendResult(ICommandSender sender, String format, Object... obs) {
        sendResult(sender, String.format(format, obs));
    }

    public static void sendError(ICommandSender sender, String text) {
        text = LABEL + "\u00A74Error: \u00A7c" + text;
        IChatComponent component = assembleComponent(text);
        sender.addChatMessage(component);
    }

    public static void sendError(ICommandSender sender, String format, Object... obs) {
        sendError(sender, String.format(format, obs));
    }

}
