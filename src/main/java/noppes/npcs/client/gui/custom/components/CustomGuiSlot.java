//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package noppes.npcs.client.gui.custom.components;

import net.minecraft.client.Minecraft;
import net.minecraft.inventory.IInventory;
import net.minecraft.inventory.Slot;
import noppes.npcs.client.gui.custom.GuiCustom;

public class CustomGuiSlot extends Slot {
    public boolean clientSide;
    public int slotNumber;

    public CustomGuiSlot(IInventory inventoryIn, int index, int xPosition, int yPosition, boolean clientSide) {
        super(inventoryIn, index, xPosition, yPosition);
        this.clientSide = clientSide;
        this.slotNumber = index;
    }

    public void onSlotChanged() {
        if (this.clientSide) {
            ((GuiCustom)Minecraft.getMinecraft().currentScreen).slotChange(this);
        }

        super.onSlotChanged();
    }
}
