"""
generate_wave_script.py — Generate a complete .ps1 migration wave script
========================================================================

Reads mc_type_classifications_original.json (or mc_type_mappings.json) and
generates a ready-to-run PowerShell migration wave script.

Usage:
    python tools/migration/generate_wave_script.py
    python tools/migration/generate_wave_script.py --sources noppes/npcs/controllers/QuestController.java
    python tools/migration/generate_wave_script.py --sources-file tools/migration/_wave_sources.txt
    python tools/migration/generate_wave_script.py --allow-paths "noppes/npcs/controllers/,noppes/npcs/quests/"
    python tools/migration/generate_wave_script.py --output tools/migration/_wave.ps1
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
    NAMING,
    EXISTING_MAPPINGS,
    get_interface_package,
    SERVICE_ROUTING,
)

STANDARD_FORBIDDEN_PREFIXES = [
    "net.minecraft.",
    "cpw.mods.",
    "net.minecraftforge.",
    "noppes.npcs.entity.",
    "noppes.npcs.client.",
    "noppes.npcs.mixin.",
    "kamkeel.npcs.network.",
    "kamkeel.npcs.entity.",
    "noppes.npcs.containers.",
    "noppes.npcs.scripted.",
    "noppes.npcs.janino.",
    "noppes.npcs.wrapper.",
    "noppes.npcs.blocks.",
    "noppes.npcs.items.",
    "noppes.npcs.ai.",
]

DEFAULT_STUBS = [
    {
        "pkg": "noppes.npcs",
        "name": "CustomNpcs",
        "type": "class",
        "body": '    public static java.io.File Dir = new java.io.File(".");\n    public static int DefaultInteractLine = 0;',
    },
    {
        "pkg": "noppes.npcs",
        "name": "EventHooks",
        "type": "class",
        "body": "    // STUB - event dispatch stays in mc1710",
    },
    {
        "pkg": "noppes.npcs",
        "name": "LogWriter",
        "type": "class",
        "body": "    public static void info(String msg) {}\n    public static void warn(String msg) {}\n    public static void error(String msg) {}\n    public static void error(String msg, Throwable e) {}",
    },
    {
        "pkg": "noppes.npcs.controllers",
        "name": "SyncController",
        "type": "class",
        "body": "    // STUB - sync stays in mc1710",
    },
]


def auto_detect_project_root():
    return SCRIPT_DIR.parent.parent


def _normalize_classifications(data):
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


def _build_all_swaps(types):
    """Build swap entries from EXISTING + INTERFACE categories."""
    swaps = []
    seen = set()
    for type_name, info in types.items():
        if type_name.startswith("_"):
            continue
        category = info.get("category", "")
        if category not in ("EXISTING", "INTERFACE"):
            continue
        pa_name = info.get("pa")
        if not pa_name:
            pa_name = NAMING.get(type_name, f"I{type_name}")
        fqn = info.get("fqn", "")
        pkg = info.get("package", get_interface_package(pa_name))
        new_fqn = f"{pkg}.{pa_name}"
        key = (type_name, fqn)
        if key not in seen:
            seen.add(key)
            swaps.append((type_name, pa_name, fqn, new_fqn))
    return swaps


def _build_all_transforms(types):
    """Build transform entries from TRANSFORM category + SERVICE static-transform patterns."""
    transforms = []
    for type_name, info in types.items():
        if type_name.startswith("_"):
            continue
        if info.get("category") != "TRANSFORM":
            continue
        for pat in info.get("patterns", []):
            if len(pat) >= 2:
                transforms.append((pat[0], pat[1]))
    return transforms


def _ps_array_literal(items, indent="    "):
    """Format a list of strings as a PowerShell @() array literal."""
    if not items:
        return "@()"
    lines = ["@("]
    for i, item in enumerate(items):
        comma = "," if i < len(items) - 1 else ""
        lines.append(f"{indent}    '{item}'{comma}")
    lines.append(f"{indent})")
    return "\n".join(lines)


def _ps_swap_array(swaps, indent="    "):
    """Format swap entries as a PowerShell @(@(...), @(...)) array."""
    if not swaps:
        return "@()"
    lines = ["@("]
    for i, (old, new, old_fqn, new_fqn) in enumerate(swaps):
        comma = "," if i < len(swaps) - 1 else ""
        lines.append(
            f"{indent}    @('{old}', '{new}', '{old_fqn}', '{new_fqn}'){comma}"
        )
    lines.append(f"{indent})")
    return "\n".join(lines)


def _ps_transform_array(transforms, indent="    "):
    """Format transform entries as a PowerShell @(@(...), @(...)) array."""
    if not transforms:
        return "@()"
    lines = ["@("]
    for i, (pattern, replacement) in enumerate(transforms):
        comma = "," if i < len(transforms) - 1 else ""
        lines.append(f"{indent}    @('{pattern}', '{replacement}'){comma}")
    lines.append(f"{indent})")
    return "\n".join(lines)


def _ps_stubs_array(stubs, indent="    "):
    """Format stubs as a PowerShell @(@{...}, @{...}) array."""
    if not stubs:
        return "@()"
    lines = ["@("]
    for i, stub in enumerate(stubs):
        comma = "," if i < len(stubs) - 1 else ""
        body_escaped = stub["body"].replace("'", "''")
        lines.append(
            f"{indent}    @{{ pkg='{stub['pkg']}'; name='{stub['name']}'; type='{stub['type']}';"
        )
        lines.append(f"{indent}       body='{body_escaped}' }}{comma}")
    lines.append(f"{indent})")
    return "\n".join(lines)


def generate_script(
    sources,
    allow_paths,
    allow_recurse_paths,
    mc_root,
    swaps,
    transforms,
    stubs,
    forbidden_prefixes,
    cls_source,
):
    now = datetime.now(timezone.utc).strftime("%Y-%m-%d")
    separator = "# " + "=" * 60

    sources_arr = _ps_array_literal(sources, "    ")
    allow_paths_arr = _ps_array_literal(allow_paths, "    ") if allow_paths else None
    allow_recurse_arr = (
        _ps_array_literal(allow_recurse_paths, "    ") if allow_recurse_paths else None
    )
    forbidden_arr = _ps_array_literal(forbidden_prefixes, "")
    swaps_arr = _ps_swap_array(swaps, "")
    transforms_arr = _ps_transform_array(transforms, "")
    stubs_arr = _ps_stubs_array(stubs, "")

    lines = []

    lines.append(separator)
    lines.append(f"# Wave Migration Script")
    lines.append(f"# Generated by generate_wave_script.py on {now}")
    lines.append(f"# Source: {cls_source}")
    lines.append(separator)
    lines.append("")
    lines.append(". (Join-Path $PSScriptRoot 'Copy-Recursive.ps1')")
    lines.append(". (Join-Path $PSScriptRoot 'Nuke-Imports.ps1')")
    lines.append(". (Join-Path $PSScriptRoot 'Symbol-Swap.ps1')")
    lines.append(". (Join-Path $PSScriptRoot 'Static-Transform.ps1')")
    lines.append(". (Join-Path $PSScriptRoot 'Generate-Stubs.ps1')")
    lines.append(". (Join-Path $PSScriptRoot 'Dedupe-Imports.ps1')")
    lines.append(". (Join-Path $PSScriptRoot 'Build-Report.ps1')")
    lines.append("")

    if sources:
        lines.append(separator)
        lines.append("# STEP 1: Copy files from mc1710 to core")
        lines.append(separator)
        copy_cmd = "Copy-Recursive `\n"
        copy_cmd += f"    -Sources {sources_arr} `\n"
        if allow_paths_arr:
            copy_cmd += f"    -AllowPaths {allow_paths_arr} `\n"
        if allow_recurse_arr:
            copy_cmd += f"    -AllowRecursePaths {allow_recurse_arr} `\n"
        copy_cmd += f"    -McRoot '{mc_root}'"
        lines.append(copy_cmd)
        lines.append("")

    lines.append(separator)
    lines.append("# STEP 2: Nuke MC imports")
    lines.append(separator)
    lines.append(
        f"Nuke-Imports -Directory 'core/src/main/java' -ForbiddenPrefixes {forbidden_arr}"
    )
    lines.append("")

    lines.append(separator)
    lines.append("# STEP 3: Symbol-Swap — replace MC types with PA abstractions")
    lines.append(separator)
    lines.append(f"Symbol-Swap -Directory 'core/src/main/java' -Swaps {swaps_arr}")
    lines.append("")

    if transforms:
        lines.append(separator)
        lines.append("# STEP 4: Static-Transform — regex rewrites")
        lines.append(separator)
        lines.append(
            f"Static-Transform -Directory 'core/src/main/java' -Transforms {transforms_arr}"
        )
        lines.append("")

    lines.append(separator)
    lines.append("# STEP 5: Generate stubs for missing dependencies")
    lines.append(separator)
    lines.append(f"Generate-Stubs -Stubs {stubs_arr}")
    lines.append("")

    lines.append(separator)
    lines.append("# STEP 6: Deduplicate imports")
    lines.append(separator)
    lines.append("Dedupe-Imports -Directory 'core/src/main/java'")
    lines.append("")

    lines.append(separator)
    lines.append("# STEP 7: Build and report")
    lines.append(separator)
    lines.append("Build-Report -GradleTask ':core:compileJava'")
    lines.append("")

    return "\n".join(lines)


def main():
    parser = argparse.ArgumentParser(
        description="Generate a complete .ps1 migration wave script"
    )
    parser.add_argument(
        "--mappings",
        help="Path to mc_type_mappings.json or classifications JSON",
    )
    parser.add_argument(
        "--sources",
        nargs="*",
        default=[],
        help="Java source files to migrate (relative to mc-root)",
    )
    parser.add_argument(
        "--sources-file",
        help="Text file with one source path per line",
    )
    parser.add_argument(
        "--allow-paths",
        help="Comma-separated AllowPaths prefixes",
    )
    parser.add_argument(
        "--allow-recurse-paths",
        help="Comma-separated AllowRecursePaths prefixes",
    )
    parser.add_argument(
        "--mc-root",
        default="src/main/java",
        help="MC source root (default: src/main/java)",
    )
    parser.add_argument(
        "--output",
        default="tools/migration/_wave.ps1",
        help="Output .ps1 file path (relative to project root)",
    )
    parser.add_argument(
        "--project-root",
        help="Project root (auto-detected if omitted)",
    )

    args = parser.parse_args()
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )

    types, cls_source = _load_classifications_data(project_root, args.mappings)

    sources = list(args.sources)
    if args.sources_file:
        sf = Path(args.sources_file)
        if not sf.is_absolute():
            sf = project_root / args.sources_file
        if sf.exists():
            with open(sf, "r", encoding="utf-8") as f:
                for line in f:
                    line = line.strip()
                    if line and not line.startswith("#"):
                        sources.append(line)

    allow_paths = None
    if args.allow_paths:
        allow_paths = [p.strip() for p in args.allow_paths.split(",") if p.strip()]

    allow_recurse_paths = None
    if args.allow_recurse_paths:
        allow_recurse_paths = [
            p.strip() for p in args.allow_recurse_paths.split(",") if p.strip()
        ]

    swaps = _build_all_swaps(types)
    transforms = _build_all_transforms(types)

    script_content = generate_script(
        sources=sources,
        allow_paths=allow_paths,
        allow_recurse_paths=allow_recurse_paths,
        mc_root=args.mc_root,
        swaps=swaps,
        transforms=transforms,
        stubs=DEFAULT_STUBS,
        forbidden_prefixes=STANDARD_FORBIDDEN_PREFIXES,
        cls_source=cls_source,
    )

    output_path = project_root / args.output
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, "w", encoding="utf-8", newline="\n") as f:
        f.write(script_content)

    print()
    print(f"Wave Script Generator")
    print(f"  Source: {cls_source}")
    print(f"  Sources: {len(sources)} files")
    print(f"  Swaps: {len(swaps)} entries")
    print(f"  Transforms: {len(transforms)} patterns")
    print(f"  Stubs: {len(DEFAULT_STUBS)} entries")
    print(f"  Written to: {args.output}")
    print()

    return 0


if __name__ == "__main__":
    sys.exit(main())
