package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import noppes.npcs.scripted.interfaces.item.IItemBook;

public class ScriptItemBook extends ScriptItemStack implements IItemBook {

    public ScriptItemBook(ItemStack item) {
        super(item);
    }

    public String[] getText() {
        return new String[0];
    }

    public void setText(String[] var1) {

    }

    public String getAuthor() {
        return null;
    }

    public void setAuthor(String var1) {

    }

    public String getTitle() {
        return null;
    }

    public void setTitle(String var1) {

    }
}
