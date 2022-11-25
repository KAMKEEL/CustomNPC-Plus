package noppes.npcs.scripted.gui;

import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.scripted.NpcAPI;

public class ScriptGuiItemSlot extends ScriptGuiComponent implements IItemSlot {
    IItemStack stack;

    public ScriptGuiItemSlot() {
    }

    public ScriptGuiItemSlot(int x, int y) {
        this.setPos(x, y);
    }

    public ScriptGuiItemSlot(int x, int y, IItemStack stack) {
        this(x, y);
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

    public NBTTagCompound toNBT(NBTTagCompound nbt) {
        super.toNBT(nbt);
        if (this.hasStack()) {
            nbt.setTag("stack", this.stack.getItemNbt().getMCNBT());
        }

        return nbt;
    }

    public ScriptGuiComponent fromNBT(NBTTagCompound nbt) {
        super.fromNBT(nbt);
        if (nbt.hasKey("stack")) {
            this.setStack(NpcAPI.Instance().getIItemStack(ItemStack.loadItemStackFromNBT(nbt.getCompoundTag("stack"))));
        }

        return this;
    }
}
