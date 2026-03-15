# core/ — MC-Free Game Logic

Core contains version-independent game logic: controllers, data classes, constants, utilities.
It depends only on `platform-api/` interfaces — **never** on MC classes, wrappers, or Forge.

## Structure

```
noppes/npcs/controllers/       → FactionController, MagicController, TransportController, etc.
noppes/npcs/controllers/data/  → Faction, Magic, Tag, TransportLocation, Frame, Lines, etc.
noppes/npcs/constants/         → 40+ enums (EnumRoleType, EnumQuestType, EnumAnimation, etc.)
noppes/npcs/core/              → NBT factory, CoreConfig
noppes/npcs/util/              → ValueUtil, LRUHashMap, CacheHashMap, MillisTimer
noppes/npcs/                   → NBTTags.java
kamkeel/npcs/                  → Additional controllers, network enums, utilities, developer registry
```

## Forbidden Imports

Core code must **never** import:
- `net.minecraft.*`, `cpw.mods.*`, `net.minecraftforge.*`, `org.spongepowered.*`
- Wrapper classes from `src/main/java/` (mc1710 module)
- Only allowed: `java.*`, `javax.*`, `noppes.npcs.*`, `kamkeel.npcs.*` (from core + platform-api)

## Key Patterns

| MC Concept | Core Replacement |
|---|---|
| `new NBTTagCompound()` | `NBT.compound()` |
| `CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` |
| `LogWriter` | `PlatformServiceHolder.get().logError()` |
| `CustomNpcs.getWorldSaveDirectory()` | `PlatformServiceHolder.get().getWorldSaveDirectory()` |
| `EntityPlayer` param | `IUser` (via platform-api) |
| `EntityNPCInterface` param | `INpc` (via platform-api, not yet created) |

## Split-Package Shadows

Core classes share packages with mc1710 classes. The mc1710 version can:
- Add `implements IFaction`, `implements IFactionHandler` (scripting API)
- Re-add `SyncController` calls for network sync
- Override methods that need `ItemStack`, `EntityPlayer`, etc.

## DO NOT TOUCH (Forbidden Zones)

- **Script system** — ScriptHandler, ScriptContainer, I*Script*, Action framework
- **Ability system** — Ability.java and ability type classes (30+ MC imports)
- **Entity classes** — extend MC Entity directly
- **Recipe system** — extends MC recipe classes

## Adding New Core Code

1. Ensure zero MC imports — use platform-api interfaces
2. NBT via `NBT.compound()` / `NBT.list()`, file I/O via `PlatformServiceHolder`
3. Stub out methods needing MC types — mc1710 shadow adds them
4. Follow existing package structure (controllers → `controllers/`, data → `controllers/data/`)
