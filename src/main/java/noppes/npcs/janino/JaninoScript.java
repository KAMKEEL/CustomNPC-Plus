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
import noppes.npcs.janino.annotations.ParamName;
import org.codehaus.commons.compiler.InternalCompilerException;
import org.codehaus.commons.compiler.Sandbox;

import java.lang.invoke.MethodHandle;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.security.Permissions;
import java.security.PrivilegedAction;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Function;
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
    // Cache of imports used in the last compilation
    private String[] cachedImports;
    
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

    protected String getHookContext() {
        return this.context.hookContext;
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


    // ==================== COMPILATION ====================

    public void compileScript(String code) {
        try {
            builder.setDefaultImports(cachedImports = collectImportsForCode(code));
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
        Set<String> hooks = new LinkedHashSet<>();

        // 1. Add hooks from ScriptHookController (new system)
        String context = getHookContext();
        if (context != null && !context.isEmpty() && ScriptHookController.Instance != null) {
            hooks.addAll(ScriptHookController.Instance.getAllHooks(context));
        }

        // 2. Add methods from interface type (backward compatibility with @ParamName)
        if (type != null) {
            for (Method m : type.getDeclaredMethods()) {
                if (!Modifier.isFinal(m.getModifiers()) && !hooks.contains(m.getName())) {
                    hooks.add(m.getName());
                }
            }
        }

        return new ArrayList<>(hooks);
    }

    @Override
    public String generateHookStub(String hookName, Object hookData) {
        // 1. Try HookDefinition from registry (new system)
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

        // 2. Fallback: Try interface method with @ParamName (backward compatibility)
        Method method = findInterfaceMethod(hookName);
        if (method != null) {
            return generateMethodStub(method);
        }

        // 3. Final fallback: generic stub
        return String.format("public void %s(Object event) {\n    \n}\n", hookName);
    }

    /**
     * Find a method in the type interface by name.
     */
    private Method findInterfaceMethod(String methodName) {
        if (type == null || methodName == null) return null;

        for (Method m : type.getDeclaredMethods()) {
            if (m.getName().equals(methodName) && !Modifier.isFinal(m.getModifiers())) {
                return m;
            }
        }
        return null;
    }

    /**
     * Generate a method stub from a Method, supporting @ParamName annotations.
     * Provides backward compatibility with interface-based hook definitions.
     */
    public static String generateMethodStub(Method method) {
        String mods = Modifier.toString(method.getModifiers());
        mods = mods.replace("abstract ", "").replace("abstract", "")
            .replace("default ", "").replace("default", "").trim();
        if (!mods.isEmpty()) mods += " ";

        String returnTypeStr = getUsableTypeName(method.getReturnType());
        String name = method.getName();

        Map<String, Integer> typeCount = new HashMap<>();
        StringBuilder params = new StringBuilder();
        for (int i = 0; i < method.getParameters().length; i++) {
            java.lang.reflect.Parameter p = method.getParameters()[i];
            String typeName = getUsableTypeName(p.getType());

            // Check for @ParamName annotation first
            ParamName annotation = p.getAnnotation(ParamName.class);
            String paramName;
            if (annotation != null) {
                paramName = annotation.value();
            } else {
                // Fallback to generated name
                String baseName = Character.toLowerCase(p.getType().getSimpleName().charAt(0))
                    + p.getType().getSimpleName().substring(1);
                int count = typeCount.getOrDefault(baseName, 0) + 1;
                typeCount.put(baseName, count);
                paramName = count == 1 ? baseName : baseName + (count - 1);
            }

            if (i > 0) params.append(", ");
            params.append(typeName).append(" ").append(paramName);
        }

        String body;
        Class<?> returnType = method.getReturnType();
        if (returnType == void.class) {
            body = "";
        } else if (returnType.isPrimitive()) {
            if (returnType == boolean.class) body = "    return false;";
            else if (returnType == char.class) body = "    return '\\0';";
            else body = "    return 0;";
        } else {
            body = "    return null;";
        }

        return String.format("%s%s %s(%s) {\n%s\n}\n", mods, returnTypeStr, name, params, body);
    }

    /**
     * Gets a usable type name for stub generation.
     * For nested classes, returns "OuterClass.InnerClass" format.
     */
    private static String getUsableTypeName(Class<?> type) {
        if (type.isPrimitive() || type.getEnclosingClass() == null) {
            return type.getSimpleName();
        }

        // Build the nested class chain
        StringBuilder sb = new StringBuilder();
        Class<?> current = type;
        while (current != null) {
            if (sb.length() > 0) sb.insert(0, ".");
            sb.insert(0, current.getSimpleName());
            current = current.getEnclosingClass();
        }
        return sb.toString();
    }

    /**
     * Get the default imports configured for this script type.
     * These are packages/classes that are automatically available without explicit import statements.
     *
     * @return Array of default import patterns (e.g., "noppes.npcs.api.*")
     */
    public String[] getDefaultImports() {
        return defaultImports;
    }

    private String[] getCachedImports() {
        if (cachedImports == null) {
            cachedImports = collectImportsForCode(getFullCode());
        }
        return cachedImports;
    }

    /**
     * Get all types used in hook method signatures (parameters and return types).
     * This includes event types like INpcEvent.InitEvent, INpcEvent.DamagedEvent, etc.,
     * as well as return types like Color, String, etc.
     * Useful for syntax highlighting to know what types are implicitly available.
     *
     * @return Set of fully qualified class names for all hook parameter and return types
     */
    public Set<String> getHookTypes() {
        Set<String> types = new HashSet<>();
        for (Method method : type.getDeclaredMethods()) {
            // Add parameter types
            for (Class<?> paramType : method.getParameterTypes())
                addTypeAndEnclosingTypes(types, paramType);

            // Add return types (in case any hook has a non-void return)
            Class<?> returnType = method.getReturnType();
            if (returnType != void.class)
                addTypeAndEnclosingTypes(types, returnType);
        }
        return types;
    }

    /**
     * Add a type and all its enclosing types to the set.
     * For nested classes like INpcEvent.InitEvent, this adds both INpcEvent and INpcEvent$InitEvent.
     */
    private void addTypeAndEnclosingTypes(Set<String> types, Class<?> clazz) {
        if (clazz == null || clazz.isPrimitive())
            return;

        // Add the type itself
        types.add(clazz.getName());

        // Add enclosing/declaring class if it's a nested type
        Class<?> enclosing = clazz.getDeclaringClass();
        while (enclosing != null) {
            types.add(enclosing.getName());
            enclosing = enclosing.getDeclaringClass();
        }
    }

    // ==================== IScriptUnit ====================

    @Override
    public String getScript() {
        return script;
    }

    @Override
    public void setScript(String script) {
        this.script = script;
        this.evaluated = false;
        this.cachedImports = null;
        hookResolver.clearResolutionCaches();
    }

    @Override
    public List<String> getExternalScripts() {
        return externalScripts;
    }

    @Override
    public void setExternalScripts(List<String> scripts) {
        this.externalScripts = scripts;
        this.evaluated = false;
        this.cachedImports = null;
        hookResolver.clearResolutionCaches();
    }

    @Override
    public TreeMap<Long, String> getConsole() {
        return console;
    }

    @Override
    public void clearConsole() {
        console.clear();
    }

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

    @Override
    public String getLanguage() {
        return "Java";
    }

    @Override
    public void setLanguage(String language) {
    }

    @Override
    public boolean hasCode() {
        return !externalScripts.isEmpty() || (script != null && !script.isEmpty());
    }

    @Override
    public boolean hasErrored() {
        return errored;
    }

    @Override
    public void setErrored(boolean errored) {
        this.errored = errored;
    }

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
