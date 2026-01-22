package noppes.npcs.scripted.item;

import com.google.common.collect.Multimap;
import kamkeel.npcs.controllers.data.attribute.requirement.IRequirementChecker;
import kamkeel.npcs.controllers.data.attribute.requirement.RequirementCheckerRegistry;
import kamkeel.npcs.util.AttributeItemUtil;
import net.minecraft.block.Block;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.entity.ai.attributes.AttributeModifier;
import net.minecraft.init.Blocks;
import net.minecraft.init.Items;
import net.minecraft.item.Item;
import net.minecraft.item.ItemArmor;
import net.minecraft.item.ItemBlock;
import net.minecraft.item.ItemEditableBook;
import net.minecraft.item.ItemFood;
import net.minecraft.item.ItemStack;
import net.minecraft.item.ItemSword;
import net.minecraft.item.ItemWritableBook;
import net.minecraft.nbt.NBTBase;
import net.minecraft.nbt.NBTBase.NBTPrimitive;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraftforge.common.IPlantable;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.api.INbt;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.items.ItemLinked;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.scripted.CustomNPCsException;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptNbt;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ScriptItemStack implements IItemStack {
    public ItemStack item;

    public ScriptItemStack(ItemStack item) {
        this.item = item;
    }

    public int getType() {
        if (this.item.getItem() instanceof IPlantable) {
            return 5;
        } else {
            return this.item.getItem() instanceof ItemSword ? 4 : 0;
        }
    }

    /**
     * @return The minecraft name for this item
     */
    public String getName() {
        return Item.itemRegistry.getNameForObject(item.getItem());
    }

    /**
     * @return Returns the stacksize
     */
    public int getStackSize() {
        return item.stackSize;
    }

    /**
     * @return Return whether or not the item has a custom name
     */
    public boolean hasCustomName() {
        return item.hasDisplayName();
    }

    /**
     * @param name The custom name this item will get
     */
    public void setCustomName(String name) {
        item.setStackDisplayName(name);
    }

    /**
     * @return Return the ingame displayed name. This is either the item name or the custom name if it has one.
     */
    public String getDisplayName() {
        return item.getDisplayName();
    }

    /**
     * @return Get the items ingame name. Use this incase the item ingame has custom name and you want the original name.
     */
    public String getItemName() {
        return item.getItem().getItemStackDisplayName(item);
    }


    public int getMaxStackSize() {
        return this.item.getMaxStackSize();
    }

    /**
     * @param size The size of the itemstack. A number between 1 and 64
     */
    public void setStackSize(int size) {
        if (size < 0)
            size = 1;
        else if (size > 64)
            size = 64;
        item.stackSize = size;
    }

    /**
     * @return Returns the item damage of this item. For tools this is the durability for other items the color and more.
     */
    public int getItemDamage() {
        return item.getItemDamage();
    }

    /**
     * @param value The value to be set as item damage. For tools this is the durability for other items the color and more.
     */
    public void setItemDamage(int value) {
        item.setItemDamage(value);
    }

    /**
     * @param key   The key of this NBTTag
     * @param value The value to be stored. Can be a Number or String
     */
    public void setTag(String key, Object value) {
        if (value instanceof Number)
            getTag().setDouble(key, ((Number) value).doubleValue());
        else if (value instanceof String)
            getTag().setString(key, (String) value);

    }

    /**
     * @param key The key of the NBTTag
     * @return Returns whether or not the key exists
     */
    public boolean hasTag(String key) {
        if (item.stackTagCompound == null)
            return false;
        return getTag().hasKey(key);
    }

    /**
     * @param key The key of the NBTTag
     * @return Returns the value associated with the key. Returns null of it doesnt exist
     */
    public Object getTag(String key) {
        if (item.stackTagCompound == null)
            return null;

        NBTBase tag = getTag().getTag(key);
        if (tag == null)
            return null;
        if (tag instanceof NBTPrimitive)
            return ((NBTPrimitive) tag).func_150286_g();
        if (tag instanceof NBTTagString)
            return ((NBTTagString) tag).func_150285_a_();
        return tag;
    }

    public INbt removeTags() {
        ScriptNbt nbt = (ScriptNbt) NpcAPI.Instance().getINbt(item.stackTagCompound);
        item.stackTagCompound = null;
        return nbt;
    }

    public boolean isEnchanted() {
        return item.isItemEnchanted();
    }

    /**
     * @param id The enchantment id
     * @return
     * @since 1.7.10d
     */
    public boolean hasEnchant(int id) {
        if (!isEnchanted())
            return false;
        NBTTagList list = item.getEnchantmentTagList();
        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compound = list.getCompoundTagAt(i);
            if (compound.getShort("id") == id)
                return true;
        }
        return false;
    }

    public void addEnchant(int id, int strength) {
        Enchantment ench = Enchantment.enchantmentsList[id];
        if (ench == null) {
            throw new CustomNPCsException("Unknown enchant id:" + id, new Object[0]);
        } else {
            this.item.addEnchantment(ench, strength);
        }
    }

    public void setAttribute(String name, double value) {
        NBTTagCompound compound = this.item.getTagCompound();
        if (compound == null) {
            this.item.setTagCompound(compound = new NBTTagCompound());
        }

        NBTTagList nbttaglist = compound.getTagList("AttributeModifiers", 10);
        NBTTagList newList = new NBTTagList();

        NBTTagCompound nbttagcompound;
        for (int i = 0; i < nbttaglist.tagCount(); ++i) {
            nbttagcompound = nbttaglist.getCompoundTagAt(i);
            if (!nbttagcompound.getString("AttributeName").equals(name)) {
                newList.appendTag(nbttagcompound);
            }
        }

        if (value != 0.0D) {
            AttributeModifier attributeModifier = new AttributeModifier(name, value, 0);
            nbttagcompound = new NBTTagCompound();
            nbttagcompound.setString("Name", attributeModifier.getName());
            nbttagcompound.setDouble("Amount", attributeModifier.getAmount());
            nbttagcompound.setInteger("Operation", attributeModifier.getOperation());
            nbttagcompound.setLong("UUIDMost", attributeModifier.getID().getMostSignificantBits());
            nbttagcompound.setLong("UUIDLeast", attributeModifier.getID().getLeastSignificantBits());
            nbttagcompound.setString("AttributeName", name);
            newList.appendTag(nbttagcompound);
        }

        compound.setTag("AttributeModifiers", newList);
    }

    public double getAttribute(String name) {
        NBTTagCompound compound = this.item.getTagCompound();
        if (compound == null) {
            return 0.0D;
        } else {
            Multimap<String, AttributeModifier> map = this.item.getAttributeModifiers();
            Iterator var4 = map.entries().iterator();

            while (var4.hasNext()) {
                Map.Entry entry = (Map.Entry) var4.next();
                if (((String) entry.getKey()).equals(name)) {
                    AttributeModifier mod = (AttributeModifier) entry.getValue();
                    return mod.getAmount();
                }
            }

            return 0.0D;
        }
    }

    public String[] getLore() {
        if (this.item.getTagCompound() != null) {
            NBTTagCompound compound = this.item.getTagCompound().getCompoundTag("display");
            if (compound != null && compound.func_150299_b("Lore") == 9) {
                NBTTagList nbttaglist = compound.getTagList("Lore", 8);
                if (nbttaglist.tagCount() > 0) {
                    List<String> lore = new ArrayList();

                    for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                        lore.add(nbttaglist.getStringTagAt(i));
                    }

                    return (String[]) ((String[]) lore.toArray(new String[lore.size()]));
                }
            }
        }
        return new String[0];
    }

    public boolean hasLore() {
        if (this.item.getTagCompound() != null) {
            NBTTagCompound compound = this.item.getTagCompound().getCompoundTag("display");
            if (compound != null && compound.func_150299_b("Lore") == 9) {
                NBTTagList nbttaglist = compound.getTagList("Lore", 8);
                return nbttaglist.tagCount() > 0;
            }
        }
        return false;
    }

    public void setLore(String[] lore) {
        NBTTagCompound compound = this.item.getTagCompound();
        if (compound == null) {
            this.item.setTagCompound(compound = new NBTTagCompound());
        }

        NBTTagCompound display = compound.getCompoundTag("display");
        if (lore == null || lore.length == 0) {
            display.removeTag("Lore");
            return;
        }

        NBTTagList nbtlist = new NBTTagList();
        for (String s : lore) {
            nbtlist.appendTag(new NBTTagString(s));
        }
        display.setTag("Lore", nbtlist);
        compound.setTag("display", display);
    }

    public boolean hasAttribute(String name) {
        NBTTagCompound compound = this.item.getTagCompound();
        if (compound == null) {
            return false;
        } else {
            NBTTagList nbttaglist = compound.getTagList("AttributeModifiers", 10);

            for (int i = 0; i < nbttaglist.tagCount(); ++i) {
                NBTTagCompound c = nbttaglist.getCompoundTagAt(i);
                if (c.getString("AttributeName").equals(name)) {
                    return true;
                }
            }

            return false;
        }
    }

    private static ScriptItemStack createNew(ItemStack item) {
        if (item == null)
            return null;

        if (item.getItem() instanceof ItemLinked) {
            return new ScriptLinkedItem(item);
        }
        if (item.getItem() instanceof ItemScripted) {
            return new ScriptCustomItem(item);
        }
        if (item.getItem() == Items.written_book || item.getItem() == Items.writable_book || item.getItem() instanceof ItemWritableBook || item.getItem() instanceof ItemEditableBook)
            return new ScriptItemBook(item);
        if (item.getItem() instanceof ItemArmor)
            return new ScriptItemArmor(item);
        if (item.getItem() instanceof ItemBlock)
            return new ScriptItemBlock(item);

        return new ScriptItemStack(item);
    }

    @Override
    public IItemStack copy() {
        return createNew(item.copy());
    }

    @Override
    public int getMaxItemDamage() {
        return item.getMaxDamage();
    }

    /**
     * @return Returns whether this item is a book
     * @since 1.7.10d
     */
    public boolean isWrittenBook() {
        return item.getItem() == Items.written_book || item.getItem() == Items.writable_book;
    }

    /**
     * @return Returns the books title
     * @since 1.7.10d
     */
    public String getBookTitle() {
        return item.getTagCompound().getString("title");
    }

    /**
     * @return Returns the books author
     * @since 1.7.10d
     */
    public String getBookAuthor() {
        return item.getTagCompound().getString("author");
    }

    /**
     * @return If the item is a book, returns a string array with book pages
     * @since 1.7.10d
     */
    public String[] getBookText() {
        if (!isWrittenBook())
            return null;
        List<String> list = new ArrayList<String>();
        NBTTagList pages = item.getTagCompound().getTagList("pages", 8);
        for (int i = 0; i < pages.tagCount(); ++i) {
            list.add(pages.getStringTagAt(i));
        }
        return list.toArray(new String[list.size()]);
    }

    private NBTTagCompound getTag() {
        if (item.stackTagCompound == null)
            item.stackTagCompound = new NBTTagCompound();
        return item.stackTagCompound;
    }

    /**
     * @return Returns whether or not this item is a block
     */
    public boolean isBlock() {
        Block block = Block.getBlockFromItem(item.getItem());
        if (block == null || block == Blocks.air)
            return false;
        return true;
    }

    public boolean isFood() {
        return item.getItem() instanceof ItemFood;
    }

    public int getFoodPoints() {
        if (isFood()) return ((ItemFood) item.getItem()).func_150905_g(item);
        return -1;
    }

    public INbt getNbt() {
        NBTTagCompound compound = this.item.getTagCompound();
        if (compound == null) {
            this.item.setTagCompound(compound = new NBTTagCompound());
        }

        return NpcAPI.Instance().getINbt(compound);
    }

    public INbt getItemNbt() {
        NBTTagCompound compound = new NBTTagCompound();
        this.item.writeToNBT(compound);
        return NpcAPI.Instance().getINbt(compound);
    }

    public NBTTagCompound getMCNbt() {
        NBTTagCompound compound = new NBTTagCompound();
        this.item.writeToNBT(compound);
        return compound;
    }

    public void setMCNbt(NBTTagCompound compound) {
    }

    /**
     * No support is given for this method. Dont use if you dont know what you are doing.
     *
     * @return Minecraft ItemStack
     */
    public ItemStack getMCItemStack() {
        return item;
    }

    public int itemHash() {
        return item.hashCode();
    }

    public boolean equals(Object object) {
        return object instanceof ScriptItemStack && ((ScriptItemStack) object).getMCItemStack().equals(this.getMCItemStack());
    }

    @Override
    public boolean compare(IItemStack item, boolean ignoreNBT) {
        return NoppesUtilPlayer.compareItems(getMCItemStack(), item.getMCItemStack(), false, ignoreNBT);
    }

    @Override
    public boolean compare(IItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
        return NoppesUtilPlayer.compareItems(getMCItemStack(), item.getMCItemStack(), ignoreDamage, ignoreNBT);
    }

    @Override
    public void setCustomAttribute(String key, double value) {
        AttributeItemUtil.applyAttribute(item, key, (float) value);
    }

    @Override
    public boolean hasCustomAttribute(String key) {
        return AttributeItemUtil.readAttributes(item).containsKey(key);
    }

    @Override
    public float getCustomAttribute(String key) {
        return hasCustomAttribute(key) ? AttributeItemUtil.readAttributes(item).get(key) : 0.0f;
    }

    @Override
    public void removeCustomAttribute(String key) {
        AttributeItemUtil.removeAttribute(item, key);
    }

    @Override
    public void setMagicAttribute(String key, int magicId, double value) {
        AttributeItemUtil.writeMagicAttribute(item, key, magicId, (float) value);
    }

    @Override
    public float getMagicAttribute(String key, int magicId) {
        Map<Integer, Float> magicMap = AttributeItemUtil.readMagicAttributeMap(item, key);
        return magicMap.getOrDefault(magicId, 0.0f);
    }

    @Override
    public boolean hasMagicAttribute(String key, int magicId) {
        Map<Integer, Float> magicMap = AttributeItemUtil.readMagicAttributeMap(item, key);
        return magicMap.containsKey(magicId);
    }

    @Override
    public void removeMagicAttribute(String key, int magicId) {
        AttributeItemUtil.removeMagicAttribute(item, key, magicId);
    }

    @Override
    public void setRequirement(String reqKey, Object value) {
        // Delegate to the utility method that applies the requirement into the item's NBT.
        AttributeItemUtil.applyRequirement(item, reqKey, value);
    }

    @Override
    public boolean hasRequirement(String reqKey) {
        if (item == null || item.stackTagCompound == null)
            return false;
        NBTTagCompound root = item.stackTagCompound;
        if (!root.hasKey(AttributeItemUtil.TAG_RPGCORE))
            return false;
        NBTTagCompound rpgCore = root.getCompoundTag(AttributeItemUtil.TAG_RPGCORE);
        if (!rpgCore.hasKey(AttributeItemUtil.TAG_REQUIREMENTS))
            return false;
        NBTTagCompound reqTag = rpgCore.getCompoundTag(AttributeItemUtil.TAG_REQUIREMENTS);
        return reqTag.hasKey(reqKey);
    }

    @Override
    public Object getRequirement(String reqKey) {
        if (item == null || item.stackTagCompound == null)
            return null;
        NBTTagCompound root = item.stackTagCompound;
        if (!root.hasKey(AttributeItemUtil.TAG_RPGCORE))
            return null;
        NBTTagCompound rpgCore = root.getCompoundTag(AttributeItemUtil.TAG_RPGCORE);
        if (!rpgCore.hasKey(AttributeItemUtil.TAG_REQUIREMENTS))
            return null;
        NBTTagCompound reqTag = rpgCore.getCompoundTag(AttributeItemUtil.TAG_REQUIREMENTS);
        if (reqTag.hasKey(reqKey)) {
            IRequirementChecker checker = RequirementCheckerRegistry.getChecker(reqKey);
            if (checker != null) {
                return checker.getValue(reqTag);
            }
            // If no checker exists, fall back to returning the raw stored value.
            NBTBase tag = reqTag.getTag(reqKey);
            if (tag instanceof NBTPrimitive)
                return ((NBTPrimitive) tag).func_150286_g();
            if (tag instanceof NBTTagString)
                return ((NBTTagString) tag).func_150285_a_();
            return tag;
        }
        return null;
    }

    @Override
    public void removeRequirement(String reqKey) {
        AttributeItemUtil.removeRequirement(item, reqKey);
    }

    @Override
    public String[] getCustomAttributeKeys() {
        Map<String, Float> attrs = AttributeItemUtil.readAttributes(item);
        return attrs.keySet().toArray(new String[attrs.size()]);
    }

    @Override
    public String[] getMagicAttributeKeys(String key) {
        Map<Integer, Float> magicMap = AttributeItemUtil.readMagicAttributeMap(item, key);
        // Convert the integer magic IDs to strings.
        String[] keys = new String[magicMap.size()];
        int i = 0;
        for (Integer magicId : magicMap.keySet()) {
            keys[i++] = magicId.toString();
        }
        return keys;
    }

    @Override
    public String[] getRequirementKeys() {
        if (item == null || item.stackTagCompound == null)
            return new String[0];
        NBTTagCompound root = item.stackTagCompound;
        if (!root.hasKey(AttributeItemUtil.TAG_RPGCORE))
            return new String[0];
        NBTTagCompound rpgCore = root.getCompoundTag(AttributeItemUtil.TAG_RPGCORE);
        if (!rpgCore.hasKey(AttributeItemUtil.TAG_REQUIREMENTS))
            return new String[0];
        NBTTagCompound reqTag = rpgCore.getCompoundTag(AttributeItemUtil.TAG_REQUIREMENTS);
        Set<String> keys = reqTag.func_150296_c();
        return keys.toArray(new String[keys.size()]);
    }
}
