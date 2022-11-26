//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by Fernflower decompiler)
//

package noppes.npcs.controllers;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;
import javax.script.Compilable;
import javax.script.CompiledScript;
import javax.script.Invocable;
import javax.script.ScriptEngine;

import cpw.mods.fml.common.eventhandler.Event;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.controllers.data.IScriptHandler;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.scripted.NpcAPI;

public class ScriptContainer {
    private static final String lock = "lock";
    public static ScriptContainer Current;
    private static String CurrentType;
    private static final HashMap<String, Object> Data = new HashMap();
    public String fullscript = "";
    public String script = "";
    public TreeMap<Long, String> console = new TreeMap();
    public boolean errored = false;
    public List<String> scripts = new ArrayList();
    private HashSet<String> unknownFunctions = new HashSet();
    public long lastCreated = 0L;
    private String currentScriptLanguage = null;
    public ScriptEngine engine = null;
    private IScriptHandler handler = null;
    private boolean init = false;
    private static Method luaCoerce;
    private static Method luaCall;
    private CompiledScript compScript = null;

    public ScriptContainer(IScriptHandler handler) {
        this.handler = handler;
    }

    public void readFromNBT(NBTTagCompound compound) {
        this.script = compound.getString("Script");
        for (int i = 0; i < CustomNpcs.ExpandedScriptLimit; i++) {
            if (compound.hasKey("ExpandedScript"+i)) {
                this.script += compound.getString("ExpandedScript"+i);
            } else {
                break;
            }
        }
        //this.type = compound.getString("Type");
        this.console = NBTTags.GetLongStringMap(compound.getTagList("Console", 10));
        this.scripts = NBTTags.getStringList(compound.getTagList("ScriptList", 10));
        this.lastCreated = 0L;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (this.script.length() < 65535) {
            compound.setString("Script", this.script);
        } else {
            if (CustomNpcs.ExpandedScriptLimit > 0) {
                int i = 0;
                int length = this.script.length();
                while (length > 0 && i <= CustomNpcs.ExpandedScriptLimit) {
                    String str = "";
                    if (i == 0) {
                        compound.setString("Script", this.script.substring(0, 65535));
                        str = this.script.substring(0, 65535);
                    } else {
                        int end = (length - 65535) >= 0 ? 65535 * (i + 1) : 65535 * i + length;
                        str = this.script.substring(65535 * i, end);
                        compound.setString("ExpandedScript" + (i - 1), str);
                    }
                    i++;
                    length -= str.length();
                }
            } else {
                compound.setString("Script", this.script.substring(0, 65535));
            }
        }
        //compound.setString("Type", this.type);
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
        this.engine.getContext().setWriter(pw);
        this.engine.getContext().setErrorWriter(pw);

        try {
            if(compScript == null && engine instanceof Compilable)
                compScript = ((Compilable)engine).compile(getFullCode());

            if(compScript != null){
                compScript.eval(engine.getContext());
            }
            else
                engine.eval(getFullCode());
        } catch (Throwable var14) {
            this.errored = true;
            var14.printStackTrace(pw);
        } finally {
            String errorString = sw.getBuffer().toString().trim();
            this.appandConsole(errorString);
            pw.close();
        }
    }

    public void run(EnumScriptType type, Event event) {
        if(!CustomNpcs.ScriptingEnabled)
            return;

        this.run((String)type.function, (Object)event);
    }

    public void run(String type, Object event) {
        if(!CustomNpcs.ScriptingEnabled)
            return;

        if (!this.errored && this.hasCode() && !this.unknownFunctions.contains(type)) {
            this.setEngine(this.handler.getLanguage());
            if (this.engine != null) {
                if (ScriptController.Instance.lastLoaded > this.lastCreated) {
                    this.lastCreated = ScriptController.Instance.lastLoaded;
                    this.init = false;
                }

                engine.put("API", NpcAPI.Instance());

                Current = this;
                CurrentType = type;
                StringWriter sw = new StringWriter();
                PrintWriter pw = new PrintWriter(sw);
                this.engine.getContext().setWriter(pw);
                this.engine.getContext().setErrorWriter(pw);

                try {
                    if (!this.init) {
                        this.engine.eval(this.getFullCode());
                        this.init = true;
                    }

                    ((Invocable)this.engine).invokeFunction(type, new Object[]{event});
                } catch (NoSuchMethodException var13) {
                    this.unknownFunctions.add(type);
                } catch (Throwable var14) {
                    this.errored = true;
                    var14.printStackTrace(pw);
                } finally {
                    String errorString = sw.getBuffer().toString().trim();
                    this.appandConsole(errorString);
                    pw.close();
                    Current = null;
                }
            }
        }
    }

    public void appandConsole(String message) {
        if (message != null && !message.isEmpty()) {
            long time = System.currentTimeMillis();
            if (this.console.containsKey(time)) {
                message = (String)this.console.get(time) + "\n" + message;
            }

            this.console.put(time, message);

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
        if (CustomNpcs.ScriptingECMA6 && scriptLanguage.equals("ECMAScript")) {
            System.setProperty("nashorn.args", "--language=es6");
        }
        engine = ScriptController.Instance.getEngineByName(scriptLanguage.toLowerCase());
        currentScriptLanguage = scriptLanguage;
    }
}
