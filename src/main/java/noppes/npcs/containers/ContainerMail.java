package noppes.npcs.containers;

import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.Slot;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerMail;
import noppes.npcs.controllers.data.PlayerMailData;

import java.util.Iterator;

public class ContainerMail extends ContainerNpcInterface {
    public static PlayerMail staticmail = new PlayerMail();
    public PlayerMail mail = new PlayerMail();
    private final boolean canEdit;
    private final boolean canSend;


    public ContainerMail(EntityPlayer player, boolean canEdit, boolean canSend) {
        super(player);
        mail = staticmail;
        staticmail = new PlayerMail();
        this.canEdit = canEdit;
        this.canSend = canSend;
        player.inventory.openInventory();
        int k;

        for (k = 0; k < 4; ++k) {
            this.addSlotToContainer(new SlotValid(mail, k, 179 + k * 24, 138, canEdit));
        }


        for (int j = 0; j < 3; ++j) {
            for (k = 0; k < 9; ++k) {
                this.addSlotToContainer(new Slot(player.inventory, k + j * 9 + 9, 28 + k * 18, 175 + j * 18));
            }
        }

        for (int j = 0; j < 9; ++j) {
            this.addSlotToContainer(new Slot(player.inventory, j, 28 + j * 18, 230));
        }
    }

    /**
     * Called when a player shift-clicks on a slot. You must override this or you will crash when someone does that.
     */
    public ItemStack transferStackInSlot(EntityPlayer par1EntityPlayer, int par2) {
        ItemStack itemstack = null;
        Slot slot = (Slot) this.inventorySlots.get(par2);

        if (slot != null && slot.getHasStack()) {
            ItemStack itemstack1 = slot.getStack();
            itemstack = itemstack1.copy();

            if (par2 < 4) {
                if (!this.mergeItemStack(itemstack1, 4, this.inventorySlots.size(), true)) {
                    return null;
                }
            } else if (!canEdit || !this.mergeItemStack(itemstack1, 0, 4, false)) {
                return null;
            }

            if (itemstack1.stackSize == 0) {
                slot.putStack(null);
            } else {
                slot.onSlotChanged();
            }
        }

        return itemstack;
    }

    /**
     * Called when the container is closed.
     */
    @Override
    public void onContainerClosed(EntityPlayer player) {
        super.onContainerClosed(player);
        if (!canEdit && !player.worldObj.isRemote) {
            PlayerMailData data = PlayerDataController.Instance.getPlayerData(player).mailData;
            Iterator<PlayerMail> it = data.playermail.iterator();
            while (it.hasNext()) {
                PlayerMail mail = it.next();
                if (mail.time == this.mail.time && mail.sender.equals(this.mail.sender)) {
                    mail.readNBT(this.mail.writeNBT());
                    break;
                }
            }
        }
    }

}
