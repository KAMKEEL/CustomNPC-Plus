package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.ScriptTypeInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldAccessInfo;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenErrorMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Helper class for marking chained field accesses in script documents.
 * Handles patterns like: identifier.field, this.field, super.field, method().field
 */
public class FieldChainMarker {
    private final ScriptDocument document;
    private final String text;

    public FieldChainMarker(ScriptDocument document, String text) {
        this.document = document;
        this.text = text;
    }

    /**
     * Mark all chained field accesses in the document.
     * Uses two passes: one for standard chains, one for method/array result chains.
     */
    public void markChainedFieldAccesses(List<ScriptLine.Mark> marks) {
        // ==================== PASS 1: Standard identifier chains ====================
        // Pattern: identifier.identifier, this.identifier, super.identifier
        Pattern chainPattern = Pattern.compile(
                "\\b(this|super|[a-zA-Z_][a-zA-Z0-9_]*)\\s*\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher m = chainPattern.matcher(text);

        while (m.find()) {
            int chainStart = m.start(1);
            
            // Skip excluded ranges and imports
            if (document.isExcluded(chainStart) || document.isInImportOrPackage(chainStart)) continue;
            
            // Skip if second segment is a method call - markMethodCalls handles it
            if (document.isFollowedByParen(m.end(2))) continue;

            // Build the full chain
            ChainData chain = buildChain(m);
            if (chain == null) continue;

            // Resolve the starting type
            ChainContext ctx = resolveChainStart(chain, chainStart);

            // Mark each segment
            for (int i = ctx.startIndex; i < chain.segments.size(); i++) {
                ctx.currentIndex = i;
                int[] pos = chain.positions.get(i);
                
                if (document.isExcluded(pos[0])) continue;

                // Determine what type of marking this segment needs
                MarkResult result = resolveSegmentMark(ctx, chainStart);
                
                // Apply the mark
                if (result.mark != null) {
                    marks.add(result.mark);
                }
                
                // Update context for next segment
                ctx.currentType = result.nextType;
            }
        }

        // ==================== PASS 2: Method/array result chains ====================
        // Handle: getMinecraft().thePlayer, array[0].length
        Pattern dotIdent = Pattern.compile("\\.\\s*([a-zA-Z_][a-zA-Z0-9_]*)");
        Matcher md = dotIdent.matcher(text);
        
        while (md.find()) {
            int identStart = md.start(1);
            int identEnd = md.end(1);
            int dotPos = md.start();

            // Skip excluded ranges and imports
            if (document.isExcluded(identStart) || document.isInImportOrPackage(identStart)) continue;

            // Only handle when preceded by ')' or ']' (method call or array access result)
            Character precedingChar = getNonWhitespaceBefore(dotPos);
            if (precedingChar == null || (precedingChar != ')' && precedingChar != ']')) continue;

            // Get receiver type from the expression before the dot
            int[] bounds = document.findReceiverBoundsBefore(dotPos);
            if (bounds == null) continue;
            
            if (document.isExcluded(bounds[0]) || document.isInImportOrPackage(bounds[0])) continue;

            String receiverExpr = text.substring(bounds[0], bounds[1]).trim();
            if (receiverExpr.isEmpty()) continue;

            TypeInfo receiverType = document.resolveExpressionType(receiverExpr, bounds[0]);

            // Mark the first field and continue the chain
            markReceiverChainSegments(marks, md.group(1), identStart, identEnd, receiverType);
        }
    }

    // ==================== CHAIN BUILDING HELPERS ====================

    /** Data class for a parsed chain */
    private static class ChainData {
        final List<String> segments = new ArrayList<>();
        final List<int[]> positions = new ArrayList<>();
    }

    /** Context for chain resolution */
    private static class ChainContext {
        ChainData chain;
        int currentIndex;
        TypeInfo currentType;
        ScriptTypeInfo enclosingType;  // The enclosing script type for 'this' resolution
        int startIndex;
        boolean firstIsThis;
        boolean firstIsSuper;
        boolean firstIsPrecededByDot;
    }

    /** Result of resolving a segment's mark */
    private static class MarkResult {
        final ScriptLine.Mark mark;
        final TypeInfo nextType;
        
        MarkResult(ScriptLine.Mark mark, TypeInfo nextType) {
            this.mark = mark;
            this.nextType = nextType;
        }
    }

    /** Build a complete chain from a regex match, continuing to read more segments */
    private ChainData buildChain(Matcher m) {
        ChainData chain = new ChainData();
        
        // Add first two segments from the match
        chain.segments.add(m.group(1));
        chain.positions.add(new int[]{m.start(1), m.end(1)});
        chain.segments.add(m.group(2));
        chain.positions.add(new int[]{m.start(2), m.end(2)});

        // Continue reading subsequent .identifier segments
        int pos = m.end(2);
        while (pos < text.length()) {
            pos = document.skipWhitespace(pos);
            if (pos >= text.length() || text.charAt(pos) != '.') break;
            pos++; // Skip dot
            
            pos = document.skipWhitespace(pos);
            if (pos >= text.length() || !Character.isJavaIdentifierStart(text.charAt(pos))) break;

            // Read identifier
            int identStart = pos;
            while (pos < text.length() && Character.isJavaIdentifierPart(text.charAt(pos))) pos++;
            int identEnd = pos;

            // Stop if this is a method call
            if (document.isFollowedByParen(identEnd)) break;

            chain.segments.add(text.substring(identStart, identEnd));
            chain.positions.add(new int[]{identStart, identEnd});
        }

        return chain;
    }

    /** Resolve the starting context for a chain */
    private ChainContext resolveChainStart(ChainData chain, int chainStart) {
        ChainContext ctx = new ChainContext();
        ctx.chain = chain;
        ctx.currentIndex = 0;
        String first = chain.segments.get(0);
        
        ctx.firstIsThis = first.equals("this");
        ctx.firstIsSuper = first.equals("super");
        ctx.firstIsPrecededByDot = document.isPrecededByDot(chainStart);
        ctx.startIndex = ctx.firstIsPrecededByDot ? 0 : 1;
        ctx.currentType = null;
        ctx.enclosingType = document.findEnclosingScriptType(chainStart);

        if (ctx.firstIsThis) {
            // 'this' - resolve to enclosing class/global fields
            ctx.currentType = ctx.enclosingType;
        } else if (ctx.firstIsSuper) {
            // 'super' - resolve to parent class
            if (ctx.enclosingType != null && ctx.enclosingType.hasSuperClass()) {
                ctx.currentType = ctx.enclosingType.getSuperClass();
            }
        } else if (ctx.firstIsPrecededByDot) {
            // Field access on a receiver (e.g., getMinecraft().thePlayer)
            TypeInfo receiverType = document.resolveReceiverChain(chainStart);
            if (receiverType != null && receiverType.hasField(first)) {
                FieldInfo varInfo = receiverType.getFieldInfo(first);
                ctx.currentType = (varInfo != null) ? varInfo.getTypeInfo() : null;
            }
        } else {
            // Try as type first (static access), then as variable
            TypeInfo typeCheck = document.resolveType(first);
            if (typeCheck != null && typeCheck.isResolved()) {
                ctx.currentType = typeCheck;
            } else {
                FieldInfo varInfo = document.resolveVariable(first, chainStart);
                ctx.currentType = (varInfo != null) ? varInfo.getTypeInfo() : null;
            }
        }

        return ctx;
    }

    /** Resolve what mark a segment should get */
    private MarkResult resolveSegmentMark(ChainContext ctx, int chainStart) {
        int index = ctx.currentIndex;
        String segment = ctx.chain.segments.get(index);
        int[] pos = ctx.chain.positions.get(index);
        
        // Case 1: First segment preceded by dot (field on receiver)
        if (index == 0 && ctx.firstIsPrecededByDot) {
            return resolveReceiverFieldMark(ctx, chainStart);
        }

        // Case 2: this.field
        if (index == 1 && ctx.firstIsThis) {
            return resolveThisFieldMark(ctx);
        }

        // Case 3: super.field
        if (index == 1 && ctx.firstIsSuper) {
            return resolveSuperFieldMark(ctx);
        }

        // Case 4: Resolved type with field
        if (ctx.currentType != null && ctx.currentType.isResolved()) {
            return resolveTypedFieldMark(ctx);
        }

        // Case 5: Unresolved - mark as undefined
        return new MarkResult(
            new ScriptLine.Mark(pos[0], pos[1], TokenType.UNDEFINED_VAR),
            null
        );
    }

    // ==================== SEGMENT RESOLUTION HELPERS ====================

    /** Resolve mark for field access on a receiver (preceded by dot) */
    private MarkResult resolveReceiverFieldMark(ChainContext ctx, int chainStart) {
        int index = ctx.currentIndex;
        String segment = ctx.chain.segments.get(index);
        int[] pos = ctx.chain.positions.get(index);
        boolean isLast = (index == ctx.chain.segments.size() - 1);
        boolean isStatic = isStaticContext(ctx);
        
        TypeInfo receiverType = document.resolveReceiverChain(chainStart);
        
        if (receiverType != null && receiverType.hasField(segment)) {
            FieldInfo fieldInfo = receiverType.getFieldInfo(segment);
            FieldAccessInfo accessInfo = document.createFieldAccessInfo(segment, pos[0], pos[1], 
                    receiverType, fieldInfo, isLast, isStatic);

            return new MarkResult(new ScriptLine.Mark(pos[0], pos[1], getFieldTokenType(fieldInfo), accessInfo),
                (fieldInfo != null) ? fieldInfo.getTypeInfo() : null
            );
        }
        
        return new MarkResult(
            new ScriptLine.Mark(pos[0], pos[1], TokenType.UNDEFINED_VAR),
            null
        );
    }

    /** Resolve mark for this.field access */
    private MarkResult resolveThisFieldMark(ChainContext ctx) {
        int index = ctx.currentIndex;
        String segment = ctx.chain.segments.get(index);
        int[] pos = ctx.chain.positions.get(index);
        boolean isLast = (index == ctx.chain.segments.size() - 1);

        boolean found = false;
        FieldInfo fieldInfo = null;

        // First check enclosing script type fields
        if (ctx.enclosingType != null && ctx.enclosingType.hasField(segment)) {
            found = true;
            fieldInfo = ctx.enclosingType.getFieldInfo(segment);
        } else if (document.getGlobalFields().containsKey(segment)) {
            found = true;
            fieldInfo = document.getGlobalFields().get(segment);
        }

        if (found) {
            FieldAccessInfo accessInfo = document.createFieldAccessInfo(segment, pos[0], pos[1], ctx.enclosingType,
                    fieldInfo, isLast, false);

            return new MarkResult(new ScriptLine.Mark(pos[0], pos[1], getFieldTokenType(fieldInfo), accessInfo),
                    (fieldInfo != null) ? fieldInfo.getTypeInfo() : null
            );
        }

        return new MarkResult(new ScriptLine.Mark(pos[0], pos[1], TokenType.UNDEFINED_VAR), null
        );
    }

    /** Resolve mark for super.field access */
    private MarkResult resolveSuperFieldMark(ChainContext ctx) {
        int index = ctx.currentIndex;
        String segment = ctx.chain.segments.get(index);
        int[] pos = ctx.chain.positions.get(index);
        boolean isLast = (index == ctx.chain.segments.size() - 1);
        TypeInfo superType = ctx.currentType;
        
        if (superType == null) {
            return new MarkResult(
                new ScriptLine.Mark(pos[0], pos[1], TokenType.UNDEFINED_VAR,
                    TokenErrorMessage.from("Cannot resolve field '" + segment + "'").clearOtherErrors()),
                null
            );
        }

        // Search through inheritance hierarchy
        boolean found = false;
        FieldInfo fieldInfo = null;

        if (superType instanceof ScriptTypeInfo) {
            ScriptTypeInfo scriptSuper = (ScriptTypeInfo) superType;
            found = scriptSuper.hasFieldInHierarchy(segment);
            if (found) fieldInfo = scriptSuper.getFieldInfoInHierarchy(segment);
        } else {
            found = superType.hasField(segment);
            if (found) fieldInfo = superType.getFieldInfo(segment);
        }

        if (found) {
            FieldAccessInfo accessInfo = document.createFieldAccessInfo(segment, pos[0], pos[1], 
                    superType, fieldInfo, isLast, false);
            return new MarkResult(new ScriptLine.Mark(pos[0], pos[1], getFieldTokenType(fieldInfo), accessInfo),
                (fieldInfo != null) ? fieldInfo.getTypeInfo() : null
            );
        }

        String errorMsg = "Field '" + segment + "' not found in parent class hierarchy starting from '" 
                + superType.getSimpleName() + "'";
        return new MarkResult(
            new ScriptLine.Mark(pos[0], pos[1], TokenType.UNDEFINED_VAR,
                TokenErrorMessage.from(errorMsg).clearOtherErrors()),
            null
        );
    }

    /** Resolve mark for typed field access */
    private MarkResult resolveTypedFieldMark(ChainContext ctx) {
        int index = ctx.currentIndex;
        String segment = ctx.chain.segments.get(index);
        int[] pos = ctx.chain.positions.get(index);
        boolean isLast = (index == ctx.chain.segments.size() - 1);
        boolean isStatic = isStaticContext(ctx);
        TypeInfo currentType = ctx.currentType;
        
        if (!currentType.hasField(segment)) {
            return new MarkResult(
                new ScriptLine.Mark(pos[0], pos[1], TokenType.UNDEFINED_VAR),
                null
            );
        }

        FieldInfo fieldInfo = currentType.getFieldInfo(segment);

        // Check for static access error
        if (isStatic && fieldInfo != null && !fieldInfo.isStatic()) {
            TokenErrorMessage errorMsg = TokenErrorMessage
                    .from("Cannot access non-static field '" + segment + "' from static context '" 
                            + currentType.getSimpleName() + "'")
                    .clearOtherErrors();
            return new MarkResult(
                new ScriptLine.Mark(pos[0], pos[1], TokenType.UNDEFINED_VAR, errorMsg),
                null
            );
        }

        // Valid field access
        FieldAccessInfo accessInfo = document.createFieldAccessInfo(segment, pos[0], pos[1], 
                currentType, fieldInfo, isLast, isStatic);

        return new MarkResult(new ScriptLine.Mark(pos[0], pos[1], getFieldTokenType(fieldInfo), accessInfo),
            (fieldInfo != null) ? fieldInfo.getTypeInfo() : null
        );
    }

    /** Mark segments following a method call or array access result */
    private void markReceiverChainSegments(List<ScriptLine.Mark> marks, String firstField, 
                                            int identStart, int identEnd, TypeInfo receiverType) {
        // Mark the first field
        FieldInfo fInfo = null;
        TypeInfo currentType = null;
        
        if (receiverType != null && receiverType.hasField(firstField)) {
            fInfo = receiverType.getFieldInfo(firstField);
            boolean hasMore = document.isFollowedByDot(identEnd);
            // For method/array result chains, the receiver is always an instance, not a type
            boolean isStatic = false;
            
            FieldAccessInfo accessInfo = document.createFieldAccessInfo(firstField, identStart, identEnd, 
                    receiverType, fInfo, !hasMore, isStatic);

            marks.add(new ScriptLine.Mark(identStart, identEnd, getFieldTokenType(fInfo), accessInfo));
            currentType = (fInfo != null) ? fInfo.getTypeInfo() : null;
        } else {
            marks.add(new ScriptLine.Mark(identStart, identEnd, TokenType.UNDEFINED_VAR));
            return;
        }

        // Continue the chain
        int pos = identEnd;
        while (pos < text.length()) {
            pos = document.skipWhitespace(pos);
            if (pos >= text.length() || text.charAt(pos) != '.') break;
            pos++; // Skip dot
            
            pos = document.skipWhitespace(pos);
            if (pos >= text.length() || !Character.isJavaIdentifierStart(text.charAt(pos))) break;

            // Read identifier
            int nStart = pos;
            while (pos < text.length() && Character.isJavaIdentifierPart(text.charAt(pos))) pos++;
            int nEnd = pos;
            String seg = text.substring(nStart, nEnd);

            // Stop if method call
            if (document.isFollowedByParen(nEnd)) break;
            if (document.isExcluded(nStart)) break;

            boolean isLast = !document.isFollowedByDot(nEnd);

            if (currentType != null && currentType.isResolved() && currentType.hasField(seg)) {
                FieldInfo segInfo = currentType.getFieldInfo(seg);
                // In method/array result chains, segments are always instance access
                boolean isStatic = false;
                
                FieldAccessInfo accessInfo = document.createFieldAccessInfo(seg, nStart, nEnd, 
                        currentType, segInfo, isLast, isStatic);

                marks.add(new ScriptLine.Mark(nStart, nEnd, getFieldTokenType(segInfo), accessInfo));
                currentType = (segInfo != null) ? segInfo.getTypeInfo() : null;
            } else {
                marks.add(new ScriptLine.Mark(nStart, nEnd, TokenType.UNDEFINED_VAR));
                break;
            }
        }
    }

    // ==================== UTILITY HELPERS ====================

    private TokenType getFieldTokenType(FieldInfo fieldInfo) {
        if (fieldInfo != null && fieldInfo.isEnumConstant())
            return TokenType.ENUM_CONSTANT;

        return TokenType.GLOBAL_FIELD;
    }

    /** Get the non-whitespace character before a position */
    private Character getNonWhitespaceBefore(int pos) {
        int before = pos - 1;
        while (before >= 0 && Character.isWhitespace(text.charAt(before))) before--;
        return (before >= 0) ? text.charAt(before) : null;
    }

    /** Check if the receiver type is a class/type (static context) */
    private boolean isStaticContext(ChainContext ctx) {
        // Static context if the previous segment name resolves to a type/class
        if (ctx.currentIndex <= 0)
            return false;

        String previousSegment = ctx.chain.segments.get(ctx.currentIndex - 1);

        // Try to resolve the previous segment as a type
        TypeInfo typeCheck = document.resolveType(previousSegment);
        return typeCheck != null && typeCheck.isResolved();
    }
}
