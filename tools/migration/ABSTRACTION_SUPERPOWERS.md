# Abstraction Superpowers — Brainstorm

> Design document for the next generation of migration tools.  
> Goal: Intelligent, modular abstraction generation that agents feed parameters to and get production-ready Java files out.

---

## The Problem

When migrating a class to `core/`, every MC type it touches needs a platform-api abstraction. Today this means:

1. Agent manually writes an `IFoo` interface in platform-api
2. Agent manually writes a `FooWrapper` in mc1710 that delegates to the MC class
3. Agent runs `Create-And-Propagate` to swap types across core/

Step 1-2 is the bottleneck. For a trivial 3-method interface it's fine. For something like `IPlayer` (100+ methods) or `IWorld` (50+ methods), it's hours of manual typing — and agents get it wrong (forgetting methods, wrong signatures, MC types leaking through).

**What we need:** Tools that take a description of what to abstract and **generate both the interface AND the wrapper** — correct, complete, matching existing patterns, in seconds.

---

## The Key Insight: You Already Have The Spec

The project has **three existing sources** for what methods an abstraction should include:

### Source 1: Existing Scripting API Interfaces
`src/api/java/noppes/npcs/api/` has 48+ interfaces (`IPlayer`, `IEntity`, `IWorld`, `ICustomNpc`, etc.) that are **already curated, already clean, already tested**. These are being merged into platform-api. They define exactly which methods matter and use clean types (no MC leakage except `getMCEntity() → Object`).

**This is the 90% case.** Most abstractions already have an API interface. The tool just needs to read it and generate the wrapper.

### Source 2: Usage in core/
When a class in `core/` calls `player.getDisplayName()`, that tells us `getDisplayName()` must exist on `IPlayer`. Scanning core/ for method calls on MC types gives the **minimal required surface** — only what's actually needed.

### Source 3: Agent Judgment
Sometimes you need methods that aren't in the API yet and aren't used in core/ yet, but will be needed for future waves. The agent provides these as additions.

**Design principle:** Tools should be able to pull from ALL THREE sources, merge them, and generate from the result.

---

## Approach Comparison

### Approach A: "Interface-First" (No Blueprint File)

The interface **IS** the spec. Agent writes (or copies) the interface, then a tool reads it and generates the wrapper.

```
Agent writes IFoo.java → Generate-Wrapper reads it → FooWrapper.java
```

**Pros:**
- No intermediate format. The interface is the single source of truth.
- Agent can use normal Java IDE features to author the interface.
- The interface is production code, not a throwaway artifact.

**Cons:**
- No way to express MC-side metadata (which MC method to delegate to, type conversions).
- Works great when method names match 1:1. Falls apart for `getName()` → `getCommandSenderName()`.
- Can't express wrapper constructor patterns or field types.

**Verdict:** Great for simple cases. Needs an escape hatch for complex mappings.

---

### Approach B: "Blueprint JSON" (Intermediate Spec File)

A JSON file describes both sides: the interface and the wrapper, including method mappings and type conversions.

```
Blueprint.json → Generate-Interface → IFoo.java
Blueprint.json → Generate-Wrapper  → FooWrapper.java
```

**Pros:**
- Full control over everything: method names, mappings, type transforms, custom delegation bodies.
- Can be auto-generated from existing API interfaces or usage scans.
- Agent can review/edit the JSON before generation.
- Composable: merge multiple blueprints.

**Cons:**
- Another file format to maintain.
- JSON is verbose for Java method signatures.
- Risk of the blueprint drifting from the actual generated code.

**Verdict:** Most powerful, but most complex. Best for large abstractions (IPlayer, IWorld).

---

### Approach C: "Hybrid" (Interface + Mapping File)

Agent writes the interface. A small mapping file handles the exceptions. Tool reads both.

```
IFoo.java + foo-mappings.json → Generate-Wrapper → FooWrapper.java
```

The mapping file only contains **overrides** — methods where the MC delegation isn't obvious:

```json
{
  "mcClass": "net.minecraft.entity.player.EntityPlayerMP",
  "fieldName": "player",
  "extends": "ScriptLivingBase",
  "mappings": {
    "getName": { "delegate": "getCommandSenderName" },
    "getHeldItem": { "transform": "return NpcAPI.getIItemStack({{mc}}.getCurrentEquippedItem())" },
    "getPosition": { "transform": "return new ScriptBlockPos({{mc}}.posX, {{mc}}.posY, {{mc}}.posZ)" }
  }
}
```

Methods NOT in the mapping file get default delegation: `return this.player.methodName(args)`.

**Pros:**
- Interface is real production code (not generated artifact).
- Mapping file is tiny — only the exceptions.
- 80% of methods need no mapping (same name, same types).
- Easy for agents to reason about.

**Cons:**
- Two files to coordinate (but the mapping file is optional — only needed for non-trivial cases).
- Still need to decide where mapping files live (probably `tools/migration/mappings/`).

**Verdict:** Best balance of power and simplicity. Recommended.

---

### Approach D: "Usage-Driven Auto-Generation"

Fully automated: scan core/ for what methods are called on `EntityPlayer`, auto-generate `IPlayer` with exactly those methods, auto-generate `PlayerWrapper`.

```
Scan-Usage "EntityPlayer" → auto-generate IPlayer + PlayerWrapper
```

**Pros:**
- Zero manual work. Truly a "superpower."
- Minimal surface area — only abstracts what's needed.

**Cons:**
- Method signatures can't always be inferred from call sites (especially return types).
- Generates a minimal interface that may be too narrow for future use.
- Can't handle type conversions without a mapping.
- Fragile: regex-based Java parsing will miss edge cases.

**Verdict:** Great as a DISCOVERY tool (tells you what to abstract), poor as a GENERATION tool on its own. Best combined with Approach B or C.

---

## Recommended Architecture: Hybrid Pipeline

```
┌─────────────────────────────────────────────────────┐
│                  DISCOVERY PHASE                     │
│                                                      │
│  Scan-Usage.ps1 ──→ usage-report.json               │
│  Extract-API.ps1 ──→ methods from existing API       │
│  Agent knowledge ──→ additional methods               │
│                                                      │
│         ↓ ↓ ↓  (all feed into)  ↓ ↓ ↓               │
├─────────────────────────────────────────────────────┤
│                  GENERATION PHASE                    │
│                                                      │
│  Generate-Interface.ps1                              │
│    Input: name, package, extends, method list        │
│    Output: IFoo.java in platform-api/                │
│                                                      │
│  Generate-Wrapper.ps1                                │
│    Input: interface file + optional mappings          │
│    Output: FooWrapper.java in mc1710/                │
│                                                      │
├─────────────────────────────────────────────────────┤
│                  PROPAGATION PHASE                   │
│                                                      │
│  Symbol-Swap.ps1 (existing)                          │
│  Create-And-Propagate.ps1 (existing)                 │
│  Dedupe-Imports.ps1 (existing)                       │
│  Build-Report.ps1 (existing)                         │
└─────────────────────────────────────────────────────┘
```

---

## Proposed Tool Suite

### Tool 1: `Scan-Usage.ps1` — Usage Discovery

Scans a directory for all method calls on a given type. Answers: "What do we actually need to abstract?"

```powershell
Scan-Usage -Type "EntityPlayer" -SearchDir "core/src/main/java"
```

**Output:** JSON report

```json
{
  "type": "EntityPlayer",
  "searchDir": "core/src/main/java",
  "methodCalls": [
    {
      "method": "getCommandSenderName",
      "files": ["controllers/DialogController.java", "controllers/QuestController.java"],
      "count": 4
    },
    {
      "method": "getUniqueID",
      "files": ["controllers/PlayerDataController.java"],
      "count": 2
    },
    {
      "method": "inventory",
      "files": ["controllers/data/QuestData.java"],
      "count": 1,
      "note": "field access"
    }
  ],
  "fieldAccesses": [
    { "field": "posX", "files": ["..."], "count": 3 },
    { "field": "worldObj", "files": ["..."], "count": 7 }
  ],
  "totalFiles": 12,
  "totalReferences": 34
}
```

**How it works:**
- Regex scan for `variableName.methodName(` where variable type matches the target
- Also catches field access patterns
- Groups by method name, counts occurrences, lists files
- Agent uses this to decide the whitelist for Generate-Interface

---

### Tool 2: `Extract-API.ps1` — Blueprint from Existing Interface

Reads an existing Java interface file and extracts a structured method list.

```powershell
Extract-API -Source "src/api/java/noppes/npcs/api/entity/IPlayer.java"
# OR
Extract-API -Source "platform-api/src/main/java/noppes/npcs/api/entity/IEntity.java"
```

**Output:** JSON method list

```json
{
  "source": "platform-api/.../IEntity.java",
  "name": "IEntity",
  "package": "noppes.npcs.api.entity",
  "extends": [],
  "imports": ["noppes.npcs.api.INbt", "noppes.npcs.api.IPos", "..."],
  "methods": [
    {
      "name": "getX",
      "returns": "double",
      "params": [],
      "javadoc": "The entity's x position."
    },
    {
      "name": "setPosition",
      "returns": "void",
      "params": [
        { "type": "double", "name": "x" },
        { "type": "double", "name": "y" },
        { "type": "double", "name": "z" }
      ],
      "javadoc": "Sets the entity's position to the specified coordinates."
    },
    {
      "name": "dropItem",
      "returns": "void",
      "params": [{ "type": "IItemStack", "name": "item" }],
      "javadoc": "Causes the entity to drop the given item."
    }
  ],
  "methodCount": 64
}
```

**Use cases:**
- Feed into Generate-Wrapper to create the mc1710 delegation class
- Compare with Scan-Usage output to find gaps
- Subset: `Extract-API -Source "..." -Whitelist "getX,getY,getZ,setPosition,getPosition"`

---

### Tool 3: `Generate-Interface.ps1` — Interface Generator

Takes a method specification and generates a production-ready Java interface file.

```powershell
Generate-Interface `
    -Name "IPlayer" `
    -Package "noppes.npcs.api.entity" `
    -Extends @("IEntityLivingBase") `
    -Methods $methodList `
    -OutputDir "platform-api/src/main/java/noppes/npcs/api/entity"
```

**The `$methodList` format** (PowerShell array of hashtables):

```powershell
$methodList = @(
    @{ name='getName';      returns='String';     params=@() },
    @{ name='getHealth';    returns='float';      params=@() },
    @{ name='setHealth';    returns='void';       params=@(
        @{ type='float'; name='health' }
    )},
    @{ name='getHeldItem';  returns='IItemStack'; params=@() },
    @{ name='setPosition';  returns='void';       params=@(
        @{ type='double'; name='x' },
        @{ type='double'; name='y' },
        @{ type='double'; name='z' }
    )},
    @{ name='dropItem';     returns='void';       params=@(
        @{ type='IItemStack'; name='item' }
    )}
)
```

**Generated output:** Clean Java interface with:
- Correct package declaration
- Auto-detected imports (scans return types and param types for known PA types)
- Javadoc on each method (if provided in the hashtable)
- Proper formatting matching existing platform-api style

**Why this matters:** Agent provides a LIST OF METHODS and gets a file. No hand-writing boilerplate. The tool handles package, imports, formatting.

---

### Tool 4: `Generate-Wrapper.ps1` — Wrapper Skeleton Generator

Takes an interface file (or method list) and generates a wrapper class that delegates to an MC object.

```powershell
Generate-Wrapper `
    -Interface "platform-api/src/main/java/noppes/npcs/api/entity/IPlayer.java" `
    -MCClass "net.minecraft.entity.player.EntityPlayerMP" `
    -WrapperName "PlayerWrapper" `
    -WrapperPackage "noppes.npcs.scripted.entity" `
    -FieldName "player" `
    -ExtendsWrapper "ScriptLivingBase" `
    -Mappings $mappings `
    -OutputDir "mc1710/src/main/java/noppes/npcs/scripted/entity"
```

**The `$mappings` parameter** (optional — only for non-trivial delegations):

```powershell
$mappings = @{
    # Method name maps to different MC method
    'getName' = @{ delegate = 'getCommandSenderName' }

    # Method needs type conversion
    'getHeldItem' = @{ transform = 'return NpcAPI.getIItemStack(this.player.getCurrentEquippedItem())' }

    # Method needs complex body
    'getPosition' = @{ body = @'
        return new ScriptBlockPos(this.player.posX, this.player.posY, this.player.posZ);
'@ }

    # Method should be a TODO (too complex for auto-gen)
    'getSurroundingEntities' = @{ todo = 'Needs entity list wrapping with NpcAPI.getIEntity()' }
}
```

**Generated output:**

```java
package noppes.npcs.scripted.entity;

import net.minecraft.entity.player.EntityPlayerMP;
import noppes.npcs.api.entity.IPlayer;
// ... auto-detected imports ...

public class PlayerWrapper extends ScriptLivingBase<EntityPlayerMP> implements IPlayer {

    public PlayerWrapper(EntityPlayerMP player) {
        super(player);
    }

    @Override
    public String getName() {
        return this.entity.getCommandSenderName();  // from mappings
    }

    @Override
    public float getHealth() {
        return this.entity.getHealth();  // auto-delegated (same name)
    }

    @Override
    public void setHealth(float health) {
        this.entity.setHealth(health);  // auto-delegated
    }

    @Override
    public IItemStack getHeldItem() {
        return NpcAPI.getIItemStack(this.player.getCurrentEquippedItem());  // from mappings
    }

    @Override
    public void setPosition(double x, double y, double z) {
        this.entity.setPosition(x, y, z);  // auto-delegated
    }

    @Override
    public IEntity[] getSurroundingEntities(int range, int type) {
        // TODO: Needs entity list wrapping with NpcAPI.getIEntity()
        throw new UnsupportedOperationException("Not yet implemented");
    }
}
```

**Delegation rules (applied automatically):**
1. If method name is in `$mappings` with `delegate` → use that MC method name
2. If method name is in `$mappings` with `transform` → use verbatim, substituting `{{mc}}` for field name
3. If method name is in `$mappings` with `body` → paste the body verbatim
4. If method name is in `$mappings` with `todo` → generate TODO + throw
5. **Default:** `return this.{field}.{methodName}({args})` (same name, pass args through)

---

### Tool 5: `Dependency-Map.ps1` — Migration Readiness Scanner

Answers: "What MC types does this class depend on, and which ones already have PA abstractions?"

```powershell
Dependency-Map -File "mc1710/src/main/java/noppes/npcs/controllers/QuestController.java"
```

**Output:**

```
╔══════════════════════════════════════════════════════════════════╗
║  Dependency Map: QuestController.java                           ║
╠══════════════════════════════════════════════════════════════════╣
║                                                                  ║
║  ✅ ABSTRACTED (ready to swap)                                   ║
║  ├── NBTTagCompound    → INbt          (platform-api)            ║
║  ├── EntityPlayer      → IPlayer       (platform-api)            ║
║  ├── EntityPlayerMP    → IPlayer       (platform-api)            ║
║  └── ItemStack         → IItemStack    (platform-api)            ║
║                                                                  ║
║  ⚠️  NEEDS ABSTRACTION (create before migrating)                 ║
║  ├── MinecraftServer   (3 usages: getPlayerList, getWorld)       ║
║  └── ChatComponentText (1 usage: new ChatComponentText(msg))     ║
║                                                                  ║
║  🔴 MC-COUPLED (cannot abstract — stays in mc1710 shadow)        ║
║  ├── EntityNPCInterface (extends Entity)                         ║
║  └── WorldServer       (needs IWorld extension)                  ║
║                                                                  ║
║  📊 Migration readiness: 4/8 types abstracted (50%)              ║
║  📋 Blocking: 2 types need new PA interfaces                     ║
╚══════════════════════════════════════════════════════════════════╝
```

**How it works:**
- Parse imports from the file
- Check each import against known PA type mappings (hardcoded table + scan of platform-api/)
- Categorize: abstracted / needs abstraction / mc-coupled
- For "needs abstraction" — scan the file for what methods are called on that type

**Use case:** Before starting a migration wave, run this on every file you plan to migrate. It tells you what PA interfaces to create first.

---

### Tool 6: `Batch-Plan.ps1` — Migration Wave Planner

Given a list of files to migrate, computes the optimal order and required abstractions.

```powershell
Batch-Plan -Files @(
    'noppes/npcs/controllers/QuestController.java',
    'noppes/npcs/controllers/PlayerQuestController.java',
    'noppes/npcs/quests/QuestItem.java',
    'noppes/npcs/quests/QuestKill.java'
)
```

**Output:**

```
═══ Migration Wave Plan ═══

Step 1: Create PA Interfaces
  └── IServerService  (needed by: QuestController)
  └── IMessageService (needed by: QuestController, PlayerQuestController)

Step 2: Copy Files (ordered by dependency depth)
  1. QuestItem.java        (0 internal deps)
  2. QuestKill.java        (0 internal deps)
  3. PlayerQuestController (depends on: QuestItem, QuestKill)
  4. QuestController.java  (depends on: PlayerQuestController, QuestItem, QuestKill)

Step 3: Stubs Needed
  └── SyncController.updateQuest()
  └── ScriptController.HasStart
  └── EventHooks (empty)

Step 4: Swaps
  └── NBTTagCompound → INbt  (12 occurrences across 4 files)
  └── EntityPlayer → IPlayer (8 occurrences across 3 files)
  └── MinecraftServer → IServerService (3 occurrences in 1 file)
```

---

## Type Mapping Registry

Instead of hardcoding type mappings in every script, maintain a single registry file: `tools/migration/type-registry.json`

```json
{
  "mappings": [
    {
      "mcType": "NBTTagCompound",
      "mcImport": "net.minecraft.nbt.NBTTagCompound",
      "paType": "INbt",
      "paImport": "noppes.npcs.api.INbt",
      "status": "done"
    },
    {
      "mcType": "EntityPlayer",
      "mcImport": "net.minecraft.entity.player.EntityPlayer",
      "paType": "IPlayer",
      "paImport": "noppes.npcs.api.entity.IPlayer",
      "wrapMethod": "PlatformServiceHolder.get().wrapPlayer({{mc}})",
      "status": "done"
    },
    {
      "mcType": "World",
      "mcImport": "net.minecraft.world.World",
      "paType": "IWorld",
      "paImport": "noppes.npcs.api.IWorld",
      "wrapMethod": "PlatformServiceHolder.get().wrapWorld({{mc}})",
      "status": "done"
    },
    {
      "mcType": "MinecraftServer",
      "mcImport": "net.minecraft.server.MinecraftServer",
      "paType": null,
      "paImport": null,
      "status": "not-abstracted"
    }
  ]
}
```

**All tools read from this registry:**
- `Dependency-Map` uses it to categorize deps as abstracted/not-abstracted
- `Generate-Wrapper` uses `wrapMethod` to auto-generate type conversions in delegation
- `Symbol-Swap` can auto-generate `-Swaps` from the registry
- `Batch-Plan` knows which abstractions already exist

**When you create a new PA interface, you add ONE entry to the registry.** Every tool instantly knows about it.

---

## Workflow Examples

### Workflow A: Migrate a controller that needs a new abstraction

```powershell
# 1. Check what QuestController depends on
Dependency-Map -File "mc1710/.../QuestController.java"
# Output: MinecraftServer needs abstraction, 3 usages

# 2. Scan what methods are called on MinecraftServer in core/
Scan-Usage -Type "MinecraftServer" -SearchDir "core/src/main/java"
# Output: getPlayerList() x2, getEntityWorld() x1

# 3. Generate the interface (agent provides method list based on scan)
Generate-Interface -Name "IServerService" -Package "noppes.npcs.api.handler" `
    -Methods @(
        @{ name='getPlayerList'; returns='Object'; params=@() },
        @{ name='getEntityWorld'; returns='IWorld'; params=@(
            @{ type='int'; name='dimensionId' }
        )}
    ) `
    -OutputDir "platform-api/.../noppes/npcs/api/handler"

# 4. Generate the wrapper
Generate-Wrapper -Interface "platform-api/.../IServerService.java" `
    -MCClass "net.minecraft.server.MinecraftServer" `
    -WrapperName "ServerServiceWrapper" `
    -OutputDir "mc1710/.../noppes/npcs/scripted"

# 5. Propagate (existing tools)
Symbol-Swap -Directory "core/src/main/java" -Swaps @(
    ,@('MinecraftServer', 'IServerService', 'net.minecraft.server.MinecraftServer', 'noppes.npcs.api.handler.IServerService')
)
```

### Workflow B: Port an existing API interface to platform-api

```powershell
# 1. Extract method list from existing scripting API interface
$api = Extract-API -Source "src/api/java/noppes/npcs/api/entity/IEntityLiving.java"
# Output: JSON with 15 methods, all clean types

# 2. The interface already exists in platform-api (merged) — just generate the wrapper
Generate-Wrapper `
    -Interface "platform-api/.../IEntityLiving.java" `
    -MCClass "net.minecraft.entity.EntityLiving" `
    -WrapperName "ScriptLiving" `
    -FieldName "entity" `
    -ExtendsWrapper "ScriptLivingBase" `
    -Mappings @{
        'getNavigator' = @{ todo = 'Need INavigator abstraction first' }
    } `
    -OutputDir "mc1710/.../noppes/npcs/scripted/entity"
```

### Workflow C: Full automated wave

```powershell
# 1. Plan the wave
$plan = Batch-Plan -Files @('controllers/QuestController.java', '...')

# 2. Create missing abstractions (from plan output)
foreach ($needed in $plan.NewAbstractions) {
    # Agent provides method lists
    Generate-Interface -Name $needed.Name -Package $needed.Package -Methods $needed.Methods
    Generate-Wrapper -Interface $needed.InterfacePath -MCClass $needed.MCClass ...
}

# 3. Copy, nuke, swap, stub, build (existing pipeline)
Copy-Recursive -Sources $plan.Files -AllowPaths $plan.AllowPaths
Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes $standardPrefixes
Symbol-Swap -Directory 'core/src/main/java' -Swaps $plan.Swaps
Generate-Stubs -Stubs $plan.Stubs
Dedupe-Imports -Directory 'core/src/main/java'
Build-Report -GradleTask ':core:compileJava'
```

---

## Open Questions

### Q1: Where do wrappers live?

Current pattern: `mc1710/src/main/java/noppes/npcs/scripted/entity/ScriptEntity.java`  
AGENTS.md says new wrappers should be `[Thing]Wrapper` (e.g., `PlayerWrapper`).

**Options:**
- A) Keep `scripted/entity/` path, use `ScriptXxx` naming (consistent with existing)
- B) New `mc1710/.../noppes/npcs/wrapper/` package, use `XxxWrapper` naming (cleaner)
- C) Agent decides per-case

### Q2: How deep should auto-delegation go?

For `IItemStack getHeldItem()`, the wrapper needs:
```java
return NpcAPI.getIItemStack(this.player.getCurrentEquippedItem());
```

Should Generate-Wrapper auto-detect that `IItemStack` return type needs wrapping (from the type registry), or should the agent always provide a mapping for type-converting methods?

**Options:**
- A) Auto-detect from type registry (smart but fragile)
- B) Agent always provides mappings for non-primitive returns (explicit but verbose)
- C) Auto-detect with agent override (best of both, most complex to implement)

### Q3: Should we generate Javadoc?

Existing platform-api interfaces have full Javadoc on every method (see IEntity.java — 604 lines for 64 methods). Should the generator:
- A) Copy Javadoc from source API interface if available
- B) Generate placeholder `/** TODO */` 
- C) Skip Javadoc (agent adds later)

### Q4: Composable method "trait packs"?

Instead of listing methods individually, define reusable groups:

```powershell
$POSITION_METHODS = @(
    @{ name='getX'; returns='double'; params=@() },
    @{ name='getY'; returns='double'; params=@() },
    @{ name='getZ'; returns='double'; params=@() },
    @{ name='setPosition'; returns='void'; params=@(
        @{ type='double'; name='x' },
        @{ type='double'; name='y' },
        @{ type='double'; name='z' }
    )}
)

$HEALTH_METHODS = @(
    @{ name='getHealth'; returns='float'; params=@() },
    @{ name='setHealth'; returns='void'; params=@(@{ type='float'; name='health' }) },
    @{ name='isAlive'; returns='boolean'; params=@() }
)

# Compose:
Generate-Interface -Name "IEntityLiving" -Methods ($POSITION_METHODS + $HEALTH_METHODS + $AI_METHODS)
```

Worth the complexity? Or is copy-paste fine since agents handle the composition?

### Q5: Registry format — JSON or PowerShell?

**JSON:** Universal, agents can read/edit it, but needs `ConvertFrom-Json` parsing.  
**PowerShell .psd1:** Native hashtable, zero parsing, but agents are less familiar with the format.  
**PowerShell .ps1 with variables:** Dot-source it, variables are live. Most ergonomic for tool consumption.

Leaning toward **JSON** — it's the lingua franca that agents handle best, and `ConvertFrom-Json` is one line.

---

## Priority Order for Implementation

| Priority | Tool | Effort | Impact |
|----------|------|--------|--------|
| **P0** | `Generate-Wrapper.ps1` | High | Eliminates the #1 bottleneck (hand-writing wrapper delegation) |
| **P0** | `Generate-Interface.ps1` | Medium | Fast interface scaffolding from method lists |
| **P1** | `Extract-API.ps1` | Medium | Reads existing API interfaces into method lists for Generate-Wrapper |
| **P1** | Type Registry (`type-registry.json`) | Low | Central truth for all MC→PA mappings |
| **P2** | `Dependency-Map.ps1` | Medium | Pre-migration readiness check |
| **P2** | `Scan-Usage.ps1` | Medium | Discovery of minimal abstraction surface |
| **P3** | `Batch-Plan.ps1` | High | Full wave planning (can be done manually for now) |

**P0 tools unlock 10x speed.** An agent that can say "generate wrapper for IPlayer against EntityPlayerMP" and get 80%+ correct code in one command — that's the superpower.

---

## Summary

The breakthrough idea: **the interface IS the spec, the type registry knows the conversions, and Generate-Wrapper reads both to produce 80-95% correct delegation code.** The remaining 5-20% (complex type conversions, multi-step logic) is handled by explicit mappings the agent provides.

No JSON blueprint files needed for most cases. The pipeline:
1. Interface exists (copied from API or generated) → **source of truth**
2. Type registry maps MC types to PA types → **auto-conversion**
3. Generate-Wrapper reads interface + registry + optional mappings → **wrapper skeleton**
4. Agent reviews, fills TODOs, fixes edge cases → **production code**
5. Existing tools propagate → **done**
