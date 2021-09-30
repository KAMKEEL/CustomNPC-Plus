package noppes.npcs;

import com.google.common.util.concurrent.Futures;
import com.google.common.util.concurrent.ListenableFuture;
import com.google.common.util.concurrent.ListenableFutureTask;
import net.minecraft.client.Minecraft;
import net.minecraft.entity.Entity;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.World;
import net.minecraft.world.WorldServer;
import cpw.mods.fml.common.eventhandler.Event;
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
import org.apache.commons.lang3.Validate;

import javax.script.ScriptEngine;
import java.util.*;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;

public class EventDataScript implements IScriptHandler {
	private List<EventScriptContainer> scripts = new ArrayList();
	private String scriptLanguage = "ECMAScript";
	public long lastInited = -1L;
	public boolean hadInteract = true;
	private boolean enabled = false;

	public EventDataScript() {
	}

	public void clear() {
		this.scripts = new ArrayList();
	}

	public void readFromNBT(NBTTagCompound compound) {
		this.scripts = NBTTags.GetScript(compound.getTagList("Scripts", 10), this);
		this.scriptLanguage = compound.getString("ScriptLanguage");
		this.enabled = compound.getBoolean("ScriptEnabled");
	}

	public NBTTagCompound writeToNBT(NBTTagCompound compound) {
		compound.setTag("Scripts", NBTTags.NBTScript(this.scripts));
		compound.setString("ScriptLanguage", this.scriptLanguage);
		compound.setBoolean("ScriptEnabled", this.enabled);
		return compound;
	}

	public void runScript(EnumScriptType type, Event event) {
	}

	public void runScript(String type, Event event) {
		if(this.isEnabled()) {
			Minecraft.getMinecraft().func_152344_a(() -> {
				if (ScriptController.Instance.lastLoaded > this.lastInited) {
					this.lastInited = ScriptController.Instance.lastLoaded;
					if (!type.equals("init")) {
						EventHooks.onForgeInit(this);
					}
				}

				Iterator var3 = this.scripts.iterator();

				while (var3.hasNext()) {
					EventScriptContainer script = (EventScriptContainer) var3.next();
					script.run(type, event);
				}

			});
		}
	}

	public boolean isEnabled() {
		return this.enabled && ScriptController.HasStart && this.scripts.size() > 0;
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

	public List<EventScriptContainer> getScripts() {
		return this.scripts;
	}

	public String noticeString() {
		return "ForgeScript";
	}

	public Map<Long, String> getConsoleText() {
		TreeMap map = new TreeMap();
		int tab = 0;
		Iterator var3 = this.getScripts().iterator();

		while(var3.hasNext()) {
			EventScriptContainer script = (EventScriptContainer)var3.next();
			++tab;
			Iterator var5 = script.console.entrySet().iterator();

			while(var5.hasNext()) {
				Map.Entry entry = (Map.Entry)var5.next();
				map.put(entry.getKey(), " tab " + tab + ":\n" + (String)entry.getValue());
			}
		}

		return map;
	}

	public void clearConsole() {
		Iterator var1 = this.getScripts().iterator();

		while(var1.hasNext()) {
			EventScriptContainer script = (EventScriptContainer)var1.next();
			script.console.clear();
		}

	}
}
