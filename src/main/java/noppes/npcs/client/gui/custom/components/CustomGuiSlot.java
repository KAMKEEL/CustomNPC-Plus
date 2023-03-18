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
    public final int slotNumber;
    public final int guiSlotId;

    public CustomGuiSlot(IInventory inventoryIn, int index, int guiSlotId, int xPosition, int yPosition, boolean clientSide) {
        super(inventoryIn, index, xPosition, yPosition);
        this.clientSide = clientSide;
        this.slotNumber = index;
        this.guiSlotId = guiSlotId;
    }

    public void onSlotChanged() {
        if (this.clientSide) {
            ((GuiCustom)Minecraft.getMinecraft().currentScreen).slotChange(this);
        }

        super.onSlotChanged();
    }
}
