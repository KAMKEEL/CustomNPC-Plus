#!/usr/bin/env python3
"""
pa_analyzer.py — PA Usage Analyzer for MC1710 Codebase

Analyzes which MC types in mc1710/ use which Platform-Abstraction (PA) members.

Usage:
    python tools/migration/pa_analyzer.py                              # dry-run
    python tools/migration/pa_analyzer.py --execute                    # run analysis
    python tools/migration/pa_analyzer.py --execute --pa INbt --pa IPlayer  # filter
    python tools/migration/pa_analyzer.py --execute --json             # machine output
    python tools/migration/pa_analyzer.py --execute --verbose          # detailed
"""

import argparse
import json
import os
import sys
from datetime import datetime, timezone


DEFAULT_CLASSIFICATIONS_PATH = "tools/migration/classification_results.json"
DEFAULT_INDEX_PATH = "tools/migration/mc_usage_index.json"
DEFAULT_OUTPUT_PATH = "tools/migration/pa_usage_analysis.json"

NON_SUPPRESSED_CLASSIFICATIONS = {"EXISTING", "INTERFACE", "SERVICE", "AUTO"}

CONFIDENCE_HIGH = 90
CONFIDENCE_MEDIUM = 50


def detect_project_root(start=None):
    cur = os.path.abspath(start or os.path.dirname(os.path.abspath(__file__)))
    for _ in range(20):
        if os.path.isfile(os.path.join(cur, "settings.gradle")):
            return cur
        parent = os.path.dirname(cur)
        if parent == cur:
            break
        cur = parent
    return os.path.abspath(start or os.path.dirname(os.path.abspath(__file__)))


def load_json_file(project_root, rel_path, description):
    full_path = os.path.join(project_root, rel_path)
    if not os.path.isfile(full_path):
        raise FileNotFoundError(f"{description} not found: {full_path}")
    with open(full_path, "r", encoding="utf-8") as f:
        return json.load(f)


def extract_non_suppressed_pas(
    classifications_data, classification_filter=None, pa_filter=None
):
    types_dict = classifications_data.get("types", {})
    pa_map = {}

    for fqn, info in types_dict.items():
        classification = info.get("classification", "")
        pa_name = info.get("pa_name") or info.get("service_name")

        if not pa_name:
            continue
        if classification not in NON_SUPPRESSED_CLASSIFICATIONS:
            continue
        if classification_filter and classification not in classification_filter:
            continue
        if pa_filter and pa_name not in pa_filter:
            continue

        if pa_name not in pa_map:
            pa_map[pa_name] = {"mc_types": [], "classifications": set()}

        pa_map[pa_name]["mc_types"].append(
            {
                "fqn": fqn,
                "shortName": info.get("shortName", ""),
                "classification": classification,
                "import_count": info.get("import_count", 0),
            }
        )
        pa_map[pa_name]["classifications"].add(classification)

    for pa_name in pa_map:
        pa_map[pa_name]["classifications"] = sorted(pa_map[pa_name]["classifications"])
        pa_map[pa_name]["mc_types"].sort(key=lambda x: -x["import_count"])

    return pa_map


def _ensure_member(member_dict, name):
    if name not in member_dict:
        member_dict[name] = {"count": 0, "files": set()}
    return member_dict[name]


def _accumulate_members(member_dict, data_source, all_files, total_usage_list):
    for name, info in data_source.items():
        count = info.get("count", 0)
        files = info.get("files", [])
        entry = _ensure_member(member_dict, name)
        entry["count"] += count
        entry["files"].update(files)
        all_files.update(files)
        total_usage_list[0] += count


def _accumulate_usage(
    target_count_files, usage_data, usage_key, all_files, total_usage_list
):
    kind_entries = usage_data.get(usage_key, [])
    if isinstance(kind_entries, list):
        # Index format: array of {file, line} objects (constructors, casts, instanceof)
        for entry in kind_entries:
            f = entry.get("file", "") if isinstance(entry, dict) else ""
            if f:
                target_count_files["files"].add(f)
                all_files.add(f)
        target_count_files["count"] += len(kind_entries)
        total_usage_list[0] += len(kind_entries)
    elif isinstance(kind_entries, dict):
        # Fallback: {count, files} dict format (methodCalls, staticCalls, fieldAccesses)
        kind_count = kind_entries.get("count", 0)
        kind_files = kind_entries.get("files", [])
        target_count_files["count"] += kind_count
        target_count_files["files"].update(kind_files)
        all_files.update(kind_files)
        total_usage_list[0] += kind_count


def analyze_pa_usage(pa_map, index_data):
    index_types = index_data.get("types", {})
    results = {}

    for pa_name, pa_info in pa_map.items():
        methods = {}
        static_methods = {}
        fields = {}
        all_files = set()
        total_usage = [0]
        total_import_count = 0
        constructors = {"count": 0, "files": set()}
        casts = {"count": 0, "files": set()}
        instanceof_usage = {"count": 0, "files": set()}

        for mc_type in pa_info["mc_types"]:
            fqn = mc_type["fqn"]
            total_import_count += mc_type["import_count"]
            type_data = index_types.get(fqn, {})

            _accumulate_members(
                methods, type_data.get("methodCalls", {}), all_files, total_usage
            )
            _accumulate_members(
                static_methods, type_data.get("staticCalls", {}), all_files, total_usage
            )
            _accumulate_members(
                fields, type_data.get("fieldAccesses", {}), all_files, total_usage
            )

            usage = type_data.get("usage", {})
            _accumulate_usage(constructors, usage, "new", all_files, total_usage)
            _accumulate_usage(casts, usage, "cast", all_files, total_usage)
            _accumulate_usage(
                instanceof_usage, usage, "instanceof", all_files, total_usage
            )

        unique_members = len(methods) + len(static_methods) + len(fields)
        confidence = _compute_confidence(
            total_usage=total_usage[0],
            unique_members=unique_members,
            file_count=len(all_files),
            total_import_count=total_import_count,
        )

        results[pa_name] = {
            "mc_types": pa_info["mc_types"],
            "classifications": pa_info["classifications"],
            "members": {
                "methods": _finalize_member_dict(methods),
                "static_methods": _finalize_member_dict(static_methods),
                "fields": _finalize_member_dict(fields),
            },
            "source_files": sorted(all_files),
            "usage_count": total_usage[0],
            "total_import_count": total_import_count,
            "unique_members": unique_members,
            "file_count": len(all_files),
            "constructors": _finalize_usage_entry(constructors),
            "casts": _finalize_usage_entry(casts),
            "instanceof": _finalize_usage_entry(instanceof_usage),
            "confidence": round(confidence, 1),
        }

    return results


def _finalize_member_dict(member_dict):
    result = {}
    for name, info in sorted(member_dict.items(), key=lambda x: -x[1]["count"]):
        result[name] = {
            "count": info["count"],
            "file_count": len(info["files"]),
            "files": sorted(info["files"]),
        }
    return result


def _finalize_usage_entry(entry):
    return {
        "count": entry["count"],
        "file_count": len(entry["files"]),
        "files": sorted(entry["files"]),
    }


def _compute_confidence(total_usage, unique_members, file_count, total_import_count):
    """Weighted score from usage density, member coverage, and file spread."""
    if total_import_count == 0:
        return 0.0

    usage_density = min(total_usage / max(total_import_count, 1), 5.0) / 5.0
    member_score = min(unique_members / 20.0, 1.0)
    file_score = min(file_count / 50.0, 1.0)

    return min(usage_density * 40 + member_score * 35 + file_score * 25, 100.0)


def build_output(results, args):
    all_files = set()
    for r in results.values():
        all_files.update(r["source_files"])

    metadata = {
        "analysis_date": datetime.now(timezone.utc).isoformat(),
        "tool_version": "1.0.0",
        "classifications_source": args.classifications,
        "index_source": args.index,
        "total_PAs_scanned": len(results),
        "total_mc_types_covered": sum(len(r["mc_types"]) for r in results.values()),
        "total_source_files_touched": len(all_files),
        "confidence_thresholds": {
            "high": f">={CONFIDENCE_HIGH}%",
            "medium": f">={CONFIDENCE_MEDIUM}%",
            "low": f"<{CONFIDENCE_MEDIUM}%",
        },
        "filters_applied": {
            "classifications": args.classification or "all non-suppressed",
            "pa_names": args.pa or "all",
        },
    }

    sorted_results = dict(sorted(results.items()))

    high_conf = []
    medium_conf = []
    low_conf = []
    by_classification = {}
    unused_pas = []

    for pa_name, data in sorted_results.items():
        conf = data["confidence"]
        if conf >= CONFIDENCE_HIGH:
            high_conf.append(pa_name)
        elif conf >= CONFIDENCE_MEDIUM:
            medium_conf.append(pa_name)
        else:
            low_conf.append(pa_name)

        for cls in data["classifications"]:
            if cls not in by_classification:
                by_classification[cls] = []
            by_classification[cls].append(pa_name)

        if data["usage_count"] == 0:
            unused_pas.append(pa_name)

    top_by_usage = [
        {
            "pa_name": name,
            "usage_count": data["usage_count"],
            "file_count": data["file_count"],
        }
        for name, data in sorted(results.items(), key=lambda x: -x[1]["usage_count"])[
            :20
        ]
    ]

    summary = {
        "by_confidence": {"high": high_conf, "medium": medium_conf, "low": low_conf},
        "by_classification": by_classification,
        "top_by_usage": top_by_usage,
        "unused_pas": unused_pas,
    }

    return {"metadata": metadata, "summary": summary, "pa_usage": sorted_results}


def format_human_readable(output_data, verbose=False):
    lines = []
    meta = output_data["metadata"]
    summary = output_data["summary"]
    pa_usage = output_data["pa_usage"]

    lines.append("")
    lines.append("=" * 72)
    lines.append("PA USAGE ANALYSIS — MC1710 Member Usage by Platform-Abstraction")
    lines.append("=" * 72)
    lines.append(f"  Date:                {meta['analysis_date']}")
    lines.append(f"  PAs scanned:         {meta['total_PAs_scanned']}")
    lines.append(f"  MC types covered:    {meta['total_mc_types_covered']}")
    lines.append(f"  Source files touched: {meta['total_source_files_touched']}")
    lines.append("")

    high = summary["by_confidence"]["high"]
    med = summary["by_confidence"]["medium"]
    low = summary["by_confidence"]["low"]
    lines.append("  CONFIDENCE DISTRIBUTION:")
    lines.append(f"    High (>={CONFIDENCE_HIGH}%):   {len(high)} PAs")
    lines.append(f"    Medium (>={CONFIDENCE_MEDIUM}%): {len(med)} PAs")
    lines.append(f"    Low (<{CONFIDENCE_MEDIUM}%):    {len(low)} PAs")
    lines.append(f"    Unused (0 usage):  {len(summary['unused_pas'])} PAs")
    lines.append("")

    lines.append("  TOP 20 PAs BY USAGE:")
    for entry in summary["top_by_usage"]:
        lines.append(
            f"    {entry['pa_name']:35s}  usage={entry['usage_count']:6d}  files={entry['file_count']:4d}"
        )
    lines.append("")

    if verbose:
        lines.append("-" * 72)
        lines.append("DETAILED PA BREAKDOWN:")
        lines.append("-" * 72)

        for pa_name, data in pa_usage.items():
            lines.append("")
            lines.append(f"  PA: {pa_name}")
            lines.append(f"    Classifications: {', '.join(data['classifications'])}")
            lines.append(
                f"    MC Types: {', '.join(t['shortName'] for t in data['mc_types'])}"
            )
            lines.append(f"    Total Usage: {data['usage_count']}")
            lines.append(f"    Unique Members: {data['unique_members']}")
            lines.append(f"    Files: {data['file_count']}")
            lines.append(f"    Confidence: {data['confidence']}%")

            methods = data["members"]["methods"]
            if methods:
                lines.append(f"    Instance Methods ({len(methods)}):")
                for name, info in list(methods.items())[:10]:
                    lines.append(
                        f"      {name:40s}  {info['count']:5d} calls  {info['file_count']:3d} files"
                    )
                if len(methods) > 10:
                    lines.append(f"      ... and {len(methods) - 10} more")

            statics = data["members"]["static_methods"]
            if statics:
                lines.append(f"    Static Methods ({len(statics)}):")
                for name, info in list(statics.items())[:5]:
                    lines.append(
                        f"      {name:40s}  {info['count']:5d} calls  {info['file_count']:3d} files"
                    )
                if len(statics) > 5:
                    lines.append(f"      ... and {len(statics) - 5} more")

            fields_data = data["members"]["fields"]
            if fields_data:
                lines.append(f"    Fields ({len(fields_data)}):")
                for name, info in list(fields_data.items())[:10]:
                    lines.append(
                        f"      {name:40s}  {info['count']:5d} accesses  {info['file_count']:3d} files"
                    )
                if len(fields_data) > 10:
                    lines.append(f"      ... and {len(fields_data) - 10} more")

            if data["constructors"]["count"] > 0:
                lines.append(
                    f"    Constructors: {data['constructors']['count']} in {data['constructors']['file_count']} files"
                )
            if data["casts"]["count"] > 0:
                lines.append(
                    f"    Casts: {data['casts']['count']} in {data['casts']['file_count']} files"
                )
            if data["instanceof"]["count"] > 0:
                lines.append(
                    f"    instanceof: {data['instanceof']['count']} in {data['instanceof']['file_count']} files"
                )

    if summary["unused_pas"]:
        lines.append("")
        lines.append("  UNUSED PAs (0 member usage detected in index):")
        for i in range(0, len(summary["unused_pas"]), 6):
            chunk = summary["unused_pas"][i : i + 6]
            lines.append(f"    {', '.join(chunk)}")

    lines.append("")
    return "\n".join(lines)


def _print_dry_run(pa_map, args):
    print("")
    print("=" * 72)
    print("PA USAGE ANALYZER — DRY RUN (add --execute to run)")
    print("=" * 72)
    print(f"  Classifications source: {args.classifications}")
    print(f"  Index source:           {args.index}")
    print(f"  Output target:          {args.output}")
    print(f"  PA filter:              {args.pa or 'all'}")
    print(f"  Classification filter:  {args.classification or 'all non-suppressed'}")
    print(f"")
    print(f"  Non-suppressed PAs found: {len(pa_map)}")
    total_mc = sum(len(v["mc_types"]) for v in pa_map.values())
    print(f"  Total MC types to scan:   {total_mc}")
    print(f"")

    by_class = {}
    for pa_name, info in sorted(pa_map.items()):
        for cls in info["classifications"]:
            if cls not in by_class:
                by_class[cls] = []
            by_class[cls].append(pa_name)

    for cls in sorted(by_class):
        pas = by_class[cls]
        print(f"  {cls} ({len(pas)} PAs):")
        for pa_name in pas[:10]:
            mc_types = pa_map[pa_name]["mc_types"]
            mc_str = ", ".join(t["shortName"] for t in mc_types[:3])
            if len(mc_types) > 3:
                mc_str += f" +{len(mc_types) - 3} more"
            total_imports = sum(t["import_count"] for t in mc_types)
            print(f"    {pa_name:35s}  mc=[{mc_str}]  imports={total_imports}")
        if len(pas) > 10:
            print(f"    ... and {len(pas) - 10} more")
        print("")

    print("  Run with --execute to perform the full analysis.")
    print("")


def export_all_methods(
    index_data,
    output_path,
    classifications_data=None,
    classification_filter=None,
    as_json=False,
):
    index_types = index_data.get("types", {})

    allowed_fqns = None
    fqn_to_classification = {}

    if classifications_data:
        types_dict = classifications_data.get("types", {})
        allowed_fqns = set()
        for fqn, info in types_dict.items():
            cls = info.get("classification", "")
            if classification_filter and cls not in classification_filter:
                continue
            allowed_fqns.add(fqn)
            fqn_to_classification[fqn] = cls

    def _sort_members(members):
        return sorted(members, key=lambda x: (-x["count"], x["name"]))

    types_output = []
    total_method_usage = 0
    total_field_usage = 0
    total_static_usage = 0
    total_methods = 0
    total_fields = 0
    total_statics = 0
    per_classification_stats = {}

    for fqn, type_data in sorted(index_types.items()):
        if allowed_fqns is not None and fqn not in allowed_fqns:
            continue

        short_name = type_data.get("shortName", "")
        fqn_cls = fqn_to_classification.get(fqn, "UNKNOWN")

        methods = []
        for name, info in type_data.get("methodCalls", {}).items():
            count = info.get("count", 0)
            files = info.get("files", [])
            methods.append(
                {"name": name, "count": count, "file_count": len(files), "files": files}
            )
            total_method_usage += count

        fields = []
        for name, info in type_data.get("fieldAccesses", {}).items():
            count = info.get("count", 0)
            files = info.get("files", [])
            fields.append(
                {"name": name, "count": count, "file_count": len(files), "files": files}
            )
            total_field_usage += count

        statics = []
        for name, info in type_data.get("staticCalls", {}).items():
            count = info.get("count", 0)
            files = info.get("files", [])
            statics.append(
                {"name": name, "count": count, "file_count": len(files), "files": files}
            )
            total_static_usage += count

        methods = _sort_members(methods)
        fields = _sort_members(fields)
        statics = _sort_members(statics)

        total_methods += len(methods)
        total_fields += len(fields)
        total_statics += len(statics)
        type_usage = (
            sum(m["count"] for m in methods)
            + sum(f["count"] for f in fields)
            + sum(s["count"] for s in statics)
        )

        type_entry = {
            "fqn": fqn,
            "shortName": short_name,
            "classification": fqn_cls,
            "import_count": type_data.get("importCount", 0),
            "total_usage": type_usage,
            "methods": methods,
            "fields": fields,
            "static_methods": statics,
        }
        types_output.append(type_entry)

        if fqn_cls not in per_classification_stats:
            per_classification_stats[fqn_cls] = {
                "types": 0,
                "methods": 0,
                "fields": 0,
                "statics": 0,
                "usage": 0,
            }
        per_classification_stats[fqn_cls]["types"] += 1
        per_classification_stats[fqn_cls]["methods"] += len(methods)
        per_classification_stats[fqn_cls]["fields"] += len(fields)
        per_classification_stats[fqn_cls]["statics"] += len(statics)
        per_classification_stats[fqn_cls]["usage"] += type_usage

    filter_label = "ALL (unfiltered)"
    if classification_filter:
        filter_label = ", ".join(sorted(classification_filter))

    output = {
        "metadata": {
            "export_date": datetime.now(timezone.utc).isoformat(),
            "classification_filter": filter_label,
            "total_mc_types": len(types_output),
            "total_methods": total_methods,
            "total_fields": total_fields,
            "total_statics": total_statics,
            "total_unique_members": total_methods + total_fields + total_statics,
            "total_method_usage": total_method_usage,
            "total_field_usage": total_field_usage,
            "total_static_usage": total_static_usage,
            "total_usage": total_method_usage + total_field_usage + total_static_usage,
            "per_classification": {
                cls: stats for cls, stats in sorted(per_classification_stats.items())
            },
        },
        "types": types_output,
    }

    os.makedirs(os.path.dirname(os.path.abspath(output_path)), exist_ok=True)
    with open(output_path, "w", encoding="utf-8", newline="\n") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)

    meta = output["metadata"]
    if as_json:
        print(json.dumps(meta, indent=2))
    else:
        print("")
        print("=" * 72)
        print(f"ALL METHODS EXPORT — Classifications: {filter_label}")
        print("=" * 72)
        print(f"  MC types scanned:    {meta['total_mc_types']}")
        print(
            f"  Unique methods:      {meta['total_methods']:,}  ({meta['total_method_usage']:,} total calls)"
        )
        print(
            f"  Unique fields:       {meta['total_fields']:,}  ({meta['total_field_usage']:,} total accesses)"
        )
        print(
            f"  Unique statics:      {meta['total_statics']:,}  ({meta['total_static_usage']:,} total calls)"
        )
        print(f"  Total unique members:{meta['total_unique_members']:,}")
        print(f"  Total usage:         {meta['total_usage']:,}")

        if per_classification_stats:
            print("")
            print("  PER-CLASSIFICATION BREAKDOWN:")
            for cls in sorted(per_classification_stats):
                s = per_classification_stats[cls]
                print(
                    f"    {cls:12s}  {s['types']:3d} types  {s['methods']:4d} methods  {s['fields']:4d} fields  {s['statics']:4d} statics  {s['usage']:6d} usage"
                )

        top_types = sorted(types_output, key=lambda t: -t["total_usage"])[:20]
        print("")
        print("  TOP 20 TYPES BY USAGE:")
        for t in top_types:
            print(
                f"    {t['shortName']:30s}  [{t['classification']:9s}]  {t['total_usage']:6d} usage  {len(t['methods']):3d}m {len(t['fields']):3d}f {len(t['static_methods']):3d}s"
            )

        print("")
        print(f"  Output written to: {output_path}")
        print("")

    return output


def main():
    parser = argparse.ArgumentParser(
        description="PA Usage Analyzer — Maps PA member usage across mc1710/ source files.",
        formatter_class=argparse.RawDescriptionHelpFormatter,
        epilog="""
Examples:
  python tools/migration/pa_analyzer.py                                # dry-run
  python tools/migration/pa_analyzer.py --execute                      # run analysis
  python tools/migration/pa_analyzer.py --execute --pa INbt --pa IPlayer
  python tools/migration/pa_analyzer.py --execute --classification EXISTING INTERFACE
  python tools/migration/pa_analyzer.py --execute --classification SERVICE
  python tools/migration/pa_analyzer.py --execute --output /tmp/pa_usage.json
  python tools/migration/pa_analyzer.py --execute --verbose
  python tools/migration/pa_analyzer.py --execute --json
  python tools/migration/pa_analyzer.py --all-methods                  # export all MC members
  python tools/migration/pa_analyzer.py --all-methods --json           # JSON metadata only
""",
    )

    parser.add_argument(
        "--execute",
        action="store_true",
        help="Run analysis and write output (default: dry-run)",
    )
    parser.add_argument(
        "--output",
        default=None,
        help=f"Output JSON path (default: {DEFAULT_OUTPUT_PATH})",
    )
    parser.add_argument(
        "--classifications",
        default=DEFAULT_CLASSIFICATIONS_PATH,
        help=f"Classification results JSON (default: {DEFAULT_CLASSIFICATIONS_PATH})",
    )
    parser.add_argument(
        "--index",
        default=DEFAULT_INDEX_PATH,
        help=f"MC usage index JSON (default: {DEFAULT_INDEX_PATH})",
    )
    parser.add_argument(
        "--pa",
        action="append",
        default=None,
        help="Filter to specific PA name(s). Repeatable.",
    )
    parser.add_argument(
        "--classification",
        nargs="*",
        default=None,
        help="Filter by classification(s): EXISTING, INTERFACE, SERVICE, AUTO",
    )
    parser.add_argument(
        "--json", action="store_true", help="Machine-readable JSON summary to stdout"
    )
    parser.add_argument(
        "--verbose", action="store_true", help="Show detailed per-PA breakdown"
    )
    parser.add_argument(
        "--all-methods",
        action="store_true",
        help="Export MC method/field/static usage from the index, filtered by classification",
    )
    parser.add_argument(
        "--all-methods-classification",
        nargs="*",
        default=None,
        help="Classifications for --all-methods: EXISTING INTERFACE SERVICE AUTO TRANSFORM (default: all). Use ALL explicitly to skip loading classifications file.",
    )
    parser.add_argument(
        "--project-root",
        default=None,
        help="Project root override (auto-detected by default)",
    )

    args = parser.parse_args()

    project_root = (
        os.path.abspath(args.project_root)
        if args.project_root
        else detect_project_root()
    )
    if args.output is None:
        args.output = DEFAULT_OUTPUT_PATH

    try:
        if args.all_methods:
            index_data = load_json_file(project_root, args.index, "Index")
            all_methods_output = args.output.replace(".json", "_all_methods.json")
            if args.output == DEFAULT_OUTPUT_PATH:
                all_methods_output = "tools/migration/all_methods_export.json"
            full_output_path = os.path.join(project_root, all_methods_output)

            cls_data = None
            cls_filter = None
            if args.all_methods_classification and "ALL" in [
                c.upper() for c in args.all_methods_classification
            ]:
                pass
            else:
                cls_data = load_json_file(
                    project_root, args.classifications, "Classifications"
                )
                if args.all_methods_classification:
                    cls_filter = set(c.upper() for c in args.all_methods_classification)

            export_all_methods(
                index_data,
                full_output_path,
                classifications_data=cls_data,
                classification_filter=cls_filter,
                as_json=args.json,
            )
            return

        classifications_data = load_json_file(
            project_root, args.classifications, "Classifications"
        )
        classification_filter = (
            set(args.classification) if args.classification else None
        )
        pa_filter = set(args.pa) if args.pa else None

        pa_map = extract_non_suppressed_pas(
            classifications_data,
            classification_filter=classification_filter,
            pa_filter=pa_filter,
        )

        if not args.execute:
            _print_dry_run(pa_map, args)
            return

        index_data = load_json_file(project_root, args.index, "Index")
        results = analyze_pa_usage(pa_map, index_data)

        output_data = build_output(results, args)

        output_path = os.path.join(project_root, args.output)
        os.makedirs(os.path.dirname(output_path), exist_ok=True)
        with open(output_path, "w", encoding="utf-8", newline="\n") as f:
            json.dump(output_data, f, indent=2, ensure_ascii=False)

        if args.json:
            conf_data = output_data["summary"]["by_confidence"]
            summary_output = {
                "status": "success",
                "output_file": args.output,
                "total_pas": len(results),
                "total_mc_types": sum(len(r["mc_types"]) for r in results.values()),
                "total_files": output_data["metadata"]["total_source_files_touched"],
                "confidence_distribution": {
                    "high": len(conf_data["high"]),
                    "medium": len(conf_data["medium"]),
                    "low": len(conf_data["low"]),
                },
                "unused_pas": len(output_data["summary"]["unused_pas"]),
            }
            print(json.dumps(summary_output, indent=2))
        else:
            print(format_human_readable(output_data, verbose=args.verbose))
            print(f"  Output written to: {args.output}")
            print("")

    except FileNotFoundError as e:
        print(f"ERROR: {e}", file=sys.stderr)
        sys.exit(1)
    except json.JSONDecodeError as e:
        print(f"ERROR: Invalid JSON — {e}", file=sys.stderr)
        sys.exit(1)
    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        sys.exit(1)


if __name__ == "__main__":
    main()
