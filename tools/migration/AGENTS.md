# AGENTS.md -- Migration Toolkit

> **READ THIS BEFORE TOUCHING ANY MIGRATION WORK.**
> These tools exist because agents kept panicking on build failures and deleting progress. The toolkit eliminates the need for slow read/edit/write cycles on individual files. One command = hundreds of file modifications in seconds.

---

## Golden Rules

1. **Tools are METHODS. You FEED them PARAMS. You NEVER edit their source. Unless they are broken or the core logic needs modification.**
2. **Every tool follows one principle: NEVER DELETE. ONLY ADD AND TRANSFORM.**
3. **Build failures after migration are EXPECTED. Do NOT panic. Do NOT delete files. Run `Build-Report` and fix categorically.**
4. **Always load tools from `.ps1` files using dot-sourcing (`. path/to/Script.ps1`). NEVER paste inline PowerShell through bash -- `$` escaping will destroy regex capture groups.**

---

## Quick Reference Card

| Tool | Type | Purpose | Key Params |
|------|------|---------|------------|
| **`pa_batch.py`** | **Python** | **BATCH create 5+ Java files from JSON manifest (auto-derives paths)** | **`manifest.json`, `--force`, `--dry-run`** |
| **`pa_editor.py`** | **Python** | **BATCH edit existing files from JSON manifest (atomic insert/delete/replace)** | **`manifest.json`, `--force`, `--dry-run`, `--backup`** |
| `Copy-Recursive` | PS | Copy files from mc1710 to core, recursively following imports | `-Sources`, `-AllowPaths` |
| `Symbol-Swap` | PS | Replace type names + import surgery across all files | `-Directory`, `-Swaps` |
| `Nuke-Imports` | PS | Remove all forbidden imports from a directory | `-Directory`, `-ForbiddenPrefixes` |
| `Create-And-Propagate` | PS | Create a PA interface AND replace its MC equivalent everywhere atomically | `-FilePath`, `-Content`, `-TargetDirectory`, `-Swaps` |
| `Generate-Stubs` | PS | Create temporary placeholder classes so the build passes | `-Stubs`, `-TargetRoot` |
| `Build-Report` | PS | Run Gradle build and categorize errors into actionable buckets | `-GradleTask`, `-MaxSamples` |
| `Static-Transform` | PS | Regex-based method call rewriting (structural changes) | `-Directory`, `-Transforms` |
| `Dedupe-Imports` | PS | Remove duplicate imports and sort | `-Directory` |

**PS tools auto-detect `$ProjectRoot` from script location. Override with `-ProjectRoot`.**  
**`pa_batch.py` auto-detects `ProjectRoot` by walking up for `settings.gradle`. Override with `--project-root`.**

---

## How to Load Tools

Always write a `.ps1` script file, then execute it with `-File`. This avoids bash `$` escaping issues that destroy regex capture groups like `$1`, `$2`.

```powershell
# In your .ps1 script file, load tools at the top:
. (Join-Path $PSScriptRoot 'Copy-Recursive.ps1')
. (Join-Path $PSScriptRoot 'Symbol-Swap.ps1')
. (Join-Path $PSScriptRoot 'Nuke-Imports.ps1')
. (Join-Path $PSScriptRoot 'Create-And-Propagate.ps1')
. (Join-Path $PSScriptRoot 'Generate-Stubs.ps1')
. (Join-Path $PSScriptRoot 'Build-Report.ps1')
. (Join-Path $PSScriptRoot 'Static-Transform.ps1')
. (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')
```

Then execute:
```bash
powershell -ExecutionPolicy Bypass -File "tools/migration/my_wave.ps1"
```

**WARNING: Single-swap calls require `,@()` syntax to prevent PowerShell array flattening:**
```powershell
# WRONG -- PowerShell flattens @(@('A','B','C','D')) into @('A','B','C','D')
Symbol-Swap -Directory '...' -Swaps @( @('Old', 'New', 'old.imp', 'new.imp') )

# CORRECT -- comma prefix forces array-of-arrays
Symbol-Swap -Directory '...' -Swaps @( ,@('Old', 'New', 'old.imp', 'new.imp') )

# Also correct -- 2+ entries don't need the comma trick
Symbol-Swap -Directory '...' -Swaps @(
    @('OldA', 'NewA', 'old.a', 'new.a'),
    @('OldB', 'NewB', 'old.b', 'new.b')
)
```

The tools have built-in guards for this, but it is still good practice.

---

## Tool Reference

### 0. PA Batch Writer (Python)

**File:** `pa_batch.py`  
**Purpose:** Batch-create 5+ Java source files from a single JSON manifest. Auto-derives file paths from package + class name. **THE PRIMARY FILE CREATION TOOL — use this instead of individual `write` calls.**

#### Usage

```bash
# Dry run (preview, no write)
python tools/migration/pa_batch.py manifest.json --dry-run

# Actually create files
python tools/migration/pa_batch.py manifest.json --force

# Machine-readable JSON output
python tools/migration/pa_batch.py manifest.json --dry-run --json

# Override project root
python tools/migration/pa_batch.py manifest.json --force --project-root /path/to/project
```

#### Manifest Format (JSON)

```json
{
  "root": "platform-api/src/main/java",
  "files": [
    {
      "content": "package noppes.npcs.api.entity;\n\npublic interface IFoo extends IBar {\n    String getName();\n}\n"
    },
    {
      "root": "core/src/main/java",
      "content": "package noppes.npcs.util;\n\npublic class Util {\n    public static int clamp(int v, int min, int max) { ... }\n}\n"
    },
    {
      "path": "src/main/java/noppes/npcs/wrappers/FooWrapper.java",
      "content": "package noppes.npcs.wrappers;\n\npublic class FooWrapper implements IFoo { ... }\n"
    }
  ]
}
```

#### Key Behaviors

**Auto-path derivation (KILLER FEATURE):**
- Parses `package noppes.npcs.api.entity;` from content
- Parses `public interface IFoo` from content
- Derives path: `platform-api/src/main/java/noppes/npcs/api/entity/IFoo.java`
- **No need to specify path for 90% of files** — just write raw Java, tool figures out where it goes

**Per-file root override:**
- Global `root` applies to all files unless overridden
- Each file entry can specify its own `root` (for mixed batches: PA interfaces + core + mc1710)

**Explicit path override:**
- If `path` is provided in entry, it takes precedence over auto-derivation
- Useful for non-standard locations or when auto-derivation would fail

**Safety defaults:**
- **No overwrite by default** — existing files are skipped, listed in SKIPPED summary
- Use `--force` to overwrite existing files
- Directories auto-created (`mkdir -p`)

**Validation:**
- Warns if explicit `path` doesn't match the package declaration (detects typos)
- Errors if `content` field missing
- Errors if no `package` declaration found (and no explicit `path`)
- Errors if no `public class/interface/enum/@interface` found (and no explicit `path`)

#### CLI Options

| Option | Default | Description |
|--------|---------|-------------|
| `manifest` | (required) | Path to JSON manifest file |
| `--force` | false | Overwrite existing files (default: skip) |
| `--dry-run` | false | Preview what would be created without writing |
| `--json` | false | Machine-readable JSON output (instead of human-readable) |
| `--project-root` | auto-detect | Project root (auto-walks up for `settings.gradle`) |

#### Output

**Human-readable (default):**
```
PA Batch Writer — 20 files processed
  CREATED:  12 files
  SKIPPED:   8 files (already exist)
  ERRORS:    0

Created:
  platform-api/src/main/java/noppes/npcs/api/entity/IFoo.java
  platform-api/src/main/java/noppes/npcs/api/entity/IBar.java
  ...
```

**Machine-readable (`--json`):**
```json
{
  "status": "success",
  "created_count": 12,
  "skipped_count": 8,
  "error_count": 0,
  "created": ["platform-api/src/main/java/noppes/npcs/api/entity/IFoo.java", ...],
  "skipped": ["platform-api/src/main/java/noppes/npcs/api/nbt/INbt.java", ...],
  "errors": [],
  "warnings": []
}
```

#### Performance Impact

**Batch creation eliminates tool call overhead:**
- Creating 30 files individually = 30+ tool calls = 60-90s overhead
- Creating 30 files via pa_batch.py = 2 tool calls (write manifest + execute) = 4-6s overhead
- **Result: 15x fewer round trips for large batches**

**Token usage:**
- Both approaches output the same Java source code
- Difference: batching reduces tool-call ceremony overhead, not token generation

#### Example Workflow

```bash
# Step 1: Compose manifest with PA interfaces + wrappers
cat > tools/migration/_my_wave_manifest.json << 'EOF'
{
  "root": "platform-api/src/main/java",
  "files": [
    { "content": "package noppes.npcs.api.entity; public interface IFoo { ... }" },
    { "content": "package noppes.npcs.api.entity; public interface IBar { ... }" },
    { "root": "src/main/java", "content": "package noppes.npcs.wrappers; public class FooWrapper { ... }" }
  ]
}
EOF

# Step 2: Dry-run to verify paths
python tools/migration/pa_batch.py tools/migration/_my_wave_manifest.json --dry-run

# Step 3: Create files
python tools/migration/pa_batch.py tools/migration/_my_wave_manifest.json --force

# Step 4: Propagate (existing tools)
Symbol-Swap -Directory 'core/src/main/java' -Swaps @(
    @('Foo', 'IFoo', 'net.minecraft.foo.Foo', 'noppes.npcs.api.entity.IFoo')
)
```

#### When to Use pa_batch.py vs Create-And-Propagate

| Scenario | Tool |
|----------|------|
| Creating 1 PA interface + propagating references everywhere | `Create-And-Propagate` |
| Creating 5+ PA interfaces / classes in one batch | **`pa_batch.py`** |
| Need to compose files programmatically (from code generation) | **`pa_batch.py`** (write manifest, run tool) |
| Creating mixed batch (PA interfaces + core + mc1710 wrappers) | **`pa_batch.py`** (per-file root override) |
| One-off single file | Direct `write` tool (slower but acceptable) |

---

### 0b. PA Batch Editor (Python)

**File:** `pa_editor.py`
**Purpose:** Batch-edit existing files from a single JSON manifest with atomic transaction semantics. **THE PRIMARY FILE EDITING TOOL — use this for surgical multi-file edits instead of individual `edit` calls.**

Companion to `pa_batch.py` (which creates files). This tool EDITS existing files.

#### Usage

```bash
# Dry run — preview diffs without writing (default if --force not given)
python tools/migration/pa_editor.py manifest.json --dry-run

# Actually apply edits
python tools/migration/pa_editor.py manifest.json --force

# Apply with backups (.bak files)
python tools/migration/pa_editor.py manifest.json --force --backup

# Machine-readable JSON output
python tools/migration/pa_editor.py manifest.json --dry-run --json

# Override project root
python tools/migration/pa_editor.py manifest.json --force --project-root /path/to/project
```

#### Manifest Format (JSON)

```json
{
  "edits": [
    {
      "path": "core/src/main/java/noppes/npcs/controllers/FactionController.java",
      "ops": [
        {
          "op": "replace",
          "target": {"pattern": "^import net\\.minecraft\\.nbt\\.NBTTagCompound;"},
          "content": "import noppes.npcs.api.nbt.INbt;"
        },
        {
          "op": "insert",
          "target": {"pattern": "^public class"},
          "position": "before",
          "content": "/** Migrated to PA types. */"
        },
        {
          "op": "delete",
          "target": {"line": 5},
          "count": 2
        }
      ]
    }
  ]
}
```

#### Operations

| Op | Required Fields | Description |
|----|----------------|-------------|
| `replace` | `target`, `content` | Replace `count` lines (default 1) starting at target with `content` |
| `insert` | `target`, `content`, `position` | Insert `content` before/after target line. `position`: `"before"` or `"after"` (default: `"after"`) |
| `delete` | `target` | Delete `count` lines (default 1) starting at target |

#### Targeting

Each op has a `target` that resolves to a line. Two modes:

| Mode | Format | Description |
|------|--------|-------------|
| Line number | `{"line": 10}` | 1-based line number |
| Regex pattern | `{"pattern": "^import.*NBT"}` | First line matching regex |
| Pattern + occurrence | `{"pattern": "^import", "occurrence": 3}` | 3rd match of pattern |

#### Content Format

`content` accepts either a string or a list of strings:

```json
{"content": "single line replacement"}
{"content": "line 1\nline 2\nline 3"}
{"content": ["line 1", "line 2", "line 3"]}
```

#### Atomicity

**All-or-nothing transaction model:**
1. PHASE 1: Validate all edit specs structurally (no I/O)
2. PHASE 2: Read all files, resolve all targets, compute all results in memory
3. PHASE 3: Write all files (or rollback on any failure)

If ANY edit in ANY file fails validation or application, NO files are written. This prevents partial state where some files are edited and others aren't.

#### CLI Options

| Option | Default | Description |
|--------|---------|-------------|
| `manifest` | (required) | Path to JSON edit manifest file |
| `--force` | false | Actually write edits (default: dry-run) |
| `--dry-run` | true | Show diffs without writing |
| `--backup` | false | Save `.bak` copies before editing |
| `--json` | false | Machine-readable JSON output |
| `--project-root` | auto-detect | Project root override |

#### Output

**Human-readable (default):**
```
PA Batch Editor — 4 files, 14 edits
  MODIFIED: 4 files
  ERRORS:   0

Modified:
  core/.../FactionController.java (3 edits)
  core/.../Faction.java (4 edits)
```

**Dry-run shows unified diffs:**
```
--- a/core/.../FactionController.java
+++ b/core/.../FactionController.java
@@ -3,4 +3,4 @@
-import net.minecraft.nbt.NBTTagCompound;
+import noppes.npcs.api.nbt.INbt;
```

#### When to Use pa_editor.py vs Other Tools

| Scenario | Tool |
|----------|------|
| Bulk import replacement across all files in a directory | `Symbol-Swap` (PS) |
| Surgical edits to specific lines in specific files | **`pa_editor.py`** |
| Adding `implements` clauses, inserting methods, deleting blocks | **`pa_editor.py`** |
| Post-migration fixups (add imports, change signatures) | **`pa_editor.py`** |
| Creating new files from scratch | `pa_batch.py` |
| Regex-based method call rewriting | `Static-Transform` (PS) |

#### Example: Post-Migration Import + Signature Fixup

```json
{
  "edits": [
    {
      "path": "core/src/main/java/noppes/npcs/controllers/QuestController.java",
      "ops": [
        {"op": "replace", "target": {"pattern": "^import net\\.minecraft\\.nbt\\.NBTTagCompound;"}, "content": "import noppes.npcs.api.nbt.INbt;"},
        {"op": "replace", "target": {"pattern": "^import net\\.minecraft\\.nbt\\.NBTTagList;"}, "content": "import noppes.npcs.api.nbt.INbtList;"},
        {"op": "insert", "target": {"pattern": "^import noppes.npcs.api.nbt.INbt;"}, "position": "after", "content": "import noppes.npcs.api.nbt.NBT;"},
        {"op": "replace", "target": {"pattern": "public void loadData\\(NBTTagCompound"}, "content": "    public void loadData(INbt nbt) {"}
      ]
    },
    {
      "path": "core/src/main/java/noppes/npcs/controllers/DialogController.java",
      "ops": [
        {"op": "replace", "target": {"pattern": "^import net\\.minecraft\\.nbt\\.NBTTagCompound;"}, "content": "import noppes.npcs.api.nbt.INbt;"},
        {"op": "insert", "target": {"pattern": "public class DialogController"}, "position": "before", "content": "/** Migrated to core — platform-api types only. */"}
      ]
    }
  ]
}
```

#### Testing

```bash
python tools/migration/test_pa_editor.py
```

17 tests covering: replace, insert before/after, delete, multi-line insert, multi-line replace, dry-run, JSON output, backup, file-not-found, line-out-of-bounds, pattern-not-found, invalid-regex, atomicity (partial failure rollback), multi-file, occurrence targeting, list content, structural validation.

---

### 1. Copy-Recursive

**File:** `Copy-Recursive.ps1`
**Purpose:** Recursively copy Java files from mc1710 to core, chasing import dependencies.

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `-Sources` | Yes | -- | Array of relative paths within McRoot (e.g., `'noppes/npcs/controllers/DialogController.java'`) |
| `-AllowPaths` | No | `@()` (all allowed) | WHITELIST of path prefixes. Only files under these paths get recursively copied. Initial `-Sources` always bypass this. |
| `-McRoot` | No | `'mc1710/src/main/java'` | Source root |
| `-CoreRoot` | No | `'core/src/main/java'` | Destination root |
| `-ProjectRoot` | No | Auto-detected | Absolute project root |
| `-ForbiddenPrefixes` | No | java/MC/Forge/etc | Import prefixes never followed |

**Behavior:**
- Initial `-Sources` files are ALWAYS copied regardless of whitelist.
- Files already in core are NOT re-copied but ARE scanned for transitive deps.
- Handles wildcard imports (`import foo.bar.*`), static imports, inner class imports.
- Returns array of copied file paths.

**Output summary shows:** COPIED, EXISTS, skipped(not whitelisted), not found.

---

### 2. Symbol-Swap

**File:** `Symbol-Swap.ps1`
**Purpose:** Bulk replace type names and perform import surgery across all .java files in a directory.

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `-Directory` | Yes | -- | Relative path to scan recursively |
| `-Swaps` | Yes | -- | Array of `@(oldSymbol, newSymbol, oldImport, newImport)` |
| `-ProjectRoot` | No | Auto-detected | Absolute project root |

**Swap tuple format:** `@('OldTypeName', 'NewTypeName', 'old.full.qualified.Import', 'new.full.qualified.Import')`
- Use `''` (empty string) for newImport if no import should be added.

**Operation order (critical for correctness):**
1. Remove ALL old imports first
2. Replace symbols in code body
3. Add new imports

This prevents the bug where replacing `NBTTagCompound` with `INbt` inside an import line makes the import unremovable.

---

### 3. Nuke-Imports

**File:** `Nuke-Imports.ps1`
**Purpose:** Remove ALL imports matching forbidden prefixes from every .java file in a directory.

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `-Directory` | Yes | -- | Relative path to scan recursively |
| `-ForbiddenPrefixes` | Yes | -- | Array of dotted package prefixes (e.g., `'net.minecraft.'`) |
| `-ProjectRoot` | No | Auto-detected | Absolute project root |

**When to use:** Run AFTER Copy-Recursive, BEFORE Symbol-Swap. Removes the worst offenders so Symbol-Swap only handles the semantic replacements.

---

### 4. Create-And-Propagate

**File:** `Create-And-Propagate.ps1`
**Purpose:** Atomically create a new file (usually a PA interface) AND replace its MC equivalent everywhere.

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `-FilePath` | Yes | -- | Relative path for the new file (e.g., `'platform-api/src/main/java/.../IFoo.java'`) |
| `-Content` | Yes | -- | Full Java source content of the new file |
| `-TargetDirectory` | Yes | -- | Relative path to scan for propagation |
| `-Swaps` | Yes | -- | Same format as Symbol-Swap |
| `-ProjectRoot` | No | Auto-detected | Absolute project root |

**Why this exists:** Creation and propagation are ATOMIC. The interface and ALL its usages appear in the same millisecond. No "I created the interface but forgot to update 3 files" bugs.

---

### 5. Generate-Stubs

**File:** `Generate-Stubs.ps1`
**Purpose:** Create temporary placeholder classes so the build passes despite missing future-wave dependencies.

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `-Stubs` | Yes | -- | Array of hashtables with `pkg`, `name`, `type`, `body` |
| `-TargetRoot` | No | `'core/src/main/java'` | Where stubs are created |
| `-ProjectRoot` | No | Auto-detected | Absolute project root |

**Stub hashtable format:**
```powershell
@{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class';
   body='    public static void updateDialog(Object d) {}
    public static void removeDialog(int id) {}' }
```

**Safety:** Will NOT overwrite real classes (only replaces files containing the `TEMPORARY STUB` marker).

---

### 6. Build-Report

**File:** `Build-Report.ps1`
**Purpose:** Run a Gradle build and categorize errors into actionable buckets instead of dumping raw output.

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `-GradleTask` | Yes | -- | Gradle task to run (e.g., `':core:compileJava'`) |
| `-MaxSamples` | No | `3` | Max errors shown per category |
| `-ProjectRoot` | No | Auto-detected | Absolute project root |

**Error categories:**
- `MISSING_CLASS:ClassName` -- needs stub or PA interface
- `MISSING_PACKAGE:pkg.name` -- leftover MC import, run Nuke-Imports or Symbol-Swap
- `MISSING_METHOD:methodName` -- method signature changed or missing
- `MISSING_VARIABLE:varName` -- field not migrated
- `MISSING_SYMBOL` -- general "cannot find symbol" (multi-line Gradle output)
- `TYPE_MISMATCH` -- incompatible types, usually needs a cast or wrapper
- `BAD_ARGS` -- wrong argument types to a method
- `ABSTRACT_VIOLATION` -- class doesn't implement required abstract methods
- `UNCHECKED_EXCEPTION` -- missing throws/try-catch
- `OTHER` -- anything uncategorized

**Returns:** `@{ Success = $bool; Errors = $hashtable; Total = $int }`

---

### 7. Static-Transform

**File:** `Static-Transform.ps1`
**Purpose:** Regex-based method call rewriting. For structural changes that Symbol-Swap can't handle.

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `-Directory` | Yes | -- | Relative path to scan recursively |
| `-Transforms` | Yes | -- | Array of `@(regexPattern, replacement)` |
| `-ProjectRoot` | No | Auto-detected | Absolute project root |

**Transform tuple format:** `@('regex\.pattern\(([^)]+)\)', 'replacement($1)')`
- Uses .NET regex syntax. `$1`, `$2` etc. are capture group references in the replacement.

---

### 8. Dedupe-Imports

**File:** `Dedupe-Imports.ps1`
**Purpose:** Remove duplicate import lines and sort. Run LAST, after all other tools.

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `-Directory` | Yes | -- | Relative path to scan recursively |
| `-ProjectRoot` | No | Auto-detected | Absolute project root |

---

## Complex Workflow Examples

These examples show end-to-end migration workflows.

**For PowerShell workflows:** Write a `.ps1` script file and execute with `-File`. **NEVER paste inline through bash.**

**For Python + batch creation:** Compose the JSON manifest in a file, then invoke `pa_batch.py`. Recommended for waves that need multiple PA interfaces.

---

## PA Batch Writer Integration Pattern

**When you need to create 5+ Java files in a migration wave, use this pattern:**

```bash
# Step 1: Agent composes manifest JSON with all PA interfaces + wrappers + utility classes
write("tools/migration/_wave_manifest.json", {all files' raw Java content})

# Step 2: Verify paths with dry-run
python tools/migration/pa_batch.py tools/migration/_wave_manifest.json --dry-run

# Step 3: Create files
python tools/migration/pa_batch.py tools/migration/_wave_manifest.json --force

# Step 4: Propagate (existing PowerShell tools)
powershell -ExecutionPolicy Bypass -File tools/migration/wave_propagate.ps1
```

**Benefit:** 5+ files created in 2 tool calls (write manifest + execute) instead of 5+ individual `write` calls.

For single PA interface creation, use `Create-And-Propagate` (simpler, all-in-one). For batches, use `pa_batch.py` (higher throughput, lower overhead).

---

### WORKFLOW 1: Full Wave Migration (Copy + Nuke + Swap + Stub + Build)

This is the standard workflow for migrating a batch of files from mc1710 to core. Used for any migration wave.

**Scenario:** Migrate Dialog system controllers and data classes.

```powershell
# File: tools/migration/wave_dialog.ps1
# Execute: powershell -ExecutionPolicy Bypass -File tools/migration/wave_dialog.ps1

. (Join-Path $PSScriptRoot 'Copy-Recursive.ps1')
. (Join-Path $PSScriptRoot 'Nuke-Imports.ps1')
. (Join-Path $PSScriptRoot 'Symbol-Swap.ps1')
. (Join-Path $PSScriptRoot 'Static-Transform.ps1')
. (Join-Path $PSScriptRoot 'Generate-Stubs.ps1')
. (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')
. (Join-Path $PSScriptRoot 'Build-Report.ps1')

# ============================================================
# STEP 1: Copy files from mc1710 to core, recursively pulling deps
# ============================================================
# The whitelist constrains which TRANSITIVE deps get copied.
# Initial sources always get copied regardless of whitelist.
Copy-Recursive `
    -Sources @(
        'noppes/npcs/controllers/DialogController.java',
        'noppes/npcs/controllers/data/Dialog.java',
        'noppes/npcs/controllers/data/DialogOption.java',
        'noppes/npcs/controllers/data/DialogCategory.java'
    ) `
    -AllowPaths @(
        'noppes/npcs/controllers/',
        'noppes/npcs/controllers/data/',
        'noppes/npcs/config/',
        'noppes/npcs/util/'
    )

# ============================================================
# STEP 2: Nuke ALL MC/Forge imports from core
# ============================================================
Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes @(
    'net.minecraft.',
    'cpw.mods.',
    'net.minecraftforge.',
    'noppes.npcs.entity.',
    'noppes.npcs.client.',
    'noppes.npcs.mixin.',
    'kamkeel.npcs.network.',
    'noppes.npcs.containers.'
)

# ============================================================
# STEP 3: Symbol-Swap - replace MC types with PA interfaces
# ============================================================
Symbol-Swap -Directory 'core/src/main/java' -Swaps @(
    @('NBTTagCompound',  'INbt',     'net.minecraft.nbt.NBTTagCompound',           'noppes.npcs.api.nbt.INbt'),
    @('NBTTagList',      'INbtList', 'net.minecraft.nbt.NBTTagList',               'noppes.npcs.api.nbt.INbtList'),
    @('EntityPlayer',    'IPlayer',  'net.minecraft.entity.player.EntityPlayer',    'noppes.npcs.api.entity.IPlayer'),
    @('EntityPlayerMP',  'IPlayer',  'net.minecraft.entity.player.EntityPlayerMP',  'noppes.npcs.api.entity.IPlayer'),
    @('ItemStack',       'IItemStack','net.minecraft.item.ItemStack',              'noppes.npcs.api.item.IItemStack')
)

# ============================================================
# STEP 4: Static transforms - structural code rewrites
# ============================================================
Static-Transform -Directory 'core/src/main/java' -Transforms @(
    @('new\s+NBTTagCompound\s*\(\)',  'NBT.compound()'),
    @('new\s+NBTTagList\s*\(\)',      'NBT.list()'),
    @('CompressedStreamTools\.readCompressed\s*\(([^)]+)\)',              'PlatformServiceHolder.get().readCompressedNBT($1)'),
    @('CompressedStreamTools\.writeCompressed\s*\(([^,]+),\s*([^)]+)\)', 'PlatformServiceHolder.get().writeCompressedNBT($1, $2)')
)

# ============================================================
# STEP 5: Generate stubs for classes not yet migrated
# ============================================================
Generate-Stubs -Stubs @(
    @{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class';
       body='    public static void updateDialog(Object d) {}
    public static void removeDialog(int id) {}
    public static void updateQuest(Object q) {}
    public static void removeQuest(int id) {}' },
    @{ pkg='noppes.npcs.controllers'; name='ScriptController'; type='class';
       body='    public static boolean HasStart = false;' }
)

# ============================================================
# STEP 6: Deduplicate imports
# ============================================================
Dedupe-Imports -Directory 'core/src/main/java'

# ============================================================
# STEP 7: Build and report (DO NOT PANIC on errors)
# ============================================================
Build-Report -GradleTask ':core:compileJava'
```

**What happens:** 
1. Files copied from mc1710 to core with all allowed transitive deps
2. MC imports stripped
3. Types replaced with PA equivalents
4. Constructor/static method patterns rewritten
5. Missing deps stubbed
6. Import duplicates cleaned
7. Build errors categorized for targeted fixing

---

### WORKFLOW 2: Create New PA Abstraction + Propagate Everywhere

This workflow is for when you need to introduce a NEW platform-api interface to replace an MC type. The interface is created AND all references across core/ are updated in a single atomic operation.

**Scenario:** Create `IMessageService` to replace `ChatComponentText` and `ChatComponentTranslation`.

```powershell
# File: tools/migration/create_message_service.ps1

. (Join-Path $PSScriptRoot 'Create-And-Propagate.ps1')
. (Join-Path $PSScriptRoot 'Nuke-Imports.ps1')
. (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')
. (Join-Path $PSScriptRoot 'Build-Report.ps1')

# ============================================================
# STEP 1: Define the new interface content
# ============================================================
$interfaceContent = @'
package noppes.npcs.api.handler;

import noppes.npcs.api.entity.IPlayer;

/**
 * Platform abstraction for Minecraft chat/message sending.
 * mc1710 implementation wraps ChatComponentText/Translation.
 */
public interface IMessageService {
    void sendMessage(IPlayer player, String message);
    void sendTranslatedMessage(IPlayer player, String key, Object... args);
    void broadcastMessage(String message);
}
'@

# ============================================================
# STEP 2: Create the interface AND propagate replacements
# ============================================================
Create-And-Propagate `
    -FilePath 'platform-api/src/main/java/noppes/npcs/api/handler/IMessageService.java' `
    -Content $interfaceContent `
    -TargetDirectory 'core/src/main/java' `
    -Swaps @(
        @('ChatComponentText',        'IMessageService', 'net.minecraft.util.ChatComponentText',        'noppes.npcs.api.handler.IMessageService'),
        @('ChatComponentTranslation', 'IMessageService', 'net.minecraft.util.ChatComponentTranslation', 'noppes.npcs.api.handler.IMessageService')
    )

# ============================================================
# STEP 3: Also nuke leftover chat formatting imports
# ============================================================
Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes @(
    'net.minecraft.util.EnumChatFormatting'
)

# ============================================================
# STEP 4: Clean up duplicates and verify
# ============================================================
Dedupe-Imports -Directory 'core/src/main/java'
Build-Report -GradleTask ':core:compileJava'
```

**What happens:**
1. `IMessageService.java` is created in `platform-api/`
2. Every `ChatComponentText` and `ChatComponentTranslation` reference in core/ is replaced with `IMessageService`
3. Old MC imports removed, new PA import added -- in every file that had the reference
4. All in one atomic operation -- no partial state

---

### WORKFLOW 3: Build-Report-Driven Fix Cycle

This workflow is for when you have build errors after a migration and need to fix them systematically. The pattern is: report -> categorize -> fix per category -> re-report -> repeat.

**Scenario:** After a migration wave, core has 47 build errors. Fix them categorically.

```powershell
# File: tools/migration/fix_cycle.ps1

. (Join-Path $PSScriptRoot 'Symbol-Swap.ps1')
. (Join-Path $PSScriptRoot 'Nuke-Imports.ps1')
. (Join-Path $PSScriptRoot 'Generate-Stubs.ps1')
. (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')
. (Join-Path $PSScriptRoot 'Build-Report.ps1')

# ============================================================
# STEP 1: Get the initial error report
# ============================================================
Write-Host "===== INITIAL BUILD =====" -ForegroundColor Cyan
$report = Build-Report -GradleTask ':core:compileJava' -MaxSamples 5

if ($report.Success) {
    Write-Host "Build already passes!" -ForegroundColor Green
    exit 0
}

# ============================================================
# STEP 2: Fix MISSING_PACKAGE errors (leftover MC imports)
# ============================================================
# These mean Nuke-Imports missed some prefixes. Fix:
Write-Host "`n===== FIXING: Leftover MC imports =====" -ForegroundColor Yellow

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
    'noppes.npcs.wrapper.',
    'noppes.npcs.scripted.',
    'noppes.npcs.janino.'
)

# ============================================================
# STEP 3: Fix MISSING_CLASS errors (need stubs)
# ============================================================
# These are classes from future waves. Create stubs so build passes.
Write-Host "`n===== FIXING: Missing class stubs =====" -ForegroundColor Yellow

Generate-Stubs -Stubs @(
    @{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class';
       body='    public static void syncUpdate(Object obj, Object... args) {}
    public static void syncRemove(String type, int id) {}' },
    @{ pkg='noppes.npcs.controllers'; name='ScriptController'; type='class';
       body='    public static boolean HasStart = false;
    public static boolean HasScriptEngine = false;' },
    @{ pkg='noppes.npcs'; name='CustomNpcs'; type='class';
       body='    public static java.io.File Dir = new java.io.File(".");
    public static int DefaultInteractLine = 0;' },
    @{ pkg='noppes.npcs'; name='EventHooks'; type='class';
       body='    // STUB' },
    @{ pkg='noppes.npcs'; name='LogWriter'; type='class';
       body='    public static void info(String msg) {}
    public static void warn(String msg) {}
    public static void error(String msg) {}
    public static void error(String msg, Throwable e) {}' }
)

# ============================================================
# STEP 4: Clean up and re-test
# ============================================================
Dedupe-Imports -Directory 'core/src/main/java'

Write-Host "`n===== SECOND BUILD =====" -ForegroundColor Cyan
$report2 = Build-Report -GradleTask ':core:compileJava' -MaxSamples 5

if ($report2.Success) {
    Write-Host "`nAll errors fixed!" -ForegroundColor Green
} else {
    Write-Host "`nRemaining errors: $($report2.Total) (was $($report.Total))" -ForegroundColor Yellow
    Write-Host "Review the report above and create additional stubs or swaps as needed."
}
```

**What happens:**
1. First build report shows categorized errors
2. `MISSING_PACKAGE` errors fixed by nuking leftover MC imports
3. `MISSING_CLASS` errors fixed by generating stubs
4. Second build report shows remaining errors (should be much fewer)
5. Agent reviews remaining errors and adds more fixes if needed

---

### WORKFLOW 4: Targeted Controller Migration (Copy -> Swap -> Stub -> Verify)

This is the most common real-world pattern: migrate a specific controller and its data classes.

**Scenario:** Migrate `QuestController` and all quest data classes.

```powershell
# File: tools/migration/wave_quests.ps1

. (Join-Path $PSScriptRoot 'Copy-Recursive.ps1')
. (Join-Path $PSScriptRoot 'Nuke-Imports.ps1')
. (Join-Path $PSScriptRoot 'Symbol-Swap.ps1')
. (Join-Path $PSScriptRoot 'Static-Transform.ps1')
. (Join-Path $PSScriptRoot 'Generate-Stubs.ps1')
. (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')
. (Join-Path $PSScriptRoot 'Build-Report.ps1')

# ============================================================
# STEP 1: Copy the quest subsystem and its dependencies
# ============================================================
# AllowPaths controls what transitive deps get pulled in.
# Quest files import controllers/data, config, and utils -- allow those.
# Do NOT allow entity/, client/, network/ -- those stay in mc1710.
Copy-Recursive `
    -Sources @(
        'noppes/npcs/controllers/QuestController.java',
        'noppes/npcs/controllers/PlayerQuestController.java',
        'noppes/npcs/quests/QuestItem.java',
        'noppes/npcs/quests/QuestKill.java',
        'noppes/npcs/quests/QuestLocation.java',
        'noppes/npcs/quests/QuestDialog.java',
        'noppes/npcs/quests/QuestManual.java'
    ) `
    -AllowPaths @(
        'noppes/npcs/controllers/',
        'noppes/npcs/controllers/data/',
        'noppes/npcs/quests/',
        'noppes/npcs/config/',
        'noppes/npcs/util/',
        'kamkeel/npcs/controllers/',
        'kamkeel/npcs/config/'
    )

# ============================================================
# STEP 2: Nuke MC imports from core
# ============================================================
Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes @(
    'net.minecraft.', 'cpw.mods.', 'net.minecraftforge.',
    'noppes.npcs.entity.', 'noppes.npcs.client.',
    'noppes.npcs.mixin.', 'kamkeel.npcs.network.',
    'kamkeel.npcs.entity.', 'noppes.npcs.containers.',
    'noppes.npcs.scripted.', 'noppes.npcs.janino.',
    'noppes.npcs.wrapper.'
)

# ============================================================
# STEP 3: Symbol-Swap MC types for PA types
# ============================================================
Symbol-Swap -Directory 'core/src/main/java' -Swaps @(
    @('NBTTagCompound',        'INbt',              'net.minecraft.nbt.NBTTagCompound',           'noppes.npcs.api.nbt.INbt'),
    @('NBTTagList',            'INbtList',           'net.minecraft.nbt.NBTTagList',               'noppes.npcs.api.nbt.INbtList'),
    @('EntityPlayer',          'IPlayer',            'net.minecraft.entity.player.EntityPlayer',   'noppes.npcs.api.entity.IPlayer'),
    @('EntityPlayerMP',        'IPlayer',            'net.minecraft.entity.player.EntityPlayerMP', 'noppes.npcs.api.entity.IPlayer'),
    @('ItemStack',             'IItemStack',         'net.minecraft.item.ItemStack',               'noppes.npcs.api.item.IItemStack'),
    @('EntityLivingBase',      'IEntityLivingBase',  'net.minecraft.entity.EntityLivingBase',      'noppes.npcs.api.entity.IEntityLivingBase'),
    @('EntityNPCInterface',    'ICustomNpc',         'noppes.npcs.entity.EntityNPCInterface',      'noppes.npcs.api.entity.ICustomNpc')
)

# ============================================================
# STEP 4: Static transforms
# ============================================================
Static-Transform -Directory 'core/src/main/java' -Transforms @(
    @('new\s+NBTTagCompound\s*\(\)',  'NBT.compound()'),
    @('new\s+NBTTagList\s*\(\)',      'NBT.list()')
)

# ============================================================
# STEP 5: Stubs for deps not yet migrated
# ============================================================
Generate-Stubs -Stubs @(
    @{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class';
       body='    public static void updateQuest(Object q) {}
    public static void removeQuest(int id) {}
    public static void updateDialog(Object d) {}
    public static void removeDialog(int id) {}' },
    @{ pkg='noppes.npcs.controllers'; name='ScriptController'; type='class';
       body='    public static boolean HasStart = false;' },
    @{ pkg='noppes.npcs'; name='CustomNpcs'; type='class';
       body='    public static java.io.File Dir = new java.io.File(".");' },
    @{ pkg='noppes.npcs'; name='LogWriter'; type='class';
       body='    public static void info(String msg) {}
    public static void warn(String msg) {}
    public static void error(String msg) {}
    public static void error(String msg, Throwable e) {}' }
)

# ============================================================
# STEP 6: Clean up and build
# ============================================================
Dedupe-Imports -Directory 'core/src/main/java'
Build-Report -GradleTask ':core:compileJava'
```

---

### WORKFLOW 5: Multiple PA Interfaces in One Shot

When a migration wave needs several new PA abstractions created simultaneously.

```powershell
# File: tools/migration/create_multiple_pa.ps1

. (Join-Path $PSScriptRoot 'Create-And-Propagate.ps1')
. (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')
. (Join-Path $PSScriptRoot 'Build-Report.ps1')

# ============================================================
# Interface 1: IServerService
# ============================================================
Create-And-Propagate `
    -FilePath 'platform-api/src/main/java/noppes/npcs/api/handler/IServerService.java' `
    -Content @'
package noppes.npcs.api.handler;

public interface IServerService {
    boolean isDedicatedServer();
    java.io.File getWorldDirectory();
}
'@ `
    -TargetDirectory 'core/src/main/java' `
    -Swaps @(
        @('MinecraftServer', 'IServerService', 'net.minecraft.server.MinecraftServer', 'noppes.npcs.api.handler.IServerService'),
        @('DedicatedServer', 'IServerService', 'net.minecraft.server.dedicated.DedicatedServer', 'noppes.npcs.api.handler.IServerService')
    )

# ============================================================
# Interface 2: IDamageService
# ============================================================
Create-And-Propagate `
    -FilePath 'platform-api/src/main/java/noppes/npcs/api/handler/IDamageService.java' `
    -Content @'
package noppes.npcs.api.handler;

public interface IDamageService {
    // Platform damage abstraction
}
'@ `
    -TargetDirectory 'core/src/main/java' `
    -Swaps @(
        ,@('DamageSource', 'IDamageService', 'net.minecraft.util.DamageSource', 'noppes.npcs.api.handler.IDamageService')
    )

# ============================================================
# Clean up and verify
# ============================================================
Dedupe-Imports -Directory 'core/src/main/java'
Build-Report -GradleTask ':core:compileJava'
```

---

### WORKFLOW 6: Batch PA Interface Creation (pa_batch.py + Propagate)

**NEW: Use this when you need to create 5+ PA interfaces in one wave.**

This workflow demonstrates the power of `pa_batch.py` for high-throughput file creation.

**Scenario:** Create multiple PA interfaces (IServerService, IMessageService, IIOService, etc.) and propagate them everywhere.

```bash
# File: tools/migration/_quest_wave_manifest.json
# (Agent composes this JSON with all PA interfaces + wrappers)
cat > tools/migration/_quest_wave_manifest.json << 'EOF'
{
  "root": "platform-api/src/main/java",
  "files": [
    {
      "content": "package noppes.npcs.api.handler;\n\npublic interface IServerService {\n    boolean isDedicatedServer();\n    Object getPlayerList();\n}\n"
    },
    {
      "content": "package noppes.npcs.api.handler;\n\nimport noppes.npcs.api.entity.IPlayer;\n\npublic interface IMessageService {\n    void sendMessage(IPlayer player, String message);\n    void broadcastMessage(String message);\n}\n"
    },
    {
      "content": "package noppes.npcs.api.handler;\n\nimport noppes.npcs.api.nbt.INbt;\n\npublic interface IIOService {\n    INbt readCompressed(java.io.DataInputStream stream);\n    void writeCompressed(INbt nbt, java.io.DataOutputStream stream);\n}\n"
    },
    {
      "root": "src/main/java",
      "content": "package noppes.npcs.wrappers.handler;\n\nimport net.minecraft.server.MinecraftServer;\nimport noppes.npcs.api.handler.IServerService;\n\npublic class ServerServiceWrapper implements IServerService {\n    private MinecraftServer server;\n    public ServerServiceWrapper(MinecraftServer server) { this.server = server; }\n    @Override\n    public boolean isDedicatedServer() { return server instanceof net.minecraft.server.dedicated.DedicatedServer; }\n    @Override\n    public Object getPlayerList() { return server.getPlayerList(); }\n}\n"
    }
  ]
}
EOF

# Step 1: Dry-run to verify all paths are correct
python tools/migration/pa_batch.py tools/migration/_quest_wave_manifest.json --dry-run
# Output shows exactly where each file will be created

# Step 2: Create all files at once
python tools/migration/pa_batch.py tools/migration/_quest_wave_manifest.json --force
# 3 PA interfaces + 1 wrapper = 4 files created in one command

# Step 3: Propagate (PowerShell script with Symbol-Swap calls)
# File: tools/migration/quest_wave_propagate.ps1
powershell -ExecutionPolicy Bypass -File tools/migration/quest_wave_propagate.ps1
```

**quest_wave_propagate.ps1:**
```powershell
. (Join-Path $PSScriptRoot 'Symbol-Swap.ps1')
. (Join-Path $PSScriptRoot 'Nuke-Imports.ps1')
. (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')
. (Join-Path $PSScriptRoot 'Build-Report.ps1')

# Nuke leftover MC imports from core
Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes @(
    'net.minecraft.server.',
    'net.minecraft.util.text'
)

# Propagate the new PA interfaces
Symbol-Swap -Directory 'core/src/main/java' -Swaps @(
    @('MinecraftServer', 'IServerService', 'net.minecraft.server.MinecraftServer', 'noppes.npcs.api.handler.IServerService'),
    @('ChatComponentText', 'IMessageService', 'net.minecraft.util.ChatComponentText', 'noppes.npcs.api.handler.IMessageService'),
    @('CompressedStreamTools', 'IIOService', 'net.minecraft.nbt.CompressedStreamTools', 'noppes.npcs.api.handler.IIOService')
)

Dedupe-Imports -Directory 'core/src/main/java'
Build-Report -GradleTask ':core:compileJava'
```

**Performance comparison:**

| Approach | Tool Calls | Wall Clock | Best For |
|----------|-----------|-----------|----------|
| Individual `write()` calls (4 files) | 8-12 | 8-15s | 1-2 files |
| `Create-And-Propagate` × 4 | 4+ | 5-10s | 1 PA interface |
| **`pa_batch.py` (NEW)** | **2** | **~4s** | **5+ files in one batch** |

For the quest wave: `pa_batch.py` creates all 4 files in 2 tool calls. Individual approach would take 8-12 calls. **Result: 75-85% fewer round trips.**

---

## Common MC Type -> PA Replacement Table

Use these in your `-Swaps` parameter:

```powershell
# Standard NBT swaps (use in almost every wave)
@('NBTTagCompound',  'INbt',              'net.minecraft.nbt.NBTTagCompound',            'noppes.npcs.api.nbt.INbt')
@('NBTTagList',      'INbtList',          'net.minecraft.nbt.NBTTagList',                'noppes.npcs.api.nbt.INbtList')

# Entity swaps
@('EntityPlayer',    'IPlayer',           'net.minecraft.entity.player.EntityPlayer',    'noppes.npcs.api.entity.IPlayer')
@('EntityPlayerMP',  'IPlayer',           'net.minecraft.entity.player.EntityPlayerMP',  'noppes.npcs.api.entity.IPlayer')
@('EntityLivingBase','IEntityLivingBase', 'net.minecraft.entity.EntityLivingBase',       'noppes.npcs.api.entity.IEntityLivingBase')
@('EntityNPCInterface','ICustomNpc',      'noppes.npcs.entity.EntityNPCInterface',       'noppes.npcs.api.entity.ICustomNpc')

# Item swaps
@('ItemStack',       'IItemStack',        'net.minecraft.item.ItemStack',                'noppes.npcs.api.item.IItemStack')
```

## Common Static Transforms

```powershell
# NBT constructors
@('new\s+NBTTagCompound\s*\(\)',   'NBT.compound()')
@('new\s+NBTTagList\s*\(\)',       'NBT.list()')

# Compressed stream tools
@('CompressedStreamTools\.readCompressed\s*\(([^)]+)\)',               'PlatformServiceHolder.get().readCompressedNBT($1)')
@('CompressedStreamTools\.writeCompressed\s*\(([^,]+),\s*([^)]+)\)',  'PlatformServiceHolder.get().writeCompressedNBT($1, $2)')

# MathHelper
@('MathHelper\.clamp_int\s*\(',    'ValueUtil.clampInt(')
@('MathHelper\.clamp_float\s*\(',  'ValueUtil.clampFloat(')
@('MathHelper\.clamp_double\s*\(', 'ValueUtil.clampDouble(')

# Annotations
@('@SideOnly\s*\(\s*Side\.CLIENT\s*\)', '@ClientOnly')
@('@SideOnly\s*\(\s*Side\.SERVER\s*\)', '@ServerOnly')
```

## Common Nuke Prefixes

```powershell
# The standard set for any wave:
@(
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
    'noppes.npcs.wrapper.'
)
```

---

## Decision Tree: Which Tool When

```
I need to migrate files from mc1710 to core/
  --> Copy-Recursive (with AllowPaths whitelist)

I have MC imports in core/ files
  --> Nuke-Imports (remove them all)

I need to replace MC type names with PA types
  --> Symbol-Swap (bulk replace + import surgery)

I need to replace method calls, not just names
  --> Static-Transform (regex with capture groups)

I need to create a NEW PA interface
  --> Create-And-Propagate (atomic create + replace everywhere)

Build fails with "cannot find symbol: class X"
  --> Generate-Stubs (create placeholder X)

Build fails and I don't know where to start
  --> Build-Report (categorize errors, fix per category)

I see duplicate imports after swaps
  --> Dedupe-Imports (clean up)
```

---

## Anti-Patterns (DO NOT DO THESE)

1. **DO NOT paste PowerShell inline through bash with `$1`/`$2` in strings.** Bash eats them. Write a `.ps1` file and use `-File`.

2. **DO NOT run Copy-Recursive without AllowPaths.** It will copy the entire codebase via transitive imports (tested: 1,360 files from one seed file).

3. **DO NOT panic on build failures after migration.** Build failures are EXPECTED. Run Build-Report. Fix categorically.

4. **DO NOT delete files from core/ when the build fails.** EVER. Add stubs. Fix imports. Transform code. NEVER delete.

5. **DO NOT edit the tool source files.** They are stable functions. You provide params.

6. **DO NOT run Symbol-Swap before Nuke-Imports.** Nuke first to remove the worst offenders, then Swap for the semantic replacements.

7. **DO NOT forget Dedupe-Imports at the end.** Two MC types mapping to the same PA type (e.g., `NBTTagCompound` and `NBTTagString` both to `INbt`) produces duplicate imports.

---

## Testing

All tools have a test suite at `tools/migration/test_runner.ps1`:

```bash
powershell -ExecutionPolicy Bypass -File "tools/migration/test_runner.ps1"
```

7 tests covering: Symbol-Swap, Nuke-Imports, Static-Transform, Dedupe-Imports, Generate-Stubs, Create-And-Propagate, and a full pipeline integration test.

---
---

# Diagnostic Toolchain — `analyze.py`

> **Diagnostic tools answer questions. Migration tools change code. ALWAYS diagnose first.**
>
> `analyze.py` is a unified Python CLI with 8 subcommands that query the mc1710 codebase index, extract signatures via tree-sitter, compare migrations, and compute migration readiness. These tools are READ-ONLY — they never modify files.

## Prerequisites

```bash
# Generate the index (required before any index-dependent command)
python tools/migration/scan_codebase.py

# Dependencies (tree-sitter needed for extract-signatures, diff-signatures, surface-miner --compare-existing)
pip install tree-sitter tree-sitter-java
```

**The index file** (`tools/migration/mc_usage_index.json`, ~2.6 MB) maps ALL 400 unique MC/Forge types across 1,722 files. It records:
- Every import, method call, static call, field access, constructor, cast, instanceof
- Which files use which types and how
- Class hierarchy (extends/implements MC types)

**Regenerate the index** whenever you add/remove files from mc1710/. Scan takes ~4 seconds.

---

## Diagnostic Quick Reference Card

| Subcommand | Purpose | Depends On |
|------------|---------|------------|
| `query-type` | "How is this MC type used across the codebase?" | Index |
| `query-file` | "How MC-coupled is this specific file?" | Index |
| `extract-signatures` | "What methods does this Java file declare?" | tree-sitter |
| `surface-miner` | "What's the minimal PA interface surface for this MC type?" | Index + optional tree-sitter |
| `type-usage` | "Show ALL members of this type used within a directory, with per-file detail" | Index |
| `diff-signatures` | "What changed between two versions of this file?" | tree-sitter |
| `migration-diff` | "Is this migrated file complete? Any unmigrated types, missing methods, body drift?" PASS/WARN/FAIL | tree-sitter + classifications JSON |
| `coverage-report` | "How far along is the overall migration?" | Index |

**All subcommands support `--json` for machine-readable output. All auto-detect project root.**

---

## Subcommand Reference

### 1. `query-type` — MC Type Intelligence

**What it answers:** "How bad is this type? How many files use it? What methods/fields are called on it? Does it have a PA abstraction?"

```bash
python tools/migration/analyze.py query-type <TypeName> [options]
```

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `type` | Yes | — | Simple class name (e.g. `EntityPlayer`, `NBTTagCompound`) |
| `--fqn` | No | — | Exact FQN override (e.g. `net.minecraft.entity.player.EntityPlayer`) |
| `--top N` | No | `0` (all) | Limit output to top N methods/fields by usage count |
| `--methods-only` | No | — | Suppress field access output |
| `--field-accesses-only` | No | — | Suppress method call output |
| `--show-files` | No | — | List files where each method is called (shows top 3 + count) |
| `--json` | No | — | Machine-readable JSON output |
| `--index` | No | `tools/migration/mc_usage_index.json` | Custom index path |
| `--project-root` | No | Auto-detected | Project root override |

**Output includes:**
- PA abstraction status (mapped or NONE)
- Import count (how many files import this type)
- Method calls: each method name, call count, file count
- Static calls: same format
- Field accesses: each field name, access count, file count
- Hierarchy: which classes extend/implement this MC type
- Usage summary: constructor count, cast count, instanceof count

**Real example — EntityPlayer:**
```
TYPE: EntityPlayer (net.minecraft.entity.player.EntityPlayer)
  PA Abstraction: IPlayer
  Imported by:    551 files

  METHOD CALLS (46 unique methods):
  getCommandSenderName                        100     39
  getUniqueID                                  70     25
  addChatMessage                               29     21
  getHeldItem                                  21     11
  ...

  FIELD ACCESSES (47 unique fields):
  inventory                                      64     28
  worldObj                                       63     32
  capabilities                                   30     16
  ...

  USAGE SUMMARY:
    Constructors: 0
    Casts:        180
    instanceof:   122
```

**When to use:**
- Before creating a PA interface → know what methods to include
- Before migrating a wave → understand how deep the coupling goes
- Evaluating migration difficulty → high cast/instanceof count = hard to abstract
- Checking if a PA mapping already exists → `PA Abstraction: IPlayer` vs `NONE`

---

### 2. `query-file` — File Migration Readiness

**What it answers:** "How MC-coupled is this file? What % of its dependencies already have PA abstractions? What's blocking migration?"

```bash
python tools/migration/analyze.py query-file <file-path> [options]
```

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `file` | Yes | — | File path (relative to mc1710/src/main/java, or full path). Smart matching — partial paths work. |
| `--readiness` | No | — | Show migration readiness score (%) and blocking types |
| `--json` | No | — | Machine-readable JSON output |
| `--index` | No | `tools/migration/mc_usage_index.json` | Custom index path |
| `--project-root` | No | Auto-detected | Project root override |

**Readiness score formula:** `(MC types with PA abstraction) / (total MC types used) * 100`

**Real example — QuestController.java:**
```
FILE: noppes/npcs/controllers/QuestController.java
  MC Import Count: 3
  MC Types:        CompressedStreamTools, NBTTagCompound, NBTTagList

  MIGRATION READINESS: 66.7%
    Abstracted:  2/3
    Blocking:    CompressedStreamTools

  MC TYPES USED:
  CompressedStreamTools               (NOT ABSTRACTED)
  NBTTagCompound                      -> INbt
  NBTTagList                          -> INbtList
```

**Real example — EntityNPCInterface.java (heaviest file):**
```
FILE: noppes/npcs/entity/EntityNPCInterface.java
  MC Import Count: 59

  MIGRATION READINESS: 15.3%
    Abstracted:  9/59
    Blocking:    FMLCommonHandler, IEntityAdditionalSpawnData, Side, SideOnly, Block
                 ... and 45 more
```

**When to use:**
- **Before starting a migration wave** → check readiness for each candidate file
- **Picking migration targets** → files with high readiness scores (60%+) are low-hanging fruit
- **Identifying blockers** → the blocking list tells you exactly which PA interfaces to create first
- **JSON mode for batch queries** → script over multiple files to find the easiest migration targets

---

### 3. `extract-signatures` — Method Signature Extraction

**What it answers:** "What methods does this Java file declare? With exact types, params, annotations, and line numbers."

```bash
python tools/migration/analyze.py extract-signatures <file-path> [options]
```

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `file` | Yes | — | Path to any Java file (interface, class, etc.) |
| `--public-only` | No | — | Filter to public methods only (interface methods count as public) |
| `--json` | No | — | Machine-readable JSON with full signature objects |
| `--project-root` | No | Auto-detected | Project root override |

**Requires:** `tree-sitter` + `tree-sitter-java` Python packages.

**JSON signature object format:**
```json
{
  "name": "setString",
  "return_type": "void",
  "params": [{"type": "String", "name": "key"}, {"type": "String", "name": "value"}],
  "modifiers": [],
  "annotations": [],
  "line": 21,
  "kind": "method",
  "javadoc": null
}
```

**Real example — INbt.java (46 methods):**
```
SIGNATURES: INbt.java
  Total methods: 46

    L21  void setString(String key, String value)
    L23  void setInteger(String key, int value)
    L25  void setBoolean(String key, boolean value)
    ...
   L143  INbt copy()
   L149  Object getMCNBT()
   L155  boolean isEqual(INbt nbt)
   L157  void clear()
```

**Real example — IPlayer.java (104 methods):**
```
SIGNATURES: IPlayer.java
  Total methods: 104

    L20  String getDisplayName()
    L25  String getName()
    L32  void kick(String reason)
    ...
   L738  boolean isUsingVaultCurrency()
   L746  String getFormattedCurrencyBalance()
   L753  IEnergyProjectile[] getActiveEnergyProjectiles()
```

**When to use:**
- **Feed into surface-miner** → `--compare-existing` uses this internally
- **Pre-migration inventory** → know exactly what a PA interface already provides
- **Wrapper generation** → extract method list for Generate-Wrapper input
- **JSON mode** → pipe into scripts for automated interface design

---

### 4. `surface-miner` — Minimal Interface Surface Discovery

**What it answers:** "Based on actual codebase usage, what's the minimal set of methods needed in the PA interface for this MC type? How does it compare to what we already have?"

This is the most powerful diagnostic tool. It answers the question: **"What methods MUST the PA interface have?"**

```bash
python tools/migration/analyze.py surface-miner <TypeName> [options]
```

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `type` | Yes | — | MC type simple name (e.g. `NBTTagCompound`) |
| `--fqn` | No | — | Exact FQN override |
| `--scope` | No | all files | Path prefix filter (e.g. `core/src/main/java` to only see core usage) |
| `--compare-existing` | No | — | Path to existing PA interface file — enables gap analysis |
| `--json` | No | — | Machine-readable JSON output |
| `--index` | No | `tools/migration/mc_usage_index.json` | Custom index path |
| `--project-root` | No | Auto-detected | Project root override |

**The killer feature: `--compare-existing`**

This compares actual codebase usage against an existing PA interface and shows:
- **`[PA]`** — method exists in PA interface AND is used in codebase (covered)
- **`[MISSING]`** — method is used in codebase but NOT in PA interface (needs adding)
- **Unused in PA** — method exists in PA interface but nobody calls it (future-proofing or dead code)

**Real example — NBTTagCompound vs INbt.java:**
```
MINIMAL SURFACE: NBTTagCompound (net.minecraft.nbt.NBTTagCompound)
  PA Abstraction: INbt

  METHODS (32 unique):
  setInteger                               instance    164 [PA]
  getInteger                               instance    160 [PA]
  setTag                                   instance    159 [PA]
  hasKey                                   instance    155 [PA]
  ...
  getCompoundTag                           instance     89 [MISSING]
  func_150296_c                            instance     16 [MISSING]
  getTag                                   instance     15 [MISSING]
  hasNoTags                                instance     10 [MISSING]
  ...

  COMPARISON WITH EXISTING PA INTERFACE:
    Covered by PA:     25/32 methods
    Missing from PA:   7
      + getCompoundTag
      + func_150296_c
      + getTag
      + hasNoTags
      + equals
      + func_150299_b
      + hashCode
    Unused in PA:      20
      - clear
      - getCompound
      - getKeySet
      - getMCNBT
      - isEmpty
      - merge
      - toJsonString
      ...
```

**Scope filtering:**
```bash
# Only show usage within core/ (already-migrated code)
python tools/migration/analyze.py surface-miner EntityPlayer --scope core/src/main/java

# Only show usage within the quest subsystem
python tools/migration/analyze.py surface-miner NBTTagCompound --scope noppes/npcs/quests
```

**When to use:**
- **Designing a new PA interface** → run without `--compare-existing` to see what methods are actually needed
- **Auditing an existing PA interface** → run with `--compare-existing` to find gaps and dead methods
- **Scoped analysis** → `--scope core/src/main/java` shows only what core needs (post-migration surface)
- **Prioritizing method additions** → methods with higher usage counts should be added first

---

### 5. `type-usage` — Scoped Type Member Census

**What it answers:** "For this MC type, within this directory, show me EVERY method call, field access, constructor, cast, and instanceof — with the exact files where they occur."

This is the deep-dive tool. While `query-type` shows global usage and `surface-miner` shows minimal interface surface, `type-usage` gives you the complete per-file breakdown scoped to a specific directory. It's what you use when you're about to migrate a subsystem and need to know exactly how it touches an MC type.

```bash
python tools/migration/analyze.py type-usage <TypeName> --scope <directory> [options]
```

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `type` | Yes | — | MC type simple name (e.g. `EntityPlayer`) |
| `--fqn` | No | — | Exact FQN override |
| `--scope` | No | all files | Directory path prefix filter (e.g. `noppes/npcs/controllers/`) |
| `--top N` | No | `0` (all) | Limit methods/fields to top N by count |
| `--json` | No | — | Machine-readable JSON output |
| `--index` | No | `tools/migration/mc_usage_index.json` | Custom index path |
| `--project-root` | No | Auto-detected | Project root override |

**What it shows (all scoped to directory):**
1. **Method calls** — each method, call count, file list with per-file counts
2. **Static calls** — same format
3. **Field accesses** — each field, access count, file list
4. **Constructors** — count + file list with per-file breakdown
5. **Casts** — count + file list with per-file breakdown
6. **instanceof** — count + file list with per-file breakdown

**Real example — EntityPlayer in controllers/:**
```
TYPE USAGE: EntityPlayer in noppes/npcs/controllers/
  PA Abstraction: IPlayer
  Files in scope:  20 (of 551 total)

  METHOD CALLS (11 unique, 20 total calls):
  getCommandSenderName                         5     5
    - noppes/npcs/controllers/AuctionController.java
    - noppes/npcs/controllers/PartyController.java
    - noppes/npcs/controllers/data/Party.java
    ...
  addChatMessage                               4     4
    - noppes/npcs/controllers/AuctionController.java
    - noppes/npcs/controllers/PlayerQuestController.java
    ...

  FIELD ACCESSES (20 unique, 29 total accesses):
  posX                                            2     2
  posY                                            2     2
  posZ                                            2     2
  worldObj                                        2     2
  ...

  CONSTRUCTORS: 0 occurrences
  CASTS: 42 occurrences in 12 files
    - noppes/npcs/controllers/CustomEffectController.java (11)
    - noppes/npcs/controllers/CustomGuiController.java (9)
    ...
  INSTANCEOF: 5 occurrences in 3 files
    - noppes/npcs/controllers/PlayerDataController.java (2)
    ...
```

**Real example — NBTTagCompound in quests/:**
```
TYPE USAGE: NBTTagCompound in noppes/npcs/quests/
  PA Abstraction: INbt
  Files in scope:  5 (of 593 total)

  METHOD CALLS (10 unique, 17 total calls):
  setTag                                       4     4
  getTagList                                   3     3
  setString                                    2     2
  ...

  CONSTRUCTORS: 0 occurrences
  CASTS: 0 occurrences
  INSTANCEOF: 0 occurrences
```

**JSON output includes all fields:**
```json
{
  "type": "net.minecraft.entity.player.EntityPlayer",
  "shortName": "EntityPlayer",
  "paAbstraction": "IPlayer",
  "scope": "noppes/npcs/controllers/",
  "totalFilesInScope": 20,
  "totalFilesGlobal": 551,
  "methodCalls": { "getCommandSenderName": { "count": 5, "files": [...] }, ... },
  "staticCalls": {},
  "fieldAccesses": { "posX": { "count": 2, "files": [...] }, ... },
  "constructors": { "count": 0, "files": [] },
  "casts": { "count": 42, "files": [{"file": "...", "count": 11}, ...] },
  "instanceof": { "count": 5, "files": [{"file": "...", "count": 2}, ...] }
}
```

**When to use:**
- **Pre-migration deep dive** → "Before I migrate controllers/, show me exactly how EntityPlayer is used there"
- **Scope-specific PA interface design** → "I only need to abstract what quests/ uses from NBTTagCompound"
- **Cast/instanceof hotspot detection** → high cast counts mean the migration needs wrapper conversion utilities
- **Subsystem dependency audit** → understand exactly which MC methods a module depends on before cutting the cord

**Key difference from other tools:**
- `query-type` → global aggregate, no scope filtering, no per-file detail on casts/instanceof
- `surface-miner` → interface-design focused (method names only), no casts/instanceof/constructors
- `type-usage` → complete census with per-file attribution, scoped to any directory

---

### 6. `diff-signatures` — Signature Change Detection

**What it answers:** "What methods were added, removed, or changed between two versions of a file?"

```bash
python tools/migration/analyze.py diff-signatures --old <file1> --new <file2> [options]
```

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `--old` | Yes | — | Path to original file |
| `--new` | Yes | — | Path to updated file |
| `--json` | No | — | Machine-readable JSON output |
| `--project-root` | No | Auto-detected | Project root override |

**Comparison key:** Method name + parameter types (ignores param names, ignoring annotations/modifiers for matching).

**Change types detected:**
- **ADDED** — method exists in new but not old (name+params don't match anything)
- **REMOVED** — method exists in old but not new
- **CHANGED** — same name+params, but return type or modifiers differ

**Real example — FactionController mc1710 vs core:**
```
SIGNATURE DIFF
  Old: FactionController.java (20 methods)
  New: FactionController.java (20 methods)

  ADDED (1):
    + public void loadFactions(INbt nbttagcompound1)

  REMOVED (1):
    - public void loadFactions(DataInputStream stream)

  CHANGED (3):
    OLD: public List<IFaction> list()
    NEW: public List<Faction> list()

    OLD: public NBTTagCompound getNBT()
    NEW: public INbt getNBT()

    OLD: public IFaction delete(int id)
    NEW: public Faction delete(int id)

  Summary: +1 added, -1 removed, ~3 changed, =16 unchanged
```

**When to use:**
- **Post-migration validation** → compare mc1710 original with core migrated version to see exactly what changed
- **Shadow file audit** → verify that mc1710 shadow files add the expected extensions
- **API evolution tracking** → compare old API interface with new platform-api version
- **Regression detection** → catch unintended method signature changes after a migration wave

---

### 7. `coverage-report` — Global Migration Progress

**What it answers:** "How far along is the overall migration? What types are blocking the most files? Which packages need the most work?"

```bash
python tools/migration/analyze.py coverage-report [options]
```

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `--detailed` | No | — | Show package breakdown and top files by MC dependency |
| `--json` | No | — | Machine-readable JSON output |
| `--index` | No | `tools/migration/mc_usage_index.json` | Custom index path |
| `--project-root` | No | Auto-detected | Project root override |

**Two coverage metrics:**
- **Type coverage:** % of unique MC types that have PA abstractions (currently 2.8% — 11/400)
- **Import coverage:** % of all MC imports that reference abstracted types (currently 37.1% — 2,494/6,723)

Import coverage is much higher because the 11 abstracted types (NBTTagCompound, EntityPlayer, etc.) are the most heavily imported. **This means the 80/20 rule applies hard** — abstracting a few more high-impact types will rapidly increase import coverage.

**Real output:**
```
MIGRATION COVERAGE REPORT

  OVERALL STATISTICS:
    Total files scanned:      1,722
    Files with MC deps:       1,453
    Total MC imports:         6,723
    Unique MC types:            400

  ABSTRACTION COVERAGE:
    Type coverage:     11/400 (2.8%)
    Import coverage:   2494/6723 (37.1%)

  TOP BLOCKING TYPES (no PA abstraction):
     1. Side                                         405
     2. SideOnly                                     395
     3. GuiButton                                    218
     4. Minecraft                                    180
     5. ResourceLocation                             145
     ...

  PACKAGE BREAKDOWN:
  net.minecraft.entity.player                       3     2     858
  net.minecraft.nbt                                15     2     812
  cpw.mods.fml.relauncher                           4     0     802
  ...

  TOP FILES BY MC DEPENDENCY:
  noppes/npcs/entity/EntityNPCInterface.java              59
  noppes/npcs/scripted/NpcAPI.java                        39
  ...
```

**When to use:**
- **Progress tracking** → run after each migration wave to see coverage improvement
- **Strategic planning** → top blocking types tell you which PA interfaces to create next for maximum impact
- **Package prioritization** → package breakdown shows which MC packages have the most unabstracted types
- **Risk assessment** → top files by MC dependency are the hardest to migrate — plan these for later waves

---

### 8. `migration-diff` — Migration Completeness Verifier

**What it answers:** "Is my migrated core/ file complete? Did I miss any type replacements? Are all methods present? Did any method bodies change in non-migration ways?"

This is the **final validation gate** after migrating a file. It compares the original mc1710 file against the migrated core/ file and gives a definitive PASS/WARN/FAIL verdict.

```bash
python tools/migration/analyze.py migration-diff --old <path> --new <path> [options]
```

| Param | Required | Default | Description |
|-------|----------|---------|-------------|
| `--old` | Yes | — | Path to original mc1710 Java file |
| `--new` | Yes | — | Path to migrated core/ Java file |
| `--classifications` | No | `tools/migration/mc_type_classifications.json` | Path to type classifications JSON (relative to project root) |
| `--json` | No | — | Machine-readable JSON output |
| `--project-root` | No | Auto-detected | Project root override |

**Three detection categories:**

1. **UNMIGRATED** — MC type names from the classifications (EXISTING, INTERFACE, SERVICE categories) that still appear verbatim in the new file. These should have been replaced by Symbol-Swap. Reports line numbers and the expected PA replacement.

2. **MISSING_METHOD** — Methods present in the old file but absent in the new file after normalizing parameter types. Uses the classifications to normalize `NBTTagCompound` → `INbt`, `EntityPlayer` → `IPlayer`, etc. in param types before matching, so `loadData(NBTTagCompound nbt)` in old correctly matches `loadData(INbt nbt)` in new.

3. **SEMANTIC_BODY_DIFF** — Methods present in both files (by normalized signature match) whose bodies differ beyond pure type substitutions. Both bodies are normalized (MC types → PA equivalents, TRANSFORM patterns applied), then compared with `difflib`. If similarity < 85% or any diff lines remain, the method is flagged. Shows up to 10 diff lines per method.

**Verdict system:**

| Verdict | Condition | Meaning |
|---------|-----------|---------|
| **FAIL** | Any MISSING_METHOD | Methods were lost during migration — investigate and restore |
| **WARN** | Any UNMIGRATED types or SEMANTIC_BODY_DIFF (but no missing methods) | Migration incomplete or has logic changes — manual review needed |
| **PASS** | None of the above | Migration looks clean — all methods present, types replaced, bodies match |

**The 15% gap — what migration-diff CANNOT detect:**

Migration-diff catches ~85% of migration issues automatically. The remaining ~15% requires human judgment:

- **Algorithmic changes** — If a method's logic was intentionally rewritten (e.g., switching from `for` loop to `stream()`), migration-diff flags it as SEMANTIC_BODY_DIFF but can't tell if the new logic is equivalent
- **Logic reordering** — If statements were reordered within a method body, the diff flags it even though it may be semantically equivalent
- **Renamed local variables** — Normalization only handles MC type names, not arbitrary variable renames
- **Semantic equivalence** — `player.posX` → `player.getX()` changes the text even after normalization if `posX` isn't in the classifications as a field transform
- **Intentional omissions** — Some methods are deliberately not migrated (e.g., MC-specific registration hooks). Migration-diff flags these as MISSING_METHOD

**Always manually review WARN and FAIL results.** The verdict is a triage signal, not a final judgment.

**Real workflow example:**

```bash
# After migrating FactionController to core/
python tools/migration/analyze.py migration-diff \
    --old src/main/java/noppes/npcs/controllers/FactionController.java \
    --new core/src/main/java/noppes/npcs/controllers/FactionController.java

# Expected output for a well-migrated file:
# VERDICT: WARN  [0 unmigrated, 0 missing, 2 body diffs]
# The body diffs are expected: loadFactions() changed from DataInputStream to INbt,
# and saveFactions() changed from CompressedStreamTools to PlatformServiceHolder.
# Manual review confirms these are correct migration changes → file is good.

# For a file with issues:
# VERDICT: FAIL  [1 unmigrated, 2 missing, 0 body diffs]
# ! EntityPlayer still in new file → forgot to run Symbol-Swap
# - loadData(DataInputStream) missing → method used MC I/O, needs rewrite
# - getWorldInstance() missing → removed accidentally during migration
```

**When to use:**
- **After every file migration** → run as the final validation step before marking a file as "migrated"
- **After a migration wave** → batch-validate all files in the wave
- **Before committing** → catch accidental method deletions or leftover MC types
- **Regression detection** → re-run on previously migrated files after Symbol-Swap updates

---

## Diagnostic Playbook — How to Solve Real Migration Problems

> **The diagnostic tools exist so you NEVER guess.** Every migration decision — which files, which types, which methods, which strategy — should be backed by data from these tools.
>
> **Read the entire playbook.** These aren't isolated patterns; real migrations chain multiple plays together. Understanding all of them lets you pick the right sequence for your specific situation.

---

### PLAY 1: "Which files should I migrate next?"

**The problem:** You have 1,453 files with MC dependencies. You need to pick the next batch that gives maximum progress with minimum new PA interfaces.

**The tool sequence:**

```bash
# 1. Start with candidate files you're considering
python tools/migration/analyze.py query-file noppes/npcs/controllers/QuestController.java --readiness
python tools/migration/analyze.py query-file noppes/npcs/controllers/DialogController.java --readiness
python tools/migration/analyze.py query-file noppes/npcs/controllers/PlayerQuestController.java --readiness

# Real results:
#   QuestController:       66.7% ready — blocked by CompressedStreamTools only
#   DialogController:      66.7% ready — blocked by CompressedStreamTools only
#   PlayerQuestController: 66.7% ready — blocked by CompressedStreamTools only
```

**How to interpret:** All three share the same blocker (CompressedStreamTools). That means fixing ONE type unblocks the ENTIRE batch. This is a high-leverage wave.

**Next step — evaluate the blocker:**
```bash
python tools/migration/analyze.py query-type CompressedStreamTools --top 5
# → 2 methods: readCompressed, writeCompressed
# → Used in 39 files total

# Decision: Only 2 methods → Static-Transform, not a PA interface
```

**The decision rule:**
- Readiness ≥ 60% → good candidate, few blockers
- Readiness 30-60% → viable but will need new PA interfaces or stubs
- Readiness < 30% → too many blockers, defer unless you're specifically targeting that subsystem
- **Same blocker across multiple files** → high leverage, fix the blocker and migrate them all

**Contrast with the wrong approach (EntityNPCInterface.java):**
```bash
python tools/migration/analyze.py query-file noppes/npcs/entity/EntityNPCInterface.java --readiness
# → 15.3% ready, 50 blocking types
# → This file NEVER migrates to core. It extends Entity. It stays in mc1710. Don't waste time.
```

---

### PLAY 2: "I need to create a PA interface — what methods go in it?"

**The problem:** You've identified `DamageSource` as a blocker. You need to design `IDamageSource`. But should it have 3 methods or 30? How do you know what's enough?

**Step 1 — Global surface scan (what does the whole codebase need?):**
```bash
python tools/migration/analyze.py surface-miner DamageSource
# METHODS (8 unique):
#   getEntity          instance   7     ← most used, definitely include
#   isMagicDamage      instance   2
#   isFireDamage       instance   2
#   isExplosion        instance   2
#   isProjectile       instance   2
#   getSourceOfDamage  instance   2
#   isUnblockable      instance   2
#   getDamageType      instance   2
#
# STATIC CALLS (3 unique):
#   causePlayerDamage  static     6     ← factory method, needs design decision
#   causeMobDamage     static     5
#   causeThrownDamage  static     1
#
# FIELDS (1 unique):
#   damageType         4               ← direct field access, must become a getter
```

**Step 2 — Scoped scan (what does ONLY the target migration area need?):**
```bash
python tools/migration/analyze.py type-usage DamageSource --scope noppes/npcs/controllers/
# → Maybe controllers/ only uses getEntity() and getDamageType()
# → You might create a minimal interface for now, expand later
```

**Step 3 — Context check (how hard is this type to abstract?):**
```bash
python tools/migration/analyze.py query-type DamageSource
# Key things to look for:
#   Constructors: 0  → nobody creates new DamageSource instances in our code (good!)
#   Casts: 1         → almost no casting (good!)
#   instanceof: 0    → no runtime type checks (good!)
#   Hierarchy: nobody extends DamageSource in our codebase (good!)
#
# Verdict: Simple wrappable type. 8 instance methods + 3 static factories.
```

**Step 4 — If PA interface already exists, audit it:**
```bash
python tools/migration/analyze.py surface-miner DamageSource \
    --compare-existing platform-api/src/main/java/noppes/npcs/api/IDamageSource.java
# Shows [PA] vs [MISSING] per method
# → Any [MISSING] method with usage > 5 should be added
# → [MISSING] methods with usage 1-2 can wait
```

**Design rules derived from diagnostics:**
- **Instance methods** with usage ≥ 2 → add to interface
- **Static methods** → become static factory methods on the interface or on a companion factory class
- **Field accesses** → become getter methods (e.g., `damageType` field → `getDamageType()`)
- **High cast count** → you need an `unwrap()` or `getMCEntity()` escape hatch method
- **High instanceof count** → you may need subtype interfaces, not just one flat interface
- **Classes extend this MC type** → you CANNOT wrap it simply. It might need to stay in mc1710 or use a more complex delegation pattern

---

### PLAY 3: "PA interface vs Static-Transform — which strategy?"

**The problem:** Not every MC type deserves a full PA interface. Some are better handled by regex replacement. How do you decide?

**The diagnostic sequence:**
```bash
python tools/migration/analyze.py query-type <MCType> --top 5
```

**Decision matrix:**

| Diagnostic Signal | Strategy | Why |
|---|---|---|
| 1-3 static methods, no instance calls, no fields | **Static-Transform** | Just rewrite the call sites with regex |
| Few method calls (<5 unique) across <10 files | **Static-Transform** | Not worth a whole interface |
| Many methods (5+) across many files (10+) | **PA Interface** | Too many call patterns for regex |
| Direct field access (`.posX`, `.inventory`) | **PA Interface** | Fields become getters; regex can't handle all contexts |
| High constructor count (`new MCType(...)`) | **PA Interface** with factory | Need `PlatformService.create...()` factory |
| High cast count (`(MCType) expr`) | **PA Interface** with `unwrap()` | Casts become wrapper conversions |
| Type appears in method signatures | **PA Interface** | Methods need to accept/return the interface type |
| Annotation type (`@SideOnly`, `@Override`) | **Static-Transform** | Just swap annotation text |

**Real examples from our codebase:**

```bash
# CompressedStreamTools → Static-Transform (2 static methods, no state)
python tools/migration/analyze.py query-type CompressedStreamTools
# → 2 methods: readCompressed, writeCompressed. Used as utility calls only.
# → Strategy: Static-Transform to PlatformServiceHolder.get().readCompressedNBT(...)

# MathHelper → Static-Transform (pure static utility)
python tools/migration/analyze.py query-type MathHelper --methods-only --top 5
# → clamp_int, clamp_float, clamp_double, sqrt_double, floor_double
# → Strategy: Static-Transform to ValueUtil.clampInt(...) etc.

# Side/SideOnly → Static-Transform (annotations)
# → 800 imports but literally just annotation text replacement
# → Strategy: @SideOnly(Side.CLIENT) → @ClientOnly

# EntityPlayer → PA Interface (46 methods, 47 fields, 551 files, 180 casts)
python tools/migration/analyze.py query-type EntityPlayer
# → Massively used, has IPlayer already. Cannot be handled by regex.

# ResourceLocation → PA Interface (145 imports, 30 methods, used in constructors + as params)
python tools/migration/analyze.py query-type ResourceLocation --top 5
# → Appears in method signatures, passed to other MC methods. Needs interface.
```

---

### PLAY 4: "I'm about to migrate a subsystem — give me the full intelligence briefing"

**The problem:** You're assigned to migrate the quest subsystem. You need to know EVERYTHING about its MC dependencies before writing a single line.

**The full intelligence sequence:**

```bash
# 1. File-by-file readiness scan
python tools/migration/analyze.py query-file noppes/npcs/quests/QuestItem.java --readiness
python tools/migration/analyze.py query-file noppes/npcs/quests/QuestKill.java --readiness
python tools/migration/analyze.py query-file noppes/npcs/quests/QuestDialog.java --readiness
python tools/migration/analyze.py query-file noppes/npcs/quests/QuestLocation.java --readiness
python tools/migration/analyze.py query-file noppes/npcs/quests/QuestManual.java --readiness
python tools/migration/analyze.py query-file noppes/npcs/controllers/QuestController.java --readiness

# 2. For EACH blocking type, get the usage picture scoped to your subsystem
python tools/migration/analyze.py type-usage NBTTagCompound --scope noppes/npcs/quests/
python tools/migration/analyze.py type-usage EntityPlayer --scope noppes/npcs/quests/
python tools/migration/analyze.py type-usage CompressedStreamTools --scope noppes/npcs/quests/

# 3. For types that need PA interfaces, get the minimal surface
python tools/migration/analyze.py surface-miner NBTTagCompound --compare-existing platform-api/src/main/java/noppes/npcs/api/INbt.java
# → Know exactly which INbt methods are needed vs already covered

# 4. Coverage baseline (before migration)
python tools/migration/analyze.py coverage-report
```

**How to read the briefing and write your migration script:**

The `type-usage` output for your scope tells you exactly what goes into your `-Swaps` parameter:
- Every method call on `NBTTagCompound` → these must exist on `INbt` after migration
- Every field access → these become getter/setter methods on the PA interface
- Constructor count → tells you how many `new NBTTagCompound()` → `NBT.compound()` transforms
- Cast count → tells you if you need wrapper conversion utilities
- instanceof count → tells you if runtime type checks need refactoring

**Then you write the wave script with confidence** — no guessing about what swaps to include, what stubs are needed, or what static transforms to apply.

---

### PLAY 5: "Build failed after migration — how do I fix it systematically?"

**The problem:** You ran your migration wave script. Build-Report shows 47 errors. Where do you start?

**Step 1 — Categorize errors (you already have Build-Report output):**
```
MISSING_PACKAGE:net.minecraft.nbt  → leftover MC import, run Nuke-Imports
MISSING_CLASS:CompressedStreamTools → need a stub or static transform
MISSING_METHOD:getCompoundTag      → INbt doesn't have this method yet
TYPE_MISMATCH                      → incompatible types, wrong swap or missing conversion
```

**Step 2 — Use diagnostics to understand each error category:**

For `MISSING_METHOD:getCompoundTag`:
```bash
# Is this method supposed to be on INbt?
python tools/migration/analyze.py surface-miner NBTTagCompound --compare-existing platform-api/src/main/java/noppes/npcs/api/INbt.java
# → getCompoundTag shows as [MISSING] with 89 usages
# → It maps to getCompound() in INbt (different name!)
# → Fix: Add a Static-Transform: getCompoundTag → getCompound
```

For `TYPE_MISMATCH`:
```bash
# Find which files are trying to use the wrong type
python tools/migration/analyze.py type-usage EntityPlayer --scope core/src/main/java
# → If there are still EntityPlayer references in core/, Symbol-Swap missed some
# → If it's a cast issue, type-usage shows the exact cast count per file
```

**The fix cycle uses both toolchains:**
```powershell
# ROUND 1: Fix what diagnostics revealed
Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes @('net.minecraft.nbt.')
Static-Transform -Directory 'core/src/main/java' -Transforms @(
    @('\.getCompoundTag\(', '.getCompound(')
)
Generate-Stubs -Stubs @(
    @{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class'; body='...' }
)

# ROUND 2: Build again
Build-Report -GradleTask ':core:compileJava'
# → Errors should drop from 47 to <10
```

**Key insight:** Don't fix errors one at a time. Use `query-type` and `surface-miner` to understand the PATTERN, then fix the entire category with one tool invocation.

---

### PLAY 6: "After migration, did I break anything?"

**The problem:** Migration wave is done, build passes. But did you accidentally drop methods, change signatures, or leave MC types leaking through?

**Validation checklist (run ALL of these):**

```bash
# 1. Signature diff — catch unintended changes
python tools/migration/analyze.py diff-signatures \
    --old mc1710/src/main/java/noppes/npcs/controllers/QuestController.java \
    --new core/src/main/java/noppes/npcs/controllers/QuestController.java

# What you EXPECT to see:
#   CHANGED: methods where NBTTagCompound → INbt, EntityPlayer → IPlayer (type migration)
#   ADDED: possibly new overloads with PA types
#   REMOVED: old MC-typed overloads replaced by PA-typed ones
#
# What you DON'T want to see:
#   REMOVED methods that weren't replaced (accidental deletion)
#   CHANGED return types that shouldn't have changed (over-zealous Symbol-Swap)
#   Methods with wrong parameter types (mangled by regex)

# 2. Readiness check — core file should be 100% clean
python tools/migration/analyze.py query-file core/src/main/java/noppes/npcs/controllers/QuestController.java --readiness
# → Must be 100%. If not, MC types leaked through.

# 3. Coverage improvement — did the wave actually help?
python tools/migration/analyze.py coverage-report
# → Compare import coverage before vs after
```

**Common post-migration problems and how diagnostics catch them:**

| Problem | Diagnostic | Fix |
|---------|-----------|-----|
| MC type leaking in core file | `query-file --readiness` shows < 100% | Run Symbol-Swap or Nuke-Imports on the missed type |
| Method accidentally deleted | `diff-signatures` shows unexpected REMOVED | Restore from mc1710 original, re-apply migration |
| Return type mangled by regex | `diff-signatures` shows unexpected CHANGED | Static-Transform was too greedy — refine regex |
| PA interface missing methods | `surface-miner --compare-existing` shows [MISSING] | Add methods to PA interface |

---

### PLAY 7: "What should I abstract next for maximum ROI?"

**The problem:** You have finite time. Which PA interfaces give you the most migration unlock per hour of work?

```bash
# 1. Get the top blockers
python tools/migration/analyze.py coverage-report --detailed

# Current top blockers:
#   Side (405) / SideOnly (395) — handle with Static-Transform (10 min)
#   GuiButton (218) — client-only, SKIP (never goes to core)
#   Minecraft (180) — client-only, SKIP
#   ResourceLocation (145) — needs PA interface
#   MathHelper (98) — handle with Static-Transform (10 min)
#   Block (84) — mixed, some uses might be abstractable
#   TileEntity (78) — complex, defer

# 2. Eliminate the easy wins first
#   Side/SideOnly: 800 imports eliminated by annotation swap (cost: 10 min, value: +12% coverage)
#   MathHelper: 98 imports eliminated by static method swap (cost: 10 min, value: +1.5%)
#   Total: 20 min work → coverage jumps from 37.1% to ~50.6%

# 3. For real PA interface candidates, check complexity
python tools/migration/analyze.py query-type ResourceLocation
#   145 imports, 30 methods called, 1 field, 3 constructors
#   → Medium complexity PA interface. Worth doing.

python tools/migration/analyze.py query-type TileEntity
#   78 imports, 63 methods, 51 fields, 10 subclasses extend it
#   → HIGH complexity. Many subclasses. Defer.

python tools/migration/analyze.py query-type AxisAlignedBB
#   59 imports, 68 methods, 28 fields
#   → But query-type reveals: mostly used in entity code that stays in mc1710
#   → SKIP for now — most users won't migrate to core anyway
```

**ROI scoring formula:**
```
ROI = (import_count × files_unblocked) / (unique_methods + field_count + constructor_complexity + cast_count)
```

- High import count + low method count = **HIGH ROI** (e.g., Side/SideOnly, MathHelper)
- High import count + high method count but simple delegation = **MEDIUM ROI** (e.g., ResourceLocation)
- High import count + subclasses + casts + field access = **LOW ROI** (e.g., TileEntity, AxisAlignedBB)

---

### PLAY 8: "I need to understand how deeply a type is woven into a subsystem"

**The problem:** Before migrating `controllers/`, you need to know if `EntityPlayer` is used lightly (easy swap) or deeply (casts everywhere, field access, instanceof — hard migration).

```bash
python tools/migration/analyze.py type-usage EntityPlayer --scope noppes/npcs/controllers/

# The output tells you EVERYTHING:
#   METHOD CALLS (11 unique, 20 total) — these become IPlayer method calls (easy)
#   FIELD ACCESSES (20 unique, 29 total) — these need IPlayer getters (medium)
#   CONSTRUCTORS: 0 — nobody creates EntityPlayer instances (good!)
#   CASTS: 42 in 12 files — DANGER: this means lots of (EntityPlayer) casts
#   INSTANCEOF: 5 in 3 files — some runtime type checking to handle
```

**How to interpret each signal:**

**Casts: 42 occurrences** — This is the hardest part. Each `(EntityPlayer) someEntity` becomes a wrapper conversion. You need:
- A `PlatformServiceHolder.get().asPlayer(entity)` utility
- Or `if (entity instanceof IPlayer)` checks
- The per-file breakdown tells you WHERE to focus: `CustomEffectController.java` has 11 casts — start there

**Field accesses: 20 unique fields** — Each `player.inventory`, `player.posX`, `player.worldObj` must become a method call on `IPlayer`. If `IPlayer` already has `getInventory()`, `getX()`, `getWorld()` — great. If not, you need to add them. Use `extract-signatures` on IPlayer to check:
```bash
python tools/migration/analyze.py extract-signatures platform-api/src/main/java/noppes/npcs/api/entity/IPlayer.java --public-only --json
# → Check if getInventory, getX, getWorld exist
```

**instanceof: 5 occurrences** — Each `if (entity instanceof EntityPlayer)` becomes `if (entity instanceof IPlayer)`. Usually straightforward, but verify the cast pattern that typically follows.

---

### PLAY 9: "The full diagnostic-to-migration pipeline"

**This is the complete workflow for a real migration wave from start to finish.**

```
PHASE 1: INTELLIGENCE GATHERING (5-10 min)
├── coverage-report --detailed        → current baseline, top blockers
├── query-file --readiness × N        → pick candidates (≥60% readiness)
├── type-usage <blocker> --scope      → understand blockers in target area
├── surface-miner <blocker>           → design PA interfaces needed
└── query-type <blocker>              → decide PA interface vs Static-Transform

PHASE 2: WAVE SCRIPT WRITING (10-20 min)
├── Copy-Recursive                    → copy files to core
├── Nuke-Imports                      → strip MC imports
├── Symbol-Swap                       → replace types with PA types
├── Static-Transform                  → rewrite method calls
├── Generate-Stubs                    → placeholder missing deps
├── Dedupe-Imports                    → clean up
└── Build-Report                      → categorize errors

PHASE 3: FIX CYCLE (5-30 min depending on wave size)
├── For MISSING_METHOD errors:
│   └── surface-miner --compare-existing → find method name mappings
│       └── Static-Transform              → rename method calls
├── For MISSING_CLASS errors:
│   └── query-type <missing>              → understand what it is
│       └── Generate-Stubs OR pa interface → fix appropriately
├── For TYPE_MISMATCH errors:
│   └── type-usage <type> --scope core/   → find the exact locations
│       └── Manual fix or Static-Transform → correct the types
└── Build-Report again                    → verify error count dropped

PHASE 4: VALIDATION (5 min)
├── diff-signatures --old mc1710/... --new core/...   → verify only expected changes
├── query-file core/.../<file> --readiness            → must be 100%
└── coverage-report                                    → confirm coverage improved
```

**The key principle:** NEVER skip Phase 1. Agents that jump straight to Phase 2 waste hours fixing avoidable errors. 10 minutes of diagnostics saves 2 hours of debugging.

---

### PLAY 10: "After migrating a file — verify it's complete"

**The problem:** You've migrated `QuestController.java` from mc1710 to core. The build passes. But did you lose any methods? Are there MC types that slipped through Symbol-Swap? Did any method bodies change in unexpected ways?

**The tool:** `migration-diff` — the final validation step in any migration wave.

```bash
# Run migration-diff as the LAST step after build passes
python tools/migration/analyze.py migration-diff \
    --old src/main/java/noppes/npcs/controllers/QuestController.java \
    --new core/src/main/java/noppes/npcs/controllers/QuestController.java
```

**How to interpret results:**

**PASS** → File is clean. All methods present, no leftover MC types, bodies match after normalization. You can confidently mark this file as migrated.

**WARN with UNMIGRATED types** → Symbol-Swap missed some type replacements. Fix:
```powershell
Symbol-Swap -Directory 'core/src/main/java' -Swaps @(
    @('MissedType', 'IPAType', 'old.import', 'new.import')
)
```
Then re-run `migration-diff` to verify.

**WARN with SEMANTIC_BODY_DIFF** → Method bodies differ after normalization. Check each flagged method:
- If the diff is `new NBTTagCompound()` → `NBT.compound()` style → expected, safe
- If the diff shows logic changes (new conditions, different algorithm) → manual review required
- If the diff is just whitespace/formatting → safe, ignore

**FAIL with MISSING_METHOD** → Methods from the original file are gone. Check:
- Was the method intentionally excluded (MC-specific registration, client hooks)?
- Was it accidentally deleted during migration?
- Was the signature changed in a way that normalization didn't catch (e.g., different param count)?

**Batch validation after a wave:**
```bash
# Validate all files in a migration wave
for old_file in QuestController DialogController PlayerQuestController; do
    echo "=== $old_file ==="
    python tools/migration/analyze.py migration-diff \
        --old "src/main/java/noppes/npcs/controllers/${old_file}.java" \
        --new "core/src/main/java/noppes/npcs/controllers/${old_file}.java"
done
```

**The complete Phase 4 with migration-diff:**
```
PHASE 4: VALIDATION (5 min)
├── migration-diff --old mc1710/... --new core/...     → comprehensive PASS/WARN/FAIL
├── diff-signatures --old mc1710/... --new core/...    → detailed signature changes
├── query-file core/.../<file> --readiness             → must be 100%
└── coverage-report                                     → confirm coverage improved
```

`migration-diff` is the broadest check (types + methods + bodies). `diff-signatures` gives more detail on signature changes. `query-file --readiness` catches any remaining MC imports. Use all three for maximum confidence.

---

## Diagnostic Output Formats

All subcommands support `--json` for piping into scripts or feeding into other tools.

**JSON outputs are ideal for:**
- Batch analysis scripts (`for file in ...; do python analyze.py query-file "$file" --readiness --json; done`)
- Feeding `surface-miner --json` results into Generate-Interface method list
- Automated migration wave planning scripts
- CI/CD coverage tracking

**Human-readable output is ideal for:**
- Quick checks during migration work
- Copy-pasting into agent conversations for context
- Decision-making (the tabular format makes comparisons easy)

---

## Current Codebase Statistics (from latest scan)

These numbers represent the **full mc1710 codebase** as of the last `scan_codebase.py` run:

| Metric | Value |
|--------|-------|
| Total Java files | 1,722 |
| Files with MC dependencies | 1,453 (84%) |
| Total MC imports | 6,723 |
| Unique MC types | 400 |
| PA abstraction coverage (types) | 11/400 (2.8%) |
| PA abstraction coverage (imports) | 2,494/6,723 (37.1%) |
| Classes extending MC types | 291 |
| Classes implementing MC interfaces | 61 |
| Most-imported type | NBTTagCompound (593 imports) |
| Most method-heavy type | NBTTagCompound (4,447 method calls) |
| Most field-heavy type | EntityLivingBase (1,113 field accesses) |
| Highest MC dependency file | EntityNPCInterface.java (59 imports) |

**Currently abstracted types (11):**
`NBTTagCompound→INbt`, `NBTTagList→INbtList`, `Entity→IEntity`, `EntityLivingBase→IEntityLivingBase`, `EntityLiving→IEntityLiving`, `EntityPlayer→IPlayer`, `EntityPlayerMP→IPlayer`, `ItemStack→IItemStack`, `World→IWorld`, `WorldServer→IWorld`, `DamageSource→IDamageSource`

---

## Limitations & Known Gaps

1. **No batch readiness query** — Can't ask "show me the 20 easiest files to migrate" in one command. Workaround: script with `--json`:
   ```bash
   # Find most-migratable files (use jq or python to sort by readiness)
   for f in $(python -c "import json; idx=json.load(open('tools/migration/mc_usage_index.json')); [print(f) for f in sorted(idx['files'])]"); do
       python tools/migration/analyze.py query-file "$f" --readiness --json 2>/dev/null
   done | python -c "import sys,json; ..."
   ```

2. **No cross-reference query** — Can't ask "which files use BOTH EntityPlayer AND NBTTagCompound?" Workaround: use the `--json` output and intersect file lists manually.

3. **Scoped surface-miner returns empty for fully-migrated modules** — If core/ already replaced `EntityPlayer` with `IPlayer`, then `--scope core/` finds 0 EntityPlayer calls. This is correct behavior — scope to `mc1710/` or omit `--scope` for full picture.

4. **Obfuscated method names** — The scanner captures MCP names as-is (`func_150296_c`). Cross-reference with MCP mappings to find the real name.

5. **No inheritance-aware method resolution** — If `EntityPlayerMP` inherits `getHealth()` from `EntityLivingBase`, the scanner attributes the call to whichever variable type was used at the call site. Both types' method lists are independent.
