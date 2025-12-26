package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.JavaTextContainer;

import java.util.ArrayList;
import java.util.List;

/**
 * New interpreter-based text container that replaces JavaTextContainer.
 * 
 * This class provides the same interface as JavaTextContainer but uses the 
 * new modular interpreter system internally. It produces compatible LineData
 * objects for seamless integration with GuiScriptTextArea.
 * 
 * Key differences from JavaTextContainer:
 * - Clean single-pass tokenization with 7 distinct phases
 * - Proper token navigation via prev()/next() methods
 * - Type-specific metadata on tokens (TypeInfo, FieldInfo, MethodInfo)
 * - Better separation of concerns (TypeResolver, ScriptDocument, ScriptLine)
 * - Hex color support in addition to Minecraft color codes
 */
public class ScriptTextContainer extends JavaTextContainer {

    /**
     * Feature flag to enable/disable the new interpreter system.
     * When false, falls back to original JavaTextContainer behavior.
     */
    public static boolean USE_NEW_INTERPRETER = true;

    private ScriptDocument document;

    public ScriptTextContainer(String text) {
        super(text);
        if (USE_NEW_INTERPRETER) {
            document = new ScriptDocument(text);
        }
    }

    @Override
    public void init(int width, int height) {
        if (!USE_NEW_INTERPRETER) {
            super.init(width, height);
            return;
        }

        lineHeight = ClientProxy.Font.height();
        if (lineHeight == 0) lineHeight = 12;

        // Initialize the document
        document.setText(text);
        document.init(width, height);

        // Convert ScriptLines to LineData for compatibility
        rebuildLineData();

        linesCount = lines.size();
        totalHeight = linesCount * lineHeight;
        visibleLines = Math.max(height / lineHeight - 1, 1);
    }

    /**
     * Initialize with explicit text (called when text changes).
     */
    @Override
    public void init(String text, int width, int height) {
        this.text = text == null ? "" : text.replaceAll("\\r?\\n|\\r", "\n");
        
        if (!USE_NEW_INTERPRETER) {
            super.init(text, width, height);
            return;
        }
        
        document = new ScriptDocument(this.text);
        init(width, height);
    }

    /**
     * Convert ScriptDocument lines to the legacy LineData format.
     */
    private void rebuildLineData() {
        lines.clear();
        for (ScriptLine scriptLine : document.getLines()) {
            LineData ld = new LineData(
                    scriptLine.getText(),
                    scriptLine.getGlobalStart(),
                    scriptLine.getGlobalEnd()
            );

            // Copy indent guides
            ld.indentCols.addAll(scriptLine.getIndentGuides());

            // Convert tokens - use fully qualified name to avoid conflict with inherited Token
            for (noppes.npcs.client.gui.util.script.interpreter.Token interpreterToken : scriptLine.getTokens()) {
                ld.tokens.add(new JavaTextContainer.Token(
                        interpreterToken.getText(),
                        toLegacyTokenType(interpreterToken.getType()),
                        interpreterToken.getGlobalStart(),
                        interpreterToken.getGlobalEnd()
                ));
            }

            lines.add(ld);
        }
    }

    /**
     * Main formatting entry point - matches JavaTextContainer signature.
     */
    @Override
    public void formatCodeText() {
        if (!USE_NEW_INTERPRETER) {
            super.formatCodeText();
            return;
        }
        
        document.formatCodeText();
        rebuildLineData();
    }

    /**
     * Convert new interpreter TokenType to legacy JavaTextContainer.TokenType format.
     */
    private JavaTextContainer.TokenType toLegacyTokenType(noppes.npcs.client.gui.util.script.interpreter.TokenType type) {
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.COMMENT) return JavaTextContainer.TokenType.COMMENT;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.STRING) return JavaTextContainer.TokenType.STRING;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.CLASS_KEYWORD) return JavaTextContainer.TokenType.CLASS_KEYWORD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.IMPORT_KEYWORD) return JavaTextContainer.TokenType.IMPORT_KEYWORD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.KEYWORD) return JavaTextContainer.TokenType.KEYWORD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.MODIFIER) return JavaTextContainer.TokenType.MODIFIER;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.NEW_TYPE) return JavaTextContainer.TokenType.NEW_TYPE;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.INTERFACE_DECL) return JavaTextContainer.TokenType.INTERFACE_DECL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.ENUM_DECL) return JavaTextContainer.TokenType.ENUM_DECL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.CLASS_DECL) return JavaTextContainer.TokenType.CLASS_DECL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.IMPORTED_CLASS) return JavaTextContainer.TokenType.IMPORTED_CLASS;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.TYPE_DECL) return JavaTextContainer.TokenType.TYPE_DECL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.METHOD_DECL) return JavaTextContainer.TokenType.METHOD_DECARE;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.METHOD_CALL) return JavaTextContainer.TokenType.METHOD_CALL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.LITERAL) return JavaTextContainer.TokenType.NUMBER;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.GLOBAL_FIELD) return JavaTextContainer.TokenType.GLOBAL_FIELD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.LOCAL_FIELD) return JavaTextContainer.TokenType.LOCAL_FIELD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.PARAMETER) return JavaTextContainer.TokenType.PARAMETER;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.UNDEFINED_VAR) return JavaTextContainer.TokenType.UNDEFINED_VAR;
        if (type == noppes.npcs.client.gui.util.script.interpreter.TokenType.VARIABLE) return JavaTextContainer.TokenType.VARIABLE;
        return JavaTextContainer.TokenType.DEFAULT;
    }

    // ==================== ACCESSORS ====================

    /**
     * Get the underlying ScriptDocument for advanced operations.
     */
    public ScriptDocument getDocument() {
        return document;
    }

    /**
     * Get the interpreter Token at a specific global position in the text.
     * Returns null if no token is at that position or if the interpreter is disabled.
     * 
     * @param globalPosition Position in the document text
     * @return The Token at that position, or null
     */
    public noppes.npcs.client.gui.util.script.interpreter.Token getInterpreterTokenAt(int globalPosition) {
        if (!USE_NEW_INTERPRETER || document == null) {
            return null;
        }
        
        ScriptLine line = document.getLineAt(globalPosition);
        if (line == null) {
            return null;
        }
        
        return line.getTokenAt(globalPosition);
    }

    /**
     * Get method blocks for compatibility with existing code.
     * Creates MethodBlock-like objects from the new MethodInfo.
     */
    public List<MethodBlockCompat> getMethodBlocks() {
        if (document == null) {
            return new ArrayList<>();
        }
        List<MethodBlockCompat> blocks = new ArrayList<>();
        for (MethodInfo method : document.getMethods()) {
            blocks.add(new MethodBlockCompat(method));
        }
        return blocks;
    }

    /**
     * Compatibility wrapper for MethodBlock.
     */
    public static class MethodBlockCompat {
        private final MethodInfo methodInfo;
        public List<String> parameters = new ArrayList<>();
        public List<String> localVariables = new ArrayList<>();

        public MethodBlockCompat(MethodInfo methodInfo) {
            this.methodInfo = methodInfo;
            for (FieldInfo param : methodInfo.getParameters()) {
                parameters.add(param.getName());
            }
        }

        public int getStartOffset() { return methodInfo.getDeclarationOffset(); }
        public int getEndOffset() { return methodInfo.getBodyEnd(); }

        public boolean containsPosition(int position) {
            return methodInfo.containsPosition(position);
        }

        public boolean isLocalDeclaredAtPosition(String varName, int position) {
            // Simplified - would need local variable tracking for full support
            return localVariables.contains(varName);
        }
    }
}
