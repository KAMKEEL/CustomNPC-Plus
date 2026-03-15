# Test runner for all migration tools
# Run: powershell -ExecutionPolicy Bypass -File tools/migration/test_runner.ps1

$ErrorActionPreference = 'Stop'
$passed = 0
$failed = 0

function Reset-Sandbox {
    $sandboxDir = Join-Path $PSScriptRoot 'test_sandbox\src'

    # TestController.java
    $dir1 = Join-Path $sandboxDir 'noppes\npcs\controllers'
    if (!(Test-Path $dir1)) { New-Item -ItemType Directory -Path $dir1 -Force | Out-Null }
    [IO.File]::WriteAllText("$dir1\TestController.java", @'
package noppes.npcs.controllers;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.entity.player.EntityPlayer;
import cpw.mods.fml.common.event.FMLServerStartingEvent;
import net.minecraftforge.common.MinecraftForge;
import noppes.npcs.controllers.data.TestData;
import noppes.npcs.util.TestUtil;
import java.util.HashMap;
import java.util.List;

public class TestController {
    private HashMap<String, NBTTagCompound> data = new HashMap<>();
    private NBTTagList tagList;

    public void save(EntityPlayer player) {
        NBTTagCompound compound = new NBTTagCompound();
        compound.setString("name", player.getCommandSenderName());
        CompressedStreamTools.writeCompressed(compound, outputStream);
    }

    public void load() {
        NBTTagCompound compound = CompressedStreamTools.readCompressed(inputStream);
        MathHelper.clamp_int(value, 0, 100);
    }
}
'@)

    # TestData.java
    $dir2 = Join-Path $sandboxDir 'noppes\npcs\controllers\data'
    if (!(Test-Path $dir2)) { New-Item -ItemType Directory -Path $dir2 -Force | Out-Null }
    [IO.File]::WriteAllText("$dir2\TestData.java", @'
package noppes.npcs.controllers.data;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.nbt.NBTTagString;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.entity.player.EntityPlayerMP;
import net.minecraft.item.ItemStack;
import noppes.npcs.controllers.TestController;
import noppes.npcs.util.TestUtil;
import java.util.ArrayList;

public class TestData {
    private NBTTagCompound nbt;
    private EntityPlayer player;

    public NBTTagCompound writeNBT(NBTTagCompound compound) {
        NBTTagList list = new NBTTagList();
        NBTTagString str = new NBTTagString("hello");
        compound.setTag("list", list);
        return compound;
    }

    public void readNBT(NBTTagCompound compound) {
        NBTTagList list = compound.getTagList("list", 10);
    }

    public ItemStack getItem() {
        return null;
    }
}
'@)

    # TestUtil.java (with duplicate imports)
    $dir3 = Join-Path $sandboxDir 'noppes\npcs\util'
    if (!(Test-Path $dir3)) { New-Item -ItemType Directory -Path $dir3 -Force | Out-Null }
    [IO.File]::WriteAllText("$dir3\TestUtil.java", @'
package noppes.npcs.util;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagCompound;
import noppes.npcs.controllers.TestController;
import noppes.npcs.controllers.TestController;
import java.util.Map;

public class TestUtil {
    public static NBTTagCompound merge(NBTTagCompound a, NBTTagCompound b) {
        NBTTagCompound result = new NBTTagCompound();
        return result;
    }
}
'@)
}

function Assert-Contains {
    param([string]$Content, [string]$Expected, [string]$TestName)
    if ($Content -match [regex]::Escape($Expected)) {
        return $true
    } else {
        Write-Host "  FAIL: Expected '$Expected' in $TestName" -ForegroundColor Red
        return $false
    }
}

function Assert-NotContains {
    param([string]$Content, [string]$Unexpected, [string]$TestName)
    if ($Content -match [regex]::Escape($Unexpected)) {
        Write-Host "  FAIL: Did NOT expect '$Unexpected' in $TestName" -ForegroundColor Red
        return $false
    } else {
        return $true
    }
}

# ===== Load all tools =====
. (Join-Path $PSScriptRoot 'Symbol-Swap.ps1')
. (Join-Path $PSScriptRoot 'Nuke-Imports.ps1')
. (Join-Path $PSScriptRoot 'Static-Transform.ps1')
. (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')
. (Join-Path $PSScriptRoot 'Generate-Stubs.ps1')
. (Join-Path $PSScriptRoot 'Create-And-Propagate.ps1')

$sandboxRel = 'tools/migration/test_sandbox/src'
$sandboxAbs = Join-Path $PSScriptRoot 'test_sandbox\src'

# ============================================================
Write-Host "`n========== TEST 1: Symbol-Swap ==========" -ForegroundColor Cyan
Reset-Sandbox

$result = Symbol-Swap -Directory $sandboxRel -Swaps @(
    @('NBTTagCompound', 'INbt', 'net.minecraft.nbt.NBTTagCompound', 'noppes.npcs.api.nbt.INbt'),
    @('NBTTagList', 'INbtList', 'net.minecraft.nbt.NBTTagList', 'noppes.npcs.api.nbt.INbtList'),
    @('EntityPlayer', 'IPlayer', 'net.minecraft.entity.player.EntityPlayer', 'noppes.npcs.api.entity.IPlayer'),
    @('ItemStack', 'IItemStack', 'net.minecraft.item.ItemStack', 'noppes.npcs.api.item.IItemStack')
)

$tc = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\controllers\TestController.java")
$td = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\controllers\data\TestData.java")

$ok = $true
$ok = (Assert-Contains $tc 'import noppes.npcs.api.nbt.INbt;' 'TestController new import') -and $ok
$ok = (Assert-NotContains $tc 'import net.minecraft.nbt.NBTTagCompound;' 'TestController old import removed') -and $ok
$ok = (Assert-Contains $tc 'HashMap<String, INbt>' 'TestController symbol swap') -and $ok
$ok = (Assert-Contains $td 'import noppes.npcs.api.entity.IPlayer;' 'TestData IPlayer import') -and $ok
$ok = (Assert-Contains $td 'private IPlayer player;' 'TestData EntityPlayer swap') -and $ok
$ok = (Assert-Contains $td 'public IItemStack getItem()' 'TestData ItemStack swap') -and $ok
# Verify old MC imports are gone
$ok = (Assert-NotContains $td 'import net.minecraft.entity.player.EntityPlayer;' 'TestData old EntityPlayer import') -and $ok
$ok = (Assert-NotContains $td 'import net.minecraft.item.ItemStack;' 'TestData old ItemStack import') -and $ok
# Verify no mangled imports (the bug we fixed)
$ok = (Assert-NotContains $tc 'import net.minecraft.nbt.INbt;' 'No mangled import') -and $ok

if ($ok) { $passed++; Write-Host "  PASS: Symbol-Swap" -ForegroundColor Green } else { $failed++ }

# ============================================================
Write-Host "`n========== TEST 2: Nuke-Imports ==========" -ForegroundColor Cyan
Reset-Sandbox

$result = Nuke-Imports -Directory $sandboxRel -ForbiddenPrefixes @(
    'net.minecraft.', 'cpw.mods.', 'net.minecraftforge.'
)

$tc = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\controllers\TestController.java")
$td = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\controllers\data\TestData.java")

$ok = $true
$ok = (Assert-NotContains $tc 'import net.minecraft' 'TestController MC imports nuked') -and $ok
$ok = (Assert-NotContains $tc 'import cpw.mods' 'TestController FML imports nuked') -and $ok
$ok = (Assert-NotContains $tc 'import net.minecraftforge' 'TestController Forge imports nuked') -and $ok
$ok = (Assert-Contains $tc 'import noppes.npcs.controllers.data.TestData;' 'TestController project imports preserved') -and $ok
$ok = (Assert-Contains $tc 'import java.util.HashMap;' 'TestController java imports preserved') -and $ok
$ok = (Assert-NotContains $td 'import net.minecraft' 'TestData MC imports nuked') -and $ok
$ok = (Assert-Contains $td 'import noppes.npcs.controllers.TestController;' 'TestData project imports preserved') -and $ok

if ($ok) { $passed++; Write-Host "  PASS: Nuke-Imports" -ForegroundColor Green } else { $failed++ }

# ============================================================
Write-Host "`n========== TEST 3: Static-Transform ==========" -ForegroundColor Cyan
Reset-Sandbox

$result = Static-Transform -Directory $sandboxRel -Transforms @(
    @('CompressedStreamTools\.writeCompressed\s*\(([^,]+),\s*([^)]+)\)', 'PlatformServiceHolder.get().writeCompressedNBT($1, $2)'),
    @('CompressedStreamTools\.readCompressed\s*\(([^)]+)\)', 'PlatformServiceHolder.get().readCompressedNBT($1)'),
    @('new\s+NBTTagCompound\s*\(\)', 'NBT.compound()'),
    @('new\s+NBTTagList\s*\(\)', 'NBT.list()'),
    @('MathHelper\.clamp_int\s*\(', 'ValueUtil.clampInt(')
)

$tc = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\controllers\TestController.java")
$td = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\controllers\data\TestData.java")

$ok = $true
$ok = (Assert-Contains $tc 'NBT.compound()' 'new NBTTagCompound -> NBT.compound()') -and $ok
$ok = (Assert-Contains $tc 'PlatformServiceHolder.get().writeCompressedNBT(compound, outputStream)' 'writeCompressed transformed') -and $ok
$ok = (Assert-Contains $tc 'PlatformServiceHolder.get().readCompressedNBT(inputStream)' 'readCompressed transformed') -and $ok
$ok = (Assert-Contains $tc 'ValueUtil.clampInt(value, 0, 100)' 'MathHelper.clamp_int transformed') -and $ok
$ok = (Assert-Contains $td 'NBT.list()' 'new NBTTagList -> NBT.list()') -and $ok

if ($ok) { $passed++; Write-Host "  PASS: Static-Transform" -ForegroundColor Green } else { $failed++ }

# ============================================================
Write-Host "`n========== TEST 4: Dedupe-Imports ==========" -ForegroundColor Cyan
Reset-Sandbox

$result = Dedupe-Imports -Directory $sandboxRel

$tu = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\util\TestUtil.java")

$ok = $true
# Count how many times 'import noppes.npcs.controllers.TestController;' appears
$dupeCount = ([regex]::Matches($tu, 'import noppes\.npcs\.controllers\.TestController;')).Count
if ($dupeCount -eq 1) {
    $ok = $true
} else {
    Write-Host "  FAIL: Expected 1 TestController import, got $dupeCount" -ForegroundColor Red
    $ok = $false
}
$nbtDupeCount = ([regex]::Matches($tu, 'import net\.minecraft\.nbt\.NBTTagCompound;')).Count
if ($nbtDupeCount -eq 1) {
    # ok
} else {
    Write-Host "  FAIL: Expected 1 NBTTagCompound import, got $nbtDupeCount" -ForegroundColor Red
    $ok = $false
}
$ok = (Assert-Contains $tu 'import java.util.Map;' 'TestUtil java import preserved') -and $ok

if ($ok) { $passed++; Write-Host "  PASS: Dedupe-Imports" -ForegroundColor Green } else { $failed++ }

# ============================================================
Write-Host "`n========== TEST 5: Generate-Stubs ==========" -ForegroundColor Cyan
Reset-Sandbox

# Clean up any previous stub
$stubDir = Join-Path $sandboxAbs 'noppes\npcs\controllers'
$stubFile = Join-Path $stubDir 'SyncController.java'
if (Test-Path $stubFile) { Remove-Item $stubFile -Force }

$result = Generate-Stubs -Stubs @(
    @{ pkg='noppes.npcs.controllers'; name='SyncController'; type='class';
       body='    public static void updateDialog(Object d) {}' }
) -TargetRoot 'tools/migration/test_sandbox/src'

$ok = $true
if (Test-Path $stubFile) {
    $stubContent = [IO.File]::ReadAllText($stubFile)
    $ok = (Assert-Contains $stubContent 'TEMPORARY STUB' 'Stub has marker comment') -and $ok
    $ok = (Assert-Contains $stubContent 'package noppes.npcs.controllers;' 'Stub correct package') -and $ok
    $ok = (Assert-Contains $stubContent 'public class SyncController' 'Stub correct class') -and $ok
    $ok = (Assert-Contains $stubContent 'public static void updateDialog(Object d) {}' 'Stub has method') -and $ok
} else {
    Write-Host "  FAIL: Stub file not created" -ForegroundColor Red
    $ok = $false
}

# Test that it does NOT overwrite a real class
$realFile = Join-Path $stubDir 'TestController.java'
$realBefore = [IO.File]::ReadAllText($realFile)
Generate-Stubs -Stubs @(
    @{ pkg='noppes.npcs.controllers'; name='TestController'; type='class'; body='// should not appear' }
) -TargetRoot 'tools/migration/test_sandbox/src' | Out-Null
$realAfter = [IO.File]::ReadAllText($realFile)
if ($realBefore -eq $realAfter) {
    # good - real class not overwritten
} else {
    Write-Host "  FAIL: Generate-Stubs overwrote a real class!" -ForegroundColor Red
    $ok = $false
}

# Clean up stub
if (Test-Path $stubFile) { Remove-Item $stubFile -Force }

if ($ok) { $passed++; Write-Host "  PASS: Generate-Stubs" -ForegroundColor Green } else { $failed++ }

# ============================================================
Write-Host "`n========== TEST 6: Create-And-Propagate ==========" -ForegroundColor Cyan
Reset-Sandbox

$interfacePath = 'tools/migration/test_sandbox/src/noppes/npcs/api/ITestService.java'
$interfaceContent = @'
package noppes.npcs.api;

public interface ITestService {
    void doSomething();
}
'@

$result = Create-And-Propagate `
    -FilePath $interfacePath `
    -Content $interfaceContent `
    -TargetDirectory $sandboxRel `
    -Swaps @(
        ,@('CompressedStreamTools', 'ITestService', 'net.minecraft.nbt.CompressedStreamTools', 'noppes.npcs.api.ITestService')
    )

$ok = $true
# Check interface was created
$ifaceFull = Join-Path $PSScriptRoot 'test_sandbox\src\noppes\npcs\api\ITestService.java'
if (Test-Path $ifaceFull) {
    $ifaceContent = [IO.File]::ReadAllText($ifaceFull)
    $ok = (Assert-Contains $ifaceContent 'public interface ITestService' 'Interface created') -and $ok
} else {
    Write-Host "  FAIL: Interface file not created" -ForegroundColor Red
    $ok = $false
}

# Check propagation in TestController (had CompressedStreamTools references)
$tc = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\controllers\TestController.java")
$ok = (Assert-Contains $tc 'ITestService' 'CompressedStreamTools replaced with ITestService') -and $ok
$ok = (Assert-NotContains $tc 'CompressedStreamTools' 'No leftover CompressedStreamTools') -and $ok
$ok = (Assert-Contains $tc 'import noppes.npcs.api.ITestService;' 'New import added') -and $ok

# Clean up
if (Test-Path $ifaceFull) { Remove-Item $ifaceFull -Force }
$apiDir = Join-Path $PSScriptRoot 'test_sandbox\src\noppes\npcs\api'
if (Test-Path $apiDir) { Remove-Item $apiDir -Recurse -Force }

if ($ok) { $passed++; Write-Host "  PASS: Create-And-Propagate" -ForegroundColor Green } else { $failed++ }

# ============================================================
Write-Host "`n========== TEST 7: Full Pipeline (Symbol-Swap + Nuke + Static + Dedupe) ==========" -ForegroundColor Cyan
Reset-Sandbox

# Step 1: Nuke MC imports
Nuke-Imports -Directory $sandboxRel -ForbiddenPrefixes @('net.minecraft.', 'cpw.mods.', 'net.minecraftforge.') | Out-Null

# Step 2: Symbol swap whatever is left
Symbol-Swap -Directory $sandboxRel -Swaps @(
    @('NBTTagCompound', 'INbt', 'net.minecraft.nbt.NBTTagCompound', 'noppes.npcs.api.nbt.INbt'),
    @('NBTTagList', 'INbtList', 'net.minecraft.nbt.NBTTagList', 'noppes.npcs.api.nbt.INbtList'),
    @('EntityPlayer', 'IPlayer', 'net.minecraft.entity.player.EntityPlayer', 'noppes.npcs.api.entity.IPlayer')
) | Out-Null

# Step 3: Static transforms
Static-Transform -Directory $sandboxRel -Transforms @(
    @('new\s+INbt\s*\(\)', 'NBT.compound()'),
    @('new\s+INbtList\s*\(\)', 'NBT.list()'),
    @('MathHelper\.clamp_int\s*\(', 'ValueUtil.clampInt(')
) | Out-Null

# Step 4: Dedupe
Dedupe-Imports -Directory $sandboxRel | Out-Null

$tc = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\controllers\TestController.java")
$tu = [IO.File]::ReadAllText("$sandboxAbs\noppes\npcs\util\TestUtil.java")

$ok = $true
$ok = (Assert-NotContains $tc 'net.minecraft' 'Pipeline: no MC imports left') -and $ok
$ok = (Assert-NotContains $tc 'cpw.mods' 'Pipeline: no FML imports left') -and $ok
$ok = (Assert-Contains $tc 'INbt' 'Pipeline: symbols swapped') -and $ok
$ok = (Assert-Contains $tc 'NBT.compound()' 'Pipeline: static transforms applied') -and $ok
$ok = (Assert-Contains $tc 'ValueUtil.clampInt(' 'Pipeline: MathHelper transformed') -and $ok
# Check deduplication worked on TestUtil
$tuDupeCount = ([regex]::Matches($tu, 'import noppes\.npcs\.controllers\.TestController;')).Count
if ($tuDupeCount -eq 1) {
    # good
} else {
    Write-Host "  FAIL: Pipeline dedup - expected 1 TestController import, got $tuDupeCount" -ForegroundColor Red
    $ok = $false
}

if ($ok) { $passed++; Write-Host "  PASS: Full Pipeline" -ForegroundColor Green } else { $failed++ }

# ============================================================
# Clean up sandbox
Reset-Sandbox

# ===== SUMMARY =====
Write-Host "`n============================================" -ForegroundColor White
Write-Host "  TEST RESULTS" -ForegroundColor White
Write-Host "============================================" -ForegroundColor White
Write-Host "  Passed: $passed" -ForegroundColor Green
Write-Host "  Failed: $failed" -ForegroundColor $(if ($failed -gt 0) { 'Red' } else { 'Green' })
Write-Host "============================================" -ForegroundColor White

if ($failed -gt 0) { exit 1 }
