//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.scripted.gui;

import net.minecraft.inventory.Slot;
import noppes.npcs.scripted.gui.ICustomGuiComponent;
import noppes.npcs.scripted.item.IItemStack;

public interface IItemSlot extends ICustomGuiComponent {
    boolean hasStack();

    IItemStack getStack();

    IItemSlot setStack(IItemStack var1);

    Slot getMCSlot();
}
