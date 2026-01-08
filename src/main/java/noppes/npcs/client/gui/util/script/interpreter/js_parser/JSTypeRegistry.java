package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;
import noppes.npcs.scripted.NpcAPI;

import java.io.*;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Central registry for all TypeScript types parsed from .d.ts files.
 * Also manages hook function signatures and type aliases.
 */
public class JSTypeRegistry {
    
    private static JSTypeRegistry INSTANCE;
    
    // Resource location for the API definitions
    private static final String API_RESOURCE_PATH = "customnpcs:api/";
    
    // All registered types by full name (e.g., "IPlayerEvent.InteractEvent")
    private final Map<String, JSTypeInfo> types = new LinkedHashMap<>();
    
    // Type aliases (simple name -> full type name)
    private final Map<String, String> typeAliases = new HashMap<>();
    
    // Hook function signatures: functionName -> list of (paramName, paramType) pairs
    // Multiple entries for overloaded hooks
    private final Map<String, List<HookSignature>> hooks = new LinkedHashMap<>();

    // Global object instances: name -> type (e.g., "API" -> "AbstractNpcAPI")
    // These are treated as instance objects, not static classes
    private final Map<String, String> globalEngineObjects = new LinkedHashMap<>();
    
    // Primitive types
    private static final Set<String> PRIMITIVES = new HashSet<>(Arrays.asList(
        "number", "string", "boolean", "void", "any", "null", "undefined", "never", "object"
    ));
    
    private boolean initialized = false;
    private boolean initializationAttempted = false;
    
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
            
            // Recursively load all .d.ts files from the api directory
            Set<String> dtsFiles = findAllDtsFilesInResources("assets/customnpcs/api");
            
            System.out.println("[JSTypeRegistry] Found " + dtsFiles.size() + " .d.ts files in resources");
            
            // Load hooks.d.ts and index.d.ts first if they exist (defines core types)
            if (dtsFiles.contains("hooks.d.ts")) {
                loadResourceFile(parser, "hooks.d.ts");
            }
            if (dtsFiles.contains("index.d.ts")) {
                loadResourceFile(parser, "index.d.ts");
            }
            
            // Load all other .d.ts files
            for (String filePath : dtsFiles) {
                if (!filePath.equals("hooks.d.ts") && !filePath.equals("index.d.ts")) {
                    loadResourceFile(parser, filePath);
                }
            }
            
            // Phase 2: Resolve all type parameters now that all types are loaded
            resolveAllTypeParameters();
            
            resolveInheritance();
            registerEngineGlobalObjects();
            
            initialized = true;
            System.out.println("[JSTypeRegistry] Loaded " + types.size() + " types, " + hooks.size() + " hooks from resources");
        } catch (Exception e) {
            System.err.println("[JSTypeRegistry] Failed to load type definitions from resources: " + e.getMessage());
            e.printStackTrace();
        }
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
     */
    public void registerType(JSTypeInfo type) {
        types.put(type.getFullName(), type);
    }
    
    /**
     * Register a type alias.
     */
    public void registerTypeAlias(String alias, String fullType) {
        typeAliases.put(alias, fullType);
    }
    
    /**
     * Register a hook function signature.
     */
    public void registerHook(String functionName, String paramName, String paramType) {
        hooks.computeIfAbsent(functionName, k -> new ArrayList<>())
             .add(new HookSignature(paramName, paramType));
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
        typeAliases.clear();
        hooks.clear();
        globalEngineObjects.clear();
        initialized = false;
    }
    
    /**
     * Represents a hook function signature.
     */
    public static class HookSignature {
        public final String paramName;
        public final String paramType;
        public final String doc;
        
        public HookSignature(String paramName, String paramType) {
            this(paramName, paramType, null);
        }
        
        public HookSignature(String paramName, String paramType, String doc) {
            this.paramName = paramName;
            this.paramType = paramType;
            this.doc = doc;
        }
        
        @Override
        public String toString() {
            return paramName + ": " + paramType;
        }
    }
}
