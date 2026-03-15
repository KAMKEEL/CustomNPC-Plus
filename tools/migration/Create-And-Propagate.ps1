# Create-And-Propagate.ps1
# ========================
# SUPERWEAPON: Atomically create a PA interface AND replace its MC equivalent everywhere.
#
# This function:
#   1. Writes a new file (typically a platform-api interface)
#   2. Scans all .java files in a target directory
#   3. For each file, performs Symbol-Swap style replacement:
#      - Replaces old type name with new type name
#      - Removes old MC import
#      - Adds new PA import
#   4. Creation and propagation happen in ONE call -- no partial states.
#
# USAGE:
#   . tools/migration/Create-And-Propagate.ps1
#
#   Create-And-Propagate `
#       -FilePath 'platform-api/src/main/java/noppes/npcs/api/handler/IMessageService.java' `
#       -Content $interfaceCode `
#       -TargetDirectory 'core/src/main/java' `
#       -Swaps @(
#           @('ChatComponentText', 'IMessageService', 'net.minecraft.util.ChatComponentText', 'noppes.npcs.api.handler.IMessageService'),
#           @('ChatComponentTranslation', 'IMessageService', 'net.minecraft.util.ChatComponentTranslation', 'noppes.npcs.api.handler.IMessageService')
#       )
#
# PARAMS:
#   -FilePath         Relative path (from ProjectRoot) for the new file to create.
#   -Content          String content of the new file.
#   -TargetDirectory  Relative path (from ProjectRoot) to scan for propagation. All .java files
#                     in this directory (recursively) will have swaps applied.
#   -Swaps            Array of 4-element arrays: @(oldSymbol, newSymbol, oldImport, newImport).
#                     Same format as Symbol-Swap.ps1.
#   -ProjectRoot      Absolute project root (auto-detected from script location if not provided)
#
# OUTPUT:
#   Returns hashtable with 'Created' (file path) and 'Modified' (array of modified files).
#   Prints log to stdout.

function Create-And-Propagate {
    param(
        [Parameter(Mandatory=$true)]
        [string]$FilePath,

        [Parameter(Mandatory=$true)]
        [string]$Content,

        [Parameter(Mandatory=$true)]
        [string]$TargetDirectory,

        [Parameter(Mandatory=$true)]
        [array]$Swaps,

        [string]$ProjectRoot
    )

    # Auto-detect ProjectRoot from script location if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
    }

    # Guard against PowerShell single-element array flattening
    if ($Swaps.Count -gt 0 -and $Swaps[0] -is [string]) {
        $Swaps = @(,$Swaps)
    }

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  CREATE-AND-PROPAGATE -- Atomic PA Creator"
    Write-Host "============================================"
    Write-Host "New file:     $FilePath"
    Write-Host "Target dir:   $TargetDirectory"
    Write-Host "Swaps:        $($Swaps.Count)"
    foreach ($s in $Swaps) {
        Write-Host "  $($s[0]) -> $($s[1])"
    }
    Write-Host "============================================"
    Write-Host ""

    # Step 1: Create the new file
    $fullPath = Join-Path $ProjectRoot $FilePath
    $dir = Split-Path $fullPath -Parent
    if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
    [IO.File]::WriteAllText($fullPath, $Content)
    Write-Host "  CREATED: $FilePath"

    # Step 2: Propagate swaps across target directory
    $fullTargetDir = Join-Path $ProjectRoot $TargetDirectory
    if (!(Test-Path $fullTargetDir)) {
        Write-Host "ERROR: Target directory not found: $fullTargetDir"
        return @{ Created = $FilePath; Modified = @() }
    }

    $files = Get-ChildItem -Recurse -Filter '*.java' -Path $fullTargetDir
    $modified = [System.Collections.ArrayList]::new()

    foreach ($f in $files) {
        $fileContent = [IO.File]::ReadAllText($f.FullName)
        $original = $fileContent
        $fileSwapped = $false

        # PHASE 1: Remove all old imports FIRST (before symbol replacement mangles them)
        foreach ($swap in $Swaps) {
            $oldImp = $swap[2]
            if ($oldImp -and ($fileContent -match "import\s+(?:static\s+)?$([regex]::Escape($oldImp))\s*;")) {
                $fileContent = $fileContent -replace "import\s+(?:static\s+)?$([regex]::Escape($oldImp))\s*;\r?\n", ''
            }
        }

        # PHASE 2: Replace symbols in code body
        foreach ($swap in $Swaps) {
            $oldSym = $swap[0]; $newSym = $swap[1]
            if ($fileContent -match [regex]::Escape($oldSym)) {
                $fileContent = $fileContent -replace [regex]::Escape($oldSym), $newSym
                $fileSwapped = $true
            }
        }

        # PHASE 3: Add new imports (if swap happened, not empty, and not already present)
        foreach ($swap in $Swaps) {
            $newImp = $swap[3]
            if ($newImp -and $fileSwapped -and !($fileContent -match "import\s+$([regex]::Escape($newImp))\s*;")) {
                $fileContent = $fileContent -replace '(package\s+[^;]+;\s*\r?\n)', "`$1import $newImp;`n"
            }
        }

        if ($fileContent -ne $original) {
            [IO.File]::WriteAllText($f.FullName, $fileContent)
            [void]$modified.Add($f.FullName.Substring($fullTargetDir.Length + 1))
            Write-Host "  PROPAGATED: $($f.Name)"
        }
    }

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  CREATE-AND-PROPAGATE SUMMARY"
    Write-Host "============================================"
    Write-Host "File created:      $FilePath"
    Write-Host "Files propagated:  $($modified.Count) / $($files.Count)"
    Write-Host "============================================"

    return @{ Created = $FilePath; Modified = $modified.ToArray() }
}
