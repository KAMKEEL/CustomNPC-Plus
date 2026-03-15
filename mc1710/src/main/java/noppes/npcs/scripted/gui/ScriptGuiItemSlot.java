package noppes.npcs.scripted.gui;

import net.minecraft.inventory.Slot;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.IItemStack;

public class ScriptGuiItemSlot extends ScriptGuiComponent implements IItemSlot {
    IItemStack stack;

    public ScriptGuiItemSlot() {
    }

    public ScriptGuiItemSlot(int id, int x, int y) {
        this.setID(id);
        this.setPos(x, y);
    }

    public ScriptGuiItemSlot(int id, int x, int y, IItemStack stack) {
        this(id, x, y);
        this.setStack(stack);
    }

    public boolean hasStack() {
        return this.stack != null && this.stack.getStackSize() > 0;
    }

    public IItemStack getStack() {
        return this.stack;
    }

    public IItemSlot setStack(IItemStack itemStack) {
        this.stack = itemStack;
        return this;
    }

    public Slot getMCSlot() {
        return null;
    }

    public int getType() {
        return 5;
    }
}
