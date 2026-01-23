package noppes.npcs.janino;

import cpw.mods.fml.common.eventhandler.Event;
import io.github.somehussar.janinoloader.api.script.IScriptBodyBuilder;
import io.github.somehussar.janinoloader.api.script.IScriptClassBody;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.api.handler.IHookDefinition;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.constants.ScriptContext;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.ScriptHookController;
import noppes.npcs.controllers.data.IScriptUnit;
import org.codehaus.commons.compiler.InternalCompilerException;
import org.codehaus.commons.compiler.Sandbox;

import java.lang.invoke.MethodHandle;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.function.Supplier;

public abstract class JaninoScript<T> implements IScriptUnit {

    public ScriptContext context = ScriptContext.GLOBAL;
    public boolean errored = false;
    public String script = "";
    public List<String> externalScripts = new ArrayList<>();
    public TreeMap<Long, String> console = new TreeMap<>();
    public boolean evaluated;

    public final Class<T> type;
    public final IScriptBodyBuilder<T> builder;
    protected final Sandbox sandbox;
    protected IScriptClassBody<T> scriptBody;

    private final JaninoHookResolver hookResolver = new JaninoHookResolver();
    private final String[] defaultImports;

    private Map<String, IHookDefinition> hookDefCache;
    private int lastHookRevision = -1;
    private int lastSeenGlobalRevision;


    protected JaninoScript(Class<T> type, String[] defaultImports, boolean isClient) {
        this.type = type;
        this.defaultImports = defaultImports != null ? defaultImports : new String[0];
        this.builder = IScriptBodyBuilder.getBuilder(type,
            isClient ? CustomNpcs.getClientCompiler() : CustomNpcs.getDynamicCompiler())
            .setDefaultImports(this.defaultImports);

        Permissions permissions = new Permissions();
        permissions.setReadOnly();
        this.sandbox = new Sandbox(permissions);
        this.scriptBody = builder.build();
    }

    protected JaninoScript(Class<T> type, String[] defaultImports) {
        this(type, defaultImports, false);
    }

    protected String getHookContext(){
        return this.context.hookContext;
    }

    // ==================== COMPILATION ====================

    public void compileScript(String code) {
        try {
            String[] imports = collectImportsForCode(code);
            builder.setDefaultImports(imports);
            this.scriptBody = builder.build();
            scriptBody.setScript(code);
        } catch (InternalCompilerException e) {
            appendConsole("Compilation error: " + e.getMessage());
            Throwable cause = e;
            while (cause.getCause() != null) cause = cause.getCause();
            if (e != cause) appendConsole(cause.getMessage());
            hookResolver.clearResolutionCaches();
        } catch (Exception e) {
            appendConsole("Error: " + e.getMessage());
            hookResolver.clearResolutionCaches();
        }
    }

    public void compileScript() {
        compileScript(getFullCode());
    }

    public void ensureCompiled() {
        int global = ScriptController.Instance.globalRevision;
        if (!evaluated || global != lastSeenGlobalRevision) {
            compileScript();
            lastSeenGlobalRevision = global;
            evaluated = true;
        }
    }

    private String getFullCode() {
        StringBuilder sb = new StringBuilder();
        if (ConfigScript.RunLoadedScriptsFirst) appendExternalScripts(sb);
        if (script != null && !script.isEmpty()) sb.append(script).append("\n");
        if (!ConfigScript.RunLoadedScriptsFirst) appendExternalScripts(sb);
        return sb.toString();
    }

    private void appendExternalScripts(StringBuilder sb) {
        for (String name : externalScripts) {
            String code = ScriptController.Instance.scripts.get(name);
            if (code != null && !code.isEmpty()) sb.append(code).append("\n");
        }
    }

    // ==================== SMART IMPORTS ====================

    private String[] collectImportsForCode(String code) {
        Set<String> imports = new LinkedHashSet<>();
        Collections.addAll(imports, defaultImports);

        if (ScriptHookController.Instance == null) {
            return imports.toArray(new String[0]);
        }

        String context = getHookContext();
        if (context == null || context.isEmpty()) {
            return imports.toArray(new String[0]);
        }

        for (IHookDefinition def : ScriptHookController.Instance.getAllHookDefinitions(context)) {
            if (code.contains(def.hookName() + "(")) {
                String[] hookImports = def.requiredImports();
                if (hookImports != null) {
                    Collections.addAll(imports, hookImports);
                }
            }
        }

        return imports.toArray(new String[0]);
    }

    // ==================== HOOK EXECUTION ====================

    @Override
    public Object callFunction(String hookName, Object... args) {
        if (hookName == null || hookName.isEmpty()) return null;

        ensureCompiled();
        T instance = scriptBody.get();
        if (instance == null) return null;

        Object[] invokeArgs = args == null ? new Object[0] : args;
        MethodHandle handle = hookResolver.resolveHookHandle(hookName, invokeArgs, instance);
        if (handle == null) return null;

        try {
            return sandbox.confine((PrivilegedAction<Object>) () -> {
                try {
                    Object[] fullArgs = new Object[invokeArgs.length + 1];
                    fullArgs[0] = instance;
                    System.arraycopy(invokeArgs, 0, fullArgs, 1, invokeArgs.length);
                    return handle.invokeWithArguments(fullArgs);
                } catch (Throwable e) {
                    appendConsole("Error in " + hookName + ": " + e.getMessage());
                    return null;
                }
            });
        } catch (Exception e) {
            appendConsole("Runtime error in " + hookName + ": " + e.getMessage());
            return null;
        }
    }

    @Override
    public void run(EnumScriptType type, Event event) {
        if (type != null) callFunction(type.function, event);
    }

    @Override
    public void run(String hookName, Object event) {
        callFunction(hookName, event);
    }

    // ==================== HOOK DEFINITIONS ====================

    protected IHookDefinition getHookDefinition(String hookName) {
        if (ScriptHookController.Instance == null) return null;

        int rev = ScriptHookController.Instance.getHookRevision();
        if (hookDefCache == null || rev != lastHookRevision) {
            rebuildHookDefCache();
            lastHookRevision = rev;
        }
        return hookDefCache.get(hookName);
    }

    private void rebuildHookDefCache() {
        hookDefCache = new HashMap<>();
        String context = getHookContext();
        if (context == null || context.isEmpty() || ScriptHookController.Instance == null) return;

        for (IHookDefinition def : ScriptHookController.Instance.getAllHookDefinitions(context)) {
            hookDefCache.put(def.hookName(), def);
        }
    }

    public List<String> getHookList() {
        String context = getHookContext();
        if (context != null && !context.isEmpty() && ScriptHookController.Instance != null) {
            return ScriptHookController.Instance.getAllHooks(context);
        }
        return Collections.emptyList();
    }

    @Override
    public String generateHookStub(String hookName, Object hookData) {
        IHookDefinition def = getHookDefinition(hookName);
        if (def != null) {
            String typeName = def.getUsableTypeName();
            String[] params = def.paramNames();
            String paramName = (params != null && params.length > 0) ? params[0] : "event";

            if (typeName != null && !typeName.isEmpty()) {
                return String.format("public void %s(%s %s) {\n    \n}\n",
                    def.hookName(), typeName, paramName);
            }
        }
        return String.format("public void %s(Object event) {\n    \n}\n", hookName);
    }

    // ==================== IScriptUnit ====================

    @Override public String getScript() { return script; }

    @Override
    public void setScript(String script) {
        this.script = script;
        this.evaluated = false;
        hookResolver.clearResolutionCaches();
    }

    @Override public List<String> getExternalScripts() { return externalScripts; }

    @Override
    public void setExternalScripts(List<String> scripts) {
        this.externalScripts = scripts;
        this.evaluated = false;
        hookResolver.clearResolutionCaches();
    }

    @Override public TreeMap<Long, String> getConsole() { return console; }
    @Override public void clearConsole() { console.clear(); }

    @Override
    public void appendConsole(String message) {
        if (message == null || message.isEmpty()) return;
        long time = System.currentTimeMillis();
        if (console.containsKey(time)) {
            message = console.get(time) + "\n" + message;
        }
        console.put(time, message);
        while (console.size() > 40) {
            console.remove(console.firstKey());
        }
    }

    @Override public String getLanguage() { return "Java"; }
    @Override public void setLanguage(String language) { }

    @Override
    public boolean hasCode() {
        return !externalScripts.isEmpty() || (script != null && !script.isEmpty());
    }

    @Override public boolean hasErrored() { return errored; }
    @Override public void setErrored(boolean errored) { this.errored = errored; }

    // ==================== NBT ====================

    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString(IScriptUnit.NBT_TYPE_KEY, IScriptUnit.TYPE_JANINO);
        compound.setTag("console", NBTTags.NBTLongStringMap(console));
        compound.setTag("externalScripts", NBTTags.nbtStringList(externalScripts));
        compound.setString("script", script);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.console = NBTTags.GetLongStringMap(compound.getTagList("console", 10));
        setExternalScripts(NBTTags.getStringList(compound.getTagList("externalScripts", 10)));
        setScript(compound.getString("script"));
    }

    public static <T, S extends JaninoScript<T>> S readFromNBT(NBTTagCompound compound, S script, Supplier<S> factory) {
        if (compound.hasKey("Script")) {
            if (script == null) script = factory.get();
            script.readFromNBT(compound.getCompoundTag("Script"));
        }
        return script;
    }

    public static <T> NBTTagCompound writeToNBT(NBTTagCompound compound, JaninoScript<T> script) {
        if (script != null) compound.setTag("Script", script.writeToNBT(new NBTTagCompound()));
        return compound;
    }
}
