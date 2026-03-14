# bigguy.treesitter — Java Tree-Sitter Integration

A production-quality Java package that ports [Zed IDE's](https://zed.dev) tree-sitter Java parsing architecture to a standalone library, designed for integration with CustomNPC+.

## Architecture

This package mirrors Zed's three-layer architecture adapted to Java idioms:

```
┌─────────────────────────────────────────────────────────────────┐
│                    Application Layer                            │
│  (CustomNPC+ or your code)                                     │
│                                                                 │
│  JavaParser.parse("...")  →  SyntaxTree  →  JavaQueryEngine     │
│                                              ├─ highlights()    │
│                                              ├─ outline()       │
│                                              ├─ runnables()     │
│                                              ├─ locals()        │
│                                              └─ executeQuery()  │
├─────────────────────────────────────────────────────────────────┤
│                    Core Library Layer                            │
│                                                                 │
│  JavaGrammar ─── manages grammar lifecycle & query cache        │
│  QueryPatterns ─ embedded .scm queries from Zed v6.8.12        │
│  SyntaxTree ──── wraps TSTree with edit support                 │
│  SyntaxNode ──── wraps TSNode with null-safe traversal          │
│  TextSpan ────── unified position/byte-offset range             │
│  JavaParserConfig ─ configurable behavior                       │
├─────────────────────────────────────────────────────────────────┤
│                    Native Layer                                 │
│                                                                 │
│  tree-sitter (JNI) ←→ tree-sitter-java grammar                │
│  TSParser, TSTree, TSNode, TSQuery, TSQueryCursor               │
└─────────────────────────────────────────────────────────────────┘
```

### Zed Architecture Mapping

| Zed Component | This Package | Purpose |
|---|---|---|
| `extension.toml` grammars | `JavaGrammar` | Grammar lifecycle management |
| `buffer.rs` parsing | `JavaParser` | Parse/reparse with incremental updates |
| `syntax_map.rs` query execution | `JavaQueryEngine` | Apply .scm queries to parse trees |
| `languages/java/*.scm` | `QueryPatterns` | All 10 query files as constants |
| `language.rs` types | `SyntaxTree`, `SyntaxNode` | Tree/node wrappers |
| `java.rs` code labels | `HighlightCapture`, `OutlineEntry` | Typed results |
| `runnables.scm` + `tasks.json` | `RunnableEntry` | Run/test detection |
| JDTLS integration | `spi.LanguageServer` | Future LSP interface |
| java-debug integration | `spi.DebugAdapter` | Future DAP interface |

## Quick Start

### 1. Parse Java Source

```java
try (JavaParser parser = new JavaParser()) {
    SyntaxTree tree = parser.parse("public class Hello { }");
    SyntaxNode root = tree.getRootNode();
    System.out.println(root.toSExpression());
    // (program (class_declaration (modifiers (modifier)) name: (identifier) body: (class_body)))
}
```

### 2. Extract Syntax Highlights

```java
JavaParser parser = new JavaParser();
JavaQueryEngine engine = new JavaQueryEngine();

String src = "public class Foo { int x = 42; }";
SyntaxTree tree = parser.parse(src);

List<HighlightCapture> highlights = engine.highlights(tree, src);
for (HighlightCapture h : highlights) {
    System.out.printf("%s → %s at %s%n",
        src.substring(h.getSpan().getStartByte(), h.getSpan().getEndByte()),
        h.getGroup().getCaptureName(),
        h.getSpan());
}
// "public" → keyword at [0:0..0:6](bytes 0..6)
// "class"  → keyword at [0:7..0:12](bytes 7..12)
// "Foo"    → type at [0:13..0:16](bytes 13..16)
// "42"     → number at [0:23..0:25](bytes 23..25)
// ...
```

### 3. Get Code Outline

```java
List<OutlineEntry> outline = engine.outline(tree, src);
for (OutlineEntry entry : outline) {
    String indent = new String(new char[entry.getDepth()]).replace("\0", "  ");
    System.out.printf("%s%s %s %s%n", indent,
        entry.getKind(), entry.getName(),
        entry.getContextKeywords());
}
// CLASS Foo [public]
//   FIELD x [int]
```

### 4. Detect Runnables (Main Methods & Tests)

```java
String src = "public class App { public static void main(String[] args) { } }";
SyntaxTree tree = parser.parse(src);
List<RunnableEntry> runnables = engine.runnables(tree, src);

for (RunnableEntry r : runnables) {
    System.out.printf("Tag: %s, FQN: %s%n",
        r.getTag().getValue(),
        r.getFullyQualifiedName());
}
// Tag: java-main, FQN: App
```

### 5. Incremental Parsing

```java
// Parse original
SyntaxTree tree1 = parser.parse("class X { int a; }");

// Describe the edit (inserted " int b;" before "}")
SyntaxTree.EditOperation edit = new SyntaxTree.EditOperation(
    17, 17, 24,  // byte offsets
    0, 17, 0, 17, 0, 24  // row/column positions
);

// Re-parse incrementally (reuses unchanged subtrees)
SyntaxTree tree2 = parser.reparseAfterEdit(edit, "class X { int a; int b; }");
```

### 6. Custom Queries

```java
String customQuery = "(method_declaration name: (identifier) @method_name)";
List<JavaQueryEngine.RawCapture> results = engine.executeQuery(tree, src, customQuery);
```

## Package Structure

```
bigguy/treesitter/
├── JavaParser.java           Thread-safe parser with incremental support
├── JavaGrammar.java          Grammar lifecycle and query cache (singleton)
├── JavaQueryEngine.java      Query execution: highlights, outline, runnables
├── JavaParserConfig.java     Configuration via builder pattern
├── QueryPatterns.java        All Zed .scm queries as Java constants
├── SyntaxTree.java           Parse tree wrapper with edit/copy support
├── SyntaxNode.java           Null-safe node with traversal helpers
├── TextSpan.java             Immutable position range
├── HighlightCapture.java     Highlight query result
├── HighlightGroup.java       Semantic highlight categories (enum)
├── OutlineEntry.java         Code outline query result
├── RunnableEntry.java        Runnable detection result
└── spi/
    ├── LanguageServer.java   LSP integration interface (future)
    └── DebugAdapter.java     DAP integration interface (future)
```

## Dependencies

- `org.treesitter:tree-sitter` — Core tree-sitter Java bindings (JNI)
- `org.treesitter:tree-sitter-java` — Java grammar
- JUnit 5 for tests

## CustomNPC+ Integration Guide

### Step 1: Add Dependencies

Add to your `build.gradle`:

```groovy
dependencies {
    implementation 'org.treesitter:tree-sitter:0.26.6'
    implementation 'org.treesitter:tree-sitter-java:0.23.5'
}
```

### Step 2: Parse NPC Script Source

```java

import bigguy.texteditor.treesitter.java.*;

public class NpcScriptAnalyzer {
    private final JavaParser parser = new JavaParser();
    private final JavaQueryEngine engine = new JavaQueryEngine();

    public void analyzeScript(String javaSource) {
        try (SyntaxTree tree = parser.parse(javaSource)) {
            if (tree.hasErrors()) {
                // Handle syntax errors gracefully
                SyntaxNode root = tree.getRootNode();
                for (SyntaxNode error : root.findAll("ERROR")) {
                    System.err.printf("Syntax error at %s%n", error.getSpan());
                }
            }

            // Get syntax highlighting for editor display
            List<HighlightCapture> highlights = engine.highlights(tree, javaSource);

            // Get outline for navigation
            List<OutlineEntry> outline = engine.outline(tree, javaSource);

            // Detect runnable targets
            List<RunnableEntry> runnables = engine.runnables(tree, javaSource);
        }
    }

    public void shutdown() {
        parser.close();
    }
}
```

### Step 3: Use Incremental Parsing for Live Editing

```java
public class LiveEditor {
    private final JavaParser parser = new JavaParser(
        JavaParserConfig.builder()
            .enableIncrementalParsing(true)
            .matchLimit(256)
            .build()
    );
    private SyntaxTree currentTree;

    public void onInitialLoad(String source) {
        currentTree = parser.parse(source);
    }

    public void onEdit(int startByte, int oldEndByte, int newEndByte,
                       int startRow, int startCol,
                       int oldEndRow, int oldEndCol,
                       int newEndRow, int newEndCol,
                       String newSource) {
        SyntaxTree.EditOperation op = new SyntaxTree.EditOperation(
            startByte, oldEndByte, newEndByte,
            startRow, startCol, oldEndRow, oldEndCol, newEndRow, newEndCol
        );
        currentTree = parser.reparseAfterEdit(op, newSource);
    }
}
```

## Thread Safety

| Component | Thread Safety | Notes |
|---|---|---|
| `JavaGrammar` | ✅ Thread-safe | Singleton with double-checked locking, ConcurrentHashMap cache |
| `JavaParser` | ✅ Thread-safe | ReentrantLock guards native parser access |
| `JavaQueryEngine` | ✅ Thread-safe | Stateless; creates cursor per invocation |
| `SyntaxTree` | ⚠️ Per-thread | Use `tree.copy()` to share across threads |
| `SyntaxNode` | ⚠️ Per-thread | Tied to its parent tree's thread ownership |
| All result types | ✅ Immutable | `HighlightCapture`, `OutlineEntry`, `RunnableEntry`, `TextSpan` |

## Query Coverage

All 10 of Zed's Java query files are ported:

| Query | Constant | Purpose |
|---|---|---|
| `highlights.scm` | `QueryPatterns.HIGHLIGHTS` | ~30 capture groups for syntax coloring |
| `outline.scm` | `QueryPatterns.OUTLINE` | Symbol navigation (classes, methods, fields) |
| `runnables.scm` | `QueryPatterns.RUNNABLES` | Main methods, JUnit tests, nested tests |
| `locals.scm` | `QueryPatterns.LOCALS` | Scope boundaries, variable definitions |
| `folds.scm` | `QueryPatterns.FOLDS` | Collapsible regions |
| `brackets.scm` | `QueryPatterns.BRACKETS` | Bracket pairs |
| `indents.scm` | `QueryPatterns.INDENTS` | Auto-indentation rules |
| `textobjects.scm` | `QueryPatterns.TEXT_OBJECTS` | Function/class/comment selection |
| `injections.scm` | `QueryPatterns.INJECTIONS` | Embedded language detection |
| `overrides.scm` | `QueryPatterns.OVERRIDES` | Context-sensitive behavior |

## Identified Gaps & Future Work

1. **Rendering pipeline**: Highlights are extracted but not rendered — rendering is application-specific (CustomNPC+ would map `HighlightGroup` to colors)
2. **LSP/DAP integration**: SPI interfaces are defined but no implementation provided — this is by design, as JDTLS requires JDK 21+ which may conflict with CustomNPC+'s target
3. **`has-ancestor?` predicate**: Not implemented in the upstream tree-sitter Java binding — some advanced highlight patterns may not filter correctly
4. **Properties grammar**: Zed also ships `tree-sitter-properties` for `.properties` files — not ported (out of scope for CustomNPC+)
5. **Format string injections**: Printf format string highlighting requires a separate `printf` grammar — not bundled

## License

This package is an independent implementation inspired by Zed IDE's architecture. Zed is licensed under Apache-2.0/GPL. This code is part of the CustomNPC+ project.
