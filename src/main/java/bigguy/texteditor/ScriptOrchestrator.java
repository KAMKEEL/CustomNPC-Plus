package bigguy.texteditor;

import bigguy.texteditor.analysis.IncrementalStrategy;
import bigguy.texteditor.analysis.SemanticAnalyzer;
import bigguy.texteditor.analysis.SemanticMarkBuilder;
import bigguy.texteditor.diagnostics.Diagnostic;
import bigguy.texteditor.diagnostics.DiagnosticStore;
import bigguy.texteditor.render.Mark;
import bigguy.texteditor.render.MarkLayer;
import bigguy.texteditor.render.RenderLine;
import bigguy.texteditor.render.RenderModel;
import bigguy.texteditor.semantics.SemanticModel;
import bigguy.texteditor.syntax.DocumentSyntax;
import bigguy.texteditor.syntax.SemanticNode;
import bigguy.texteditor.treesitter.java.HighlightCapture;
import bigguy.texteditor.treesitter.java.HighlightGroup;
import bigguy.texteditor.treesitter.java.JavaParser;
import bigguy.texteditor.treesitter.java.JavaQueryEngine;
import bigguy.texteditor.treesitter.java.SyntaxTree;
import bigguy.texteditor.treesitter.java.TextSpan;
import noppes.npcs.client.gui.util.script.interpreter.token.TokenType;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Central pipeline coordinator for the script editor.
 * Replaces ScriptDocument's coordination role with a clean,
 * modular pipeline following IntelliJ IDEA's architecture.
 *
 * <h3>Pipeline</h3>
 * <pre>{@code
 * onTextChanged(text)
 *   → parser.parse(text) → SyntaxTree
 *   → DocumentSyntax.create(tree, version)
 *   → queryEngine.highlights(tree, text) → HighlightCaptures
 *   → convertHighlightsToMarks(captures, text) → List<Mark>
 *   → markLayer.setSyntaxMarks(marks)
 *   → markLayer.merge() → mergedMarks
 *   → renderModel.rebuild(text, mergedMarks, lineHeight)
 *   → state = SYNTAX_READY
 *   → semanticAnalyzer.scheduleAnalysis(syntax, rev, callback)
 *   → state = ANALYZING
 *   → (background) onAnalysisComplete → state = SMART
 *                   onAnalysisFailed  → state = DEGRADED
 * }</pre>
 *
 * <h3>Thread model</h3>
 * <ul>
 *   <li>Syntax highlighting runs on the UI thread (fast, &lt;5ms)</li>
 *   <li>Semantic analysis runs on a background daemon thread via {@link SemanticAnalyzer}</li>
 *   <li>Analysis callbacks fire on the background thread, updating {@code volatile state}</li>
 *   <li>State transitions are atomic via volatile {@link ReadinessState}</li>
 * </ul>
 *
 * <h3>State machine</h3>
 * <pre>{@code
 * UNINITIALIZED → SYNTAX_READY  (first onTextChanged)
 * SYNTAX_READY  → ANALYZING     (analysis scheduled)
 * ANALYZING     → SMART         (analysis complete, revision matches)
 * ANALYZING     → DEGRADED      (analysis failed, revision matches)
 * ANALYZING     → SYNTAX_READY  (new text change arrives)
 * SMART         → SYNTAX_READY  (text changed again)
 * DEGRADED      → SYNTAX_READY  (text changed again)
 * }</pre>
 */
public class ScriptOrchestrator implements SemanticAnalyzer.AnalysisCallback {

    // --- Error underline colors (ARGB) ---
    private static final int ERROR_COLOR = 0xFFFF5555;
    private static final int WARNING_COLOR = 0xFFFFAA00;
    private static final int INFO_COLOR = 0xFF5555FF;

    // --- Core state ---
    private volatile ReadinessState state = ReadinessState.UNINITIALIZED;
    private final AtomicLong revision = new AtomicLong(0);

    // --- Syntax layer ---
    private final JavaParser parser;
    private final JavaQueryEngine queryEngine;
    private volatile DocumentSyntax currentSyntax;

    // --- Render pipeline ---
    private final MarkLayer markLayer;
    private final RenderModel renderModel;
    private final DiagnosticStore diagnosticStore;

    // --- Configuration ---
    private int lineHeight = 10;

    // --- Semantic analysis ---
    private final SemanticAnalyzer semanticAnalyzer;
    private final IncrementalStrategy incrementalStrategy;
    private TextSpan[] lastChangedRanges;
    private volatile String currentSourceText = "";

    /**
     * Creates a new orchestrator with default-configured components.
     *
     * <p>Initializes the tree-sitter parser, query engine, mark layer,
     * render model, diagnostic store, semantic analyzer, and incremental
     * strategy. All components start empty; call
     * {@link #onTextChanged(String)} to kick off the pipeline.</p>
     */
    public ScriptOrchestrator() {
        this.parser = new JavaParser();
        this.queryEngine = new JavaQueryEngine();
        this.markLayer = new MarkLayer();
        this.renderModel = new RenderModel();
        this.diagnosticStore = new DiagnosticStore();
        this.semanticAnalyzer = new SemanticAnalyzer();
        this.incrementalStrategy = new IncrementalStrategy();
    }

    // ==================== MAIN PIPELINE ====================

    /**
     * Called when the source text changes. Runs the syntax pipeline
     * synchronously on the UI thread (fast path), then schedules
     * background semantic analysis.
     *
     * <p>Any in-flight analysis from a previous edit is cancelled before
     * scheduling a new one, ensuring stale results are never applied.</p>
     *
     * @param text the complete source text of the document
     */
    public void onTextChanged(String text) {
        long rev = revision.incrementAndGet();

        // Cancel any in-flight analysis from the previous edit
        semanticAnalyzer.cancelInFlight();

        // Step 1: Parse CST
        SyntaxTree syntaxTree = parser.parse(text);

        // Step 2: Create DocumentSyntax
        currentSyntax = DocumentSyntax.create(syntaxTree, (int) rev);

        // Step 3: Collect parser errors as diagnostics
        diagnosticStore.clearBySource(Diagnostic.Source.PARSER);
        collectParserErrors(currentSyntax);

        // Step 4: Extract syntax highlights from tree-sitter
        List<HighlightCapture> highlights = queryEngine.highlights(syntaxTree, text);

        // Step 5: Convert highlights to Mark objects
        List<Mark> syntaxMarks = convertHighlightsToMarks(highlights, text);

        // Step 6: Set syntax marks in the mark layer
        markLayer.setSyntaxMarks(syntaxMarks);

        // Step 7: Merge marks (syntax + any prior semantic marks)
        List<Mark> mergedMarks = markLayer.merge();

        // Step 8: Rebuild render model from source + merged marks
        currentSourceText = text;
        renderModel.rebuild(text, mergedMarks, lineHeight);

        // Step 9: Syntax pipeline complete
        state = ReadinessState.SYNTAX_READY;

        // Step 10: Schedule background semantic analysis
        state = ReadinessState.ANALYZING;
        semanticAnalyzer.scheduleAnalysis(currentSyntax, rev, this);
    }

    // ==================== ANALYSIS CALLBACKS ====================

    @Override
    public void onAnalysisComplete(long revision, SemanticModel semanticModel, DocumentSyntax syntax) {
        if (revision != this.revision.get()) {
            return;
        }

        List<Mark> semanticMarks = SemanticMarkBuilder.build(semanticModel, syntax);
        markLayer.setSemanticMarks(semanticMarks);
        List<Mark> mergedMarks = markLayer.merge();
        renderModel.rebuild(currentSourceText, mergedMarks, lineHeight);

        state = ReadinessState.SMART;
    }

    @Override
    public void onAnalysisFailed(long revision, Exception error) {
        if (revision != this.revision.get()) {
            return;
        }

        state = ReadinessState.DEGRADED;
    }

    // ==================== HIGHLIGHT → MARK CONVERSION ====================

    /**
     * Converts tree-sitter {@link HighlightCapture}s into {@link Mark} objects.
     * Maps each {@link HighlightGroup} to a {@link TokenType} for rendering.
     *
     * @param highlights the highlight captures from the query engine
     * @param text       the complete source text (used for bounds validation)
     * @return a list of syntax marks, one per highlight capture
     */
    private List<Mark> convertHighlightsToMarks(List<HighlightCapture> highlights, String text) {
        if (highlights.isEmpty()) {
            return Collections.emptyList();
        }
        int textLength = text.length();
        List<Mark> marks = new ArrayList<>(highlights.size());
        for (HighlightCapture capture : highlights) {
            TextSpan span = capture.getSpan();
            if (span == null) {
                continue;
            }
            int startByte = span.getStartByte();
            int endByte = span.getEndByte();
            if (startByte < 0 || endByte <= startByte || startByte >= textLength) {
                continue;
            }
            if (endByte > textLength) {
                endByte = textLength;
            }
            TokenType tokenType = mapHighlightGroupToTokenType(capture.getGroup());
            marks.add(Mark.syntax(startByte, endByte, tokenType));
        }
        return marks;
    }

    /**
     * Maps a tree-sitter {@link HighlightGroup} to the rendering
     * {@link TokenType}. Every group is explicitly mapped — no fallthrough.
     *
     * <p>The mapping aims to match IntelliJ-style colorization:</p>
     * <ul>
     *   <li>Language keywords and control flow → {@link TokenType#KEYWORD}</li>
     *   <li>Type references and declarations → {@link TokenType#TYPE_DECL} / specific decl types</li>
     *   <li>Function/method names → {@link TokenType#METHOD_DECL} or {@link TokenType#METHOD_CALL}</li>
     *   <li>Strings, numbers, booleans → their respective literal types</li>
     *   <li>Comments → {@link TokenType#COMMENT}</li>
     *   <li>Punctuation/operators → {@link TokenType#DEFAULT} (white, lowest priority)</li>
     * </ul>
     *
     * @param group the highlight group from tree-sitter
     * @return the corresponding token type for rendering
     */
    private static TokenType mapHighlightGroupToTokenType(HighlightGroup group) {
        switch (group) {
            // --- Properties and variables ---
            case PROPERTY:
                return TokenType.GLOBAL_FIELD;
            case VARIABLE:
                return TokenType.VARIABLE;
            case PARAMETER:
                return TokenType.PARAMETER;

            // --- Functions / methods ---
            case FUNCTION:
                return TokenType.METHOD_DECL;
            case FUNCTION_CALL:
                return TokenType.METHOD_CALL;
            case SUPER:
                // "super" is function.builtin in tree-sitter — render as keyword
                return TokenType.KEYWORD;
            case CONSTRUCTOR:
                return TokenType.METHOD_DECL;

            // --- Types ---
            case TYPE:
                return TokenType.TYPE_DECL;
            case TYPE_PARAMETER:
                return TokenType.GENERIC_TYPE_PARAM;
            case INTERFACE:
                return TokenType.INTERFACE_DECL;
            case TYPE_BUILTIN:
                // Primitive types (int, boolean, void) — styled like keywords
                return TokenType.KEYWORD;
            case ENUM:
                return TokenType.ENUM_DECL;

            // --- Constants ---
            case CONSTANT:
                return TokenType.STATIC_FINAL_FIELD;
            case CONSTANT_BUILTIN:
                // true, false, null
                return TokenType.LITERAL;

            // --- Keywords (all keyword.* variants) ---
            case KEYWORD:
            case KEYWORD_CONTROL:
            case KEYWORD_OPERATOR:
            case KEYWORD_FUNCTION:
            case KEYWORD_TYPE:
            case KEYWORD_MODIFIER:
                return TokenType.KEYWORD;

            // --- Operators ---
            case OPERATOR:
                return TokenType.DEFAULT;

            // --- Strings ---
            case STRING:
            case STRING_ESCAPE:
            case STRING_SPECIAL:
                return TokenType.STRING;

            // --- Numeric and boolean literals ---
            case NUMBER:
                return TokenType.LITERAL;
            case BOOLEAN:
                return TokenType.LITERAL;

            // --- Comments ---
            case COMMENT:
                return TokenType.COMMENT;
            case COMMENT_DOC:
                return TokenType.COMMENT;

            // --- Annotations ---
            case ATTRIBUTE:
                // Java annotations (@Override, @Test) — styled like JSDoc tags
                return TokenType.JSDOC_TAG;

            // --- Labels ---
            case LABEL:
                return TokenType.KEYWORD;

            // --- Punctuation ---
            case PUNCTUATION_BRACKET:
            case PUNCTUATION_DELIMITER:
            case PUNCTUATION_SPECIAL:
                return TokenType.DEFAULT;

            // --- Embedded / unknown ---
            case EMBEDDED:
                return TokenType.DEFAULT;
            case UNKNOWN:
                return TokenType.DEFAULT;

            default:
                return TokenType.DEFAULT;
        }
    }

    // ==================== PARSER ERROR COLLECTION ====================

    /**
     * Walks the CST root and collects all ERROR and MISSING nodes as
     * parser diagnostics in the {@link DiagnosticStore}.
     *
     * <p>ERROR nodes represent unparseable source regions. MISSING nodes
     * represent tokens the parser expected but did not find (e.g. a
     * missing semicolon).</p>
     *
     * @param syntax the document syntax to scan for errors
     */
    private void collectParserErrors(DocumentSyntax syntax) {
        SemanticNode root = syntax.root();
        if (root == null || !root.hasError()) {
            return;
        }
        collectParserErrorsRecursive(root);
    }

    /**
     * Recursively walks the semantic node tree, creating diagnostics
     * for any ERROR or MISSING nodes encountered.
     *
     * @param node the current node being visited
     */
    private void collectParserErrorsRecursive(SemanticNode node) {
        if (node.isError()) {
            TextSpan span = node.span();
            if (span != null && span.getByteLength() > 0) {
                String text = node.text();
                String message = "Syntax error" + (text != null && !text.isEmpty()
                        ? ": unexpected '" + truncate(text, 30) + "'"
                        : "");
                diagnosticStore.addDiagnostic(Diagnostic.of(
                        span, Diagnostic.Severity.ERROR, message, Diagnostic.Source.PARSER));
            }
            return;
        }
        if (node.isMissing()) {
            TextSpan span = node.span();
            if (span != null) {
                String nodeType = node.nodeType();
                String message = "Missing " + (nodeType != null ? "'" + nodeType + "'" : "token");
                diagnosticStore.addDiagnostic(Diagnostic.of(
                        span, Diagnostic.Severity.ERROR, message, Diagnostic.Source.PARSER));
            }
            return;
        }
        int childCount = node.childCount();
        for (int i = 0; i < childCount; i++) {
            SemanticNode child = node.child(i);
            if (child != null && child.hasError()) {
                collectParserErrorsRecursive(child);
            }
        }
    }

    // ==================== STATE QUERIES ====================

    /**
     * Returns the current readiness state of the pipeline.
     *
     * @return the current {@link ReadinessState}
     */
    public ReadinessState getReadinessState() {
        return state;
    }

    /**
     * Returns the monotonically increasing revision counter.
     * Incremented on every {@link #onTextChanged(String)} call.
     *
     * @return the current revision number
     */
    public long getRevision() {
        return revision.get();
    }

    // ==================== COMPONENT ACCESS ====================

    /**
     * Returns the render model containing the line layout and viewport calculations.
     *
     * @return the render model, never {@code null}
     */
    public RenderModel getRenderModel() {
        return renderModel;
    }

    /**
     * Returns the mark layer used for syntax/semantic/error mark merging.
     *
     * @return the mark layer, never {@code null}
     */
    public MarkLayer getMarkLayer() {
        return markLayer;
    }

    /**
     * Returns the diagnostic store containing all parser and analysis errors.
     *
     * @return the diagnostic store, never {@code null}
     */
    public DiagnosticStore getDiagnosticStore() {
        return diagnosticStore;
    }

    /**
     * Returns the current document syntax, or {@code null} if no parse
     * has been performed yet.
     *
     * @return the latest {@link DocumentSyntax}, or {@code null}
     */
    public DocumentSyntax getCurrentSyntax() {
        return currentSyntax;
    }

    /**
     * Returns the current semantic model from the analyzer.
     * May be {@code null} or unbuilt if no analysis has completed yet.
     *
     * @return the latest {@link SemanticModel}, or {@code null}
     */
    public SemanticModel getSemanticModel() {
        return semanticAnalyzer.getSemanticModel();
    }

    // ==================== CONFIGURATION ====================

    /**
     * Sets the pixel height per line used for render model layout.
     *
     * @param lineHeight pixel height per line (must be positive)
     */
    public void setLineHeight(int lineHeight) {
        this.lineHeight = lineHeight;
    }

    /**
     * Returns the current pixel height per line.
     *
     * @return the line height in pixels
     */
    public int getLineHeight() {
        return lineHeight;
    }

    // ==================== DIAGNOSTIC HELPERS ====================

    /**
     * Returns diagnostic ranges for a specific render line, suitable for
     * passing to {@link RenderLine#drawErrorUnderlines(int, int, List)}.
     *
     * <p>Queries the {@link DiagnosticStore} for diagnostics overlapping the
     * line's byte range, then converts each to a
     * {@link RenderLine.DiagnosticRange} with an appropriate underline color
     * based on severity.</p>
     *
     * @param line the render line to query diagnostics for
     * @return a list of diagnostic ranges, possibly empty, never {@code null}
     */
    public List<RenderLine.DiagnosticRange> getDiagnosticsForLine(RenderLine line) {
        if (line == null) {
            return Collections.emptyList();
        }
        List<Diagnostic> lineDiagnostics = diagnosticStore.getDiagnosticsInRange(
                line.getGlobalStart(), line.getGlobalEnd());
        if (lineDiagnostics.isEmpty()) {
            return Collections.emptyList();
        }
        List<RenderLine.DiagnosticRange> ranges = new ArrayList<>(lineDiagnostics.size());
        for (Diagnostic diag : lineDiagnostics) {
            TextSpan range = diag.range();
            int color = diagnosticColor(diag.severity());
            ranges.add(new RenderLine.DiagnosticRange(
                    range.getStartByte(), range.getEndByte(), color));
        }
        return ranges;
    }

    /**
     * Returns the ARGB underline color for a diagnostic severity level.
     *
     * @param severity the diagnostic severity
     * @return the ARGB color for the wavy underline
     */
    private static int diagnosticColor(Diagnostic.Severity severity) {
        switch (severity) {
            case ERROR:
                return ERROR_COLOR;
            case WARNING:
                return WARNING_COLOR;
            case INFO:
            case HINT:
                return INFO_COLOR;
            default:
                return ERROR_COLOR;
        }
    }

    // ==================== LIFECYCLE ====================

    /**
     * Releases all resources held by this orchestrator.
     * Called when the editor is closed.
     *
     * <p>Disposes the semantic analyzer (cancels in-flight analysis and shuts
     * down its executor), closes the tree-sitter parser (releasing native
     * memory), clears the mark layer, diagnostic store, and nullifies the
     * current syntax reference.</p>
     */
    public void dispose() {
        semanticAnalyzer.dispose();
        parser.close();
        markLayer.clear();
        diagnosticStore.clear();
        currentSyntax = null;
        state = ReadinessState.UNINITIALIZED;
    }

    // ==================== INTERNAL UTILITIES ====================

    /**
     * Truncates a string to the given maximum length, appending "..." if truncated.
     *
     * @param text      the string to truncate
     * @param maxLength the maximum length before truncation
     * @return the original string if short enough, or a truncated version
     */
    private static String truncate(String text, int maxLength) {
        if (text.length() <= maxLength) {
            return text;
        }
        return text.substring(0, maxLength) + "...";
    }

    @Override
    public String toString() {
        return "ScriptOrchestrator{state=" + state
                + ", revision=" + revision.get()
                + ", syntax=" + (currentSyntax != null ? currentSyntax : "null")
                + ", renderModel=" + renderModel
                + ", diagnostics=" + diagnosticStore
                + '}';
    }
}
