# test_allow_recurse_paths.ps1
# ============================
# Test script for the -AllowRecursePaths parameter on Copy-Recursive.
#
# Runs 3 scenarios against REAL codebase files:
#   Scenario A: No AllowRecursePaths (all imports followed - current/default behavior)
#   Scenario B: AllowRecursePaths = kamkeel/npcs/controllers only
#   Scenario C: AllowRecursePaths = noppes/npcs/controllers + noppes/npcs/util
#
# Each scenario copies to a temporary core directory, counts results, and cleans up.
# Execute: powershell -ExecutionPolicy Bypass -File "tools/migration/test_allow_recurse_paths.ps1"

. (Join-Path $PSScriptRoot 'Copy-Recursive.ps1')

$ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
$McRoot = 'mc1710/src/main/java'
$TestCoreBase = Join-Path $ProjectRoot '_test_allow_recurse_paths'

# Cleanup from previous runs
if (Test-Path $TestCoreBase) {
    Remove-Item $TestCoreBase -Recurse -Force
}

# Common sources and AllowPaths for all scenarios
# We use DialogController because it imports from many different packages:
#   - kamkeel/npcs/controllers/SyncController
#   - noppes/npcs/controllers/data/Dialog, DialogCategory, DialogOption
#   - noppes/npcs/constants/EnumOptionType
#   - noppes/npcs/util/NBTJsonUtil
#   - noppes/npcs/CustomNpcs, noppes/npcs/LogWriter, noppes/npcs/NoppesStringUtils
$Sources = @(
    'noppes/npcs/controllers/DialogController.java'
)
# Wide AllowPaths so files from many directories CAN be copied (destination-allowed)
$AllowPaths = @(
    'noppes/npcs/controllers/',
    'noppes/npcs/controllers/data/',
    'noppes/npcs/constants/',
    'noppes/npcs/util/',
    'noppes/npcs/',
    'kamkeel/npcs/controllers/'
)

function Run-Scenario {
    param(
        [string]$ScenarioName,
        [string]$Description,
        [string[]]$RecursePaths = @()
    )
    Write-Host ""
    Write-Host "================================================================" -ForegroundColor Cyan
    Write-Host ("  SCENARIO {0}: {1}" -f $ScenarioName, $Description) -ForegroundColor Cyan
    Write-Host "================================================================" -ForegroundColor Cyan

    $scenarioDir = "scenario_${ScenarioName}"
    $scenarioCore = Join-Path $TestCoreBase $scenarioDir
    New-Item -ItemType Directory -Path (Join-Path $scenarioCore 'src/main/java') -Force | Out-Null

    $result = Copy-Recursive `
        -Sources $Sources `
        -AllowPaths $AllowPaths `
        -AllowRecursePaths $RecursePaths `
        -McRoot $McRoot `
        -CoreRoot "_test_allow_recurse_paths/${scenarioDir}/src/main/java" `
        -ProjectRoot $ProjectRoot

    Write-Host ""
    $cnt = 0
    if ($result) { $cnt = $result.Count }
    Write-Host ("  >> Scenario {0} result: {1} files copied" -f $ScenarioName, $cnt) -ForegroundColor Yellow
    Write-Host ""
    return $result
}

# ================================================================
# SCENARIO A: No AllowRecursePaths (default - all imports followed)
# ================================================================
$resultA = Run-Scenario -ScenarioName 'A' -Description 'No AllowRecursePaths (default behavior, all imports followed)'

# ================================================================
# SCENARIO B: AllowRecursePaths = kamkeel/npcs/controllers only
# ================================================================
$resultB = Run-Scenario -ScenarioName 'B' `
    -Description 'AllowRecursePaths = kamkeel/npcs/controllers/ only' `
    -RecursePaths @('kamkeel/npcs/controllers/')

# ================================================================
# SCENARIO C: AllowRecursePaths = noppes/npcs/controllers + noppes/npcs/util
# ================================================================
$resultC = Run-Scenario -ScenarioName 'C' `
    -Description 'AllowRecursePaths = noppes/npcs/controllers/ + noppes/npcs/util/' `
    -RecursePaths @('noppes/npcs/controllers/', 'noppes/npcs/util/')

# ================================================================
# COMPARISON TABLE
# ================================================================
Write-Host ""
Write-Host "================================================================" -ForegroundColor Green
Write-Host "  COMPARISON TABLE" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host ""
$countA = 0; if ($resultA) { $countA = $resultA.Count }
$countB = 0; if ($resultB) { $countB = $resultB.Count }
$countC = 0; if ($resultC) { $countC = $resultC.Count }
Write-Host ("  Scenario  AllowRecursePaths                   Files Copied") -ForegroundColor White
Write-Host ("  --------  ----------------------------------  ------------") -ForegroundColor White
Write-Host ("  A         (none - all recursed)               {0}" -f $countA) -ForegroundColor White
Write-Host ("  B         kamkeel/npcs/controllers/           {0}" -f $countB) -ForegroundColor White
Write-Host ("  C         noppes/.../controllers/ + util/     {0}" -f $countC) -ForegroundColor White
Write-Host ""

# Show what's in A but not B (files that were NOT copied due to restricted recursion)
if ($resultA -and $resultB) {
    $onlyInA_notB = $resultA | Where-Object { $resultB -notcontains $_ }
    if ($onlyInA_notB -and $onlyInA_notB.Count -gt 0) {
        Write-Host ("Files in Scenario A but NOT in Scenario B ({0} files):" -f $onlyInA_notB.Count) -ForegroundColor Yellow
        foreach ($f in $onlyInA_notB) {
            Write-Host "  - $f" -ForegroundColor DarkYellow
        }
    }
}

if ($resultA -and $resultC) {
    $onlyInA_notC = $resultA | Where-Object { $resultC -notcontains $_ }
    if ($onlyInA_notC -and $onlyInA_notC.Count -gt 0) {
        Write-Host ""
        Write-Host ("Files in Scenario A but NOT in Scenario C ({0} files):" -f $onlyInA_notC.Count) -ForegroundColor Yellow
        foreach ($f in $onlyInA_notC) {
            Write-Host "  - $f" -ForegroundColor DarkYellow
        }
    }
}

Write-Host ""

# Backward compat check
Write-Host "================================================================" -ForegroundColor Green
Write-Host "  BACKWARD COMPATIBILITY CHECK" -ForegroundColor Green
Write-Host "================================================================" -ForegroundColor Green
Write-Host ("Scenario A (no AllowRecursePaths) = default behavior: {0} files copied" -f $countA) -ForegroundColor White
Write-Host "This matches the behavior of Copy-Recursive WITHOUT the new parameter." -ForegroundColor White
Write-Host ""

# Cleanup
Write-Host "Cleaning up test directories..." -ForegroundColor DarkGray
if (Test-Path $TestCoreBase) {
    Remove-Item $TestCoreBase -Recurse -Force
}
Write-Host "Done." -ForegroundColor DarkGray
