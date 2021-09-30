//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.item;

import net.minecraft.item.ItemStack;
import noppes.npcs.scripted.INbt;
import noppes.npcs.scripted.entity.IEntityLiving;
import noppes.npcs.scripted.entity.data.IData;

public interface IItemStack {
    int getStackSize();

    void setStackSize(int var1);

    int getMaxStackSize();

    int getItemDamage();

    void setItemDamage(int var1);

    int getMaxItemDamage();

    double getAttackDamage();

    void damageItem(int var1, IEntityLiving var2);

    void addEnchantment(String var1, int var2);

    boolean isEnchanted();

    boolean hasEnchant(String var1);

    boolean removeEnchant(String var1);

    /** @deprecated */
    boolean isBlock();

    boolean isWearable();

    boolean hasCustomName();

    void setCustomName(String var1);

    String getDisplayName();

    String getItemName();

    String getName();

    /** @deprecated */
    boolean isBook();

    IItemStack copy();

    ItemStack getMCItemStack();

    INbt getNbt();

    boolean hasNbt();

    void removeNbt();

    INbt getItemNbt();

    boolean isEmpty();

    int getType();

    String[] getLore();

    void setLore(String[] var1);

    /** @deprecated */
    void setAttribute(String var1, double var2);

    void setAttribute(String var1, double var2, int var4);

    double getAttribute(String var1);

    boolean hasAttribute(String var1);

    IData getTempdata();

    IData getStoreddata();

    int getFoodLevel();

    boolean compare(IItemStack var1, boolean var2);
}
