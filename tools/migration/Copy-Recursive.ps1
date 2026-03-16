# Copy-Recursive.ps1
# ====================
# SUPERWEAPON: Recursive dependency-aware file copier.
#
# Given one or more Java source files from mc1710, this function:
#   1. Copies each file to the corresponding location in core/
#   2. Parses all import statements in the copied file
#   3. For each import that maps to another project file:
#      - Checks if the file's path starts with an ALLOWED prefix (whitelist)
#      - Checks if it already exists in core/
#      - If allowed AND not in core, copies it
#      - Then recursively processes THAT file's imports (subject to AllowRecursePaths)
#   4. Skips all Minecraft/Forge/Java stdlib imports
#   5. Logs every file it copies and every file it skips (with reason)
#
# USAGE:
#   . tools/migration/Copy-Recursive.ps1
#
#   # Copy AnimationController and ALL its allowed deps:
#   Copy-Recursive -Sources @('noppes/npcs/controllers/AnimationController.java') `
#       -AllowPaths @('noppes/npcs/controllers/', 'noppes/npcs/roles/', 'noppes/npcs/quests/', 'noppes/npcs/config/')
#
#   # Copy with recursion restricted to controllers/ only:
#   Copy-Recursive -Sources @('noppes/npcs/controllers/AnimationController.java') `
#       -AllowPaths @('noppes/npcs/controllers/', 'noppes/npcs/roles/', 'noppes/npcs/quests/') `
#       -AllowRecursePaths @('noppes/npcs/controllers/')
#
# PARAMS:
#   -Sources            Array of relative paths within McRoot (e.g., 'noppes/npcs/controllers/Foo.java')
#   -AllowPaths         WHITELIST: only recursively copy files whose path starts with one of these prefixes.
#                       The initial -Sources files are ALWAYS copied regardless of whitelist.
#                       If empty/not provided, ALL project files are allowed (no filter).
#   -AllowRecursePaths  RECURSION WHITELIST: only follow/scan imports of files whose path starts with
#                       one of these prefixes. Files from non-whitelisted paths are still COPIED as
#                       dependencies, but their own imports are NOT recursively followed.
#                       Initial -Sources files ALWAYS have their imports followed regardless.
#                       If empty/not provided, ALL files have their imports followed (current behavior).
#                       DIFFERENT from -AllowPaths:
#                         -AllowPaths = WHERE files can be copied TO (destination filter)
#                         -AllowRecursePaths = WHERE imports can be recursively FOLLOWED FROM (recursion filter)
#   -McRoot             Source root directory (default: 'mc1710/src/main/java')
#   -CoreRoot           Destination root directory (default: 'core/src/main/java')
#   -ProjectRoot        Absolute project root (auto-detected from script location if not provided)
#   -ForbiddenPrefixes  Import prefixes that are NEVER followed (external deps).
#                       Defaults to java.*, net.minecraft.*, cpw.mods.*, net.minecraftforge.*, etc.
#
# OUTPUT:
#   Returns array of all files that were copied (relative paths).
#   Prints detailed log to stdout.

function Copy-Recursive {
    param(
        [Parameter(Mandatory=$true)]
        [string[]]$Sources,

        [string[]]$AllowPaths = @(),

        [string[]]$AllowRecursePaths = @(),

        [string]$McRoot = 'mc1710/src/main/java',
        [string]$CoreRoot = 'core/src/main/java',
        [string]$ProjectRoot,

        # Import prefixes that are NEVER followed/copied (external deps)
        [string[]]$ForbiddenPrefixes = @(
            'java.', 'javax.', 'sun.',
            'net.minecraft.', 'cpw.mods.', 'net.minecraftforge.',
            'org.lwjgl.', 'org.apache.', 'com.google.',
            'org.mozilla.', 'io.netty.', 'org.objectweb.',
            'scala.', 'org.spongepowered.'
        )
    )

    # Auto-detect ProjectRoot from script location if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
    }

    # State
    $script:visited = @{}
    $script:copied = [System.Collections.ArrayList]::new()
    $script:skippedExisting = [System.Collections.ArrayList]::new()
    $script:skippedNotAllowed = [System.Collections.ArrayList]::new()
    $script:skippedRecurse = [System.Collections.ArrayList]::new()
    $script:notFound = [System.Collections.ArrayList]::new()
    $script:initialSourceCount = 0
    $script:nonRecurseImportCount = 0
    $script:depth = 0

    $hasWhitelist = $AllowPaths.Count -gt 0
    $hasRecurseWhitelist = $AllowRecursePaths.Count -gt 0

    function Is-Forbidden {
        param([string]$Import)
        foreach ($prefix in $ForbiddenPrefixes) {
            if ($Import.StartsWith($prefix)) { return $true }
        }
        return $false
    }

    function Is-Allowed {
        param([string]$RelativePath)
        if (-not $hasWhitelist) { return $true }
        foreach ($allow in $AllowPaths) {
            if ($RelativePath.StartsWith($allow)) { return $true }
        }
        return $false
    }

    function Is-RecurseAllowed {
        param([string]$RelativePath)
        if (-not $hasRecurseWhitelist) { return $true }
        foreach ($allow in $AllowRecursePaths) {
            if ($RelativePath.StartsWith($allow)) { return $true }
        }
        return $false
    }

    function Process-File {
        param(
            [string]$RelativePath,
            [bool]$IsInitialSource = $false
        )

        # Normalize separators
        $RelativePath = $RelativePath -replace '\\', '/'

        # Avoid cycles
        if ($script:visited.ContainsKey($RelativePath)) { return }
        $script:visited[$RelativePath] = $true

        # Whitelist check (initial sources bypass)
        if (-not $IsInitialSource -and -not (Is-Allowed -RelativePath $RelativePath)) {
            [void]$script:skippedNotAllowed.Add($RelativePath)
            return
        }

        # Determine if we should recurse into this file's imports
        $shouldRecurse = $IsInitialSource -or (Is-RecurseAllowed -RelativePath $RelativePath)

        $srcFull = Join-Path $ProjectRoot (Join-Path $McRoot $RelativePath)
        $dstFull = Join-Path $ProjectRoot (Join-Path $CoreRoot $RelativePath)

        # Skip if already exists in core
        if (Test-Path $dstFull) {
            [void]$script:skippedExisting.Add($RelativePath)
            $indent = '  ' * $script:depth
            if ($shouldRecurse) {
                # STILL parse imports to find transitive deps that may not exist in core yet
                $content = [IO.File]::ReadAllText($dstFull)
                Write-Host "${indent}EXISTS: $RelativePath"
                Parse-And-Follow -Content $content
            } else {
                Write-Host "${indent}EXISTS: $RelativePath  (recursion restricted -- not in AllowRecursePaths)"
                [void]$script:skippedRecurse.Add($RelativePath)
                $script:nonRecurseImportCount++
            }
            return
        }

        # Skip if source doesn't exist in mc1710
        if (!(Test-Path $srcFull)) {
            [void]$script:notFound.Add($RelativePath)
            return
        }

        # Copy the file
        $dir = Split-Path $dstFull -Parent
        if (!(Test-Path $dir)) { New-Item -ItemType Directory -Path $dir -Force | Out-Null }
        Copy-Item $srcFull $dstFull -Force

        [void]$script:copied.Add($RelativePath)
        $indent = '  ' * $script:depth

        if ($IsInitialSource) {
            $script:initialSourceCount++
            Write-Host "${indent}COPIED: $RelativePath  [initial source]"
        } elseif ($shouldRecurse) {
            Write-Host "${indent}COPIED: $RelativePath"
        } else {
            Write-Host "${indent}COPIED: $RelativePath  (non-recurse import -- path not in AllowRecursePaths)"
            $script:nonRecurseImportCount++
        }

        # Parse imports and recurse (only if allowed)
        if ($shouldRecurse) {
            $content = [IO.File]::ReadAllText($srcFull)
            Parse-And-Follow -Content $content
        } else {
            [void]$script:skippedRecurse.Add($RelativePath)
        }
    }

    function Parse-And-Follow {
        param([string]$Content)

        $imports = [regex]::Matches($Content, 'import\s+(?:static\s+)?([^;]+);')

        $script:depth++

        foreach ($match in $imports) {
            $imp = $match.Groups[1].Value.Trim()

            # Skip forbidden (MC, Forge, Java stdlib, etc.)
            if (Is-Forbidden -Import $imp) {
                continue
            }

            # Handle wildcard imports: import noppes.npcs.controllers.data.*;
            if ($imp -match '\.\*$') {
                $pkgPath = ($imp -replace '\.\*$', '') -replace '\.', '/'
                $pkgDir = Join-Path $ProjectRoot (Join-Path $McRoot $pkgPath)
                if (Test-Path $pkgDir) {
                    Get-ChildItem -Path $pkgDir -Filter '*.java' -File | ForEach-Object {
                        $relPath = ($pkgPath + '/' + $_.Name) -replace '\\', '/'
                        Process-File -RelativePath $relPath
                    }
                }
                continue
            }

            # Convert import to file path and try to find it
            $filePath = ($imp -replace '\.', '/') + '.java'
            $fullCheck = Join-Path $ProjectRoot (Join-Path $McRoot $filePath)

            if (Test-Path $fullCheck) {
                Process-File -RelativePath $filePath
            } else {
                # Might be inner class or static member -- try removing last segment(s)
                $parts = $imp -split '\.'
                for ($i = $parts.Length - 1; $i -ge 1; $i--) {
                    $tryImport = ($parts[0..($i-1)] -join '/') + '.java'
                    $tryFull = Join-Path $ProjectRoot (Join-Path $McRoot $tryImport)
                    if (Test-Path $tryFull) {
                        Process-File -RelativePath $tryImport
                        break
                    }
                }
            }
        }

        $script:depth--
    }

    # ===== MAIN EXECUTION =====
    Write-Host ""
    Write-Host "============================================"
    Write-Host "  COPY-RECURSIVE -- Dependency-Aware Copier"
    Write-Host "============================================"
    Write-Host "Source root:  $McRoot"
    Write-Host "Dest root:    $CoreRoot"
    Write-Host "Targets:      $($Sources.Count) file(s)"
    if ($hasWhitelist) {
        Write-Host "AllowPaths:   $($AllowPaths.Count) path prefix(es)  [copy destination whitelist]"
        foreach ($a in $AllowPaths) { Write-Host "  >> $a" }
    } else {
        Write-Host "AllowPaths:   NONE (all project files allowed)"
    }
    if ($hasRecurseWhitelist) {
        Write-Host "AllowRecursePaths: $($AllowRecursePaths.Count) prefix(es)  [recursion source whitelist]"
        foreach ($a in $AllowRecursePaths) { Write-Host "  ~> $a" }
    } else {
        Write-Host "AllowRecursePaths: NONE (all discovered files recursed)"
    }
    Write-Host "============================================"
    Write-Host ""

    foreach ($src in $Sources) {
        $src = $src -replace '\\', '/'
        Process-File -RelativePath $src -IsInitialSource $true
    }

    # ===== SUMMARY =====
    $recursivelyDiscovered = $script:copied.Count - $script:initialSourceCount
    Write-Host ""
    Write-Host "============================================"
    Write-Host "  RECURSIVE COPY SUMMARY"
    Write-Host "============================================"
    Write-Host "Copied: $($script:copied.Count) files ($($script:initialSourceCount) initial sources, $($recursivelyDiscovered) recursively discovered, $($script:nonRecurseImportCount) non-recurse imports)"
    Write-Host ""
    Write-Host "Files COPIED:            $($script:copied.Count)"
    foreach ($f in $script:copied) {
        Write-Host "  + $f"
    }
    Write-Host ""
    Write-Host "Already in core:         $($script:skippedExisting.Count)"
    foreach ($f in $script:skippedExisting) {
        Write-Host "  = $f"
    }
    Write-Host ""
    Write-Host "Skipped (not whitelisted): $($script:skippedNotAllowed.Count)"
    foreach ($f in $script:skippedNotAllowed) {
        Write-Host "  x $f"
    }
    if ($hasRecurseWhitelist) {
        Write-Host ""
        Write-Host "Recursion restricted:    $($script:skippedRecurse.Count)  (copied but imports NOT followed)"
        foreach ($f in $script:skippedRecurse) {
            Write-Host "  ~ $f  -- path not in AllowRecursePaths"
        }
    }
    Write-Host ""
    Write-Host "Not found in mc1710:     $($script:notFound.Count)"
    foreach ($f in $script:notFound) {
        Write-Host "  ? $f"
    }
    Write-Host "============================================"

    return $script:copied.ToArray()
}
