#!/usr/bin/env python3
"""PA Batch Writer — Write multiple Java source files from a single JSON manifest.

Usage:
    python tools/migration/pa_batch.py <manifest.json> [options]

Reads a JSON manifest containing raw Java source strings and writes them to disk.
Auto-derives file paths from package declarations and public type names when
no explicit path is provided.

See PA_IMPLEMENTOR_DESIGN.md for full design rationale and manifest format.
"""

import argparse
import json
import os
import re
import sys

RE_PACKAGE = re.compile(r"^\s*package\s+([\w.]+)\s*;", re.MULTILINE)
RE_PUBLIC_TYPE = re.compile(
    r"public\s+(?:abstract\s+|final\s+|strictfp\s+)*"
    r"(?:class|interface|enum|@interface)\s+(\w+)",
    re.MULTILINE,
)


def detect_project_root(start: str) -> str:
    """Walk up from *start* looking for a directory that contains settings.gradle."""
    cur = os.path.abspath(start)
    for _ in range(20):
        if os.path.isfile(os.path.join(cur, "settings.gradle")):
            return cur
        parent = os.path.dirname(cur)
        if parent == cur:
            break
        cur = parent
    return os.path.abspath(start)


def derive_path(content: str, root: str) -> tuple:
    """Derive the output file path from *content*'s package + public type name.

    Returns ``(path, error_message)``.  On success *error_message* is ``None``.
    """
    pkg_match = RE_PACKAGE.search(content)
    if not pkg_match:
        return None, "no package declaration found"
    type_match = RE_PUBLIC_TYPE.search(content)
    if not type_match:
        return None, "no public class/interface/enum/@interface found"
    pkg = pkg_match.group(1).replace(".", os.sep)
    name = type_match.group(1)
    return os.path.join(root, pkg, f"{name}.java"), None


def validate_package_vs_path(content: str, path: str) -> str | None:
    """Return a warning string if *path* doesn't match the package in *content*."""
    pkg_match = RE_PACKAGE.search(content)
    if not pkg_match:
        return None
    expected_pkg_dir = pkg_match.group(1).replace(".", os.sep)
    norm = path.replace("\\", "/")
    expected = expected_pkg_dir.replace("\\", "/")
    if expected not in norm:
        return f'package "{pkg_match.group(1)}" does not match path "{path}"'
    return None


def process_manifest(
    manifest_path: str, project_root: str, force: bool, dry_run: bool, json_output: bool
):
    """Read *manifest_path* and write (or preview) all files."""
    with open(manifest_path, "r", encoding="utf-8") as fh:
        manifest = json.load(fh)

    global_root = manifest.get("root", "")
    entries = manifest.get("files", [])

    created: list[str] = []
    skipped: list[str] = []
    errors: list[dict] = []
    warnings: list[str] = []

    for idx, entry in enumerate(entries):
        content = entry.get("content")
        if content is None:
            errors.append({"index": idx, "error": 'missing "content" field'})
            continue

        explicit_path = entry.get("path")
        entry_root = entry.get("root", global_root)

        if explicit_path:
            rel_path = explicit_path
            warn = validate_package_vs_path(content, rel_path)
            if warn:
                warnings.append(f"entry {idx}: {warn}")
        else:
            if not entry_root:
                errors.append(
                    {
                        "index": idx,
                        "error": "cannot derive path — no root and no explicit path",
                    }
                )
                continue
            rel_path, err = derive_path(content, entry_root)
            if err:
                errors.append({"index": idx, "error": f"cannot derive path — {err}"})
                continue

        abs_path = os.path.join(project_root, rel_path)

        if os.path.isfile(abs_path) and not force:
            skipped.append(rel_path)
            continue

        if not dry_run:
            os.makedirs(os.path.dirname(abs_path), exist_ok=True)
            with open(abs_path, "w", encoding="utf-8", newline="\n") as out:
                out.write(content)

        created.append(rel_path)

    if json_output:
        result = {
            "total": len(entries),
            "created": created,
            "skipped": skipped,
            "errors": errors,
            "warnings": warnings,
            "dry_run": dry_run,
        }
        print(json.dumps(result, indent=2))
    else:
        mode = " (DRY RUN)" if dry_run else ""
        print(f"\nPA Batch Writer — {len(entries)} files processed{mode}")
        print(f"  CREATED:  {len(created)} files")
        print(f"  SKIPPED:  {len(skipped)} files (already exist)")
        print(f"  ERRORS:   {len(errors)}")
        if warnings:
            print(f"  WARNINGS: {len(warnings)}")
        if created:
            verb = "Would create" if dry_run else "Created"
            print(f"\n{verb}:")
            for p in created:
                print(f"  {p}")
        if skipped:
            print("\nSkipped (already exist):")
            for p in skipped:
                print(f"  {p}")
        if errors:
            print("\nErrors:")
            for e in errors:
                print(f"  entry {e['index']}: {e['error']}")
        if warnings:
            print("\nWarnings:")
            for w in warnings:
                print(f"  {w}")

    return len(errors) == 0


def main():
    parser = argparse.ArgumentParser(
        description="PA Batch Writer — write multiple Java source files from a JSON manifest.",
    )
    parser.add_argument("manifest", help="Path to JSON manifest file")
    parser.add_argument("--force", action="store_true", help="Overwrite existing files")
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show what would be created without writing",
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

    if not os.path.isfile(args.manifest):
        print(f"ERROR: manifest not found: {args.manifest}", file=sys.stderr)
        sys.exit(1)

    if args.project_root:
        project_root = os.path.abspath(args.project_root)
    else:
        project_root = detect_project_root(os.path.dirname(os.path.abspath(__file__)))

    try:
        ok = process_manifest(
            args.manifest, project_root, args.force, args.dry_run, args.json
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
