package noppes.npcs.client;

import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.stats.Achievement;
import net.minecraft.util.ChatComponentText;
import net.minecraft.util.IChatComponent;
import noppes.npcs.CustomItems;

public class MessageAchievement extends Achievement {

    private final String description;
    private final String message;

    public MessageAchievement(Item item, String message, String description) {
        super("", message, 0, 0, item, null);
        this.description = description;
        this.message = message;
    }

    public MessageAchievement(String message, String description) {
        this(CustomItems.letter == null ? Items.paper : CustomItems.letter, message, description);
    }

    @Override
    public IChatComponent func_150951_e() {
        return new ChatComponentText(message);
    }

    public String getDescription() {
        return description;
    }
}
