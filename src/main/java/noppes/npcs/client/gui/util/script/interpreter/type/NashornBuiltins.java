package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.SyntheticMethod;
import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.SyntheticParameter;
import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.SyntheticTypeBuilder;
import noppes.npcs.client.gui.util.script.interpreter.type.synthetic.SyntheticType;

import java.util.*;

/**
 * Registry for Nashorn built-in types and global functions.
 * 
 * <p>Nashorn provides several built-in objects and functions:</p>
 * <ul>
 *   <li><b>Java</b> - Object for Java interop: type(), extend(), from(), to()</li>
 *   <li><b>Packages</b> - Root of package hierarchy for accessing Java packages</li>
 *   <li><b>JavaImporter</b> - Creates scope with imported Java packages</li>
 *   <li><b>print()</b> - Global print function</li>
 *   <li><b>load()</b> - Load and execute a script</li>
 * </ul>
 */
public class NashornBuiltins {
    
    private static NashornBuiltins instance;
    
    private final Map<String, SyntheticType> builtinTypes = new LinkedHashMap<>();
    private final Map<String, SyntheticMethod> globalFunctions = new LinkedHashMap<>();
    
    private NashornBuiltins() {
        initializeBuiltins();
    }
    
    public static NashornBuiltins getInstance() {
        if (instance == null) {
            instance = new NashornBuiltins();
        }
        return instance;
    }
    
    private void initializeBuiltins() {
        // ==================== Java object ====================
        SyntheticType javaType = new SyntheticTypeBuilder("Java")
            .documentation("Nashorn's Java interop object for accessing Java types and utilities.")
            
            .addMethod("type")
                .parameter("className", "String", "Fully-qualified Java class name")
                .returns("java.lang.Class")
                .returnsResolved(args -> {
                    if (args != null && args.length > 0 && args[0] != null) {
                        String className = args[0];
                        // Remove quotes if present
                        if (className.startsWith("\"") && className.endsWith("\"")) {
                            className = className.substring(1, className.length() - 1);
                        } else if (className.startsWith("'") && className.endsWith("'")) {
                            className = className.substring(1, className.length() - 1);
                        }
                        // Try to load the class
                        TypeInfo resolved = TypeResolver.getInstance().resolve(className);
                        if (resolved != null && resolved.isResolved()) {
                            return new ClassTypeInfo(resolved);
                        }
                    }
                    return null;
                })
                .documentation("Loads a Java class by its fully-qualified name.\n\n" +
                    "**Usage:**\n" +
                    "```javascript\n" +
                    "var File = Java.type(\"java.io.File\");\n" +
                    "var file = new File(\"path/to/file\");\n" +
                    "```\n\n" +
                    "@param className The fully-qualified Java class name\n" +
                    "@returns The Java class reference (use with 'new' to create instances)")
                .done()
                
            .addMethod("extend")
                .parameter("type", "java.lang.Class", "Java class or interface to extend/implement")
                .returns("java.lang.Class")
                .documentation("Creates a subclass or implementation of a Java class or interface.\n\n" +
                    "**Usage:**\n" +
                    "```javascript\n" +
                    "var MyRunnable = Java.extend(Java.type(\"java.lang.Runnable\"), {\n" +
                    "    run: function() {\n" +
                    "        print(\"Running!\");\n" +
                    "    }\n" +
                    "});\n" +
                    "```\n\n" +
                    "@param type The Java class or interface to extend\n" +
                    "@returns A new class that extends/implements the given type")
                .done()
                
            .addMethod("from")
                .parameter("javaArray", "Object", "A Java array or Collection")
                .returns("Array")
                .documentation("Converts a Java array or Collection to a JavaScript array.\n\n" +
                    "**Usage:**\n" +
                    "```javascript\n" +
                    "var jsList = Java.from(javaArrayList);\n" +
                    "jsList.forEach(function(item) { print(item); });\n" +
                    "```\n\n" +
                    "@param javaArray A Java array or java.util.Collection\n" +
                    "@returns A JavaScript array containing the elements")
                .done()
                
            .addMethod("to")
                .parameter("jsArray", "Array", "A JavaScript array")
                .parameter("javaType", "java.lang.Class", "The target Java array type")
                .returns("Object")
                .documentation("Converts a JavaScript array to a Java array of the specified type.\n\n" +
                    "**Usage:**\n" +
                    "```javascript\n" +
                    "var jsArray = [1, 2, 3];\n" +
                    "var intArray = Java.to(jsArray, \"int[]\");\n" +
                    "```\n\n" +
                    "@param jsArray A JavaScript array\n" +
                    "@param javaType The target Java array type (e.g., \"int[]\", \"java.lang.String[]\")\n" +
                    "@returns A Java array of the specified type")
                .done()
                
            .addMethod("super")
                .parameter("object", "Object", "A Java object created via Java.extend()")
                .returns("Object")
                .documentation("Gets a reference to the super class for calling super methods.\n\n" +
                    "**Usage:**\n" +
                    "```javascript\n" +
                    "var MyList = Java.extend(Java.type(\"java.util.ArrayList\"), {\n" +
                    "    add: function(e) {\n" +
                    "        print(\"Adding: \" + e);\n" +
                    "        return Java.super(this).add(e);\n" +
                    "    }\n" +
                    "});\n" +
                    "```\n\n" +
                    "@param object An extended Java object\n" +
                    "@returns A reference to call super methods")
                .done()
                
            .addMethod("synchronized")
                .parameter("func", "Function", "The function to synchronize")
                .parameter("lock", "Object", "The object to synchronize on")
                .returns("Function")
                .documentation("Wraps a function to execute synchronized on a given object.\n\n" +
                    "**Usage:**\n" +
                    "```javascript\n" +
                    "var syncFunc = Java.synchronized(function() {\n" +
                    "    // Thread-safe code here\n" +
                    "}, lockObject);\n" +
                    "```\n\n" +
                    "@param func The function to synchronize\n" +
                    "@param lock The object to use as the monitor\n" +
                    "@returns A synchronized version of the function")
                .done()
                
            .build();
        builtinTypes.put("Java", javaType);
        
        // ==================== Global print function ====================
        SyntheticType printFunc = new SyntheticTypeBuilder("print")
            .addMethod("print")
                .parameter("message", "Object", "The message to print")
                .returns("void")
                .documentation("Prints a message to standard output.\n\n" +
                    "**Usage:**\n" +
                    "```javascript\n" +
                    "print(\"Hello, world!\");\n" +
                    "print(myObject);\n" +
                    "```")
                .done()
            .build();
        globalFunctions.put("print", printFunc.getMethod("print"));
        
        // ==================== Global load function ====================
        SyntheticType loadFunc = new SyntheticTypeBuilder("load")
            .addMethod("load")
                .parameter("script", "String", "Path or URL to the script")
                .returns("Object")
                .documentation("Loads and executes a script file or URL.\n\n" +
                    "**Usage:**\n" +
                    "```javascript\n" +
                    "load(\"./myScript.js\");\n" +
                    "load(\"http://example.com/script.js\");\n" +
                    "```\n\n" +
                    "@param script Path to a local script or URL\n" +
                    "@returns The result of the script execution")
                .done()
            .build();
        globalFunctions.put("load", loadFunc.getMethod("load"));
    }
    
    /**
     * Get a built-in type by name.
     * @param name The type name (e.g., "Java")
     * @return The SyntheticType or null if not found
     */
    public SyntheticType getBuiltinType(String name) {
        return builtinTypes.get(name);
    }
    
    /**
     * Check if a name is a built-in type.
     */
    public boolean isBuiltinType(String name) {
        return builtinTypes.containsKey(name);
    }
    
    /**
     * Get a global function by name.
     */
    public SyntheticMethod getGlobalFunction(String name) {
        return globalFunctions.get(name);
    }
    
    /**
     * Check if a name is a global function.
     */
    public boolean isGlobalFunction(String name) {
        return globalFunctions.containsKey(name);
    }
    
    /**
     * Get all built-in type names.
     */
    public Set<String> getBuiltinTypeNames() {
        return Collections.unmodifiableSet(builtinTypes.keySet());
    }
    
    /**
     * Get all global function names.
     */
    public Set<String> getGlobalFunctionNames() {
        return Collections.unmodifiableSet(globalFunctions.keySet());
    }
    
    /**
     * Get all built-in types.
     */
    public Collection<SyntheticType> getAllBuiltinTypes() {
        return Collections.unmodifiableCollection(builtinTypes.values());
    }
    
    /**
     * Get all global functions as SyntheticTypes.
     * Each function is wrapped in its own SyntheticType for the registry.
     */
    public Map<String, SyntheticType> getAllGlobalFunctions() {
        Map<String, SyntheticType> result = new LinkedHashMap<>();
        
        // Wrap each global function in a minimal SyntheticType
        for (Map.Entry<String, SyntheticMethod> entry : globalFunctions.entrySet()) {
            String name = entry.getKey();
            SyntheticMethod method = entry.getValue();
            
            // Create a synthetic type that just contains this one global function
            SyntheticTypeBuilder builder = new SyntheticTypeBuilder(name);
            builder.documentation("Global " + name + " function");
            
            // Add the method
            SyntheticTypeBuilder.MethodBuilder methodBuilder = builder.addMethod(name);
            for (SyntheticParameter param : method.parameters) {
                methodBuilder.parameter(param.name, param.typeName, param.documentation);
            }
            methodBuilder.returns(method.returnType);
            if (method.documentation != null && !method.documentation.isEmpty()) {
                methodBuilder.documentation(method.documentation);
            }
            methodBuilder.done();
            
            result.put(name, builder.build());
        }
        
        return result;
    }
    
    /**
     * Resolve the return type of Java.type() given the class name argument.
     */
    public TypeInfo resolveJavaType(String className) {
        SyntheticType javaBuiltin = builtinTypes.get("Java");
        if (javaBuiltin != null) {
            return javaBuiltin.resolveMethodReturnType("type", new String[]{className});
        }
        return null;
    }
}
