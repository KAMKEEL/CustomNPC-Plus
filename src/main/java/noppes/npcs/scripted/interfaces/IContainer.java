//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;

public interface IContainer {
    int getSize();

    IItemStack getSlot(int var1);

    void setSlot(int slot, IItemStack item);

    IInventory getMCInventory();

    Container getMCContainer();

    int count(IItemStack var1, boolean var2, boolean var3);

    IItemStack[] getItems();

    boolean isCustomGUI();
}
