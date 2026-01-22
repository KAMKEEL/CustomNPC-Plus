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
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.IScriptHookHandler;
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
import java.util.Iterator;
import java.util.List;

public class ForgeDataScript implements IScriptHandlerPacket {
    private static final Object HOOK_LOCK = new Object();
    private static List<String> cachedHooks;
    private List<IScriptUnit> scripts = new ArrayList<>();
    private String scriptLanguage = "ECMAScript";
    public long lastInited = -1L;
    private long lastForgeUpdate = -1L;
    private boolean enabled = false;

    public ForgeDataScript() {
    }

    public void clear() {
        this.scripts = new ArrayList<>();
    }

    public void readFromNBT(NBTTagCompound compound) {
        if (compound.hasKey("Scripts")) {
            this.scripts = new ArrayList<>(NBTTags.GetScriptOld(compound.getTagList("Scripts", 10), this));
        } else {
            this.scripts = new ArrayList<>(NBTTags.GetScript(compound, this));
        }
        this.scriptLanguage = compound.getString("ScriptLanguage");
        if (!ScriptController.Instance.languages.containsKey(scriptLanguage)) {
            if (!ScriptController.Instance.languages.isEmpty()) {
                this.scriptLanguage = (String) ScriptController.Instance.languages.keySet().toArray()[0];
            } else {
                this.scriptLanguage = "ECMAScript";
            }
        }
        this.enabled = compound.getBoolean("ScriptEnabled");
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setInteger("TotalScripts", this.scripts.size());
        for (int i = 0; i < this.scripts.size(); i++) {
            compound.setTag("Tab" + i, this.scripts.get(i).writeToNBT(new NBTTagCompound()));
        }
        compound.setString("ScriptLanguage", this.scriptLanguage);
        compound.setBoolean("ScriptEnabled", this.enabled);
        return compound;
    }


    public void callScript(String type, Event event) {
        if (this.isEnabled()) {
            if (ScriptController.Instance.lastLoaded > this.lastInited || ScriptController.Instance.lastForgeUpdate > this.lastForgeUpdate) {
                this.lastInited = ScriptController.Instance.lastLoaded;
                this.lastForgeUpdate = ScriptController.Instance.lastForgeUpdate;

                for (IScriptUnit script : this.scripts) {
                    if (script instanceof ScriptContainer)
                        ((ScriptContainer) script).errored = false;
                }

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
    }

    public boolean isEnabled() {
        return this.enabled && ConfigScript.GlobalForgeScripts && ScriptController.HasStart && this.scripts.size() > 0;
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.FORGE;
    }

    @Override
    public String getHookContext() {
        return IScriptHookHandler.CONTEXT_FORGE;
    }

    @Override
    public List<String> getHooks() {
        if (cachedHooks != null)
            return new ArrayList<>(cachedHooks);

        synchronized (HOOK_LOCK) {
            if (cachedHooks != null)
                return new ArrayList<>(cachedHooks);

            List<String> hookList = new ArrayList<>(ScriptHookController.Instance.getBuiltInHooks(IScriptHookHandler.CONTEXT_FORGE));

            ArrayList<ClassPath.ClassInfo> list = new ArrayList();
            try {
                list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("cpw.mods.fml.common.gameevent"));
                list.addAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event"));
                list.removeAll(ClassPath.from(this.getClass().getClassLoader()).getTopLevelClassesRecursive("net.minecraftforge.event.terraingen"));
            } catch (IOException e) {
                e.printStackTrace();
            }

            for (ClassPath.ClassInfo e1 : list) {
                ClassPath.ClassInfo classLoader = e1;
                Class infoClass = classLoader.load();
                ArrayList c = new ArrayList(Arrays.asList(infoClass.getDeclaredClasses()));
                if (c.isEmpty()) {
                    c.add(infoClass);
                }
                Iterator var10 = c.iterator();
                while (var10.hasNext()) {
                    Class c1 = (Class) var10.next();
                    if (!EntityEvent.EntityConstructing.class.isAssignableFrom(c1)
                        && !WorldEvent.PotentialSpawns.class.isAssignableFrom(c1)
                        && !TickEvent.RenderTickEvent.class.isAssignableFrom(c1)
                        && !TickEvent.ClientTickEvent.class.isAssignableFrom(c1)
                        && !FMLNetworkEvent.ClientCustomPacketEvent.class.isAssignableFrom(c1)
                        && !ItemTooltipEvent.class.isAssignableFrom(c1)
                        && Event.class.isAssignableFrom(c1)
                        && !Modifier.isAbstract(c1.getModifiers())
                        && Modifier.isPublic(c1.getModifiers())
                        && !ChunkEvent.class.isAssignableFrom(c1)
                        && !ChunkWatchEvent.class.isAssignableFrom(c1)
                        && !ChunkDataEvent.class.isAssignableFrom(c1)) {
                        String eventName = c1.getName();
                        int i = eventName.lastIndexOf(".");
                        eventName = StringUtils.uncapitalize(eventName.substring(i + 1).replace("$", ""));

                        hookList.add(eventName);
                    }
                }
            }

            hookList.add("onCNPCNaturalSpawn");

            for (String addonHook : ScriptHookController.Instance.getAddonHooks(IScriptHookHandler.CONTEXT_FORGE)) {
                if (!hookList.contains(addonHook)) {
                    hookList.add(addonHook);
                }
            }

            cachedHooks = hookList;
        }

        return new ArrayList<>(cachedHooks);
    }

    @Override
    public void requestData() {
        ForgeScriptPacket.Get();
    }

    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        ForgeScriptPacket.Save(index, totalCount, nbt);
    }

    public boolean isClient() {
        return false;
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean bo) {
        this.enabled = bo;
    }

    public String getLanguage() {
        return this.scriptLanguage;
    }

    public void setLanguage(String lang) {
        this.scriptLanguage = lang;
    }

    public void setScripts(List<IScriptUnit> list) {
        this.scripts = list;
    }

    public List<IScriptUnit> getScripts() {
        return this.scripts;
    }


}
