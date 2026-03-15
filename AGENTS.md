# AGENTS.md — CustomNPC-Plus

## ⚠️ AGENT CONTEXT — Read First
**All documents below this line are created BY and FOR agents operating on this codebase.**
This is the master architecture reference. Before any work:
1. Read `.AGENTS/SESSION_MEMORY.md` — strategic vision, roadmap, critical decisions
2. Read `.AGENTS/CORE_PLAN.md` — platform abstraction design and naming conventions
3. Skim `.AGENTS/FORBIDDEN_PRACTICES.md` — architectural guardrails (NEVER violate these)
4. Reference `.AGENTS/MC_COUPLING_ANALYSIS.md` — what's MC-dependent, migration priority
   **If working on multi-version porting**, also read:
- `.AGENTS/MULTI_VERSION_RESEARCH.md` — real-world strategies, tools, pitfalls
- `.AGENTS/BUILD_SYSTEM.md` — Gradle architecture, version-specific build requirements

-**IMPORTANT** ALWAYS CHECK OUT `SESSION_MEMORY.md` WHEN WORKING ON MIGRATING/UPPORTING THIS PROJECT TO LATER MINECRAFT VERSIONS.
---


## What This Is
Minecraft 1.7.10 Forge mod. NPC customization, AI, quests, abilities, scripting (JS/Java), economy.
~2,170 source files. Entry: `CustomNpcs.java` (`@Mod`). Packages: `noppes.npcs.*`, `kamkeel.npcs.*`.

## Module Structure

| Module | Path | Purpose | MC Dependencies |
|---|---|---|---|
| **platform-api** | `platform-api/` | Interfaces only (`PlatformService`, `NBTFactory`, `INBTCompound`) | None |
| **core** | `core/` | MC-free game logic: controllers, data classes, enums, utils | None (depends on platform-api) |
| **mc1710** (root) | `src/main/java/` | MC 1.7.10 implementations: entities, GUI, packets, wrappers | Full Forge/MC |
| **gradle-plugins** | `gradle-plugins/` | TypeScript `.d.ts` generator from Java API sources | Gradle only |
| **API** (submodule) | `src/api/java/` | Scripting API interfaces (`IPlayer`, `ICustomNpc`, `IWorld`, etc.) | Git submodule — run `./gradlew updateAPI` |

**Dependency flow:** `platform-api` ← `core` ← `mc1710 (root src)`

## Architecture Pattern
- **Split-package shadow**: Core class in `core/` + mc1710 version in `src/main/java/` same package. mc1710 shadows core at compile time (adds `implements IFaction`, SyncController calls, MC-specific methods).
- **Platform abstraction**: `PlatformServiceHolder.get()` → `PlatformService` impl registered at mod init.
- **NBT wrapping**: `NBTTagCompound` → `INBTCompound`, `new NBTTagCompound()` → `NBT.compound()`.
- **Entity wrapping**: `EntityPlayer` → `IUser` (platform-api), mc1710 provides `PlayerWrapper`.

## Subsystem Location Map

| Subsystem | Primary Path(s) | Key Files |
|---|---|---|
| **NPC Entities** | `src/.../noppes/npcs/entity/` | `EntityNPCInterface.java`, `EntityCustomNpc.java` |
| **NPC Data** | `src/.../noppes/npcs/Data*.java` | `DataAI`, `DataDisplay`, `DataStats`, `DataAbilities`, `DataAdvanced` |
| **AI System** | `src/.../noppes/npcs/ai/` | 28 AI tasks (`EntityAI*.java`), `CombatHandler`, `pathfinder/`, `selector/`, `target/` |
| **Quest System** | `src/.../noppes/npcs/quests/` | `QuestItem`, `QuestKill`, `QuestLocation`, `QuestDialog`, `QuestManual` |
| **Quest Controller** | `src/.../noppes/npcs/controllers/` | `QuestController.java`, `PlayerQuestController.java` |
| **Dialog System** | `src/.../noppes/npcs/controllers/` | `DialogController.java` + data in `controllers/data/` |
| **Faction System** | `core/.../noppes/npcs/controllers/` | `FactionController.java`, `Faction.java` (migrated to core) |
| **Ability System** | `src/.../kamkeel/npcs/controllers/` | `AbilityController.java`, `EnergyController.java`, `TelegraphController.java` |
| **Ability Entities** | `src/.../kamkeel/npcs/entity/` | `EntityAbility*.java`, `EntityEnergy*.java` (13 entity types) |
| **Scripting Engine** | `src/.../noppes/npcs/controllers/` | `ScriptController.java`, `ScriptContainer.java`, `ScriptHookController.java` |
| **Scripting Wrappers** | `src/.../noppes/npcs/scripted/` | `ScriptWorld`, `ScriptBlock`, `NpcAPI`, `event/`, `entity/`, `item/`, `gui/` |
| **Economy/Auction** | `src/.../noppes/npcs/controllers/` | `AuctionController.java`, `MarketRegistry.java` |
| **Roles & Jobs** | `src/.../noppes/npcs/roles/` | `RoleTrader`, `RoleBank`, `RoleFollower`, `JobGuard`, `JobBard`, etc. |
| **Profile System** | `src/.../kamkeel/npcs/controllers/` | `ProfileController.java` |
| **Animation** | `src/.../noppes/npcs/controllers/` | `AnimationController.java` |
| **Commands** | `src/.../kamkeel/npcs/command/` | Hierarchical `/kam` system. `CommandKamkeel.java` is root. |
| **Config** | `src/.../noppes/npcs/config/` | `ConfigMain`, `ConfigClient`, `ConfigScript`, `ConfigMarket`, `ConfigEnergy`, etc. |
| **Blocks** | `src/.../noppes/npcs/blocks/` | 34 block types + `tiles/` |
| **Items** | `src/.../noppes/npcs/items/` | Weapons, linked items, scripted items |
| **Networking** | `src/.../kamkeel/npcs/network/` | `PacketHandler.java` + `SyncController.java` |
| **Mixins** | `src/.../noppes/npcs/mixin/` | Forge/MC mixins |
| **Client** | `src/.../noppes/npcs/client/` + `src/.../kamkeel/npcs/client/` | GUI, rendering, overlays |
| **Compat** | `src/.../noppes/npcs/compat/` | Cross-mod compatibility |
| **TypeScript Gen** | `gradle-plugins/src/` | Gradle plugin: Java API → `.d.ts` definitions |

## Core Migration Status
- **183 files** abstracted to `core/` + `platform-api/` out of ~1,810 total
- Migrated: Faction, Magic, Transport, Tag, GlobalData controllers + 60+ data classes
- See `CORE_MIGRATION_STATUS.md` for per-file blocker analysis
- See `CORE_PLAN.md` for migration roadmap and naming conventions

## NEVER / DO NOT Rules

1. **NEVER touch script system for migration** — `ScriptHandler`, `ScriptContainer`, all `I*Script*` interfaces, Action framework. Deeply coupled, not yet planned.
2. **NEVER touch Ability.java or ability type classes for migration** — 30+ MC imports, deeply coupled to Entity/World/DamageSource.
3. **NEVER touch entity classes for migration** — extend MC Entity directly, must stay platform-side.
4. **NEVER touch recipe system for migration** — `RecipeCarpentry` extends `ShapedRecipes`, `RecipeController` uses `CraftingManager`.
5. **NEVER import MC classes in `core/` or `platform-api/`** — use platform interfaces only.
6. **NEVER import wrapper classes in `core/`** — only interfaces from `platform-api/`.
7. **Platform interface naming**: Short `I` prefix, no `Platform` in name (`IUser` not `IPlatformUser`).
8. **MC1710 wrapper naming**: `[Thing]Wrapper` — no `MC1710` prefix.
9. **Scripting API** (`IPlayer`, `IEntity`, `ICustomNpc`) is a **separate concern** from platform interfaces — different package, different purpose. Do not conflate.
10. **Split-package rule**: When a class exists in both `core/` and `src/main/java/` with same package, mc1710 version shadows core. Never break this contract.

## Build & Run

```bash
# First-time setup (pulls API submodule)
./gradlew updateAPI

# Build (includes TypeScript .d.ts generation)
./gradlew build

# Build without mixin embedding
./gradlew buildNoMixin

# Run game
./gradlew runClient
./gradlew runServer

# Run without Nashorn scripting engine
./gradlew runClientNoNashorn
./gradlew runServerNoNashorn

# Generate TypeScript definitions only
./gradlew generateTypeScriptDefinitions

# IDE setup (IntelliJ)
./gradlew genIntellijRuns
# Then add args: --tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin customnpcs.mixins.json
```

**Build system**: GTNH Convention plugin + RetroFuturaGradle. Java 8 target (core/platform-api), Java 8 compile with JDK 8 (mc1710).

## Convention Summary
- Two root packages: `noppes.npcs.*` (original), `kamkeel.npcs.*` (additions)
- Controllers are singletons in `controllers/` packages — instantiated at server start
- Data classes use NBT serialization (`readNBT`/`writeNBT` pattern with `INBTCompound`)
- Commands: hierarchical under `/kam` — each subcommand is a `CommandKamkeelBase` subclass
- Script hooks: 154+ events across NPC, Player, Block, Item, Quest, Dialog, Ability, Auction, Animation
- Config: separate config classes per domain (`ConfigMain`, `ConfigScript`, `ConfigMarket`, etc.)
- Async saves: `CustomNPCsThreader` thread pool for controller persistence
- API submodule: `src/api/java/` — must `git submodule update --init --recursive` before build

## Key Reference Files
- `SESSION_MEMORY.md` — **Multi-version porting strategy**, owner's vision, corrected architecture, research synthesis
- `MULTI_VERSION_RESEARCH.md` — External research: real multi-version mod repos, build tools, preprocessor details
- `MC_COUPLING_ANALYSIS.md` — Quantified MC-dependency breakdown by subsystem (1,424 files analyzed)
- `FORBIDDEN_PRACTICES.md` — Anti-patterns, deprecated components, architectural guardrails
- `CORE_MIGRATION_STATUS.md` — Detailed per-file blocker analysis for core migration
- `CORE_PLAN.md` — Migration roadmap, naming conventions, architecture decisions
- `CHANGELOG.md` — Version history
- `todo.txt` — Pending tasks
- `dts-patches/` — Manual patches applied to generated TypeScript definitions
