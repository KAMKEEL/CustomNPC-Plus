# NPC, Animation & Tool Improvements

A collection of improvements to NPCs, animations, items, and tools alongside the major feature updates.

## Cloner Tab Overhaul
The Cloner now supports **folders and subfolders** for organizing all cloner categories (animations, abilities, forms, auras, outlines, effects, linked items). Full-width directory browser, improved tab navigation, and tag-based quick filtering.

## Animation Improvements
- **Data Store** for passing data across animation events and frames
- **Task System** - Consumer-based animation lifecycle events
- **Expanded scripting API**

## Animated Textures
**Frame-based animated textures** for linked/scripted items, custom effect icons, ability hotbar icons, and scripted GUI textured rectangles. Configurable frames per row, ticks per frame, and interpolation.

## NPC API Additions
- **Hitbox Data** (`IHitboxData`) - Independent width/height scaling
- **Tint Data** (`ITintData`) - Hurt tint and persistent color tints with alpha
- **Behavior Properties** - Sprint, swim, doors, fire reactions, tactical chance, walking range now scriptable
- **Sound Control** (`ISound`) - Volume, pitch, repeat, position
- **Skin Overlay Colors** - `getColor()`/`setColor()` for tinting overlay layers
- **Knockback API** - With support for respecting/ignoring knockback resistance
- **Six Overlays in Singleplayer** now work properly
- **Error Reporting** - Script exceptions report to Script Console with stack traces

## Natural Spawn Improvements
Improved GUI, runtime control, despawn handling, and fixed biome spawn persistence.

## NPC Combat & Behavior
- NPCs now properly respect **attack speed** before allowing hits
- **Passive Faction Protection** prevents damage to passive faction NPCs
- Improved **track speed** handling for smoother target following
- **Invalid Texture Fallback** to default skin instead of errors

## Additional Changes
- Player Dialog Events now work like NPC Dialog Events
- Linked Item attack speed configuration and new scripting API methods
- Right-click cycling on multi-option NPC buttons (Hair, Eyes, Fur, etc.)
- Legacy Quest Import (CustomNPCs 1.6.4 format)
- Fullscreen & pannable GUIs, unified menu background
- Textfield CTRL shortcuts (A, C, V, etc.)
- `java.lang.reflect` banned in scripts for security
- Server-side fix for entity packet loss (invisible NPCs)

# Bug Fixes

## Scripting Engine
- Fixed script config sync between server and client
- Fixed Bukkit integration compatibility

## Quests & Dialogs
- Fixed quest cooldown for MC Custom and RL Custom timer types
- Fixed quest/dialog modification names, saving, and category assignment
- Fixed bard music restarting/duplicating on dialog open

## NPCs
- Fixed spawner yaw using camera yaw instead of proper facing
- Fixed biome spawn settings not persisting
- Fixed NPC invisibility from request client packet issues
- Fixed track speed handling and set speed edge cases
- Fixed missing spawn data, block waypoint pathing, and job spawner behavior
- Fixed hitbox scaling and collision issues

## Animations
- Fixed animation offsets conflicting with vanilla offsets
- Fixed lookup failures, syncing, resync on teleport
- Fixed wrong names on registration and controller loading twice on startup

## Mounts
- Fixed movement jitter (now follows vanilla horse/boat sync pattern)
- Fixed dismount edge cases
