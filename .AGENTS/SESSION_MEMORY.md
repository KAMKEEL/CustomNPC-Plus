________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
## ⚠️ MIGRATION ORCHESTRATION RULES (APPLY TO ALL MIGRATION WORK)

These rules govern how ALL agents operate during the multi-version migration. Every delegated task MUST follow them.

### 1. Self-Review Protocol
Every agent completing migration work MUST self-review before reporting completion:
- **Compile check**: Does `./gradlew build` still succeed? (or at minimum, do the affected modules compile?)
- **Pattern check**: Does the change follow existing codebase conventions? (split-package shadow, naming, etc.)
- **Regression check**: Did the change break any existing behavior or imports?
- **Completeness check**: Are there any loose ends, missed files, or incomplete stubs?

### 2. Orchestrator Verification
The orchestrating agent (Sisyphus) will manually verify ALL delegated work:
- Read key changed files to confirm correctness
- Run `./gradlew build` after each phase
- Check that no forbidden practices were introduced (see FORBIDDEN_PRACTICES.md)
- Verify the deliverables match the expected outcome exactly

### 3. Incremental Commits
- Each sub-phase (0A, 0B, 0C, 0D) gets its own commit upon successful verification
- Never combine multiple phases into one commit
- Commit messages follow: `migration(phase-X): <concise description>`

### 4. Fail-Safe Rules
- If a build breaks during any phase, STOP and fix before proceeding
- If a change would affect >100 files, get orchestrator approval first
- If uncertain about a pattern, check 2-3 existing examples before proceeding
- NEVER delete or modify files outside the stated scope without explicit approval

### 5. Context Passing
- Every delegated agent MUST receive the full content of SESSION_MEMORY.md in their prompt
- This ensures every agent understands the vision, architecture, constraints, and forbidden practices

### 6. Phase Reports
- Upon completing a phase, the agent MUST create `.AGENTS/PHASE_0X_REPORT.md` (e.g., `PHASE_0A_REPORT.md`)
- Report format:
  ```
  # Phase 0X Report — [Title]
  **Date:** YYYY-MM-DD
  **Agent:** [agent type]
  
  ## What Was Done
  - [list of concrete changes]
  
  ## Files Changed
  - [file paths with brief description of change]
  
  ## Files Created
  - [new file paths]
  
  ## Verification
  - Build status: [PASS/FAIL]
  - Tests: [PASS/FAIL/N/A]
  - Self-review findings: [any issues found and fixed]
  
  ## Discoveries / Notes
  - [anything unexpected, deviations from plan, or future considerations]
  ```

### 7. Documentation Updates
- Update AGENTS.md, SESSION_MEMORY.md, and MIGRATION_ROADMAP.md checkboxes as phases complete
- Record any discoveries, surprises, or deviations in SESSION_MEMORY.md

________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
CURRENT USER PROMPT:

## 1) MINECRAFT/FORGE PLATFORM ABSTRACTION:

### package: platform-api/
- Context: Before the migration plan, this was previously (and currently, we only just started migrating) just used for the Scripting API, but as it has much more use cases, and it's already solid general abstraction of Minecraft and CNPC+ classes.

1) This should host the Minecraft abstraction like @ClientOnly, @ServerOnly, and ALL other mc classes  so that other versions like 1.7.10, 1.12.2, 1.16.5 and 1.20.1 can replace them completely with their own versions.
For annotations, we will write a custom annotation parser later to replace those annotations with their platform-correct implementations

2) We already have systems in place, like IPlayer to represent a player, IItemStack, INbt, ICustomNpc, etc. (check src/api/java/noppes/npcs/api/) that will all be used and recycled into this. Move these kinds of classes into the platform-api.
- Don't worry about the parameterized interfaces, strip them of parameterization entirely and make them return Object in their parameterized methods. I have personally made sure that most parameters are unused (99.9% functions use raw interfaces) and we have confirmed that
  it would be incredibly easy to convert the returned Object to its platform specific type with simple casting when needed.

> **AGENT REVIEW (2026-03-15):**
> The scripting API (src/api/java/) has 202 files total. Key data for planning the move:
>
> | Category | Count | Action Needed |
> |---|---|---|
> | Already in platform-api (split-package shadow) | 30 | Already done — merge/reconcile |
> | MC-free interfaces (no MC imports) | ~131 | Move directly, strip generics from 5 entity interfaces |
> | MC-contaminated (import net.minecraft/cpw.mods.fml) | 71 | Strip MC imports, replace with platform-api equivalents or Object returns |
> | MC class stubs (net/minecraft/ vendored) | 5 | Drop — platform-api shouldn't need these |
>
> **Technical considerations for the move:**
> 1. **Generics stripping** — Only 5 interfaces use generics (entity hierarchy: IEntity, IEntityLivingBase, IEntityLiving, ICustomNpc, IPlayer). All bound to MC Entity classes. Already stripped in platform-api copies. Straightforward.
> 2. **71 MC-contaminated files** — These need method signatures rewritten to use platform-api types (Object returns where MC types were, or existing platform interfaces like `IPlayer`, `IItemStack`, `IWorld`). Biggest offenders: AbstractNpcAPI (10 MC imports), event interfaces (17 use @Cancelable from FML), entity/handler interfaces referencing MC types.
> 3. **Event system** — 20 event interfaces all use `@Cancelable` from `cpw.mods.fml`. Options: (a) create a platform `@Cancelable` annotation, (b) use the custom annotation parser mentioned in the plan, or (c) move events to a separate platform-events module.
> 4. **AbstractNpcAPI** — Abstract class (not interface) with 10 MC imports and static singleton. Needs to become an interface or be split into MC-free interface + mc1710 implementation.
> 5. **Addon impact** — The API is a git submodule used by DBC/Gecko/AW addons. Moving it means those addons need to depend on platform-api instead. Plan the transition: ship platform-api as a published artifact, update addon build files.
> 6. **30 existing split-package shadows** — These need reconciliation. The platform-api version is the MC-free base; the API version adds MC-specific methods. After the move, there should be ONE authoritative version per interface in platform-api.
>
> **Recommended execution order:** Move MC-free interfaces first (~131 files, low risk) → then tackle the 71 MC-contaminated files in batches (entity hierarchy → handlers → events → AbstractNpcAPI).


### package: src/
- The main project source. Currently, everything in the source is jumbled up in here. This will be completely refactored and the appropriately diagnosed systems will be moved to either platform-api/ or core/ or remains in the new src/
- This contains the implementation of the platform-api/ abstractions for each version of the game
- The GOAL is to HOST the LEAST amount of POSSIBLE files necessary, typically rendering, entity construction, events, etc.
- In each version, let's take 1.7.10 for example: the API abstractions in the platform-api/ noppes.npcs.api packages are represented by WRAPPERS like, ScriptNpc, ScriptItemStack, ScriptPlayer, etc. (check src/main/java/noppes/npcs/scripted/wrapper/) 
that are registered to the PlatformService in Platform-API. These are the PER-VERSION implementations of our wrappers. So when in Core, when we move all logic, controllers, etc. We can simply use PlatformAPI 
to supply us with an ICustomNpc or an IPlayer, etc. which represent the system.
- We should also consider abstracting a NetworkService into platform-api/ and Core system thus Packet Handling and Sync Controllers can also be fully adapted.



## 2) CORE CNPC+ THAT IS ISOLATED FROM PLATFORM IMPLEMENTATION IN src/.
- package: core/ 
- This is where all the CORE CNPC+ classes and logic will be implemented BASED ON the abstraction layer in platform-api
1) MUST ONLY USE platform-api/ abstractions AND NOT any of the platform implementations in src/. NO MINECRAFT WHATSOEVER.
2) All Data, Controller and Registry systems and other isolated CNPC+ system are completely ported to Core.



## 3) What's currently required + ROADMAP:
1) We want the current src/api package to be moved to platform-api/. You must retain the same package structure of noppes.npcs.api and everything it encapsulates.
DO NOT CHANGE THEIR NAMES AND DO NOT CREATE SEMANTICALLY DUPLICATE INTERFACES/CLASSES .
2) TO BE DECIDED BY YOU.

> **AGENT RECOMMENDATION FOR POINT 1:**
> Feasible. Move the API into platform-api in phases:
> - **Phase 1:** Move ~131 MC-free interfaces directly (retain package structure, strip 5 generic type params)
> - **Phase 2:** Migrate 71 MC-contaminated files by replacing MC type references with platform-api equivalents or Object returns
> - **Phase 3:** Reconcile the 30 existing split-package shadow conflicts (keep one authoritative version)
> - **Phase 4:** Update addon mods (DBC, Gecko, AW) to depend on platform-api artifact instead of git submodule
>
> **AGENT RECOMMENDATION FOR POINT 2 — SEE MIGRATION ROADMAP BELOW**

________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________




________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________
VERY FIRST USER PROMPT:

I want you to create a permanent memory session .md for this session and hook it up to the root project AGENTS.md so you always refer to it. In that memory session I want all my critical requests, 
ideas and visions for whats next noted, as well as your own essential responses, as well as any important research data found by you or any subagent spawned.

Ok so this mod is currently for minecraft forge 1.7.10. As you noticed, I am trying to upport this  mod to modern minecraft versions like 1.12.2, 1.16.5 and 1.20.1 SIMULTANEOUSLY. I dont just want to work on the upport 
for one version at a time for like 2-3 months, then after im done start working on the next, that's too expensive and I don't have time. I want to tackle all of them at once, to the highest possible degree. 
For that purpose, I want you to give me your best suggestions for how to create a roadmap on tackling this
________________________________________________________________________________________________________________________________________________________________________________________________________________________________________________



# SESSION_MEMORY.md — Multi-Version Porting Strategy

**Created:** 2026-03-15
**Last updated:** 2026-03-15
**Purpose:** Persistent memory of critical decisions, research, and roadmap for porting CustomNPC-Plus to multiple Minecraft versions simultaneously.


**Related documents:**
- `MIGRATION_ROADMAP.md` — Phased migration plan with milestones
- `MULTI_VERSION_RESEARCH.md` — Detailed external research (tools, repos, real implementations)
- `MC_COUPLING_ANALYSIS.md` — Quantified MC-dependency breakdown by subsystem
- `BUILD_SYSTEM.md` — Gradle architecture, version-specific build requirements
- `SCRIPTING_TOOLCHAIN.md` — Java → TypeScript → Editor pipeline

---


## Owner's Vision

**Goal:** Port CustomNPC-Plus from 1.7.10 to **1.12.2, 1.16.5, and 1.20.1 simultaneously**.

**Key Constraints (from owner):**
- **NOT sequential** — refuses to spend 2-3 months per version one at a time
- Must tackle all versions at once, to the highest possible degree
- 1.7.10 remains the primary/active build during porting
- Time and budget are limited — efficiency is paramount

---

## Current State Assessment

### Abstraction Progress
- **183 / ~1,810 files abstracted** (~10%) into `core/` + `platform-api/`
- `platform-api/`: 48 interface files (NBT, entity wrapping, file I/O, logging, scheduling)
- `core/`: 140 files (8 controllers, 60+ data classes, enums, utils)
- `PlatformService` exists but is used in <0.2% of MC-dependent code

### MC-Dependent Code Distribution (~1,424 files import net.minecraft.*)

| Subsystem | File Count | Coupling Level | Porting Difficulty |
|---|---|---|---|
| **GUI/Client/Rendering** | 212 | VERSION-LOCKED | Extreme — rewrite per version |
| **Networking/Packets** | 100+ | HEAVY-MC | High — framework changes every version |
| **Blocks + Tiles** | 97 | VERSION-LOCKED | High — Flattening breaks everything |
| **Controllers/Data** | 81 | LIGHT-MC | Medium — mostly NBT + EntityPlayer params |
| **Scripting Wrappers** | 50+ | HEAVY-MC | Medium — API surface stable-ish |
| **Abilities** | 44 | HEAVY-MC | High — DO NOT TOUCH yet |
| **Items** | 41 | LIGHT-MC | Medium — registration centralized in CustomItems.java |
| **AI** | 29 | HEAVY-MC | Medium — EntityAI API changes significantly |
| **Commands** | 22 | VERSION-LOCKED | Medium — Brigadier in 1.13+ |
| **Roles/Jobs** | 19 | LIGHT-MC | Low — mostly game logic with entity params |
| **Config** | 9 | LIGHT-MC | Low — Forge config API changes but logic same |

### 1.7.10 → 1.12.2 API Differences (Verified Against This Codebase)

| Pattern | Change Level | Files Affected | Details |
|---|---|---|---|
| FML package (cpw.mods.fml → net.minecraftforge.fml) | MINOR_CHANGE | 524 files | Mass rename, mechanical |
| Entity registration | MINOR_CHANGE | 1 file (CustomNpcs.java) | Signature change (added tracking params) |
| Event system (@SubscribeEvent, EVENT_BUS) | IDENTICAL | 16+ files | Same API |
| GUI handler (NetworkRegistry) | IDENTICAL | 1 file | Same API |
| NBT (NBTTagCompound) | IDENTICAL | Hundreds | Same API |
| World save data (WorldSavedData) | IDENTICAL | Several | Same API |
| @Mod, @EventHandler, @SidedProxy | MINOR_CHANGE | ~5 files | Package rename |
| **ItemStack null→EMPTY** (1.11+) | **BREAKING_CHANGE** | **37+ null checks, 390+ files ref ItemStack** | **stack==null must become stack.isEmpty(). MASSIVE — the plan originally missed this entirely** |
| Packet system (SimpleNetworkWrapper) | BREAKING_CHANGE | 100+ files | Becomes SimpleChannel |
| IInventory → IItemHandler (Capabilities) | BREAKING_CHANGE | 59+ files | New capability system alongside old |
| Block/Item registration (GameRegistry) | BREAKING_CHANGE | 18 files (62 calls in CustomItems.java) | Deprecated in 1.12; must use RegistryEvent.Register<Block/Item> |
| **World.getBlock → getBlockState** | BREAKING_CHANGE | 59+ files | Block metadata → IBlockState system |
| **getUnlocalizedName → getTranslationKey** | BREAKING_CHANGE | 59+ files | Deprecated in 1.11+ |

> **⚠️ CORRECTION (2026-03-15):** The previous version of this table listed GameRegistry as "IDENTICAL" and was missing ItemStack nullability, getBlockState, and getTranslationKey changes entirely. The 1.12.2 port is NOT 95% mechanical — it's ~60-70% mechanical (the 524 renames are real), with 30-40% requiring real code changes that will cause crashes if missed.

---

## Critical Research Findings

### The Hard Truth: Nobody Has Done This Before

**No Minecraft mod in existence successfully shares code between 1.7.10 and modern (1.16+) versions.** Exhaustive GitHub search confirmed:

| Pattern | Example | Lowest MC Version | Shared Code? |
|---|---|---|---|
| Preprocessor multi-version | Polyfrost/OneConfig (280★) | **1.8.9** (not 1.7.10) | Yes, via `#if MC>=` |
| Multi-module per-loader | JEI (Core/Common/NeoForge/Fabric) | Per-branch only | Yes, within one version |
| Branch-per-version | Applied Energistics 2 | 1.15+ (no 1.7.10 branch) | No — independent codebases |
| Separate repos | GregTechCEu (1.12) + GT-Modern (1.20) | 1.12 / 1.20 separately | No — complete rewrites |
| 1.7.10 only | GTNH ecosystem (93 repos) | 1.7.10 | N/A |

**CustomNPC-Plus's platform-api/core approach is genuinely novel** — it's the JEI Core/Common pattern applied retroactively to 1.7.10, which nobody else has attempted.

### Build System: Composite Builds, NOT Separate Wrappers

Oracle recommended separate Gradle wrappers. **This is wrong.** Evidence from real implementations:

| Tool | What It Does | Status |
|---|---|---|
| **Gradle Composite Builds** (`includeBuild`) | Each version is an independent build with its own plugin classpath | VERIFIED — only stable way to mix RFG + FG |
| **Gradle Toolchains** | Run Gradle on JDK 21, compile with JDK 8 or 17 per subproject | VERIFIED — standard Gradle feature |
| **ModStitch** (github.com/isXander/modstitch) | Unifies RFG + ForgeGradle + NeoGradle in one build | **UNVERIFIED** — RFG support is still an unmerged PR (#42). Do not rely on this. |
| **ReplayMod Preprocessor** | `#if MC>=` directives in Java comments | VERIFIED — works, but build tooling doesn't support 1.7.10 via Loom |
| **Essential Gradle Toolkit** | Wraps preprocessor with cleaner DSL | VERIFIED — plugin `gg.essential.multi-version` v0.6.7 |
| **Deftu multiversion** | Production multi-version plugin (used by OneConfig) | VERIFIED — `dev.deftu.gradle.multiversion-root` v2.73.0 |

**Key constraint:** RFG and ForgeGradle/NeoGradle CANNOT share a Gradle classpath in multi-project builds. Composite builds (`includeBuild`) are required.

### Preprocessor Limitations for 1.7.10

The ReplayMod preprocessor **syntax** supports 1.7.10 (version `10710`). But:
- The **build tooling** (Architectury Loom, Essential Loom) does NOT support 1.7.10
- ReplayMod itself dropped 1.7.10 support when moving to Gradle 7
- The preprocessor is a text tool — it works on any Java file regardless of MC version
- **For CustomNPC-Plus:** The preprocessor could process source files for 1.7.10 IF combined with RetroFuturaGradle (not Loom). This needs prototyping.

---

## Corrected Architecture (Post-Critical-Review)

### Oracle's Recommendation vs Evidence-Based Correction

| Aspect | Oracle Said | Evidence Says | Decision |
|---|---|---|---|
| Build system | Separate Gradle wrappers per leaf | Composite builds + single wrapper | **Composite builds** |
| Layer count | 6 layers (platform-api → core → legacy-common → modern-common → leaves) | Preprocessor can replace family-common layers | **5 layers with optional preprocessor in version leaves** |
| legacy-common / modern-common | Dedicated modules | ~100+ files could go here, BUT preprocessor handles syntactic diffs | **Start without them; add only if code duplication proves >50 files** |
| 1.7.10→1.12.2 gap | Treated as equivalent to other gaps | Easier than 1.16+ but NOT trivial — 524 renames + 5 breaking changes (ItemStack null→EMPTY, IInventory, getBlockState, getTranslationKey, GameRegistry events) | **1.12.2 is ~60-70% mechanical, 30-40% real code changes** |

### Recommended Project Structure

```
CustomNPC-Plus/
  platform-api/          → Plain Java 8 library (interfaces) — SHARED BY ALL
  core/                  → Plain Java 8 library (game logic) — SHARED BY ALL
  mc1710/                → Current src/main/java/ (RetroFuturaGradle) — COMPOSITE BUILD
  mc1122/                → 1.12.2 (RetroFuturaGradle — yes, RFG handles 1.12.2) — COMPOSITE BUILD
  mc1165/                → 1.16.5 (ForgeGradle 6) — COMPOSITE BUILD
  mc1201/                → 1.20.1 (NeoGradle/ModDevGradle 2) — COMPOSITE BUILD
  gradle-plugins/        → TypeScript .d.ts generator (unchanged)
```

**Why no legacy-common/modern-common initially:**
- The preprocessor can handle syntactic differences within version leaves
- Most LIGHT-MC code should be migrated to `core/` with platform interfaces instead of duplicated in a family-common layer
- If >50 files of genuine version-family-specific code emerges during porting, THEN add the intermediate layers
- Simpler architecture = less overhead = faster iteration

### Build System Architecture

```
Root settings.gradle:
  include 'platform-api', 'core'           → Plain Java libraries (multi-project)
  includeBuild 'mc1710'                     → Composite build (RetroFuturaGradle)
  includeBuild 'mc1122'                     → Composite build (RetroFuturaGradle)
  includeBuild 'mc1165'                     → Composite build (ForgeGradle 6)
  includeBuild 'mc1201'                     → Composite build (NeoGradle)
  includeBuild 'gradle-plugins'             → Already composite

Each mc*/ has its own:
  settings.gradle         → Declares dependency on root platform-api/core via includeBuild '..'
  build.gradle            → Applies version-specific Gradle plugin
  gradle.properties       → MC version, Forge version, mappings
  src/main/java/          → Version-specific implementations
```

**Gradle Toolchains** handle JDK differences (8 for legacy, 17 for 1.20.1).

---

## Revised Abstraction Order (Maximum Parallelism)

The key insight: **1.12.2 is significantly closer to 1.7.10 than 1.16.5+, but it is NOT trivial.** Beyond the 524 mechanical renames, there are 5 breaking API changes affecting 200+ files total. Still, it is the easiest version jump and should be tackled first.

### Track A: Core Abstraction (enables ALL versions)
1. Finish mc1710 shadow files for migrated controllers
2. Expand `IPlayer` / `IItemStack` / `IWorld` usage in core (use the real platform-api interfaces — DO NOT create duplicates)
3. Dialog cluster → Quest cluster → PlayerData migration to core
4. Packet DTO abstraction in core
5. Command abstraction in core

### Track B: 1.12.2 Port (CAN START IMMEDIATELY — easier than 1.16+ but NOT trivial)
1. Copy mc1710/ → mc1122/
2. Mass-rename cpw.mods.fml → net.minecraftforge.fml (524 files, mechanical)
3. **Replace all ItemStack null checks with isEmpty()** (37+ files with null checks, 390+ files reference ItemStack — the BIGGEST hidden change)
4. Update block/item registration to RegistryEvent pattern (18 files, 62 calls in CustomItems.java)
5. Update entity registration signature (1 file)
6. Replace getUnlocalizedName → getTranslationKey (59+ files)
7. Address World.getBlock → getBlockState usage (59+ files)
8. Refactor packet system (SimpleNetworkWrapper → SimpleChannel — structural rewrite)
9. Begin IInventory → IItemHandler migration for containers (59+ files, can be incremental)
10. Set up RetroFuturaGradle for 1.12.2 (confirmed: RFG supports 1.12.2)
11. Test and iterate

### Track C: 1.16.5 Port (start after Track A Phase 1-3)
1. Create mc1165/ with ForgeGradle 6
2. Implement PlatformService for 1.16.5
3. Port core controllers (they just work — MC-free)
4. Port networking (new packet system)
5. Port commands (Brigadier adapter)
6. Port blocks (BlockState — the Flattening work)
7. Port essential admin GUIs (rewrite, not port)

### Track D: 1.20.1 Port (start after Track C is 50% done)
1. Create mc1201/ based on mc1165/ (close enough)
2. Implement PlatformService for 1.20.1
3. Adapt for NeoForge API differences
4. Mostly rides on 1.16.5 work

**Tracks A and B can run in parallel immediately.** Track C starts when core interfaces exist. Track D is incremental from C.

---

## MVP Targets Per Version

### 1.7.10 — Full Primary (unchanged)
All features, full parity. This is the production build.

### 1.12.2 — "Bridge Build" (Closest to 1.7.10, but has real breaking changes)
- NPC spawn, persist, despawn lifecycle
- Dialog system, Quest system, Faction system
- Core networking (NPC sync, player data)
- Basic AI + combat
- Essential editor/admin GUIs
- Commands (`/kam` hierarchy)
- **NOT initially:** Abilities, auction, animation maker, scripting IDE

### 1.16.5 — "NPC Core" Modern
- Same feature set as 1.12.2 MVP
- Server-first: commands and admin workflows before full GUI
- Basic NPC editor screen
- **NOT initially:** Full GUI suite, abilities, auction, custom models

### 1.20.1 — "NPC Core" Latest
- Same as 1.16.5 — incremental from it
- Client polish lags behind server feature parity

---

## Critical Warnings

1. **You're the first to do this** — no existing mod shares code between 1.7.10 and modern. Expect uncharted territory.
2. **Don't mirror Minecraft class-for-class in platform-api** — keep interfaces coarse and purpose-driven
3. **Don't promise full GUI parity on modern versions first** — 212 files, completely different per version, do LAST
4. **Don't let preprocessor guards spread into core/** — only in version-leaf code
5. **1.12.2 is easier than 1.16+ but NOT trivial** — 524 mechanical renames + 5 breaking changes (ItemStack null→EMPTY, IInventory, getBlockState, getTranslationKey, GameRegistry events) affecting 200+ files
6. **Composite builds, not separate wrappers** — one Gradle wrapper, `includeBuild` per version leaf
7. **API merge is a phased operation** — move MC-free interfaces first (~131), then strip MC imports from the remaining 71. Don't try to move everything in one shot.

---

## Open Questions / Future Decisions

1. Can the ReplayMod preprocessor be made to work with RetroFuturaGradle (instead of Loom)? Needs prototyping.
2. Should version leaves share a single `build.gradle.kts` via the preprocessor's pattern (all versions point to `../../build.gradle.kts`)? Or keep independent build files?
3. Scripting API compatibility — can the same `.js`/`.java` scripts work across versions?
4. Addon compatibility — DBC Addon, Gecko Addon, AW Addon need version-specific builds
5. Data format compatibility — can NPC save files from 1.7.10 load on 1.20.1?
6. When to add `legacy-common/` and `modern-common/` layers — set threshold at >50 duplicated files

---

## Research Agent Summary (12 agents deployed)

| Agent | Type | Key Finding |
|---|---|---|
| explore #1 | MC-dependent code | 1,424 files import net.minecraft.*, GUI is biggest hotspot (212 files) |
| explore #2 | Build system | Single-version locked, GTNH Convention + RFG |
| explore #3 | Subsystem coupling | Controllers/data are LIGHT-MC, AI/networking are HEAVY-MC |
| explore #4 | 1.7.10→1.12.2 API diff | 95% identical, only packets break, 524 files need package rename |
| explore #5 | legacy-common validation | ~100+ files use pre-Flattening patterns (metadata, IInventory) |
| librarian #1 | Porting strategies | Architectury 1.16.5+ only, preprocessor for syntactic diffs |
| librarian #2 | Real multi-version repos | Essential Mod gold standard, modstitch bridges RFG+FG, Loom consumes plain Java libs |
| librarian #3 | ForgeGradle matrix | RFG handles 1.7.10+1.12.2, FG6 for 1.16.5, ModDevGradle for 1.20.1, composite builds required |
| unspecified-high #1 | Preprocessor research | ReplayMod preprocessor syntax supports 1.7.10 (10710), but Loom doesn't; Essential wraps it |
| unspecified-high #2 | Real multi-version mods | **Nobody shares code between 1.7.10 and modern** — CustomNPC+ approach is genuinely novel |
| unspecified-high #3 | Write MULTI_VERSION_RESEARCH.md | Compiled all external research to permanent doc |
| unspecified-high #4 | Write MC_COUPLING_ANALYSIS.md | Compiled all coupling data to permanent doc |
| Oracle | Architecture strategy | Two-family monorepo (partially overridden by evidence) |
