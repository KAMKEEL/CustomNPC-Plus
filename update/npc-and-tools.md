# NPC, Animation & Tool Improvements

---

A collection of improvements to NPCs, animations, items, and tools added alongside the major feature updates.

---

## Cloner Tab Overhaul

The Cloner now supports **folders** for organizing your saved NPCs, items, and entries.

- **Folder System** - Create folders and subfolders to organize the cloner however you like. Works for animations, abilities, forms, auras, outlines, effects, and linked items
- **Full Screen Browser** - A new full-width directory view for browsing large collections
- **Improved Tabs** - Better tab navigation for switching between cloner categories
- **Quick Tags** - Tag-based filtering for fast searching within the cloner

---

## Animation Improvements

- **Data Store** - Animations can store and pass data across events and frames for complex animation logic
- **Task System** - A new consumer-based system for reacting to animation lifecycle events
- **Expanded API** - More animation control exposed to the scripting API

---

## Animated Textures

Items and effects now support **frame-based animated textures**.

- **Linked & Scripted Items** - Configure animated textures with custom frame count and frame time
- **Custom Effects** - Custom effect icons support animation with the same frame system
- **Ability Icons** - Ability icons in the hotbar support animated textures
- **Scripted GUI** - Textured rectangles in scripted GUIs support animation
- **Configurable** - Set frames per row, ticks per frame, and interpolation between frames

---

## NPC API Additions

New scripting API properties for finer control over NPCs:

- **Hitbox Data** - Scale NPC hitbox width and height independently via `IHitboxData`
- **Tint Data** - Control hurt tint (damage flash) and apply persistent color tints with alpha via `ITintData`
- **Behavior Properties** - Sprint, swim, door interaction, fire reactions, tactical chance, and walking range are now scriptable
- **Sound Control** - Full sound management via `ISound` with volume, pitch, repeat, and position
- **Skin Overlay Colors** - `getColor()` and `setColor()` on skin overlays for tinting overlay layers
- **Six Overlays in Singleplayer** - Skin overlays now work properly in singleplayer
- **Knockback API** - New methods for applying knockback with support for respecting or ignoring knockback resistance
- **Error Reporting** - Script exceptions now report to the Script Console with full stack traces instead of failing silently

---

## Natural Spawn Improvements

The natural NPC spawning system received several improvements:

- **GUI Improvements** - Better interface for configuring biome-based NPC spawns
- **Runtime Control** - Improved runtime management of natural spawns
- **Despawn Handling** - Better logic for naturally despawning NPCs when appropriate
- **Biome Persistence** - Fixed NPC biome spawn settings not saving after editing

---

## NPC Combat & Behavior

- **Attack Speed** - NPCs now properly respect attack speed when dealing damage, checking elapsed time before allowing hits
- **Passive Faction Protection** - NPCs in passive factions are now prevented from taking damage
- **Track Speed** - Improved NPC tracking speed handling for smoother target following
- **Invalid Texture Fallback** - NPCs with invalid or missing texture paths now gracefully fall back to a default skin instead of showing errors

---

## Additional Changes

- **Player Dialog Events** - Now work the same way as NPC Dialog Events for consistency
- **Linked Item Attack Speed** - Added attack speed configuration to linked items
- **Linked Item API** - New scripting API methods for linked items
- **Right-Click Cycling** - Multi-option NPC buttons (Hair, Eyes, Fur, etc.) can be right-clicked to cycle backwards
- **Legacy Quest Import** - Support for importing quest data from legacy CustomNPCs 1.6.4 format
- **Fullscreen GUIs** - GUIs extending the CNPC+ base can now be fullscreen and pannable
- **Menu Background** - Unified menu background applied across all CNPC+ GUI screens
- **Textfield CTRL Operations** - Text fields now support CTRL+A, CTRL+C, CTRL+V, and other standard shortcuts
- **Reflection Ban** - `java.lang.reflect` is now banned in scripts for security hardening
- **Entity Packet Loss Fix** - Server-side mixin fix for entity packet loss preventing NPCs from becoming invisible

---
