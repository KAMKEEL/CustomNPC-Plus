# Phase 1A Report — mc1122 Workspace Setup

## Summary
Created the `mc1122/` Gradle module as a parallel Minecraft 1.12.2 leaf alongside `mc1710/`, using the same GTNH Convention Plugin + RetroFuturaGradle (RFG) build system.

**Result: BUILD SUCCESSFUL**

## What Was Created

### Directory Structure
```
mc1122/
├── .settings/
│   └── org.eclipse.jdt.core.prefs      (Java 8 Eclipse compiler settings)
├── src/
│   └── main/
│       ├── java/
│       │   └── noppes/npcs/             (empty — required by GTNH convention plugin modGroup validation)
│       └── resources/
│           ├── META-INF/                (empty — for access transformers later)
│           ├── assets/customnpcs/       (empty — for mod assets later)
│           └── mcmod.info               (1.12.2 mod metadata with token substitution)
├── dts-patches/                         (empty — for TypeScript definition patches later)
├── tools/                               (empty — for build tools later)
├── build.gradle                         (GTNH convention plugin, minimal config)
├── dependencies.gradle                  (core dependency only)
├── repositories.gradle                  (Sponge maven for mixins later)
└── gradle.properties                    (1.12.2 RFG configuration — the critical file)
```

### Files Modified
- `settings.gradle` (root) — Added `'mc1122'` to the `include` statement

## RFG Configuration Choices

| Property | Value | Rationale |
|---|---|---|
| `minecraftVersion` | `1.12.2` | Target MC version |
| `forgeVersion` | `14.23.5.2860` | Latest stable Forge for 1.12.2 |
| `channel` | `stable` | MCP mapping channel |
| `mappingsVersion` | `39` | `stable_39` — standard/latest stable MCP mappings for 1.12.2 |
| `enableModernJavaSyntax` | `false` | Java 8 strict — no Jabel |
| `enableGenericInjection` | `false` | Matching mc1710 config |
| `usesMixins` | `false` | Disabled initially — will enable in Phase 1B when mixin porting begins |
| `usesShadowedDependencies` | `false` | Disabled initially — no embedded deps yet |
| `disableSpotless` | `true` | No code style enforcement for now |
| `disableCheckstyle` | `true` | No checkstyle enforcement for now |
| `gtnh.modules.ideIntegration` | `false` | IDE setup handled at root level |

### Key Design Decision: GTNH Convention Plugin (not raw RFG)
The mc1122 module uses `com.gtnewhorizons.gtnhconvention` (same as mc1710), NOT the raw `com.gtnewhorizons.retrofuturagradle` plugin. This is critical because:
1. The settings-level `com.gtnewhorizons.gtnhsettingsconvention` plugin (v1.0.27) is already applied in root `settings.gradle`
2. The convention plugin automatically reads `gradle.properties` to configure RFG, MCP mappings, Forge version, mixin setup, etc.
3. It also sets up `dependencies.gradle` and `repositories.gradle` loading conventions
4. Using the same plugin stack ensures consistent build behavior across mc1710 and mc1122

## Build Verification

### Build Command
```
./gradlew :mc1122:build
```

### Build Result: **PASS**
```
BUILD SUCCESSFUL in 3m 3s
27 actionable tasks: 19 executed, 8 up-to-date
```

### Build Tasks Executed
The RFG pipeline ran the full MC 1.12.2 decompile/patch cycle:
1. `mergeVanillaSidedJars` — Merged client/server MC 1.12.2 JARs
2. `deobfuscateMergedJarToSrg` — Applied SRG mappings + access transformers
3. `decompileSrgJar` — FernFlower decompilation (cached)
4. `cleanupDecompSrgJar` — Applied FF patches, MCP patches, MCP cleanup
5. `patchDecompiledJar` — Applied 311 Forge patches
6. `remapDecompiledJar` — Remapped with MCP stable_39
7. `compilePatchedMcJava` — Compiled patched MC source
8. `compileJava` — NO-SOURCE (empty src, expected)
9. `jar` — Produced output JARs
10. `reobfJar` — Re-obfuscated for distribution

### JARs Produced
```
mc1122/build/libs/CustomNPC-Plus-1.12.2-1.11.1.jar          (1,279 bytes — obfuscated, distribution)
mc1122/build/libs/CustomNPC-Plus-1.12.2-1.11.1-dev.jar      (1,175 bytes — development, SRG names)
mc1122/build/libs/CustomNPC-Plus-1.12.2-1.11.1-sources.jar  (1,377 bytes — sources)
```
JARs are minimal (empty mod, only metadata) — expected for Phase 1A.

### Cross-Module Verification
- `./gradlew :mc1710:build` — **PASS** (still builds correctly, no regressions)
- `./gradlew :mc1122:build` — **PASS**

## Known Issue: `:core:compileJava` Standalone Failure

The `:core` module has a pre-existing compile error in `Faction.java:149` where `Faction` (which doesn't implement `IFaction` in core) is added to a `List<IFaction>`. This is a known consequence of the split-package shadow architecture:
- In `mc1710`, the shadow version of `Faction.java` adds `implements IFaction`, making the code valid
- Standalone `core` compilation fails because the core `Faction` class doesn't implement the interface

**Impact on mc1122:** None currently. The `:core:compileJava` task uses cached outputs from a previous successful mc1710 build. When mc1122 gets its own shadow `Faction.java` (Phase 1B), the same split-package mechanism will apply.

**Mitigation for CI:** If a clean build environment has no cached core classes, `mc1122:build` will fail on `:core:compileJava`. Solutions:
1. Build `mc1710` first (which compiles core with shadows), then `mc1122`
2. Or fix the core `Faction.java` to not require the shadow (e.g., cast to raw `List`)

## Deviations from mc1710

| Aspect | mc1710 | mc1122 | Reason |
|---|---|---|---|
| TypeScript generation | Yes (`dts.typescript-generator` plugin) | No | Not needed until source porting |
| API submodule | Yes (`src/api/java`) | No | API will be added in Phase 1B |
| Mixin embedding | Yes (shadow mixin 0.7.11) | No | Different mixin version for 1.12.2; deferred |
| Access transformers | Yes (`customnpcs_at.cfg`) | No | Will be ported in Phase 1B |
| `buildNoMixin` task | Yes | No | Deferred until mixins are configured |
| `archivesBaseName` | `CustomNPC-Plus` | `CustomNPC-Plus-1.12.2` | Disambiguate artifacts between versions |
| `mcmod.info` format | JSON object wrapper | JSON array (1.12.2 format) | 1.12.2 uses array-style mcmod.info |

## Notes on 1.12.2-Specific Tooling

1. **MCP Mappings**: 1.12.2 uses `stable_39` (vs `stable_12` for 1.7.10). These are the latest stable MCP mappings available for 1.12.2.
2. **Forge Version**: `14.23.5.2860` is the latest recommended Forge for 1.12.2 (last build of the 14.23.5 series).
3. **FernFlower Cache**: RFG cached the decompiled 1.12.2 jar, so subsequent builds are fast (~4s for cached builds).
4. **Forge Patches**: 311 patches applied to decompiled MC source (vs fewer for 1.7.10), reflecting the larger Forge surface area in 1.12.2.
5. **Mixin Version**: 1.12.2 typically uses SpongePowered Mixin 0.8.x (vs 0.7.11 for 1.7.10). This will be configured in Phase 1B.

## Next Steps (Phase 1B)

1. Port `CustomNpcs.java` entry point (`@Mod` class) with 1.12.2 registration patterns
2. Port `PlatformService` implementation for 1.12.2
3. Add access transformer file if needed
4. Configure mixins with Mixin 0.8.x
5. Begin porting entity, data, and controller classes with 1.12.2 API differences
