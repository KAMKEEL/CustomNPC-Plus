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

import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.EventPriority;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.util.ChatComponentTranslation;
import net.minecraft.world.WorldServer;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.ServerChatEvent;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.EntityEvent.EntityConstructing;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.WorldEvent;
import net.minecraftforge.event.world.WorldEvent.PotentialSpawns;
import noppes.npcs.controllers.PlayerData;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.event.ForgeEvent;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.ScriptController;

public class ScriptPlayerEventHandler {
    public ScriptPlayerEventHandler() {
    }
    @SubscribeEvent
    public void onServerTick(TickEvent.PlayerTickEvent event) {
        if(event.side == Side.SERVER && event.phase == TickEvent.Phase.START) {
            EntityPlayer player = event.player;
            PlayerData data = PlayerData.get(player);
            if(player.ticksExisted % 10 == 0) {
                EventHooks.onPlayerTick(data.scriptData);
            }

            if(data.playerLevel != player.experienceLevel) {
                EventHooks.onPlayerLevelUp(data.scriptData, data.playerLevel - player.experienceLevel);
                data.playerLevel = player.experienceLevel;
            }

            data.timers.update();
        }
    }
    /*
    @SubscribeEvent
    public void invoke(LeftClickBlock event) {
        if(!event.entityPlayer.field_70170_p.field_72995_K && event.getWorld() instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            AttackEvent ev = new AttackEvent(handler.getPlayer(), 2, NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()));
            event.setCanceled(EventHooks.onPlayerAttack(handler, ev));
            if(event.getItemStack().func_77973_b() == CustomItems.scripted_item && !event.isCanceled()) {
                ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
                noppes.npcs.api.event.ItemEvent.AttackEvent eve = new noppes.npcs.api.event.ItemEvent.AttackEvent(isw, handler.getPlayer(), 2, NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()));
                eve.setCanceled(event.isCanceled());
                event.setCanceled(EventHooks.onScriptItemAttack(isw, eve));
            }

        }
    }

    @SubscribeEvent
    public void invoke(RightClickBlock event) {
        if(!event.entityPlayer.field_70170_p.field_72995_K && event.getWorld() instanceof WorldServer) {
            if(event.getItemStack().func_77973_b() == CustomItems.nbt_book) {
                ((ItemNbtBook)event.getItemStack().func_77973_b()).blockEvent(event);
                event.setCanceled(true);
            } else {
                PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
                handler.hadInteract = true;
                InteractEvent ev = new InteractEvent(handler.getPlayer(), 2, NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()));
                event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
                if(event.getItemStack().func_77973_b() == CustomItems.scripted_item && !event.isCanceled()) {
                    ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
                    noppes.npcs.api.event.ItemEvent.InteractEvent eve = new noppes.npcs.api.event.ItemEvent.InteractEvent(isw, handler.getPlayer(), 2, NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()));
                    event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
                }

            }
        }
    }

    @SubscribeEvent
    public void invoke(EntityInteractEvent event) {
        if(!event.entityPlayer.field_70170_p.field_72995_K && event.getWorld() instanceof WorldServer) {
            if(event.getItemStack().func_77973_b() == CustomItems.nbt_book) {
                ((ItemNbtBook)event.getItemStack().func_77973_b()).entityEvent(event);
                event.setCanceled(true);
            } else {
                PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
                InteractEvent ev = new InteractEvent(handler.getPlayer(), 1, NpcAPI.Instance().getIEntity(event.getTarget()));
                event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
                if(event.getItemStack().func_77973_b() == CustomItems.scripted_item && !event.isCanceled()) {
                    ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
                    noppes.npcs.api.event.ItemEvent.InteractEvent eve = new noppes.npcs.api.event.ItemEvent.InteractEvent(isw, handler.getPlayer(), 1, NpcAPI.Instance().getIEntity(event.getTarget()));
                    event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
                }

            }
        }
    }

    @SubscribeEvent
    public void invoke(RightClickItem event) {
        if(!event.entityPlayer.field_70170_p.field_72995_K && event.getWorld() instanceof WorldServer) {
            if(event.entityPlayer.func_184812_l_() && event.entityPlayer.func_70093_af() && event.getItemStack().func_77973_b() == CustomItems.scripted_item) {
                NoppesUtilServer.sendOpenGui(event.entityPlayer, EnumGuiType.ScriptItem, (EntityNPCInterface)null);
            } else {
                PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
                if(handler.hadInteract) {
                    handler.hadInteract = false;
                } else {
                    InteractEvent ev = new InteractEvent(handler.getPlayer(), 0, (Object)null);
                    event.setCanceled(EventHooks.onPlayerInteract(handler, ev));
                    if(event.getItemStack().func_77973_b() == CustomItems.scripted_item && !event.isCanceled()) {
                        ItemScriptedWrapper isw = ItemScripted.GetWrapper(event.getItemStack());
                        noppes.npcs.api.event.ItemEvent.InteractEvent eve = new noppes.npcs.api.event.ItemEvent.InteractEvent(isw, handler.getPlayer(), 0, (Object)null);
                        event.setCanceled(EventHooks.onScriptItemInteract(isw, eve));
                    }

                }
            }
        }
    }

    @SubscribeEvent
    public void invoke(ArrowLooseEvent event) {
        if(!event.entityPlayer.field_70170_p.field_72995_K && event.getWorld() instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            RangedLaunchedEvent ev = new RangedLaunchedEvent(handler.getPlayer());
            event.setCanceled(EventHooks.onPlayerRanged(handler, ev));
        }
    }

    @SubscribeEvent
    public void invoke(BreakEvent event) {
        if(!event.getPlayer().field_70170_p.field_72995_K && event.getWorld() instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.getPlayer()).scriptData;
            noppes.npcs.api.event.PlayerEvent.BreakEvent ev = new noppes.npcs.api.event.PlayerEvent.BreakEvent(handler.getPlayer(), NpcAPI.Instance().getIBlock(event.getWorld(), event.getPos()), event.getExpToDrop());
            event.setCanceled(EventHooks.onPlayerBreak(handler, ev));
            event.setExpToDrop(ev.exp);
        }
    }

    @SubscribeEvent
    public void invoke(ItemTossEvent event) {
        if(event.getPlayer().field_70170_p instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.getPlayer()).scriptData;
            event.setCanceled(EventHooks.onPlayerToss(handler, event.getEntityItem()));
        }
    }

    @SubscribeEvent
    public void invoke(EntityItemPickupEvent event) {
        if(event.entityPlayer.field_70170_p instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            event.setCanceled(EventHooks.onPlayerPickUp(handler, event.getItem()));
        }
    }

    @SubscribeEvent
    public void invoke(Open event) {
        if(event.entityPlayer.field_70170_p instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerContainerOpen(handler, event.getContainer());
        }
    }

    @SubscribeEvent
    public void invoke(Close event) {
        if(event.entityPlayer.field_70170_p instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.entityPlayer).scriptData;
            EventHooks.onPlayerContainerClose(handler, event.getContainer());
        }
    }

    @SubscribeEvent
    public void invoke(LivingDeathEvent event) {
        if(event.getEntityLiving().field_70170_p instanceof WorldServer) {
            Entity source = NoppesUtilServer.GetDamageSourcee(event.getSource());
            PlayerDataScript handler;
            if(event.getEntityLiving() instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)event.getEntityLiving()).scriptData;
                EventHooks.onPlayerDeath(handler, event.getSource(), source);
            }

            if(source instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)source).scriptData;
                EventHooks.onPlayerKills(handler, event.getEntityLiving());
            }

        }
    }

    @SubscribeEvent
    public void invoke(LivingHurtEvent event) {
        if(event.getEntityLiving().field_70170_p instanceof WorldServer) {
            Entity source = NoppesUtilServer.GetDamageSourcee(event.getSource());
            PlayerDataScript handler;
            if(event.getEntityLiving() instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)event.getEntityLiving()).scriptData;
                DamagedEvent pevent = new DamagedEvent(handler.getPlayer(), source, event.getAmount(), event.getSource());
                event.setCanceled(EventHooks.onPlayerDamaged(handler, pevent));
                event.setAmount(pevent.damage);
            }

            if(source instanceof EntityPlayer) {
                handler = PlayerData.get((EntityPlayer)source).scriptData;
                DamagedEntityEvent pevent1 = new DamagedEntityEvent(handler.getPlayer(), event.getEntityLiving(), event.getAmount(), event.getSource());
                event.setCanceled(EventHooks.onPlayerDamagedEntity(handler, pevent1));
                event.setAmount(pevent1.damage);
            }

        }
    }

    @SubscribeEvent
    public void invoke(LivingAttackEvent event) {
        if(event.getEntityLiving().field_70170_p instanceof WorldServer) {
            Entity source = NoppesUtilServer.GetDamageSourcee(event.getSource());
            if(source instanceof EntityPlayer) {
                PlayerDataScript handler = PlayerData.get((EntityPlayer)source).scriptData;
                ItemStack item = ((EntityPlayer)source).func_184614_ca();
                IEntity target = NpcAPI.Instance().getIEntity(event.getEntityLiving());
                AttackEvent ev = new AttackEvent(handler.getPlayer(), 1, target);
                event.setCanceled(EventHooks.onPlayerAttack(handler, ev));
                if(item.func_77973_b() == CustomItems.scripted_item && !event.isCanceled()) {
                    ItemScriptedWrapper isw = ItemScripted.GetWrapper(item);
                    noppes.npcs.api.event.ItemEvent.AttackEvent eve = new noppes.npcs.api.event.ItemEvent.AttackEvent(isw, handler.getPlayer(), 1, target);
                    eve.setCanceled(event.isCanceled());
                    event.setCanceled(EventHooks.onScriptItemAttack(isw, eve));
                }
            }

        }
    }

    @SubscribeEvent
    public void invoke(PlayerLoggedInEvent event) {
        if(event.player.field_70170_p instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            EventHooks.onPlayerLogin(handler);
        }
    }

    @SubscribeEvent
    public void invoke(PlayerLoggedOutEvent event) {
        if(event.player.field_70170_p instanceof WorldServer) {
            PlayerDataScript handler = PlayerData.get(event.player).scriptData;
            EventHooks.onPlayerLogout(handler);
        }
    }
    */

    @SubscribeEvent(
            priority = EventPriority.HIGHEST
    )
    public void invoke(ServerChatEvent event) {
        if(event.player.worldObj instanceof WorldServer && event.player != EntityNPCInterface.chateventPlayer) {
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
            Method register = MinecraftForge.EVENT_BUS.getClass().getDeclaredMethod("register", new Class[]{Class.class, Object.class, Method.class, ModContainer.class});
            register.setAccessible(true);
            ArrayList list = new ArrayList(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event"));
            list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.fml.common"));
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
                        register.invoke(MinecraftForge.EVENT_BUS, new Object[]{c1, handler, e, Loader.instance().activeModContainer()});
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
