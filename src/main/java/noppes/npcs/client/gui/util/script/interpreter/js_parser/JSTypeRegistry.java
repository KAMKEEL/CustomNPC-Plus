package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.scripted.NpcAPI;
import noppes.npcs.client.gui.util.script.interpreter.bridge.DtsJavaBridge;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Central registry for all TypeScript types parsed from .d.ts files.
 * Also manages hook function signatures and type aliases.
 */
public class JSTypeRegistry {
    
    private static JSTypeRegistry INSTANCE;
    
    // All registered types by full name (e.g., "IPlayerEvent.InteractEvent")
    private final Map<String, JSTypeInfo> types = new LinkedHashMap<>();

    // Registered types by Java fully-qualified name (e.g., "noppes.npcs.api.entity.IPlayer")
    private final Map<String, JSTypeInfo> typesByJavaFqn = new LinkedHashMap<>();
    
    // Type aliases (simple name -> full type name)
    private final Map<String, String> typeAliases = new HashMap<>();

    // Type full name -> origin string (modId:domain:relativePath)
    private final Map<String, String> typeOrigins = new HashMap<>();

    // Alias name -> origin string (modId:domain:relativePath)
    private final Map<String, String> aliasOrigins = new HashMap<>();
    
    // Context-aware hook function signatures: namespace -> functionName -> list of signatures
    // The namespace is the event interface name (e.g., "INpcEvent", "IPlayerEvent")
    // This allows any mod to register hooks without modifying an enum
    private final Map<String, Map<String, List<HookSignature>>> contextHooks = new LinkedHashMap<>();

    // Legacy hook storage: functionName -> list of (paramName, paramType) pairs
    // Kept for backward compatibility with code that doesn't use contexts
    private final Map<String, List<HookSignature>> hooks = new LinkedHashMap<>();

    // Fallback namespace for hooks that don't match a specific context
    private static final String GLOBAL_NAMESPACE = "Global";

    // Global object instances: name -> type (e.g., "API" -> "AbstractNpcAPI")
    // These are treated as instance objects, not static classes
    private final Map<String, String> globalEngineObjects = new LinkedHashMap<>();
    
    // Primitive types
    private static final Set<String> PRIMITIVES = new HashSet<>(Arrays.asList(
        "number", "string", "boolean", "void", "any", "null", "undefined", "never", "object"
    ));
    
    private boolean initialized = false;
    private boolean initializationAttempted = false;

    private String currentSource = null;
    
    public static JSTypeRegistry getInstance() {
        if (INSTANCE == null) {
            INSTANCE = new JSTypeRegistry();
        }
        return INSTANCE;
    }
    
    private JSTypeRegistry() {}
    
    /**
     * Initialize the registry from the embedded resources.
     * Recursively loads all .d.ts files from assets/customnpcs/api/
     */
    public void initializeFromResources() {
        if (initialized || initializationAttempted) return;
        initializationAttempted = true;
        
        try {
            TypeScriptDefinitionParser parser = new TypeScriptDefinitionParser(this);

            List<DtsModScanner.DtsFileRef> dtsFiles = DtsModScanner.collectDtsFilesFromMods();
            if (dtsFiles.isEmpty()) {
                Set<String> fallbackFiles = findAllDtsFilesInResources("assets/customnpcs/api");

                System.out.println("[JSTypeRegistry] Found " + fallbackFiles.size() + " .d.ts files in resources");

                if (fallbackFiles.contains("hooks.d.ts")) {
                    loadResourceFile(parser, "hooks.d.ts");
                }
                if (fallbackFiles.contains("index.d.ts")) {
                    loadResourceFile(parser, "index.d.ts");
                }

                for (String filePath : fallbackFiles) {
                    if (!filePath.equals("hooks.d.ts") && !filePath.equals("index.d.ts")) {
                        loadResourceFile(parser, filePath);
                    }
                }
            } else {
                DtsModScanner.logSummary(dtsFiles);
                loadDtsFiles(parser, dtsFiles);
            }
            
            // Phase 2: Resolve all type parameters now that all types are loaded
            resolveAllTypeParameters();
            
            // Phase 2b: Resolve member types (return types, field types, param types)
            resolveAllMemberTypes();
            
            // Phase 2c: Resolve JSDoc types (@param, @return, @type type references)
            resolveAllJSDocTypes();
            
            resolveInheritance();
            registerEngineGlobalObjects();
            
            initialized = true;
            System.out.println("[JSTypeRegistry] Loaded " + types.size() + " types, " + hooks.size() + " hooks from resources");
        } catch (Exception e) {
            System.err.println("[JSTypeRegistry] Failed to load type definitions from resources: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadDtsFiles(TypeScriptDefinitionParser parser, List<DtsModScanner.DtsFileRef> dtsFiles) {
        DtsModScanner.sortDtsFiles(dtsFiles);
        for (DtsModScanner.DtsFileRef ref : dtsFiles) {
            try (InputStream is = ref.openStream()) {
                if (is == null) {
                    continue;
                }
                String content = readFully(new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8)));
                setCurrentSource(ref.getOrigin());
                parser.parseDefinitionFile(content, ref.getRelativePath());
            } catch (Exception e) {
                System.err.println("[JSTypeRegistry] Failed to load " + ref.getOrigin() + ": " + e.getMessage());
            } finally {
                setCurrentSource(null);
            }
        }
    }

    private String readFully(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
    
    /**
     * Recursively find all .d.ts files in the resources directory.
     * Similar to ClassIndex.addPackage, scans both file system and JAR resources.
     *
     * @param resourcePath The full resource path (e.g., "assets/customnpcs/api")
     */
    private Set<String> findAllDtsFilesInResources(String resourcePath) {
        Set<String> dtsFiles = new HashSet<>();
        
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(resourcePath);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                
                if (resource.getProtocol().equals("file")) {
                    // Scan file system directory
                    File directory = new File(resource.getFile());
                    scanDirectoryForDts(directory, "", dtsFiles);
                } else if (resource.getProtocol().equals("jar")) {
                    // Scan JAR file
                    String jarPath = resource.getPath();
                    if (jarPath.startsWith("file:")) {
                        jarPath = jarPath.substring(5);
                    }
                    int separatorIndex = jarPath.indexOf("!");
                    if (separatorIndex != -1) {
                        jarPath = jarPath.substring(0, separatorIndex);
                    }
                    scanJarForDts(jarPath, resourcePath, dtsFiles);
                }
            }
        } catch (Exception e) {
            System.err.println("[JSTypeRegistry] Error scanning for .d.ts files: " + e.getMessage());
        }
        
        return dtsFiles;
    }
    
    /**
     * Recursively scan a file system directory for .d.ts files.
     *
     * @param directory The directory to scan
     * @param currentPath The relative path from the base (used for building file paths)
     * @param dtsFiles The set to collect .d.ts file paths
     */
    private void scanDirectoryForDts(File directory, String currentPath, Set<String> dtsFiles) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            String fileName = file.getName();
            String filePath = currentPath.isEmpty() ? fileName : currentPath + "/" + fileName;
            
            if (file.isDirectory()) {
                // Recursively scan subdirectory
                scanDirectoryForDts(file, filePath, dtsFiles);
            } else if (fileName.endsWith(".d.ts")) {
                // Add .d.ts file path
                dtsFiles.add(filePath);
            }
        }
    }
    
    /**
     * Scan a JAR file for .d.ts files in the specified resource path.
     */
    private void scanJarForDts(String jarPath, String resourcePath, Set<String> dtsFiles) {
        try {
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();

                // Check if entry is under our resource path and is a .d.ts file
                if (entryName.startsWith(resourcePath + "/") && entryName.endsWith(".d.ts")) {
                    // Convert to relative path from resourcePath
                    String relativePath = entryName.substring(resourcePath.length() + 1);
                    dtsFiles.add(relativePath);
                }
            }
            
            jarFile.close();
        } catch (Exception e) {
            System.err.println("[JSTypeRegistry] Error scanning JAR for .d.ts files: " + e.getMessage());
        }
    }

    /**
     * Load a specific .d.ts file from resources.
     */
    private void loadResourceFile(TypeScriptDefinitionParser parser, String fileName) {
        try {
            ResourceLocation loc = new ResourceLocation("customnpcs", "api/" + fileName);
            InputStream is = Minecraft.getMinecraft().getResourceManager().getResource(loc).getInputStream();
            if (is != null) {
                try (BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                    StringBuilder sb = new StringBuilder();
                    String line;
                    while ((line = reader.readLine()) != null) {
                        sb.append(line).append("\n");
                    }
                    parser.parseDefinitionFile(sb.toString(), fileName);
                } 
            }
        } catch (Exception e) {
            // File might not exist, that's ok
            System.out.println("[JSTypeRegistry] Could not load " + fileName + ": " + e.getMessage());
        }
    }
    
    /**
     * Initialize the registry from a directory containing .d.ts files.
     */
    public void initializeFromDirectory(File directory) {
        if (initialized) return;
        
        try {
            TypeScriptDefinitionParser parser = new TypeScriptDefinitionParser(this);
            parser.parseDirectory(directory);
            resolveAllTypeParameters();
            resolveAllMemberTypes();
            resolveAllJSDocTypes();
            resolveInheritance();
            initialized = true;
            System.out.println("[JSTypeRegistry] Loaded " + types.size() + " types, " + hooks.size() + " hooks");
        } catch (IOException e) {
            System.err.println("[JSTypeRegistry] Failed to load type definitions: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Initialize the registry from a VSIX file.
     */
    public void initializeFromVsix(File vsixFile) {
        if (initialized) return;
        
        try {
            TypeScriptDefinitionParser parser = new TypeScriptDefinitionParser(this);
            parser.parseVsixArchive(vsixFile);
            resolveAllTypeParameters();
            resolveAllMemberTypes();
            resolveAllJSDocTypes();
            resolveInheritance();
            initialized = true;
            System.out.println("[JSTypeRegistry] Loaded " + types.size() + " types, " + hooks.size() + " hooks from VSIX");
        } catch (IOException e) {
            System.err.println("[JSTypeRegistry] Failed to load VSIX: " + e.getMessage());
            e.printStackTrace();
        }
    }
    
    /**
     * Register a type.
     * If a type with matching javaFqn or fullName already exists, merges instead of discarding.
     */
    public void registerType(JSTypeInfo type) {
        String fullName = type.getFullName();
        String javaFqn = type.getJavaFqn();
        
        JSTypeInfo existingType = null;
        
        if (javaFqn != null && !javaFqn.isEmpty()) {
            existingType = typesByJavaFqn.get(javaFqn);
        }
        
        if (existingType == null && types.containsKey(fullName)) {
            existingType = types.get(fullName);
        }
        
        if (existingType != null) {
            mergeType(existingType, type, getCurrentSource());

            // Preserve an addressable key for the incoming declared name.
            // Some .d.ts parsing paths can encounter the same Java type via multiple textual names
            // (e.g., due to nested namespaces). Even if we merge by @javaFqn, callers still expect
            // lookups like "INpcEvent.DamagedEvent" to work.
            if (!types.containsKey(fullName)) {
                types.put(fullName, existingType);
                typeOrigins.put(fullName, getCurrentSource());
            }
            return;
        }
        
        types.put(fullName, type);
        typeOrigins.put(fullName, getCurrentSource());
        if (javaFqn != null && !javaFqn.isEmpty()) {
            if (!typesByJavaFqn.containsKey(javaFqn)) {
                typesByJavaFqn.put(javaFqn, type);
            } else {
                System.out.println("[JSTypeRegistry] Duplicate javaFqn " + javaFqn + " from " + getCurrentSource());
            }
        }
    }

    /**
     * Get a type by Java fully-qualified name.
     */
    public JSTypeInfo getTypeByJavaFqn(String javaFqn) {
        if (javaFqn == null || javaFqn.isEmpty()) return null;
        return typesByJavaFqn.get(javaFqn);
    }
    
    /**
     * Register a type alias.
     */
    public void registerTypeAlias(String alias, String fullType) {
        if (typeAliases.containsKey(alias)) {
            String existing = typeAliases.get(alias);
            if (!Objects.equals(existing, fullType)) {
                logAliasCollision(alias, existing, fullType, getCurrentSource());
            }
            return;
        }
        typeAliases.put(alias, fullType);
        aliasOrigins.put(alias, getCurrentSource());
    }
    
    /**
     * Register a hook function signature with a namespace.
     *
     * @param namespace The event interface namespace (e.g., "INpcEvent", "IPlayerEvent")
     * @param functionName The hook function name (e.g., "interact", "damaged")
     * @param paramName The parameter name (e.g., "event")
     * @param paramType The parameter type (e.g., "INpcEvent.InteractEvent")
     */
    public void registerHook(String namespace, String functionName, String paramName, String paramType) {
        HookSignature sig = new HookSignature(paramName, paramType, null, namespace);

        // Add to context-specific map
        contextHooks.computeIfAbsent(namespace, k -> new LinkedHashMap<>())
                    .computeIfAbsent(functionName, k -> new ArrayList<>())
                    .add(sig);

        // Also add to legacy hooks map for backward compatibility
        hooks.computeIfAbsent(functionName, k -> new ArrayList<>()).add(sig);
    }
    
    /**
     * Get a type by name (handles aliases and primitives).
     */
    public JSTypeInfo getType(String name) {
        return getType(name, new HashSet<>());
    }
    
    /**
     * Internal method with cycle detection for type aliases.
     */
    private JSTypeInfo getType(String name, Set<String> visited) {
        if (name == null || name.isEmpty()) return null;
        
        // Strip array brackets for lookup
        String baseName = name.replace("[]", "").trim();
        
        // Check if primitive
        if (PRIMITIVES.contains(baseName)) {
            return null; // Primitives don't have JSTypeInfo
        }
        
        // Detect circular alias references
        if (visited.contains(baseName)) {
            // Circular alias detected - try direct type lookup as fallback
            if (types.containsKey(baseName)) {
                return types.get(baseName);
            }
            return null;
        }
        visited.add(baseName);
        
        // Direct lookup in types first (higher priority than aliases)
        if (types.containsKey(baseName)) {
            return types.get(baseName);
        }
        
        // Check type aliases
        if (typeAliases.containsKey(baseName)) {
            String resolved = typeAliases.get(baseName);
            // Don't follow alias if it resolves to itself
            if (resolved.equals(baseName)) {
                return null;
            }
            return getType(resolved, visited);
        }
        
        // Try simple name lookup (for types like "IEntity" without namespace)
        for (JSTypeInfo type : types.values()) {
            if (type.getSimpleName().equals(baseName)) {
                return type;
            }
        }
        return null;
    }
    
    /**
     * Check if a type name is a known primitive.
     */
    public boolean isPrimitive(String typeName) {
        return PRIMITIVES.contains(typeName);
    }
    
    /**
     * Get hook signatures for a function name.
     */
    public List<HookSignature> getHookSignatures(String functionName) {
        return hooks.getOrDefault(functionName, Collections.emptyList());
    }
    
    /**
     * Check if a function name is a known hook.
     */
    public boolean isHook(String functionName) {
        return hooks.containsKey(functionName);
    }
    
    /**
     * Get the parameter type for a hook function.
     * If multiple overloads exist, returns the first one.
     */
    public String getHookParameterType(String functionName) {
        List<HookSignature> sigs = hooks.get(functionName);
        if (sigs != null && !sigs.isEmpty()) {
            return sigs.get(0).paramType;
        }
        return null;
    }

    // ==================== Context-Aware Hook Methods ====================

    /**
     * Get hook signatures for a specific script context.
     * Falls back to GLOBAL context if not found in the specified context.
     *
     * @param namespace The event interface namespace (e.g., "INpcEvent", "IPlayerEvent")
     * @param functionName The hook function name
     * @return List of hook signatures, or empty list if not found
     */
    public List<HookSignature> getHookSignatures(String namespace, String functionName) {
        // First try the specific namespace
        Map<String, List<HookSignature>> namespaceMap = contextHooks.get(namespace);
        if (namespaceMap != null) {
            List<HookSignature> sigs = namespaceMap.get(functionName);
            if (sigs != null && !sigs.isEmpty()) {
                return sigs;
            }
        }

        // Fall back to GLOBAL namespace
        if (!GLOBAL_NAMESPACE.equals(namespace)) {
            Map<String, List<HookSignature>> globalMap = contextHooks.get(GLOBAL_NAMESPACE);
            if (globalMap != null) {
                List<HookSignature> sigs = globalMap.get(functionName);
                if (sigs != null && !sigs.isEmpty()) {
                    return sigs;
                }
            }
        }

        return Collections.emptyList();
    }

    /**
     * Check if a function name is a known hook in a specific namespace.
     *
     * @param namespace The event interface namespace
     * @param functionName The hook function name
     * @return true if the hook exists in this namespace or GLOBAL
     */
    public boolean isHook(String namespace, String functionName) {
        return !getHookSignatures(namespace, functionName).isEmpty();
    }

    /**
     * Get the parameter type for a hook function in a specific namespace.
     * Falls back to GLOBAL namespace if not found.
     *
     * @param namespace The event interface namespace
     * @param functionName The hook function name
     * @return The parameter type, or null if not found
     */
    public String getHookParameterType(String namespace, String functionName) {
        List<HookSignature> sigs = getHookSignatures(namespace, functionName);
        if (!sigs.isEmpty()) {
            return sigs.get(0).paramType;
        }
        return null;
    }

    /**
     * Get all hook names for a specific namespace.
     *
     * @param namespace The event interface namespace
     * @return Set of hook function names available in this namespace
     */
    public Set<String> getHookNames(String namespace) {
        Set<String> names = new HashSet<>();

        // Add hooks from the specific namespace
        Map<String, List<HookSignature>> namespaceMap = contextHooks.get(namespace);
        if (namespaceMap != null) {
            names.addAll(namespaceMap.keySet());
        }

        // Also add GLOBAL hooks
        if (!GLOBAL_NAMESPACE.equals(namespace)) {
            Map<String, List<HookSignature>> globalMap = contextHooks.get(GLOBAL_NAMESPACE);
            if (globalMap != null) {
                names.addAll(globalMap.keySet());
            }
        }

        return names;
    }

    /**
     * Get all hooks organized by namespace.
     *
     * @return Map of namespace -> hookName -> signatures
     */
    public Map<String, Map<String, List<HookSignature>>> getAllContextHooks() {
        return Collections.unmodifiableMap(contextHooks);
    }

    // ==================== MULTI-NAMESPACE LOOKUP (for ScriptContext) ====================

    /**
     * Get hook signatures by searching through multiple namespaces.
     * This is used when a ScriptContext has multiple event types (e.g., Player has
     * IPlayerEvent, IAnimationEvent, IPartyEvent, etc.)
     *
     * @param namespaces List of namespaces to search (in priority order)
     * @param functionName The hook function name
     * @return List of hook signatures from the first matching namespace, or empty list
     */
    public List<HookSignature> getHookSignatures(List<String> namespaces, String functionName) {
        // Search through all namespaces in order
        for (String namespace : namespaces) {
            Map<String, List<HookSignature>> namespaceMap = contextHooks.get(namespace);
            if (namespaceMap != null) {
                List<HookSignature> sigs = namespaceMap.get(functionName);
                if (sigs != null && !sigs.isEmpty()) {
                    return sigs;
                }
            }
        }

        // Fall back to GLOBAL namespace
        Map<String, List<HookSignature>> globalMap = contextHooks.get(GLOBAL_NAMESPACE);
        if (globalMap != null) {
            List<HookSignature> sigs = globalMap.get(functionName);
            if (sigs != null && !sigs.isEmpty()) {
                return sigs;
            }
        }

        return Collections.emptyList();
    }

    /**
     * Check if a function name is a known hook in any of the given namespaces.
     *
     * @param namespaces List of namespaces to search
     * @param functionName The hook function name
     * @return true if the hook exists in any namespace or GLOBAL
     */
    public boolean isHook(List<String> namespaces, String functionName) {
        return !getHookSignatures(namespaces, functionName).isEmpty();
    }

    /**
     * Get the parameter type for a hook function, searching through multiple namespaces.
     *
     * @param namespaces List of namespaces to search
     * @param functionName The hook function name
     * @return The parameter type, or null if not found
     */
    public String getHookParameterType(List<String> namespaces, String functionName) {
        List<HookSignature> sigs = getHookSignatures(namespaces, functionName);
        if (!sigs.isEmpty()) {
            return sigs.get(0).paramType;
        }
        return null;
    }

    /**
     * Get all hook names available in any of the given namespaces.
     *
     * @param namespaces List of namespaces to search
     * @return Set of hook function names available in these namespaces
     */
    public Set<String> getHookNames(List<String> namespaces) {
        Set<String> names = new HashSet<>();

        // Add hooks from all specified namespaces
        for (String namespace : namespaces) {
            Map<String, List<HookSignature>> namespaceMap = contextHooks.get(namespace);
            if (namespaceMap != null) {
                names.addAll(namespaceMap.keySet());
            }
        }

        // Also add GLOBAL hooks
        Map<String, List<HookSignature>> globalMap = contextHooks.get(GLOBAL_NAMESPACE);
        if (globalMap != null) {
            names.addAll(globalMap.keySet());
        }

        return names;
    }

    /**
     * Resolve all type parameters for all types.
     * Called after all .d.ts files are loaded (Phase 2).
     * This ensures that type parameters can reference any type in the registry.
     */
    public void resolveAllTypeParameters() {
        for (JSTypeInfo type : types.values()) {
            type.resolveTypeParameters();
        }
    }
    
    /**
     * Resolve all member types (return types, field types, parameter types) for all types.
     * Called after resolveAllTypeParameters (Phase 2b).
     * This resolves types like "Java.java.io.File" to proper TypeInfo objects.
     */
    public void resolveAllMemberTypes() {
        for (JSTypeInfo type : types.values()) 
            type.resolveMemberTypes();
    }
    
    public void resolveAllJSDocTypes() {
        for (JSTypeInfo type : types.values())
            type.resolveJSDocTypes();
    }
    
    /**
     * Resolve inheritance relationships between types.
     * For each type, walks up the parent chain and resolves all ancestors.
     * Efficient O(n) approach - each type's chain is walked once.
     */
    public void resolveInheritance() {
        for (JSTypeInfo type : types.values()) {
            JSTypeInfo child = type;
            while (child != null && child.getExtendsType() != null && child.getResolvedParent() == null) {
                JSTypeInfo parent = getType(child.getExtendsType());
                if (parent != null) {
                    child.setResolvedParent(parent);
                }
                child = parent;
            }
        }
    }
    
    /**
     * Get all registered types.
     */
    public Collection<JSTypeInfo> getAllTypes() {
        return types.values();
    }
    
    /**
     * Get all type names.
     */
    public Set<String> getTypeNames() {
        return types.keySet();
    }
    
    /**
     * Get all hook function names.
     */
    public Set<String> getHookNames() {
        return hooks.keySet();
    }
    
    /**
     * Get all registered hooks.
     */
    public Map<String, List<HookSignature>> getAllHooks() {
        return hooks;
    }

    /**
     * Register a global object instance (like API, DBCAPI from NpcAPI.engineObjects).
     * These are treated as instance objects, not static classes.
     * @param name The global variable name (e.g., "API")
     * @param typeName The type name (e.g., "AbstractNpcAPI")
     */
    public void registerGlobalObject(String name, String typeName) {
        globalEngineObjects.put(name, typeName);
    }

    /**
     * Get the type name for a global object.
     * @param name The global variable name
     * @return The type name, or null if not a registered global object
     */
    public String getGlobalObjectType(String name) {
        return globalEngineObjects.get(name);
    }

    /**
     * Check if a name is a registered global object instance.
     */
    public boolean isGlobalObject(String name) {
        return globalEngineObjects.containsKey(name);
    }

    /**
     * Get all registered global objects.
     */
    public Map<String, String> getGlobalEngineObjects() {
        return Collections.unmodifiableMap(globalEngineObjects);
    }

    /**
     * Registers global objects from NpcAPI.engineObjects into JSTypeRegistry.
     * This allows the IDE to recognize API, DBCAPI, etc. as instance objects with autocomplete.
     */
    private void registerEngineGlobalObjects() {
        Map<String, Object> engineObjects = new HashMap<>(NpcAPI.engineObjects);
        engineObjects.put("API", NpcAPI.Instance()); //default API object

        if (engineObjects != null) {
            for (Map.Entry<String, Object> entry : engineObjects.entrySet()) {
                String name = entry.getKey(); // e.g., "API", "DBCAPI"
                Object obj = entry.getValue(); // e.g., AbstractNpcAPI instance

                if (obj != null) {
                    // Get the actual class name
                    String concreteClassName = obj.getClass().getSimpleName();

                    // Map concrete implementation classes to their abstract interface names
                    // (because .d.ts files define the abstract interfaces, not the implementations)
                    String typeName = mapConcreteToAbstractClassName(concreteClassName);

                    // Register as global object (instance, not static)
                    registerGlobalObject(name, typeName);
                }
            }
        }
    }

    /**
     * Maps concrete implementation class names to their abstract interface names.
     * For example: "NpcAPI" -> "AbstractNpcAPI", "DBCAPI" -> "AbstractDBCAPI"
     */
    private String mapConcreteToAbstractClassName(String concreteClassName) {
        // Check if the type exists in the registry as-is
        if (types.containsKey(concreteClassName))
            return concreteClassName;

        // Try prepending "Abstract" if it doesn't exist
        String abstractName = "Abstract" + concreteClassName;
        if (types.containsKey(abstractName))
            return abstractName;

        // Default: return the original name
        return concreteClassName;
    }
    
    /**
     * Check if initialized.
     */
    public boolean isInitialized() {
        return initialized;
    }
    
    /**
     * Clear the registry (for reloading).
     */
    public void clear() {
        types.clear();
        typesByJavaFqn.clear();
        typeAliases.clear();
        typeOrigins.clear();
        aliasOrigins.clear();
        hooks.clear();
        contextHooks.clear();
        globalEngineObjects.clear();
        DtsJavaBridge.clearCache();
        initialized = false;
    }

    private void setCurrentSource(String source) {
        currentSource = source;
    }

    private String getCurrentSource() {
        return currentSource == null ? "unknown" : currentSource;
    }

    private void logTypeCollision(String fullName, String incomingSource) {
        String existingSource = typeOrigins.getOrDefault(fullName, "unknown");
        System.out.println("[JSTypeRegistry] Duplicate type " + fullName + " from " + incomingSource + " (kept " + existingSource + ")");
    }

    private void logAliasCollision(String alias, String existingType, String incomingType, String incomingSource) {
        String existingSource = aliasOrigins.getOrDefault(alias, "unknown");
        System.out.println("[JSTypeRegistry] Duplicate alias " + alias + " from " + incomingSource + " (kept " + existingSource + ")");
        System.out.println("[JSTypeRegistry] Alias " + alias + " existing=" + existingType + " incoming=" + incomingType);
    }

    /**
     * Find the method key in a type's methods map that matches the given method name and parameter count.
     * This handles overload keys (methodName, methodName$1, methodName$2, etc.)
     *
     * @param type The type to search in
     * @param methodName The method name to search for
     * @param paramCount The parameter count to match
     * @return The method key if found, or null if no match
     */
    private String findOwnMethodKeyByParamCount(JSTypeInfo type, String methodName, int paramCount) {
        Map<String, JSMethodInfo> methods = type.getMethods();
        
        // Check direct key first
        JSMethodInfo direct = methods.get(methodName);
        if (direct != null && direct.getParameterCount() == paramCount) {
            return methodName;
        }
        
        // Check overload keys: methodName$1, methodName$2, etc.
        int index = 1;
        String overloadKey = methodName + "$" + index;
        while (methods.containsKey(overloadKey)) {
            JSMethodInfo overload = methods.get(overloadKey);
            if (overload.getParameterCount() == paramCount) {
                return overloadKey;
            }
            index++;
            overloadKey = methodName + "$" + index;
        }
        
        return null; // No match found
    }

    /**
     * Merge an incoming type into an existing type.
     * This allows addon mods to patch base mod types via .d.ts files.
     *
     * @param existing The existing type to merge into
     * @param incoming The incoming type to merge from
     * @param incomingSource The source of the incoming type (for logging)
     */
    private void mergeType(JSTypeInfo existing, JSTypeInfo incoming, String incomingSource) {
        int methodsMerged = 0;
        int fieldsMerged = 0;
        
        // Merge methods (replace matching overloads by param count)
        for (Map.Entry<String, JSMethodInfo> entry : incoming.getMethods().entrySet()) {
            JSMethodInfo incomingMethod = entry.getValue();
            String methodName = incomingMethod.getName();
            int paramCount = incomingMethod.getParameterCount();
            
            // Find existing method with same param count
            String existingKey = findOwnMethodKeyByParamCount(existing, methodName, paramCount);
            if (existingKey != null) {
                // Replace existing overload
                existing.getMethods().put(existingKey, incomingMethod);
                incomingMethod.setContainingType(existing);
                methodsMerged++;
            } else {
                // Add as new overload
                existing.addMethod(incomingMethod);
                methodsMerged++;
            }
        }
        
        // Merge fields (replace by name)
        for (Map.Entry<String, JSFieldInfo> entry : incoming.getFields().entrySet()) {
            String fieldName = entry.getKey();
            JSFieldInfo incomingField = entry.getValue();
            existing.getFields().put(fieldName, incomingField);
            incomingField.setContainingType(existing);
            fieldsMerged++;
        }
        
        // Merge metadata (fill gaps only - preserve existing non-null values)
        if (existing.getJavaFqn() == null && incoming.getJavaFqn() != null) {
            existing.setJavaFqn(incoming.getJavaFqn());
        }
        if (existing.getJsDocInfo() == null && incoming.getJsDocInfo() != null) {
            existing.setJsDocInfo(incoming.getJsDocInfo());
        }
        if (existing.getExtendsType() == null && incoming.getExtendsType() != null) {
            existing.setExtends(incoming.getExtendsType());
        }
        
        // Log merge details
        System.out.println("[JSTypeRegistry] Merged type " + existing.getFullName() + 
                           " with " + methodsMerged + " methods, " + fieldsMerged + 
                           " fields from " + incomingSource);
    }

    /**
     * Represents a hook function signature with its namespace.
     */
    public static class HookSignature {
        public final String paramName;
        public final String paramType;
        public final String doc;
        public final String namespace;  // The event interface namespace (e.g., "INpcEvent")

        public HookSignature(String paramName, String paramType) {
            this(paramName, paramType, null, GLOBAL_NAMESPACE);
        }

        public HookSignature(String paramName, String paramType, String doc) {
            this(paramName, paramType, doc, GLOBAL_NAMESPACE);
        }

        public HookSignature(String paramName, String paramType, String doc, String namespace) {
            this.paramName = paramName;
            this.paramType = paramType;
            this.doc = doc;
            this.namespace = namespace != null ? namespace : GLOBAL_NAMESPACE;
        }

        @Override
        public String toString() {
            return paramName + ": " + paramType + " [" + namespace + "]";
        }
    }
}
