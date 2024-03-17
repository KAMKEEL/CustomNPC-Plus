//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import noppes.npcs.EventHooks;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.gui.IItemSlot;
import noppes.npcs.containers.ContainerCustomGui;
import noppes.npcs.scripted.NpcAPI;

public class CustomGuiSlot extends Slot {
    public boolean clientSide;
    public final int slotNumber;
    public final IItemSlot slot;
    public final EntityPlayer player;

    public CustomGuiSlot(EntityPlayer player, IInventory inventoryIn, int index, IItemSlot slot, int xPosition, int yPosition, boolean clientSide) {
        super(inventoryIn, index, xPosition, yPosition);
        this.clientSide = clientSide;
        this.slotNumber = index;
        this.slot = slot;
        this.player = player;
    }

    @Override
    public void onSlotChanged() {
        if(!player.worldObj.isRemote){
            boolean changed;
            if (getStack() != null && slot.getStack() != null) {
                changed = !getStack().equals(slot.getStack().getMCItemStack());
            } else {
                // Handle the case when either getStack() or slot.getStack() is null (indicating an empty slot)
                changed = getStack() != null || slot.getStack() != null;
            }
            if(changed) {
                slot.setStack(NpcAPI.Instance().getIItemStack(getStack()));
                if (player.openContainer instanceof ContainerCustomGui) {
                    EventHooks.onCustomGuiSlot((IPlayer) NpcAPI.Instance().getIEntity(player), ((ContainerCustomGui)player.openContainer).customGui,
                        getSlotIndex(), getStack(), slot);
                }
            }
        }
        super.onSlotChanged();
    }

}
