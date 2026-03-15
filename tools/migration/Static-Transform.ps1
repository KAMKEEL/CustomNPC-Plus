# Static-Transform.ps1
# ====================
# SUPERWEAPON: Regex-based method call rewriting across all files.
#
# Unlike Symbol-Swap (which does simple name replacement), this function handles
# structural transformations where method signatures change:
#   - CompressedStreamTools.readCompressed(file) -> PlatformServiceHolder.get().readCompressedNBT(file)
#   - new NBTTagCompound() -> NBT.compound()
#   - MathHelper.clamp_int(x, y, z) -> ValueUtil.clampInt(x, y, z)
#
# USAGE:
#   . tools/migration/Static-Transform.ps1
#
#   Static-Transform -Directory 'core/src/main/java' -Transforms @(
#       @('CompressedStreamTools\.readCompressed\s*\(([^)]+)\)',
#         'PlatformServiceHolder.get().readCompressedNBT($1)'),
#       @('new\s+NBTTagCompound\s*\(\)',
#         'NBT.compound()'),
#       @('MathHelper\.clamp_int\s*\(',
#         'ValueUtil.clampInt(')
#   )
#
# PARAMS:
#   -Directory    Relative path (from ProjectRoot) to scan for .java files recursively.
#   -Transforms   Array of 2-element arrays: @(regexPattern, replacement).
#                 - regexPattern: A .NET regex pattern matching the code to transform.
#                   Use capture groups ($1, $2, etc.) to preserve arguments.
#                 - replacement: The replacement string. Use $1, $2 for captured groups.
#   -ProjectRoot  Absolute project root (auto-detected from script location if not provided)
#
# OUTPUT:
#   Returns array of modified file paths (relative to Directory).
#   Prints log to stdout.

function Static-Transform {
    param(
        [Parameter(Mandatory=$true)]
        [string]$Directory,

        [Parameter(Mandatory=$true)]
        [array]$Transforms,

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

    # Guard against PowerShell single-element array flattening
    if ($Transforms.Count -gt 0 -and $Transforms[0] -is [string]) {
        $Transforms = @(,$Transforms)
    }

    $files = Get-ChildItem -Recurse -Filter '*.java' -Path $fullDir
    $modified = [System.Collections.ArrayList]::new()
    $totalTransforms = 0

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  STATIC-TRANSFORM -- Method Call Rewriter"
    Write-Host "============================================"
    Write-Host "Directory:    $Directory"
    Write-Host "Files:        $($files.Count)"
    Write-Host "Transforms:   $($Transforms.Count)"
    foreach ($t in $Transforms) {
        # Show a truncated version of the pattern for readability
        $shortPattern = if ($t[0].Length -gt 50) { $t[0].Substring(0, 50) + '...' } else { $t[0] }
        Write-Host "  /$shortPattern/ -> $($t[1])"
    }
    Write-Host "============================================"
    Write-Host ""

    foreach ($f in $files) {
        $content = [IO.File]::ReadAllText($f.FullName)
        $original = $content
        $fileTransformCount = 0

        foreach ($t in $Transforms) {
            $pattern = $t[0]; $replacement = $t[1]

            $matches = [regex]::Matches($content, $pattern)
            if ($matches.Count -gt 0) {
                $content = [regex]::Replace($content, $pattern, $replacement)
                $fileTransformCount += $matches.Count
            }
        }

        if ($content -ne $original) {
            [IO.File]::WriteAllText($f.FullName, $content)
            [void]$modified.Add($f.FullName.Substring($fullDir.Length + 1))
            $totalTransforms += $fileTransformCount
            Write-Host "  TRANSFORMED ($fileTransformCount hits): $($f.Name)"
        }
    }

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  STATIC-TRANSFORM SUMMARY"
    Write-Host "============================================"
    Write-Host "Files modified:     $($modified.Count) / $($files.Count)"
    Write-Host "Total transforms:   $totalTransforms"
    Write-Host "============================================"

    return $modified.ToArray()
}
