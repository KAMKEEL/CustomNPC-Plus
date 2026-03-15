# Scripting Toolchain — Java → TypeScript → Editor

## Overview
Pipeline that turns Java API interfaces into `.d.ts` TypeScript definitions, loaded at runtime
by the in-game script editor for autocomplete, hover info, and error detection.
Addons can ship their own `.d.ts` files for full editor integration.

## Pipeline Flow
```
Java API interfaces (src/api/java)
        ▼
dts.typescript-generator (Gradle plugin)
  1. JavaToTypeScriptConverter → .d.ts per file
  2. ImplementationFieldEnricher → inject impl fields
  3. hooks.d.ts → event hooks with param types
  4. dts-patches/ → ECMAScript built-in overrides
        ▼
src/main/resources/assets/customnpcs/api/**/*.d.ts  (in mod JAR)
        ▼
Runtime: JSTypeRegistry (client-side)
  1. DtsModScanner → scan ALL loaded mods for assets/<id>/api/
  2. TypeScriptDefinitionParser → parse .d.ts into JSTypeInfo
  3. DtsJavaBridge → map .d.ts types to Java reflection
        ▼
Script editor: autocomplete, hover, error detection
```

## Gradle Plugin (`gradle-plugins/`)
| File | Role |
|------|------|
| `TypeScriptGeneratorPlugin.groovy` | Plugin entry — registers `generateTypeScriptDefinitions` task |
| `GenerateTypeScriptTask.groovy` | Task orchestration — generation, patches, enrichment |
| `JavaToTypeScriptConverter.groovy` | Core converter — Java source → `.d.ts` with Javadoc, generics, nested types, `hooks.d.ts` |
| `ImplementationFieldEnricher.groovy` | Injects public impl fields into API interface `.d.ts` |

### Configuration (root `build.gradle`)
| Option | Purpose |
|--------|---------|
| `apiPackages` | Packages to generate `.d.ts` for (`noppes.npcs.api`, `net.minecraft`) |
| `implementationPackages` | Impl classes scanned for field enrichment (`noppes.npcs.scripted`, `noppes.npcs.controllers.data`, etc.) |
| `patchesDirectory` | Hand-written `.d.ts` overrides copied to `assets/<modid>/api/patches` |
| `cleanOutputFirst` | Deletes old generated `.d.ts` before regenerating |
| `mapJavaPrimitivesToJS` | `false` = preserve `int`/`float`/`double`; `true` = map all to `number` |

`processResources.dependsOn generateTypeScriptDefinitions` — runs automatically during build.

### Implementation Enrichment
Scripts access `event.npc`, `event.player` — public fields on impl classes, not API interfaces.
The enricher scans impl packages, and if exactly one implementer exists for an API interface,
injects its public instance fields. `final` → `readonly`. Multiple implementers → skipped.

## dts-patches Directory
Hand-written ECMAScript 5.1 built-in `.d.ts` files: `Array`, `Boolean`, `Date`, `Error`,
`GlobalFunctions`, `JSON`, `Math`, `Number`, `Object`, `RegExp`, `String`.
Provides autocomplete for standard JS globals in the Nashorn environment.

## Addon API Support
1. Place `.d.ts` files at `assets/<your-modid>/api/**/*.d.ts` in your mod JAR
2. `DtsModScanner` discovers them automatically (priority: `customnpcs` > `npcdbc` > others)
3. Register hooks via `IScriptHookHandler.registerHookDefinition()` for sidebar + stub generation

## Generated Output Structure
```
assets/customnpcs/api/
├── hooks.d.ts           # Hook signatures organized by context
├── entity/              # IPlayer.d.ts, ICustomNpc.d.ts, IEntity.d.ts
├── event/               # INpcEvent.d.ts, IPlayerEvent.d.ts (with @hookName)
├── handler/data/        # IQuest.d.ts, IDialog.d.ts, IFaction.d.ts
├── patches/ecmascript/  # Math.d.ts, JSON.d.ts, etc.
└── index.d.ts           # Re-exports all generated types
```

## Running
```bash
./gradlew generateTypeScriptDefinitions  # Generate only
./gradlew build                          # Generates as part of build
```
