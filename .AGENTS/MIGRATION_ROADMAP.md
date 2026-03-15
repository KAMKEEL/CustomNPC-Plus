## Migration Roadmap (Added 2026-03-15 — Post-Critical-Review)

### Overview

This roadmap is organized into **4 phases** with **parallel tracks** where possible. Each phase has concrete milestones, estimated scope, dependencies, and a verification strategy.

**Key principle:** Maximize code sharing early (platform-api + core), then build version leaves on top. The more code that lives in platform-api/core before starting version leaves, the less per-version work there is.

```
Timeline (approximate):
Phase 0 ████░░░░░░░░░░░░░░░░  Foundation (API merge + build system)
Phase 1 ░░░░████████░░░░░░░░  Core expansion + 1.12.2 leaf
Phase 2 ░░░░░░░░░░░░████████  1.16.5 + 1.20.1 leaves
Phase 3 ░░░░░░░░░░░░░░░░████  Feature parity + polish
```

---

### Phase 0: Foundation (Prerequisites for everything else)

**Goal:** Get the build system working for multi-version and merge the scripting API into platform-api.

#### 0A. Composite Build Setup
- [x] Create `mc1710/` directory, move current `src/main/java/` and `src/main/resources/` into it
- [x] Create `mc1710/settings.gradle` and `mc1710/build.gradle` with RFG
- [x] Update root `settings.gradle` to use `includeBuild 'mc1710'` instead of direct source
- [x] Verify `mc1710/` builds and runs identically to current root build
- [x] Verify `platform-api` and `core` are consumed correctly via composite build dependency

**Scope:** ~5-10 files (build configs only). No Java changes.
**Risk:** Medium — build system restructure can surface classpath/dependency issues.
**Verification:** `./gradlew build` succeeds, `runClient` launches, existing tests pass.

#### 0B. Scripting API → platform-api Merge (Phase 1 of API merge: MC-free interfaces)
- [x] Identify the ~131 MC-free interfaces in `src/api/java/` (no `net.minecraft.*` or `cpw.mods.fml.*` imports)
- [x] Move them into `platform-api/` retaining exact package structure (`noppes.npcs.api.*`)
- [x] Reconcile the 30 existing split-package shadows (platform-api already has MC-free versions of these)
- [x] Strip generic type parameters from 5 entity interfaces (`IEntity`, `IEntityLivingBase`, `IEntityLiving`, `ICustomNpc`, `IPlayer`)
- [x] Update `core/` imports if any were pointing at the old location
- [x] Update `mc1710/` to shadow the moved interfaces where MC-specific extensions are needed

**Scope:** ~131 file moves + 30 reconciliations + 5 generics strips.
**Risk:** Low-Medium — package structure preserved, no behavioral changes.
**Verification:** `core/` compiles, `mc1710/` compiles, no new import errors.
**Dependency:** Phase 0A must be complete (mc1710 is a separate build).

#### 0C. Scripting API → platform-api Merge (Phase 2: MC-contaminated interfaces)
- [x] Identify 71 MC-contaminated interfaces in `src/api/java/`
- [x] For each, replace MC type references with platform-api equivalents or Object returns
- [x] Handle event interfaces: create platform `@Cancelable` annotation (or use the planned custom annotation parser)
- [x] Refactor `AbstractNpcAPI` — split into MC-free interface in platform-api + mc1710 implementation class
- [x] Move cleaned interfaces to `platform-api/`
- [x] Drop the 5 vendored MC class stubs (`net/minecraft/` in api/)
- [x] Update all `mc1710/` code importing these interfaces

**Scope:** 71 files, each needs individual review and MC-stripping.
**Risk:** Medium-High — method signature changes ripple through 276 files in mc1710 that import the API (743 import sites).
**Verification:** Full `mc1710/` build succeeds, scripting engine tests pass.
**Dependency:** Phase 0B complete.

#### 0D. Complete Existing Core TODOs (mc1710 shadow files)

The previous core migration left 22 TODOs across 8 files — all are incomplete mc1710 shadow implementations that need to be created or finished:

**Controllers needing mc1710 shadows (4 files, 11 TODOs):**
- [ ] `FactionController.java` — create mc1710 shadow that `implements IFactionHandler`, adds SyncController calls in save/delete
- [ ] `MagicController.java` — create mc1710 shadow that `implements IMagicHandler`, adds ItemStack resolution, SyncController calls in save/delete
- [ ] `TransportController.java` — create mc1710 shadow that `implements ITransportHandler`
- [ ] `TagController.java` — create mc1710 shadow that `implements ITagHandler`

**Data classes needing mc1710 shadows (2 files, 5 TODOs):**
- [ ] `Faction.java` — create mc1710 shadow that `implements IFaction`
- [ ] `Magic.java` — create mc1710 shadow that `implements IMagic`, adds ItemStack field and GameRegistry resolution in readNBT/writeNBT

**Utility needing mc1710 version (1 file, 4 TODOs):**
- [ ] `NBTTags.java` — mc1710 version needs: ItemStack list methods, nbtDoubleList (uses NBTTagDouble), Script methods (uses IScriptHandler/NBTTagCompound), getIntAt (uses NBTTagInt)

**Data needing completion (1 file, 2 TODOs):**
- [ ] `PlayerEffect.java` — needs CustomEffectController integration + IPlayer callback mechanism

**Scope:** 8 files with 22 TODOs. Creates ~6-8 new mc1710 shadow files + completes 2 existing stubs.
**Risk:** Low — these are well-understood patterns (split-package shadow). Each TODO explicitly describes what the mc1710 version must add.
**Verification:** Per file: mc1710 shadow compiles, controller loads/saves correctly, no regressions in NPC behavior.
**Dependency:** Can run in parallel with 0B/0C, or immediately.

---

### Phase 1: Core Expansion + 1.12.2 Leaf (Parallel tracks)

**Goal:** Migrate the bulk of game logic to core/ while simultaneously standing up the 1.12.2 version leaf.

#### Track 1A: Core Controller Migration

Now that subsystem restrictions are lifted, the migration order is by ROI:

| Priority | Subsystem | Files | Blockers | Unlocks |
|---|---|---|---|---|
| 1 | Remaining Controllers/Data | ~35 unmigrated | Need `IPlayer`, `IItemStack` coverage expanded in platform-api | Quest, Dialog, PlayerData sharing |
| 2 | Roles/Jobs | 19 | Entity param abstraction via existing interfaces | Role/Job logic shared across versions |
| 3 | Items (logic only) | ~25 | `IItemStack` abstraction | Item behavior shared (registration stays per-version) |
| 4 | Quest system | ~10 | Dialog + PlayerData in core | Quest logic shared |
| 5 | Dialog system | ~8 | Controller/Data in core | Dialog logic shared |
| 6 | Script system (containers/hooks) | ~30 | ScriptContainer needs platform abstraction | Scripting behavior shared |
| 7 | Profile system | ~5 | `IPlayer` + PlayerData in core | Profile logic shared |
| 8 | Packet DTOs | ~50 | INetworkService in platform-api | Packet data shared (transport per-version) |
| 9 | Command abstraction | ~22 | ICommandService in platform-api | Command logic shared (dispatch per-version) |

**New platform-api interfaces needed:**
- `INetworkService` — packet registration, sending, channel management
- `ICommandService` — command registration and dispatch
- `IRegistryService` — block/item/entity registration abstraction
- `IEventBus` — event subscription and dispatch abstraction
- Expand `IPlayer` and `IItemStack` coverage as needed by core migration (use the EXISTING interfaces — do NOT create duplicates)

**Scope:** ~200 files migrated to core over this phase.
**Risk:** High — each controller migration can surface hidden MC dependencies.
**Verification:** Per-controller: core compiles, mc1710 shadow compiles, controller tests pass.

#### Track 1B: 1.12.2 Version Leaf (parallel with 1A)

- [ ] Create `mc1122/` directory structure with RFG build config
- [ ] Copy mc1710 source → mc1122 as starting point
- [ ] **Mechanical changes (can be scripted/automated):**
    - Mass-rename `cpw.mods.fml` → `net.minecraftforge.fml` (524 files)
    - Replace `getUnlocalizedName` → `getTranslationKey` (59+ files)
- [ ] **Breaking changes (require manual work):**
    - ItemStack null→isEmpty(): audit and fix 37+ null-check sites, 390+ files that handle ItemStack
    - GameRegistry → RegistryEvent.Register pattern (18 files, 62 calls)
    - Entity registration signature update (1 file)
    - World.getBlock → getBlockState adaptation (59+ files)
    - SimpleNetworkWrapper → SimpleChannel packet rewrite (100+ files)
    - IInventory → IItemHandler for containers (59+ files, can be incremental — IInventory still works in 1.12)
- [ ] Set up 1.12.2 Forge MDK mappings in `mc1122/gradle.properties`
- [ ] Build, fix compilation errors, iterate
- [ ] Test core NPC lifecycle: spawn, persist, despawn

**Scope:** Full mc1710 codebase copy (~1,722 files) + modifications.
**Risk:** Medium — most changes are well-understood patterns, but the volume is large.
**Verification:** `mc1122/` builds, basic NPC spawns in 1.12.2 client, save/load works.
**Key insight:** IInventory still works in 1.12.2 (just deprecated), so container migration can be deferred. Focus on the 4 truly breaking items first: ItemStack null→EMPTY, GameRegistry events, packet system, entity registration.

---

### Phase 2: Modern Version Leaves (1.16.5 + 1.20.1)

**Goal:** Stand up 1.16.5 and 1.20.1 version leaves. These benefit from all the core/ work done in Phase 1.

**Dependency:** Phase 1A should be at least 50% complete (controllers, data, roles/jobs in core).

#### Track 2A: 1.16.5 Version Leaf

- [ ] Create `mc1165/` with ForgeGradle 6 build config
- [ ] Implement `PlatformService` for 1.16.5 (MC type wrappers, NBT, file I/O)
- [ ] Core controllers work automatically — they depend only on platform-api
- [ ] Port networking (entirely new packet system in 1.16)
- [ ] Port commands (Brigadier adapter — command tree maps conceptually)
- [ ] Port blocks (BlockState — the Flattening. Metadata → block properties. DeferredRegister for registration)
- [ ] Port items (DeferredRegister, no metadata)
- [ ] Port AI (EntityAI → Goal system)
- [ ] Port essential admin GUIs: NPC editor, Quest editor, Dialog editor (rewrite, not port — Screen/Button/MatrixStack API)
- [ ] Port entity registration (DeferredRegister + EntityType.Builder)

**Scope:** ~800-1000 files (version-specific implementations of what core doesn't cover).
**Risk:** High — this is the biggest version gap (1.7.10 → post-Flattening). Every block, item, entity, and GUI changes.
**Verification:** NPC spawns, basic dialog works, quests work, save/load works.

#### Track 2B: 1.20.1 Version Leaf (start when 2A is ~50% done)

- [ ] Create `mc1201/` based on `mc1165/` (1.16.5 → 1.20.1 is much smaller than 1.7.10 → 1.16.5)
- [ ] Implement `PlatformService` for 1.20.1
- [ ] Adapt for NeoForge API differences (1.20.1 is the Forge→NeoForge transition)
- [ ] Update rendering (GuiGraphics wrapper, component-based text)
- [ ] Update registration patterns (NeoForge DeferredRegister differences)

**Scope:** ~200-400 files of delta from 1.16.5.
**Risk:** Medium — incremental from 1.16.5, mostly API surface changes.
**Verification:** Same as 1.16.5 milestones.

---

### Phase 3: Feature Parity + Polish

**Goal:** Bring all version leaves to MVP feature parity, then expand.

#### Per-version MVP checklist:
- [ ] NPC lifecycle (spawn, persist, despawn, AI, combat)
- [ ] Dialog system (create, edit, trigger, branching)
- [ ] Quest system (all quest types, tracking, rewards)
- [ ] Faction system (points, NPC reactions, conditions)
- [ ] Networking (NPC sync, player data, editor packets)
- [ ] Commands (`/kam` hierarchy, all subcommands)
- [ ] Essential GUIs (NPC editor, Quest editor, Dialog editor, Cloner)
- [ ] Config system
- [ ] Basic scripting (JS engine, script hooks for NPC/Player)

#### Post-MVP (per version, prioritized):
1. Full scripting engine + script editor GUI
2. Ability system (entity types, energy system, telegraph)
3. Animation system
4. Auction/Economy system
5. Profile system
6. Full GUI suite (all editor screens, HUD, overlays)
7. Custom models / addon compatibility (Gecko, AW, DBC)

---

### Risk Register

| Risk | Likelihood | Impact | Mitigation |
|---|---|---|---|
| API merge breaks addon mods | High | High | Coordinate with addon authors. Ship platform-api as Maven artifact. Provide migration guide. |
| ItemStack null→EMPTY changes cause subtle runtime bugs | High | Medium | Automated grep + manual audit. Write a test suite for ItemStack handling. |
| Build system complexity (4 Gradle plugins, composite builds) | Medium | High | Get Phase 0A working and stable BEFORE any Java changes. Document build setup. |
| Core migration surfaces hidden MC dependencies in "LIGHT-MC" code | Medium | Medium | Migrate one controller at a time. Verify compilation after each. |
| 1.16.5 port takes much longer than estimated (Flattening impact) | High | High | Start with server-only (commands + data), defer GUI. Track 2B (1.20.1) doesn't start until 2A proves the pattern. |
| Preprocessor+RFG integration doesn't work | Medium | Low | Preprocessor is optional. The platform-api/core/leaf architecture works without it. |
| Script API behavior differences across MC versions | Medium | Medium | Define a compatibility spec: which script APIs are guaranteed cross-version vs. version-specific. |

---

### Milestone Summary

| Milestone | Phase | Deliverable | Success Criteria |
|---|---|---|---|
| **M0: Multi-build compiles** | 0A | mc1710/ as composite build | `./gradlew build` works, runClient launches |
| **M0.5: Core TODOs complete** | 0D | All 22 TODOs in core/ resolved, mc1710 shadows created | All 8 files have no remaining TODOs, mc1710 compiles, controllers save/load correctly |
| **M1: API in platform-api** | 0B+0C | All 202 API interfaces in platform-api | core/ and mc1710/ compile, no MC imports in platform-api |
| **M2: Core 50%** | 1A | Controllers, data, roles, items in core | ~200 files in core, mc1710 shadows compile |
| **M3: 1.12.2 compiles** | 1B | mc1122/ builds successfully | Gradle build succeeds |
| **M4: 1.12.2 NPC spawns** | 1B | Basic NPC lifecycle on 1.12.2 | NPC spawns, persists, loads, has AI |
| **M5: 1.16.5 compiles** | 2A | mc1165/ builds successfully | Gradle build succeeds |
| **M6: 1.16.5 NPC spawns** | 2A | Basic NPC lifecycle on 1.16.5 | NPC spawns, persists, loads, has AI |
| **M7: 1.20.1 compiles** | 2B | mc1201/ builds successfully | Gradle build succeeds |
| **M8: All versions MVP** | 3 | NPC + Dialog + Quest + Faction on all versions | Full MVP checklist passes on each |

---

### What This Roadmap Does NOT Cover (Future Planning Needed)

1. **Addon porting strategy** — DBC, Gecko, AW addons each need version-specific builds. Requires separate planning.
2. **Data migration** — Can NPC save files from 1.7.10 load on 1.20.1? Needs a data format compatibility analysis.
3. **CI/CD** — Automated builds for all 4 versions, artifact publishing, test matrix.
4. **Scripting compatibility spec** — Which script APIs are guaranteed cross-version.
5. **Client-side scripting** — The client script system has version-specific rendering hooks. Needs per-version analysis.
6. **Performance testing** — Core abstraction layer adds indirection. Measure overhead on large NPC populations.

**USER REVIEW FOR WHATS UNCOVERED**
1) Addon porting shouldn't be handled on CNPC+ side, but on addon side. Skip
2) Data migration is ESSENTIAL. Must be fleshed out ASAP.
3) Multi-version builds are also ESSENTIAL. One of the first things that must be tackled (paralelly while working on the refactor)
4) Handle this after migration
5) There is no client script system, but if you see one just migrate it as is. This will be handled after the migration
6) This too is after migration
---

## Critical Review Agent Summary (Session 2 — 2026-03-15, 4 agents deployed)

| Agent | Type | Key Finding |
|---|---|---|
| unspecified-high #1 | Verify abstraction state | 188 files abstracted (claim ~correct). PlatformService adoption near-zero in mc1710 (1 file). ~50 files are trivial enums. NBT abstraction pattern works well. |
| unspecified-high #2 | Assess API merge feasibility | 202 API files total. 71 MC-contaminated. 30 naming conflicts (split-package shadow). 5 generics to strip. 743 import sites in mc1710. Move is feasible in phases. |
| librarian #1 | Validate build system claims | RFG 1.12.2 VERIFIED. Composite builds VERIFIED. ModStitch UNVERIFIED (PR not merged). Essential single-wrapper VERIFIED. Java toolchains VERIFIED. |
| explore #1 | Verify 1.12.2 mechanical port claim | "95% mechanical" is WRONG. Found 5 breaking changes plan missed: ItemStack null→EMPTY (37+ files), IInventory (59+), getBlockState (59+), getTranslationKey (59+), GameRegistry events (18). Corrected to ~60-70% mechanical. |
