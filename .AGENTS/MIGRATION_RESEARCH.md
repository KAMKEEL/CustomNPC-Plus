
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