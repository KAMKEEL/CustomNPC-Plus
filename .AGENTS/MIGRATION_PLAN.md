# MIGRATION PLAN — m7 → core/ (Phase 1A: Core Expansion)

**Created:** 2026-03-15
**Purpose:** Exhaustive, mechanically-executable plan for migrating game logic from mc1710 (m7) into core/.
**Consumed by:** Sisyphus (orchestrator) executing directly + verification by humans.

---

## 🚀 EXECUTION STRATEGY (UPDATED 2026-03-15)

### Why Direct Orchestrator Execution (NOT Agent Delegation)

**Sisyphus will execute all Parts directly.** Here's why this is CRITICAL:

| Factor | Agents | Direct Execution |
|--------|--------|------------------|
| **Speed** | ~8+ hours (spawn overhead, context-switching, slow edits) | ~2-3 hours (no overhead, bulk edits) |
| **Edit Quality** | Multiple passes per file, each touching code → bloats diffs, loses formatting | Single surgical read → one bulk edit per file → preserves ALL formatting |
| **File Handling** | Edit 1 file at a time, multiple edits per file | Read 5-10 files in parallel, bulk-edit each once |
| **Git Hygiene** | Many small commits, reformatted files → large bloated diffs | Minimal diffs, one commit per Part (human commits after build verification) |
| **Context** | Loses full context between calls, must re-explain | Maintains full session context, understands all tradeoffs |

### 🔴 CRITICAL EXECUTION RULES (UPDATED 2026-03-15)

**MANDATORY — NO EXCEPTIONS:**

1. **Parallel Bulk Edits (VITAL)**
   - For EACH Part: Read ALL files for that Part in parallel
   - Edit ALL files in ONE response using parallel `edit()` calls
   - Each file gets ONE edit() call with ALL its required changes (imports + types + everything)
   - NEVER override files — use exact string matching oldString → newString swaps only
   - Result: surgical edits, formatting preserved, comments intact

2. **LSP-Driven Workflow (VITAL)**
   - LSP is the PRIMARY source of truth — NOT code analysis, NOT grepping
   - After ALL edits for a Part are applied, run `lsp_diagnostics` on EVERY moved file
   - If LSP shows ZERO errors → Part is done, move to next Part
   - If LSP shows errors → STOP immediately and fix those specific errors
   - **Do NOT proceed to next Part until LSP shows zero errors for current Part**
   - **If LSP is broken or unresponsive, STOP WORK and notify human immediately**

3. **No Commits by Agent (VITAL)**
   - DO NOT commit after Parts complete
   - Committing requires build verification (which is slow)
   - Human will verify build afterward and commit
   - Just complete the Part, report status, move to next
   - Sisyphus responsibility: code correctness via LSP. Human responsibility: build verification + commit.

4. **Wave Completion Requirement (VITAL)**
   - Per each "wave" (set of Parts), do NOT stop until LSP shows no errors
   - If a Part produces LSP errors, fix them immediately (same response or next response)
   - Only move to next wave when ENTIRE current wave has LSP ✅

### Execution Pattern (Per Part)

```
1. READ all files for this Part in parallel
2. FOR EACH FILE (parallel edits in ONE response):
   - Analyze ALL changes needed (imports, types, constructors, method signatures)
   - Build ONE bulk oldString with all code sections that need changing
   - Build ONE bulk newString with all corrections
   - Apply ONE edit() call (exact string swap, no file rewrite)
3. Run lsp_diagnostics on ALL moved files → MUST show zero errors
4. If errors → FIX them immediately (go back to step 2)
5. If zero errors → Part COMPLETE, move to next Part
```

### Parallel Editing Capability

**YES — Sisyphus can edit 8-10 files in a single response:**
- `edit()` tool accepts one file per call but multiple calls execute in parallel
- All tool calls in one response block run concurrently
- Typical Part has 3-8 files → all edited at once in one turn
- Larger Parts (10-12 files) handled in one batch

**Example (Part 5: Pure-NBT Data Classes):**
```
Response 1: Read Dialog.java, DialogOption.java, DialogCategory.java, QuestData.java (parallel reads)
Response 2: 
  - edit(Dialog.java) with ALL changes
  - edit(DialogOption.java) with ALL changes
  - edit(DialogCategory.java) with ALL changes
  - edit(QuestData.java) with ALL changes
  → ALL 4 edits happen at same time
Response 3: Run lsp_diagnostics on all 4 → verify zero errors
```

### Bulk Edit Implementation

Each file edit follows this pattern:
```java
// Read the file (done in parallel with other files)
// Analyze: list ALL changes needed (imports, types, constructors)
// Build oldString with MULTIPLE lines/sections if needed
// Build newString with ALL corrections
// One edit() call → all changes applied atomically
// Result: file is correct, formatting unchanged, comments preserved, spacing identical
```

### Failure Modes (STOP IMMEDIATELY)

- **LSP is unresponsive** → Stop work, notify human
- **LSP shows errors after edits** → Fix them immediately (do not proceed)
- **Error pattern suggests bad abstraction** → Stop, notify human with details
- **Build system is offline** → Stop, notify human

---

## How This Plan Works

### Sisyphus Execution Workflow (Per Part)

**⚠️ UPDATED: Direct execution, LSP-driven, parallel edits, no commits**

```
PART N:
  1. Sisyphus reads ALL files for Part N (parallel)
  2. Sisyphus applies ALL edits in ONE response (parallel edit() calls, bulk changes)
  3. Sisyphus runs lsp_diagnostics on ALL moved files
  4. IF errors found:
     - Sisyphus fixes errors immediately
     - Go back to step 3 (re-run LSP)
  5. IF zero errors:
     - Part N COMPLETE ✅
     - Move to Part N+1
  6. Human manually builds + commits when wave is complete
```

### Rules
- Files retain their package when moved (e.g., `noppes.npcs.controllers.data.Dialog` stays `noppes.npcs.controllers.data.Dialog`)
- New PA abstractions go to `common.minecraft.*` in platform-api (NOT `noppes.npcs.common.minecraft`) (unless one already exists in `noppes.npcs.api.*`)
- m7 shadows use split-package pattern: same package in mc1710, extends/overrides the core version
- **LSP diagnostics is the ONLY source of truth** — not code analysis, not grepping
- **One bulk edit per file** — all changes at once, never file rewrites
- **NO commits by agent** — human verifies build and commits
- **Do NOT proceed until LSP shows zero errors** — per wave completion rule
- **Parallel edits mandatory** — maximize file throughput per response

### Hardest Parts for Agents (Human Should Be Aware)
These Parts require creative abstraction work, not just mechanical import swapping:

| Part | Why It's Hard |
|------|--------------|
| **Part 6: Availability** | Cross-cutting dependency. Has `@SideOnly` client display methods + `StatCollector` (i18n) + `MathHelper`. Core version must split data/NBT from player-checking logic. m7 shadow is complex. |
| **Part 14: AuctionController** | Uses `ChatComponentText` + `EnumChatFormatting` for player messages. Needs new PlatformService methods for sending formatted messages. |
| **Part 16: PlayerDataController** | Uses `MinecraftServer`, `ICommandSender`, `PlayerSelector` — deep server coupling. Core version needs significant PlatformService expansion. |
| **Part 17: PlayerQuestController** | Uses `ChatComponentTranslation` for localized messages. |
| **Part 20: SpawnController** | Uses `WeightedRandom` — MC utility class. Need to reimplement or abstract. |
| **Part 24: ServerCloneController** | Uses `EntityList`, `ICommandSender`, `ChatComponentText` — entity registry + commands. |

---

## Summary Table

| Part | Title | Files | Complexity | Dependencies | Agent Difficulty |
|------|-------|-------|------------|--------------|-----------------|
| 1 | Zero-MC Root Utilities | 8 | Low | None | MECHANICAL |
| 2 | Zero-MC kamkeel Utilities | 5 | Low | None | MECHANICAL |
| 3 | Action Framework | 9 | Low | None | MECHANICAL |
| 4 | Config Data Classes | 3 | Low | None | MECHANICAL |
| 5 | Pure-NBT Data Classes (Batch 1) | 4 | Low | None | MECHANICAL |
| 6 | Availability (Cross-Cutting) | 1 | High | Part 1 | CREATIVE |
| 7 | Dialog Data Cluster | 3 | Medium | Part 6 | MECHANICAL |
| 8 | Quest Data Cluster | 3 | Medium | Part 6 | MECHANICAL |
| 9 | FactionOptions | 1 | Medium | None | MECHANICAL |
| 10 | Auction Data Classes | 3 | Medium | None | MECHANICAL |
| 11 | LinkedItem | 1 | Medium | None | MECHANICAL |
| 12 | Resistances | 1 | Medium | None | MECHANICAL |
| 13 | ScriptHookController + HookDefinition | 2 | Low | None | MECHANICAL |
| 14 | BankController | 1 | Medium | Part 5 | MECHANICAL |
| 15 | AnimationController | 1 | Medium | None | MECHANICAL |
| 16 | DialogController | 1 | Medium | Part 7 | MECHANICAL |
| 17 | QuestController | 1 | Medium | Part 8 | MECHANICAL |
| 18 | LinkedItemController | 1 | Medium | Part 11 | MECHANICAL |
| 19 | CustomEffectController | 1 | Medium | None | MECHANICAL |
| 20 | SpawnController | 1 | Medium | None | CREATIVE |
| 21 | AuctionController | 1 | High | Part 10 | CREATIVE |
| 22 | MarketRegistry | 1 | Medium | None | MECHANICAL |
| 23 | PlayerQuestController | 1 | High | Part 8 | CREATIVE |
| 24 | PlayerDataController | 1 | High | None | CREATIVE |
| 25 | PartyController | 1 | Medium | None | MECHANICAL |
| 26 | ServerCloneController | 1 | High | None | CREATIVE |
| 27 | ProfileController | 1 | Medium | None | MECHANICAL |
| 28 | AttributeController | 1 | Low | None | MECHANICAL |
| 29 | Quest Types (Batch 1) | 3 | Medium | Part 8 | MECHANICAL |
| 30 | Quest Types (Batch 2) | 3 | Medium | Part 8 | MECHANICAL |
| 31 | VersionCompatibility | 1 | Medium | None | MECHANICAL |
| **TOTAL** | | **~63 files** | | | |

---

## New PA Abstractions Needed (Batch-Create Before Parts)

These new abstractions appear across multiple Parts. **Create them ALL upfront** so Parts can reference them.

### New PlatformService Methods

Add to `kamkeel.npcs.platform.PlatformService`:

```java
// --- Messaging ---
void sendMessage(IPlayer player, String message);
void sendTranslatedMessage(IPlayer player, String translationKey, Object... args);
void sendFormattedMessage(IPlayer player, String message, String color);

// --- Server ---
List<IPlayer> getOnlinePlayers();
IPlayer getPlayerByName(String name);
IPlayer getPlayerByUUID(UUID uuid);

// --- Translation ---
String translate(String key);
String translate(String key, Object... args);

// --- Weighted Random ---
/**
 * Select a random entry from weighted items.
 * Each entry has an associated integer weight.
 */
<T> T weightedRandom(List<T> items, java.util.function.ToIntFunction<T> weightExtractor, java.util.Random random);
```

### New PA Interfaces (in `common.minecraft.*`)

| Interface | Package | Wraps | Used By Parts |
|-----------|---------|-------|---------------|
| *(none needed)* | — | — | — |

**Key insight:** All needed MC types already have PA equivalents or can be handled by PlatformService methods. No new `common.minecraft.*` interfaces are required at this time. The types that would need new interfaces (ChatComponentText, StatCollector, MathHelper, MinecraftServer, WeightedRandom) are all better served by PlatformService methods or Java stdlib equivalents.

### MC Type → Resolution Map

| MC Type | Resolution | Notes |
|---------|-----------|-------|
| `NBTTagCompound` | `INbt` (exists) | Direct swap |
| `NBTTagList` | `INbtList` (exists) | Direct swap |
| `NBTTagString` | `INbt.setString()` calls | Inline — no separate wrapper needed |
| `EntityPlayer` / `EntityPlayerMP` | `IPlayer` (exists) | Direct swap |
| `Entity` | `IEntity` (exists) | Direct swap |
| `EntityLivingBase` | `IEntityLivingBase` (exists) | Direct swap |
| `ItemStack` | `IItemStack` (exists) | Direct swap |
| `World` | `IWorld` (exists) | Direct swap |
| `DamageSource` | `IDamageSource` (exists) | Direct swap |
| `CompressedStreamTools` | `PlatformService.readCompressedNBT()` / `.writeCompressedNBT()` | Method call swap |
| `new NBTTagCompound()` | `NBT.compound()` (exists in core) | Factory swap |
| `new NBTTagList()` | `NBT.list()` (exists in core) | Factory swap |
| `StatCollector.translateToLocal()` | `PlatformService.translate()` | NEW — add to PlatformService |
| `MathHelper.clamp_int/float` | `ValueUtil.clamp()` (exists in core) or `Math.min/max` | Use stdlib/core |
| `ChatComponentText` | `PlatformService.sendMessage()` | NEW — add to PlatformService |
| `ChatComponentTranslation` | `PlatformService.sendTranslatedMessage()` | NEW — add to PlatformService |
| `EnumChatFormatting` | Plain `String` color codes or `PlatformService.sendFormattedMessage()` | NEW — add to PlatformService |
| `MinecraftServer.getConfigurationManager()` | `PlatformService.getOnlinePlayers()` / `.getPlayerByName()` | NEW — add to PlatformService |
| `ICommandSender` | Method parameter becomes `Object` (cast in m7 shadow) | Decouple in core |
| `PlayerSelector` | Stays in m7 shadow only | Platform-specific |
| `WeightedRandom` | `PlatformService.weightedRandom()` or reimplement in core | NEW or reimplement |
| `Constants.NBT` | Inline int constants (e.g., `Constants.NBT.TAG_COMPOUND` = `10`) | Just use `NBTTypes` enum from core constants |
| `Side` / `SideOnly` | `@ClientOnly` / `@ServerOnly` (exist in PA) | Annotation swap |
| `FMLLog` | `PlatformService.logWarn/Error()` | Method call swap |
| `Configuration` / `Property` | `IConfigProvider` (exists in PA) or stays in m7 | Config loading stays in m7 |
| `EntityList` | Stays in m7 shadow | Platform-specific registry |
| `ForgeChunkManager` | STAYS in m7 | Not abstracted |

---

## Verification Protocol

Every Part follows this exact verification sequence:

```
1. Human moves files to core/
2. Agent: lsp_diagnostics on EVERY moved file → capture all errors
3. Agent: For each error:
   a. If it's an MC import → replace with PA equivalent per the table above
   b. If it's a type usage in code (method param, field, local var) → change type
   c. If it's a constructor call (new NBTTagCompound()) → use NBT.compound()
   d. If it's a static method call (CompressedStreamTools.read) → PlatformService call
4. Agent: lsp_diagnostics again → should be zero errors in moved files
5. Agent: Check if m7 shadow is needed (file had MC-specific methods that were removed)
   a. If yes → create m7 shadow that extends/overrides core version, adds MC methods back
6. Agent: lsp_diagnostics on m7 shadow files
7. Agent: ./gradlew build → must pass
```

---

## Parts

---

### Part 1: Zero-MC Root Utilities — Complexity: Low
**Dependencies:** None
**Estimated files:** 8 files to move, 0 abstractions
**Agent Difficulty:** MECHANICAL — pure file move, no MC imports to fix

#### Human: Move these files from m7 → core/
(retain same package)
- `noppes/npcs/LogWriter.java`
- `noppes/npcs/NPCLogLevel.java`
- `noppes/npcs/IChatMessages.java`
- `noppes/npcs/ICompatibilty.java`
- `noppes/npcs/util/JsonException.java`
- `noppes/npcs/util/MarketCachedObject.java`
- `noppes/npcs/util/AuctionFormatUtil.java`
- `noppes/npcs/util/ScriptToStringHelper.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| *(none expected)* | — | — |

These files have zero MC imports. After move, run `lsp_diagnostics` to confirm. If any internal cross-references break (e.g., references to other m7 classes), those are bugs to investigate — they should all reference core-safe types.

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
*(none expected — these are pure utilities)*

#### Notes
- `ICompatibilty` (note the typo — it's the actual class name, keep it) is an interface for version compatibility
- `ScriptToStringHelper` provides toString utilities for scripting — no MC dependency
- This Part validates the human-agent workflow on the simplest possible case

---

### Part 2: Zero-MC kamkeel Utilities — Complexity: Low
**Dependencies:** None
**Estimated files:** 5 files to move, 0 abstractions
**Agent Difficulty:** MECHANICAL

#### Human: Move these files from m7 → core/
- `kamkeel/npcs/CustomAttributes.java`
- `kamkeel/npcs/addon/AddonManager.java`
- `kamkeel/npcs/util/ColorUtil.java`
- `kamkeel/npcs/util/Register.java`
- `kamkeel/npcs/util/BukkitUtil.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| *(none expected)* | — | — |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
*(none)*

#### Notes
- `BukkitUtil` was marked as STAYS in earlier analysis — verify it truly has zero MC imports via LSP. If it does have MC imports, SKIP it from this Part and leave in m7.
- `Developer.java` is already in core — do NOT move again.

---

### Part 3: Action Framework — Complexity: Low
**Dependencies:** None
**Estimated files:** 9 files to move, 0 abstractions
**Agent Difficulty:** MECHANICAL — zero MC imports, pure game logic

#### Human: Move these files from m7 → core/
Package: `noppes/npcs/controllers/data/action/`
- `noppes/npcs/controllers/data/action/Action.java`
- `noppes/npcs/controllers/data/action/ActionChain.java`
- `noppes/npcs/controllers/data/action/ActionList.java`
- `noppes/npcs/controllers/data/action/ActionListener.java`
- `noppes/npcs/controllers/data/action/ActionLogger.java`
- `noppes/npcs/controllers/data/action/ActionManager.java`
- `noppes/npcs/controllers/data/action/ActionQueue.java`
- `noppes/npcs/controllers/data/action/ActionThread.java`
- `noppes/npcs/controllers/data/action/ConditionalAction.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| *(none — all zero MC imports)* | — | — |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
*(none — these are complete as-is)*

#### Notes
- The entire `action/` subdirectory is self-contained, MC-free game logic
- Move the whole directory at once

---

### Part 4: Config Legacy Data Classes — Complexity: Low
**Dependencies:** None
**Estimated files:** 3 files to move, 0 abstractions
**Agent Difficulty:** MECHANICAL

#### Human: Move these files from m7 → core/
- `noppes/npcs/config/LoadConfiguration.java`
- `noppes/npcs/config/legacy/LegacyLoader.java`
- `noppes/npcs/config/legacy/ConfigProp.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| *(none — LoadConfiguration has zero MC imports, LegacyLoader/ConfigProp have zero MC imports)* | — | — |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
*(none)*

#### Notes
- `LoadConfiguration.java` orchestrates config loading — it calls into `ConfigMain`, `ConfigScript`, etc. Those config files themselves STAY in m7 (they use Forge `Configuration` class). If LoadConfiguration references them, its core version should reference config value holders only. Check via LSP.
- `LegacyConfig.java` STAYS — uses `Minecraft` client class
- The actual Config*.java files (ConfigMain, ConfigClient, etc.) STAY in m7 — they all use `net.minecraftforge.common.config.Configuration`. The config VALUES already sync to `CoreConfig` in core.
- `StringCache.java`, `GlyphCache.java` — STAY (rendering)
- `ConfigMixin.java` — STAY (mixin config)

---

### Part 5: Pure-NBT Data Classes (Batch 1) — Complexity: Low
**Dependencies:** None
**Estimated files:** 4 files to move, 0 new abstractions
**Agent Difficulty:** MECHANICAL — only NBTTagCompound → INbt swaps

#### Human: Move these files from m7 → core/
- `noppes/npcs/controllers/data/Bank.java`
- `noppes/npcs/controllers/data/QuestData.java`
- `noppes/npcs/controllers/data/DialogCategory.java`
- `noppes/npcs/controllers/data/QuestCategory.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |

#### Agent: Type changes in code
- `NBTTagCompound` → `INbt` (all field types, method params, return types, local vars)
- `NBTTagList` → `INbtList`
- `new NBTTagCompound()` → `NBT.compound()` (import `noppes.npcs.core.NBT`)
- `new NBTTagList()` → `NBT.list()`
- `compound.setString(...)` → `compound.setString(...)` (INbt has same method names — check LSP)
- `compound.getCompoundTag(...)` → `compound.getCompound(...)` (INbt method name differs — check LSP)

#### Agent: Create these PA abstractions
*(none — INbt and INbtList exist)*

#### Agent: Create/update these m7 shadow files
- `mc1710/src/main/java/noppes/npcs/controllers/data/Bank.java` — shadow if m7 needs to accept raw `NBTTagCompound` in readNBT/writeNBT from callers that pass MC types
- Same pattern for DialogCategory, QuestCategory, QuestData

#### Notes
- These 4 files use ONLY NBTTagCompound/NBTTagList — the simplest SPLITTABLE case
- Bank.java has `implements ICompatibilty` — this interface moves in Part 1, so no conflict
- DialogCategory references `Dialog` objects — Dialog moves in Part 7. If DialogCategory has compile errors referencing Dialog, that's expected and will resolve after Part 7. Move them together if needed.
- **ALTERNATIVE:** If DialogCategory depends on Dialog, merge Part 5 and Part 7 into a single move.

---

### Part 6: Availability (Cross-Cutting) — Complexity: High
**Dependencies:** Part 1 (ICompatibilty, VersionCompatibility)
**Estimated files:** 1 file to move, new PlatformService methods
**Agent Difficulty:** CREATIVE — this is the hardest single-file migration

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/data/Availability.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.util.MathHelper` | `noppes.npcs.util.ValueUtil` or `java.lang.Math` | USE CORE/STDLIB |
| `net.minecraft.util.StatCollector` | `kamkeel.npcs.platform.PlatformServiceHolder.get().translate()` | NEW — add translate() to PlatformService |
| `cpw.mods.fml.relauncher.Side` | Remove import, remove `@SideOnly` methods from core version | ANNOTATION |
| `cpw.mods.fml.relauncher.SideOnly` | Remove import, remove `@SideOnly` methods from core version | ANNOTATION |

#### Agent: Detailed code changes
1. **Remove `@SideOnly(Side.CLIENT)` methods** from core version — these are client display methods. Move them to the m7 shadow.
2. **Replace `MathHelper.clamp_int(x, min, max)`** → `ValueUtil.clamp(x, min, max)` or `Math.max(min, Math.min(max, x))`
3. **Replace `StatCollector.translateToLocal(key)`** → `PlatformServiceHolder.get().translate(key)`
4. **Replace `EntityPlayer` parameter** → `IPlayer` in `isAvailable(EntityPlayer player)` method signature
5. The `isAvailable` method body accesses player quest data, faction data, dialog data — verify these controller references work from core (they should if those controllers are in core or referenced via static Instance)

#### Agent: Create these PA abstractions
Add to `PlatformService.java`:
```java
String translate(String key);
String translate(String key, Object... args);
```

#### Agent: Create/update these m7 shadow files
- `mc1710/src/main/java/noppes/npcs/controllers/data/Availability.java` — shadow that:
  - Adds back `@SideOnly(Side.CLIENT)` display methods
  - Adds convenience method `isAvailable(EntityPlayer player)` that wraps player → IPlayer and delegates to core
  - Adds `StatCollector` calls for client-side text that the core version delegates to PlatformService

#### Notes
- **THIS IS THE MOST IMPORTANT MIGRATION.** Availability is used by Dialog, Quest, DialogOption, and many other classes. Moving it early unblocks everything.
- The core version becomes data-only (NBT read/write) + `isAvailable(IPlayer)` (game logic)
- The m7 shadow adds MC-specific display methods
- The `isAvailable` method calls `PlayerQuestController`, `FactionController`, `QuestController` — verify these are accessible from core at this point. FactionController is already in core. QuestController moves later — may need to use `static Instance` access pattern (which works since Instance is set at runtime).

---

### Part 7: Dialog Data Cluster — Complexity: Medium
**Dependencies:** Part 6 (Availability)
**Estimated files:** 3 files to move, 0 new abstractions
**Agent Difficulty:** MECHANICAL

#### Human: Move these files from m7 → core/
- `noppes/npcs/controllers/data/Dialog.java`
- `noppes/npcs/controllers/data/DialogOption.java`
- `noppes/npcs/controllers/data/FactionOptions.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |
| `cpw.mods.fml.relauncher.Side` | Remove — move `@SideOnly` methods to m7 shadow | ANNOTATION |
| `cpw.mods.fml.relauncher.SideOnly` | Remove — move `@SideOnly` methods to m7 shadow | ANNOTATION |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../Dialog.java` — shadow that adds `implements IDialog`, MC-specific convenience methods
- `mc1710/.../DialogOption.java` — shadow with MC-specific methods if any
- `mc1710/.../FactionOptions.java` — shadow that adds `@SideOnly` client methods back

#### Notes
- Dialog.java references `Availability` (moved in Part 6) — should resolve
- Dialog.java references `EventHooks` — EventHooks STAYS in m7. If Dialog calls EventHooks, those calls must move to the m7 shadow
- Dialog.java references `NpcAPI` — scripting wrapper, STAYS. Same treatment: move scripting calls to m7 shadow
- DialogOption.java references Availability — OK after Part 6

---

### Part 8: Quest Data Cluster — Complexity: Medium
**Dependencies:** Part 6 (Availability)
**Estimated files:** 3 files to move, 0 new abstractions
**Agent Difficulty:** MECHANICAL

#### Human: Move these files from m7 → core/
- `noppes/npcs/controllers/data/Quest.java`
- `noppes/npcs/controllers/data/QuestInterface.java` *(base class for quest types — verify MC imports first)*

**⚠️ PRE-CHECK:** Before moving QuestInterface.java, agent must verify its MC imports. If it references `EntityNPCInterface`, it STAYS in m7. Only move it if its MC coupling is limited to `EntityPlayer` + `NBTTagCompound`.

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.entity.player.EntityPlayerMP` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../Quest.java` — shadow that adds `implements IQuest`, EntityPlayer convenience methods

#### Notes
- Quest.java references `Availability` (Part 6), `EventHooks` (STAYS), `NpcAPI` (STAYS)
- Move EventHooks/NpcAPI calls to m7 shadow
- QuestCategory.java already moved in Part 5

---

### Part 9: FactionOptions — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file, already covered in Part 7

*FactionOptions is included in Part 7. This Part is intentionally empty — kept for numbering consistency.*

---

### Part 10: Auction Data Classes — Complexity: Medium
**Dependencies:** None
**Estimated files:** 3 files to move, 0 new abstractions
**Agent Difficulty:** MECHANICAL

#### Human: Move these files from m7 → core/
- `noppes/npcs/controllers/data/AuctionBlacklist.java`
- `noppes/npcs/controllers/data/AuctionClaim.java`
- `noppes/npcs/controllers/data/AuctionListing.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.item.ItemStack` | `noppes.npcs.api.item.IItemStack` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- m7 shadows for each file if they need to accept raw MC types from callers

#### Notes
- AuctionBlacklist uses EntityPlayer for permission checks — abstract to IPlayer
- AuctionClaim/AuctionListing hold ItemStack fields — change to IItemStack
- These reference `AuctionFilter` which is already in core

---

### Part 11: LinkedItem — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file to move, 0 new abstractions
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/data/LinkedItem.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.item.ItemStack` | `noppes.npcs.api.item.IItemStack` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraftforge.common.util.Constants` | Use `noppes.npcs.constants.NBTTypes` (core enum) for NBT type constants | EXISTS IN CORE |

#### Agent: Additional fixes
- `Constants.NBT.TAG_COMPOUND` → `NBTTypes.TAG_COMPOUND` (or inline value `10`)
- `Constants.NBT.TAG_STRING` → `NBTTypes.TAG_STRING` (or inline value `8`)
- References to `CustomItems` (m7) → move to m7 shadow
- References to `EventHooks` (m7) → move to m7 shadow
- References to `NpcAPI`, `ScriptLinkedItem`, `NBTWrapper` (scripting) → move to m7 shadow

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../LinkedItem.java` — shadow that adds back `CustomItems`, `EventHooks`, `NpcAPI` references

#### Notes
- LinkedItem has many references to m7-specific classes (CustomItems, EventHooks, NpcAPI, ScriptLinkedItem)
- Core version keeps: NBT serialization, data fields, tag management
- m7 shadow adds: item registration hooks, event firing, script wrapper creation

---

### Part 12: Resistances — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file to move, 0 new abstractions
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/Resistances.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.util.DamageSource` | `noppes.npcs.api.IDamageSource` | EXISTS |

#### Agent: Detailed code changes
- `writeToNBT()` return type: `NBTTagCompound` → `INbt`
- `readToNBT(NBTTagCompound)` param: → `readToNBT(INbt)`
- `new NBTTagCompound()` → `NBT.compound()`
- `applyResistance(DamageSource)` param: → `applyResistance(IDamageSource)`
- Check if DamageSource methods used (`.isProjectile()`, `.isExplosion()`, etc.) exist on IDamageSource

#### Agent: Create these PA abstractions
*(none — IDamageSource exists)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../Resistances.java` — shadow with `applyResistance(DamageSource)` convenience method that wraps → IDamageSource

---

### Part 13: ScriptHookController + HookDefinition — Complexity: Low
**Dependencies:** None
**Estimated files:** 2 files to move, 0 abstractions
**Agent Difficulty:** MECHANICAL

#### Human: Move these files from m7 → core/
- `noppes/npcs/controllers/ScriptHookController.java`
- `noppes/npcs/controllers/HookDefinition.java` *(if this exists as a separate file — verify)*

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| *(ScriptHookController has zero MC imports)* | — | — |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
*(none expected)*

#### Notes
- ScriptHookController defines hook types (154+ events) — pure metadata, no MC dependencies
- Verify via LSP that it truly has zero MC imports after move

---

### Part 14: BankController — Complexity: Medium
**Dependencies:** Part 5 (Bank.java data class)
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/BankController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.nbt.CompressedStreamTools` | `kamkeel.npcs.platform.PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |

#### Agent: Detailed code changes
- File I/O pattern: `CompressedStreamTools.readCompressed(new FileInputStream(file))` → `PlatformServiceHolder.get().readCompressedNBT(file)`
- `CompressedStreamTools.writeCompressed(compound, new FileOutputStream(file))` → `PlatformServiceHolder.get().writeCompressedNBT(compound, file)`
- All NBT type swaps as in Part 5

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../BankController.java` — shadow that adds `implements IBankHandler` (if such interface exists), or any MC-specific initialization code

---

### Part 15: AnimationController — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/AnimationController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../AnimationController.java` — shadow that adds `implements IAnimationHandler`

#### Notes
- AnimationController manages animation definitions (JSON-based) — the save/load is NBT-only
- Animation.java (entity-coupled data) STAYS in m7 — this is just the controller/registry

---

### Part 16: DialogController — Complexity: Medium
**Dependencies:** Part 7 (Dialog, DialogOption, DialogCategory data classes)
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/DialogController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../DialogController.java` — shadow that adds `implements IDialogHandler`, SyncController calls

#### Notes
- DialogController has NO EntityPlayer imports — it's purely NBT file I/O
- References to `SyncController` (m7) for client sync → move sync calls to m7 shadow
- References to `Dialog`, `DialogCategory` → these should be in core after Parts 5+7

---

### Part 17: QuestController — Complexity: Medium
**Dependencies:** Part 8 (Quest, QuestCategory, QuestData data classes)
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/QuestController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../QuestController.java` — shadow that adds `implements IQuestHandler`, SyncController calls

---

### Part 18: LinkedItemController — Complexity: Medium
**Dependencies:** Part 11 (LinkedItem data class)
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/LinkedItemController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../LinkedItemController.java` — shadow that adds `implements ILinkedItemHandler`

---

### Part 19: CustomEffectController — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/CustomEffectController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../CustomEffectController.java` — shadow that adds `implements ICustomEffectHandler`, player-accepting convenience methods

---

### Part 20: SpawnController — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file to move, 1 potential new abstraction
**Agent Difficulty:** CREATIVE — uses `WeightedRandom`

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/SpawnController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |
| `net.minecraft.util.WeightedRandom` | Reimplement in core util or add to PlatformService | NEW |

#### Agent: WeightedRandom resolution
Option A (PREFERRED): Reimplement weighted random selection in core. It's a simple algorithm:
```java
// In core util:
public static <T> T weightedRandom(List<T> items, ToIntFunction<T> weightFn, Random random) {
    int totalWeight = items.stream().mapToInt(weightFn).sum();
    int r = random.nextInt(totalWeight);
    for (T item : items) {
        r -= weightFn.applyAsInt(item);
        if (r < 0) return item;
    }
    return items.get(items.size() - 1);
}
```
Option B: Add `weightedRandom()` to PlatformService.

#### Agent: Create these PA abstractions
*(none — reimplement WeightedRandom in core)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../SpawnController.java` — shadow that adds `implements INaturalSpawnsHandler`

---

### Part 21: AuctionController — Complexity: High
**Dependencies:** Part 10 (Auction data classes)
**Estimated files:** 1 file to move, new PlatformService methods
**Agent Difficulty:** CREATIVE — uses ChatComponentText + EnumChatFormatting for player messaging

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/AuctionController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.entity.player.EntityPlayerMP` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.item.ItemStack` | `noppes.npcs.api.item.IItemStack` | EXISTS |
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |
| `net.minecraft.util.ChatComponentText` | `PlatformServiceHolder.get().sendMessage(player, text)` | NEW |
| `net.minecraft.util.EnumChatFormatting` | Plain string color codes or `PlatformServiceHolder.get().sendFormattedMessage()` | NEW |

#### Agent: Create these PA abstractions
Add to `PlatformService.java`:
```java
void sendMessage(IPlayer player, String message);
void sendFormattedMessage(IPlayer player, String message, String color);
```

#### Agent: Create/update these m7 shadow files
- `mc1710/.../AuctionController.java` — shadow that adds `implements IAuctionHandler`, MC-specific messaging

#### Notes
- The chat message sending is the main challenge. In core, replace `player.addChatMessage(new ChatComponentText(...))` with `PlatformServiceHolder.get().sendMessage(wrappedPlayer, text)`
- Color formatting: `EnumChatFormatting.RED + text` → pass color as parameter or use § codes in the string

---

### Part 22: MarketRegistry — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/MarketRegistry.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayerMP` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../MarketRegistry.java` — shadow with EntityPlayerMP convenience methods

---

### Part 23: PlayerQuestController — Complexity: High
**Dependencies:** Part 8 (Quest data), Part 17 (QuestController)
**Estimated files:** 1 file to move, new PlatformService methods
**Agent Difficulty:** CREATIVE — uses ChatComponentTranslation

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/PlayerQuestController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.entity.player.EntityPlayerMP` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.util.ChatComponentTranslation` | `PlatformServiceHolder.get().sendTranslatedMessage(player, key, args)` | NEW |

#### Agent: Create these PA abstractions
Add to `PlatformService.java` (if not already added in Part 21):
```java
void sendTranslatedMessage(IPlayer player, String translationKey, Object... args);
```

#### Agent: Create/update these m7 shadow files
- `mc1710/.../PlayerQuestController.java` — shadow with EntityPlayer convenience methods

---

### Part 24: PlayerDataController — Complexity: High
**Dependencies:** None (but many things depend on IT)
**Estimated files:** 1 file to move, new PlatformService methods
**Agent Difficulty:** CREATIVE — uses MinecraftServer, ICommandSender, PlayerSelector, ChatComponentText

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/PlayerDataController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.command.ICommandSender` | Remove — change methods to use `String`/`Object` params, cast in m7 shadow | DECOUPLE |
| `net.minecraft.command.PlayerSelector` | Remove — move player selector logic to m7 shadow | STAYS IN M7 |
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.entity.player.EntityPlayerMP` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |
| `net.minecraft.server.MinecraftServer` | `PlatformServiceHolder.get().getOnlinePlayers()` / `.getPlayerByName()` | NEW |
| `net.minecraft.util.ChatComponentText` | `PlatformServiceHolder.get().sendMessage()` | NEW (or already added in Part 21) |

#### Agent: Create these PA abstractions
Add to `PlatformService.java` (if not already added):
```java
List<IPlayer> getOnlinePlayers();
IPlayer getPlayerByName(String name);
IPlayer getPlayerByUUID(java.util.UUID uuid);
```

#### Agent: Create/update these m7 shadow files
- `mc1710/.../PlayerDataController.java` — shadow that adds:
  - `implements IPlayerData` (if such handler interface exists)
  - `PlayerSelector` logic for command-based player lookups
  - `ICommandSender` methods that delegate to core with wrapped types
  - Direct MinecraftServer access for player list queries

#### Notes
- This is one of the most coupled controllers. The `ICommandSender` and `PlayerSelector` usage is for command-based player lookups (e.g., `/kam player @p`). In core, replace with `PlatformService.getPlayerByName()`. The command dispatch itself stays in m7.
- `MinecraftServer.getConfigurationManager().playerEntityList` → `PlatformServiceHolder.get().getOnlinePlayers()`

---

### Part 25: PartyController — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/PartyController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.entity.player.EntityPlayerMP` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |
| `net.minecraft.nbt.NBTTagString` | Use `INbt.setString()` / `INbtList.appendString()` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../PartyController.java` — shadow that adds `implements IPartyHandler`

---

### Part 26: ServerCloneController — Complexity: High
**Dependencies:** None
**Estimated files:** 1 file to move
**Agent Difficulty:** CREATIVE — uses EntityList, ICommandSender, ChatComponentText

#### Human: Move this file from m7 → core/
- `noppes/npcs/controllers/ServerCloneController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.command.ICommandSender` | Remove — decouple, use String/Object params | DECOUPLE |
| `net.minecraft.entity.Entity` | `noppes.npcs.api.entity.IEntity` | EXISTS |
| `net.minecraft.entity.EntityList` | Move entity creation to m7 shadow | STAYS IN M7 |
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.nbt.NBTTagList` | `noppes.npcs.api.INbtList` | EXISTS |
| `net.minecraft.nbt.NBTTagString` | `INbtList.appendString()` | EXISTS |
| `net.minecraft.util.ChatComponentText` | `PlatformServiceHolder.get().sendMessage()` | NEW (or already added) |

#### Agent: Create these PA abstractions
*(none new — sendMessage already added in Part 21)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../ServerCloneController.java` — shadow that adds:
  - `implements ICloneHandler`
  - `EntityList.createEntityFromNBT()` calls for cloning entities
  - `ICommandSender` convenience methods

#### Notes
- Core version manages clone data (NBT save/load of clone tabs). Entity instantiation from NBT stays in m7 shadow since it requires `EntityList` (MC entity registry).

---

### Part 27: ProfileController — Complexity: Medium
**Dependencies:** None
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `kamkeel/npcs/controllers/ProfileController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.entity.player.EntityPlayerMP` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.CompressedStreamTools` | `PlatformServiceHolder.get().readCompressedNBT()` / `.writeCompressedNBT()` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../ProfileController.java` — shadow that adds `implements IProfileHandler`, EntityPlayer convenience methods

---

### Part 28: AttributeController — Complexity: Low
**Dependencies:** None
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `kamkeel/npcs/controllers/AttributeController.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- `mc1710/.../AttributeController.java` — shadow that adds `implements IAttributeHandler`

---

### Part 29: Quest Types (Batch 1) — Complexity: Medium
**Dependencies:** Part 8 (Quest data classes)
**Estimated files:** 3 files to move
**Agent Difficulty:** MECHANICAL

#### Human: Move these files from m7 → core/
- `noppes/npcs/quests/QuestDialog.java`
- `noppes/npcs/quests/QuestKill.java`
- `noppes/npcs/quests/QuestManual.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |

#### Agent: Create these PA abstractions
*(none)*

#### Agent: Create/update these m7 shadow files
- m7 shadows for each if they need EntityPlayer convenience methods

#### Notes
- All quest types extend QuestInterface — verify it's accessible (either in core from Part 8, or still in m7 as interface)
- Quest completion checking uses `EntityPlayer` → change to `IPlayer`

---

### Part 30: Quest Types (Batch 2) — Complexity: Medium
**Dependencies:** Part 8, Part 29
**Estimated files:** 2 files to move
**Agent Difficulty:** MECHANICAL

#### Human: Move these files from m7 → core/
- `noppes/npcs/quests/QuestItem.java`
- `noppes/npcs/quests/QuestLocation.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| `net.minecraft.entity.player.EntityPlayer` | `noppes.npcs.api.entity.IPlayer` | EXISTS |
| `net.minecraft.item.ItemStack` | `noppes.npcs.api.item.IItemStack` | EXISTS |
| `net.minecraft.nbt.NBTTagCompound` | `noppes.npcs.api.INbt` | EXISTS |
| `net.minecraft.util.StatCollector` | `PlatformServiceHolder.get().translate()` | NEW (or already added in Part 6) |

#### Agent: Create these PA abstractions
*(translate() already added in Part 6)*

#### Agent: Create/update these m7 shadow files
- m7 shadows for each with EntityPlayer + ItemStack convenience methods

#### Notes
- QuestItem uses `ItemStack` for item collection checks → change to `IItemStack`
- QuestLocation uses `StatCollector` for dimension name translation → use PlatformService.translate()

---

### Part 31: VersionCompatibility — Complexity: Medium
**Dependencies:** Part 1 (ICompatibilty interface)
**Estimated files:** 1 file to move
**Agent Difficulty:** MECHANICAL

#### Human: Move this file from m7 → core/
- `noppes/npcs/VersionCompatibility.java`

#### Agent: After move, fix these MC imports
| MC Import to Remove | Replace With | PA Status |
|---------------------|-------------|-----------|
| *(verify via LSP — may reference NBTTagCompound in upgrade methods)* | — | — |

#### Agent: Pre-check
Run `lsp_diagnostics` immediately after move. VersionCompatibility contains data fixup/upgrade logic. If it references MC types (NBTTagCompound) in upgrade methods, swap to INbt. If it references controller-specific types, those should be in core by now.

#### Agent: Create these PA abstractions
*(none expected)*

#### Agent: Create/update these m7 shadow files
*(none expected — this is pure data versioning logic)*

---

## Controllers That STAY in m7

For reference, these controllers are NOT migrated and remain in m7:

| Controller | Reason |
|-----------|--------|
| `ChunkController` | Uses `ForgeChunkManager` — deeply Forge-specific |
| `CustomGuiController` | Uses `EntityPlayer/EntityPlayerMP` for GUI tracking — tightly coupled to packet system |
| `LinkedNpcController` | Uses `NBTTagCompound` but manages NPC entity links — consider later |
| `RecipeController` | Uses `CraftingManager`, `InventoryCrafting` — recipe system FORBIDDEN |
| `ScriptController` | Uses script engines — FORBIDDEN per rules |
| `SyncController` (kamkeel) | Uses `Minecraft` client, `MinecraftServer`, `EntityPlayerMP`, packets — networking STAYS |
| `AbilityController` (kamkeel) | Uses `EntityLivingBase`, `Constants.NBT` — ability system deeply coupled |
| `EnergyController` (kamkeel) | Uses `Entity`, `EntityLivingBase`, `World` — spawns MC entities |
| `TelegraphController` (kamkeel) | Uses `Entity`, `EntityPlayerMP`, `World` — entity/networking coupled |

## Roles/Jobs That STAY in m7

These are NOT migrated in this plan. They are deeply coupled to `EntityNPCInterface` (which extends `EntityCreature`):

| File | Reason |
|------|--------|
| `RoleInterface.java` | Base interface — method params use `EntityNPCInterface` |
| `JobInterface.java` | Base interface — method params use `EntityNPCInterface` |
| All Role*.java | Extend RoleInterface → EntityNPCInterface dependency |
| All Job*.java | Extend JobInterface → EntityNPCInterface dependency |
| All Companion*.java | Extend CompanionJobInterface → EntityNPCInterface dependency |

**Future consideration:** Roles/Jobs CAN be migrated if `RoleInterface` and `JobInterface` are refactored to use an `ICustomNpc` parameter instead of `EntityNPCInterface`. This is a larger refactor that should happen AFTER the controller/data migration is complete and proven stable.

---

## Files NOT Covered by This Plan

These were analyzed and determined to STAY in m7. They are listed here for completeness:

### Root Package (STAYS)
CustomNpcs.java, CommonProxy.java, CreativeTabNpcs.java, EventHooks.java, ServerEventsHandler.java, ServerTickHandler.java, NPCSpawning.java, NoppesUtilPlayer.java, NoppesUtilServer.java, Server.java, TextBlock.java, NpcDamageSource.java, NpcDamageSourceInderect.java, NpcMiscInventory.java, ScriptPlayerEventHandler.java, ScriptItemEventHandler.java, ScriptForgeEventHandler.java, AnimationMixinFunctions.java, CustomItems.java, CustomTeleporter.java

### controllers/data/ (STAYS)
All Script*.java (AbilityScript, ChainedAbilityScript, DataScript, EffectScript, ForgeDataScript, GlobalNPCDataScript, LinkedItemScript, PlayerDataScript, RecipeScript, ScriptHandler, SingleScriptHandler, MultiScriptHandler), All IScript*.java interfaces, JaninoScriptHandler.java, Animation.java, AnimationData.java, BuiltInAnimation.java, DataTransform.java, MarkData.java, Party.java, PlayerData.java, All Player*Data.java, BankData.java, PlayerMail.java, CustomEffect.java, RecipeCarpentry.java, RecipeAnvil.java, RecipesDefault.java, Magic.java (already in core)

### Utilities (STAYS)
GameProfileAlt.java, Vec3NPC.java, NBTJsonUtil.java, NPCMountUtil.java, IProjectileCallback.java, MathUtil.java, AnchorPointHelper.java, AttributeAttackUtil.java, AttributeItemUtil.java, ByteBufUtils.java, NoppesStringUtils.java

### Version-Specific Directories (ALL STAY)
entity/, blocks/, tiles/, items/, client/, ai/, network/, commands/, mixin/, addon/, scripted/, config/ (except LoadConfiguration, legacy/)

---

## Execution Order Summary

```
Phase 1: Zero-MC warm-up (Parts 1-4)
  → Validates workflow, moves ~25 files with zero MC imports

Phase 2: Foundation data classes (Parts 5-12)
  → Moves critical data classes, creates PlatformService.translate()
  → Availability (Part 6) is the key gate — unblocks everything

Phase 3: Controllers (Parts 13-28)
  → Moves controllers one by one, each building on data from Phase 2
  → Creates PlatformService.sendMessage(), getOnlinePlayers(), etc.
  → Hardest: AuctionController (21), PlayerDataController (24), ServerCloneController (26)

Phase 4: Quest types + cleanup (Parts 29-31)
  → Moves quest type implementations
  → VersionCompatibility last (it touches everything)
```

**Estimated total: ~63 files migrated to core, ~10 m7 shadow files created, ~8 new PlatformService methods.**

---

## Post-Plan: What Opens Up After This

Once all Parts are complete:
1. **Roles/Jobs migration** — refactor RoleInterface/JobInterface to use ICustomNpc → then migrate role/job logic to core
2. **Config value extraction** — move config value constants to core, keep Forge Configuration loading in m7
3. **1.12.2 version leaf** — with core holding all game logic, mc1122 only needs to provide PlatformService implementation + version-specific entity/block/item/GUI code
4. **Data migration tooling** — NPC save format compatibility across versions
