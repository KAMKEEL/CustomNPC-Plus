package noppes.npcs;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.item.EntityItem;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.tileentity.TileEntity;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.AnvilRepairEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.PlayerDestroyItemEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerUseItemEvent;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemCustomizable;
import noppes.npcs.api.item.IItemStack;
import noppes.npcs.blocks.BlockBanner;
import noppes.npcs.blocks.BlockTallLamp;
import noppes.npcs.blocks.tiles.TileColorable;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerData;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.items.ItemNpcTool;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.ItemEvent;

import java.util.HashMap;

public class ScriptItemEventHandler {
    public ScriptItemEventHandler() {
    }

    @SubscribeEvent
    public void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
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
    public void onItemToss(ItemTossEvent e) {
        if (!isValidContext(e.player) || e.isCanceled())
            return;

        if (e.entityItem == null)
            return;

        EntityItem entityItem = e.entityItem;
        IItemCustomizable c = getCustomizable(entityItem.getEntityItem());
        if (c != null) {
            e.setCanceled(EventHooks.onScriptItemTossed(c, e.player, entityItem));
        }
    }


    @SubscribeEvent
    public void onItemPickup(PlayerEvent.ItemPickupEvent e) {
        if (!isValidContext(e.player) || e.isCanceled())
            return;

        IItemCustomizable c = getCustomizable(e.player.getHeldItem());
        if (c != null) {
            EventHooks.onScriptItemPickedUp(c, e.player);
        }
    }

    @SubscribeEvent
    public void onEntityJoinWorld(EntityJoinWorldEvent e) {
        if (e.world.isRemote || !(e.entity instanceof EntityItem))
            return;

        EntityItem itemEnt = (EntityItem) e.entity;
        IItemCustomizable c = getCustomizable(itemEnt.getEntityItem());
        if (c != null) {
            e.setCanceled(EventHooks.onScriptItemSpawn(c, itemEnt));
        }
    }

    @SubscribeEvent
    public void onEntityInteract(EntityInteractEvent e) {
        EntityPlayer player = e.entityPlayer;
        if (!isValidContext(player) || e.isCanceled())
            return;

        IItemCustomizable c = getCustomizable(player.getHeldItem());
        IPlayer ip = NoppesUtilServer.getIPlayer(player);
        if (c != null && ip != null) {
            boolean cancelA = EventHooks.onScriptItemInteract(
                c, new ItemEvent.InteractEvent(c, ip, 2, NpcAPI.Instance().getIEntity(e.target))
            );
            boolean cancelB = EventHooks.onScriptItemRightClick(
                c, new ItemEvent.RightClickEvent(c, ip, 1, NpcAPI.Instance().getIEntity(e.target))
            );
            if (cancelA || cancelB) {
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onPlayerInteract(PlayerInteractEvent e) {
        EntityPlayer player = e.entityPlayer;
        if (player == null || e.action == null) return;

        if (!isValidContext(e.entityPlayer))
            return;

        // Paintbrush on LEFT_CLICK_BLOCK
        if (e.action == PlayerInteractEvent.Action.LEFT_CLICK_BLOCK) {
            ItemStack held = player.getHeldItem();
            if (ItemNpcTool.isPaintbrush(held)
                && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.NPC_BUILD)) {

                int x = e.x;
                int y = e.y;
                int z = e.z;

                Block block = player.worldObj.getBlock(x, y, z);
                if (block instanceof BlockTallLamp || block instanceof BlockBanner) {
                    int meta = player.worldObj.getBlockMetadata(x, y, z);
                    if (meta >= 7)
                        y--;
                }

                TileEntity tile = player.worldObj.getTileEntity(x, y, z);
                if (tile instanceof TileColorable) {
                    int color = ((TileColorable) tile).color;
                    if (!held.hasTagCompound()) {
                        held.setTagCompound(new NBTTagCompound());
                    }

                    ItemNpcTool.setColor(held.getTagCompound(), color);
                    e.setCanceled(true);
                    return;
                }
            }
        }

        // Scriptable-item RIGHT_CLICK hooks
        if (PlayerDataController.Instance == null) {
            return;
        }
        PlayerData pd = PlayerData.get(player);
        if (pd == null) {
            return;
        }

        ItemStack held = player.getHeldItem();
        IItemCustomizable c = getCustomizable(held);
        IPlayer ip = NoppesUtilServer.getIPlayer(player);
        if (c == null || ip == null) {
            return;
        }

        if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_AIR) {
            if (pd.hadInteract) {
                pd.hadInteract = false;
                return;
            }
            if (EventHooks.onScriptItemRightClick(
                c, new ItemEvent.RightClickEvent(c, ip, 0, null))) {
                e.setCanceled(true);
            }

        } else if (e.action == PlayerInteractEvent.Action.RIGHT_CLICK_BLOCK) {
            pd.hadInteract = true;
            IWorld iw = (IWorld) NpcAPI.Instance().getIWorld(e.world);
            Object blockCtx = NpcAPI.Instance().getIBlock(iw, e.x, e.y, e.z);
            if (EventHooks.onScriptItemRightClick(
                c, new ItemEvent.RightClickEvent(c, ip, 2, blockCtx))) {
                e.setCanceled(true);
            }
        }
    }

    @SubscribeEvent
    public void onStartUseItem(PlayerUseItemEvent.Start e) {
        if (!isValidContext(e.entityPlayer))
            return;

        IItemCustomizable c = getCustomizable(e.item);
        IPlayer ip = NoppesUtilServer.getIPlayer(e.entityPlayer);
        if (c != null && ip != null && EventHooks.onStartUsingCustomItem(c, ip, e.duration)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onUseItemTick(PlayerUseItemEvent.Tick e) {
        if (!isValidContext(e.entityPlayer))
            return;

        IItemCustomizable c = getCustomizable(e.item);
        IPlayer ip = NoppesUtilServer.getIPlayer(e.entityPlayer);
        if (c != null && ip != null && EventHooks.onUsingCustomItem(c, ip, e.duration)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onStopUseItem(PlayerUseItemEvent.Stop e) {
        if (!isValidContext(e.entityPlayer))
            return;

        IItemCustomizable c = getCustomizable(e.item);
        IPlayer ip = NoppesUtilServer.getIPlayer(e.entityPlayer);
        if (c != null && ip != null
            && EventHooks.onStopUsingCustomItem(c, ip, e.duration)) {
            e.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void onFinishUseItem(PlayerUseItemEvent.Finish e) {
        if (!isValidContext(e.entityPlayer))
            return;

        IItemCustomizable c = getCustomizable(e.item);
        IPlayer ip = NoppesUtilServer.getIPlayer(e.entityPlayer);
        if (c != null && ip != null) {
            EventHooks.onFinishUsingCustomItem(c, ip, e.duration);
        }
    }

    @SubscribeEvent
    public void onAnvilRepair(AnvilRepairEvent e) {
        if (!isValidContext(e.entityPlayer))
            return;

        if (e.output == null)
            return;

        IItemStack out = NpcAPI.Instance().getIItemStack(e.output);
        if (out instanceof IItemCustomizable) {
            IPlayer ip = NoppesUtilServer.getIPlayer(e.entityPlayer);
            IItemStack left = NpcAPI.Instance().getIItemStack(e.left);
            IItemStack right = NpcAPI.Instance().getIItemStack(e.right);
            EventHooks.onRepairCustomItem((IItemCustomizable) out, ip, left, right, e.breakChance);
        }
    }

    @SubscribeEvent
    public void onDestroyItem(PlayerDestroyItemEvent e) {
        if (!isValidContext(e.entityPlayer))
            return;

        if (e.original == null)
            return;

        IItemStack orig = NpcAPI.Instance().getIItemStack(e.original);
        if (orig instanceof IItemCustomizable) {
            IPlayer ip = NoppesUtilServer.getIPlayer(e.entityPlayer);
            EventHooks.onBreakCustomItem((IItemCustomizable) orig, ip);
        }
    }

    private boolean isValidContext(EntityPlayer player) {
        return player != null
            && player.worldObj instanceof WorldServer
            && !player.worldObj.isRemote
            && !(player instanceof FakePlayer);
    }

    private IItemCustomizable getCustomizable(ItemStack stack) {
        if (stack == null || stack.getItem() == null || !NoppesUtilServer.isScriptableItem(stack.getItem())) {
            return null;
        }

        IItemStack raw = NpcAPI.Instance().getIItemStack(stack);
        return (raw instanceof IItemCustomizable) ? (IItemCustomizable) raw : null;
    }
}
