# Core Abstraction Plan

**Last updated:** 2026-03-13
**Current state:** 140 core + 43 platform-api = **183 abstracted files** out of ~1,810

---

## Naming Conventions

### Platform Interfaces (`platform-api/.../kamkeel/npcs/platform/entity/`)
| Interface | Wraps | Extends |
|---|---|---|
| `IMob` | Any entity (Entity) | — |
| `ILiving` | Living entity (EntityLivingBase) | `IMob` |
| `IUser` | Player (EntityPlayerMP) | `ILiving` |
| `INpc` | NPC entity (EntityNPCInterface) | `ILiving` *(not yet created)* |
| `IStack` | ItemStack | — |
| `IGameWorld` | World | — |
| `IDamage` | DamageSource | — |

### MC1710 Wrapper Implementations (`src/.../kamkeel/npcs/wrapper/platform/`)
| Wrapper | Implements |
|---|---|
| `EntityWrapper` | `IMob` |
| `LivingWrapper` | `ILiving` |
| `PlayerWrapper` | `IUser` |
| `StackWrapper` | `IStack` |
| `WorldWrapper` | `IGameWorld` |
| `DamageWrapper` | `IDamage` |

### NBT Wrappers (`src/.../noppes/npcs/wrapper/nbt/`)
| Wrapper | Implements |
|---|---|
| `NBTWrapper` | `INBTCompound` |
| `NBTListWrapper` | `INBTList` |
| `NBTWrapperFactory` | `NBTFactory` |
| `NBTWrapperIO` | `NBTIO` |

### Naming Rules
- **Platform interfaces**: Short `I` prefix names. No `Platform` in the name.
- **MC1710 wrappers**: `[Thing]Wrapper` — no `MC1710` prefix. Each platform module's wrappers live in their own Gradle module so no collision.
- **Core code** never imports wrappers or MC classes. Only interfaces from `platform-api`.
- **Scripting API** (`IPlayer`, `IEntity`, `ICustomNpc`) stays unchanged — different package, different purpose.

---

## Architecture

```
platform-api/          → Interfaces only (INBTCompound, IUser, IStack, etc.)
     ↑
   core/               → All game logic (controllers, data classes, enums, utils)
     ↑
src/main/java/ (mc1710) → MC 1.7.10 implementations (wrappers, entities, GUI, packets)
```

### Key Patterns
- **NBT**: `NBTTagCompound` → `INBTCompound`, `new NBTTagCompound()` → `NBT.compound()`
- **File I/O**: `CompressedStreamTools` → `PlatformServiceHolder.get().readCompressedNBT()`
- **Logging**: `LogWriter` → `PlatformServiceHolder.get().logError()`
- **Paths**: `CustomNpcs.getWorldSaveDirectory()` → `PlatformServiceHolder.get().getWorldSaveDirectory()`
- **Entity params**: `EntityPlayer` → `IUser`, `EntityNPCInterface` → `INpc`
- **Split-package shadow**: Core class in `core/`, mc1710 version in `src/main/java/` same package — shadows at compile time (adds `implements IFaction`, SyncController calls, etc.)

### What stays mc1710-only
- Entity classes (extend MC Entity directly)
- GUI / rendering (`@SideOnly(Side.CLIENT)`)
- Packets / network (ByteBuf, AbstractPacket)
- SyncController calls
- Scripting API interfaces (`IFaction`, `IMagicHandler`, etc.)
- `FieldDef` / ability GUI definitions
- Anything touching `GameRegistry`, `ForgeChunkManager`, `WeightedRandom`

---

## Next Steps (in priority order)

### Step 1: mc1710 Shadow Files for Migrated Controllers
The core versions of FactionController, MagicController, TagController are done but the mc1710 shadow files haven't been updated yet.

**For each controller, the mc1710 version needs:**
- `implements IFactionHandler` / `IMagicHandler` / `ITagHandler`
- SyncController calls re-added in save/delete methods
- EntityPlayer/IPlayer methods re-added
- MagicController: override `createDefaults()` to set ItemStack items

**Files to update:**
- `src/main/java/noppes/npcs/controllers/FactionController.java`
- `src/main/java/noppes/npcs/controllers/MagicController.java`
- `src/main/java/noppes/npcs/controllers/TagController.java`

### Step 2: IUser Implementation
The `IUser` interface exists but isn't used by any core class yet. This is the **single biggest unblock** — it gates ~20 data classes.

**What to do:**
- Start using `IUser` in core data classes that need player references
- Replace `EntityPlayer` params with `IUser` in migrated methods
- Wrap at mc1710 boundaries: `PlatformServiceHolder.get().wrapPlayer(entityPlayer)`

### Step 3: Availability → Core
`Availability.java` is the gatekeeper for Dialog and Quest systems. Once `IUser` is usable:
- Migrate `isAvailable(EntityPlayer)` → `isAvailable(IUser)`
- `StatCollector` calls need a platform abstraction or extraction
- `MathHelper` calls can be replaced with plain Java math

### Step 4: Dialog Cluster Migration
Depends on Availability being in core.

**Migration order:**
1. `DialogOption` → core (uses `dialog.availability.isAvailable()`)
2. `Dialog` → core (uses DialogOption, Availability, Lines/Line already in core)
3. `DialogCategory` → core (just references Dialog)
4. `DialogController` → core (file I/O pattern same as FactionController)

**Blockers to resolve:**
- `NBTJsonUtil` — Dialog/Quest controllers use this for JSON file storage. Need a core version or platform abstraction.
- `ICompatibilty` / `VersionCompatibility` — Dialog.readNBT references these

### Step 5: Quest Cluster Migration
Same pattern as Dialog, depends on Availability.

**Migration order:**
1. `Quest` → core
2. `QuestCategory` → core
3. `QuestData` → core
4. `QuestController` → core

### Step 6: IStack / Inventory Abstraction
Unblocks Bank, AuctionClaim/Listing, LinkedItem.

**What to do:**
- Use `IStack` in core data classes that handle items
- May need `IInventory` or similar for `NpcMiscInventory` abstraction

### Step 7: Player Data Classes
Once `IUser` is proven and Availability/Dialog/Quest are done:
- `PlayerData` → core (heavy, central class)
- `PlayerFactionData` → core
- `PlayerDialogData` (already in core)
- `PlayerTransportData` (already in core)
- Remaining `Player*Data` classes

### Step 8: INpc + Entity Data Sub-Objects
Create `INpc` interface, then migrate NPC data sub-objects:
- `DataDisplay`, `DataStats`, `DataAI`, `DataAdvanced`
- `DataTimers`, `DataSkinOverlays`
- These are NBT serialization classes — straightforward migration

---

## DO NOT TOUCH (yet)

- **Script system** — ScriptHandler, ScriptContainer, all I*Script* interfaces, Action framework
- **Ability.java** and ability type classes — 30+ MC imports, deeply coupled
- **Entity classes** — extend MC Entity, stay platform-side
- **Recipe system** — extends MC recipe classes

---

## Reference

- **Blocker details**: See `CORE_MIGRATION_STATUS.md` for per-file blocker analysis
- **Build**: `./gradlew.bat build` from project root
- **Pre-existing build warning**: `ClientEventHandler.java:79` orphaned token (not ours)
