//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs;

import com.google.common.reflect.ClassPath;
import com.google.common.reflect.ClassPath.ClassInfo;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.InputEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.ContainerPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.EntityStruckByLightningEvent;
import net.minecraftforge.event.entity.PlaySoundAtEntityEvent;
import net.minecraftforge.event.entity.item.ItemTossEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.living.LivingFallEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.*;
import net.minecraftforge.event.world.BlockEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.PotentialSpawns;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.controllers.PlayerDataController;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.event.ForgeEvent;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.ScriptController;
import org.lwjgl.input.Mouse;

public class ScriptPlayerEventHandler {
    public ScriptPlayerEventHandler() {
    }
    @SubscribeEvent
    public void onServerTick(TickEvent.PlayerTickEvent event) {
        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.START) {
            EntityPlayer player = event.player;
            PlayerData data = PlayerData.get(player);
            EventHooks.onPlayerTick(data.scriptData);

            if(PlayerDataController.instance != null) {
                PlayerDataController.instance.getPlayerData(player).scriptData = data.scriptData;
                PlayerDataController.instance.getPlayerData(player).timers.update();
            }
        }
    }

    @SubscribeEvent
    public void invoke(EntityInteractEvent event) {
        if(!event.entityPlayer.worldObj.isRemote && event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            noppes.npcs.scripted.event.PlayerEvent.InteractEvent ev = new noppes.npcs.scripted.event.PlayerEvent.InteractEvent(handler.getPlayer(), 1, NpcAPI.Instance().getIEntity(event.target));
            event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
        }
    }

    @SubscribeEvent
    public void invoke(ArrowNockEvent event) {
        if(!event.entityPlayer.worldObj.isRemote && event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            noppes.npcs.scripted.event.PlayerEvent.RangedChargeEvent ev = new noppes.npcs.scripted.event.PlayerEvent.RangedChargeEvent(handler.getPlayer());
            EventHooks.onPlayerBowCharge(handler, ev);
        }
    }

    @SubscribeEvent
    public void invoke(ArrowLooseEvent event) {
        if(!event.entityPlayer.worldObj.isRemote && event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent ev = new noppes.npcs.scripted.event.PlayerEvent.RangedLaunchedEvent(handler.getPlayer(), event.bow, event.charge);
            EventHooks.onPlayerRanged(handler, ev);
        }
    }

    @SubscribeEvent
    public void invoke(BlockEvent.BreakEvent event) {
        if(!event.getPlayer().worldObj.isRemote && event.world instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.getPlayer()).scriptData;
            noppes.npcs.scripted.event.PlayerEvent.BreakEvent ev = new noppes.npcs.scripted.event.PlayerEvent.BreakEvent(handler.getPlayer(), NpcAPI.Instance().getIBlock(event.world, new BlockPos(event.x,event.y,event.z)), event.getExpToDrop());
            event.setCanceled(EventHooks.onPlayerBreak(handler, ev));
            event.setExpToDrop(ev.exp);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Start event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            event.setCanceled(EventHooks.onStartUsingItem(handler, event.duration, event.item));
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Tick event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            event.setCanceled(EventHooks.onUsingItem(handler, event.duration, event.item));
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Stop event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            event.setCanceled(EventHooks.onStopUsingItem(handler, event.duration, event.item));
        }
    }
    @SubscribeEvent
    public void invoke(PlayerUseItemEvent.Finish event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onFinishUsingItem(handler, event.duration, event.item);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerDropsEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            event.setCanceled(EventHooks.onPlayerDropItems(handler, event.drops));
        }
    }

    @SubscribeEvent
    public void invoke(PlayerPickupXpEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerPickupXP(handler, event.orb);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.PlayerChangedDimensionEvent event) {
        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            EventHooks.onPlayerChangeDim(handler, event.fromDim, event.toDim);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.ItemPickupEvent event) {
        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            EventHooks.onPlayerPickUp(handler, event.pickedUp);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerOpenContainerEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer && !(event.entityPlayer.openContainer instanceof ContainerPlayer)) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerContainerOpen(handler, event.entityPlayer.openContainer);
        }
    }

    @SubscribeEvent
    public void invoke(UseHoeEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerUseHoe(handler, event.current, event.x, event.y, event.z);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerSleepInBedEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerSleep(handler, event.x, event.y, event.z);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerWakeUpEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerWakeUp(handler, event.setSpawn);
        }
    }

    @SubscribeEvent
    public void invoke(FillBucketEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerFillBucket(handler, event.current, event.result);
        }
    }

    @SubscribeEvent
    public void invoke(BonemealEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerBonemeal(handler, event.x, event.y, event.z, event.world);
        }
    }

    @SubscribeEvent
    public void invoke(AchievementEvent event) {
        if(event.entityPlayer.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerAchievement(handler, CustomNpcs.proxy.getAchievementDesc(event.achievement));
        }
    }

    @SubscribeEvent
    public void invoke(ItemTossEvent event) {
        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            EventHooks.onPlayerToss(handler, event.entityItem);
        }
    }

    @SubscribeEvent
    public void invoke(LivingFallEvent event) {
        if(event.entityLiving.worldObj instanceof WorldServer) {
            PlayerDataScript handler;
            if (event.entityLiving instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)event.entityLiving).scriptData;
                EventHooks.onPlayerFall(handler, event.distance);
            }
        }
    }

    @SubscribeEvent
    public void invoke(LivingEvent.LivingJumpEvent event) {
        if(event.entityLiving.worldObj instanceof WorldServer) {
            PlayerDataScript handler;
            if (event.entityLiving instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)event.entityLiving).scriptData;
                EventHooks.onPlayerJump(handler);
            }
        }
    }

    @SubscribeEvent
    public void invoke(EntityStruckByLightningEvent event) {
        if(event.entity.worldObj instanceof WorldServer) {
            PlayerDataScript handler;
            if (event.entity instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)event.entity).scriptData;
                EventHooks.onPlayerLightning(handler);
            }
        }
    }

    @SubscribeEvent
    public void invoke(PlaySoundAtEntityEvent event) {
        if(event.entity.worldObj instanceof WorldServer) {
            PlayerDataScript handler;
            if (event.entity instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)event.entity).scriptData;
                EventHooks.onPlayerSound(handler,event.name,event.pitch,event.volume);
            }
        }
    }

    @SubscribeEvent
    public void invoke(LivingDeathEvent event) {
        if(event.entityLiving.worldObj instanceof WorldServer) {
            Entity source = NoppesUtilServer.GetDamageSourcee(event.source);
            PlayerDataScript handler;
            if(event.entityLiving instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)event.entityLiving).scriptData;
                EventHooks.onPlayerDeath(handler, event.source, source);
            }

            if(source instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)source).scriptData;
                EventHooks.onPlayerKills(handler, event.entityLiving);
            }

        }
    }

    @SubscribeEvent
    public void invoke(LivingHurtEvent event) {
        if(event.entityLiving.worldObj instanceof WorldServer) {
            Entity source = NoppesUtilServer.GetDamageSourcee(event.source);
            PlayerDataScript handler;
            if(event.entityLiving instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)event.entityLiving).scriptData;
                noppes.npcs.scripted.event.PlayerEvent.DamagedEvent pevent = new noppes.npcs.scripted.event.PlayerEvent.DamagedEvent(handler.getPlayer(), source, event.ammount, event.source);
                event.setCanceled(EventHooks.onPlayerDamaged(handler, pevent));
                event.ammount = pevent.damage;
            }

            if(source instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)source).scriptData;
                noppes.npcs.scripted.event.PlayerEvent.DamagedEntityEvent pevent1 = new noppes.npcs.scripted.event.PlayerEvent.DamagedEntityEvent(handler.getPlayer(), event.entityLiving, event.ammount, event.source);
                event.setCanceled(EventHooks.onPlayerDamagedEntity(handler, pevent1));
                event.ammount = pevent1.damage;
            }

        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.PlayerRespawnEvent event) {
        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            EventHooks.onPlayerRespawn(handler);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.PlayerLoggedInEvent event) {
        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            EventHooks.onPlayerLogin(handler);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerEvent.PlayerLoggedOutEvent event) {
        if(event.player.worldObj instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            EventHooks.onPlayerLogout(handler);
        }
    }

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void invoke(net.minecraftforge.event.ServerChatEvent event) {
        if(event.player.worldObj instanceof WorldServer && !event.player.equals(EntityNPCInterface.chateventPlayer)) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            String message = event.message;
            noppes.npcs.scripted.event.PlayerEvent.ChatEvent ev = new noppes.npcs.scripted.event.PlayerEvent.ChatEvent(handler.getPlayer(), event.message);
            EventHooks.onPlayerChat(handler, ev);
            event.setCanceled(ev.isCanceled());
            if(!message.equals(ev.message)) {
                ChatComponentTranslation chat = new ChatComponentTranslation("", new Object[0]);
                chat.appendSibling(ForgeHooks.newChatWithLinks(ev.message));
                event.component = chat;
            }
        }
    }

    public ScriptPlayerEventHandler registerForgeEvents() {
        ScriptPlayerEventHandler.ForgeEventHandler handler = new ScriptPlayerEventHandler.ForgeEventHandler();

        try {
            Method e = handler.getClass().getMethod("forgeEntity", new Class[]{Event.class});
            Method register = FMLCommonHandler.instance().bus().getClass().getDeclaredMethod("register", new Class[]{Class.class, Object.class, Method.class, ModContainer.class});
            register.setAccessible(true);
            ArrayList list = new ArrayList();
            list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("cpw.mods.fml.common.gameevent"));
            list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event"));
            Iterator e1 = list.iterator();

            while(true) {
                ClassInfo classLoader;
                String classes;
                do {
                    if(!e1.hasNext()) {
                        if(PixelmonHelper.Enabled) {
                            try {
                                Field e2 = ClassLoader.class.getDeclaredField("classes");
                                e2.setAccessible(true);
                                ClassLoader classLoader1 = Thread.currentThread().getContextClassLoader();
                                ArrayList classes1 = new ArrayList((Vector)e2.get(classLoader1));
                                Iterator infoClass1 = classes1.iterator();

                                while(infoClass1.hasNext()) {
                                    Class c2 = (Class)infoClass1.next();
                                }
                            } catch (Exception var12) {
                                var12.printStackTrace();
                            }

                            return this;
                        }

                        return this;
                    }

                    classLoader = (ClassInfo)e1.next();
                    classes = classLoader.getName();
                } while(classes.startsWith("net.minecraftforge.event.terraingen"));

                Class infoClass = classLoader.load();
                ArrayList c = new ArrayList(Arrays.asList(infoClass.getDeclaredClasses()));
                if(c.isEmpty()) {
                    c.add(infoClass);
                }

                Iterator var10 = c.iterator();
                while(var10.hasNext()) {
                    Class c1 = (Class)var10.next();
                    if(!EntityConstructing.class.isAssignableFrom(c1) && !PotentialSpawns.class.isAssignableFrom(c1) && !TickEvent.RenderTickEvent.class.isAssignableFrom(c1) && !TickEvent.ClientTickEvent.class.isAssignableFrom(c1) && !FMLNetworkEvent.ClientCustomPacketEvent.class.isAssignableFrom(c1) && !ItemTooltipEvent.class.isAssignableFrom(c1) && Event.class.isAssignableFrom(c1) && !Modifier.isAbstract(c1.getModifiers()) && Modifier.isPublic(c1.getModifiers())) {
                        register.invoke(FMLCommonHandler.instance().bus(), new Object[]{c1, handler, e, Loader.instance().activeModContainer()});
                    }
                }
            }
        } catch (Exception var13) {
            var13.printStackTrace();
            return this;
        }
    }

    public class ForgeEventHandler {
        public ForgeEventHandler() {
        }

        @SubscribeEvent
        public void forgeEntity(Event event) {
            if(CustomNpcs.getServer() != null && ScriptController.Instance.forgeScripts.isEnabled()) {
                if(event instanceof EntityEvent) {
                    EntityEvent ev2 = (EntityEvent)event;
                    if(ev2.entity != null && ev2.entity.worldObj instanceof WorldServer) {
                        EventHooks.onForgeEntityEvent(ev2);
                    }
                } else if(event instanceof WorldEvent) {
                    WorldEvent ev1 = (WorldEvent)event;
                    if(ev1.world instanceof WorldServer) {
                        EventHooks.onForgeWorldEvent(ev1);
                    }
                } else if(!(event instanceof TickEvent) || ((TickEvent)event).side != Side.CLIENT) {
                    if(event instanceof PlayerEvent) {
                        PlayerEvent ev = (PlayerEvent)event;
                        if(!(ev.player.worldObj instanceof WorldServer)) {
                            return;
                        }
                    }

                    EventHooks.onForgeEvent(new ForgeEvent(event), event);
                }
            }
        }
    }
}