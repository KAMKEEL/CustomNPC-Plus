package noppes.npcs.controllers.data;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import kamkeel.npcs.network.packets.request.script.EventScriptPacket;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.NBTTags;
import noppes.npcs.api.IWorld;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.config.ConfigDebug;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ScriptHookController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.janino.EventJaninoScript;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.scripted.entity.ScriptNpc;
import noppes.npcs.api.handler.IHookDefinition;

import javax.script.ScriptEngine;
import java.util.*;

public class DataScript implements IScriptHandlerPacket {
    public List<IScriptUnit> eventScripts = new ArrayList<>();

    private HashMap<EnumScriptType, ScriptContainer> scripts = new HashMap<>();
    private final static EntityType entities = new EntityType();
    private final static JobType jobs = new JobType();
    private final static RoleType roles = new RoleType();

    public String scriptLanguage = "ECMAScript";
    private EntityNPCInterface npc;
    public boolean enabled = false;

    public ICustomNpc dummyNpc;
    public IWorld dummyWorld;
    public boolean clientNeedsUpdate = false;
    public boolean aiNeedsUpdate = false;
    public boolean hasInited = false;

    // Editor/runtime globals descriptor used for a single source of truth.
    private static final class GlobalDefinition {
        private final String typeName;
        private final Object value;

        private GlobalDefinition(String typeName, Object value) {
            this.typeName = typeName;
            this.value = value;
        }
    }

    public DataScript(EntityNPCInterface npc) {
        for (int i = 0; i < 15; i++) {
            this.setNPCScript(i, new ScriptContainer(this));
        }

        this.npc = npc;
        if (npc.wrappedNPC == null) {
            npc.wrappedNPC = new ScriptNpc(this.npc);
        }
        dummyNpc = npc.wrappedNPC;

        if (npc.worldObj instanceof WorldServer)
            dummyWorld = NpcAPI.Instance().getIWorld((WorldServer) npc.worldObj);//new ScriptWorld((WorldServer) npc.worldObj);
    }

    public void readFromNBT(NBTTagCompound compound) {
        scripts = readScript(compound.getTagList("ScriptsContainers", 10));
        this.scriptLanguage = compound.getString("ScriptLanguage");
        if (!ScriptController.Instance.languages.containsKey(scriptLanguage)) {
            if (!ScriptController.Instance.languages.isEmpty()) {
                this.scriptLanguage = (String) ScriptController.Instance.languages.keySet().toArray()[0];
            } else {
                this.scriptLanguage = "ECMAScript";
            }
        }

        enabled = compound.getBoolean("ScriptEnabled");
    }

    public void readEventsFromNBT(NBTTagCompound compound) {
        eventScripts = NBTTags.GetScript(compound, this);
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setTag("ScriptsContainers", writeScript(scripts));
        compound.setString("ScriptLanguage", scriptLanguage);
        compound.setBoolean("ScriptEnabled", enabled);
        return compound;
    }

    public NBTTagCompound writeEventsToNBT(NBTTagCompound compound) {
        compound.setInteger("TotalScripts", this.eventScripts.size());
        for (int i = 0; i < this.eventScripts.size(); i++) {
            compound.setTag("Tab" + i, this.eventScripts.get(i).writeToNBT(new NBTTagCompound()));
        }
        return compound;
    }

    private HashMap<EnumScriptType, ScriptContainer> readScript(NBTTagList list) {
        HashMap<EnumScriptType, ScriptContainer> scripts = new HashMap<>();
        for (int i = 0; i < 15; i++) {
            scripts.put(EnumScriptType.values()[i], new ScriptContainer(this));
        }

        for (int i = 0; i < list.tagCount(); i++) {
            NBTTagCompound compoundd = list.getCompoundTagAt(i);
            ScriptContainer script = new ScriptContainer(this);
            script.readFromNBT(compoundd);
            if (script.hasCode() || npc.isRemote()) {
                scripts.put(EnumScriptType.values()[compoundd.getInteger("Type")], script);
            }
        }
        return scripts;
    }

    private NBTTagList writeScript(HashMap<EnumScriptType, ScriptContainer> scripts) {
        NBTTagList list = new NBTTagList();
        for (Map.Entry<EnumScriptType, ScriptContainer> entry : scripts.entrySet()) {
            NBTTagCompound tagCompound = new NBTTagCompound();
            ScriptContainer container = entry.getValue();
            if (container.hasCode()) {
                tagCompound.setInteger("Type", entry.getKey().ordinal());
                container.writeToNBT(tagCompound);
                list.appendTag(tagCompound);
            }
        }
        return list;
    }

    public boolean callScript(EnumScriptType type, Event event, Object... obs) {
        if (aiNeedsUpdate) {
            npc.updateAI = true;
            aiNeedsUpdate = false;
        }
        if (clientNeedsUpdate) {
            npc.updateClient = true;
            clientNeedsUpdate = false;
        }
        if (!isEnabled())
            return false;

        if (!hasInited && !npc.isRemote() && type != EnumScriptType.INIT) {
            hasInited = true;
            for (IScriptUnit scriptUnit : this.eventScripts) {
                scriptUnit.setErrored(false);
            }
            EventHooks.onNPCInit(this.npc);
        }

        for (IScriptUnit script : this.eventScripts) {
            script.run(type, event);
        }

        ScriptContainer script = scripts.get(type);
        if (script == null || script.errored || !script.hasCode())
            return false;

        script.setEngine(this.getLanguage());
        if (script.engine == null)
            return false;

        for (int i = 0; i + 1 < obs.length; i += 2) {
            Object ob = obs[i + 1];
            if (ob instanceof Entity)
                ob = NpcAPI.Instance().getIEntity((Entity) ob);
            script.engine.put(obs[i].toString(), ob);
        }
        if (ConfigDebug.ScriptLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER) {
            if (obs.length > 1 && obs[1] == null) {
                LogWriter.postScriptLog(npc.field_110179_h, type, String.format("[%s] NPC %s (%s, %s, %s)", ((String) type.function).toUpperCase(), npc.display.name, (int) npc.posX, (int) npc.posY, (int) npc.posZ));
            } else {
                LogWriter.postScriptLog(npc.field_110179_h, type, String.format("[%s] NPC %s (%s, %s, %s) | Objects: %s", ((String) type.function).toUpperCase(), npc.display.name, (int) npc.posX, (int) npc.posY, (int) npc.posZ, Arrays.toString(obs)));
            }
        }
        return callScript(script, type.function, event);
    }

    private boolean callScript(ScriptContainer script, String hookName, Event event) {
        ScriptEngine engine = script.engine;
        applyGlobalsToEngine(engine, hookName, event);
        script.run(engine);

        if (clientNeedsUpdate) {
            npc.updateClient = true;
            clientNeedsUpdate = false;
        }
        if (aiNeedsUpdate) {
            npc.updateAI = true;
            aiNeedsUpdate = false;
        }
        return event.isCanceled();
    }

    public boolean isEnabled() {
        return enabled && ScriptController.HasStart && !npc.worldObj.isRemote && !scripts.isEmpty() && ConfigScript.ScriptingEnabled;
    }

    @Override
    public ScriptContext getContext() {
        return ScriptContext.NPC;
    }

    @Override
    public void requestData() {
        EventScriptPacket.Get();
    }

    @Override
    public void sendSavePacket(int index, int totalCount, NBTTagCompound nbt) {
        EventScriptPacket.Save(index, totalCount, nbt);
    }

    public Map<Long, String> getOldConsoleText() {
        Map<Long, String> map = new TreeMap<>();

        for (Map.Entry<EnumScriptType, ScriptContainer> entry : scripts.entrySet()) {
            for (Map.Entry<Long, String> consoleEntry : entry.getValue().console.entrySet()) {
                map.put(consoleEntry.getKey(), " tab " + entry.getKey().ordinal() + ":\n" + consoleEntry.getValue());
            }
        }

        return map;
    }


    @Override
    public void callScript(EnumScriptType type, Event event) {
        callScript(type, event, "$$IGNORED$$", null);
    }

    @Override
    public void callScript(String hookName, Event event) {
        try {
            EnumScriptType enumScriptType = EnumScriptType.valueOfIgnoreCase(hookName);
            this.callScript(enumScriptType, event);
        } catch (IllegalArgumentException ignored) {
        }
    }

    public Map<String, String> getEditorGlobals(String hookName) {
        Map<String, GlobalDefinition> definitions = getGlobalDefinitions(hookName, null);
        Map<String, String> globals = new LinkedHashMap<>();
        for (Map.Entry<String, GlobalDefinition> entry : definitions.entrySet()) {
            String typeName = entry.getValue().typeName;
            if (typeName != null && !typeName.isEmpty()) {
                globals.put(entry.getKey(), typeName);
            }
        }
        return globals;
    }

    // Apply runtime globals plus engine-only bindings.
    private void applyGlobalsToEngine(ScriptEngine engine, String hookName, Event event) {
        Map<String, GlobalDefinition> definitions = getGlobalDefinitions(hookName, event);
        for (Map.Entry<String, GlobalDefinition> entry : definitions.entrySet()) {
            engine.put(entry.getKey(), entry.getValue().value);
        }

        engine.put("API", NpcAPI.Instance());
        for (Map.Entry<String, Object> engineObjects : NpcAPI.engineObjects.entrySet()) {
            engine.put(engineObjects.getKey(), engineObjects.getValue());
        }
    }

    // Build shared globals (editor types + runtime values).
    private Map<String, GlobalDefinition> getGlobalDefinitions(String hookName, Event event) {
        Map<String, GlobalDefinition> globals = new LinkedHashMap<>();
        globals.put("npc", new GlobalDefinition("ICustomNpc", dummyNpc));
        globals.put("world", new GlobalDefinition("IWorld", dummyWorld));
        globals.put("event", new GlobalDefinition(resolveEditorEventTypeName(hookName), event));
        globals.put("EntityType", new GlobalDefinition("noppes.npcs.scripted.constants.EntityType", entities));
        globals.put("RoleType", new GlobalDefinition("noppes.npcs.scripted.constants.RoleType", roles));
        globals.put("JobType", new GlobalDefinition("noppes.npcs.scripted.constants.JobType", jobs));

        return globals;
    }

    // Resolve editor-only event type from hook name with a single fallback.
    private String resolveEditorEventTypeName(String hookName) {
        final String fallback = "INpcEvent";
        if (hookName == null || hookName.isEmpty()) 
            return fallback;

        IHookDefinition definition = ScriptHookController.Instance.getHookDefinition(getContext().hookContext, hookName);
        if (definition == null) 
            return fallback;
        
        String typeName = definition.getUsableTypeName();
        if (typeName == null || typeName.isEmpty()) 
            return fallback;
        
        return typeName;
    }

    public boolean isClient() {
        return this.npc.isRemote();
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
        this.eventScripts = list;
    }

    public List<IScriptUnit> getScripts() {
        return this.eventScripts;
    }

    public String noticeString() {
        //BlockPos pos = this.npc.func_180425_c();
        //return MoreObjects.toStringHelper(this.npc).add("x", pos.func_177958_n()).add("y", pos.func_177956_o()).add("z", pos.func_177952_p()).toString();
        return "";
    }

    public void setWorld(World world) {
        if (world instanceof WorldServer)
            dummyWorld = new ScriptWorld((WorldServer) world);
    }

    public ScriptContainer getNPCScript(EnumScriptType scriptType) {
        return this.scripts.get(scriptType);
    }

    public ScriptContainer getNPCScript(int ordinal) {
        return this.getNPCScript(EnumScriptType.values()[ordinal]);
    }

    public void setNPCScript(EnumScriptType scriptType, ScriptContainer container) {
        this.scripts.put(scriptType, container);
    }

    public void setNPCScript(int ordinal, ScriptContainer container) {
        this.setNPCScript(EnumScriptType.values()[ordinal], container);
    }

    public Collection<ScriptContainer> getNPCScripts() {
        return this.scripts.values();
    }

    @Override
    public IScriptUnit createJaninoScriptUnit() {
        return new EventJaninoScript(ScriptContext.NPC);
    }
}
