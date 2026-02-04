# ⚔️ CustomNPC+ 1.11 - Abilities ⚔️

---

The **Ability Update** introduces a full combat ability system to CNPC+! Give your NPCs powerful attacks with **visual telegraphs**, launch **energy projectiles** that home in on targets, and let players unlock abilities of their own!

---

## How Abilities Work 🎯

Every ability follows a **phase-based execution flow** that gives combat a readable, strategic feel.

**Phases:**
- **Wind-up** - The ability charges. Telegraphs appear, animations play, and observant players can react
- **Active** - The ability fires. Damage is dealt, projectiles launch, effects apply
- **Dazed** - Optional recovery state if the ability is interrupted during wind-up

**Core Configuration:**
- **Cooldown** - Time before the ability can be used again
- **Targeting Mode** - Aggro Target, Self, AOE on Self, or AOE on Target
- **Range** - Configurable min and max range for activation
- **Lock Movement** - Lock the caster's movement, rotation, or both during any phase
- **Interruptible** - Allow damage to interrupt wind-up and force a dazed state
- **Weight** - Priority weighting for NPC random ability selection

> Abilities support both **inline configuration** on individual NPCs and **global presets** that update everywhere when edited.

---

## Melee Abilities 🗡️

Close-range attacks that hit hard and punish positioning.

- **Slam** - Leap toward the target in an arc, dealing AOE damage on impact. Configurable radius, knockback, and leap height
- **Heavy Hit** - A single powerful melee strike with bonus knockback
- **Cutter** - A slashing arc attack in a cone. Configurable angle and reach
- **Shockwave** - An AOE burst radiating outward from the caster

> All melee abilities support configurable **damage**, **knockback**, and **potion effects** on hit.

---

## Energy Projectiles ✨

A brand new projectile system built from the ground up with full visual customization.

### Orbs

Homing energy spheres that track their target through the air.

- **Orb** - A single homing projectile
- **Dual Orb** - Fires two orbs simultaneously
- **Orb Barrage** - Fires a volley of orbs in rapid sequence

### Discs

Flat spinning projectiles with optional **boomerang** behavior.

- **Disc** - A single spinning disc with configurable radius and thickness
- **Dual Disc** - Fires two discs simultaneously

### Beams

Sustained energy beams that connect the caster to their target.

- **Beam** - A homing beam with configurable width and head size. Locks the caster in place during channeling
- **Dual Beam** - Fires two beams simultaneously

### Laser Shot

An **instant-fire** projectile with configurable width and speed. No homing — pure aim.

### Projectile

A standard projectile using classic Minecraft physics for a more traditional ranged attack.

### Energy Configuration

Every energy projectile shares a **modular data system** for deep customization:

- **Anchor Point** - Choose where the projectile spawns: Right Hand, Left Hand, Front, Center, Chest, or Above Head
- **Display** - Set core color, edge color, glow effect, transparency, and size
- **Homing** - Toggle homing, adjust turn rate and max tracking range
- **Combat** - Damage, knockback, vertical knockback, hit cooldown, and optional AOE splash
- **Lifespan** - Projectile speed, max age, and max travel distance

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

- **Sweeper** - A wide rotational sweep attack around the caster with configurable radius and rotation speed
- **Vortex** - Creates a pulling force that drags nearby entities toward the center while dealing damage

---

## Telegraph System 📍

**Telegraphs** are visual ground indicators that warn players before an ability lands. They appear during the wind-up phase and disappear when the ability fires.

**Shapes:**
- **Circle** - Filled circle with configurable radius
- **Ring** - Donut shape with inner and outer radius
- **Line** - Rectangle along the attack direction
- **Cone** - Fan/wedge shape with configurable angle
- **Point** - Single point indicator

**Features:**
- **Color Customization** - Set ARGB colors for both the telegraph and its warning phase
- **Warning Phase** - Color shifts in the final ticks before the ability fires
- **Entity Tracking** - Telegraphs can follow a moving entity or stay at a fixed position
- **Smooth Interpolation** - Position updates are interpolated for clean visuals

> Telegraphs are saved as **reusable presets** and can be shared across multiple abilities.

---

## Animations & Sound 🎬

Abilities integrate directly with the animation and sound systems.

- **Wind-up Animation** - Plays during the charging phase
- **Active Animation** - Plays when the ability fires
- **Animation Sync** - Optionally sync wind-up duration to match animation length exactly
- **Sound Events** - Assign custom sounds to both wind-up and active phases
- **Phase Colors** - Visual color feedback on the caster during wind-up and active

> Many ability types include **built-in animation names** that work automatically when matching animations exist.

---

## Effects & Conditions ⚡

### Potion Effects

Abilities can apply **potion effects** to any entity they hit. Each effect has configurable type, duration, and amplifier.

**Available Effects:**
Slowness, Weakness, Poison, Wither, Blindness, Nausea, Hunger, Mining Fatigue

### Conditions

Abilities support a **condition system** that controls when they can be used. Conditions are evaluated before execution and can require specific target states, ranges, or custom predicates.

---

## Player Abilities 🧑

Players can unlock and use abilities too!

- **Unlock System** - Players receive abilities via scripting or commands
- **Ability Selection** - Choose one ability at a time from your unlocked list
- **Universal Cooldown** - Shared cooldown across all player abilities

---

## Scripting API 📜

Full scripting support for ability lifecycle events:

- **StartEvent** *(Cancelable)* - Fired when wind-up begins
- **ExecuteEvent** *(Cancelable)* - Fired when the ability activates
- **InterruptEvent** - Fired when wind-up is interrupted by damage
- **CompleteEvent** - Fired when the ability finishes
- **HitEvent** *(Cancelable)* - Fired on each entity hit. Damage and knockback values can be modified
- **TickEvent** - Fired every tick with current phase and tick count

> Scripts can cancel, modify damage, and react to every phase of ability execution.

---

## Ability Preview 🔎

The ability editor includes a **live preview system**. Test your telegraph shapes, animations, and projectile visuals directly in the GUI on a dummy NPC — no world effects applied.

---

## Ability Commands 💬

- `/kam ability list` - List all custom abilities
- `/kam ability types` - List all registered ability types
- `/kam ability info <name|uuid>` - View ability details
- `/kam ability delete <uuid>` - Delete a custom ability
- `/kam ability reload` - Reload abilities from disk

---
