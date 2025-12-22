package noppes.npcs.client.gui.util.script;

import java.util.*;

/**
 * Helper class for resolving Java classpath imports and determining class types.
 * Handles:
 * - Class resolution via Class.forName
 * - Inner class notation (Outer.Inner -> Outer$Inner)
 * - Package validation
 * - Type detection (interface/enum/class)
 * - Result caching for performance
 */
public class ClassPathFinder {

    // Cache: fully-qualified class name -> ClassInfo (null if not found)
    private final Map<String, ClassInfo> classCache = new HashMap<>();

    // Cache: package prefix -> whether it's a valid package (has classes under it)
    private final Set<String> validPackages = new HashSet<>();

    // Common java.lang classes (auto-imported in Java)
    public static final Set<String> JAVA_LANG_CLASSES = new HashSet<>(Arrays.asList(
            "Object", "String", "Class", "System", "Math", "Integer", "Double", "Float", "Long", "Short", "Byte",
            "Character", "Boolean", "Number", "Void", "Thread", "Runnable", "Exception", "RuntimeException",
            "Error", "Throwable", "StringBuilder", "StringBuffer", "Enum", "Comparable", "Iterable",
            "CharSequence", "Cloneable", "Process", "ProcessBuilder", "Runtime", "SecurityManager",
            "ClassLoader", "Package", "ArithmeticException", "ArrayIndexOutOfBoundsException",
            "ClassCastException", "IllegalArgumentException", "IllegalStateException",
            "IndexOutOfBoundsException", "NullPointerException", "NumberFormatException",
            "UnsupportedOperationException", "AssertionError", "OutOfMemoryError", "StackOverflowError"
    ));

    /**
     * Information about a resolved class
     */
    public static class ClassInfo {
        public final String resolvedName;  // Full class name with $ for inner classes
        public final ClassType type;       // INTERFACE, ENUM, or CLASS
        public final int packageEndIndex;  // Index in original path where package ends (for "java.util.List" -> 9)

        public ClassInfo(String resolvedName, ClassType type, int packageEndIndex) {
            this.resolvedName = resolvedName;
            this.type = type;
            this.packageEndIndex = packageEndIndex;
        }
    }

    public enum ClassType {
        INTERFACE,
        ENUM,
        CLASS
    }

    /**
     * Result of resolving an import path
     */
    public static class ResolveResult {
        public final boolean found;
        public final ClassInfo classInfo;
        public final String packagePortion;    // The package part (e.g., "java.util")
        public final String classPortion;      // The class part (e.g., "List" or "IOverlay.ColorType")
        public final List<ClassSegment> classSegments; // Each class/inner-class segment with its type
        public final int invalidStartOffset;   // Character offset where invalid portion starts (-1 if all valid or all invalid)
        public final int packageSegmentCount;  // Number of dot-separated identifier tokens that belong to the package portion

        private ResolveResult(boolean found, ClassInfo classInfo, String packagePortion,
                              String classPortion, List<ClassSegment> classSegments, int invalidStartOffset,
                              int packageSegmentCount) {
            this.found = found;
            this.classInfo = classInfo;
            this.packagePortion = packagePortion;
            this.classPortion = classPortion;
            this.classSegments = classSegments != null ? classSegments : Collections.emptyList();
            this.invalidStartOffset = invalidStartOffset;
            this.packageSegmentCount = packageSegmentCount;
        }

        public static ResolveResult notFound(String packagePortion, String classPortion, int invalidStartOffset, int packageSegmentCount) {
            return new ResolveResult(false, null, packagePortion, classPortion, null, invalidStartOffset, packageSegmentCount);
        }

        public static ResolveResult found(ClassInfo info, String packagePortion,
                                          String classPortion, List<ClassSegment> segments, int packageSegmentCount) {
            return new ResolveResult(true, info, packagePortion, classPortion, segments, -1, packageSegmentCount);
        }
    }

    /**
     * Represents a segment of the class portion (e.g., "IOverlay" or "ColorType")
     */
    public static class ClassSegment {
        public final String name;
        public final ClassType type;

        public ClassSegment(String name, ClassType type) {
            this.name = name;
            this.type = type;
        }
    }

    /**
     * Clear all caches (call when imports change)
     */
    public void clearCache() {
        classCache.clear();
        validPackages.clear();
    }

    /**
     * Resolve an import path to determine if it's a valid class and get its type.
     * Handles inner classes like "kamkeel.npcdbc.api.client.overlay.IOverlay.ColorType"
     *
     * @param importPath The import path (e.g., "java.util.List" or "kamkeel...IOverlay.ColorType")
     * @return ResolveResult with resolution details
     */
    public ResolveResult resolve(String importPath) {
        if (importPath == null || importPath.isEmpty()) {
            return ResolveResult.notFound("", "", -1, 0);
        }

        // Normalize whitespace/newlines around dots so wrapped imports like
        // "kamkeel.npcdbc.api.client.overlay.IOverlay.\n TextureFunction" become
        // "kamkeel.npcdbc.api.client.overlay.IOverlay.TextureFunction" before resolving.
        String normalizedPath = importPath.replaceAll("\\s*\\.\\s*", ".").trim();

        // Handle trailing dot (user is still typing a package path like "java.util.")
        boolean trailingDot = normalizedPath.endsWith(".");
        String pathToResolve = trailingDot ? normalizedPath.substring(0, normalizedPath.length() - 1) : normalizedPath;

        if (pathToResolve.isEmpty()) {
            return ResolveResult.notFound("", "", -1, 0);
        }

        String[] segments = pathToResolve.split("\\.");
        int n = segments.length;

        // Find the first uppercase segment (likely start of class name)
        int firstUpperIndex = findFirstUppercaseSegment(segments);

        // No uppercase segments at all: this is a package-like path.
        // Validate as far as we can and mark invalid tail red.
        if (firstUpperIndex == n) {
            int longestValid = findLongestValidPackagePrefixIndex(segments);
            if (longestValid == n) {
                // Entire path is a valid package (or cached as valid)
                return ResolveResult.notFound(pathToResolve, "", -1, n);
            }

            if (longestValid > 0) {
                String validPrefix = buildPackagePortion(segments, longestValid);
                String invalidRemainder = buildClassPortion(segments, longestValid, n);
                int invalidOffset = validPrefix.length() + 1; // after the dot
                return ResolveResult.notFound(validPrefix, invalidRemainder, invalidOffset, longestValid);
            }

            // Nothing validated
            return ResolveResult.notFound("", pathToResolve, 0, 0);
        }

        // Trailing dot is handled above for lowercase-only paths; for mixed-case paths
        // we continue resolving as usual.

        // Try different splits: package vs class boundary
        // Start from firstUpperIndex and try treating each uppercase segment as potential class start
        for (int classStartIdx = firstUpperIndex; classStartIdx < n; classStartIdx++) {
            // Try different inner class depths (deepest first)
            for (int classEndIdx = n; classEndIdx > classStartIdx; classEndIdx--) {
                String candidate = buildClassName(segments, classStartIdx, classEndIdx);
                ClassInfo info = tryResolveClass(candidate);

                if (info != null) {
                    // Found a valid class!
                    String packagePortion = buildPackagePortion(segments, classStartIdx);
                    String validClassPortion = buildClassPortion(segments, classStartIdx, classEndIdx);

                    // Register this package as valid for future lookups
                    if (!packagePortion.isEmpty()) {
                        registerValidPackage(packagePortion);
                    }

                    // Check if there are MORE segments after what we resolved (invalid inner class)
                    if (classEndIdx < n) {
                        // e.g., IOverlay.ColorType resolved but there's more like ".idk"
                        // Mark the extra parts as invalid
                        List<ClassSegment> classSegments = buildClassSegments(segments, classStartIdx, classEndIdx);
                        String fullClassPortion = buildClassPortion(segments, classStartIdx, n);
                        // Calculate where the invalid portion starts (after valid class + dot)
                        int validEndOffset = packagePortion.length() + (packagePortion.isEmpty() ? 0 : 1) + validClassPortion.length() + 1;
                        return new ResolveResult(false, info, packagePortion,
                            fullClassPortion, classSegments, validEndOffset, classStartIdx);
                    }

                    // Full resolution successful
                    List<ClassSegment> classSegments = buildClassSegments(segments, classStartIdx, n);
                        return ResolveResult.found(
                            new ClassInfo(info.resolvedName, info.type, packagePortion.length()),
                            packagePortion, validClassPortion, classSegments, classStartIdx
                        );
                }
            }
        }

        // Class resolution failed completely
        // Strategy: Use heuristic - everything before first uppercase = package, rest = class
        // This ensures consistent behavior regardless of cache state

        if (firstUpperIndex > 0 && firstUpperIndex < n) {
            // There's a package portion (lowercase) and class portion (uppercase start)
            String packagePortion = buildPackagePortion(segments, firstUpperIndex);
            String classPortion = buildClassPortion(segments, firstUpperIndex, n);

            // Check if we can detect a typo in the package by comparing with cached packages
            int typoIndex = findPackageTypoIndex(segments, firstUpperIndex);
            if (typoIndex >= 0 && typoIndex < firstUpperIndex) {
                // Found a divergence from a known valid package
                String validPrefix = buildPackagePortion(segments, typoIndex);
                int invalidOffset = validPrefix.isEmpty() ? 0 : validPrefix.length() + 1;
                return ResolveResult.notFound(validPrefix, buildClassPortion(segments, typoIndex, n), invalidOffset, typoIndex);
            }

            // No typo detected - assume package is valid, only class portion is invalid
            int invalidOffset = packagePortion.length() + 1; // After the package dot
            return ResolveResult.notFound(packagePortion, classPortion, invalidOffset, firstUpperIndex);

        } else if (firstUpperIndex == 0) {
            // Entire path starts with uppercase (e.g., "String" or "MyClass.Inner")
            // Treat as class with no package
            return ResolveResult.notFound("", pathToResolve, 0, 0);

        } else {
            // All lowercase, no uppercase found - entire path is package-like
            // If trailing dot was handled above, this is just a package without class
            return ResolveResult.notFound(pathToResolve, "", -1, n);
        }
    }

    /**
     * Find index where the typed package diverges from a known cached package.
     * Returns -1 if no typo detected, or the segment index where divergence starts.
     */
    private int findPackageTypoIndex(String[] segments, int classStartIdx) {
        // Build the typed package path segment by segment and compare with cache
        StringBuilder sb = new StringBuilder();
        int lastValidIndex = -1;
        
        for (int i = 0; i < classStartIdx; i++) {
            if (i > 0) sb.append(".");
            sb.append(segments[i]);
            String currentPath = sb.toString();

            // Check if this exact path is in cache
            if (validPackages.contains(currentPath)) {
                lastValidIndex = i + 1; // This segment is valid
            } else if (i > 0) {
                // Not in cache - check if parent is valid but this segment is wrong
                String parentPath = buildPackagePortion(segments, i);
                if (validPackages.contains(parentPath)) {
                    // Parent is valid, but current segment isn't in any cached path
                    // Check if there ARE cached paths extending from parent
                    boolean hasAlternative = false;
                    for (String cached : validPackages) {
                        if (cached.startsWith(parentPath + ".") && cached.length() > parentPath.length() + 1) {
                            hasAlternative = true;
                            break;
                        }
                    }
                    if (hasAlternative) {
                        // There are valid continuations from parent, but this segment doesn't match any
                        return i; // This segment is likely a typo
                    }
                }
            }
        }
        
        // If we found some valid prefix but not all, return where it stopped being valid
        if (lastValidIndex > 0 && lastValidIndex < classStartIdx) {
            // Check if segment at lastValidIndex diverges from any known path
            String validPrefix = buildPackagePortion(segments, lastValidIndex);
            for (String cached : validPackages) {
                if (cached.startsWith(validPrefix + ".")) {
                    // There's a known continuation - check if typed segment matches
                    String nextTyped = segments[lastValidIndex];
                    String cachedRemainder = cached.substring(validPrefix.length() + 1);
                    String nextCached = cachedRemainder.contains(".") ? 
                            cachedRemainder.substring(0, cachedRemainder.indexOf('.')) : cachedRemainder;
                    if (!nextTyped.equals(nextCached)) {
                        return lastValidIndex; // Divergence point
                    }
                }
            }
        }
        
        return -1; // No typo detected
    }

    /**
     * Check if a package path is valid (has been seen in successful resolutions or can be validated)
     */
    public boolean isValidPackage(String packagePath) {
        if (packagePath == null || packagePath.isEmpty()) {
            return false;
        }

        // Check cache first
        if (validPackages.contains(packagePath)) {
            return true;
        }
        
        // Try to find any class under this package to validate it
        // Common test classes for well-known packages
        String[] testClasses = getTestClassesForPackage(packagePath);
        for (String testClass : testClasses) {
            try {
                Class.forName(testClass);
                registerValidPackage(packagePath);
                return true;
            } catch (ClassNotFoundException | LinkageError ignored) {
            }
        }

        return false;
    }

    /**
     * Get the ClassInfo for a simple class name, using provided imports map
     */
    public ClassInfo resolveSimpleName(String simpleName, Map<String, String> importedClasses) {
        return resolveSimpleName(simpleName, importedClasses, Collections.emptySet());
    }

    /**
     * Resolve a simple class name using explicit imports, java.lang, or wildcard imports.
     * Supports package wildcard imports (e.g., java.util.*) and class-member wildcards
     * that have been added to `importedPackages` (e.g., kamkeel..IOverlay.* treated as
     * attempting to resolve inner types of IOverlay).
     */
    public ClassInfo resolveSimpleName(String simpleName, Map<String, String> importedClasses, Set<String> importedPackages) {
        if (simpleName == null || simpleName.isEmpty()) {
            return null;
        }

        // Check imported classes first
        String fullName = importedClasses.get(simpleName);
        if (fullName != null) {
            ClassInfo info = tryResolveClass(fullName);
            if (info != null) return info;
        }

        // Check java.lang classes
        if (JAVA_LANG_CLASSES.contains(simpleName)) {
            ClassInfo info = tryResolveClass("java.lang." + simpleName);
            if (info != null) return info;
        }

        // Check wildcard imports: these can be package wildcards (pkg.*) or
        // class-member wildcards (Outer.*) where we want to allow resolving
        // inner types of the Outer class. Heuristic: if the last segment of
        // the imported path starts with uppercase, treat it as a class; otherwise
        // treat it as a package.
        if (importedPackages != null && !importedPackages.isEmpty()) {
            for (String p : importedPackages) {
                if (p == null || p.isEmpty()) continue;
                String[] segs = p.split("\\.");
                String last = segs[segs.length - 1];
                boolean lastIsClass = last.length() > 0 && Character.isUpperCase(last.charAt(0));

                // Try package wildcard: pkg.SimpleName
                if (!lastIsClass) {
                    String candidate = p + "." + simpleName;
                    ClassInfo info = tryResolveClass(candidate);
                    if (info != null) return info;
                } else {
                    // Try inner type under the class: Outer$Inner
                    String candidateInner = p + "$" + simpleName;
                    ClassInfo info = tryResolveClass(candidateInner);
                    if (info != null) return info;

                    // Also try package-like (in case someone imported a class-looking path but it is actually a package)
                    String candidatePkg = p + "." + simpleName;
                    info = tryResolveClass(candidatePkg);
                    if (info != null) return info;
                }
            }
        }

        return null;
    }

    /**
     * Try to resolve a fully-qualified class name (with $ for inner classes)
     */
    private ClassInfo tryResolveClass(String className) {
        if (className == null || className.isEmpty()) {
            return null;
        }

        // Check cache
        if (classCache.containsKey(className)) {
            return classCache.get(className);
        }

        ClassInfo result = null;

        try {
            Class<?> clazz = Class.forName(className);
            result = createClassInfo(className, clazz);
            classCache.put(className, result);
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Not found with given name, will be cached as null
        } catch (LinkageError e) {
            // Linkage error - treat as not found
        }

        return result;
    }

    private ClassInfo createClassInfo(String resolvedName, Class<?> clazz) {
        ClassType type;
        if (clazz.isInterface()) {
            type = ClassType.INTERFACE;
        } else if (clazz.isEnum()) {
            type = ClassType.ENUM;
        } else {
            type = ClassType.CLASS;
        }

        // Calculate package end index
        int pkgEnd = resolvedName.lastIndexOf('.');
        if (pkgEnd < 0)
            pkgEnd = 0;

        return new ClassInfo(resolvedName, type, pkgEnd);
    }

    /**
     * Build a class name candidate from segments, using $ for inner classes
     */
    private String buildClassName(String[] segments, int classStartIdx, int classEndIdx) {
        StringBuilder sb = new StringBuilder();

        // Package portion (dot-separated)
        for (int i = 0; i < classStartIdx; i++) {
            if (i > 0)
                sb.append('.');
            sb.append(segments[i]);
        }

        // Class portion ($ for inner classes after the first class)
        for (int i = classStartIdx; i < classEndIdx; i++) {
            if (i == classStartIdx) {
                if (sb.length() > 0)
                    sb.append('.');
            } else {
                sb.append('$');
            }
            sb.append(segments[i]);
        }

        return sb.toString();
    }

    private String buildPackagePortion(String[] segments, int classStartIdx) {
        if (classStartIdx <= 0)
            return "";
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < classStartIdx; i++) {
            if (i > 0)
                sb.append('.');
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    private String buildClassPortion(String[] segments, int classStartIdx, int endIdx) {
        StringBuilder sb = new StringBuilder();
        for (int i = classStartIdx; i < endIdx; i++) {
            if (i > classStartIdx)
                sb.append('.');
            sb.append(segments[i]);
        }
        return sb.toString();
    }

    private List<ClassSegment> buildClassSegments(String[] segments, int classStartIdx, int endIdx) {
        List<ClassSegment> result = new ArrayList<>();
        StringBuilder fullName = new StringBuilder();

        // Build package portion
        for (int i = 0; i < classStartIdx; i++) {
            if (i > 0)
                fullName.append('.');
            fullName.append(segments[i]);
        }

        // Add each class segment
        for (int i = classStartIdx; i < endIdx; i++) {
            if (i == classStartIdx) {
                if (fullName.length() > 0)
                    fullName.append('.');
            } else {
                fullName.append('$');
            }
            fullName.append(segments[i]);

            // Try to resolve this partial class name
            ClassInfo info = tryResolveClass(fullName.toString());
            ClassType type = (info != null) ? info.type : ClassType.CLASS;
            result.add(new ClassSegment(segments[i], type));
        }

        return result;
    }

    private int findFirstUppercaseSegment(String[] segments) {
        for (int i = 0; i < segments.length; i++) {
            if (segments[i].length() > 0 && Character.isUpperCase(segments[i].charAt(0))) {
                return i;
            }
        }
        return segments.length; // No uppercase segment
    }

    private int findLongestValidPackagePrefixIndex(String[] segments) {
        int longestValid = 0;
        for (int i = 1; i <= segments.length; i++) {
            String pkg = buildPackagePortion(segments, i);
            if (isValidPackage(pkg)) {
                longestValid = i;
            } else {
                // If a prefix isn't valid, deeper prefixes can't be valid packages either.
                break;
            }
        }
        return longestValid;
    }

    private void registerValidPackage(String packagePath) {
        validPackages.add(packagePath);

        // Also register all parent packages
        int lastDot;
        String current = packagePath;
        while ((lastDot = current.lastIndexOf('.')) > 0) {
            current = current.substring(0, lastDot);
            validPackages.add(current);
        }
    }

    private String[] getTestClassesForPackage(String packagePath) {
        // Return common classes for well-known packages
        switch (packagePath) {
            case "java":
                return new String[]{"java.lang.Object"};
            case "java.util":
                return new String[]{"java.util.List", "java.util.Map", "java.util.Set"};
            case "java.io":
                return new String[]{"java.io.File", "java.io.InputStream"};
            case "java.net":
                return new String[]{"java.net.URL", "java.net.Socket"};
            case "java.lang":
                return new String[]{"java.lang.Object", "java.lang.String"};
            default:
                // For unknown packages, try common class suffixes
                return new String[]{
                        packagePath + ".package-info",  // Won't work but harmless
                };
        }
    }

    // ==================== GENERIC TYPE PARSING ====================

    /**
     * Parse generic type content and return type names with their positions.
     * Handles arbitrary nesting depth like "Map<String, List<Map<String, String>>>"
     *
     * @param genericContent The content inside <...> (without the outer angle brackets)
     * @param importedClasses Map of simple name -> full class name for resolution
     * @return List of TypeOccurrence with position and type info
     */
    public List<TypeOccurrence> parseGenericTypes(String genericContent, Map<String, String> importedClasses) {
        return parseGenericTypes(genericContent, importedClasses, Collections.emptySet());
    }

    public List<TypeOccurrence> parseGenericTypes(String genericContent, Map<String, String> importedClasses, Set<String> importedPackages) {
        List<TypeOccurrence> results = new ArrayList<>();
        parseGenericTypesRecursive(genericContent, 0, importedClasses, importedPackages, results);
        return results;
    }

    /**
     * Represents a type occurrence within generic content
     */
    public static class TypeOccurrence {
        public final int startOffset;  // Relative to the start of genericContent
        public final int endOffset;
        public final String typeName;
        public final ClassType type;

        public TypeOccurrence(int startOffset, int endOffset, String typeName, ClassType type) {
            this.startOffset = startOffset;
            this.endOffset = endOffset;
            this.typeName = typeName;
            this.type = type;
        }
    }

    private void parseGenericTypesRecursive(String content, int baseOffset,
                                            Map<String, String> importedClasses,
                                            Set<String> importedPackages,
                                            List<TypeOccurrence> results) {
        if (content == null || content.isEmpty())
            return;

        int i = 0;
        while (i < content.length()) {
            char c = content.charAt(i);

            // Skip whitespace and punctuation
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

            // Only process if it looks like a type name (starts with uppercase)
            if (Character.isUpperCase(typeName.charAt(0))) {
                // Resolve the type
                ClassInfo info = resolveSimpleName(typeName, importedClasses, importedPackages);
                ClassType classType = (info != null) ? info.type : ClassType.CLASS;

                results.add(new TypeOccurrence(baseOffset + start, baseOffset + i, typeName, classType));
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

                // Find matching >
                while (i < content.length() && depth > 0) {
                    if (content.charAt(i) == '<')
                        depth++;
                    else if (content.charAt(i) == '>')
                        depth--;
                    i++;
                }

                // Recursively parse nested content
                if (nestedStart < i - 1) {
                    String nestedContent = content.substring(nestedStart, i - 1);
                    parseGenericTypesRecursive(nestedContent, baseOffset + nestedStart, importedClasses, importedPackages, results);
                }
            }
        }
    }
}
