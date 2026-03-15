# platform-api/ — Interface Definitions

MC-free interfaces consumed by `core/`. Each MC version module provides implementations.
The scripting API interfaces (`noppes.npcs.api.*`) are being merged into this module.

## Structure

```
kamkeel/npcs/platform/           → PlatformService, PlatformServiceHolder, annotations
kamkeel/npcs/platform/config/    → IConfigProvider
noppes/npcs/api/                 → Entity, item, handler, ability, NBT interfaces (from scripting API)
noppes/npcs/platform/nbt/        → NBTFactory, NBTIO abstractions
```

## Naming Conventions

- **Use the existing scripting API names as-is**: `IPlayer`, `IEntity`, `IItemStack`, `IWorld`, `ICustomNpc`, `INbt`, etc.
- **NEVER create semantically duplicate interfaces** — no `IUser`, `IStack`, `IGameWorld`, `IMob`. These don't exist and never should.
- **No `Platform` prefix** on interfaces — only `PlatformService` itself uses it
- **MC1710 wrappers**: `[Thing]Wrapper` — `PlayerWrapper`, not `MC1710PlayerWrapper`

## Key Interfaces (actual, from noppes.npcs.api.*)

| Interface | Wraps | Package |
|---|---|---|
| `IPlayer` | EntityPlayerMP | `noppes.npcs.api.entity` |
| `IEntity` | Entity | `noppes.npcs.api.entity` |
| `IEntityLivingBase` | EntityLivingBase | `noppes.npcs.api.entity` |
| `IEntityLiving` | EntityLiving | `noppes.npcs.api.entity` |
| `ICustomNpc` | EntityCustomNpc | `noppes.npcs.api.entity` |
| `IItemStack` | ItemStack | `noppes.npcs.api.item` |
| `IWorld` | World | `noppes.npcs.api` |
| `INbt` | NBTTagCompound | `noppes.npcs.api` |
| `INbtList` | NBTTagList | `noppes.npcs.api` |
| `IPos` | BlockPos | `noppes.npcs.api` |
| `IDamageSource` | DamageSource | `noppes.npcs.api` |

## PlatformService

Central SPI (`kamkeel.npcs.platform.PlatformService`): NBT factory + I/O, file paths, entity/item/world wrapping, logging, environment queries, scheduling. Access: `PlatformServiceHolder.get()`.

## Rules

- **Interfaces only** — no MC imports allowed
- `@ClientOnly` / `@ServerOnly` annotations for side-specific contracts
- New interfaces use existing API names — check `src/api/java/` before creating anything new
