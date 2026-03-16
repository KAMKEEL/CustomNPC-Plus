package noppes.npcs;

import noppes.npcs.api.INbt;
import java.util.List;
import java.util.ArrayList;

/**
 * TEMPORARY STUB -- auto-generated for compilation.
 * Miscellaneous inventory container.
 */
public class NpcMiscInventory {
    public int size;
    public NpcMiscInventory(int size) { this.size = size; }
    public noppes.npcs.api.item.IItemStack getStackInSlot(int slot) { return null; }
    public void setInventorySlotContents(int slot, noppes.npcs.api.item.IItemStack stack) {}
    public INbt getToNBT() { return noppes.npcs.api.NBT.compound(); }
    public void setFromNBT(INbt nbt) {}
    public int getSizeInventory() { return size; }
}
