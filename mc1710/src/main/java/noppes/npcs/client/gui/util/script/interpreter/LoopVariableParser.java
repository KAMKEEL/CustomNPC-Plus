package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.gui.util.script.ScopeInfo;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;
import noppes.npcs.client.gui.util.script.interpreter.type.TypeInfo;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class LoopVariableParser {

    private static final Pattern ENHANCED_FOR = Pattern.compile(
            "\\bfor\\s*\\(\\s*([A-Za-z_][\\w.<>,\\[\\] \\t\\n\\r]*?(?:\\[\\s*\\])*)\\s+(\\w+)\\s*:\\s*([^)]+)\\)");
    private static final Pattern CLASSIC_FOR = Pattern.compile(
            "\\bfor\\s*\\(\\s*([A-Za-z_][\\w.<>,\\[\\] \\t\\n\\r]*?(?:\\[\\s*\\])*)\\s+(\\w+)\\s*=");
    private static final Pattern FOR_IN = Pattern.compile(
            "\\bfor\\s*\\(\\s*(?:var|let|const)\\s+(\\w+)\\s+in\\s+([^)]+)\\)");
    private static final Pattern FOR_OF = Pattern.compile(
            "\\bfor\\s*\\(\\s*(?:var|let|const)\\s+(\\w+)\\s+of\\s+([^)]+)\\)");
    static final Pattern FOR_IN_OF_LOOKAHEAD = Pattern.compile("^\\s+(?:in|of)\\s");

    private static final Set<String> SKIP_TYPES = new HashSet<>(Arrays.asList(
            "return", "if", "while", "for", "switch", "catch", "new", "throw"));

    private final ScriptDocument doc;
    private final String text;

    LoopVariableParser(ScriptDocument doc, String text) {
        this.doc = doc;
        this.text = text;
    }

    boolean isForInOrOf(String textAfterVarName) {
        return FOR_IN_OF_LOOKAHEAD.matcher(textAfterVarName).find();
    }

    boolean isInsideForHeader(int bodyStart, int absPos) {
        int enclosingParen = doc.findEnclosingParenStart(bodyStart, absPos);
        return enclosingParen >= 0 && "for".equals(doc.readKeywordBefore(enclosingParen));
    }

    void parse(Map<Integer, Map<String, List<FieldInfo>>> methodLocals) {
        boolean js = doc.isJavaScript();

        for (MethodInfo method : doc.getAllMethods()) {
            int bodyStart = method.getBodyStart();
            int bodyEnd = method.getBodyEnd();
            if (bodyStart < 0 || bodyEnd <= bodyStart) continue;
            String bodyText = text.substring(bodyStart, Math.min(bodyEnd, text.length()));
            Map<String, List<FieldInfo>> locals = methodLocals.computeIfAbsent(method.getDeclarationOffset(), k -> new HashMap<>());
            scanRange(bodyText, bodyStart, bodyEnd, method, locals, js);
        }

        for (InnerCallableScope scope : doc.getInnerScopes()) {
            int bodyStart = scope.getBodyStart();
            int bodyEnd = scope.getBodyEnd();
            if (bodyStart < 0 || bodyEnd <= bodyStart) continue;
            String bodyText = text.substring(bodyStart, Math.min(bodyEnd, text.length()));
            MethodInfo ownerMethod = doc.findMethodAtPosition(bodyStart);
            if (ownerMethod == null) continue;
            Map<String, List<FieldInfo>> locals = methodLocals.computeIfAbsent(ownerMethod.getDeclarationOffset(), k -> new HashMap<>());
            scanRange(bodyText, bodyStart, bodyEnd, ownerMethod, locals, js);
        }

        scanTopLevelLoops();
    }

    private void scanTopLevelLoops() {
        boolean js = doc.isJavaScript();

        if (!js) {
            for (Matcher m = ENHANCED_FOR.matcher(text); m.find(); ) {
                int absForPos = m.start();
                if (doc.isExcluded(absForPos) || isInsideAnyScope(absForPos)) continue;
                String typeName = m.group(1).trim();
                String varName = m.group(2);
                TypeInfo typeInfo = doc.resolveType(typeName);
                ScopeInfo scope = computeForBodyScope(absForPos, 0, text.length());
                FieldInfo fi = FieldInfo.localField(varName, typeInfo, m.start(2), null, -1, -1, 0);
                fi.setScopeInfo(scope);
                doc.addTopLevelLocal(fi);
            }

            for (Matcher m = CLASSIC_FOR.matcher(text); m.find(); ) {
                int absForPos = m.start();
                if (doc.isExcluded(absForPos) || isInsideAnyScope(absForPos)) continue;
                String typeName = m.group(1).trim();
                String varName = m.group(2);
                if (SKIP_TYPES.contains(typeName)) continue;
                TypeInfo typeInfo = doc.resolveType(typeName);
                ScopeInfo scope = computeForBodyScope(absForPos, 0, text.length());
                FieldInfo fi = FieldInfo.localField(varName, typeInfo, m.start(2), null, -1, -1, 0);
                fi.setScopeInfo(scope);
                doc.addTopLevelLocal(fi);
            }
        } else {
            for (Matcher m = FOR_IN.matcher(text); m.find(); ) {
                int absForPos = m.start();
                if (doc.isExcluded(absForPos) || isInsideAnyScope(absForPos)) continue;
                String varName = m.group(1);
                ScopeInfo scope = computeForBodyScope(absForPos, 0, text.length());
                FieldInfo fi = FieldInfo.localField(varName, TypeInfo.fromPrimitive("int"), m.start(1), null, -1, -1, 0);
                fi.setScopeInfo(scope);
                doc.addTopLevelLocal(fi);
            }

            for (Matcher m = FOR_OF.matcher(text); m.find(); ) {
                int absForPos = m.start();
                if (doc.isExcluded(absForPos) || isInsideAnyScope(absForPos)) continue;
                String varName = m.group(1);
                String iterableExpr = m.group(2).trim();
                TypeInfo iterableType = doc.resolveExpressionType(iterableExpr, absForPos);
                TypeInfo elementType = (iterableType != null) ? iterableType.getElementType() : null;
                if (elementType == null) elementType = TypeInfo.ANY;
                ScopeInfo scope = computeForBodyScope(absForPos, 0, text.length());
                FieldInfo fi = FieldInfo.localField(varName, elementType, m.start(1), null, -1, -1, 0);
                fi.setScopeInfo(scope);
                doc.addTopLevelLocal(fi);
            }
        }
    }

    private boolean isInsideAnyScope(int pos) {
        for (MethodInfo method : doc.getAllMethods()) {
            if (method.containsPosition(pos)) return true;
        }
        for (InnerCallableScope scope : doc.getInnerScopes()) {
            if (scope.containsPosition(pos)) return true;
        }
        return false;
    }

    private void scanRange(String bodyText, int bodyStart, int bodyEnd,
                           MethodInfo method, Map<String, List<FieldInfo>> locals, boolean js) {
        if (!js) {
            Matcher m = ENHANCED_FOR.matcher(bodyText);
            while (m.find()) {
                int absForPos = bodyStart + m.start();
                if (doc.isExcluded(absForPos)) continue;
                String typeName = m.group(1).trim();
                String varName = m.group(2);
                int absVarPos = bodyStart + m.start(2);
                TypeInfo typeInfo = doc.resolveType(typeName);
                ScopeInfo scope = computeForBodyScope(absForPos, bodyStart, bodyEnd);
                FieldInfo fi = FieldInfo.localField(varName, typeInfo, absVarPos, method, -1, -1, 0);
                fi.setScopeInfo(scope);
                locals.computeIfAbsent(varName, k -> new ArrayList<>()).add(fi);
            }

            m = CLASSIC_FOR.matcher(bodyText);
            while (m.find()) {
                int absForPos = bodyStart + m.start();
                if (doc.isExcluded(absForPos)) continue;
                String typeName = m.group(1).trim();
                String varName = m.group(2);
                if (SKIP_TYPES.contains(typeName)) continue;
                int absVarPos = bodyStart + m.start(2);
                TypeInfo typeInfo = doc.resolveType(typeName);
                ScopeInfo scope = computeForBodyScope(absForPos, bodyStart, bodyEnd);
                FieldInfo fi = FieldInfo.localField(varName, typeInfo, absVarPos, method, -1, -1, 0);
                fi.setScopeInfo(scope);
                locals.computeIfAbsent(varName, k -> new ArrayList<>()).add(fi);
            }
        } else {
            Matcher m = FOR_IN.matcher(bodyText);
            while (m.find()) {
                int absForPos = bodyStart + m.start();
                if (doc.isExcluded(absForPos)) continue;
                String varName = m.group(1);
                int absVarPos = bodyStart + m.start(1);
                ScopeInfo scope = computeForBodyScope(absForPos, bodyStart, bodyEnd);
                FieldInfo fi = FieldInfo.localField(varName, TypeInfo.fromPrimitive("int"), absVarPos, method, -1, -1, 0);
                fi.setScopeInfo(scope);
                locals.computeIfAbsent(varName, k -> new ArrayList<>()).add(fi);
            }

            m = FOR_OF.matcher(bodyText);
            while (m.find()) {
                int absForPos = bodyStart + m.start();
                if (doc.isExcluded(absForPos)) continue;
                String varName = m.group(1);
                String iterableExpr = m.group(2).trim();
                int absVarPos = bodyStart + m.start(1);
                TypeInfo iterableType = doc.resolveExpressionType(iterableExpr, absForPos);
                TypeInfo elementType = (iterableType != null) ? iterableType.getElementType() : null;
                if (elementType == null) elementType = TypeInfo.ANY;
                ScopeInfo scope = computeForBodyScope(absForPos, bodyStart, bodyEnd);
                FieldInfo fi = FieldInfo.localField(varName, elementType, absVarPos, method, -1, -1, 0);
                fi.setScopeInfo(scope);
                locals.computeIfAbsent(varName, k -> new ArrayList<>()).add(fi);
            }
        }
    }

    private ScopeInfo computeForBodyScope(int forKeywordPos, int bodyStart, int bodyEnd) {
        int max = Math.min(bodyEnd, text.length());
        int openParen = -1;
        for (int i = forKeywordPos; i < max; i++) {
            if (doc.isExcluded(i)) continue;
            char c = text.charAt(i);
            if (c == '(') { openParen = i; break; }
            if (!Character.isWhitespace(c) && c != 'f' && c != 'o' && c != 'r') break;
        }
        if (openParen < 0) return new ScopeInfo(forKeywordPos, bodyEnd, false, "block");

        int closeParen = doc.findMatchingParen(openParen, max);
        if (closeParen < 0) return new ScopeInfo(forKeywordPos, bodyEnd, false, "block");

        int after = doc.skipWhitespaceAndExcluded(closeParen + 1, max);
        if (after < 0 || after >= max) return new ScopeInfo(forKeywordPos, bodyEnd, false, "block");

        if (text.charAt(after) == '{') {
            int closeBrace = doc.findMatchingBrace(after);
            if (closeBrace > 0) return new ScopeInfo(forKeywordPos, closeBrace + 1, false, "block");
            return new ScopeInfo(forKeywordPos, bodyEnd, false, "block");
        }

        int stmtEnd = doc.findStatementEnd(after, max);
        if (stmtEnd > after) return new ScopeInfo(forKeywordPos, stmtEnd, false, "block");
        return new ScopeInfo(forKeywordPos, bodyEnd, false, "block");
    }
}
