package noppes.npcs.controllers.data;

import com.google.common.reflect.ClassPath;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.common.gameevent.TickEvent;
import cpw.mods.fml.common.network.FMLNetworkEvent;
import kamkeel.npcs.network.packets.request.script.ForgeScriptPacket;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraftforge.event.entity.EntityEvent;
import net.minecraftforge.event.entity.player.ItemTooltipEvent;
import net.minecraftforge.event.world.ChunkDataEvent;
import net.minecraftforge.event.world.ChunkEvent;
import net.minecraftforge.event.world.ChunkWatchEvent;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.EventHooks;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ScriptHookController;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ForgeDataScript extends MultiScriptHandler {
    private static final Object HOOK_LOCK = new Object();
    private static List<String> cachedHooks;

    private long lastForgeUpdate = -1L;

    public ForgeDataScript() {
    }

    public boolean isEnabled() {
        return this.enabled && ConfigScript.GlobalForgeScripts && ScriptController.HasStart && this.scripts.size() > 0;
    }

    @Override
    protected boolean canRunScripts() {
        return isEnabled();
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.FORGE;
    }

    @Override
    protected boolean needsReInit() {
        return ScriptController.Instance.lastLoaded > lastInited || ScriptController.Instance.lastForgeUpdate > lastForgeUpdate;
    }

    @Override
    protected void reInitScripts() {
        lastInited = ScriptController.Instance.lastLoaded;
        lastForgeUpdate = ScriptController.Instance.lastForgeUpdate;

        for (IScriptUnit script : this.scripts) {
            if (script instanceof ScriptContainer)
                ((ScriptContainer) script).errored = false;
        }
    }

    @Override
    public void callScript(String type, Event event) {
        if (!canRunScripts()) {
            return;
        }

        if (needsReInit()) {
            reInitScripts();
            if (!type.equals("init")) {
                EventHooks.onForgeInit(this);
            }
        }

        for (IScriptUnit script : this.scripts) {
            if (script == null || script.hasErrored() || !script.hasCode())
                continue;
            script.run(type, event);
        }
    }

    @Override
    public List<String> getHooks() {
        if (cachedHooks != null)
            return new ArrayList<>(cachedHooks);

        synchronized (HOOK_LOCK) {
            if (cachedHooks != null)
                return new ArrayList<>(cachedHooks);

            // Start with built-in + addon hooks from ScriptHookController
            List<String> hookList = new ArrayList<>(ScriptHookController.Instance.getAllHooks(ScriptContext.FORGE.hookContext));

            // Dynamically discover Forge event classes
            ArrayList<ClassPath.ClassInfo> list = new ArrayList<>();
            try {
                list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("cpw.mods.fml.common.gameevent"));
                list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event"));
                list.removeAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event.terraingen"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (ClassPath.ClassInfo classInfo : list) {
                Class<?> infoClass = classInfo.load();
                List<Class<?>> classes = new ArrayList<>(Arrays.asList(infoClass.getDeclaredClasses()));
                if (classes.isEmpty()) {
                    classes.add(infoClass);
                }

                for (Class<?> eventClass : classes) {
                    if (isValidForgeEvent(eventClass)) {
                        String eventName = eventClass.getName();
                        int lastDot = eventName.lastIndexOf(".");
                        eventName = StringUtils.uncapitalize(eventName.substring(lastDot + 1).replace("$", ""));

                        if (!hookList.contains(eventName)) {
                            hookList.add(eventName);
                        }
                    }
                }
            }

            cachedHooks = hookList;
        }

        return new ArrayList<>(cachedHooks);
    }

    /**
     * Check if a class is a valid Forge event that should be exposed as a hook.
     */
    private boolean isValidForgeEvent(Class<?> eventClass) {
        return Event.class.isAssignableFrom(eventClass)
            && Modifier.isPublic(eventClass.getModifiers())
            && !Modifier.isAbstract(eventClass.getModifiers())
            && !EntityEvent.EntityConstructing.class.isAssignableFrom(eventClass)
            && !WorldEvent.PotentialSpawns.class.isAssignableFrom(eventClass)
            && !TickEvent.RenderTickEvent.class.isAssignableFrom(eventClass)
            && !TickEvent.ClientTickEvent.class.isAssignableFrom(eventClass)
            && !FMLNetworkEvent.ClientCustomPacketEvent.class.isAssignableFrom(eventClass)
            && !ItemTooltipEvent.class.isAssignableFrom(eventClass)
            && !ChunkEvent.class.isAssignableFrom(eventClass)
            && !ChunkWatchEvent.class.isAssignableFrom(eventClass)
            && !ChunkDataEvent.class.isAssignableFrom(eventClass);
    }

    @Override
    public void requestData() {
        ForgeScriptPacket.Get();
    }

    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        ForgeScriptPacket.Save(index, totalCount, nbt);
    }

    @Override
    public boolean isClient() {
        return false;
    }
}
