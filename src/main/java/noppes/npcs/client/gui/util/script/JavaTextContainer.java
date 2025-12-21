package noppes.npcs.client.gui.util.script;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.TextContainer;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class JavaTextContainer extends TextContainer {

    public static final Pattern MODIFIER = Pattern.compile(
            "\\b(public|protected|private|static|final|abstract|synchronized|native|default)\\b");
    public static final Pattern KEYWORD = Pattern.compile(
            "\\b(null|boolean|int|float|double|long|char|byte|short|void|if|else|switch|case|for|while|do|try|catch|finally|return|throw|var|let|const|function|continue|break|this|new|typeof|instanceof|import)\\b");

    public static final Pattern CLASS_DECL = Pattern.compile("\\b(class|interface|enum)\\s+([A-Za-z_][a-zA-Z0-9_]*)");
    
    public static final Pattern TYPE_DECL = Pattern.compile(
            "(?:(?:public|private|protected|static|final|transient|volatile)\\s+)*" + // optional modifiers
            "([A-Za-z_][a-zA-Z0-9_]*)" + // Group 1 → main type
            "\\s*(<([^<>]*(?:<[^<>]*>)*)>)?" + // Group 2 → <…>, Group 3 → inner type (handles one level of nesting)
            "\\s+[a-zA-Z_][a-zA-Z0-9_]*" // variable name
    );

    public static final Pattern NEW_TYPE = Pattern.compile("\\bnew\\s+([A-Za-z_][a-zA-Z0-9_]*)");

    public static final Pattern METHOD_DECL = Pattern.compile("\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)\\s+" + // return type
            "([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(" // method name
    );
    public static final Pattern METHOD_CALL = Pattern.compile("([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\(");

    // Class-level global fields
    public static final Pattern GLOBAL_FIELD_DECL = Pattern.compile(
            "\\b([A-Za-z_][a-zA-Z0-9_<>\\[\\]]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|;)");

    // Local fields inside methods (simplified)
    public static final Pattern LOCAL_FIELD_DECL = Pattern.compile(
            "\\b([A-Z][a-zA-Z0-9_<>\\[\\]]*|[a-z][a-zA-Z0-9_]*)\\s+([a-zA-Z_][a-zA-Z0-9_]*)\\s*(=|;)");

    public static final Pattern STRING = Pattern.compile("([\"'])(?:(?=(\\\\?))\\2.)*?\\1");
    public static final Pattern COMMENT = Pattern.compile("/\\*[\\s\\S]*?(?:\\*/|$)|//.*|#.*");
    public static final Pattern NUMBER = Pattern.compile(
            "\\b-?(?:0[xX][\\dA-Fa-f]+|0[bB][01]+|0[oO][0-7]+|\\d*\\.?\\d+(?:[Ee][+-]?\\d+)?(?:[fFbBdDlLsS])?|NaN|null|Infinity|true|false)\\b");

    // Import statement: import [static] package.path.ClassName [.*];
    public static final Pattern IMPORT = Pattern.compile(
            "\\bimport\\s+(?:static\\s+)?([a-zA-Z_][a-zA-Z0-9_.]*?)(?:\\s*\\.\\s*\\*)?\\s*;");

    public List<LineData> lines = new ArrayList<>();
    public List<MethodBlock> methodBlocks = new ArrayList<>();

    public JavaTextContainer(String text) {
        super(text);
    }

    public void init(String text,int width, int height) {
        this.text = text == null ? "" : text.replaceAll("\\r?\\n|\\r", "\n");
        lines.clear();
        String[] split = text.split("\n",-1);

        int totalChars = 0;
        for (String l : split) {
            StringBuilder line = new StringBuilder();
            // Break the source line `l` into layout-friendly segments using `regexWord`.
            // `regexWord` finds word tokens (letters/numbers/underscore/hyphen), newlines
            // or the end-of-line. The loop takes substrings from the previous match
            // index `i` up to the current `m.start()` so each `word` contains the
            // next token plus any following delimiters (spaces, punctuation).
            // This lets us measure and wrap at token boundaries rather than mid-word.
            Matcher m = regexWord.matcher(l);
            int i = 0;
            while (m.find()) {
                String word = l.substring(i, m.start());
                if (ClientProxy.Font.width(line + word) > width - 10) {
                    // Note: `end` is an exclusive offset into the full text (start..end).
                    // For wrapped lines we record the current `totalChars` as the start
                    // and compute the exclusive end as `start + line.length()`.
                    lines.add(new LineData(line.toString(), totalChars, totalChars += line.length()));
                    line = new StringBuilder();
                }
                line.append(word);
                i = m.start();
            }
            lines.add(new LineData(line.toString(), totalChars, totalChars += line.length() + 1));
        }
        linesCount = lines.size();
        totalHeight = linesCount * lineHeight;
        // Number of fully-visible lines that fit in the given viewport height.
        // Use floor division and ensure at least 1 line is visible.
        // Don't forget -1, fixes enter auto-scrolling properly
        visibleLines = Math.max(height / lineHeight - 1, 1);
    }

    private List<String> globalFields = new ArrayList<>();
    private List<String> localFields = new ArrayList<>();
    private java.util.Map<String, String> importedClasses = new java.util.HashMap<>(); // simpleName -> fullName
    private java.util.Set<String> importedPackages = new java.util.HashSet<>(); // wildcard imports
    private java.util.Map<String, TokenType> classTypeCache = new java.util.HashMap<>(); // fullClassName -> TokenType (cached reflection results)
    private java.util.Set<String> resolvedPackages = new java.util.HashSet<>();
    private java.util.Set<String> knownPackages = new java.util.HashSet<>();

    // Common java.lang classes (auto-imported)
    private static final java.util.Set<String> JAVA_LANG_CLASSES = new java.util.HashSet<>(java.util.Arrays.asList(
            "Object", "String", "Class", "System", "Math", "Integer", "Double", "Float", "Long", "Short", "Byte",
            "Character", "Boolean",
            "Number", "Void", "Thread", "Runnable", "Exception", "RuntimeException", "Error", "Throwable",
            "StringBuilder", "StringBuffer", "Enum", "Comparable", "Iterable", "CharSequence", "Cloneable",
            "Process", "ProcessBuilder", "Runtime", "SecurityManager", "ClassLoader", "Package",
            "ArithmeticException", "ArrayIndexOutOfBoundsException", "ClassCastException", "IllegalArgumentException",
            "IllegalStateException", "IndexOutOfBoundsException", "NullPointerException", "NumberFormatException",
            "UnsupportedOperationException", "AssertionError", "OutOfMemoryError", "StackOverflowError"
    ));

    private void collectImports() {
        importedClasses.clear();
        importedPackages.clear();
        // Clear cached import resolutions when re-parsing imports (keeps editor responsive during typing)
        importResolveCache.clear();
        knownPackages.clear();

        List<int[]> excluded = MethodBlock.getExcludedRanges(text);
        Matcher m = IMPORT.matcher(text);
        while (m.find()) {
            if (isInExcludedRange(m.start(), excluded))
                continue;
            String fullPath = m.group(1).trim();
            // Check if wildcard import (import a.b.*;)
            if (text.substring(m.start(), m.end()).contains("*")) {
                importedPackages.add(fullPath);
            } else {
                // Specific class import: extract simple name
                int lastDot = fullPath.lastIndexOf('.');
                String simpleName = lastDot >= 0 ? fullPath.substring(lastDot + 1) : fullPath;
                
                // Remember the package prefix as a known package (even before resolution)
                if (lastDot > 0) {
                    String pkg = fullPath.substring(0, lastDot);
                    knownPackages.add(pkg);
                }
                // Store with $ notation for inner classes (e.g., IOverlay.ColorType -> IOverlay$ColorType)
                String storagePath = fullPath.replaceAll("\\.(\\p{Upper})", "\\$$1");
                importedClasses.put(simpleName, storagePath);
            }
        }
    }

    // Cache for resolved import attempts: original fullPath -> resolved class name (with $ for inner classes) or null if unresolved
    private java.util.Map<String, String> importResolveCache = new java.util.HashMap<>();

    private static class ResolvedImport {
        String resolvedClass; // full class name with $ notation if found
        int classStartIndex; // index in segments where class starts
        TokenType tokenType;
        boolean found;
    }

    /**
     * Try to resolve an import path to an actual class using Class.forName by attempting
     * different class-start splits and inner-class ($) combinations. Caches results.
     */
    private ResolvedImport resolveImportPath(String fullPath) {
        ResolvedImport ri = new ResolvedImport();
        if (fullPath == null || fullPath.isEmpty()) {
            ri.found = false; return ri;
        }
        if (importResolveCache.containsKey(fullPath)) {
            String cached = importResolveCache.get(fullPath);
            if (cached == null) { ri.found = false; return ri; }
            ri.resolvedClass = cached;
            ri.found = true;
            ri.tokenType = getClassTokenType(cached, TokenType.IMPORTED_CLASS);
            // derive classStartIndex by comparing segments
            String[] segs = fullPath.split("\\.");
            String[] rcsegs = cached.replace('$', '.').split("\\.");
            int si = 0; while (si < segs.length && si < rcsegs.length && segs[si].equals(rcsegs[si])) si++;
            ri.classStartIndex = si;
            return ri;
        }

        String[] segments = fullPath.split("\\.");
        int n = segments.length;

        // Try candidate class start positions. Start from left-most uppercase-looking segment for efficiency.
        int firstUpper = 0;
        for (int i = 0; i < n; i++) {
            if (segments[i].length() > 0 && Character.isUpperCase(segments[i].charAt(0))) { firstUpper = i; break; }
        }

        outer:
        for (int k = firstUpper; k < n; k++) {
            // number of inner parts available
            int innerAvailable = n - (k + 1);
            // Try the longest inner suffix first (deepest inner class)
            for (int innerCount = innerAvailable; innerCount >= 0; innerCount--) {
                // build top-level class and inner suffix
                StringBuilder classBuilder = new StringBuilder();
                // package portion
                if (k > 0) {
                    for (int i = 0; i < k; i++) {
                        if (i > 0) classBuilder.append('.');
                        classBuilder.append(segments[i]);
                    }
                    classBuilder.append('.');
                }
                // top class
                classBuilder.append(segments[k]);
                // inner parts
                if (innerCount > 0) {
                    classBuilder.append('$');
                    for (int j = 0; j < innerCount; j++) {
                        if (j > 0) classBuilder.append('$');
                        classBuilder.append(segments[k + 1 + j]);
                    }
                }
                String candidate = classBuilder.toString();
                TokenType tt = getClassTokenType(candidate, TokenType.IMPORTED_CLASS);
                if (tt != TokenType.UNDEFINED_VAR) {
                    ri.found = true;
                    ri.resolvedClass = candidate;
                    ri.classStartIndex = k;
                    ri.tokenType = tt;
                    importResolveCache.put(fullPath, candidate);
                    break outer;
                }
                // also try without package separator if k==0 (top-level without package)
                if (k == 0 && innerCount == 0) {
                    // candidate is segments[0] only, already tried
                }
            }
        }

        if (!ri.found) {
            importResolveCache.put(fullPath, null);
            ri.found = false;
            // approximate classStartIndex as first uppercase or last segment
            int classStart = 0;
            for (int i = 0; i < segments.length; i++) {
                if (segments[i].length() > 0 && Character.isUpperCase(segments[i].charAt(0))) { classStart = i; break; }
            }
            ri.classStartIndex = classStart;
        }
        return ri;
    }
    
    /**
     * Determine the token type for a class using reflection.
     * Returns INTERFACE_DECL for interfaces, ENUM_DECL for enums, UNDEFINED for unfound classes, or the defaultType for classes.
     */
    private TokenType getClassTokenType(String fullClassName, TokenType defaultType) {
        if (fullClassName == null || fullClassName.isEmpty()) return defaultType;
        
        // Check cache first
       // if (classTypeCache.containsKey(fullClassName)) {
          //  return classTypeCache.get(fullClassName);
        //}
        TokenType result = defaultType;
        try {
            Class<?> clazz = Class.forName(fullClassName);
            if (clazz.isInterface()) {
                result = TokenType.INTERFACE_DECL;
            } else if (clazz.isEnum()) {
                result = TokenType.ENUM_DECL;
            } else {
                result = defaultType; // regular class
            }
        } catch (ClassNotFoundException | NoClassDefFoundError e) {
            // Try with $ notation for inner classes (e.g., IOverlay$ColorType)
            String innerClassNotation = fullClassName.replaceAll("\\.(\\p{Upper})", "\\$$1");
            if (!innerClassNotation.equals(fullClassName)) {
                try {
                    Class<?> clazz = Class.forName(innerClassNotation);
                    if (clazz.isInterface()) {
                        result = TokenType.INTERFACE_DECL;
                    } else if (clazz.isEnum()) { 
                        result = TokenType.ENUM_DECL;
                    } else {
                        result = defaultType; // regular class
                    }
                    // Cache with original notation
                    classTypeCache.put(fullClassName, result);
                    return result;
                } catch (Exception e2) {
                    // Still not found
                }
            }
            // Class not found - mark as undefined
            result = TokenType.UNDEFINED_VAR;
        } catch (LinkageError e) {
            // Linkage errors (other than NoClassDefFoundError) - treat as undefined
            result = TokenType.UNDEFINED_VAR;
        }
        
        // Cache the result
        classTypeCache.put(fullClassName, result);
        return result;
    }
    
    /**
     * Parse and highlight generic type parameters recursively (e.g., "List<String>" or "Map<String, List<List<Integer>>>")
     * Handles arbitrarily nested generics by tracking bracket depth.
     */
    private void highlightGenericTypes(String genericContent, int contentStart, List<Mark> marks, List<int[]> excluded) {
        if (genericContent == null || genericContent.isEmpty()) return;
        
        int i = 0;
        while (i < genericContent.length()) {
            char c = genericContent.charAt(i);
            
            // Check if we're starting a type name (uppercase letter)
            if (Character.isJavaIdentifierStart(c) && Character.isUpperCase(c)) {
                int start = i;
                while (i < genericContent.length() && Character.isJavaIdentifierPart(genericContent.charAt(i))) {
                    i++;
                }
                String typeName = genericContent.substring(start, i);
                int absStart = contentStart + start;
                int absEnd = contentStart + i;
                
                if (!isInExcludedRange(absStart, excluded)) {
                    // Resolve full class name
                    String fullClassName = importedClasses.get(typeName);
                    if (fullClassName == null && JAVA_LANG_CLASSES.contains(typeName)) {
                        fullClassName = "java.lang." + typeName;
                    }
                    
                    TokenType tokenType = getClassTokenType(fullClassName, TokenType.IMPORTED_CLASS);
                    marks.add(new Mark(absStart, absEnd, tokenType));
                }
                
                // Check for nested generics after this type
                while (i < genericContent.length() && Character.isWhitespace(genericContent.charAt(i))) i++;
                if (i < genericContent.length() && genericContent.charAt(i) == '<') {
                    // Find matching >
                    int depth = 1;
                    int nestedStart = i + 1;
                    i++;
                    while (i < genericContent.length() && depth > 0) {
                        if (genericContent.charAt(i) == '<') depth++;
                        else if (genericContent.charAt(i) == '>') depth--;
                        i++;
                    }
                    // Recursively process nested content
                    String nestedContent = genericContent.substring(nestedStart, i - 1);
                    highlightGenericTypes(nestedContent, contentStart + nestedStart, marks, excluded);
                    // Continue after the closing > to find more types
                }
                // Continue to next character (handles commas, spaces, etc.)
            } else {
                i++;
            }
        }
    }
    
    private void collectFields() {
        globalFields.clear();
        localFields.clear();
        methodBlocks.clear();

        // Extract method blocks first
        methodBlocks = MethodBlock.collectMethodBlocks(text);
        
        // Get excluded ranges (strings and comments) for the entire text
        List<int[]> excludedRanges = MethodBlock.getExcludedRanges(text);

        // Global fields (excluding those inside methods, strings, and comments)
        Matcher mGlobal = GLOBAL_FIELD_DECL.matcher(text);
        while (mGlobal.find()) {
            String varName = mGlobal.group(2);
            int varPosition = mGlobal.start(2);
            
            // Skip if inside a string or comment
            if (isInExcludedRange(varPosition, excludedRanges)) {
                continue;
            }

            // Check if this variable is inside a method
            boolean isInsideMethod = false;
            for (MethodBlock block : methodBlocks) {
                if (block.containsPosition(varPosition)) {
                    isInsideMethod = true;
                    break;
                }
            }

            // Only add as global if it's not inside any method
            if (!isInsideMethod) {
                globalFields.add(varName);
            }
        }

        // Extract local variables from each method block
        for (MethodBlock block : methodBlocks) {
            // localVariables are already extracted in MethodBlock constructor
            for (String var : block.localVariables) {
                if (!globalFields.contains(var) && !localFields.contains(var)) {
                    localFields.add(var);
                }
            }
        }
    }

    // Find which method block contains a given position
    private MethodBlock findMethodBlockAtPosition(int position) {
        for (MethodBlock block : methodBlocks) {
            if (block.containsPosition(position)) {
                return block;
            }
        }
        return null;
    }

    private void highlightVariableReferences(List<Mark> marks) {
        // Known Java keywords and types that shouldn't be flagged as undefined
        java.util.Set<String> knownIdentifiers = new java.util.HashSet<>(java.util.Arrays.asList(
            // Primitive types
            "boolean", "int", "float", "double", "long", "char", "byte", "short", "void",
            // Keywords
            "null", "true", "false", "if", "else", "switch", "case", "for", "while", "do",
            "try", "catch", "finally", "return", "throw", "var", "let", "const", "function",
            "continue", "break", "this", "new", "typeof", "instanceof", "class", "interface",
            "extends", "implements", "import", "package", "public", "private", "protected",
            "static", "final", "abstract", "synchronized", "native", "default", "enum",
            "throws", "super", "assert", "volatile", "transient", "strictfp", "goto",
            // Common JS/scripting keywords
            "undefined", "NaN", "Infinity", "arguments", "prototype", "constructor",
            // Common types (first letter uppercase pattern handles most)
            "String", "Object", "Array", "Math", "System", "Integer", "Double", "Float",
            "Boolean", "Long", "Byte", "Short", "Character", "List", "Map", "Set"
        ));
        
        // First pass: handle field accesses (this.field and obj.field patterns)
        Pattern thisFieldPattern = Pattern.compile("\\bthis\\s*\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher thisFieldMatcher = thisFieldPattern.matcher(text);
        while (thisFieldMatcher.find()) {
            String fieldName = thisFieldMatcher.group(1);
            int fieldPos = thisFieldMatcher.start(1);
            // Highlight as GLOBAL_FIELD if it exists, UNDEFINED_VAR otherwise
            if (globalFields.contains(fieldName)) {
                marks.add(new Mark(fieldPos, thisFieldMatcher.end(1), TokenType.GLOBAL_FIELD));
            } else {
                marks.add(new Mark(fieldPos, thisFieldMatcher.end(1), TokenType.UNDEFINED_VAR));
            }
        }
        
        // Handle identifier.field patterns (e.g., obj.field, global.field)
        // The field part should be highlighted as GLOBAL_FIELD (light blue)
        Pattern objFieldPattern = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\s*\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher objFieldMatcher = objFieldPattern.matcher(text);
        while (objFieldMatcher.find()) {
            String fieldName = objFieldMatcher.group(2);
            int fieldPos = objFieldMatcher.start(2);
            // Skip if this is a "this.field" pattern (already handled above)
            String objName = objFieldMatcher.group(1);
            if (!objName.equals("this")) {
                // Highlight the field part as GLOBAL_FIELD (represents another object's field)
                marks.add(new Mark(fieldPos, objFieldMatcher.end(2), TokenType.GLOBAL_FIELD));
            }
        }
        
        Pattern identifier = Pattern.compile("\\b([a-zA-Z_][a-zA-Z0-9_]*)\\b");
        Matcher m = identifier.matcher(text);
        while (m.find()) {
            String name = m.group(1);
            int position = m.start(1);
            
            // Skip known keywords and types
            if (knownIdentifiers.contains(name)) continue;
            
            // Skip type names (first letter uppercase)
            if (Character.isUpperCase(name.charAt(0))) continue;
            
            // Skip field access (identifier preceded by a dot) - these should be gray/default
            // e.g., in "container.lines", the "lines" part should not be marked
            if (isFieldAccess(position)) continue;
            
            // Check if inside a method
            MethodBlock methodBlock = findMethodBlockAtPosition(position);
            
            if (methodBlock != null) {
                // Inside a method - check in order: parameter, local (position-aware), global
                if (methodBlock.parameters.contains(name)) {
                    marks.add(new Mark(m.start(1), m.end(1), TokenType.PARAMETER));
                } else if (methodBlock.isLocalDeclaredAtPosition(name, position)) {
                    // Local variable is declared at or before this position
                    marks.add(new Mark(m.start(1), m.end(1), TokenType.LOCAL_FIELD));
                } else if (globalFields.contains(name)) {
                    marks.add(new Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD));
                } else {
                    // Unknown variable - mark as undefined (Bug 11)
                    // But only if it looks like a variable reference (not a method call)
                    if (!isMethodCall(position) && !isTypeReference(name, position)) {
                        marks.add(new Mark(m.start(1), m.end(1), TokenType.UNDEFINED_VAR));
                    }
                }
            } else {
                // Outside any method - check global fields
                if (globalFields.contains(name)) {
                    marks.add(new Mark(m.start(1), m.end(1), TokenType.GLOBAL_FIELD));
                }
                // Don't mark as undefined outside methods - could be a type name or declaration
            }
        }
    }
    
    /**
     * Check if the identifier at this position is a field access (preceded by a dot)
     * e.g., in "obj.field", the "field" part is a field access
     */
    private boolean isFieldAccess(int position) {
        if (position <= 0) return false;
        
        // Look backwards from position, skipping any whitespace
        int i = position - 1;
        while (i >= 0 && Character.isWhitespace(text.charAt(i))) {
            i--;
        }
        
        // Check if preceded by a dot
        return i >= 0 && text.charAt(i) == '.';
    }
    
    /**
     * Check if the identifier at this position is a method call (followed by parenthesis)
     */
    private boolean isMethodCall(int position) {
        // Skip whitespace after identifier
        int i = position;
        while (i < text.length() && Character.isLetterOrDigit(text.charAt(i)) || text.charAt(i) == '_') {
            i++;
        }
        // Skip whitespace
        while (i < text.length() && Character.isWhitespace(text.charAt(i))) {
            i++;
        }
        // Check for opening paren
        return i < text.length() && text.charAt(i) == '(';
    }
    
    /**
     * Check if this looks like a type reference (e.g., part of a declaration or generic)
     * But NOT a comparison like "i < container" which uses < as less-than operator
     */
    private boolean isTypeReference(String name, int position) {
        // Check if preceded by 'new '
        if (position > 4) {
            String before = text.substring(Math.max(0, position - 5), position);
            if (before.endsWith("new ")) {
                return true;
            }
        }
        
        // Check if preceded by < but make sure it's a generic, not a comparison
        // Generic: Type<Name or ,Name in generics
        // Comparison: value < name (space before < means comparison)
        if (position > 1) {
            int checkPos = position - 1;
            // Skip whitespace
            while (checkPos > 0 && Character.isWhitespace(text.charAt(checkPos))) {
                checkPos--;
            }
            if (checkPos >= 0 && text.charAt(checkPos) == '<') {
                // Check what's before the <
                int beforeLt = checkPos - 1;
                while (beforeLt >= 0 && Character.isWhitespace(text.charAt(beforeLt))) {
                    beforeLt--;
                }
                if (beforeLt >= 0) {
                    char beforeChar = text.charAt(beforeLt);
                    // If it's a letter/digit/underscore (identifier) or ) followed by space + <, it's a comparison
                    // If it's a type name directly followed by <, it's a generic
                    // Check if there was whitespace between the identifier and <
                    boolean hasSpaceBeforeLt = (checkPos > 0 && Character.isWhitespace(text.charAt(checkPos - 1)));
                    if (hasSpaceBeforeLt && (Character.isLetterOrDigit(beforeChar) || beforeChar == '_' || beforeChar == ')')) {
                        // This is a comparison like "i < container" or "(x + 1) < y"
                        return false;
                    }
                    // Otherwise it's likely a generic like List<String>
                    if (Character.isLetter(beforeChar) || beforeChar == '>') {
                        return true;
                    }
                }
            }
            // Check for comma in generics like Map<K, V>
            if (checkPos >= 0 && text.charAt(checkPos) == ',') {
                return true;
            }
        }
        return false;
    }
    
    /**
     * Check if position is in an excluded range (string or comment)
     */
    private boolean isInExcludedRange(int pos, List<int[]> ranges) {
        for (int[] range : ranges) {
            if (pos >= range[0] && pos < range[1]) {
                return true;
            }
        }
        return false;
    }

    private void collectImportStatements(List<Mark> marks) {
        List<int[]> excluded = MethodBlock.getExcludedRanges(text);
        // Pattern to match: import [static] package.path.ClassName [.*];
        Matcher m = IMPORT.matcher(text);
        while (m.find()) {
            if (isInExcludedRange(m.start(), excluded))
                continue;
            String fullPath = m.group(1).trim();
            int pathStart = m.start(1);
            int pathEnd = m.end(1);

            // Highlight the 'import' keyword itself
            int importKwStart = m.start();
            int importKwEnd = Math.min(importKwStart + 6, text.length()); // "import"
            marks.add(new Mark(importKwStart, importKwEnd, TokenType.IMPORT_KEYWORD));

            // Resolve import path to an actual class if possible
            ResolvedImport ri = resolveImportPath(fullPath);
            String[] segments = fullPath.split("\\.");

            // Compute package portion length
            int pkgSegments = 0;
            if (ri.found && ri.resolvedClass != null) {
                pkgSegments = Math.max(0, ri.classStartIndex);
                // register resolved package prefix
                if (pkgSegments > 0) {
                    StringBuilder pb = new StringBuilder();
                    for (int i = 0; i < pkgSegments; i++) {
                        if (i > 0) pb.append('.');
                        pb.append(segments[i]);
                    }
                    resolvedPackages.add(pb.toString());
                }
            } else {
                // Try to find the longest previously-resolved package prefix, or known package prefix
                for (int p = segments.length - 1; p >= 1; p--) {
                    StringBuilder test = new StringBuilder();
                    for (int i = 0; i < p; i++) {
                        if (i > 0) test.append('.');
                        test.append(segments[i]);
                    }
                    String testStr = test.toString();
                    if (resolvedPackages.contains(testStr)) {
                        pkgSegments = p;
                        break;
                    }
                    if (knownPackages.contains(testStr)) {
                        pkgSegments = p;
                        // don't break; prefer a resolved package if found later in loop
                    }
                }
                // fallback to ri.classStartIndex if nothing known
                if (pkgSegments == 0) pkgSegments = Math.max(0, ri.classStartIndex);
            }
            StringBuilder pkgBuilder = new StringBuilder();
            for (int i = 0; i < pkgSegments; i++) {
                if (i > 0) pkgBuilder.append('.');
                pkgBuilder.append(segments[i]);
            }
            int pkgLen = pkgBuilder.length();
            if (pkgLen > 0) {
                marks.add(new Mark(pathStart, pathStart + pkgLen + 1, TokenType.TYPE_DECL)); // package + dot
            }

            // Highlight class and inner-class segments
            int classStart = pathStart + (pkgLen > 0 ? pkgLen + 1 : 0);
            if (ri.found && ri.resolvedClass != null) {
                // Use resolved class (with $). Break into segments for progressive checks.
                for (int i = ri.classStartIndex; i < segments.length; i++) {
                    int segStart = classStart;
                    int segEnd = classStart + segments[i].length();

                    // Build candidate full name up to this segment using $ for inner separators
                    StringBuilder testBuilder = new StringBuilder();
                    if (ri.classStartIndex > 0) {
                        for (int p = 0; p < ri.classStartIndex; p++) {
                            if (p > 0) testBuilder.append('.');
                            testBuilder.append(segments[p]);
                        }
                        testBuilder.append('.');
                    }
                    testBuilder.append(segments[ri.classStartIndex]);
                    for (int q = ri.classStartIndex + 1; q <= i; q++) {
                        testBuilder.append('$').append(segments[q]);
                    }
                    String testFull = testBuilder.toString();
                    TokenType segType = getClassTokenType(testFull, TokenType.IMPORTED_CLASS);
                    marks.add(new Mark(segStart, segEnd, segType));

                    // Update importedClasses map for simple name resolution
                    if (i == segments.length - 1) {
                        importedClasses.put(segments[i], ri.resolvedClass);
                    }

                    classStart = segEnd + 1; // +1 for dot
                }
            } else {
                // Unresolved import: mark the entire tail (after known package prefix) as undefined
                int undefStart = classStart;
                int undefEnd = pathEnd;
                if (undefStart < undefEnd) {
                    marks.add(new Mark(undefStart, undefEnd, TokenType.UNDEFINED_VAR));
                }
                // Remove any previously-added simple-name mapping to avoid treating it as imported
                String simpleName = segments[segments.length - 1];
                if (importedClasses.containsKey(simpleName)) {
                    importedClasses.remove(simpleName);
                }
            }

            // Highlight .* if present (find '*' between match bounds)
            String matchText = text.substring(m.start(), m.end());
            int starIndex = matchText.indexOf('*');
            if (starIndex != -1) {
                int absStar = m.start() + starIndex;
                marks.add(new Mark(absStar, absStar + 1, TokenType.DEFAULT));
            }
        }
    }

    private void collectImportedClassUsages(List<Mark> marks) {
        List<int[]> excluded = MethodBlock.getExcludedRanges(text);
        // Find identifiers followed by dot (e.g., Math.min, ArrayList.class)
        Pattern classUsage = Pattern.compile("\\b([A-Z][a-zA-Z0-9_]*)\\s*\\.");
        Matcher m = classUsage.matcher(text);
        while (m.find()) {
            String className = m.group(1);
            int start = m.start(1);
            int end = m.end(1);
            if (isInExcludedRange(start, excluded))
                continue;

            // Check if this is an imported class, wildcard match, or java.lang class
            boolean isImported = importedClasses.containsKey(className) || JAVA_LANG_CLASSES.contains(className);
            if (!isImported) {
                // Check wildcard imports (heuristic: if any imported package exists, mark it)
                for (String pkg : importedPackages) {
                    isImported = true;
                    break;
                }
            }

            // Also skip if it's a local/global field or parameter (avoid false positives)
            MethodBlock block = findMethodBlockAtPosition(start);
            boolean isShadowed = globalFields.contains(className) || localFields.contains(className);
            if (block != null) {
                isShadowed = isShadowed || block.parameters.contains(className) || block.isLocalDeclaredAtPosition(
                        className, start);
            }

            if (isImported && !isShadowed) {
                // Determine token type dynamically using reflection
                String fullClassName = importedClasses.get(className);
                if (fullClassName == null && JAVA_LANG_CLASSES.contains(className)) {
                    fullClassName = "java.lang." + className;
                }
                TokenType tokenType = getClassTokenType(fullClassName, TokenType.IMPORTED_CLASS);
                marks.add(new Mark(start, end, tokenType));
            }
        }
    }

    private void collectClassDeclarations(List<Mark> marks) {
        Matcher m = CLASS_DECL.matcher(text);
        List<int[]> excluded = MethodBlock.getExcludedRanges(text);
        while (m.find()) {
            marks.add(new Mark(m.start(1), m.end(1), TokenType.CLASS_KEYWORD));

            int nameStart = m.start(2);
            int nameEnd = m.end(2);
            if (isInExcludedRange(nameStart, excluded))
                continue;
            String kind = m.group(1);
            if ("interface".equals(kind)) {
                marks.add(new Mark(nameStart, nameEnd, TokenType.INTERFACE_DECL));
            } else if ("enum".equals(kind)) {
                marks.add(new Mark(nameStart, nameEnd, TokenType.ENUM_DECL));
            } else {
                marks.add(new Mark(nameStart, nameEnd, TokenType.CLASS_DECL));
            }
        }
    }
    
    public void formatCodeText() {
        // Step 1: Tokenize the full text
        List<Mark> marks = new ArrayList<>();

        collectImports(); // Parse import statements first
        collectFields(); // Extract global and local fields with method scoping
        collectPatternMatches(marks, COMMENT, TokenType.COMMENT);
        collectPatternMatches(marks, STRING, TokenType.STRING);

        collectImportStatements(marks); // Highlight import statements
        collectClassDeclarations(marks); // Highlight class/interface/enum declarations

        // Highlight general keywords
        collectPatternMatches(marks, KEYWORD, TokenType.KEYWORD);
        collectPatternMatches(marks, MODIFIER, TokenType.MODIFIER);

        collectTypeDeclarations(marks);
        collectPatternMatches(marks, NEW_TYPE, TokenType.NEW_TYPE, 1);

        collectMethodDeclarations(marks);
        collectPatternMatches(marks, METHOD_CALL, TokenType.METHOD_CALL, 1);

        collectPatternMatches(marks, NUMBER, TokenType.NUMBER);

        highlightVariableReferences(marks);
        collectImportedClassUsages(marks); // Highlight imported class usages (e.g., Math.min)

        marks = resolveConflicts(marks);

        // Compute indent guides based on matched braces, ignoring strings/comments
        computeIndentGuides(marks);
        
        // Step 2: Clear existing tokens
        for (LineData line : lines) {
            line.tokens.clear();
        }

        // Step 3: Assign tokens to the correct lines
        for (LineData line : lines) {
            int cursor = line.start;

            for (Mark mark : marks) {
                if (mark.end <= line.start || mark.start >= line.end)
                    continue;

                int tokenStart = Math.max(mark.start, line.start);
                int tokenEnd = Math.min(mark.end, line.end);

                tokenStart = Math.max(0, Math.min(tokenStart, text.length()));
                tokenEnd = Math.max(0, Math.min(tokenEnd, text.length()));

                // plain text before token
                if (cursor < tokenStart) {
                    int end = Math.min(tokenStart, text.length());
                    line.tokens.add(
                            new Token(text.substring(cursor, end), TokenType.DEFAULT, cursor, end));
                }

                // token text
                if (tokenStart < tokenEnd) {
                    line.tokens.add(new Token(text.substring(tokenStart, Math.min(tokenEnd, text.length())), mark.type, tokenStart, Math.min(tokenEnd, text.length())));
                }

                cursor = tokenEnd;
            }

            // trailing plain text
            if (cursor < line.end) {
                int end = Math.min(line.end, text.length());
                line.tokens.add(new Token(text.substring(cursor, end), TokenType.DEFAULT, cursor, end));
            }
        }
    }

    private void collectMethodDeclarations(List<Mark> marks) {
        Matcher m = METHOD_DECL.matcher(text);
        while (m.find()) { // method name
            marks.add(new Mark(m.start(2), m.end(2), TokenType.METHOD_DECARE));
            // return type
            marks.add(new Mark(m.start(1), m.end(1), TokenType.TYPE_DECL));
        }
    }

    private void collectTypeDeclarations(List<Mark> marks) {
        // Run TYPE_DECL on each line to avoid matches bleeding across newlines
        List<int[]> excluded = MethodBlock.getExcludedRanges(text);
        for (LineData ld : lines) {
            int lineStart = ld.start;
            int lineEnd = ld.end;
            if (lineStart >= lineEnd) continue;
            String s = ld.text;
            Matcher m = TYPE_DECL.matcher(s);
            while (m.find()) {
                int g1s = lineStart + m.start(1);
                int g1e = lineStart + m.end(1);
                // Skip if any part of the match overlaps an excluded range (string/comment)
                boolean skip = false;
                for (int[] r : excluded) {
                    if (g1s < r[1] && g1e > r[0]) { skip = true; break; }
                }
                if (skip) continue;
                // Check if this type is a known interface or enum using reflection
                String typeName = m.group(1);
                String fullClassName = importedClasses.get(typeName);
                if (fullClassName == null && JAVA_LANG_CLASSES.contains(typeName)) {
                    fullClassName = "java.lang." + typeName;
                }
                TokenType tokenType = getClassTokenType(fullClassName, TokenType.TYPE_DECL);
                marks.add(new Mark(g1s, g1e, tokenType));

                if (m.group(2) != null) {
                    int start = lineStart + m.start(2);
                    int end = lineStart + m.end(2);
                    // Color < and > as default
                    marks.add(new Mark(start, start + 1, TokenType.DEFAULT)); // <
                    marks.add(new Mark(end - 1, end, TokenType.DEFAULT)); // >

                    // Parse and highlight all types in generic content (handles nested generics)
                    if (m.group(3) != null) {
                        String genericContent = m.group(3);
                        int contentStart = lineStart + m.start(3);
                        highlightGenericTypes(genericContent, contentStart, marks, excluded);
                    }
                }
            }
        }
    }

    private void collectPatternMatches(List<Mark> marks, Pattern pattern, TokenType type, int group) {
        Matcher m = pattern.matcher(text);
        while (m.find()) {
            marks.add(new Mark(m.start(group), m.end(group), type));
        }
    }

    private void collectPatternMatches(List<Mark> marks, Pattern pattern, TokenType type) {
        collectPatternMatches(marks, pattern, type, 0);
    }

    private List<Mark> resolveConflicts(List<Mark> marks) {
        // Sort by start index first, then by descending priority so higher-priority marks
        // at the same start are considered first.
        marks.sort((a, b) -> {
            if (a.start != b.start)
                return Integer.compare(a.start, b.start);
            return Integer.compare(b.type.priority, a.type.priority);
        });

        List<Mark> result = new ArrayList<>();
        for (Mark m : marks) {
            boolean skip = false;
            // Remove any existing lower-priority marks that overlap with this higher-priority mark
            for (int i = result.size() - 1; i >= 0; i--) {
                Mark r = result.get(i);
                if (m.start < r.end && m.end > r.start) {
                    if (r.type.priority < m.type.priority) {
                        // remove lower-priority overlapping mark
                        result.remove(i);
                    } else {
                        // an existing mark has equal or higher priority and overlaps -> skip adding m
                        skip = true;
                        break;
                    }
                }
            }
            if (!skip) {
                result.add(m);
            }
        }
        // Optionally sort the result by start before returning
        result.sort((a, b) -> Integer.compare(a.start, b.start));
        return result;
    }

    public static class LineData {
        public String text;
        public int start, end;
        public List<Token> tokens = new ArrayList<>();
        public List<Integer> indentCols = new ArrayList<>();

        public LineData(String text, int startIndex, int end) {
            this.text = text;
            this.start = startIndex;
            this.end = end;
        }

        public void drawString(int x, int y, int color) {
            StringBuilder builder = new StringBuilder();
            int lastIndex = 0;

            for (Token t : tokens) {
                int tokenStart = t.start - this.start; // relative position in line
                if (tokenStart > lastIndex) {
                    // append the text before the token (spaces, punctuation)
                    builder.append(text, lastIndex, tokenStart);
                }
                builder.append(colorChar).append(t.type.color).append(t.text).append(colorChar).append('f');
                lastIndex = tokenStart + t.text.length();
            }

            // append any remaining text after the last token
            if (lastIndex < text.length()) {
                builder.append(text.substring(lastIndex));
            }

            ClientProxy.Font.drawString(builder.toString(), x, y, color);
        }
    }

    public static class Token {
        public String text;
        public TokenType type;
        public int start, end; // start/end relative to the line

        public Token(String text, TokenType type, int start, int end) {
            this.text = text;
            this.type = type;
            this.start = start;
            this.end = end;
        }

        @Override
        public String toString() {
            return "Token{'" + text + '\'' + ", " + type + ", (color=" + type.color + ", priority=" + type.priority + ")" + '}';
        }
    }

    private static class Mark {
        public int start, end;
        public TokenType type;

        public Mark(int start, int end, TokenType type) {
            this.start = start;
            this.end = end;
            this.type = type;
        }

        @Override
        public String toString() {
            return "Mark{" + type + ", (start=" + start + ", end=" + end + ")" + '}';
        }
    }

    public enum TokenType {
        COMMENT('7', 130),
        STRING('5', 120),
        CLASS_KEYWORD('c', 115),
        IMPORT_KEYWORD('6', 110),
        KEYWORD('c', 100),
        MODIFIER('6', 90),
        NEW_TYPE('d', 80),
        IMPORTED_CLASS('3', 75), //imported classes used in code
        INTERFACE_DECL('b', 85),
        ENUM_DECL('d', 85),
        CLASS_DECL('3', 85),
        TYPE_DECL('3', 70),
        METHOD_DECARE('2', 60),
        METHOD_CALL('a', 50),
        NUMBER('7', 40),
        VARIABLE('f', 30),
        GLOBAL_FIELD('b', 35), // aqua - class-level fields
        LOCAL_FIELD('e', 25),  // yellow - local variables
        PARAMETER('9', 36),    // blue - method parameters (Bug 10)
        UNDEFINED_VAR('4', 105), // dark red - undefined variables (higher priority so it isn't overridden)
        DEFAULT('f', 0);

        public final char color;
        public final int priority;

        TokenType(char color, int priority) {
            this.color = color;
            this.priority = priority;
        }
    }

    // Compute indent guide columns per line based on brace matching.
    private void computeIndentGuides(List<Mark> marks) {
        // Clear existing guides
        for (LineData ld : lines) {
            ld.indentCols.clear();
        }

        // Build ignored ranges from STRING and COMMENT tokens
        List<int[]> ignored = new ArrayList<>();
        for (Mark m : marks) {
            if (m.type == TokenType.STRING || m.type == TokenType.COMMENT) {
                ignored.add(new int[]{m.start, m.end});
            }
        }

        java.util.function.Predicate<Integer> isIgnored = (pos) -> {
            for (int[] r : ignored) {
                if (pos >= r[0] && pos < r[1])
                    return true;
            }
            return false;
        };

        class OpenBrace {
            int line, col;

            OpenBrace(int l, int c) {
                line = l;
                col = c;
            }
        }
        java.util.Deque<OpenBrace> stack = new java.util.ArrayDeque<>();

        final int tabSize = 4;

        for (int li = 0; li < lines.size(); li++) {
            LineData ld = lines.get(li);
            String s = ld.text;
            for (int i = 0; i < s.length(); i++) {
                int absPos = ld.start + i;
                if (isIgnored.test(absPos))
                    continue;
                char c = s.charAt(i);
                if (c == '{') {
                    // compute leading indentation (expanded) up to this char
                    int leading = 0;
                    for (int k = 0; k < i; k++) {
                        char ch = s.charAt(k);
                        if (ch == '\t')
                            leading += tabSize;
                        else
                            leading += 1;
                    }
                    stack.push(new OpenBrace(li, leading));
                } else if (c == '}') {
                    if (!stack.isEmpty()) {
                        OpenBrace open = stack.pop();
                        int startLine = open.line;
                        int col = open.col;
                        if (startLine == li)
                            continue; // same-line block, skip
                        // add guide column to lines inside the block
                        int from = Math.max(0, startLine + 1);
                        int to = Math.min(lines.size() - 1, li);
                        for (int l = from; l <= to; l++) {
                            List<Integer> list = lines.get(l).indentCols;
                            if (!list.contains(col))
                                list.add(col);
                        }
                    }
                }
            }
        }
    }
}
