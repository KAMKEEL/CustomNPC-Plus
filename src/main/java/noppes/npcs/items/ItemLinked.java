package noppes.npcs.items;

import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import net.minecraft.network.play.server.S04PacketEntityEquipment;
import net.minecraft.world.World;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.LinkedItemController;
import noppes.npcs.controllers.data.LinkedItem;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.item.ScriptLinkedItem;

public class ItemLinked extends ItemCustomizable {

    public ItemLinked() {
        maxStackSize = 1;
        CustomNpcs.proxy.registerItem(this);
        setHasSubtypes(true);
    }

    @Override
    public void onUpdate(ItemStack stack, World world, Entity entity, int itemSlot, boolean isSelected) {
        if (world.isRemote)
            return;

        if (world.getTotalWorldTime() % 10 != 0)
            return;

        IItemStack itemStack = NpcAPI.Instance().getIItemStack(stack);
        if (itemStack instanceof ScriptLinkedItem) {
            ScriptLinkedItem scriptLinkedItem = (ScriptLinkedItem) itemStack;
            LinkedItem linkedItem = LinkedItemController.getInstance().get(scriptLinkedItem.linkedItem.getId());
            int prevVersion = scriptLinkedItem.linkedVersion;
            if (linkedItem != null && scriptLinkedItem.linkedVersion != linkedItem.version) {
                scriptLinkedItem.linkedItem = linkedItem.clone();
                scriptLinkedItem.linkedVersion = linkedItem.version;
                scriptLinkedItem.saveItemData();

                // Send Version Change Event
                EventHooks.onLinkedItemVersionChange(scriptLinkedItem, linkedItem.version, prevVersion);
            } else if (linkedItem == null) {
                if (entity instanceof EntityPlayer) {
                    EntityPlayer player = (EntityPlayer) entity;
                    player.inventory.setInventorySlotContents(itemSlot, null);
                    player.inventory.markDirty();

                    // Sync inventory with client
                    if (player instanceof EntityPlayerMP) {
                        EntityPlayerMP playerMP = (EntityPlayerMP) player;
                        int equipmentSlot = -1;
                        // In vanilla 1.7.10, container slots 36-39 are the armor slots:
                        // slot 36: boots, 37: leggings, 38: chestplate, 39: helmet.
                        if (itemSlot >= 36 && itemSlot < 40) {
                            // Map container slot to equipment slot: boots=1, leggings=2, chestplate=3, helmet=4.
                            equipmentSlot = itemSlot - 35;
                        }
                        if (equipmentSlot != -1) {
                            playerMP.playerNetServerHandler.sendPacket(
                                new S04PacketEntityEquipment(player.getEntityId(), equipmentSlot, null)
                            );
                        } else {
                            playerMP.sendContainerToPlayer(player.inventoryContainer);
                        }
                    }
                }
            }
        }
    }
}
