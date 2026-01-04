package noppes.npcs.client.gui.util.script.interpreter.type;

import java.io.File;
import java.net.URL;
import java.util.*;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;

/**
 * Index of Java classes for autocomplete suggestions.
 * Uses reflection to automatically extract class names and packages.
 * Supports multiple classes with the same simple name (e.g., java.util.Date and java.sql.Date).
 */
public class ClassIndex {
    private static ClassIndex instance;
    
    // Map simple names to multiple classes (handles name collisions)
    private final Map<String, List<Class<?>>> simpleNameToClasses = new LinkedHashMap<>();
    private final Set<Class<?>> registeredClasses = new HashSet<>();
    
    private ClassIndex() {
        // Initialize with common classes
        initializeCommonClasses();
    }
    
    public static ClassIndex getInstance() {
        if (instance == null) {
            instance = new ClassIndex();
        }
        return instance;
    }
    
    public static void init(){
        getInstance();
    }
    /**
     * Register a single class by its Class<?> object.
     * The simple name and full name are extracted via reflection.
     * Supports multiple classes with the same simple name.
     */
    public void addClass(Class<?> clazz) {
        if (clazz == null) return;
        
        // Check if already registered using the Class<?> object itself
        if (registeredClasses.contains(clazz)) {
            return; // Already registered
        }
        
        registeredClasses.add(clazz);
        String simpleName = clazz.getSimpleName();
        
        // Only add if simple name is not empty (avoid inner classes with $ in name)
        if (!simpleName.isEmpty() && !simpleName.contains("$")) {
            // Add to list of classes with this simple name
            simpleNameToClasses.computeIfAbsent(simpleName, k -> new ArrayList<>()).add(clazz);
        }
    }
    
    /**
     * Register all classes in a package and all subpackages recursively.
     * Uses ClassLoader to scan the classpath.
     */
    public void addPackage(String packageName) {
        try {
            ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
            String path = packageName.replace('.', '/');
            Enumeration<URL> resources = classLoader.getResources(path);
            
            Set<String> classNames = new HashSet<>();
            
            while (resources.hasMoreElements()) {
                URL resource = resources.nextElement();
                
                if (resource.getProtocol().equals("file")) {
                    // Scan directory
                    File directory = new File(resource.getFile());
                    scanDirectory(directory, packageName, classNames);
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
                    scanJar(jarPath, packageName, classNames);
                }
            }
            
            // Load all discovered classes
            for (String className : classNames) {
                try {
                    Class<?> clazz = Class.forName(className, false, classLoader);
                    addClass(clazz);
                } catch (ClassNotFoundException | NoClassDefFoundError | ExceptionInInitializerError e) {
                    // Skip classes that can't be loaded
                }
            }
        } catch (Exception e) {
            // Silently fail - package might not exist
        }
    }
    
    private void scanDirectory(File directory, String packageName, Set<String> classNames) {
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
                scanDirectory(file, packageName + "." + fileName, classNames);
            } else if (fileName.endsWith(".class")) {
                // Add class name
                String className = packageName + "." + fileName.substring(0, fileName.length() - 6);
                classNames.add(className);
            }
        }
    }
    
    private void scanJar(String jarPath, String packageName, Set<String> classNames) {
        try {
            JarFile jarFile = new JarFile(jarPath);
            Enumeration<JarEntry> entries = jarFile.entries();
            String packagePath = packageName.replace('.', '/');
            
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                String entryName = entry.getName();
                
                if (entryName.startsWith(packagePath) && entryName.endsWith(".class")) {
                    // Convert path to class name
                    String className = entryName
                        .substring(0, entryName.length() - 6)
                        .replace('/', '.');
                    classNames.add(className);
                }
            }
            
            jarFile.close();
        } catch (Exception e) {
            // Silently fail
        }
    }
    
    /**
     * Find all classes whose simple name starts with the given prefix (case-insensitive).
     * Returns fully-qualified class names for all matches, including multiple classes with the same simple name.
     */
    public List<String> findByPrefix(String prefix, int maxResults) {
        List<String> results = new ArrayList<>();
        String lowerPrefix = prefix.toLowerCase();
        
        for (Map.Entry<String, List<Class<?>>> entry : simpleNameToClasses.entrySet()) {
            if (entry.getKey().toLowerCase().startsWith(lowerPrefix)) {
                // Add all classes with this simple name
                for (Class<?> clazz : entry.getValue()) {
                    results.add(clazz.getName());
                    
                    if (results.size() >= maxResults) {
                        return results;
                    }
                }
            }
        }
        
        return results;
    }
    
    /**
     * Initialize the index with commonly used Java, Minecraft, and NPC classes.
     */
    private void initializeCommonClasses() {
        // Java standard library
        addClass(String.class);
        addClass(Integer.class);
        addClass(Double.class);
        addClass(Float.class);
        addClass(Long.class);
        addClass(Boolean.class);
        addClass(Character.class);
        addClass(Byte.class);
        addClass(Short.class);
        addClass(Object.class);
        addClass(Math.class);
        addClass(System.class);
        addClass(Thread.class);
        addClass(Runnable.class);
        addClass(Exception.class);
        addClass(RuntimeException.class);
        addClass(Error.class);
        addClass(Throwable.class);
        
        // Java collections
        addClass(List.class);
        addClass(ArrayList.class);
        addClass(LinkedList.class);
        addClass(Set.class);
        addClass(HashSet.class);
        addClass(LinkedHashSet.class);
        addClass(TreeSet.class);
        addClass(Map.class);
        addClass(HashMap.class);
        addClass(LinkedHashMap.class);
        addClass(TreeMap.class);
        addClass(Collection.class);
        addClass(Iterator.class);
        addClass(Comparator.class);
        addClass(Collections.class);
        addClass(Arrays.class);
        addClass(Queue.class);
        addClass(Deque.class);
        addClass(Stack.class);
        addClass(Vector.class);
        addClass(Hashtable.class);
        
        // Java I/O
        addClass(java.io.File.class);
        addClass(java.io.InputStream.class);
        addClass(java.io.OutputStream.class);
        addClass(java.io.Reader.class);
        addClass(java.io.Writer.class);
        addClass(java.io.BufferedReader.class);
        addClass(java.io.BufferedWriter.class);
        addClass(java.io.FileReader.class);
        addClass(java.io.FileWriter.class);
        addClass(java.io.IOException.class);
        
        // Java utilities
        addClass(java.util.Random.class);
        addClass(java.util.Date.class);
        addClass(java.util.Calendar.class);
        addClass(java.util.UUID.class);
        addClass(java.util.regex.Pattern.class);
        addClass(java.util.regex.Matcher.class);
        addPackage("java.util.function");
        
        // Now add common Minecraft/Forge/NPC packages
        addPackage("net.minecraft.entity");
        addPackage("net.minecraft.item");
        addPackage("net.minecraft.block");
        addPackage("net.minecraft.world");
        addPackage("net.minecraft.util");
        addPackage("net.minecraft.nbt");
        addPackage("net.minecraft.potion");
        addPackage("net.minecraft.enchantment");
        addPackage("net.minecraft.inventory");
        addPackage("net.minecraft.tileentity");
        addPackage("net.minecraft.command");
        addPackage("net.minecraft.client");
        
        // Forge
        addPackage("net.minecraftforge.common");
        addPackage("net.minecraftforge.event");
        addPackage("net.minecraftforge.fml.common");
        
        // CustomNPCs
        addPackage("noppes.npcs");
        
        // DBC (if available)
        try {
            addPackage("JinRyuu.JRMCore");
            addPackage("JinRyuu.DragonBC");
            addPackage("kamkeel.npcdbc");
        } catch (Exception e) {
            // DBC might not be available
        }
    }
}