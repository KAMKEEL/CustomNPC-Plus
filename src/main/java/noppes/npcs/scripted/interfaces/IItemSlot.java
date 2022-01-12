//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted.interfaces;

import net.minecraft.inventory.Slot;

public interface IItemSlot extends ICustomGuiComponent {
    boolean hasStack();

    IItemStack getStack();

    IItemSlot setStack(IItemStack var1);

    Slot getMCSlot();
}
