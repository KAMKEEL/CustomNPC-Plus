package noppes.npcs.controllers;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.script.ScriptEngine;
import javax.script.ScriptEngineFactory;
import javax.script.ScriptEngineManager;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntityMob;
import net.minecraft.entity.passive.EntityAnimal;
import net.minecraft.entity.passive.EntityTameable;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.server.MinecraftServer;
import net.minecraftforge.event.world.WorldEvent;
import noppes.npcs.CustomNpcs;
import noppes.npcs.LogWriter;
import noppes.npcs.controllers.data.ForgeDataScript;
import noppes.npcs.controllers.data.PlayerDataScript;
import noppes.npcs.entity.EntityNPCInterface;
import noppes.npcs.scripted.entity.ScriptAnimal;
import noppes.npcs.scripted.entity.ScriptEntity;
import noppes.npcs.scripted.entity.ScriptLiving;
import noppes.npcs.scripted.entity.ScriptLivingBase;
import noppes.npcs.scripted.entity.ScriptMonster;
import noppes.npcs.scripted.entity.ScriptPixelmon;
import noppes.npcs.scripted.entity.ScriptPlayer;
import noppes.npcs.scripted.ScriptWorld;
import noppes.npcs.util.JsonException;
import noppes.npcs.util.NBTJsonUtil;
import cpw.mods.fml.common.eventhandler.SubscribeEvent;

public class ScriptController {

	public static ScriptController Instance;
	public static boolean HasStart = false;
	private ScriptEngineManager manager;
	public Map<String, String> languages = new HashMap<String, String>();
	public Map<String, String> scripts = new HashMap<String, String>();
	public long lastLoaded = 0;
	public File dir;
	public NBTTagCompound compound = new NBTTagCompound();

	private boolean loaded = false;
	public boolean shouldSave = false;

	public PlayerDataScript playerScripts = new PlayerDataScript((EntityPlayer)null);
	public long lastPlayerUpdate = 0L;

	public ForgeDataScript forgeScripts = new ForgeDataScript();

	public ScriptController(){
		loaded = false;
		Instance = this;
		manager = new ScriptEngineManager();
		LogWriter.info("Script Engines Available:");
		for(ScriptEngineFactory fac : manager.getEngineFactories()){
			if(fac.getExtensions().isEmpty())
				continue;
			fac.getScriptEngine();
			String ext = "." + fac.getExtensions().get(0).toLowerCase();
			LogWriter.info(fac.getLanguageName() + ": " + ext);
			languages.put(fac.getLanguageName(), ext);
		}
	}

	private File forgeScriptsFile() {
		return new File(this.dir, "forge_scripts.json");
	}

	public boolean loadForgeScripts() {
		this.forgeScripts.clear();
		File file = this.forgeScriptsFile();

		try {
			if(!file.exists()) {
				return false;
			} else {
				this.forgeScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
				return true;
			}
		} catch (Exception var3) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), var3);
			return false;
		}
	}

	public void setForgeScripts(NBTTagCompound compound) {
		this.forgeScripts.readFromNBT(compound);
		File file = this.forgeScriptsFile();

		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.forgeScripts.lastInited = -1L;
		} catch (IOException var4) {
			var4.printStackTrace();
		} catch (JsonException var5) {
			var5.printStackTrace();
		}

	}

	private File playerScriptsFile() {
		return new File(dir, "player_scripts.json");
	}

	public boolean loadPlayerScripts() {
		this.playerScripts.clear();
		File file = this.playerScriptsFile();

		try {
			if(!file.exists()) {
				return false;
			} else {
				this.playerScripts.readFromNBT(NBTJsonUtil.LoadFile(file));
				shouldSave = false;
				return true;
			}
		} catch (Exception var3) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), var3);
			return false;
		}
	}

	public void setPlayerScripts(NBTTagCompound compound) {
		this.playerScripts.readFromNBT(compound);
		File file = this.playerScriptsFile();

		try {
			NBTJsonUtil.SaveFile(file, compound);
			this.lastPlayerUpdate = System.currentTimeMillis();
		} catch (IOException var4) {
			var4.printStackTrace();
		} catch (JsonException var5) {
			var5.printStackTrace();
		}
	}

	private void loadCategories(){
		dir = new File(CustomNpcs.getWorldSaveDirectory(), "scripts");
		if(!dir.exists())
			dir.mkdir();
		if(!getSavedFile().exists())
			shouldSave = true;
		new ScriptWorld(null).clearTempData();
		scripts.clear();
		for(String language : languages.keySet()){
			String ext = languages.get(language);
			File scriptDir = new File(dir,language.toLowerCase());
			if(!scriptDir.exists())
				scriptDir.mkdir();
			else
				loadDir(scriptDir, "", ext);
		}
		lastLoaded = System.currentTimeMillis();
	}
	private void loadDir(File dir, String name, String ext){
		for(File file : dir.listFiles()){
			String filename = name + file.getName().toLowerCase();
			if(file.isDirectory()){
				loadDir(file, filename + "/", ext);
				continue;
			}
			if(!filename.endsWith(ext))
				continue;
			try {
				scripts.put(filename, readFile(file));
			} catch (IOException e) {
				e.printStackTrace();
			}
		}
	}
	public boolean loadStoredData(){
		loadCategories();
		File file = getSavedFile();
		try {
			if(!file.exists())
				return false;
			this.compound = NBTJsonUtil.LoadFile(file);
			shouldSave = false;
		}
		catch (Exception e) {
			LogWriter.error("Error loading: " + file.getAbsolutePath(), e);
			return false;
		}
		return true;
	}

	private File getSavedFile(){
		return new File(dir, "world_data.json");
	}

	private String readFile(File file) throws IOException {
		BufferedReader br = new BufferedReader(new FileReader(file));
		try {
			StringBuilder sb = new StringBuilder();
			String line = br.readLine();

			while (line != null) {
				sb.append(line);
				sb.append("\n");
				line = br.readLine();
			}
			return sb.toString();
		} finally {
			br.close();
		}
	}

	public ScriptEngine getEngineByName(String language) {
		return manager.getEngineByName(language);
	}

	public NBTTagList nbtLanguages() {
		NBTTagList list = new NBTTagList();
		for(String language : languages.keySet()){
			NBTTagCompound compound = new NBTTagCompound();
			NBTTagList scripts = new NBTTagList();
			for(String script : getScripts(language)){
				scripts.appendTag(new NBTTagString(script));
			}
			compound.setTag("Scripts", scripts);
			compound.setString("Language", language);
			list.appendTag(compound);
		}
		return list;
	}

	private List<String> getScripts(String language){
		List<String> list = new ArrayList<String>();
		String ext = languages.get(language);
		if(ext == null)
			return list;
		for(String script : scripts.keySet()){
			if(script.endsWith(ext)){
				list.add(script);
			}
		}
		return list;
	}

	public ScriptEntity getScriptForEntity(Entity entity) {
		if(entity == null)
			return null;
		if(entity instanceof EntityNPCInterface)
			return ((EntityNPCInterface) entity).script.dummyNpc;
		else{
			ScriptEntityData data = (ScriptEntityData) entity.getExtendedProperties("ScriptedObject");
			if(data != null)
				return data.base;
			if(entity instanceof EntityPlayerMP)
				data = new ScriptEntityData(new ScriptPlayer((EntityPlayerMP) entity));
			else if(PixelmonHelper.isPixelmon(entity))
				return new ScriptPixelmon((EntityTameable) entity);
			else if(entity instanceof EntityAnimal)
				data = new ScriptEntityData(new ScriptAnimal((EntityAnimal) entity));
			else if(entity instanceof EntityMob)
				data = new ScriptEntityData(new ScriptMonster((EntityMob) entity));
			else if(entity instanceof EntityLiving)
				data = new ScriptEntityData(new ScriptLiving((EntityLiving) entity));
			else if(entity instanceof EntityLivingBase)
				data = new ScriptEntityData(new ScriptLivingBase((EntityLivingBase)entity));
			else
				data = new ScriptEntityData(new ScriptEntity(entity));
			entity.registerExtendedProperties("ScriptedObject", data);
			return data.base;
		}
	}

	@SubscribeEvent
	public void saveWorld(WorldEvent.Save event){
		if(!shouldSave || event.world.isRemote || event.world != MinecraftServer.getServer().worldServers[0])
			return;

		try {
			NBTJsonUtil.SaveFile(getSavedFile(), compound);
		}
		catch (Exception e) {
			LogWriter.except(e);
		}

		shouldSave = false;
	}
}