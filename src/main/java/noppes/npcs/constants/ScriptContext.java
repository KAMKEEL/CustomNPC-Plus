package noppes.npcs.constants;

import noppes.npcs.api.event.*;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.net.URLDecoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Represents a script context that maps to one or more event interface namespaces.
 *
 * Script contexts are automatically discovered by scanning for {@link ScriptContextMarker}
 * annotations on event interfaces. For example:
 *
 * <pre>
 * {@literal @}ScriptContextMarker("NPC")
 * public interface INpcEvent extends ICustomNPCsEvent { }
 * </pre>
 *
 * Multiple interfaces can share the same context ID - they will be grouped together.
 * This is a registerable system - any mod can register their own contexts by annotating
 * their event interfaces.
 */
public class ScriptContext {

    // Registry of all script contexts
    private static final Map<String, ScriptContext> REGISTRY = new ConcurrentHashMap<>();
    
    // Static initializer to scan and register annotated classes
    static {
        scanAndRegisterAnnotatedClasses();
    }

    // ==================== BUILT-IN CONTEXTS ====================
    // These are loaded dynamically from annotations, but we keep references for convenience

    public static final ScriptContext NPC = byId("NPC");
    public static final ScriptContext PLAYER = byId("PLAYER");
    public static final ScriptContext BLOCK = byId("BLOCK");
    public static final ScriptContext ITEM = byId("ITEM");
    public static final ScriptContext FORGE = byId("FORGE");
    public static final ScriptContext GLOBAL = byId("GLOBAL");

    // ==================== INSTANCE FIELDS ====================

    /** Unique identifier for this context (e.g., "NPC", "PLAYER", "DBC") */
    public final String id;

    /** The event interface namespaces this context supports */
    private final List<String> namespaces;

    // ==================== CONSTRUCTOR ====================

    private ScriptContext(String id, String... namespaces) {
        this.id = id;
        this.namespaces = new ArrayList<>(Arrays.asList(namespaces));
    }

    // ==================== NAMESPACE ACCESS ====================

    /**
     * Get all namespaces this context supports.
     *
     * @return Unmodifiable list of namespace strings
     */
    public List<String> getNamespaces() {
        return Collections.unmodifiableList(namespaces);
    }

    /**
     * Check if this context includes a specific namespace.
     *
     * @param namespace The namespace to check (e.g., "IPlayerEvent")
     * @return true if this context supports hooks from that namespace
     */
    public boolean hasNamespace(String namespace) {
        return namespaces.contains(namespace);
    }

    /**
     * Add a namespace to this context.
     * Mods can use this to extend built-in contexts with their own event types.
     *
     * @param namespace The namespace to add (e.g., "IDBCPlayerEvent")
     */
    public void addNamespace(String namespace) {
        if (!namespaces.contains(namespace)) {
            namespaces.add(namespace);
        }
    }

    /**
     * Get the primary namespace (first one registered).
     * This is mainly for backward compatibility.
     *
     * @return The first namespace, or "Global" if none
     */
    public String getPrimaryNamespace() {
        return namespaces.isEmpty() ? "Global" : namespaces.get(0);
    }

    // ==================== REGISTRATION API ====================

    /**
     * Scan the classpath for classes annotated with {@link ScriptContextMarker}
     * and automatically register them.
     */
    private static void scanAndRegisterAnnotatedClasses() {
        try {
            // Scan the noppes.npcs.api.event package for annotated interfaces
            List<Class<?>> annotatedClasses = findAnnotatedClasses("noppes.npcs.api.event", ScriptContextMarker.class);
            
            // Group classes by their context ID
            Map<String, List<Class<?>>> contextGroups = new HashMap<>();
            
            for (Class<?> eventClass : annotatedClasses) {
                ScriptContextMarker annotation = eventClass.getAnnotation(ScriptContextMarker.class);
                if (annotation != null) {
                    String contextId = annotation.value();
                    contextGroups.computeIfAbsent(contextId, k -> new ArrayList<>()).add(eventClass);
                }
            }
            
            // Register each context with its grouped classes
            for (Map.Entry<String, List<Class<?>>> entry : contextGroups.entrySet()) {
                String contextId = entry.getKey();
                List<Class<?>> classes = entry.getValue();
                register(contextId, classes.toArray(new Class<?>[0]));
            }

            // Special case: GLOBAL context (no annotation needed)
            if (!REGISTRY.containsKey("GLOBAL")) {
                register("GLOBAL", "Global");
            }
        } catch (Exception e) {
            // Fallback: if scanning fails, at least register known core events
            System.err.println("[ScriptContext] Failed to scan for annotated classes: " + e.getMessage());
            e.printStackTrace();
            fallbackRegistration();
        }
    }
    
    /**
     * Find all classes in a package that have a specific annotation.
     */
    private static List<Class<?>> findAnnotatedClasses(String packageName, Class<? extends java.lang.annotation.Annotation> annotationClass) throws Exception {
        List<Class<?>> annotatedClasses = new ArrayList<>();
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        String packagePath = packageName.replace('.', '/');
        
        // Get resources for the package
        Enumeration<URL> resources = classLoader.getResources(packagePath);
        
        while (resources.hasMoreElements()) {
            URL resource = resources.nextElement();
            String protocol = resource.getProtocol();
            
            if ("file".equals(protocol)) {
                // Scanning from file system (development environment)
                String filePath = URLDecoder.decode(resource.getFile(), "UTF-8");
                File directory = new File(filePath);
                if (directory.exists()) {
                    findClassesInDirectory(directory, packageName, annotationClass, annotatedClasses);
                }
            } else if ("jar".equals(protocol)) {
                // Scanning from JAR file (production)
                String jarPath = resource.getPath().substring(5, resource.getPath().indexOf("!"));
                jarPath = URLDecoder.decode(jarPath, "UTF-8");
                findClassesInJar(jarPath, packageName, annotationClass, annotatedClasses);
            }
        }
        
        return annotatedClasses;
    }
    
    /**
     * Find classes with annotation in a directory (file system).
     */
    private static void findClassesInDirectory(File directory, String packageName, 
                                               Class<? extends java.lang.annotation.Annotation> annotationClass,
                                               List<Class<?>> result) {
        File[] files = directory.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            String fileName = file.getName();
            if (file.isDirectory()) {
                findClassesInDirectory(file, packageName + "." + fileName, annotationClass, result);
            } else if (fileName.endsWith(".class")) {
                String className = packageName + '.' + fileName.substring(0, fileName.length() - 6);
                try {
                    Class<?> clazz = Class.forName(className);
                    if (clazz.isAnnotationPresent(annotationClass)) {
                        result.add(clazz);
                    }
                } catch (Throwable e) {
                    // Skip classes that can't be loaded
                }
            }
        }
    }
    
    /**
     * Find classes with annotation in a JAR file.
     */
    private static void findClassesInJar(String jarPath, String packageName,
                                         Class<? extends java.lang.annotation.Annotation> annotationClass,
                                         List<Class<?>> result) {
        try {
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            String packagePath = packageName.replace('.', '/');
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    String className = entryName.replace('/', '.').substring(0, entryName.length() - 6);
                    try {
                        Class<?> clazz = Class.forName(className);
                        if (clazz.isAnnotationPresent(annotationClass)) {
                            result.add(clazz);
                        }
                    } catch (Throwable e) {
                        // Skip classes that can't be loaded
                    }
                }
            }
            jarFile.close();
        } catch (IOException e) {
            System.err.println("[ScriptContext] Failed to scan JAR: " + jarPath);
        }
    }
    
    /**
     * Fallback registration if classpath scanning fails.
     */
    private static void fallbackRegistration() {
        try {
            register("NPC", INpcEvent.class, IProjectileEvent.class);
            register("PLAYER", IPlayerEvent.class, IAnimationEvent.class, IPartyEvent.class, 
                    IDialogEvent.class, IQuestEvent.class, IFactionEvent.class, ICustomGuiEvent.class);
            register("BLOCK", IBlockEvent.class);
            register("ITEM", IItemEvent.class);
            register("FORGE", IForgeEvent.class);
            register("GLOBAL", "Global");
        } catch (Exception e) {
            System.err.println("[ScriptContext] Fallback registration also failed: " + e.getMessage());
        }
    }

    /**
     * Register a new script context with one or more namespaces.
     *
     * @param id Unique identifier (e.g., "DBC", "CUSTOM")
     * @param namespaces The event interface names (e.g., "IDBCEvent", "IDBCPlayerEvent")
     * @return The registered ScriptContext
     */
    public static ScriptContext register(String id, String... namespaces) {
        ScriptContext context = new ScriptContext(id, namespaces);
        REGISTRY.put(id, context);
        return context;
    }

    /**
     * Register a new script context using event interface classes.
     * Automatically extracts the simple name from each class.
     *
     * Example:
     *   ScriptContext.register("NPC", INpcEvent.class, IProjectileEvent.class);
     *
     * @param id Unique identifier (e.g., "DBC", "CUSTOM")
     * @param eventClasses The event interface classes (simple names will be extracted)
     * @return The registered ScriptContext
     */
    public static ScriptContext register(String id, Class<?>... eventClasses) {
        String[] namespaces = new String[eventClasses.length];
        for (int i = 0; i < eventClasses.length; i++) {
            namespaces[i] = eventClasses[i].getSimpleName();
        }
        return register(id, namespaces);
    }

    /**
     * Get a script context by its ID.
     *
     * @param id The context ID (e.g., "NPC", "PLAYER", "DBC")
     * @return The ScriptContext, or GLOBAL if not found
     */
    public static ScriptContext byId(String id) {
        return REGISTRY.getOrDefault(id, GLOBAL);
    }

    /**
     * Find a script context that contains a specific namespace.
     *
     * @param namespace The event interface namespace (e.g., "INpcEvent")
     * @return The first ScriptContext that contains this namespace, or GLOBAL if not found
     */
    public static ScriptContext byNamespace(String namespace) {
        if (namespace == null) return GLOBAL;
        for (ScriptContext ctx : REGISTRY.values()) {
            if (ctx.hasNamespace(namespace)) {
                return ctx;
            }
        }
        return GLOBAL;
    }

    /**
     * Get all registered script contexts.
     *
     * @return Unmodifiable collection of all registered contexts
     */
    public static Collection<ScriptContext> values() {
        return Collections.unmodifiableCollection(REGISTRY.values());
    }

    /**
     * Check if a context with the given ID exists.
     *
     * @param id The context ID to check
     * @return true if registered
     */
    public static boolean exists(String id) {
        return REGISTRY.containsKey(id);
    }

    // ==================== OBJECT METHODS ====================

    @Override
    public String toString() {
        return id + " -> " + namespaces;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof ScriptContext)) return false;
        ScriptContext other = (ScriptContext) obj;
        return id.equals(other.id);
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }
}
