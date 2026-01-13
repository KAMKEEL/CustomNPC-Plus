package noppes.npcs.janino;

import cpw.mods.fml.common.FMLCommonHandler;
import cpw.mods.fml.common.eventhandler.Event;
import io.github.somehussar.janinoloader.api.script.IScriptBodyBuilder;
import io.github.somehussar.janinoloader.api.script.IScriptClassBody;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.CustomNpcs;
import noppes.npcs.NBTTags;
import noppes.npcs.config.ConfigScript;
import noppes.npcs.constants.EnumScriptType;
import noppes.npcs.controllers.ScriptController;
import noppes.npcs.controllers.data.IScriptUnit;
import noppes.npcs.janino.annotations.ParamName;
import noppes.npcs.janino.annotations.ScriptHook;
import org.codehaus.commons.compiler.InternalCompilerException;
import org.codehaus.commons.compiler.Sandbox;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.stream.Collectors;

public abstract class JaninoScript<T> implements IScriptUnit {
    public boolean enabled;
    public boolean errored = false;
    private String language = "Java";
    public TreeMap<Long, String> console = new TreeMap();

    /**
     * Text written in the script GUI
     */
    public String script = "";
    /**
     * This script's selected external files
     *  stored in "world/customnpcs/scripts/java"
     */
    public List<String> externalScripts = new ArrayList<>();
    /**
     *  To evaluate if script/externalScripts were changed or not,
     *  and compile the fullScript freshly if so.
     */
    public boolean evaluated;

    public final Class<T> type;
    protected final Sandbox sandbox;
    public final IScriptBodyBuilder<T> builder;
    protected IScriptClassBody<T> scriptBody;

    private boolean hooksInitialized = false;
    // Hook list for GUI display
    private List<String> hookList = new ArrayList<>();
    // Map from display name (with parameters if overloaded) to Method
    private Map<String, Method> hookMap = new HashMap<>();
    // Map from EnumScriptType to Method for OLD tab-based script system
    private Map<EnumScriptType, Method> hookTypeMap = new HashMap<>();

    protected JaninoScript(Class<T> type, Consumer<IScriptBodyBuilder<T>> buildSettings) {
        this.type = type;
        this.builder = IScriptBodyBuilder
            .getBuilder(type, CustomNpcs.getClientCompiler());
        buildSettings.accept(builder);

        Permissions permissions = new Permissions();
        permissions.setReadOnly();
        sandbox = new Sandbox(permissions);

        this.scriptBody = builder.build();

        // Build hook type map from @ScriptHook annotations
        initializeHookTypeMap();
    }

    protected T getUnsafe() {
        return scriptBody.get();
    }

    public <R> R call(Function<T, R> fn) {
        ensureCompiled();

        T t = getUnsafe();
        if (t == null)
            return null;

//        CodeSource cs = t.getClass().getProtectionDomain().getCodeSource();
//        ProtectionDomain pd = new ProtectionDomain(cs, new Permissions());

        try {
            return sandbox.confine((PrivilegedAction<R>) () -> fn.apply(t));
//            return AccessController.doPrivileged((PrivilegedAction<? extends R>) () -> fn.apply(t), new AccessControlContext(
//                new ProtectionDomain[] {
//                    pd
//                }
//            ));
        } catch (Exception e) {
            appendConsole("Runtime Error: " + e.getMessage());
            return null;
        }
    }

    public void run(Consumer<T> fn) {
        ensureCompiled();

        T t = getUnsafe();
        if (t == null)
            return;

        try {
            sandbox.confine((PrivilegedAction<Void>) () -> {
                fn.accept(t);
                return null;
            });
        } catch (Exception e) {
            appendConsole("Runtime Error: " + e.getMessage());
        }
    }

    public void unload() {
    }

    /**
     * Feed the code into the engine and compile it
     */
    public void compileScript(String code) {

        try {
            scriptBody.setScript(code);
        } catch (InternalCompilerException e ) {
            Throwable parentCause = null;
            Throwable cause = e;

            appendConsole("Compilation error: " + e.getMessage());

            while (cause.getCause() != null) { parentCause = cause; cause = cause.getCause(); }
            Throwable rootCause = cause;

            if (e != cause) {
                appendConsole("\n");

                if (parentCause != null)
                    appendConsole(parentCause.getMessage());
                appendConsole(rootCause.getMessage());
            }
        } catch (Exception e) {
            appendConsole("Unknown error: " + e.getMessage());
        }
    }

    public void compileScript() {
        compileScript(getFullCode());
    }

    public void ensureCompiled() {
        if (!isEnabled()) {
            if (!evaluated) {
                compileScript("");
                evaluated = true;
            }
            return;
        }

        int global = ScriptController.Instance.globalRevision;

        if (!evaluated || global != lastSeenGlobalRevision) {
            compileScript();
            lastSeenGlobalRevision = global;
            evaluated = true;
        }
    }

    /**
     * globalRevision is incremented on server
     * each time externalScripts are updated
     * then sent to client.
     */
    private int lastSeenGlobalRevision;

    private String getFullCode() {
        if (!isEnabled())
            return "";

        StringBuilder sb = new StringBuilder();

        if (ConfigScript.RunLoadedScriptsFirst)
            this.appendExternalScripts(sb);

        // Main script
        if (this.script != null && !this.script.isEmpty())
            sb.append(this.script).append("\n");

        if (!ConfigScript.RunLoadedScriptsFirst)
            this.appendExternalScripts(sb);

        return sb.toString();
    }

    private void appendExternalScripts(StringBuilder sb) {
        for (String scriptName : externalScripts) {
            String code = ScriptController.Instance.scripts.get(scriptName);
            if (code != null && !code.isEmpty())
                sb.append(code).append("\n");
        }
    }

    /**
     * Scans the Functions interface for @ScriptHook annotations and builds the hookTypeMap.
     * This allows direct method invocation by EnumScriptType without switch statements.
     */
    private void initializeHookTypeMap() {
        for (Method method : type.getDeclaredMethods()) {
            ScriptHook annotation = method.getAnnotation(ScriptHook.class);
            if (annotation != null) {
                for (EnumScriptType hookType : annotation.value()) {
                    hookTypeMap.put(hookType, method);
                }
            }
        }
    }

    /**
     * Call a hook method by its EnumScriptType.
     * Used by the OLD tab-based script system to execute hooks directly.
     *
     * @param hookType The script type/hook to execute
     * @param event The event object to pass to the hook method
     */
    public void callByHookType(EnumScriptType hookType, Object event) {
        Method method = hookTypeMap.get(hookType);
        if (method == null) {
            // Hook not implemented or not annotated
            return;
        }

        ensureCompiled();
        T t = getUnsafe();
        if (t == null)
            return;

        try {
            sandbox.confine((PrivilegedAction<Void>) () -> {
                try {
                    method.invoke(t, event);
                } catch (Exception e) {
                    appendConsole("Error calling hook " + hookType.name() + ": " + e.getMessage());
                }
                return null;
            });
        } catch (Exception e) {
            appendConsole("Runtime Error in hook " + hookType.name() + ": " + e.getMessage());
        }
    }

    // ==================== IScriptUnit IMPLEMENTATION ====================

    /**
     * Execute this script unit for the given hook type and event.
     * Implementation of IScriptUnit.run(EnumScriptType, Event)
     *
     * @param type The script hook type
     * @param event The event object
     */
    @Override
    public void run(EnumScriptType type, Event event) {
        callByHookType(type, event);
    }

    /**
     * Execute this script unit for the given hook name and event.
     * Implementation of IScriptUnit.run(String, Event)
     *
     * @param hookName The hook name (e.g., "init", "interact")
     * @param event The event object
     */
    @Override
    public void run(String hookName, Object event) {
        try {
            EnumScriptType enumType = EnumScriptType.valueOfIgnoreCase(hookName);
            callByHookType(enumType, event);
        } catch (IllegalArgumentException ignored) {
            // Hook name doesn't map to an EnumScriptType
        }
    }
    @Override
    public String getScript() {
        return this.script;
    }

    @Override
    public void setScript(String script) {
        this.script = script;
        this.evaluated = false;
    }
    
    @Override
    public List<String> getExternalScripts() {
        return this.externalScripts;
    }

    @Override
    public void setExternalScripts(List<String> externalScripts) {
        this.externalScripts = externalScripts;
        this.evaluated = false;
    }
    
    @Override
    public TreeMap<Long, String> getConsole() {
        return this.console;
    }
    
    @Override
    public void clearConsole() {
        console.clear();
    }
    
    @Override
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
    
    @Override
    public String getLanguage() {
        return this.language;
    }
    
    @Override
    public void setLanguage(String language) {
        // JaninoScript is always Java, ignore attempts to change
        // This method exists for IScriptUnit interface compatibility
    }
    
    @Override
    public boolean hasCode() {
        if (!externalScripts.isEmpty())
            return true;
        return script != null && !script.isEmpty();
    }

    @Override
    public String generateHookStub(String hookName, Object hookData) {
        // Build hook list and map
        if (!hooksInitialized) {
            createHookList();
            hooksInitialized = true;
        }

        // Look up in hookMap (handles both simple names and overloaded display names)
        Method method = hookMap.get(hookName);
        if (method != null)
            return generateMethodStub(method);

        // Fallback for unknown hook names - generate basic override
        return String.format("public void %s() {\n    \n}\n", hookName);
    }
    
    /**
     * Generates a stub string for a given Method.
     */
    public static String generateMethodStub(Method method) {
        String mods = Modifier.toString(method.getModifiers());
        // Remove 'abstract' from modifiers for implementation
        mods = mods.replace("abstract ", "").replace("abstract", "").trim();
        if (!mods.isEmpty())
            mods += " ";

        String returnTypeStr = method.getReturnType().getSimpleName();
        String name = method.getName();

        Map<String, Integer> typeCount = new HashMap<>();
        String params = Arrays.stream(method.getParameters())
            .map(p -> {
                String typeName = p.getType().getSimpleName();

                // Check for @ParamName annotation first
                ParamName paramNameAnnotation = p.getAnnotation(ParamName.class);
                String paramName;

                if (paramNameAnnotation != null) {
                    // Use annotated name
                    paramName = paramNameAnnotation.value();
                } else {
                    // Fall back to generated name
                    String baseName = Character.toLowerCase(typeName.charAt(0)) + typeName.substring(1);
                    int count = typeCount.getOrDefault(baseName, 0) + 1;
                    typeCount.put(baseName, count);
                    paramName = count == 1 ? baseName : baseName + (count - 1);
                }
                
                return typeName + " " + paramName;
            })
            .collect(Collectors.joining(", "));

        String body;
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class)
            body = "";
        else if (returnType.isPrimitive()) {
            if (returnType == boolean.class)
                body = "    return false;";
            else if (returnType == char.class)
                body = "    return '\\0';";
            else
                body = "    return 0;";
        } else {
            body = "    return null;";
        }

        return String.format("%s%s %s(%s) {\n%s\n}\n", mods, returnTypeStr, name, params, body);
    }


    /**
     * Create the hook list and hook map for GUI display and generation.
     * Groups methods by name and creates display names with parameters for overloaded methods.
     */
    private void createHookList() {
        // Group all declared methods by their name
        Map<String, List<Method>> grouped = Arrays.stream(type.getDeclaredMethods())
                                                  .collect(Collectors.groupingBy(Method::getName));

        for (Map.Entry<String, List<Method>> entry : grouped.entrySet()) {
            List<Method> methods = entry.getValue();

            // Sort overloads by parameter count (ascending)
            methods.sort(Comparator.comparingInt(Method::getParameterCount));
            boolean first = true;

            for (Method m : methods) {
                if (Modifier.isFinal(m.getModifiers()))
                    continue; // Skip final methods

                // For first (simplest) overload, use just the method name
                // For additional overloads, include parameter types in display name
                String displayName = first ? m.getName() :
                        m.getName() + "(" + Arrays.stream(m.getParameterTypes())
                                                  .map(Class::getSimpleName)
                                                  .collect(Collectors.joining(", ")) + ")";

                hookList.add(displayName);
                hookMap.put(displayName, m);
                first = false;
            }
        }
    }

    /**
     * Get the list of hook names for GUI display.
     */
    public List<String> getHookList() {
        return new ArrayList<>(hookList);
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString(IScriptUnit.NBT_TYPE_KEY, IScriptUnit.TYPE_JANINO);
        compound.setBoolean("enabled", enabled);
        compound.setTag("console", NBTTags.NBTLongStringMap(this.console));
        compound.setTag("externalScripts", NBTTags.nbtStringList(this.externalScripts));
        compound.setString("script", script);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        this.enabled = compound.getBoolean("enabled");
        this.console = NBTTags.GetLongStringMap(compound.getTagList("console", 10));
        setExternalScripts(NBTTags.getStringList(compound.getTagList("externalScripts", 10)));
        setScript(compound.getString("script"));
    }

    public boolean isEnabled() {
        return this.enabled && ScriptController.HasStart && ConfigScript.ScriptingEnabled;
    }

    public boolean isClient() {
        return FMLCommonHandler.instance()
            .getEffectiveSide()
            .isClient();
    }

    public boolean getEnabled() {
        return this.enabled;
    }

    public void setEnabled(boolean b) {
        if (this.enabled != b) {
            this.enabled = b;
            this.evaluated = false;
        }
    }

    public Map<Long, String> getConsoleText() {
        TreeMap<Long, String> map = new TreeMap();
        int tab = 0;

        Iterator var5 = console.entrySet()
            .iterator();

        while (var5.hasNext()) {
            Map.Entry<Long, String> longStringEntry = (Map.Entry) var5.next();
            map.put(longStringEntry.getKey(), " tab " + tab + ":\n" + (String) longStringEntry.getValue());
        }

        return map;
    }
    
    @Override
    public boolean hasErrored() {
        return this.errored;
    }
    
    @Override
    public void setErrored(boolean errored) {
        this.errored = errored;
    }
}