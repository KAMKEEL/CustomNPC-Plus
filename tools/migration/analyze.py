#!/usr/bin/env python3
"""
analyze.py — Unified Diagnostic Toolchain for Migration Analysis
====================================================================
Comprehensive subcommand interface for querying, analyzing, and validating
the CustomNPC-Plus migration workflow.

Subcommands:
  query-type          Query MC type from index (P0)
  query-file          Query file from index (P0)
  extract-signatures  Extract method signatures from Java files (P0)
  surface-miner       Find minimal interface surface for a type (P0)
  type-usage          Show all members of an MC type used within a directory scope (P0)
  diff-signatures     Compare signatures between two Java files (P3)
  migration-diff      Compare mc1710 original vs migrated core/ file — PASS/WARN/FAIL verdict (P3)
  coverage-report     Migration coverage analysis (P3)
  classify            Classify MC type for migration strategy (P0)
  classify-all        Classify ALL MC types in index and output as JSON (P0)
  generate-mappings   Generate mc_type_mappings.json from curated classifications

Usage:
  python analyze.py query-type EntityPlayer
  python analyze.py query-type EntityPlayer --fqn net.minecraft.entity.player.EntityPlayer
  python analyze.py query-file src/main/java/noppes/npcs/controllers/QuestController.java --readiness
  python analyze.py extract-signatures platform-api/src/main/java/noppes/npcs/api/INbt.java --json
  python analyze.py surface-miner NBTTagCompound --scope core/src/main/java
  python analyze.py surface-miner EntityPlayer --compare-existing platform-api/.../IPlayer.java
  python analyze.py type-usage EntityPlayer --scope noppes/npcs/controllers/
  python analyze.py diff-signatures --old file1.java --new file2.java
  python analyze.py migration-diff --old mc1710/FactionController.java --new core/FactionController.java
  python analyze.py coverage-report --detailed
  python analyze.py classify MinecraftServer
  python analyze.py classify MinecraftServer --surface
  python analyze.py classify --file PlayerDataController.java
  python analyze.py classify-all
  python analyze.py classify-all --output /tmp/classes.json
  python analyze.py classify-all --json
"""

import sys
import re
import json
import difflib
import argparse
from pathlib import Path
from collections import defaultdict
from datetime import datetime, timezone

MC_PREFIXES = ("net.minecraft.", "cpw.mods.", "net.minecraftforge.")

from pa_rules import EXISTING_MAPPINGS as KNOWN_PA
from pa_rules import classify_type as _classify_type, get_pa_name, get_service_target

# ============================================================
# Utilities
# ============================================================


def auto_detect_project_root():
    """Auto-detect project root from script location (tools/migration/ -> root)."""
    script_dir = Path(__file__).resolve().parent
    return script_dir.parent.parent


def load_index(project_root, index_path="tools/migration/mc_usage_index.json"):
    """Load the MC usage index JSON. Raises FileNotFoundError with clear message."""
    index_file = Path(project_root) / index_path
    if not index_file.exists():
        raise FileNotFoundError(
            f"Index not found: {index_file}\n"
            f"Run: python tools/migration/scan_codebase.py"
        )
    with open(index_file, "r", encoding="utf-8") as f:
        return json.load(f)


def resolve_java_file(project_root, path_hint):
    """Resolve a Java file path — handles both relative and absolute paths."""
    p = Path(path_hint)
    if p.is_absolute() and p.exists():
        return p
    resolved = Path(project_root) / path_hint
    if resolved.exists():
        return resolved
    # Try normalizing slashes
    normalized = path_hint.replace("\\", "/")
    resolved = Path(project_root) / normalized
    if resolved.exists():
        return resolved
    return None


def setup_treesitter_parser():
    """Initialize tree-sitter parser with Java grammar. Returns (parser, Language) or (None, None)."""
    try:
        import tree_sitter_java as tsjava
        from tree_sitter import Language, Parser
    except ImportError:
        return None, None
    lang = Language(tsjava.language())
    parser = Parser(lang)
    return parser, lang


def _node_text(node, source_bytes):
    """Extract text from a tree-sitter node."""
    if node is None:
        return ""
    return source_bytes[node.start_byte : node.end_byte].decode(
        "utf-8", errors="replace"
    )


def extract_signatures_from_file(file_path, parser, public_only=False):
    """
    Extract all method/constructor signatures from a Java file using tree-sitter.

    Returns a list of dicts:
      {
        "name": str,
        "return_type": str,  (empty for constructors)
        "params": [{"type": str, "name": str}, ...],
        "modifiers": [str, ...],
        "annotations": [str, ...],
        "line": int,
        "kind": "method" | "constructor",
        "javadoc": str | None,
      }
    """
    source_bytes = Path(file_path).read_bytes()
    tree = parser.parse(source_bytes)
    signatures = []
    _walk_for_signatures(tree.root_node, source_bytes, signatures, public_only)
    return signatures


def _walk_for_signatures(node, source_bytes, signatures, public_only):
    """Walk AST collecting method/constructor declarations."""
    if node.type in ("method_declaration", "constructor_declaration"):
        sig = _extract_single_signature(node, source_bytes, public_only)
        if sig is not None:
            signatures.append(sig)
    for child in node.children:
        _walk_for_signatures(child, source_bytes, signatures, public_only)


_MODIFIER_KEYWORDS = frozenset(
    {
        "public",
        "private",
        "protected",
        "static",
        "final",
        "abstract",
        "synchronized",
        "native",
        "default",
        "strictfp",
    }
)


def _extract_single_signature(node, source_bytes, public_only):
    """Extract signature from a method_declaration or constructor_declaration node."""
    is_constructor = node.type == "constructor_declaration"

    # Collect modifiers and annotations
    modifiers = []
    annotations = []
    for child in node.children:
        if child.type == "modifiers":
            for mod_child in child.children:
                if mod_child.type in ("marker_annotation", "annotation"):
                    annotations.append(_node_text(mod_child, source_bytes))
                else:
                    txt = _node_text(mod_child, source_bytes).strip()
                    if txt in _MODIFIER_KEYWORDS:
                        modifiers.append(txt)

    # Filter by visibility
    if public_only:
        has_access = any(m in ("public", "private", "protected") for m in modifiers)
        if has_access and "public" not in modifiers:
            return None
        # In interfaces, no-access-modifier means public — allow through

    # Name
    name_node = node.child_by_field_name("name")
    name = _node_text(name_node, source_bytes) if name_node else "?"

    # Return type (not for constructors)
    return_type = ""
    if not is_constructor:
        type_node = node.child_by_field_name("type")
        return_type = _node_text(type_node, source_bytes) if type_node else "void"

    # Parameters
    params = []
    params_node = node.child_by_field_name("parameters")
    if params_node:
        for param_child in params_node.children:
            if param_child.type in ("formal_parameter", "spread_parameter"):
                p_type_node = param_child.child_by_field_name("type")
                p_name_node = param_child.child_by_field_name("name")
                p_type = _node_text(p_type_node, source_bytes) if p_type_node else "?"

                # Handle varargs
                if param_child.type == "spread_parameter":
                    p_type += "..."

                p_name = _node_text(p_name_node, source_bytes) if p_name_node else "?"

                # Handle array dimensions on name
                dims_node = param_child.child_by_field_name("dimensions")
                if dims_node:
                    p_type += _node_text(dims_node, source_bytes)

                params.append({"type": p_type, "name": p_name})

    # Javadoc — look for block_comment immediately before this node
    javadoc = None
    prev = node.prev_named_sibling
    if prev and prev.type == "block_comment":
        comment_text = _node_text(prev, source_bytes)
        if comment_text.startswith("/**"):
            javadoc = comment_text

    line = node.start_point[0] + 1

    return {
        "name": name,
        "return_type": return_type,
        "params": params,
        "modifiers": modifiers,
        "annotations": annotations,
        "line": line,
        "kind": "constructor" if is_constructor else "method",
        "javadoc": javadoc,
    }


def format_signature(sig):
    """Format a signature dict into a human-readable one-liner."""
    parts = []
    if sig["annotations"]:
        parts.append(" ".join(sig["annotations"]))
    if sig["modifiers"]:
        parts.append(" ".join(sig["modifiers"]))
    if sig["return_type"]:
        parts.append(sig["return_type"])
    param_str = ", ".join(f"{p['type']} {p['name']}" for p in sig["params"])
    parts.append(f"{sig['name']}({param_str})")
    return " ".join(parts)


def signature_key(sig):
    """Create a comparison key for a signature (name + param types, ignoring param names)."""
    param_types = tuple(p["type"] for p in sig["params"])
    return (sig["name"], param_types)


# ============================================================
# SUBCOMMAND: query-type (P0)
# ============================================================


def cmd_query_type(args):
    """Query MC type from index — imports, method calls, field accesses, hierarchy, PA status."""
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    index = load_index(project_root, args.index)

    # Find the type by FQN or short name
    type_key = _find_type_key(index, args.type, args.fqn)
    if type_key is None:
        print(
            f"ERROR: Type '{args.fqn or args.type}' not found in index", file=sys.stderr
        )
        return 1

    ti = index["types"][type_key]

    if args.json_output:
        json.dump({"fqn": type_key, **ti}, sys.stdout, indent=2)
        print()
        return 0

    # Human-readable output
    print()
    print("=" * 80)
    print(f"TYPE: {ti['shortName']} ({type_key})")
    print("=" * 80)
    print(f"  Package:        {ti['package']}")
    print(f"  PA Abstraction: {ti.get('paAbstraction') or 'NONE'}")
    print(f"  Imported by:    {ti['importCount']} files")
    print()

    # Method calls
    if ti.get("methodCalls") and not args.field_accesses_only:
        sorted_methods = sorted(
            ti["methodCalls"].items(), key=lambda x: x[1]["count"], reverse=True
        )
        limit = args.top if args.top else len(sorted_methods)
        print(f"  METHOD CALLS ({len(ti['methodCalls'])} unique methods):")
        print(f"  {'Method':<40} {'Calls':>6} {'Files':>6}")
        print(f"  {'-' * 40} {'-' * 6} {'-' * 6}")
        for method, data in sorted_methods[:limit]:
            print(f"  {method:<40} {data['count']:>6} {len(data['files']):>6}")
            if args.show_files:
                for f in data["files"][:3]:
                    print(f"    - {f}")
                if len(data["files"]) > 3:
                    print(f"    ... and {len(data['files']) - 3} more")
        print()

    # Static calls
    if ti.get("staticCalls") and not args.field_accesses_only:
        sorted_statics = sorted(
            ti["staticCalls"].items(), key=lambda x: x[1]["count"], reverse=True
        )
        limit = args.top if args.top else len(sorted_statics)
        print(f"  STATIC CALLS ({len(ti['staticCalls'])} unique methods):")
        print(f"  {'Method':<40} {'Calls':>6} {'Files':>6}")
        print(f"  {'-' * 40} {'-' * 6} {'-' * 6}")
        for method, data in sorted_statics[:limit]:
            print(f"  {method:<40} {data['count']:>6} {len(data['files']):>6}")
        print()

    # Field accesses
    if ti.get("fieldAccesses") and not args.methods_only:
        sorted_fields = sorted(
            ti["fieldAccesses"].items(), key=lambda x: x[1]["count"], reverse=True
        )
        limit = args.top if args.top else len(sorted_fields)
        print(f"  FIELD ACCESSES ({len(ti['fieldAccesses'])} unique fields):")
        print(f"  {'Field':<40} {'Accesses':>8} {'Files':>6}")
        print(f"  {'-' * 40} {'-' * 8} {'-' * 6}")
        for field, data in sorted_fields[:limit]:
            print(f"  {field:<40} {data['count']:>8} {len(data['files']):>6}")
        print()

    # Hierarchy
    extends = index["hierarchy"]["extends"].get(type_key, [])
    implements = index["hierarchy"]["implements"].get(type_key, [])

    if extends:
        print(f"  EXTENDED BY ({len(extends)} classes):")
        for entry in extends[:10]:
            print(f"    {entry['class']:<40} in {entry['file']}")
        if len(extends) > 10:
            print(f"    ... and {len(extends) - 10} more")
        print()

    if implements:
        print(f"  IMPLEMENTED BY ({len(implements)} classes):")
        for entry in implements[:10]:
            print(f"    {entry['class']:<40} in {entry['file']}")
        if len(implements) > 10:
            print(f"    ... and {len(implements) - 10} more")
        print()

    # Usage summary
    usage = ti.get("usage", {})
    print("  USAGE SUMMARY:")
    print(f"    Constructors: {len(usage.get('constructors', []))}")
    print(f"    Casts:        {len(usage.get('casts', []))}")
    print(f"    instanceof:   {len(usage.get('instanceof', []))}")
    print()

    return 0


def _find_type_key(index, short_name, fqn=None):
    """Find type key in index by FQN or short name."""
    if fqn:
        return fqn if fqn in index["types"] else None
    # Search by short name — exact suffix match
    for key in index["types"]:
        if key.endswith("." + short_name) or key == short_name:
            return key
    return None


def add_query_type_args(subparsers):
    p = subparsers.add_parser("query-type", help="Query MC type from index (P0)")
    p.add_argument("type", help="Type simple name (e.g. EntityPlayer)")
    p.add_argument(
        "--fqn", help="Exact fully-qualified name (overrides simple name search)"
    )
    p.add_argument(
        "--methods-only", action="store_true", help="Show method/static calls only"
    )
    p.add_argument(
        "--field-accesses-only", action="store_true", help="Show field accesses only"
    )
    p.add_argument(
        "--show-files", action="store_true", help="Show file list for each method"
    )
    p.add_argument(
        "--top", type=int, default=0, help="Limit output to top N entries (0=all)"
    )
    p.add_argument(
        "--json", dest="json_output", action="store_true", help="Output as JSON"
    )
    p.add_argument(
        "--index",
        default="tools/migration/mc_usage_index.json",
        help="Path to index JSON (relative to project root)",
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_query_type)


# ============================================================
# SUBCOMMAND: query-file (P0)
# ============================================================


def cmd_query_file(args):
    """Query file from index — MC types used, readiness score, blocking items."""
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    index = load_index(project_root, args.index)

    # Normalize and find file
    file_hint = args.file.replace("\\", "/")
    file_key = _find_file_key(index, file_hint)

    if file_key is None:
        print(f"ERROR: File '{args.file}' not found in index", file=sys.stderr)
        # Show closest matches
        basename = file_hint.split("/")[-1]
        candidates = [f for f in index["files"] if basename in f]
        if candidates:
            print("Did you mean:", file=sys.stderr)
            for c in candidates[:5]:
                print(f"  {c}", file=sys.stderr)
        return 1

    fd = index["files"][file_key]

    if args.json_output:
        output = {"file": file_key, **fd}
        if args.readiness:
            output["readiness"] = _calc_readiness(fd)
        json.dump(output, sys.stdout, indent=2)
        print()
        return 0

    # Human-readable
    print()
    print("=" * 80)
    print(f"FILE: {file_key}")
    print("=" * 80)
    print(f"  MC Import Count: {fd['mcImportCount']}")
    print(f"  MC Types:        {', '.join(fd['mcTypes'][:10])}")
    if len(fd["mcTypes"]) > 10:
        print(f"                   ... and {len(fd['mcTypes']) - 10} more")
    print()

    # Readiness score
    if args.readiness:
        readiness = _calc_readiness(fd)
        print(f"  MIGRATION READINESS: {readiness['score']:.1f}%")
        print(f"    Abstracted:  {readiness['abstracted']}/{readiness['total']}")
        if readiness["blocking"]:
            print(
                f"    Blocking:    {', '.join(t.rsplit('.', 1)[-1] for t in readiness['blocking'][:5])}"
            )
            if len(readiness["blocking"]) > 5:
                print(f"                 ... and {len(readiness['blocking']) - 5} more")
        print()

    # Detailed type listing
    print("  MC TYPES USED:")
    print(f"  {'Type':<35} {'PA Mapping'}")
    print(f"  {'-' * 35} {'-' * 30}")
    for fqn in sorted(fd["mcFqns"]):
        short = fqn.rsplit(".", 1)[-1]
        pa = KNOWN_PA.get(fqn)
        pa_str = f"-> {pa}" if pa else "(NOT ABSTRACTED)"
        print(f"  {short:<35} {pa_str}")
    print()

    return 0


def _find_file_key(index, file_hint):
    """Find file key in index by exact or suffix match."""
    if file_hint in index["files"]:
        return file_hint
    # Exact suffix match
    for fpath in index["files"]:
        if fpath.endswith(file_hint) or file_hint.endswith(fpath):
            return fpath
    # Partial match from end (at least filename + one directory)
    hint_parts = file_hint.replace("\\", "/").split("/")
    for fpath in index["files"]:
        fpath_parts = fpath.split("/")
        match_len = 0
        for hp, fp in zip(reversed(hint_parts), reversed(fpath_parts)):
            if hp == fp:
                match_len += 1
            else:
                break
        if match_len >= 2:
            return fpath
    return None


def _calc_readiness(fd):
    """Calculate migration readiness score for a file."""
    mc_fqns = fd.get("mcFqns", [])
    if not mc_fqns:
        return {"score": 100.0, "abstracted": 0, "total": 0, "blocking": []}
    abstracted = sum(1 for fqn in mc_fqns if KNOWN_PA.get(fqn))
    blocking = [fqn for fqn in mc_fqns if not KNOWN_PA.get(fqn)]
    score = abstracted / len(mc_fqns) * 100
    return {
        "score": score,
        "abstracted": abstracted,
        "total": len(mc_fqns),
        "blocking": blocking,
    }


def add_query_file_args(subparsers):
    p = subparsers.add_parser("query-file", help="Query file from index (P0)")
    p.add_argument("file", help="File path (relative or absolute)")
    p.add_argument(
        "--readiness", action="store_true", help="Show migration readiness score"
    )
    p.add_argument(
        "--json", dest="json_output", action="store_true", help="Output as JSON"
    )
    p.add_argument(
        "--index",
        default="tools/migration/mc_usage_index.json",
        help="Path to index JSON",
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_query_file)


# ============================================================
# SUBCOMMAND: extract-signatures (P0)
# ============================================================


def cmd_extract_signatures(args):
    """Extract full method signatures from a Java file using tree-sitter."""
    parser, lang = setup_treesitter_parser()
    if parser is None:
        print("ERROR: tree-sitter-java not installed", file=sys.stderr)
        print("Run: pip install tree-sitter tree-sitter-java", file=sys.stderr)
        return 1

    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    java_file = resolve_java_file(project_root, args.file)
    if java_file is None or not java_file.exists():
        print(f"ERROR: File not found: {args.file}", file=sys.stderr)
        return 1

    signatures = extract_signatures_from_file(
        java_file, parser, public_only=args.public_only
    )

    if args.json_output:
        json.dump(
            {
                "file": str(java_file),
                "total_methods": len(signatures),
                "signatures": signatures,
            },
            sys.stdout,
            indent=2,
        )
        print()
        return 0

    # Human-readable
    rel = java_file.name
    print()
    print("=" * 80)
    print(f"SIGNATURES: {rel}")
    print("=" * 80)
    print(f"  Total methods: {len(signatures)}")
    print()

    if not signatures:
        print("  (no methods found)")
        return 0

    for sig in signatures:
        line_str = f"L{sig['line']}"
        kind_str = f"[{sig['kind']}]" if sig["kind"] == "constructor" else ""
        formatted = format_signature(sig)
        print(f"  {line_str:>5}  {kind_str:<14} {formatted}")
    print()

    return 0


def add_extract_signatures_args(subparsers):
    p = subparsers.add_parser(
        "extract-signatures", help="Extract method signatures from Java files (P0)"
    )
    p.add_argument("file", help="Path to Java file")
    p.add_argument(
        "--public-only", action="store_true", help="Show only public methods"
    )
    p.add_argument(
        "--json", dest="json_output", action="store_true", help="Output as JSON"
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_extract_signatures)


# ============================================================
# SUBCOMMAND: surface-miner (P0)
# ============================================================


def cmd_surface_miner(args):
    """
    Find minimal interface surface for a type based on actual codebase usage.
    Examines the index for all method calls and field accesses to the given type,
    producing the minimal set needed for a PA interface.
    """
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    index = load_index(project_root, args.index)

    # Find the type
    type_key = _find_type_key(index, args.type, args.fqn)
    if type_key is None:
        print(
            f"ERROR: Type '{args.fqn or args.type}' not found in index", file=sys.stderr
        )
        return 1

    ti = index["types"][type_key]

    # Scope filtering
    scope = args.scope.replace("\\", "/") if args.scope else None

    def _filter_files(files):
        if not scope:
            return files
        return [f for f in files if f.startswith(scope) or scope in f]

    # Gather methods
    methods = {}
    for method, data in ti.get("methodCalls", {}).items():
        files = _filter_files(data["files"])
        if files:
            methods[method] = {"count": len(files), "files": files, "kind": "instance"}

    for method, data in ti.get("staticCalls", {}).items():
        files = _filter_files(data["files"])
        if files:
            methods[method] = {"count": len(files), "files": files, "kind": "static"}

    # Gather fields
    fields = {}
    for field, data in ti.get("fieldAccesses", {}).items():
        files = _filter_files(data["files"])
        if files:
            fields[field] = {"count": len(files), "files": files}

    # Compare with existing PA interface
    existing_methods = set()
    if args.compare_existing:
        parser, lang = setup_treesitter_parser()
        if parser:
            pa_file = resolve_java_file(project_root, args.compare_existing)
            if pa_file and pa_file.exists():
                pa_sigs = extract_signatures_from_file(pa_file, parser)
                existing_methods = {s["name"] for s in pa_sigs}

    if args.json_output:
        output = {
            "type": type_key,
            "pa_abstraction": ti.get("paAbstraction"),
            "methods": methods,
            "fields": fields,
        }
        if existing_methods:
            output["existing_pa_methods"] = sorted(existing_methods)
            output["missing_from_pa"] = sorted(set(methods.keys()) - existing_methods)
            output["unused_in_pa"] = sorted(existing_methods - set(methods.keys()))
        json.dump(output, sys.stdout, indent=2, default=list)
        print()
        return 0

    # Human-readable
    print()
    print("=" * 80)
    print(f"MINIMAL SURFACE: {ti['shortName']} ({type_key})")
    print("=" * 80)
    print(f"  PA Abstraction: {ti.get('paAbstraction') or 'NONE'}")
    scope_str = f" (scoped to {scope})" if scope else ""
    print(f"  Scope: all usage sites{scope_str}")
    print()

    # Methods
    sorted_methods = sorted(methods.items(), key=lambda x: x[1]["count"], reverse=True)
    print(f"  METHODS ({len(sorted_methods)} unique):")
    print(f"  {'Method':<40} {'Kind':<8} {'Usage':>6}")
    print(f"  {'-' * 40} {'-' * 8} {'-' * 6}")
    for method, data in sorted_methods:
        marker = ""
        if existing_methods:
            marker = " [PA]" if method in existing_methods else " [MISSING]"
        print(f"  {method:<40} {data['kind']:<8} {data['count']:>6}{marker}")
    print()

    # Fields
    if fields:
        sorted_fields = sorted(
            fields.items(), key=lambda x: x[1]["count"], reverse=True
        )
        print(f"  FIELDS ({len(sorted_fields)} unique):")
        print(f"  {'Field':<40} {'Usage':>6}")
        print(f"  {'-' * 40} {'-' * 6}")
        for field, data in sorted_fields:
            print(f"  {field:<40} {data['count']:>6}")
        print()

    # Comparison summary
    if existing_methods:
        used_methods = set(methods.keys())
        missing = used_methods - existing_methods
        unused = existing_methods - used_methods
        covered = used_methods & existing_methods

        print(f"  COMPARISON WITH EXISTING PA INTERFACE:")
        print(f"    Covered by PA:     {len(covered)}/{len(used_methods)} methods")
        print(f"    Missing from PA:   {len(missing)}")
        if missing:
            for m in sorted(missing):
                print(f"      + {m}")
        print(f"    Unused in PA:      {len(unused)}")
        if unused:
            for m in sorted(unused):
                print(f"      - {m}")
        print()

    return 0


def add_surface_miner_args(subparsers):
    p = subparsers.add_parser(
        "surface-miner", help="Find minimal interface surface for a type (P0)"
    )
    p.add_argument("type", help="MC type simple name (e.g. NBTTagCompound)")
    p.add_argument("--fqn", help="Exact FQN")
    p.add_argument(
        "--scope",
        help="Limit analysis to files under this path prefix (e.g. core/src/main/java)",
    )
    p.add_argument("--compare-existing", help="Compare with existing PA interface file")
    p.add_argument(
        "--json", dest="json_output", action="store_true", help="Output as JSON"
    )
    p.add_argument(
        "--index",
        default="tools/migration/mc_usage_index.json",
        help="Path to index JSON",
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_surface_miner)


# ============================================================
# SUBCOMMAND: type-usage (P0)
# ============================================================


def cmd_type_usage(args):
    """Show ALL members of an MC type used within files under a given directory scope."""
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    index = load_index(project_root, args.index)

    # Find the type
    type_key = _find_type_key(index, args.type, args.fqn)
    if type_key is None:
        print(
            f"ERROR: Type '{args.fqn or args.type}' not found in index", file=sys.stderr
        )
        return 1

    ti = index["types"][type_key]

    # Scope filtering
    scope = args.scope.replace("\\", "/") if args.scope else None

    def _filter_files(files):
        if not scope:
            return files
        return [f for f in files if f.startswith(scope) or scope in f]

    def _filter_usage_entries(entries):
        """Filter usage entries (list of {file, line} dicts) by scope, return file list."""
        if not entries:
            return []
        files = [e["file"] for e in entries if isinstance(e, dict) and "file" in e]
        if scope:
            files = [f for f in files if f.startswith(scope) or scope in f]
        return files

    # Gather method calls (scoped)
    method_calls = {}
    for method, data in ti.get("methodCalls", {}).items():
        files = _filter_files(data["files"])
        if files:
            method_calls[method] = {"count": len(files), "files": files}

    # Gather static calls (scoped)
    static_calls = {}
    for method, data in ti.get("staticCalls", {}).items():
        files = _filter_files(data["files"])
        if files:
            static_calls[method] = {"count": len(files), "files": files}

    # Gather field accesses (scoped)
    field_accesses = {}
    for field, data in ti.get("fieldAccesses", {}).items():
        files = _filter_files(data["files"])
        if files:
            field_accesses[field] = {"count": len(files), "files": files}

    # Gather constructors, casts, instanceof (scoped)
    usage = ti.get("usage", {})
    constructor_files = _filter_usage_entries(usage.get("constructors", []))
    cast_files = _filter_usage_entries(usage.get("casts", []))
    instanceof_files = _filter_usage_entries(usage.get("instanceof", []))

    # Count occurrences per file for casts/instanceof/constructors
    def _count_per_file(file_list):
        counts = defaultdict(int)
        for f in file_list:
            counts[f] += 1
        return counts

    constructor_counts = _count_per_file(constructor_files)
    cast_counts = _count_per_file(cast_files)
    instanceof_counts = _count_per_file(instanceof_files)

    # Compute total files in scope
    all_scope_files = set()
    for data in method_calls.values():
        all_scope_files.update(data["files"])
    for data in static_calls.values():
        all_scope_files.update(data["files"])
    for data in field_accesses.values():
        all_scope_files.update(data["files"])
    all_scope_files.update(constructor_counts.keys())
    all_scope_files.update(cast_counts.keys())
    all_scope_files.update(instanceof_counts.keys())

    total_files_in_scope = len(all_scope_files)

    # Count total files globally for this type
    all_global_files = set()
    for data in ti.get("methodCalls", {}).values():
        all_global_files.update(data["files"])
    for data in ti.get("staticCalls", {}).values():
        all_global_files.update(data["files"])
    for data in ti.get("fieldAccesses", {}).values():
        all_global_files.update(data["files"])
    for e in usage.get("constructors", []):
        if isinstance(e, dict) and "file" in e:
            all_global_files.add(e["file"])
    for e in usage.get("casts", []):
        if isinstance(e, dict) and "file" in e:
            all_global_files.add(e["file"])
    for e in usage.get("instanceof", []):
        if isinstance(e, dict) and "file" in e:
            all_global_files.add(e["file"])
    # Also include importedBy if available
    imported_by = ti.get("importedBy", [])
    if imported_by:
        all_global_files.update(imported_by)
    total_files_global = (
        len(all_global_files) if all_global_files else ti.get("importCount", 0)
    )

    # Apply --top limit
    top = args.top if args.top else 0

    if args.json_output:
        output = {
            "type": type_key,
            "shortName": ti["shortName"],
            "paAbstraction": ti.get("paAbstraction"),
            "scope": scope or "(all)",
            "totalFilesInScope": total_files_in_scope,
            "totalFilesGlobal": total_files_global,
            "methodCalls": method_calls,
            "staticCalls": static_calls,
            "fieldAccesses": field_accesses,
            "constructors": {
                "count": len(constructor_files),
                "files": sorted(constructor_counts.keys()),
            },
            "casts": {
                "count": len(cast_files),
                "files": sorted(cast_counts.keys()),
            },
            "instanceof": {
                "count": len(instanceof_files),
                "files": sorted(instanceof_counts.keys()),
            },
        }
        json.dump(output, sys.stdout, indent=2)
        print()
        return 0

    # Human-readable output
    scope_label = scope if scope else "(all files)"
    print()
    print("=" * 80)
    print(f"TYPE USAGE: {ti['shortName']} in {scope_label}")
    print("=" * 80)
    print(f"  PA Abstraction: {ti.get('paAbstraction') or 'NONE'}")
    print(f"  Files in scope:  {total_files_in_scope} (of {total_files_global} total)")
    print()

    # Method calls
    total_method_calls = sum(d["count"] for d in method_calls.values())
    sorted_methods = sorted(
        method_calls.items(), key=lambda x: x[1]["count"], reverse=True
    )
    limit_methods = sorted_methods[:top] if top else sorted_methods
    print(
        f"  METHOD CALLS ({len(method_calls)} unique, {total_method_calls} total calls):"
    )
    if limit_methods:
        print(f"  {'Method':<40} {'Calls':>5} {'Files':>5}")
        print(f"  {'-' * 40} {'-' * 5} {'-' * 5}")
        for method, data in limit_methods:
            print(f"  {method:<40} {data['count']:>5} {len(data['files']):>5}")
            for f in data["files"][:5]:
                print(f"    - {f}")
            if len(data["files"]) > 5:
                print(f"    ... and {len(data['files']) - 5} more")
    else:
        print("  (none)")
    print()

    # Static calls
    total_static_calls = sum(d["count"] for d in static_calls.values())
    sorted_statics = sorted(
        static_calls.items(), key=lambda x: x[1]["count"], reverse=True
    )
    limit_statics = sorted_statics[:top] if top else sorted_statics
    if limit_statics:
        print(
            f"  STATIC CALLS ({len(static_calls)} unique, {total_static_calls} total calls):"
        )
        print(f"  {'Method':<40} {'Calls':>5} {'Files':>5}")
        print(f"  {'-' * 40} {'-' * 5} {'-' * 5}")
        for method, data in limit_statics:
            print(f"  {method:<40} {data['count']:>5} {len(data['files']):>5}")
            for f in data["files"][:5]:
                print(f"    - {f}")
            if len(data["files"]) > 5:
                print(f"    ... and {len(data['files']) - 5} more")
        print()
    else:
        print("  STATIC CALLS: (none)")
        print()

    # Field accesses
    total_field_accesses = sum(d["count"] for d in field_accesses.values())
    sorted_fields = sorted(
        field_accesses.items(), key=lambda x: x[1]["count"], reverse=True
    )
    limit_fields = sorted_fields[:top] if top else sorted_fields
    if limit_fields:
        print(
            f"  FIELD ACCESSES ({len(field_accesses)} unique, {total_field_accesses} total accesses):"
        )
        print(f"  {'Field':<40} {'Accesses':>8} {'Files':>5}")
        print(f"  {'-' * 40} {'-' * 8} {'-' * 5}")
        for field, data in limit_fields:
            print(f"  {field:<40} {data['count']:>8} {len(data['files']):>5}")
            for f in data["files"][:5]:
                print(f"    - {f}")
            if len(data["files"]) > 5:
                print(f"    ... and {len(data['files']) - 5} more")
        print()
    else:
        print("  FIELD ACCESSES: (none)")
        print()

    # Constructors
    print(
        f"  CONSTRUCTORS: {len(constructor_files)} occurrences"
        + (f" in {len(constructor_counts)} files" if constructor_files else "")
    )
    if constructor_counts:
        for f in sorted(constructor_counts.keys()):
            print(f"    - {f} ({constructor_counts[f]})")
    print()

    # Casts
    print(
        f"  CASTS: {len(cast_files)} occurrences"
        + (f" in {len(cast_counts)} files" if cast_files else "")
    )
    if cast_counts:
        for f in sorted(cast_counts.keys()):
            print(f"    - {f} ({cast_counts[f]})")
    print()

    # instanceof
    print(
        f"  INSTANCEOF: {len(instanceof_files)} occurrences"
        + (f" in {len(instanceof_counts)} files" if instanceof_files else "")
    )
    if instanceof_counts:
        for f in sorted(instanceof_counts.keys()):
            print(f"    - {f} ({instanceof_counts[f]})")
    print()

    return 0


def add_type_usage_args(subparsers):
    p = subparsers.add_parser(
        "type-usage",
        help="Show all members of an MC type used within a directory scope (P0)",
    )
    p.add_argument("type", help="MC type simple name (e.g. EntityPlayer)")
    p.add_argument("--fqn", help="Exact FQN override")
    p.add_argument(
        "--scope",
        help="Directory path prefix filter (e.g. noppes/npcs/controllers/)",
    )
    p.add_argument(
        "--top",
        type=int,
        default=0,
        help="Limit methods/fields to top N by count (0=all)",
    )
    p.add_argument(
        "--json",
        dest="json_output",
        action="store_true",
        help="Machine-readable JSON output",
    )
    p.add_argument(
        "--index",
        default="tools/migration/mc_usage_index.json",
        help="Path to index JSON (relative to project root)",
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_type_usage)


# ============================================================
# SUBCOMMAND: diff-signatures (P3)
# ============================================================


def cmd_diff_signatures(args):
    """Compare method signatures between two Java files."""
    parser, lang = setup_treesitter_parser()
    if parser is None:
        print("ERROR: tree-sitter-java not installed", file=sys.stderr)
        print("Run: pip install tree-sitter tree-sitter-java", file=sys.stderr)
        return 1

    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )

    old_file = resolve_java_file(project_root, args.old)
    new_file = resolve_java_file(project_root, args.new)

    if old_file is None or not old_file.exists():
        print(f"ERROR: Old file not found: {args.old}", file=sys.stderr)
        return 1
    if new_file is None or not new_file.exists():
        print(f"ERROR: New file not found: {args.new}", file=sys.stderr)
        return 1

    old_sigs = extract_signatures_from_file(old_file, parser)
    new_sigs = extract_signatures_from_file(new_file, parser)

    # Build maps by signature key
    old_map = {}
    for sig in old_sigs:
        key = signature_key(sig)
        old_map[key] = sig

    new_map = {}
    for sig in new_sigs:
        key = signature_key(sig)
        new_map[key] = sig

    old_keys = set(old_map.keys())
    new_keys = set(new_map.keys())

    added = new_keys - old_keys
    removed = old_keys - new_keys
    common = old_keys & new_keys

    # Detect changed signatures (same key, different return type or modifiers)
    changed = []
    for key in common:
        o = old_map[key]
        n = new_map[key]
        if o["return_type"] != n["return_type"] or o["modifiers"] != n["modifiers"]:
            changed.append((o, n))

    if args.json_output:
        output = {
            "old_file": str(old_file),
            "new_file": str(new_file),
            "added": [new_map[k] for k in sorted(added)],
            "removed": [old_map[k] for k in sorted(removed)],
            "changed": [{"old": o, "new": n} for o, n in changed],
            "unchanged": len(common) - len(changed),
        }
        json.dump(output, sys.stdout, indent=2)
        print()
        return 0

    # Human-readable
    print()
    print("=" * 80)
    print("SIGNATURE DIFF")
    print("=" * 80)
    print(f"  Old: {old_file.name} ({len(old_sigs)} methods)")
    print(f"  New: {new_file.name} ({len(new_sigs)} methods)")
    print()

    if added:
        print(f"  ADDED ({len(added)}):")
        for key in sorted(added):
            print(f"    + {format_signature(new_map[key])}")
        print()

    if removed:
        print(f"  REMOVED ({len(removed)}):")
        for key in sorted(removed):
            print(f"    - {format_signature(old_map[key])}")
        print()

    if changed:
        print(f"  CHANGED ({len(changed)}):")
        for old_sig, new_sig in changed:
            print(f"    OLD: {format_signature(old_sig)}")
            print(f"    NEW: {format_signature(new_sig)}")
            print()

    if not added and not removed and not changed:
        print("  No differences found.")
        print()

    print(
        f"  Summary: +{len(added)} added, -{len(removed)} removed, "
        f"~{len(changed)} changed, ={len(common) - len(changed)} unchanged"
    )
    print()

    return 0


def add_diff_signatures_args(subparsers):
    p = subparsers.add_parser(
        "diff-signatures", help="Compare method signatures between two Java files (P3)"
    )
    p.add_argument("--old", required=True, help="Path to old/original Java file")
    p.add_argument("--new", required=True, help="Path to new/updated Java file")
    p.add_argument(
        "--json", dest="json_output", action="store_true", help="Output as JSON"
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_diff_signatures)


# ============================================================
# SUBCOMMAND: coverage-report (P3)
# ============================================================


def cmd_coverage_report(args):
    """Migration coverage analysis — % types abstracted, breakdown by subsystem, blocking."""
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    index = load_index(project_root, args.index)

    summary = index.get("summary", {})
    types = index.get("types", {})
    files = index.get("files", {})

    # Basic stats
    total_files = summary.get("totalFiles", len(files))
    files_with_deps = summary.get("filesWithMcDeps", len(files))
    total_mc_imports = summary.get("totalMcImports", 0)
    unique_types = summary.get("uniqueMcTypes", len(types))

    # Abstraction coverage
    abstracted_types = []
    blocking_types = []

    for fqn, ti in types.items():
        if ti.get("paAbstraction"):
            abstracted_types.append((fqn, ti))
        else:
            blocking_types.append((fqn, ti))

    abstracted = len(abstracted_types)
    not_abstracted = len(blocking_types)
    total_types = abstracted + not_abstracted
    coverage_pct = (abstracted / total_types * 100) if total_types > 0 else 0

    # Import-weighted coverage
    abstracted_imports = sum(ti["importCount"] for _, ti in abstracted_types)
    total_import_count = sum(ti["importCount"] for ti in types.values())
    import_coverage_pct = (
        (abstracted_imports / total_import_count * 100) if total_import_count > 0 else 0
    )

    # Per-package breakdown
    by_package = defaultdict(lambda: {"types": 0, "abstracted": 0, "imports": 0})
    for fqn, ti in types.items():
        pkg = ti["package"]
        by_package[pkg]["types"] += 1
        by_package[pkg]["imports"] += ti["importCount"]
        if ti.get("paAbstraction"):
            by_package[pkg]["abstracted"] += 1

    if args.json_output:
        output = {
            "totalFiles": total_files,
            "filesWithMcDeps": files_with_deps,
            "totalMcImports": total_mc_imports,
            "uniqueMcTypes": unique_types,
            "abstractionCoverage": {
                "typeCoverage": f"{coverage_pct:.1f}%",
                "importCoverage": f"{import_coverage_pct:.1f}%",
                "abstracted": abstracted,
                "notAbstracted": not_abstracted,
            },
            "topBlockingTypes": [
                {
                    "fqn": fqn,
                    "shortName": ti["shortName"],
                    "importCount": ti["importCount"],
                }
                for fqn, ti in sorted(
                    blocking_types, key=lambda x: x[1]["importCount"], reverse=True
                )[:20]
            ],
            "byPackage": {
                pkg: data
                for pkg, data in sorted(
                    by_package.items(), key=lambda x: x[1]["imports"], reverse=True
                )
            },
        }
        json.dump(output, sys.stdout, indent=2)
        print()
        return 0

    # Human-readable
    print()
    print("=" * 80)
    print("MIGRATION COVERAGE REPORT")
    print("=" * 80)
    print()

    print("  OVERALL STATISTICS:")
    print(f"    Total files scanned:     {total_files:>6,}")
    print(f"    Files with MC deps:      {files_with_deps:>6,}")
    print(f"    Total MC imports:        {total_mc_imports:>6,}")
    print(f"    Unique MC types:         {unique_types:>6}")
    print()

    print("  ABSTRACTION COVERAGE:")
    print(f"    Type coverage:     {abstracted}/{total_types} ({coverage_pct:.1f}%)")
    print(
        f"    Import coverage:   {abstracted_imports}/{total_import_count} ({import_coverage_pct:.1f}%)"
    )
    print()

    # Top blocking types
    sorted_blocking = sorted(
        blocking_types, key=lambda x: x[1]["importCount"], reverse=True
    )
    limit = 20 if args.detailed else 10
    print(f"  TOP BLOCKING TYPES (no PA abstraction):")
    print(f"  {'#':>4}  {'Type':<40} {'Imports':>7}")
    print(f"  {'':4}  {'-' * 40} {'-' * 7}")
    for i, (fqn, ti) in enumerate(sorted_blocking[:limit]):
        print(f"  {i + 1:>4}. {ti['shortName']:<40} {ti['importCount']:>7}")
    if len(sorted_blocking) > limit:
        print(f"  {'':4}  ... and {len(sorted_blocking) - limit} more")
    print()

    # Package breakdown (detailed)
    if args.detailed:
        sorted_packages = sorted(
            by_package.items(), key=lambda x: x[1]["imports"], reverse=True
        )
        print(f"  PACKAGE BREAKDOWN:")
        print(f"  {'Package':<45} {'Types':>5} {'Abstr':>5} {'Imports':>7}")
        print(f"  {'-' * 45} {'-' * 5} {'-' * 5} {'-' * 7}")
        for pkg, data in sorted_packages[:20]:
            print(
                f"  {pkg:<45} {data['types']:>5} {data['abstracted']:>5} {data['imports']:>7}"
            )
        print()

    # Top files (detailed)
    if args.detailed:
        sorted_files = sorted(
            files.items(), key=lambda x: x[1]["mcImportCount"], reverse=True
        )
        print(f"  TOP FILES BY MC DEPENDENCY:")
        print(f"  {'File':<60} {'MC Types':>8}")
        print(f"  {'-' * 60} {'-' * 8}")
        for fpath, fd in sorted_files[:15]:
            print(f"  {fpath:<60} {fd['mcImportCount']:>8}")
        print()

    return 0


def add_coverage_report_args(subparsers):
    p = subparsers.add_parser(
        "coverage-report", help="Migration coverage analysis (P3)"
    )
    p.add_argument(
        "--detailed",
        action="store_true",
        help="Show detailed breakdown (packages, files)",
    )
    p.add_argument(
        "--json", dest="json_output", action="store_true", help="Output as JSON"
    )
    p.add_argument(
        "--index",
        default="tools/migration/mc_usage_index.json",
        help="Path to index JSON",
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_coverage_report)


# ============================================================
# SUBCOMMAND: classify (P0)
# ============================================================


def _gather_surface_data(index, type_key):
    """Build surface_data dict from index for auto-classification."""
    ti = index["types"][type_key]
    methods = []
    for method, data in ti.get("methodCalls", {}).items():
        methods.append({"name": method, "kind": "instance", "count": data["count"]})
    for method, data in ti.get("staticCalls", {}).items():
        methods.append({"name": method, "kind": "static", "count": data["count"]})
    return {"methods": methods}


def _format_classification(result, simple_name):
    """Format a classification result for human-readable output."""
    lines = []
    cls = result["classification"]
    lines.append(f"  {simple_name:<30} {cls}")
    reason = result["reason"].replace("\u2192", "->")
    lines.append(f"    Reason:  {reason}")
    if result.get("pa_name"):
        lines.append(f"    PA Name: {result['pa_name']}")
    if result.get("target"):
        lines.append(f"    Target:  {result['target']}")
    if result.get("service_name"):
        lines.append(f"    Service: {result['service_name']}")
    if result.get("transform_patterns"):
        lines.append(f"    Patterns ({len(result['transform_patterns'])}):")
        for pattern, replacement in result["transform_patterns"]:
            lines.append(f"      {pattern}")
            lines.append(f"        -> {replacement}")
    return "\n".join(lines)


def cmd_classify(args):
    """Classify MC types using pa_rules — single type or file mode."""
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )

    if args.file:
        return _classify_file_mode(args, project_root)
    if not args.type:
        print("ERROR: Provide a type name or --file", file=sys.stderr)
        return 1
    return _classify_single_mode(args, project_root)


def _classify_single_mode(args, project_root):
    """Classify a single MC type."""
    simple_name = args.type
    surface_data = None

    if args.surface or args.fqn:
        try:
            index = load_index(project_root, args.index)
            type_key = _find_type_key(index, simple_name, args.fqn)
            if type_key and args.surface:
                surface_data = _gather_surface_data(index, type_key)
        except FileNotFoundError:
            if args.surface:
                print(
                    "WARNING: Index not found, --surface data unavailable",
                    file=sys.stderr,
                )

    result = _classify_type(simple_name, surface_data)

    if args.json_output:
        output = {"type": simple_name, **result}
        json.dump(output, sys.stdout, indent=2)
        print()
        return 0

    print()
    print("=" * 60)
    print("CLASSIFY")
    print("=" * 60)
    print(_format_classification(result, simple_name))
    print()
    return 0


def _classify_file_mode(args, project_root):
    """Classify all MC types in a file — migration plan."""
    index = load_index(project_root, args.index)
    file_hint = args.file.replace("\\", "/")
    file_key = _find_file_key(index, file_hint)

    if file_key is None:
        print(f"ERROR: File '{args.file}' not found in index", file=sys.stderr)
        basename = file_hint.split("/")[-1]
        candidates = [f for f in index["files"] if basename in f]
        if candidates:
            print("Did you mean:", file=sys.stderr)
            for c in candidates[:5]:
                print(f"  {c}", file=sys.stderr)
        return 1

    fd = index["files"][file_key]
    mc_fqns = fd.get("mcFqns", [])

    if not mc_fqns:
        if args.json_output:
            json.dump({"file": file_key, "types": [], "plan": []}, sys.stdout, indent=2)
            print()
        else:
            print(f"\n  {file_key}: No MC types found.\n")
        return 0

    results = []
    for fqn in sorted(mc_fqns):
        simple = fqn.rsplit(".", 1)[-1]
        surface_data = None
        if args.surface:
            type_key = _find_type_key(index, simple)
            if type_key:
                surface_data = _gather_surface_data(index, type_key)
        classification = _classify_type(simple, surface_data)
        results.append({"fqn": fqn, "simple": simple, **classification})

    if args.json_output:
        json.dump({"file": file_key, "plan": results}, sys.stdout, indent=2)
        print()
        return 0

    print()
    print("=" * 80)
    print(f"MIGRATION PLAN: {file_key}")
    print("=" * 80)
    print(f"  MC Types: {len(mc_fqns)}")
    print()

    by_cls = defaultdict(list)
    for r in results:
        by_cls[r["classification"]].append(r)

    order = ["EXISTING", "INTERFACE", "SERVICE", "TRANSFORM", "SUPPRESS", "AUTO"]
    for cls in order:
        items = by_cls.get(cls, [])
        if not items:
            continue
        print(f"  {cls} ({len(items)}):")
        for item in items:
            pa_str = f" -> {item['pa_name']}" if item.get("pa_name") else ""
            svc_str = f" [{item['service_name']}]" if item.get("service_name") else ""
            print(f"    {item['simple']:<30}{pa_str}{svc_str}")
            print(f"      {item['reason'].replace(chr(0x2192), '->')}")
        print()

    return 0


def add_classify_args(subparsers):
    p = subparsers.add_parser(
        "classify", help="Classify MC type for migration strategy (P0)"
    )
    p.add_argument(
        "type",
        nargs="?",
        default=None,
        help="MC type simple name (e.g. MinecraftServer)",
    )
    p.add_argument(
        "--file", help="Classify all MC types in a file (migration plan mode)"
    )
    p.add_argument("--fqn", help="Exact FQN override")
    p.add_argument(
        "--surface",
        action="store_true",
        help="Gather surface data from index for auto-classification",
    )
    p.add_argument(
        "--json", dest="json_output", action="store_true", help="Output as JSON"
    )
    p.add_argument(
        "--index",
        default="tools/migration/mc_usage_index.json",
        help="Path to index JSON (relative to project root)",
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_classify)


# ============================================================
# SUBCOMMAND: classify-all (P0)
# ============================================================


_CLASSIFY_ORDER = ["EXISTING", "INTERFACE", "SERVICE", "TRANSFORM", "SUPPRESS", "AUTO"]
_CLASSIFY_LABELS = {
    "EXISTING": "already have PA abstractions",
    "INTERFACE": "become standalone PA interfaces",
    "SERVICE": "add methods to PlatformService or domain services",
    "TRANSFORM": "handle via Static-Transform regex",
    "SUPPRESS": "never abstracted, stay in mc1710",
    "AUTO": "not explicitly classified, use heuristics if surface data available",
}
_LARGE_GROUP_CAP = 10


def _format_type_line(cls, entry):
    """Format a single type line based on classification."""
    name = entry["shortName"]
    if cls == "EXISTING" or cls == "INTERFACE":
        pa = entry.get("pa_name") or "?"
        return f"    {name:<30} -> {pa}"
    if cls == "SERVICE":
        svc = entry.get("service_name") or "PlatformService"
        return f"    {name:<30} -> {svc}"
    if cls == "TRANSFORM":
        patterns = entry.get("transform_patterns") or []
        return f"    {name:<30} -> ({len(patterns)} patterns)"
    return f"    {name}"


def _map_line(cls, entry):
    """Build a single line for the flat map file."""
    name = entry["shortName"]
    if cls in ("EXISTING", "INTERFACE"):
        return f"{name} -> {entry.get('pa_name') or '?'}"
    if cls == "SERVICE":
        return f"{name} -> {entry.get('service_name') or 'PlatformService'}"
    if cls == "TRANSFORM":
        n = len(entry.get("transform_patterns") or [])
        return f"{name} -> ({n} patterns)"
    return name


def _write_map_file(map_path, grouped):
    """Write the flat name->mapping text file (all entries, uncapped)."""
    map_path.parent.mkdir(parents=True, exist_ok=True)
    with open(map_path, "w", encoding="utf-8") as f:
        first = True
        for cls in _CLASSIFY_ORDER:
            items = grouped.get(cls, [])
            if not items:
                continue
            if not first:
                f.write("\n")
            first = False
            f.write(f"{cls}:\n")
            for entry in items:
                f.write(f"{_map_line(cls, entry)}\n")


def cmd_classify_all(args):
    """Classify ALL MC types in the index and output results as JSON."""
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )
    index = load_index(project_root, args.index)

    types = index.get("types", {})
    total = len(types)

    results = {}
    grouped = defaultdict(list)

    for fqn, ti in types.items():
        simple_name = ti["shortName"]

        surface_data = _gather_surface_data(index, fqn)
        if not surface_data["methods"]:
            surface_data = None

        result = _classify_type(simple_name, surface_data)
        classification = result["classification"]

        entry = {
            "shortName": simple_name,
            "classification": classification,
            "reason": result["reason"],
            "pa_name": result.get("pa_name"),
            "target": result.get("target"),
            "import_count": ti.get("importCount", 0),
            "file_count": len(ti.get("importedBy", [])),
        }
        if result.get("service_name"):
            entry["service_name"] = result["service_name"]
        if result.get("transform_patterns"):
            entry["transform_patterns"] = [
                [p, r] for p, r in result["transform_patterns"]
            ]

        results[fqn] = entry
        grouped[classification].append({"fqn": fqn, **entry})

    for cls in grouped:
        grouped[cls].sort(key=lambda e: e["import_count"], reverse=True)

    timestamp = datetime.now(timezone.utc).isoformat()
    classified_counts = {
        cls: len(grouped[cls]) for cls in _CLASSIFY_ORDER if grouped.get(cls)
    }

    json_grouped = {}
    for cls in _CLASSIFY_ORDER:
        items = grouped.get(cls, [])
        if not items:
            continue
        json_items = []
        for e in items:
            item = {"fqn": e["fqn"], "shortName": e["shortName"]}
            if cls in ("EXISTING", "INTERFACE", "AUTO"):
                item["pa_name"] = e.get("pa_name")
            if cls == "SERVICE":
                item["service_name"] = e.get("service_name")
            if cls == "TRANSFORM":
                item["pattern_count"] = len(e.get("transform_patterns") or [])
            item["import_count"] = e["import_count"]
            json_items.append(item)
        json_grouped[cls] = json_items

    output = {
        "metadata": {
            "timestamp": timestamp,
            "total_types": total,
            "classified": classified_counts,
        },
        "grouped_by_classification": json_grouped,
        "types": results,
    }

    output_path = Path(args.output)
    if not output_path.is_absolute():
        output_path = Path(project_root) / args.output
    output_path.parent.mkdir(parents=True, exist_ok=True)
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)
        f.write("\n")

    map_path = Path(args.map_file)
    if not map_path.is_absolute():
        map_path = Path(project_root) / args.map_file
    _write_map_file(map_path, grouped)

    if args.json_stdout:
        json.dump(output, sys.stdout, indent=2, ensure_ascii=True)
        print()
    else:
        print()
        print("=" * 80)
        print("CLASSIFY ALL MC TYPES")
        print("=" * 80)
        print(f"  Processing {total} types from index...")
        print()
        print("  CLASSIFICATION SUMMARY:")
        for cls in _CLASSIFY_ORDER:
            count = classified_counts.get(cls, 0)
            if count > 0:
                print(f"    {cls:<14}{count:>4} types")
        print()
        print(f"  {'_' * 78}")

        for cls in _CLASSIFY_ORDER:
            items = grouped.get(cls, [])
            if not items:
                continue
            label = _CLASSIFY_LABELS.get(cls, "")
            print()
            print(f"  {cls} ({len(items)} types -- {label}):")
            cap = _LARGE_GROUP_CAP if len(items) > _LARGE_GROUP_CAP + 3 else len(items)
            for entry in items[:cap]:
                print(_format_type_line(cls, entry))
            remaining = len(items) - cap
            if remaining > 0:
                print(f"    ... ({remaining} more)")

        print()
        print(f"  {'_' * 78}")
        print(f"  Output JSON written: {output_path}")
        print(f"  Map file written:    {map_path}")
        print()

    return 0


def add_classify_all_args(subparsers):
    p = subparsers.add_parser(
        "classify-all", help="Classify ALL MC types in index and output as JSON (P0)"
    )
    p.add_argument(
        "--output",
        default="tools/migration/classification_results.json",
        help="Output JSON file path (default: tools/migration/classification_results.json)",
    )
    p.add_argument(
        "--map-file",
        default="tools/migration/pa_classifications_map.txt",
        help="Output flat map file (default: tools/migration/pa_classifications_map.txt)",
    )
    p.add_argument(
        "--json",
        dest="json_stdout",
        action="store_true",
        help="Output summary to stdout as JSON instead of human-readable text",
    )
    p.add_argument(
        "--index",
        default="tools/migration/mc_usage_index.json",
        help="Path to index JSON (relative to project root)",
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_classify_all)


# ============================================================
# Normalization utilities for migration-diff
# ============================================================


def _load_classifications(project_root, classifications_path):
    """Load the mc_type_classifications.json file. Returns the 'types' dict."""
    cls_file = Path(project_root) / classifications_path
    if not cls_file.exists():
        raise FileNotFoundError(
            f"Classifications file not found: {cls_file}\n"
            f"Expected at: {classifications_path}"
        )
    with open(cls_file, "r", encoding="utf-8") as f:
        data = json.load(f)
    return data.get("types", {})


def normalize_text(text, classifications):
    """
    Normalize text by replacing MC type names with their PA equivalents.

    For EXISTING/INTERFACE/SERVICE entries (those with 'pa' or 'service' field):
      - Whole-word replace MC simple name -> PA name
    For TRANSFORM entries:
      - Apply each regex pattern from the 'patterns' list

    This is used to normalize BOTH old and new file content so that pure
    type migrations vanish from diffs.
    """
    result = text

    # Phase 1: TRANSFORM patterns (apply regex replacements)
    for type_name, info in classifications.items():
        if type_name.startswith("_"):
            continue
        category = info.get("category", "")
        if category == "TRANSFORM":
            patterns = info.get("patterns", [])
            for pat in patterns:
                if len(pat) >= 2:
                    try:
                        result = re.sub(pat[0], pat[1], result)
                    except re.error:
                        pass

    # Phase 2: EXISTING/INTERFACE/SERVICE whole-word replacements
    for type_name, info in classifications.items():
        if type_name.startswith("_"):
            continue
        category = info.get("category", "")
        if category not in ("EXISTING", "INTERFACE", "SERVICE"):
            continue
        pa_name = info.get("pa") or info.get("service")
        if not pa_name:
            continue
        # Skip very short names to avoid false positives
        if len(type_name) < 4:
            continue
        # Whole-word replacement
        result = re.sub(r"\b" + re.escape(type_name) + r"\b", pa_name, result)

    return result


# ============================================================
# SUBCOMMAND: migration-diff (P3)
# ============================================================


def _extract_method_bodies(file_path, parser):
    """
    Extract method signatures AND bodies from a Java file using tree-sitter.

    Returns a list of dicts:
      {
        "name": str,
        "params": [{"type": str, "name": str}, ...],
        "line": int,
        "body": str | None,
        "signature_text": str,
      }
    """
    source_bytes = Path(file_path).read_bytes()
    tree = parser.parse(source_bytes)
    methods = []
    _walk_for_method_bodies(tree.root_node, source_bytes, methods)
    return methods, source_bytes


def _walk_for_method_bodies(node, source_bytes, methods):
    """Walk AST collecting method declarations with bodies."""
    if node.type in ("method_declaration", "constructor_declaration"):
        is_constructor = node.type == "constructor_declaration"
        name_node = node.child_by_field_name("name")
        name = _node_text(name_node, source_bytes) if name_node else "?"

        # Return type
        return_type = ""
        if not is_constructor:
            type_node = node.child_by_field_name("type")
            return_type = _node_text(type_node, source_bytes) if type_node else "void"

        # Parameters
        params = []
        params_node = node.child_by_field_name("parameters")
        if params_node:
            for param_child in params_node.children:
                if param_child.type in ("formal_parameter", "spread_parameter"):
                    p_type_node = param_child.child_by_field_name("type")
                    p_name_node = param_child.child_by_field_name("name")
                    p_type = (
                        _node_text(p_type_node, source_bytes) if p_type_node else "?"
                    )
                    if param_child.type == "spread_parameter":
                        p_type += "..."
                    p_name = (
                        _node_text(p_name_node, source_bytes) if p_name_node else "?"
                    )
                    dims_node = param_child.child_by_field_name("dimensions")
                    if dims_node:
                        p_type += _node_text(dims_node, source_bytes)
                    params.append({"type": p_type, "name": p_name})

        # Body
        body_node = node.child_by_field_name("body")
        body_text = _node_text(body_node, source_bytes) if body_node else None

        # Build a readable signature
        param_str = ", ".join(f"{p['type']} {p['name']}" for p in params)
        sig_text = f"{return_type + ' ' if return_type else ''}{name}({param_str})"

        methods.append(
            {
                "name": name,
                "return_type": return_type,
                "params": params,
                "line": node.start_point[0] + 1,
                "body": body_text,
                "signature_text": sig_text.strip(),
            }
        )

    for child in node.children:
        _walk_for_method_bodies(child, source_bytes, methods)


def _normalized_signature_key(sig, classifications):
    """Create a signature key with normalized param types (MC -> PA)."""
    normalized_params = []
    for p in sig["params"]:
        ptype = p["type"]
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
            ptype = re.sub(r"\b" + re.escape(type_name) + r"\b", pa_name, ptype)
        normalized_params.append(ptype)
    return (sig["name"], tuple(normalized_params))


def _scan_unmigrated_types(file_text, classifications):
    """
    Scan file text for whole-word occurrences of MC type names that should
    have been replaced (EXISTING, INTERFACE, SERVICE with pa/service field).

    Returns a list of dicts: { "mc_name": str, "pa_name": str, "lines": [int], "in_import": bool }
    """
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
        # Skip short names to avoid false positives
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


def cmd_migration_diff(args):
    """Compare an original mc1710 Java file against a migrated core/ file."""
    parser, lang = setup_treesitter_parser()
    if parser is None:
        print("ERROR: tree-sitter-java not installed", file=sys.stderr)
        print("Run: pip install tree-sitter tree-sitter-java", file=sys.stderr)
        return 1

    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )

    # Load classifications
    classifications_path = args.classifications
    classifications = _load_classifications(project_root, classifications_path)

    # Resolve files
    old_file = resolve_java_file(project_root, args.old)
    new_file = resolve_java_file(project_root, args.new)

    if old_file is None or not old_file.exists():
        print(f"ERROR: Old file not found: {args.old}", file=sys.stderr)
        return 1
    if new_file is None or not new_file.exists():
        print(f"ERROR: New file not found: {args.new}", file=sys.stderr)
        return 1

    # Count classification types
    type_count = sum(1 for k in classifications if not k.startswith("_"))

    # ── STEP 1: UNMIGRATED types scan on new file ──
    new_text = new_file.read_text(encoding="utf-8", errors="replace")
    unmigrated = _scan_unmigrated_types(new_text, classifications)

    # ── STEP 2: MISSING_METHOD detection ──
    old_methods, _ = _extract_method_bodies(old_file, parser)
    new_methods, _ = _extract_method_bodies(new_file, parser)

    # Build maps by normalized key
    old_method_map = {}
    for m in old_methods:
        key = _normalized_signature_key(m, classifications)
        old_method_map[key] = m

    new_method_map = {}
    for m in new_methods:
        key = _normalized_signature_key(m, classifications)
        new_method_map[key] = m

    old_keys = set(old_method_map.keys())
    new_keys = set(new_method_map.keys())

    missing_methods = []
    for key in sorted(old_keys - new_keys):
        m = old_method_map[key]
        missing_methods.append(
            {
                "name": m["name"],
                "signature": m["signature_text"],
                "line": m["line"],
            }
        )

    # ── STEP 3: SEMANTIC_BODY_DIFF detection ──
    semantic_diffs = []
    common_keys = old_keys & new_keys
    for key in sorted(common_keys):
        old_m = old_method_map[key]
        new_m = new_method_map[key]

        old_body = old_m.get("body")
        new_body = new_m.get("body")

        # Skip methods without bodies (abstract/interface)
        if old_body is None or new_body is None:
            continue

        # Normalize both bodies
        norm_old = normalize_text(old_body, classifications)
        norm_new = normalize_text(new_body, classifications)

        # Strip whitespace for comparison
        norm_old_lines = norm_old.strip().splitlines()
        norm_new_lines = norm_new.strip().splitlines()

        # Use SequenceMatcher for similarity ratio
        matcher = difflib.SequenceMatcher(None, norm_old_lines, norm_new_lines)
        ratio = matcher.ratio()

        # Generate unified diff for detail
        diff_lines = list(
            difflib.unified_diff(
                norm_old_lines,
                norm_new_lines,
                fromfile="old (normalized)",
                tofile="new (normalized)",
                lineterm="",
            )
        )

        # Filter to actual change lines (not headers)
        change_lines = [l for l in diff_lines if l.startswith("+") or l.startswith("-")]
        change_lines = [
            l
            for l in change_lines
            if not l.startswith("+++") and not l.startswith("---")
        ]

        if ratio < 0.85 or len(change_lines) > 0:
            semantic_diffs.append(
                {
                    "name": old_m["name"],
                    "signature": old_m["signature_text"],
                    "ratio": round(ratio, 3),
                    "diff_lines": diff_lines[:10],
                    "total_diff_lines": len(change_lines),
                    "old_line": old_m["line"],
                    "new_line": new_m["line"],
                }
            )

    # ── VERDICT ──
    if missing_methods:
        verdict = "FAIL"
        verdict_reason = f"{len(missing_methods)} missing method(s)"
    elif unmigrated or semantic_diffs:
        verdict = "WARN"
        parts = []
        if unmigrated:
            parts.append(f"{len(unmigrated)} unmigrated type(s)")
        if semantic_diffs:
            parts.append(f"{len(semantic_diffs)} body diff(s)")
        verdict_reason = ", ".join(parts)
    else:
        verdict = "PASS"
        verdict_reason = "all methods present, no unmigrated types, no body drift"

    note = (
        "NOTE: migration-diff cannot detect subtle semantic drift (algorithmic changes, "
        "logic reordering, renamed variables). Manual review is always recommended "
        "when WARN or FAIL."
    )

    # ── OUTPUT ──
    if args.json_output:
        output = {
            "old_file": str(old_file),
            "new_file": str(new_file),
            "unmigrated": unmigrated,
            "missing_methods": missing_methods,
            "semantic_body_diffs": [
                {
                    "name": d["name"],
                    "signature": d["signature"],
                    "similarity_ratio": d["ratio"],
                    "diff_line_count": d["total_diff_lines"],
                    "diff_preview": d["diff_lines"],
                }
                for d in semantic_diffs
            ],
            "verdict": verdict,
            "verdict_reason": verdict_reason,
        }
        json.dump(output, sys.stdout, indent=2)
        print()
        return 0

    # Human-readable output
    print()
    print("=" * 80)
    print("MIGRATION DIFF")
    print("=" * 80)
    print(f"  Old: {old_file.name} (mc1710)")
    print(f"  New: {new_file.name} (core)")
    print(f"  Classifications: {Path(classifications_path).name} ({type_count} types)")
    print()

    # Unmigrated types
    if unmigrated:
        print(f"  UNMIGRATED TYPES ({len(unmigrated)} found in new file):")
        for u in unmigrated:
            line_str = ", ".join(str(l) for l in u["lines"][:10])
            if len(u["lines"]) > 10:
                line_str += f" ... ({len(u['lines'])} total)"
            import_note = " [import not cleaned]" if u["in_import"] else ""
            print(
                f"    ! {u['mc_name']}  ->  should be {u['pa_name']}  "
                f"(found on line {line_str}){import_note}"
            )
        print()
    else:
        print("  UNMIGRATED TYPES: None [OK]")
        print()

    # Missing methods
    if missing_methods:
        print(
            f"  MISSING METHODS ({len(missing_methods)} found in old, absent in new):"
        )
        for m in missing_methods:
            print(f"    - {m['signature']}           [line {m['line']} in old]")
        print()
    else:
        print("  MISSING METHODS: None [OK]")
        print()

    # Semantic body diffs
    if semantic_diffs:
        print(
            f"  SEMANTIC BODY DIFFS ({len(semantic_diffs)} methods have non-migration body changes):"
        )
        for d in semantic_diffs:
            print(f"    ~ {d['signature']}")
            print(
                f"      Similarity: {d['ratio']:.1%}  "
                f"[{d['total_diff_lines']} diff line(s)]"
            )
            # Show up to 10 diff lines
            shown = 0
            for dl in d["diff_lines"]:
                if shown >= 10:
                    remaining = len(d["diff_lines"]) - 10
                    print(f"      ... ({remaining} more diff lines)")
                    break
                print(f"      {dl}")
                shown += 1
            print(
                f"      [!] Manual review required -- could be logic change or safe refactor"
            )
            print()
    else:
        print("  SEMANTIC BODY DIFFS: None [OK]")
        print()

    # Verdict
    print("  " + "-" * 78)
    print(
        f"  VERDICT: {verdict}  "
        f"[{len(unmigrated)} unmigrated type(s), "
        f"{len(missing_methods)} missing method(s), "
        f"{len(semantic_diffs)} body diff(s)]"
    )
    print(f"  {note}")
    print("=" * 80)
    print()

    return 0


def add_migration_diff_args(subparsers):
    p = subparsers.add_parser(
        "migration-diff",
        help="Compare mc1710 original vs migrated core/ file — PASS/WARN/FAIL verdict (P3)",
    )
    p.add_argument("--old", required=True, help="Path to original mc1710 Java file")
    p.add_argument("--new", required=True, help="Path to migrated core/ Java file")
    p.add_argument(
        "--classifications",
        default="tools/migration/mc_type_classifications.json",
        help="Path to classifications JSON (relative to project root)",
    )
    p.add_argument(
        "--json", dest="json_output", action="store_true", help="Output as JSON"
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_migration_diff)


# ============================================================
# generate-mappings: Build mc_type_mappings.json from curated classifications
# ============================================================


def cmd_generate_mappings(args):
    """Read mc_type_classifications_original.json and produce mc_type_mappings.json."""
    project_root = (
        Path(args.project_root) if args.project_root else auto_detect_project_root()
    )

    cls_path = Path(args.classifications)
    if not cls_path.is_absolute():
        cls_path = project_root / args.classifications
    if not cls_path.exists():
        print(f"ERROR: Classifications file not found: {cls_path}", file=sys.stderr)
        return 1

    with open(cls_path, "r", encoding="utf-8") as f:
        cls_data = json.load(f)

    types = cls_data.get("types", {})
    allowed_categories = None
    if args.category:
        allowed_categories = set(c.upper() for c in args.category)

    mappings = {}
    category_counts = defaultdict(int)

    for type_name, info in types.items():
        if type_name.startswith("__"):
            continue

        category = info.get("category", "")
        if not category:
            continue

        if allowed_categories and category not in allowed_categories:
            continue

        category_counts[category] += 1
        imports = info.get("imports", 0)
        fqn = info.get("fqn", "")

        if category == "EXISTING":
            pa_name = info.get("pa", "")
            pa_package = info.get("package", "")
            pa_fqn = f"{pa_package}.{pa_name}" if pa_package and pa_name else ""
            entry = {
                "fqn": fqn,
                "category": category,
                "pa_name": pa_name,
                "pa_package": pa_package,
                "pa_fqn": pa_fqn,
                "imports": imports,
                "swap": [type_name, pa_name, fqn, pa_fqn],
            }

        elif category == "INTERFACE":
            pa_name = info.get("pa", "")
            pa_package = info.get("package", "")
            pa_fqn = f"{pa_package}.{pa_name}" if pa_package and pa_name else ""
            entry = {
                "fqn": fqn,
                "category": category,
                "pa_name": pa_name,
                "pa_package": pa_package,
                "pa_fqn": pa_fqn,
                "imports": imports,
                "swap": [type_name, pa_name, fqn, pa_fqn],
            }
            if "methods" in info:
                entry["methods"] = info["methods"]
            if "constructors" in info:
                entry["constructors"] = info["constructors"]

        elif category == "SERVICE":
            entry = {
                "fqn": fqn,
                "category": category,
                "service_target": info.get("service", ""),
                "imports": imports,
                "methods": info.get("methods", {}),
                "swap": None,
            }

        elif category == "TRANSFORM":
            entry = {
                "fqn": fqn,
                "category": category,
                "patterns": info.get("patterns", []),
                "imports": imports,
                "swap": None,
            }

        elif category == "SUPPRESS":
            entry = {
                "fqn": fqn,
                "category": category,
                "reason": info.get("reason", ""),
                "imports": imports,
                "swap": None,
            }

        else:
            entry = {
                "fqn": fqn,
                "category": category,
                "imports": imports,
                "swap": None,
            }

        mappings[type_name] = entry

    timestamp = datetime.now(timezone.utc).isoformat()
    total_types = len(mappings)

    cat_order = ["EXISTING", "INTERFACE", "SERVICE", "TRANSFORM", "SUPPRESS"]
    categories_summary = {}
    for cat in cat_order:
        if category_counts.get(cat, 0) > 0:
            categories_summary[cat] = category_counts[cat]
    for cat, count in sorted(category_counts.items()):
        if cat not in categories_summary and count > 0:
            categories_summary[cat] = count

    output = {
        "_meta": {
            "generated": timestamp,
            "source": cls_path.name,
            "total_types": total_types,
            "categories": categories_summary,
        },
        "mappings": mappings,
    }

    if args.json_output:
        json.dump(output, sys.stdout, indent=2, ensure_ascii=True)
        print()
    else:
        output_path = Path(args.output)
        if not output_path.is_absolute():
            output_path = project_root / args.output
        output_path.parent.mkdir(parents=True, exist_ok=True)
        with open(output_path, "w", encoding="utf-8") as f:
            json.dump(output, f, indent=2, ensure_ascii=False)
            f.write("\n")

        print()
        print("=" * 80)
        print("GENERATE MAPPINGS")
        print("=" * 80)
        print(f"  Source:       {cls_path.name}")
        print(f"  Total types:  {total_types}")
        print()
        print("  CATEGORY BREAKDOWN:")
        for cat in cat_order:
            count = categories_summary.get(cat, 0)
            if count > 0:
                print(f"    {cat:<14}{count:>4} types")
        print()
        print(f"  Output written: {output_path}")
        print("=" * 80)
        print()

    return 0


def add_generate_mappings_args(subparsers):
    p = subparsers.add_parser(
        "generate-mappings",
        help="Generate mc_type_mappings.json from curated classifications",
    )
    p.add_argument(
        "--output",
        default="tools/migration/mc_type_mappings.json",
        help="Output JSON file path (default: tools/migration/mc_type_mappings.json)",
    )
    p.add_argument(
        "--classifications",
        default="tools/migration/mc_type_classifications_original.json",
        help="Path to source classifications JSON (relative to project root)",
    )
    p.add_argument(
        "--json",
        dest="json_output",
        action="store_true",
        help="Print output to stdout as JSON instead of writing to file",
    )
    p.add_argument(
        "--category",
        nargs="+",
        help="Filter to specific categories (e.g. EXISTING INTERFACE)",
    )
    p.add_argument("--project-root", help="Project root (auto-detected if omitted)")
    p.set_defaults(func=cmd_generate_mappings)


# ============================================================
# Main CLI
# ============================================================


def main():
    parser = argparse.ArgumentParser(
        prog="analyze.py",
        description="Unified Diagnostic Toolchain for CustomNPC-Plus Migration Analysis",
    )
    subparsers = parser.add_subparsers(dest="command", help="Available subcommands")

    # Register all subcommands
    add_query_type_args(subparsers)
    add_query_file_args(subparsers)
    add_extract_signatures_args(subparsers)
    add_surface_miner_args(subparsers)
    add_type_usage_args(subparsers)
    add_diff_signatures_args(subparsers)
    add_migration_diff_args(subparsers)
    add_coverage_report_args(subparsers)
    add_classify_args(subparsers)
    add_classify_all_args(subparsers)
    add_generate_mappings_args(subparsers)

    args = parser.parse_args()

    if not args.command:
        parser.print_help()
        return 1

    try:
        return args.func(args)
    except FileNotFoundError as e:
        print(f"ERROR: {e}", file=sys.stderr)
        return 1
    except Exception as e:
        print(f"ERROR: {e}", file=sys.stderr)
        import traceback

        traceback.print_exc(file=sys.stderr)
        return 1


if __name__ == "__main__":
    sys.exit(main())
