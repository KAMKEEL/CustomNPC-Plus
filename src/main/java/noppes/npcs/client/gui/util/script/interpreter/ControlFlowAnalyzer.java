package noppes.npcs.client.gui.util.script.interpreter;

/**
 * Analyzes control flow in method bodies to determine if all paths return a value.
 * Used for detecting "missing return statement" errors.
 */
public final class ControlFlowAnalyzer {

    private ControlFlowAnalyzer() {} // Utility class

    /**
     * Check if the method body has a guaranteed return on all code paths.
     */
    public static boolean hasGuaranteedReturn(String body) {
        String cleanBody = CodeParser.removeStringsAndComments(body);
        
        return hasTopLevelReturn(cleanBody)
            || hasCompleteIfElseReturn(cleanBody)
            || hasTryFinallyReturn(cleanBody)
            || hasCompleteSwitchReturn(cleanBody);
    }

    /**
     * Check for a bare return statement at the method's top level (brace depth 0).
     */
    private static boolean hasTopLevelReturn(String body) {
        int braceDepth = 0;
        int parenDepth = 0;
        
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            
            if (c == '{') braceDepth++;
            else if (c == '}') braceDepth--;
            else if (c == '(') parenDepth++;
            else if (c == ')') parenDepth--;
            
            if (braceDepth == 0 && parenDepth == 0 && isKeywordAt(body, i, "return")) {
                return true;
            }
        }
        return false;
    }

    /**
     * Check for complete if-else chains where all branches return.
     */
    private static boolean hasCompleteIfElseReturn(String body) {
        int braceDepth = 0;
        
        for (int i = 0; i < body.length(); i++) {
            char c = body.charAt(i);
            if (c == '{') { braceDepth++; continue; }
            if (c == '}') { braceDepth--; continue; }
            
            if (braceDepth == 0 && isKeywordAt(body, i, "if")) {
                if (checkIfElseChainReturns(body, i)) {
                    return true;
                } 
            }
        }
        return false;
    }

    /**
     * Check if an if-else chain starting at the given position guarantees a return.
     * Handles: if(true) { return; } - always returns (else unreachable)
     *          if(false) { } else { return; } - else always executes
     */
    private static boolean checkIfElseChainReturns(String body, int ifStart) {
        int pos = ifStart;
        
        while (pos < body.length()) {
            if (!body.substring(pos).startsWith("if")) return false;
            pos += 2;
            
            pos = skipWhitespace(body, pos);
            if (pos >= body.length() || body.charAt(pos) != '(') return false;
            
            int condStart = pos;
            int condEnd = CodeParser.findMatchingParen(body, pos);
            if (condEnd < 0) return false;
            
            // Check for literal true/false conditions
            String condition = body.substring(condStart + 1, condEnd).trim();
            boolean isAlwaysTrue = condition.equals("true");
            boolean isAlwaysFalse = condition.equals("false");
            
            pos = skipWhitespace(body, condEnd + 1);
            if (pos >= body.length() || body.charAt(pos) != '{') return false;
            
            int blockEnd = CodeParser.findMatchingBrace(body, pos);
            if (blockEnd < 0) return false;
            
            String ifBlock = body.substring(pos + 1, blockEnd);
            boolean ifBlockReturns = hasGuaranteedReturn(ifBlock);
            
            // if(true) with return means we always return (else is unreachable)
            if (isAlwaysTrue && ifBlockReturns) {
                return true;
            }
            
            pos = skipWhitespace(body, blockEnd + 1);
            
            // Must have else clause for guaranteed return
            if (!body.substring(pos).startsWith("else")) {
                return false;
            }
            pos += 4;
            pos = skipWhitespace(body, pos);
            
            // else if - continue the chain
            if (isKeywordAt(body, pos, "if")) {
                continue;
            }
            
            // Plain else block - final branch
            if (pos >= body.length() || body.charAt(pos) != '{') return false;
            int elseBlockEnd = CodeParser.findMatchingBrace(body, pos);
            if (elseBlockEnd < 0) return false;
            
            String elseBlock = body.substring(pos + 1, elseBlockEnd);
            boolean elseBlockReturns = hasGuaranteedReturn(elseBlock);
            
            // For if-else to guarantee return: both branches must return
            // Special case: if(false) means if-block never executes, so only else matters
            if (isAlwaysFalse) {
                return elseBlockReturns;
            }
            
            // Normal case: both if and else must return
            return ifBlockReturns && elseBlockReturns;
        }
        return false;
    }

    /**
     * Check for try-finally where finally block returns.
     */
    private static boolean hasTryFinallyReturn(String body) {
        for (int i = 0; i < body.length(); i++) {
            if (!isKeywordAt(body, i, "try")) continue;
            
            int tryBlockStart = body.indexOf('{', i);
            if (tryBlockStart < 0) continue;
            
            int tryBlockEnd = CodeParser.findMatchingBrace(body, tryBlockStart);
            if (tryBlockEnd < 0) continue;
            
            // Skip catch blocks
            int searchPos = skipCatchBlocks(body, tryBlockEnd + 1);
            searchPos = skipWhitespace(body, searchPos);
            
            // Check for finally
            if (body.substring(searchPos).startsWith("finally")) {
                int finallyBlockStart = body.indexOf('{', searchPos);
                if (finallyBlockStart >= 0) {
                    int finallyBlockEnd = CodeParser.findMatchingBrace(body, finallyBlockStart);
                    if (finallyBlockEnd >= 0) {
                        String finallyBlock = body.substring(finallyBlockStart + 1, finallyBlockEnd);
                        if (hasGuaranteedReturn(finallyBlock)) {
                            return true;
                        }
                    }
                }
            }
        }
        return false;
    }

    private static int skipCatchBlocks(String body, int start) {
        int pos = start;
        while (true) {
            pos = skipWhitespace(body, pos);
            if (pos >= body.length() || !body.substring(pos).startsWith("catch")) break;
            
            int catchBlockStart = body.indexOf('{', pos);
            if (catchBlockStart < 0) break;
            
            int catchBlockEnd = CodeParser.findMatchingBrace(body, catchBlockStart);
            if (catchBlockEnd < 0) break;
            
            pos = catchBlockEnd + 1;
        }
        return pos;
    }

    /**
     * Check for switch with default where all cases return.
     * Returns false for now - full implementation is complex.
     */
    private static boolean hasCompleteSwitchReturn(String body) {
        return false;
    }

    // ==================== Utility Methods ====================

    private static boolean isKeywordAt(String text, int pos, String keyword) {
        if (pos + keyword.length() > text.length()) return false;
        if (!text.substring(pos).startsWith(keyword)) return false;
        
        boolean validBefore = pos == 0 || !Character.isLetterOrDigit(text.charAt(pos - 1));
        boolean validAfter = pos + keyword.length() >= text.length() 
            || !Character.isLetterOrDigit(text.charAt(pos + keyword.length()));
        
        return validBefore && validAfter;
    }

    private static int skipWhitespace(String text, int pos) {
        while (pos < text.length() && Character.isWhitespace(text.charAt(pos))) {
            pos++;
        }
        return pos;
    }
}
