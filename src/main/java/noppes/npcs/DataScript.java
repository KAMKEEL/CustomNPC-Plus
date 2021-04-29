package noppes.npcs;

import java.util.HashMap;
import java.util.Map;

import javax.script.ScriptEngine;

import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptContainer;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.entity.EntityCustomNpc;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.ScriptEvent;
import noppes.npcs.scripted.ScriptNpc;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.scripted.constants.EntityType;
import noppes.npcs.scripted.constants.JobType;
import noppes.npcs.scripted.constants.RoleType;

public class DataScript {
	public Map<Integer,ScriptContainer> scripts = new HashMap<Integer,ScriptContainer>();
	private final static EntityType entities = new EntityType();
	private final static JobType jobs = new JobType();
	private final static RoleType roles = new RoleType();
	
	public String scriptLanguage = "ECMAScript";
	private EntityNPCInterface npc;
	public boolean enabled = false;
	
	public ScriptNpc dummyNpc;
	public ScriptWorld dummyWorld;
	public boolean clientNeedsUpdate = false;
	public boolean aiNeedsUpdate = false;
	public boolean hasInited = false;
	
	public DataScript(EntityNPCInterface npc) {
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
	
	private Map<Integer,ScriptContainer> readScript(NBTTagList list){
		Map<Integer,ScriptContainer> scripts = new HashMap<Integer,ScriptContainer>();
		for(int i = 0; i < list.tagCount(); i++){
			NBTTagCompound compoundd = list.getCompoundTagAt(i);
			ScriptContainer script = new ScriptContainer();
			script.readFromNBT(compoundd);
			if(script.hasCode() || npc.isRemote())				
				scripts.put(compoundd.getInteger("Type"), script);			
		}
		return scripts;
	}
	
	private NBTTagList writeScript(Map<Integer,ScriptContainer> scripts){
		NBTTagList list = new NBTTagList();
		for(Integer type : scripts.keySet()){
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
	
	public boolean isEnabled(){
		return enabled && ScriptController.HasStart && !npc.worldObj.isRemote && !scripts.isEmpty();
	}
	
	private boolean callScript(ScriptContainer script){
		ScriptEngine engine = script.engine;
		engine.put("npc", dummyNpc);
		engine.put("world", dummyWorld);
		ScriptEvent result = (ScriptEvent) engine.get("event");
		if(result == null)
			engine.put("event", result = new ScriptEvent());
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

	public void setWorld(World world) {
		if(world instanceof WorldServer)
			dummyWorld = new ScriptWorld((WorldServer) world);
	}
	
}
