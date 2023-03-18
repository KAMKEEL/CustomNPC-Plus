//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import noppes.npcs.api.gui.IItemSlot;

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
}
