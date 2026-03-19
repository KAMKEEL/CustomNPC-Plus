package bigguy.texteditor.diagnostics;

import bigguy.texteditor.treesitter.java.TextSpan;

import java.util.Objects;

/**
 * An immutable diagnostic (error, warning, info, or hint) anchored to a range
 * in the source document. Replaces the legacy {@code DocumentError} class and
 * the misused {@code AssignmentInfo}-as-error pattern.
 *
 * <p>Severity levels and source categories follow the Language Server Protocol
 * specification to ease future LSP integration.</p>
 */
public final class Diagnostic {

    /**
     * Diagnostic severity, following the LSP {@code DiagnosticSeverity} enumeration.
     */
    public enum Severity {
        /** A problem that prevents compilation or execution. */
        ERROR(1),
        /** A potential issue that does not prevent compilation. */
        WARNING(2),
        /** Informational message (e.g. deprecation notice). */
        INFO(3),
        /** Subtle suggestion (e.g. unused import). */
        HINT(4);

        private final int lspValue;

        Severity(int lspValue) {
            this.lspValue = lspValue;
        }

        /**
         * Returns the numeric value matching the LSP specification.
         *
         * @return the LSP {@code DiagnosticSeverity} integer (1–4)
         */
        public int lspValue() {
            return lspValue;
        }
    }

    /**
     * Identifies which analysis phase produced the diagnostic, enabling
     * targeted invalidation when only part of the pipeline reruns.
     */
    public enum Source {
        /** Syntax errors from the tree-sitter parser. */
        PARSER,
        /** Declaration-level issues (duplicate names, missing types). */
        DECLARATION,
        /** Type-checking errors (incompatible assignment, wrong argument type). */
        TYPE_CHECK,
        /** Import resolution issues (unresolved import, wildcard ambiguity). */
        IMPORT,
        /** Higher-level semantic checks (unreachable code, unused variable). */
        SEMANTIC
    }

    private final TextSpan range;
    private final Severity severity;
    private final String message;
    private final Source source;
    private final String code;

    /**
     * @param range    the source range this diagnostic applies to
     * @param severity the severity level
     * @param message  a human-readable description of the issue
     * @param source   the analysis phase that produced this diagnostic
     * @param code     an optional machine-readable error code (may be {@code null})
     */
    public Diagnostic(TextSpan range, Severity severity, String message, Source source, String code) {
        this.range = Objects.requireNonNull(range, "range");
        this.severity = Objects.requireNonNull(severity, "severity");
        this.message = Objects.requireNonNull(message, "message");
        this.source = Objects.requireNonNull(source, "source");
        this.code = code;
    }

    /**
     * Convenience factory for diagnostics without an error code.
     *
     * @param range    the source range
     * @param severity the severity level
     * @param message  a human-readable description
     * @param source   the originating analysis phase
     * @return a new diagnostic with a {@code null} error code
     */
    public static Diagnostic of(TextSpan range, Severity severity, String message, Source source) {
        return new Diagnostic(range, severity, message, source, null);
    }

    /** @return the source range this diagnostic applies to */
    public TextSpan range() { return range; }

    /** @return the severity level */
    public Severity severity() { return severity; }

    /** @return a human-readable description of the issue */
    public String message() { return message; }

    /** @return the analysis phase that produced this diagnostic */
    public Source source() { return source; }

    /** @return an optional machine-readable error code, or {@code null} */
    public String code() { return code; }

    /** @return {@code true} if this diagnostic represents a compilation error */
    public boolean isError() {
        return severity == Severity.ERROR;
    }

    /** @return {@code true} if this diagnostic represents a warning */
    public boolean isWarning() {
        return severity == Severity.WARNING;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (!(obj instanceof Diagnostic)) return false;
        Diagnostic that = (Diagnostic) obj;
        return range.equals(that.range)
                && severity == that.severity
                && message.equals(that.message)
                && source == that.source
                && Objects.equals(code, that.code);
    }

    @Override
    public int hashCode() {
        int result = range.hashCode();
        result = 31 * result + severity.hashCode();
        result = 31 * result + message.hashCode();
        result = 31 * result + source.hashCode();
        result = 31 * result + (code != null ? code.hashCode() : 0);
        return result;
    }

    @Override
    public String toString() {
        String loc = range.getStartRow() + ":" + range.getStartColumn();
        String codeStr = code != null ? " [" + code + "]" : "";
        return severity.name().toLowerCase() + "(" + loc + "): " + message + codeStr
                + " (" + source.name().toLowerCase() + ")";
    }
}
