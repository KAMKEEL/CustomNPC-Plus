# platform-api/ — Interface Definitions

MC-free interfaces consumed by `core/`. Each MC version module provides implementations.

## Structure

```
kamkeel/npcs/platform/       → PlatformService, PlatformServiceHolder, annotations
noppes/npcs/api/             → Entity, item, handler, NBT interfaces
noppes/npcs/platform/nbt/    → NBTFactory, NBTIO abstractions
```

## Naming Conventions

- **Short `I` prefix**: `IMob`, `ILiving`, `IUser`, `INpc`, `IStack`, `IGameWorld`, `IDamage`
- **No `Platform` prefix** on interfaces — only `PlatformService` itself uses it
- **Scripting API** (`IPlayer`, `IEntity`, `ICustomNpc`) is in the same package but separate concern

## Key Interfaces

| Interface | Wraps | Extends |
|---|---|---|
| `IMob` | Entity | — |
| `ILiving` | EntityLivingBase | `IMob` |
| `IUser` | EntityPlayerMP | `ILiving` |
| `INpc` | EntityNPCInterface | `ILiving` *(planned)* |
| `IStack` | ItemStack | — |
| `IGameWorld` | World | — |
| `IDamage` | DamageSource | — |

## PlatformService

Central SPI (`kamkeel.npcs.platform.PlatformService`): NBT factory + I/O, file paths, entity/item/world wrapping, logging, environment queries, scheduling. Access: `PlatformServiceHolder.get()`.

## Rules

- **Interfaces only** — no MC imports allowed
- `@ClientOnly` / `@ServerOnly` annotations for side-specific contracts
- New interfaces follow `I[Thing]` naming — keep names short
