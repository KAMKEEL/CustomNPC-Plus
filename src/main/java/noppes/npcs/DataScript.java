package noppes.npcs;

import java.util.*;

import javax.script.ScriptEngine;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.event.ScriptEvent;
import noppes.npcs.scripted.entity.ScriptNpc;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.scripted.constants.RoleType;
import noppes.npcs.scripted.interfaces.IWorld;
import noppes.npcs.scripted.wrapper.WrapperNpcAPI;

public class DataScript implements IScriptHandler {
	public List<ScriptContainer> scripts = new ArrayList();
	private final static EntityType entities = new EntityType();
	private final static JobType jobs = new JobType();
	private final static RoleType roles = new RoleType();
	
	public String scriptLanguage = "ECMAScript";
	private EntityNPCInterface npc;
	public boolean enabled = false;
	
	public ScriptNpc dummyNpc;
	public IWorld dummyWorld;
	public boolean clientNeedsUpdate = false;
	public boolean aiNeedsUpdate = false;
	public boolean hasInited = false;
	
	public DataScript(EntityNPCInterface npc) {
		for (int i = 0; i < 12; i++) {
			scripts.add(new ScriptContainer(this));
		}

		this.npc = npc;
		if(npc instanceof EntityCustomNpc)
			dummyNpc = new ScriptNpc((EntityCustomNpc) npc);
		if(npc.worldObj instanceof WorldServer)
			dummyWorld = new ScriptWorld((WorldServer) npc.worldObj);
	}

	public void readFromNBT(NBTTagCompound compound) {
		scripts = readScript(compound.getTagList("ScriptsContainers", 10));
		scriptLanguage = compound.getString("ScriptLanguage");
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

	public boolean callScript(EnumScriptType type, Object... obs){
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
		if(!hasInited){
			hasInited = true;
			callScript(EnumScriptType.INIT);
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
				ob = ScriptController.Instance.getScriptForEntity((Entity)ob);
			script.engine.put(obs[i].toString(), ob);
		}

		return callScript(script);
	}

	private boolean callScript(ScriptContainer script){
		ScriptEngine engine = script.engine;
		engine.put("npc", dummyNpc);
		engine.put("world", dummyWorld);
		ScriptEvent result = (ScriptEvent) engine.get("event");
		if(result == null)
			engine.put("event", result = new ScriptEvent());
		engine.put("API", new WrapperNpcAPI());
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
		return result.isCancelled();
	}
	
	public boolean isEnabled(){
		return enabled && ScriptController.HasStart && !npc.worldObj.isRemote && !scripts.isEmpty();
	}

	public void callScript(EnumScriptType var1, Event var2, Object... obs) {

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
