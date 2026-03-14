package bigguy.treesitter.java.spi;

import java.util.Map;

/**
 * Service Provider Interface for Java language server integration.
 *
 * <p>Modeled after Zed's JDTLS integration architecture:
 * the tree-sitter layer handles parsing and syntax queries, while the
 * language server provides semantic intelligence (completions, diagnostics,
 * navigation). This interface defines the boundary between the two.</p>
 *
 * <p>Implementors would typically wrap Eclipse JDTLS or another Java LS.</p>
 */
public interface LanguageServer {

    enum Status { STARTING, RUNNING, STOPPED, ERROR }

    Status getStatus();

    void initialize(Map<String, Object> initializationOptions);

    void shutdown();

    /**
     * Requests diagnostics for the given source file.
     *
     * @param filePath the absolute path to the Java file
     * @return a list of diagnostic objects (format defined by implementor)
     */
    Object diagnostics(String filePath);

    /**
     * Requests completions at the given position.
     *
     * @param filePath the file path
     * @param line     0-based line number
     * @param column   0-based column offset
     * @return completion results (format defined by implementor)
     */
    Object completions(String filePath, int line, int column);

    /**
     * Requests the definition location of the symbol at the given position.
     *
     * @param filePath the file path
     * @param line     0-based line number
     * @param column   0-based column offset
     * @return definition location (format defined by implementor)
     */
    Object gotoDefinition(String filePath, int line, int column);
}
