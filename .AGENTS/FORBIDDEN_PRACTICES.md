# Forbidden Practices — CustomNPC-Plus

Quick-reference guardrails. Prevents regressions, guides contributors.
See also: `AGENTS.md` (architecture), `SESSION_MEMORY.md` (roadmap).

---

## 1. NEVER Rules

### Thread Safety
- **NEVER use mutable static fields for shared state.** Known violation: `RandomPositionGeneratorAlt.staticVector` — a mutable `Vec3` reused across calls. Do not replicate this pattern.

### Core / Platform-API Isolation
- **NEVER import MC classes in `core/` or `platform-api/`.** Use the existing platform-api interfaces (`INbt`, `IPlayer`, `IItemStack`, `IWorld`, `IEntity`, `ICustomNpc`, etc.).
- **NEVER import wrapper classes in `core/`.** Only interfaces from `platform-api/`.
- **NEVER call `getMCNBT()` / `getMCTagList()` from `core/` code.** These are escape hatches on `INbt`/`INbtList` for mc1710 code only.
- **NEVER use `new NBTTagCompound()` in `core/`.** Use `NBT.compound()` / `NBT.list()`.
- **NEVER use `CompressedStreamTools` in `core/`.** Use `PlatformServiceHolder.get().readCompressedNBT()`.
- **NEVER use `LogWriter` in `core/`.** Use `PlatformServiceHolder.get().logError()`.
- **NEVER create semantically duplicate interfaces.** Use the real interfaces from `platform-api/` (`IPlayer`, `IItemStack`, `IWorld`, `IEntity`, `ICustomNpc`, `INbt`, etc.). Do NOT invent new names like `IUser`, `IStack`, `IGameWorld` — those don't exist and never should.

### Split-Package Contract
- **NEVER break the split-package shadow.** When a class exists in both `core/` and `src/main/java/` with the same package, mc1710 shadows core at compile time. The mc1710 version adds `implements IFaction`, SyncController calls, MC-specific methods. Core version must remain MC-free.

### Naming
- **Platform interfaces:** Use the scripting API interface names as-is (`IPlayer`, `IEntity`, `IItemStack`, `IWorld`, `ICustomNpc`). These are being merged into platform-api from the scripting API. Do NOT create new names.
- **MC1710 wrappers:** `[Thing]Wrapper`. No `MC1710` prefix. `PlayerWrapper` not `MC1710PlayerWrapper`.
- **Scripting API** (`IPlayer`, `IEntity`, `ICustomNpc`) is being merged into `platform-api/` — same package (`noppes.npcs.api.*`), stripped of MC type parameters. MC-free versions live in platform-api, version-specific extensions shadow in each mc*/ leaf.

### Per-Version Code (DO NOT ABSTRACT)
- **NEVER abstract mixins** (`noppes.npcs.mixin`). Leave the current 1.7.10 mixins as-is. Each version leaf will have its own version-specific mixins.
- **NEVER abstract addon classes** (`kamkeel.npcs.addon`). These are per-version integrations. Leave 1.7.10 addons as-is. New version implementations will be created separately after migration.
- **NEVER abstract `BucketUtil` or `VaultUtil`**. These are per-version utility classes. Leave the 1.7.10 versions as-is. Each version leaf gets its own implementation.

---

## 2. Architectural Guardrails

### Dependency Flow
```
platform-api/  (interfaces only, zero MC deps)
     ^
   core/       (game logic, MC-free)
     ^
src/main/java/ (mc1710: wrappers, entities, GUI, packets)
```
Violations = build failures. Core depends on platform-api only.
