#!/usr/bin/env python3
"""Controlled live probes for Multipartes external API validation."""

from __future__ import annotations

import argparse
import base64
import gzip
import json
import os
from pathlib import Path
from typing import Dict, Iterable, Optional, Tuple
from urllib import error, parse, request

SAFE_METHODS = {"GET", "HEAD", "OPTIONS"}


def parse_key_value(value: str) -> Tuple[str, str]:
    if "=" not in value:
        raise argparse.ArgumentTypeError("Expected KEY=VALUE")
    key, raw = value.split("=", 1)
    return key, raw


def redact_headers(headers: Dict[str, str]) -> Dict[str, str]:
    redacted = dict(headers)
    if "Authorization" in redacted:
        redacted["Authorization"] = "<redacted>"
    return redacted


def env_required(name: str) -> str:
    value = os.environ.get(name)
    if value:
        return value
    raise SystemExit(f"Missing required environment variable: {name}")


def env_optional(name: str) -> Optional[str]:
    return os.environ.get(name)


def join_url(base_url: str, path: str, query_items: Iterable[Tuple[str, str]] = ()) -> str:
    if path.startswith("http://") or path.startswith("https://"):
        url = path
    else:
        url = base_url.rstrip("/") + "/" + path.lstrip("/")
    query_items = list(query_items)
    if query_items:
        separator = "&" if "?" in url else "?"
        url = url + separator + parse.urlencode(query_items)
    return url


def default_headers(*, json_body: bool) -> Dict[str, str]:
    headers: Dict[str, str] = {"Accept": "application/json"}
    api_version = env_optional("MULTIPARTES_API_VERSION")
    if api_version:
        headers["api_version"] = api_version
    if json_body:
        headers["Content-type"] = "application/json"
    return headers


def load_json_body(raw: Optional[str]) -> Optional[bytes]:
    if raw is None:
        return None
    if raw.startswith("@"):
        payload = Path(raw[1:]).read_text(encoding="utf-8")
    else:
        payload = raw
    json.loads(payload)
    return payload.encode("utf-8")


def read_response_body(resp) -> Tuple[int, str]:
    charset = resp.headers.get_content_charset() or "utf-8"
    body = resp.read()
    return resp.status, body.decode(charset, errors="replace")


def send_request(
    method: str,
    url: str,
    headers: Optional[Dict[str, str]] = None,
    body: Optional[bytes] = None,
    timeout: int = 15,
) -> Tuple[int, str, Dict[str, str]]:
    req = request.Request(url=url, method=method, data=body, headers=headers or {})
    try:
        with request.urlopen(req, timeout=timeout) as resp:
            status, text = read_response_body(resp)
            return status, text, dict(resp.headers.items())
    except error.HTTPError as exc:
        status, text = read_response_body(exc)
        return status, text, dict(exc.headers.items())


def decode_body(text: str, mode: str) -> str:
    if mode != "base64-gzip":
        return text
    compressed = base64.b64decode(text)
    return gzip.decompress(compressed).decode("utf-8")


def maybe_json(value: str):
    try:
        return json.loads(value)
    except json.JSONDecodeError:
        return value


def print_payload(payload: Dict) -> None:
    print(json.dumps(payload, indent=2, sort_keys=True, ensure_ascii=True))


def add_common_flags(parser: argparse.ArgumentParser) -> None:
    parser.add_argument("--timeout", type=int, default=15, help="Timeout in seconds.")
    parser.add_argument(
        "--dry-run",
        action="store_true",
        help="Print the planned request instead of executing it.",
    )


def cmd_health(args: argparse.Namespace) -> int:
    base_url = env_required("MULTIPARTES_API_BASE_URL")
    token = args.token or env_optional("MULTIPARTES_API_TOKEN")
    headers = default_headers(json_body=False)
    if token:
        headers["Authorization"] = f"Bearer {token}"
    url = join_url(base_url, args.path)
    if args.dry_run:
        print_payload(
            {
                "command": "health",
                "url": url,
                "headers": redact_headers(headers),
                "expected_statuses": [200, 401, 403],
            }
        )
        return 0
    status, text, response_headers = send_request("GET", url, headers=headers, timeout=args.timeout)
    healthy = status in {200, 401, 403} and "Portal Movil Tigo" not in text
    print_payload(
        {
            "command": "health",
            "healthy": healthy,
            "status": status,
            "url": url,
            "headers": redact_headers(headers),
            "response": maybe_json(text),
            "response_headers": response_headers,
        }
    )
    return 0 if healthy else 1


def cmd_login(args: argparse.Namespace) -> int:
    base_url = env_required("MULTIPARTES_API_BASE_URL")
    user = args.user or env_required("MULTIPARTES_API_USER")
    password = args.password or env_required("MULTIPARTES_API_PASSWORD")
    url = join_url(base_url, args.path)
    headers = default_headers(json_body=True)
    payload = json.dumps(
        {
            "username": user,
            "password": password,
            "remember": not args.no_remember,
        }
    )
    if args.dry_run:
        print_payload(
            {
                "command": "login",
                "url": url,
                "headers": headers,
                "json": {
                    "username": user,
                    "password": "<redacted>",
                    "remember": not args.no_remember,
                },
            }
        )
        return 0
    status, text, response_headers = send_request(
        "POST",
        url,
        headers=headers,
        body=payload.encode("utf-8"),
        timeout=args.timeout,
    )
    parsed = maybe_json(text)
    token = parsed.get("id_token") if isinstance(parsed, dict) else None
    result = {
        "command": "login",
        "status": status,
        "url": url,
        "headers": headers,
        "token_present": bool(token),
        "response_headers": response_headers,
        "response": parsed,
    }
    if token and not args.show_token and isinstance(result["response"], dict):
        result["response"] = dict(result["response"])
        result["response"]["id_token"] = "<redacted>"
    print_payload(result)
    return 0 if status == 200 and token else 1


def cmd_account(args: argparse.Namespace) -> int:
    base_url = env_required("MULTIPARTES_API_BASE_URL")
    token = args.token or env_optional("MULTIPARTES_API_TOKEN")
    if not token:
        raise SystemExit("Account probe requires --token or MULTIPARTES_API_TOKEN")
    url = join_url(base_url, args.path)
    headers = default_headers(json_body=False)
    headers["Authorization"] = f"Bearer {token}"
    if args.dry_run:
        print_payload(
            {
                "command": "account",
                "url": url,
                "headers": redact_headers(headers),
            }
        )
        return 0
    status, text, response_headers = send_request("GET", url, headers=headers, timeout=args.timeout)
    print_payload(
        {
            "command": "account",
            "status": status,
            "url": url,
            "headers": redact_headers(headers),
            "response": maybe_json(text),
            "response_headers": response_headers,
        }
    )
    return 0 if status == 200 else 1


def cmd_request(args: argparse.Namespace) -> int:
    base_url = env_required("MULTIPARTES_API_BASE_URL")
    method = args.method.upper()
    if method not in SAFE_METHODS and not args.allow_mutation:
        raise SystemExit("Refusing mutating request without --allow-mutation")
    query_items = [parse_key_value(item) for item in args.query]
    extra_headers = dict(parse_key_value(item) for item in args.header)
    headers = default_headers(json_body=bool(args.json))
    headers.update(extra_headers)
    url = join_url(base_url, args.path, query_items=query_items)
    body = load_json_body(args.json)
    if args.dry_run:
        print_payload(
            {
                "command": "request",
                "method": method,
                "url": url,
                "headers": redact_headers(headers),
                "body": maybe_json(body.decode("utf-8")) if body else None,
                "decode": args.decode,
            }
        )
        return 0
    status, text, response_headers = send_request(
        method,
        url,
        headers=headers,
        body=body,
        timeout=args.timeout,
    )
    decoded = decode_body(text, args.decode)
    success = True
    if args.expect_status:
        success = status in args.expect_status
    print_payload(
        {
            "command": "request",
            "method": method,
            "status": status,
            "url": url,
            "headers": redact_headers(headers),
            "response": maybe_json(decoded),
            "response_headers": response_headers,
            "decode": args.decode,
        }
    )
    return 0 if success else 1


def build_parser() -> argparse.ArgumentParser:
    parser = argparse.ArgumentParser(description="Controlled probes for Multipartes external API.")
    subparsers = parser.add_subparsers(dest="command", required=True)

    health = subparsers.add_parser("health", help="Run a synthetic health check against the auth stack.")
    health.add_argument("--path", default="erp/api/account", help="Relative path to probe.")
    health.add_argument("--token", help="Bearer token override.")
    add_common_flags(health)
    health.set_defaults(func=cmd_health)

    login = subparsers.add_parser("login", help="Validate the active JWT login flow.")
    login.add_argument("--path", default="erp/api/authenticate", help="Relative login path.")
    login.add_argument("--user", help="Username override.")
    login.add_argument("--password", help="Password override.")
    login.add_argument("--no-remember", action="store_true", help="Send remember=false.")
    login.add_argument("--show-token", action="store_true", help="Print the returned JWT.")
    add_common_flags(login)
    login.set_defaults(func=cmd_login)

    account = subparsers.add_parser("account", help="Validate account lookup with a bearer token.")
    account.add_argument("--path", default="erp/api/account", help="Relative account path.")
    account.add_argument("--token", help="Bearer token override.")
    add_common_flags(account)
    account.set_defaults(func=cmd_account)

    generic = subparsers.add_parser("request", help="Run a generic request against a relative path.")
    generic.add_argument("method", help="HTTP method.")
    generic.add_argument("path", help="Relative path or full URL.")
    generic.add_argument("--query", action="append", default=[], metavar="KEY=VALUE", help="Repeatable query parameter.")
    generic.add_argument("--header", action="append", default=[], metavar="KEY=VALUE", help="Repeatable request header.")
    generic.add_argument("--json", help="Inline JSON string or @path/to/file.json.")
    generic.add_argument("--decode", choices=("none", "base64-gzip"), default="none", help="Optional response decoding mode.")
    generic.add_argument("--expect-status", action="append", type=int, default=[], help="Repeatable expected HTTP status.")
    generic.add_argument("--allow-mutation", action="store_true", help="Allow POST/PUT/PATCH/DELETE execution.")
    add_common_flags(generic)
    generic.set_defaults(func=cmd_request)

    return parser


def main() -> int:
    parser = build_parser()
    args = parser.parse_args()
    return args.func(args)


if __name__ == "__main__":
    raise SystemExit(main())
