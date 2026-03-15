#!/usr/bin/env python3
"""PA Batch Editor — Surgical multi-file editing from a single JSON manifest.

Usage:
    python tools/migration/pa_editor.py <manifest.json> [options]

Companion to pa_batch.py (which creates files). This tool EDITS existing files
with atomic transaction semantics: all edits succeed or ALL rollback.

Operations: insert (before/after line), delete (N lines), replace (pattern→content).
Targeting: line numbers OR regex patterns (agent chooses per-edit).

See AGENTS.md in tools/migration/ for full manifest format and usage patterns.
"""

import argparse
import copy
import difflib
import json
import os
import re
import shutil
import sys
from typing import Any


def detect_project_root(start: str) -> str:
    """Walk up from *start* looking for settings.gradle."""
    cur = os.path.abspath(start)
    for _ in range(20):
        if os.path.isfile(os.path.join(cur, "settings.gradle")):
            return cur
        parent = os.path.dirname(cur)
        if parent == cur:
            break
        cur = parent
    return os.path.abspath(start)


# ---------------------------------------------------------------------------
# Edit validation
# ---------------------------------------------------------------------------


def _resolve_line(
    lines: list[str], target: dict[str, Any], filepath: str
) -> tuple[int | None, str | None]:
    """Resolve a target spec to a 0-based line index.

    Target formats:
      {"line": 10}              → line 10 (1-based → index 9)
      {"pattern": "import.*"}   → first line matching regex
      {"pattern": "import.*", "occurrence": 2}  → 2nd match
    Returns (index, error_msg). On success error_msg is None.
    """
    if "line" in target:
        ln = target["line"]
        if not isinstance(ln, int) or ln < 1:
            return None, f"invalid line number: {ln}"
        if ln > len(lines):
            return None, f"line {ln} out of bounds (file has {len(lines)} lines)"
        return ln - 1, None

    if "pattern" in target:
        pat = target["pattern"]
        try:
            regex = re.compile(pat)
        except re.error as exc:
            return None, f"invalid regex '{pat}': {exc}"
        occurrence = target.get("occurrence", 1)
        if not isinstance(occurrence, int) or occurrence < 1:
            return None, f"invalid occurrence: {occurrence}"
        count = 0
        for i, line in enumerate(lines):
            if regex.search(line):
                count += 1
                if count == occurrence:
                    return i, None
        return None, f"pattern '{pat}' not found in {filepath}" + (
            f" (occurrence {occurrence})" if occurrence > 1 else ""
        )

    return None, "target must specify 'line' or 'pattern'"


def _validate_edit(edit: dict[str, Any], idx: int) -> str | None:
    """Validate a single edit spec. Returns error message or None."""
    op = edit.get("op")
    if op not in ("insert", "delete", "replace"):
        return f"edit {idx}: invalid op '{op}' (must be insert/delete/replace)"

    if op == "insert":
        if "content" not in edit:
            return f"edit {idx}: insert requires 'content'"
        if "target" not in edit:
            return f"edit {idx}: insert requires 'target'"
        pos = edit.get("position", "after")
        if pos not in ("before", "after"):
            return f"edit {idx}: position must be 'before' or 'after', got '{pos}'"

    elif op == "delete":
        if "target" not in edit:
            return f"edit {idx}: delete requires 'target'"
        count = edit.get("count", 1)
        if not isinstance(count, int) or count < 1:
            return f"edit {idx}: delete count must be a positive integer"

    elif op == "replace":
        if "content" not in edit:
            return f"edit {idx}: replace requires 'content'"
        if "target" not in edit:
            return f"edit {idx}: replace requires 'target'"
        count = edit.get("count", 1)
        if not isinstance(count, int) or count < 1:
            return f"edit {idx}: replace count must be a positive integer"

    return None


# ---------------------------------------------------------------------------
# Edit application (on in-memory line list)
# ---------------------------------------------------------------------------


def _apply_edit(
    lines: list[str], edit: dict[str, Any], idx: int, filepath: str
) -> tuple[list[str], str | None]:
    """Apply a single edit to *lines* (in-memory). Returns (new_lines, error_msg).

    Edits are applied to a copy. On error, returns (original_lines, error_msg).
    """
    op = edit["op"]
    target = edit["target"]

    line_idx, err = _resolve_line(lines, target, filepath)
    if err or line_idx is None:
        return lines, f"edit {idx}: {err}"

    result = list(lines)

    if op == "insert":
        content = edit["content"]
        new_lines = _content_to_lines(content)
        pos = edit.get("position", "after")
        insert_at = line_idx if pos == "before" else line_idx + 1
        for i, nl in enumerate(new_lines):
            result.insert(insert_at + i, nl)

    elif op == "delete":
        count = edit.get("count", 1)
        end = line_idx + count
        if end > len(result):
            return (
                lines,
                f"edit {idx}: delete would go past end of file (line {line_idx + 1} + {count} > {len(result)})",
            )
        del result[line_idx:end]

    elif op == "replace":
        count = edit.get("count", 1)
        end = line_idx + count
        if end > len(result):
            return (
                lines,
                f"edit {idx}: replace range past end of file (line {line_idx + 1} + {count} > {len(result)})",
            )
        new_lines = _content_to_lines(edit["content"])
        result[line_idx:end] = new_lines

    return result, None


def _content_to_lines(content: str | list[str]) -> list[str]:
    """Normalize content (string or list of strings) to list of lines with newlines."""
    if isinstance(content, list):
        return [ln if ln.endswith("\n") else ln + "\n" for ln in content]
    # String: split into lines, preserve trailing newline
    raw_lines = content.split("\n")
    # If content ends with \n, split produces an empty trailing element — drop it
    if raw_lines and raw_lines[-1] == "":
        raw_lines = raw_lines[:-1]
    return [ln + "\n" for ln in raw_lines]


# ---------------------------------------------------------------------------
# Diff generation
# ---------------------------------------------------------------------------


def _generate_diff(
    old_lines: list[str], new_lines: list[str], filepath: str
) -> list[str]:
    """Generate unified diff between old and new content."""
    return list(
        difflib.unified_diff(
            old_lines,
            new_lines,
            fromfile=f"a/{filepath}",
            tofile=f"b/{filepath}",
            lineterm="\n",
        )
    )


# ---------------------------------------------------------------------------
# Core processing
# ---------------------------------------------------------------------------


def process_manifest(
    manifest_path: str,
    project_root: str,
    force: bool,
    dry_run: bool,
    backup: bool,
    json_output: bool,
) -> bool:
    """Read *manifest_path*, validate ALL edits, then apply atomically (or preview)."""
    with open(manifest_path, "r", encoding="utf-8") as fh:
        manifest = json.load(fh)

    file_edits: list[dict[str, Any]] = manifest.get("edits", [])
    if not file_edits:
        _output(json_output, dry_run, [], [], [], [])
        return True

    # ======================================================================
    # PHASE 1: Structural validation (no file I/O)
    # ======================================================================
    errors: list[dict[str, Any]] = []
    for fi, fe in enumerate(file_edits):
        path = fe.get("path")
        if not path:
            errors.append({"file_index": fi, "error": "missing 'path'"})
            continue
        ops = fe.get("ops", [])
        if not ops:
            errors.append({"file_index": fi, "path": path, "error": "no ops specified"})
            continue
        for oi, op_spec in enumerate(ops):
            err = _validate_edit(op_spec, oi)
            if err:
                errors.append({"file_index": fi, "path": path, "error": err})

    if errors:
        _output(json_output, dry_run, [], [], errors, [])
        return False

    # ======================================================================
    # PHASE 2: Read all files, resolve all targets, compute results
    # ======================================================================
    file_states: list[
        dict[str, Any]
    ] = []  # {path, abs_path, original, modified, diffs, edit_count}
    warnings: list[str] = []

    for fi, fe in enumerate(file_edits):
        rel_path = fe["path"]
        abs_path = os.path.join(project_root, rel_path)

        if not os.path.isfile(abs_path):
            errors.append(
                {"file_index": fi, "path": rel_path, "error": "file not found"}
            )
            continue

        try:
            with open(abs_path, "r", encoding="utf-8") as f:
                original = f.readlines()
        except Exception as exc:
            errors.append(
                {"file_index": fi, "path": rel_path, "error": f"read error: {exc}"}
            )
            continue

        # Apply edits sequentially (order matters — line numbers shift)
        current = list(original)
        ops = fe["ops"]
        edit_count = 0
        file_ok = True

        for oi, op_spec in enumerate(ops):
            current, err = _apply_edit(current, op_spec, oi, rel_path)
            if err:
                errors.append({"file_index": fi, "path": rel_path, "error": err})
                file_ok = False
                break
            edit_count += 1

        if not file_ok:
            continue

        if current == original:
            warnings.append(f"{rel_path}: edits produced no changes")

        diffs = _generate_diff(original, current, rel_path)
        file_states.append(
            {
                "path": rel_path,
                "abs_path": abs_path,
                "original": original,
                "modified": current,
                "diffs": diffs,
                "edit_count": edit_count,
            }
        )

    # If ANY file had errors, abort ALL
    if errors:
        _output(json_output, dry_run, [], [], errors, warnings)
        return False

    # ======================================================================
    # PHASE 3: Apply (or dry-run)
    # ======================================================================
    modified_paths: list[str] = []
    backup_paths: list[str] = []

    if not dry_run:
        # Create backups first (if requested), so rollback is possible
        if backup:
            for fs in file_states:
                bak_path = fs["abs_path"] + ".bak"
                try:
                    shutil.copy2(fs["abs_path"], bak_path)
                    backup_paths.append(bak_path)
                except Exception as exc:
                    errors.append(
                        {"path": fs["path"], "error": f"backup failed: {exc}"}
                    )

            if errors:
                # Clean up any backups we already made
                for bp in backup_paths:
                    try:
                        os.remove(bp)
                    except OSError:
                        pass
                _output(json_output, dry_run, [], [], errors, warnings)
                return False

        written: list[str] = []
        write_error = False
        for fs in file_states:
            try:
                with open(fs["abs_path"], "w", encoding="utf-8", newline="") as out:
                    out.writelines(fs["modified"])
                written.append(fs["abs_path"])
                modified_paths.append(fs["path"])
            except Exception as exc:
                errors.append({"path": fs["path"], "error": f"write failed: {exc}"})
                write_error = True
                break

        # ROLLBACK on write failure
        if write_error:
            for abs_p in written:
                # Find the original content for this path
                for fs in file_states:
                    if fs["abs_path"] == abs_p:
                        try:
                            with open(abs_p, "w", encoding="utf-8", newline="") as out:
                                out.writelines(fs["original"])
                        except Exception:
                            pass  # best-effort rollback
                        break
            # Also remove backups since we rolled back
            for bp in backup_paths:
                try:
                    os.remove(bp)
                except OSError:
                    pass
            _output(json_output, dry_run, [], [], errors, warnings)
            return False
    else:
        modified_paths = [fs["path"] for fs in file_states]

    _output(json_output, dry_run, file_states, modified_paths, errors, warnings)
    return True


# ---------------------------------------------------------------------------
# Output formatting
# ---------------------------------------------------------------------------


def _output(
    json_mode: bool,
    dry_run: bool,
    file_states: list[dict[str, Any]],
    modified: list[str],
    errors: list[dict[str, Any]],
    warnings: list[str],
):
    """Print results in human-readable or JSON format."""
    total_edits = sum(fs.get("edit_count", 0) for fs in file_states)

    if json_mode:
        result: dict[str, Any] = {
            "dry_run": dry_run,
            "files_modified": len(modified),
            "total_edits": total_edits,
            "modified": modified,
            "errors": errors,
            "warnings": warnings,
        }
        if dry_run and file_states:
            result["diffs"] = {fs["path"]: "".join(fs["diffs"]) for fs in file_states}
        print(json.dumps(result, indent=2))
        return

    mode = " (DRY RUN)" if dry_run else ""
    print(f"\nPA Batch Editor — {len(modified)} files, {total_edits} edits{mode}")
    print(f"  MODIFIED: {len(modified)} files")
    print(f"  ERRORS:   {len(errors)}")
    if warnings:
        print(f"  WARNINGS: {len(warnings)}")

    if modified and not dry_run:
        print("\nModified:")
        for p in modified:
            edit_count = 0
            for fs in file_states:
                if fs["path"] == p:
                    edit_count = fs["edit_count"]
                    break
            print(f"  {p} ({edit_count} edits)")

    if dry_run and file_states:
        print("\nDiffs:")
        for fs in file_states:
            if fs["diffs"]:
                print(f"\n--- {fs['path']} ({fs['edit_count']} edits) ---")
                for d in fs["diffs"]:
                    line = d.rstrip("\n")
                    print(line)
            else:
                print(f"\n--- {fs['path']} (no changes) ---")

    if errors:
        print("\nErrors:")
        for e in errors:
            path_info = e.get("path", f"index {e.get('file_index', '?')}")
            print(f"  {path_info}: {e['error']}")

    if warnings:
        print("\nWarnings:")
        for w in warnings:
            print(f"  {w}")


# ---------------------------------------------------------------------------
# CLI
# ---------------------------------------------------------------------------


def main():
    parser = argparse.ArgumentParser(
        description="PA Batch Editor — surgical multi-file editing from a JSON manifest.",
    )
    parser.add_argument("manifest", help="Path to JSON edit manifest file")
    parser.add_argument(
        "--force", action="store_true", help="Apply edits (required to write)"
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show diffs without writing (default if --force not given)",
    )
    parser.add_argument(
        "--backup",
        action="store_true",
        help="Save .bak copies of original files before editing",
    )
    parser.add_argument(
        "--json", action="store_true", help="Machine-readable JSON output"
    )
    parser.add_argument(
        "--project-root",
        default=None,
        help="Project root override (auto-detected by default)",
    )
    args = parser.parse_args()

    # Default to dry-run if --force not specified
    if not args.force and not args.dry_run:
        args.dry_run = True

    if not os.path.isfile(args.manifest):
        print(f"ERROR: manifest not found: {args.manifest}", file=sys.stderr)
        sys.exit(1)

    if args.project_root:
        project_root = os.path.abspath(args.project_root)
    else:
        project_root = detect_project_root(os.path.dirname(os.path.abspath(__file__)))

    try:
        ok = process_manifest(
            args.manifest,
            project_root,
            args.force,
            args.dry_run,
            args.backup,
            args.json,
        )
    except json.JSONDecodeError as exc:
        print(f"ERROR: invalid JSON in manifest — {exc}", file=sys.stderr)
        sys.exit(1)
    except Exception as exc:
        print(f"ERROR: {exc}", file=sys.stderr)
        sys.exit(1)

    sys.exit(0 if ok else 1)


if __name__ == "__main__":
    main()
