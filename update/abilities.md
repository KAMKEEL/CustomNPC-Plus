# Abilities

---

The **Ability System** adds full combat abilities to CustomNPC+. Give your NPCs powerful attacks with visual telegraphs, launch energy projectiles, set traps, chain abilities into combos, and let players unlock abilities of their own.

---

## How Abilities Work

Every ability follows a **phase-based flow**:

- **Wind-up** - The ability charges up. Telegraphs appear on the ground, animations play, and observant players can react
- **Active** - The ability fires. Damage is dealt, projectiles launch, effects apply
- **Dazed** - Optional recovery state if the ability gets interrupted during wind-up

**Settings you can configure:**
- **Cooldown** - Time before the ability can be used again. Supports both a universal cooldown and per-ability cooldowns
- **Targeting** - Aggro Target, Self, AOE on Self, or AOE on Target
- **Range** - Minimum and maximum range for activation
- **Lock Movement** - Lock the caster's movement, rotation, or both during any phase
- **Interruptible** - Allow damage to interrupt the wind-up and force a dazed state
- **Weight** - Priority weighting for NPC ability selection
- **Invulnerability** - Make the caster invulnerable during Wind-up, Active, or both
- **Ignore I-Frames** - Bypass the damage immunity timer for rapid consecutive hits
- **Free on Cast** - The ability enters cooldown the moment it fires, freeing the caster while projectiles or zones persist on their own
- **Magic Damage** - Deal magic damage as a separate damage type with configurable values and split ratios

> Abilities can be configured directly on individual NPCs or saved as **global presets** that update everywhere when edited.

---

## Melee Abilities

Close-range attacks for punishing positioning.

- **Slam** - Leap toward the target in an arc, dealing AOE damage on impact. Configure the radius, knockback, and leap height. Air slams deal reduced damage based on the required leap height
- **Heavy Hit** - A single powerful melee strike with bonus knockback and a configurable hit delay
- **Cutter** - A slashing arc attack in a cone. Configure the angle and reach

> All melee abilities support configurable damage, knockback, and potion effects on hit.

---

## Energy Projectiles

A full projectile system with deep visual customization. Energy projectiles use a **Variant System** — each base type can be configured with variants like dual-fire or barrage patterns.

### Types

- **Orb** - Homing energy spheres that track their target. Supports dual-fire and barrage variants
- **Disc** - Flat spinning projectiles with optional boomerang behavior. Supports vertical and horizontal orientation
- **Beam** - Sustained energy beams connecting the caster to their target. Supports Free Aim mode for manual aiming
- **Laser Shot** - Instant-fire hitscan projectile with configurable width and speed. No homing

### Projectile Settings

Every energy projectile shares these configurable properties:

- **Anchor Point** - Where the projectile spawns: Right Hand, Left Hand, Front, Center, Chest, or Above Head. Supports custom offset anchors
- **Display** - Core color, edge color, glow effect, transparency, and size
- **Homing** - Toggle homing, adjust turn rate and max tracking range
- **Combat** - Damage, knockback, vertical knockback, hit cooldown, and optional AOE splash
- **Hit Type** - How the projectile interacts: Single (hits one), Pierce (passes through), or Multi (hits multiple)
- **Lifespan** - Speed, max age, and max travel distance (up to 150 blocks)
- **Explosions** - Detonate on impact with configurable radius. Block damage can be toggled

---

## Energy Barriers

Defensive energy constructs that block incoming attacks.

- **Dome** - A spherical barrier centered on the caster. Blocks incoming projectiles with configurable damage multipliers, HP, and duration
- **Wall** - A flat energy panel with configurable dimensions. Has an optional launch mode that fires the wall forward, dealing damage and knockback
- **Shield** - A held energy panel that follows the caster's facing direction

**Barrier Properties:**
- HP and durability — barriers break when depleted
- Configurable melee vulnerability
- Damage absorption — attacks on the caster can be redirected to the barrier
- Ally/target filtering for which projectiles are blocked
- Custom position offsets

---

## Defensive Abilities

Reactive stances that protect the caster when hit.

- **Guard** - Reduces incoming damage by a configurable percentage. Lasts until duration expires or max hit count is reached
- **Counter** - Absorbs attacks and counter-attacks the attacker. Counter damage can be flat or a percentage of absorbed damage. Plays a counter animation on each trigger
- **Dodge** - Fully evades incoming attacks, negating all damage. Plays randomized dodge animations from up to three configurable animation slots

> All defensive abilities support configurable duration and max hit counts.

---

## Movement Abilities

Reposition the caster during combat.

- **Charge** - Rush forward in a line, damaging everything in the path. Configure speed, hit width, and knockback
- **Dash** - Quick directional movement without dealing damage
- **Teleport** - Instantly relocate to the target's position

> Charge locks direction during wind-up, so the telegraph accurately shows the attack path.

---

## AOE Abilities

Area control abilities that dominate space.

- **Shockwave** - AOE burst from the caster, dealing damage and knockback in a configurable radius
- **Sweeper** - Wide rotational sweep around the caster with configurable radius and rotation speed. Can run alongside other abilities in chains
- **Vortex** - Creates a pulling force that drags nearby entities toward the center while dealing damage

---

## Hazards & Traps

Ground-based abilities for area denial and ambushes.

### Hazards

Persistent damaging zones that continuously hurt entities inside them. Configure the damage interval, number of zones (up to 20), shape (circle or square), zone height, and duration. Can optionally affect the caster.

**Visual Presets:** Default, Toxic, Inferno, Arcane, Electric, Frost — each with themed colors, particles, and effects.

### Traps

Proximity-triggered abilities that sit dormant until an entity enters the trigger radius. Configure arm time, number of triggers, trigger cooldown, damage radius, and knockback. Traps can be hidden (invisible) until triggered.

**Trap Presets:**
- **Hidden** - Minimal visibility, pure stealth trap
- **Venom** - Applies Poison with toxic green visuals
- **Explosive** - Applies Fire with fiery orange particles
- **Cursed** - Applies Weakness with purple arcane effects
- **Shock** - Applies Mining Fatigue with blue electrical sparks
- **Snare** - Applies Slowness with icy blue visuals

### Zone Visuals

Both hazards and traps have a rich visual system with configurable ground fill, concentric rings, rotating borders, accents (static, swaying, or flickering), lightning arcs, and particle effects with multiple motion types.

---

## Ability Effect

A support ability for applying buffs, debuffs, healing, and custom effects.

**Targeting:**
- **Self** - Affect only the caster
- **AOE Self** - Area-of-effect centered on the caster

**Target Filter (AOE):**
- **Allies** - Only friendly entities (determined by Party for players, Faction for NPCs)
- **Enemies** - Only hostile entities
- **All** - Everyone in range

**What it can apply:**
- Flat and percentage-based healing (instant or over time)
- Vanilla potion effects
- Custom effects from the Custom Effect system
- Mod-registered effect actions

**Built-in Variants:** Self Heal, Healing Aura, Poison Debuff.

---

## Custom Ability

A blank-slate ability type for scripters to build fully custom abilities through script hooks.

**Script Hooks:**
- **Start** - When the ability begins
- **Execute** - When entering the active phase
- **Tick** - Every game tick with phase and tick count
- **Complete** - When the ability finishes
- **Toggle / Toggle Tick** - For toggle-mode abilities with multiple states

Full telegraph control with all shape types, configurable dimensions, colors, and duration. Supports both instant execution and multi-state toggle modes.

---

## Burst System

Abilities can fire **multiple times in rapid succession** as a single use.

- **Burst Amount** - Number of additional repetitions after the initial cast
- **Burst Delay** - Ticks between each burst
- **Replay Animations** - Optionally replay the wind-up animation on each burst
- **Overlap Mode** - Previous entities persist while the next burst fires, allowing stacking effects

> Available on most ability types including energy projectiles, melee abilities, and movement abilities.

---

## Chained Abilities

Chain multiple abilities into **sequential combos** that execute one after another.

- **Ordered Entries** - Each entry references an ability with a configurable delay before it fires
- **Wind-up All** - Optionally wind up all abilities simultaneously before the chain begins
- **Chain Cooldown** - A single cooldown after the entire chain completes
- **Chain Conditions** - Conditions checked once for the whole chain, not per-ability

### Concurrency

Specific ability types can run **concurrently** within a chain — executing alongside the primary ability instead of waiting. Concurrent-capable types include Effect, Hazard, Trap, and Sweeper.

> Chained abilities work with the ability hotbar and support live previewing in the editor.

---

## Telegraph System

**Telegraphs** are visual ground indicators that warn players before an ability lands. They appear during wind-up and shift color before the ability fires.

**Shapes:**
- **Circle** - Filled circle with configurable radius
- **Ring** - Donut shape with inner and outer radius
- **Line** - Rectangle along the attack direction
- **Cone** - Fan/wedge shape with configurable angle
- **Point** - Single point indicator
- **Square** - Square area indicator

**Features:**
- ARGB color customization for both telegraph and warning phase
- Color shifts in the final ticks before firing
- Can follow a moving entity or stay at a fixed position
- Proper ground and air height handling
- Saved as reusable presets shared across abilities

---

## Animations & Sound

Abilities integrate directly with the animation and sound systems.

- **Wind-up Animation** - Plays during charging
- **Active Animation** - Plays when the ability fires
- **Dazed Animation** - Plays during recovery
- **Animation Sync** - Optionally match wind-up duration to animation length
- **Sound Events** - Custom sounds for wind-up and active phases
- **Phase Colors** - Visual color feedback on the caster during wind-up and active
- **Multiple Animations** - Multiple animations can be registered under one common name

> Many ability types include built-in animation names that work automatically when matching animations exist.

---

## Effects & Conditions

### Potion Effects

Abilities can apply potion effects to any entity they hit. Each effect has a configurable type, duration, and amplifier. Supports preset effect types and manual potion IDs for modded effects with amplification up to 255.

**Preset Effects:** Fire, Slowness, Weakness, Poison, Wither, Blindness, Nausea, Hunger, Mining Fatigue

### Conditions

Conditions control when an ability can be used. Each condition can check the **Caster**, the **Target**, or **Both**.

- **HP Threshold** - Health above, below, or equal to a value (flat or percentage)
- **Hit Count** - Caster hit a certain number of times within a time window
- **Has Item** - Entity holding, wearing, or carrying a specific item
- **Quest Completed** - Player has completed a specific quest
- **Has Effect** - Entity has a specific custom effect applied

---

## Player Abilities

Players can unlock and use abilities with a full hotbar system.

### Ability Hotbar

The **Ability Hotbar** is a 12-slot bar for equipping abilities. Hold the **HUD Key** and use the **scroll wheel** to cycle through equipped abilities. The hotbar shows ability icons with cooldown progress and supports horizontal or vertical layouts with 3, 5, or 7 visible slots.

Configure your hotbar through drag-and-drop in the **CNPC+ Inventory Ability Tab**, and customize its appearance in the **HUD Editor** under CNPC+ Inventory Settings.

### Player Features
- **Unlock System** - Players receive abilities via scripting or commands
- **Per-Ability Cooldowns** - Each ability can have its own cooldown on top of the universal cooldown
- **Cancel on Double-Tap** - Double-tap the ability key during wind-up to cancel and enter cooldown immediately
- **Ability Visibility** - Hide or show specific abilities in the HUD
- **State Management** - Abilities are properly cancelled on logout, dimension change, death, and respawn. Movement input is suppressed during ability locks
- **Flight Safety** - Abilities do not affect players who are flying

---

## Scripting API

Full scripting support for ability lifecycle events:

- **StartEvent** *(Cancelable)* - When wind-up begins
- **ExecuteEvent** *(Cancelable)* - When the ability activates
- **InterruptEvent** - When wind-up is interrupted
- **CompleteEvent** - When the ability finishes
- **HitEvent** *(Cancelable)* - On each entity hit. Damage and knockback can be modified
- **TickEvent** - Every tick with current phase and tick count
- **PlayerAbilityEvent** - For player ability actions

### Energy Projectile Scripting

Full scripting for all energy projectile types:
- **Fired** - When the projectile launches
- **Update** - Every tick during flight
- **EntityImpact** - On hitting an entity
- **BlockImpact** - On hitting a block
- **Expired** - When the projectile's lifespan ends

### Ability Extender System

Register custom lifecycle hooks on abilities:
- **onAbilityStart** - When the ability begins
- **onAbilityTick** - Every tick during execution
- **onAbilityComplete** - When the ability finishes
- **onAbilityDamage** - When the ability deals damage

Multiple extenders can be registered per ability. Scripts are instanced — every use gets its own fresh set of variables.

---

## Ability Preview

The ability editor includes a **live preview system**. Test telegraph shapes, animations, projectile visuals, burst patterns, and chained ability sequences directly in the GUI on a dummy NPC — no world effects applied.

---

## Commands

- `/kam ability list` - List all custom abilities
- `/kam ability types` - List all registered ability types
- `/kam ability prebuilts` - List all built-in abilities
- `/kam ability info <name>` - View ability details
- `/kam ability delete <name>` - Delete a custom ability
- `/kam ability reload` - Reload abilities from disk
- `/kam ability give <player> <ability>` - Give an ability to a player
- `/kam ability remove <player> <ability>` - Remove an ability from a player
- `/kam ability giveChain <player> <chain>` - Give a chained ability to a player
- `/kam ability removeChain <player> <chain>` - Remove a chained ability from a player
- `/kam ability player` - Manage player abilities

---
