package noppes.npcs;

import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.relauncher.Side;
import kamkeel.npcs.controllers.SyncController;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.item.ItemStack;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.util.FakePlayer;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.*;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import noppes.npcs.api.entity.IPlayer;
import noppes.npcs.api.item.IItemCustomizable;
import kamkeel.npcs.controllers.AttributeController;
import kamkeel.npcs.util.AttributeAttackUtil;
import noppes.npcs.config.ConfigMain;
import noppes.npcs.constants.EnumQuestType;
import noppes.npcs.controllers.CustomEffectController;
import noppes.npcs.controllers.PartyController;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.*;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.items.ItemNpcTool;
import noppes.npcs.quests.QuestItem;
import noppes.npcs.scripted.NpcAPI;

import static noppes.npcs.config.ConfigMain.TrackedQuestUpdateFrequency;

public class ScriptPlayerEventHandler {
    public ScriptPlayerEventHandler() {
    }
    @SubscribeEvent
    public void onServerTick(TickEvent.PlayerTickEvent event) {
        if (event.player == null || event.player.worldObj == null)
            return;

        if (event.side == Side.SERVER && event.phase == TickEvent.Phase.START) {
            EntityPlayer player = event.player;

            if (player.ticksExisted % 10 == 0) {
                PlayerDataScript handler = ScriptController.Instance.playerScripts;
                IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(player);
                EventHooks.onPlayerTick(handler, scriptPlayer);

                for(int i = 0; i < player.inventory.getSizeInventory(); i++){
                    ItemStack item = player.inventory.getStackInSlot(i);
                    if(item != null && NoppesUtilServer.isScriptableItem(item.getItem())){
                        IItemCustomizable isw = (IItemCustomizable) NpcAPI.Instance().getIItemStack(item);
                        EventHooks.onScriptItemUpdate(isw, player);
                    }
                }

                if(ConfigMain.AttributesEnabled)
                    AttributeController.getTracker(player).updateIfChanged(player);
            }

            if (PlayerDataController.Instance != null) {
                PlayerData playerData = PlayerDataController.Instance.getPlayerData(player);

                if(playerData.updateClient) {
                    SyncController.syncPlayerData((EntityPlayerMP)player, true);
                    playerData.updateClient = false;
                }

                if (playerData.timers.size() > 0) {
                    playerData.timers.update();
                }

                if(player.ticksExisted % 10 == 0)
                    SyncController.syncEffects((EntityPlayerMP) player);

                // TODO: Fix Config
                // player.ticksExisted % ConfigDBCGameplay.CheckEffectsTick == 0
                if (player.ticksExisted % 10 == 0)
                    CustomEffectController.Instance.runEffects(player);

                if (player.ticksExisted % 20 == 0)
                    CustomEffectController.Instance.decrementEffects(player);

                Party party = playerData.getPlayerParty();
                boolean trackingPartyQuest = playerData.questData.getTrackedQuest() != null && party != null && party.getQuest() != null && party.getQuest().getId() == playerData.questData.getTrackedQuest().getId();
                if (playerData.questData.getTrackedQuest() != null && !playerData.questData.activeQuests.containsKey(playerData.questData.getTrackedQuest().getId()) && !trackingPartyQuest) {
                    PlayerDataController.Instance.getPlayerData(player).questData.untrackQuest();
                }

                if (player.ticksExisted % 20 == 0 && !PlayerDataController.Instance.getPlayerData(player).skinOverlays.overlayList.isEmpty()) {
                    PlayerDataController.Instance.getPlayerData(player).skinOverlays.updateClient();
                }

                if (player.ticksExisted % (TrackedQuestUpdateFrequency * 20) == 0) {
                    PlayerQuestData questData = playerData.questData;
                    if(questData != null){
                        if (questData.getTrackedQuest() != null && ((Quest) questData.getTrackedQuest()).type == EnumQuestType.Item) {
                            if(trackingPartyQuest){
                                NoppesUtilPlayer.sendPartyTrackedQuestData((EntityPlayerMP) event.player, party);
                            }
                            else {
                                NoppesUtilPlayer.sendTrackedQuestData((EntityPlayerMP) event.player);
                            }
                        }
                    }
                }
            }
        }
    }

    @SubscribeEvent
    public void invoke(EntityInteractEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(!event.entityPlayer.worldObj.isRemote && event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            noppes.npcs.scripted.event.PlayerEvent.InteractEvent ev = new noppes.npcs.scripted.event.PlayerEvent.InteractEvent(scriptPlayer, 1, NpcAPI.Instance().getIEntity(event.target));
            event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
        }
    }

    @SubscribeEvent
    public void invoke(ArrowNockEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(!event.entityPlayer.worldObj.isRemote && event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            noppes.npcs.scripted.event.PlayerEvent.RangedChargeEvent ev = new noppes.npcs.scripted.event.PlayerEvent.RangedChargeEvent(scriptPlayer);
            EventHooks.onPlayerBowCharge(handler, ev);
        }
    }

    @SubscribeEvent
    public void invoke(ArrowLooseEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(!event.entityPlayer.worldObj.isRemote && event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent ev = new noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent(scriptPlayer, event.bow, event.charge);
            EventHooks.onPlayerRanged(handler, ev);
        }
    }

    @SubscribeEvent
    public void invoke(BlockEvent.BreakEvent event) {
        if(event.getPlayer() == null || event.getPlayer().worldObj == null)
            return;

        if(event.getPlayer().getHeldItem() != null && event.getPlayer().getHeldItem().getItem() instanceof ItemNpcTool){
            event.setCanceled(true);
            return;
        }

        if(!event.getPlayer().worldObj.isRemote && event.world instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.getPlayer());
            noppes.npcs.scripted.event.PlayerEvent.BreakEvent ev = new noppes.npcs.scripted.event.PlayerEvent.BreakEvent(scriptPlayer, NpcAPI.Instance().getIBlock(NpcAPI.Instance().getIWorld(event.world), NpcAPI.Instance().getIPos(event.x,event.y,event.z)), event.getExpToDrop());
            event.setCanceled(EventHooks.onPlayerBreak(handler, ev));
            event.setExpToDrop(ev.exp);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Start event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            event.setCanceled(EventHooks.onStartUsingItem(handler, scriptPlayer, event.duration, event.item));
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Tick event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            event.setCanceled(EventHooks.onUsingItem(handler, scriptPlayer, event.duration, event.item));
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Stop event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            event.setCanceled(EventHooks.onStopUsingItem(handler, scriptPlayer, event.duration, event.item));
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Finish event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onFinishUsingItem(handler, scriptPlayer, event.duration, event.item);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerDropsEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            event.setCanceled(EventHooks.onPlayerDropItems(handler, scriptPlayer, event.drops));
        }
    }

    @SubscribeEvent
    public void invoke(PlayerPickupXpEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onPlayerPickupXP(handler, scriptPlayer, event.orb);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.player);
            EventHooks.onPlayerChangeDim(handler, scriptPlayer, event.fromDim, event.toDim);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.ItemPickupEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(!event.player.worldObj.isRemote && !(event.player instanceof FakePlayer)){
            PlayerData playerData = PlayerDataController.Instance.getPlayerData(event.player);
            PlayerQuestData questData = playerData.questData;
            QuestItem.pickedUp = event.pickedUp.getEntityItem();

            Party playerParty = playerData.getPlayerParty();
            if(playerParty != null){
                QuestItem.pickedUpParty = event.pickedUp.getEntityItem();
                QuestItem.pickedUpPlayer = event.player;
                PartyController.Instance().checkQuestCompletion(playerParty, EnumQuestType.Item);
            }

            questData.checkQuestCompletion(playerData, EnumQuestType.Item);
        }

        if(event.player.worldObj instanceof WorldServer && event.player instanceof EntityPlayer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.player);
            EventHooks.onPlayerPickUp(handler, scriptPlayer, event.pickedUp);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerOpenContainerEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer && !(event.entityPlayer.openContainer instanceof ContainerPlayer)) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onPlayerContainerOpen(handler, scriptPlayer, event.entityPlayer.openContainer);
        }
    }

    @SubscribeEvent
    public void invoke(UseHoeEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onPlayerUseHoe(handler, scriptPlayer, event.current, event.x, event.y, event.z);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerSleepInBedEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onPlayerSleep(handler, scriptPlayer, event.x, event.y, event.z);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerWakeUpEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onPlayerWakeUp(handler, scriptPlayer, event.setSpawn);
        }
    }

    @SubscribeEvent
    public void invoke(FillBucketEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onPlayerFillBucket(handler, scriptPlayer, event.current, event.result);
        }
    }

    @SubscribeEvent
    public void invoke(BonemealEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onPlayerBonemeal(handler, scriptPlayer, event.x, event.y, event.z, event.world);
        }
    }

    @SubscribeEvent
    public void invoke(AchievementEvent event) {
        if(event.entityPlayer == null || event.entityPlayer.worldObj == null)
            return;

        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityPlayer);
            EventHooks.onPlayerAchievement(handler, scriptPlayer, CustomNpcs.proxy.getAchievementDesc(event.achievement));
        }
    }

    @SubscribeEvent
    public void invoke(ItemTossEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.player);
            event.setCanceled(EventHooks.onPlayerToss(handler, scriptPlayer, event.entityItem));
        }
    }

    @SubscribeEvent
    public void invoke(LivingFallEvent event) {
        if(event.entityLiving == null || event.entityLiving.worldObj == null)
            return;

        if(event.entityLiving.worldObj instanceof WorldServer) {
            if (event.entityLiving instanceof EntityPlayer) {
                PlayerDataScript handler = ScriptController.Instance.playerScripts;
                IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityLiving);
                event.setCanceled(EventHooks.onPlayerFall(handler, scriptPlayer, event.distance));
            }
        }
    }

    @SubscribeEvent
    public void invoke(LivingEvent.LivingJumpEvent event) {
        if(event.entityLiving == null || event.entityLiving.worldObj == null)
            return;

        if(event.entityLiving.worldObj instanceof WorldServer) {
            if (event.entityLiving instanceof EntityPlayer) {
                PlayerDataScript handler = ScriptController.Instance.playerScripts;
                IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityLiving);
                EventHooks.onPlayerJump(handler, scriptPlayer);
            }
        }
    }

    @SubscribeEvent
    public void invoke(EntityStruckByLightningEvent event) {
        if(event.entity == null || event.entity.worldObj == null)
            return;

        if(event.entity.worldObj instanceof WorldServer) {
            if (event.entity instanceof EntityPlayer) {
                PlayerDataScript handler = ScriptController.Instance.playerScripts;
                IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entity);
                EventHooks.onPlayerLightning(handler, scriptPlayer);
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlaySoundAtEntityEvent event) {
        if(event.entity == null || event.entity.worldObj == null)
            return;

        if(event.entity.worldObj instanceof WorldServer) {
            if (event.entity instanceof EntityPlayer) {
                PlayerDataScript handler = ScriptController.Instance.playerScripts;
                IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entity);
                EventHooks.onPlayerSound(handler,scriptPlayer,event.name,event.pitch,event.volume);
            }
        }
    }

    @SubscribeEvent
    public void invoke(LivingDeathEvent event) {
        if(event.entityLiving == null || event.entityLiving.worldObj == null)
            return;

        if(event.entityLiving.worldObj instanceof WorldServer) {
            Entity source = NoppesUtilServer.GetDamageSource(event.source);
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            if(event.entityLiving instanceof EntityPlayer) {
                try {
                    CustomEffectController.getInstance().killEffects((EntityPlayer) event.entityLiving);

                    IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.entityLiving);
                    EventHooks.onPlayerDeath(handler,scriptPlayer, event.source, source);
                } catch (Exception ignored) {}
            }

            if(event.source.getEntity() instanceof EntityPlayer) {
                IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.source.getEntity());
                EventHooks.onPlayerKills(handler,scriptPlayer, event.entityLiving);
            }

        }
    }

    @SubscribeEvent
    public void invoke(LivingAttackEvent event) {
        if(event.entityLiving == null || event.entityLiving.worldObj == null)
            return;

        if(event.entityLiving.worldObj instanceof WorldServer) {
            boolean cancel = event.isCanceled();
            Entity source = NoppesUtilServer.GetDamageSource(event.source);
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            if(event.entityLiving instanceof EntityPlayer) {
                noppes.npcs.scripted.event.PlayerEvent.AttackedEvent pevent = new noppes.npcs.scripted.event.PlayerEvent.AttackedEvent((IPlayer)NpcAPI.Instance().getIEntity((EntityPlayer)event.entityLiving), source, event.ammount, event.source);
                cancel = EventHooks.onPlayerAttacked(handler, pevent);
            }

            if(event.source.getEntity() instanceof EntityPlayer) {
                float attackAmount = event.ammount;
                if(ConfigMain.AttributesEnabled)
                    attackAmount = AttributeAttackUtil.calculateOutgoing((EntityPlayer) event.source.getEntity(), event.ammount);

                noppes.npcs.scripted.event.PlayerEvent.AttackEvent pevent1 = new noppes.npcs.scripted.event.PlayerEvent.AttackEvent((IPlayer)NpcAPI.Instance().getIEntity((EntityPlayer)event.source.getEntity()), event.entityLiving, attackAmount, event.source);
                cancel = cancel || EventHooks.onPlayerAttack(handler, pevent1);
            }
            event.setCanceled(cancel);
        }
    }

    @SubscribeEvent
    public void invoke(LivingHurtEvent event) {
        if(event.entityLiving == null || event.entityLiving.worldObj == null)
            return;

        if(event.entityLiving.worldObj instanceof WorldServer) {
            boolean cancel = event.isCanceled();
            Entity source = NoppesUtilServer.GetDamageSource(event.source);
            PlayerDataScript handler = ScriptController.Instance.playerScripts;

            // Player to Player Interaction
            if(ConfigMain.AttributesEnabled){
                if(event.entityLiving instanceof EntityPlayer && source instanceof EntityPlayer){
                    event.ammount = AttributeAttackUtil.calculateDamagePlayerToPlayer((EntityPlayer) event.entityLiving, (EntityPlayer) source, event.ammount);
                } else if (!(event.entityLiving instanceof EntityNPCInterface) && source instanceof EntityPlayer){
                    event.ammount = AttributeAttackUtil.calculateOutgoing((EntityPlayer) source, event.ammount);
                }
            }

            if(event.entityLiving instanceof EntityPlayer) {
                noppes.npcs.scripted.event.PlayerEvent.DamagedEvent pevent = new noppes.npcs.scripted.event.PlayerEvent.DamagedEvent((IPlayer)NpcAPI.Instance().getIEntity((EntityPlayer)event.entityLiving), source, event.ammount, event.source);
                cancel = EventHooks.onPlayerDamaged(handler, pevent);
                event.ammount = pevent.damage;
            }

            if(source instanceof EntityPlayer) {
                noppes.npcs.scripted.event.PlayerEvent.DamagedEntityEvent pevent1 = new noppes.npcs.scripted.event.PlayerEvent.DamagedEntityEvent((IPlayer)NpcAPI.Instance().getIEntity(source), event.entityLiving, event.ammount, event.source);
                cancel = cancel || EventHooks.onPlayerDamagedEntity(handler, pevent1);
                event.ammount = pevent1.damage;
            }

            event.setCanceled(cancel);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.PlayerRespawnEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.player);
            EventHooks.onPlayerRespawn(handler, scriptPlayer);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.player);
            EventHooks.onPlayerLogin(handler, scriptPlayer);

            PlayerData playerData =  PlayerDataController.Instance.getPlayerData(event.player);
            Quest quest = (Quest) playerData.questData.getTrackedQuest();
            if (quest != null) {
                Party party = playerData.getPlayerParty();
                if(party != null && party.getQuest() != null && party.getQuest().getId() == quest.getId()){
                    NoppesUtilPlayer.sendPartyTrackedQuestData((EntityPlayerMP) event.player, party);
                }
                else {
                    NoppesUtilPlayer.sendTrackedQuestData((EntityPlayerMP) event.player);
                }
            }

            PlayerDataController.Instance.getPlayerData(event.player).skinOverlays.updateClient();
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.player);
            EventHooks.onPlayerLogout(handler, scriptPlayer);
        }
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void invoke(net.minecraftforge.event.ServerChatEvent event) {
        if(event.player == null || event.player.worldObj == null)
            return;

        if(event.player.worldObj instanceof WorldServer && !event.player.equals(EntityNPCInterface.chateventPlayer)) {
            PlayerDataScript handler = ScriptController.Instance.playerScripts;
            IPlayer scriptPlayer = (IPlayer) NpcAPI.Instance().getIEntity(event.player);
            String message = event.message;
            noppes.npcs.scripted.event.PlayerEvent.ChatEvent ev = new noppes.npcs.scripted.event.PlayerEvent.ChatEvent(scriptPlayer, event.message);
            EventHooks.onPlayerChat(handler, ev);
            event.setCanceled(ev.isCanceled());
            if(!message.equals(ev.message)) {
                ChatComponentTranslation chat = new ChatComponentTranslation("", new Object[0]);
                chat.appendSibling(ForgeHooks.newChatWithLinks(ev.message));
                event.component = chat;
            }
        }
    }
}
