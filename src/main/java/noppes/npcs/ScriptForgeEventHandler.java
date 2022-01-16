package noppes.npcs;

import com.google.common.reflect.ClassPath;
import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.Loader;
import cpw.mods.fml.common.ModContainer;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;
import cpw.mods.fml.common.gameevent.PlayerEvent;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.world.WorldServer;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.controllers.PixelmonHelper;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.event.ForgeEvent;

import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Vector;

public class ScriptForgeEventHandler {
    public ScriptForgeEventHandler() {
    }

    @SubscribeEvent
    public void forgeEntity(Event event) {
        if(CustomNpcs.getServer() != null && ScriptController.Instance.forgeScripts.isEnabled()) {
            if(event instanceof TickEvent && !(((TickEvent)event).side == Side.SERVER && ((TickEvent)event).phase == TickEvent.Phase.START)) {
                return;
            }

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
                if (event instanceof PlayerEvent) {
                    PlayerEvent ev = (PlayerEvent) event;
                    if (ev.player == null || !(ev.player.worldObj instanceof WorldServer)) {
                        return;
                    }
                }

                EventHooks.onForgeEvent(new ForgeEvent(event), event);
            }
        }
    }

    public ScriptForgeEventHandler registerForgeEvents() {
        try {
            Method e = this.getClass().getMethod("forgeEntity", new Class[]{Event.class});
            Method register = FMLCommonHandler.instance().bus().getClass().getDeclaredMethod("register", new Class[]{Class.class, Object.class, Method.class, ModContainer.class});
            register.setAccessible(true);
            ArrayList list = new ArrayList();
            list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("cpw.mods.fml.common.gameevent"));
            list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event"));
            list.removeAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event.terraingen"));
            Iterator e1 = list.iterator();

            while(true) {
                ClassPath.ClassInfo classLoader;
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

                    classLoader = (ClassPath.ClassInfo)e1.next();
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
                    if(!EntityEvent.EntityConstructing.class.isAssignableFrom(c1) && !WorldEvent.PotentialSpawns.class.isAssignableFrom(c1) && !TickEvent.RenderTickEvent.class.isAssignableFrom(c1) && !TickEvent.ClientTickEvent.class.isAssignableFrom(c1) && !FMLNetworkEvent.ClientCustomPacketEvent.class.isAssignableFrom(c1) && !ItemTooltipEvent.class.isAssignableFrom(c1) && Event.class.isAssignableFrom(c1) && !Modifier.isAbstract(c1.getModifiers()) && Modifier.isPublic(c1.getModifiers()) && !ChunkEvent.class.isAssignableFrom(c1) && !ChunkWatchEvent.class.isAssignableFrom(c1) && !ChunkDataEvent.class.isAssignableFrom(c1)) {
                        register.invoke(FMLCommonHandler.instance().bus(), new Object[]{c1, this, e, Loader.instance().activeModContainer()});
                    }
                }
            }
        } catch (Exception var13) {
            var13.printStackTrace();
            return this;
        }
    }
}