# Generate-Stubs.ps1
# ==================
# SUPERWEAPON: Create temporary compilation stubs for missing types.
#
# When core/ references classes that haven't been migrated yet (e.g., SyncController,
# ScriptController), the build fails. This function generates minimal placeholder
# classes so the build passes. The stubs get replaced when the real class arrives.
#
# USAGE:
#   . tools/migration/Generate-Stubs.ps1
#
#   Generate-Stubs -Stubs @(
#       @{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class';
#          body='public static void updateDialog(Object d) {} public static void removeDialog(int id) {}' },
#       @{ pkg='noppes.npcs.controllers'; name='ScriptController'; type='class';
#          body='public static boolean HasStart = false;' }
#   ) -TargetRoot 'core/src/main/java'
#
# PARAMS:
#   -Stubs       Array of hashtables, each with:
#                  pkg  - Package name (e.g., 'noppes.npcs.controllers')
#                  name - Class/interface name (e.g., 'SyncController')
#                  type - 'class' or 'interface'
#                  body - Code body (method stubs, field declarations). Can be multiline.
#   -TargetRoot  Relative path (from ProjectRoot) where stubs are created.
#                Defaults to 'core/src/main/java'.
#   -ProjectRoot Absolute project root (auto-detected from script location if not provided)
#
# OUTPUT:
#   Returns array of created file paths (relative to TargetRoot).
#   Prints log to stdout.

function Generate-Stubs {
    param(
        [Parameter(Mandatory=$true)]
        [array]$Stubs,

        [string]$TargetRoot = 'core/src/main/java',

        [string]$ProjectRoot
    )

    # Auto-detect ProjectRoot from script location if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
    }

    $created = [System.Collections.ArrayList]::new()

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  GENERATE-STUBS -- Compilation Placeholders"
    Write-Host "============================================"
    Write-Host "Target root: $TargetRoot"
    Write-Host "Stubs:       $($Stubs.Count)"
    Write-Host "============================================"
    Write-Host ""

    foreach ($s in $Stubs) {
        $pkgPath = $s.pkg -replace '\.', '/'
        $dir = Join-Path $ProjectRoot (Join-Path $TargetRoot $pkgPath)
        if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }

        $filePath = Join-Path $dir "$($s.name).java"
        $relativePath = "$pkgPath/$($s.name).java"

        # Check if file already exists -- do NOT overwrite real classes
        if (Test-Path $filePath) {
            $existingContent = [IO.File]::ReadAllText($filePath)
            if ($existingContent -notmatch 'TEMPORARY STUB') {
                Write-Host "  SKIPPED (real class exists): $relativePath"
                continue
            }
        }

        $content = @"
package $($s.pkg);

/**
 * TEMPORARY STUB -- auto-generated for compilation.
 * Will be replaced when this class is properly migrated.
 * DO NOT add logic here.
 */
public $($s.type) $($s.name) {
    $($s.body)
}
"@
        [IO.File]::WriteAllText($filePath, $content)
        [void]$created.Add($relativePath)
        Write-Host "  STUB: $($s.pkg).$($s.name)"
    }

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  GENERATE-STUBS SUMMARY"
    Write-Host "============================================"
    Write-Host "Stubs created: $($created.Count) / $($Stubs.Count)"
    Write-Host "============================================"

    return $created.ToArray()
}
