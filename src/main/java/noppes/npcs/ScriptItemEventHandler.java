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
import net.minecraftforge.event.entity.player.*;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemCustomizable;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.ItemEvent;

import java.util.HashMap;

public class ScriptItemEventHandler {
    public ScriptItemEventHandler() {
    }

    @SubscribeEvent
    public void invoke(LivingEvent.LivingUpdateEvent event) {
        if (event.entityLiving == null || event.entityLiving.worldObj == null || event.entityLiving instanceof EntityPlayer || event.entityLiving.ticksExisted % 10 != 0)
            return;

        if (event.entityLiving instanceof EntityCustomNpc) {
            HashMap<Integer, ItemStack> armor = ((EntityCustomNpc) event.entityLiving).inventory.armor;
            HashMap<Integer, ItemStack> weapons = ((EntityCustomNpc) event.entityLiving).inventory.weapons;
            HashMap<Integer, ItemStack>[] inventories = new HashMap[]{armor, weapons};

            for (HashMap<Integer, ItemStack> inventory : inventories) {
                for (ItemStack stack : inventory.values()) {
                    if (stack != null && NoppesUtilServer.isScriptableItem(stack.getItem())) {
                        IItemStack istack = NpcAPI.Instance().getIItemStack(stack);
                        EventHooks.onScriptItemUpdate((IItemCustomizable) istack, event.entityLiving);
                    }
                }
            }
        } else if (event.entityLiving.getHeldItem() != null && NoppesUtilServer.isScriptableItem(event.entityLiving.getHeldItem().getItem()) && !event.isCanceled()) {
            IItemStack itemStack = NpcAPI.Instance().getIItemStack(event.entityLiving.getHeldItem());
            EventHooks.onScriptItemUpdate((IItemCustomizable) itemStack, event.entityLiving);
        }
    }

    @SubscribeEvent
    public void invoke(ItemTossEvent event) {
        if (event.player == null || event.player.worldObj == null)
            return;

        if (event.player.worldObj instanceof WorldServer) {
            try {
                if (NoppesUtilServer.isScriptableItem(event.entityItem.getEntityItem().getItem()) && !event.isCanceled()) {
                    IItemStack isw = NpcAPI.Instance().getIItemStack(event.entityItem.getEntityItem());
                    if (isw instanceof IItemCustomizable)
                        event.setCanceled(EventHooks.onScriptItemTossed((IItemCustomizable) isw, event.player, event.entityItem));
                }
            } catch (Exception e) {
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.ItemPickupEvent event) {
        if (event.player == null || event.player.worldObj == null)
            return;

        if (event.player.worldObj instanceof WorldServer) {
            try {
                if (NoppesUtilServer.isScriptableItem(event.player.getHeldItem().getItem()) && !event.isCanceled()) {
                    IItemStack isw = NpcAPI.Instance().getIItemStack(event.player.getHeldItem());
                    if (isw instanceof IItemCustomizable)
                        EventHooks.onScriptItemPickedUp((IItemCustomizable) isw, event.player);
                }
            } catch (Exception e) {
            }
        }
    }

    @SubscribeEvent
    public void invoke(EntityJoinWorldEvent event) {
        if (!event.world.isRemote && event.entity instanceof EntityItem) {
            EntityItem entity = (EntityItem) event.entity;
            ItemStack stack = entity.getEntityItem();

            try {
                if (stack.stackSize > 0 && NoppesUtilServer.isScriptableItem(stack.getItem())) {
                    IItemStack isw = NpcAPI.Instance().getIItemStack(stack);
                    if (isw instanceof IItemCustomizable) {
                        if (EventHooks.onScriptItemSpawn((IItemCustomizable) isw, entity)) {
                            event.setCanceled(true);
                        }
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @SubscribeEvent
    public void invoke(EntityInteractEvent event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if (!event.entityPlayer.worldObj.isRemote && event.entityPlayer.worldObj instanceof WorldServer) {
            try {
                if (NoppesUtilServer.isScriptableItem(event.entityPlayer.getHeldItem().getItem()) && !event.isCanceled()) {
                    IItemStack isw = NpcAPI.Instance().getIItemStack(event.entityPlayer.getHeldItem());
                    if (isw instanceof IItemCustomizable) {
                        ItemEvent.InteractEvent eve = new ItemEvent.InteractEvent((IItemCustomizable) isw, (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer), 2, NpcAPI.Instance().getIEntity(event.target));
                        event.setCanceled(EventHooks.onScriptItemInteract((IItemCustomizable) isw, eve));

                        ItemEvent.RightClickEvent rightClickEvent = new ItemEvent.RightClickEvent((IItemCustomizable) isw, (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer), 1, NpcAPI.Instance().getIEntity(event.target));
                        event.setCanceled(EventHooks.onScriptItemRightClick((IItemCustomizable) isw, rightClickEvent));
                    }
                }
            } catch (Exception e) {
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlayerInteractEvent event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj == null || event.action == null)
            return;

        if (event.entityPlayer.worldObj.isRemote || !(event.entityPlayer.worldObj instanceof WorldServer))
            return;

        if (PlayerDataController.Instance != null) {
            if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
                PlayerData handler = PlayerData.get(event.entityPlayer);
                if (handler == null)
                    return;

                if (handler.hadInteract) {
                    handler.hadInteract = false;
                    return;
                }
                try {
                    if (NoppesUtilServer.isScriptableItem(event.entityPlayer.getHeldItem().getItem()) && !event.isCanceled()) {
                        IItemStack isw = NpcAPI.Instance().getIItemStack(event.entityPlayer.getHeldItem());
                        if (isw instanceof IItemCustomizable) {
                            ItemEvent.RightClickEvent eve = new ItemEvent.RightClickEvent((IItemCustomizable) isw, (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer), 0, null);
                            event.setCanceled(EventHooks.onScriptItemRightClick((IItemCustomizable) isw, eve));
                        }
                    }
                } catch (Exception e) {
                }
            } else if (event.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
                PlayerData handler = PlayerData.get(event.entityPlayer);
                if (handler == null)
                    return;

                handler.hadInteract = true;
                try {
                    if (NoppesUtilServer.isScriptableItem(event.entityPlayer.getHeldItem().getItem()) && !event.isCanceled()) {
                        IItemStack isw = NpcAPI.Instance().getIItemStack(event.entityPlayer.getHeldItem());
                        if (isw instanceof IItemCustomizable) {
                            ItemEvent.RightClickEvent eve = new ItemEvent.RightClickEvent((IItemCustomizable) isw, (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer), 2, NpcAPI.Instance().getIBlock((IWorld) NpcAPI.Instance().getIWorld(event.world), event.x, event.y, event.z));
                            event.setCanceled(EventHooks.onScriptItemRightClick((IItemCustomizable) isw, eve));
                        }
                    }
                } catch (Exception e) {
                }
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Start event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if (event.entityPlayer.worldObj instanceof WorldServer) {

            IItemStack isw = NpcAPI.Instance().getIItemStack(event.item);
            if (isw instanceof IItemCustomizable) {
                IPlayer IPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
                event.setCanceled(EventHooks.onStartUsingCustomItem((IItemCustomizable) isw, IPlayer, event.duration));
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Tick event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if (event.entityPlayer.worldObj instanceof WorldServer && NoppesUtilServer.isScriptableItem(event.item.getItem())) {

            IItemStack isw = NpcAPI.Instance().getIItemStack(event.item);
            if (isw instanceof IItemCustomizable) {
                IPlayer IPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
                event.setCanceled(EventHooks.onUsingCustomItem((IItemCustomizable) isw, IPlayer, event.duration));
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Stop event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if (event.entityPlayer.worldObj instanceof WorldServer && NoppesUtilServer.isScriptableItem(event.item.getItem())) {
            IItemStack isw = NpcAPI.Instance().getIItemStack(event.item);
            if (isw instanceof IItemCustomizable) {
                IPlayer IPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
                event.setCanceled(EventHooks.onStopUsingCustomItem((IItemCustomizable) isw, IPlayer, event.duration));
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Finish event) {
        if (event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if (event.entityPlayer.worldObj instanceof WorldServer && NoppesUtilServer.isScriptableItem(event.item.getItem())) {
            IItemStack isw = NpcAPI.Instance().getIItemStack(event.item);
            if (isw instanceof IItemCustomizable) {
                IPlayer IPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
                EventHooks.onFinishUsingCustomItem((IItemCustomizable) isw, IPlayer, event.duration);
            }
        }
    }

    @SubscribeEvent
    public void invoke(AnvilRepairEvent repairEvent) {
        IItemStack output = NpcAPI.Instance().getIItemStack(repairEvent.output);
        if (output instanceof IItemCustomizable) {
            IPlayer player = (IPlayer) NpcAPI.Instance().getIEntity(repairEvent.entityPlayer);
            IItemStack left = NpcAPI.Instance().getIItemStack(repairEvent.left);
            IItemStack right = NpcAPI.Instance().getIItemStack(repairEvent.right);
            float breakChance = repairEvent.breakChance;
            EventHooks.onRepairCustomItem((IItemCustomizable) output, player, left, right, breakChance);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerDestroyItemEvent destroyItemEvent) {
        IItemStack itemStack = NpcAPI.Instance().getIItemStack(destroyItemEvent.original);
        if (itemStack instanceof IItemCustomizable) {
            IPlayer player = (IPlayer) NpcAPI.Instance().getIEntity(destroyItemEvent.entityPlayer);
            EventHooks.onBreakCustomItem((IItemCustomizable) itemStack, player);
        }
    }
}
