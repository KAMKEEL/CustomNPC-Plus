# BUILD_SYSTEM.md — Gradle Build Architecture

## Module Graph

```
Dependency flow:  platform-api  ←(api)──  core  ←(impl)──  root (mc1710)
                  gradle-plugins/ → composite build (dts.typescript-generator)
```

- **platform-api**: Zero deps. Interfaces (`PlatformService`, `INBTCompound`). Java 8.
- **core**: Depends on platform-api (transitive). MC-free game logic. Java 8.
- **root**: Depends on core. Full MC 1.7.10/Forge via GTNH convention plugin.
- **gradle-plugins**: Isolated composite build for `dts.typescript-generator` plugin.

## Build Flow

```
updateAPI (submodule init) → decompileMc (RetroFuturaGradle) → compileJava
  → generateTypeScriptDefinitions → processResources (bundles .d.ts)
  → shadowJar (Mixin + JaninoLoader unless -Pnomixin) → build
```

## Config Files

| File | Purpose |
|---|---|
| `build.gradle` | Plugins, version, custom tasks, TS gen config, javadoc |
| `settings.gradle` | Module includes, composite build, GTNH convention |
| `gradle.properties` | MC/Forge version, modId, mixin/shadow/spotless flags |
| `dependencies.gradle` | Runtime deps, shadow deps, core module reference |
| `repositories.gradle` | Sponge maven (Mixin), flatDir `lib/` |

## Custom Tasks (group: `CustomNPC+`)

| Task | Purpose |
|---|---|
| `updateAPI` | `git submodule update --init --recursive` — required first time |
| `buildNoMixin` | `build -Pnomixin` — jar without embedded Mixin |
| `runClientNoNashorn` / `runServerNoNashorn` | Launch without Nashorn scripting engine |
| `generateTypeScriptDefinitions` | Java API → `.d.ts` (see gradle-plugins/AGENTS.md) |

## TypeScript Generation Config

```
sourceDirectories:      src/api/java, src/main/java
outputDirectory:        src/main/resources/assets/customnpcs/api
apiPackages:            noppes.npcs.api, net.minecraft
implementationPackages: noppes.npcs.scripted, noppes.npcs.controllers.data,
                        noppes.npcs.entity.data, noppes.npcs.quests, kamkeel.npcs.controllers.data
patchesDirectory:       dts-patches/
```

## Dependencies (dependencies.gradle)

| Configuration | Used For |
|---|---|
| `implementation project(':core')` | Core game logic (transitively includes platform-api) |
| `shadowImplementation` | Embedded: Mixin (unless `-Pnomixin`), JaninoLoader |
| `runtimeOnlyNonPublishable` | Dev-only: NEI, Nashorn (unless `-PnoNashorn`) |

## Key gradle.properties

| Property | Value | Effect |
|---|---|---|
| `modId` | `customnpcs` | Mixin JSON, resource paths, TS output |
| `usesMixins` | `true` | Mixin AP + embed. Plugin: `mixin.CustomNPCsMixinPlugin` |
| `usesShadowedDependencies` | `true` | Enables `shadowImplementation` |
| `minimizeShadowedDependencies` | `false` | Keep all shadowed classes (reflection) |
| `disableSpotless` | `true` | No auto-formatting |
| `enableModernJavaSyntax` | `false` | Strict Java 8 (no Jabel) |
| `accessTransformersFile` | `customnpcs_at.cfg` | Forge access transformer |

## Quick Reference

```bash
./gradlew updateAPI                      # Init API submodule (first time)
./gradlew build                          # Full build with mixin embedding
./gradlew buildNoMixin                   # Without embedded Mixin
./gradlew runClient / runServer          # Launch game
./gradlew generateTypeScriptDefinitions  # Regen .d.ts only
./gradlew genIntellijRuns                # IDE setup (add Mixin tweaker args)
```
