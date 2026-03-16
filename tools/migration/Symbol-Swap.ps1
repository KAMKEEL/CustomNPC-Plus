# Symbol-Swap.ps1
# ================
# SUPERWEAPON: Bulk type replacement with import surgery.
#
# For every .java file in a directory, this function:
#   1. Replaces ALL occurrences of old symbol names with new ones
#   2. Removes the old MC import line
#   3. Adds the new PA import line (if not already present)
#   4. Logs every file it modifies
#
# USAGE:
#   . tools/migration/Symbol-Swap.ps1
#
#   Symbol-Swap -Directory 'core/src/main/java' -Swaps @(
#       @('NBTTagCompound', 'INbt', 'net.minecraft.nbt.NBTTagCompound', 'noppes.npcs.api.nbt.INbt'),
#       @('EntityPlayer',   'IPlayer', 'net.minecraft.entity.player.EntityPlayer', 'noppes.npcs.api.entity.IPlayer')
#   )
#
# PARAMS:
#   -Directory    Relative path (from ProjectRoot) to scan for .java files recursively.
#   -Swaps        Array of 4-element arrays: @(oldSymbol, newSymbol, oldImport, newImport).
#                 - oldSymbol: The type name to replace in code (e.g., 'NBTTagCompound')
#                 - newSymbol: The replacement type name (e.g., 'INbt')
#                 - oldImport: The fully-qualified old import to REMOVE (e.g., 'net.minecraft.nbt.NBTTagCompound')
#                 - newImport: The fully-qualified new import to ADD (e.g., 'noppes.npcs.api.nbt.INbt')
#                   Use '' (empty string) for newImport if no import should be added.
#   -ProjectRoot  Absolute project root (auto-detected from script location if not provided)
#
# OUTPUT:
#   Returns array of modified file paths (relative to Directory).
#   Prints log to stdout.

function Symbol-Swap {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Directory,

        [Parameter(Mandatory=$true)]
        [array]$Swaps,

        [string]$ProjectRoot
    )

    # Auto-detect ProjectRoot from script location if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
    }

    $fullDir = Join-Path $ProjectRoot $Directory
    if (!(Test-Path $fullDir)) {
        Write-Host "ERROR: Directory not found: $fullDir"
        return @()
    }

    # Guard against PowerShell single-element array flattening:
    # If Swaps is a flat array of strings (e.g., @('A','B','C','D')) instead of
    # array-of-arrays, wrap it so the code treats it as one swap entry.
    if ($Swaps.Count -gt 0 -and $Swaps[0] -is [string]) {
        $Swaps = @(,$Swaps)
    }

    $files = Get-ChildItem -Recurse -Filter '*.java' -Path $fullDir
    $modified = [System.Collections.ArrayList]::new()

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  SYMBOL-SWAP -- Bulk Type Replacement"
    Write-Host "============================================"
    Write-Host "Directory:  $Directory"
    Write-Host "Files:      $($files.Count)"
    Write-Host "Swaps:      $($Swaps.Count)"
    foreach ($s in $Swaps) {
        Write-Host "  $($s[0]) -> $($s[1])"
    }
    Write-Host "============================================"
    Write-Host ""

    foreach ($f in $files) {
        $content = [IO.File]::ReadAllText($f.FullName)
        $original = $content
        $fileSwapped = $false

        # PHASE 1: Remove all old imports FIRST (before symbol replacement mangles them)
        foreach ($swap in $Swaps) {
            $oldImp = $swap[2]
            if ($oldImp -and ($content -match "import\s+(?:static\s+)?$([regex]::Escape($oldImp))\s*;")) {
                $content = $content -replace "import\s+(?:static\s+)?$([regex]::Escape($oldImp))\s*;\r?\n", ''
            }
        }

        # PHASE 2: Replace symbols in code body (whole-word, CASE-SENSITIVE via -creplace)
        foreach ($swap in $Swaps) {
            $oldSym = $swap[0]; $newSym = $swap[1]
            $pattern = '\b' + [regex]::Escape($oldSym) + '\b'
            if ($content -cmatch $pattern) {
                $content = $content -creplace $pattern, $newSym
                $fileSwapped = $true
            }
        }

        # PHASE 3: Add new imports (if swap happened, not empty, and not already present)
        foreach ($swap in $Swaps) {
            $newImp = $swap[3]
            if ($newImp -and $fileSwapped -and !($content -match "import\s+$([regex]::Escape($newImp))\s*;")) {
                $content = $content -replace '(package\s+[^;]+;\s*\r?\n)', "`$1import $newImp;`n"
            }
        }

        if ($content -ne $original) {
            [IO.File]::WriteAllText($f.FullName, $content)
            [void]$modified.Add($f.FullName.Substring($fullDir.Length + 1))
            Write-Host "  SWAPPED: $($f.Name)"
        }
    }

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  SYMBOL-SWAP SUMMARY"
    Write-Host "============================================"
    Write-Host "Files modified: $($modified.Count) / $($files.Count)"
    Write-Host "============================================"

    return $modified.ToArray()
}
