# ✏️ CustomNPC+ 1.11 - The Scripting Update ✏️

---

The **Scripting Update** completely transforms how you write scripts in CNPC+! A brand new **Script Editor** with **syntax highlighting**, **autocomplete**, and **error detection** makes scripting feel like a real IDE. Write scripts in **Java** with the new Janino engine, enable **client-side scripting** for responsive experiences, and organize your Cloner with **folders**!

---

## Script Editor 📝

The script editor has been rebuilt from the ground up into a **full-featured code editor** right inside Minecraft.

### Writing Code

- **Syntax Highlighting** - Keywords, types, variables, methods, strings, and comments are all color-coded automatically. The editor understands your code -- local variables, global fields, and chained method calls each get their own color
- **Autocomplete** - Start typing and get smart suggestions for methods, fields, and classes. Suggestions know whether you're in a static or instance context, and frequently used members appear first. Smart cursor placement inserts methods with the cursor between parentheses when parameters exist
- **Auto Imports** - Type a class name and the editor suggests the correct import for you
- **Error Detection** - Catch mistakes before you even run your script! Wrong argument types, missing methods, type mismatches, unused imports, and more are all underlined with detailed error messages on hover
- **ECMAScript 5.1 Globals** - Built-in objects like `Math`, `JSON`, `Date`, and `Number` are recognized automatically in JavaScript mode
- **Global Script Variables** - Variables like `npc`, `world`, and `event` are recognized in the syntax highlighter and autocomplete as global definitions

> Hover over any symbol to see its **full type information**, documentation, and declaration -- just like a desktop IDE. Hover tooltips render **JSDoc with markdown formatting**, headings, and code blocks, and support **scrolling and panning** for long documentation.

---

### Navigation

- **Go To Line** - `CTRL+G` jumps to any line instantly
- **Search & Replace** - `CTRL+F` to search, `CTRL+H` to replace. All matches highlight in real-time
- **Go To Definition** - `CTRL+Click` any method, field, or type to jump straight to its definition
- **Fullscreen Mode** - Expand the editor to fill your entire game window for maximum workspace

### Editing

- **Undo/Redo** - Smart undo that works on whole words at a time, not individual characters
- **Line Operations** - Duplicate lines with `CTRL+D`, delete lines, and move lines up or down with shortcuts
- **Comment Toggle** - `CTRL+/` to comment or uncomment selected lines
- **Rename** - Rename a variable or method and every occurrence updates at once
- **Copy Whole Line** - Copy with nothing selected grabs the entire line
- **Triple Click** - Select an entire line instantly

### Visual Polish

- **Line Numbers** - A clean gutter with line numbers and current line highlighting
- **Smooth Scrolling** - Eased scroll animations for a polished feel
- **Indent Guides** - Vertical lines show your code's nesting depth, with the current scope highlighted in green
- **Brace Matching** - Matching `{}` braces light up, and unclosed braces turn red
- **Smart Brackets** - Typing `{` automatically creates the closing `}` with correct indentation. Same for `()`, `[]`, `""`, and `''`
- **Resizable Panels** - Autocomplete and hover info panels can be resized and panned to fit your workspace
- **Keyboard Shortcut Overlay** - Press a button to see all available shortcuts at a glance

---

## Java Scripting ☕

Write scripts in **real Java** instead of JavaScript! Powered by the **Janino** compiler, your Java code is compiled and runs natively.

- **Full Java Language** - Use classes, interfaces, enums, generics, lambdas, and everything you know from Java
- **Automatic Hook Resolution** - Just name your methods after the hook you want (e.g., `init`, `tick`, `interact`) and they're wired up automatically
- **Language Selector** - Each script tab has a dropdown to choose between JavaScript and Java. Mix and match across tabs
- **External Files** - Write `.java` scripts in an external editor and they're loaded automatically

> Java and JavaScript scripts coexist side by side. Pick whichever language fits your workflow!

---

## Client-Side Scripting 🖥️

Scripts can now run **on the client** for responsive, visual experiences!

- **Server Controlled** - Server owners decide whether client scripting is allowed. Players can't enable it on their own
- **Automatic Sync** - Script files are sent from the server to all connected clients on login, and re-synced whenever scripts are reloaded
- **Safe by Default** - Client scripting is off by default and must be explicitly enabled in the server config

> Client-side scripts open the door to custom UI effects, visual feedback, and client-only hooks -- all while the server stays in full control.

---

## Cloner Tab Overhaul 📂

The Cloner now supports **folders** for organizing your saved NPCs, items, and entries!

- **Folder System** - Create folders and subfolders to keep your cloner organized however you like
- **Full Screen Browser** - A new full-width directory view makes browsing large collections much easier
- **Improved Tabs** - Better tab navigation for switching between cloner categories

---

## Addon API Support 📘

Addon developers can now ship **type definitions** alongside their mods, giving scripters full autocomplete and documentation for addon APIs!

- **Automatic Loading** - Any mod can include API definitions and they'll appear in the script editor automatically
- **Patch Support** - Addons can extend existing CNPC+ types with new methods (e.g., a DBC addon adding `getDBCPlayer()` to the Player type)
- **Full Documentation** - Parameter names, return types, and descriptions all show up in autocomplete and hover info

> If you're a scripter using addons, you get autocomplete for their APIs with zero setup. If you're an addon developer, just include your definitions and everything works.

---

## Animation Improvements 🎬

- **Data Store** - Animations can now store and pass data across events and frames for more complex animation logic
- **Task System** - A new consumer-based system for reacting to animation lifecycle events, replacing the old approach
- **Expanded API** - More animation control exposed to the scripting API

---

## Animated Textures 🖼️

Items and effects now support **frame-based animated textures**!

- **Linked & Scripted Items** - Configure animated textures with custom frame count and frame time
- **Custom Effects** - Custom effect icons support animation with the same frame system
- **Configurable** - Set frames per row, ticks per frame, and interpolation between frames

---

## NPC API Additions 🧩

New scripting API properties for finer control over NPC behavior and appearance:

- **Hitbox Data** - Scale NPC hitbox width and height independently via `IHitboxData`
- **Tint Data** - Control hurt tint (damage flash) and apply persistent color tints with alpha via `ITintData`
- **Behavior Properties** - Sprint, swim, door interaction, and walking range are now scriptable
- **Sound Control** - Full sound management via `ISound` with volume, pitch, repeat, and position
- **Error Reporting** - Script action exceptions now report to the Script Console with full stack traces instead of failing silently

---

## Additional Changes 🔧

- **Player Dialog Events** - Now work the same way as NPC Dialog Events for consistency
- **Linked Item Attack Speed** - Added attack speed configuration to linked items
- **Trader Balance Preview** - Client-side balance prediction shows your expected balance during trades
- **Right-Click Cycling** - Multi-option NPC buttons (Hair, Eyes, Fur, etc.) can now be right-clicked to cycle backwards
- **Quest Cooldown Fix** - Fixed cooldown not working for MC Custom and RL Custom timer types
- **Bard Music Fix** - Fixed music restarting or duplicating when opening dialogs
- **Biome Spawn Fix** - Fixed NPC biome spawn settings not saving after editing
- **Block Waypoint Fix** - Fixed issues with block waypoints
- **Script Config Fix** - Fixed script configuration not syncing properly between server and client

---
