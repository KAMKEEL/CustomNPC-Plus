//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.entity.player.InventoryPlayer;
import net.minecraft.inventory.Container;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import noppes.npcs.EventHooks;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.client.gui.custom.components.CustomGuiSlot;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.gui.ScriptGui;
import noppes.npcs.util.CustomNPCsScheduler;

import java.util.Iterator;

public class ContainerCustomGui extends Container {
    public ScriptGui customGui;
    public IInventory guiInventory;
    int slotCount = 0;

    public int playerInvX;
    public int playerInvY;

    public ContainerCustomGui(IInventory inventory) {
        this.guiInventory = inventory;
    }

    public void setGui(ScriptGui gui, EntityPlayer player) {
        this.customGui = gui;

        if (this.customGui.getShowPlayerInv()) {
            this.addPlayerInventory(player, this.customGui.getPlayerInvX(), this.customGui.getPlayerInvY());
        }

        Iterator var3 = this.customGui.getSlots().iterator();

        while(var3.hasNext()) {
            IItemSlot slot = (IItemSlot)var3.next();
            if (slot.hasStack()) {
                this.addSlot(player, slot.getPosX(), slot.getPosY(), slot, slot.getStack().getMCItemStack(), player.worldObj.isRemote);
            } else {
                this.addSlot(player, slot.getPosX(), slot.getPosY(), slot, player.worldObj.isRemote);
            }
        }
    }

    public ItemStack transferStackInSlot(EntityPlayer playerIn, int index) {
        ItemStack itemstack = null;
        Slot slot = (Slot)this.inventorySlots.get(index);
        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();
            if (index < this.guiInventory.getSizeInventory()) {
                if (!this.mergeItemStack(itemstack1, this.guiInventory.getSizeInventory(), this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!this.mergeItemStack(itemstack1, 0, this.guiInventory.getSizeInventory(), false)) {
                return null;
            }

            if (itemstack1.stackSize <= 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    void addSlot(EntityPlayer player, int x, int y, IItemSlot slot, boolean clientSide) {
        this.addSlotToContainer(new CustomGuiSlot(player, this.guiInventory, this.slotCount++, slot, x, y, clientSide));
    }

    void addSlot(EntityPlayer player, int x, int y, IItemSlot slot, ItemStack itemStack, boolean clientSide) {
        this.guiInventory.setInventorySlotContents(this.slotCount, itemStack);
        this.addSlotToContainer(new CustomGuiSlot(player, this.guiInventory, this.slotCount++, slot, x, y, clientSide));
    }

    void addPlayerInventory(EntityPlayer player, int x, int y) {
        this.playerInvX = x;
        this.playerInvY = y;

        int row;
        for(row = 0; row < 3; ++row) {
            for(int col = 0; col < 9; ++col) {
                this.addSlotToContainer(new Slot(player.inventory, col + row * 9 + 9, x + col * 18, y + row * 18));
            }
        }

        for(row = 0; row < 9; ++row) {
            this.addSlotToContainer(new Slot(player.inventory, row, x + row * 18, y + 58));
        }
    }

    public boolean canInteractWith(EntityPlayer p_75145_1_) {
        return true;
    }

    @Override
    public ItemStack slotClick(int slotId, int dragType, int clickTypeIn, EntityPlayer player) {
        if(slotId < 0)
            return super.slotClick(slotId, dragType, clickTypeIn, player);
        Slot mcSlot = (Slot)this.inventorySlots.get(slotId);
        if(!player.worldObj.isRemote && mcSlot != null) {
            IItemSlot slot = null;
            if (this.getSlot(slotId) instanceof CustomGuiSlot) {
                slot = ((CustomGuiSlot)this.getSlot(slotId)).slot;
            }
            if(!EventHooks.onCustomGuiSlotClicked((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui)player.openContainer).customGui, slotId, slot, dragType, clickTypeIn)) {
                ItemStack prevStack = mcSlot.getStack();
                if (slot != null) {
                    prevStack = slot.getStack() == null ? null : slot.getStack().getMCItemStack();
                }
                ItemStack item = super.slotClick(slotId, dragType, clickTypeIn, player);

                if (!ItemStack.areItemStacksEqual(prevStack,item)) {
                    if (slot != null) {
                        slot.setStack(NpcAPI.Instance().getIItemStack(mcSlot.getStack()));
                    }
                    if (player.openContainer instanceof ContainerCustomGui) {
                        EventHooks.onCustomGuiSlot((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui)player.openContainer).customGui,
                                mcSlot.slotNumber, prevStack, slot);
                    }
                }

                EntityPlayerMP p = (EntityPlayerMP) player;
                CustomNPCsScheduler.runTack(() -> {p.sendContainerToPlayer(this);}, 10);
                return item;
            }
        }

        return null;
    }

    public boolean canDragIntoSlot(Slot p_94531_1_) { return true; }
}
