# AGENTS.md — noppes.npcs.scripted (Scripting API Wrappers)

## What This Is
Script-facing API layer. Wraps MC objects (entities, worlds, blocks, items, damage) into safe
interfaces for JavaScript/Java scripts. 16 root files + 3 subdirectories (entity/, event/, roles/).

## Architecture
Scripts access game objects through wrapper classes that implement `noppes.npcs.api.*` interfaces.
The `NpcAPI` singleton is bound as the `API` global in every script context.

```
Script code → API.getIEntity(mcEntity) → ScriptPlayer/ScriptNpc wrapper
           → wrapper.method() → delegates to underlying MC object
```

## Key Files

| File | Purpose |
|---|---|
| `NpcAPI.java` | Singleton API entry point. `extends AbstractNpcAPI`. Factory methods: `getIEntity()`, `getIWorld()`, `getIBlock()`, `getIItemStack()`, `createNPC()`, `spawnNPC()`. Registers handlers for all controllers. 1000+ lines. |
| `ScriptWorld.java` | World wrapper: block/entity queries, weather, time, explosions, particles, sounds |
| `ScriptBlock.java` | Block wrapper: metadata, tile entity access, neighbor queries |
| `ScriptNbt.java` | NBT wrapper for script-safe NBT manipulation |
| `ScriptDamageSource.java` | Wraps MC `DamageSource` → `IDamageSource`. Exposes type, source entity, flags (unblockable, projectile, magic) |
| `ScriptParticle.java` | Particle configuration wrapper |
| `ScriptSound.java` | Sound playback wrapper |
| `ScriptTelegraph.java` | Telegraph visual configuration |
| `ScriptBlockPos.java` | Position wrapper with arithmetic |
| `ScriptContainer.java` | Inventory container wrapper (different from controllers.ScriptContainer) |
| `ScriptTileEntity.java` | Tile entity wrapper |
| `BlockScriptedWrapper.java` | Scripted block bridge |

## entity/ (28 files) — Entity Wrapper Hierarchy
```
ScriptEntity (base — all entities)
  └─ ScriptLivingBase (health, effects, equipment)
       ├─ ScriptLiving (AI, pathfinding)
       │    ├─ ScriptNpc (NPC-specific: AI data, role, job, dialog, faction, abilities)
       │    ├─ ScriptMonster
       │    ├─ ScriptAnimal
       │    └─ ScriptVillager
       ├─ ScriptPlayer (quests, factions, currency, GUI, abilities, profile)
       │    ├─ ScriptDBCPlayer (Dragon Block C addon)
       │    └─ ScriptPixelmon (Pixelmon addon)
       └─ ScriptEnergyAbility (base for energy entities)
            ├─ ScriptEnergyOrb, ScriptEnergyDisc, ScriptEnergyLaser, ScriptEnergyBeam
            ├─ ScriptEnergySweeper, ScriptEnergyZone, ScriptEnergyDome, ScriptEnergyPanel
            ├─ ScriptEnergyProjectile, ScriptEnergySlicer, ScriptEnergyExplosion
            └─ ScriptEnergyBarrier (ScriptEnergyDome parent)
ScriptEntityItem (dropped items)
ScriptArrow, ScriptThrowable, ScriptFishHook (projectiles)
ScriptProjectile (NPC projectile wrapper)
```

**Wrapper creation**: `NpcAPI.getIEntity(Entity)` auto-detects type and returns correct wrapper.

## event/ (14 files + player/) — Script Event Types
Each file defines inner classes for specific hook points:

| File | Events |
|---|---|
| `NpcEvent.java` | Init, Tick, Update, Interact, Target, Died, Killed, Damaged, Ranged, Melee, Collide, Dialog |
| `PlayerEvent.java` (player/) | Login, Logout, Chat, Damaged, Attack, Death, Interact, PickUp, Drop, KeyPressed, ContainerOpen/Close, Timer, LevelUp, Toss |
| `BlockEvent.java` | Interact, Redstone, NeighborChanged, Timer, Harvest, Exploded, Rain, Click, FallenUpon, Collide |
| `ItemEvent.java` | Init, Tick, Attack, Interact, Toss, PickUp, Spawn, RightClick |
| `AbilityEvent.java` | WindUp, Active, Dazed, Hit, Cancelled, PhaseChange |
| `AnimationEvent.java` | Started, Ended, FrameEnter, FrameExit |
| `ProjectileEvent.java` | Impact, Tick |
| `EnergyProjectileEvent.java` | Impact, Tick, Spawn, Despawn |
| `EnergyBarrierEvent.java` | Impact, Damaged, Destroyed |
| `ChainEvent.java` | ChainStart, ChainEnd, AbilityStart, AbilityEnd |
| `ForgeEvent.java` | Generic Forge event bridge |
| `PartyEvent.java` | Invite, Join, Leave, Disband, QuestComplete |
| `LinkedItemEvent.java` | Item-specific script hooks |
| `RecipeScriptEvent.java` | Crafting event hooks |
| `CustomNPCsEvent.java` | Base event class |

## roles/ (15 files) — Scripted Role Wrappers
Wrap NPC role/job data for script access:
- `ScriptRoleTrader`, `ScriptRoleBank`, `ScriptRoleFollower`, `ScriptRoleTransporter`
- `ScriptRolePostman`, `ScriptRoleCompanion`, `ScriptRoleInnkeeper`, `ScriptRoleMount`
- `ScriptJobGuard`, `ScriptJobHealer`, `ScriptJobBard`, `ScriptJobItemGiver`
- `ScriptJobSpawner`, `ScriptJobConversation`, `ScriptJobFollower`

## Event Flow
```
Game action (e.g. NPC damaged)
  → EventHooks.onNPCDamaged()
    → ScriptController runs global scripts with EnumScriptType.DAMAGED
    → EntityNPCInterface's IScriptHandler.callScript(EnumScriptType.DAMAGED, event)
      → ScriptContainer compiles/caches script, binds API + event object
      → Script runs: event.setCancelled(true) can cancel damage
    → NpcAPI.EVENT_BUS.post(event) for addon listeners
```

## Integration Points
- **API interfaces** (`src/api/java/`): All wrappers implement `noppes.npcs.api.*` interfaces (IPlayer, ICustomNpc, IWorld, etc.)
- **ScriptContainer** (controllers/): Manages compilation, caching, and execution of script code
- **EventHooks** (noppes/npcs/): Dispatches events that create instances of event/ classes
- **Controllers**: `NpcAPI` exposes handler accessors: `getQuests()`, `getDialogs()`, `getFactions()`, `getAbilities()`, etc.

## Forbidden
- **Do NOT migrate scripted/ to core** — every wrapper holds MC objects directly
- **Do NOT change wrapper constructors** — scripts depend on `NpcAPI.getIEntity()` factory, not direct construction
- **Do NOT add MC-specific methods to api interfaces** — wrappers implement API interfaces from the submodule
- **Do NOT break event cancellation** — `setCancelled(true)` contract is used by all user scripts
