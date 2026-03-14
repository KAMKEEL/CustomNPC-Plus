package bigguy.texteditor.treesitter.java;

/**
 * Configuration for {@link JavaParser} behavior.
 *
 * <p>Uses the builder pattern for readable construction:</p>
 * <pre>{@code
 * JavaParserConfig config = JavaParserConfig.builder()
 *     .matchLimit(256)
 *     .enableIncrementalParsing(true)
 *     .build();
 * }</pre>
 */
public final class JavaParserConfig {

    private static final int DEFAULT_MATCH_LIMIT = 512;

    private final int matchLimit;
    private final boolean incrementalParsingEnabled;
    private final boolean errorRecoveryEnabled;

    private JavaParserConfig(Builder builder) {
        this.matchLimit = builder.matchLimit;
        this.incrementalParsingEnabled = builder.incrementalParsingEnabled;
        this.errorRecoveryEnabled = builder.errorRecoveryEnabled;
    }

    /**
     * Maximum number of in-progress matches allowed by the query cursor.
     * Higher values use more memory but handle deeply nested patterns.
     */
    public int getMatchLimit() { return matchLimit; }

    /**
     * Whether incremental parsing is enabled. When true, the parser
     * retains the previous tree for re-use on subsequent parses.
     */
    public boolean isIncrementalParsingEnabled() { return incrementalParsingEnabled; }

    /**
     * Whether to enable tree-sitter's error recovery.
     * When true, partial parse trees are returned for malformed input.
     */
    public boolean isErrorRecoveryEnabled() { return errorRecoveryEnabled; }

    public static Builder builder() { return new Builder(); }

    public static JavaParserConfig defaults() { return new Builder().build(); }

    public static final class Builder {
        private int matchLimit = DEFAULT_MATCH_LIMIT;
        private boolean incrementalParsingEnabled = true;
        private boolean errorRecoveryEnabled = true;

        private Builder() {}

        public Builder matchLimit(int matchLimit) {
            if (matchLimit <= 0) throw new IllegalArgumentException("matchLimit must be positive");
            this.matchLimit = matchLimit;
            return this;
        }

        public Builder enableIncrementalParsing(boolean enabled) {
            this.incrementalParsingEnabled = enabled;
            return this;
        }

        public Builder enableErrorRecovery(boolean enabled) {
            this.errorRecoveryEnabled = enabled;
            return this;
        }

        public JavaParserConfig build() {
            return new JavaParserConfig(this);
        }
    }
}
