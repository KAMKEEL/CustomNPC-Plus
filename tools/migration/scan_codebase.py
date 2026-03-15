#!/usr/bin/env python3
"""
scan_codebase.py — Full MC/Forge Usage Index Builder
=====================================================
Uses tree-sitter for AST-accurate scanning of all Java files.
Produces a JSON index of every Minecraft/Forge type: imports, usage patterns,
method calls, field accesses, class hierarchy, and PA abstraction status.

Usage:
    pip install tree-sitter tree-sitter-java
    python tools/migration/scan_codebase.py
    python tools/migration/scan_codebase.py --scan-dir mc1710/src/main/java --output mc_usage_index.json
    python tools/migration/scan_codebase.py --top 30
"""

import os
import sys
import json
import re
import argparse
import time
from collections import defaultdict
from pathlib import Path

# ============================================================
# Configuration
# ============================================================

MC_PREFIXES = ("net.minecraft.", "cpw.mods.", "net.minecraftforge.")

from pa_rules import EXISTING_MAPPINGS as KNOWN_PA

# ============================================================
# Tree-sitter Setup
# ============================================================


def setup_parser():
    """Initialize tree-sitter parser with Java grammar."""
    try:
        import tree_sitter_java as tsjava
        from tree_sitter import Language, Parser
    except ImportError:
        print("ERROR: Required packages not installed.")
        print("Run:  pip install tree-sitter tree-sitter-java")
        sys.exit(1)

    lang = Language(tsjava.language())
    parser = Parser(lang)
    return parser


# ============================================================
# Java File Analyzer
# ============================================================


class JavaFileAnalyzer:
    """Analyzes a single Java file for MC/Forge type usage via tree-sitter AST."""

    def __init__(self, parser, source_bytes, rel_path):
        self.source = source_bytes
        self.rel_path = rel_path
        self.tree = parser.parse(source_bytes)
        self.root = self.tree.root_node

        # Import maps
        self.mc_imports = {}  # simple_name → fqn (MC/Forge only)

        # Variable → type mapping (flat per file, last-write-wins)
        self.type_vars = {}  # var_name → simple_type_name

        # Extracted data
        self.extends_list = []  # (our_class, mc_simple_name)
        self.implements_list = []  # (our_class, mc_simple_name)
        self.new_calls = []  # (mc_simple_name, line)
        self.instanceof_checks = []  # (mc_simple_name, line)
        self.casts = []  # (mc_simple_name, line)
        self.method_calls = defaultdict(list)  # mc_fqn → [(method_name, line)]
        self.static_calls = defaultdict(list)  # mc_fqn → [(method_name, line)]
        self.field_accesses = defaultdict(list)  # mc_fqn → [(field_name, line)]

    def _text(self, node):
        if node is None:
            return None
        return node.text.decode("utf-8")

    def analyze(self):
        self._extract_imports()
        if self.mc_imports:  # Skip AST walk if no MC imports
            self._walk(self.root)
        return self._build_result()

    # --- Import extraction (regex — imports are trivially structured) ---

    def _extract_imports(self):
        text = self.source.decode("utf-8", errors="replace")
        for line in text.split("\n"):
            line = line.strip()
            m = re.match(r"import\s+(static\s+)?([a-zA-Z0-9_.*]+)\s*;", line)
            if not m:
                continue

            is_static = bool(m.group(1))
            fqn = m.group(2)

            if fqn.endswith(".*"):
                continue  # Wildcard — can't resolve simple names

            if is_static:
                # Static import: last part is method/field, parent is class
                parts = fqn.rsplit(".", 1)
                if len(parts) == 2:
                    class_fqn = parts[0]
                    simple = class_fqn.rsplit(".", 1)[-1]
                    if self._is_mc(class_fqn):
                        self.mc_imports[simple] = class_fqn
            else:
                simple = fqn.rsplit(".", 1)[-1]
                if self._is_mc(fqn):
                    self.mc_imports[simple] = fqn

    def _is_mc(self, fqn):
        return any(fqn.startswith(p) for p in MC_PREFIXES)

    # --- AST walking ---

    def _walk(self, node):
        self._process_node(node)
        for child in node.children:
            self._walk(child)

    def _process_node(self, node):
        t = node.type
        if (
            t == "class_declaration"
            or t == "interface_declaration"
            or t == "enum_declaration"
        ):
            self._handle_class_decl(node)
        elif t == "field_declaration":
            self._handle_var_decl(node)
        elif t == "local_variable_declaration":
            self._handle_var_decl(node)
        elif t == "formal_parameter":
            self._handle_formal_param(node)
        elif t == "enhanced_for_statement":
            self._handle_enhanced_for(node)
        elif t == "method_invocation":
            self._handle_method_invocation(node)
        elif t == "object_creation_expression":
            self._handle_object_creation(node)
        elif t == "cast_expression":
            self._handle_cast(node)
        elif t == "instanceof_expression":
            self._handle_instanceof(node)
        elif t == "field_access":
            self._handle_field_access(node)

    # --- Node handlers ---

    def _handle_class_decl(self, node):
        name_node = node.child_by_field_name("name")
        class_name = self._text(name_node) or "?"

        # Superclass: (superclass  extends  type_identifier)
        sc = node.child_by_field_name("superclass")
        if sc:
            for child in sc.children:
                tn = self._extract_type_name(child)
                if tn and tn in self.mc_imports:
                    self.extends_list.append((class_name, tn))

        # Interfaces: (super_interfaces  implements  type_list(type_identifier ...))
        ifaces = node.child_by_field_name("interfaces")
        if ifaces:
            for child in self._iter_all(ifaces):
                tn = self._extract_type_name(child)
                if tn and tn in self.mc_imports:
                    self.implements_list.append((class_name, tn))

    def _handle_var_decl(self, node):
        """Handle field_declaration and local_variable_declaration.
        AST: type_identifier @type  variable_declarator @declarator(identifier @name)"""
        type_node = node.child_by_field_name("type")
        type_name = self._extract_type_name(type_node)
        if not type_name:
            return
        for child in node.children:
            if child.type == "variable_declarator":
                name_node = child.child_by_field_name("name")
                if name_node:
                    self.type_vars[self._text(name_node)] = type_name

    def _handle_formal_param(self, node):
        """AST: type_identifier @type  identifier @name"""
        type_node = node.child_by_field_name("type")
        name_node = node.child_by_field_name("name")
        type_name = self._extract_type_name(type_node)
        var_name = self._text(name_node)
        if type_name and var_name:
            self.type_vars[var_name] = type_name

    def _handle_enhanced_for(self, node):
        """AST: for ( type_identifier @type  identifier @name : expr ) block"""
        type_node = node.child_by_field_name("type")
        name_node = node.child_by_field_name("name")
        type_name = self._extract_type_name(type_node)
        var_name = self._text(name_node)
        if type_name and var_name:
            self.type_vars[var_name] = type_name

    def _handle_method_invocation(self, node):
        """AST: identifier @object . identifier @name argument_list @arguments
        OR:   field_access @object . identifier @name argument_list @arguments"""
        obj_node = node.child_by_field_name("object")
        name_node = node.child_by_field_name("name")

        if not name_node or not obj_node:
            return

        method_name = self._text(name_node)
        line = node.start_point[0] + 1

        # Case 1: direct variable — player.getHealth()
        if obj_node.type == "identifier":
            receiver = self._text(obj_node)

            # Check if it's a type name (static call): MinecraftServer.getServer()
            if receiver in self.mc_imports and receiver[0:1].isupper():
                self.static_calls[self.mc_imports[receiver]].append((method_name, line))
                return

            # Instance call: look up variable type
            if receiver in self.type_vars:
                type_name = self.type_vars[receiver]
                if type_name in self.mc_imports:
                    self.method_calls[self.mc_imports[type_name]].append(
                        (method_name, line)
                    )
            return

        # Case 2: this.field.method() — field_access(this, field)
        if obj_node.type == "field_access":
            obj_obj = obj_node.child_by_field_name("object")
            field = obj_node.child_by_field_name("field")
            if obj_obj and obj_obj.type == "this" and field:
                receiver = self._text(field)
                if receiver in self.type_vars:
                    type_name = self.type_vars[receiver]
                    if type_name in self.mc_imports:
                        self.method_calls[self.mc_imports[type_name]].append(
                            (method_name, line)
                        )

    def _handle_object_creation(self, node):
        """AST: new  type_identifier @type  argument_list @arguments"""
        type_node = node.child_by_field_name("type")
        type_name = self._extract_type_name(type_node)
        if type_name and type_name in self.mc_imports:
            self.new_calls.append((type_name, node.start_point[0] + 1))

    def _handle_cast(self, node):
        """AST: ( type_identifier @type )  expression @value"""
        type_node = node.child_by_field_name("type")
        type_name = self._extract_type_name(type_node)
        if type_name and type_name in self.mc_imports:
            self.casts.append((type_name, node.start_point[0] + 1))

    def _handle_instanceof(self, node):
        """AST: expression @left  instanceof  type_identifier @right"""
        right = node.child_by_field_name("right")
        type_name = self._extract_type_name(right)
        if type_name and type_name in self.mc_imports:
            self.instanceof_checks.append((type_name, node.start_point[0] + 1))

    def _handle_field_access(self, node):
        """AST: identifier @object . identifier @field
        Only records when the receiver is a variable with MC type AND
        this field_access is NOT the object of a method_invocation (those are
        handled by method_invocation handler)."""
        # Skip if this is the object of a method call (method handler covers it)
        parent = node.parent
        if parent and parent.type == "method_invocation":
            p_obj = parent.child_by_field_name("object")
            if p_obj is not None and p_obj.id == node.id:
                return

        obj_node = node.child_by_field_name("object")
        field_node = node.child_by_field_name("field")
        if not obj_node or not field_node:
            return

        field_name = self._text(field_node)
        line = node.start_point[0] + 1

        receiver = None
        if obj_node.type == "identifier":
            receiver = self._text(obj_node)
        elif obj_node.type == "this":
            return  # Can't resolve this.field without class context

        if receiver and receiver in self.type_vars:
            type_name = self.type_vars[receiver]
            if type_name in self.mc_imports:
                self.field_accesses[self.mc_imports[type_name]].append(
                    (field_name, line)
                )

    # --- Type name extraction ---

    def _extract_type_name(self, node):
        """Extract the simple type name from any type AST node."""
        if node is None:
            return None
        t = node.type
        if t == "type_identifier":
            return self._text(node)
        elif t == "scoped_type_identifier":
            # e.g. NBTBase.NBTPrimitive — return first type_identifier
            for child in node.children:
                if child.type == "type_identifier":
                    return self._text(child)
            return self._text(node)
        elif t == "generic_type":
            # e.g. List<EntityPlayer> — return the base type
            for child in node.children:
                if child.type in ("type_identifier", "scoped_type_identifier"):
                    return self._extract_type_name(child)
        elif t == "array_type":
            # e.g. EntityPlayer[] — return element type
            el = node.child_by_field_name("element")
            if el:
                return self._extract_type_name(el)
            for child in node.children:
                r = self._extract_type_name(child)
                if r:
                    return r
        return None

    def _iter_all(self, node):
        """Recursively yield all descendant nodes."""
        for child in node.children:
            yield child
            yield from self._iter_all(child)

    # --- Build result ---

    def _build_result(self):
        return {
            "mc_imports": dict(self.mc_imports),
            "extends": self.extends_list,
            "implements": self.implements_list,
            "new_calls": self.new_calls,
            "instanceof": self.instanceof_checks,
            "casts": self.casts,
            "method_calls": {k: list(v) for k, v in self.method_calls.items()},
            "static_calls": {k: list(v) for k, v in self.static_calls.items()},
            "field_accesses": {k: list(v) for k, v in self.field_accesses.items()},
        }


# ============================================================
# Index Builder (aggregates across all files)
# ============================================================


class IndexBuilder:
    def __init__(self, project_root, scan_dir, parser):
        self.project_root = Path(project_root)
        self.scan_dir = self.project_root / scan_dir
        self.parser = parser

        # Per-type aggregation
        self.type_data = {}  # fqn → dict
        self.file_data = {}  # rel_path → dict

        self.stats = {
            "totalFiles": 0,
            "filesWithMcDeps": 0,
            "totalMcImports": 0,
            "parseErrors": 0,
        }

    def _ensure_type(self, fqn, short_name):
        if fqn not in self.type_data:
            self.type_data[fqn] = {
                "shortName": short_name,
                "package": fqn.rsplit(".", 1)[0] if "." in fqn else "",
                "importCount": 0,
                "importedBy": [],
                "paAbstraction": KNOWN_PA.get(fqn),
                "usage": {
                    "extends": [],
                    "implements": [],
                    "constructors": [],
                    "casts": [],
                    "instanceof": [],
                },
                "methodCalls": {},
                "staticCalls": {},
                "fieldAccesses": {},
            }
        return self.type_data[fqn]

    def _add_call(self, store, method_name, rel_path):
        if method_name not in store:
            store[method_name] = {"count": 0, "files": []}
        entry = store[method_name]
        entry["count"] += 1
        if rel_path not in entry["files"]:
            entry["files"].append(rel_path)

    def scan(self):
        java_files = []
        for root, _dirs, files in os.walk(self.scan_dir):
            for f in files:
                if f.endswith(".java"):
                    java_files.append(Path(root) / f)

        self.stats["totalFiles"] = len(java_files)
        t0 = time.time()

        for i, filepath in enumerate(java_files):
            rel_path = filepath.relative_to(self.scan_dir).as_posix()

            try:
                source = filepath.read_bytes()
                analyzer = JavaFileAnalyzer(self.parser, source, rel_path)
                result = analyzer.analyze()
                self._aggregate(rel_path, result)
            except Exception as e:
                self.stats["parseErrors"] += 1
                print(f"  WARN: {rel_path}: {e}", file=sys.stderr)

            # Progress
            if (i + 1) % 500 == 0:
                print(f"  ... {i + 1}/{len(java_files)} files", file=sys.stderr)

        elapsed = time.time() - t0
        print(
            f"  Scanned {len(java_files)} files in {elapsed:.1f}s "
            f"({self.stats['parseErrors']} errors)",
            file=sys.stderr,
        )

    def _aggregate(self, rel_path, result):
        mc_imports = result["mc_imports"]
        if not mc_imports:
            return

        self.stats["filesWithMcDeps"] += 1
        self.stats["totalMcImports"] += len(mc_imports)

        # File index
        self.file_data[rel_path] = {
            "mcImportCount": len(mc_imports),
            "mcTypes": sorted(mc_imports.keys()),
            "mcFqns": sorted(set(mc_imports.values())),
        }

        # Type index — imports
        for simple, fqn in mc_imports.items():
            ti = self._ensure_type(fqn, simple)
            ti["importCount"] += 1
            ti["importedBy"].append(rel_path)

        # Hierarchy
        for class_name, mc_simple in result["extends"]:
            if mc_simple in mc_imports:
                fqn = mc_imports[mc_simple]
                self._ensure_type(fqn, mc_simple)["usage"]["extends"].append(
                    {"file": rel_path, "class": class_name}
                )

        for class_name, mc_simple in result["implements"]:
            if mc_simple in mc_imports:
                fqn = mc_imports[mc_simple]
                self._ensure_type(fqn, mc_simple)["usage"]["implements"].append(
                    {"file": rel_path, "class": class_name}
                )

        # Constructors
        for mc_simple, line in result["new_calls"]:
            if mc_simple in mc_imports:
                fqn = mc_imports[mc_simple]
                self._ensure_type(fqn, mc_simple)["usage"]["constructors"].append(
                    {"file": rel_path, "line": line}
                )

        # Casts
        for mc_simple, line in result["casts"]:
            if mc_simple in mc_imports:
                fqn = mc_imports[mc_simple]
                self._ensure_type(fqn, mc_simple)["usage"]["casts"].append(
                    {"file": rel_path, "line": line}
                )

        # instanceof
        for mc_simple, line in result["instanceof"]:
            if mc_simple in mc_imports:
                fqn = mc_imports[mc_simple]
                self._ensure_type(fqn, mc_simple)["usage"]["instanceof"].append(
                    {"file": rel_path, "line": line}
                )

        # Method calls
        for fqn, calls in result["method_calls"].items():
            ti = self._ensure_type(fqn, fqn.rsplit(".", 1)[-1])
            for method_name, _line in calls:
                self._add_call(ti["methodCalls"], method_name, rel_path)

        # Static calls
        for fqn, calls in result["static_calls"].items():
            ti = self._ensure_type(fqn, fqn.rsplit(".", 1)[-1])
            for method_name, _line in calls:
                self._add_call(ti["staticCalls"], method_name, rel_path)

        # Field accesses
        for fqn, accesses in result["field_accesses"].items():
            ti = self._ensure_type(fqn, fqn.rsplit(".", 1)[-1])
            for field_name, _line in accesses:
                self._add_call(ti["fieldAccesses"], field_name, rel_path)

    def build_output(self, top_n=50):
        sorted_types = sorted(
            self.type_data.items(), key=lambda x: x[1]["importCount"], reverse=True
        )

        # Per-package stats
        by_package = defaultdict(lambda: {"types": set(), "totalImports": 0})
        abstracted = 0
        not_abstracted = 0

        for fqn, ti in sorted_types:
            pkg = ti["package"]
            by_package[pkg]["types"].add(ti["shortName"])
            by_package[pkg]["totalImports"] += ti["importCount"]
            if ti["paAbstraction"]:
                abstracted += 1
            else:
                not_abstracted += 1

        total_types = abstracted + not_abstracted

        # Hierarchy rollup
        hierarchy_extends = defaultdict(list)
        hierarchy_implements = defaultdict(list)
        for fqn, ti in sorted_types:
            for entry in ti["usage"]["extends"]:
                hierarchy_extends[fqn].append(entry)
            for entry in ti["usage"]["implements"]:
                hierarchy_implements[fqn].append(entry)

        summary = {
            "totalFiles": self.stats["totalFiles"],
            "filesWithMcDeps": self.stats["filesWithMcDeps"],
            "totalMcImports": self.stats["totalMcImports"],
            "uniqueMcTypes": len(self.type_data),
            "parseErrors": self.stats["parseErrors"],
            "abstractionCoverage": {
                "abstracted": abstracted,
                "notAbstracted": not_abstracted,
                "total": total_types,
                "percentage": f"{abstracted / total_types * 100:.1f}%"
                if total_types > 0
                else "0%",
            },
            "topTypes": [
                {
                    "fqn": fqn,
                    "shortName": ti["shortName"],
                    "importCount": ti["importCount"],
                    "pa": ti["paAbstraction"],
                    "methodCallCount": sum(
                        m["count"] for m in ti["methodCalls"].values()
                    ),
                    "staticCallCount": sum(
                        m["count"] for m in ti["staticCalls"].values()
                    ),
                    "fieldAccessCount": sum(
                        m["count"] for m in ti["fieldAccesses"].values()
                    ),
                    "extendsCount": len(ti["usage"]["extends"]),
                    "implementsCount": len(ti["usage"]["implements"]),
                    "constructorCount": len(ti["usage"]["constructors"]),
                }
                for fqn, ti in sorted_types[:top_n]
            ],
            "topFiles": sorted(
                [
                    {"file": f, "mcImports": d["mcImportCount"]}
                    for f, d in self.file_data.items()
                ],
                key=lambda x: x["mcImports"],
                reverse=True,
            )[:top_n],
            "byPackage": {
                pkg: {
                    "types": len(data["types"]),
                    "typeNames": sorted(data["types"]),
                    "totalImports": data["totalImports"],
                }
                for pkg, data in sorted(
                    by_package.items(), key=lambda x: x[1]["totalImports"], reverse=True
                )
            },
        }

        return {
            "meta": {
                "scanDir": str(self.scan_dir.relative_to(self.project_root)),
                "projectRoot": str(self.project_root),
                "generatedBy": "scan_codebase.py (tree-sitter)",
                "timestamp": time.strftime("%Y-%m-%dT%H:%M:%S"),
            },
            "summary": summary,
            "types": {fqn: ti for fqn, ti in sorted_types},
            "files": self.file_data,
            "hierarchy": {
                "extends": dict(hierarchy_extends),
                "implements": dict(hierarchy_implements),
            },
        }


# ============================================================
# Console Summary
# ============================================================


def print_summary(output):
    s = output["summary"]
    cov = s["abstractionCoverage"]

    print()
    print("=" * 72)
    print(f"  MC/FORGE USAGE INDEX — {output['meta']['scanDir']}")
    print("=" * 72)
    print(f"  Total files scanned:    {s['totalFiles']:>6,}")
    print(f"  Files with MC deps:     {s['filesWithMcDeps']:>6,}")
    print(f"  Total MC imports:       {s['totalMcImports']:>6,}")
    print(f"  Unique MC types:        {s['uniqueMcTypes']:>6}")
    print(f"  Parse errors:           {s['parseErrors']:>6}")
    print(
        f"  PA coverage:            {cov['abstracted']}/{cov['total']} ({cov['percentage']})"
    )
    print()

    # Top types
    print("  TOP MC TYPES BY IMPORT COUNT")
    print("  " + "-" * 68)
    fmt = "  {rank:>3}. {name:<30} {imp:>4} imports  {methods:>4} methods  {fields:>3} fields  {pa}"
    for i, t in enumerate(s["topTypes"][:30]):
        pa_str = f"-> {t['pa']}" if t["pa"] else ""
        print(
            fmt.format(
                rank=i + 1,
                name=t["shortName"],
                imp=t["importCount"],
                methods=t["methodCallCount"] + t["staticCallCount"],
                fields=t["fieldAccessCount"],
                pa=pa_str,
            )
        )
    print()

    # Top packages
    print("  TOP MC PACKAGES")
    print("  " + "-" * 68)
    for pkg, data in list(s["byPackage"].items())[:15]:
        print(
            f"  {pkg:<45} {data['types']:>3} types  {data['totalImports']:>5} imports"
        )
    print()

    # Top files
    print("  TOP FILES BY MC DEPENDENCY COUNT")
    print("  " + "-" * 68)
    for f in s["topFiles"][:15]:
        print(f"  {f['file']:<55} {f['mcImports']:>3} imports")
    print()

    # Hierarchy summary
    ext = output["hierarchy"]["extends"]
    impl = output["hierarchy"]["implements"]
    if ext:
        print(
            f"  CLASSES EXTENDING MC TYPES ({sum(len(v) for v in ext.values())} total)"
        )
        print("  " + "-" * 68)
        for fqn, entries in sorted(ext.items(), key=lambda x: len(x[1]), reverse=True)[
            :10
        ]:
            short = fqn.rsplit(".", 1)[-1]
            classes = [e["class"] for e in entries]
            print(f"  {short:<30} <- {', '.join(classes[:5])}")
            if len(classes) > 5:
                print(f"  {'':30}    ... and {len(classes) - 5} more")
        print()

    if impl:
        print(
            f"  CLASSES IMPLEMENTING MC INTERFACES ({sum(len(v) for v in impl.values())} total)"
        )
        print("  " + "-" * 68)
        for fqn, entries in sorted(impl.items(), key=lambda x: len(x[1]), reverse=True)[
            :10
        ]:
            short = fqn.rsplit(".", 1)[-1]
            classes = [e["class"] for e in entries]
            print(f"  {short:<30} <- {', '.join(classes[:5])}")
            if len(classes) > 5:
                print(f"  {'':30}    ... and {len(classes) - 5} more")
        print()

    print("=" * 72)


# ============================================================
# Main
# ============================================================


def main():
    # Auto-detect project root from script location
    script_dir = Path(__file__).resolve().parent
    default_root = script_dir.parent.parent  # tools/migration/ -> project root

    ap = argparse.ArgumentParser(
        description="MC/Forge Usage Index Builder (tree-sitter)"
    )
    ap.add_argument(
        "--scan-dir",
        default="mc1710/src/main/java",
        help="Directory to scan (relative to project root)",
    )
    ap.add_argument(
        "--output",
        "-o",
        default="mc_usage_index.json",
        help="Output JSON file path (relative to project root)",
    )
    ap.add_argument(
        "--project-root", default=str(default_root), help="Project root directory"
    )
    ap.add_argument(
        "--top", type=int, default=50, help="Number of top entries in summary"
    )
    ap.add_argument(
        "--quiet", "-q", action="store_true", help="Suppress console summary"
    )
    args = ap.parse_args()

    project_root = Path(args.project_root).resolve()
    output_path = project_root / args.output

    print(f"Initializing tree-sitter parser...", file=sys.stderr)
    parser = setup_parser()

    print(f"Scanning {args.scan_dir}...", file=sys.stderr)
    builder = IndexBuilder(project_root, args.scan_dir, parser)
    builder.scan()

    print(f"Building index...", file=sys.stderr)
    output = builder.build_output(top_n=args.top)

    # Write JSON
    with open(output_path, "w", encoding="utf-8") as f:
        json.dump(output, f, indent=2, ensure_ascii=False)
    size_kb = output_path.stat().st_size / 1024
    print(f"Wrote {output_path} ({size_kb:.0f} KB)", file=sys.stderr)

    # Console summary
    if not args.quiet:
        print_summary(output)


if __name__ == "__main__":
    main()
