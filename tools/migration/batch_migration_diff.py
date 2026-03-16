"""
batch_migration_diff.py — Batch Migration Diff
================================================

Runs migration-diff logic on ALL migrated files in core/ and produces
one unified JSON with every file's verdict (PASS/WARN/FAIL/NO_COUNTERPART).

Usage:
    python tools/migration/batch_migration_diff.py
    python tools/migration/batch_migration_diff.py --json
    python tools/migration/batch_migration_diff.py --output tools/migration/_batch_diffs.json
    python tools/migration/batch_migration_diff.py --mc-root src/main/java --core-root core/src/main/java
    python tools/migration/batch_migration_diff.py --classifications tools/migration/mc_type_classifications_original.json
    python tools/migration/batch_migration_diff.py --project-root /path/to/project
"""

import sys
import os
import json
import argparse
import difflib
import re
from pathlib import Path
from datetime import datetime, timezone

# Add script directory to path so we can import analyze.py and pa_rules.py
SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))


def auto_detect_project_root():
    """Auto-detect project root from script location (tools/migration/ -> root)."""
    return SCRIPT_DIR.parent.parent


def _try_import_analyze():
    """Try to import functions from analyze.py. Returns a dict of functions or None."""
    try:
        from analyze import (
            setup_treesitter_parser,
            _load_classifications,
            _extract_method_bodies,
            _normalized_signature_key,
            normalize_text,
            _scan_unmigrated_types,
        )

        return {
            "setup_treesitter_parser": setup_treesitter_parser,
            "_load_classifications": _load_classifications,
            "_extract_method_bodies": _extract_method_bodies,
            "_normalized_signature_key": _normalized_signature_key,
            "normalize_text": normalize_text,
            "_scan_unmigrated_types": _scan_unmigrated_types,
        }
    except ImportError as e:
        print(f"WARNING: Could not import from analyze.py: {e}", file=sys.stderr)
        return None


def _normalize_classifications(data):
    """Normalize both mc_type_mappings.json and mc_type_classifications_original.json formats."""
    types = data.get("types") or data.get("mappings") or {}
    normalized = {}
    for type_name, info in types.items():
        if type_name.startswith("_"):
            normalized[type_name] = info
            continue
        entry = dict(info)
        if "pa_name" in entry and "pa" not in entry:
            entry["pa"] = entry["pa_name"]
        if "pa_package" in entry and "package" not in entry:
            entry["package"] = entry["pa_package"]
        if "service_name" in entry and "service" not in entry:
            entry["service"] = entry["service_name"]
        normalized[type_name] = entry
    return normalized


def _load_classifications_fallback(project_root, classifications_path):
    cls_file = Path(project_root) / classifications_path
    if not cls_file.exists():
        raise FileNotFoundError(f"Classifications file not found: {cls_file}")
    with open(cls_file, "r", encoding="utf-8") as f:
        data = json.load(f)
    return _normalize_classifications(data)


def _scan_unmigrated_types_fallback(file_text, classifications):
    """Scan file text for MC type names that should have been replaced. Standalone fallback."""
    results = []
    lines = file_text.split("\n")
    for type_name, info in classifications.items():
        if type_name.startswith("_"):
            continue
        category = info.get("category", "")
        if category not in ("EXISTING", "INTERFACE", "SERVICE"):
            continue
        pa_name = info.get("pa") or info.get("service")
        if not pa_name:
            continue
        if len(type_name) < 4:
            continue
        pattern = re.compile(r"\b" + re.escape(type_name) + r"\b")
        found_lines = []
        has_import = False
        for i, line in enumerate(lines, 1):
            if pattern.search(line):
                if line.strip().startswith("import "):
                    has_import = True
                found_lines.append(i)
        if found_lines:
            results.append(
                {
                    "mc_name": type_name,
                    "pa_name": pa_name,
                    "lines": found_lines,
                    "in_import": has_import,
                }
            )
    return results


def _resolve_classifications_path(args):
    """Resolve which classifications file to use."""
    if args.classifications:
        return args.classifications
    # Prefer mc_type_mappings.json if it exists
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    mappings_path = project_root / "tools" / "migration" / "mc_type_mappings.json"
    if mappings_path.exists():
        return "tools/migration/mc_type_mappings.json"
    # Fallback to original classifications
    original_path = (
        project_root / "tools" / "migration" / "mc_type_classifications_original.json"
    )
    if original_path.exists():
        return "tools/migration/mc_type_classifications_original.json"
    # Default
    return "tools/migration/mc_type_classifications.json"


def run_single_diff(old_file, new_file, parser, classifications, analyze_fns):
    """
    Run migration-diff logic on a single file pair.
    Returns a result dict with verdict, unmigrated, missing_methods, semantic_body_diffs.
    """
    result = {
        "unmigrated": [],
        "missing_methods": [],
        "semantic_body_diffs": [],
    }

    # Step 1: Scan for unmigrated types in new file
    new_text = new_file.read_text(encoding="utf-8", errors="replace")
    if analyze_fns:
        unmigrated = analyze_fns["_scan_unmigrated_types"](new_text, classifications)
    else:
        unmigrated = _scan_unmigrated_types_fallback(new_text, classifications)
    result["unmigrated"] = unmigrated

    # Step 2 & 3: Method-level analysis (requires tree-sitter)
    if parser is not None and analyze_fns:
        try:
            _extract = analyze_fns["_extract_method_bodies"]
            _norm_key = analyze_fns["_normalized_signature_key"]
            _norm_text = analyze_fns["normalize_text"]

            old_methods, _ = _extract(old_file, parser)
            new_methods, _ = _extract(new_file, parser)

            # Build maps by normalized key
            old_method_map = {}
            for m in old_methods:
                key = _norm_key(m, classifications)
                old_method_map[key] = m

            new_method_map = {}
            for m in new_methods:
                key = _norm_key(m, classifications)
                new_method_map[key] = m

            old_keys = set(old_method_map.keys())
            new_keys = set(new_method_map.keys())

            # Missing methods
            for key in sorted(old_keys - new_keys):
                m = old_method_map[key]
                result["missing_methods"].append(
                    {
                        "name": m["name"],
                        "signature": m["signature_text"],
                        "line": m["line"],
                    }
                )

            # Semantic body diffs
            common_keys = old_keys & new_keys
            for key in sorted(common_keys):
                old_m = old_method_map[key]
                new_m = new_method_map[key]

                old_body = old_m.get("body")
                new_body = new_m.get("body")
                if old_body is None or new_body is None:
                    continue

                norm_old = _norm_text(old_body, classifications)
                norm_new = _norm_text(new_body, classifications)

                norm_old_lines = norm_old.strip().splitlines()
                norm_new_lines = norm_new.strip().splitlines()

                matcher = difflib.SequenceMatcher(None, norm_old_lines, norm_new_lines)
                ratio = matcher.ratio()

                diff_lines = list(
                    difflib.unified_diff(
                        norm_old_lines,
                        norm_new_lines,
                        fromfile="old (normalized)",
                        tofile="new (normalized)",
                        lineterm="",
                    )
                )
                change_lines = [
                    l
                    for l in diff_lines
                    if (l.startswith("+") or l.startswith("-"))
                    and not l.startswith("+++")
                    and not l.startswith("---")
                ]

                if ratio < 0.85 or len(change_lines) > 0:
                    result["semantic_body_diffs"].append(
                        {
                            "name": old_m["name"],
                            "signature": old_m["signature_text"],
                            "similarity_ratio": round(ratio, 3),
                            "diff_line_count": len(change_lines),
                            "diff_preview": diff_lines[:10],
                        }
                    )
        except Exception as e:
            result["note"] = f"tree-sitter analysis error: {str(e)}"
    else:
        result["note"] = "tree-sitter not available"

    # Verdict
    if result["missing_methods"]:
        result["verdict"] = "FAIL"
        result["verdict_reason"] = f"{len(result['missing_methods'])} missing method(s)"
    elif result["unmigrated"] or result["semantic_body_diffs"]:
        parts = []
        if result["unmigrated"]:
            parts.append(f"{len(result['unmigrated'])} unmigrated type(s)")
        if result["semantic_body_diffs"]:
            parts.append(f"{len(result['semantic_body_diffs'])} body diff(s)")
        result["verdict"] = "WARN"
        result["verdict_reason"] = ", ".join(parts)
    else:
        result["verdict"] = "PASS"
        result["verdict_reason"] = (
            "all methods present, no unmigrated types, no body drift"
        )

    return result


def main():
    parser = argparse.ArgumentParser(
        description="Batch migration-diff: run migration-diff on ALL core/ files"
    )
    parser.add_argument(
        "--mc-root",
        default="src/main/java",
        help="MC 1.7.10 source root (default: src/main/java)",
    )
    parser.add_argument(
        "--core-root",
        default="core/src/main/java",
        help="Core source root (default: core/src/main/java)",
    )
    parser.add_argument(
        "--classifications",
        help="Path to classifications JSON (relative to project root)",
    )
    parser.add_argument(
        "--output",
        default="tools/migration/_batch_diffs.json",
        help="Output JSON file path (relative to project root)",
    )
    parser.add_argument(
        "--json",
        dest="json_output",
        action="store_true",
        help="Print JSON to stdout instead of human-readable output",
    )
    parser.add_argument(
        "--project-root",
        help="Project root (auto-detected if omitted)",
    )

    args = parser.parse_args()

    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )

    # Resolve classifications path
    classifications_path = _resolve_classifications_path(args)

    # Try to import from analyze.py
    analyze_fns = _try_import_analyze()

    # Load classifications
    if analyze_fns:
        try:
            classifications = analyze_fns["_load_classifications"](
                project_root, classifications_path
            )
            if not classifications:
                classifications = _load_classifications_fallback(
                    project_root, classifications_path
                )
        except (FileNotFoundError, KeyError):
            classifications = _load_classifications_fallback(
                project_root, classifications_path
            )
    else:
        classifications = _load_classifications_fallback(
            project_root, classifications_path
        )

    # Setup tree-sitter parser
    ts_parser = None
    if analyze_fns:
        try:
            ts_parser, _ = analyze_fns["setup_treesitter_parser"]()
        except Exception:
            pass

    # Enumerate all .java files in core-root
    core_root = project_root / args.core_root
    mc_root = project_root / args.mc_root

    if not core_root.exists():
        print(f"ERROR: Core root not found: {core_root}", file=sys.stderr)
        return 1

    core_files = sorted(core_root.rglob("*.java"))
    total_files = len(core_files)

    results = []
    summary = {"PASS": [], "WARN": [], "FAIL": [], "NO_COUNTERPART": []}
    files_with_counterpart = 0
    files_without_counterpart = 0

    for core_file in core_files:
        rel_path = core_file.relative_to(core_root)
        # Normalize to forward slashes for consistency
        rel_path_str = str(rel_path).replace("\\", "/")

        mc_file = mc_root / rel_path

        if mc_file.exists():
            files_with_counterpart += 1
            diff_result = run_single_diff(
                mc_file, core_file, ts_parser, classifications, analyze_fns
            )

            entry = {
                "relative_path": rel_path_str,
                "old_file": str(Path(args.mc_root) / rel_path).replace("\\", "/"),
                "new_file": str(Path(args.core_root) / rel_path).replace("\\", "/"),
                "verdict": diff_result["verdict"],
                "verdict_reason": diff_result["verdict_reason"],
                "unmigrated_count": len(diff_result["unmigrated"]),
                "missing_methods_count": len(diff_result["missing_methods"]),
                "semantic_diffs_count": len(diff_result["semantic_body_diffs"]),
                "unmigrated": diff_result["unmigrated"],
                "missing_methods": diff_result["missing_methods"],
                "semantic_body_diffs": diff_result["semantic_body_diffs"],
            }
            if "note" in diff_result:
                entry["note"] = diff_result["note"]

            results.append(entry)
            summary[diff_result["verdict"]].append(rel_path_str)
        else:
            files_without_counterpart += 1
            entry = {
                "relative_path": rel_path_str,
                "verdict": "NO_COUNTERPART",
                "verdict_reason": "No mc1710 original found",
                "new_file": str(Path(args.core_root) / rel_path).replace("\\", "/"),
            }
            results.append(entry)
            summary["NO_COUNTERPART"].append(rel_path_str)

    # Build output
    output = {
        "_meta": {
            "generated": datetime.now(timezone.utc).isoformat(),
            "mc_root": args.mc_root,
            "core_root": args.core_root,
            "classifications_source": classifications_path,
            "total_files_in_core": total_files,
            "files_with_counterpart": files_with_counterpart,
            "files_without_counterpart": files_without_counterpart,
            "passed": len(summary["PASS"]),
            "warned": len(summary["WARN"]),
            "failed": len(summary["FAIL"]),
        },
        "results": results,
        "summary": summary,
    }

    if args.json_output:
        json.dump(output, sys.stdout, indent=2)
        print()
        return 0

    # Human-readable output
    print()
    print(f"Batch Migration Diff — {total_files} core files analyzed")
    print(f"  With counterpart:    {files_with_counterpart}")
    print(f"  Without counterpart: {files_without_counterpart}")
    print()
    print(f"  PASS: {len(summary['PASS']):>4}")
    print(f"  WARN: {len(summary['WARN']):>4}")
    print(f"  FAIL: {len(summary['FAIL']):>4}")
    print()

    if summary["FAIL"]:
        print("FAILED FILES:")
        for rel in summary["FAIL"]:
            entry = next(r for r in results if r["relative_path"] == rel)
            print(f"  {rel}")
            if entry.get("missing_methods"):
                methods = ", ".join(m["name"] for m in entry["missing_methods"])
                print(
                    f"    - {len(entry['missing_methods'])} missing methods: {methods}"
                )
            if entry.get("unmigrated"):
                types = ", ".join(u["mc_name"] for u in entry["unmigrated"])
                print(f"    - {len(entry['unmigrated'])} unmigrated types: {types}")
        print()

    if summary["WARN"]:
        shown = min(5, len(summary["WARN"]))
        print(f"WARNED FILES (showing first {shown}):")
        for rel in summary["WARN"][:shown]:
            entry = next(r for r in results if r["relative_path"] == rel)
            parts = []
            if entry.get("semantic_body_diffs"):
                sims = [
                    str(round(d["similarity_ratio"] * 100)) + "%"
                    for d in entry["semantic_body_diffs"]
                ]
                parts.append(
                    f"{len(entry['semantic_body_diffs'])} body diffs (similarity: {', '.join(sims)})"
                )
            if entry.get("unmigrated"):
                parts.append(f"{len(entry['unmigrated'])} unmigrated types")
            print(f"  {rel}")
            for part in parts:
                print(f"    ~ {part}")
        if len(summary["WARN"]) > shown:
            print(f"  ... and {len(summary['WARN']) - shown} more")
        print()

    # Write JSON output file
    output_path = project_root / args.output
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2)
    print(f"Results written to: {args.output}")
    print()

    return 0


if __name__ == "__main__":
    sys.exit(main())
