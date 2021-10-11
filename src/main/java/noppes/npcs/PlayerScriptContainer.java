//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.TreeMap;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.util.math.BlockPos;
import cpw.mods.fml.common.eventhandler.Event;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.IScriptHandler;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.scripted.constants.*;

public class PlayerScriptContainer {
    public static PlayerScriptContainer Current;
    public String type = "init";
    public String fullscript = "";
    public String script = "";
    public TreeMap<Long, String> console = new TreeMap();
    public boolean errored = false;
    public List<String> scripts = new ArrayList();
    public long lastCreated = 0L;
    private String currentScriptLanguage = null;
    public ScriptEngine engine = null;
    private IScriptHandler handler = null;
    private CompiledScript compScript = null;
    private boolean init = false;

    public PlayerScriptContainer(IScriptHandler handler) {
        this.handler = handler;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.script = compound.getString("Script");
        this.type = compound.getString("Type");
        this.console = NBTTags.GetLongStringMap(compound.getTagList("Console", 10));
        this.scripts = NBTTags.getStringList(compound.getTagList("ScriptList", 10));
        this.lastCreated = 0L;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString("Script", this.script);
        compound.setString("Type", this.type);
        compound.setTag("Console", NBTTags.NBTLongStringMap(this.console));
        compound.setTag("ScriptList", NBTTags.nbtStringList(this.scripts));
        return compound;
    }

    private String getFullCode() {
        if(!this.init) {
            this.fullscript = this.script;
            if(!this.fullscript.isEmpty()) {
                this.fullscript = this.fullscript + "\n";
            }

            Iterator var1 = this.scripts.iterator();

            while(var1.hasNext()) {
                String loc = (String)var1.next();
                String code = (String)ScriptController.Instance.scripts.get(loc);
                if(code != null && !code.isEmpty()) {
                    this.fullscript = this.fullscript + code + "\n";
                }
            }
        }

        return this.fullscript;
    }
    public void run(ScriptEngine engine){
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        engine.getContext().setWriter(pw);
        engine.getContext().setErrorWriter(pw);
        try {
            if(compScript == null && engine instanceof Compilable)
                compScript = ((Compilable)engine).compile(getFullCode());

            if(compScript != null){
                compScript.eval(engine.getContext());
            }
            else
                engine.eval(getFullCode());

        } catch (Exception e) {
            errored = true;
            appandConsole(e.getMessage());
        }
        appandConsole(sw.getBuffer().toString().trim());
    }

    public void setType(String type) {
        this.type = type;
    }

    public void appandConsole(String message) {
        if(message != null && !message.isEmpty()) {
            long time = System.currentTimeMillis();
            if(this.console.containsKey(Long.valueOf(time))) {
                message = (String)this.console.get(Long.valueOf(time)) + "\n" + message;
            }

            this.console.put(Long.valueOf(time), message);

            while(this.console.size() > 40) {
                this.console.remove(this.console.firstKey());
            }

        }
    }

    public boolean hasCode() {
        return !this.getFullCode().isEmpty();
    }

    public void setEngine(String scriptLanguage) {
        if(currentScriptLanguage != null && currentScriptLanguage.equals(scriptLanguage))
            return;
        engine = ScriptController.Instance.getEngineByName(scriptLanguage.toLowerCase());
        currentScriptLanguage = scriptLanguage;
    }
}
