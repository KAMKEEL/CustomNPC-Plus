# AGENTS.md — noppes.npcs (NPC Core Logic)

## What This Is
MC1710 NPC entity system: mod entry point, NPC data models, AI tasks, entity types, roles/jobs,
event dispatching, server lifecycle, and utility classes. ~37 root files + 4 major subdirectories.

## Key Files

| File | Purpose |
|---|---|
| `CustomNpcs.java` | `@Mod` entry point. Registers entities, controllers, event buses, platform service. Server lifecycle via `@EventHandler` methods. |
| `EntityNPCInterface` (entity/) | Base class for ALL NPC entities. Owns Data* classes. Handles NBT save/load, AI task registration, combat. |
| `EventHooks.java` | Central event dispatcher. Static methods route events to global scripts, entity handlers, then `NpcAPI.EVENT_BUS`. 1400+ lines, 154+ hook types. |
| `ServerEventsHandler.java` | Forge `@SubscribeEvent` handler: entity interactions (wand/cloner), damage processing, quest updates, world saves. |
| `ServerTickHandler.java` | Server tick loop: player login/logout sync, periodic saves, controller updates. |

## Data* Classes Pattern
Each NPC has 6 data containers owned by `EntityNPCInterface`:

| Class | Configures |
|---|---|
| `DataAI` | Movement type, combat tactics (EnumNavType, EnumCombatPolicy), pathfinding, flying, sprint, door interaction |
| `DataStats` | HP, damage, armor, resistances, attack speed, projectile properties, respawn |
| `DataDisplay` | Skin, model type, name, overlays, tint, hitbox scale, visible body parts |
| `DataAbilities` | Ability slots, cooldowns, phase execution. Extends `AbstractDataAbilities` |
| `DataAdvanced` | Role/job instances, dialog lines, faction settings, sounds, transform |
| `DataInventory` | Equipment slots, drops, loot table. Implements `IInventory` |

**Pattern**: All use `readToNBT(NBTTagCompound)` / `writeToNBT(NBTTagCompound)` serialization.
Values loaded in `EntityNPCInterface.readEntityFromNBT`, flow to entity attributes via `SharedMonsterAttributes`.

## Subdirectories

### ai/ (29 files)
MC EntityAIBase tasks registered on NPC entity. Key groups:
- **Combat**: `EntityAIAttackTarget`, `CombatHandler`, `EntityAIRangedAttack`, `EntityAILeapAtTargetNpc`
- **Tactical**: `EntityAIAmbushTarget`, `EntityAIDodgeShoot`, `EntityAIOrbitTarget`, `EntityAIZigZagTarget`, `EntityAIStalkTarget`, `EntityAIPounceTarget`, `EntityAISprintToTarget`
- **Navigation**: `EntityAIMovingPath`, `EntityAIWander`, `EntityAIReturn`, `EntityAIWaterNav`, `EntityAIFollow`
- **Behavior**: `EntityAIPanic`, `EntityAIAvoidTarget`, `EntityAIFindShade`, `EntityAIMoveIndoors`, `EntityAIBustDoor`
- **Utility**: `EntityAILook`, `EntityAIWatchClosest`, `EntityAIAnimation`, `EntityAIJob`, `EntityAIRole`, `EntityAITransform`
- Also: `pathfinder/`, `selector/`, `target/` subdirs for pathfinding and target selection.

### entity/ (13 files + old/)
- `EntityNPCInterface` — abstract base, all NPCs extend this
- `EntityCustomNpc` — primary user-created NPC type
- Specialized: `EntityNPCFlying`, `EntityNPCGolem`, `EntityNpcDragon`, `EntityNpcSlime`, `EntityNpcCrystal`, `EntityNpcPony`
- Support: `EntityProjectile`, `EntityMagicProjectile`, `EntityChairMount`, `EntityDialogNpc`, `EntityFakeLiving`
- `old/` — 18 legacy NPC model classes (human, dwarf, elf, orc, etc.)

### roles/ (19 files)
Two interfaces: `RoleInterface` (interactive function) + `JobInterface` (background task).
- **Roles**: Trader, Bank, Follower, Companion, Transporter, Postman, Innkeeper, Auctioneer, Mount
- **Jobs**: Guard, Healer, Bard, ItemGiver, Spawner, Conversation, ChunkLoader, Follower

## Event Dispatch Flow
```
Forge/Game Event → ServerEventsHandler / Script*EventHandler
  → EventHooks.onXxx() (static)
    → ScriptController global scripts
    → Entity-specific IScriptHandler.callScript()
    → NpcAPI.EVENT_BUS.post() (for addons)
```

## Integration Points
- **Controllers**: `CustomNpcs.java` initializes all controllers in `setAboutToStart()`, saves in `stopped()`
- **Scripting**: `ScriptPlayerEventHandler`, `ScriptItemEventHandler`, `ScriptForgeEventHandler` bridge Forge events to scripts
- **Platform**: `PlatformServiceHolder.set(new MC1710PlatformService())` in pre-init
- **Networking**: `PacketHandler` for client-server sync, `SyncController` for data push
- **Config**: `LoadConfiguration.init()` loads modular configs, syncs to `CoreConfig`

## Forbidden
- **Do NOT migrate entity classes to core** — extend MC Entity directly, must stay platform-side
- **Do NOT migrate Data* classes yet** — depend on EntityNPCInterface, NBTTagCompound, SharedMonsterAttributes
- **Do NOT modify EventHooks dispatch order** — global → entity → bus ordering is contractual for addons
- **Do NOT mix NBTTags utility with platform-api INBTCompound** — NBTTags is MC-side only
