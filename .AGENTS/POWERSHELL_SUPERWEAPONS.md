# PowerShell Superweapons — Migration Toolkit

**Created:** 2026-03-15  
**Purpose:** A library of parameterized PowerShell functions that turn multi-hundred-file migrations into single-command operations. These replace the slow read/edit/write agent tools with raw filesystem power.

---

## The Problem

Agents currently:
1. Copy files one-by-one using slow tools ❌
2. Read each file, then edit each file individually ❌
3. When build fails (EXPECTED — missing PA interfaces), **panic and delete everything** ❌
4. Each tool call has ~1-2 second overhead × 500 calls = 15+ minutes of pure overhead ❌

**Target:** Execute an entire Wave (25+ files, 10+ symbol swaps, import surgery, new interfaces) in **ONE PowerShell command** taking **< 5 seconds**.

---

## Architecture: Function Library + Params

### Core Principle: Tools are STABLE FUNCTIONS. Agent provides PARAMS.

The agent NEVER edits the tool source files. They are loaded once and called with parameters.

```
tools/migration/
  lib.ps1               — All functions in one file. Dot-source to load.
```

### Agent Workflow

```powershell
# Step 1: Load the library (once per session)
. X:\projects\CustomNPC-Plus\tools\migration\lib.ps1

# Step 2: Call functions with params — that's it. No editing. No JSON. Just calls.
MassCopy -Pairs @(
    @('mc1710/src/main/java/.../DialogController.java', 'core/src/main/java/.../DialogController.java'),
    @('mc1710/src/main/java/.../Dialog.java',           'core/src/main/java/.../data/Dialog.java')
)

NukeImports -Directory 'core/src/main/java'

SymbolSwap -Directory 'core/src/main/java' -Swaps @(
    @('NBTTagCompound', 'INbt', 'net.minecraft.nbt.NBTTagCompound', 'noppes.npcs.api.nbt.INbt'),
    @('EntityPlayer',   'IPlayer', 'net.minecraft.entity.player.EntityPlayer', 'noppes.npcs.api.entity.IPlayer')
)

CreateAndPropagate `
    -FilePath 'platform-api/src/main/java/common/minecraft/IMessageService.java' `
    -Content 'package common.minecraft; ...' `
    -OldClass 'ChatComponentText' -NewClass 'IMessageService' `
    -OldImport 'net.minecraft.util.ChatComponentText' `
    -NewImport 'common.minecraft.IMessageService' `
    -TargetDir 'core/src/main/java'

GenerateStubs -Stubs @(
    @{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class';
       body='public static void updateDialog(Object d) {} public static void removeDialog(int id) {}' }
)

BuildReport
```

**The agent's ONLY job:** decide what params to pass. The functions handle everything else.

### Why Functions, Not Scripts + JSON

| Approach | Problem |
|----------|---------|
| Inline PowerShell | Escaping nightmare through cmd.exe |
| Separate scripts + JSON configs | Agent has to write JSON files, manage configs, more moving parts |
| **Function library + direct params** | **Agent dot-sources once, calls functions inline. Zero overhead. Zero file management.** |

### Key Rule

```
Tools are METHODS. You FEED them PARAMS. You NEVER edit their source.
```

---

## TIER 1 — Core Arsenal (Solve the fatal problems)

### Tool 1: `Mass-Copy` — Bulk File Copier

**Problem:** Copying 25 files one-by-one with agent tools takes 25 tool calls (50+ seconds).

**Solution:** One PowerShell command copies ALL files for a wave, creating directories as needed.

```powershell
# INPUT: Array of [source, dest] pairs
$copies = @(
    @('mc1710/src/main/java/noppes/npcs/controllers/DialogController.java',
      'core/src/main/java/noppes/npcs/controllers/DialogController.java'),
    @('mc1710/src/main/java/noppes/npcs/controllers/data/Dialog.java',
      'core/src/main/java/noppes/npcs/controllers/data/Dialog.java'),
    # ... 25 more
)

foreach ($pair in $copies) {
    $src = Join-Path 'X:\projects\CustomNPC-Plus' $pair[0]
    $dst = Join-Path 'X:\projects\CustomNPC-Plus' $pair[1]
    $dir = Split-Path $dst -Parent
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    Copy-Item $src $dst -Force
    Write-Host "Copied: $($pair[0]) -> $($pair[1])"
}
```

**Speed:** 25 files in < 1 second.

---

### Tool 2: `Symbol-Swap` — Bulk Type Replacement with Import Surgery

**Problem:** Replacing `NBTTagCompound` → `INbt` across 25 files requires reading+editing each file = 50+ tool calls.

**Solution:** One loop that for each file:
1. Replaces ALL symbol occurrences (class names in code)
2. Removes the old MC import line
3. Adds the new PA import line (if not already present)

```powershell
# INPUT: Files to process + replacement map
$files = Get-ChildItem -Recurse -Filter '*.java' -Path 'X:\projects\CustomNPC-Plus\core\src\main\java'

# Replacement map: [oldSymbol, newSymbol, oldImport, newImport]
$swaps = @(
    @('NBTTagCompound',          'INbt',              'net.minecraft.nbt.NBTTagCompound',                  'noppes.npcs.api.nbt.INbt'),
    @('NBTTagList',              'INbtList',           'net.minecraft.nbt.NBTTagList',                      'noppes.npcs.api.nbt.INbtList'),
    @('NBTTagString',            'INbt',               'net.minecraft.nbt.NBTTagString',                    ''),
    @('CompressedStreamTools',   'PlatformServiceHolder.get()', 'net.minecraft.nbt.CompressedStreamTools', 'noppes.npcs.platform.PlatformServiceHolder'),
    @('EntityPlayer',            'IPlayer',            'net.minecraft.entity.player.EntityPlayer',          'noppes.npcs.api.entity.IPlayer'),
    @('EntityPlayerMP',          'IPlayer',            'net.minecraft.entity.player.EntityPlayerMP',        'noppes.npcs.api.entity.IPlayer'),
    @('ItemStack',               'IItemStack',         'net.minecraft.item.ItemStack',                      'noppes.npcs.api.item.IItemStack'),
    @('EntityLivingBase',        'IEntityLivingBase',  'net.minecraft.entity.EntityLivingBase',             'noppes.npcs.api.entity.IEntityLivingBase'),
    @('Entity ',                 'IEntity ',           'net.minecraft.entity.Entity',                       'noppes.npcs.api.entity.IEntity'),
    @('EntityNPCInterface',      'ICustomNpc',         'noppes.npcs.entity.EntityNPCInterface',             'noppes.npcs.api.entity.ICustomNpc'),
)

foreach ($f in $files) {
    $content = [IO.File]::ReadAllText($f.FullName)
    $modified = $false
    
    foreach ($swap in $swaps) {
        $oldSym = $swap[0]; $newSym = $swap[1]
        $oldImp = $swap[2]; $newImp = $swap[3]
        
        # Replace symbol in code body
        if ($content -match [regex]::Escape($oldSym)) {
            $content = $content -replace [regex]::Escape($oldSym), $newSym
            $modified = $true
        }
        
        # Remove old import
        if ($oldImp -and ($content -match "import\s+$([regex]::Escape($oldImp))\s*;")) {
            $content = $content -replace "import\s+$([regex]::Escape($oldImp))\s*;\r?\n", ''
            $modified = $true
        }
        
        # Add new import (if not already present and not empty)
        if ($newImp -and $modified -and !($content -match "import\s+$([regex]::Escape($newImp))\s*;")) {
            # Insert after package declaration
            $content = $content -replace '(package\s+[^;]+;\s*\n)', "`$1`nimport $newImp;`n"
        }
    }
    
    if ($modified) {
        [IO.File]::WriteAllText($f.FullName, $content)
        Write-Host "Swapped: $($f.Name)"
    }
}
```

**Speed:** 25 files × 10 replacements in < 2 seconds.

**Key insight:** The `$swaps` array is the ENTIRE migration knowledge. An agent just needs to define the right swaps for a wave and execute.

---

### Tool 3: `Git-Checkpoint` — Indestructible Progress Protection

**Problem:** Agents panic on build failure and `git reset --hard`, destroying ALL progress.

**Solution:** Before each wave, create a named git tag. After the wave, NEVER allow `git reset`. If something goes wrong, the checkpoint is preserved.

```powershell
# BEFORE wave:
git tag -f "pre-wave-1" HEAD
git add core/ platform-api/
git stash push -m "wave-1-checkpoint" -- core/ platform-api/
git stash pop  # immediately restore, but the stash entry remains as backup

# Or simpler — just commit to a temp branch:
git checkout -b migration-wave1-wip
git add -A core/ platform-api/
git commit -m "WIP: wave 1 migration checkpoint"
git checkout -  # back to original branch, changes preserved in the wip branch
```

**Key rule for agents:** `NEVER run git reset, git checkout -- ., or git clean`. Period. If build fails, the wave's changes stay. Build failures are EXPECTED and NORMAL.

---

### Tool 4: `Build-Reporter` — Parse Build Errors Without Panic

**Problem:** Agent runs `./gradlew :core:compileJava`, sees 100 errors, panics, deletes everything.

**Solution:** A tool that runs the build, captures errors, categorizes them, and outputs a structured report. The agent READS the report and fixes errors one category at a time. No panic.

```powershell
# Run build and capture output
$output = & cmd /c "gradlew.bat :core:compileJava 2>&1" | Out-String

# Parse errors into categories
$errors = @{}
$output -split "`n" | Where-Object { $_ -match 'error:' } | ForEach-Object {
    if ($_ -match '(?<file>[^:]+\.java):(?<line>\d+): error: (?<msg>.+)') {
        $msg = $Matches.msg.Trim()
        $category = switch -Regex ($msg) {
            'cannot find symbol.*class (\w+)'    { "MISSING_CLASS:$($Matches[1])" }
            'package (\S+) does not exist'       { "MISSING_PACKAGE:$($Matches[1])" }
            'cannot find symbol.*method (\w+)'   { "MISSING_METHOD:$($Matches[1])" }
            'incompatible types'                 { "TYPE_MISMATCH" }
            default                              { "OTHER" }
        }
        if (!$errors[$category]) { $errors[$category] = @() }
        $errors[$category] += "$($Matches.file):$($Matches.line) — $msg"
    }
}

# Report
Write-Host "`n===== BUILD ERROR REPORT ====="
Write-Host "Total errors: $(($errors.Values | ForEach-Object { $_.Count } | Measure-Object -Sum).Sum)"
foreach ($cat in $errors.Keys | Sort-Object) {
    Write-Host "`n--- $cat ($($errors[$cat].Count) occurrences) ---"
    $errors[$cat] | Select-Object -First 3 | ForEach-Object { Write-Host "  $_" }
    if ($errors[$cat].Count -gt 3) { Write-Host "  ... and $($errors[$cat].Count - 3) more" }
}
```

**Output example:**
```
===== BUILD ERROR REPORT =====
Total errors: 47

--- MISSING_CLASS:SyncController (12 occurrences) ---
  DialogController.java:45 — cannot find symbol class SyncController
  QuestController.java:82 — cannot find symbol class SyncController
  BankController.java:33 — cannot find symbol class SyncController
  ... and 9 more

--- MISSING_CLASS:ChatComponentText (5 occurrences) ---
  FactionOptions.java:12 — cannot find symbol class ChatComponentText
  ... and 4 more

--- MISSING_PACKAGE:net.minecraft.nbt (3 occurrences) ---
  ...
```

**Key insight:** Categorized errors tell the agent EXACTLY what to do next:
- `MISSING_CLASS:SyncController` → These references need to be removed/stubbed (SyncController stays in mc1710)
- `MISSING_PACKAGE:net.minecraft.nbt` → Missed import replacement, run Symbol-Swap again
- `MISSING_CLASS:ChatComponentText` → Need IMessageService abstraction or remove these calls

---

### Tool 5: `MC-Import-Nuker` — Remove ALL MC Imports from a Directory

**Problem:** After copying files from mc1710 → core, they still have `import net.minecraft.*` everywhere.

**Solution:** One command removes ALL forbidden imports from all files in core/.

```powershell
$files = Get-ChildItem -Recurse -Filter '*.java' -Path 'X:\projects\CustomNPC-Plus\core\src\main\java'

$forbidden = @(
    'net\.minecraft\.',
    'cpw\.mods\.',
    'net\.minecraftforge\.',
    'noppes\.npcs\.entity\.',    # MC entity classes
    'noppes\.npcs\.client\.',    # Client-side MC code
    'noppes\.npcs\.mixin\.',     # MC mixins
    'kamkeel\.npcs\.network\.',  # MC networking
    'noppes\.npcs\.containers\.' # MC containers
)

$pattern = ($forbidden | ForEach-Object { "import\s+$_" }) -join '|'

foreach ($f in $files) {
    $content = [IO.File]::ReadAllText($f.FullName)
    $original = $content
    
    # Remove matching import lines
    $content = [regex]::Replace($content, "(?m)^(?:$pattern)[^;]*;\s*\r?\n", '')
    
    if ($content -ne $original) {
        [IO.File]::WriteAllText($f.FullName, $content)
        Write-Host "Nuked MC imports: $($f.Name)"
    }
}
```

**Speed:** Entire core/ directory in < 1 second.

**Use case:** Run FIRST after mass-copy, BEFORE Symbol-Swap. This removes the worst offenders immediately.

---

## TIER 2 — Force Multipliers (10x productivity)

### Tool 6: `PA-Creator-Propagator` — Create Interface + Replace Everywhere in One Shot

**Problem:** Creating a new PA interface (e.g., `IMessageService`) requires:
1. Write the interface file
2. Find all files in core/ that use the MC equivalent
3. Replace the MC type with the new PA type
4. Add the import everywhere
5. Remove the old import everywhere

That's 5 operations across 20+ files = 100+ tool calls.

**Solution:** One command that does ALL of this:

```powershell
# INPUT: New interface content, where to write it, what MC type it replaces
$interfacePath = 'platform-api/src/main/java/common/minecraft/IMessageService.java'
$interfaceContent = @'
package common.minecraft;

import noppes.npcs.api.entity.IPlayer;

public interface IMessageService {
    void sendMessage(IPlayer player, String message);
    void sendTranslatedMessage(IPlayer player, String key, Object... args);
    void broadcastMessage(String message);
}
'@

# Replacement targets (MC types this replaces)
$targets = @(
    @('ChatComponentText',        'IMessageService', 'net.minecraft.util.ChatComponentText',           'common.minecraft.IMessageService'),
    @('ChatComponentTranslation', 'IMessageService', 'net.minecraft.util.ChatComponentTranslation',    'common.minecraft.IMessageService'),
    @('EnumChatFormatting',       '',                 'net.minecraft.util.EnumChatFormatting',           ''),
)

# Step 1: Write the interface
$fullPath = Join-Path 'X:\projects\CustomNPC-Plus' $interfacePath
$dir = Split-Path $fullPath -Parent
if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
[IO.File]::WriteAllText($fullPath, $interfaceContent)
Write-Host "Created: $interfacePath"

# Step 2: Replace everywhere in core/
$files = Get-ChildItem -Recurse -Filter '*.java' -Path 'X:\projects\CustomNPC-Plus\core\src\main\java'
foreach ($f in $files) {
    $content = [IO.File]::ReadAllText($f.FullName)
    $modified = $false
    foreach ($t in $targets) {
        # ... same Symbol-Swap logic as Tool 2 ...
    }
    if ($modified) {
        [IO.File]::WriteAllText($f.FullName, $content)
        Write-Host "  Propagated to: $($f.Name)"
    }
}
```

**Key insight:** The PA interface and ALL its usages across the entire codebase materialize in the SAME MILLISECOND. No "I created the interface but forgot to update 3 files" bugs.

---

### Tool 7: `Shadow-Generator` — Auto-Generate mc1710 Shadow Stubs

**Problem:** Every file moved to core/ needs an mc1710 shadow that:
- Lives in the same package but in mc1710/src/main/java/
- Extends the core class
- Adds `implements IWhatever` if applicable
- Contains TODO stubs for MC-specific methods

**Solution:** Auto-generate skeleton shadows:

```powershell
# INPUT: List of core files that need shadows
$coreFiles = @(
    @{
        core = 'core/src/main/java/noppes/npcs/controllers/DialogController.java'
        shadow = 'mc1710/src/main/java/noppes/npcs/controllers/DialogController.java'
        implements = @('IDialogHandler')
        extraMethods = @(
            'public void syncUpdate(Dialog dialog) { SyncController.updateDialog(dialog); }',
            'public void syncRemove(int id) { SyncController.removeDialog(id); }'
        )
    },
    # ... more
)

foreach ($spec in $coreFiles) {
    # Parse core file for class name and package
    $coreContent = [IO.File]::ReadAllText((Join-Path 'X:\projects\CustomNPC-Plus' $spec.core))
    $pkg = if ($coreContent -match 'package\s+([^;]+);') { $Matches[1] } else { '' }
    $cls = if ($coreContent -match 'public\s+class\s+(\w+)') { $Matches[1] } else { 'Unknown' }
    
    $implClause = if ($spec.implements) { " implements $($spec.implements -join ', ')" } else { '' }
    $methods = ($spec.extraMethods | ForEach-Object { "    $_`n" }) -join "`n"
    
    $shadow = @"
package $pkg;

// MC1710 shadow — adds MC-specific behavior to core $cls
public class $cls extends $pkg.$cls$implClause {
    
$methods
    // TODO: Add remaining MC-specific methods
}
"@
    
    $fullPath = Join-Path 'X:\projects\CustomNPC-Plus' $spec.shadow
    $dir = Split-Path $fullPath -Parent
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    [IO.File]::WriteAllText($fullPath, $shadow)
    Write-Host "Shadow: $($spec.shadow)"
}
```

**Speed:** 25 shadow stubs in < 1 second.

---

### Tool 8: `Stub-Generator` — Create Temporary Stubs for Missing Types

**Problem:** Core compilation fails because `QuestController` references `PlayerQuestController` which hasn't been migrated yet. Agent panics.

**Solution:** Generate minimal stubs for types that will be migrated LATER but are referenced NOW. These stubs live in core/ temporarily and get replaced when the real class arrives.

```powershell
# INPUT: Missing types from Build-Reporter output
$stubs = @(
    @{ pkg = 'noppes.npcs.controllers'; name = 'SyncController'; type = 'class';
       body = '    // STUB — will be replaced by real migration or stay as interface reference
    public static void updateDialog(Object dialog) {}
    public static void removeDialog(int id) {}
    public static void updateQuest(Object quest) {}
    public static void removeQuest(int id) {}' },
    
    @{ pkg = 'noppes.npcs.controllers'; name = 'ScriptController'; type = 'class';
       body = '    public static boolean HasStart = false;' },
)

foreach ($s in $stubs) {
    $pkgPath = $s.pkg -replace '\.', '/'
    $dir = "X:\projects\CustomNPC-Plus\core\src\main\java\$pkgPath"
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    
    $content = @"
package $($s.pkg);

/**
 * TEMPORARY STUB — auto-generated for compilation.
 * Will be replaced when this class is properly migrated to core.
 * DO NOT add logic here.
 */
public $($s.type) $($s.name) {
$($s.body)
}
"@
    [IO.File]::WriteAllText("$dir\$($s.name).java", $content)
    Write-Host "Stub: $($s.pkg).$($s.name)"
}
```

**Key insight:** This is the ANTI-PANIC tool. Build fails because `SyncController` doesn't exist in core? Create a stub with the methods core needs. Build passes. Agent continues. When Wave 7 properly migrates SyncController, the stub gets replaced.

**Critical rule:** Stubs are marked with `TEMPORARY STUB` comment and contain NO logic — just enough signatures to compile.

---

### Tool 9: `Static-Call-Transformer` — Rewrite Static Method Calls

**Problem:** `CompressedStreamTools.readCompressed(file)` needs to become `PlatformServiceHolder.get().readCompressedNBT(file)`. This isn't a simple symbol swap — it's a method call transformation.

**Solution:** Regex-based call site rewriting:

```powershell
$files = Get-ChildItem -Recurse -Filter '*.java' -Path 'X:\projects\CustomNPC-Plus\core\src\main\java'

# Static call transformations: [regex_pattern, replacement]
$transforms = @(
    # CompressedStreamTools
    @('CompressedStreamTools\.readCompressed\s*\(([^)]+)\)',
      'PlatformServiceHolder.get().readCompressedNBT($1)'),
    @('CompressedStreamTools\.writeCompressed\s*\(([^,]+),\s*([^)]+)\)',
      'PlatformServiceHolder.get().writeCompressedNBT($1, $2)'),
    
    # CustomNpcs static accessors
    @('CustomNpcs\.getWorldSaveDirectory\s*\(\)',
      'PlatformServiceHolder.get().getWorldSaveDirectory()'),
    
    # NBT constructors
    @('new\s+NBTTagCompound\s*\(\)',  'NBT.compound()'),
    @('new\s+NBTTagList\s*\(\)',      'NBT.list()'),
    
    # MathHelper → ValueUtil
    @('MathHelper\.clamp_int\s*\(',    'ValueUtil.clampInt('),
    @('MathHelper\.clamp_float\s*\(',  'ValueUtil.clampFloat('),
    @('MathHelper\.clamp_double\s*\(', 'ValueUtil.clampDouble('),
    
    # Logging
    @('LogWriter\.(info|warn|error)\s*\(', 'PlatformServiceHolder.get().log$1('),
    
    # SideOnly annotations
    @('@SideOnly\s*\(\s*Side\.CLIENT\s*\)', '@ClientOnly'),
    @('@SideOnly\s*\(\s*Side\.SERVER\s*\)', '@ServerOnly'),
)

foreach ($f in $files) {
    $content = [IO.File]::ReadAllText($f.FullName)
    $original = $content
    
    foreach ($t in $transforms) {
        $content = [regex]::Replace($content, $t[0], $t[1])
    }
    
    if ($content -ne $original) {
        [IO.File]::WriteAllText($f.FullName, $content)
        Write-Host "Transformed: $($f.Name)"
    }
}
```

**Speed:** ALL static call transformations across ALL files in < 2 seconds.

---

### Tool 10: `MC-Method-Quarantine` — Isolate MC-Dependent Code Blocks

**Problem:** A class is 90% MC-free, but has 2 methods that use `EntityPlayerMP`, `MinecraftServer`, etc. Agent tries to migrate, fails on those methods, panics.

**Solution:** Instead of failing, QUARANTINE the MC-dependent methods — comment them out with a marker so they can be moved to shadow files later.

```powershell
# For a given file, comment out methods that contain any forbidden MC type
$file = 'X:\projects\CustomNPC-Plus\core\src\main\java\noppes\npcs\controllers\SomeController.java'
$content = [IO.File]::ReadAllText($file)

$forbiddenTypes = @(
    'MinecraftServer', 'EntityPlayerMP', 'ICommandSender', 'ChatComponentText',
    'AxisAlignedBB', 'Vec3', 'DamageSource', 'Container', 'IInventory',
    'ForgeChunkManager', 'Minecraft', 'GuiScreen', 'Tessellator',
    'SharedMonsterAttributes', 'EntityCreature', 'EntityList'
)

$pattern = ($forbiddenTypes | ForEach-Object { [regex]::Escape($_) }) -join '|'

# Find method bodies containing forbidden types and wrap them
# This is a simplified approach — real implementation would use brace-counting
$lines = $content -split "`n"
$inQuarantine = $false
$braceCount = 0
$result = @()

# For each line, track if we're inside a method with MC types
# (Full implementation would need Java-aware parsing, but regex works for 90% of cases)
foreach ($line in $lines) {
    if ($line -match "^\s*(public|private|protected)\s+.*($pattern)") {
        $result += "    // MC_QUARANTINE_START — move to mc1710 shadow"
        $result += "    // $line"
        $inQuarantine = $true
        $braceCount = 0
    } elseif ($inQuarantine) {
        $result += "    // $line"
        $braceCount += ($line.ToCharArray() | Where-Object { $_ -eq '{' }).Count
        $braceCount -= ($line.ToCharArray() | Where-Object { $_ -eq '}' }).Count
        if ($braceCount -le 0 -and $line -match '}') {
            $result += "    // MC_QUARANTINE_END"
            $inQuarantine = $false
        }
    } else {
        $result += $line
    }
}

[IO.File]::WriteAllText($file, $result -join "`n")
```

**Key insight:** This preserves the MC code as comments. When creating the shadow file later, you just uncomment the quarantined methods and move them there. Nothing is lost.

---

## TIER 3 — Wave Orchestration (The Ultimate Weapon)

### Tool 11: `Wave-Executor` — Run an ENTIRE Wave in ONE Command

**Problem:** Each wave requires: copy → nuke imports → swap symbols → transform calls → build check. An agent doing this manually takes 50+ responses.

**Solution:** One mega-command that orchestrates ALL tools for a wave:

```powershell
# ===== WAVE DEFINITION =====
$waveName = "Wave-1"

# Files to copy (source → dest)
$copies = @(
    @('mc1710/.../DialogController.java', 'core/.../DialogController.java'),
    @('mc1710/.../Dialog.java',           'core/.../data/Dialog.java'),
    # ... all 25 files
)

# Symbol swaps (old, new, oldImport, newImport)
$swaps = @(
    @('NBTTagCompound', 'INbt', 'net.minecraft.nbt.NBTTagCompound', 'noppes.npcs.api.nbt.INbt'),
    # ... all replacements
)

# Static call transforms
$transforms = @(
    @('CompressedStreamTools\.readCompressed\(([^)]+)\)', 'PlatformServiceHolder.get().readCompressedNBT($1)'),
    # ...
)

# New PA interfaces to create (optional)
$newInterfaces = @(
    @{ path = 'platform-api/.../IMessageService.java'; content = '...' },
)

# Stubs for missing types (optional)
$stubs = @(
    @{ pkg = 'noppes.npcs.controllers'; name = 'SyncController'; body = '...' },
)

# ===== EXECUTION =====

# Phase 1: Git checkpoint
git tag -f "pre-$waveName"
Write-Host "=== Checkpoint: pre-$waveName ==="

# Phase 2: Create new PA interfaces
foreach ($iface in $newInterfaces) { <# ... write file ... #> }
Write-Host "=== PA interfaces created ==="

# Phase 3: Create stubs
foreach ($s in $stubs) { <# ... write stub ... #> }
Write-Host "=== Stubs created ==="

# Phase 4: Mass copy
foreach ($pair in $copies) { <# ... copy ... #> }
Write-Host "=== Files copied ==="

# Phase 5: Nuke MC imports
<# ... MC-Import-Nuker on core/ ... #>
Write-Host "=== MC imports nuked ==="

# Phase 6: Symbol swap + import surgery
<# ... Symbol-Swap on core/ ... #>
Write-Host "=== Symbols swapped ==="

# Phase 7: Static call transforms
<# ... Static-Call-Transformer on core/ ... #>
Write-Host "=== Static calls transformed ==="

# Phase 8: Build check (report only, DON'T PANIC)
$output = & cmd /c "gradlew.bat :core:compileJava 2>&1" | Out-String
<# ... Build-Reporter ... #>
Write-Host "=== Build report generated ==="
# NEVER delete files based on build result. ONLY report.
```

**Speed:** ENTIRE WAVE in < 10 seconds (excluding the build check which takes ~30s).

---

### Tool 12: `Dependency-Scanner` — Know What You Need Before You Start

**Problem:** Agent starts migrating a wave, discovers halfway through that `Dialog.java` references `Availability.java` which hasn't been migrated yet. Panic.

**Solution:** BEFORE starting a wave, scan all files to find ALL dependencies and categorize them:

```powershell
# INPUT: Files being migrated in this wave
$waveFiles = @(
    'mc1710/src/main/java/noppes/npcs/controllers/DialogController.java',
    'mc1710/src/main/java/noppes/npcs/controllers/data/Dialog.java',
    # ...
)

# For each file, extract all import statements
$allImports = @{}
foreach ($f in $waveFiles) {
    $content = [IO.File]::ReadAllText((Join-Path 'X:\projects\CustomNPC-Plus' $f))
    $imports = [regex]::Matches($content, 'import\s+([^;]+);') | ForEach-Object { $_.Groups[1].Value }
    $allImports[$f] = $imports
}

# Categorize imports
$categories = @{
    'ALREADY_IN_CORE' = @()       # Already migrated, no action needed
    'IN_THIS_WAVE' = @()          # Being migrated right now
    'IN_PLATFORM_API' = @()       # Already abstracted
    'MC_REPLACE_KNOWN' = @()      # Has known PA replacement (from swap table)
    'MC_NEEDS_STUB' = @()         # MC type, no replacement yet — needs stub or quarantine
    'MC_REMOVE' = @()             # MC type that should just be removed (client-only, etc.)
    'JAVA_STDLIB' = @()           # java.*, no action
}

# Check each import against known locations
foreach ($entry in $allImports.GetEnumerator()) {
    foreach ($imp in $entry.Value) {
        # Categorize... (check file existence, swap table, etc.)
    }
}

# Report
Write-Host "=== DEPENDENCY SCAN for wave ==="
foreach ($cat in $categories.Keys) {
    Write-Host "`n--- $cat ---"
    $categories[$cat] | Select-Object -Unique | ForEach-Object { Write-Host "  $_" }
}
```

**Key insight:** Run this BEFORE the wave. The output tells you exactly:
- Which PA interfaces to create (MC_NEEDS_STUB)
- Which stubs to generate (MC_NEEDS_STUB)
- Which imports just need swapping (MC_REPLACE_KNOWN)
- Which code to quarantine (MC_REMOVE)

---

### Tool 13: `Method-Extractor` — Extract MC Methods to Shadow

**Problem:** `PlayerData.java` is 500 lines. 400 lines are MC-free (NBT, game logic). 100 lines reference `EntityPlayer`, `IExtendedEntityProperties`, etc. Agent needs to split this.

**Solution:** Extract methods by MC-dependency:

```powershell
# Scan a Java file and split it into:
# 1. MC-free portion (stays in core)
# 2. MC-dependent portion (becomes shadow content)

$file = 'X:\projects\CustomNPC-Plus\core\src\main\java\noppes\npcs\controllers\data\PlayerData.java'
$content = [IO.File]::ReadAllText($file)

$mcTypes = @('EntityPlayer', 'EntityPlayerMP', 'MinecraftServer', 'IExtendedEntityProperties',
             'AxisAlignedBB', 'DamageSource', 'Container', 'World ', 'WorldServer')

$lines = $content -split "`n"
$coreMethods = @()
$shadowMethods = @()
$currentMethod = @()
$inMethod = $false
$methodHasMC = $false
$braceDepth = 0

foreach ($line in $lines) {
    if (!$inMethod -and $line -match '^\s*(public|private|protected|static)\s+') {
        $inMethod = $true
        $currentMethod = @($line)
        $braceDepth = ($line.ToCharArray() | Where-Object { $_ -eq '{' }).Count
        $braceDepth -= ($line.ToCharArray() | Where-Object { $_ -eq '}' }).Count
        $methodHasMC = ($mcTypes | Where-Object { $line -match $_ }).Count -gt 0
    } elseif ($inMethod) {
        $currentMethod += $line
        $braceDepth += ($line.ToCharArray() | Where-Object { $_ -eq '{' }).Count
        $braceDepth -= ($line.ToCharArray() | Where-Object { $_ -eq '}' }).Count
        if (!$methodHasMC) {
            $methodHasMC = ($mcTypes | Where-Object { $line -match $_ }).Count -gt 0
        }
        if ($braceDepth -le 0) {
            if ($methodHasMC) { $shadowMethods += $currentMethod }
            else { $coreMethods += $currentMethod }
            $inMethod = $false
            $currentMethod = @()
        }
    } else {
        $coreMethods += $line  # Field/import/class declaration
    }
}

Write-Host "=== SPLIT ANALYSIS ==="
Write-Host "Core methods: $($coreMethods.Count) lines"
Write-Host "Shadow methods: $($shadowMethods.Count) lines"
Write-Host "`nShadow methods contain:"
$shadowMethods | Where-Object { $_ -match '(public|private|protected)' -and $_ -match '\(' } |
    ForEach-Object { Write-Host "  $_" }
```

---

### Tool 14: `Import-Deduplicator` — Clean Up Duplicate Imports

**Problem:** After Symbol-Swap runs, a file might have duplicate imports (e.g., `import noppes.npcs.api.nbt.INbt;` appears twice because two different MC types both mapped to INbt).

**Solution:** 

```powershell
$files = Get-ChildItem -Recurse -Filter '*.java' -Path 'X:\projects\CustomNPC-Plus\core\src\main\java'

foreach ($f in $files) {
    $content = [IO.File]::ReadAllText($f.FullName)
    
    # Extract all import lines, deduplicate, sort
    $importLines = [regex]::Matches($content, '(?m)^import\s+[^;]+;\s*$') |
                   ForEach-Object { $_.Value.Trim() } | Sort-Object -Unique
    
    # Remove all existing import lines
    $noImports = [regex]::Replace($content, '(?m)^import\s+[^;]+;\s*\r?\n', '')
    
    # Re-insert deduplicated imports after package line
    $importBlock = ($importLines -join "`n") + "`n"
    $result = $noImports -replace '(package\s+[^;]+;\s*\n)\s*', "`$1`n$importBlock`n"
    
    if ($result -ne $content) {
        [IO.File]::WriteAllText($f.FullName, $result)
        Write-Host "Deduped: $($f.Name)"
    }
}
```

---

### Tool 15: `Recursive-Dir-Processor` — Apply Transformations to EVERYTHING in a Directory

**Problem:** You don't always know the exact list of files. Sometimes you just want to say "fix every .java file in core/".

**Solution:** Every tool above already supports this, but explicitly — a wrapper that takes a directory + a set of transformations and applies them recursively:

```powershell
# Define transformations as composable functions
function Invoke-MigrationPipeline {
    param(
        [string]$Directory,
        [hashtable[]]$SymbolSwaps,     # Tool 2
        [string[][]]$StaticTransforms, # Tool 9
        [switch]$NukeImports,          # Tool 5
        [switch]$DedupeImports         # Tool 14
    )
    
    $files = Get-ChildItem -Recurse -Filter '*.java' -Path $Directory
    
    foreach ($f in $files) {
        $content = [IO.File]::ReadAllText($f.FullName)
        $original = $content
        
        if ($NukeImports) { <# remove MC imports #> }
        foreach ($swap in $SymbolSwaps) { <# apply symbol swaps #> }
        foreach ($transform in $StaticTransforms) { <# apply static call transforms #> }
        if ($DedupeImports) { <# deduplicate imports #> }
        
        if ($content -ne $original) {
            [IO.File]::WriteAllText($f.FullName, $content)
            Write-Host "Pipeline: $($f.Name)"
        }
    }
}
```

---

## TIER 4 — Strategic Weapons (Project-level)

### Tool 16: `Migration-Status-Dashboard` — Know Where You Are

**Problem:** After 5 waves, nobody knows which files have been migrated, which have stubs, which are quarantined.

**Solution:** Scan core/ and generate a status report:

```powershell
$coreFiles = Get-ChildItem -Recurse -Filter '*.java' -Path 'X:\projects\CustomNPC-Plus\core\src\main\java'

$status = @{
    'CLEAN' = @()          # No MC imports, no stubs, no quarantine markers
    'HAS_STUBS' = @()      # Contains TEMPORARY STUB marker
    'QUARANTINED' = @()    # Contains MC_QUARANTINE markers  
    'MC_CONTAMINATED' = @() # Still has MC imports (shouldn't happen!)
}

foreach ($f in $coreFiles) {
    $content = [IO.File]::ReadAllText($f.FullName)
    if ($content -match 'TEMPORARY STUB') { $status['HAS_STUBS'] += $f.FullName }
    elseif ($content -match 'MC_QUARANTINE') { $status['QUARANTINED'] += $f.FullName }
    elseif ($content -match 'import\s+net\.minecraft') { $status['MC_CONTAMINATED'] += $f.FullName }
    else { $status['CLEAN'] += $f.FullName }
}

Write-Host "=== MIGRATION STATUS ==="
foreach ($s in $status.Keys) {
    Write-Host "$s : $($status[$s].Count) files"
}
```

---

### Tool 17: `Wave-Definition-Generator` — Auto-Generate Wave Definitions from the Plan

**Problem:** Manually writing out all 25 file paths and 10 symbol swaps for each wave is tedious and error-prone.

**Solution:** Parse the CORE_MIGRATION_PLAN.md and auto-generate the PowerShell wave definitions:

```powershell
# Read the plan, extract file lists per wave
# (This would parse markdown tables and generate $copies, $swaps arrays)
# Could output a ready-to-execute .ps1 file per wave
```

**Key insight:** The migration plan already has ALL the information — file names, MC types, PA replacements. A parser can turn each Part into an executable script automatically.

---

### Tool 18: `Reverse-Diff-Guard` — Prevent Agents from Destroying Progress

**Problem:** Agent runs `git reset --hard` or deletes files from core/. Everything lost.

**Solution:** A pre-wave snapshot + a post-wave guard that BLOCKS destructive operations:

```powershell
# At the START of a session, count files
$baseline = (Get-ChildItem -Recurse -Filter '*.java' -Path 'X:\projects\CustomNPC-Plus\core\src\main\java').Count

# VALIDATION (run periodically or after agent operations)
$current = (Get-ChildItem -Recurse -Filter '*.java' -Path 'X:\projects\CustomNPC-Plus\core\src\main\java').Count

if ($current -lt $baseline) {
    Write-Host "!!! ALERT: core/ lost $($baseline - $current) files! Was $baseline, now $current !!!"
    Write-Host "!!! DO NOT PROCEED. Check git status. !!!"
    exit 1
}
```

**Integration:** Run this check BEFORE and AFTER every wave execution. If file count drops, something went catastrophically wrong.

---

## COMBO ATTACKS — Composing Tools for Maximum Devastation

### Combo 1: "The Full Wave" (Tools 3 + 1 + 5 + 2 + 9 + 8 + 4)
```
Git Checkpoint → Mass Copy → MC Import Nuke → Symbol Swap → Static Transform → Stub Generator → Build Report
```
One command. Entire wave. < 15 seconds + build time.

### Combo 2: "The PA Blitz" (Tools 6 + 2)
```
Create 3 new PA interfaces + Propagate ALL of them across core/ simultaneously
```
One command. All interfaces created and wired. < 3 seconds.

### Combo 3: "The Split" (Tools 13 + 7 + 1)
```
Analyze a file for MC methods → Generate shadow with those methods → Copy MC-free portion to core
```
One command. File perfectly split. < 1 second.

### Combo 4: "The Recovery" (Tools 4 + 8 + 12)
```
Build Report → Analyze missing types → Auto-generate stubs for all of them
```
Build fails? Run this. Auto-fix. Rebuild. < 5 seconds.

### Combo 5: "The Status Check" (Tools 16 + 18 + 4)  
```
Migration Dashboard → File Count Guard → Build Report
```
Quick health check. < 10 seconds.

---

## Implementation Priority

| Priority | Tool | Impact | Effort |
|----------|------|--------|--------|
| 🔴 P0 | **Tool 11: Wave-Executor** | Combines everything, one command per wave | Medium (composes others) |
| 🔴 P0 | **Tool 2: Symbol-Swap** | Core of ALL transformations | Low |
| 🔴 P0 | **Tool 1: Mass-Copy** | Foundation | Trivial |
| 🔴 P0 | **Tool 4: Build-Reporter** | Prevents panic | Low |
| 🔴 P0 | **Tool 3: Git-Checkpoint** | Prevents destruction | Trivial |
| 🟡 P1 | **Tool 8: Stub-Generator** | Fixes "missing class" failures | Low |
| 🟡 P1 | **Tool 5: MC-Import-Nuker** | Quick cleanup | Trivial |
| 🟡 P1 | **Tool 9: Static-Call-Transformer** | Handles complex rewrites | Low |
| 🟡 P1 | **Tool 6: PA-Creator-Propagator** | Creates + wires interfaces atomically | Medium |
| 🟢 P2 | **Tool 12: Dependency-Scanner** | Pre-wave planning | Medium |
| 🟢 P2 | **Tool 7: Shadow-Generator** | Auto-generate mc1710 shadows | Medium |
| 🟢 P2 | **Tool 10: MC-Method-Quarantine** | Isolate MC code without deleting | Medium |
| 🟢 P2 | **Tool 14: Import-Deduplicator** | Cleanup after swaps | Trivial |
| 🔵 P3 | **Tool 16: Migration-Status-Dashboard** | Progress tracking | Low |
| 🔵 P3 | **Tool 18: Reverse-Diff-Guard** | Safety net | Trivial |
| 🔵 P3 | **Tool 13: Method-Extractor** | Help with SPLITTABLE files | High |
| 🔵 P3 | **Tool 17: Wave-Definition-Generator** | Auto-generate from plan | High |

---

## The Golden Rule

**Every tool follows one principle: NEVER DELETE. ONLY ADD AND TRANSFORM.**

- Build fails? Report the errors. Don't delete.
- Symbol missing? Create a stub. Don't delete.
- Method has MC code? Quarantine it. Don't delete.
- Wave went wrong? Git checkpoint protects you. Don't delete.

The agent's ONLY job is to define the inputs (file lists, swap tables, interface content) and execute. The tools handle everything else.

---

## APPRAISAL — What Actually Matters

After designing all 18 tools and thinking through how they compose, here's my honest assessment of what's truly essential vs. what's nice-to-have.

### The 4 Functions That Change Everything

**1. `SymbolSwap` (Tool 2) — THE weapon. Non-negotiable.**

This is the single most important function in the entire arsenal. 95% of what this migration does is mechanical type replacement. `NBTTagCompound` → `INbt`. `EntityPlayer` → `IPlayer`. Over and over, across hundreds of files. The fact that we were doing this one file at a time with read/edit cycles was insane. SymbolSwap does it across ALL files in ALL of core/ in under 2 seconds. But the KILLER feature isn't the replacement itself — it's the **import surgery** baked in. Adding the new import, removing the old one, in the same pass. That's what makes it complete. Without this, every swap leaves behind broken imports that require a second manual pass.

Every other function in this document is either a wrapper around SymbolSwap, a preparation step for SymbolSwap, or a cleanup step after SymbolSwap. It is the nucleus.

**2. `CreateAndPropagate` (Tool 6) — The atomic PA-creation bomb. Equally non-negotiable.**

I initially deprioritized this because "we only create ~4 new PA interfaces." That was WRONG. Here's what I missed:

This isn't just for NEW interfaces. It's the universal pattern for **any time you introduce a type that replaces an MC type.** That happens CONSTANTLY:
- Create `IMessageService.java` → replace ALL `ChatComponentText` / `ChatComponentTranslation` everywhere → fix all imports. ONE CALL.
- Create `IServerService.java` → replace ALL `MinecraftServer` references → fix all imports. ONE CALL.
- Create a new method on an existing PA interface → now propagate its usage. ONE CALL.
- Even for STUBS: create `SyncController.java` stub in core → replace all references to the mc1710 version → fix imports. ONE CALL.

The power is that **creation and propagation are ATOMIC.** The interface and ALL its usages across the entire codebase materialize in the SAME MILLISECOND. No "I created the interface but forgot to update 3 files" bugs. No "the import was added but the old import wasn't removed." It's all-or-nothing.

This is the function that makes Waves 2-8 possible without agent meltdown. When Wave 2 needs `IMessageService`, `IServerService`, and `ITranslationService`, you fire three `CreateAndPropagate` calls and the ENTIRE core/ is updated. Done.

**3. `GenerateStubs` (Tool 8) — The anti-panic drug. Saves every wave.**

This is the function that would have prevented EVERY catastrophic failure we've had so far. The pattern is always the same: agent copies files, runs SymbolSwap, builds, sees `cannot find symbol: SyncController`, and panics. SyncController hasn't been migrated to core yet. It's in Wave 7. We're in Wave 1. Of COURSE it's not there.

GenerateStubs creates a 5-line placeholder: `public class SyncController { public static void updateDialog(Object d) {} }`. Build passes. Agent moves on. When Wave 7 properly migrates SyncController, the stub gets REPLACED by the real implementation. Zero lost work. Zero panic. Zero git resets.

This is the philosophical shift: **build failures from missing future-wave classes are not errors, they're expected.** Stubs acknowledge this reality.

**4. `BuildReport` (Tool 4) — The translator between build output and agent action.**

Raw Gradle output is 500 lines of noise with 47 errors buried in it. Agents can't parse this effectively. They see a wall of red and assume "everything is broken, nuke it." BuildReport categorizes those 47 errors into maybe 4 buckets: "12 files reference SyncController (create stub)", "5 files still import net.minecraft.nbt (run SymbolSwap again)", "3 files use ChatComponentText (need IMessageService PA)". 

With categorized errors, the agent knows EXACTLY what to do next. It's not 47 independent problems — it's 4 patterns, each with a known fix. The agent runs the fix, rebuilds, and the error count drops from 47 to 0.

### The Supporting Cast (Important, Not Revolutionary)

**`MassCopy` (Tool 1):** Obviously needed but trivial. Just `Copy-Item` in a loop. Matters because agent tools have ~1 second overhead per file, so 25 files = 25 seconds vs < 1 second. But there's nothing clever here. It's plumbing.

**`StaticCallTransform` (Tool 9):** SymbolSwap's big brother. Handles `CompressedStreamTools.readCompressed(file)` → `PlatformServiceHolder.get().readCompressedNBT(file)` where you restructure a method call, not just rename a type. In practice, merge this INTO SymbolSwap as a second pass (regex transforms after simple replacements).

**`NukeImports` (Tool 5):** Safety net after mass-copy. Removes all `net.minecraft.*`, `cpw.mods.*`, `net.minecraftforge.*` imports from core/. Run BEFORE SymbolSwap to clean the slate. Trivial but prevents leftover import errors.

**`DedupeImports` (Tool 14):** Cleanup after SymbolSwap. Two MC types might map to the same PA type (e.g., `NBTTagCompound` and `NBTTagString` both → `INbt`), leaving duplicate imports. This deduplicates and sorts them.

### What I'd Deprioritize

**`ShadowGenerator` (Tool 7):** Shadows are too varied to template well. DialogController's shadow adds SyncController calls. BankData's shadow adds Container management. PlayerData's shadow adds IExtendedEntityProperties. There's no common pattern worth automating. Write shadows manually.

**`MCMethodQuarantine` (Tool 10):** Clever idea, but brace-counting in regex is fragile and Java methods can be complex (lambdas, inner classes, try-catch). For the SPLITTABLE files (Waves 7-8), manual analysis is safer.

**`WaveDefinitionGenerator` (Tool 17):** Parsing markdown tables to generate PowerShell arrays sounds cool but is brittle. Faster to just write the params by hand.

### The Minimum Viable Toolkit (What We Must Build)

If I had to ship with the FEWEST functions that still solve the problem:

```
MUST HAVE (the migration cannot succeed without these):
  1. SymbolSwap          — Replace types + imports across all files
  2. CreateAndPropagate  — Create PA interface + replace target everywhere atomically
  3. GenerateStubs       — Create placeholder classes for future-wave dependencies  
  4. BuildReport         — Categorize build errors into actionable patterns

SHOULD HAVE (saves significant time):
  5. MassCopy            — Bulk file copy with directory creation
  6. NukeImports         — Remove all MC imports from a directory
  7. StaticCallTransform — Regex-based method call rewriting
  8. DedupeImports       — Clean up duplicate imports after swaps

NICE TO HAVE (add later if needed):
  9. DependencyScanner   — Pre-wave analysis of what's missing
  10. WaveStatus         — Migration progress dashboard
  11. DiffGuard          — Prevent file count regression
```

### Why This Changes the Game

The old workflow:
```
Agent copies 1 file → reads it → edits imports → edits types → reads next file → ...
→ 50 tool calls later → builds → 100 errors → PANIC → git reset → all work lost
```

The new workflow:
```
. lib.ps1
MassCopy -Pairs @(...)           # 25 files in < 1 second
NukeImports -Directory 'core/...'  # Clean slate in < 1 second
SymbolSwap -Directory 'core/...' -Swaps @(...)  # All types fixed in < 2 seconds
CreateAndPropagate -FilePath '...' -Content '...' -OldClass '...' ...  # PA created + wired
GenerateStubs -Stubs @(...)      # Missing classes stubbed
BuildReport                       # "4 fixable patterns" not "100 scary errors"
→ Total: < 60 seconds including build. ZERO panic. ZERO deletion.
```

That's not a 10x improvement. That's a 100x improvement.
