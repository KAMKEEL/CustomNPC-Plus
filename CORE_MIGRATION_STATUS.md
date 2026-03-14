# Core Migration Status

**Last updated:** 2026-03-13
**Total:** 140 core + 43 platform-api = **183 abstracted files**
**Remaining in src/main/java:** ~1,627 files

---

## What's Done

### Controllers in Core (8 files)
| Controller | Data Classes in Core | Notes |
|---|---|---|
| `GlobalDataController` | _(self-contained)_ | Async save via CustomNPCsThreader |
| `ServerTagMapController` | `TagMap`, `Tag` | Clone tab/folder management |
| `TransportController` | `TransportCategory`, `TransportLocation` | Full CRUD |
| `FactionController` | `Faction` | SyncController calls stubbed |
| `MagicController` | `Magic`, `MagicCycle`, `MagicAssociation`, `MagicEntry`, `MagicData` | `createDefaults()` overrideable for ItemStack setup |
| `TagController` | `Tag` | UUID tag helpers migrated too |
| `CategoryManager` | `Category` | Filesystem-based category management |
| `APIRegistry` | _(self-contained)_ | Name→URL registry |

### Data Classes in Core (~60+ files)
See `core/src/main/java/` for full listing. Key clusters:
- Transport system (TransportCategory, TransportLocation)
- Faction system (Faction)
- Magic system (Magic, MagicCycle, MagicAssociation, MagicEntry, MagicData)
- Tag system (Tag, TagMap)
- Energy display/combat/anchor/homing/lifespan/lightning/panel/barrier/charging data
- Ability enums + ConditionFilter
- Attribute system (AttributeDefinition, PlayerAttribute, PlayerAttributeMap)
- Profile system (Slot, ProfileOptions, ProfileInfoEntry, ProfileOperation)
- Dialog display data (DialogImage, DialogColorData, Lines, Line)
- Frame/animation data (Frame, FramePart, HitboxData, TintData)
- Player data fragments (PlayerDialogData, PlayerTransportData, PlayerEffect, PlayerEffectData, AbilityHotbarData)
- Auction (AuctionFilter)
- Misc (SkinOverlay, InnDoorData, ItemDisplayData, PartyOptions, TraderStock, CloneFolder)

### Infrastructure in Core
- `NBTTags.java` — All map/set/list NBT helpers using INBTCompound/INBTList
- `NBT.java` — Factory: `NBT.compound()`, `NBT.list()`
- `CustomNPCsThreader` — Thread pool for async saves
- Network enums (EnumSyncType, EnumDataPacket, EnumRequestPacket, etc.)
- Developer registry

---

## TODO Stubs in Core (mc1710 must override/add)

### Scripting API Interface Implementations
These core classes do NOT implement their scripting API interfaces. The mc1710 split-package shadow must add `implements IXxx`:

| Core Class | mc1710 Must Add | Interface Location |
|---|---|---|
| `Faction` | `implements IFaction` | `src/api/java/.../IFaction.java` |
| `FactionController` | `implements IFactionHandler` | `src/api/java/.../IFactionHandler.java` |
| `Magic` | `implements IMagic` | `src/api/java/.../IMagic.java` |
| `MagicController` | `implements IMagicHandler` | `src/api/java/.../IMagicHandler.java` |
| `TagController` | `implements ITagHandler` | `src/api/java/.../ITagHandler.java` |
| `TransportController` | `implements ITransportHandler` | (referenced in TODO) |

### SyncController Calls
All core controllers stub out SyncController network sync. mc1710 must re-add these:

| Core File | Stubbed Method | SyncController Call |
|---|---|---|
| `FactionController` | `saveFaction()` | `SyncController.syncUpdate(EnumSyncType.FACTION, -1, compound)` |
| `FactionController` | `delete()` | `SyncController.syncRemove(EnumSyncType.FACTION, id)` |
| `MagicController` | `saveMagic()` | `SyncController.syncUpdate(EnumSyncType.MAGIC, -1, compound)` |
| `MagicController` | `removeMagic()` | `SyncController.syncRemove(EnumSyncType.MAGIC, magicID)` |
| `MagicController` | `saveCycle()` | `SyncController.syncUpdate(EnumSyncType.MAGIC_CYCLE, -1, compound)` |
| `MagicController` | `removeCycle()` | `SyncController.syncRemove(EnumSyncType.MAGIC_CYCLE, categoryId)` |
| `TagController` | _(not present — delete() doesn't sync in core)_ | `SyncController.syncRemove` if needed |

### EntityPlayer / Entity Methods
Methods that require MC entity types, left out of core:

| Core Class | Stubbed Methods | Depends On |
|---|---|---|
| `Faction` | `isFriendlyToPlayer(EntityPlayer)`, `isAggressiveToPlayer(EntityPlayer)`, `isNeutralToPlayer(EntityPlayer)` | `PlayerData.get(player).factionData` |
| `Faction` | `isAggressiveToNpc(EntityNPCInterface)` | `entity.faction.id` |
| `Faction` | `playerStatus(IPlayer)`, `isAggressiveToNpc(ICustomNpc)` | Scripting API entity types |
| `Faction` | `isFriendlyToPlayer(IPlayer)`, etc. | Delegates to EntityPlayer overloads |
| `TagController` | `sendCategoryTagMap(EntityPlayerMP, ...)` | `GuiDataPacket` |
| `TransportController` | `saveLocation(int, NBTTagCompound, EntityNPCInterface)` | `RoleTransporter`, `EnumRoleType` |
| `PlayerEffect` | `getEffect()` | `CustomEffectController` (mc1710 only) |
| `PlayerEffect` | `getAPI()` | `IPlayer` scripting API |

### ItemStack / GameRegistry
Methods that require MC item types:

| Core Class | Stubbed Feature | Depends On |
|---|---|---|
| `Magic` | `ItemStack item` field | `net.minecraft.item.ItemStack` |
| `Magic` | `setItem(ItemStack)` / `getItem()` | `GameRegistry.findUniqueIdentifierFor` |
| `Magic` | Item resolution in `readNBT`/`writeNBT` | `GameRegistry.findItem(modID, itemName)` |
| `MagicController` | Default magic ItemStack assignment in `createDefaults()` | `CustomItems.*` item references |

### NBTTags Omitted Methods
Methods left out of core `NBTTags.java` (stay in mc1710 version):

| Method | Reason |
|---|---|
| `getItemStackList`, `getItemStackArray` | Uses `NoppesUtilServer.readItem()` + `ItemStack` |
| `nbtItemStackList`, `nbtItemStackArray` | Uses `NoppesUtilServer.writeItem()` + `ItemStack` |
| `nbtDoubleList` | Uses `NBTTagDouble` directly |
| `GetScript`, `GetScriptOld`, `NBTScript` | Uses `IScriptHandler` / `IScriptUnit` |
| `getIntAt` | Uses `NBTTagInt.func_150287_d()` |

---

## Blocked Migrations & What Unblocks Them

### Tier 1: Blocked by ItemStack/Inventory Abstraction
**Unblock:** Add `IStack` wrapping to `PlatformService`, or create inventory abstraction

| File | Blocker |
|---|---|
| `Bank.java` | `NpcMiscInventory` (ItemStack-based currency/upgrade inventories) |
| `BankController.java` | Depends on `Bank` |
| `AuctionClaim.java` | `ItemStack.writeToNBT` / `readFromNBT` |
| `AuctionListing.java` | `ItemStack` operations |
| `AuctionBlacklist.java` | `GameRegistry`, `ItemStack` |
| `LinkedItem.java` | `ItemStack`, `Constants.NBT` |
| `RecipeAnvil.java` | `ItemStack`, `Constants.NBT` |
| `PlayerItemGiverData.java` | `JobItemGiver` (roles package) |

### Tier 2: Blocked by EntityPlayer Abstraction
**Unblock:** Implement Phase 0-1 `IUser` wrappers + use in core

| File | Blocker |
|---|---|
| `Availability.java` | `EntityPlayer` checks, `StatCollector`, `MathHelper` |
| `FactionOptions.java` | `EntityPlayer`, `PlayerFactionData`, `ChatComponentTranslation` |
| `DialogOption.java` | `EntityPlayer` via `dialog.availability.isAvailable(player)` |
| `Dialog.java` | `EntityPlayer`, `ICompatibilty`, `VersionCompatibility`, scripting API |
| `DialogCategory.java` | References `Dialog` (same-package) |
| `Quest.java` | `EntityPlayer`, `Availability`, scripting API |
| `QuestCategory.java` | References `Quest` (same-package) |
| `QuestData.java` | References `Quest` (same-package) |
| `CustomEffect.java` | `EntityPlayer`, `Constants.NBT` |
| `Party.java` | `EntityPlayer`, `EntityPlayerMP`, FML imports |
| `PlayerData.java` | `EntityPlayer` throughout |
| `PlayerFactionData.java` | `EntityPlayer`, `PlayerData` |
| `All Player*Data classes` | `EntityPlayer` / `EntityPlayerMP` coupling |
| `IRequirementChecker.java` | `EntityPlayer` in `check()` method |

### Tier 3: Blocked by Entity/World Abstraction
**Unblock:** `IMob`, `IGameWorld` wrappers

| File | Blocker |
|---|---|
| `SpawnData.java` | Extends `WeightedRandom.Item`, `DimensionManager`, `EntityList` |
| `SpawnController.java` | `WeightedRandom.getRandomItem()` |
| `Animation.java` | `@SideOnly`, `EntityNPCInterface`, `EventHooks` |
| `AnimationData.java` | `EntityPlayer`, `AxisAlignedBB`, dimension access |
| `DataTransform.java` | `EntityNPCInterface`, `EntityCustomNpc` throughout |
| `MarkData.java` | `IExtendedEntityProperties`, `EntityLivingBase` |
| `TelegraphInstance.java` | `Entity`, `World` |

### Tier 4: Blocked by Script System (DO NOT TOUCH YET)
| File | Blocker |
|---|---|
| `All I*Script*.java` | Script handler/unit interfaces |
| `ScriptHandler.java`, `SingleScriptHandler.java`, etc. | Script execution |
| `AbilityScript.java`, `ChainedAbilityScript.java` | Script containers |
| `EffectScript.java`, `LinkedItemScript.java` | Script containers |
| `DataScript.java` | FML, World, entities + scripting |
| `Action.java` + action framework | `ScriptContainer` dependency |

### Tier 5: Blocked by Heavy MC Coupling (Long-term)
| File | Blocker |
|---|---|
| `RecipeCarpentry.java` | Extends `ShapedRecipes` |
| `RecipeController.java` | `CraftingManager`, `InventoryCrafting` |
| `Ability.java` (kamkeel) | 30+ MC imports (Block, Entity, DamageSource, Vec3, World) |
| `All ability type/ classes` | Depend on `Ability.java` |
| `ChunkController.java` | `ForgeChunkManager` |
| `ProfileController.java` | EntityPlayer lifecycle, Mojang API, events |
| `SyncController.java` | ByteBuf, packets, Minecraft server |

---

## Recommended Next Steps (in order)

### 1. mc1710 Shadow Files for Migrated Controllers
The mc1710 versions (`src/main/java/`) of FactionController, MagicController, TagController need to be updated to:
- Add `implements IFactionHandler` / `IMagicHandler` / `ITagHandler`
- Re-add SyncController calls in save/delete methods
- Re-add EntityPlayer/IPlayer methods that were stubbed
- Override `createDefaults()` in MagicController to set ItemStack items

### 2. IUser Implementation (Phase 0-1 of plan)
Creates the biggest unblock — enables migration of Availability, FactionOptions, Dialog system, Quest system, CustomEffect, all Player*Data classes.

### 3. IStack / Inventory Abstraction
Unblocks Bank, AuctionClaim/Listing, LinkedItem — the ItemStack-dependent data classes.

### 4. Dialog + Quest Cluster Migration
Once IUser exists: migrate Dialog → DialogOption → DialogCategory → DialogController as a cluster. Then Quest → QuestCategory → QuestData → QuestController.

### 5. NBTJsonUtil to Core
Many controllers (Dialog, Quest, CustomEffect) use NBTJsonUtil for JSON file storage. Creating a core version would unblock those controllers.
