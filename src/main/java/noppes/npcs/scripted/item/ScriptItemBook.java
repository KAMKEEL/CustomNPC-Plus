package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import noppes.npcs.api.item.IItemBook;

import java.util.ArrayList;
import java.util.List;

public class ScriptItemBook extends ScriptItemStack implements IItemBook {

    public ScriptItemBook(ItemStack item) {
        super(item);
    }

    public String[] getText() {
        List<String> list = new ArrayList<String>();
        NBTTagList pages = this.getTag().getTagList("pages", 8);

        for(int i = 0; i < pages.tagCount(); ++i) {
            list.add(pages.getStringTagAt(i));
        }

        return (String[])list.toArray(new String[list.size()]);
    }

    public void setText(String[] pages) {
        NBTTagList list = new NBTTagList();
        if (pages != null && pages.length > 0) {
            String[] var3 = pages;
            int var4 = pages.length;

            for(int var5 = 0; var5 < var4; ++var5) {
                String page = var3[var5];
                list.appendTag(new NBTTagString(page));
            }
        }

        this.getTag().setTag("pages", list);
    }

    public String getAuthor() {
        return this.getTag().getString("author");
    }

    public void setAuthor(String author) {
        this.getTag().setString("author",author);
    }

    public String getTitle() {
        return this.getTag().getString("title");
    }

    public void setTitle(String title) {
        this.getTag().setString("title",title);
    }

    private NBTTagCompound getTag() {
        NBTTagCompound comp = this.item.getTagCompound();
        if (comp == null) {
            this.item.setTagCompound(comp = new NBTTagCompound());
        }

        return comp;
    }

    public int getType() {
        return 1;
    }
}
