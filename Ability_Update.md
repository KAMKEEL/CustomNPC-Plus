# ⚔️ CustomNPC+ 1.11 - Abilities ⚔️

---

The **Ability Update** introduces a full combat ability system to CNPC+! Give your NPCs powerful attacks with **visual telegraphs**, launch **energy projectiles** that home in on targets, set **traps** and **hazards**, chain abilities into devastating combos, and let players unlock abilities of their own!

---

## How Abilities Work 🎯

Every ability follows a **phase-based execution flow** that gives combat a readable, strategic feel.

**Phases:**
- **Wind-up** - The ability charges. Telegraphs appear, animations play, and observant players can react
- **Active** - The ability fires. Damage is dealt, projectiles launch, effects apply
- **Dazed** - Optional recovery state if the ability is interrupted during wind-up

**Core Configuration:**
- **Cooldown** - Time before the ability can be used again. Supports both a **universal cooldown** and **per-ability cooldowns**
- **Targeting Mode** - Aggro Target, Self, AOE on Self, or AOE on Target
- **Range** - Configurable min and max range for activation
- **Lock Movement** - Lock the caster's movement, rotation, or both during any phase
- **Interruptible** - Allow damage to interrupt wind-up and force a dazed state
- **Weight** - Priority weighting for NPC random ability selection
- **Invulnerability** - Make the caster invulnerable during Wind-up, Active, or both phases
- **Ignore I-Frames** - Bypass Minecraft's damage immunity timer, allowing rapid consecutive hits
- **Free on Cast** - The ability enters cooldown the moment it launches, freeing the caster while spawned entities (projectiles, zones, barriers) persist independently
- **Magic Damage** - Abilities can deal magic damage as a separate damage type, with configurable damage values and split ratios

> Abilities support both **inline configuration** on individual NPCs and **global presets** that update everywhere when edited.

---

## Melee Abilities 🗡️

Close-range attacks that hit hard and punish positioning.

- **Slam** - Leap toward the target in an arc, dealing AOE damage on impact. Configurable radius, knockback, and leap height. Air slams deal reduced damage based on required leap height
- **Heavy Hit** - A single powerful melee strike with bonus knockback and a configurable hit delay
- **Cutter** - A slashing arc attack in a cone. Configurable angle and reach

> All melee abilities support configurable **damage**, **knockback**, and **potion effects** on hit.

---

## Energy Projectiles ✨

A brand new projectile system built from the ground up with full visual customization. Energy projectiles use a **Variant System** — each base type (Orb, Beam, Disc) can be configured with variants like dual-fire or barrage patterns, all from within the same ability type.

### Orbs

Homing energy spheres that track their target through the air. Configurable with variants for dual or barrage firing patterns.

### Discs

Flat spinning projectiles with optional **boomerang** behavior. Supports both vertical and horizontal orientation.

### Beams

Sustained energy beams that connect the caster to their target. Supports a **Free Aim** mode for manual aiming without homing.

### Laser Shot

An **instant-fire** hitscan projectile with configurable width and speed. No homing — pure aim.

### Energy Configuration

Every energy projectile shares a **modular data system** for deep customization:

- **Anchor Point** - Choose where the projectile spawns: Right Hand, Left Hand, Front, Center, Chest, or Above Head. Supports custom offset anchors and a **Launch From Anchor** setting
- **Display** - Set core color, edge color, glow effect, transparency, and size
- **Homing** - Toggle homing, adjust turn rate and max tracking range
- **Combat** - Damage, knockback, vertical knockback, hit cooldown, and optional AOE splash
- **Hit Type** - Choose how the projectile interacts with targets: **Single** (hits one), **Pierce** (passes through), or **Multi** (hits multiple)
- **Lifespan** - Projectile speed, max age, and max travel distance (up to 150 blocks)
- **Explosions** - Energy projectiles can detonate on impact with configurable explosion radius. Block damage can be toggled in the Energy Config

---

## Energy Barriers 🛡️

Defensive energy constructs that block incoming projectiles and attacks.

- **Dome** - A spherical barrier centered on the caster. Blocks incoming energy projectiles with configurable per-type damage multipliers, HP/durability, and duration
- **Wall** - A placeable flat energy panel with configurable dimensions and height offset. Has an optional **launch mode** that fires the wall forward, dealing damage and knockback
- **Shield** - A held energy panel that follows the caster's facing direction for directional blocking

**Barrier Properties:**
- **HP & Durability** - Barriers have health and break when depleted
- **Melee Vulnerability** - Barriers can be configured to take damage from melee attacks
- **Damage Absorption** - Attacks aimed at the caster can be redirected to damage the barrier instead
- **Ally/Target Filtering** - Configure which projectiles are blocked based on ownership
- **Offset Support** - Position barriers with custom offsets from the caster

---

## Defensive Abilities 🏰

Reactive defensive stances that protect the caster when hit.

- **Guard** - Enter a defensive stance that **reduces incoming damage** by a configurable percentage. Lasts until duration expires or a max hit count is reached
- **Counter** - Absorb incoming attacks and **counter-attack the attacker**. Counter damage can be a flat value or a percentage of the absorbed damage. Plays a counter-attack animation on each successful counter
- **Dodge** - Fully **evade incoming attacks**, negating all damage. Plays randomized dodge animations from up to three configurable animation slots

> All defensive abilities support configurable duration and maximum hit counts before they break.

---

## Movement Abilities 💨

Abilities that reposition the caster in combat.

- **Charge** - Rush forward in a line, damaging all entities in the path. Configurable speed, hit width, and knockback
- **Dash** - A quick directional movement without dealing damage
- **Teleport** - Instantly relocate to the target's position

> **Charge** locks direction during wind-up, so the telegraph accurately shows the attack path.

---

## AOE Abilities 🌀

Area control abilities that dominate space.

- **Shockwave** - An AOE burst radiating outward from the caster, dealing damage and knockback in a configurable radius
- **Sweeper** - A wide rotational sweep attack around the caster with configurable radius and rotation speed. Can run concurrently with other abilities in chains
- **Vortex** - Creates a pulling force that drags nearby entities toward the center while dealing damage

---

## Hazards & Traps 🪤

Ground-based zone abilities for area denial and ambush tactics.

### Hazards

Persistent damaging areas that continuously hurt entities standing inside them. Configurable damage interval, zone count (up to 20), shape (circle or square), zone height, and duration. Can optionally affect the caster.

**Zone Visual Presets:** Default, Toxic, Inferno, Arcane, Electric, Frost — each with themed colors, particles, and visual effects.

### Traps

Proximity-triggered burst abilities that sit dormant until an entity enters the trigger radius. Support configurable arm time, multiple triggers before expiring, trigger cooldown, damage radius, and knockback. Traps can be **hidden** (invisible) until triggered, revealing with a brief burst effect.

**Trap Presets:**
- **Hidden** - Minimal visibility, no effects — a pure stealth trap
- **Venom** - Applies Poison with toxic green visuals
- **Explosive** - Applies Fire with fiery orange particles
- **Cursed** - Applies Weakness with purple arcane effects
- **Shock** - Applies Mining Fatigue with blue electrical sparks
- **Snare** - Applies Slowness with icy blue visuals

### Zone Visuals

Both hazards and traps share a rich visual system with configurable layers: ground fill, concentric rings, rotating borders, accents (static, swaying, or flickering), lightning arcs, and particle effects with multiple motion types (rising, drifting, sparks).

---

## Ability Effect 💫

A support ability for applying buffs, debuffs, healing, and custom effects to allies or enemies.

**Targeting:**
- **Self** - Affect only the caster (self-buffs, self-healing)
- **AOE Self** - Area-of-effect centered on the caster with a configurable radius

**Target Filter (AOE):**
- **Allies** - Only affect friendly entities
- **Enemies** - Only affect hostile entities
- **All** - Affect everyone in range

Allies are determined by **Party** for players and **Faction** for NPCs. A toggle controls whether the caster is included in AOE effects.

**Effect Types:**
- Flat and percentage-based healing (instant or over time)
- Vanilla potion effects
- Custom effects from the Custom Effect system
- Mod-registered effect actions

**Built-in Variants:** Self Heal, Healing Aura, Poison Debuff.

---

## Custom Ability 🔧

A blank-slate ability type designed for **scripters** to build fully custom abilities through script hooks.

**Script Hooks:**
- **Start** - Fired when the ability begins
- **Execute** - Fired when entering the active phase
- **Tick** - Fired every game tick with phase and tick count
- **Complete** - Fired when the ability finishes
- **Toggle / Toggle Tick** - For toggle-mode abilities with multiple states

**Features:**
- Full telegraph control with all shape types (Circle, Ring, Line, Cone, Point, Square)
- Configurable telegraph dimensions, colors, and duration
- Supports both instant execution and multi-state toggle modes

---

## Burst System 🔁

Abilities can be configured to **fire multiple times in rapid succession** as a single use.

- **Burst Amount** - Number of additional repetitions after the initial cast
- **Burst Delay** - Ticks between each burst iteration
- **Replay Animations** - Optionally replay the wind-up animation on each burst
- **Overlap Mode** - Previous entities can persist while the next burst fires, allowing stacking effects

> Burst is available on most ability types including energy projectiles, melee abilities, and movement abilities.

---

## Chained Abilities ⛓️

Chain multiple abilities together into **sequential combos** that execute one after another.

- **Ordered Entries** - Each entry in the chain references an ability with a configurable delay before it fires
- **Wind-up All** - Optionally wind up all abilities simultaneously before the chain begins
- **Chain Cooldown** - A single cooldown applied after the entire chain completes
- **Chain Conditions** - Conditions checked once for the whole chain, not per-ability

### Concurrency

Specific ability types can be marked to run **concurrently** within a chain — executing alongside the primary ability instead of waiting in sequence. Concurrent-capable types include Effect, Hazard, Trap, and Sweeper.

> Chained abilities work with the ability hotbar and support live previewing in the editor.

---

## Telegraph System 📍

**Telegraphs** are visual ground indicators that warn players before an ability lands. They appear during the wind-up phase and transition into a warning state before the ability fires.

**Shapes:**
- **Circle** - Filled circle with configurable radius
- **Ring** - Donut shape with inner and outer radius
- **Line** - Rectangle along the attack direction
- **Cone** - Fan/wedge shape with configurable angle
- **Point** - Single point indicator
- **Square** - Square area indicator

**Features:**
- **Color Customization** - Set ARGB colors for both the telegraph and its warning phase
- **Warning Phase** - Color shifts in the final ticks before the ability fires
- **Entity Tracking** - Telegraphs can follow a moving entity or stay at a fixed position
- **Height Handling** - Telegraphs properly handle ground and air positioning
- **Custom Controls** - Full telegraph configuration available on Custom Abilities

> Telegraphs are saved as **reusable presets** and can be shared across multiple abilities.

---

## Animations & Sound 🎬

Abilities integrate directly with the animation and sound systems.

- **Wind-up Animation** - Plays during the charging phase
- **Active Animation** - Plays when the ability fires
- **Dazed Animation** - Plays during the dazed recovery state
- **Animation Sync** - Optionally sync wind-up duration to match animation length exactly
- **Sound Events** - Assign custom sounds to both wind-up and active phases
- **Phase Colors** - Visual color feedback on the caster during wind-up and active
- **Multiple Animations** - Multiple animations can be registered under one common name

> Many ability types include **built-in animation names** that work automatically when matching animations exist.

---

## Effects & Conditions ⚡

### Potion Effects

Abilities can apply **potion effects** to any entity they hit. Each effect has configurable type, duration, and amplifier. Supports both preset effect types and **manual potion IDs** for custom or modded effects with amplification up to 255.

**Available Preset Effects:**
Fire, Slowness, Weakness, Poison, Wither, Blindness, Nausea, Hunger, Mining Fatigue

### Conditions

Abilities support a **condition system** that controls when they can be used. Each condition can be checked on the **Caster**, the **Target**, or **Both**.

**Condition Types:**
- **HP Threshold** - Check if an entity's health is above, below, or equal to a value (supports flat or percentage)
- **Hit Count** - Require the caster to have been hit a certain number of times within a time window
- **Has Item** - Check if an entity is holding, wearing, or carrying a specific item
- **Quest Completed** - Check if a player has completed a specific quest
- **Has Effect** - Check if an entity has a specific custom effect applied

---

## Player Abilities 🧑

Players can unlock and use abilities with a full hotbar and management system!

### Ability Hotbar

The **Ability Hotbar** is a 12-slot bar for equipping and switching between unlocked abilities. Hold the **HUD Key** and use the **scroll wheel** to cycle through your equipped abilities. The hotbar displays ability icons with cooldown progress and supports horizontal or vertical layouts with 3, 5, or 7 visible slots.

Configure your hotbar through drag-and-drop in the **CNPC+ Inventory Ability Tab**, and customize its appearance in the **HUD Editor** under CNPC+ Inventory Settings.

### Player Features
- **Unlock System** - Players receive abilities via scripting or commands
- **Per-Ability Cooldowns** - Each ability can have its own cooldown in addition to the universal cooldown
- **Cancel on Double-Tap** - Double-tap the ability key during wind-up to cancel and immediately enter cooldown
- **Ability Visibility** - Hide or show specific abilities in the HUD and usage
- **State Management** - Abilities are properly cancelled on logout, dimension change, death, and respawn. Movement input is suppressed during ability locks
- **Flight Safety** - Abilities do not affect players who are flying

---

## Scripting API 📜

Full scripting support for ability lifecycle events:

- **StartEvent** *(Cancelable)* - Fired when wind-up begins
- **ExecuteEvent** *(Cancelable)* - Fired when the ability activates
- **InterruptEvent** - Fired when wind-up is interrupted by damage
- **CompleteEvent** - Fired when the ability finishes
- **HitEvent** *(Cancelable)* - Fired on each entity hit. Damage and knockback values can be modified
- **TickEvent** - Fired every tick with current phase and tick count
- **PlayerAbilityEvent** - Events for player ability actions

### Energy Projectile Scripting

Full scripting interfaces for all energy projectile types with 5 event hooks:
- **Fired** - When the projectile launches
- **Update** - Every tick during flight
- **EntityImpact** - On hitting an entity
- **BlockImpact** - On hitting a block
- **Expired** - When the projectile's lifespan ends

### Ability Extender System

Register custom **lifecycle hooks** on abilities with the Ability Extender system:
- **onAbilityStart** - When the ability begins
- **onAbilityTick** - Every tick during execution
- **onAbilityComplete** - When the ability finishes
- **onAbilityDamage** - When the ability deals damage

Multiple extenders can be registered per ability. Ability scripts are **instanced** — every use gets its own fresh set of variables for optimal performance.

> Scripts can cancel, modify damage, and react to every phase of ability execution.

---

## Ability Preview 🔎

The ability editor includes a **live preview system**. Test your telegraph shapes, animations, projectile visuals, burst patterns, and chained ability sequences directly in the GUI on a dummy NPC — no world effects applied.

---

## Ability Commands 💬

- `/kam ability list` - List all custom abilities
- `/kam ability types` - List all registered ability types
- `/kam ability prebuilts` - List all built-in abilities
- `/kam ability info <name>` - View ability details
- `/kam ability delete <name>` - Delete a custom ability
- `/kam ability reload` - Reload abilities from disk
- `/kam ability give` - Give an ability to a player
- `/kam ability remove` - Remove an ability from a player
- `/kam ability giveChain` - Give a chained ability to a player
- `/kam ability removeChain` - Remove a chained ability from a player
- `/kam ability player` - Manage player abilities

---
