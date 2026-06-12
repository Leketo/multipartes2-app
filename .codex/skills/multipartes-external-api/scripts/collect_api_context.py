#!/usr/bin/env python3
"""Collect a lightweight API inventory from the Multipartes Android client."""

from __future__ import annotations

import argparse
import json
import re
from dataclasses import asdict, dataclass, field
from pathlib import Path
from typing import Dict, List, Optional, Sequence

COMMREQ_PATH = Path("app/src/main/java/py/multipartesapp/comm/CommReq.java")
JAVA_ROOT = Path("app/src/main/java")
METHOD_SIGNATURE_RE = re.compile(r"^\s*(public|private|protected)\s+")


@dataclass
class Record:
    kind: str
    method: str
    file: str
    line: int
    path_expression: str
    resolved_path: Optional[str] = None
    params: List[str] = field(default_factory=list)
    query_keys: List[str] = field(default_factory=list)
    body_keys: List[str] = field(default_factory=list)
    headers: List[str] = field(default_factory=list)
    response_base64: Optional[bool] = None
    class_name: Optional[str] = None


def parse_args() -> argparse.Namespace:
    parser = argparse.ArgumentParser(
        description="Extract API callsites and constants from multipartes2-app."
    )
    parser.add_argument("root", nargs="?", default=".", help="Repository root.")
    parser.add_argument(
        "--format",
        choices=("markdown", "json"),
        default="markdown",
        help="Output format.",
    )
    return parser.parse_args()


def split_concat_expression(expr: str) -> List[str]:
    parts: List[str] = []
    buf: List[str] = []
    in_string = False
    escape = False
    for ch in expr:
        if in_string:
            buf.append(ch)
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
            buf.append(ch)
            continue
        if ch == "+":
            part = "".join(buf).strip()
            if part:
                parts.append(part)
            buf = []
            continue
        buf.append(ch)
    part = "".join(buf).strip()
    if part:
        parts.append(part)
    return parts


def decode_literal(token: str, constants: Dict[str, str]) -> Optional[str]:
    token = token.strip()
    if token in constants:
        return constants[token]
    if token.startswith('"') and token.endswith('"'):
        return bytes(token[1:-1], "utf-8").decode("unicode_escape")
    return None


def resolve_expression(expr: str, constants: Dict[str, str], *, allow_unknown: bool) -> Optional[str]:
    pieces: List[str] = []
    for token in split_concat_expression(expr):
        value = decode_literal(token, constants)
        if value is None:
            if not allow_unknown:
                return None
            pieces.append("{" + token.strip() + "}")
        else:
            pieces.append(value)
    return "".join(pieces)


def parse_commreq_constants(path: Path) -> Dict[str, str]:
    all_constants: Dict[str, str] = {}
    text = path.read_text(encoding="utf-8")
    pattern = re.compile(r"(?:private|public)\s+static\s+final\s+String\s+(\w+)\s*=\s*(.*?);", re.S)
    for match in pattern.finditer(text):
        name = match.group(1)
        expr = " ".join(match.group(2).split())
        value = resolve_expression(expr, all_constants, allow_unknown=False)
        if value is not None:
            all_constants[name] = value
    return {name: value for name, value in all_constants.items() if name.startswith("CommReq")}


def extract_statement(lines: Sequence[str], start_index: int, marker: str) -> str:
    text_parts: List[str] = []
    depth = 0
    in_string = False
    escape = False
    started = False
    for index in range(start_index, len(lines)):
        line = lines[index]
        if not started:
            pos = line.find(marker)
            if pos < 0:
                continue
            segment = line[pos:]
            started = True
        else:
            segment = line
        text_parts.append(segment)
        for ch in segment:
            if in_string:
                if escape:
                    escape = False
                elif ch == "\\":
                    escape = True
                elif ch == '"':
                    in_string = False
                continue
            if ch == '"':
                in_string = True
            elif ch == "(":
                depth += 1
            elif ch == ")":
                depth -= 1
        if started and depth <= 0 and ";" in segment:
            break
    return "\n".join(text_parts)


def split_top_level_args(arg_text: str) -> List[str]:
    args: List[str] = []
    buf: List[str] = []
    depth_paren = depth_brace = depth_bracket = 0
    in_string = False
    escape = False
    for ch in arg_text:
        if in_string:
            buf.append(ch)
            if escape:
                escape = False
            elif ch == "\\":
                escape = True
            elif ch == '"':
                in_string = False
            continue
        if ch == '"':
            in_string = True
            buf.append(ch)
            continue
        if ch == "(":
            depth_paren += 1
        elif ch == ")":
            depth_paren -= 1
        elif ch == "{":
            depth_brace += 1
        elif ch == "}":
            depth_brace -= 1
        elif ch == "[":
            depth_bracket += 1
        elif ch == "]":
            depth_bracket -= 1
        elif ch == "," and depth_paren == 0 and depth_brace == 0 and depth_bracket == 0:
            part = "".join(buf).strip()
            if part:
                args.append(part)
            buf = []
            continue
        buf.append(ch)
    part = "".join(buf).strip()
    if part:
        args.append(part)
    return args


def resolve_commreq_references(expr: str, commreq: Dict[str, str]) -> Optional[str]:
    resolved = expr
    for name, value in sorted(commreq.items(), key=lambda item: len(item[0]), reverse=True):
        resolved = resolved.replace(f"CommReq.{name}", f'"{value}"')
    return resolve_expression(resolved, {}, allow_unknown=True)


def extract_pair_keys(snippet: str) -> List[str]:
    return list(dict.fromkeys(re.findall(r'\{\s*"([^"]+)"\s*,', snippet)))


def capture_following_lines(lines: Sequence[str], start_index: int, limit: int = 90) -> str:
    collected: List[str] = []
    for index in range(start_index, min(len(lines), start_index + limit)):
        if index > start_index and METHOD_SIGNATURE_RE.match(lines[index]):
            break
        collected.append(lines[index])
    return "\n".join(collected)


def extract_comm_records(java_root: Path, commreq: Dict[str, str]) -> List[Record]:
    records: List[Record] = []
    for file_path in java_root.rglob("*.java"):
        if file_path.name == "Comm.java":
            continue
        lines = file_path.read_text(encoding="utf-8").splitlines()
        for index, line in enumerate(lines):
            marker = None
            method = None
            if "requestGet(" in line:
                marker = "requestGet("
                method = "GET"
            elif "requestPost(" in line:
                marker = "requestPost("
                method = "POST"
            if marker is None:
                continue
            statement = extract_statement(lines, index, marker)
            inner = statement.split(marker, 1)[1].rsplit(")", 1)[0]
            args = split_top_level_args(inner)
            if not args:
                continue
            explicit_base = len(args) >= 2 and "Comm.URL" in args[0]
            path_expr = args[1] if explicit_base else args[0]
            params: List[str] = []
            response_base64 = None
            class_name = None
            for arg in args:
                if "new String[][]" in arg:
                    params = extract_pair_keys(arg)
                elif arg.strip() in ("true", "false"):
                    response_base64 = arg.strip() == "true"
                elif ".class.getName()" in arg or arg.strip() == '""':
                    class_name = arg.strip()
            records.append(
                Record(
                    kind="comm",
                    method=method,
                    file=file_path.as_posix(),
                    line=index + 1,
                    path_expression=path_expr.strip(),
                    resolved_path=resolve_commreq_references(path_expr, commreq),
                    params=params,
                    response_base64=response_base64,
                    class_name=class_name,
                )
            )
    return records


def extract_hardcoded_records(java_root: Path, commreq: Dict[str, str]) -> List[Record]:
    records: List[Record] = []
    url_pattern = re.compile(r"String\s+url\s*=\s*Comm\.URL\s*\+\s*(.+);")
    for file_path in java_root.rglob("*.java"):
        lines = file_path.read_text(encoding="utf-8").splitlines()
        for index, line in enumerate(lines):
            match = url_pattern.search(line)
            if not match:
                continue
            path_expr = match.group(1).strip()
            window = capture_following_lines(lines, index)
            method = "UNKNOWN"
            if "new HttpPost(url)" in window:
                method = "POST"
            elif "new HttpGet(url)" in window:
                method = "GET"
            query_keys = list(dict.fromkeys(re.findall(r"[?&]([A-Za-z0-9_]+)=", window)))
            body_keys = list(dict.fromkeys(re.findall(r"\.(?:accumulate|put)\(\"([^\"]+)\"", window)))
            headers = list(dict.fromkeys(re.findall(r"setHeader\(\"([^\"]+)\"", window)))
            records.append(
                Record(
                    kind="hardcoded_url",
                    method=method,
                    file=file_path.as_posix(),
                    line=index + 1,
                    path_expression=path_expr,
                    resolved_path=resolve_commreq_references(path_expr, commreq),
                    query_keys=query_keys,
                    body_keys=body_keys,
                    headers=headers,
                )
            )
    return records


def render_markdown(commreq: Dict[str, str], records: Sequence[Record]) -> str:
    lines: List[str] = []
    lines.append("# API Context Inventory")
    lines.append("")
    lines.append("## Resolved CommReq Constants")
    lines.append("")
    lines.append("| Constant | Path |")
    lines.append("| --- | --- |")
    for name in sorted(commreq):
        lines.append(f"| `{name}` | `{commreq[name]}` |")
    lines.append("")
    lines.append("## Callsites")
    lines.append("")
    lines.append(
        "| Kind | Method | File:Line | Path Expression | Resolved Path | Params | Query Keys | Body Keys | Headers | Base64 | Class |"
    )
    lines.append(
        "| --- | --- | --- | --- | --- | --- | --- | --- | --- | --- | --- |"
    )
    for record in sorted(records, key=lambda item: (item.file, item.line)):
        lines.append(
            "| "
            + " | ".join(
                [
                    record.kind,
                    record.method,
                    f"`{record.file}:{record.line}`",
                    f"`{record.path_expression}`",
                    f"`{record.resolved_path or ''}`",
                    f"`{', '.join(record.params)}`",
                    f"`{', '.join(record.query_keys)}`",
                    f"`{', '.join(record.body_keys)}`",
                    f"`{', '.join(record.headers)}`",
                    f"`{record.response_base64}`",
                    f"`{record.class_name or ''}`",
                ]
            )
            + " |"
        )
    return "\n".join(lines)


def main() -> int:
    args = parse_args()
    root = Path(args.root).resolve()
    commreq = parse_commreq_constants(root / COMMREQ_PATH)
    records = extract_comm_records(root / JAVA_ROOT, commreq)
    records.extend(extract_hardcoded_records(root / JAVA_ROOT, commreq))
    if args.format == "json":
        print(
            json.dumps(
                {
                    "commreq": commreq,
                    "records": [asdict(record) for record in records],
                },
                indent=2,
                sort_keys=True,
            )
        )
        return 0
    print(render_markdown(commreq, records))
    return 0


if __name__ == "__main__":
    raise SystemExit(main())

