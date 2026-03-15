# Script Editor Infrastructure — `client/gui/util/script/`

## Purpose
Full-featured code editor built into Minecraft's GUI. Syntax highlighting, autocomplete,
error detection, hover info, go-to-definition, and refactoring for JS (Nashorn) and Java (Janino).

## Entry Points
| Class | Role |
|-------|------|
| `GuiScriptTextArea` | Main editor widget — rendering, input, scrolling, keybinds. Extends `GuiNpcTextField` |
| `GuiScriptInterface` | Editor GUI frame — tab management, hook list sidebar, settings, fullscreen toggle |
| `GuiNPCEventScripts` / `GuiScriptGlobal` | NPC-specific and global/player editor subclasses |

## Helper Components (`script/`)
`ScrollState`, `SelectionState`, `BracketMatcher`, `IndentHelper`, `CommentHandler`,
`CursorNavigation`, `UndoManager`, `SearchReplaceBar`, `GoToLineDialog`, `RenameRefactorHandler`, `FormatHelper`

## Interpreter System (`script/interpreter/`)
7-phase tokenization pipeline (strings/comments → imports → structure → marks → conflicts → tokens → indent guides).

| Subpackage | Contents |
|------------|----------|
| `token/` | `Token`, `TokenType` (enum with hex colors/priorities), `ScriptColorScheme` |
| `type/` | `TypeInfo`, `TypeResolver`, `ClassIndex`, `TypeChecker`, `GenericContext`, `ImportData` |
| `method/` | `MethodInfo`, `MethodSignature`, `MethodCallInfo` |
| `field/` | `FieldInfo`, `FieldAccessInfo`, `AssignmentInfo`, `EnumConstantInfo` |
| `expression/` | `ExpressionParser`, `ExpressionTypeResolver`, `TypeRules`, `CastExpressionResolver` |
| `hover/` | `TokenHoverInfo`, `TokenHoverRenderer`, `HoverState`, `GutterIconRenderer` |
| `jsdoc/` | `JSDocParser`, `JSDocInfo` — Javadoc/JSDoc parsing for hover display |
| `bridge/` | `DtsJavaBridge` — maps reflected Java methods to `.d.ts` type info for Janino scripts |

## JavaScript Type System (`interpreter/js_parser/`)
| Class | Role |
|-------|------|
| `JSTypeRegistry` | Singleton — all types from `.d.ts`, hook signatures, global objects |
| `TypeScriptDefinitionParser` | Parses `.d.ts` → `JSTypeInfo` |
| `JSScriptAnalyzer` | Analyzes JS source for local variables, scopes, type inference |
| `DtsModScanner` | Discovers `.d.ts` from all loaded mods (`assets/<modid>/api/**/*.d.ts`) |

## Autocomplete System (`script/autocomplete/`)
| Class | Role |
|-------|------|
| `AutocompleteManager` | Orchestrates triggers, filtering, insertion |
| `AutocompleteMenu` | Dropdown rendering with icons, descriptions |
| `JSAutocompleteProvider` | JS completions from `JSTypeRegistry` + `JSScriptAnalyzer` |
| `JavaAutocompleteProvider` | Java completions from reflection + `DtsJavaBridge` |
| `weighter/` | Scoring chain: match quality, member kind, context fit, deprecation, alphabetical |

## How It Works
1. Editor opens → `JSTypeRegistry` initialized from `.d.ts` resources
2. Text changes → `ScriptTextContainer` runs 7-phase pipeline → `ScriptLine`/`Token` lists
3. Autocomplete triggers → provider (JS or Java) generates scored completions
4. Hover → `TokenHoverRenderer` + `DtsJavaBridge` build info panels
5. Errors → `DocumentError` + `ErrorUnderlineRenderer` underline type mismatches

## Key Design Rules
- **Dual language**: JS uses `JSTypeRegistry` (from `.d.ts`), Java uses reflection + `DtsJavaBridge`
- **Context-aware hooks**: `ScriptContext` enum determines which hooks appear in the sidebar
- **Addon loading**: `DtsModScanner` scans all Forge mods for `assets/<modid>/api/*.d.ts`
- **Non-blocking**: Type resolution designed for real-time use at GUI frame rate
