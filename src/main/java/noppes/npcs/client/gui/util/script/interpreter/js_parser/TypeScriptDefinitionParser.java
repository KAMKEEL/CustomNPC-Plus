package noppes.npcs.client.gui.util.script.interpreter.js_parser;

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
    private static final Pattern INTERFACE_PATTERN = Pattern.compile(
        "export\\s+interface\\s+(\\w+)(?:\\s+extends\\s+([^{]+?))?\\s*\\{");
    
    private static final Pattern NAMESPACE_PATTERN = Pattern.compile(
        "export\\s+namespace\\s+(\\w+)\\s*\\{");
    
    private static final Pattern TYPE_ALIAS_PATTERN = Pattern.compile(
        "export\\s+type\\s+(\\w+)\\s*=\\s*([^;]+);");
    
    private static final Pattern METHOD_PATTERN = Pattern.compile(
        "^\\s*(\\w+)\\s*\\(([^)]*)\\)\\s*:\\s*([^;]+);", Pattern.MULTILINE);
    
    private static final Pattern FIELD_PATTERN = Pattern.compile(
        "^\\s*(readonly\\s+)?(\\w+)\\s*:\\s*([^;]+);", Pattern.MULTILINE);
    
    private static final Pattern GLOBAL_FUNCTION_PATTERN = Pattern.compile(
        "function\\s+(\\w+)\\s*\\(([^)]*)\\)\\s*:\\s*([^;]+);");
    
    private static final Pattern GLOBAL_TYPE_ALIAS_PATTERN = Pattern.compile(
        "type\\s+(\\w+)\\s*=\\s*import\\(['\"]([^'\"]+)['\"]\\)\\.([\\w.]+);");
    
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
        parseInterfaceFile(content, null);
    }
    
    /**
     * Parse hooks.d.ts to extract function signatures for JS hooks.
     */
    private void parseHooksFile(String content) {
        Matcher m = GLOBAL_FUNCTION_PATTERN.matcher(content);
        while (m.find()) {
            String funcName = m.group(1);
            String params = m.group(2);
            String returnType = m.group(3).trim();
            
            // Parse parameter - format is "paramName: TypeName"
            if (!params.isEmpty()) {
                String[] parts = params.split(":\\s*", 2);
                if (parts.length == 2) {
                    String paramName = parts[0].trim();
                    String paramType = parts[1].trim();
                    registry.registerHook(funcName, paramName, paramType);
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
     * Parse interface definitions from content.
     */
    private void parseInterfaceFile(String content, String parentNamespace) {
        // Find interfaces
        Matcher interfaceMatcher = INTERFACE_PATTERN.matcher(content);
        while (interfaceMatcher.find()) {
            String interfaceName = interfaceMatcher.group(1);
            String extendsClause = interfaceMatcher.group(2);
            
            JSTypeInfo typeInfo = new JSTypeInfo(interfaceName, parentNamespace);
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
                typeInfo.setExtends(extendsType);
            }
            
            // Find the body of this interface
            int bodyStart = interfaceMatcher.end();
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
                
                // Recursively parse inner types with namespace prefix
                String fullNamespace = parentNamespace != null ? 
                    parentNamespace + "." + namespaceName : namespaceName;
                parseInterfaceFile(body, fullNamespace);
                
                // Also handle type aliases within namespace
                parseTypeAliases(body, fullNamespace);
            }
        }
        
        // Handle top-level type aliases
        if (parentNamespace == null) {
            parseTypeAliases(content, null);
        }
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
}
