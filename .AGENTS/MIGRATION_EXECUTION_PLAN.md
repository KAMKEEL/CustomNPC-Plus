# MIGRATION EXECUTION PLAN — Core Extraction Wave 1

> **Created:** 2026-03-16  
> **Status:** PLANNING (not yet executing)  
> **Scope:** Whitelisted packages → core/ via the migration toolchain  
> **Data Sources:** `mc_type_classifications_original.json` (37 curated types), `all_methods_export.json` (member data), `mc_usage_index.json` (usage index)

---

## Executive Summary

This plan extracts ~250 files from 8 whitelisted mc1710 packages into `core/`, replacing all MC/Forge type references with platform-api abstractions. It is the **first full-scale use of the migration toolchain** and is structured into 4 sequential phases:

| Phase | What | Tool | Output |
|-------|------|------|--------|
| **1** | Create PA abstractions for all MC types used in scope | `pa_batch.py` | ~20 new PA files in `platform-api/` |
| **2** | Copy whitelisted files to core/ | `Copy-Recursive.ps1` | ~150 new files in `core/` |
| **3** | Replace MC types with PA abstractions in core/ | `Symbol-Swap`, `Static-Transform`, `Nuke-Imports` | All MC imports eliminated from core/ |
| **4** | Verify, diagnose, stabilize | `Build-Report`, `migration-diff` | Clean `:core:compileJava` build |

**Estimated new tool development before execution:** 1 new Python script (`generate_pa_manifest.py`)

---

## 0. Current State Snapshot

### Already Migrated (DO NOT re-copy)

| Location | Count | Contents |
|----------|-------|----------|
| `platform-api/` | 239 files | Entity interfaces (IPlayer, IEntity, etc.), handlers, events, abilities, NBT, overlays, GUI, roles, scoreboard |
| `core/` | 141 files | 8 controllers (Faction, Transport, Tag, Magic, GlobalData, CategoryManager, APIRegistry, ServerTagMap), 30+ data classes, 40+ enums/constants, 6 utils, network enums |

### Whitelisted Migration Scope (mc1710 → core/)

| Package | Files in mc1710 | Already in core/ | Net New |
|---------|----------------|-----------------|---------|
| `kamkeel/npcs/controllers/` (+ data/) | 89 | ~14 (ability data/enums/profile/telegraph) | ~75 |
| `noppes/npcs/controllers/` (+ data/) | 98 | 38 (8 controllers + 30 data classes) | ~60 |
| `noppes/npcs/quests/` | 6 | 0 | 6 |
| `noppes/npcs/roles/` | 24 | 0 | 24 |
| `noppes/npcs/constants/` | 8 | 40 (most already migrated) | ~3 |
| `noppes/npcs/util/` | 10 (3 MC-free) | 6 | ~4 |
| `noppes/npcs/config/` | 15 | 0 | 15 |
| `kamkeel/npcs/network/` | 100+ | 9 (enums only) | **0** (all MC-dependent, skip) |
| **TOTAL** | **~350** | **~107** | **~187** |

### What's NOT in scope (explicitly excluded)

- `containers/`, `guis/`, `gui/`, `fx/` — Client GUI code
- `key/`, `model/`, `renderer/` — Client rendering
- Proxies, Event Handlers — Platform-specific lifecycle
- `kamkeel/npcs/network/` packets — All MC-dependent (100+ files)
- Ability type classes (extend MC Entity) — Stay in mc1710
- Script system — Deeply coupled, not planned

---

## 1. MC Type Classification Registry

### Authoritative Source: `mc_type_classifications_original.json`

This is the **hand-curated** classification of all 400 MC types. The auto-generated `classification_results.json` has inflated counts (46 INTERFACE vs 15, 95 SERVICE vs 12) because it lacks human judgment. **Always use the original.**

| Category | Count | Action |
|----------|-------|--------|
| EXISTING | 11 | Symbol-Swap ready — PA interfaces exist |
| INTERFACE | 15 | Need new PA interface files |
| SERVICE | 12 | Need PlatformService method additions |
| TRANSFORM | 10 | Need Static-Transform regex + support classes |
| SUPPRESS | 352 | Never abstract — code stays in mc1710 |

### 1A. EXISTING Types (11) — Symbol-Swap Ready

These already have PA abstractions. Just run Symbol-Swap.

| MC Type | PA Type | PA Package | Imports |
|---------|---------|------------|---------|
| `NBTTagCompound` | `INbt` | `noppes.npcs.api` | 593 |
| `EntityPlayer` | `IPlayer` | `noppes.npcs.api.entity` | 551 |
| `EntityPlayerMP` | `IPlayer` | `noppes.npcs.api.entity` | 304 |
| `Entity` | `IEntity` | `noppes.npcs.api.entity` | 262 |
| `ItemStack` | `IItemStack` | `noppes.npcs.api.item` | 228 |
| `EntityLivingBase` | `IEntityLivingBase` | `noppes.npcs.api.entity` | 201 |
| `World` | `IWorld` | `noppes.npcs.api` | 172 |
| `NBTTagList` | `INbtList` | `noppes.npcs.api` | 111 |
| `DamageSource` | `IDamageSource` | `noppes.npcs.api` | 36 |
| `WorldServer` | `IWorld` | `noppes.npcs.api` | 18 |
| `EntityLiving` | `IEntityLiving` | `noppes.npcs.api.entity` | 18 |

### 1B. INTERFACE Types (15) — Need PA Creation

New PA interfaces to create in `platform-api/`. Package rule: `net.minecraft.X` → `common.minecraft.X`, `net.minecraftforge.X` → `common.minecraftforge.X`, `cpw.mods.X` → `common.minecraftforge.X`.

| MC Type | PA Name | New Package | Imports | Priority for Scope |
|---------|---------|-------------|---------|-------------------|
| `ResourceLocation` | `IResourceLocation` | `common.minecraft.util` | 145 | **HIGH** — controllers, config |
| `ICommandSender` | `ICommandSender` | `common.minecraft.command` | 47 | **HIGH** — controllers |
| `ChatComponentText` | `ITextComponent` | `common.minecraft.util` | 20 | **HIGH** — controllers (messages) |
| `ChatComponentTranslation` | `ITextComponent` | `common.minecraft.util` | 24 | **HIGH** — controllers (messages) |
| `IChatComponent` | `ITextComponent` | `common.minecraft.util` | 10 | **HIGH** — base type |
| `NBTBase` | `INbtBase` | `common.minecraft.nbt` | 13 | **HIGH** — data classes |
| `Configuration` | `IConfiguration` | `common.minecraftforge.common.config` | 9 | **HIGH** — config system |
| `Property` | `IConfigProperty` | `common.minecraftforge.common.config` | 7 | **HIGH** — config system |
| `AxisAlignedBB` | `IBoundingBox` | `common.minecraft.util` | 59 | **MEDIUM** — some controllers |
| `Vec3` | `IVector3` | `common.minecraft.util` | 43 | **MEDIUM** — some controllers |
| `MovingObjectPosition` | `IRayTraceResult` | `common.minecraft.util` | 15 | **LOW** — mostly abilities |
| `ChatStyle` | `ITextStyle` | `common.minecraft.util` | 3 | **LOW** — text formatting |
| `Potion` | `IPotionType` | `common.minecraft.potion` | 7 | **MEDIUM** — effects system |
| `PotionEffect` | `IPotionEffect` | `common.minecraft.potion` | 6 | **MEDIUM** — effects system |
| `Explosion` | `IExplosion` | `common.minecraft.world` | 3 | **LOW** — abilities only |

**Filtering required**: Run `type-usage <Type> --scope` on each to confirm they're actually used in the whitelisted packages before creating PAs. Types with LOW scope relevance may be deferred.

### 1C. SERVICE Types (12) — PlatformService Method Additions

These are static utilities. Their methods get added to `PlatformService` (or `NBTIO`). No separate files needed — just interface method additions.

| MC Type | Routes To | Imports | Key Methods | Scope Relevance |
|---------|-----------|---------|-------------|-----------------|
| `StatCollector` | `PlatformService` | 106 | `translateToLocal()`, `translateToLocalFormatted()` | **HIGH** |
| `Item` | `PlatformService` | 72 | `getItemById()`, `getIdFromItem()` | **MEDIUM** |
| `MinecraftServer` | `PlatformService` | 26 | `getServer()`, `isDedicatedServer()`, `worldServerForDimension()` | **HIGH** |
| `EntityList` | `PlatformService` | 25 | `getEntityString()`, `createEntityByName()` | **MEDIUM** |
| `FMLCommonHandler` | `PlatformService` | 25 | `getSide()`, `getEffectiveSide()` | **HIGH** |
| `CompressedStreamTools` | `NBTIO` | 24 | `readCompressed()`, `writeCompressed()` | **HIGH** |
| `CommandBase` | `PlatformService` | 14 | `parseDouble()`, `parseInt()` | **LOW** |
| `Loader` | `PlatformService` | 11 | `isModLoaded()` | **HIGH** |
| `I18n` | `PlatformService` | 9 | `format()` | **LOW** (client) |
| `DimensionManager` | `PlatformService` | 2 | `getWorld()` | **LOW** |
| `PlayerSelector` | `PlatformService` | 2 | `matchPlayers()` | **LOW** |
| `NBTUtil` | `NBTIO` | 1 | GameProfile NBT | **LOW** |

### 1D. TRANSFORM Types (10) — Static-Transform Patterns

These get handled by regex replacement. Some need support classes created in `core/`.

| MC Type | Transform Target | Imports | Support Class Needed |
|---------|-----------------|---------|---------------------|
| `Side` + `SideOnly` | `@ClientOnly`/`@ServerOnly` | 800 | Already exist in platform-api |
| `MathHelper` | `ValueUtil.*` / `Math.*` | 98 | `ValueUtil` already in core/ |
| `EnumChatFormatting` | `TextFormatting.*` | 19 | **YES** — `TextFormatting` enum in core/ |
| `Constants` (Forge NBT) | `NbtConstants.*` | 9 | **YES** — `NbtConstants` class in core/ |
| `CommandException` | Same name, new package | 31 | **YES** — `CommandException` in platform-api |
| `SharedMonsterAttributes` | `Attributes.*` | 10 | **YES** — `Attributes` constants in core/ |
| `EnumFacing` | `Direction.*` | 3 | **YES** — `Direction` enum in core/ |
| `ChunkCoordinates` | `BlockPos`/inline | 4 | Uses existing `IPos` |
| `EnumAction` | `ItemAction.*` | 11 | **YES** — `ItemAction` enum in core/ |

**Support classes to create (6):** TextFormatting, NbtConstants, CommandException, Attributes, Direction, ItemAction

---

## 2. Phase 1 — PA Abstraction Creation

### Goal
Create ALL required PA interfaces and support classes BEFORE copying any files. This ensures Symbol-Swap has targets.

### Step 1.1: Scope Verification (NEW TOOL)

Before creating PAs, verify which INTERFACE/SERVICE types are actually used in the whitelisted packages. This prevents creating unused abstractions.

```bash
# For each INTERFACE type, check if it's used in scope
python tools/migration/analyze.py type-usage ResourceLocation --scope noppes/npcs/controllers/ --json
python tools/migration/analyze.py type-usage ResourceLocation --scope noppes/npcs/config/ --json
python tools/migration/analyze.py type-usage ResourceLocation --scope noppes/npcs/roles/ --json
# ... repeat for all types × all packages
```

**⚠️ TOOLING GAP:** This is manual and slow. We need `generate_pa_manifest.py` (see Section 6).

### Step 1.2: Create INTERFACE PA Files

Single `pa_batch.py` manifest with ALL INTERFACE PA files. Package mapping:
- `net.minecraft.*` → `common.minecraft.*`
- `net.minecraftforge.*` → `common.minecraftforge.*`  
- `cpw.mods.*` → `common.minecraftforge.*`

**Root:** `platform-api/src/main/java`

**Manifest structure (example entries):**
```json
{
  "root": "platform-api/src/main/java",
  "files": [
    {
      "content": "package common.minecraft.util;\n\npublic interface IResourceLocation {\n    String getResourceDomain();\n    String getResourcePath();\n    String toString();\n}\n"
    },
    {
      "content": "package common.minecraft.util;\n\npublic interface ITextComponent {\n    String getText();\n    String getFormattedText();\n    String getUnformattedText();\n    ITextComponent appendSibling(ITextComponent sibling);\n    ITextComponent setChatStyle(ITextStyle style);\n}\n"
    },
    {
      "content": "package common.minecraft.nbt;\n\npublic interface INbtBase {\n    byte getId();\n    INbtBase copy();\n}\n"
    },
    {
      "content": "package common.minecraftforge.common.config;\n\npublic interface IConfiguration {\n    IConfigProperty get(String category, String key, String defaultValue, String comment);\n    IConfigProperty get(String category, String key, int defaultValue, String comment);\n    IConfigProperty get(String category, String key, boolean defaultValue, String comment);\n    IConfigProperty get(String category, String key, double defaultValue, String comment);\n    void save();\n    void load();\n    boolean hasKey(String category, String key);\n    boolean hasCategory(String category);\n}\n"
    }
  ]
}
```

**Method content source:** `all_methods_export.json` — this file has EVERY method/field/static for each MC type with usage counts. The manifest generator reads this to include only methods with usage > 0.

### Step 1.3: Add SERVICE Methods to PlatformService

For SERVICE types, we add methods to the existing `PlatformService` interface. Use `pa_editor.py` to insert method declarations.

**Methods to add to `kamkeel/npcs/platform/PlatformService.java`:**

```java
// StatCollector (106 imports)
String translateToLocal(String key);
String translateToLocalFormatted(String key, Object... args);

// MinecraftServer (26 imports)  
boolean isDedicatedServer();  // may already exist
Object getServer();
IWorld getWorldForDimension(int dimensionId);

// FMLCommonHandler (25 imports)
boolean isPhysicalClient();  // already exists
boolean isLogicalClient();

// Loader (11 imports)
boolean isModLoaded(String modId);

// Item registry (72 imports)
IItemStack getItemById(int id);
int getIdFromItem(IItemStack item);

// EntityList (25 imports)
String getEntityRegistryName(IEntity entity);
IEntity createEntityByName(String name, IWorld world);
```

**Methods to add to `noppes/npcs/platform/nbt/NBTIO.java`:**

```java
// CompressedStreamTools (24 imports) — may already exist
INbt readCompressed(java.io.InputStream stream);
void writeCompressed(INbt nbt, java.io.OutputStream stream);
```

### Step 1.4: Create TRANSFORM Support Classes

These go into `core/src/main/java/` (they're platform-independent).

```json
{
  "root": "core/src/main/java",
  "files": [
    {
      "content": "package noppes.npcs.constants;\n\npublic class TextFormatting {\n    public static final String BLACK = \"\\u00a70\";\n    public static final String DARK_BLUE = \"\\u00a71\";\n    public static final String DARK_GREEN = \"\\u00a72\";\n    public static final String DARK_AQUA = \"\\u00a73\";\n    public static final String DARK_RED = \"\\u00a74\";\n    public static final String DARK_PURPLE = \"\\u00a75\";\n    public static final String GOLD = \"\\u00a76\";\n    public static final String GRAY = \"\\u00a77\";\n    public static final String DARK_GRAY = \"\\u00a78\";\n    public static final String BLUE = \"\\u00a79\";\n    public static final String GREEN = \"\\u00a7a\";\n    public static final String AQUA = \"\\u00a7b\";\n    public static final String RED = \"\\u00a7c\";\n    public static final String LIGHT_PURPLE = \"\\u00a7d\";\n    public static final String YELLOW = \"\\u00a7e\";\n    public static final String WHITE = \"\\u00a7f\";\n    public static final String OBFUSCATED = \"\\u00a7k\";\n    public static final String BOLD = \"\\u00a7l\";\n    public static final String STRIKETHROUGH = \"\\u00a7m\";\n    public static final String UNDERLINE = \"\\u00a7n\";\n    public static final String ITALIC = \"\\u00a7o\";\n    public static final String RESET = \"\\u00a7r\";\n}\n"
    },
    {
      "content": "package noppes.npcs.constants;\n\npublic class NbtConstants {\n    public static final int TAG_END = 0;\n    public static final int TAG_BYTE = 1;\n    public static final int TAG_SHORT = 2;\n    public static final int TAG_INT = 3;\n    public static final int TAG_LONG = 4;\n    public static final int TAG_FLOAT = 5;\n    public static final int TAG_DOUBLE = 6;\n    public static final int TAG_BYTE_ARRAY = 7;\n    public static final int TAG_STRING = 8;\n    public static final int TAG_LIST = 9;\n    public static final int TAG_COMPOUND = 10;\n    public static final int TAG_INT_ARRAY = 11;\n}\n"
    }
  ]
}
```

Additional support classes: `CommandException`, `Attributes`, `Direction`, `ItemAction` — all simple enums/classes with no MC dependencies.

### Step 1.5: Execution Sequence

```
1. Write PA INTERFACE manifest → pa_batch.py --dry-run → pa_batch.py --force
2. Edit PlatformService with SERVICE methods → pa_editor.py --force
3. Write TRANSFORM support class manifest → pa_batch.py --force
4. Verify: ls platform-api/src/main/java/common/ — confirm structure
```

---

## 3. Phase 2 — Core File Copy

### Goal
Copy all files from whitelisted mc1710 packages to core/, following import dependencies within scope.

### Step 2.1: Build the Sources List

Every `.java` file in the whitelisted packages that is NOT already in core/.

**Initial Sources** (explicit files to copy — the roots of the recursion):

```powershell
# noppes/npcs/controllers/ — files NOT already in core/
$controllerSources = @(
    'noppes/npcs/controllers/QuestController.java',
    'noppes/npcs/controllers/PlayerQuestController.java',
    'noppes/npcs/controllers/PlayerDataController.java',
    'noppes/npcs/controllers/DialogController.java',
    'noppes/npcs/controllers/LinkedItemController.java',
    'noppes/npcs/controllers/LinkedNpcController.java',
    'noppes/npcs/controllers/PartyController.java',
    'noppes/npcs/controllers/AuctionController.java',
    'noppes/npcs/controllers/CustomEffectController.java',
    'noppes/npcs/controllers/CustomGuiController.java',
    'noppes/npcs/controllers/BankController.java',
    'noppes/npcs/controllers/ChunkController.java',
    'noppes/npcs/controllers/RecipeController.java',
    'noppes/npcs/controllers/SpawnController.java',
    'noppes/npcs/controllers/ServerCloneController.java',
    'noppes/npcs/controllers/ScriptController.java',
    'noppes/npcs/controllers/ScriptHookController.java',
    'noppes/npcs/controllers/AnimationController.java',
    'noppes/npcs/controllers/MarketRegistry.java',
    'noppes/npcs/controllers/HookDefinition.java'
    # ... + ALL data/* files not already in core/
)

# noppes/npcs/quests/ — all 6 files
$questSources = @(
    'noppes/npcs/quests/QuestItem.java',
    'noppes/npcs/quests/QuestKill.java',
    'noppes/npcs/quests/QuestLocation.java',
    'noppes/npcs/quests/QuestDialog.java',
    'noppes/npcs/quests/QuestManual.java',
    'noppes/npcs/quests/QuestInterface.java'
)

# noppes/npcs/roles/ — all 24 files
$roleSources = @(
    'noppes/npcs/roles/RoleTrader.java',
    'noppes/npcs/roles/RoleBank.java',
    'noppes/npcs/roles/RoleFollower.java',
    'noppes/npcs/roles/RoleCompanion.java',
    # ... all 24 files
)

# noppes/npcs/config/ — all 15 files
$configSources = @(
    'noppes/npcs/config/ConfigMain.java',
    'noppes/npcs/config/ConfigClient.java',
    'noppes/npcs/config/ConfigScript.java',
    'noppes/npcs/config/ConfigMarket.java',
    'noppes/npcs/config/ConfigEnergy.java',
    'noppes/npcs/config/ConfigItem.java',
    'noppes/npcs/config/ConfigDebug.java',
    'noppes/npcs/config/ConfigExperimental.java',
    'noppes/npcs/config/ConfigMixin.java',
    'noppes/npcs/config/LoadConfiguration.java',
    'noppes/npcs/config/StringCache.java',
    'noppes/npcs/config/GlyphCache.java',
    'noppes/npcs/config/legacy/LegacyLoader.java',
    'noppes/npcs/config/legacy/LegacyConfig.java',
    'noppes/npcs/config/legacy/ConfigProp.java'
)

# MC-free util files only
$utilSources = @(
    'noppes/npcs/util/GameProfileAlt.java',
    'noppes/npcs/util/JsonException.java',
    'noppes/npcs/util/ScriptToStringHelper.java'
)

# kamkeel/npcs/controllers/ — files NOT already in core/
$kamkeelSources = @(
    'kamkeel/npcs/controllers/AbilityController.java',
    'kamkeel/npcs/controllers/EnergyController.java',
    'kamkeel/npcs/controllers/ProfileController.java',
    'kamkeel/npcs/controllers/AttributeController.java',
    'kamkeel/npcs/controllers/TelegraphController.java',
    'kamkeel/npcs/controllers/SyncController.java'
    # ... + data/* files not already in core/
)
```

### Step 2.2: AllowPaths & AllowRecursePaths

```powershell
$AllowPaths = @(
    'noppes/npcs/controllers/',
    'noppes/npcs/quests/',
    'noppes/npcs/roles/',
    'noppes/npcs/constants/',
    'noppes/npcs/util/',
    'noppes/npcs/config/',
    'kamkeel/npcs/controllers/',
    'kamkeel/npcs/util/',
    'kamkeel/npcs/config/'
)

# Only recurse into these — prevent explosion into entity/client/network
$AllowRecursePaths = @(
    'noppes/npcs/controllers/',
    'noppes/npcs/quests/',
    'noppes/npcs/roles/',
    'noppes/npcs/constants/',
    'noppes/npcs/config/',
    'kamkeel/npcs/controllers/'
)
# util/ is in AllowPaths but NOT AllowRecursePaths — files get copied but imports aren't chased
```

### Step 2.3: Execute

**IMPORTANT:** `McRoot` defaults to `mc1710/src/main/java` but current code lives at `src/main/java`. Override required.

```powershell
Copy-Recursive `
    -Sources ($controllerSources + $questSources + $roleSources + $configSources + $utilSources + $kamkeelSources) `
    -AllowPaths $AllowPaths `
    -AllowRecursePaths $AllowRecursePaths `
    -McRoot 'src/main/java'
```

**Expected output:** ~150-187 files copied to `core/src/main/java/`.

### Step 2.4: What Copy-Recursive Will Skip

Files already in core/ will be reported as EXISTS (not re-copied). Files outside AllowPaths will be reported as SKIPPED. This is correct behavior.

---

## 4. Phase 3 — Symbol Replacement

### Goal
Replace every MC/Forge type reference in core/ with PA abstractions.

### Step 3.1: Nuke-Imports (first)

Remove all MC/Forge/platform-specific imports from core/.

```powershell
Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes @(
    'net.minecraft.',
    'cpw.mods.',
    'net.minecraftforge.',
    'noppes.npcs.entity.',
    'noppes.npcs.client.',
    'noppes.npcs.mixin.',
    'kamkeel.npcs.network.',
    'kamkeel.npcs.entity.',
    'noppes.npcs.containers.',
    'noppes.npcs.scripted.',
    'noppes.npcs.janino.',
    'noppes.npcs.wrapper.',
    'noppes.npcs.blocks.',
    'noppes.npcs.items.',
    'noppes.npcs.ai.'
)
```

### Step 3.2: Symbol-Swap — EXISTING Types (11)

```powershell
Symbol-Swap -Directory 'core/src/main/java' -Swaps @(
    @('NBTTagCompound',     'INbt',              'net.minecraft.nbt.NBTTagCompound',            'noppes.npcs.api.INbt'),
    @('NBTTagList',         'INbtList',           'net.minecraft.nbt.NBTTagList',                'noppes.npcs.api.INbtList'),
    @('EntityPlayer',       'IPlayer',            'net.minecraft.entity.player.EntityPlayer',    'noppes.npcs.api.entity.IPlayer'),
    @('EntityPlayerMP',     'IPlayer',            'net.minecraft.entity.player.EntityPlayerMP',  'noppes.npcs.api.entity.IPlayer'),
    @('Entity',             'IEntity',            'net.minecraft.entity.Entity',                 'noppes.npcs.api.entity.IEntity'),
    @('ItemStack',          'IItemStack',         'net.minecraft.item.ItemStack',                'noppes.npcs.api.item.IItemStack'),
    @('EntityLivingBase',   'IEntityLivingBase',  'net.minecraft.entity.EntityLivingBase',       'noppes.npcs.api.entity.IEntityLivingBase'),
    @('World',              'IWorld',             'net.minecraft.world.World',                   'noppes.npcs.api.IWorld'),
    @('WorldServer',        'IWorld',             'net.minecraft.world.WorldServer',             'noppes.npcs.api.IWorld'),
    @('DamageSource',       'IDamageSource',      'net.minecraft.util.DamageSource',             'noppes.npcs.api.IDamageSource'),
    @('EntityLiving',       'IEntityLiving',      'net.minecraft.entity.EntityLiving',           'noppes.npcs.api.entity.IEntityLiving')
)
```

### Step 3.3: Symbol-Swap — NEW INTERFACE Types

```powershell
Symbol-Swap -Directory 'core/src/main/java' -Swaps @(
    @('ResourceLocation',         'IResourceLocation',  'net.minecraft.util.ResourceLocation',                    'common.minecraft.util.IResourceLocation'),
    @('ICommandSender',           'ICommandSender',     'net.minecraft.command.ICommandSender',                   'common.minecraft.command.ICommandSender'),
    @('ChatComponentText',        'ITextComponent',     'net.minecraft.util.ChatComponentText',                   'common.minecraft.util.ITextComponent'),
    @('ChatComponentTranslation', 'ITextComponent',     'net.minecraft.util.ChatComponentTranslation',            'common.minecraft.util.ITextComponent'),
    @('IChatComponent',           'ITextComponent',     'net.minecraft.util.IChatComponent',                      'common.minecraft.util.ITextComponent'),
    @('ChatStyle',                'ITextStyle',         'net.minecraft.util.ChatStyle',                           'common.minecraft.util.ITextStyle'),
    @('NBTBase',                  'INbtBase',           'net.minecraft.nbt.NBTBase',                              'common.minecraft.nbt.INbtBase'),
    @('Configuration',            'IConfiguration',     'net.minecraftforge.common.config.Configuration',         'common.minecraftforge.common.config.IConfiguration'),
    @('Property',                 'IConfigProperty',    'net.minecraftforge.common.config.Property',              'common.minecraftforge.common.config.IConfigProperty'),
    @('AxisAlignedBB',            'IBoundingBox',       'net.minecraft.util.AxisAlignedBB',                       'common.minecraft.util.IBoundingBox'),
    @('Vec3',                     'IVector3',           'net.minecraft.util.Vec3',                                'common.minecraft.util.IVector3'),
    @('MovingObjectPosition',     'IRayTraceResult',    'net.minecraft.util.MovingObjectPosition',                'common.minecraft.util.IRayTraceResult'),
    @('Potion',                   'IPotionType',        'net.minecraft.potion.Potion',                            'common.minecraft.potion.IPotionType'),
    @('PotionEffect',             'IPotionEffect',      'net.minecraft.potion.PotionEffect',                      'common.minecraft.potion.IPotionEffect'),
    @('Explosion',                'IExplosion',         'net.minecraft.world.Explosion',                          'common.minecraft.world.IExplosion')
)
```

### Step 3.4: Static-Transform — SERVICE Rerouting

```powershell
Static-Transform -Directory 'core/src/main/java' -Transforms @(
    # NBT constructors
    @('new\s+NBTTagCompound\s*\(\)',                                                           'NBT.compound()'),
    @('new\s+NBTTagList\s*\(\)',                                                               'NBT.list()'),
    
    # CompressedStreamTools → NBTIO
    @('CompressedStreamTools\.readCompressed\s*\(([^)]+)\)',                                    'NBTIO.readCompressed($1)'),
    @('CompressedStreamTools\.writeCompressed\s*\(([^,]+),\s*([^)]+)\)',                        'NBTIO.writeCompressed($1, $2)'),
    
    # StatCollector → PlatformServiceHolder
    @('StatCollector\.translateToLocal\s*\(([^)]+)\)',                                          'PlatformServiceHolder.get().translateToLocal($1)'),
    @('StatCollector\.translateToLocalFormatted\s*\(([^)]+)\)',                                 'PlatformServiceHolder.get().translateToLocalFormatted($1)'),
    
    # MinecraftServer
    @('MinecraftServer\.getServer\s*\(\)',                                                      'PlatformServiceHolder.get().getServer()'),
    
    # FMLCommonHandler
    @('FMLCommonHandler\.instance\(\)\.getSide\(\)\s*==\s*Side\.CLIENT',                        'PlatformServiceHolder.get().isPhysicalClient()'),
    @('FMLCommonHandler\.instance\(\)\.getEffectiveSide\(\)\s*==\s*Side\.CLIENT',               'PlatformServiceHolder.get().isLogicalClient()'),
    
    # Loader
    @('Loader\.isModLoaded\s*\(([^)]+)\)',                                                     'PlatformServiceHolder.get().isModLoaded($1)'),
    
    # MathHelper
    @('MathHelper\.clamp_int\s*\(',                                                            'ValueUtil.clampInt('),
    @('MathHelper\.clamp_float\s*\(',                                                          'ValueUtil.clampFloat('),
    @('MathHelper\.clamp_double\s*\(',                                                         'ValueUtil.clampDouble('),
    @('MathHelper\.sqrt_double\s*\(',                                                          'Math.sqrt('),
    @('MathHelper\.sqrt_float\s*\(',                                                           '(float)Math.sqrt('),
    @('MathHelper\.floor_double\s*\(',                                                         'ValueUtil.floorDouble('),
    @('MathHelper\.floor_float\s*\(',                                                          'ValueUtil.floorFloat('),
    @('MathHelper\.sin\s*\(',                                                                  '(float)Math.sin('),
    @('MathHelper\.cos\s*\(',                                                                  '(float)Math.cos('),
    @('MathHelper\.abs\s*\(',                                                                  'Math.abs('),
    
    # EnumChatFormatting
    @('EnumChatFormatting\.',                                                                   'TextFormatting.'),
    
    # Constants.NBT
    @('Constants\.NBT\.',                                                                      'NbtConstants.'),
    
    # SharedMonsterAttributes
    @('SharedMonsterAttributes\.maxHealth',                                                     'Attributes.MAX_HEALTH'),
    @('SharedMonsterAttributes\.movementSpeed',                                                 'Attributes.MOVEMENT_SPEED'),
    @('SharedMonsterAttributes\.attackDamage',                                                  'Attributes.ATTACK_DAMAGE'),
    @('SharedMonsterAttributes\.followRange',                                                   'Attributes.FOLLOW_RANGE'),
    @('SharedMonsterAttributes\.knockbackResistance',                                           'Attributes.KNOCKBACK_RESISTANCE'),
    
    # Annotations
    @('@SideOnly\s*\(\s*Side\.CLIENT\s*\)',                                                     '@ClientOnly'),
    @('@SideOnly\s*\(\s*Side\.SERVER\s*\)',                                                     '@ServerOnly')
)
```

### Step 3.5: Generate-Stubs

Classes that will be referenced but aren't in scope. These are TEMPORARY placeholders.

```powershell
Generate-Stubs -Stubs @(
    @{ pkg='noppes.npcs'; name='CustomNpcs'; type='class';
       body='    public static java.io.File Dir = new java.io.File(".");
    public static int DefaultInteractLine = 0;
    public static java.util.logging.Logger logger;' },
    @{ pkg='noppes.npcs'; name='EventHooks'; type='class';
       body='    // STUB - event dispatch stays in mc1710' },
    @{ pkg='noppes.npcs'; name='LogWriter'; type='class';
       body='    public static void info(String msg) {}
    public static void warn(String msg) {}
    public static void error(String msg) {}
    public static void error(String msg, Throwable e) {}' },
    @{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class';
       body='    // STUB - sync stays in mc1710' },
    @{ pkg='noppes.npcs.entity'; name='EntityNPCInterface'; type='class';
       body='    // STUB - entity stays in mc1710' }
)
```

### Step 3.6: Dedupe-Imports

```powershell
Dedupe-Imports -Directory 'core/src/main/java'
```

### Step 3.7: All-in-One Wave Script

**All steps 3.1–3.6 go into a single `.ps1` file:**

```
tools/migration/wave1_core_extraction.ps1
```

Execute:
```bash
powershell -ExecutionPolicy Bypass -File "tools/migration/wave1_core_extraction.ps1"
```

---

## 5. Phase 4 — Verification & Stabilization

### Goal
Get `:core:compileJava` to pass. Diagnose and fix all errors categorically.

### Step 4.1: Initial Build-Report

```powershell
Build-Report -GradleTask ':core:compileJava' -MaxSamples 10
```

**Expected error categories and fixes:**

| Category | Likely Cause | Fix |
|----------|-------------|-----|
| `MISSING_PACKAGE` | Leftover MC import Nuke-Imports missed | Re-run Nuke with expanded prefixes |
| `MISSING_CLASS` | Transitive dep not in scope | Generate-Stubs |
| `MISSING_METHOD` | PA interface missing method | Add method to PA interface via pa_editor |
| `TYPE_MISMATCH` | Wrong PA type used | Check Symbol-Swap mapping |
| `BAD_ARGS` | Method signature changed | Static-Transform or manual fix |
| `ABSTRACT_VIOLATION` | Class implements MC interface | Remove `implements` or stub |

### Step 4.2: Batch migration-diff

Run `migration-diff` on ALL copied files to verify completeness:

```bash
# For each migrated file:
python tools/migration/analyze.py migration-diff \
    --old src/main/java/noppes/npcs/controllers/QuestController.java \
    --new core/src/main/java/noppes/npcs/controllers/QuestController.java \
    --json
```

**⚠️ TOOLING GAP:** No batch migration-diff exists. Need to script a loop or build `batch_migration_diff.py`.

### Step 4.3: Store Diagnostic Cache

All outputs cached in JSON for easy navigation:

```bash
# Build report
python tools/migration/analyze.py coverage-report --json > tools/migration/_wave1_coverage.json

# All migration-diff results
python tools/migration/_batch_migration_diff.py --output tools/migration/_wave1_diffs.json
```

### Step 4.4: Fix Cycle

```
WHILE build fails:
    1. Build-Report → categorize errors
    2. MISSING_PACKAGE → Nuke-Imports (add new prefixes)
    3. MISSING_CLASS → Generate-Stubs OR Create-And-Propagate (if PA needed)
    4. MISSING_METHOD → pa_editor (add to PA) OR Static-Transform (rename)
    5. TYPE_MISMATCH → Symbol-Swap (fix mapping) OR manual cast
    6. Dedupe-Imports
    7. Build-Report → verify error count dropped
```

### Step 4.5: Final Verification

```bash
# Build must pass
./gradlew :core:compileJava

# Coverage should improve significantly
python tools/migration/analyze.py coverage-report --detailed

# mc1710 build must also still pass
./gradlew build
```

---

## 6. Missing Tools — Build Before Execution

### 6A. `generate_pa_manifest.py` (HIGH PRIORITY)

**Purpose:** Bridge the gap between analysis data → executable pa_batch manifest.

**Input:**
- `mc_type_classifications_original.json` — which types need PAs
- `all_methods_export.json` — what methods each type has
- `mc_usage_index.json` — which files use which types
- Package scope filter — list of package prefixes to restrict to

**Output:**
- `pa_batch` manifest JSON with complete Java interface/class content
- Swap table (old → new symbol mappings) for Symbol-Swap
- Static-Transform pattern list for SERVICE types

**Key behavior:**
1. Read classifications, filter to INTERFACE types
2. For each INTERFACE type, filter to types used in specified packages (via usage index)
3. For each filtered type, read methods from `all_methods_export.json`
4. Generate Java interface content with correct package mapping:
   - `net.minecraft.X` → `common.minecraft.X`
   - `net.minecraftforge.X` → `common.minecraftforge.X`
   - `cpw.mods.X` → `common.minecraftforge.X`
5. Output pa_batch manifest JSON

**Estimated development time:** 2-3 hours

**Why this is critical:** Without it, composing the manifest for ~15-20 PA interfaces is manual and error-prone. Each interface needs the RIGHT methods (from all_methods_export), the RIGHT package, and the RIGHT imports. This tool automates the entire process.

### 6B. `batch_migration_diff.py` (MEDIUM PRIORITY)

**Purpose:** Run `migration-diff` on ALL migrated files in one command, output unified JSON.

**Input:**
- Source root (mc1710 path)
- Target root (core path)  
- List of migrated file paths (or auto-discover by comparing directories)

**Output:**
- JSON with per-file PASS/WARN/FAIL verdicts
- Aggregate statistics
- Cached for agent navigation

**Estimated development time:** 1-2 hours

### 6C. `generate_wave_script.py` (LOW PRIORITY — nice to have)

**Purpose:** Auto-generate the complete `.ps1` wave script from classification data.

**Input:**
- Type classifications
- Package scope
- Phase parameters

**Output:**
- Complete `.ps1` file with Copy-Recursive + Nuke + Swap + Transform + Stubs + Dedupe + Build

**Estimated development time:** 2-3 hours

**Why low priority:** The wave script can be composed manually from the plan. Automation saves time on FUTURE waves but is not blocking for Wave 1.

---

## 7. Execution Checklist

### Pre-Flight

- [ ] **Tool development:** Build `generate_pa_manifest.py`
- [ ] **Scope verification:** Run `type-usage` for each INTERFACE/SERVICE type against whitelisted packages
- [ ] **Confirm existing PAs:** Verify all 11 EXISTING types have complete PA interfaces (run `surface-miner --compare-existing`)
- [ ] **Git clean state:** Ensure working tree is clean before starting

### Phase 1 Execution

- [ ] Generate PA manifest via `generate_pa_manifest.py`
- [ ] Dry-run: `pa_batch.py manifest.json --dry-run`
- [ ] Create PAs: `pa_batch.py manifest.json --force`
- [ ] Edit PlatformService with SERVICE methods: `pa_editor.py`
- [ ] Create TRANSFORM support classes: `pa_batch.py`
- [ ] Verify: `ls platform-api/src/main/java/common/`

### Phase 2 Execution

- [ ] Write wave script with Copy-Recursive params
- [ ] Execute: Copy ~150-187 files to core/
- [ ] Verify: count files in core/ (should increase by ~150)

### Phase 3 Execution

- [ ] Execute wave script (Nuke + Swap + Transform + Stubs + Dedupe)
- [ ] Initial Build-Report

### Phase 4 Execution

- [ ] Fix cycle until `:core:compileJava` passes
- [ ] Run batch migration-diff
- [ ] Store diagnostic cache
- [ ] Run `coverage-report` — measure improvement
- [ ] Verify mc1710 build still passes: `./gradlew build`
- [ ] **Human commits** (agent does NOT commit)

---

## 8. Risk Assessment

| Risk | Impact | Mitigation |
|------|--------|------------|
| Script system files copied (deeply MC-coupled) | Build explosion | AllowRecursePaths prevents recursion into script packages |
| Symbol-Swap replaces too aggressively (e.g., `Entity` → `IEntity` in non-type contexts) | Broken code | Word-boundary regex in Symbol-Swap handles this. Verify with migration-diff. |
| Some controllers have too many MC deps to clean up | Phase 3 stalls | Generate-Stubs for anything not in scope. Fix cycle handles residuals. |
| Config system needs Forge Configuration deeply | Config files can't compile | IConfiguration PA must match Forge API surface closely. Use surface-miner to verify. |
| Role/Quest files reference EntityNPCInterface | Missing stubs | Generate-Stubs for EntityNPCInterface. Core version is a placeholder. |
| Copy-Recursive pulls in too many transitive deps | Scope explosion | AllowPaths + AllowRecursePaths constrain the recursion tightly |

---

## 9. Success Criteria

| Metric | Before | Target |
|--------|--------|--------|
| Files in core/ | 141 | ~290-330 |
| PA type coverage (types with abstractions) | 11/400 (2.8%) | ~30/400 (7.5%) |
| PA import coverage (imports to abstracted types) | 37.1% | ~55-60% |
| `:core:compileJava` | PASS | PASS |
| `:build` (mc1710) | PASS | PASS |

---

## Appendix A: Complete Swap Table

This is the full `-Swaps` parameter for Symbol-Swap, combining EXISTING + new INTERFACE types.

```powershell
$AllSwaps = @(
    # === EXISTING (11) ===
    @('NBTTagCompound',     'INbt',              'net.minecraft.nbt.NBTTagCompound',            'noppes.npcs.api.INbt'),
    @('NBTTagList',         'INbtList',           'net.minecraft.nbt.NBTTagList',                'noppes.npcs.api.INbtList'),
    @('EntityPlayer',       'IPlayer',            'net.minecraft.entity.player.EntityPlayer',    'noppes.npcs.api.entity.IPlayer'),
    @('EntityPlayerMP',     'IPlayer',            'net.minecraft.entity.player.EntityPlayerMP',  'noppes.npcs.api.entity.IPlayer'),
    @('Entity',             'IEntity',            'net.minecraft.entity.Entity',                 'noppes.npcs.api.entity.IEntity'),
    @('ItemStack',          'IItemStack',         'net.minecraft.item.ItemStack',                'noppes.npcs.api.item.IItemStack'),
    @('EntityLivingBase',   'IEntityLivingBase',  'net.minecraft.entity.EntityLivingBase',       'noppes.npcs.api.entity.IEntityLivingBase'),
    @('World',              'IWorld',             'net.minecraft.world.World',                   'noppes.npcs.api.IWorld'),
    @('WorldServer',        'IWorld',             'net.minecraft.world.WorldServer',             'noppes.npcs.api.IWorld'),
    @('DamageSource',       'IDamageSource',      'net.minecraft.util.DamageSource',             'noppes.npcs.api.IDamageSource'),
    @('EntityLiving',       'IEntityLiving',      'net.minecraft.entity.EntityLiving',           'noppes.npcs.api.entity.IEntityLiving'),
    
    # === NEW INTERFACE (15) ===
    @('ResourceLocation',         'IResourceLocation',  'net.minecraft.util.ResourceLocation',                    'common.minecraft.util.IResourceLocation'),
    @('ICommandSender',           'ICommandSender',     'net.minecraft.command.ICommandSender',                   'common.minecraft.command.ICommandSender'),
    @('ChatComponentText',        'ITextComponent',     'net.minecraft.util.ChatComponentText',                   'common.minecraft.util.ITextComponent'),
    @('ChatComponentTranslation', 'ITextComponent',     'net.minecraft.util.ChatComponentTranslation',            'common.minecraft.util.ITextComponent'),
    @('IChatComponent',           'ITextComponent',     'net.minecraft.util.IChatComponent',                      'common.minecraft.util.ITextComponent'),
    @('ChatStyle',                'ITextStyle',         'net.minecraft.util.ChatStyle',                           'common.minecraft.util.ITextStyle'),
    @('NBTBase',                  'INbtBase',           'net.minecraft.nbt.NBTBase',                              'common.minecraft.nbt.INbtBase'),
    @('Configuration',            'IConfiguration',     'net.minecraftforge.common.config.Configuration',         'common.minecraftforge.common.config.IConfiguration'),
    @('Property',                 'IConfigProperty',    'net.minecraftforge.common.config.Property',              'common.minecraftforge.common.config.IConfigProperty'),
    @('AxisAlignedBB',            'IBoundingBox',       'net.minecraft.util.AxisAlignedBB',                       'common.minecraft.util.IBoundingBox'),
    @('Vec3',                     'IVector3',           'net.minecraft.util.Vec3',                                'common.minecraft.util.IVector3'),
    @('MovingObjectPosition',     'IRayTraceResult',    'net.minecraft.util.MovingObjectPosition',                'common.minecraft.util.IRayTraceResult'),
    @('Potion',                   'IPotionType',        'net.minecraft.potion.Potion',                            'common.minecraft.potion.IPotionType'),
    @('PotionEffect',             'IPotionEffect',      'net.minecraft.potion.PotionEffect',                      'common.minecraft.potion.IPotionEffect'),
    @('Explosion',                'IExplosion',         'net.minecraft.world.Explosion',                          'common.minecraft.world.IExplosion')
)
```

## Appendix B: Static-Transform Registry

Complete list from Section 4, Step 3.4. All patterns use .NET regex syntax for PowerShell.

## Appendix C: File Inventory by Package

### Files to copy (not already in core/)

**noppes/npcs/controllers/** (estimated ~60 new):
- QuestController, PlayerQuestController, PlayerDataController, DialogController
- LinkedItemController, LinkedNpcController, PartyController, AuctionController
- CustomEffectController, CustomGuiController, BankController, ChunkController
- RecipeController, SpawnController, ServerCloneController, AnimationController
- ScriptController, ScriptHookController, MarketRegistry, HookDefinition
- + ~40 data/* classes (Quest, Dialog, PlayerData, etc.)

**noppes/npcs/quests/** (6 new):
- QuestItem, QuestKill, QuestLocation, QuestDialog, QuestManual, QuestInterface

**noppes/npcs/roles/** (24 new):
- RoleTrader, RoleBank, RoleFollower, RoleCompanion, RoleTransporter, RolePostman
- RoleMount, RoleInterface, RoleInnkeeper, RoleAuctioneer
- JobBard, JobGuard, JobHealer, JobSpawner, JobItemGiver, JobFollower, JobConversation, JobChunkLoader, JobInterface
- companion/* (5 files)

**noppes/npcs/config/** (15 new):
- ConfigMain, ConfigClient, ConfigScript, ConfigMarket, ConfigEnergy, ConfigItem
- ConfigDebug, ConfigExperimental, ConfigMixin, LoadConfiguration, StringCache, GlyphCache
- legacy/* (3 files)

**noppes/npcs/util/** (3 new MC-free):
- GameProfileAlt, JsonException, ScriptToStringHelper

**kamkeel/npcs/controllers/** (estimated ~75 new):
- AbilityController, EnergyController, ProfileController, AttributeController
- TelegraphController, SyncController
- + ~69 data/* classes (abilities, ability types, conditions, etc.)

**noppes/npcs/constants/** (~3 new):
- ScriptContext, EnumPotionType, EnumNotificationType (others already in core/)
