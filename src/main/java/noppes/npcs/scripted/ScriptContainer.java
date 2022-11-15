//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.scripted;

import net.minecraft.block.Block;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import noppes.npcs.NoppesUtilPlayer;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.api.IContainer;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemStack;

public class ScriptContainer implements IContainer {
    private IInventory inventory;
    private Container container;

    public ScriptContainer(IInventory inventory) {
        this.inventory = inventory;
    }

    public ScriptContainer(Container container) {
        this.container = container;
    }

    public int getSize() {
        return this.inventory != null ? this.inventory.getSizeInventory() : this.container.inventorySlots.size();
    }

    public IItemStack getSlot(int slot) {
        if (slot >= 0 && slot < this.getSize()) {
            return this.inventory != null ? NpcAPI.Instance().getIItemStack(this.inventory.getStackInSlot(slot)) : NpcAPI.Instance().getIItemStack(this.container.getSlot(slot).getStack());
        } else {
            throw new CustomNPCsException("Slot is out of range " + slot, new Object[0]);
        }
    }

    public void setSlot(int slot, IItemStack item) {
        if (slot >= 0 && slot < this.getSize()) {
            ItemStack itemstack = item == null ? new ItemStack(Block.getBlockById(0)) : item.getMCItemStack();
            if (this.inventory != null) {
                this.inventory.setInventorySlotContents(slot, itemstack);
            } else {
                this.container.putStackInSlot(slot, itemstack);
                this.container.detectAndSendChanges();
            }

        } else {
            throw new CustomNPCsException("Slot is out of range " + slot, new Object[0]);
        }
    }

    public int count(IItemStack item, boolean ignoreDamage, boolean ignoreNBT) {
        int count = 0;

        for(int i = 0; i < this.getSize(); ++i) {
            IItemStack toCompare = this.getSlot(i);
            if (NoppesUtilPlayer.compareItems(item.getMCItemStack(), toCompare.getMCItemStack(), ignoreDamage, ignoreNBT)) {
                count += toCompare.getStackSize();
            }
        }

        return count;
    }

    public IInventory getMCInventory() {
        return this.inventory;
    }

    public Container getMCContainer() {
        return this.container;
    }

    public IItemStack[] getItems() {
        IItemStack[] items = new IItemStack[this.getSize()];

        for(int i = 0; i < this.getSize(); ++i) {
            items[i] = this.getSlot(i);
        }

        return items;
    }

    public boolean isCustomGUI() {
        return this.container instanceof ContainerCustomGui;
    }

    public void detectAndSendChanges() {
        container.detectAndSendChanges();
    }

    public boolean isPlayerNotUsingContainer(IPlayer player){
        return container.isPlayerNotUsingContainer((EntityPlayerMP) player.getMCEntity());
    }
}
