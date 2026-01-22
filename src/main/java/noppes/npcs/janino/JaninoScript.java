package noppes.npcs.janino;

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
import org.codehaus.commons.compiler.InternalCompilerException;
import org.codehaus.commons.compiler.Sandbox;

import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.*;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
import java.util.function.Supplier;
import java.util.stream.Collectors;

public abstract class JaninoScript<T> implements IScriptUnit {
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

    private final JaninoHookResolver hookResolver;

    private final String[] defaultImports;

    protected JaninoScript(Class<T> type, String[] defaultImports, boolean isClient) {
        this.type = type;
        this.defaultImports = defaultImports != null ? defaultImports : new String[0];
        this.builder = IScriptBodyBuilder.getBuilder(type, isClient ? CustomNpcs.getClientCompiler() : CustomNpcs.getDynamicCompiler())
            .setDefaultImports(defaultImports);

        Permissions permissions = new Permissions();
        permissions.setReadOnly();
        sandbox = new Sandbox(permissions);

        this.scriptBody = builder.build();

        this.hookResolver = new JaninoHookResolver(type);
    }

    protected JaninoScript(Class<T> type, String[] defaultImports) {
        this(type, defaultImports, false);
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
            hookResolver.clearResolutionCaches();
        } catch (Exception e) {
            appendConsole("Unknown error: " + e.getMessage());
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

    /**
     * globalRevision is incremented on server
     * each time externalScripts are updated
     * then sent to client.
     */
    private int lastSeenGlobalRevision;

    private String getFullCode() {
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

    public void callByHookName(String hookName, Object arg) {
        callFunction(hookName, arg);
    }

    @Override
    public Object callFunction(String hookName, Object... args) {
        if (hookName == null || hookName.isEmpty())
            return null;

        // Ensure compiled first so we can search the compiled class
        ensureCompiled();
        T t = getUnsafe();
        if (t == null)
            return null;

        // Pass compiled instance to enable dynamic hook resolution
        // This allows users to define methods for ANY hook (Forge events, addon hooks, etc.)
        Method method = hookResolver.resolveHookMethod(hookName, args, t);
        if (method == null)
            return null;

        Method invokeMethod = method;
        Object[] invokeArgs = args == null ? new Object[0] : args;

        try {
            return sandbox.confine((PrivilegedAction<Object>) () -> {
                try {
                    return invokeMethod.invoke(t, invokeArgs);
                } catch (Exception e) {
                    appendConsole("Error calling hook " + hookName + ": " + e.getMessage());
                    return null;
                }
            });
        } catch (Exception e) {
            appendConsole("Runtime Error in hook " + hookName + ": " + e.getMessage());
            return null;
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
        if (type == null)
            return;

        callFunction(type.function, event);
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
        callFunction(hookName, event);
    }

    @Override
    public String getScript() {
        return this.script;
    }

    @Override
    public void setScript(String script) {
        this.script = script;
        this.evaluated = false;
        hookResolver.clearResolutionCaches();
    }

    @Override
    public List<String> getExternalScripts() {
        return this.externalScripts;
    }

    @Override
    public void setExternalScripts(List<String> externalScripts) {
        this.externalScripts = externalScripts;
        this.evaluated = false;
        hookResolver.clearResolutionCaches();
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
         hookResolver.clearResolutionCaches();

         Method method = hookResolver.getMethodForDisplayName(hookName);
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


    public List<String> getHookList() {
        return hookResolver.getDisplayHookList();
    }


    @Override
    public NBTTagCompound writeToNBT(NBTTagCompound compound) {
        compound.setString(IScriptUnit.NBT_TYPE_KEY, IScriptUnit.TYPE_JANINO);
        compound.setTag("console", NBTTags.NBTLongStringMap(this.console));
        compound.setTag("externalScripts", NBTTags.nbtStringList(this.externalScripts));
        compound.setString("script", script);
        return compound;
    }

    @Override
    public void readFromNBT(NBTTagCompound compound) {
        // Read legacy enabled field but ignore it (handler owns enabled now)
        this.console = NBTTags.GetLongStringMap(compound.getTagList("console", 10));
        setExternalScripts(NBTTags.getStringList(compound.getTagList("externalScripts", 10)));
        setScript(compound.getString("script"));
    }

    public static <T, S extends JaninoScript<T>> S readFromNBT(NBTTagCompound compound, S script, Supplier<S> factory) {
        if (compound.hasKey("Script")) {
            if (script == null)
                script = factory.get();

            script.readFromNBT(compound.getCompoundTag("Script"));
        }
        return script;
    }

    public static <T> NBTTagCompound writeToNBT(NBTTagCompound compound, JaninoScript<T> script) {
        if (script != null)
            compound.setTag("Script", script.writeToNBT(new NBTTagCompound()));

        return compound;
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
