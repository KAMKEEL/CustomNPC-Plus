package noppes.npcs.client;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.FontRenderer;
import net.minecraft.command.ICommandSender;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.ChatStyle;
import noppes.npcs.NoppesStringUtils;
import noppes.npcs.TextBlock;
import noppes.npcs.controllers.data.Dialog;

import java.util.List;

public class TextBlockClient extends TextBlock {
    private ChatStyle style;
    public int color = 0xe0e0e0;
    public int titleColor = 0xe0e0e0;
    public int titlePos = 0;
    private String name;
    private ICommandSender sender;

    public TextBlockClient(ICommandSender sender, Dialog dialog, Object... obs) {
        this(dialog.text, dialog.textWidth, false, obs);
        this.color = dialog.color;
        this.titleColor = dialog.titleColor;
        this.titlePos = dialog.titlePos;
        this.sender = sender;
    }

    public TextBlockClient(String name, String text, int lineWidth, int color, Object... obs) {
        this(text, lineWidth, false, obs);
        this.color = color;
        this.name = name;
    }

    public String getName() {
        if (sender != null)
            return sender.getCommandSenderName();
        return name;
    }

    public TextBlockClient(String text, int lineWidth, boolean mcFont, Object... obs) {
        style = new ChatStyle();
        text = NoppesStringUtils.formatText(text, obs);

        final FontRenderer font = Minecraft.getMinecraft().fontRenderer;
        UnicodeLineWrapper.WidthMeasurer measurer = mcFont
            ? new UnicodeLineWrapper.WidthMeasurer() {
                @Override
                public int width(String value) {
                    return font.getStringWidth(value);
                }
            }
            : new UnicodeLineWrapper.WidthMeasurer() {
                @Override
                public int width(String value) {
                    return ClientProxy.Font.width(value);
                }
            };

        List<String> wrappedLines = UnicodeLineWrapper.wrap(text, lineWidth, measurer);
        for (String line : wrappedLines) {
            addLine(line);
        }
    }

    private void addLine(String text) {
        ChatComponentText line = new ChatComponentText(text);
        line.setChatStyle(style);
        lines.add(line);
    }
}
