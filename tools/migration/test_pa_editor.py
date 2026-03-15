#!/usr/bin/env python3
"""Test suite for pa_editor.py — validates all operation types and error handling.

Run: python tools/migration/test_pa_editor.py
"""

import json
import os
import shutil
import subprocess
import sys
import tempfile

SCRIPT_DIR = os.path.dirname(os.path.abspath(__file__))
PA_EDITOR = os.path.join(SCRIPT_DIR, "pa_editor.py")
PROJECT_ROOT = os.path.abspath(os.path.join(SCRIPT_DIR, "..", ".."))

PASS = 0
FAIL = 0


def test(name: str, passed: bool, detail: str = ""):
    global PASS, FAIL
    if passed:
        PASS += 1
        print(f"  PASS  {name}")
    else:
        FAIL += 1
        print(f"  FAIL  {name}" + (f" — {detail}" if detail else ""))


def run_editor(manifest_path: str, *args: str) -> tuple[int, str, str]:
    cmd = [
        sys.executable,
        PA_EDITOR,
        manifest_path,
        *args,
        "--project-root",
        PROJECT_ROOT,
    ]
    result = subprocess.run(cmd, capture_output=True, text=True, cwd=PROJECT_ROOT)
    return result.returncode, result.stdout, result.stderr


def write_file(path: str, content: str):
    os.makedirs(os.path.dirname(path), exist_ok=True)
    with open(path, "w", encoding="utf-8", newline="\n") as f:
        f.write(content)


def read_file(path: str) -> str:
    with open(path, "r", encoding="utf-8") as f:
        return f.read()


def write_manifest(tmpdir: str, manifest: dict) -> str:
    path = os.path.join(tmpdir, "manifest.json")
    with open(path, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2)
    return path


SAMPLE_JAVA = """\
package test;

import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.entity.player.EntityPlayer;
import java.util.List;

public class Sample {
    private int id;

    public void load(NBTTagCompound nbt) {
        id = nbt.getInteger("id");
    }

    public void interact(EntityPlayer player) {
        player.addChatMessage(null);
    }
}
"""


def run_tests():
    tmpdir = tempfile.mkdtemp(
        prefix="pa_editor_test_", dir=os.path.join(PROJECT_ROOT, "tools", "migration")
    )
    try:
        _run_all(tmpdir)
    finally:
        shutil.rmtree(tmpdir, ignore_errors=True)

    print(f"\n{'=' * 50}")
    print(f"  RESULTS: {PASS} passed, {FAIL} failed")
    print(f"{'=' * 50}")
    return FAIL == 0


def _run_all(tmpdir: str):
    print("\n=== PA Editor Test Suite ===\n")

    # ----------------------------------------------------------------
    # TEST 1: Replace by pattern
    # ----------------------------------------------------------------
    f1 = os.path.join(tmpdir, "Replace.java")
    write_file(f1, SAMPLE_JAVA)
    rel1 = os.path.relpath(f1, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel1,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {
                                "pattern": "^import net\\.minecraft\\.nbt\\.NBTTagCompound;"
                            },
                            "content": "import api.INbt;",
                        },
                    ],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("replace-by-pattern: exit 0", code == 0)
    content = read_file(f1)
    test("replace-by-pattern: import changed", "import api.INbt;" in content)
    test(
        "replace-by-pattern: old import gone",
        "import net.minecraft.nbt.NBTTagCompound;" not in content,
    )

    # ----------------------------------------------------------------
    # TEST 2: Insert before/after
    # ----------------------------------------------------------------
    f2 = os.path.join(tmpdir, "Insert.java")
    write_file(f2, SAMPLE_JAVA)
    rel2 = os.path.relpath(f2, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel2,
                    "ops": [
                        {
                            "op": "insert",
                            "target": {"line": 1},
                            "position": "before",
                            "content": "// Header comment",
                        },
                        {
                            "op": "insert",
                            "target": {"pattern": "^public class"},
                            "position": "after",
                            "content": "    // class body start",
                        },
                    ],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("insert: exit 0", code == 0)
    content = read_file(f2)
    lines = content.split("\n")
    test("insert-before: first line is comment", lines[0] == "// Header comment")
    test(
        "insert-after: class body comment exists", "    // class body start" in content
    )

    # ----------------------------------------------------------------
    # TEST 3: Delete lines
    # ----------------------------------------------------------------
    f3 = os.path.join(tmpdir, "Delete.java")
    write_file(f3, SAMPLE_JAVA)
    rel3 = os.path.relpath(f3, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel3,
                    "ops": [
                        {
                            "op": "delete",
                            "target": {"pattern": "^import net\\.minecraft\\.nbt"},
                            "count": 1,
                        },
                        {
                            "op": "delete",
                            "target": {"pattern": "^import net\\.minecraft\\.entity"},
                            "count": 1,
                        },
                    ],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("delete: exit 0", code == 0)
    content = read_file(f3)
    test("delete: MC imports removed", "net.minecraft" not in content)
    test("delete: java import preserved", "java.util.List" in content)

    # ----------------------------------------------------------------
    # TEST 4: Multi-line insert
    # ----------------------------------------------------------------
    f4 = os.path.join(tmpdir, "MultiInsert.java")
    write_file(f4, SAMPLE_JAVA)
    rel4 = os.path.relpath(f4, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel4,
                    "ops": [
                        {
                            "op": "insert",
                            "target": {"pattern": "^public class"},
                            "position": "before",
                            "content": "/**\n * Migrated class.\n * PA types only.\n */",
                        },
                    ],
                }
            ]
        },
    )
    code, _, _ = run_editor(manifest, "--force")
    test("multi-line-insert: exit 0", code == 0)
    content = read_file(f4)
    test("multi-line-insert: javadoc present", "* Migrated class." in content)
    test("multi-line-insert: javadoc 3 lines", "* PA types only." in content)

    # ----------------------------------------------------------------
    # TEST 5: Multi-line replace (count > 1)
    # ----------------------------------------------------------------
    f5 = os.path.join(tmpdir, "MultiReplace.java")
    write_file(f5, SAMPLE_JAVA)
    rel5 = os.path.relpath(f5, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel5,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {"pattern": "^import net\\.minecraft\\.nbt"},
                            "count": 2,
                            "content": "import api.INbt;\nimport api.IPlayer;",
                        },
                    ],
                }
            ]
        },
    )
    code, _, _ = run_editor(manifest, "--force")
    test("multi-replace: exit 0", code == 0)
    content = read_file(f5)
    test(
        "multi-replace: new imports present",
        "import api.INbt;" in content and "import api.IPlayer;" in content,
    )
    test("multi-replace: old imports gone", "net.minecraft" not in content)

    # ----------------------------------------------------------------
    # TEST 6: Dry-run does not write
    # ----------------------------------------------------------------
    f6 = os.path.join(tmpdir, "DryRun.java")
    write_file(f6, SAMPLE_JAVA)
    rel6 = os.path.relpath(f6, PROJECT_ROOT)
    original = read_file(f6)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel6,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {"line": 1},
                            "content": "// MODIFIED",
                        }
                    ],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--dry-run")
    test("dry-run: exit 0", code == 0)
    test("dry-run: file unchanged", read_file(f6) == original)
    test("dry-run: output shows DRY RUN", "DRY RUN" in out)

    # ----------------------------------------------------------------
    # TEST 7: Dry-run JSON output
    # ----------------------------------------------------------------
    code, out, _ = run_editor(manifest, "--dry-run", "--json")
    test("dry-run-json: exit 0", code == 0)
    data = json.loads(out)
    test("dry-run-json: has diffs", "diffs" in data)
    test("dry-run-json: dry_run flag true", data.get("dry_run") is True)

    # ----------------------------------------------------------------
    # TEST 8: Backup creates .bak files
    # ----------------------------------------------------------------
    f8 = os.path.join(tmpdir, "Backup.java")
    write_file(f8, SAMPLE_JAVA)
    rel8 = os.path.relpath(f8, PROJECT_ROOT)
    original = read_file(f8)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel8,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {"line": 1},
                            "content": "// MODIFIED",
                        }
                    ],
                }
            ]
        },
    )
    code, _, _ = run_editor(manifest, "--force", "--backup")
    test("backup: exit 0", code == 0)
    test("backup: .bak exists", os.path.isfile(f8 + ".bak"))
    test("backup: .bak has original content", read_file(f8 + ".bak") == original)

    # ----------------------------------------------------------------
    # TEST 9: Error — file not found
    # ----------------------------------------------------------------
    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": os.path.join(tmpdir, "NONEXISTENT.java"),
                    "ops": [{"op": "replace", "target": {"line": 1}, "content": "x"}],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("error-file-not-found: exit 1", code == 1)
    test("error-file-not-found: error in output", "file not found" in out)

    # ----------------------------------------------------------------
    # TEST 10: Error — line out of bounds
    # ----------------------------------------------------------------
    f10 = os.path.join(tmpdir, "OOB.java")
    write_file(f10, SAMPLE_JAVA)
    rel10 = os.path.relpath(f10, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel10,
                    "ops": [{"op": "delete", "target": {"line": 9999}, "count": 1}],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("error-oob: exit 1", code == 1)
    test("error-oob: error mentions bounds", "out of bounds" in out)

    # ----------------------------------------------------------------
    # TEST 11: Error — pattern not found
    # ----------------------------------------------------------------
    f11 = os.path.join(tmpdir, "PatternFail.java")
    write_file(f11, SAMPLE_JAVA)
    rel11 = os.path.relpath(f11, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel11,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {"pattern": "WILL_NEVER_MATCH"},
                            "content": "x",
                        }
                    ],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("error-pattern-not-found: exit 1", code == 1)
    test("error-pattern-not-found: error mentions pattern", "not found" in out)

    # ----------------------------------------------------------------
    # TEST 12: Error — invalid regex
    # ----------------------------------------------------------------
    f12 = os.path.join(tmpdir, "BadRegex.java")
    write_file(f12, SAMPLE_JAVA)
    rel12 = os.path.relpath(f12, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel12,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {"pattern": "[invalid"},
                            "content": "x",
                        }
                    ],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("error-bad-regex: exit 1", code == 1)
    test("error-bad-regex: error mentions regex", "invalid regex" in out)

    # ----------------------------------------------------------------
    # TEST 13: Atomicity — partial failure prevents ALL writes
    # ----------------------------------------------------------------
    f13a = os.path.join(tmpdir, "Atomic_A.java")
    f13b = os.path.join(tmpdir, "Atomic_B.java")
    write_file(f13a, SAMPLE_JAVA)
    write_file(f13b, SAMPLE_JAVA)
    orig_a = read_file(f13a)
    rel13a = os.path.relpath(f13a, PROJECT_ROOT)
    rel13b = os.path.relpath(f13b, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel13a,
                    "ops": [
                        {
                            "op": "insert",
                            "target": {"line": 1},
                            "position": "before",
                            "content": "// SHOULD NOT APPEAR",
                        }
                    ],
                },
                {
                    "path": rel13b,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {"pattern": "WILL_NEVER_MATCH"},
                            "content": "x",
                        }
                    ],
                },
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("atomicity: exit 1", code == 1)
    test("atomicity: file A unchanged", read_file(f13a) == orig_a, read_file(f13a)[:50])

    # ----------------------------------------------------------------
    # TEST 14: Multi-file success
    # ----------------------------------------------------------------
    f14a = os.path.join(tmpdir, "Multi_A.java")
    f14b = os.path.join(tmpdir, "Multi_B.java")
    f14c = os.path.join(tmpdir, "Multi_C.java")
    write_file(f14a, SAMPLE_JAVA)
    write_file(f14b, SAMPLE_JAVA)
    write_file(f14c, SAMPLE_JAVA)
    rel14a = os.path.relpath(f14a, PROJECT_ROOT)
    rel14b = os.path.relpath(f14b, PROJECT_ROOT)
    rel14c = os.path.relpath(f14c, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel14a,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {"pattern": "^import net\\.minecraft\\.nbt"},
                            "content": "import api.INbt;",
                        }
                    ],
                },
                {
                    "path": rel14b,
                    "ops": [
                        {
                            "op": "delete",
                            "target": {"pattern": "^import net\\.minecraft\\.entity"},
                            "count": 1,
                        }
                    ],
                },
                {
                    "path": rel14c,
                    "ops": [
                        {
                            "op": "insert",
                            "target": {"line": 1},
                            "position": "before",
                            "content": "// File C header",
                        }
                    ],
                },
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force", "--json")
    test("multi-file: exit 0", code == 0)
    data = json.loads(out)
    test("multi-file: 3 files modified", data.get("files_modified") == 3)
    test("multi-file A: import replaced", "import api.INbt;" in read_file(f14a))
    test(
        "multi-file B: import deleted",
        "import net.minecraft.entity.player.EntityPlayer;" not in read_file(f14b),
    )
    test("multi-file C: header added", read_file(f14c).startswith("// File C header"))

    # ----------------------------------------------------------------
    # TEST 15: Pattern occurrence targeting
    # ----------------------------------------------------------------
    f15 = os.path.join(tmpdir, "Occurrence.java")
    write_file(f15, "line A\nline B\nline A\nline C\nline A\n")
    rel15 = os.path.relpath(f15, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel15,
                    "ops": [
                        {
                            "op": "replace",
                            "target": {"pattern": "^line A", "occurrence": 2},
                            "content": "line A_REPLACED",
                        }
                    ],
                }
            ]
        },
    )
    code, _, _ = run_editor(manifest, "--force")
    test("occurrence: exit 0", code == 0)
    lines = read_file(f15).split("\n")
    test("occurrence: line 1 unchanged", lines[0] == "line A")
    test("occurrence: line 3 replaced", lines[2] == "line A_REPLACED")
    test("occurrence: line 5 unchanged", lines[4] == "line A")

    # ----------------------------------------------------------------
    # TEST 16: Content as list of strings
    # ----------------------------------------------------------------
    f16 = os.path.join(tmpdir, "ListContent.java")
    write_file(f16, SAMPLE_JAVA)
    rel16 = os.path.relpath(f16, PROJECT_ROOT)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": rel16,
                    "ops": [
                        {
                            "op": "insert",
                            "target": {"line": 1},
                            "position": "before",
                            "content": ["// Line 1", "// Line 2", "// Line 3"],
                        }
                    ],
                }
            ]
        },
    )
    code, _, _ = run_editor(manifest, "--force")
    test("list-content: exit 0", code == 0)
    lines = read_file(f16).split("\n")
    test(
        "list-content: 3 lines inserted",
        lines[0] == "// Line 1" and lines[1] == "// Line 2" and lines[2] == "// Line 3",
    )

    # ----------------------------------------------------------------
    # TEST 17: Structural validation (missing fields)
    # ----------------------------------------------------------------
    manifest = write_manifest(
        tmpdir, {"edits": [{"path": "x.java", "ops": [{"op": "insert"}]}]}
    )
    code, out, _ = run_editor(manifest, "--force")
    test("validation: missing content/target", code == 1)

    manifest = write_manifest(
        tmpdir,
        {
            "edits": [
                {
                    "path": "x.java",
                    "ops": [{"op": "bogus", "target": {"line": 1}, "content": "x"}],
                }
            ]
        },
    )
    code, out, _ = run_editor(manifest, "--force")
    test("validation: invalid op name", code == 1)


if __name__ == "__main__":
    ok = run_tests()
    sys.exit(0 if ok else 1)
