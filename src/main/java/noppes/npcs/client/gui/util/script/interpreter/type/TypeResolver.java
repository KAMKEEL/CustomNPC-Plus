package noppes.npcs.client.gui.util.script.interpreter.type;

import noppes.npcs.client.gui.util.script.PackageFinder;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

import java.util.*;

/**
 * Resolves class/interface/enum types from import paths and simple names.
 * Maintains caches for performance and handles inner class resolution.
 * 
 * This is a clean reimplementation of ClassPathFinder with better OOP structure.
 */
public class TypeResolver {

    // Cache: fully-qualified class name -> TypeInfo
    private final Map<String, TypeInfo> typeCache = new HashMap<>();

    // Cache: validated package paths
    private final Set<String> validPackages = new HashSet<>();

    // Auto-imported java.lang classes
    public static final Set<String> JAVA_LANG_CLASSES = Collections.unmodifiableSet(new HashSet<>(Arrays.asList(
            "Object", "String", "Class", "System", "Math", "Integer", "Double", "Float", "Long", "Short", "Byte",
            "Character", "Boolean", "Number", "Void", "Thread", "Runnable", "Exception", "RuntimeException",
            "Error", "Throwable", "StringBuilder", "StringBuffer", "Enum", "Comparable", "Iterable",
            "CharSequence", "Cloneable", "Process", "ProcessBuilder", "Runtime", "SecurityManager",
            "ClassLoader", "Package", "ArithmeticException", "ArrayIndexOutOfBoundsException",
            "ClassCastException", "IllegalArgumentException", "IllegalStateException",
            "IndexOutOfBoundsException", "NullPointerException", "NumberFormatException",
            "UnsupportedOperationException", "AssertionError", "OutOfMemoryError", "StackOverflowError"
    )));

    // Singleton instance for global caching (optional - can also use per-document instances)
    private static TypeResolver instance;

    public static TypeResolver getInstance() {
        if (instance == null) {
            instance = new TypeResolver();
        }
        return instance;
    }

    public TypeResolver() {
        // Pre-register common packages
        validPackages.add("java");
        validPackages.add("java.lang");
        validPackages.add("java.util");
        validPackages.add("java.io");
    }

    // ==================== CACHE MANAGEMENT ====================

    /**
     * Clear all caches. Call when imports change significantly.
     */
    public void clearCache() {
        typeCache.clear();
        validPackages.clear();
        // Re-add common packages
        validPackages.add("java");
        validPackages.add("java.lang");
        validPackages.add("java.util");
        validPackages.add("java.io");
    }

    // ==================== TYPE RESOLUTION ====================

    /**
     * Resolve a fully-qualified class name.
     * Handles inner classes with both dot and dollar notation.
     */
    public TypeInfo resolveFullName(String fullName) {
        if (fullName == null || fullName.isEmpty()) {
            return null;
        }

        // Normalize: remove whitespace around dots
        String normalized = fullName.replaceAll("\\s*\\.\\s*", ".").trim();

        // Check cache first
        if (typeCache.containsKey(normalized)) {
            return typeCache.get(normalized);
        }

        // Try direct resolution
        TypeInfo result = tryResolveClass(normalized);
        if (result != null) {
            return result;
        }

        // Try converting dots to $ for inner classes
        // Work backwards from the end, trying each segment as an inner class
        String[] segments = normalized.split("\\.");
        for (int i = segments.length - 1; i > 0; i--) {
            StringBuilder candidate = new StringBuilder();
            // Package portion
            for (int j = 0; j < i; j++) {
                if (j > 0) candidate.append('.');
                candidate.append(segments[j]);
            }
            // Class portion with $
            for (int j = i; j < segments.length; j++) {
                candidate.append(j == i ? '.' : '$');
                candidate.append(segments[j]);
            }

            result = tryResolveClass(candidate.toString());
            if (result != null) {
                // Cache the original lookup
                typeCache.put(normalized, result);
                return result;
            }
        }

        // Not found - cache the miss
        typeCache.put(normalized, null);
        return null;
    }

    /**
     * Resolve a simple class name using provided import context.
     */
    public TypeInfo resolveSimpleName(String simpleName, 
                                      Map<String, ImportData> imports,
                                      Set<String> wildcardPackages) {
        if (simpleName == null || simpleName.isEmpty()) {
            return null;
        }

        // 1. Check explicit imports
        ImportData importData = imports.get(simpleName);
        if (importData != null && importData.getResolvedType() != null) {
            return importData.getResolvedType();
        }
        if (importData != null) {
            TypeInfo resolved = resolveFullName(importData.getFullPath());
            if (resolved != null) {
                importData.setResolvedType(resolved);
                return resolved;
            }
        }

        // 2. Check java.lang
        if (JAVA_LANG_CLASSES.contains(simpleName)) {
            TypeInfo langType = resolveFullName("java.lang." + simpleName);
            if (langType != null) {
                return langType;
            }
        }

        // 3. Check wildcard packages
        if (wildcardPackages != null) {
            for (String pkg : wildcardPackages) {
                // Try as package.ClassName
                TypeInfo fromPkg = resolveFullName(pkg + "." + simpleName);
                if (fromPkg != null) {
                    return fromPkg;
                }

                // Try as OuterClass$InnerClass (for class-level wildcards like IOverlay.*)
                TypeInfo fromInner = resolveFullName(pkg + "$" + simpleName);
                if (fromInner != null) {
                    return fromInner;
                }
            }
        }

        return null;
    }

    /**
     * Try to load a class and create TypeInfo for it.
     */
    private TypeInfo tryResolveClass(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }

        // Check cache
        if (typeCache.containsKey(className)) {
            return typeCache.get(className);
        }

        try {
            Class<?> clazz = Class.forName(className);
            TypeInfo info = TypeInfo.fromClass(clazz);
            typeCache.put(className, info);

            // Register the package as valid
            registerPackage(info.getPackageName());

            return info;
        } catch (ClassNotFoundException e) {
            // Cache the miss
            typeCache.put(className, null);
            return null;
        } catch (LinkageError e) {
            // NoClassDefFoundError is a subclass of LinkageError
            typeCache.put(className, null);
            return null;
        }
    }

    // ==================== PACKAGE VALIDATION ====================

    /**
     * Check if a package path is valid.
     */
    public boolean isValidPackage(String packagePath) {
        if (packagePath == null || packagePath.isEmpty()) {
            return false;
        }

        if (validPackages.contains(packagePath)) {
            return true;
        }

        if (PackageFinder.find(packagePath)) {
            registerPackage(packagePath);
            return true;
        }

        // Try to find a class in this package to validate it
        String[] testClasses = getTestClassesForPackage(packagePath);
        for (String testClass : testClasses) {
            try {
                Class.forName(testClass);
                registerPackage(packagePath);
                return true;
            } catch (ClassNotFoundException | LinkageError ignored) {
            }
        }

        return false;
    }

    /**
     * Register a package path as valid (and all parent packages).
     */
    private void registerPackage(String packagePath) {
        if (packagePath == null || packagePath.isEmpty()) {
            return;
        }
        validPackages.add(packagePath);

        // Also register parent packages
        int lastDot;
        String current = packagePath;
        while ((lastDot = current.lastIndexOf('.')) > 0) {
            current = current.substring(0, lastDot);
            validPackages.add(current);
        }
    }

    private String[] getTestClassesForPackage(String packagePath) {
        switch (packagePath) {
            case "java":
                return new String[]{"java.lang.Object"};
            case "java.util":
                return new String[]{"java.util.List", "java.util.Map"};
            case "java.io":
                return new String[]{"java.io.File", "java.io.InputStream"};
            case "java.net":
                return new String[]{"java.net.URL", "java.net.Socket"};
            case "java.lang":
                return new String[]{"java.lang.Object", "java.lang.String"};
            default:
                return new String[]{};
        }
    }

    // ==================== IMPORT RESOLUTION ====================

    /**
     * Resolve all imports and return a map of simple name -> ImportData.
     */
    public Map<String, ImportData> resolveImports(List<ImportData> imports) {
        Map<String, ImportData> resolved = new HashMap<>();

        for (ImportData imp : imports) {
            if (imp.isWildcard()) {
                // Wildcard imports: validate the package exists
                imp.markResolved(isValidPackage(imp.getFullPath()) || 
                                resolveFullName(imp.getFullPath()) != null);
            } else {
                // Specific import: resolve the class
                TypeInfo typeInfo = resolveFullName(imp.getFullPath());
                imp.setResolvedType(typeInfo);
                if (imp.getSimpleName() != null) {
                    resolved.put(imp.getSimpleName(), imp);
                }
            }
        }

        return resolved;
    }

    // ==================== GENERIC TYPE PARSING ====================

    /**
     * Parse type names from generic content like "Map<String, List<Integer>>".
     * Returns TypeInfo for each type found.
     */
    public List<GenericTypeOccurrence> parseGenericTypes(String content, 
                                                         Map<String, ImportData> imports,
                                                         Set<String> wildcardPackages) {
        List<GenericTypeOccurrence> results = new ArrayList<>();
        parseGenericTypesRecursive(content, 0, imports, wildcardPackages, results);
        return results;
    }

    private void parseGenericTypesRecursive(String content, int baseOffset,
                                            Map<String, ImportData> imports,
                                            Set<String> wildcardPackages,
                                            List<GenericTypeOccurrence> results) {
        if (content == null || content.isEmpty()) return;

        int i = 0;
        while (i < content.length()) {
            char c = content.charAt(i);

            // Skip non-identifier characters
            if (!Character.isJavaIdentifierStart(c)) {
                i++;
                continue;
            }

            // Found start of identifier
            int start = i;
            while (i < content.length() && Character.isJavaIdentifierPart(content.charAt(i))) {
                i++;
            }
            String typeName = content.substring(start, i);

            // Only process uppercase-starting identifiers as types
            if (Character.isUpperCase(typeName.charAt(0))) {
                TypeInfo info = resolveSimpleName(typeName, imports, wildcardPackages);
                results.add(new GenericTypeOccurrence(
                    baseOffset + start, 
                    baseOffset + i, 
                    typeName, 
                    info
                ));
            }

            // Skip whitespace
            while (i < content.length() && Character.isWhitespace(content.charAt(i))) {
                i++;
            }

            // Check for nested generic
            if (i < content.length() && content.charAt(i) == '<') {
                int nestedStart = i + 1;
                int depth = 1;
                i++;

                while (i < content.length() && depth > 0) {
                    if (content.charAt(i) == '<') depth++;
                    else if (content.charAt(i) == '>') depth--;
                    i++;
                }

                // Recursively parse nested content
                if (nestedStart < i - 1) {
                    String nestedContent = content.substring(nestedStart, i - 1);
                    parseGenericTypesRecursive(nestedContent, baseOffset + nestedStart, 
                                               imports, wildcardPackages, results);
                }
            }
        }
    }


    /**
     * Check if a type name is a primitive type.
     */
    public static boolean isPrimitiveType(String typeName) {
        return typeName.equals("boolean") || typeName.equals("byte") || typeName.equals("char") ||
                typeName.equals("short") || typeName.equals("int") || typeName.equals("long") ||
                typeName.equals("float") || typeName.equals("double") || typeName.equals("void");
    }

    /**
     * Check if a word is a Java modifier keyword.
     */
    public static boolean isModifier(String word) {
        return word.equals("public") || word.equals("private") || word.equals("protected") ||
                word.equals("static") || word.equals("final") || word.equals("abstract") ||
                word.equals("synchronized") || word.equals("volatile") || word.equals("transient") ||
                word.equals("native") || word.equals("strictfp");
    }
    
    /**
     * Represents a type occurrence within generic content.
     */
    public static class GenericTypeOccurrence {
        public final int startOffset;
        public final int endOffset;
        public final String typeName;
        public final TypeInfo typeInfo;

        public GenericTypeOccurrence(int startOffset, int endOffset, String typeName, TypeInfo typeInfo) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.typeName = typeName;
            this.typeInfo = typeInfo;
        }

        public TokenType getTokenType() {
            if (typeInfo == null || !typeInfo.isResolved()) {
                return TokenType.UNDEFINED_VAR;
            }
            return typeInfo.getTokenType();
        }
    }
}
