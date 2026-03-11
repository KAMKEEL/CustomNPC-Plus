package noppes.npcs.client.gui.util.script.interpreter.type;

import java.util.ArrayList;
import java.util.List;

/**
 * Parser for generic type expressions with nested type arguments.
 * 
 * Handles type expressions like:
 * - Simple types: "String", "IPlayer"
 * - Qualified names: "java.util.List", "IPlayerEvent.InteractEvent"
 * - Single generic: "List<String>", "Consumer<IAction>"
 * - Multiple generics: "Map<String, Integer>"
 * - Nested generics: "List<Map<String, Integer>>"
 *
 * Note: union/nullability/array semantics are handled by higher-level resolvers.
 * This parser only consumes suffix tokens where needed to correctly delimit type arguments.
 * 
 * This parser produces a ParsedType tree that can be resolved into parameterized TypeInfo instances.
 */
public class GenericTypeParser {

    /**
     * Represents a parsed type expression with potential generic arguments.
     */
    public static class ParsedType {
        /** The base type name (may be qualified, e.g., "java.util.List" or "IPlayer") */
        public final String baseName;
        
        /** The applied type arguments (for generics like List<String>), each is itself a ParsedType */
        public final List<ParsedType> typeArgs;
        
        /** The original raw string before parsing */
        public final String rawString;
        
        public ParsedType(String baseName, List<ParsedType> typeArgs, String rawString) {
            this.baseName = baseName;
            this.typeArgs = typeArgs != null ? typeArgs : new ArrayList<>();
            this.rawString = rawString;
        }
        
        /**
         * Check if this parsed type has generic arguments.
         */
        public boolean hasTypeArgs() {
            return !typeArgs.isEmpty();
        }
        
        /**
         * Get display string for debugging.
         */
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder(baseName);
            if (!typeArgs.isEmpty()) {
                sb.append("<");
                for (int i = 0; i < typeArgs.size(); i++) {
                    if (i > 0) sb.append(", ");
                    sb.append(typeArgs.get(i).toString());
                }
                sb.append(">");
            }
            return sb.toString();
        }
    }
    
    /**
     * Parse a type expression string into a ParsedType tree.
     * 
     * @param typeExpr The type expression to parse (e.g., "List<Map<String, Integer>>[]")
     * @return The parsed type, or null if parsing fails
     */
    public static ParsedType parse(String typeExpr) {
        if (typeExpr == null || typeExpr.isEmpty()) {
            return null;
        }
        
        String expr = typeExpr.trim();
        
        // Now parse the base type with potential generic arguments
        ParseResult result = parseTypeWithGenerics(expr, 0);
        if (result == null || result.type == null) {
            // Fallback: treat entire expression as a simple type
            return new ParsedType(expr, null, typeExpr);
        }
        
        return result.type;
    }
    
    /**
     * Internal result class for recursive parsing.
     */
    private static class ParseResult {
        final ParsedType type;
        final int endIndex; // position after this type in the string
        
        ParseResult(ParsedType type, int endIndex) {
            this.type = type;
            this.endIndex = endIndex;
        }
    }
    
    /**
     * Parse a type expression starting at the given index, handling nested generics.
     * Returns the parsed type and the index after this type expression.
     */
    private static ParseResult parseTypeWithGenerics(String expr, int startIndex) {
        int i = startIndex;
        int len = expr.length();
        
        // Skip leading whitespace
        while (i < len && Character.isWhitespace(expr.charAt(i))) {
            i++;
        }
        
        if (i >= len) {
            return null;
        }
        
        // Read the base type name (can include dots for qualified names)
        StringBuilder baseNameBuilder = new StringBuilder();
        while (i < len) {
            char c = expr.charAt(i);
            if (Character.isJavaIdentifierPart(c) || c == '.') {
                baseNameBuilder.append(c);
                i++;
            } else {
                break;
            }
        }
        String baseName = baseNameBuilder.toString().trim();

        if (baseName.isEmpty()) {
            return null;
        }
        
        // Remove trailing dots if any
        while (baseName.endsWith(".")) {
            baseName = baseName.substring(0, baseName.length() - 1);
        }
        
        // Skip whitespace before potential <
        while (i < len && Character.isWhitespace(expr.charAt(i))) {
            i++;
        }
        
        // Check for generic arguments
        List<ParsedType> typeArgs = new ArrayList<>();
        if (i < len && expr.charAt(i) == '<') {
            i++; // Skip '<'
            
            // Parse type arguments (comma-separated, handling nested <>)
            while (i < len) {
                // Skip whitespace
                while (i < len && Character.isWhitespace(expr.charAt(i))) {
                    i++;
                }
                
                if (i >= len) break;
                
                char c = expr.charAt(i);
                if (c == '>') {
                    i++; // Skip '>'
                    break;
                }
                
                if (c == ',') {
                    i++; // Skip ','
                    continue;
                }
                
                // Parse a type argument (may itself have generics)
                ParseResult argResult = parseTypeArgument(expr, i);
                if (argResult != null && argResult.type != null) {
                    typeArgs.add(argResult.type);
                    i = argResult.endIndex;
                } else {
                    // Skip forward until the next ',' or '>' at the current nesting level.
                    i = skipToNextTypeArgDelimiter(expr, i);
                }
            }
        }
        
        ParsedType type = new ParsedType(baseName, typeArgs, expr.substring(startIndex, i));
        return new ParseResult(type, i);
    }

    /**
     * Skip forward until we hit ',' or '>' that would delimit a type argument.
     * This prevents producing spurious additional type args when encountering unsupported syntax.
     */
    private static int skipToNextTypeArgDelimiter(String expr, int startIndex) {
        int i = startIndex;
        int depth = 0;
        boolean inString = false;
        char stringChar = 0;
        while (i < expr.length()) {
            char c = expr.charAt(i);
            if (inString) {
                if (c == stringChar && (i == 0 || expr.charAt(i - 1) != '\\')) {
                    inString = false;
                }
                i++;
                continue;
            }

            if (c == '\'' || c == '"') {
                inString = true;
                stringChar = c;
                i++;
                continue;
            }

            if (c == '<' || c == '(' || c == '[') {
                depth++;
            } else if (c == '>' || c == ')' || c == ']') {
                if (depth > 0) {
                    depth--;
                } else if (c == '>') {
                    return i; // Let caller handle closing '>'
                }
            } else if ((c == ',' || c == '>') && depth == 0) {
                return i;
            }
            i++;
        }
        return i;
    }
    
    /**
     * Parse a single type argument, which may include nested generics and array suffixes.
     */
    private static ParseResult parseTypeArgument(String expr, int startIndex) {
        int i = startIndex;
        int len = expr.length();
        
        // Skip leading whitespace
        while (i < len && Character.isWhitespace(expr.charAt(i))) {
            i++;
        }
        
        if (i >= len) {
            return null;
        }
        
        // Check for wildcard (? extends X, ? super X, or just ?)
        if (expr.charAt(i) == '?') {
            i++;
            // Skip whitespace
            while (i < len && Character.isWhitespace(expr.charAt(i))) {
                i++;
            }
            // Check for "extends" or "super"
            if (i < len && expr.substring(i).startsWith("extends ")) {
                i += 8; // Skip "extends "
                // Parse the bound type
                ParseResult boundResult = parseTypeWithGenerics(expr, i);
                if (boundResult != null) {
                    return boundResult;
                }
            } else if (i < len && expr.substring(i).startsWith("super ")) {
                i += 6; // Skip "super "
                // Parse the bound type
                ParseResult boundResult = parseTypeWithGenerics(expr, i);
                if (boundResult != null) {
                    return boundResult;
                }
            }
            // Plain wildcard, treat as Object
            return new ParseResult(new ParsedType("Object", null, "?"), i);
        }
        
        // Parse regular type with potential generics
        ParseResult typeResult = parseTypeWithGenerics(expr, i);
        if (typeResult == null) {
            return null;
        }
        
        i = typeResult.endIndex;
        ParsedType baseType = typeResult.type;
        
        // Skip whitespace
        while (i < len && Character.isWhitespace(expr.charAt(i))) {
            i++;
        }
        
        // Consume array suffixes / nullable suffix so the caller can correctly find delimiters.
        while (i + 1 < len && expr.charAt(i) == '[' && expr.charAt(i + 1) == ']') {
            i += 2;
            while (i < len && Character.isWhitespace(expr.charAt(i))) {
                i++;
            }
        }
        if (i < len && expr.charAt(i) == '?') {
            i++;
        }

        // Preserve the full raw substring (including suffixes) for higher-level resolvers.
        if (i != typeResult.endIndex) {
            return new ParseResult(new ParsedType(baseType.baseName, baseType.typeArgs, expr.substring(startIndex, i)), i);
        }

        return new ParseResult(baseType, i);
    }
    
    /**
     * Convenience method to check if a type string contains generic arguments.
     */
    public static boolean hasGenerics(String typeExpr) {
        return typeExpr != null && typeExpr.contains("<") && typeExpr.contains(">");
    }
    
    /**
     * Strip generic arguments from a type string (for base type lookup).
     * Example: "List<String>" -> "List"
     */
    public static String stripGenerics(String typeExpr) {
        if (typeExpr == null) return null;
        int idx = typeExpr.indexOf('<');
        if (idx > 0) {
            return typeExpr.substring(0, idx).trim();
        }
        return typeExpr;
    }
}
