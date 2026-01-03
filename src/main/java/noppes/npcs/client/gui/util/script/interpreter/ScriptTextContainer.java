package noppes.npcs.client.gui.util.script.interpreter;

import noppes.npcs.client.ClientProxy;
import noppes.npcs.client.gui.util.script.JavaTextContainer;
import noppes.npcs.client.gui.util.script.interpreter.field.FieldInfo;
import noppes.npcs.client.gui.util.script.interpreter.method.MethodInfo;

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
    
    /** The scripting language: "ECMAScript", "Groovy", etc. */
    private String language = "ECMAScript";

    public ScriptTextContainer(String text) {
        super(text);
        if (USE_NEW_INTERPRETER) {
            document = new ScriptDocument(text);
        }
    }
    
    public ScriptTextContainer(String text, String language) {
        super(text);
        this.language = language != null ? language : "ECMAScript";
        if (USE_NEW_INTERPRETER) {
            document = new ScriptDocument(text, this.language);
        }
    }
    
    /**
     * Set the scripting language. This affects syntax highlighting and type inference.
     * @param language The language name (e.g., "ECMAScript", "Groovy")
     */
    public void setLanguage(String language) {
        this.language = language != null ? language : "ECMAScript";
        if (document != null) {
            document.setLanguage(this.language);
        }
    }
    
    /**
     * Get the current scripting language.
     */
    public String getLanguage() {
        return language;
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
        
        document = new ScriptDocument(this.text, this.language);
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
            for (noppes.npcs.client.gui.util.script.interpreter.token.Token interpreterToken : scriptLine.getTokens()) {
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
    private JavaTextContainer.TokenType toLegacyTokenType(
            noppes.npcs.client.gui.util.script.interpreter.token.TokenType type) {
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.COMMENT) return JavaTextContainer.TokenType.COMMENT;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.STRING) return JavaTextContainer.TokenType.STRING;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.CLASS_KEYWORD) return JavaTextContainer.TokenType.CLASS_KEYWORD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.IMPORT_KEYWORD) return JavaTextContainer.TokenType.IMPORT_KEYWORD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.KEYWORD) return JavaTextContainer.TokenType.KEYWORD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.MODIFIER) return JavaTextContainer.TokenType.MODIFIER;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.INTERFACE_DECL) return JavaTextContainer.TokenType.INTERFACE_DECL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.ENUM_DECL) return JavaTextContainer.TokenType.ENUM_DECL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.CLASS_DECL) return JavaTextContainer.TokenType.CLASS_DECL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.IMPORTED_CLASS) return JavaTextContainer.TokenType.IMPORTED_CLASS;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.TYPE_DECL) return JavaTextContainer.TokenType.TYPE_DECL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.METHOD_DECL) return JavaTextContainer.TokenType.METHOD_DECARE;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.METHOD_CALL) return JavaTextContainer.TokenType.METHOD_CALL;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.LITERAL) return JavaTextContainer.TokenType.NUMBER;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.GLOBAL_FIELD) return JavaTextContainer.TokenType.GLOBAL_FIELD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.LOCAL_FIELD) return JavaTextContainer.TokenType.LOCAL_FIELD;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.PARAMETER) return JavaTextContainer.TokenType.PARAMETER;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.UNDEFINED_VAR) return JavaTextContainer.TokenType.UNDEFINED_VAR;
        if (type == noppes.npcs.client.gui.util.script.interpreter.token.TokenType.VARIABLE) return JavaTextContainer.TokenType.VARIABLE;
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
    public noppes.npcs.client.gui.util.script.interpreter.token.Token getInterpreterTokenAt(int globalPosition) {
        if (!USE_NEW_INTERPRETER || document == null) {
            return null;
        }
        
        ScriptLine line = document.getLineAt(globalPosition);
        if (line == null) {
            return null;
        }
        
        return line.getTokenAt(globalPosition);
    }
}
