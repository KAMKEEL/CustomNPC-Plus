# AGENTS.md — bigguy.treesitter.java LLM Navigation Guide

> **[PARSE]** This file is optimized for LLM agents. Scan headers → tables → decision trees. ASCII diagrams show data flow. Code blocks are copy-paste ready.

---

## Table of Contents

1. [Quick Orientation](#1-quick-orientation)
2. [File-by-File Reference](#2-file-by-file-reference)
3. [Data Flow Maps](#3-data-flow-maps)
4. [Task Decision Trees](#4-task-decision-trees)
5. [Class Interactions & Lifecycles](#5-class-interactions--lifecycles)
6. [Thread Safety Reference](#6-thread-safety-reference)
7. [Query System Deep Dive](#7-query-system-deep-dive)
8. [Common Patterns](#8-common-patterns)
9. [Integration Checklist](#9-integration-checklist-customnpc)
10. [Anti-Patterns](#10-anti-patterns)
11. [Debugging Guide](#11-debugging-guide)
12. [Extension Points](#12-extension-points)
13. [References](#13-references)

---

## 1. Quick Orientation

**[PARSE] What is this?** A Java port of Zed IDE's tree-sitter Java parsing pipeline. 16 files (14 Java + 1 test + 1 README). Parses Java 8+ source, runs 10 Zed `.scm` queries, returns typed results.

**[PARSE] 3-step API:**
```
JavaParser.parse(src) → SyntaxTree → JavaQueryEngine.highlights/outline/runnables()
```

### Architecture Diagram

```
┌──────────────────────────────────────────────────────────────────────────────┐
│                           ENTRY POINTS                                      │
│  JavaParser ──parse()──→ SyntaxTree                                         │
│  JavaQueryEngine ──highlights()/outline()/runnables()/locals()──→ Results    │
├──────────────────────────────────────────────────────────────────────────────┤
│                                                                              │
│  ┌─────────────────┐    ┌───────────────────┐    ┌────────────────────────┐  │
│  │   PARSING        │    │   QUERYING         │    │   RESULT TYPES        │  │
│  │                  │    │                    │    │                       │  │
│  │  JavaParser      │───▶│  JavaQueryEngine   │───▶│  HighlightCapture     │  │
│  │  (156 lines)     │    │  (352 lines)       │    │  (43 lines)           │  │
│  │                  │    │                    │    │  HighlightGroup       │  │
│  │  JavaGrammar     │◀───│  QueryPatterns     │    │  (73 lines)           │  │
│  │  (113 lines)     │    │  (344 lines)       │    │  OutlineEntry         │  │
│  │                  │    │                    │    │  (70 lines)           │  │
│  │  JavaParserConfig│    │                    │    │  RunnableEntry        │  │
│  │  (77 lines)      │    │                    │    │  (106 lines)          │  │
│  └────────┬─────────┘    └────────────────────┘    │  TextSpan            │  │
│           │                                        │  (136 lines)          │  │
│  ┌────────▼─────────┐                              └────────────────────────┘ │
│  │   TREE/NODE       │                                                       │
│  │                   │    ┌────────────────────────────────────────────────┐  │
│  │  SyntaxTree       │    │   SPI (future)                                │  │
│  │  (146 lines)      │    │                                               │  │
│  │  SyntaxNode       │    │   spi/LanguageServer.java (52 lines)          │  │
│  │  (206 lines)      │    │   spi/DebugAdapter.java   (42 lines)          │  │
│  └───────────────────┘    └────────────────────────────────────────────────┘  │
│                                                                              │
└──────────────────────────────────────────────────────────────────────────────┘

Test: JavaTreeSitterTest.java (739 lines, 30+ tests across 10 @Nested classes)
```

### Dependency Graph (who imports whom)

```
JavaParser ──uses──▶ JavaGrammar, JavaParserConfig, SyntaxTree
JavaQueryEngine ──uses──▶ JavaGrammar, QueryPatterns, SyntaxTree,
                          HighlightCapture, HighlightGroup, OutlineEntry,
                          RunnableEntry, TextSpan
SyntaxTree ──wraps──▶ TSTree (native)
SyntaxNode ──wraps──▶ TSNode (native)
JavaGrammar ──wraps──▶ TreeSitterJava, TSLanguage, TSQuery
QueryPatterns ──standalone──▶ (no internal deps, just String constants)
All result types ──use──▶ TextSpan
spi/* ──standalone──▶ (no internal deps, interfaces only)
```

---

## 2. File-by-File Reference

**[PARSE] LLM Lookup Table — sorted by concern.**

### Parsing Layer

| File | Lines | Purpose | Key Methods | Thread-Safe | Notes |
|---|---|---|---|---|---|
| `JavaParser.java` | 156 | Thread-safe parser with incremental support | `parse(String)`, `reparse(SyntaxTree, String)`, `reparseAfterEdit(EditOp, String)`, `close()` | ✅ ReentrantLock | Entry point. Implements `AutoCloseable`. Guards `TSParser` with lock. |
| `JavaGrammar.java` | 113 | Grammar lifecycle singleton + query cache | `getInstance()`, `createIsolated()`, `compileQuery(String)`, `clearQueryCache()`, `close()` | ✅ DCL + ConcurrentHashMap | Double-checked locking singleton. Query cache keyed by S-expression string. |
| `JavaParserConfig.java` | 77 | Builder-pattern config | `builder()`, `defaults()`, `getMatchLimit()`, `isIncrementalParsingEnabled()`, `isErrorRecoveryEnabled()` | ✅ Immutable | Defaults: matchLimit=512, incremental=true, errorRecovery=true. |

### Tree/Node Layer

| File | Lines | Purpose | Key Methods | Thread-Safe | Notes |
|---|---|---|---|---|---|
| `SyntaxTree.java` | 146 | TSTree wrapper with edit/copy | `getRootNode()`, `hasErrors()`, `edit(EditOp)`, `copy()`, `getChangedRanges(SyntaxTree)`, `unwrap()`, `close()` | ⚠️ Per-thread | Use `copy()` for cross-thread. Inner class `EditOperation` (9 fields: bytes + row/col). |
| `SyntaxNode.java` | 206 | Null-safe TSNode wrapper with traversal | `getType()`, `getText()`, `getChildByFieldName(String)`, `findFirst(String)`, `findAll(String)`, `getNamedChildren()`, `toSExpression()` | ⚠️ Per-thread | Never throws on null — returns null instead. Recursive search via `findFirst`/`findAll`. |
| `TextSpan.java` | 136 | Immutable position + byte range | `getStartRow/Col()`, `getEndRow/Col()`, `getStartByte()`, `getEndByte()`, `contains(TextSpan)`, `overlaps(TextSpan)`, `containsByte(int)` | ✅ Immutable | Implements `equals`/`hashCode`. Format: `[row:col..row:col](bytes N..M)`. |

### Query Layer

| File | Lines | Purpose | Key Methods | Thread-Safe | Notes |
|---|---|---|---|---|---|
| `JavaQueryEngine.java` | 352 | Query execution → typed results | `highlights(tree, src)`, `outline(tree, src)`, `runnables(tree, src)`, `locals(tree, src)`, `executeQuery(tree, src, queryStr)` | ✅ Stateless | Creates TSQueryCursor per call. Inner classes: `LocalCapture`, `RawCapture`. |
| `QueryPatterns.java` | 344 | All 10 Zed `.scm` queries as constants | `HIGHLIGHTS`, `OUTLINE`, `RUNNABLES`, `LOCALS`, `FOLDS`, `BRACKETS`, `INDENTS`, `TEXT_OBJECTS`, `INJECTIONS`, `OVERRIDES` | ✅ Constants | Pure `static final String` constants. Ported from Zed v6.8.12. |

### Result Types

| File                    | Lines | Purpose                        | Key Fields                                                                                  | Thread-Safe | Notes                                                                                                                                                           |
|-------------------------|-------|--------------------------------|---------------------------------------------------------------------------------------------|-------------|-----------------------------------------------------------------------------------------------------------------------------------------------------------------|
| `HighlightCapture.java` | 43    | Single highlight result        | `span: TextSpan`, `group: HighlightGroup`, `nodeType: String`, `patternIndex: int`          | ✅ Immutable | Produced by `engine.highlights()`.                                                                                                                              |
| `HighlightGroup.java`   | 73    | Enum of ~27 Zed capture groups | `fromCaptureName(String)` resolves `@keyword` → `KEYWORD` etc.                              | ✅ Enum      | Values: VARIABLE, FUNCTION, TYPE, KEYWORD, STRING, NUMBER, BOOLEAN, COMMENT, ATTRIBUTE, OPERATOR, CONSTRUCTOR, PROPERTY, CONSTANT, ENUM, PUNCTUATION_*, etc.    |
| `OutlineEntry.java`     | 70    | Code symbol outline result     | `name`, `kind: Kind`, `nameSpan`, `bodySpan`, `contextKeywords: List<String>`, `depth: int` | ✅ Immutable | `Kind` enum: CLASS, INTERFACE, ENUM, RECORD, ANNOTATION_TYPE, METHOD, CONSTRUCTOR, FIELD, ENUM_CONSTANT, STATIC_INITIALIZER, UNKNOWN.                           |
| `RunnableEntry.java`    | 106   | Runnable detection result      | `tag: Tag`, `span`, `className`, `methodName`, `packageName`, `metadata: Map`               | ✅ Immutable | `Tag` enum: JAVA_MAIN, JAVA_TEST_METHOD, JAVA_TEST_CLASS, JAVA_TEST_METHOD_NESTED, JAVA_TEST_CLASS_NESTED. `getFullyQualifiedName()` builds `pkg.Class#method`. |

### SPI Layer (Future)

| File | Lines | Purpose | Key Methods | Notes |
|---|---|---|---|---|
| `spi/LanguageServer.java` | 52 | LSP interface | `initialize()`, `shutdown()`, `diagnostics()`, `completions()`, `gotoDefinition()` | Interface only. Status enum: STARTING, RUNNING, STOPPED, ERROR. |
| `spi/DebugAdapter.java` | 42 | DAP interface | `launch()`, `attach()`, `disconnect()` | Interface only. SessionState enum: IDLE, LAUNCHING, RUNNING, PAUSED, STOPPED. |

### Tests

| File | Lines | Test Count | Coverage Areas |
|---|---|---|---|
| `JavaTreeSitterTest.java` | 739 | 30+ | Parsing (9), NodeTraversal (5), TextSpan (3), Highlights (8), Outline (6), Runnables (3), CustomQueries (3), IncrementalParsing (1), Locals (1), HighlightGroup (2), ResourceManagement (2), Java8Features (1) |

---

## 3. Data Flow Maps

### Primary Flow: Source → Highlights

```
                 ┌──────────┐
                 │  String   │  (Java source code)
                 │  source   │
                 └─────┬─────┘
                       │
            ┌──────────▼──────────┐
            │    JavaParser       │  parse(source)
            │  ┌────────────────┐ │
            │  │ ReentrantLock  │ │  ◄── thread boundary
            │  │ TSParser       │ │
            │  │   .parseString │ │
            │  └────────────────┘ │
            └──────────┬──────────┘
                       │
            ┌──────────▼──────────┐
            │    SyntaxTree       │  wraps TSTree + source text
            │  ┌────────────────┐ │
            │  │ TSTree (native)│ │
            │  │ String source  │ │
            │  └────────────────┘ │
            └──────────┬──────────┘
                       │
          ┌────────────▼────────────┐
          │    JavaQueryEngine      │  highlights(tree, src)
          │  ┌────────────────────┐ │
          │  │ JavaGrammar        │ │
          │  │  .compileQuery(    │ │  ◄── ConcurrentHashMap cache
          │  │   QueryPatterns    │ │
          │  │   .HIGHLIGHTS)    │ │
          │  │                    │ │
          │  │ TSQueryCursor      │ │  ◄── created per invocation
          │  │  .exec(query,node) │ │
          │  │  .nextCapture()    │ │
          │  └────────────────────┘ │
          └────────────┬────────────┘
                       │
          ┌────────────▼────────────┐
          │  List<HighlightCapture> │  Typed results
          │  ┌────────────────────┐ │
          │  │ TextSpan           │ │  position + byte range
          │  │ HighlightGroup     │ │  semantic category
          │  │ nodeType           │ │  tree-sitter node type
          │  └────────────────────┘ │
          └─────────────────────────┘
```

### Incremental Parsing Flow

```
  Original parse                    Edit event                   Re-parse
  ─────────────                    ──────────                   ────────
  parser.parse(src1)               tree.edit(EditOp)            parser.reparse(tree, src2)
        │                                │                            │
        ▼                                ▼                            ▼
  SyntaxTree t1 ──────────────▶ t1 (modified) ──────────────▶ SyntaxTree t2
  (full parse)                  (marks changed                (reuses unchanged
                                 regions)                      subtrees, fast)
```

### Query Compilation & Caching

```
  QueryPatterns.HIGHLIGHTS (String constant)
        │
        ▼
  JavaGrammar.compileQuery(source)
        │
        ├── Cache HIT ──▶ return cached TSQuery
        │
        └── Cache MISS
              │
              ▼
        new TSQuery(language, source)  ◄── can throw TSQueryException
              │
              ▼
        queryCache.put(source, query)
              │
              ▼
        return TSQuery
```

---

## 4. Task Decision Trees

**[PARSE] "I want to X, what do I modify?"**

### Decision Table

| I want to... | Modify | Also consider |
|---|---|---|
| **Add a new query type** (e.g. `diagnostics.scm`) | 1. `QueryPatterns.java` — add `public static final String DIAGNOSTICS = "..."` | 2. `JavaQueryEngine.java` — add `diagnostics()` method | 3. Create new result type if needed |
| **Add highlighting for a new node** | 1. `QueryPatterns.HIGHLIGHTS` — add pattern | 2. `HighlightGroup.java` — add enum if new group needed | Test with `engine.highlights()` |
| **Add a new outline symbol type** | 1. `QueryPatterns.OUTLINE` — add capture pattern | 2. `OutlineEntry.Kind` enum — add new constant | 3. `JavaQueryEngine.resolveOutlineKind()` — add case |
| **Detect a new runnable pattern** | 1. `QueryPatterns.RUNNABLES` — add pattern with `#set! tag "..."` | 2. `RunnableEntry.Tag` enum — add new constant | Test with `engine.runnables()` |
| **Improve parsing performance** | 1. `JavaParserConfig` — tune `matchLimit` | 2. Enable incremental parsing | 3. `JavaGrammar.queryCache` handles query caching already |
| **Add LSP support** | Implement `spi/LanguageServer.java` | Register via service loader or dependency injection |
| **Add DAP support** | Implement `spi/DebugAdapter.java` | Use `RunnableEntry` to find launch targets |
| **Parse on a background thread** | 1. Create `JavaParser` per-thread OR share with lock | 2. Use `tree.copy()` to move tree across threads | Never share `SyntaxNode` across threads |
| **Execute a custom one-off query** | `engine.executeQuery(tree, src, queryString)` | Returns `List<RawCapture>`, no new files needed |
| **Change parsing config** | `JavaParserConfig.builder().matchLimit(N).build()` | Pass to `new JavaParser(config)` |
| **Find syntax errors in parsed code** | `tree.hasErrors()` → `root.findAll("ERROR")` | Each ERROR node has span via `.getSpan()` |
| **Get changed regions after edit** | `oldTree.getChangedRanges(newTree)` | Returns `TextSpan[]` |

### Add New Query Type — Step by Step

```
IF you need a new query type:

1. ADD constant to QueryPatterns.java:
   public static final String MY_QUERY = "(pattern) @capture";

2. IF the query produces typed results:
   a. CREATE result class: MyResult.java (immutable, with TextSpan)
   b. ADD method to JavaQueryEngine:
      public List<MyResult> myQuery(SyntaxTree tree, String src) { ... }

3. IF the query only needs raw captures:
   USE engine.executeQuery(tree, src, QueryPatterns.MY_QUERY)
   → returns List<RawCapture>

4. ADD test in JavaTreeSitterTest.java
```

### Add New HighlightGroup — Step by Step

```
IF you need a new highlight category:

1. ADD enum constant to HighlightGroup.java:
   MY_GROUP("my.group"),

2. ADD pattern to QueryPatterns.HIGHLIGHTS:
   "(some_node) @my.group\n"

3. fromCaptureName() already handles it via iteration — no code change needed

4. ADD test asserting engine.highlights() returns the new group
```

---

## 5. Class Interactions & Lifecycles

### JavaGrammar Lifecycle

```
                    ┌──────────────┐
                    │ UNINITIALIZED│  instance == null
                    └──────┬───────┘
                           │ getInstance() [first call]
                           │ synchronized(LOCK)
                           ▼
                    ┌──────────────┐
                    │   LOADED     │  language = new TreeSitterJava()
                    │              │  queryCache = empty ConcurrentHashMap
                    └──────┬───────┘
                           │ compileQuery() [first unique query]
                           ▼
                    ┌──────────────┐
                    │   CACHED     │  queryCache populated
                    │              │  subsequent compileQuery() → cache hit
                    └──────┬───────┘
                           │ close()
                           ▼
                    ┌──────────────┐
                    │   CLOSED     │  closed = true
                    │              │  all cached queries freed
                    └──────┬───────┘
                           │ getInstance() [after close]
                           │ creates new instance
                           ▼
                    ┌──────────────┐
                    │   LOADED     │  (fresh singleton)
                    └──────────────┘
```

### JavaParser Lifecycle

```
  new JavaParser()
        │
        ├── sets language on TSParser
        │   (throws IllegalStateException if ABI mismatch)
        ▼
  ┌─────────────┐
  │   READY     │  parse() / reparse() available
  │             │  parseLock guards all operations
  └──────┬──────┘
         │ close()
         ▼
  ┌─────────────┐
  │   CLOSED    │  all methods throw IllegalStateException
  │             │  nativeParser.close() called
  └─────────────┘
```

### SyntaxTree Lifecycle

```
  parser.parse(src)
        │
        ▼
  ┌─────────────┐
  │   PARSED    │  getRootNode(), hasErrors(), copy() available
  │             │
  └──────┬──────┘
         │ edit(EditOp)
         ▼
  ┌─────────────┐
  │   EDITED    │  tree has dirty regions marked
  │             │  pass to parser.reparse() for incremental parse
  └──────┬──────┘
         │ close()
         ▼
  ┌─────────────┐
  │   CLOSED    │  all methods throw IllegalStateException
  │             │  native memory freed
  └─────────────┘
```

### Result Extraction Pipeline

```
  TSQuery (compiled from QueryPatterns constant)
     │
     ▼
  TSQueryCursor.exec(query, rootNode, sourceText)
     │
     │  nextCapture(match) / nextMatch(match) loop
     ▼
  TSQueryMatch
     ├── getCaptures() → TSQueryCapture[]
     │     ├── getNode() → TSNode
     │     └── getIndex() → capture index → getCaptureNameForId()
     │
     ├── getMetadata() → Map<String,String>  (from #set! directives)
     │
     └── getPatternIndex() → int
     │
     ▼
  Typed Result Construction
     ├── highlights(): captureName → HighlightGroup.fromCaptureName() → HighlightCapture
     ├── outline():    "name"/"item"/"context" captures → OutlineEntry
     ├── runnables():  "java_class_name"/"java_method_name"/"run" + metadata.tag → RunnableEntry
     └── locals():     captureName + text + span → LocalCapture
```

---

## 6. Thread Safety Reference

**[PARSE] Critical thread safety information.**

### Component Safety Matrix

| Component | Safety Level | Mechanism | Details |
|---|---|---|---|
| `JavaGrammar` (singleton) | ✅ SAFE | Double-checked locking + `ConcurrentHashMap` | `getInstance()` uses `volatile` + `synchronized`. `compileQuery()` uses `computeIfAbsent`. |
| `JavaParser` | ✅ SAFE | `ReentrantLock parseLock` | All `parse`/`reparse` methods acquire lock. `closed` is `volatile`. |
| `JavaQueryEngine` | ✅ SAFE | Stateless | Creates new `TSQueryCursor` per method call. No mutable state. |
| `JavaParserConfig` | ✅ SAFE | Immutable | All fields `final`, set in constructor via builder. |
| `QueryPatterns` | ✅ SAFE | `static final String` constants | Compile-time constants. |
| `TextSpan` | ✅ SAFE | Immutable | All fields `final`. Implements `equals`/`hashCode`. |
| `HighlightCapture` | ✅ SAFE | Immutable | All fields `final`. |
| `HighlightGroup` | ✅ SAFE | Enum | Inherently thread-safe. |
| `OutlineEntry` | ✅ SAFE | Immutable | List wrapped in `Collections.unmodifiableList`. |
| `RunnableEntry` | ✅ SAFE | Immutable | Map wrapped in `Collections.unmodifiableMap`. |
| `SyntaxTree` | ⚠️ PER-THREAD | None (native constraint) | **Use `tree.copy()` to share across threads.** TSTree is not thread-safe. |
| `SyntaxNode` | ⚠️ PER-THREAD | None (native constraint) | **Tied to parent tree's thread.** Do NOT pass between threads. |

### Lock Inventory

| Lock | Location | Guards | Contention Risk |
|---|---|---|---|
| `ReentrantLock parseLock` | `JavaParser:34` | `nativeParser.parseString()` calls | Low — parsing is fast (<1ms for typical files) |
| `synchronized(LOCK)` | `JavaGrammar:51` | Singleton creation only | Negligible — only on first access or after close |
| `ConcurrentHashMap.computeIfAbsent` | `JavaGrammar:88` | Query compilation cache | Low — each unique query compiled once |

### Thread Safety Rules

```
RULE 1: JavaParser, JavaQueryEngine, JavaGrammar → freely share across threads
RULE 2: SyntaxTree → call .copy() before passing to another thread
RULE 3: SyntaxNode → NEVER pass across threads (ephemeral, tied to tree)
RULE 4: All result types (HighlightCapture, OutlineEntry, etc.) → freely share
RULE 5: One JavaParser per thread is ALSO valid (simpler, no lock contention)
```

---

## 7. Query System Deep Dive

**[PARSE] All 10 query files, their captures, and result mappings.**

### Query Inventory

| Constant | Zed File | Size | Typed Method | Result Type | Capture Names |
|---|---|---|---|---|---|
| `HIGHLIGHTS` | `highlights.scm` | ~88 lines | `highlights()` | `List<HighlightCapture>` | `@function`, `@type`, `@keyword`, `@string`, `@number`, `@boolean`, `@comment`, `@operator`, `@attribute`, `@property`, `@constant`, `@constructor`, `@enum`, `@punctuation.bracket`, `@punctuation.delimiter`, `@string.escape`, `@constant.builtin`, `@function.builtin` |
| `OUTLINE` | `outline.scm` | ~50 lines | `outline()` | `List<OutlineEntry>` | `@name`, `@item`, `@context` |
| `RUNNABLES` | `runnables.scm` | ~83 lines | `runnables()` | `List<RunnableEntry>` | `@java_class_name`, `@java_method_name`, `@java_package_name`, `@run`, plus `_`-prefixed internal captures |
| `LOCALS` | `locals.scm` | ~47 lines | `locals()` | `List<LocalCapture>` | `@local.scope`, `@local.definition.type`, `@local.definition.method`, `@local.definition.var`, `@local.definition.parameter`, `@local.definition.field`, `@local.definition.import`, `@local.definition.namespace`, `@local.definition.enum`, `@local.reference` |
| `FOLDS` | `folds.scm` | ~6 lines | `executeQuery()` | `List<RawCapture>` | `@fold` |
| `BRACKETS` | `brackets.scm` | ~5 lines | `executeQuery()` | `List<RawCapture>` | `@open`, `@close` |
| `INDENTS` | `indents.scm` | ~3 lines | `executeQuery()` | `List<RawCapture>` | `@indent`, `@end` |
| `TEXT_OBJECTS` | `textobjects.scm` | ~20 lines | `executeQuery()` | `List<RawCapture>` | `@function.around`, `@function.inside`, `@class.around`, `@class.inside`, `@comment.around` |
| `INJECTIONS` | `injections.scm` | ~9 lines | `executeQuery()` | `List<RawCapture>` | `@content` (with `#set! "language"` metadata) |
| `OVERRIDES` | `overrides.scm` | ~2 lines | `executeQuery()` | `List<RawCapture>` | `@comment.inclusive`, `@string` |

### Highlights Capture → HighlightGroup Mapping

| Capture Name | HighlightGroup Enum | What It Matches |
|---|---|---|
| `@function` | `FUNCTION` | Method declarations and invocations |
| `@function.builtin` | `FUNCTION_BUILTIN` | `super` keyword |
| `@type` | `TYPE` | `type_identifier`, class/interface/record names |
| `@constructor` | `CONSTRUCTOR` | Constructor declarations |
| `@property` | `PROPERTY` | Field access and declarations |
| `@keyword` | `KEYWORD` | 39 Java keywords (public, class, if, etc.) |
| `@operator` | `OPERATOR` | All Java operators (+, -, ==, ->, etc.) |
| `@string` | `STRING` | String/character/text block literals |
| `@string.escape` | `STRING_ESCAPE` | Escape sequences in strings |
| `@number` | `NUMBER` | All numeric literals (decimal, hex, octal, binary, float) |
| `@boolean` | `BOOLEAN` | `true`, `false` |
| `@constant` | `CONSTANT` | SCREAMING_CASE identifiers (`#match? "^[A-Z_$][A-Z\\d_$]*$"`) |
| `@constant.builtin` | `CONSTANT_BUILTIN` | `null` literal |
| `@comment` | `COMMENT` | Line and block comments |
| `@attribute` | `ATTRIBUTE` | Annotations (`@Override`, `@Test`, etc.) |
| `@enum` | `ENUM` | Enum declaration names |
| `@punctuation.bracket` | `PUNCTUATION_BRACKET` | `()`, `[]`, `{}` |
| `@punctuation.delimiter` | `PUNCTUATION_DELIMITER` | `.`, `;`, `,` |

### Runnables — Tag Detection Logic

| Tag Value | Detection Pattern | Predicates Used |
|---|---|---|
| `java-main` | `class { public static void main(String[] args) }` | `#eq? @_public "public"`, `#eq? @_static "static"`, `#eq? @_main_name "main"` |
| `java-test-method` | `class { @Test void method() }` | `#any-of? @_test_annotation "Test" "ParameterizedTest" "RepeatedTest"` |
| `java-test-class` | `class { @Test void ...() }` (class-level) | Same annotation check, `@run` on class |
| `java-test-method-nested` | `class { @Nested class { @Test void method() } }` | `#eq? @_nested_annotation "Nested"` + test annotation check |
| `java-test-class-nested` | `class { @Nested class { @Test void ... } }` (class-level) | Same as above, `@run` on outer class |

### Predicate Types Used in Queries

| Predicate | Native Type | Used In | Purpose |
|---|---|---|---|
| `#eq?` | `TSQueryPredicateEq` | RUNNABLES | Exact string match on capture text |
| `#match?` | `TSQueryPredicateMatch` | HIGHLIGHTS, INJECTIONS | Regex match on capture text |
| `#any-of?` | `TSQueryPredicateAnyOf` | RUNNABLES | Match against set of values |
| `#set!` | `TSQueryPredicateSet` | RUNNABLES, INJECTIONS | Sets metadata key-value on match |

---

## 8. Common Patterns

**[PARSE] Copy-paste ready code for common tasks.**

### Pattern 1: Parse Single-Use Code

```java
try (JavaParser parser = new JavaParser()) {
    SyntaxTree tree = parser.parse(sourceCode);
    // use tree...
} // parser auto-closed, native memory freed
```

### Pattern 2: Parse with Incremental Updates (Editor Integration)

```java
JavaParser parser = new JavaParser(
    JavaParserConfig.builder()
        .enableIncrementalParsing(true)
        .build()
);

// Initial parse
SyntaxTree tree = parser.parse(originalSource);

// After user edits text at byte position 50, replacing 3 bytes with 7 bytes:
SyntaxTree.EditOperation edit = new SyntaxTree.EditOperation(
    50,   // startByte
    53,   // oldEndByte (50 + 3 deleted)
    57,   // newEndByte (50 + 7 inserted)
    2, 10,  // startRow, startCol
    2, 13,  // oldEndRow, oldEndCol
    2, 17   // newEndRow, newEndCol
);
SyntaxTree newTree = parser.reparseAfterEdit(edit, newSource);
```

### Pattern 3: Extract Highlights

```java
JavaQueryEngine engine = new JavaQueryEngine();
List<HighlightCapture> highlights = engine.highlights(tree, sourceText);

for (HighlightCapture h : highlights) {
    String text = sourceText.substring(h.getSpan().getStartByte(), h.getSpan().getEndByte());
    HighlightGroup group = h.getGroup();  // e.g. KEYWORD, FUNCTION, TYPE
    // Map group → color in your renderer
}
```

### Pattern 4: Extract Outline for Navigation

```java
List<OutlineEntry> outline = engine.outline(tree, sourceText);

for (OutlineEntry entry : outline) {
    // entry.getName()             → "MyClass", "myMethod", "myField"
    // entry.getKind()             → CLASS, METHOD, FIELD, ENUM, etc.
    // entry.getDepth()            → 0 (top-level), 1 (nested), ...
    // entry.getContextKeywords()  → ["public", "static", "final"]
    // entry.getNameSpan()         → position of the name token
    // entry.getBodySpan()         → position of the full declaration
}
```

### Pattern 5: Detect Runnables

```java
List<RunnableEntry> runnables = engine.runnables(tree, sourceText);

for (RunnableEntry r : runnables) {
    switch (r.getTag()) {
        case JAVA_MAIN:
            System.out.println("Run: " + r.getFullyQualifiedName());
            break;
        case JAVA_TEST_METHOD:
            System.out.println("Test: " + r.getFullyQualifiedName());
            break;
        case JAVA_TEST_CLASS:
            System.out.println("Test class: " + r.getClassName());
            break;
    }
}
```

### Pattern 6: Execute Any Query (Raw)

```java
// Run folds query
List<JavaQueryEngine.RawCapture> folds = engine.executeQuery(tree, src, QueryPatterns.FOLDS);

// Run custom query
String customQuery = "(method_declaration name: (identifier) @name type: (_) @return_type)";
List<JavaQueryEngine.RawCapture> results = engine.executeQuery(tree, src, customQuery);

for (JavaQueryEngine.RawCapture r : results) {
    r.getCaptureName();  // "name" or "return_type"
    r.getText();         // extracted source text
    r.getSpan();         // TextSpan position
    r.getNodeType();     // tree-sitter node type
    r.getMetadata();     // Map from #set! directives
}
```

### Pattern 7: Find Syntax Errors

```java
SyntaxTree tree = parser.parse(source);
if (tree.hasErrors()) {
    SyntaxNode root = tree.getRootNode();
    List<SyntaxNode> errors = root.findAll("ERROR");
    for (SyntaxNode error : errors) {
        TextSpan span = error.getSpan();
        System.err.printf("Syntax error at line %d, col %d%n",
            span.getStartRow() + 1, span.getStartColumn());
    }
}
```

### Pattern 8: Thread-Safe Shared Parser

```java
// Option A: Share parser (lock-guarded internally)
JavaParser sharedParser = new JavaParser();
// Safe to call from any thread:
SyntaxTree tree = sharedParser.parse(source);
// But: copy tree before passing to another thread
SyntaxTree threadCopy = tree.copy();

// Option B: Parser per thread (no contention)
ThreadLocal<JavaParser> parserPerThread = ThreadLocal.withInitial(JavaParser::new);
SyntaxTree tree = parserPerThread.get().parse(source);
```

### Pattern 9: Isolated Grammar for Testing

```java
// Don't pollute the singleton in tests
JavaGrammar grammar = JavaGrammar.createIsolated();
JavaParser parser = new JavaParser(grammar, JavaParserConfig.defaults());
JavaQueryEngine engine = new JavaQueryEngine(grammar);
// ... test ...
parser.close();
grammar.close();
```

---

## 9. Integration Checklist (CustomNPC+)

**[PARSE] Step-by-step integration for CustomNPC+ developers.**

### Step 1: Dependencies (build.gradle)

```groovy
dependencies {
    implementation 'org.treesitter:tree-sitter:0.26.6'
    implementation 'org.treesitter:tree-sitter-java:0.23.5'
}
```

> **Native libraries**: tree-sitter uses JNI. The native `.dll`/`.so`/`.dylib` must be on the library path at runtime. The tree-sitter Maven artifacts bundle them.

### Step 2: Initialize (Application Startup)

```java
// Single initialization — JavaGrammar is a lazy singleton
JavaParser parser = new JavaParser();
JavaQueryEngine engine = new JavaQueryEngine();
// Store as fields or in your dependency container
```

### Step 3: Parse Loop (On Script Load/Edit)

```java
public void onScriptLoaded(String npcScriptSource) {
    try (SyntaxTree tree = parser.parse(npcScriptSource)) {
        // Check for errors
        if (tree.hasErrors()) {
            handleErrors(tree);
        }
        // Get highlights for syntax coloring
        List<HighlightCapture> highlights = engine.highlights(tree, npcScriptSource);
        applyHighlighting(highlights);
        // Get outline for navigation panel
        List<OutlineEntry> outline = engine.outline(tree, npcScriptSource);
        updateOutlinePanel(outline);
    }
}
```

### Step 4: Shutdown (Application Exit)

```java
public void onShutdown() {
    parser.close();
    // JavaGrammar singleton lives until JVM exit (or manual close)
    // If you want explicit cleanup:
    JavaGrammar.getInstance().close();
}
```

### Step 5: Error Handling

```java
// All failures are RuntimeExceptions:
// - IllegalStateException: parser/tree closed, ABI mismatch, parse failure
// - TSQueryException: malformed S-expression query
// - IllegalArgumentException: null tree passed to SyntaxTree

try {
    tree = parser.parse(source);
} catch (IllegalStateException e) {
    if (e.getMessage().contains("ABI version")) {
        // tree-sitter library version doesn't match grammar version
        log.error("Version mismatch: update tree-sitter or tree-sitter-java");
    }
}
```

---

## 10. Anti-Patterns

**[PARSE] What NOT to do. Each entry = a bug waiting to happen.**

### ❌ DON'T: Hold SyntaxNode References Across Threads

```java
// BAD — SyntaxNode is tied to native tree memory
SyntaxNode node = tree.getRootNode().findFirst("method_declaration");
executor.submit(() -> node.getText());  // UNSAFE: native pointer may be invalid

// GOOD — extract data before crossing thread boundary
String text = node.getText();
TextSpan span = node.getSpan();
executor.submit(() -> process(text, span));  // SAFE: immutable values
```

### ❌ DON'T: Reuse SyntaxTree After close()

```java
// BAD
SyntaxTree tree = parser.parse(src);
tree.close();
tree.getRootNode();  // throws IllegalStateException

// GOOD — use try-with-resources
try (SyntaxTree tree = parser.parse(src)) {
    // safe to use here
}
```

### ❌ DON'T: Share SyntaxTree Across Threads Without copy()

```java
// BAD
SyntaxTree tree = parser.parse(src);
thread1.use(tree);  // concurrent native access = crash or corruption
thread2.use(tree);

// GOOD
SyntaxTree copy1 = tree.copy();
SyntaxTree copy2 = tree.copy();
thread1.use(copy1);
thread2.use(copy2);
```

### ❌ DON'T: Ignore tree.hasErrors()

```java
// BAD — silently using a tree with parse errors
SyntaxTree tree = parser.parse(badSource);
List<OutlineEntry> outline = engine.outline(tree, badSource);
// outline may be incomplete or nonsensical

// GOOD — check for errors first
if (tree.hasErrors()) {
    List<SyntaxNode> errors = tree.getRootNode().findAll("ERROR");
    reportErrors(errors);
    // still usable for partial results if needed
}
```

### ❌ DON'T: Forget to Close the Parser

```java
// BAD — native memory leak
JavaParser parser = new JavaParser();
parser.parse(src);
// parser goes out of scope, native TSParser not freed

// GOOD
try (JavaParser parser = new JavaParser()) {
    parser.parse(src);
}
```

### ❌ DON'T: Create Multiple JavaGrammar Singletons

```java
// BAD — bypassing cache
JavaGrammar g1 = JavaGrammar.getInstance();
JavaGrammar g2 = JavaGrammar.getInstance();
// g1 == g2 (this is fine, it's the same instance)

// But:
JavaGrammar g1 = JavaGrammar.getInstance();
g1.close();
// Now queries compiled on g1 are freed
// getInstance() will create a new one with empty cache
```

### ❌ DON'T: Hardcode Capture Names Without Checking HighlightGroup

```java
// BAD — brittle string matching
if (captureName.equals("keyword")) { ... }

// GOOD — use the enum
HighlightGroup group = HighlightGroup.fromCaptureName(captureName);
if (group == HighlightGroup.KEYWORD) { ... }
```

### ❌ DON'T: Mix Byte Offsets and Character Offsets

```java
// BAD — source.charAt(span.getStartByte()) is wrong for multi-byte chars
// tree-sitter uses BYTE offsets, Java strings use char (UTF-16) offsets

// GOOD — use sourceText.substring(startByte, endByte) for ASCII
// For full Unicode safety, consider byte→char offset conversion
String text = node.getText();  // SyntaxNode handles this correctly
```

---

## 11. Debugging Guide

**[PARSE] Troubleshooting — symptom → cause → fix.**

| Symptom | Likely Cause | Fix |
|---|---|---|
| `IllegalStateException: Failed to set Java language — ABI version mismatch` | tree-sitter core library version incompatible with tree-sitter-java grammar | Update both to compatible versions. Current: tree-sitter 0.26.6 + tree-sitter-java 0.23.5 |
| `TSQueryException` on `compileQuery()` | Malformed S-expression in query pattern | Check QueryPatterns constant. Common issue: unescaped backslashes in Java strings (need `\\\\d` for `\d` in regex). |
| Highlights list is empty | Query compiled but no matches found | 1. Verify tree has no errors (`tree.hasErrors()`). 2. Check if source text was passed correctly. 3. Try `tree.getRootNode().toSExpression()` to verify parse tree. |
| Outline missing entries | Pattern doesn't match node structure | Run `executeQuery()` with the OUTLINE constant and inspect raw captures. Check node types with `toSExpression()`. |
| Runnables returns empty | `#set! tag` metadata not populated | Check `TSQueryMatch.getMetadata()` returns non-null. Verify the predicates (`#eq?`, `#any-of?`) are being evaluated (requires sourceText passed to `cursor.exec()`). |
| Incremental parsing gives wrong tree | EditOperation byte offsets are incorrect | Byte offsets must exactly match the edit. `startByte` = where edit begins, `oldEndByte` = end of deleted range, `newEndByte` = end of inserted range. Row/col must also be accurate. |
| `NullPointerException` in SyntaxNode | TSNode is null (missing child) | SyntaxNode methods return `null` for missing nodes. Always null-check: `SyntaxNode child = node.getChildByFieldName("name"); if (child != null) { ... }` |
| Thread-related crash/corruption | SyntaxTree shared across threads | Use `tree.copy()` per thread. Never share `SyntaxNode` instances. |
| `IllegalStateException: JavaParser has been closed` | Using parser after `close()` was called | Ensure `close()` is only called at shutdown. Use try-with-resources for scoped parsers. |
| Query results have wrong text | Source text passed to query doesn't match parsed source | The `sourceText` passed to `engine.highlights()` MUST be the same string that was passed to `parser.parse()`. |
| `OutOfMemoryError` during query | Too many in-progress matches | Lower `JavaParserConfig.matchLimit` (default 512). Very complex/deeply nested files may need tuning. |

### Debugging Checklist

```
1. PARSE: parser.parse(src) returns tree?
   YES → continue
   NO  → check parser not closed, language set correctly

2. TREE: tree.hasErrors()?
   YES → root.findAll("ERROR") to locate issues, still usable for partial results
   NO  → continue

3. QUERY: engine.highlights/outline/runnables() returns results?
   YES → continue
   NO  → try engine.executeQuery(tree, src, QueryPatterns.HIGHLIGHTS) for raw output
         → check toSExpression() to verify tree structure matches query patterns

4. RESULTS: results have correct spans/text?
   YES → done
   NO  → verify src parameter matches the string passed to parse()
```

---

## 12. Extension Points

**[PARSE] How to extend the package for new features.**

### New Query Type

```
Files to modify:
1. QueryPatterns.java     — add public static final String MY_QUERY = "...";
2. JavaQueryEngine.java   — add public List<MyResult> myQuery(SyntaxTree, String) { ... }
3. Create MyResult.java   — immutable result class with TextSpan
4. JavaTreeSitterTest.java — add test

Template for engine method:
    public List<MyResult> myQuery(SyntaxTree tree, String sourceText) {
        TSQuery query = grammar.compileQuery(QueryPatterns.MY_QUERY);
        List<MyResult> results = new ArrayList<>();
        try (TSQueryCursor cursor = new TSQueryCursor()) {
            TSNode rootNode = tree.unwrap().getRootNode();
            cursor.exec(query, rootNode, sourceText);
            TSQueryMatch match = new TSQueryMatch();
            while (cursor.nextCapture(match)) {
                for (TSQueryCapture capture : match.getCaptures()) {
                    // ... build MyResult
                }
            }
        }
        return results;
    }
```

### New HighlightGroup

```
Files to modify:
1. HighlightGroup.java — add enum value: MY_GROUP("my.group"),
2. QueryPatterns.HIGHLIGHTS — add pattern: "(some_node) @my.group\n" +

No change needed to fromCaptureName() — it iterates all values automatically.
```

### New OutlineEntry.Kind

```
Files to modify:
1. OutlineEntry.Kind enum — add: MY_KIND,
2. JavaQueryEngine.resolveOutlineKind() — add case: case "my_node_type": return Kind.MY_KIND;
3. QueryPatterns.OUTLINE — add capture pattern if needed
```

### New RunnableEntry.Tag

```
Files to modify:
1. RunnableEntry.Tag enum — add: MY_TAG("my-tag-value"),
2. QueryPatterns.RUNNABLES — add pattern with (#set! tag "my-tag-value")

No change needed to Tag.fromValue() — it iterates all values automatically.
```

### Implement LSP (Language Server)

```
Files to create:
1. spi/impl/JdtlsLanguageServer.java — implements spi.LanguageServer

The interface defines:
- initialize(Map<String, Object> initializationOptions)
- diagnostics(String filePath) → Object
- completions(String filePath, int line, int column) → Object
- gotoDefinition(String filePath, int line, int column) → Object
- shutdown()
- getStatus() → Status

Note: JDTLS requires JDK 21+, which may conflict with CustomNPC+ targets.
```

### Implement DAP (Debug Adapter)

```
Files to create:
1. spi/impl/JavaDebugAdapter.java — implements spi.DebugAdapter

The interface defines:
- launch(mainClass, args, vmArgs, classPaths, env)
- attach(hostName, port, timeout)
- disconnect()
- getState() → SessionState

Use RunnableEntry to find launch targets:
    List<RunnableEntry> mains = engine.runnables(tree, src).stream()
        .filter(r -> r.getTag() == RunnableEntry.Tag.JAVA_MAIN)
        .collect(Collectors.toList());
    for (RunnableEntry main : mains) {
        debugAdapter.launch(main.getFullyQualifiedName(), args, vmArgs, ...);
    }
```

---

## 13. References

**[PARSE] External resources and paths.**

### Project Paths

| Resource | Path |
|---|---|
| Source code | `Z:\old desktop\projects\CustomNPC-Plus-goat\src\main\java\bigguy\treesitter\` |
| SPI interfaces | `Z:\old desktop\projects\CustomNPC-Plus-goat\src\main\java\bigguy\treesitter\spi\` |
| Tests | `Z:\old desktop\projects\CustomNPC-Plus-goat\src\test\java\bigguy\treesitter\JavaTreeSitterTest.java` |
| Package README | `Z:\old desktop\projects\CustomNPC-Plus-goat\src\main\java\bigguy\treesitter\README.md` |
| CustomNPC+ project root | `Z:\old desktop\projects\CustomNPC-Plus-goat\` |

### Analysis Documents

| Resource | Path |
|---|---|
| Zed integration analysis | `X:\projects\tree-sitter\zed-java-tree-sitter-integration.md` |
| tree-sitter Java bindings | `X:\projects\tree-sitter\tree-sitter-ng-v0.26.6\tree-sitter\src\main\java\org\treesitter\` |
| tree-sitter-java grammar | `X:\projects\tree-sitter\tree-sitter-ng-v0.26.6\tree-sitter-java\src\main\java\org\treesitter\TreeSitterJava.java` |

### External Links

| Resource | URL |
|---|---|
| tree-sitter documentation | https://tree-sitter.github.io/tree-sitter/ |
| tree-sitter-java grammar | https://github.com/tree-sitter/tree-sitter-java |
| Zed IDE | https://zed.dev |
| Zed source (reference) | https://github.com/zed-industries/zed |
| tree-sitter query syntax | https://tree-sitter.github.io/tree-sitter/using-parsers/queries/ |

### Version Compatibility

| Component | Version | Notes |
|---|---|---|
| tree-sitter core | 0.26.6 | JNI bindings via Maven |
| tree-sitter-java grammar | 0.23.5 | Java 8+ syntax support |
| Zed queries ported from | v6.8.12 | All 10 .scm files |
| Java source compatibility | 8+ | No Java 9+ APIs used in source |
| JUnit | 5 | Tests only |

---

## Appendix: File Sizes Quick Reference

```
JavaParser.java           156 lines   ██████░░░░░░░░░░
JavaGrammar.java          113 lines   ████░░░░░░░░░░░░
JavaQueryEngine.java      352 lines   ██████████████░░  ◄ largest core file
JavaParserConfig.java      77 lines   ███░░░░░░░░░░░░░
QueryPatterns.java        344 lines   █████████████░░░  ◄ all 10 queries
SyntaxTree.java           146 lines   ██████░░░░░░░░░░
SyntaxNode.java           206 lines   ████████░░░░░░░░
TextSpan.java             136 lines   █████░░░░░░░░░░░
HighlightCapture.java      43 lines   ██░░░░░░░░░░░░░░
HighlightGroup.java        73 lines   ███░░░░░░░░░░░░░
OutlineEntry.java          70 lines   ███░░░░░░░░░░░░░
RunnableEntry.java        106 lines   ████░░░░░░░░░░░░
spi/LanguageServer.java    52 lines   ██░░░░░░░░░░░░░░
spi/DebugAdapter.java      42 lines   ██░░░░░░░░░░░░░░
─────────────────────────────────────
TOTAL                    1916 lines   (core package)
JavaTreeSitterTest.java   739 lines   (tests)
```
