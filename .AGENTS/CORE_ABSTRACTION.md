# Core Abstraction Reference

Quick-reference for platform abstraction architecture and migration status.
For full details: `CORE_PLAN.md` (architecture), `CORE_MIGRATION_STATUS.md` (per-file blockers).

## Module Layout

```
platform-api/   → Interfaces only (43 files). No MC imports.
     ^
   core/         → Game logic (140 files). Imports platform-api only.
     ^
src/main/java/   → MC 1.7.10 implementations, wrappers, GUI, packets (~1,627 files)
```

## Migration Status: 183 / ~1,810 files abstracted

### Fully Abstracted (in core/)
- **Controllers**: GlobalData, ServerTagMap, Transport, Faction, Magic, Tag, CategoryManager, APIRegistry
- **Data clusters**: Transport, Faction, Magic/MagicCycle/MagicEntry, Tag/TagMap, Frame/FramePart
- **Player data fragments**: PlayerDialogData, PlayerTransportData, PlayerEffect, AbilityHotbarData
- **Display data**: DialogImage, DialogColorData, Lines/Line, SkinOverlay, HitboxData, TintData
- **Infrastructure**: NBTTags, NBT factory, CustomNPCsThreader, network enums, developer registry
- **Constants**: 40+ enums (EnumRoleType, EnumQuestType, EnumAnimation, etc.)
- **Utilities**: ValueUtil, LRUHashMap, CacheHashMap, TextSplitter, FileNameHelper

### Tier 1 — Blocked by IStack/Inventory Abstraction
Bank, BankController, AuctionClaim, AuctionListing, AuctionBlacklist, LinkedItem, RecipeAnvil, PlayerItemGiverData

### Tier 2 — Blocked by IUser (EntityPlayer) Abstraction
Availability, FactionOptions, Dialog/DialogOption/DialogCategory, Quest/QuestCategory/QuestData,
CustomEffect, Party, PlayerData, PlayerFactionData, all remaining Player*Data classes

### Tier 3 — Blocked by Entity/World Abstraction (IMob, IGameWorld)
SpawnData, SpawnController, Animation, AnimationData, DataTransform, MarkData, TelegraphInstance

### Tier 4 — Script System (DO NOT TOUCH)
All I*Script* interfaces, ScriptHandler, ScriptContainer, AbilityScript, EffectScript, Action framework

### Tier 5 — Heavy MC Coupling (Long-term)
RecipeCarpentry, RecipeController, Ability.java + all ability types, ChunkController, ProfileController, SyncController

## Key Rules

- **Interfaces**: Short `I` prefix (`IMob`, `IUser`, `IStack`). No `Platform` prefix. Access: `PlatformServiceHolder.get()`
- **Core imports**: Only `java.*`, platform-api interfaces, other core classes — never MC
- **NBT**: `NBT.compound()` / `NBT.list()`. File I/O via `PlatformServiceHolder`
- **Shadow pattern**: Core + mc1710 share packages. mc1710 adds `implements IFaction`, SyncController calls, MC-type methods

## Recommended Migration Order

1. mc1710 shadow files for already-migrated controllers
2. `IUser` implementation (unblocks Tier 2: ~15 files)
3. `IStack` / inventory abstraction (unblocks Tier 1: ~8 files)
4. Dialog cluster → Quest cluster → Player data classes
5. `NBTJsonUtil` to core (unblocks Dialog/Quest controllers)
