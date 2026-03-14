package bigguy.texteditor.treesitter.java;

import org.treesitter.*;

import java.util.*;

/**
 * Executes tree-sitter queries against parsed Java syntax trees.
 *
 * <p>This is the Java equivalent of Zed's syntax_map.rs query application
 * pipeline. It takes a compiled query and a syntax tree, runs the query,
 * and produces typed result objects ({@link HighlightCapture},
 * {@link OutlineEntry}, {@link RunnableEntry}).</p>
 *
 * <p>Thread safety: instances are stateless and can be shared across threads.
 * The underlying {@link TSQueryCursor} is created per-invocation.</p>
 */
public final class JavaQueryEngine {

    private final JavaGrammar grammar;

    public JavaQueryEngine() {
        this(JavaGrammar.getInstance());
    }

    public JavaQueryEngine(JavaGrammar grammar) {
        this.grammar = grammar;
    }

    /**
     * Runs the highlight query against a syntax tree and returns capture results.
     *
     * @param tree       the parsed syntax tree
     * @param sourceText the source text (used for predicate evaluation)
     * @return ordered list of highlight captures, from earliest to latest
     */
    public List<HighlightCapture> highlights(SyntaxTree tree, String sourceText) {
        TSQuery query = grammar.compileQuery(QueryPatterns.HIGHLIGHTS);
        List<HighlightCapture> results = new ArrayList<>();

        try (TSQueryCursor cursor = new TSQueryCursor()) {
            TSNode rootNode = tree.unwrap().getRootNode();
            cursor.exec(query, rootNode, sourceText);
            TSQueryMatch match = new TSQueryMatch();

            while (cursor.nextCapture(match)) {
                for (TSQueryCapture capture : match.getCaptures()) {
                    TSNode node = capture.getNode();
                    if (node == null || node.isNull()) continue;

                    String captureName = query.getCaptureNameForId(capture.getIndex());
                    if (captureName.startsWith("_")) continue;

                    HighlightGroup group = HighlightGroup.fromCaptureName(captureName);
                    TextSpan span = spanFromNode(node);
                    results.add(new HighlightCapture(span, group, node.getType(), match.getPatternIndex()));
                }
            }
        }

        return results;
    }

    /**
     * Runs the outline query against a syntax tree and returns symbol entries.
     *
     * @param tree       the parsed syntax tree
     * @param sourceText the source text
     * @return ordered list of outline entries
     */
    public List<OutlineEntry> outline(SyntaxTree tree, String sourceText) {
        TSQuery query = grammar.compileQuery(QueryPatterns.OUTLINE);
        List<OutlineEntry> results = new ArrayList<>();

        try (TSQueryCursor cursor = new TSQueryCursor()) {
            TSNode rootNode = tree.unwrap().getRootNode();
            cursor.exec(query, rootNode, sourceText);
            TSQueryMatch match = new TSQueryMatch();

            while (cursor.nextMatch(match)) {
                String name = null;
                TextSpan nameSpan = null;
                TextSpan bodySpan = null;
                List<String> contextKeywords = new ArrayList<>();
                String itemType = null;

                for (TSQueryCapture capture : match.getCaptures()) {
                    TSNode node = capture.getNode();
                    if (node == null || node.isNull()) continue;

                    String captureName = query.getCaptureNameForId(capture.getIndex());

                    switch (captureName) {
                        case "name":
                            name = extractNodeText(node, sourceText);
                            nameSpan = spanFromNode(node);
                            break;
                        case "item":
                            bodySpan = spanFromNode(node);
                            itemType = node.getType();
                            break;
                        case "context":
                            String ctxText = extractNodeText(node, sourceText);
                            if (ctxText != null && !ctxText.isEmpty()) {
                                contextKeywords.add(ctxText);
                            }
                            break;
                    }
                }

                if (name != null && bodySpan != null) {
                    OutlineEntry.Kind kind = resolveOutlineKind(itemType);
                    int depth = computeDepth(bodySpan, results);
                    results.add(new OutlineEntry(name, kind, nameSpan, bodySpan, contextKeywords, depth));
                }
            }
        }

        return results;
    }

    /**
     * Runs the runnables query against a syntax tree and returns detected run targets.
     *
     * @param tree       the parsed syntax tree
     * @param sourceText the source text
     * @return list of runnable entries (main methods, test methods, test classes)
     */
    public List<RunnableEntry> runnables(SyntaxTree tree, String sourceText) {
        TSQuery query = grammar.compileQuery(QueryPatterns.RUNNABLES);
        List<RunnableEntry> results = new ArrayList<>();

        try (TSQueryCursor cursor = new TSQueryCursor()) {
            TSNode rootNode = tree.unwrap().getRootNode();
            cursor.exec(query, rootNode, sourceText);
            TSQueryMatch match = new TSQueryMatch();

            while (cursor.nextMatch(match)) {
                String className = null;
                String methodName = null;
                String packageName = null;
                TextSpan runSpan = null;

                for (TSQueryCapture capture : match.getCaptures()) {
                    TSNode node = capture.getNode();
                    if (node == null || node.isNull()) continue;

                    String captureName = query.getCaptureNameForId(capture.getIndex());
                    switch (captureName) {
                        case "java_class_name":
                            className = extractNodeText(node, sourceText);
                            break;
                        case "java_method_name":
                            methodName = extractNodeText(node, sourceText);
                            break;
                        case "java_package_name":
                            packageName = extractNodeText(node, sourceText);
                            break;
                        case "run":
                            runSpan = spanFromNode(node);
                            break;
                    }
                }

                Map<String, String> metadata = match.getMetadata();
                String tagValue = metadata != null ? metadata.get("tag") : null;
                RunnableEntry.Tag tag = tagValue != null
                        ? RunnableEntry.Tag.fromValue(tagValue)
                        : RunnableEntry.Tag.UNKNOWN;

                if (runSpan != null && tag != RunnableEntry.Tag.UNKNOWN) {
                    results.add(new RunnableEntry(tag, runSpan, className, methodName,
                            packageName, metadata != null ? new HashMap<>(metadata) : null));
                }
            }
        }

        return results;
    }

    /**
     * Runs the locals query to extract scope and definition information.
     *
     * @param tree       the parsed syntax tree
     * @param sourceText the source text
     * @return raw capture results as name→span pairs
     */
    public List<LocalCapture> locals(SyntaxTree tree, String sourceText) {
        TSQuery query = grammar.compileQuery(QueryPatterns.LOCALS);
        List<LocalCapture> results = new ArrayList<>();

        try (TSQueryCursor cursor = new TSQueryCursor()) {
            TSNode rootNode = tree.unwrap().getRootNode();
            cursor.exec(query, rootNode, sourceText);
            TSQueryMatch match = new TSQueryMatch();

            while (cursor.nextCapture(match)) {
                for (TSQueryCapture capture : match.getCaptures()) {
                    TSNode node = capture.getNode();
                    if (node == null || node.isNull()) continue;

                    String captureName = query.getCaptureNameForId(capture.getIndex());
                    String text = extractNodeText(node, sourceText);
                    results.add(new LocalCapture(captureName, text, spanFromNode(node), node.getType()));
                }
            }
        }

        return results;
    }

    /**
     * Runs a custom query against a syntax tree.
     *
     * @param tree        the parsed syntax tree
     * @param sourceText  the source text
     * @param querySource the S-expression query pattern
     * @return raw capture results
     */
    public List<RawCapture> executeQuery(SyntaxTree tree, String sourceText, String querySource) {
        TSQuery query = grammar.compileQuery(querySource);
        List<RawCapture> results = new ArrayList<>();

        try (TSQueryCursor cursor = new TSQueryCursor()) {
            TSNode rootNode = tree.unwrap().getRootNode();
            cursor.exec(query, rootNode, sourceText);
            TSQueryMatch match = new TSQueryMatch();

            while (cursor.nextCapture(match)) {
                for (TSQueryCapture capture : match.getCaptures()) {
                    TSNode node = capture.getNode();
                    if (node == null || node.isNull()) continue;

                    String captureName = query.getCaptureNameForId(capture.getIndex());
                    String text = extractNodeText(node, sourceText);
                    Map<String, String> meta = match.getMetadata();
                    results.add(new RawCapture(captureName, text, spanFromNode(node),
                            node.getType(), match.getPatternIndex(),
                            meta != null ? new HashMap<>(meta) : Collections.<String, String>emptyMap()));
                }
            }
        }

        return results;
    }

    private static TextSpan spanFromNode(TSNode node) {
        TSPoint start = node.getStartPoint();
        TSPoint end = node.getEndPoint();
        return new TextSpan(
                start.getRow(), start.getColumn(),
                end.getRow(), end.getColumn(),
                node.getStartByte(), node.getEndByte()
        );
    }

    private static String extractNodeText(TSNode node, String sourceText) {
        if (sourceText == null || node == null || node.isNull()) return null;
        int start = node.getStartByte();
        int end = Math.min(node.getEndByte(), sourceText.length());
        if (start < 0 || start >= end) return "";
        return sourceText.substring(start, end);
    }

    private static OutlineEntry.Kind resolveOutlineKind(String nodeType) {
        if (nodeType == null) return OutlineEntry.Kind.UNKNOWN;
        switch (nodeType) {
            case "class_declaration": return OutlineEntry.Kind.CLASS;
            case "interface_declaration": return OutlineEntry.Kind.INTERFACE;
            case "enum_declaration": return OutlineEntry.Kind.ENUM;
            case "record_declaration": return OutlineEntry.Kind.RECORD;
            case "annotation_type_declaration": return OutlineEntry.Kind.ANNOTATION_TYPE;
            case "method_declaration": return OutlineEntry.Kind.METHOD;
            case "constructor_declaration": return OutlineEntry.Kind.CONSTRUCTOR;
            case "field_declaration": return OutlineEntry.Kind.FIELD;
            case "enum_constant": return OutlineEntry.Kind.ENUM_CONSTANT;
            case "static_initializer": return OutlineEntry.Kind.STATIC_INITIALIZER;
            default: return OutlineEntry.Kind.UNKNOWN;
        }
    }

    private static int computeDepth(TextSpan bodySpan, List<OutlineEntry> existingEntries) {
        int depth = 0;
        for (OutlineEntry existing : existingEntries) {
            if (existing.getBodySpan() != null && existing.getBodySpan().contains(bodySpan)) {
                depth = Math.max(depth, existing.getDepth() + 1);
            }
        }
        return depth;
    }

    /**
     * Raw capture result from a locals query.
     */
    public static final class LocalCapture {
        private final String captureName;
        private final String text;
        private final TextSpan span;
        private final String nodeType;

        public LocalCapture(String captureName, String text, TextSpan span, String nodeType) {
            this.captureName = captureName;
            this.text = text;
            this.span = span;
            this.nodeType = nodeType;
        }

        public String getCaptureName() { return captureName; }
        public String getText() { return text; }
        public TextSpan getSpan() { return span; }
        public String getNodeType() { return nodeType; }

        @Override
        public String toString() {
            return String.format("Local{%s '%s' %s}", captureName, text, span);
        }
    }

    /**
     * Generic capture result from a custom query execution.
     */
    public static final class RawCapture {
        private final String captureName;
        private final String text;
        private final TextSpan span;
        private final String nodeType;
        private final int patternIndex;
        private final Map<String, String> metadata;

        public RawCapture(String captureName, String text, TextSpan span,
                          String nodeType, int patternIndex, Map<String, String> metadata) {
            this.captureName = captureName;
            this.text = text;
            this.span = span;
            this.nodeType = nodeType;
            this.patternIndex = patternIndex;
            this.metadata = Collections.unmodifiableMap(metadata);
        }

        public String getCaptureName() { return captureName; }
        public String getText() { return text; }
        public TextSpan getSpan() { return span; }
        public String getNodeType() { return nodeType; }
        public int getPatternIndex() { return patternIndex; }
        public Map<String, String> getMetadata() { return metadata; }

        @Override
        public String toString() {
            return String.format("Raw{%s '%s' %s node=%s}", captureName, text, span, nodeType);
        }
    }
}
