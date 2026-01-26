package noppes.npcs.client.gui.util.script.interpreter.js_parser;

import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocInfo;
import noppes.npcs.client.gui.util.script.interpreter.jsdoc.JSDocTag;

import java.io.*;
import java.util.*;
import java.util.regex.*;
import java.util.zip.*;

/**
 * Parses TypeScript definition files (.d.ts) to extract type information.
 * Can read from individual files, directories, or .vsix archives.
 */
public class TypeScriptDefinitionParser {
    
    // Patterns for parsing .d.ts content
    // Updated to capture generic type parameters like: export interface IEntity<T extends Entity /* net.minecraft.entity.Entity */>
    private static final Pattern INTERFACE_PATTERN = Pattern.compile(
        "export\\s+interface\\s+(\\w+)(?:<([^>]*)>)?(?:\\s+extends\\s+([^{]+?))?\\s*\\{");
    
    // Pattern for nested interfaces without export keyword
    private static final Pattern NESTED_INTERFACE_PATTERN = Pattern.compile(
        "(?<!export\\s)\\binterface\\s+(\\w+)(?:<([^>]*)>)?(?:\\s+extends\\s+([^{]+?))?\\s*\\{");
    
    // Similar pattern for classes
    private static final Pattern CLASS_PATTERN = Pattern.compile(
        "export\\s+class\\s+(\\w+)(?:<([^>]*)>)?(?:\\s+extends\\s+([^{]+?))?\\s*\\{");
    
    // Pattern to parse individual type parameters like: T extends EntityPlayerMP /* net.minecraft.entity.player.EntityPlayerMP */
    private static final Pattern TYPE_PARAM_PATTERN = Pattern.compile(
        "(\\w+)(?:\\s+extends\\s+(\\w+)(?:\\s*/\\*\\s*([\\w.]+)\\s*\\*/)?)?");
    
    // Match both "export namespace" and plain "namespace" (for declare global blocks)
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile(
        "(?:export\\s+)?namespace\\s+(\\w+)\\s*\\{");
    
    // Make semicolon optional for type aliases (TypeScript doesn't require them)
    private static final Pattern TYPE_ALIAS_PATTERN = Pattern.compile(
        "export\\s+type\\s+(\\w+)\\s*=\\s*([^;\\n]+);?");
    
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "^\\s*(\\w+)\\s*\\(([^)]*)\\)\\s*:\\s*([^;]+);", Pattern.MULTILINE);
    
    private static final Pattern FIELD_PATTERN = Pattern.compile(
        "^\\s*(readonly\\s+)?(\\w+)\\s*:\\s*([^;]+);", Pattern.MULTILINE);
    
    private static final Pattern GLOBAL_FUNCTION_PATTERN = Pattern.compile(
        "function\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*:\\s*([^;]+);");

    private static final Pattern GLOBAL_TYPE_ALIAS_PATTERN = Pattern.compile(
        "type\\s+(\\w+)\\s*=\\s*import\\(['\"]([^'\"]+)['\"]\\)\\.([\\w.]+);");

    // Pattern for context-namespaced hooks like: declare namespace INpcEvent { ... }
    // Matches any "declare namespace <Name> {" where Name starts with I and contains Event,
    // or any other namespace pattern used for hooks
    private static final Pattern HOOKS_NAMESPACE_PATTERN = Pattern.compile(
        "declare\\s+namespace\\s+(I\\w*Event|\\w+)\\s*\\{", Pattern.MULTILINE);
    
    private final JSTypeRegistry registry;
    
    public TypeScriptDefinitionParser(JSTypeRegistry registry) {
        this.registry = registry;
    }
    
    /**
     * Parse all .d.ts files from a VSIX archive.
     */
    public void parseVsixArchive(File vsixFile) throws IOException {
        try (ZipFile zip = new ZipFile(vsixFile)) {
            Enumeration<? extends ZipEntry> entries = zip.entries();
            while (entries.hasMoreElements()) {
                ZipEntry entry = entries.nextElement();
                if (entry.getName().endsWith(".d.ts")) {
                    try (InputStream is = zip.getInputStream(entry);
                         BufferedReader reader = new BufferedReader(new InputStreamReader(is))) {
                        String content = readFully(reader);
                        String fileName = entry.getName();
                        parseDefinitionFile(content, fileName);
                    }
                }
            }
        }
    }
    
    /**
     * Parse all .d.ts files from a directory recursively.
     */
    public void parseDirectory(File directory) throws IOException {
        if (!directory.isDirectory()) {
            throw new IllegalArgumentException("Not a directory: " + directory);
        }
        parseDirectoryRecursive(directory);
    }
    
    private void parseDirectoryRecursive(File dir) throws IOException {
        File[] files = dir.listFiles();
        if (files == null) return;
        
        for (File file : files) {
            if (file.isDirectory()) {
                parseDirectoryRecursive(file);
            } else if (file.getName().endsWith(".d.ts")) {
                parseFile(file);
            }
        }
    }
    
    /**
     * Parse a single .d.ts file.
     */
    public void parseFile(File file) throws IOException {
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            String content = readFully(reader);
            parseDefinitionFile(content, file.getName());
        }
    }
    
    /**
     * Parse a .d.ts file content.
     */
    public void parseDefinitionFile(String content, String fileName) {
        // Special handling for hooks.d.ts - extract function signatures
        if (fileName.contains("hooks.d.ts")) {
            parseHooksFile(content);
            return;
        }
        
        // Special handling for index.d.ts - extract global type aliases
        if (fileName.contains("index.d.ts")) {
            parseIndexFile(content);
            return;
        }

        // Parse regular interface files
        String packageName = derivePackageName(fileName);
        parseInterfaceFile(content, null, packageName);
    }
    
    /**
     * Parse hooks.d.ts to extract function signatures for JS hooks.
     *
     * Hooks are organized by their parent event interface, with the interface name
     * used directly as the namespace. Any mod can register its own event interfaces
     * and they will be automatically parsed:
     *
     *   declare namespace INpcEvent {
     *       function interact(event: INpcEvent.InteractEvent): void;
     *       function init(event: INpcEvent.InitEvent): void;
     *   }
     *
     *   declare namespace IPlayerEvent {
     *       function interact(event: IPlayerEvent.InteractEvent): void;
     *   }
     *
     *   declare namespace IDBCEvent {
     *       function customHook(event: IDBCEvent.CustomEvent): void;
     *   }
     *
     * The namespace name is stored as a string, allowing dynamic registration
     * without requiring enum modifications.
     */
    private void parseHooksFile(String content) {
        // Parse namespaced hooks (e.g., declare namespace INpcEvent { ... })
        // The namespace name is used directly - any event interface can register hooks
        Matcher namespaceMatcher = HOOKS_NAMESPACE_PATTERN.matcher(content);
        while (namespaceMatcher.find()) {
            String namespace = namespaceMatcher.group(1);  // e.g., "INpcEvent", "IPlayerEvent", "IDBCEvent"

            // Find the body of this namespace
            int bodyStart = namespaceMatcher.end();
            int bodyEnd = findMatchingBrace(content, bodyStart - 1);
            if (bodyEnd > bodyStart) {
                String namespaceBody = content.substring(bodyStart, bodyEnd);

                // Parse functions within this namespace
                Matcher funcMatcher = GLOBAL_FUNCTION_PATTERN.matcher(namespaceBody);
                while (funcMatcher.find()) {
                    String funcName = funcMatcher.group(1);
                    String params = funcMatcher.group(2);

                    // Parse parameter - format is "paramName: TypeName"
                    if (!params.isEmpty()) {
                        String[] parts = params.split(":\\s*", 2);
                        if (parts.length == 2) {
                            String paramName = parts[0].trim();
                            String paramType = parts[1].trim();
                            // Register hook with the namespace string directly
                            registry.registerHook(namespace, funcName, paramName, paramType);
                        }
                    }
                }
            }
        }
    }
    
    /**
     * Parse index.d.ts to extract global type aliases.
     */
    private void parseIndexFile(String content) {
        Matcher m = GLOBAL_TYPE_ALIAS_PATTERN.matcher(content);
        while (m.find()) {
            String aliasName = m.group(1);
            String importPath = m.group(2);
            String typeName = m.group(3);
            registry.registerTypeAlias(aliasName, typeName);
        }
    }
    
    /**
     * Parse interface and class definitions from content.
     */
    private void parseInterfaceFile(String content, String parentNamespace, String packageName) {
        // IMPORTANT:
        // Many generated .d.ts files declare nested exported interfaces inside `namespace X { ... }` blocks.
        // If we scan `content` directly for `export interface` / `export class`, we'll accidentally treat
        // those nested exports as top-level for the current `parentNamespace`.
        // That pollutes the registry with incorrectly-scoped type names (e.g. "DamagedEvent" instead of
        // "INpcEvent.DamagedEvent"), and later registry merges (by @javaFqn) can prevent the correctly
        // namespaced key from ever being registered.
        //
        // To avoid this, we strip namespace blocks out of the scan input and then parse namespaces
        // explicitly via the NAMESPACE_PATTERN recursion further below.
        String scanContent = stripNamespaceBlocks(content);

        // Find exported interfaces
        Matcher interfaceMatcher = INTERFACE_PATTERN.matcher(scanContent);
        while (interfaceMatcher.find()) {
            String interfaceName = interfaceMatcher.group(1);
            String typeParamsStr = interfaceMatcher.group(2);  // e.g., "T extends EntityPlayerMP /* net.minecraft.entity.player.EntityPlayerMP */"
            String extendsClause = interfaceMatcher.group(3);
            
            JSTypeInfo typeInfo = new JSTypeInfo(interfaceName, parentNamespace);
            
            JSDocInfo jsDoc = extractJSDocBefore(content, interfaceMatcher.start());
            if (jsDoc != null) {
                typeInfo.setJsDocInfo(jsDoc);
            }

            String javaFqn = findJavaFqn(jsDoc, packageName, typeInfo.getFullName());
            if (javaFqn != null && !javaFqn.isEmpty()) {
                typeInfo.setJavaFqn(javaFqn);
            }
            
            // Parse type parameters
            if (typeParamsStr != null && !typeParamsStr.isEmpty()) {
                parseTypeParameters(typeParamsStr, typeInfo);
            }
            
            if (extendsClause != null) {
                // Handle multiple extends (e.g., "IEntityLivingBase<T>, IAnimatable")
                // Take the first one, stripping generics
                String extendsType = extendsClause.trim();
                // Remove generic parameters like <T>
                extendsType = extendsType.replaceAll("<[^>]*>", "");
                // If multiple types (comma-separated), take the first one
                if (extendsType.contains(",")) {
                    extendsType = extendsType.substring(0, extendsType.indexOf(',')).trim();
                }
                // Clean up import() syntax if present
                extendsType = cleanType(extendsType);
                typeInfo.setExtends(extendsType);
            }
            
            // Find the body of this interface
            int bodyStart = interfaceMatcher.end();
            int bodyEnd = findMatchingBrace(content, bodyStart - 1);
            if (bodyEnd > bodyStart) {
                String body = content.substring(bodyStart, bodyEnd);
                parseInterfaceBody(body, typeInfo);
                
                // Parse nested interfaces and type aliases within this interface body
                String fullNamespace = parentNamespace != null ? 
                    parentNamespace + "." + interfaceName : interfaceName;
                parseNestedTypes(body, fullNamespace, packageName);
            }
            
            registry.registerType(typeInfo);
        }
        
        // Find nested interfaces (without export keyword)
        Matcher nestedInterfaceMatcher = NESTED_INTERFACE_PATTERN.matcher(scanContent);
        while (nestedInterfaceMatcher.find()) {
            String interfaceName = nestedInterfaceMatcher.group(1);
            String typeParamsStr = nestedInterfaceMatcher.group(2);
            String extendsClause = nestedInterfaceMatcher.group(3);
            
            JSTypeInfo typeInfo = new JSTypeInfo(interfaceName, parentNamespace);
            
            JSDocInfo jsDoc = extractJSDocBefore(content, nestedInterfaceMatcher.start());
            if (jsDoc != null) {
                typeInfo.setJsDocInfo(jsDoc);
            }
            
            // Parse type parameters
            if (typeParamsStr != null && !typeParamsStr.isEmpty()) {
                parseTypeParameters(typeParamsStr, typeInfo);
            }
            
            if (extendsClause != null) {
                String extendsType = extendsClause.trim();
                extendsType = extendsType.replaceAll("<[^>]*>", "");
                if (extendsType.contains(",")) {
                    extendsType = extendsType.substring(0, extendsType.indexOf(',')).trim();
                }
                extendsType = cleanType(extendsType);
                typeInfo.setExtends(extendsType);
            }
            
            // Find the body of this nested interface
            int bodyStart = nestedInterfaceMatcher.end();
            int bodyEnd = findMatchingBrace(content, bodyStart - 1);
            if (bodyEnd > bodyStart) {
                String body = content.substring(bodyStart, bodyEnd);
                parseInterfaceBody(body, typeInfo);
            }
            
            registry.registerType(typeInfo);
        }
        
        // Find classes (same logic as interfaces)
        Matcher classMatcher = CLASS_PATTERN.matcher(scanContent);
        while (classMatcher.find()) {
            String className = classMatcher.group(1);
            String typeParamsStr = classMatcher.group(2);
            String extendsClause = classMatcher.group(3);
            
            JSTypeInfo typeInfo = new JSTypeInfo(className, parentNamespace);
            
            JSDocInfo jsDoc = extractJSDocBefore(content, classMatcher.start());
            if (jsDoc != null) {
                typeInfo.setJsDocInfo(jsDoc);
            }
            
            // Parse type parameters
            if (typeParamsStr != null && !typeParamsStr.isEmpty()) {
                parseTypeParameters(typeParamsStr, typeInfo);
            }
            
            if (extendsClause != null) {
                String extendsType = extendsClause.trim();
                extendsType = extendsType.replaceAll("<[^>]*>", "");
                if (extendsType.contains(",")) {
                    extendsType = extendsType.substring(0, extendsType.indexOf(',')).trim();
                }
                extendsType = cleanType(extendsType);
                typeInfo.setExtends(extendsType);
            }
            
            // Find the body of this class
            int bodyStart = classMatcher.end();
            int bodyEnd = findMatchingBrace(content, bodyStart - 1);
            if (bodyEnd > bodyStart) {
                String body = content.substring(bodyStart, bodyEnd);
                parseInterfaceBody(body, typeInfo);
            }
            
            registry.registerType(typeInfo);
        }
        
        // Find namespaces (which contain inner types)
        Matcher namespaceMatcher = NAMESPACE_PATTERN.matcher(content);
        while (namespaceMatcher.find()) {
            String namespaceName = namespaceMatcher.group(1);
            
            // Find the body of this namespace
            int bodyStart = namespaceMatcher.end();
            int bodyEnd = findMatchingBrace(content, bodyStart - 1);
            if (bodyEnd > bodyStart) {
                String body = content.substring(bodyStart, bodyEnd);
                
                // Parse inner types with namespace prefix
                // Namespaces contain exported types, so use parseInterfaceFile
                String fullNamespace = parentNamespace != null ? 
                    parentNamespace + "." + namespaceName : namespaceName;
                parseInterfaceFile(body, fullNamespace, packageName);
                // Don't call parseNestedTypes here - namespace members are all exported
                // and will be caught by parseInterfaceFile's INTERFACE_PATTERN
            }
        }
        
        // Handle top-level type aliases
        if (parentNamespace == null) {
            parseTypeAliases(content, null);
        }
    }

    /**
     * Strip namespace blocks from a file-level scan to avoid incorrectly capturing nested exports.
     *
     * This replaces the entire `namespace X { ... }` span (including braces) with whitespace so that
     * match indices remain stable when we later slice `content` for bodies.
     */
    private String stripNamespaceBlocks(String content) {
        if (content == null || content.isEmpty()) {
            return content;
        }

        Matcher m = NAMESPACE_PATTERN.matcher(content);
        if (!m.find()) {
            return content;
        }

        char[] chars = content.toCharArray();

        int searchFrom = 0;
        m.reset();
        while (m.find(searchFrom)) {
            int start = m.start();
            int bodyStart = m.end();
            int bodyEnd = findMatchingBrace(content, bodyStart - 1);
            if (bodyEnd <= start) {
                searchFrom = Math.max(m.end(), searchFrom + 1);
                continue;
            }

            int endExclusive = Math.min(chars.length, bodyEnd + 1);
            for (int i = start; i < endExclusive; i++) {
                chars[i] = ' ';
            }

            searchFrom = endExclusive;
        }

        return new String(chars);
    }
    
    /**
     * Parse type parameters from a string like "T extends EntityPlayerMP /* net.minecraft.entity.player.EntityPlayerMP *`/".
     * Handles multiple parameters separated by commas.
     * Stores strings only - resolution happens in Phase 2 after all types are loaded.
     */
    private void parseTypeParameters(String typeParamsStr, JSTypeInfo typeInfo) {
        // Split by comma, but be careful with nested generics (shouldn't happen at this level, but be safe)
        String[] params = typeParamsStr.split(",");
        for (String param : params) {
            param = param.trim();
            if (param.isEmpty()) continue;
            
            Matcher m = TYPE_PARAM_PATTERN.matcher(param);
            if (m.find()) {
                String name = m.group(1);
                String boundType = m.group(2);          // Simple name like "EntityPlayerMP"
                String fullBoundType = m.group(3);      // Full name like "net.minecraft.entity.player.EntityPlayerMP"
                
                // Store strings only - resolution happens in Phase 2
                typeInfo.addTypeParam(new TypeParamInfo(name, boundType, fullBoundType));
            }
        }
    }
    
    /**
     * Parse nested types (interfaces and type aliases) within a parent type or namespace.
     */
    private void parseNestedTypes(String content, String namespace, String packageName) {
        // Parse nested interfaces
        Matcher nestedInterfaceMatcher = NESTED_INTERFACE_PATTERN.matcher(content);
        while (nestedInterfaceMatcher.find()) {
            String interfaceName = nestedInterfaceMatcher.group(1);
            String typeParamsStr = nestedInterfaceMatcher.group(2);
            String extendsClause = nestedInterfaceMatcher.group(3);
            
            JSTypeInfo typeInfo = new JSTypeInfo(interfaceName, namespace);

            JSDocInfo jsDoc = extractJSDocBefore(content, nestedInterfaceMatcher.start());
            if (jsDoc != null) {
                typeInfo.setJsDocInfo(jsDoc);
            }

            String javaFqn = findJavaFqn(jsDoc, packageName, typeInfo.getFullName());
            if (javaFqn != null && !javaFqn.isEmpty()) {
                typeInfo.setJavaFqn(javaFqn);
            }
            
            // Parse type parameters
            if (typeParamsStr != null && !typeParamsStr.isEmpty()) {
                parseTypeParameters(typeParamsStr, typeInfo);
            }
            
            if (extendsClause != null) {
                String extendsType = extendsClause.trim();
                extendsType = extendsType.replaceAll("<[^>]*>", "");
                if (extendsType.contains(",")) {
                    extendsType = extendsType.substring(0, extendsType.indexOf(',')).trim();
                }
                extendsType = cleanType(extendsType);
                typeInfo.setExtends(extendsType);
            }
            
            // Find the body of this nested interface
            int bodyStart = nestedInterfaceMatcher.end();
            int bodyEnd = findMatchingBrace(content, bodyStart - 1);
            if (bodyEnd > bodyStart) {
                String body = content.substring(bodyStart, bodyEnd);
                parseInterfaceBody(body, typeInfo);
            }
            
            registry.registerType(typeInfo);
        }
        
        // Parse type aliases within this context
        parseTypeAliases(content, namespace);
    }

    private String findJavaFqn(JSDocInfo jsDoc, String packageName, String typeFullName) {
        String tagged = extractJavaFqnFromJSDoc(jsDoc);
        if (tagged != null && !tagged.isEmpty()) {
            return tagged;
        }
        return buildJavaFqnFromPackage(packageName, typeFullName);
    }

    private String extractJavaFqnFromJSDoc(JSDocInfo jsDoc) {
        if (jsDoc == null) return null;
        for (JSDocTag tag : jsDoc.getAllTags()) {
            if (tag == null) continue;
            if ("javaFqn".equals(tag.getTagName())) {
                if (tag.getDescription() != null && !tag.getDescription().trim().isEmpty()) {
                    return tag.getDescription().trim();
                }
                if (tag.getTypeName() != null && !tag.getTypeName().trim().isEmpty()) {
                    return tag.getTypeName().trim();
                }
            }
        }
        return null;
    }

    private String buildJavaFqnFromPackage(String packageName, String typeFullName) {
        if (typeFullName == null || typeFullName.isEmpty()) return null;
        if (packageName == null || packageName.isEmpty()) return null;
        return packageName + "." + typeFullName;
    }

    private String derivePackageName(String fileName) {
        if (fileName == null || fileName.isEmpty()) return null;
        String normalized = fileName.replace('\\', '/');
        if (normalized.endsWith(".d.ts")) {
            normalized = normalized.substring(0, normalized.length() - 5);
        }
        int lastSlash = normalized.lastIndexOf('/');
        if (lastSlash < 0) return null;
        String pkgPath = normalized.substring(0, lastSlash);
        if (pkgPath.isEmpty()) return null;
        return pkgPath.replace('/', '.');
    }
    
    /**
     * Parse type aliases (export type X = Y).
     */
    private void parseTypeAliases(String content, String namespace) {
        Matcher m = TYPE_ALIAS_PATTERN.matcher(content);
        while (m.find()) {
            String aliasName = m.group(1);
            String targetType = m.group(2).trim();
            
            // Create a simple type that extends the target
            JSTypeInfo typeInfo = new JSTypeInfo(aliasName, namespace);
            typeInfo.setExtends(targetType);
            registry.registerType(typeInfo);
        }
    }
    
    /**
     * Parse the body of an interface to extract methods and fields.
     */
    private void parseInterfaceBody(String body, JSTypeInfo typeInfo) {
        // Parse methods
        Matcher methodMatcher = METHOD_PATTERN.matcher(body);
        while (methodMatcher.find()) {
            String methodName = methodMatcher.group(1);
            String params = methodMatcher.group(2);
            String returnType = cleanType(methodMatcher.group(3));
            
            List<JSMethodInfo.JSParameterInfo> parameters = parseParameters(params);
            JSMethodInfo method = new JSMethodInfo(methodName, returnType, parameters);
            
            JSDocInfo jsDoc = extractJSDocBefore(body, methodMatcher.start());
            if (jsDoc != null) {
                method.setJsDocInfo(jsDoc);
            }
            
            typeInfo.addMethod(method);
        }
        
        // Parse fields (that aren't method signatures)
        Matcher fieldMatcher = FIELD_PATTERN.matcher(body);
        while (fieldMatcher.find()) {
            String fieldText = fieldMatcher.group(0);
            // Skip if this looks like a method (has parentheses in the match)
            if (fieldText.contains("(")) continue;
            
            boolean readonly = fieldMatcher.group(1) != null;
            String fieldName = fieldMatcher.group(2);
            String fieldType = cleanType(fieldMatcher.group(3));
            
            JSFieldInfo field = new JSFieldInfo(fieldName, fieldType, readonly);
            
            JSDocInfo jsDoc = extractJSDocBefore(body, fieldMatcher.start());
            if (jsDoc != null) {
                field.setJsDocInfo(jsDoc);
            }
            
            typeInfo.addField(field);
        }
    }
    
    /**
     * Parse parameters string into list of parameter info.
     */
    private List<JSMethodInfo.JSParameterInfo> parseParameters(String params) {
        List<JSMethodInfo.JSParameterInfo> result = new ArrayList<>();
        if (params == null || params.trim().isEmpty()) {
            return result;
        }
        
        // Split by comma, but be careful of nested types like Map<K, V>
        List<String> paramParts = splitParameters(params);
        
        for (String part : paramParts) {
            part = part.trim();
            if (part.isEmpty()) continue;
            
            // Format: name: type or name?: type
            int colonIndex = part.indexOf(':');
            if (colonIndex > 0) {
                String name = part.substring(0, colonIndex).trim().replace("?", "");
                String type = cleanType(part.substring(colonIndex + 1).trim());
                result.add(new JSMethodInfo.JSParameterInfo(name, type));
            }
        }
        
        return result;
    }
    
    /**
     * Split parameters respecting nested angle brackets.
     */
    private List<String> splitParameters(String params) {
        List<String> result = new ArrayList<>();
        int depth = 0;
        int start = 0;
        
        for (int i = 0; i < params.length(); i++) {
            char c = params.charAt(i);
            if (c == '<' || c == '(' || c == '[') {
                depth++;
            } else if (c == '>' || c == ')' || c == ']') {
                depth--;
            } else if (c == ',' && depth == 0) {
                result.add(params.substring(start, i));
                start = i + 1;
            }
        }
        
        if (start < params.length()) {
            result.add(params.substring(start));
        }
        
        return result;
    }
    
    /**
     * Clean up a type string (remove import() syntax, trim, etc.).
     */
    private String cleanType(String type) {
        if (type == null) return "any";
        type = type.trim();
        
        // Handle import('./path').TypeName syntax
        if (type.startsWith("import(")) {
            // Extract the type name after the import
            int dotIndex = type.lastIndexOf(").");
            if (dotIndex > 0) {
                type = type.substring(dotIndex + 2);
            }
        }
        
        // Handle arrays
        type = type.replace("[]", "[]");
        
        return type;
    }
    
    /**
     * Find the matching closing brace.
     */
    private int findMatchingBrace(String text, int openBracePos) {
        int depth = 1;
        for (int i = openBracePos + 1; i < text.length(); i++) {
            char c = text.charAt(i);
            if (c == '{') depth++;
            else if (c == '}') {
                depth--;
                if (depth == 0) return i;
            }
        }
        return -1;
    }
    
    /**
     * Read entire content from reader.
     */
    private String readFully(BufferedReader reader) throws IOException {
        StringBuilder sb = new StringBuilder();
        String line;
        while ((line = reader.readLine()) != null) {
            sb.append(line).append("\n");
        }
        return sb.toString();
    }
    
    private JSDocInfo extractJSDocBefore(String content, int elementStart) {
        String jsDocBlock = DTSJSDocParser.extractJSDocBefore(content, elementStart);
        if (jsDocBlock != null) {
            return DTSJSDocParser.parseJSDocBlock(jsDocBlock);
        }
        return null;
    }
}
