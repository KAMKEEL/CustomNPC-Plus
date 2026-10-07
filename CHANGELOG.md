# CustomNPC+ 1.11 Changelog

## NPC 引路
- `/guide <NPC名>`（别名 `/yl`）在世界里画出通往目标 NPC 的路径，所有玩家可用，不需要 OP
- 死亡空间风格的贴地发光带：能量脉冲沿线流向目标，近处清晰、20 格外淡出，往前走前方线段依次浮现
- 终点菱形标记，15 格内开始渐显、5 格内完全不透明
- 远距离自动分段引路：每秒重算一段，走一段算一段
- `/guide stop` 停止，`/guide` 查看当前目标与距离
- 已知限制：目标 NPC 所在区块未加载时找不到；不支持跨维度

## Ability System
- Phase-based abilities (Wind-up, Active, Dazed) with configurable cooldowns, targeting, range, interrupts, invulnerability, and magic damage
- **Melee:** Slam, Heavy Hit, Cutter
- **Energy Projectiles:** Orb, Disc, Beam, Laser Shot with variant system, homing, hit types (Single/Pierce/Multi), size interpolation, explosions
- **Barriers:** Dome, Wall, Shield with HP, reflection, damage absorption
- **Defensive:** Guard, Counter, Dodge
- **Movement:** Charge, Dash, Teleport
- **AOE:** Shockwave, Sweeper, Vortex
- **Hazards & Traps:** Persistent zones and proximity triggers with visual presets
- **Effect:** Healing, buffs, debuffs with target filtering (Allies/Enemies/All)
- **Custom Ability:** Blank-slate type with full script hooks and toggle modes
- Burst system, chained ability combos with concurrency, telegraph system (6 shapes)
- Per-phase animations and sound events
- Ability conditions: HP Threshold, Hit Count, Has Item, Quest Completed, Has Effect
- 12-slot player ability hotbar with HUD editor
- Full scripting API for ability and energy projectile lifecycle events
- Ability Extender system for custom hooks
- Live preview in editor
- Commands: `/kam ability list|types|prebuilts|info|delete|reload|give|remove|giveChain|removeChain|player`

## Auction House
- Global marketplace with bidding, buyout, and snipe protection
- Built-in currency system (configurable, profile-persistent, Vault support)
- Listing fees, sales tax, minimum prices
- My Trades panel with claim management (20-day expiry)
- Search/sort, notifications (online + offline summary)
- Trade slots (default 8, expandable via permissions)
- Item blacklist (by item, wildcard, mod, NBT; soulbound always blocked)
- Auctioneer NPC role
- Admin GUI and logging
- Cancelable scripting events: Create, Bid, Buyout, Cancel, Claim

## Script Editor
- Full code editor with syntax highlighting, autocomplete, error detection, and hover tooltips
- Go to Line, Search/Replace, Go to Definition, Fullscreen
- Undo/Redo, Duplicate Line, Comment Toggle, Rename, Move Lines
- Line numbers, smooth scrolling, indent guides, brace matching, smart brackets

## Java Scripting
- Write scripts in Java via Janino compiler with automatic hook resolution
- Per-tab language selector (mix Java and JavaScript)
- External `.java` file support

## Client-Side Scripting
- Server-controlled client script execution with auto-sync (off by default)

## Addon API Support
- Ship `.d.ts` type definitions for automatic script editor autocomplete
- Patch support for extending CNPC+ types, hot reload

## Traders
- Stock system: Global or Per-Player modes with configurable restock timers
- Currency trades (items + coins)
- Client-side balance preview

## Cloner
- Folder and subfolder organization for all categories
- Full-width directory browser, improved tabs, tag-based filtering

## Animations
- Data store for passing data across events/frames
- Consumer-based task system
- Expanded scripting API

## Animated Textures
- Frame-based animation for items, effect icons, ability hotbar, scripted GUIs

## NPC API
- `IHitboxData` (width/height scaling), `ITintData` (hurt tint, persistent tints)
- Scriptable behavior: sprint, swim, doors, fire reactions, tactical chance, walking range
- `ISound` (volume, pitch, repeat, position)
- Skin overlay `getColor()`/`setColor()`, knockback API
- Six overlays in singleplayer, script error reporting to console

## NPC Combat & Behavior
- Attack speed enforcement, passive faction protection
- Improved track speed, invalid texture fallback

## Other Changes
- Player Dialog Events match NPC Dialog Events
- Linked Item attack speed and scripting API
- Right-click cycling on multi-option buttons
- Legacy quest import (1.6.4), fullscreen/pannable GUIs, unified menu background
- Textfield CTRL shortcuts, `java.lang.reflect` banned in scripts
- Entity packet loss fix (invisible NPCs)
- Natural spawn GUI, runtime control, and despawn improvements

## Bug Fixes
- Script config sync, Bukkit integration
- Quest cooldown for MC/RL Custom timers, quest/dialog saving and categories
- Bard music restart on dialog open
- Spawner yaw, biome spawn persistence, NPC invisibility
- Track speed, set speed edge cases, spawn data, block waypoints, job spawner
- Hitbox scaling and collision
- Animation offsets, lookup failures, syncing, resync on teleport, registration names, double loading
- Mount movement jitter, dismount edge cases
