# Phase 0A Report — Build Restructure: mc1710 as Gradle Subproject

**Date:** 2026-03-15
**Status:** COMPLETE
**Build verification:** `./gradlew :mc1710:build` ✅ | `./gradlew build` ✅

---

## Summary

Phase 0A restructures the CustomNPC-Plus build system so that the MC 1.7.10 Forge mod lives in a Gradle multi-project subproject (`mc1710/`), while the root project becomes a lightweight coordinator. This enables future MC version leaves (mc1122, mc1165, mc1201) to be added as additional subprojects or composite builds.

**Zero Java source files were modified.** This was purely a build system restructure.

---

## What Changed

### New Directory Structure
```
Root/
  build.gradle           → Minimal coordinator (buildscript repos + idea plugin only)
  settings.gradle        → includes 'platform-api', 'core', 'mc1710' + includeBuild 'gradle-plugins'
  gradle.properties      → Settings-level props only (blowdryerTag, modName/Id/Group for GTNH settings convention)
  platform-api/          → Unchanged (plain Java 8 library)
  core/                  → Unchanged (plain Java 8 library, depends on platform-api)
  mc1710/                → NEW: Minecraft 1.7.10 Forge mod subproject
    build.gradle         → GTNH convention + RFG + dts.typescript-generator
    gradle.properties    → All mod-specific properties (forgeVersion, mixins, shadows, etc.)
    dependencies.gradle  → Moved from root
    repositories.gradle  → Moved from root
    src/main/java/       → Moved from root src/main/java/
    src/main/resources/  → Moved from root src/main/resources/
    src/api/             → Git submodule (re-added at new path)
    tools/               → Moved from root tools/
    dts-patches/         → Moved from root dts-patches/
  gradle-plugins/        → Unchanged (composite build, TypeScript .d.ts generator)
  .github/               → Unchanged (javadoc theme files referenced by mc1710 via ../)
```

### Files Created
| File | Purpose |
|------|---------|
| `mc1710/build.gradle` | GTNH convention + RFG plugin application, TypeScript generation, javadoc config |
| `mc1710/gradle.properties` | All mod-specific properties (MC version, Forge, mixins, shadows, publishing) |

### Files Modified
| File | Change |
|------|--------|
| `build.gradle` | Replaced with minimal coordinator (buildscript repos + `apply plugin: 'idea'`) |
| `settings.gradle` | Changed `include 'core', 'platform-api'` → `include 'platform-api', 'core', 'mc1710'` |
| `gradle.properties` | Stripped to settings-level props only; added modName/modId/modGroup for GTNH settings convention |
| `.gitmodules` | API submodule path changed from `src/api` → `mc1710/src/api` |

### Files Moved
| From | To |
|------|------|
| `src/main/` | `mc1710/src/main/` |
| `src/api` (submodule) | `mc1710/src/api` (submodule, re-added) |
| `tools/` | `mc1710/tools/` |
| `dts-patches/` | `mc1710/dts-patches/` |
| `dependencies.gradle` | `mc1710/dependencies.gradle` |
| `repositories.gradle` | `mc1710/repositories.gradle` |

---

## Key Decisions & Discoveries

### 1. GTNH Settings Convention Requires Root Properties
The `gtnhsettingsconvention` plugin validates `modName`, `modId`, and `modGroup` at settings evaluation time — **before** subproject gradle.properties are loaded. These three properties must remain in root gradle.properties even though they're also in mc1710/gradle.properties.

### 2. IDE Integration Module Incompatible with Multi-Project
The GTNH convention's `IdeIntegrationModule` calls `idea.getProject()` which returns `null` for non-root projects in Gradle. This causes a `NullPointerException` when the convention is applied to a subproject. **Fix:** Set `gtnh.modules.ideIntegration = false` in mc1710/gradle.properties. IDE setup will need to be handled at root level separately if needed.

### 3. Root Needs `idea` Plugin and Buildscript Repositories
The GTNH settings convention adds plugin version mappings that require classpath resolution. The root project needs:
- `buildscript.repositories` block with GTNH Maven + Gradle Plugin Portal
- `apply plugin: 'idea'` to prevent NPE in subproject convention modules that reference root IDEA model

### 4. Code Style Disabled at Root
`gtnh.modules.codeStyle = false` at root level prevents the settings convention from attempting to resolve Spotless/Checkstyle for the root project. mc1710/gradle.properties has `disableSpotless = true` and `disableCheckstyle = true` (same as before).

### 5. API Submodule Pinned to Correct Commit
The API submodule was re-added via `git submodule add`. The `dev` branch HEAD had newer commits with API changes that the existing code doesn't implement yet. The submodule was pinned to the exact commit (`d2194bb`) that was checked out before the restructure.

### 6. mc1710/build.gradle Adjustments from Original
| Change | Reason |
|--------|--------|
| `commandLine gradleWrapper, ':mc1710:build', '-Pnomixin'` | buildNoMixin task targets mc1710 specifically |
| `commandLine gradleWrapper, ':mc1710:runClient', '-PnoNashorn'` | runClientNoNashorn targets mc1710 |
| `commandLine gradleWrapper, ':mc1710:runServer', '-PnoNashorn'` | runServerNoNashorn targets mc1710 |
| `file('../.github/javadoc/...')` | Javadoc resources are at repo root, not in mc1710 |
| Removed `id 'idea'` from plugins | GTNH convention applies IDEA internally; standalone caused conflicts |

---

## Build Output

```
mc1710/build/libs/
  CustomNPC-Plus-1.11.1.jar              (17.7 MB, production JAR)
  CustomNPC-Plus-1.11.1-dev.jar          (17.6 MB, development JAR)
  CustomNPC-Plus-1.11.1-dev-preshadow.jar (15.4 MB, pre-shadow JAR)
  CustomNPC-Plus-1.11.1-sources.jar      (12.9 MB, sources)
```

---

## Build Commands (Updated)

```bash
# Build mc1710 module only
./gradlew :mc1710:build

# Build all modules (platform-api, core, mc1710)
./gradlew build

# Build without mixin embedding
./gradlew :mc1710:buildNoMixin

# Run the game
./gradlew :mc1710:runClient
./gradlew :mc1710:runServer

# Run without Nashorn
./gradlew :mc1710:runClientNoNashorn
./gradlew :mc1710:runServerNoNashorn

# Update API submodule
./gradlew :mc1710:updateAPI

# Generate TypeScript definitions
./gradlew :mc1710:generateTypeScriptDefinitions

# IDE setup
./gradlew :mc1710:genIntellijRuns
```

---

## Self-Review Checklist

| # | Check | Status |
|---|-------|--------|
| 1 | `./gradlew :mc1710:build` exits 0 and produces JAR in mc1710/build/libs/ | ✅ |
| 2 | `./gradlew build` exits 0 (all modules build) | ✅ |
| 3 | No files remain in root `src/` directory | ✅ (directory removed) |
| 4 | Root build.gradle does NOT apply `com.gtnewhorizons.gtnhconvention` | ✅ |
| 5 | mc1710/src/main/java/noppes/npcs/ exists | ✅ |
| 6 | mc1710/src/main/java/noppes/npcs/mixin/ exists | ✅ |
| 7 | mc1710/tools/ exists with JaninoLoader JAR | ✅ |
| 8 | mc1710/dts-patches/ exists | ✅ |
| 9 | `git submodule status` shows mc1710/src/api with valid commit hash | ✅ (d2194bb) |
| 10 | Root gradle.properties only has settings-level properties | ✅ (+ required modName/Id/Group) |
| 11 | mc1710/gradle.properties has all mod-specific properties | ✅ (57 properties) |
| 12 | .AGENTS/PHASE_0A_REPORT.md exists and is complete | ✅ (this file) |

---

## Known Residual Issues

1. **Locked file:** `tools/standalone-JaninoLoader-1.0.2.jar` at root level was locked by a process during the move. It is removed from git tracking but the physical file may persist until the lock is released. It's in `.gitignore` territory and won't affect the build.

2. **`gtnh.modules.ideIntegration = false`:** IntelliJ run configurations won't be auto-generated for mc1710. Users need to manually configure run configs with `--tweakClass org.spongepowered.asm.launch.MixinTweaker --mixin customnpcs.mixins.json` as before. This can be addressed in a follow-up by configuring IDE integration at root level.

3. **Root gradle.properties duplication:** `modName`, `modId`, `modGroup` are duplicated between root and mc1710 gradle.properties. This is necessary because the GTNH settings convention validates them before subproject properties are loaded. Future MC version modules will have their own modId values — the root values serve as defaults for the settings convention.

---

## Next Phase

**Phase 0B/0C:** Merge scripting API interfaces into platform-api, expanding the abstraction layer for multi-version support.
