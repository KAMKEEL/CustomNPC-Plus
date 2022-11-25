//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.api.gui;

import net.minecraft.inventory.Slot;
import noppes.npcs.api.item.IItemStack;

public interface IItemSlot extends ICustomGuiComponent {
    boolean hasStack();

    IItemStack getStack();

    IItemSlot setStack(IItemStack var1);

    Slot getMCSlot();
}
