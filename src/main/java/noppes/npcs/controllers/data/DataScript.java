package noppes.npcs.controllers.data;

import java.util.*;

import javax.script.ScriptEngine;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import cpw.mods.fml.relauncher.Side;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.CustomNpcs;
import noppes.npcs.EventHooks;
import noppes.npcs.LogWriter;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.entity.ScriptNpc;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.api.entity.ICustomNpc;
import noppes.npcs.api.IWorld;
import noppes.npcs.scripted.NpcAPI;

public class DataScript implements IScriptHandler {
	public List<ScriptContainer> scripts = new ArrayList();
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

	public DataScript(EntityNPCInterface npc) {
		for (int i = 0; i < 12; i++) {
			scripts.add(new ScriptContainer(this));
		}

		this.npc = npc;
		if (npc.wrappedNPC == null) {
			npc.wrappedNPC = new ScriptNpc(this.npc);
		}
		dummyNpc = npc.wrappedNPC;

		if(npc.worldObj instanceof WorldServer)
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

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("ScriptsContainers", writeScript(scripts));
		compound.setString("ScriptLanguage", scriptLanguage);
		compound.setBoolean("ScriptEnabled", enabled);
		return compound;
	}

	private List<ScriptContainer> readScript(NBTTagList list){
		List<ScriptContainer> scripts = new ArrayList<>();
		for (int i = 0; i < 12; i++) {
			scripts.add(new ScriptContainer(this));
		}

		for(int i = 0; i < list.tagCount(); i++){
			NBTTagCompound compoundd = list.getCompoundTagAt(i);
			ScriptContainer script = new ScriptContainer(this);
			script.readFromNBT(compoundd);
			if(script.hasCode() || npc.isRemote())
				scripts.set(compoundd.getInteger("Type"), script);
		}
		return scripts;
	}

	private NBTTagList writeScript(List<ScriptContainer> scripts){
		NBTTagList list = new NBTTagList();
		for(int type = 0; type < scripts.size(); type++){
			NBTTagCompound compoundd = new NBTTagCompound();
			compoundd.setInteger("Type", type);
			ScriptContainer script = scripts.get(type);
			script.writeToNBT(compoundd);
			list.appendTag(compoundd);
		}
		return list;
	}

	public boolean callScript(EnumScriptType type, Event event, Object... obs){
		if(aiNeedsUpdate){
			npc.updateAI = true;
			aiNeedsUpdate = false;
		}
		if(clientNeedsUpdate){
			npc.updateClient = true;
			clientNeedsUpdate = false;
		}
		if(!isEnabled())
			return false;
		if(!hasInited && !npc.isRemote() && type != EnumScriptType.INIT){
			hasInited = true;
			EventHooks.onNPCInit(this.npc);
		}
		ScriptContainer script = scripts.get(type.ordinal());
		if(script == null || script.errored || !script.hasCode())
			return false;
		script.setEngine(scriptLanguage);
		if(script.engine == null)
			return false;
		for(int i = 0; i + 1 < obs.length; i += 2){
			Object ob = obs[i + 1];
			if(ob instanceof Entity)
				ob = NpcAPI.Instance().getIEntity((Entity)ob);
			script.engine.put(obs[i].toString(), ob);
		}
		if(CustomNpcs.ScriptLogging && FMLCommonHandler.instance().getEffectiveSide() == Side.SERVER){
			if(obs.length > 1 && obs[1] == null){
				LogWriter.postScriptLog(npc.field_110179_h, type, String.format("[%s] NPC %s (%s, %s, %s)", ((String)type.function).toUpperCase(), npc.display.name, (int)npc.posX, (int)npc.posY, (int)npc.posZ));
			} else {
				LogWriter.postScriptLog(npc.field_110179_h, type, String.format("[%s] NPC %s (%s, %s, %s) | Objects: %s", ((String)type.function).toUpperCase(), npc.display.name, (int)npc.posX, (int)npc.posY, (int)npc.posZ, Arrays.toString(obs)));
			}
		}
		return callScript(script, event);
	}

	private boolean callScript(ScriptContainer script, Event event){
		ScriptEngine engine = script.engine;
		engine.put("npc", dummyNpc);
		engine.put("world", dummyWorld);
		engine.put("event", event);
		engine.put("API", new NpcAPI());
		engine.put("EntityType", entities);
		engine.put("RoleType", roles);
		engine.put("JobType", jobs);
		script.run(engine);

		if(clientNeedsUpdate){
			npc.updateClient = true;
			clientNeedsUpdate = false;
		}
		if(aiNeedsUpdate){
			npc.updateAI = true;
			aiNeedsUpdate = false;
		}
		return event.isCanceled();
	}

	public boolean isEnabled(){
		return enabled && ScriptController.HasStart && !npc.worldObj.isRemote && !scripts.isEmpty() && CustomNpcs.ScriptingEnabled;
	}

	public Map<Long, String> getConsoleText() {
		Map<Long, String> map = new TreeMap();
		int tab = 0;
		Iterator var3 = this.getScripts().iterator();

		while(var3.hasNext()) {
			ScriptContainer script = (ScriptContainer)var3.next();
			++tab;
			Iterator var5 = script.console.entrySet().iterator();

			while(var5.hasNext()) {
				Map.Entry<Long, String> entry = (Map.Entry)var5.next();
				map.put(entry.getKey(), " tab " + tab + ":\n" + (String)entry.getValue());
			}
		}

		return map;
	}

	public void clearConsole() {
		Iterator var1 = this.getScripts().iterator();

		while(var1.hasNext()) {
			ScriptContainer script = (ScriptContainer)var1.next();
			script.console.clear();
		}

	}

	@Override
	public void callScript(EnumScriptType var1, Event var2) {
		callScript(var1, var2, "$$IGNORED$$", null);
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

	public List<ScriptContainer> getScripts() {
		return this.scripts;
	}

	public String noticeString() {
		//BlockPos pos = this.npc.func_180425_c();
		//return MoreObjects.toStringHelper(this.npc).add("x", pos.func_177958_n()).add("y", pos.func_177956_o()).add("z", pos.func_177952_p()).toString();
		return "";
	}

	public void setWorld(World world) {
		if(world instanceof WorldServer)
			dummyWorld = new ScriptWorld((WorldServer) world);
	}

}