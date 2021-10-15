package noppes.npcs.scripted;

import net.minecraft.block.Block;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;

import java.util.ArrayList;
import java.util.List;

public interface IItemStack {
    public String getName();

    /**
     * @return Returns the stacksize
     */
    public int getStackSize();

    /**
     * @return Return whether or not the item has a custom name
     */
    public boolean hasCustomName();

    /**
     * @param name The custom name this item will get
     */
    public void setCustomName(String name);

    /**
     * @return Return the ingame displayed name. This is either the item name or the custom name if it has one.
     */
    public String getDisplayName();

    /**
     * @return Get the items ingame name. Use this incase the item ingame has custom name and you want the original name.
     */
    public String getItemName();

    /**
     * @param size The size of the itemstack. A number between 1 and 64
     */
    public void setStackSize(int size);

    /**
     * @return Returns the item damage of this item. For tools this is the durability for other items the color and more.
     */
    public int getItemDamage();

    /**
     * @param value The value to be set as item damage. For tools this is the durability for other items the color and more.
     */
    public void setItemDamage(int value);

    /**
     * @param key The key of this NBTTag
     * @param value The value to be stored. Can be a Number or String
     */
    public void setTag(String key, Object value);

    /**
     * @param key The key of the NBTTag
     * @return Returns whether or not the key exists
     */
    public boolean hasTag(String key);

    /**
     * @param key The key of the NBTTag
     * @return Returns the value associated with the key. Returns null of it doesnt exist
     */
    public Object getTag(String key);

    public boolean isEnchanted();

    /**
     * @since 1.7.10d
     * @param id The enchantment id
     * @return
     */
    public boolean hasEnchant(int id);

    /**
     * @since 1.7.10d
     * @return Returns whether this item is a book
     */
    public boolean isWrittenBook();

    /**
     * @since 1.7.10d
     * @return Returns the books title
     */
    public String getBookTitle();

    /**
     * @since 1.7.10d
     * @return Returns the books author
     */
    public String getBookAuthor();

    /**
     * @since 1.7.10d
     * @return If the item is a book, returns a string array with book pages
     */
    public String[] getBookText();

    /**
     * @return Returns whether or not this item is a block
     */
    public boolean isBlock();

    public INbt getNbt();

    public INbt getItemNbt();

    /**
     * No support is given for this method. Dont use if you dont know what you are doing.
     * @return Minecraft ItemStack
     */
    public ItemStack getMCItemStack();
}
