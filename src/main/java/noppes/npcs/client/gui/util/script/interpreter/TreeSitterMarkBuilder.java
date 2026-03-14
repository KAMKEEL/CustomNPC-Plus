package noppes.npcs.client.gui.util.script.interpreter;

import bigguy.treesitter.java.*;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

import java.util.ArrayList;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;

/**
 * Tree-sitter-based syntax highlighting mark builder — the Zed-style replacement
 * for regex-based visual highlighting.
 *
 * <p>Architecture mirrors Zed's pipeline: Parse → Query (highlights.scm) → Map → Emit.
 * Produces a base layer of {@link ScriptLine.Mark}s from tree-sitter captures.
 * Semantic passes in {@link ScriptDocument} (method calls, variables, field accesses)
 * layer on top via the priority system in {@link TokenType}.
 *
 * <p>Not thread-safe per instance. The underlying {@link JavaParser} and
 * {@link JavaQueryEngine} are thread-safe singletons.
 */
public final class TreeSitterMarkBuilder {

    private static volatile JavaParser sharedParser;
    private static final Object PARSER_LOCK = new Object();
    private static final JavaQueryEngine ENGINE = new JavaQueryEngine();

    /**
     * HighlightGroup → TokenType mapping. Equivalent to Zed's theme capture → color resolution.
     * Intentionally conservative: identifiers default to DEFAULT so semantic passes can override.
     */
    private static final Map<HighlightGroup, TokenType> GROUP_TO_TOKEN;

    static {
        Map<HighlightGroup, TokenType> m = new EnumMap<>(HighlightGroup.class);

        m.put(HighlightGroup.PROPERTY,              TokenType.GLOBAL_FIELD);
        m.put(HighlightGroup.VARIABLE,              TokenType.LOCAL_FIELD);
        m.put(HighlightGroup.PARAMETER,             TokenType.PARAMETER);
        m.put(HighlightGroup.FUNCTION,              TokenType.METHOD_DECL);
        m.put(HighlightGroup.FUNCTION_CALL,         TokenType.METHOD_CALL);
        m.put(HighlightGroup.SUPER,                 TokenType.KEYWORD);
        m.put(HighlightGroup.CONSTRUCTOR,           TokenType.METHOD_DECL);
        m.put(HighlightGroup.TYPE,                  TokenType.IMPORTED_CLASS);
        m.put(HighlightGroup.INTERFACE,             TokenType.INTERFACE_DECL);
        m.put(HighlightGroup.TYPE_BUILTIN,          TokenType.KEYWORD);
        m.put(HighlightGroup.ENUM,                  TokenType.ENUM_DECL);
        m.put(HighlightGroup.CONSTANT,              TokenType.STATIC_FINAL_FIELD);
        m.put(HighlightGroup.TYPE_PARAMETER,        TokenType.GENERIC_TYPE_PARAM);
        m.put(HighlightGroup.CONSTANT_BUILTIN,      TokenType.LITERAL);
        m.put(HighlightGroup.KEYWORD,               TokenType.KEYWORD);
        m.put(HighlightGroup.KEYWORD_CONTROL,       TokenType.KEYWORD);
        m.put(HighlightGroup.KEYWORD_OPERATOR,      TokenType.KEYWORD);
        m.put(HighlightGroup.KEYWORD_FUNCTION,      TokenType.KEYWORD);
        m.put(HighlightGroup.KEYWORD_TYPE,          TokenType.KEYWORD);
        m.put(HighlightGroup.KEYWORD_MODIFIER,      TokenType.KEYWORD);
        m.put(HighlightGroup.OPERATOR,              TokenType.DEFAULT);
        m.put(HighlightGroup.PUNCTUATION_BRACKET,   TokenType.DEFAULT);
        m.put(HighlightGroup.PUNCTUATION_DELIMITER, TokenType.DEFAULT);
        m.put(HighlightGroup.PUNCTUATION_SPECIAL,   TokenType.DEFAULT);
        m.put(HighlightGroup.STRING,                TokenType.STRING);
        m.put(HighlightGroup.STRING_ESCAPE,         TokenType.STRING);
        m.put(HighlightGroup.STRING_SPECIAL,        TokenType.STRING);
        m.put(HighlightGroup.NUMBER,                TokenType.LITERAL);
        m.put(HighlightGroup.BOOLEAN,               TokenType.LITERAL);
        m.put(HighlightGroup.COMMENT,               TokenType.COMMENT);
        m.put(HighlightGroup.COMMENT_DOC,           TokenType.COMMENT);
        m.put(HighlightGroup.ATTRIBUTE,             TokenType.KEYWORD);
        m.put(HighlightGroup.LABEL,                 TokenType.DEFAULT);
        m.put(HighlightGroup.EMBEDDED,              TokenType.DEFAULT);
        m.put(HighlightGroup.UNKNOWN,               TokenType.DEFAULT);

        GROUP_TO_TOKEN = m;
    }

    private final ScriptDocument document;

    public TreeSitterMarkBuilder(ScriptDocument document) {
        this.document = document;
    }

    /**
     * Parses source with tree-sitter, runs highlights.scm, and returns marks.
     * This replaces the old regex-based passes for strings, comments, keywords,
     * numbers, modifiers, and declaration coloring.
     */
    public List<ScriptLine.Mark> buildMarks(String sourceText) {
        List<ScriptLine.Mark> marks = new ArrayList<>();
        if (sourceText == null || sourceText.isEmpty()) {
            return marks;
        }

        JavaParser parser = getSharedParser();
        SyntaxTree tree;
        try {
            tree = parser.parse(sourceText);
            System.out.println(SyntaxTreeDebugger.printTree(tree));
        } catch (Exception e) {
            return marks;
        }
        
        

        try {
            List<HighlightCapture> captures = ENGINE.highlights(tree, sourceText);

            for (HighlightCapture capture : captures) {
                TokenType tokenType = mapAndRefine(capture, sourceText);
                if (tokenType == null) {
                    continue;
                }

                TextSpan span = capture.getSpan();
                int start = span.getStartByte();
                int end = span.getEndByte();

                if (start < 0 || end <= start || end > sourceText.length()) {
                    continue;
                }

                marks.add(new ScriptLine.Mark(start, end, tokenType));
            }
        } finally {
            tree.close();
        }

        return marks;
    }

    private TokenType mapAndRefine(HighlightCapture capture, String sourceText) {
        TokenType base = GROUP_TO_TOKEN.getOrDefault(capture.getGroup(), TokenType.DEFAULT);
        return refineByNodeContext(capture, base, sourceText);
    }

    /**
     * Disambiguates captures where a single HighlightGroup maps to different
     * visual styles based on CST position. Mirrors Zed's multi-pattern approach.
     */
    private TokenType refineByNodeContext(HighlightCapture capture, TokenType base,
                                          String sourceText) {
        String nodeType = capture.getNodeType();
        HighlightGroup group = capture.getGroup();

        switch (group) {
            case FUNCTION:
              return TokenType.METHOD_DECL;
            case TYPE:
                if ("identifier".equals(nodeType)) {
                    return TokenType.CLASS_DECL;
                }
                return TokenType.IMPORTED_CLASS;
            case ENUM:
                return TokenType.ENUM_DECL;

            case CONSTANT:
                if ("identifier".equals(nodeType)) {
                    return TokenType.STATIC_FINAL_FIELD;
                }
                return TokenType.ENUM_CONSTANT;

            case CONSTANT_BUILTIN:
                return TokenType.LITERAL;

            case CONSTRUCTOR:
                return TokenType.METHOD_DECL;

            case ATTRIBUTE:
                return TokenType.KEYWORD;

            case BOOLEAN:
                return TokenType.LITERAL;

            case COMMENT_DOC:
                return TokenType.COMMENT;

            default:
                break;
        }

        return base;
    }

    private static JavaParser getSharedParser() {
        JavaParser local = sharedParser;
        if (local == null) {
            synchronized (PARSER_LOCK) {
                local = sharedParser;
                if (local == null) {
                    local = new JavaParser();
                    sharedParser = local;
                }
            }
        }
        return local;
    }

    /** Releases the shared parser's native resources. Call during application shutdown. */
    public static void shutdown() {
        synchronized (PARSER_LOCK) {
            if (sharedParser != null) {
                sharedParser.close();
                sharedParser = null;
            }
        }
    }
}
