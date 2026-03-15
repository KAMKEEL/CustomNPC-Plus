# Multi-Version Mod Architecture Research

> Permanent reference document. Findings marked **VERIFIED** come from real repositories; **THEORETICAL** findings need validation.

---

## 1. Real-World Multi-Version Mod Architectures

### Essential Mod (Gold Standard) — VERIFIED

- **Repo:** SparkUniverse/Essential-Mod (formerly EssentialGG)
- **Toolkit:** [Essential Gradle Toolkit](https://github.com/EssentialGG/essential-gradle-toolkit)
- Uses **ReplayMod Preprocessor** at scale
- **Structure:** `versions/` folder with sub-projects per MC version
- Single modern Gradle wrapper (8.x) with **Java toolchains** for Java 8/17/21
- Does **NOT** use separate `gradlew` files — uses one unified build

### ReplayMod Preprocessor — VERIFIED

- **Repo:** [github.com/ReplayMod/preprocessor](https://github.com/ReplayMod/preprocessor)
- **Structure:**
  - `common/src/main/java` — shared code with `#if` directives
  - `versions/` — subprojects per MC version
- Each version subproject applies its own Gradle plugin (ForgeGradle / RFG / Loom)
- `settings.json` defines version variables
- Root `build.gradle` wires common to version nodes via `preprocess {}` DSL
- **Real example:** Fallen-Breath/fast-ip-ping

### Essential Gradle Toolkit — VERIFIED

- **Plugin IDs:** `gg.essential.multi-version.root` / `gg.essential.multi-version`
- Wraps ReplayMod Preprocessor with cleaner DSL
- Supports version branching with `version(10710)`, `version(12001)`, etc.

### ModStitch Plugin — VERIFIED

- **Repo:** [github.com/isXander/modstitch](https://github.com/isXander/modstitch)
- Designed to unify Official Tooling (NeoGradle / ForgeGradle) with RetroFuturaGradle
- Enables single build system spanning 1.7.10 to 1.20.1
- Used by Archipelago CoreClient (bridging 1.7.10 RFG and 1.20.1 NeoGradle)

---

## 2. Build System Findings

| Finding | Status |
|---|---|
| Architectury Loom CAN consume plain Java libraries (`core/`, `platform-api/`) as standard project dependencies — no remapping needed since they have no MC imports | **VERIFIED** |
| Modern Gradle 8.x CAN compile Java 8 code using Java Toolchains — no need for separate Gradle wrappers | **VERIFIED** |
| RetroFuturaGradle and ForgeGradle CAN potentially coexist in one build via modstitch | **THEORETICAL** — modstitch repo confirms design intent; needs validation with CustomNPC-Plus specifically |
| Single unified Gradle wrapper is the recommended approach (Essential mod proves this at scale) | **VERIFIED** |

---

## 3. Preprocessor Syntax

The ReplayMod Preprocessor uses comment-based directives that are valid Java (comments), so code compiles without the preprocessor too. Inactive branches are prefixed with `//$$ ` to comment them out.

```java
//#if MC>=11600
import net.minecraft.client.util.math.MatrixStack;
//#else
//$$ import net.minecraft.client.renderer.GlStateManager;
//#endif
```

Key syntax elements:
- `//#if MC>=11600` — conditional on version variable
- `//#else` — else branch
- `//#endif` — end conditional
- `//$$ ` — prefix for inactive code (commented out by preprocessor)

---

## 4. Key Insight: Oracle Was Wrong About Separate Gradle Wrappers

Oracle recommended separate Gradle wrappers per version leaf. Real-world evidence shows:

- **Essential Mod** uses **ONE** Gradle wrapper for **ALL** versions
- **Java Toolchains** handle JDK version differences (Java 8 for 1.7.10, Java 17 for 1.18+, Java 21 for 1.20.5+)
- **modstitch** plugin unifies RFG + ForgeGradle in one build
- Separate wrappers add unnecessary complexity and maintenance burden

---

## 5. Project Structure Template (Based on Real Implementations)

```
CustomNPC-Plus/
  platform-api/          → Plain Java 8 library (interfaces)
  core/                  → Plain Java 8 library (game logic)
  common/                → Shared MC-aware code with preprocessor directives
  versions/
    1.7.10/              → RetroFuturaGradle
    1.12.2/              → ForgeGradle or RFG
    1.16.5/              → ForgeGradle 5/6 or Architectury Loom
    1.20.1/              → NeoGradle or Architectury Loom
  gradle-plugins/        → TypeScript .d.ts generator
  settings.json          → Preprocessor version definitions
  build.gradle           → Root wiring
```

- `platform-api/` and `core/` are consumed as plain `implementation project(":core")` dependencies by all version leaves
- `common/` contains MC-aware code that uses `#if` directives for version differences
- Each `versions/<ver>/` subproject applies the appropriate Gradle plugin for that MC version
- One `gradlew` at root, one `settings.gradle`, one unified build

---

## 6. Corrected Architecture (vs Oracle's 6-Layer Recommendation)

**Oracle recommended:**
```
platform-api → core → legacy-common → modern-common → version leaves
```

**Evidence suggests:**
```
platform-api → core → common (with preprocessor) → version leaves
```

### Why the preprocessor replaces separate common modules:

- The preprocessor **REPLACES** the need for separate `legacy-common` / `modern-common` modules
- Syntactic differences (import paths, method signatures, rendering APIs) are handled by `#if` directives in a single `common/` module
- Only truly version-specific code (entity registration, rendering pipelines, network handlers) goes in version leaves
- This is proven at scale by Essential Mod, which supports 10+ MC versions with a single `common/` source set

### What goes where:

| Layer | Contents | MC Dependencies |
|---|---|---|
| `platform-api/` | Interfaces only (`IPlayer`, `INbt`, `PlatformService`) | None |
| `core/` | MC-free game logic (controllers, data classes, enums, utils) | None |
| `common/` | Shared MC-aware code with `#if` preprocessor directives | Yes (version-gated) |
| `versions/<ver>/` | Entity registration, rendering, version-specific implementations | Full MC + Forge/NeoForge |
