package noppes.npcs.controllers;

import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;

import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.ScriptEngine;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.List;

public class ScriptContainer {
	public String fullscript = "";
	public String script = "";
	public String console = "";
	public boolean errored = false;
	public List<String> scripts = new ArrayList<String>();
	private long lastCreated = 0;
	private CompiledScript compScript = null;
	private String currentScriptLanguage = null;
	public ScriptEngine engine = null;

	public void readFromNBT(NBTTagCompound compound) {
		script = compound.getString("Script");
		console = compound.getString("ScriptConsole");
		scripts = NBTTags.getStringList(compound.getTagList("ScriptList", 10));
		lastCreated = 0;
	}

	public void writeToNBT(NBTTagCompound compound) {
		compound.setString("Script", script);
		compound.setString("ScriptConsole", console);
		compound.setTag("ScriptList", NBTTags.nbtStringList(scripts));
	}

	public String getCode() {
		if(ScriptController.Instance.lastLoaded > lastCreated){
			lastCreated = ScriptController.Instance.lastLoaded;
			fullscript = script;
			if(!fullscript.isEmpty())
				fullscript += "\n";
			for(String loc : scripts){
				String code = ScriptController.Instance.scripts.get(loc);
				if(code != null && !code.isEmpty())
					fullscript += code + "\n";
			}
			compScript = null;
		}
		return fullscript;
	}
	
	public void run(ScriptEngine engine){
		StringWriter sw = new StringWriter();
	    PrintWriter pw = new PrintWriter(sw);
	    engine.getContext().setWriter(pw);
	    engine.getContext().setErrorWriter(pw);
		try {
			if(compScript == null && engine instanceof Compilable)
				compScript = ((Compilable)engine).compile(getCode());
			
			if(compScript != null){
				compScript.eval(engine.getContext());
			}
			else
				engine.eval(getCode());
			
		} catch (Exception e) {
			errored = true;
			appandConsole(e.getMessage());
		}
		appandConsole(sw.getBuffer().toString().trim());
	}

	public void appandConsole(String message) {
		if(message == null || message.isEmpty())
			return;
		console = message + "\n" + console;
	}

	public boolean hasCode() {
		return !getCode().isEmpty();
	}

	public void setEngine(String scriptLanguage) {
		if(currentScriptLanguage != null && currentScriptLanguage.equals(scriptLanguage))
			return;
		engine = ScriptController.Instance.getEngineByName(scriptLanguage.toLowerCase());
		currentScriptLanguage = scriptLanguage;
	}
}
