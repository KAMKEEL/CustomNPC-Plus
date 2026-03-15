# Nuke-Imports.ps1
# =================
# SUPERWEAPON: Remove ALL forbidden imports from a directory.
#
# Scans every .java file in a directory and removes any import line
# whose package matches a forbidden prefix. Run this AFTER mass-copy,
# BEFORE Symbol-Swap, to clean the slate.
#
# USAGE:
#   . tools/migration/Nuke-Imports.ps1
#
#   Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes @(
#       'net.minecraft.', 'cpw.mods.', 'net.minecraftforge.'
#   )
#
# PARAMS:
#   -Directory          Relative path (from ProjectRoot) to scan for .java files recursively.
#   -ForbiddenPrefixes  Array of import prefixes to remove. ALL imports starting with these
#                       will be deleted. Passed as dotted package prefixes (e.g., 'net.minecraft.').
#   -ProjectRoot        Absolute project root (auto-detected from script location if not provided)
#
# OUTPUT:
#   Returns array of modified file paths (relative to Directory).
#   Prints log to stdout.

function Nuke-Imports {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Directory,

        [Parameter(Mandatory=$true)]
        [string[]]$ForbiddenPrefixes,

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

    # Build regex pattern from forbidden prefixes
    $escapedPrefixes = $ForbiddenPrefixes | ForEach-Object { [regex]::Escape($_) }
    $pattern = ($escapedPrefixes | ForEach-Object { "import\s+(?:static\s+)?$_" }) -join '|'

    $files = Get-ChildItem -Recurse -Filter '*.java' -Path $fullDir
    $modified = [System.Collections.ArrayList]::new()
    $totalNuked = 0

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  NUKE-IMPORTS -- Forbidden Import Remover"
    Write-Host "============================================"
    Write-Host "Directory:       $Directory"
    Write-Host "Files to scan:   $($files.Count)"
    Write-Host "Forbidden:       $($ForbiddenPrefixes.Count) prefix(es)"
    foreach ($p in $ForbiddenPrefixes) { Write-Host "  x $p" }
    Write-Host "============================================"
    Write-Host ""

    foreach ($f in $files) {
        $content = [IO.File]::ReadAllText($f.FullName)
        $original = $content

        # Remove matching import lines (entire line including newline)
        $nukedContent = [regex]::Replace($content, "(?m)^(?:$pattern)[^;]*;\s*\r?\n", '')

        if ($nukedContent -ne $original) {
            # Count how many imports were removed
            $beforeCount = ([regex]::Matches($original, "(?m)^(?:$pattern)[^;]*;")).Count
            $totalNuked += $beforeCount

            [IO.File]::WriteAllText($f.FullName, $nukedContent)
            [void]$modified.Add($f.FullName.Substring($fullDir.Length + 1))
            Write-Host "  NUKED $beforeCount imports: $($f.Name)"
        }
    }

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  NUKE-IMPORTS SUMMARY"
    Write-Host "============================================"
    Write-Host "Files modified:  $($modified.Count) / $($files.Count)"
    Write-Host "Imports removed: $totalNuked"
    Write-Host "============================================"

    return $modified.ToArray()
}
