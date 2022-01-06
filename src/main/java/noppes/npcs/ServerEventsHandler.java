package noppes.npcs;

import java.util.HashMap;
import java.util.List;

import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import net.minecraft.block.Block;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityList;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.passive.EntityVillager;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.init.Blocks;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.util.*;
import net.minecraft.village.MerchantRecipeList;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.EntityInteractEvent;
import net.minecraftforge.event.entity.player.EntityItemPickupEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent.Action;
import net.minecraftforge.event.terraingen.PopulateChunkEvent;
import noppes.npcs.blocks.tiles.TileBanner;
import noppes.npcs.constants.EnumGuiType;
import noppes.npcs.constants.EnumPacketClient;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.constants.EnumRoleType;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.event.FactionGainPointsEvent;
import noppes.npcs.event.NPCDeathEvent;
import noppes.npcs.event.NPCKillEntityEvent;
import noppes.npcs.event.PlayerInteractAtNPCEvent;
import noppes.npcs.items.ItemExcalibur;
import noppes.npcs.items.ItemShield;
import noppes.npcs.items.ItemSoulstoneEmpty;
import noppes.npcs.quests.QuestKill;
import noppes.npcs.roles.RoleFollower;
import noppes.npcs.util.EventBus;

public class ServerEventsHandler {

    private final MinecraftServer server = MinecraftServer.getServer();

    public static EntityVillager Merchant;
    public static Entity mounted;

    @SubscribeEvent
    public void invoke(EntityInteractEvent event) {
        EntityPlayer player = event.entityPlayer;

        ItemStack item = player.getCurrentEquippedItem();

        if (item == null) {
            return;
        }

        boolean isRemote = player.worldObj.isRemote,
          isNPC = event.target instanceof EntityNPCInterface;

        if (
          !isRemote && CustomNpcs.OpsOnly &&
            !server.getConfigurationManager().func_152596_g(player.getGameProfile())
        ) {
            return;
        }

        Item stack = item.getItem();

        if (!isRemote && stack == CustomItems.soulstoneEmpty && event.target instanceof EntityLivingBase) {
            ItemSoulstoneEmpty soulstoneEmpty = (ItemSoulstoneEmpty) stack;

            soulstoneEmpty.store(
              (EntityLivingBase) event.target,
              item,
              player
            );
        }

        if (stack == CustomItems.wand && isNPC && !isRemote) {
            EntityNPCInterface npc = (EntityNPCInterface) event.target;

            PlayerInteractAtNPCEvent call = EventBus.callTo(
              new PlayerInteractAtNPCEvent(
                player,
                npc
              )
            );

            if (!call.isCanceled()) {
                if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.NPC_GUI)) {
                    return;
                }

                event.setCanceled(true);

                NoppesUtilServer.sendOpenGui(player, EnumGuiType.MainMenuDisplay, npc);
            }
        }

        if (stack == CustomItems.cloner && !isRemote && !(event.target instanceof EntityPlayer)) {
            NBTTagCompound compound = new NBTTagCompound();

            if (!event.target.writeToNBTOptional(compound)) {
                return;
            }

            PlayerData data = PlayerDataController.instance.getPlayerData(player);
            ServerCloneController.Instance.cleanTags(compound);

            if (!Server.sendData((EntityPlayerMP) player, EnumPacketClient.CLONE, compound)) {
                player.addChatMessage(new ChatComponentText("Entity too big to clone"));
            }

            data.cloned = compound;

            event.setCanceled(true);
        }

        if (stack == CustomItems.scripter && !isRemote && isNPC) {
            EntityNPCInterface npc = (EntityNPCInterface) event.target;

            PlayerInteractAtNPCEvent call = EventBus.callTo(
              new PlayerInteractAtNPCEvent(
                player,
                npc
              )
            );

            boolean result = call.isCanceled();

            event.setCanceled(
              result
            );

            if (!result) {
                if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.NPC_GUI)) {
                    return;
                }

                NoppesUtilServer.setEditingNpc(player, npc);

                Server.sendData((EntityPlayerMP) player, EnumPacketClient.GUI, EnumGuiType.Script.ordinal());
            }
        }

        if (stack == CustomItems.mount) {
            PlayerInteractAtNPCEvent npcEvent = EventBus.callTo(
              new PlayerInteractAtNPCEvent(
                player,
                (EntityNPCInterface) event.target
              )
            );

            if (!CustomNpcsPermissions.hasPermission(event.entityPlayer, CustomNpcsPermissions.TOOL_MOUNTER)) {
                return;
            }

            event.setCanceled(true);

            mounted = event.target;

            if (isRemote) {
                CustomNpcs.proxy.openGui(
                  MathHelper.floor_double(mounted.posX),
                  MathHelper.floor_double(mounted.posY),
                  MathHelper.floor_double(mounted.posZ),
                  EnumGuiType.MobSpawnerMounter,
                  player);
            }
        }

        if (stack == CustomItems.wand && event.target instanceof EntityVillager) {
            if (!CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_VILLAGER)) {
                return;
            }

            event.setCanceled(true);

            Merchant = (EntityVillager) event.target;

            if (!isRemote) {
                player.openGui(
                  CustomNpcs.instance,
                  EnumGuiType.MerchantAdd.ordinal(),
                  player.worldObj,
                  0,
                  0,
                  0
                );

                MerchantRecipeList merchantrecipelist = Merchant.getRecipes(player);

                if (merchantrecipelist != null) {
                    Server.sendData(
                      (EntityPlayerMP) player,
                      EnumPacketClient.VILLAGER_LIST,
                      merchantrecipelist
                    );
                }
            }

        }
    }

    @SubscribeEvent
    public void invoke(LivingHurtEvent event) {
        if (!(event.entityLiving instanceof EntityPlayer))
            return;

        EntityPlayer player = (EntityPlayer) event.entityLiving;
        if (event.source.isUnblockable() || event.source.isFireDamage())
            return;
        if (!player.isBlocking())
            return;
        ItemStack item = player.getCurrentEquippedItem();
        if (item == null || !(item.getItem() instanceof ItemShield))
            return;
        if (((ItemShield) item.getItem()).material.getDamageVsEntity() < player.getRNG().nextInt(9))
            return;
        float damage = item.getItemDamage() + event.ammount;

        item.damageItem((int) event.ammount, player);

        if (damage > item.getMaxDamage())
            event.ammount = damage - item.getMaxDamage();
        else {
            event.ammount = 0;
            event.setCanceled(true);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerInteractEvent event) {
        EntityPlayer player = event.entityPlayer;
        Block block = player.worldObj.getBlock(event.x, event.y, event.z);
        if (event.action == Action.LEFT_CLICK_BLOCK && player.getHeldItem() != null && player.getHeldItem().getItem() == CustomItems.teleporter) {
            event.setCanceled(true);
        }

        if (block == Blocks.crafting_table && event.action == Action.RIGHT_CLICK_BLOCK && !player.worldObj.isRemote) {
            RecipeController controller = RecipeController.instance;
            NBTTagList list = new NBTTagList();
            int i = 0;
            for (RecipeCarpentry recipe : controller.globalRecipes.values()) {
                list.appendTag(recipe.writeNBT());
                i++;
                if (i % 10 == 0) {
                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setTag("recipes", list);
                    Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNCRECIPES_ADD, compound);
                    list = new NBTTagList();
                }
            }

            if (i % 10 != 0) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setTag("recipes", list);
                Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNCRECIPES_ADD, compound);
            }
            Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNCRECIPES_WORKBENCH);
        }
        if (block == CustomItems.carpentyBench && event.action == Action.RIGHT_CLICK_BLOCK && !player.worldObj.isRemote) {
            RecipeController controller = RecipeController.instance;
            NBTTagList list = new NBTTagList();
            int i = 0;
            for (RecipeCarpentry recipe : controller.anvilRecipes.values()) {
                list.appendTag(recipe.writeNBT());
                i++;
                if (i % 10 == 0) {
                    NBTTagCompound compound = new NBTTagCompound();
                    compound.setTag("recipes", list);
                    Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNCRECIPES_ADD, compound);
                    list = new NBTTagList();
                }
            }

            if (i % 10 != 0) {
                NBTTagCompound compound = new NBTTagCompound();
                compound.setTag("recipes", list);
                Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNCRECIPES_ADD, compound);
            }
            Server.sendData((EntityPlayerMP) player, EnumPacketClient.SYNCRECIPES_CARPENTRYBENCH);
        }
        if ((block == CustomItems.banner || block == CustomItems.wallBanner || block == CustomItems.sign) && event.action == Action.RIGHT_CLICK_BLOCK) {
            ItemStack item = player.inventory.getCurrentItem();
            if (item == null || item.getItem() == null)
                return;
            int y = event.y;
            int meta = player.worldObj.getBlockMetadata(event.x, event.y, event.z);
            if (meta >= 7)
                y--;
            TileBanner tile = (TileBanner) player.worldObj.getTileEntity(event.x, y, event.z);
            if (!tile.canEdit()) {
                if (item.getItem() == CustomItems.wand && CustomNpcsPermissions.hasPermission(player, CustomNpcsPermissions.EDIT_BLOCKS)) {
                    tile.time = System.currentTimeMillis();
                    if (player.worldObj.isRemote)
                        player.addChatComponentMessage(new ChatComponentTranslation("availability.editIcon"));
                }
                return;
            }

            if (!player.worldObj.isRemote) {
                tile.icon = item.copy();
                player.worldObj.markBlockForUpdate(event.x, y, event.z);
                event.setCanceled(true);
            }

        }
    }

    @SubscribeEvent
    public void invoke(LivingDeathEvent event) {
        if (event.entityLiving.worldObj.isRemote) return;

        DamageSource source = event.source;
        EntityLivingBase entity = event.entityLiving;

        Entity attacker = source.getEntity();

        if (attacker instanceof EntityPlayer) {
            doExcalibur(
              (EntityPlayer) attacker,
              (EntityPlayer) attacker
            );
        } else if (attacker instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) event.source.getEntity();

            Line line = npc.advanced.getKillLine();

            if (line != null) {
                npc.saySurrounding(
                  line.formatTarget(entity)
                );
            }

            NPCKillEntityEvent bus = EventBus.callTo(
              new NPCKillEntityEvent(
                (EntityLiving) entity,
                npc
              )
            );

            if (!bus.isCanceled()) {
                npc.script.callScript(EnumScriptType.KILLS, "target", entity);
            }
        }

        if (entity instanceof EntityNPCInterface) {
            EntityNPCInterface npc = (EntityNPCInterface) entity;

            EntityPlayer entityPlayer = attacker instanceof EntityPlayer ? (EntityPlayer) attacker : null;

            if (npc.advanced.role == EnumRoleType.Follower && entityPlayer == null) {
                entityPlayer = ((RoleFollower) npc.roleInterface).owner;
            }

            if (entityPlayer != null) {
                NPCDeathEvent npcDeathEvent = EventBus.callTo(
                  new NPCDeathEvent(
                    npc,
                    entityPlayer
                  )
                );

                if (!npcDeathEvent.isCanceled()) {
                    doQuest(entityPlayer, entity, true);
                    doFactionPoints(entityPlayer, npc);
                }
            }

        }
//
//        if (event.entityLiving instanceof EntityPlayer) {
//            PlayerData data = PlayerDataController.instance.getPlayerData((EntityPlayer) event.entityLiving);
//            data.saveNBTData(null);
//        } Why it's saving?
    }

    private void doExcalibur(EntityPlayer player, EntityLivingBase entity) {
        ItemStack item = player.getCurrentEquippedItem();
        if (item == null || item.getItem() != CustomItems.excalibur)
            return;
        Server.sendData((EntityPlayerMP) player, EnumPacketClient.PLAY_MUSIC, "customnpcs:songs.excalibur");
        player.addChatMessage(new ChatComponentTranslation("<" + StatCollector.translateToLocal(item.getItem().getUnlocalizedName() + ".name") + "> " + ItemExcalibur.quotes[player.getRNG().nextInt(ItemExcalibur.quotes.length)]));
    }

    private void doFactionPoints(EntityPlayer player, EntityNPCInterface npc) {
        FactionOptions options = npc.advanced.factions;

        FactionGainPointsEvent event = EventBus.callTo(
          new FactionGainPointsEvent(
            options,
            player
          )
        );

        if (event.isCanceled()) return;

        options.addPoints(player);
    }

    private void doQuest(EntityPlayer player, EntityLivingBase entity, boolean all) {
        PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(player).questData;
        boolean change = false;
        String entityName = EntityList.getEntityString(entity);

        for (QuestData data : playerdata.activeQuests.values()) {
            if (data.quest.type != EnumQuestType.Kill && data.quest.type != EnumQuestType.AreaKill)
                continue;
            if (data.quest.type == EnumQuestType.AreaKill && all) {
                List<EntityPlayer> list = player.worldObj.getEntitiesWithinAABB(EntityPlayer.class, entity.boundingBox.expand(10, 10, 10));
                for (EntityPlayer pl : list)
                    if (pl != player)
                        doQuest(pl, entity, false);

            }
            String name = entityName;
            QuestKill quest = (QuestKill) data.quest.questInterface;
            if (quest.targets.containsKey(entity.getCommandSenderName()))
                name = entity.getCommandSenderName();
            else if (!quest.targets.containsKey(name))
                continue;
            HashMap<String, Integer> killed = quest.getKilled(data);
            if (killed.containsKey(name) && killed.get(name) >= quest.targets.get(name))
                continue;
            int amount = 0;
            if (killed.containsKey(name))
                amount = killed.get(name);
            killed.put(name, amount + 1);
            quest.setKilled(data, killed);
            change = true;
        }
        if (!change)
            return;

        playerdata.checkQuestCompletion(player, EnumQuestType.Kill);
    }

    @SubscribeEvent
    public void pickUp(EntityItemPickupEvent event) {
        if (event.entityPlayer.worldObj.isRemote)
            return;
        PlayerQuestData playerdata = PlayerDataController.instance.getPlayerData(event.entityPlayer).questData;
        playerdata.checkQuestCompletion(event.entityPlayer, EnumQuestType.Item);
    }

    @SubscribeEvent
    public void world(EntityJoinWorldEvent event) {
        if (event.world.isRemote || !(event.entity instanceof EntityPlayer))
            return;
        PlayerData data = PlayerDataController.instance.getPlayerData((EntityPlayer) event.entity);
        data.updateCompanion(event.world);
    }

    @SubscribeEvent
    public void populateChunk(PopulateChunkEvent.Post event) {
        NPCSpawning.performWorldGenSpawning(event.world, event.chunkX, event.chunkZ, event.rand);
    }
}
