# Abilities

---

The **Ability System** adds full combat abilities to CustomNPC+. Give NPCs powerful attacks with visual telegraphs, launch energy projectiles, set traps, chain abilities into combos, and let players unlock abilities of their own.

---

## How Abilities Work

Every ability follows a **phase-based flow**: **Wind-up** (charge with telegraphs/animations) → **Active** (fire, deal damage, apply effects) → **Dazed** (optional recovery if interrupted).

**Core Settings:** Cooldown (universal + per-ability), Targeting (Aggro/Self/AOE Self/AOE Target), Range (min/max), Lock Movement/Rotation per phase, Interruptible wind-up, Weight (NPC priority), Invulnerability per phase, Ignore I-Frames, Free on Cast, and Magic Damage (separate type with split ratios).

> Abilities can be configured on individual NPCs or saved as **global presets**.

---

## Melee Abilities

- **Slam** - Leap toward target, AOE on impact. Configurable radius, knockback, leap height. Air slams deal reduced damage
- **Heavy Hit** - Powerful single strike with bonus knockback and configurable hit delay
- **Cutter** - Slashing arc in a cone with configurable angle and reach

> All melee abilities support configurable damage, knockback, and potion effects on hit.

---

## Energy Projectiles

A full projectile system with a **Variant System** for patterns like dual-fire or barrages.

**Types:** Orb (homing spheres, dual-fire/barrage variants), Disc (spinning, optional boomerang, vertical/horizontal), Beam (sustained, Free Aim mode), Laser Shot (instant hitscan, no homing).

**Shared Settings:** Anchor Point (7 presets + custom offset), Display (colors, glow, transparency, size), Homing (toggle, turn rate, range), Combat (damage, knockback, AOE splash), Hit Type (Single/Pierce/Multi with max hit count), Lifespan (speed, max age, max distance), Size Interpolation, Proximity Alpha, and Explosions (configurable radius, toggleable block damage).

---

## Energy Barriers

Defensive constructs that block incoming attacks.

- **Dome** - Spherical barrier around the caster
- **Wall** - Flat panel with optional launch mode (fires forward dealing damage/knockback)
- **Shield** - Held panel following the caster's facing direction

**Properties:** HP/durability, melee vulnerability toggle, damage absorption (redirects caster damage to barrier), projectile reflection, ally/target filtering, custom position offsets.

---

## Defensive Abilities

- **Guard** - Reduces incoming damage by configurable %. Ends on duration or max hit count
- **Counter** - Absorbs attacks and counter-attacks. Flat or percentage-based counter damage with counter animation
- **Dodge** - Fully evades incoming attacks. Randomized dodge animations from up to 3 slots

---

## Movement Abilities

- **Charge** - Rush forward dealing damage in path. Configurable speed, hit width, knockback. Direction locks during wind-up
- **Dash** - Quick directional movement without damage. Supports directional key input
- **Teleport** - Instant relocate to target's position

---

## AOE Abilities

- **Shockwave** - AOE burst from caster with configurable radius and knockback
- **Sweeper** - Rotational sweep around caster. Can run alongside other abilities in chains
- **Vortex** - Pulling force dragging entities toward center while dealing damage

---

## Hazards & Traps

### Hazards

Persistent damaging zones with configurable damage interval, zone count (up to 20), shape (circle/square), height, and duration. Can affect caster.

**Visual Presets:** Default, Toxic, Inferno, Arcane, Electric, Frost.

### Traps

Proximity-triggered, sit dormant until an entity enters trigger radius. Configurable arm time, trigger count, cooldown, damage radius, knockback. Can be hidden until triggered.

**Presets:** Hidden, Venom (Poison), Explosive (Fire), Cursed (Weakness), Shock (Mining Fatigue), Snare (Slowness).

### Zone Visuals

Both have configurable ground fill, concentric rings, rotating borders, accents (static/swaying/flickering), lightning arcs, and particles with multiple motion types.

---

## Ability Effect

Support ability for buffs, debuffs, healing, and custom effects.

**Targeting:** Self or AOE Self (with Allies/Enemies/All filter based on Party/Faction).

**Applies:** Flat and percentage healing (instant or over time), vanilla potion effects, custom effects, and mod-registered effect actions.

**Built-in Variants:** Self Heal, Healing Aura, Poison Debuff.

---

## Custom Ability

Blank-slate ability for scripters with hooks: **Start**, **Execute**, **Tick** (with phase/count), **Complete**, **Toggle / Toggle Tick** (multi-state). Full telegraph control with all shapes. Supports instant and multi-state toggle modes.

---

## Burst System

Abilities fire **multiple times in rapid succession** as a single use.

- **Burst Amount** - Additional repetitions after initial cast
- **Burst Delay** - Ticks between each burst
- **Replay Animations** and **Overlap Mode** (previous entities persist during next burst)

> Available on most ability types including energy projectiles, melee, and movement abilities.

---

## Chained Abilities

Chain abilities into **sequential combos** with configurable delays between entries. Options: Wind-up All (simultaneous wind-up), Chain Cooldown, Chain Conditions (checked once for the whole chain).

**Concurrency:** Effect, Hazard, Trap, and Sweeper types can execute alongside the primary ability instead of waiting.

> Works with the ability hotbar and supports live previewing in the editor.

---

## Telegraph System

Visual ground indicators during wind-up that shift color before firing.

**Shapes:** Circle, Ring (donut), Line (rectangle), Cone (fan/wedge), Point, Square.

**Features:** ARGB color customization for telegraph and warning phases, entity-following or fixed position, proper ground/air height handling, saved as reusable presets.

---

## Animations & Sound

Per-phase animations (Wind-up, Active, Dazed) with optional animation-synced duration. Custom sound events for wind-up and active phases. Phase colors on the caster. Multiple animations can be registered under one common name.

---

## Effects & Conditions

### Potion Effects

Abilities can apply potion effects on hit with configurable type, duration, and amplifier (up to 255). Supports modded effects via manual potion IDs.

**Presets:** Fire, Slowness, Weakness, Poison, Wither, Blindness, Nausea, Hunger, Mining Fatigue.

### Conditions

Control when abilities can be used. Each checks **Caster**, **Target**, or **Both**:

- **HP Threshold** - Above/below/equal (flat or percentage)
- **Hit Count** - Times hit within a time window
- **Has Item** - Holding, wearing, or carrying a specific item
- **Quest Completed** - Player completed a specific quest
- **Has Effect** - Has a specific custom effect

---

## Player Abilities

### Ability Hotbar

12-slot bar accessed by holding **HUD Key** + **scroll wheel**. Shows icons with cooldown progress. Horizontal or vertical layout with 3, 5, or 7 visible slots. Configured via drag-and-drop in the CNPC+ Inventory Ability Tab, customized in the HUD Editor.

### Player Features

Unlock via scripting/commands, per-ability cooldowns, cancel on double-tap during wind-up, ability visibility toggle, proper state management (logout/dimension/death/respawn cleanup, movement suppression during locks), and flight safety.

---

## Scripting API

**Ability Events:** StartEvent, ExecuteEvent (both cancelable), InterruptEvent, CompleteEvent, HitEvent (cancelable, modifiable damage/knockback), TickEvent (phase + count), PlayerAbilityEvent.

**Energy Projectile Events:** Fired, Update, EntityImpact, BlockImpact, Expired.

**Ability Extender System:** Register custom lifecycle hooks (onAbilityStart, onAbilityTick, onAbilityComplete, onAbilityDamage) per ability. Scripts are instanced per use.

---

## Ability Preview

Live preview in the editor — test telegraphs, animations, projectiles, burst patterns, and chains on a dummy NPC with no world effects.

---

## Commands

- `/kam ability list|types|prebuilts` - List abilities, types, or built-ins
- `/kam ability info|delete|reload <name>` - View, delete, or reload abilities
- `/kam ability give|remove <player> <ability>` - Player ability management
- `/kam ability giveChain|removeChain <player> <chain>` - Chain management
- `/kam ability player` - Manage player abilities

---
