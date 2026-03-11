# Script Editor & Scripting Enhancements

---

The script editor has been rebuilt into a **full-featured code editor** inside Minecraft with syntax highlighting, autocomplete, error detection, and Java scripting support.

---

## Script Editor

### Writing Code

- **Syntax Highlighting** - Color-coded keywords, types, variables, methods, strings, comments. Context-aware coloring for locals, globals, and chained calls
- **Autocomplete** - Smart suggestions with static/instance context awareness, frequency-based ordering, and smart cursor placement for method parameters. Auto-imports for class names
- **Error Detection** - Catches wrong argument types, missing methods, type mismatches, unused imports with detailed hover messages
- **Built-in Globals** - ECMAScript 5.1 objects (`Math`, `JSON`, `Date`, `Number`) and script globals (`npc`, `world`, `event`) recognized automatically
- **JS Language Support** - Arrow functions, shorthand methods, object literals, and array expressions understood by highlighter and type resolver

> Hover any symbol for **full type info and documentation** with JSDoc markdown formatting, scrollable tooltips, and code blocks.

### Navigation

`CTRL+G` Go to Line, `CTRL+F` Search, `CTRL+H` Replace, `CTRL+Click` Go to Definition, Fullscreen Mode.

### Editing

Undo/Redo (word-level), Duplicate Line (`CTRL+D`), Comment Toggle (`CTRL+/`), Rename (all occurrences), Move Lines, Copy Whole Line (empty selection), Triple Click (select line).

### Visual Polish

Line numbers with current line highlight, smooth scrolling, indent guides (green current scope), brace matching (red unclosed), smart bracket auto-close, resizable/pannable panels, keyboard shortcut overlay.

---

## Java Scripting (Janino)

Write scripts in **real Java** compiled natively via the **Janino** compiler. Full Java language support (classes, interfaces, enums, generics, lambdas, method references). Automatic hook resolution by method name. Per-tab language selector to mix Java and JavaScript. Works in NPC, Player, Forge, Recipe, Effect, and Linked Item scripts. External `.java` files load automatically.

---

## Client-Side Scripting

Scripts can run **on the client** — server-controlled, auto-synced on login and reload, off by default. Enables custom UI effects, visual feedback, and client-only hooks while the server stays in control.

