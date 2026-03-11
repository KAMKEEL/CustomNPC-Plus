# Script Editor & Scripting Enhancements

---

The script editor has been rebuilt from the ground up into a **full-featured code editor** right inside Minecraft, with syntax highlighting, autocomplete, error detection, and support for writing scripts in Java.

---

## Script Editor

### Writing Code

- **Syntax Highlighting** - Keywords, types, variables, methods, strings, and comments are color-coded automatically. Local variables, global fields, and chained method calls each get their own color
- **Autocomplete** - Smart suggestions for methods, fields, and classes as you type. Knows whether you're in a static or instance context. Frequently used members appear first, and the cursor is placed between parentheses when a method has parameters
- **Auto Imports** - Type a class name and the editor suggests the correct import
- **Error Detection** - Catches mistakes before you run your script. Wrong argument types, missing methods, type mismatches, and unused imports are underlined with detailed error messages on hover
- **Built-in Globals** - Objects like `Math`, `JSON`, `Date`, and `Number` are recognized automatically in JavaScript mode. Script variables like `npc`, `world`, and `event` are also recognized
- **JS Language Support** - Arrow functions (`=>`), shorthand methods, object literals, and array expressions are all understood by the highlighter and type resolver

> Hover over any symbol to see its **full type information**, documentation, and declaration. Hover tooltips support JSDoc with markdown formatting, headings, code blocks, and scrolling for long documentation.

---

### Navigation

- **Go To Line** - `CTRL+G` jumps to any line
- **Search & Replace** - `CTRL+F` to search, `CTRL+H` to replace. All matches highlight in real-time
- **Go To Definition** - `CTRL+Click` any method, field, or type to jump to its definition
- **Fullscreen Mode** - Expand the editor to fill your entire game window

---

### Editing

- **Undo/Redo** - Smart undo that works on whole words, not individual characters
- **Duplicate Line** - `CTRL+D` to duplicate the current line
- **Comment Toggle** - `CTRL+/` to comment or uncomment selected lines
- **Rename** - Rename a variable or method and every occurrence updates at once
- **Move Lines** - Move lines up or down with shortcuts
- **Copy Whole Line** - Copy with nothing selected grabs the entire line
- **Triple Click** - Select an entire line instantly

---

### Visual Polish

- **Line Numbers** - Clean gutter with current line highlighting
- **Smooth Scrolling** - Eased scroll animations
- **Indent Guides** - Vertical lines showing nesting depth, with the current scope highlighted in green
- **Brace Matching** - Matching `{}` braces light up, unclosed braces turn red
- **Smart Brackets** - Typing `{` auto-creates the closing `}` with correct indentation. Same for `()`, `[]`, `""`, and `''`
- **Resizable Panels** - Autocomplete and hover panels can be resized and panned
- **Keyboard Shortcut Overlay** - Press a button to see all available shortcuts

---

## Java Scripting (Janino)

Write scripts in **real Java** instead of JavaScript. Powered by the **Janino** compiler, your Java code is compiled and runs natively.

- **Full Java Language** - Classes, interfaces, enums, generics, lambdas, and method references — everything you know from Java
- **Automatic Hook Resolution** - Name your methods after the hook you want (e.g., `init`, `tick`, `interact`) and they're wired up automatically
- **Language Selector** - Each script tab has a dropdown to choose between JavaScript and Java. Mix and match across tabs
- **Broad Support** - Java scripting works in NPC, Player, Forge, Recipe, Effect, and Linked Item scripts
- **External Files** - Write `.java` scripts in an external editor and they load automatically

> Java and JavaScript scripts coexist side by side. Pick whichever language fits your workflow.

---

## Client-Side Scripting

Scripts can now run **on the client** for responsive, visual experiences.

- **Server Controlled** - Server owners decide whether client scripting is allowed. Players can't enable it themselves
- **Automatic Sync** - Script files are sent from the server to all clients on login, and re-synced on script reload
- **Safe by Default** - Off by default, must be explicitly enabled in the server config

> Client-side scripts open the door to custom UI effects, visual feedback, and client-only hooks — all while the server stays in control.

---

## Addon API Support

Addon developers can ship **type definitions** alongside their mods, giving scripters full autocomplete and documentation for addon APIs.

- **Automatic Loading** - Any mod can include `.d.ts` definition files in their `assets/<modid>/api/` directory and they appear in the script editor automatically
- **Patch Support** - Addons can extend existing CNPC+ types with new methods (e.g., a DBC addon adding `getDBCPlayer()` to the Player type)
- **Full Documentation** - Parameter names, return types, and descriptions show up in autocomplete and hover info
- **Hot Reload** - Definition files are reloaded when Minecraft resources are reloaded

> Scripters get autocomplete for addon APIs with zero setup. Addon developers just include their definitions.

---
