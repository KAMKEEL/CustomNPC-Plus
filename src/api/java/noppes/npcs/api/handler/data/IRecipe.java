//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.handler.data;

import net.minecraft.item.ItemStack;

public interface IRecipe {
    String getName();

    boolean isGlobal();

    void setIsGlobal(boolean var1);

    boolean getIgnoreNBT();

    void setIgnoreNBT(boolean var1);

    boolean getIgnoreDamage();

    void setIgnoreDamage(boolean var1);

    int getWidth();

    int getHeight();

    ItemStack getResult();

    ItemStack[] getRecipe();

    void delete();

    int getId();
}
