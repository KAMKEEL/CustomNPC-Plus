package noppes.npcs.api.item;

import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.INbt;

public interface IItemStack {
    String getName();

    /**
     * @return Returns the stacksize
     */
    int getStackSize();

    /**
     * @return Return whether the item has a custom name
     */
    boolean hasCustomName();

    /**
     * @param name The custom name this item will get
     */
    void setCustomName(String name);

    /**
     * @return Return the ingame displayed name. This is either the item name or the custom name if it has one.
     */
    String getDisplayName();

    /**
     * @return Get the items ingame name. Use this incase the item ingame has custom name and you want the original name.
     */
    String getItemName();

    /**
     * @param size The size of the itemstack. A number between 1 and 64
     */
    void setStackSize(int size);

    int getMaxStackSize();

    /**
     * @return Returns the item damage of this item. For tools this is the durability for other items the color and more.
     */
    int getItemDamage();

    /**
     * @param value The value to be set as item damage. For tools this is the durability for other items the color and more.
     */
    void setItemDamage(int value);

    /**
     * @param key The key of this NBTTag
     * @param value The value to be stored. Can be a Number or String
     */
    void setTag(String key, Object value);

    /**
     * @param key The key of the NBTTag
     * @return Returns whether or not the key exists
     */
    boolean hasTag(String key);

    /**
     * @param key The key of the NBTTag
     * @return Returns the value associated with the key. Returns null of it doesnt exist
     */
    Object getTag(String key);

    INbt removeTags();

    boolean isEnchanted();

    /**
     * @since 1.7.10d
     * @param id The enchantment id
     * @return
     */
    boolean hasEnchant(int id);

    void addEnchant(int id, int strength);

    void setAttribute(String name, double value);

    double getAttribute(String name);

    String[] getLore();

    boolean hasLore();

    void setLore(String[] lore);

    boolean hasAttribute(String name);

    /**
     * @since 1.7.10d
     * @return Returns whether this item is a book
     */
    boolean isWrittenBook();

    /**
     * @since 1.7.10d
     * @return Returns the books title
     */
    String getBookTitle();

    /**
     * @since 1.7.10d
     * @return Returns the books author
     */
    String getBookAuthor();

    /**
     * @since 1.7.10d
     * @return If the item is a book, returns a string array with book pages
     */
    String[] getBookText();

    /**
     * @return Returns whether or not this item is a block
     */
    boolean isBlock();

    INbt getNbt();

    INbt getItemNbt();

    /**
     * No support is given for this method. Dont use if you dont know what you are doing.
     * @return Minecraft ItemStack
     */
    ItemStack getMCItemStack();

    NBTTagCompound getMCNbt();

    void setMCNbt(NBTTagCompound compound);
}
