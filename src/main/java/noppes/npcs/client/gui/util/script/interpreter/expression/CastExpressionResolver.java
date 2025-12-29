package noppes.npcs.client.gui.util.script.interpreter.expression;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import java.util.List;
import java.util.function.Function;

/**
 * Helper class for resolving cast expressions and distinguishing them from parenthesized expressions.
 * Handles:
 * - Simple casts: (Type) expr
 * - Nested casts: ((Type) expr)
 * - Cast chains: ((Type) expr).method()
 * - Parenthesized expressions: (expr)
 */
public class CastExpressionResolver {

    /**
     * Resolve a cast or parenthesized expression.
     * 
     * @param expr The expression starting with '('
     * @param position The position in the source text
     * @param resolveType Function to resolve type names to TypeInfo
     * @param resolveExpression Function to resolve arbitrary expressions
     * @param parseChain Function to parse expression chain
     * @param resolveChainSegment Function to resolve chain segments
     * @return The resolved type
     */
    public static <T> TypeInfo resolveCastOrParenthesizedExpression(
            String expr, 
            int position,
            Function<String, TypeInfo> resolveType,
            TypeResolver resolveExpression,
            ChainParser<T> parseChain,
            ChainSegmentResolver<T> resolveChainSegment) {
        
        if (!expr.startsWith("(")) return null;
        
        // Find the matching closing parenthesis for the first open paren
        int depth = 0;
        int closeParen = -1;
        boolean inString = false;
        char stringChar = 0;
         
        for (int i = 0; i < expr.length(); i++) {
            char c = expr.charAt(i);
            
            // Handle string literals
            if ((c == '"' || c == '\'') && (i == 0 || expr.charAt(i - 1) != '\\')) {
                if (!inString) {
                    inString = true;
                    stringChar = c;
                } else if (c == stringChar) {
                    inString = false;
                }
                continue;
            }
            if (inString) continue;
            
            if (c == '(') {
                depth++;
            } else if (c == ')') {
                depth--;
                if (depth == 0) {
                    closeParen = i;
                    break;
                }
            }
        }
        
        if (closeParen < 0) return null;
        
        // Extract content inside parens
        String insideParens = expr.substring(1, closeParen).trim();
        String afterParens = expr.substring(closeParen + 1).trim();
        
        // Check if this is a cast expression
        // A cast has a type name inside the parens (starts with uppercase or is primitive)
        // and is followed by an expression (not an operator or nothing)
        if (looksLikeCastType(insideParens) && looksLikeExpressionStart(afterParens)) {
            // This is a cast: (Type) rest
            TypeInfo castType = resolveType.apply(insideParens);
            if (castType != null && castType.isResolved()) {
                // If there's more after the cast target, resolve the chain
                // E.g., ((EntityPlayer) entity).worldObj
                if (!afterParens.isEmpty()) {
                    // Check if afterParens starts with '.' for a method/field chain
                    if (afterParens.startsWith(".")) {
                        // Continue resolving the chain from the cast type
                        String chainExpr = afterParens.substring(1).trim();
                        List<T> segments = parseChain.parse(chainExpr);
                        TypeInfo currentType = castType;
                        for (T segment : segments) {
                            currentType = resolveChainSegment.resolve(currentType, segment);
                            if (currentType == null) return null;
                        }
                        return currentType;
                    }
                }
                return castType;
            }
        }
        
        // Not a cast - treat as parenthesized expression
        // Could be ((expr).method()) or just (expr)
        
        // First, resolve what's inside the parens
        TypeInfo innerType = resolveExpression.resolve(insideParens, position);
        
        // If there's a chain after the parens, continue resolving
        if (innerType != null && !afterParens.isEmpty() && afterParens.startsWith(".")) {
            String chainExpr = afterParens.substring(1).trim();
            List<T> segments = parseChain.parse(chainExpr);
            TypeInfo currentType = innerType;
            for (T segment : segments) {
                currentType = resolveChainSegment.resolve(currentType, segment);
                if (currentType == null) return null;
            }
            return currentType;
        }
        
        return innerType;
    }
    
    /**
     * Check if a string looks like a valid type name for a cast.
     * Valid: EntityPlayer, int, String, java.lang.String, int[]
     */
    public static boolean looksLikeCastType(String s) {
        if (s == null || s.isEmpty()) return false;
        s = s.trim();
        
        // Handle array types: Type[] or Type[][]
        while (s.endsWith("[]")) {
            s = s.substring(0, s.length() - 2).trim();
        }
        
        if (s.isEmpty()) return false;
        
        // Primitive types
        switch (s) {
            case "byte": case "short": case "int": case "long":
            case "float": case "double": case "char": case "boolean":
                return true;
        }
        
        // Class type: starts with uppercase or is fully qualified
        if (Character.isUpperCase(s.charAt(0))) return true;
        
        // Fully qualified name: contains dots and ends with uppercase segment
        if (s.contains(".")) {
            String[] parts = s.split("\\.");
            String lastPart = parts[parts.length - 1];
            return !lastPart.isEmpty() && Character.isUpperCase(lastPart.charAt(0));
        }
        
        return false;
    }
    
    /**
     * Check if a string looks like the start of an expression (what follows a cast).
     * Valid: identifier, (, new, literals, this, etc.
     * Invalid: empty, operators like +, -, etc.
     */
    public static boolean looksLikeExpressionStart(String s) {
        if (s == null || s.isEmpty()) return false;
        s = s.trim();
        if (s.isEmpty()) return false;
        
        char first = s.charAt(0);
        
        // Parenthesis (nested cast or parenthesized expr)
        if (first == '(') return true;
        
        // Identifier or keyword
        if (Character.isJavaIdentifierStart(first)) return true;
        
        // String or char literal
        if (first == '"' || first == '\'') return true;
        
        // Number literal (could start with digit or dot for .5)
        if (Character.isDigit(first)) return true;
        if (first == '.' && s.length() > 1 && Character.isDigit(s.charAt(1))) return true;
        
        // Unary operators followed by expression
        if (first == '!' || first == '~') return true;
        if ((first == '+' || first == '-') && s.length() > 1) {
            // Could be unary +/- or ++/--
            char second = s.charAt(1);
            if (second == first || Character.isDigit(second) || Character.isJavaIdentifierStart(second) || second == '(') {
                return true;
            }
        }
        
        return false;
    }
    
    /**
     * Functional interface for resolving type expressions.
     */
    @FunctionalInterface
    public interface TypeResolver {
        TypeInfo resolve(String expr, int position);
    }
    
    /**
     * Functional interface for parsing expression chains.
     */
    @FunctionalInterface
    public interface ChainParser<T> {
        List<T> parse(String expr);
    }
    
    /**
     * Functional interface for resolving chain segments.
     */
    @FunctionalInterface
    public interface ChainSegmentResolver<T> {
        TypeInfo resolve(TypeInfo currentType, T segment);
    }
}
