# CORE MIGRATION PLAN — Move-Abstract-Implement Cycle

**Created:** 2026-03-15
**Purpose:** Exhaustive, actionable plan for migrating ~90% of mc1710 (m7) game logic into `core/`, wired to `platform-api/` (PA) abstractions, with version-specific implementations remaining in each version submodule.

---

## ⚠️ CRITICAL PERFORMANCE NOTE FOR AGENTS

**HEAVILY PRIORITIZE `ast_grep_replace` AND `edit(replaceAll=true)` TOOLS — they are 100-1000x faster than individual read/edit cycles.**

For bulk changes across multiple files:
- ❌ **NEVER** do individual `read()` + `edit()` per file
- ✅ **ALWAYS** use `ast_grep_replace()` for pattern-based bulk fixes across all files simultaneously
- ✅ **ALWAYS** use `edit(replaceAll=true)` for string replacements within a file (single call fixes ALL occurrences)

**OPTIMIZED EXECUTION STRATEGY FOR EACH WAVE:**

1. **Bulk Copy Phase**: Use SINGLE `cp` command to copy ALL files for the wave in one operation
   ```bash
   cp file1 dest1 && cp file2 dest2 && ... && cp fileN destN
   ```

2. **Bulk Fix Phase**: Fire ast_grep_replace patterns in rapid succession to fix ALL files at once
   - Each pattern = one concurrent batch fix affecting all copied files
   - Fire 20+ patterns without waiting between them

3. **Batch Create Phase**: If new PA interfaces are needed, create them in BATCH
   - Don't create one interface per file
   - Create all new interfaces ONCE in platform-api/
   - Then bulk-fix all files to import/use the new interfaces

**Example FAST approach:**
```bash
# Instead of: read DialogController, edit, read Quest, edit, read Bank, edit (slow tool overhead)
# Do this: 
# 1. cp DialogController Quest QuestCategory ... (single command, all files copied)
# 2. ast_grep_replace pattern="NBTTagCompound" rewrite="INbt" (affects all files at once)
# 3. ast_grep_replace pattern="EntityPlayer" rewrite="IPlayer" (affects all files at once)
# 4. Done in seconds vs. hours
```

Each ast_grep_replace pattern = one concurrent batch fix across potentially hundreds of files. This is the ONLY way to achieve acceptable speed for Wave 1-5 migrations.

---

## How To Read This Document

Each **Part** is an atomic migration unit that can be delegated to a single agent. Parts are grouped into **Waves** by dependency order. Within each Part:

- **MOVE** = files moving from m7 to `core/` (retain same package)
- **ABSTRACT** = new PA interfaces needed (if not under `noppes.npcs.*`, put it in `common.minecraft.*` if it already doesn't exist). MUST NOT CREATE DUPLICATE INTERFACES, SO ALWAYS CHECK IF IT EXISTS
- **SHADOW** = m7 files that need mc1710-specific shadows after core move
- **VERIFY** = build commands to confirm success

### PA Naming Rule
> When moving abstractions to PA: if the interface already exists under `noppes.npcs.api.*`, use it. If a NEW abstraction is needed and it doesn't belong under `noppes.npcs.*` or `common.minecraft.*`, create it under `common.minecraft.*`.

### The Cycle (Every Part)
```
1. CREATE any new PA interfaces needed
2. MOVE files to core/, replacing MC types with PA interfaces
3. CREATE m7 shadow files that add MC-specific behavior back
4. VERIFY: ./gradlew :core:compileJava && ./gradlew :mc1710:build
```

### MC Type → PA Replacement Quick Reference

| MC Type | PA Replacement | Status |
|---------|---------------|--------|
| `NBTTagCompound` | `INbt` (`noppes.npcs.api`) | EXISTS |
| `NBTTagList` | `INbtList` (`noppes.npcs.api`) | EXISTS |
| `CompressedStreamTools` | `PlatformService.readCompressedNBT/writeCompressedNBT` | EXISTS |
| `EntityPlayer` / `EntityPlayerMP` | `IPlayer` (`noppes.npcs.api.entity`) | EXISTS |
| `Entity` | `IEntity` (`noppes.npcs.api.entity`) | EXISTS |
| `EntityLivingBase` | `IEntityLivingBase` (`noppes.npcs.api.entity`) | EXISTS |
| `ItemStack` | `IItemStack` (`noppes.npcs.api.item`) | EXISTS |
| `World` | `IWorld` (`noppes.npcs.api`) | EXISTS |
| `DamageSource` | `IDamageSource` (`noppes.npcs.api`) | EXISTS |
| `IInventory` | `IContainer` (`noppes.npcs.api`) | EXISTS |
| `MathHelper` | `ValueUtil` (`noppes.npcs.util`, in core) | EXISTS |
| `ChatComponentText` | `IMessageService` | **NEW — Wave 2** |
| `MinecraftServer` | `IServerService` | **NEW — Wave 2** |
| `StatCollector` | `ITranslationService` | **NEW — Wave 3** |
| `ForgeChunkManager` | N/A — STAYS in m7 | N/A |
| `EntityNPCInterface` | `ICustomNpc` (`noppes.npcs.api.entity`) | EXISTS |
| `SharedMonsterAttributes` | N/A — STAYS in m7 (entity-level) | N/A |
| `SideOnly/Side` | `@ClientOnly/@ServerOnly` (`kamkeel.npcs.platform.annotation`) | EXISTS |
| `IExtendedEntityProperties` | N/A — STAYS in m7 (Forge-specific) | N/A |
| `Forge Configuration` | `IConfigProvider` (`kamkeel.npcs.platform.config`) | EXISTS |
| `CustomNpcs.getWorldSaveDirectory()` | `PlatformService.getWorldSaveDirectory()` | EXISTS |

---

## WAVE 0 — Foundation PA Interfaces (Prerequisite for everything)

### Part 0.1: NBT JSON Utilities
> Move NBTJsonUtil to core with PA types.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `NBTJsonUtil.java` | `noppes.npcs.util` | `NBTTagCompound` → `INbt` |

**ABSTRACT (new in PA):**
- Add `toJson(INbt)` / `fromJson(String)` methods to `noppes.npcs.platform.nbt.NBTIO` (already exists)

**SHADOW in m7:**
- m7 `NBTJsonUtil` wraps core version, converts between `NBTTagCompound` and `INbt`

**VERIFY:** `./gradlew :core:compileJava`

---

### Part 0.2: Logging & String Utilities
> Move LogWriter and NoppesStringUtils to core.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `LogWriter.java` | `noppes.npcs` | Uses `PlatformService.logInfo/logWarn/logError` (already exists) |
| `NoppesStringUtils.java` | `noppes.npcs` | Pure logic — likely zero MC imports for string cleaning |

**ABSTRACT:** None needed — `PlatformService` logging already exists.

**SHADOW in m7:** m7 retains thin delegates if needed.

**VERIFY:** `./gradlew :core:compileJava`

---

### Part 0.3: Version Compatibility
> Move ICompatibilty and VersionCompatibility to core.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `ICompatibilty.java` | `noppes.npcs` | None (interface) |
| `VersionCompatibility.java` | `noppes.npcs` | `NBTTagCompound` → `INbt`. Static methods that upgrade old NBT formats. |

**ABSTRACT:** None.

**SHADOW in m7:** m7 shadow adds any MC-specific version compat logic.

**VERIFY:** `./gradlew :core:compileJava`

---

### Part 0.4: Action Framework (MC-FREE — Direct Move)
> 9 files, 0 MC imports. Pure game logic.

**MOVE to `core/`:**
| File | Package | MC Imports |
|------|---------|------------|
| `Action.java` | `noppes.npcs.controllers.data.action` | 0 |
| `ActionChain.java` | `noppes.npcs.controllers.data.action.chain` | 0 |
| `ConditionalAction.java` | `noppes.npcs.controllers.data.action.action` | 0 |
| `ActionThread.java` | `noppes.npcs.controllers.data.action` | 0 |
| `ActionQueue.java` | `noppes.npcs.controllers.data.action` | 0 |
| `ActionManager.java` | `noppes.npcs.controllers.data.action` | 0 |
| `ActionLogger.java` | `noppes.npcs.controllers.data.action` | 0 |
| `ActionListener.java` | `noppes.npcs.controllers.data.action` | 0 |
| `ActionList.java` | `noppes.npcs.controllers.data.action` | 0 |

**ABSTRACT:** None.
**SHADOW:** None.
**VERIFY:** `./gradlew :core:compileJava`

---

### Part 0.5: Zero-MC Controllers (Direct Move)
> Controllers with 0 MC imports.

**MOVE to `core/`:**
| File | Package | MC Imports |
|------|---------|------------|
| `CategoryManager.java` | `noppes.npcs.controllers` | 0 |
| `HookDefinition.java` | `noppes.npcs.controllers` | 0 |
| `ScriptHookController.java` | `noppes.npcs.controllers` | 0 |
| `APIRegistry.java` | `noppes.npcs.controllers` | 0 (already in core — verify) |

**ABSTRACT:** None.
**SHADOW:** None.
**VERIFY:** `./gradlew :core:compileJava`

---

## WAVE 1 — NBT-Only Controllers (Replace CompressedStreamTools + NBTTag*)

These controllers use ONLY NBT types and `CustomNpcs.getWorldSaveDirectory()` — both already abstracted.

### Part 1.1: DialogController + Dialog Data Classes
> Core dialog system. 4 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `DialogController.java` | `noppes.npcs.controllers` | `CompressedStreamTools` → `PlatformService`, `NBTTagCompound/List` → `INbt/INbtList` |
| `Dialog.java` | `noppes.npcs.controllers.data` | `NBTTagCompound/List` → `INbt/INbtList`, `EntityPlayer` → `IPlayer` (only in `hasDialogs()` and `copy()`) |
| `DialogCategory.java` | `noppes.npcs.controllers.data` | `NBTTagCompound/List` → `INbt/INbtList` |
| `DialogOption.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt`, `EntityPlayer` → `IPlayer` (in `isAvailable()`) |

**Dependencies:** Part 0.1 (NBTJsonUtil), Part 0.2 (LogWriter, NoppesStringUtils), Availability (Part 1.5)

**ABSTRACT:** None new — all types already in PA.

**SHADOW in m7:**
- `DialogController.java` — shadow adds `SyncController` calls (networking, stays m7-side)
- `Dialog.java` — shadow adds `NBTWrapper` bridging for `DialogImage.writeToNBT`

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.2: QuestController + Quest Data Classes
> Core quest system. 4 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `QuestController.java` | `noppes.npcs.controllers` | `CompressedStreamTools` → `PlatformService`, `NBTTagCompound/List` → `INbt/INbtList` |
| `Quest.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt`, `EntityPlayer` → `IPlayer` |
| `QuestCategory.java` | `noppes.npcs.controllers.data` | `NBTTagCompound/List` → `INbt/INbtList` |
| `QuestData.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |

**Dependencies:** Part 0.1, Part 0.2, Availability (Part 1.5)

**ABSTRACT:** None new.

**SHADOW in m7:**
- `QuestController.java` — adds `SyncController` calls
- `Quest.java` — adds `EntityPlayer`-specific methods

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.3: BankController + Bank Data
> Banking system. 3 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `BankController.java` | `noppes.npcs.controllers` | `CompressedStreamTools` → `PlatformService`, `NBTTagCompound/List` → `INbt/INbtList` |
| `Bank.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `BankData.java` | `noppes.npcs.controllers.data` | `NBTTagCompound/List` → `INbt/INbtList`, `EntityPlayer` → `IPlayer`, `ItemStack` → `IItemStack`, `Container` → remove (m7 shadow) |

**Dependencies:** Part 0.1, Part 0.2

**ABSTRACT:** None new.

**SHADOW in m7:**
- `BankData.java` — shadow adds `Container` management, `EntityPlayerMP` packet sending

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.4: AnimationController + Animation Data
> Animation system. 4 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `AnimationController.java` | `noppes.npcs.controllers` | `CompressedStreamTools` → `PlatformService`, `NBTTagCompound/List` → `INbt/INbtList` |
| `Animation.java` | `noppes.npcs.controllers.data` | `NBTTagCompound/List` → `INbt/INbtList` |
| `BuiltInAnimation.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `AnimationData.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt`, `Entity/EntityLivingBase/EntityPlayer/EntityPlayerMP` → `IEntity/IPlayer`, `AxisAlignedBB` → remove (m7 shadow) |

**Dependencies:** Part 0.1, Part 0.2

**ABSTRACT:** None new.

**SHADOW in m7:**
- `AnimationData.java` — shadow adds MC entity interaction (position, AABB, player sync)

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.5: Availability
> Cross-cutting availability checker. Used by Dialog, Quest, many others.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `Availability.java` | `noppes.npcs.controllers.data` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt`, `MathHelper` → `ValueUtil`, `StatCollector` → remove client method (m7 shadow), `SideOnly` → `@ClientOnly` |

**Key insight:** `isAvailable(EntityPlayer)` checks world time, dialog/quest/faction progress, and XP level. Core version takes `IPlayer`. The `isAvailableText()` method is `@SideOnly(Side.CLIENT)` and uses `StatCollector` — this stays in m7 shadow only.

**Dependencies:** Part 0.3 (ICompatibilty), FactionController (already in core), PlayerQuestController (Part 2.2), QuestController (Part 1.2)

**ABSTRACT:**
- Add `getWorldTime()` to `IPlayer` or `IWorld` in PA (check if exists)
- Add `getExperienceLevel()` to `IPlayer` in PA (check if exists)

**SHADOW in m7:**
- Adds `isAvailableText()` client-side method with `StatCollector`
- Adds `isAvailable(EntityPlayer)` bridge that wraps to `isAvailable(IPlayer)`

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.6: LinkedItemController + LinkedItem Data
> Linked item template system. 2 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `LinkedItemController.java` | `noppes.npcs.controllers` | `CompressedStreamTools` → `PlatformService`, `NBTTagCompound/List` → `INbt/INbtList` |
| `LinkedItem.java` | `noppes.npcs.controllers.data` | `ItemStack` → `IItemStack`, `NBTTagCompound` → `INbt` |

**Dependencies:** Part 0.1, Part 0.2

**ABSTRACT:** None new.

**SHADOW in m7:**
- `LinkedItem.java` — shadow adds MC `ItemStack` field handling

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.7: SpawnController + SpawnData
> Natural NPC spawning rules. 2 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `SpawnController.java` | `noppes.npcs.controllers` | `CompressedStreamTools` → `PlatformService`, `NBTTagCompound/List` → `INbt/INbtList`, `WeightedRandom` → inline or utility |
| `SpawnData.java` | `noppes.npcs.controllers.data` | `Entity/EntityList` → remove (m7 shadow), `NBTTagCompound` → `INbt`, `WeightedRandom.Item` → custom base class, `DimensionManager` → `PlatformService` |

**ABSTRACT:**
- Add `getDimensionIds()` to `PlatformService` or create `common.minecraft.IServerService`

**SHADOW in m7:**
- `SpawnData.java` — shadow adds `Entity` creation and `EntityList` lookups

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.8: CustomEffectController + CustomEffect
> Custom status effects. 2 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `CustomEffectController.java` | `noppes.npcs.controllers` | `EntityPlayer` → `IPlayer`, `CompressedStreamTools` → `PlatformService`, `NBTTagCompound/List` → `INbt/INbtList` |
| `CustomEffect.java` | `noppes.npcs.controllers.data` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |

**Dependencies:** Part 0.1, Part 0.2

**ABSTRACT:** None new.

**SHADOW in m7:**
- `CustomEffectController.java` — shadow adds `EntityPlayer` UUID access, m7-specific effect application

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.9: AuctionConfigSync + ScriptConfigSync + ProfileConfigSync
> Config sync utilities. 3 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `AuctionConfigSync.java` | `noppes.npcs.controllers` | `NBTTagCompound` → `INbt` |
| `ScriptConfigSync.java` | `noppes.npcs.controllers` | `NBTTagCompound` → `INbt` |
| `ProfileConfigSync.java` | `noppes.npcs.controllers` | `EntityPlayer` → `IPlayer`, `NBTTagCompound/List` → `INbt/INbtList` |

**ABSTRACT:** None new.
**SHADOW:** None needed (or thin wrappers).
**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 1.10: FactionOptions + DataTransform
> Small data classes. 2 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `FactionOptions.java` | `noppes.npcs.controllers.data` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt`, `ChatComponentTranslation` → remove (m7 shadow) |
| `DataTransform.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |

**ABSTRACT:** None new.
**SHADOW in m7:** `FactionOptions.java` — shadow adds chat message sending.
**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

## WAVE 2 — Player-Dependent Systems (Need IPlayer expansion + IMessageService)

### Part 2.0: PA Interface Expansion
> Before Wave 2 work begins, expand PA.

**NEW PA Interfaces:**
| Interface | Package | Methods |
|-----------|---------|---------|
| `IMessageService` | `common.minecraft` | `sendMessage(IPlayer, String)`, `sendTranslatedMessage(IPlayer, String, Object...)`, `broadcastMessage(String)` |
| `IServerService` | `common.minecraft` | `getOnlinePlayers()`, `getPlayerByName(String)`, `getPlayerByUUID(UUID)`, `isServerRunning()`, `getDimensionIds()` |

**Expand existing `IPlayer`** (in `noppes.npcs.api.entity`):
- `getWorldTime()` (if not present)
- `getExperienceLevel()` (if not present)
- `getDialogsRead()` → `Set<Integer>` (for Availability)
- `getPersistentID()` → `UUID`

**Expand `PlatformService`:**
- `IMessageService messages()`
- `IServerService server()`

**VERIFY:** `./gradlew :core:compileJava`

---

### Part 2.1: Quest Type Implementations
> The actual quest logic. 6 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `QuestInterface.java` | `noppes.npcs.quests` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |
| `QuestDialog.java` | `noppes.npcs.quests` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |
| `QuestKill.java` | `noppes.npcs.quests` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |
| `QuestLocation.java` | `noppes.npcs.quests` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt`, `StatCollector` → `ITranslationService` or remove |
| `QuestManual.java` | `noppes.npcs.quests` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |
| `QuestItem.java` | `noppes.npcs.quests` | `EntityPlayer` → `IPlayer`, `ItemStack` → `IItemStack`, `NBTTagCompound` → `INbt` |

**Key pattern:** `QuestInterface` is abstract with `isCompleted(PlayerData)`, `handleComplete(EntityPlayer)`, `getQuestLogStatus(EntityPlayer)`, `getObjectives(EntityPlayer)`. Core version changes `EntityPlayer` → `IPlayer` and `PlayerData` reference stays (PlayerData will also move to core in Wave 3).

**Dependencies:** Part 1.2 (QuestController), Part 2.0 (PA expansion)

**ABSTRACT:**
- `ITranslationService` in `common.minecraft` — `translate(String key)`, `translateFormatted(String key, Object... args)` — needed for `QuestLocation`

**SHADOW in m7:**
- Each quest type may need shadow for `StatCollector` calls and inventory operations on raw `EntityPlayer`

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 2.2: PlayerQuestController
> Quest progress tracking. 1 file.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `PlayerQuestController.java` | `noppes.npcs.controllers` | `EntityPlayer` → `IPlayer`, `EntityPlayerMP` → `IPlayer`, `ChatComponentTranslation` → `IMessageService` |

**Dependencies:** Part 2.0 (IMessageService), Part 1.2 (QuestController)

**ABSTRACT:** None new (IMessageService from Part 2.0).

**SHADOW in m7:** Shadow adds MC-specific chat formatting.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 2.3: PartyController + Party
> Player grouping. 2 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `PartyController.java` | `noppes.npcs.controllers` | `EntityPlayer/EntityPlayerMP` → `IPlayer`, `NBTTagCompound/List/String` → `INbt/INbtList`, `ChatComponentTranslation` → `IMessageService` |
| `Party.java` | `noppes.npcs.controllers.data` | `EntityPlayer/EntityPlayerMP` → `IPlayer`, `NBTTagCompound/List` → `INbt/INbtList`, `ChatComponentText` → `IMessageService` |

**Dependencies:** Part 2.0 (IMessageService, IServerService)

**ABSTRACT:** None new.

**SHADOW in m7:** Shadows add MC player lookups and chat message construction.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 2.4: AuctionController + Auction Data
> Auction/economy system. 4 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `AuctionController.java` | `noppes.npcs.controllers` | `EntityPlayer/EntityPlayerMP` → `IPlayer`, `ItemStack` → `IItemStack`, `CompressedStreamTools` → `PlatformService`, `NBTTagCompound/List` → `INbt/INbtList`, `ChatComponentText/EnumChatFormatting` → `IMessageService` |
| `AuctionListing.java` | `noppes.npcs.controllers.data` | `ItemStack` → `IItemStack`, `NBTTagCompound` → `INbt` |
| `AuctionClaim.java` | `noppes.npcs.controllers.data` | `ItemStack` → `IItemStack`, `NBTTagCompound` → `INbt` |
| `AuctionBlacklist.java` | `noppes.npcs.controllers.data` | `EntityPlayer` → `IPlayer`, `ItemStack` → `IItemStack`, `NBTTagCompound` → `INbt` |

**Dependencies:** Part 2.0 (IMessageService), Part 0.1 (NBTJsonUtil)

**ABSTRACT:** None new.

**SHADOW in m7:** `AuctionController` shadow adds SyncController calls, packet sending.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 2.5: MarketRegistry
> Currency and trader integration. 1 file.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `MarketRegistry.java` | `noppes.npcs.controllers` | `EntityPlayerMP` → `IPlayer`, `NBTTagCompound` → `INbt` |

**Dependencies:** Part 2.0

**ABSTRACT:** None new.
**SHADOW in m7:** Adds packet sending.
**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 2.6: AttributeController
> Player attribute definitions. 1 file.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `AttributeController.java` | `kamkeel.npcs.controllers` | `EntityPlayer` → `IPlayer` |

**Dependencies:** Part 2.0

**ABSTRACT:** None new.
**SHADOW:** None needed.
**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 2.7: ProfileController
> Multi-character profiles. 1 file.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `ProfileController.java` | `kamkeel.npcs.controllers` | `EntityPlayer/EntityPlayerMP` → `IPlayer`, `CompressedStreamTools` → `PlatformService`, `NBTTagCompound` → `INbt` |

**Dependencies:** Part 2.0 (IServerService for player lookups)

**ABSTRACT:** None new.

**SHADOW in m7:** Adds `EntityPlayerMP`-specific packet operations.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 2.8: ServerCloneController + ServerTagMapController
> Clone tab and tag map management. 2 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `ServerTagMapController.java` | `noppes.npcs.controllers` | `CompressedStreamTools` → `PlatformService`, `NBTTagCompound` → `INbt` |

**SPLITTABLE (clone controller uses Entity/EntityList):**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `ServerCloneController.java` | `noppes.npcs.controllers` | Core: file/NBT management moves. Shadow: `Entity`, `EntityList`, `ICommandSender`, `ChatComponentText` stay. |

**Dependencies:** Part 0.1 (NBTJsonUtil), Part 2.0 (IMessageService)

**ABSTRACT:** None new.

**SHADOW in m7:** `ServerCloneController` shadow adds entity spawning from NBT.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

## WAVE 3 — Player Data System (Central, Many Dependencies)

### Part 3.0: Player Sub-Data Classes (NBT-only)
> Player data components that only use NBT. 6 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `PlayerBankData.java` | `noppes.npcs.controllers.data` | `NBTTagCompound/List` → `INbt/INbtList` |
| `PlayerItemGiverData.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `PlayerMailData.java` | `noppes.npcs.controllers.data` | `NBTTagCompound/List` → `INbt/INbtList` |
| `PlayerTradeData.java` | `noppes.npcs.controllers.data` | `NBTTagCompound/List` → `INbt/INbtList` |

**Dependencies:** Part 1.3 (Bank)

**ABSTRACT:** None new.
**SHADOW:** Thin m7 wrappers if any MC bridge needed.
**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 3.1: PlayerFactionData + PlayerQuestData
> Player progress tracking. 2 files with EntityPlayer usage.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `PlayerFactionData.java` | `noppes.npcs.controllers.data` | `EntityPlayer/EntityPlayerMP` → `IPlayer`, `NBTTagCompound/List` → `INbt/INbtList` |
| `PlayerQuestData.java` | `noppes.npcs.controllers.data` | `EntityPlayer/EntityPlayerMP` → `IPlayer`, `NBTTagCompound/List` → `INbt/INbtList` |

**Dependencies:** Part 2.0, Part 2.2

**ABSTRACT:** None new.

**SHADOW in m7:** Shadows add `EntityPlayerMP` packet sending for sync.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 3.2: PlayerMail
> Mail system. 1 file — heavier MC coupling.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `PlayerMail.java` | `noppes.npcs.controllers.data` | `EntityPlayer` → `IPlayer`, `IInventory` → `IContainer`, `ItemStack` → `IItemStack`, `NBTTagCompound/List/String` → `INbt/INbtList` |

**Dependencies:** Part 2.0

**ABSTRACT:** None new (IContainer exists).

**SHADOW in m7:** Shadow adds MC inventory implementation.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 3.3: PlayerAbilityData + PlayerAbilityHotbarData
> Ability system player data. 2 files — medium MC coupling.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `PlayerAbilityHotbarData.java` | `noppes.npcs.controllers.data` | `EntityPlayer/EntityPlayerMP` → `IPlayer`, `NBTTagCompound` → `INbt` |

**SPLITTABLE:**
| File | Package | Notes |
|------|---------|-------|
| `PlayerAbilityData.java` | `noppes.npcs.controllers.data` | 7 MC imports including `EntityLivingBase`, `DamageSource`. Core version holds data/state. Shadow adds combat methods. |

**Dependencies:** Part 2.0, AbilityController (already in core)

**ABSTRACT:** None new.

**SHADOW in m7:** `PlayerAbilityData` shadow adds damage processing, entity targeting.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 3.4: PlayerDataController
> Central player data management. 1 file — heavy MC coupling.

**SPLITTABLE:**
| File | Package | MC Imports |
|------|---------|------------|
| `PlayerDataController.java` | `noppes.npcs.controllers` | 9 MC imports: `ICommandSender`, `PlayerSelector`, `EntityPlayer`, `EntityPlayerMP`, `CompressedStreamTools`, `NBTTagCompound`, `NBTTagList`, `MinecraftServer`, `ChatComponentText` |

**Core version:** Data persistence logic (load/save/cache), player map management.
**Shadow:** MC-specific player lookup (`PlayerSelector`), `MinecraftServer` access, command sender operations.

**Dependencies:** Part 2.0, Part 3.0-3.3

**ABSTRACT:** Uses `IServerService` from Part 2.0.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 3.5: PlayerData (THE BIG ONE)
> Central player data aggregator. 1 file — heaviest MC coupling of any data class.

**SPLITTABLE — Core version holds:**
- All sub-data instances (dialogData, bankData, questData, etc.)
- `setNBT()` / `getNBT()` logic using `INbt`
- `playername`, `uuid` fields
- `save()` logic (using `PlatformService`)
- Party invite management (pure game logic)

**Shadow adds:**
- `implements IExtendedEntityProperties` (Forge-specific)
- `EntityPlayer player` field → raw MC player reference
- `EntityNPCInterface editingNpc`, `activeCompanion` fields
- `setCompanion()`, `updateCompanion()` methods (entity-level)
- `get(EntityPlayer)` static factory method
- `onLogin()` / `onLogout()` MC-specific lifecycle
- `DBCAddon` integration

**Dependencies:** All of Wave 3, Part 2.0

**ABSTRACT:** None new — uses `IPlayer`, `INbt`, `PlatformService`.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

## WAVE 4 — Config System

### Part 4.1: Config Data Classes (Values Only)
> Extract pure config values to core. 9+ files.

**Pattern:** Each config file has two parts:
1. **Values** (static fields like `NpcNavRange`, `EnableUpdateChecker`) — move to `core/`
2. **Forge loading** (uses `Configuration`, `Property`) — stays in m7

**MOVE to `core/` (new files):**
Create `CoreConfig*.java` classes in `noppes.npcs.config` that hold ONLY the static value fields.

| New Core File | Extracts Values From |
|--------------|---------------------|
| `CoreConfigMain.java` | `ConfigMain.java` |
| `CoreConfigMarket.java` | `ConfigMarket.java` |
| `CoreConfigEnergy.java` | `ConfigEnergy.java` |
| `CoreConfigItem.java` | `ConfigItem.java` |
| `CoreConfigDebug.java` | `ConfigDebug.java` |
| `CoreConfigExperimental.java` | `ConfigExperimental.java` |
| `CoreConfigScript.java` | `ConfigScript.java` |

**m7 versions:** Keep Forge `Configuration` loading, write values into the core config classes.

**Already exists:** `CoreConfig.java` in core — extend this pattern.

**MOVE to `core/`:**
| File | Package | MC Imports |
|------|---------|------------|
| `LoadConfiguration.java` | `noppes.npcs.config` | 0 — pure coordination |
| `legacy/ConfigProp.java` | `noppes.npcs.config.legacy` | 0 — pure annotation |
| `legacy/LegacyLoader.java` | `noppes.npcs.config.legacy` | 0 — pure file parsing |

**STAYS in m7:**
- `GlyphCache.java` — OpenGL rendering
- `StringCache.java` — MC Tessellator rendering
- `legacy/LegacyConfig.java` — uses `Minecraft`, `MinecraftServer` for paths
- `ConfigClient.java` — client-specific, uses `Minecraft`
- `ConfigMixin.java` — mixin-specific

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

## WAVE 5 — Roles & Jobs System

### Part 5.0: Role/Job Base Interfaces
> The abstract base classes. 2 files.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `RoleInterface.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt`. Constructor: `EntityNPCInterface` → `ICustomNpc` |
| `JobInterface.java` | `noppes.npcs.roles` | `ItemStack` → `IItemStack`, `NBTTagCompound` → `INbt`. Constructor: `EntityNPCInterface` → `ICustomNpc` |

**Key decision:** The `npc` field changes from `EntityNPCInterface` to `ICustomNpc`. This is the single most impactful change — every role/job subclass uses `this.npc`.

**Dependencies:** Part 2.0

**ABSTRACT:** `ICustomNpc` already exists in PA. May need method additions for:
- `getWorldTime()`, `getPosition()`, `isAlive()`, `isDead()`

**SHADOW in m7:** Both get shadows that cast `ICustomNpc` back to `EntityNPCInterface` for MC-specific method access.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 5.1: Simple Roles (Low MC Coupling)
> Roles with 2-3 MC imports, mostly EntityPlayer.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `RoleBank.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |
| `RoleAuctioneer.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |
| `RoleMount.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |
| `RoleTransporter.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt`, `ChatComponentTranslation` → `IMessageService` |
| `RolePostman.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt`, `ChatComponentTranslation` → `IMessageService` |

**Dependencies:** Part 5.0, Part 2.0

**ABSTRACT:** None new.

**SHADOW in m7:** Shadows add `interact()` MC-specific GUI opening.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 5.2: Medium Roles
> Roles with more MC coupling.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `RoleFollower.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt`, `ChatComponentTranslation/StatCollector` → `IMessageService`/`ITranslationService` |
| `RoleInnkeeper.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTBase/NBTTagCompound/List` → `INbt/INbtList`, `ChatComponentTranslation` → `IMessageService` |
| `RoleTrader.java` | `noppes.npcs.roles` | `EntityPlayer/EntityPlayerMP` → `IPlayer`, `ItemStack` → `IItemStack`, `NBTTagCompound/List` → `INbt/INbtList` |

**Dependencies:** Part 5.0, Part 2.0, Part 2.5 (MarketRegistry)

**ABSTRACT:** None new.

**SHADOW in m7:** Shadows add trader stock ItemStack operations, GUI packet sending.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 5.3: Simple Jobs
> Jobs with low MC coupling.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `JobFollower.java` | `noppes.npcs.roles` | `NBTTagCompound` → `INbt` (1 MC import — MOVABLE) |
| `JobConversation.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `NBTTagCompound/List` → `INbt/INbtList` |
| `JobHealer.java` | `noppes.npcs.roles` | `EntityLivingBase` → `IEntityLivingBase`, `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |
| `JobItemGiver.java` | `noppes.npcs.roles` | `EntityPlayer` → `IPlayer`, `Item/ItemStack` → `IItemStack`, `NBTTagCompound/List` → `INbt/INbtList` |

**Dependencies:** Part 5.0, Part 2.0

**ABSTRACT:** None new.

**SHADOW in m7:** Shadows add MC item giving, entity healing.

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 5.4: Complex Jobs
> Jobs with heavier MC coupling.

**SPLITTABLE:**
| File | Package | Notes |
|------|---------|-------|
| `JobGuard.java` | `noppes.npcs.roles` | Core: targeting config, NBT. Shadow: `EntityList`, `IMob`, `EntityDragon`, etc. |
| `JobBard.java` | `noppes.npcs.roles` | Core: music config, NBT. Shadow: `Minecraft` client music, `ItemStack` |

**STAYS in m7:**
| File | Package | Reason |
|------|---------|--------|
| `JobSpawner.java` | `noppes.npcs.roles` | 6 MC entity imports, spawns entities directly |
| `JobChunkLoader.java` | `noppes.npcs.roles` | `ForgeChunkManager` — deeply Forge-specific |

**Dependencies:** Part 5.0, Part 2.0

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 5.5: Companion System
> RoleCompanion + companion jobs. 6 files.

**STAYS in m7 (too MC-coupled):**
| File | Package | MC Imports | Reason |
|------|---------|------------|--------|
| `RoleCompanion.java` | `noppes.npcs.roles` | 18 | Items, armor, attributes, damage, food, Vec3 |
| `CompanionFoodStats.java` | `noppes.npcs.roles.companion` | 5 | `ItemFood`, `DamageSource`, `EnumDifficulty` |
| `CompanionGuard.java` | `noppes.npcs.roles.companion` | 5 | `Entity`, `IMob`, `EntityCreeper` |

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `CompanionJobInterface.java` | `noppes.npcs.roles.companion` | `NBTTagCompound` → `INbt` |
| `CompanionFarmer.java` | `noppes.npcs.roles.companion` | `NBTTagCompound` → `INbt` (MOVABLE — 1 MC import) |
| `CompanionTrader.java` | `noppes.npcs.roles.companion` | `EntityPlayer` → `IPlayer`, `NBTTagCompound` → `INbt` |

**Dependencies:** Part 5.0

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

## WAVE 6 — Script System (Partial Migration)

### Part 6.1: ScriptContainer
> Individual script execution context.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `ScriptContainer.java` | `noppes.npcs.controllers` | `NBTTagCompound` → `INbt` (1 MC import — MOVABLE) |

**Dependencies:** None.
**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

### Part 6.2: Script Data Classes (NBT-only)
> Script handler data that only uses NBT.

**MOVE to `core/`:**
| File | Package | MC Imports to Replace |
|------|---------|----------------------|
| `AbilityScript.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `EffectScript.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `LinkedItemScript.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `RecipeScript.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `ChainedAbilityScript.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `IScriptHandlerPacket.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |
| `IScriptUnit.java` | `noppes.npcs.controllers.data` | `NBTTagCompound` → `INbt` |

**STAYS in m7 (too MC-coupled or Forge-specific):**
| File | Reason |
|------|--------|
| `ScriptHandler.java` | Extends script engine internals |
| `SingleScriptHandler.java` | Forge `Constants` usage |
| `MultiScriptHandler.java` | `NBTTagCompound` deep integration |
| `DataScript.java` | `Entity`, `World`, `WorldServer` |
| `PlayerDataScript.java` | `EntityPlayer`, `BlockPos` |
| `ForgeDataScript.java` | 7 Forge event imports |
| `GlobalNPCDataScript.java` | `NBTTagCompound`, `BlockPos` |
| `JaninoScriptHandler.java` | `NBTTagCompound` |
| `IScriptHandler.java` | Deep scripting contract |
| `IScriptBlockHandler.java` | Block-level scripting |
| `INpcScriptHandler.java` | NPC-level scripting |
| `ScriptController.java` | 7 MC imports, `MinecraftServer` |
| `ScriptEntityData.java` | `IExtendedEntityProperties` |

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

## WAVE 7 — SyncController (Networking Bridge)

### Part 7.1: SyncController Core Logic
> The sync system is heavily MC-coupled but its DATA preparation logic can be extracted.

**SPLITTABLE:**
| File | Package | Notes |
|------|---------|-------|
| `SyncController.java` | `kamkeel.npcs.controllers` | Core: sync revision tracking, data preparation methods (`updateDialogCat`, `updateQuestCat`, etc.). Shadow: `Minecraft`, `EntityPlayerMP`, `MinecraftServer`, packet sending. |

**ABSTRACT:**
- Add `ISyncService` to `common.minecraft` — `syncUpdate(EnumSyncType, int, INbt)`, `syncRemove(EnumSyncType, int)`

**Dependencies:** All of Waves 1-3 (controllers that call SyncController)

**VERIFY:** `./gradlew :core:compileJava && ./gradlew :mc1710:build`

---

## WAVE 8 — NPC Data Classes (Entity-Coupled, SPLIT ONLY)

> These are the hardest. Each stores an `EntityNPCInterface` field and calls MC methods extensively.

### Part 8.1: DataAdvanced
> NPC lines, sounds, factions, roles, jobs.

**SPLITTABLE:**
- Core: All serializable fields (faction ID, lines, sounds config), `readNBT`/`writeNBT` using `INbt`
- Shadow: `EntityNPCInterface` reference, role/job instance creation, MC-specific setters

### Part 8.2: DataStats
> Combat stats, resistances, attributes.

**SPLITTABLE:**
- Core: All stat fields (HP, damage, armor, resistances), NBT serialization
- Shadow: `SharedMonsterAttributes` application, `EnumCreatureAttribute`

### Part 8.3: DataAI
> Movement, combat tactics, pathfinding config.

**SPLITTABLE:**
- Core: All AI config fields (nav type, combat policy, speeds, ranges), NBT serialization
- Shadow: `SharedMonsterAttributes` application, `MathHelper`, position access

### Part 8.4: DataDisplay
> Skin, model, overlays, visual settings.

**SPLITTABLE:**
- Core: All display fields (skin URL, model type, overlays, name, tint), NBT serialization
- Shadow: `Minecraft` client texture loading, `EntityPlayer` skin resolution, `NBTUtil`, `MinecraftServer` profile lookups

### Part 8.5: DataAbilities
> Ability slots, cooldowns, execution phases.

**SPLITTABLE:**
- Core: Ability slot data, cooldown timers, NBT serialization
- Shadow: `Entity/EntityLivingBase` targeting, `DamageSource`, world time access

### Part 8.6: DataInventory
> Equipment, drops, loot. Implements `IInventory`.

**STAYS in m7:**
- 11 MC imports, `implements IInventory`, direct `ItemStack` field management, `EntityItem` spawning
- Too deeply coupled to MC inventory system to split cleanly

---

## STAYS IN M7 — Files That Do NOT Move

These files are deeply MC-coupled and stay version-specific. Listed for completeness.

### Controllers
| File | Reason |
|------|--------|
| `ChunkController.java` | `ForgeChunkManager`, `LoadingCallback` — Forge-specific |
| `CustomGuiController.java` | GUI packets, `EntityPlayerMP` networking |
| `LinkedNpcController.java` | `EntityNPCInterface`, `EntityCustomNpc` entity operations |
| `RecipeController.java` | `InventoryCrafting`, `CraftingManager`, `ItemStack` — extends MC recipe system |
| `EnergyController.java` | `Entity`, `EntityLivingBase`, `World` — spawns energy entities |
| `TelegraphController.java` | `Entity`, `EntityPlayerMP`, `World` — visual effects |
| `ScriptController.java` | `MinecraftServer`, `WorldEvent`, player commands |
| `ScriptEntityData.java` | `IExtendedEntityProperties` — Forge-specific |

### Data Classes
| File | Reason |
|------|--------|
| `RecipeCarpentry.java` | `extends ShapedRecipes` — MC recipe class |
| `RecipeAnvil.java` | `ItemStack`, Forge `Constants` |
| `RecipesDefault.java` | `Block`, `Items`, `Item`, `ItemStack` — MC block/item references |
| `MarkData.java` | `IExtendedEntityProperties`, `Entity`, `World` |
| `DataInventory.java` | `implements IInventory`, 11 MC imports |

### Roles
| File | Reason |
|------|--------|
| `RoleCompanion.java` | 18 MC imports — items, armor, attributes, damage |
| `JobSpawner.java` | Entity spawning |
| `JobChunkLoader.java` | `ForgeChunkManager` |
| `JobBard.java` (partial) | `Minecraft` client audio |
| `CompanionFoodStats.java` | `ItemFood`, `DamageSource` |
| `CompanionGuard.java` | `Entity`, `IMob` |

### Entire Subsystems (per-version, no core equivalent)
| Subsystem | Files | Reason |
|-----------|-------|--------|
| `noppes.npcs.entity/` | 40 | Extend MC Entity classes |
| `noppes.npcs.blocks/` + `tiles/` | 98 | Extend MC Block/TileEntity |
| `noppes.npcs.items/` | 56 | Extend MC Item |
| `noppes.npcs.ai/` | 42 | Extend MC EntityAIBase |
| `noppes.npcs.client/` + `kamkeel.npcs.client/` | 642 | MC rendering |
| `kamkeel.npcs.network/` | 296 | MC networking |
| `kamkeel.npcs.command/` | 33 | MC command system |
| `noppes.npcs.scripted/` | 109 | MC wrapper classes |
| `noppes.npcs.containers/` | ~30 | Extend MC Container |
| `noppes.npcs.mixin/` | ~8 | Per-version mixins |
| `kamkeel.npcs.addon/` | ~5 | Per-version addons |
| `noppes.npcs.enchants/` | ~5 | Extend MC Enchantment |
| `noppes.npcs.compat/` | ~5 | Per-version compatibility |

---

## Summary Statistics

| Category | Files to Core | Files Staying | New PA Interfaces |
|----------|--------------|---------------|-------------------|
| **Wave 0** (Foundation) | ~18 | 0 | 0 |
| **Wave 1** (NBT-only) | ~25 | 0 | ~2 methods on IPlayer/IWorld |
| **Wave 2** (Player-dependent) | ~20 | 0 | `IMessageService`, `IServerService`, `ITranslationService` |
| **Wave 3** (PlayerData) | ~12 | 0 | 0 |
| **Wave 4** (Config) | ~10 | 5 | 0 |
| **Wave 5** (Roles/Jobs) | ~18 | 6 | 0 |
| **Wave 6** (Scripts) | ~8 | 13 | 0 |
| **Wave 7** (Sync) | 1 (split) | 0 | `ISyncService` |
| **Wave 8** (NPC Data) | 5 (split) | 1 | 0 |
| **TOTAL** | **~117 files** | **~1,370+** | **4 new interfaces** |

### After full migration:
- **core/**: ~305 files (188 current + 117 new)
- **m7-only**: ~1,370 files (entity, blocks, items, AI, client, networking, commands, scripting wrappers)
- **Code sharing ceiling**: ~18% of total codebase in core — but this covers ~90% of GAME LOGIC (controllers, data, quest logic, roles, config values)

---

## Execution Order

```
Wave 0 ████  (Parts 0.1-0.5) — No dependencies, do first
Wave 1 ████████  (Parts 1.1-1.10) — Depends on Wave 0
Wave 2 ██████  (Parts 2.0-2.8) — Depends on Wave 1 (esp. Availability)
Wave 3 ████████  (Parts 3.0-3.5) — Depends on Wave 2
Wave 4 ████  (Part 4.1) — Independent of Waves 2-3, can parallel
Wave 5 ████████  (Parts 5.0-5.5) — Depends on Wave 2 (IMessageService)
Wave 6 ████  (Parts 6.1-6.2) — Independent, can parallel with Wave 3+
Wave 7 ██  (Part 7.1) — Depends on Waves 1-3
Wave 8 ██████  (Parts 8.1-8.6) — Last, most complex splits
```

**Parallelizable:**
- Wave 0 → then Wave 1
- Waves 4, 6 can run in parallel with Waves 2-3
- Wave 5 can start after Part 2.0

---

## Agent Delegation Guide

Each Part is designed to be delegated to a single `unspecified-high` agent with:
1. This document as context
2. SESSION_MEMORY.md for architecture understanding
3. Clear success criteria: `./gradlew :core:compileJava && ./gradlew :mc1710:build`

**Per-Part agent prompt template:**
```
Execute Part X.Y of the CORE_MIGRATION_PLAN.
- Read the Part description for exact files and changes
- Follow the MOVE → ABSTRACT → SHADOW → VERIFY cycle
- Use INbt instead of NBTTagCompound, IPlayer instead of EntityPlayer, etc.
- Retain same package when moving to core/
- New PA interfaces go under common.minecraft if not under noppes.npcs.api
- Run ./gradlew :core:compileJava && ./gradlew :mc1710:build to verify
- Create .AGENTS/PART_X_Y_REPORT.md upon completion
```
