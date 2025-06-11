package noppes.npcs.controllers;

import cpw.mods.fml.common.eventhandler.Event;
import jdk.nashorn.api.scripting.ScriptObjectMirror;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.NBTTags;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.data.IScriptHandler;
import noppes.npcs.scripted.NpcAPI;

import javax.script.*;
import java.io.PrintWriter;
import java.io.StringWriter;
import java.lang.reflect.Method;
import java.util.*;

public class ScriptContainer {
    private static final String lock = "lock";
    public static ScriptContainer Current;
    private static String CurrentType;
    public String fullscript = "";
    public String script = "";
    public TreeMap<Long, String> console = new TreeMap<>();
    public boolean errored = false;
    public List<String> scripts = new ArrayList<>();
    private HashSet<String> unknownFunctions = new HashSet<>();
    public long lastCreated = 0L;
    private String currentScriptLanguage = null;
    public ScriptEngine engine = null;
    private IScriptHandler handler = null;
    private boolean evaluated = false;
    private static Method luaCoerce;
    private static Method luaCall;
    private CompiledScript compScript = null;
    private final HashMap<String, ScriptObjectMirror> cachedFunctions = new HashMap<>();

    public ScriptContainer(IScriptHandler handler) {
        this.handler = handler;
    }

    public void readFromNBT(NBTTagCompound compound) {
        String prevScript = this.script;
        this.script = compound.getString("Script");
        for (int i = 0; i < ConfigScript.ExpandedScriptLimit; i++) {
            if (compound.hasKey("ExpandedScript" + i)) {
                this.script += compound.getString("ExpandedScript" + i);
            } else {
                break;
            }
        }

        if (!this.script.equals(prevScript)) {
            this.evaluated = false;
        }

        this.console = NBTTags.GetLongStringMap(compound.getTagList("Console", 10));
        this.scripts = NBTTags.getStringList(compound.getTagList("ScriptList", 10));
        this.lastCreated = 0L;
    }

    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        if (this.script.length() < 65535) {
            compound.setString("Script", this.script);
        } else {
            if (ConfigScript.ExpandedScriptLimit > 0) {
                int i = 0;
                int length = this.script.length();
                while (length > 0 && i <= ConfigScript.ExpandedScriptLimit) {
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
        if (!this.evaluated) {
            // build includes first
            StringBuilder sb = new StringBuilder();
            if (ConfigScript.RunLoadedScriptsFirst) {
                this.appendExternalScripts(sb);
            }
            // then your per‐hook script
            if (this.script != null && !this.script.isEmpty()) {
                sb.append(this.script).append("\n");
            }
            if (!ConfigScript.RunLoadedScriptsFirst) {
                this.appendExternalScripts(sb);
            }
            this.fullscript = sb.toString();
            this.unknownFunctions = new HashSet<>();
        }
        return this.fullscript;
    }

    private void appendExternalScripts(StringBuilder sb) {
        for (String loc : this.scripts) {
            String code = ScriptController.Instance.scripts.get(loc);
            if (code != null && !code.isEmpty()) {
                sb.append(code).append("\n");
            }
        }
    }

    public void run(ScriptEngine engine) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        this.engine.getContext().setWriter(pw);
        this.engine.getContext().setErrorWriter(pw);

        try {
            if (compScript == null && engine instanceof Compilable)
                compScript = ((Compilable) engine).compile(getFullCode());

            if (compScript != null) {
                compScript.eval(engine.getContext());
            } else {
                engine.eval(getFullCode());
            }
        } catch (Throwable var14) {
            this.errored = true;
            var14.printStackTrace(pw);
        } finally {
            String errorString = sw.getBuffer().toString().trim();
            this.appendConsole(errorString);
            pw.close();
        }
    }

    public void run(EnumScriptType type, Event event) {
        if (!ConfigScript.ScriptingEnabled)
            return;

        this.run((String) type.function, (Object) event);
    }

    public void run(String type, Object event) {
        if (!ConfigScript.ScriptingEnabled || errored || !hasCode() || unknownFunctions.contains(type))
            return;

        this.setEngine(handler.getLanguage());
        if (engine == null)
            return;

        if (ScriptController.Instance.lastLoaded > this.lastCreated) {
            this.lastCreated = ScriptController.Instance.lastLoaded;
            evaluated = false;
        }

        synchronized (lock) {
            Current = this;
            CurrentType = type;

            StringWriter sw = new StringWriter();
            PrintWriter pw = new PrintWriter(sw);

            engine.getContext().setWriter(pw);
            engine.getContext().setErrorWriter(pw);

            engine.put("API", NpcAPI.Instance());
            HashMap<String, Object> engineEntries = new HashMap<>(NpcAPI.engineObjects);
            for (Map.Entry<String, Object> objectEntry : engineEntries.entrySet()) {
                engine.put(objectEntry.getKey(), objectEntry.getValue());
            }

            try {
                if (!evaluated) {
                    this.cachedFunctions.clear();
                    engine.eval(getFullCode());
                    evaluated = true;
                }
                if (engine.getFactory().getLanguageName().equals("lua")) {
                    Object ob = engine.get(type);
                    if (ob != null) {
                        if (luaCoerce == null) {
                            luaCoerce = Class.forName("org.luaj.vm2.lib.jse.CoerceJavaToLua").getMethod("coerce", Object.class);
                            luaCall = ob.getClass().getMethod("call", Class.forName("org.luaj.vm2.LuaValue"));
                        }
                        luaCall.invoke(ob, luaCoerce.invoke(null, event));
                    } else {
                        unknownFunctions.add(type);
                    }
                } else {
                    if (!this.cachedFunctions.containsKey(type)) {
                        ScriptObjectMirror global = (ScriptObjectMirror) engine.getBindings(ScriptContext.ENGINE_SCOPE);
                        ScriptObjectMirror func = (ScriptObjectMirror) global.get(type);
                        this.cachedFunctions.put(type, func);
                    }
                    ScriptObjectMirror func = this.cachedFunctions.get(type);
                    if (func != null) {
                        func.call(null, event);
                    }
                }
            } catch (NoSuchMethodException e) {
                unknownFunctions.add(type);
            } catch (Throwable e) {
                errored = true;
                e.printStackTrace(pw);
            } finally {
                appendConsole(sw.getBuffer().toString().trim());
                pw.close();
                Current = null;
            }
        }
    }

    public void appendConsole(String message) {
        if (message != null && !message.isEmpty()) {
            long time = System.currentTimeMillis();
            if (this.console.containsKey(time)) {
                message = (String) this.console.get(time) + "\n" + message;
            }

            this.console.put(time, message);

            while (this.console.size() > 40) {
                this.console.remove(this.console.firstKey());
            }
        }
    }

    public boolean isValid() {
        return evaluated && !errored;
    }

    public boolean hasCode() {
        if (!scripts.isEmpty())
            return true;
        return !this.getFullCode().isEmpty();
    }

    public void setEngine(String scriptLanguage) {
        if (!Objects.equals(scriptLanguage, this.currentScriptLanguage)) {
            this.currentScriptLanguage = scriptLanguage;
            if (ConfigScript.ScriptingECMA6 && scriptLanguage.equals("ECMAScript")) {
                System.setProperty("nashorn.args", "--language=es6");
            }
            this.engine = ScriptController.Instance.getEngineByName(scriptLanguage.toLowerCase());
            this.evaluated = false;
        }
    }
}
