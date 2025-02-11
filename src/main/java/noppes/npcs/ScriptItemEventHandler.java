package noppes.npcs;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemCustom;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.items.ItemScripted;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.ItemEvent;
import noppes.npcs.scripted.item.ScriptCustomItem;

import java.util.HashMap;

public class ScriptItemEventHandler {
    public ScriptItemEventHandler() {}

    @SubscribeEvent
    public void invoke(LivingEvent.LivingUpdateEvent event) {
        if(event.entityLiving == null || event.entityLiving.worldObj == null || event.entityLiving instanceof EntityPlayer || event.entityLiving.ticksExisted%10 != 0)
            return;

        if (event.entityLiving instanceof EntityCustomNpc) {
            HashMap<Integer, ItemStack> armor = ((EntityCustomNpc) event.entityLiving).inventory.armor;
            HashMap<Integer, ItemStack> weapons = ((EntityCustomNpc) event.entityLiving).inventory.weapons;
            HashMap<Integer, ItemStack>[] inventories = new HashMap[]{armor, weapons};

            for (HashMap<Integer, ItemStack> inventory : inventories) {
                for (ItemStack stack : inventory.values()) {
                    if (stack != null && stack.getItem() == CustomItems.scripted_item) {
                        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
                        EventHooks.onScriptItemUpdate((IItemCustom) istack, event.entityLiving);
                    }
                }
            }
        } else if(event.entityLiving.getHeldItem() != null && event.entityLiving.getHeldItem().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
            IItemStack itemStack = NpcAPI.Instance().getIItemStack(event.entityLiving.getHeldItem());
            EventHooks.onScriptItemUpdate((IItemCustom) itemStack, event.entityLiving);
        }
    }

    @SubscribeEvent
    public void invoke(ItemTossEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer) {
            try {
                if (event.entityItem.getEntityItem().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
                    IItemStack isw = NpcAPI.Instance().getIItemStack(event.entityItem.getEntityItem());
                    if(isw instanceof IItemCustom)
                        event.setCanceled(EventHooks.onScriptItemTossed((IItemCustom) isw, event.player, event.entityItem));
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
                    IItemStack isw = NpcAPI.Instance().getIItemStack(event.player.getHeldItem());
                    if(isw instanceof IItemCustom)
                        EventHooks.onScriptItemPickedUp((IItemCustom) isw, event.player);
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
                if (stack.stackSize > 0 && stack.getItem() == CustomItems.scripted_item) {
                    IItemStack isw = NpcAPI.Instance().getIItemStack(stack);
                    if(isw instanceof IItemCustom){
                        if(EventHooks.onScriptItemSpawn((IItemCustom) isw, entity)){
                            event.setCanceled(true);
                        }
                    }
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
                    IItemStack isw = NpcAPI.Instance().getIItemStack(event.entityPlayer.getHeldItem());
                    if(isw instanceof IItemCustom){
                        ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent((IItemCustom) isw, (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer), 2, NpcAPI.Instance().getIEntity(event.target));
                        event.setCanceled(EventHooks.onScriptItemInteract((IItemCustom) isw, eve));

                        ItemEvent.RightClickEvent rightClickEvent = new ItemEvent.RightClickEvent((IItemCustom) isw, (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer), 1,  NpcAPI.Instance().getIEntity(event.target));
                        event.setCanceled(EventHooks.onScriptItemRightClick((IItemCustom) isw, rightClickEvent));
                    }
                }
            } catch(Exception e) {}
        }
    }

    @SubscribeEvent
    public void invoke(PlayerInteractEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null || event.action == null)
            return;

        if(event.entityPlayer.worldObj.isRemote || !(event.entityPlayer.worldObj instanceof WorldServer))
            return;

        if (PlayerDataController.Instance != null) {
            if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR){
                PlayerData handler = PlayerDataController.Instance.getPlayerData(event.entityPlayer);
                if(handler == null)
                    return;

                if(handler.hadInteract) {
                    handler.hadInteract = false;
                    return;
                }
                try {
                    if (event.entityPlayer.getHeldItem().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
                        IItemStack isw = NpcAPI.Instance().getIItemStack(event.entityPlayer.getHeldItem());
                        if(isw instanceof IItemCustom){
                            ItemEvent.RightClickEvent eve = new ItemEvent.RightClickEvent((IItemCustom) isw, (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer), 0, null);
                            event.setCanceled(EventHooks.onScriptItemRightClick((IItemCustom) isw, eve));
                        }
                    }
                } catch(Exception e) {}
            }
            else if(event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK){
                PlayerData handler = PlayerDataController.Instance.getPlayerData(event.entityPlayer);
                if(handler == null)
                    return;

                handler.hadInteract = true;
                try {
                    if (event.entityPlayer.getHeldItem().getItem() == CustomItems.scripted_item && !event.isCanceled()) {
                        IItemStack isw = NpcAPI.Instance().getIItemStack(event.entityPlayer.getHeldItem());
                        if(isw instanceof IItemCustom){
                            ItemEvent.RightClickEvent eve = new ItemEvent.RightClickEvent((IItemCustom) isw, (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer), 2, NpcAPI.Instance().getIBlock((IWorld) NpcAPI.Instance().getIWorld(event.world), event.x, event.y, event.z));
                            event.setCanceled(EventHooks.onScriptItemRightClick((IItemCustom) isw, eve));
                        }
                    }
                } catch(Exception e) {}
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Start event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {

            IItemStack isw = NpcAPI.Instance().getIItemStack(event.item);
            if(isw instanceof IItemCustom){
                IPlayer IPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
                event.setCanceled(EventHooks.onStartUsingCustomItem((IItemCustom) isw, IPlayer, event.duration));
            }
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Tick event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer && event.item.getItem() == CustomItems.scripted_item) {

            IItemStack isw = NpcAPI.Instance().getIItemStack(event.item);
            if(isw instanceof IItemCustom){
                IPlayer IPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
                event.setCanceled(EventHooks.onUsingCustomItem((IItemCustom) isw, IPlayer, event.duration));
            }
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Stop event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer && event.item.getItem() == CustomItems.scripted_item) {
            IItemStack isw = NpcAPI.Instance().getIItemStack(event.item);
            if(isw instanceof IItemCustom){
                IPlayer IPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
                event.setCanceled(EventHooks.onStopUsingCustomItem((IItemCustom) isw, IPlayer, event.duration));
            }
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Finish event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer && event.item.getItem() == CustomItems.scripted_item) {
            IItemStack isw = NpcAPI.Instance().getIItemStack(event.item);
            if(isw instanceof IItemCustom){
                IPlayer IPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
                EventHooks.onFinishUsingCustomItem((IItemCustom) isw, IPlayer, event.duration);
            }
        }
    }
}
