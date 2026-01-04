package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import net.minecraft.client.Minecraft;
import net.minecraft.util.ResourceLocation;

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
            if (dtsFiles.contains("api/hooks.d.ts")) {
                loadResourceFile(parser, "hooks.d.ts");
            }
            if (dtsFiles.contains("api/index.d.ts")) {
                loadResourceFile(parser, "index.d.ts");
            }
            
            // Load all other .d.ts files
            for (String filePath : dtsFiles) {
                if (filePath.startsWith("api/")) {
                    String relPath = filePath.substring(4); // Remove "api/" prefix
                    if (!relPath.equals("hooks.d.ts") && !relPath.equals("index.d.ts")) {
                        loadResourceFile(parser, relPath);
                    }
                }
            }
            
            resolveInheritance();
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
     */
    private Set<String> findAllDtsFilesInResources(String basePath) {
        Set<String> dtsFiles = new HashSet<>();
        
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            Enumeration<URL> resources = classLoader.getResources(basePath);
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                
                if (resource.getProtocol().equals("file")) {
                    // Scan file system directory
                    File directory = new File(resource.getFile());
                    scanDirectoryForDts(directory, basePath, dtsFiles);
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
                    scanJarForDts(jarPath, basePath, dtsFiles);
                }
            }
        } catch (Exception e) {
            System.err.println("[JSTypeRegistry] Error scanning for .d.ts files: " + e.getMessage());
        }
        
        return dtsFiles;
    }
    
    /**
     * Recursively scan a file system directory for .d.ts files.
     */
    private void scanDirectoryForDts(File directory, String basePath, Set<String> dtsFiles) {
        if (!directory.exists() || !directory.isDirectory()) {
            return;
        }
        
        File[] files = directory.listFiles();
        if (files == null) {
            return;
        }
        
        for (File file : files) {
            String fileName = file.getName();
            
            if (file.isDirectory()) {
                // Recursively scan subdirectory
                scanDirectoryForDts(file, basePath + "/" + fileName, dtsFiles);
            } else if (fileName.endsWith(".d.ts")) {
                // Add .d.ts file path relative to assets/customnpcs/
                dtsFiles.add(basePath + "/" + fileName);
            }
        }
    }
    
    /**
     * Scan a JAR file for .d.ts files in the specified base path.
     */
    private void scanJarForDts(String jarPath, String basePath, Set<String> dtsFiles) {
        try {
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            String searchPath = "assets/customnpcs/" + basePath;
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                // Check if entry is under our base path and is a .d.ts file
                if (entryName.startsWith(searchPath) && entryName.endsWith(".d.ts")) {
                    // Convert to relative path from assets/customnpcs/
                    String relativePath = entryName.substring("assets/customnpcs/".length());
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
     * Resolve inheritance relationships between types.
     */
    private void resolveInheritance() {
        int totalInheritance = 0;
        int resolvedInheritance = 0;
        
        for (JSTypeInfo type : types.values()) {
            String extendsType = type.getExtendsType();
            if (extendsType != null) {
                totalInheritance++;
                JSTypeInfo parent = getType(extendsType);
                if (parent != null) {
                    type.setResolvedParent(parent);
                    resolvedInheritance++;
                    System.out.println("[JSTypeRegistry] Resolved " + type.getSimpleName() + " extends " + parent.getSimpleName());
                } else {
                    System.err.println("[JSTypeRegistry] FAILED to resolve parent type '" + extendsType + "' for " + type.getFullName());
                }
            }
        }
        
        System.out.println("[JSTypeRegistry] Inheritance resolution: " + resolvedInheritance + "/" + totalInheritance + " successful");
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
