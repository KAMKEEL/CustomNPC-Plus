package dts

import groovy.transform.TypeChecked
import org.gradle.api.logging.Logger

import java.util.regex.Pattern
import java.util.regex.Matcher

/**
 * Parses Java source files and converts them to TypeScript definition (.d.ts) files.
 * Handles interfaces, classes, nested types, generics, and JavaDoc preservation.
 */
class JavaToTypeScriptConverter {
    
    // Common Java type mappings to TypeScript
    private static final Map<String, String> PRIMITIVE_MAPPINGS = [
        'void': 'void',
        'boolean': 'boolean',
        'byte': 'number',
        'short': 'number',
        'int': 'number',
        'long': 'number',
        'float': 'number',
        'double': 'number',
        'char': 'string',
        'String': 'string',
        'Object': 'any',
        'Boolean': 'boolean',
        'Byte': 'number',
        'Short': 'number',
        'Integer': 'number',
        'Long': 'number',
        'Float': 'number',
        'Double': 'number',
        'Character': 'string',
        'Number': 'number',
    ]
    
    // Java functional interface mappings
    private static final Map<String, String> FUNCTIONAL_MAPPINGS = [
        'Consumer': '(arg: %s) => void',
        'Supplier': '() => %s',
        'Function': '(arg: %s) => %s',
        'Predicate': '(arg: %s) => boolean',
        'BiConsumer': '(arg1: %s, arg2: %s) => void',
        'BiFunction': '(arg1: %s, arg2: %s) => %s',
        'Runnable': '() => void',
        'Callable': '() => %s',
    ]
    
    // Packages that are part of API (generate imports to local .d.ts)
    private Set<String> apiPackages = [] as Set
    
    // Base output directory for generated files
    private File outputDir
    
    // Track all generated types for index.d.ts
    private List<TypeInfo> generatedTypes = []
    
    // Track hooks for hooks.d.ts
    // Hooks are organized by their parent event type (e.g., INpcEvent, IPlayerEvent)
    // The namespace in hooks.d.ts matches the event type name directly
    private Map<String, List<HookInfo>> hooks = [:]

    private Logger logger;
    
    JavaToTypeScriptConverter(File outputDir, Set<String> apiPackages) {
        this.outputDir = outputDir
        this.apiPackages = apiPackages
    }
    
    /**
     * Process all Java files in the given directories
     */
    void processDirectories(List<File> sourceDirs, Logger logger) {
        this.logger = logger
        sourceDirs.each { dir ->
            if (dir.exists()) {
                processDirectory(dir, dir)
            }
        }
        
        // Generate index.d.ts
        generateIndexFile()
        
        // Generate hooks.d.ts
        generateHooksFile()
    }
    
    private void processDirectory(File dir, File baseDir) {
        dir.eachFileRecurse { file ->
            if (file.name.endsWith('.java') && !file.name.equals('package-info.java')) {
                // Early filtering - check if file matches any API package
                String relativePath = baseDir.toPath().relativize(file.toPath()).toString()
                String packagePath = relativePath.replace('\\', '/').replace('.java', '').replace('/', '.')
                
                // Only process if the package starts with one of the apiPackages
                boolean shouldProcess = apiPackages.any { apiPkg -> packagePath.startsWith(apiPkg) }
                
                if (shouldProcess) 
                    processJavaFile(file, baseDir)
            }
        }
    }

    

    /**
     * Process a single Java file
     */
    void processJavaFile(File javaFile, File baseDir) {
        String content = javaFile.text
        ParsedJavaFile parsed = parseJavaFile(content)
        
        if (parsed == null) {
            logger.warn("Failed to parse ${javaFile.name} - parseJavaFile returned null")
            return
        }
        if (parsed.types.isEmpty()) {
            logger.warn("No types extracted from ${javaFile.name} - package: ${parsed.packageName}")
            return
        }
        // logger.lifecycle("Successfully parsed ${javaFile.name}: ${parsed.types.size()} type(s) found: ${parsed.types*.name}")
        
        // Determine output path
        String relativePath = baseDir.toPath().relativize(javaFile.toPath()).toString()
        String dtsPath = relativePath.replace('.java', '.d.ts').replace('\\', '/')
        File outputFile = new File(outputDir, dtsPath)
        
        // Generate TypeScript content
        String tsContent = generateTypeScript(parsed, dtsPath)
        
        // Write file
        outputFile.parentFile.mkdirs()
        outputFile.text = tsContent
        
        // Track for index generation
        parsed.types.each { type ->
            generatedTypes << new TypeInfo(
                name: type.name,
                packageName: parsed.packageName,
                filePath: dtsPath,
                isClass: type.isClass,
                isInterface: type.isInterface,
                extendsType: type.extendsType
            )
            
            // Track nested types
            type.nestedTypes.each { nested ->
                generatedTypes << new TypeInfo(
                    name: "${type.name}.${nested.name}",
                    packageName: parsed.packageName,
                    filePath: dtsPath,
                    isClass: nested.isClass,
                    isInterface: nested.isInterface,
                    parentType: type.name
                )
            }
        }
        
        // Collect hooks from event interfaces
        collectHooks(parsed)
    }
    
    /**
     * Parse a Java file into structured data
     */
    ParsedJavaFile parseJavaFile(String content) {
        ParsedJavaFile result = new ParsedJavaFile()
        
        // Extract package
        def packageMatcher = content =~ /package\s+([\w.]+)\s*;/
        if (packageMatcher.find()) {
            result.packageName = packageMatcher.group(1)
        }
        
        // Extract imports
        def importMatcher = content =~ /import\s+([\w.*]+)\s*;/
        while (importMatcher.find()) {
            result.imports << importMatcher.group(1)
        }
        
        // Parse types (interfaces and classes)
        parseTypes(content, result)
        
        return result
    }
    
    private void parseTypes(String content, ParsedJavaFile result) {
        // Match ONLY top-level (public) interface or class declarations
        // Top-level types MUST have 'public' modifier in Java
        // Use a simpler pattern first, then manually extract type parameters
        // Handle modifiers like abstract, final, static (in any order)
        def typePattern = ~/(\/\*\*[\s\S]*?\*\/\s*)?public\s+(?:(?:abstract|final|static)\s+)*(interface|class)\s+(\w+)/
        
        def matcher = content =~ typePattern
        while (matcher.find()) {
            JavaType type = new JavaType()
            type.jsdoc = matcher.group(1)?.trim()
            type.isInterface = matcher.group(2) == 'interface'
            type.isClass = matcher.group(2) == 'class'
            type.name = matcher.group(3)
            
            // Manually extract type parameters with balanced bracket matching
            int afterName = matcher.end()
            String remainder = content.substring(afterName)
            
            // Check if there are type parameters
            if (remainder.trim().startsWith('<')) {
                int startIndex = remainder.indexOf('<')
                int depth = 0
                int endIndex = -1
                
                for (int i = startIndex; i < remainder.length(); i++) {
                    char c = remainder.charAt(i)
                    if (c == '<') {
                        depth++
                    } else if (c == '>') {
                        depth--
                        if (depth == 0) {
                            endIndex = i
                            break
                        }
                    }
                }
                
                if (endIndex > startIndex) {
                    type.typeParams = remainder.substring(startIndex + 1, endIndex).trim()
                    // Parse type parameters with full class names
                    type.parsedTypeParams = parseTypeParams(type.typeParams, result)
                    remainder = remainder.substring(endIndex + 1)
                }
            }
            
            // Now parse extends and implements
            def extendsPattern = ~/\s+extends\s+([\w.<>,\s]+?)(?:\s+implements|\s*\{)/
            def extendsMatcher = remainder =~ extendsPattern
            if (extendsMatcher.find()) {
                type.extendsType = extendsMatcher.group(1)?.trim()
            }
            
            def implementsPattern = ~/\s+implements\s+([\w.<>,\s]+?)\s*\{/
            def implementsMatcher = remainder =~ implementsPattern
            if (implementsMatcher.find()) {
                type.implementsTypes = implementsMatcher.group(1)?.split(',')?.collect { it.trim() } ?: []
            } else {
                type.implementsTypes = []
            }
            
            // Find the opening brace for the body
            int braceIndex = content.indexOf('{', afterName)
            if (braceIndex == -1) {
                continue // No body found, skip this type
            }
            
            // Find the body of this type
            int bodyStart = braceIndex
            int bodyEnd = findMatchingBrace(content, bodyStart)
            if (bodyEnd > bodyStart) {
                String body = content.substring(bodyStart + 1, bodyEnd)
                
                // Parse methods - pass original body for JSDoc extraction
                type.methods = parseMethods(body)
                
                // Parse nested types
                type.nestedTypes = parseNestedTypes(body, type.name)
                
                // Parse fields (for classes)
                if (type.isClass) {
                    type.fields = parseFields(body)
                }
            }
            
            result.types << type
        }
    }
    
    /**
     * Remove nested type bodies from a string so we only parse top-level methods
     */
    private String removeNestedTypeBodies(String body) {
        StringBuilder result = new StringBuilder()
        int depth = 0
        boolean inNestedType = false
        int nestedStart = -1
        
        // Find nested type declarations and remove their bodies
        def nestedPattern = ~/(?:public\s+)?(?:static\s+)?(interface|class)\s+\w+/
        
        int i = 0
        while (i < body.length()) {
            char c = body.charAt(i)
            
            if (c == '{') {
                if (!inNestedType) {
                    // Check if this brace starts a nested type
                    String before = body.substring(Math.max(0, i - 100), i)
                    if (before =~ /(?:public\s+)?(?:static\s+)?(?:interface|class)\s+\w+[^{]*$/) {
                        inNestedType = true
                        nestedStart = i
                        depth = 1
                        i++
                        continue
                    }
                }
                if (inNestedType) {
                    depth++
                }
            } else if (c == '}') {
                if (inNestedType) {
                    depth--
                    if (depth == 0) {
                        inNestedType = false
                        // Don't add the nested type body to result
                        i++
                        continue
                    }
                }
            }
            
            if (!inNestedType) {
                result.append(c)
            }
            i++
        }
        
        return result.toString()
    }
    
    private List<JavaMethod> parseMethods(String body) {
        List<JavaMethod> methods = []
        
        // Remove nested type bodies first to only get top-level methods
        String topLevelBody = removeNestedTypeBodies(body)
        
        // Match method signatures - handles complex generics
        // Anchored with (?m)^ to prevent matching inside // comment lines
        // Capture JSDoc in group 1, returnType in group 2, methodName in group 3, params in group 4
        def methodPattern = ~/(?m)^\s*(\/\*\*[\s\S]*?\*\/\s*)?(?:@\w+(?:\([^)]*\))?\s*)*(?:public\s+|protected\s+|private\s+)?(?:static\s+)?(?:abstract\s+)?(?:default\s+)?(?:synchronized\s+)?(?:final\s+)?(?:<[^>]+>\s+)?(\w[\w.<>,\[\]\s]*?)\s+(\w+)\s*\(([^)]*)\)\s*(?:throws\s+[\w,\s]+)?[;{]/
        
        def matcher = topLevelBody =~ methodPattern
        while (matcher.find()) {
            String jsdoc = matcher.group(1)?.trim()
            String returnType = matcher.group(2).trim()
            String methodName = matcher.group(3)
            
            // Skip constructors - where return type is a visibility modifier
            // or the method name matches the class name (which we'd need to track)
            if (['public', 'protected', 'private', 'abstract', 'static', 'final', 'synchronized', 'native', 'strictfp'].contains(returnType)) {
                continue
            }
            
            JavaMethod method = new JavaMethod()
            method.returnType = returnType
            method.name = methodName
            method.parameters = parseParameters(matcher.group(4))
            method.jsdoc = jsdoc
            
            methods << method
        }
        
        return methods
    }
    
    private List<JavaField> parseFields(String body) {
        List<JavaField> fields = []
        
        // Remove nested type bodies first
        String topLevelBody = removeNestedTypeBodies(body)
        
        // Capture JSDoc in group 1, visibility in group 2, fieldType in group 3, fieldName in group 4
        def fieldPattern = ~/(\/\*\*[\s\S]*?\*\/\s*)?(public\s+|protected\s+|private\s+)(?:static\s+)?(?:final\s+)?(\w[\w.<>,\[\]]*)\s+(\w+)\s*[;=]/
        
        def matcher = topLevelBody =~ fieldPattern
        while (matcher.find()) {
            String jsdoc = matcher.group(1)?.trim()
            String fieldType = matcher.group(3).trim()
            String fieldName = matcher.group(4)
            
            // Skip Java keywords that might be mismatched
            if (['return', 'if', 'else', 'for', 'while', 'switch', 'case', 'break', 'continue', 'throw', 'try', 'catch', 'finally', 'new', 'this', 'super'].contains(fieldType)) {
                continue
            }
            
            JavaField field = new JavaField()
            field.type = fieldType
            field.name = fieldName
            field.jsdoc = jsdoc
            
            fields << field
        }
        
        return fields
    }
    
    private List<JavaType> parseNestedTypes(String body, String parentName) {
        List<JavaType> nestedTypes = []
        
        // Capture JSDoc in group 1, interface/class in group 2, name in group 3, typeParams in group 4, extends in group 5
        def nestedPattern = ~/(\/\*\*[\s\S]*?\*\/\s*)?(?:@\w+(?:\([^)]*\))?\s*)*(?:public\s+)?(?:static\s+)?(interface|class)\s+(\w+)(?:<([^>]+)>)?(?:\s+extends\s+([\w.<>,\s]+))?\s*\{/
        
        // We need to track position and skip over bodies of found types to avoid finding nested-nested types
        int searchStart = 0
        def matcher = nestedPattern.matcher(body)
        
        while (matcher.find(searchStart)) {
            JavaType nested = new JavaType()
            nested.jsdoc = matcher.group(1)?.trim()
            nested.isInterface = matcher.group(2) == 'interface'
            nested.isClass = matcher.group(2) == 'class'
            nested.name = matcher.group(3)
            nested.typeParams = matcher.group(4)
            nested.extendsType = matcher.group(5)?.trim()
            
            int bodyStart = matcher.end() - 1
            int bodyEnd = findMatchingBrace(body, bodyStart)
            if (bodyEnd > bodyStart) {
                String nestedBody = body.substring(bodyStart + 1, bodyEnd)
                nested.methods = parseMethods(nestedBody)
                // Recursively parse nested types within this nested type
                nested.nestedTypes = parseNestedTypes(nestedBody, nested.name)
                
                // Skip past the entire body of this type for the next search
                searchStart = bodyEnd + 1
            } else {
                // If we couldn't find the matching brace, move past this match
                searchStart = matcher.end()
            }
            
            nestedTypes << nested
        }
        
        return nestedTypes
    }
    
    private List<JavaParameter> parseParameters(String paramsStr) {
        List<JavaParameter> params = []
        if (paramsStr == null || paramsStr.trim().isEmpty()) return params
        
        // Handle complex generic parameters
        List<String> paramParts = splitParameters(paramsStr)
        
        paramParts.each { part ->
            part = part.trim()
            if (part.isEmpty()) return
            
            // Handle varargs
            boolean isVarargs = part.contains('...')
            part = part.replace('...', '[]')
            
            // Split type and name
            int lastSpace = part.lastIndexOf(' ')
            if (lastSpace > 0 && lastSpace < part.length() - 1) {
                JavaParameter param = new JavaParameter()
                param.type = part.substring(0, lastSpace).trim()
                param.name = part.substring(lastSpace + 1).trim()
                param.isVarargs = isVarargs
                params << param
            } else if (lastSpace == -1 && !part.isEmpty()) {
                // No space found - might be a single token, skip it
                // This can happen with malformed or unusual parameter declarations
            }
        }
        
        return params
    }
    
    /**
     * Split parameters handling nested generics
     */
    private List<String> splitParameters(String params) {
        List<String> result = []
        int depth = 0
        StringBuilder current = new StringBuilder()
        
        params.each { ch ->
            if (ch == '<') depth++
            else if (ch == '>') depth--
            else if (ch == ',' && depth == 0) {
                result << current.toString()
                current = new StringBuilder()
                return
            }
            current.append(ch)
        }
        
        if (current.length() > 0) {
            result << current.toString()
        }
        
        return result
    }
    
    private String extractJsDocBefore(String content, int position) {
        // Look backwards for JSDoc
        String before = content.substring(0, position)
        def jsdocMatcher = before =~ /\/\*\*[\s\S]*?\*\/\s*$/
        if (jsdocMatcher.find()) {
            return jsdocMatcher.group(0).trim()
        }
        return null
    }
    
    private int findMatchingBrace(String content, int start) {
        int depth = 0
        for (int i = start; i < content.length(); i++) {
            char c = content.charAt(i)
            if (c == '{') depth++
            else if (c == '}') {
                depth--
                if (depth == 0) return i
            }
        }
        return -1
    }
    
    private String removeBlockComments(String content) {
        // Remove JSDoc and block comments for structure parsing
        return content.replaceAll(/\/\*[\s\S]*?\*\//, '')
    }
    
    /**
     * Generate TypeScript content from parsed Java
     */
    String generateTypeScript(ParsedJavaFile parsed, String currentPath) {
        StringBuilder sb = new StringBuilder()
        
        // Header comment
        sb.append('/**\n')
        sb.append(' * Generated from Java file for CustomNPC+ Minecraft Mod 1.7.10\n')
        sb.append(" * Package: ${parsed.packageName}\n")
        sb.append(' */\n\n')
        
        parsed.types.each { type ->
            generateType(sb, type, parsed, currentPath, '')
        }
        
        return sb.toString()
    }
    
    private void generateType(StringBuilder sb, JavaType type, ParsedJavaFile parsed, String currentPath, String indent) {
        // Build a set of type parameter names for this type (e.g., "T", "U", etc.)
        Set<String> typeParamNames = type.parsedTypeParams?.collect { it.name }?.toSet() ?: [] as Set
        
        // JSDoc
        String javaFqn = buildJavaFqn(parsed.packageName, type.name)
        String typeJsDoc = ensureJavaFqnTag(type.jsdoc, javaFqn, indent)
        if (typeJsDoc) {
            sb.append(typeJsDoc)
            sb.append('\n')
        }
        
        // Type declaration
        String keyword = type.isClass ? 'export class' : 'export interface'
        sb.append("${indent}${keyword} ${type.name}")
        
        // Type parameters with full class name bounds in JSDoc comment
        if (type.typeParams) {
            sb.append("<${convertTypeParams(type.typeParams, type.parsedTypeParams)}>")
        }
        
        // Extends - skip if extending itself
        if (type.extendsType && type.extendsType != type.name) {
            sb.append(" extends ${convertType(type.extendsType, parsed, currentPath, typeParamNames)}")
        }
        
        sb.append(' {\n')
        
        // Methods - compact format, no comments
        type.methods.each { method ->
            generateMethod(sb, method, parsed, currentPath, indent + '    ', typeParamNames)
        }
        
        // Fields (for classes)
        type.fields.each { field ->
            generateField(sb, field, parsed, currentPath, indent + '    ', typeParamNames)
        }
        
        sb.append("${indent}}\n")
        
        // Nested types as namespace
        if (!type.nestedTypes.isEmpty()) {
            sb.append("\n${indent}export namespace ${type.name} {\n")
            type.nestedTypes.each { nested ->
                // Always generate as interface, even if empty
                // Empty interfaces with extends are important for type hierarchy
                generateNestedType(sb, nested, type.name, type.name, parsed, currentPath, indent + '    ')
            }
            sb.append("${indent}}\n")
        }
        sb.append('\n')
    }
    
    /**
     * Generate a nested type (interface/class within a namespace)
     * Recursively handles nested types that themselves have nested types
     */
    private void generateNestedType(StringBuilder sb, JavaType type, String parentTypeName, String parentTypeChain, ParsedJavaFile parsed, String currentPath, String indent) {
        // Build a set of type parameter names for this nested type
        Set<String> typeParamNames = type.parsedTypeParams?.collect { it.name }?.toSet() ?: [] as Set
        
        // JSDoc
        String nestedChain = parentTypeChain ? "${parentTypeChain}.${type.name}" : type.name
        String javaFqn = buildJavaFqn(parsed.packageName, nestedChain)
        String nestedJsDoc = ensureJavaFqnTag(type.jsdoc, javaFqn, indent)
        if (nestedJsDoc) {
            sb.append(nestedJsDoc)
            sb.append('\n')
        }
        
        // Type declaration
        String keyword = type.isClass ? 'export class' : 'export interface'
        sb.append("${indent}${keyword} ${type.name}")
        
        // Type parameters with full class name bounds
        if (type.typeParams) {
            sb.append("<${convertTypeParams(type.typeParams, type.parsedTypeParams)}>")
        }
        
        // Extends - handle parent type reference specially, skip if extending itself
        if (type.extendsType && type.extendsType != type.name) {
            String extendsRef = convertTypeForNested(type.extendsType, parentTypeName, parsed, currentPath, typeParamNames)
            sb.append(" extends ${extendsRef}")
        }
        
        sb.append(' {\n')
        
        // Methods - compact format, no comments
        type.methods.each { method ->
            generateMethod(sb, method, parsed, currentPath, indent + '    ', typeParamNames)
        }
        
        sb.append("${indent}}\n")
        
        // If this nested type also has nested types, create a namespace for them
        if (!type.nestedTypes.isEmpty()) {
            sb.append("${indent}export namespace ${type.name} {\n")
            type.nestedTypes.each { nested ->
                // Always generate as interface, even if empty
                // Empty interfaces with extends are important for type hierarchy
                generateNestedType(sb, nested, type.name, nestedChain, parsed, currentPath, indent + '    ')
            }
            sb.append("${indent}}\n")
        }
    }
    
    /**
     * Convert type reference for nested types - handle parent type specially
     */
    private String convertTypeForNested(String javaType, String parentTypeName, ParsedJavaFile parsed, String currentPath, Set<String> typeParamNames) {
        if (javaType == null || javaType.isEmpty()) return 'any'
        
        javaType = javaType.trim()
        
        // If the type is the parent type name, use it directly (not as import from same file)
        if (javaType == parentTypeName) {
            return parentTypeName
        }
        
        // Otherwise use normal conversion
        return convertType(javaType, parsed, currentPath, typeParamNames)
    }
    
    private void generateMethod(StringBuilder sb, JavaMethod method, ParsedJavaFile parsed, String currentPath, String indent, Set<String> typeParamNames) {
        // JSDoc
        if (method.jsdoc) {
            sb.append(convertJsDoc(method.jsdoc, indent))
            sb.append('\n')
        }
        
        sb.append("${indent}${method.name}(")
        
        // Parameters
        List<String> paramStrs = method.parameters.collect { param ->
            String tsType = convertType(param.type, parsed, currentPath, typeParamNames)
            if (param.isVarargs) {
                return "...${param.name}: ${tsType}"
            }
            return "${param.name}: ${tsType}"
        }
        sb.append(paramStrs.join(', '))
        
        sb.append('): ')
        sb.append(convertType(method.returnType, parsed, currentPath, typeParamNames))
        sb.append(';\n')
    }
    
    private void generateField(StringBuilder sb, JavaField field, ParsedJavaFile parsed, String currentPath, String indent, Set<String> typeParamNames) {
        if (field.jsdoc) {
            sb.append(convertJsDoc(field.jsdoc, indent))
            sb.append('\n')
        }
        sb.append("${indent}${field.name}: ${convertType(field.type, parsed, currentPath, typeParamNames)};\n")
    }
    
    /**
     * Convert Java type to TypeScript type
     * @param typeParamNames Set of type parameter names from the enclosing type (e.g., "T", "U")
     */
    String convertType(String javaType, ParsedJavaFile parsed, String currentPath, Set<String> typeParamNames = [] as Set) {
        if (javaType == null || javaType.isEmpty()) return 'any'
        
        javaType = javaType.trim()
        
        // Check if it's a type parameter (like T, U, etc.)
        if (typeParamNames.contains(javaType)) {
            return javaType
        }
        
        // Check primitives first
        if (PRIMITIVE_MAPPINGS.containsKey(javaType)) {
            return PRIMITIVE_MAPPINGS[javaType]
        }
        
        // Handle arrays
        if (javaType.endsWith('[]') && javaType.length() > 2) {
            String baseType = javaType.substring(0, javaType.length() - 2)
            return convertType(baseType, parsed, currentPath, typeParamNames) + '[]'
        }
        
        // Handle generics
        if (javaType.contains('<')) {
            return convertGenericType(javaType, parsed, currentPath, typeParamNames)
        }
        
        // Check if it is an API type (needs import)
        String importPath = resolveImportPath(javaType, parsed, currentPath)
        if (importPath != null) {
            return "import('${importPath}').${javaType}"
        }
        
        // Check if it is a java.* type
        String fullType = resolveFullType(javaType, parsed)
        if (fullType != null && fullType.startsWith('java.')) {
            return "Java.${fullType}"
        }
        
        // Default - return as is (might be a type parameter like T, or unknown type)
        return javaType
    }
    
    private String convertGenericType(String type, ParsedJavaFile parsed, String currentPath, Set<String> typeParamNames = [] as Set) {
        int ltIndex = type.indexOf('<')
        int gtIndex = type.lastIndexOf('>')
        if (ltIndex == -1 || gtIndex == -1 || ltIndex >= gtIndex) {
            // Malformed generic, return as-is
            return type
        }
        String baseType = type.substring(0, ltIndex).trim()
        String genericPart = type.substring(ltIndex + 1, gtIndex).trim()
        
        // Handle common collections
        switch (baseType) {
            case 'List':
            case 'ArrayList':
            case 'LinkedList':
            case 'Collection':
            case 'Set':
            case 'HashSet':
            case 'Queue':
                return convertType(genericPart, parsed, currentPath, typeParamNames) + '[]'
            
            case 'Map':
            case 'HashMap':
            case 'LinkedHashMap':
                List<String> parts = splitGenericParams(genericPart)
                if (parts.size() >= 2) {
                    String keyType = convertType(parts[0], parsed, currentPath, typeParamNames)
                    String valueType = convertType(parts[1], parsed, currentPath, typeParamNames)
                    return "Record<${keyType}, ${valueType}>"
                }
                return 'Record<any, any>'
            
            case 'Optional':
                return convertType(genericPart, parsed, currentPath, typeParamNames) + ' | null'
            
            // Functional interfaces
            case 'Consumer':
                return "(arg: ${convertType(genericPart, parsed, currentPath, typeParamNames)}) => void"
            
            case 'Supplier':
                return "() => ${convertType(genericPart, parsed, currentPath, typeParamNames)}"
            
            case 'Function':
                List<String> funcParts = splitGenericParams(genericPart)
                if (funcParts.size() >= 2) {
                    return "(arg: ${convertType(funcParts[0], parsed, currentPath, typeParamNames)}) => ${convertType(funcParts[1], parsed, currentPath, typeParamNames)}"
                }
                return '(arg: any) => any'
            
            case 'Predicate':
                return "(arg: ${convertType(genericPart, parsed, currentPath, typeParamNames)}) => boolean"
            
            case 'BiConsumer':
                List<String> biParts = splitGenericParams(genericPart)
                if (biParts.size() >= 2) {
                    return "(arg1: ${convertType(biParts[0], parsed, currentPath, typeParamNames)}, arg2: ${convertType(biParts[1], parsed, currentPath, typeParamNames)}) => void"
                }
                return '(arg1: any, arg2: any) => void'
            
            case 'BiFunction':
                List<String> biFuncParts = splitGenericParams(genericPart)
                if (biFuncParts.size() >= 3) {
                    return "(arg1: ${convertType(biFuncParts[0], parsed, currentPath, typeParamNames)}, arg2: ${convertType(biFuncParts[1], parsed, currentPath, typeParamNames)}) => ${convertType(biFuncParts[2], parsed, currentPath, typeParamNames)}"
                }
                return '(arg1: any, arg2: any) => any'
            
            default:
                // Regular generic type
                String convertedBase = convertType(baseType, parsed, currentPath, typeParamNames)
                List<String> convertedParams = splitGenericParams(genericPart).collect { 
                    convertType(it, parsed, currentPath, typeParamNames) 
                }
                // For import types, we cannot add generics easily, so simplify
                if (convertedBase.startsWith('import(')) {
                    return convertedBase
                }
                return "${convertedBase}<${convertedParams.join(', ')}>"
        }
    }
    
    private List<String> splitGenericParams(String params) {
        List<String> result = []
        int depth = 0
        StringBuilder current = new StringBuilder()
        
        params.each { ch ->
            if (ch == '<') depth++
            else if (ch == '>') depth--
            else if (ch == ',' && depth == 0) {
                result << current.toString().trim()
                current = new StringBuilder()
                return
            }
            current.append(ch)
        }
        
        if (current.length() > 0) {
            result << current.toString().trim()
        }
        
        return result
    }
    
    /**
     * Convert Java type parameters to TypeScript
     * Outputs format like: T extends EntityPlayerMP /`*` net.minecraft.entity.player.EntityPlayerMP `*`/
     * This allows runtime parsing to extract the full Java class name for the type bound.
     */
    private String convertTypeParams(String typeParams, List<TypeParamInfo> parsedParams) {
        if (typeParams == null || typeParams.isEmpty()) return typeParams
        if (parsedParams == null || parsedParams.isEmpty()) return typeParams
        
        // Build result by iterating over parsed params
        List<String> convertedParams = []
        for (TypeParamInfo info in parsedParams) {
            StringBuilder sb = new StringBuilder()
            sb.append(info.name)
            
            if (info.boundType != null) {
                sb.append(" extends ")
                sb.append(info.boundType)
                
                // Add full class name as JSDoc-style comment if available
                if (info.fullBoundType != null) {
                    sb.append(" /* ${info.fullBoundType} */")
                }
            }
            
            convertedParams << sb.toString()
        }
        
        return convertedParams.join(', ')
    }
    
    private String resolveImportPath(String typeName, ParsedJavaFile parsed, String currentPath) {
        // Check imports for this type
        String fullType = resolveFullType(typeName, parsed)
        
        if (fullType == null) return null
        
        // Check if it is an API type
        int lastDotIndex = fullType.lastIndexOf('.')
        if (lastDotIndex == -1) return null
        String packagePrefix = fullType.substring(0, lastDotIndex)
        if (apiPackages.any { fullType.startsWith(it) }) {
            // Calculate relative path
            String typeFilePath = fullType.replace('.', '/') + '.d.ts'
            return calculateRelativePath(currentPath, typeFilePath)
        }
        
        return null
    }
    
    private String resolveFullType(String typeName, ParsedJavaFile parsed) {
        // Check explicit imports
        String explicitImport = parsed.imports.find { it.endsWith(".${typeName}") }
        if (explicitImport) return explicitImport
        
        // Check wildcard imports
        parsed.imports.each { imp ->
            if (imp.endsWith('.*')) {
                // Would need classpath to resolve, skip for now
            }
        }
        
        // Same package
        return "${parsed.packageName}.${typeName}"
    }
    
    /**
     * Parse type parameters like "T extends EntityPlayerMP" into structured TypeParamInfo
     * with full class names resolved from imports
     */
    private List<TypeParamInfo> parseTypeParams(String typeParams, ParsedJavaFile parsed) {
        List<TypeParamInfo> result = []
        if (typeParams == null || typeParams.isEmpty()) return result
        
        // Split by comma, but respect nested angle brackets
        List<String> params = splitGenericParams(typeParams)
        
        for (String param in params) {
            param = param.trim()
            TypeParamInfo info = new TypeParamInfo()
            
            // Check for "T extends BoundType" pattern
            def extendsMatcher = param =~ /(\w+)\s+extends\s+(.+)/
            if (extendsMatcher.find()) {
                info.name = extendsMatcher.group(1).trim()
                String boundType = extendsMatcher.group(2).trim()
                
                // Handle generic bounds like "Comparable<T>" - extract base type
                int angleIndex = boundType.indexOf('<')
                String baseBoundType = angleIndex > 0 ? boundType.substring(0, angleIndex) : boundType
                
                info.boundType = baseBoundType
                info.fullBoundType = resolveFullType(baseBoundType, parsed)
            } else {
                // Just a type param like "T" with no explicit bound
                info.name = param
                info.boundType = null
                info.fullBoundType = null
            }
            
            result << info
        }
        
        return result
    }
    
    private String calculateRelativePath(String fromPath, String toPath) {
        // Calculate relative path between two .d.ts files
        String[] fromParts = fromPath.split('/')
        String[] toParts = toPath.split('/')
        
        // Find common prefix
        int common = 0
        while (common < fromParts.length - 1 && common < toParts.length && fromParts[common] == toParts[common]) {
            common++
        }
        
        // Build relative path
        StringBuilder result = new StringBuilder()
        
        // Go up from current location
        int ups = fromParts.length - common - 1
        if (ups == 0) {
            result.append('./')
        } else {
            for (int i = 0; i < ups; i++) {
                result.append('../')
            }
        }
        
        // Go down to target
        for (int i = common; i < toParts.length; i++) {
            if (i > common) result.append('/')
            result.append(toParts[i])
        }
        
        // Remove .d.ts extension for imports
        String path = result.toString()
        if (path.endsWith('.d.ts')) {
            path = path.substring(0, path.length() - 5)
        }
        
        return path
    }
    
    /**
     * Convert JavaDoc to JSDoc format
     */
    private String convertJsDoc(String jsdoc, String indent) {
        if (jsdoc == null) return ''
        
        // Already in JSDoc format, just fix indentation
        String[] lines = jsdoc.split('\n')
        return lines.collect { line ->
            String trimmed = line.trim()
            if (trimmed.startsWith('*')) {
                return "${indent} ${trimmed}"
            } else if (trimmed.startsWith('/**') || trimmed.startsWith('*/')) {
                return "${indent}${trimmed}"
            } else {
                return "${indent}${trimmed}"
            }
        }.join('\n')
    }

    private String ensureJavaFqnTag(String jsdoc, String javaFqn, String indent) {
        if (javaFqn == null || javaFqn.isEmpty()) {
            return convertJsDoc(jsdoc, indent)
        }

        if (jsdoc != null && jsdoc.contains('@javaFqn')) {
            return convertJsDoc(jsdoc, indent)
        }

        if (jsdoc == null || jsdoc.trim().isEmpty()) {
            return "${indent}/**\n${indent} * @javaFqn ${javaFqn}\n${indent} */"
        }

        String converted = convertJsDoc(jsdoc, indent)
        int endIndex = converted.lastIndexOf('*/')
        if (endIndex >= 0) {
            String insert = "${indent} * @javaFqn ${javaFqn}\n"
            return converted.substring(0, endIndex) + insert + converted.substring(endIndex)
        }

        return converted + "\n${indent} * @javaFqn ${javaFqn}"
    }

    private String buildJavaFqn(String packageName, String typeName) {
        if (typeName == null || typeName.isEmpty()) return null
        if (packageName == null || packageName.isEmpty()) return typeName
        return packageName + '.' + typeName
    }
    
    /**
     * Extract @hookName value from a JSDoc comment.
     *
     * @param jsdoc The JSDoc comment string (may be null)
     * @return The hook name if @hookName tag is present, null otherwise
     */
    private String extractHookNameFromJsDoc(String jsdoc) {
        if (jsdoc == null || jsdoc.isEmpty()) return null

        // Match @hookName followed by the hook name value
        // Examples: @hookName animationStart, @hookName customGuiButton
        def matcher = jsdoc =~ /@hookName\s+(\w+)/
        if (matcher.find()) {
            return matcher.group(1)
        }
        return null
    }

    /**
     * Collect hook information from event interfaces.
     *
     * Looks for interfaces that:
     * 1. End with 'Event' (e.g., IPlayerEvent, IDBCEvent)
     * 2. Are in an 'event' package
     * 3. Have nested types that are also events
     *
     * Hook names are determined by:
     * 1. @hookName JSDoc tag if present (e.g., @hookName animationStart)
     * 2. Otherwise derived from the event class name (e.g., InitEvent -> init)
     *
     * The parent event type name (e.g., "INpcEvent", "IPlayerEvent") is used directly
     * as the namespace in hooks.d.ts. This allows any mod to register event interfaces
     * and have them automatically organized by their type name.
     */
    private void collectHooks(ParsedJavaFile parsed) {
        boolean isEventPackage = parsed.packageName.contains('.event')

        parsed.types.each { type ->
            // Check if this is an event interface:
            // - Must be an interface
            // - Either ends with 'Event' OR is in an event package
            boolean isEventType = type.isInterface && (
                type.name.endsWith('Event') || type.name.endsWith('Events') ||
                isEventPackage
            )

            if (isEventType && !type.nestedTypes.isEmpty()) {
                collectHooksFromNestedTypes(type.nestedTypes, type.name, parsed.packageName, isEventPackage)
            }
        }
    }

    /**
     * Recursively collect hooks from nested types.
     * This handles deeply nested event types like IAnimationEvent.IFrameEvent.Entered
     */
    private void collectHooksFromNestedTypes(List<JavaType> nestedTypes, String parentTypeName, String packageName, boolean isEventPackage) {
        nestedTypes.each { nested ->
            // Check if this nested type should be processed as a hook
            boolean isNestedEvent = nested.isInterface && (
                nested.name.endsWith('Event') ||
                nested.name.endsWith('Events') ||
                isEventPackage ||
                // Also include simple names like "Started", "Ended", "Entered", "Exited"
                // if they're inside an event interface
                parentTypeName.endsWith('Event')
            )

            if (isNestedEvent) {
                // Check for @hookName in JSDoc first, otherwise derive from class name
                String hookName = extractHookNameFromJsDoc(nested.jsdoc)
                if (hookName == null) {
                    hookName = deriveHookName(nested.name)
                }

                if (!hooks.containsKey(hookName)) {
                    hooks[hookName] = []
                }

                // Use the root event type name as the namespace
                // Extract the root type (e.g., "IAnimationEvent" from "IAnimationEvent.IFrameEvent")
                String rootType = parentTypeName.contains('.') ?
                    parentTypeName.substring(0, parentTypeName.indexOf('.')) : parentTypeName

                hooks[hookName] << new HookInfo(
                    eventType: rootType,
                    subEvent: nested.name,
                    fullType: "${parentTypeName}.${nested.name}",
                    packageName: packageName,
                    contextNamespace: rootType
                )

                // Recursively process any nested types within this nested type
                if (!nested.nestedTypes.isEmpty()) {
                    collectHooksFromNestedTypes(nested.nestedTypes, "${parentTypeName}.${nested.name}", packageName, isEventPackage)
                }
            }
        }
    }
    
    private String deriveHookName(String eventName) {
        // Convert event names to hook function names
        // e.g., "InitEvent" -> "init", "DamagedEvent" -> "damaged"
        String name = eventName
        if (name.endsWith('Event') && name.length() > 5) {
            name = name.substring(0, name.length() - 5)
        }
        // Convert to camelCase
        if (name.isEmpty()) return 'event'
        String hookName = name.length() > 1 ? (name.substring(0, 1).toLowerCase() + name.substring(1)) : name.toLowerCase()
        
        // Handle JavaScript reserved words
        Set<String> reservedWords = ['break', 'case', 'catch', 'continue', 'debugger', 'default', 'delete', 
                                      'do', 'else', 'finally', 'for', 'function', 'if', 'in', 'instanceof', 
                                      'new', 'return', 'switch', 'this', 'throw', 'try', 'typeof', 'var', 
                                      'void', 'while', 'with', 'class', 'const', 'enum', 'export', 'extends', 
                                      'import', 'super', 'implements', 'interface', 'let', 'package', 'private', 
                                      'protected', 'public', 'static', 'yield'] as Set
        
        if (reservedWords.contains(hookName)) {
            // Prefix with 'on' for reserved words
            hookName = 'on' + name
        }
        
        return hookName
    }
    
    /**
     * Generate index.d.ts with all type aliases
     */
    private void generateIndexFile() {
        StringBuilder sb = new StringBuilder()
        
        sb.append('/**\n')
        sb.append(' * Centralized global declarations for CustomNPC+ scripting.\n')
        sb.append(' * Auto-generated - do not edit manually.\n')
        sb.append(' */\n\n')
        
        sb.append('declare global {\n')
        sb.append('    // ============================================================================\n')
        sb.append('    // TYPE ALIASES - Make all interfaces available globally\n')
        sb.append('    // ============================================================================\n\n')
        
        generatedTypes.sort { a, b -> a.name <=> b.name }
        
        generatedTypes.each { type ->
            if (!type.name.contains('.')) {  // Skip nested types here
                sb.append("    type ${type.name} = import('./${type.filePath.replace('.d.ts', '')}').${type.name};\n")
            }
        }
        
        // Collect all parent types that have nested types (for namespace declarations)
        Map<String, List<TypeInfo>> parentToNested = [:]
        generatedTypes.each { type ->
            if (type.parentType) {
                if (!parentToNested.containsKey(type.parentType)) {
                    parentToNested[type.parentType] = []
                }
                parentToNested[type.parentType] << type
            }
        }
        
        // Generate namespace declarations for types with nested interfaces
        if (!parentToNested.isEmpty()) {
            sb.append('\n    // ============================================================================\n')
            sb.append('    // NESTED INTERFACES - Allow autocomplete like INpcEvent.InitEvent\n')
            sb.append('    // ============================================================================\n\n')
            
            parentToNested.sort { a, b -> a.key <=> b.key }.each { parentName, nestedTypes ->
                sb.append("    namespace ${parentName} {\n")
                nestedTypes.sort { a, b -> a.name <=> b.name }.each { nested ->
                    // Extract just the nested type name (e.g., "InitEvent" from "IPlayerEvent.InitEvent")
                    String nestedName = nested.name.contains('.') ? 
                        nested.name.substring(nested.name.lastIndexOf('.') + 1) : nested.name
                    sb.append("        interface ${nestedName} extends ${parentName} {}\n")
                }
                sb.append("    }\n\n")
            }
        }
        
        sb.append('}\n\n')
        sb.append('export {};\n')
        
        new File(outputDir, 'index.d.ts').text = sb.toString()
    }
    
    /**
     * Generate hooks.d.ts with event hook function declarations.
     *
     * Hooks are organized by their parent event interface type, using the type name
     * directly as the namespace. This provides context-aware autocomplete where
     * the same hook name (e.g., "interact") can have different event types depending
     * on the script context.
     *
     * Output format example:
     *   declare namespace INpcEvent {
     *       function interact(event: INpcEvent.InteractEvent): void;
     *       function init(event: INpcEvent.InitEvent): void;
     *   }
     *
     *   declare namespace IPlayerEvent {
     *       function interact(event: IPlayerEvent.InteractEvent): void;
     *       function init(event: IPlayerEvent.InitEvent): void;
     *   }
     *
     * The namespace name matches the event interface type exactly, making it easy
     * for the runtime parser to match hooks to their correct context.
     */
    private void generateHooksFile() {
        StringBuilder sb = new StringBuilder()

        sb.append('/**\n')
        sb.append(' * CustomNPC+ Event Hook Declarations\n')
        sb.append(' *\n')
        sb.append(' * Hooks are organized by their parent event interface (e.g., INpcEvent, IPlayerEvent).\n')
        sb.append(' * This allows the same hook name to have different event types per script context.\n')
        sb.append(' *\n')
        sb.append(' * Auto-generated from Java event interfaces - do not edit manually.\n')
        sb.append(' */\n\n')

        sb.append("import './minecraft-raw.d.ts';\n")
        sb.append("import './forge-events-raw.d.ts';\n\n")

        // Group hooks by their parent event type (contextNamespace = type.name)
        Map<String, Map<String, List<HookInfo>>> hooksByEventType = [:].withDefault { [:].withDefault { [] } }

        hooks.each { hookName, hookInfos ->
            hookInfos.each { info ->
                hooksByEventType[info.contextNamespace][hookName] << info
            }
        }

        // Output each event type as a namespace
        hooksByEventType.sort { a, b -> a.key <=> b.key }.each { eventTypeName, eventHooks ->
            sb.append("declare namespace ${eventTypeName} {\n")

            // Output all hooks for this event type, sorted alphabetically
            eventHooks.sort { a, b -> a.key <=> b.key }.each { hookName, hookInfos ->
                hookInfos.each { info ->
                    sb.append("    function ${hookName}(event: ${info.fullType}): void;\n")
                }
            }

            sb.append('}\n\n')
        }

        sb.append('export {};\n')

        new File(outputDir, 'hooks.d.ts').text = sb.toString()
    }
    
    // Data classes
    static class ParsedJavaFile {
        String packageName = ''
        List<String> imports = []
        List<JavaType> types = []
    }
    
    static class JavaType {
        String name
        String typeParams                          // Raw string like "T extends EntityPlayerMP"
        List<TypeParamInfo> parsedTypeParams = []  // Parsed type parameters with full class names
        String extendsType
        List<String> implementsTypes = []
        boolean isInterface
        boolean isClass
        String jsdoc
        List<JavaMethod> methods = []
        List<JavaField> fields = []
        List<JavaType> nestedTypes = []
    }
    
    static class JavaMethod {
        String name
        String returnType
        List<JavaParameter> parameters = []
        String jsdoc
    }
    
    static class JavaParameter {
        String name
        String type
        boolean isVarargs
    }
    
    static class JavaField {
        String name
        String type
        String jsdoc
    }
    
    static class TypeParamInfo {
        String name          // e.g., "T"
        String boundType     // e.g., "EntityPlayerMP" 
        String fullBoundType // e.g., "net.minecraft.entity.player.EntityPlayerMP"
    }
    
    static class TypeInfo {
        String name
        String packageName
        String filePath
        boolean isClass
        boolean isInterface
        String extendsType
        String parentType
    }
    
    static class HookInfo {
        String eventType        // Parent event interface name (e.g., "INpcEvent")
        String subEvent         // Nested event type name (e.g., "InteractEvent")
        String fullType         // Full type path (e.g., "INpcEvent.InteractEvent")
        String packageName      // Java package (e.g., "noppes.npcs.api.event")
        String contextNamespace // Namespace in hooks.d.ts (same as eventType, e.g., "INpcEvent")
    }
}
