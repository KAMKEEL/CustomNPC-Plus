# CNPC+ 1.11 Ability System Beta

🟡  **In Testing**



## Features: ⚒️

- **Phase-Based Execution:**

> All abilities follow a Wind-up → Active → Completion flow. Wind-up lets players react, Active deals the effect.



- **Melee Abilities:**

> - **Slam** - Leap and AOE on impact.
> - **Heavy Hit** - Single powerful strike with bonus knockback.
> - **Cutter** - Slashing cone arc attack.
> - **Shockwave** - AOE burst radiating from the caster.



- **Energy Orbs:**

> - **Orb** - Homing energy sphere.
> - **Dual Orb** - Two simultaneous homing orbs.
> - **Orb Barrage** - Rapid volley of orbs in sequence.



- **Energy Discs:**

> - **Disc** - Spinning flat projectile with optional boomerang.
> - **Dual Disc** - Two simultaneous discs.



- **Energy Beams:**

> - **Beam** - Sustained homing beam from caster to target.
> - **Dual Beam** - Two simultaneous beams.



- **Laser Shot:**

> Instant-fire projectile with no homing. Pure aim.



- **Projectile:**

> Standard projectile using classic Minecraft physics.



- **Movement Abilities:**

> - **Charge** - Rush forward in a line, damaging everything in path.
> - **Dash** - Quick directional movement without damage.
> - **Teleport** - Instant relocation to the target.



- **AOE Abilities:**

> - **Sweeper** - Wide rotational sweep around the caster.
> - **Vortex** - Pulls entities inward while dealing damage.



- **Telegraph System:**

> Visual ground indicators during wind-up. Supports Circle, Ring, Line, Cone, and Point shapes with color customization and warning phase transitions.



- **Energy Projectile Customization:**

> Modular data system for all energy projectiles: Anchor Point, Display Colors, Homing, Combat Stats, and Lifespan.



- **Ability Effects:**

> Abilities can apply potion effects on hit: Slowness, Weakness, Poison, Wither, Blindness, Nausea, Hunger, Mining Fatigue.



- **Ability Conditions:**

> Condition system controlling when abilities can execute based on target state and custom predicates.



- **Interruptible Wind-ups:**

> Abilities can be interrupted by damage during wind-up, forcing a configurable Dazed recovery state.



- **Movement & Rotation Locking:**

> Lock caster movement, rotation, or both during Wind-up, Active, or both phases.



- **Animation & Sound Integration:**

> Wind-up and Active animations with optional duration sync. Custom sound events per phase.



- **Player Abilities:**

> Players can unlock abilities, select one at a time, and share a universal cooldown.



- **Ability Preview:**

> Live preview in the ability editor GUI. Test telegraphs, animations, and projectiles on a dummy NPC.



- **Scripting API:**

> Full event support: Start, Execute, Interrupt, Complete, Hit (modifiable damage/knockback), and Tick events. Start, Execute, and Hit are cancelable.



- **Ability Commands:**

> `/kam ability list`, `types`, `info`, `delete`, `reload` for managing custom abilities.



**Extras**

- Global ability presets that update everywhere when edited.
- Weighted ability selection for NPC random usage.
- Configurable min/max range per ability.
- Reusable telegraph presets shared across abilities.
