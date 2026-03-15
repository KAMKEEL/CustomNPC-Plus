# AGENTS.md — core/ (Platform-Abstracted Game Logic)

## What This Is
MC-free module containing game logic, enums, data classes, and controllers that compile without
Minecraft imports. Uses `platform-api/` interfaces for MC operations. 188 files migrated so far.
Dependency: `platform-api` ← `core` ← `mc1710 (root src)`.

## Split-Package Shadow Pattern
Core classes and mc1710 classes share the **same package**. At compile time, mc1710 versions shadow
core by adding MC-specific functionality:

```
core/  Faction.java         → Pure data + INbt serialization
src/   Faction.java         → adds `implements IFaction`, SyncController calls, EntityPlayer methods
```

**Rule**: Core class must compile independently. mc1710 version extends/overrides to add MC logic.

## Module Structure

### constants/ (42 files)
All game enums. No MC imports. Domains:
- **AI**: `AiMutex`, `EnumAnimation`, `EnumCombatPolicy`, `EnumMovingType`, `EnumNavType`, `EnumStandingType`
- **Quest**: `EnumQuestType`, `EnumQuestRepeat`, `EnumQuestCompletion`
- **Dialog**: `EnumAvailabilityDialog`, `EnumAvailabilityFaction`, `EnumAvailabilityFactionType`, `EnumAvailabilityQuest`, `EnumOptionType`
- **NPC**: `EnumJobType`, `EnumRoleType`, `EnumCompanionJobs`, `EnumCompanionStage`
- **Display**: `EnumTextureType`, `EnumBannerVariant`, `EnumDiagramLayout`, `EnumScrollData`
- **Economy**: `EnumAuctionPage`, `EnumAuctionSort`, `EnumStockReset`, `EnumTradeSlotType`
- **Party**: `EnumPartyExchange`, `EnumPartyObjectives`, `EnumPartyRequirements`
- **System**: `EnumGuiType`, `EnumPlayerData`, `EnumPlayerPacket`, `EnumScriptType`, `EnumProfileSync`, `EnumCategoryType`
- **Misc**: `EnumBardInstrument`, `EnumDayTime`, `EnumParticleType`, `MarkType`, `NBTTypes`, `EnumNpcToolMaterial`

### controllers/ (8 files)
Migrated controller singletons using `INbt` instead of `NBTTagCompound`:

| Controller | Status |
|---|---|
| `FactionController` | Fully migrated. Load/save via `PlatformServiceHolder.get()` |
| `MagicController` | Migrated with TODO stubs for ItemStack methods |
| `TransportController` | Fully migrated |
| `GlobalDataController` | Migrated. Async save via `CustomNPCsThreader` |
| `TagController` | Fully migrated |
| `ServerTagMapController` | Migrated (clone folder management) |
| `CategoryManager` | Utility — filesystem category operations |
| `APIRegistry` | Simple name→URL map, no MC deps |

### controllers/data/ (33 files)
Migrated data classes. Key groups:
- **Faction**: `Faction`, `Category` (base category class)
- **Magic**: `Magic`, `MagicCycle`, `MagicEntry`, `MagicAssociation`, `MagicData`
- **Transport**: `TransportCategory`, `TransportLocation`
- **Tags**: `Tag`, `TagMap`
- **Profile**: `ProfileOptions`, `ProfileInfoEntry`
- **Animation**: `Frame`, `FramePart`, `HitboxData`, `TintData`, `ItemDisplayData`
- **Dialog**: `DialogColorData`, `DialogImage`, `Line`, `Lines`
- **Player data fragments**: `PlayerDialogData`, `PlayerTransportData`, `PlayerEffectData`, `PlayerEffect`, `AbilityHotbarData`, `EffectKey`
- **Economy**: `AuctionFilter`, `TraderStock`, `SkinOverlay`, `InnDoorData`
- **Misc**: `CloneFolder`, `PartyOptions`

### util/ (6 files)
- `CustomNPCsThreader` — Thread pool for async controller saves
- `CacheHashMap` — TTL-based cache (used by PlayerDataController)
- `LRUHashMap` — Least-recently-used eviction map
- `ValueUtil` — Numeric clamping utilities
- `MillisTimer` — Simple timing
- `SizeOfObjectUtil` — Object size estimation

### kamkeel/ additions in core
- `controllers/data/ability/` — Ability enums (`AbilityPhase`, `HitType`, `TargetFilter`, `TargetingMode`, etc.) + data classes (`ProjectileData`, `EnergyBarrierData`, `EnergyHomingData`, etc.)
- `controllers/data/attribute/` — `AttributeDefinition`, `PlayerAttribute`, `PlayerAttributeMap`, `AttributeValueType`
- `controllers/data/profile/` — `Slot`, `ProfileOperation`, `EnumProfileOperation`, `ProfileInfoEntry`
- `controllers/data/telegraph/` — `Telegraph`, `TelegraphType`
- `network/enums/` — 9 network enum types (`EnumSyncType`, `EnumSyncAction`, `EnumDataPacket`, etc.)
- `util/` — `CNPCDebug`, `FileNameHelper`, `TextSplitter`
- `developer/` — `Developer` registry

## Platform Abstraction
Core uses `platform-api/` interfaces (the real ones from `noppes.npcs.api.*`):
- `INbt` / `INbtList` instead of `NBTTagCompound` / `NBTTagList`
- `NBT.compound()` / `NBT.list()` factory methods instead of `new NBTTagCompound()`
- `PlatformServiceHolder.get()` for file I/O, logging, world save directory access
- `IPlayer` for player references, `IItemStack` for items, `IWorld` for worlds

## Migration Status
188/~1810 files migrated. See `.AGENTS/SESSION_MEMORY.md` for the full migration roadmap.

## Forbidden
- **NEVER import net.minecraft.* in core/** — use platform-api interfaces only
- **NEVER import wrapper classes in core/** — only interfaces from platform-api/
- **NEVER break split-package shadow** — core class must compile alone, mc1710 adds MC logic
- **NEVER create duplicate interface names** — use the existing `IPlayer`, `IItemStack`, `IWorld`, `IEntity` etc. from platform-api
