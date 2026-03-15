# Forbidden Practices — CustomNPC-Plus

Quick-reference guardrails. Prevents regressions, guides contributors.
See also: `AGENTS.md` (architecture), `CORE_PLAN.md` (roadmap), `CORE_MIGRATION_STATUS.md` (blockers).

---

## 1. NEVER Rules

### Thread Safety
- **NEVER use mutable static fields for shared state.** Known violation: `RandomPositionGeneratorAlt.staticVector` — a mutable `Vec3` reused across calls. Do not replicate this pattern.

### Core / Platform-API Isolation
- **NEVER import MC classes in `core/` or `platform-api/`.** Use platform interfaces only (`INBTCompound`, `IUser`, `IStack`, `IGameWorld`).
- **NEVER import wrapper classes in `core/`.** Only interfaces from `platform-api/`.
- **NEVER call `getMCNBT()` / `getMCTagList()` from `core/` code.** These are escape hatches on `INbt`/`INbtList` for mc1710 code only.
- **NEVER use `new NBTTagCompound()` in `core/`.** Use `NBT.compound()` / `NBT.list()`.
- **NEVER use `CompressedStreamTools` in `core/`.** Use `PlatformServiceHolder.get().readCompressedNBT()`.
- **NEVER use `LogWriter` in `core/`.** Use `PlatformServiceHolder.get().logError()`.

### Split-Package Contract
- **NEVER break the split-package shadow.** When a class exists in both `core/` and `src/main/java/` with the same package, mc1710 shadows core at compile time. The mc1710 version adds `implements IFaction`, SyncController calls, MC-specific methods. Core version must remain MC-free.

### Naming
- **Platform interfaces:** Short `I` prefix, no `Platform` in name. `IUser` not `IPlatformUser`.
- **MC1710 wrappers:** `[Thing]Wrapper`. No `MC1710` prefix. `PlayerWrapper` not `MC1710PlayerWrapper`.
- **Scripting API** (`IPlayer`, `IEntity`, `ICustomNpc`) is a separate concern from platform interfaces. Different package, different purpose. Do not conflate.

---

## 2. Deprecated Components

| Component | Status | Replacement |
|---|---|---|
| `CommandNoppes` (`foxz.command`) | Deprecated | Use `/kam` command hierarchy (`kamkeel.npcs.command.CommandKamkeel`) |
| `GuiScriptInterface.saveText()` | Deprecated | Use current script save mechanism |
| `NBTTags.GetScript` / `GetScriptOld` / `NBTScript` | mc1710-only | Not migrated to core; uses `IScriptHandler` directly |
| `NBTTags.getItemStackList/Array` | mc1710-only | Uses `NoppesUtilServer.readItem()` + `ItemStack`; no core equivalent yet |

---

## 3. Forbidden Subsystem Areas (for Core Migration)

Do NOT attempt to migrate these to `core/`. See `CORE_PLAN.md` §DO NOT TOUCH.

| Subsystem | Reason | Tier |
|---|---|---|
| **Script system** — `ScriptHandler`, `ScriptContainer`, `I*Script*`, Action framework | Deeply coupled, not yet planned | 4 |
| **Ability.java** + ability type classes | 30+ MC imports (Block, Entity, DamageSource, Vec3, World) | 5 |
| **Entity classes** — `EntityNPCInterface`, `EntityCustomNpc`, all `EntityAbility*` | Extend MC `Entity` directly | 5 |
| **Recipe system** — `RecipeCarpentry`, `RecipeController` | Extends `ShapedRecipes`, uses `CraftingManager` | 5 |
| **Chunk system** — `ChunkController` | `ForgeChunkManager` dependency | 5 |
| **SyncController** | ByteBuf, packets, Minecraft server | 5 |
| **Profile system** — `ProfileController` | EntityPlayer lifecycle, Mojang API | 5 |

---

## 4. Architectural Guardrails

### Dependency Flow
```
platform-api/  (interfaces only, zero MC deps)
     ^
   core/       (game logic, MC-free)
     ^
src/main/java/ (mc1710: wrappers, entities, GUI, packets)
```
Violations = build failures. Core depends on platform-api only.

### mc1710 Shadow Responsibilities
When a controller is migrated to `core/`, the mc1710 shadow file MUST:
1. Add `implements IFactionHandler` (or equivalent scripting API interface)
2. Re-add `SyncController` calls in save/delete methods
3. Re-add `EntityPlayer`/`IPlayer` overload methods
4. Override MC-dependent factory methods (e.g., `createDefaults()` for ItemStack)

### NBT Serialization
- Core: `readNBT(INBTCompound)` / `writeNBT(INBTCompound)` — interfaces only
- mc1710: can use `NBTTagCompound` directly, cast via `getMCNBT()` at boundary

### File I/O in Core
- `CustomNpcs.getWorldSaveDirectory()` → `PlatformServiceHolder.get().getWorldSaveDirectory()`
- Async saves use `CustomNPCsThreader` thread pool (already in core)

### What Stays mc1710-Only (permanently)
GUI/rendering (`@SideOnly`), packets/network (ByteBuf), `FieldDef`/ability GUI, `GameRegistry`, `ForgeChunkManager`, `WeightedRandom`, scripting API interface implementations.
