# Build-Report.ps1
# =================
# SUPERWEAPON: Run a Gradle build and categorize errors into actionable patterns.
#
# Instead of dumping 500 lines of raw Gradle output, this function:
#   1. Runs the specified Gradle task
#   2. Captures all error lines
#   3. Categorizes them into patterns: MISSING_CLASS, MISSING_PACKAGE, MISSING_METHOD,
#      TYPE_MISMATCH, OTHER
#   4. Outputs a structured report with counts and sample errors
#
# The agent reads the report and fixes errors one category at a time. No panic.
#
# USAGE:
#   . tools/migration/Build-Report.ps1
#
#   Build-Report -GradleTask ':core:compileJava'
#   Build-Report -GradleTask ':core:compileJava' -MaxSamples 5
#
# PARAMS:
#   -GradleTask   The Gradle task to run (e.g., ':core:compileJava', 'build').
#   -MaxSamples   Max number of sample errors to show per category. Default: 3.
#   -ProjectRoot  Absolute project root (auto-detected from script location if not provided)
#
# OUTPUT:
#   Returns hashtable of categorized errors.
#   Prints structured report to stdout.

function Build-Report {
    param(
        [Parameter(Mandatory=$true)]
        [string]$GradleTask,

        [int]$MaxSamples = 3,

        [string]$ProjectRoot
    )

    # Auto-detect ProjectRoot from script location if not provided
    if (-not $ProjectRoot) {
        $ProjectRoot = (Resolve-Path (Join-Path $PSScriptRoot '..\..')).Path
    }

    Write-Host ""
    Write-Host "============================================"
    Write-Host "  BUILD-REPORT -- Error Categorizer"
    Write-Host "============================================"
    Write-Host "Task:     $GradleTask"
    Write-Host "Root:     $ProjectRoot"
    Write-Host "============================================"
    Write-Host ""
    Write-Host "Running build..."

    # Run Gradle and capture output
    $gradlew = Join-Path $ProjectRoot 'gradlew.bat'
    if (!(Test-Path $gradlew)) {
        $gradlew = Join-Path $ProjectRoot 'gradlew'
    }

    $output = & cmd /c "`"$gradlew`" $GradleTask 2>&1" | Out-String

    # Parse error lines
    $errors = @{}
    $output -split "`n" | Where-Object { $_ -match 'error:' } | ForEach-Object {
        $line = $_.Trim()

        if ($line -match '(?<file>[^\s:]+\.java):(?<linenum>\d+):\s*error:\s*(?<msg>.+)') {
            $msg = $Matches.msg.Trim()
            $file = $Matches.file
            $linenum = $Matches.linenum

            $category = if ($msg -match 'cannot find symbol.*symbol:\s*class\s+(\w+)') {
                "MISSING_CLASS:$($Matches[1])"
            } elseif ($msg -match 'package\s+(\S+)\s+does not exist') {
                "MISSING_PACKAGE:$($Matches[1])"
            } elseif ($msg -match 'cannot find symbol.*symbol:\s*method\s+(\w+)') {
                "MISSING_METHOD:$($Matches[1])"
            } elseif ($msg -match 'cannot find symbol.*symbol:\s*variable\s+(\w+)') {
                "MISSING_VARIABLE:$($Matches[1])"
            } elseif ($msg -match 'cannot find symbol') {
                "MISSING_SYMBOL"
            } elseif ($msg -match 'incompatible types') {
                "TYPE_MISMATCH"
            } elseif ($msg -match 'cannot be applied') {
                "BAD_ARGS"
            } elseif ($msg -match 'is not abstract') {
                "ABSTRACT_VIOLATION"
            } elseif ($msg -match 'unreported exception') {
                "UNCHECKED_EXCEPTION"
            } else {
                "OTHER"
            }

            if (!$errors[$category]) { $errors[$category] = [System.Collections.ArrayList]::new() }
            [void]$errors[$category].Add("$file`:$linenum -- $msg")
        }
    }

    # Count total
    $totalErrors = 0
    foreach ($cat in $errors.Keys) { $totalErrors += $errors[$cat].Count }

    # Check if build succeeded
    $buildPassed = $output -match 'BUILD SUCCESSFUL'

    # Report
    Write-Host ""
    Write-Host "============================================"
    Write-Host "  BUILD REPORT"
    Write-Host "============================================"

    if ($buildPassed) {
        Write-Host "BUILD SUCCESSFUL -- no errors!"
        Write-Host "============================================"
        return @{ Success = $true; Errors = @{}; Total = 0 }
    }

    Write-Host "Total errors: $totalErrors"
    Write-Host ""

    foreach ($cat in $errors.Keys | Sort-Object) {
        $count = $errors[$cat].Count
        Write-Host "--- $cat ($count occurrences) ---"
        $errors[$cat] | Select-Object -First $MaxSamples | ForEach-Object {
            Write-Host "  $_"
        }
        if ($count -gt $MaxSamples) {
            Write-Host "  ... and $($count - $MaxSamples) more"
        }
        Write-Host ""
    }

    Write-Host "============================================"

    return @{ Success = $false; Errors = $errors; Total = $totalErrors }
}
