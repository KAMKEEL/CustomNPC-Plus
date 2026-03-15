# Dedupe-Imports.ps1
# ==================
# SUPERWEAPON: Clean up duplicate imports after Symbol-Swap operations.
#
# After Symbol-Swap runs, a file might have duplicate imports (e.g., both
# NBTTagCompound and NBTTagString map to INbt, producing two identical import lines).
# This function deduplicates and sorts all imports in every .java file.
#
# USAGE:
#   . tools/migration/Dedupe-Imports.ps1
#
#   Dedupe-Imports -Directory 'core/src/main/java'
#
# PARAMS:
#   -Directory    Relative path (from ProjectRoot) to scan for .java files recursively.
#   -ProjectRoot  Absolute project root (auto-detected from script location if not provided)
#
# OUTPUT:
#   Returns array of modified file paths (relative to Directory).
#   Prints log to stdout.

function Dedupe-Imports {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Directory,

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

    $files = Get-ChildItem -Recurse -Filter '*.java' -Path $fullDir
    $modified = [System.Collections.ArrayList]::new()
    $totalDupsRemoved = 0

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  DEDUPE-IMPORTS -- Import Deduplicator"
    Write-Host "============================================"
    Write-Host "Directory:  $Directory"
    Write-Host "Files:      $($files.Count)"
    Write-Host "============================================"
    Write-Host ""

    foreach ($f in $files) {
        $content = [IO.File]::ReadAllText($f.FullName)

        # Extract all import lines (preserving static vs non-static)
        $importMatches = [regex]::Matches($content, '(?m)^import\s+(?:static\s+)?[^;]+;\s*$')
        if ($importMatches.Count -eq 0) { continue }

        $importLines = $importMatches | ForEach-Object { $_.Value.Trim() }
        $uniqueImports = $importLines | Sort-Object -Unique

        # Check if there are duplicates
        $dupsRemoved = $importLines.Count - $uniqueImports.Count
        if ($dupsRemoved -eq 0) { continue }

        # Remove all existing import lines
        $noImports = [regex]::Replace($content, '(?m)^import\s+(?:static\s+)?[^;]+;\s*\r?\n', '')

        # Clean up any extra blank lines left behind (max 2 consecutive)
        $noImports = [regex]::Replace($noImports, '(\r?\n){3,}', "`n`n")

        # Re-insert deduplicated imports after package line
        $importBlock = ($uniqueImports -join "`n") + "`n"
        $result = $noImports -replace '(package\s+[^;]+;\s*\r?\n)\s*', "`$1`n$importBlock`n"

        if ($result -ne $content) {
            [IO.File]::WriteAllText($f.FullName, $result)
            [void]$modified.Add($f.FullName.Substring($fullDir.Length + 1))
            $totalDupsRemoved += $dupsRemoved
            Write-Host "  DEDUPED ($dupsRemoved dups): $($f.Name)"
        }
    }

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  DEDUPE-IMPORTS SUMMARY"
    Write-Host "============================================"
    Write-Host "Files modified:      $($modified.Count) / $($files.Count)"
    Write-Host "Duplicates removed:  $totalDupsRemoved"
    Write-Host "============================================"

    return $modified.ToArray()
}
