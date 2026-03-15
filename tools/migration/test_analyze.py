#!/usr/bin/env python3
"""
test_analyze.py — Comprehensive test suite for analyze.py subcommands.
Tests all 6 subcommands with mock index data and real tree-sitter parsing.

Run:
  python tools/migration/test_analyze.py
"""

import sys
import os
import json
import tempfile
import shutil
from pathlib import Path

SCRIPT_DIR = Path(__file__).resolve().parent
PROJECT_ROOT = SCRIPT_DIR.parent.parent
ANALYZE_PY = SCRIPT_DIR / "analyze.py"

sys.path.insert(0, str(SCRIPT_DIR))
import analyze


MOCK_INDEX = {
    "meta": {
        "scanDir": "src/main/java",
        "projectRoot": str(PROJECT_ROOT),
        "generatedBy": "test_analyze.py",
        "timestamp": "2025-01-01T00:00:00",
    },
    "summary": {
        "totalFiles": 500,
        "filesWithMcDeps": 300,
        "totalMcImports": 1200,
        "uniqueMcTypes": 45,
        "parseErrors": 0,
        "abstractionCoverage": {
            "abstracted": 5,
            "notAbstracted": 40,
            "total": 45,
            "percentage": "11.1%",
        },
        "topTypes": [],
        "topFiles": [],
        "byPackage": {},
    },
    "types": {
        "net.minecraft.entity.player.EntityPlayer": {
            "shortName": "EntityPlayer",
            "package": "net.minecraft.entity.player",
            "importCount": 120,
            "importedBy": [
                "noppes/npcs/controllers/QuestController.java",
                "noppes/npcs/controllers/DialogController.java",
            ],
            "paAbstraction": "IPlayer",
            "usage": {
                "extends": [
                    {
                        "file": "noppes/npcs/entity/FakePlayer.java",
                        "class": "FakePlayer",
                    }
                ],
                "implements": [],
                "constructors": [],
                "casts": [
                    {"file": "noppes/npcs/controllers/QuestController.java", "line": 50}
                ],
                "instanceof": [
                    {"file": "noppes/npcs/controllers/QuestController.java", "line": 42}
                ],
            },
            "methodCalls": {
                "getCommandSenderName": {
                    "count": 15,
                    "files": [
                        "noppes/npcs/controllers/QuestController.java",
                        "noppes/npcs/controllers/DialogController.java",
                    ],
                },
                "getEntityId": {
                    "count": 8,
                    "files": ["noppes/npcs/controllers/QuestController.java"],
                },
            },
            "staticCalls": {},
            "fieldAccesses": {
                "worldObj": {
                    "count": 22,
                    "files": [
                        "noppes/npcs/controllers/QuestController.java",
                        "noppes/npcs/controllers/DialogController.java",
                        "noppes/npcs/roles/RoleTrader.java",
                    ],
                }
            },
        },
        "net.minecraft.nbt.NBTTagCompound": {
            "shortName": "NBTTagCompound",
            "package": "net.minecraft.nbt",
            "importCount": 250,
            "importedBy": [
                "noppes/npcs/controllers/QuestController.java",
                "noppes/npcs/controllers/data/Quest.java",
                "core/src/main/java/noppes/npcs/controllers/FactionController.java",
            ],
            "paAbstraction": "INbt",
            "usage": {
                "extends": [],
                "implements": [],
                "constructors": [
                    {"file": "noppes/npcs/controllers/data/Quest.java", "line": 30}
                ],
                "casts": [],
                "instanceof": [],
            },
            "methodCalls": {
                "setString": {
                    "count": 80,
                    "files": [
                        "noppes/npcs/controllers/data/Quest.java",
                        "noppes/npcs/controllers/QuestController.java",
                        "core/src/main/java/noppes/npcs/controllers/FactionController.java",
                    ],
                },
                "getString": {
                    "count": 60,
                    "files": [
                        "noppes/npcs/controllers/data/Quest.java",
                        "core/src/main/java/noppes/npcs/controllers/FactionController.java",
                    ],
                },
                "setInteger": {
                    "count": 45,
                    "files": ["noppes/npcs/controllers/data/Quest.java"],
                },
            },
            "staticCalls": {},
            "fieldAccesses": {},
        },
        "net.minecraft.server.MinecraftServer": {
            "shortName": "MinecraftServer",
            "package": "net.minecraft.server",
            "importCount": 30,
            "importedBy": ["noppes/npcs/CustomNpcs.java"],
            "paAbstraction": None,
            "usage": {
                "extends": [],
                "implements": [],
                "constructors": [],
                "casts": [],
                "instanceof": [],
            },
            "methodCalls": {},
            "staticCalls": {
                "getServer": {
                    "count": 10,
                    "files": ["noppes/npcs/CustomNpcs.java"],
                }
            },
            "fieldAccesses": {},
        },
    },
    "files": {
        "noppes/npcs/controllers/QuestController.java": {
            "mcImportCount": 5,
            "mcTypes": [
                "EntityPlayer",
                "NBTTagCompound",
                "NBTTagList",
                "ItemStack",
                "MinecraftServer",
            ],
            "mcFqns": [
                "net.minecraft.entity.player.EntityPlayer",
                "net.minecraft.nbt.NBTTagCompound",
                "net.minecraft.nbt.NBTTagList",
                "net.minecraft.item.ItemStack",
                "net.minecraft.server.MinecraftServer",
            ],
        },
        "noppes/npcs/controllers/DialogController.java": {
            "mcImportCount": 2,
            "mcTypes": ["EntityPlayer", "NBTTagCompound"],
            "mcFqns": [
                "net.minecraft.entity.player.EntityPlayer",
                "net.minecraft.nbt.NBTTagCompound",
            ],
        },
        "noppes/npcs/CustomNpcs.java": {
            "mcImportCount": 3,
            "mcTypes": ["MinecraftServer", "NBTTagCompound", "FMLServerStartingEvent"],
            "mcFqns": [
                "net.minecraft.server.MinecraftServer",
                "net.minecraft.nbt.NBTTagCompound",
                "cpw.mods.fml.common.event.FMLServerStartingEvent",
            ],
        },
    },
    "hierarchy": {
        "extends": {
            "net.minecraft.entity.player.EntityPlayer": [
                {"file": "noppes/npcs/entity/FakePlayer.java", "class": "FakePlayer"}
            ]
        },
        "implements": {},
    },
}


def _write_mock_index(tmpdir):
    """Write mock index to a temp directory matching the expected path."""
    idx_dir = Path(tmpdir) / "tools" / "migration"
    idx_dir.mkdir(parents=True, exist_ok=True)
    idx_path = idx_dir / "mc_usage_index.json"
    with open(idx_path, "w", encoding="utf-8") as f:
        json.dump(MOCK_INDEX, f, indent=2)
    return str(tmpdir)


def _make_namespace(overrides):
    """Build an argparse.Namespace with common defaults."""
    defaults = {
        "project_root": None,
        "index": "tools/migration/mc_usage_index.json",
        "json_output": False,
        "top": 0,
        "methods_only": False,
        "field_accesses_only": False,
        "show_files": False,
    }
    defaults.update(overrides)
    return argparse.Namespace(**defaults)


import argparse
import io
from contextlib import redirect_stdout, redirect_stderr


def _capture_cmd(func, args):
    """Run a command function, capturing stdout/stderr. Returns (returncode, stdout, stderr)."""
    out_buf = io.StringIO()
    err_buf = io.StringIO()
    with redirect_stdout(out_buf), redirect_stderr(err_buf):
        try:
            rc = func(args)
        except SystemExit as e:
            rc = e.code if e.code is not None else 0
        except Exception as e:
            err_buf.write(str(e))
            rc = 1
    return rc, out_buf.getvalue(), err_buf.getvalue()


# ============================================================
# Tests
# ============================================================

passed = 0
failed = 0


def check(condition, desc):
    global passed, failed
    if condition:
        passed += 1
        print(f"  PASS {desc}")
    else:
        failed += 1
        print(f"  FAIL {desc}")
    return condition


def test_query_type_basic():
    """query-type: finds EntityPlayer, shows PA abstraction and method calls."""
    print("TEST: query-type basic")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "EntityPlayer",
                "fqn": None,
                "project_root": mock_root,
            }
        )
        rc, stdout, stderr = _capture_cmd(analyze.cmd_query_type, args)
        check(rc == 0, "Return code 0")
        check("EntityPlayer" in stdout, "Type name in output")
        check("IPlayer" in stdout, "PA abstraction shown")
        check("METHOD CALLS" in stdout, "Method calls section")
        check("getCommandSenderName" in stdout, "Method name present")
        check("FIELD ACCESSES" in stdout, "Field accesses section")
        check("worldObj" in stdout, "Field name present")
        check("EXTENDED BY" in stdout, "Hierarchy section")
        check("Constructors:" in stdout, "Usage summary")
    finally:
        shutil.rmtree(tmpdir)


def test_query_type_by_fqn():
    """query-type: find by explicit FQN."""
    print("TEST: query-type by FQN")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "EntityPlayer",
                "fqn": "net.minecraft.entity.player.EntityPlayer",
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_query_type, args)
        check(rc == 0, "Return code 0")
        check("net.minecraft.entity.player.EntityPlayer" in stdout, "FQN in output")
    finally:
        shutil.rmtree(tmpdir)


def test_query_type_not_found():
    """query-type: missing type returns error."""
    print("TEST: query-type not found")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "NonexistentType",
                "fqn": None,
                "project_root": mock_root,
            }
        )
        rc, stdout, stderr = _capture_cmd(analyze.cmd_query_type, args)
        check(rc == 1, "Return code 1 for missing type")
        check("not found" in stderr.lower(), "Error message mentions not found")
    finally:
        shutil.rmtree(tmpdir)


def test_query_type_json():
    """query-type: JSON output mode."""
    print("TEST: query-type JSON output")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "NBTTagCompound",
                "fqn": None,
                "project_root": mock_root,
                "json_output": True,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_query_type, args)
        check(rc == 0, "Return code 0")
        data = json.loads(stdout)
        check(data["fqn"] == "net.minecraft.nbt.NBTTagCompound", "Correct FQN in JSON")
        check(data["paAbstraction"] == "INbt", "PA abstraction in JSON")
    finally:
        shutil.rmtree(tmpdir)


def test_query_file_basic():
    """query-file: finds QuestController, shows MC types."""
    print("TEST: query-file basic")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "file": "noppes/npcs/controllers/QuestController.java",
                "readiness": False,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_query_file, args)
        check(rc == 0, "Return code 0")
        check("QuestController" in stdout, "File name in output")
        check("MC Import Count: 5" in stdout, "Import count shown")
        check("MC TYPES USED" in stdout, "Types section present")
    finally:
        shutil.rmtree(tmpdir)


def test_query_file_readiness():
    """query-file: readiness score calculation."""
    print("TEST: query-file readiness")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "file": "noppes/npcs/controllers/QuestController.java",
                "readiness": True,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_query_file, args)
        check(rc == 0, "Return code 0")
        check("MIGRATION READINESS" in stdout, "Readiness header present")
        check("Blocking" in stdout, "Blocking types shown")
    finally:
        shutil.rmtree(tmpdir)


def test_query_file_not_found():
    """query-file: missing file returns error."""
    print("TEST: query-file not found")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "file": "nonexistent/File.java",
                "readiness": False,
                "project_root": mock_root,
            }
        )
        rc, stdout, stderr = _capture_cmd(analyze.cmd_query_file, args)
        check(rc == 1, "Return code 1 for missing file")
        check("not found" in stderr.lower(), "Error message about missing file")
    finally:
        shutil.rmtree(tmpdir)


def test_query_file_json():
    """query-file: JSON output with readiness."""
    print("TEST: query-file JSON output")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "file": "noppes/npcs/controllers/DialogController.java",
                "readiness": True,
                "project_root": mock_root,
                "json_output": True,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_query_file, args)
        check(rc == 0, "Return code 0")
        data = json.loads(stdout)
        check(data["mcImportCount"] == 2, "Import count correct")
        check("readiness" in data, "Readiness in JSON")
        check(data["readiness"]["score"] == 100.0, "All types abstracted = 100%")
    finally:
        shutil.rmtree(tmpdir)


def test_extract_signatures_real_file():
    """extract-signatures: parse real PA interface file with tree-sitter."""
    print("TEST: extract-signatures real file")
    parser, lang = analyze.setup_treesitter_parser()
    if parser is None:
        print("  SKIP tree-sitter not available")
        return

    inbt_path = (
        PROJECT_ROOT
        / "platform-api"
        / "src"
        / "main"
        / "java"
        / "noppes"
        / "npcs"
        / "api"
        / "INbt.java"
    )
    if not inbt_path.exists():
        print(f"  SKIP {inbt_path} not found")
        return

    args = _make_namespace(
        {
            "file": str(inbt_path),
            "public_only": False,
            "project_root": str(PROJECT_ROOT),
        }
    )
    rc, stdout, stderr = _capture_cmd(analyze.cmd_extract_signatures, args)
    check(rc == 0, f"Return code 0 (stderr: {stderr[:100]})")
    check("SIGNATURES:" in stdout, "Header present")
    check("Total methods:" in stdout, "Method count shown")
    check("setString" in stdout, "Known method found")
    check("getString" in stdout, "Known getter found")
    check("hasKey" in stdout, "Query method found")


def test_extract_signatures_temp_file():
    """extract-signatures: parse a temp Java file."""
    print("TEST: extract-signatures temp file")
    parser, lang = analyze.setup_treesitter_parser()
    if parser is None:
        print("  SKIP tree-sitter not available")
        return

    with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False) as f:
        f.write("""
package test;

public class TestSig {
    /** Constructor doc */
    public TestSig(int x) {}

    public String getName() { return "test"; }

    private void helper(int a, String b) {}

    public static void doStuff(Object... args) {}

    @Override
    public String toString() { return ""; }
}
""")
        tmp = f.name

    try:
        args = _make_namespace(
            {
                "file": tmp,
                "public_only": False,
                "project_root": str(PROJECT_ROOT),
            }
        )
        rc, stdout, stderr = _capture_cmd(analyze.cmd_extract_signatures, args)
        check(rc == 0, f"Return code 0 (stderr: {stderr[:100]})")
        check("getName" in stdout, "getName method found")
        check("helper" in stdout, "private helper found")
        check("doStuff" in stdout, "static varargs method found")
        check("[constructor]" in stdout, "Constructor detected")
    finally:
        os.unlink(tmp)


def test_extract_signatures_public_only():
    """extract-signatures: --public-only filters private methods."""
    print("TEST: extract-signatures public-only")
    parser, lang = analyze.setup_treesitter_parser()
    if parser is None:
        print("  SKIP tree-sitter not available")
        return

    with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False) as f:
        f.write("""
public class Filtered {
    public void pubMethod() {}
    private void privMethod() {}
    protected void protMethod() {}
    void packageMethod() {}
}
""")
        tmp = f.name

    try:
        args = _make_namespace(
            {
                "file": tmp,
                "public_only": True,
                "project_root": str(PROJECT_ROOT),
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_extract_signatures, args)
        check(rc == 0, "Return code 0")
        check("pubMethod" in stdout, "Public method included")
        check("privMethod" not in stdout, "Private method excluded")
        check("protMethod" not in stdout, "Protected method excluded")
    finally:
        os.unlink(tmp)


def test_extract_signatures_json():
    """extract-signatures: JSON output includes all fields."""
    print("TEST: extract-signatures JSON output")
    parser, lang = analyze.setup_treesitter_parser()
    if parser is None:
        print("  SKIP tree-sitter not available")
        return

    with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False) as f:
        f.write("""
public interface IFoo {
    String doThing(int x, String y);
    void noArgs();
}
""")
        tmp = f.name

    try:
        args = _make_namespace(
            {
                "file": tmp,
                "public_only": False,
                "json_output": True,
                "project_root": str(PROJECT_ROOT),
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_extract_signatures, args)
        check(rc == 0, "Return code 0")
        data = json.loads(stdout)
        check(data["total_methods"] == 2, "Two methods found")
        sig0 = data["signatures"][0]
        check(sig0["name"] == "doThing", "First method name")
        check(sig0["return_type"] == "String", "Return type captured")
        check(len(sig0["params"]) == 2, "Two parameters captured")
        check(sig0["params"][0]["type"] == "int", "First param type")
        check(sig0["params"][1]["name"] == "y", "Second param name")
    finally:
        os.unlink(tmp)


def test_surface_miner_basic():
    """surface-miner: find methods/fields for NBTTagCompound."""
    print("TEST: surface-miner basic")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "NBTTagCompound",
                "fqn": None,
                "scope": None,
                "compare_existing": None,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_surface_miner, args)
        check(rc == 0, "Return code 0")
        check("MINIMAL SURFACE" in stdout, "Header present")
        check("METHODS" in stdout, "Methods section")
        check("setString" in stdout, "setString method found")
        check("getString" in stdout, "getString method found")
    finally:
        shutil.rmtree(tmpdir)


def test_surface_miner_scoped():
    """surface-miner: --scope filters to files under path."""
    print("TEST: surface-miner scoped")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "NBTTagCompound",
                "fqn": None,
                "scope": "core/src/main/java",
                "compare_existing": None,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_surface_miner, args)
        check(rc == 0, "Return code 0")
        check("scoped to core/src/main/java" in stdout, "Scope shown in output")
        check("setString" in stdout, "setString in core scope")
        check("getString" in stdout, "getString in core scope")
        check("setInteger" not in stdout, "setInteger not in core scope")
    finally:
        shutil.rmtree(tmpdir)


def test_surface_miner_not_found():
    """surface-miner: missing type returns error."""
    print("TEST: surface-miner not found")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "FakeType",
                "fqn": None,
                "scope": None,
                "compare_existing": None,
                "project_root": mock_root,
            }
        )
        rc, _, stderr = _capture_cmd(analyze.cmd_surface_miner, args)
        check(rc == 1, "Return code 1 for missing type")
        check("not found" in stderr.lower(), "Error message")
    finally:
        shutil.rmtree(tmpdir)


def test_diff_signatures_basic():
    """diff-signatures: detect added/removed methods between two temp files."""
    print("TEST: diff-signatures basic")
    parser, lang = analyze.setup_treesitter_parser()
    if parser is None:
        print("  SKIP tree-sitter not available")
        return

    with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False) as f1:
        f1.write("""
public class OldClass {
    public void keepMethod() {}
    public void removeMe() {}
    public int changeable() { return 0; }
}
""")
        old_path = f1.name

    with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False) as f2:
        f2.write("""
public class NewClass {
    public void keepMethod() {}
    public void addedMethod() {}
    public String changeable() { return ""; }
}
""")
        new_path = f2.name

    try:
        args = _make_namespace(
            {
                "old": old_path,
                "new": new_path,
                "project_root": str(PROJECT_ROOT),
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_diff_signatures, args)
        check(rc == 0, "Return code 0")
        check("SIGNATURE DIFF" in stdout, "Header present")
        check("ADDED" in stdout, "Added section")
        check("addedMethod" in stdout, "Added method detected")
        check("REMOVED" in stdout, "Removed section")
        check("removeMe" in stdout, "Removed method detected")
        check("CHANGED" in stdout, "Changed section")
        check("changeable" in stdout, "Changed method detected")
    finally:
        os.unlink(old_path)
        os.unlink(new_path)


def test_diff_signatures_identical():
    """diff-signatures: identical files produce no diffs."""
    print("TEST: diff-signatures identical")
    parser, lang = analyze.setup_treesitter_parser()
    if parser is None:
        print("  SKIP tree-sitter not available")
        return

    with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False) as f:
        f.write("""
public class Same {
    public void alpha() {}
    public void beta(int x) {}
}
""")
        path = f.name

    try:
        args = _make_namespace(
            {
                "old": path,
                "new": path,
                "project_root": str(PROJECT_ROOT),
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_diff_signatures, args)
        check(rc == 0, "Return code 0")
        check("No differences found" in stdout, "No diffs for identical files")
        check("=2 unchanged" in stdout, "2 unchanged methods")
    finally:
        os.unlink(path)


def test_diff_signatures_json():
    """diff-signatures: JSON output has added/removed/changed."""
    print("TEST: diff-signatures JSON output")
    parser, lang = analyze.setup_treesitter_parser()
    if parser is None:
        print("  SKIP tree-sitter not available")
        return

    with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False) as f1:
        f1.write("public class A { public void m1() {} }")
        p1 = f1.name
    with tempfile.NamedTemporaryFile(mode="w", suffix=".java", delete=False) as f2:
        f2.write("public class A { public void m1() {} public void m2() {} }")
        p2 = f2.name

    try:
        args = _make_namespace(
            {
                "old": p1,
                "new": p2,
                "json_output": True,
                "project_root": str(PROJECT_ROOT),
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_diff_signatures, args)
        check(rc == 0, "Return code 0")
        data = json.loads(stdout)
        check(len(data["added"]) == 1, "One method added")
        check(data["added"][0]["name"] == "m2", "Added method is m2")
        check(len(data["removed"]) == 0, "No methods removed")
        check(data["unchanged"] == 1, "One unchanged")
    finally:
        os.unlink(p1)
        os.unlink(p2)


def test_type_usage_basic():
    """type-usage: basic invocation shows all sections."""
    print("TEST: type-usage basic")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "EntityPlayer",
                "fqn": None,
                "scope": None,
                "top": 0,
                "project_root": mock_root,
            }
        )
        rc, stdout, stderr = _capture_cmd(analyze.cmd_type_usage, args)
        check(rc == 0, f"Return code 0 (stderr: {stderr[:100]})")
        check("TYPE USAGE:" in stdout, "Header present")
        check("EntityPlayer" in stdout, "Type name in output")
        check("IPlayer" in stdout, "PA abstraction shown")
        check("METHOD CALLS" in stdout, "Method calls section")
        check("getCommandSenderName" in stdout, "Method name present")
        check("FIELD ACCESSES" in stdout, "Field accesses section")
        check("worldObj" in stdout, "Field name present")
        check("CONSTRUCTORS:" in stdout, "Constructors section")
        check("CASTS:" in stdout, "Casts section")
        check("INSTANCEOF:" in stdout, "Instanceof section")
    finally:
        shutil.rmtree(tmpdir)


def test_type_usage_scoped():
    """type-usage: --scope filters to files under path."""
    print("TEST: type-usage scoped")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "EntityPlayer",
                "fqn": None,
                "scope": "noppes/npcs/controllers/",
                "top": 0,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_type_usage, args)
        check(rc == 0, "Return code 0")
        check("noppes/npcs/controllers/" in stdout, "Scope shown in output")
        check("getCommandSenderName" in stdout, "Method in scope present")
        check("QuestController" in stdout, "Scoped file present")
        # RoleTrader is outside controllers/ scope for method calls
        # but worldObj has it — check that scoped files appear
        check("worldObj" in stdout, "Field in scope present")
    finally:
        shutil.rmtree(tmpdir)


def test_type_usage_json():
    """type-usage: JSON output has all expected keys."""
    print("TEST: type-usage JSON output")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "EntityPlayer",
                "fqn": None,
                "scope": None,
                "top": 0,
                "json_output": True,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_type_usage, args)
        check(rc == 0, "Return code 0")
        data = json.loads(stdout)
        check(data["type"] == "net.minecraft.entity.player.EntityPlayer", "Correct FQN")
        check(data["shortName"] == "EntityPlayer", "Short name present")
        check(data["paAbstraction"] == "IPlayer", "PA abstraction present")
        check("methodCalls" in data, "methodCalls key present")
        check("staticCalls" in data, "staticCalls key present")
        check("fieldAccesses" in data, "fieldAccesses key present")
        check("constructors" in data, "constructors key present")
        check("casts" in data, "casts key present")
        check("instanceof" in data, "instanceof key present")
        check("count" in data["casts"], "casts has count")
        check("files" in data["casts"], "casts has files")
        check(data["totalFilesInScope"] > 0, "totalFilesInScope > 0")
        check(data["totalFilesGlobal"] > 0, "totalFilesGlobal > 0")
    finally:
        shutil.rmtree(tmpdir)


def test_type_usage_top_limit():
    """type-usage: --top limits methods/fields shown."""
    print("TEST: type-usage --top limit")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "NBTTagCompound",
                "fqn": None,
                "scope": None,
                "top": 1,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_type_usage, args)
        check(rc == 0, "Return code 0")
        # NBTTagCompound has 3 methods (setString, getString, setInteger)
        # With --top 1, only the highest should show
        check("setString" in stdout, "Top method (setString) present")
        # setInteger has lowest count (1 file), should be excluded
        check("setInteger" not in stdout, "Lower method excluded by --top 1")
    finally:
        shutil.rmtree(tmpdir)


def test_type_usage_not_found():
    """type-usage: missing type returns error."""
    print("TEST: type-usage not found")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "type": "NonexistentType",
                "fqn": None,
                "scope": None,
                "top": 0,
                "project_root": mock_root,
            }
        )
        rc, _, stderr = _capture_cmd(analyze.cmd_type_usage, args)
        check(rc == 1, "Return code 1 for missing type")
        check("not found" in stderr.lower(), "Error message mentions not found")
    finally:
        shutil.rmtree(tmpdir)


def test_coverage_report_basic():
    """coverage-report: basic stats from mock index."""
    print("TEST: coverage-report basic")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "detailed": False,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_coverage_report, args)
        check(rc == 0, "Return code 0")
        check("MIGRATION COVERAGE REPORT" in stdout, "Header present")
        check("OVERALL STATISTICS:" in stdout, "Stats section")
        check("Total files scanned" in stdout, "File count label")
        check("ABSTRACTION COVERAGE:" in stdout, "Coverage section")
        check("TOP BLOCKING TYPES" in stdout, "Blocking types section")
        check("MinecraftServer" in stdout, "Blocking type shown")
    finally:
        shutil.rmtree(tmpdir)


def test_coverage_report_detailed():
    """coverage-report: detailed mode includes package and file breakdown."""
    print("TEST: coverage-report detailed")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "detailed": True,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_coverage_report, args)
        check(rc == 0, "Return code 0")
        check("PACKAGE BREAKDOWN" in stdout, "Package breakdown in detailed mode")
        check("TOP FILES BY MC DEPENDENCY" in stdout, "File breakdown in detailed mode")
    finally:
        shutil.rmtree(tmpdir)


def test_coverage_report_json():
    """coverage-report: JSON output."""
    print("TEST: coverage-report JSON output")
    tmpdir = tempfile.mkdtemp()
    try:
        mock_root = _write_mock_index(tmpdir)
        args = _make_namespace(
            {
                "detailed": False,
                "json_output": True,
                "project_root": mock_root,
            }
        )
        rc, stdout, _ = _capture_cmd(analyze.cmd_coverage_report, args)
        check(rc == 0, "Return code 0")
        data = json.loads(stdout)
        check("abstractionCoverage" in data, "Coverage in JSON")
        check(data["abstractionCoverage"]["abstracted"] == 2, "2 abstracted types")
        check(len(data["topBlockingTypes"]) == 1, "1 blocking type")
        check(
            data["topBlockingTypes"][0]["shortName"] == "MinecraftServer",
            "MinecraftServer is blocking",
        )
    finally:
        shutil.rmtree(tmpdir)


def test_missing_index():
    """All index-dependent commands fail gracefully when index is missing."""
    print("TEST: missing index")
    tmpdir = tempfile.mkdtemp()
    try:
        args = _make_namespace(
            {
                "type": "EntityPlayer",
                "fqn": None,
                "project_root": tmpdir,
            }
        )
        rc, _, stderr = _capture_cmd(analyze.cmd_query_type, args)
        check(rc == 1, "Return code 1 when index missing")
        check(
            "not found" in stderr.lower() or "index" in stderr.lower(),
            "Error mentions missing index",
        )
    finally:
        shutil.rmtree(tmpdir)


def test_utility_functions():
    """Test utility functions directly."""
    print("TEST: utility functions")

    root = analyze.auto_detect_project_root()
    check(root.exists(), "auto_detect_project_root returns existing path")

    check(
        analyze._find_type_key(MOCK_INDEX, "EntityPlayer")
        == "net.minecraft.entity.player.EntityPlayer",
        "_find_type_key by short name",
    )

    check(
        analyze._find_type_key(MOCK_INDEX, "X", fqn="net.minecraft.nbt.NBTTagCompound")
        == "net.minecraft.nbt.NBTTagCompound",
        "_find_type_key by FQN",
    )

    check(
        analyze._find_type_key(MOCK_INDEX, "Nonexistent") is None,
        "_find_type_key returns None for missing",
    )

    check(
        analyze._find_file_key(
            MOCK_INDEX, "noppes/npcs/controllers/QuestController.java"
        )
        == "noppes/npcs/controllers/QuestController.java",
        "_find_file_key exact match",
    )

    check(
        analyze._find_file_key(MOCK_INDEX, "controllers/QuestController.java")
        == "noppes/npcs/controllers/QuestController.java",
        "_find_file_key suffix match",
    )

    readiness = analyze._calc_readiness(
        MOCK_INDEX["files"]["noppes/npcs/CustomNpcs.java"]
    )
    check(0 < readiness["score"] < 100, "Partial readiness for CustomNpcs")
    check(len(readiness["blocking"]) > 0, "Has blocking types")


def test_signature_key():
    """Test signature_key uniqueness."""
    print("TEST: signature_key")
    sig1 = {"name": "foo", "params": [{"type": "int", "name": "x"}]}
    sig2 = {"name": "foo", "params": [{"type": "int", "name": "y"}]}
    sig3 = {"name": "foo", "params": [{"type": "String", "name": "x"}]}

    check(
        analyze.signature_key(sig1) == analyze.signature_key(sig2),
        "Same name+types, different param names = same key",
    )
    check(
        analyze.signature_key(sig1) != analyze.signature_key(sig3),
        "Same name, different types = different key",
    )


def main():
    global passed, failed
    print("=" * 80)
    print("ANALYZE.PY COMPREHENSIVE TEST SUITE")
    print("=" * 80)
    print()

    tests = [
        test_utility_functions,
        test_signature_key,
        test_query_type_basic,
        test_query_type_by_fqn,
        test_query_type_not_found,
        test_query_type_json,
        test_query_file_basic,
        test_query_file_readiness,
        test_query_file_not_found,
        test_query_file_json,
        test_extract_signatures_real_file,
        test_extract_signatures_temp_file,
        test_extract_signatures_public_only,
        test_extract_signatures_json,
        test_surface_miner_basic,
        test_surface_miner_scoped,
        test_surface_miner_not_found,
        test_type_usage_basic,
        test_type_usage_scoped,
        test_type_usage_json,
        test_type_usage_top_limit,
        test_type_usage_not_found,
        test_diff_signatures_basic,
        test_diff_signatures_identical,
        test_diff_signatures_json,
        test_coverage_report_basic,
        test_coverage_report_detailed,
        test_coverage_report_json,
        test_missing_index,
    ]

    for test_fn in tests:
        try:
            test_fn()
        except Exception as e:
            failed += 1
            print(f"  FAIL EXCEPTION: {e}")
            import traceback

            traceback.print_exc()
        print()

    print("=" * 80)
    print("SUMMARY")
    print("=" * 80)
    total = passed + failed
    print(f"  {passed}/{total} checks passed, {failed} failed")
    print()

    return 0 if failed == 0 else 1


if __name__ == "__main__":
    sys.exit(main())
