/**
 * <h1>Interpreter Package - Java Syntax Highlighting System</h1>
 * 
 * <p>This package provides a clean, modular rewrite of the JavaTextContainer
 * syntax highlighting system. It replaces the original implementation with
 * well-structured, atomic classes that work together like LEGO pieces.</p>
 * 
 * <h2>Architecture Overview</h2>
 * 
 * <pre>
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                    ScriptTextContainer                          │
 * │  (Compatibility adapter extending JavaTextContainer)            │
 * │  - USE_NEW_INTERPRETER flag to toggle between systems           │
 * │  - Produces JavaTextContainer.LineData/Token for GUI compat     │
 * └───────────────────────┬─────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                     ScriptDocument                              │
 * │  (Main document container - replaces JavaTextContainer core)    │
 * │                                                                 │
 * │  7-Phase Tokenization Pipeline:                                 │
 * │  1. findExcludedRanges() - strings/comments                     │
 * │  2. parseImports() - import statements                          │
 * │  3. parseStructure() - methods, fields                          │
 * │  4. buildMarks() - all highlighting marks                       │
 * │  5. resolveConflicts() - priority-based resolution              │
 * │  6. buildTokens() - convert marks to tokens per line            │
 * │  7. computeIndentGuides() - visual indent guides                │
 * └───────────────────────┬─────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                      ScriptLine                                 │
 * │  (Line container with token management and rendering)           │
 * │  - Token list with navigation                                   │
 * │  - Indent guides                                                │
 * │  - drawString() with color codes                                │
 * │  - drawStringHex() for direct hex color rendering               │
 * └───────────────────────┬─────────────────────────────────────────┘
 *                         │
 *                         ▼
 * ┌─────────────────────────────────────────────────────────────────┐
 * │                        Token                                    │
 * │  (Atomic token unit with type-specific metadata)                │
 * │  - Text, start/end offsets                                      │
 * │  - TokenType (with hex color and priority)                      │
 * │  - prev()/next() navigation                                     │
 * │  - Type-specific metadata: TypeInfo, FieldInfo, MethodInfo      │
 * └─────────────────────────────────────────────────────────────────┘
 * </pre>
 * 
 * <h2>Supporting Classes</h2>
 * 
 * <ul>
 *   <li><b>TokenType</b> - Enum with hex colors and priorities for all token types</li>
 *   <li><b>TypeInfo</b> - Immutable type metadata (class/interface/enum)</li>
 *   <li><b>TypeResolver</b> - Class resolution with caching (replaces ClassPathFinder)</li>
 *   <li><b>ImportData</b> - Import statement tracking with resolution status</li>
 *   <li><b>FieldInfo</b> - Field/variable metadata with scope (global/local/parameter)</li>
 *   <li><b>MethodInfo</b> - Method declaration/call metadata with parameters</li>
 * </ul>
 * 
 * <h2>Key Improvements Over Original</h2>
 * 
 * <ol>
 *   <li><b>Single-pass tokenization</b> - 7 distinct phases vs multiple loops</li>
 *   <li><b>Token navigation</b> - prev()/next() methods for easy traversal</li>
 *   <li><b>Type-specific metadata</b> - Rich information attached to tokens</li>
 *   <li><b>Hex color support</b> - Direct RGB colors in addition to MC color codes</li>
 *   <li><b>Clean separation</b> - Each class has one responsibility</li>
 *   <li><b>Immutable data</b> - TypeInfo, FieldInfo, MethodInfo are immutable</li>
 *   <li><b>Feature toggle</b> - USE_NEW_INTERPRETER flag for safe rollback</li>
 * </ol>
 * 
 * <h2>Usage</h2>
 * 
 * <pre>{@code
 * // The new system is automatically used via ScriptTextContainer
 * // To disable and use original JavaTextContainer:
 * ScriptTextContainer.USE_NEW_INTERPRETER = false;
 * 
 * // To access the underlying ScriptDocument:
 * ScriptTextContainer container = (ScriptTextContainer) textArea.getContainer();
 * ScriptDocument doc = container.getDocument();
 * 
 * // Token navigation example:
 * for (ScriptLine line : doc.getLines()) {
 *     for (Token token : line.getTokens()) {
 *         Token next = token.next();
 *         Token prev = token.prev();
 *     }
 * }
 * }</pre>
 * 
 * @since 2.0
 * @see ScriptTextContainer
 * @see ScriptDocument
 * @see Token
 */
package noppes.npcs.client.gui.util.script.interpreter;
