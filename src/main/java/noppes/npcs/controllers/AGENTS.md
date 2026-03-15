# AGENTS.md — noppes.npcs.controllers + kamkeel.npcs.controllers

## What This Is
Singleton controller layer managing all persistent game data: dialogs, quests, factions, scripting,
economy, player data, abilities, profiles. 33 noppes controllers + 6 kamkeel controllers + 56 data classes.

## Singleton Pattern
All controllers use `static Instance` initialized in constructor: `DialogController.Instance = this;`.
Instantiated in `CustomNpcs.setAboutToStart()` at server start, data saved in `CustomNpcs.stopped()`.
No dependency injection — access via `ControllerName.Instance` globally.

## Controller Registry

### noppes.npcs.controllers (33 files)
| Controller | Purpose | Storage |
|---|---|---|
| `DialogController` | Dialog trees + categories | JSON dirs `dialogs/` |
| `QuestController` | Quest definitions + categories | JSON dirs `quests/` |
| `FactionController` | Faction definitions + standings | `factions.dat` (NBT) |
| `PlayerDataController` | Per-player data, `CacheHashMap` (1hr TTL) | `playerdata/` per-player files |
| `PlayerQuestController` | Quest progress tracking | Via PlayerData |
| `ScriptController` | Script engines (Nashorn/Janino), global handlers | `scripts/` dirs |
| `ScriptContainer` | Individual script execution context, caching | In-memory |
| `ScriptHookController` | Hook definitions (154+ types) | Static |
| `AnimationController` | Frame-based NPC animations | JSON `animations/` |
| `AuctionController` | Listings, bids, claims, tax. ConcurrentHashMap, async save every 30s | `auction.dat` |
| `BankController` | NPC bank slot configs | `bank.dat` |
| `MarketRegistry` | Currency + trader integration | Config-driven |
| `RecipeController` | Carpentry/anvil recipes | `recipes/` |
| `PartyController` | Player grouping, quest sharing | Via PlayerData (transient) |
| `TransportController` | Transport locations + categories | `transport.dat` |
| `MagicController` | Magic system data | `magic.dat` |
| `GlobalDataController` | Cross-world persistent data | `globaldata.dat` |
| `SpawnController` | Natural NPC spawn rules | `spawns.dat` |
| `TagController` | Tag management for filtering | `tags.dat` |
| `CategoryManager` | Filesystem category operations | Utility |
| `CustomEffectController` | Custom status effects | JSON `effects/` |
| `CustomGuiController` | Script-created GUI tracking | In-memory |
| `ChunkController` | Chunk loading callbacks | Via ForgeChunkManager |
| `LinkedNpcController` / `LinkedItemController` | Linked NPC/item templates | JSON dirs |
| `ServerCloneController` / `ServerTagMapController` | Clone tab management | `clones/` |
| `APIRegistry` | Addon API registration (name→URL) | In-memory |

### kamkeel.npcs.controllers (6 files)
| Controller | Purpose | Storage |
|---|---|---|
| `AbilityController` | Ability definitions, custom types, chained combos | JSON `abilities/` |
| `AttributeController` | Player attribute definitions + tracking | In-memory definitions |
| `EnergyController` | Energy entity spawning + damage routing | Transient |
| `ProfileController` | Multi-character profile slots | `profiles/` per-player DAT |
| `SyncController` | Client-server data sync, revision caching, chunked payloads | In-memory cache |
| `TelegraphController` | Visual telegraph presets for abilities | JSON `telegraphs/` |

## data/ Subdirectory (56 files)
Data model classes owned by controllers. Key groups:
- **Dialog**: `Dialog`, `DialogCategory`, `DialogOption` — branching conversation data
- **Quest**: `Quest`, `QuestCategory`, `QuestData` — quest definitions + progress
- **Faction**: `Faction`, `FactionOptions` — faction config (also in core/)
- **Player**: `PlayerData` (central), `PlayerQuestData`, `PlayerFactionData`, `PlayerBankData`, `PlayerMailData`, `PlayerAbilityData`, `PlayerAbilityHotbarData`, `PlayerTradeData`, `PlayerItemGiverData`, `PlayerEffect`, `PlayerEffectData`, `PlayerDialogData`
- **Economy**: `AuctionListing`, `AuctionClaim`, `AuctionBlacklist`, `Bank`, `BankData`
- **Script**: `ScriptHandler`, `SingleScriptHandler`, `MultiScriptHandler`, `DataScript`, `PlayerDataScript`, `ForgeDataScript`, `GlobalNPCDataScript`, `AbilityScript`, `EffectScript`, `LinkedItemScript`, `RecipeScript`, `ChainedAbilityScript` + interfaces `IScriptHandler`, `IScriptBlockHandler`, `IScriptUnit`
- **Animation**: `Animation`, `AnimationData`, `BuiltInAnimation`
- **Party**: `Party` — membership + shared quest state
- **Other**: `Availability`, `CustomEffect`, `LinkedItem`, `MarkData`, `SpawnData`, `DataTransform`, `RecipeCarpentry`, `RecipeAnvil`
- **action/**: Sub-package for quest/dialog action framework

## Data Persistence Flow
```
Controller.load()  → readNBT/JSON from world save dir
  → populate in-memory maps (HashMap/LinkedHashMap)
  → serve via Instance.getXxx()

Controller.save()  → writeNBT/JSON
  → CustomNPCsThreader.customNPCThread.execute() (async)
  → write to world save dir
```

## Cross-Controller Dependencies
- `SyncController` ← used by Dialog, Quest, Faction, Recipe, Effect, Ability, Magic controllers for client push
- `PlayerDataController` ← used by Party, Quest, Profile, Auction controllers for player state
- `AbilityController` → `EnergyController` (projectile spawning), `TelegraphController` (visuals)
- `ProfileController` → all player data controllers (slot switching reloads everything)
- `ScriptController` → provides execution context for all `*Script` data classes

## Forbidden
- **Do NOT break singleton contract** — controllers are accessed globally via `.Instance`
- **Do NOT save synchronously on main thread** — use `CustomNPCsThreader` for file I/O
- **Do NOT touch ScriptHandler/ScriptContainer for migration** — deeply coupled to Nashorn/Janino engines
- **Do NOT modify RecipeController for migration** — extends MC CraftingManager
