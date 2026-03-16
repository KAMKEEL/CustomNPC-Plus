"""
generate_pa_manifest.py — Generate PA Interface Manifest + Swap Table
=====================================================================

Generates a pa_batch.py-compatible manifest JSON for creating PA interface
files in platform-api/, plus a swap table for Symbol-Swap.

Usage:
    python tools/migration/generate_pa_manifest.py
    python tools/migration/generate_pa_manifest.py --category INTERFACE
    python tools/migration/generate_pa_manifest.py --category INTERFACE EXISTING
    python tools/migration/generate_pa_manifest.py --output tools/migration/_pa_manifest.json
    python tools/migration/generate_pa_manifest.py --swap-output tools/migration/_swap_table.json
    python tools/migration/generate_pa_manifest.py --json
    python tools/migration/generate_pa_manifest.py --dry-run
"""

import sys
import json
import argparse
from pathlib import Path
from datetime import datetime, timezone

SCRIPT_DIR = Path(__file__).resolve().parent
if str(SCRIPT_DIR) not in sys.path:
    sys.path.insert(0, str(SCRIPT_DIR))

from pa_rules import (
    resolve_param_type,
    should_exclude_method,
    resolve_obfuscated_method,
    get_interface_package,
    NAMING,
    EXISTING_MAPPINGS,
    TRANSFORM_PATTERNS,
    SERVICE_ROUTING,
)


def auto_detect_project_root():
    return SCRIPT_DIR.parent.parent


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


def _load_classifications_data(project_root, mappings_path=None):
    if mappings_path:
        p = Path(mappings_path)
        if not p.is_absolute():
            p = project_root / mappings_path
        if p.exists():
            with open(p, "r", encoding="utf-8") as f:
                data = json.load(f)
            return _normalize_classifications(data), str(p.name)
    for candidate in [
        "tools/migration/mc_type_mappings.json",
        "tools/migration/mc_type_classifications_original.json",
    ]:
        p = project_root / candidate
        if p.exists():
            with open(p, "r", encoding="utf-8") as f:
                data = json.load(f)
            return _normalize_classifications(data), candidate
    raise FileNotFoundError("No classifications file found")


def _load_methods_export(project_root, methods_path=None):
    """Load all_methods_export.json for usage counts."""
    if methods_path:
        p = Path(methods_path)
        if not p.is_absolute():
            p = project_root / methods_path
    else:
        p = project_root / "tools" / "migration" / "all_methods_export.json"
    if not p.exists():
        return {}
    with open(p, "r", encoding="utf-8") as f:
        data = json.load(f)
    lookup = {}
    for t in data.get("types", []):
        lookup[t["shortName"]] = t
    return lookup


def _infer_return_type(method_name):
    """Infer return type from Java naming conventions."""
    if method_name.startswith("get"):
        return "Object"
    if method_name.startswith("is") or method_name.startswith("has"):
        return "boolean"
    if (
        method_name.startswith("set")
        or method_name.startswith("add")
        or method_name.startswith("remove")
    ):
        return "void"
    return "void"


def _get_usage_count(method_name, methods_export_entry):
    """Look up usage count for a method from the export data."""
    if not methods_export_entry:
        return 0
    for m in methods_export_entry.get("methods", []):
        if m["name"] == method_name:
            return m["count"]
    for m in methods_export_entry.get("static_methods", []):
        if m["name"] == method_name:
            return m["count"]
    return 0


def _generate_interface_content(type_name, info, methods_export_entry):
    """Generate Java interface source code for a PA interface."""
    pa_name = info.get("pa", NAMING.get(type_name, f"I{type_name}"))
    fqn = info.get("fqn", "")
    pkg = info.get("package", get_interface_package(pa_name))

    raw_methods = info.get("methods", [])

    filtered_methods = []
    for m in raw_methods:
        method_name = m if isinstance(m, str) else m.get("name", m)
        if should_exclude_method(method_name):
            continue
        resolved_name = resolve_obfuscated_method(method_name)
        if resolved_name != method_name and should_exclude_method(resolved_name):
            continue
        filtered_methods.append(resolved_name)

    seen = set()
    unique_methods = []
    for m in filtered_methods:
        if m not in seen:
            seen.add(m)
            unique_methods.append(m)

    lines = []
    lines.append(f"package {pkg};")
    lines.append("")
    lines.append("/**")
    lines.append(f" * Platform abstraction for {fqn}.")
    lines.append(f" * MC 1.7.10: {type_name}")
    lines.append(f" * Generated by generate_pa_manifest.py")
    lines.append(" */")
    lines.append(f"public interface {pa_name} {{")

    for method_name in unique_methods:
        usage = _get_usage_count(method_name, methods_export_entry)
        ret_type = _infer_return_type(method_name)
        usage_note = f" — used {usage} times" if usage > 0 else ""
        lines.append(f"    /** {method_name}{usage_note} */")
        lines.append(f"    {ret_type} {method_name}();")

    lines.append("}")
    lines.append("")

    return "\n".join(lines), pa_name, pkg


def _build_swap_entry(type_name, info):
    """Build a swap tuple [old_name, new_name, old_fqn, new_fqn]."""
    pa_name = info.get("pa", NAMING.get(type_name, f"I{type_name}"))
    fqn = info.get("fqn", "")
    pkg = info.get("package", get_interface_package(pa_name))
    new_fqn = f"{pkg}.{pa_name}"
    return [type_name, pa_name, fqn, new_fqn]


def _build_transform_entries(classifications):
    """Build transform entries from TRANSFORM-category types."""
    transforms = []
    for type_name, info in classifications.items():
        if type_name.startswith("_"):
            continue
        if info.get("category") != "TRANSFORM":
            continue
        patterns = info.get("patterns", [])
        for pat in patterns:
            if len(pat) >= 2:
                transforms.append(pat)

    for svc_name, routing in SERVICE_ROUTING.items():
        if routing.get("service") == "NBTIO":
            pass

    return transforms


def _format_powershell_swaps(swaps):
    """Format swaps as a PowerShell array string."""
    lines = ["$AllSwaps = @("]
    for i, swap in enumerate(swaps):
        comma = "," if i < len(swaps) - 1 else ""
        lines.append(
            f"    @('{swap[0]}', '{swap[1]}', '{swap[2]}', '{swap[3]}'){comma}"
        )
    lines.append(")")
    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(
        description="Generate PA interface manifest and swap table"
    )
    parser.add_argument(
        "--mappings",
        help="Path to mc_type_mappings.json or classifications JSON",
    )
    parser.add_argument(
        "--methods",
        default=None,
        help="Path to all_methods_export.json",
    )
    parser.add_argument(
        "--category",
        nargs="+",
        default=["INTERFACE"],
        help="Categories to include (default: INTERFACE)",
    )
    parser.add_argument(
        "--output",
        default="tools/migration/_pa_manifest.json",
        help="Output manifest JSON path (relative to project root)",
    )
    parser.add_argument(
        "--swap-output",
        default="tools/migration/_swap_table.json",
        help="Output swap table JSON path (relative to project root)",
    )
    parser.add_argument(
        "--json",
        dest="json_output",
        action="store_true",
        help="Print manifest to stdout as JSON",
    )
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Show what would be generated without writing files",
    )
    parser.add_argument(
        "--project-root",
        help="Project root (auto-detected if omitted)",
    )

    args = parser.parse_args()
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    requested_categories = set(c.upper() for c in args.category)

    types, cls_source = _load_classifications_data(project_root, args.mappings)

    methods_export = _load_methods_export(project_root, args.methods)

    manifest_files = []
    all_swaps = []
    generated_count = 0

    for type_name, info in types.items():
        if type_name.startswith("_"):
            continue
        category = info.get("category", "")
        if category not in requested_categories:
            continue

        pa_name = info.get("pa") or info.get("service")
        if not pa_name:
            pa_name = NAMING.get(type_name, f"I{type_name}")

        if category in ("EXISTING", "INTERFACE"):
            swap = _build_swap_entry(type_name, info)
            if swap not in all_swaps:
                all_swaps.append(swap)

        if category == "INTERFACE" and info.get("methods"):
            export_entry = methods_export.get(type_name)
            content, gen_pa_name, pkg = _generate_interface_content(
                type_name, info, export_entry
            )
            manifest_files.append({"content": content})
            generated_count += 1

    manifest = {
        "root": "platform-api/src/main/java",
        "files": manifest_files,
    }

    all_transforms = _build_transform_entries(types)

    swap_table = {
        "_meta": {
            "generated": datetime.now(timezone.utc).isoformat(),
            "source": cls_source,
            "total_swaps": len(all_swaps),
            "total_transforms": len(all_transforms),
        },
        "swaps": all_swaps,
        "transforms": all_transforms,
        "powershell_swaps": _format_powershell_swaps(all_swaps),
    }

    if args.json_output:
        json.dump(manifest, sys.stdout, indent=2)
        print()
        return 0

    if args.dry_run:
        print()
        print(f"PA Manifest Generator — DRY RUN")
        print(f"  Source: {cls_source}")
        print(f"  Categories: {', '.join(sorted(requested_categories))}")
        print(f"  Interfaces to generate: {generated_count}")
        print(f"  Swap entries: {len(all_swaps)}")
        print(f"  Transform entries: {len(all_transforms)}")
        print()
        if manifest_files:
            print("INTERFACES:")
            for f in manifest_files:
                content = f["content"]
                first_line = next(
                    (
                        l
                        for l in content.split("\n")
                        if l.startswith("public interface")
                    ),
                    "?",
                )
                pkg_line = next(
                    (l for l in content.split("\n") if l.startswith("package ")), "?"
                )
                print(f"  {pkg_line.rstrip(';')} / {first_line.rstrip(' {')}")
            print()
        if all_swaps:
            print("SWAPS:")
            for s in all_swaps[:10]:
                print(f"  {s[0]:30s} -> {s[1]:20s}  ({s[2]} -> {s[3]})")
            if len(all_swaps) > 10:
                print(f"  ... and {len(all_swaps) - 10} more")
            print()
        return 0

    manifest_path = project_root / args.output
    manifest_path.parent.mkdir(parents=True, exist_ok=True)
    with open(manifest_path, "w", encoding="utf-8") as f:
        json.dump(manifest, f, indent=2)

    swap_path = project_root / args.swap_output
    swap_path.parent.mkdir(parents=True, exist_ok=True)
    with open(swap_path, "w", encoding="utf-8") as f:
        json.dump(swap_table, f, indent=2)

    print()
    print(f"PA Manifest Generator — {generated_count} interfaces generated")
    print(f"  Source: {cls_source}")
    print(f"  Categories: {', '.join(sorted(requested_categories))}")
    print(f"  Manifest written to: {args.output}")
    print(f"  Swap table written to: {args.swap_output}")
    print(f"  Swap entries: {len(all_swaps)}")
    print(f"  Transform entries: {len(all_transforms)}")
    print()

    return 0


if __name__ == "__main__":
    sys.exit(main())
