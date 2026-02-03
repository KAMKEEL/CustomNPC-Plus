# dts-gradle-plugin

This repo contains the Gradle plugin for generating TypeScript `.d.ts` files for the CustomNPC-Plus API.

These files are used for the generation of auto-completion suggestions and type/interface documentation when scripting
in the Script Editor in-game.

### Implementation using JitPack

Add JitPack to the buildscript and depend on the plugin JAR. Using `main-SNAPSHOT` will build the latest `main` commit;
for reproducible builds use a tag or commit hash instead.

`settings.gradle`
```gradle
pluginManagement {
    resolutionStrategy {
        eachPlugin {
            if(requested.id.toString() == "dts.typescript-generator") {
                useModule("com.github.bigguy345:dts-gradle-plugin:main-SNAPSHOT")
            }
        }
    }

    
    repositories {
        maven { url "https://jitpack.io" }
        gradlePluginPortal()
        mavenCentral()
        mavenLocal()
    }
}
```




`build.gradle`:

```gradle
plugins {
    id 'dts.typescript-generator'
}

// ============================================================================
// TypeScript Definition Generation Task
// Generates .d.ts files from Java API sources for scripting IDE support
// ============================================================================
// TypeScript plugin is applied above in the main plugins block

tasks.named("generateTypeScriptDefinitions").configure {
    // Source directories containing the Java API code
    sourceDirectories = ['src/main/java']
    
    // Packages in source directories to generate .d.ts files for
    apiPackages = ['noppes.npcs.api'] as Set

    // Output directory for the generated .d.ts files
    // Must be within resources/assets/${modId}/api to be detected by CNPC+
    outputDirectory = "src/main/resources/assets/${modId}/api"
    
    // Whether to clean old generated files before regenerating
    cleanOutputFirst = true 

    // Optional: copy external patch .d.ts files into assets/<modid>/api/patches
    patchesDirectory = "dts-patches"
}


// Optional: To ensure definitions are generated on processing resources on jar build
// But in most cases, you may want to run the task manually when needed
// processResources.dependsOn generateTypeScriptDefinitions
```

---

## Patches (optional overrides)

Use `dts-patches/` to override or refine generated types. Files in this folder are copied to
`assets/<modid>/api/patches` after generation.

### Example: override `IPlayer.getDBCPlayer()` to return `IDBCAddon` instead of `IDBCPlayer`

File: `dts-patches/IPlayer.d.ts`

```ts
/**
 * DBC Addon patch for IPlayer
 * @javaFqn noppes.npcs.api.entity.IPlayer
 */
export interface IPlayer<T> {
  getDBCPlayer(): IDBCAddon;
}
```

