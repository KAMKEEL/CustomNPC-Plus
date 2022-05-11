package noppes.npcs;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.ItemEvent;
import noppes.npcs.scripted.interfaces.IItemStack;
import noppes.npcs.scripted.interfaces.IPlayer;
import noppes.npcs.scripted.item.ScriptCustomItem;
import java.util.HashMap;

public class ScriptItemEventHandler {
    public ScriptItemEventHandler() {}

    @SubscribeEvent
    public void invoke(LivingEvent.LivingUpdateEvent event) {
        if(event.entityLiving == null || event.entityLiving.worldObj == null || event.entityLiving instanceof EntityPlayer)
            return;

        if (event.entityLiving instanceof EntityCustomNpc) {
            HashMap<Integer, ItemStack> armor = ((EntityCustomNpc) event.entityLiving).inventory.armor;
            HashMap<Integer, ItemStack> items = ((EntityCustomNpc) event.entityLiving).inventory.items;
            HashMap<Integer, ItemStack> weapons = ((EntityCustomNpc) event.entityLiving).inventory.weapons;
            HashMap<Integer, ItemStack>[] inventories = new HashMap[]{armor, items, weapons};

            for (HashMap<Integer, ItemStack> inventory : inventories) {
                for (ItemStack stack : inventory.values()) {
                    if (stack != null && stack.getItem() == CustomItems.scripted_item) {
                        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
                        EventHooks.onScriptItemUpdate((ScriptCustomItem) istack, event.entityLiving);
                    }
                }
            }
        } else if(event.entityLiving.getHeldItem() != null && event.entityLiving.getHeldItem().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
            IItemStack itemStack = NpcAPI.Instance().getIItemStack(event.entityLiving.getHeldItem());
            EventHooks.onScriptItemUpdate((ScriptCustomItem) itemStack, event.entityLiving);
        }
    }

    @SubscribeEvent
    public void invoke(ItemTossEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer) {
            try {
                if (event.entityItem.getEntityItem().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
                    ScriptCustomItem isw = ItemScripted.GetWrapper(event.entityItem.getEntityItem());
                    EventHooks.onScriptItemTossed(isw, event.player, event.entityItem);
                }
            } catch (Exception e) {}
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.ItemPickupEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer) {
            try {
                if (event.player.getHeldItem().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
                    ScriptCustomItem isw = ItemScripted.GetWrapper(event.player.getHeldItem());
                    EventHooks.onScriptItemPickedUp(isw, event.player);
                }
            } catch(Exception e) {}
        }
    }

    @SubscribeEvent
    public void invoke(EntityJoinWorldEvent event) {
        if (!event.world.isRemote && event.entity instanceof EntityItem) {
            EntityItem entity = (EntityItem)event.entity;
            ItemStack stack = entity.getEntityItem();

            try {
                if (stack.stackSize > 0 && stack.getItem() == CustomItems.scripted_item && EventHooks.onScriptItemSpawn(ItemScripted.GetWrapper(stack), entity)) {
                    event.setCanceled(true);
                }
            } catch(Exception e) {}
        }
    }

    @SubscribeEvent
    public void invoke(EntityInteractEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(!event.entityPlayer.worldObj.isRemote && event.entityPlayer.worldObj instanceof WorldServer) {
            try {
                if (event.entityPlayer.getHeldItem().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
                    ScriptCustomItem isw = ItemScripted.GetWrapper(event.entityPlayer.getHeldItem());
                    ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent(isw, (IPlayer) ScriptController.Instance.getScriptForEntity(event.entityPlayer), 2, NpcAPI.Instance().getIBlock(event.entityPlayer.worldObj, new BlockPos(event.entityPlayer.posX, event.entityPlayer.posY, event.entityPlayer.posZ)));
                    event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
                }
            } catch(Exception e) {}
        }
    }
}
