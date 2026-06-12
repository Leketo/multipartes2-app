#!/usr/bin/env python3
"""
Collect a compact static diagnosis summary for an Android/Gradle repo.

Usage:
    python collect_gradle_context.py [repo_root]
"""

from __future__ import annotations

import re
import sys
from pathlib import Path

EXCLUDED_DIRS = {".git", ".gradle", ".idea", "build", "skills"}


def read_text(path: Path) -> str:
    try:
        return path.read_text(encoding="utf-8", errors="replace")
    except Exception:
        return ""


def first_match(pattern: str, text: str) -> str | None:
    match = re.search(pattern, text, re.MULTILINE)
    return match.group(1).strip() if match else None


def last_match(pattern: str, text: str) -> str | None:
    matches = re.findall(pattern, text, re.MULTILINE)
    if not matches:
        return None
    last = matches[-1]
    return last.strip() if isinstance(last, str) else last[-1].strip()


def collect_modules(settings_text: str) -> list[str]:
    modules: list[str] = []
    for line in settings_text.splitlines():
        if "include" not in line:
            continue
        modules.extend(re.findall(r"['\"](:[^'\"]+)['\"]", line))
    return modules


def resolve_gradle_value(raw_value: str | None, ext_values: dict[str, str]) -> str | None:
    if raw_value is None:
        return None
    if raw_value.startswith("project.ext."):
        key = raw_value.split(".")[-1]
        return ext_values.get(key, raw_value)
    return raw_value


def list_source_extensions(repo_root: Path) -> tuple[bool, bool]:
    has_java = False
    has_kotlin = False
    for path in repo_root.rglob("*"):
        if not path.is_file():
            continue
        if any(part in EXCLUDED_DIRS for part in path.parts):
            continue
        suffix = path.suffix.lower()
        if suffix == ".java":
            has_java = True
        elif suffix == ".kt":
            has_kotlin = True
        if has_java and has_kotlin:
            break
    return has_java, has_kotlin


def main() -> int:
    repo_root = Path(sys.argv[1]).resolve() if len(sys.argv) > 1 else Path.cwd().resolve()

    settings_path = repo_root / "settings.gradle"
    if not settings_path.exists():
        settings_path = repo_root / "settings.gradle.kts"

    root_build_path = repo_root / "build.gradle"
    if not root_build_path.exists():
        root_build_path = repo_root / "build.gradle.kts"

    app_build_path = repo_root / "app" / "build.gradle"
    if not app_build_path.exists():
        app_build_path = repo_root / "app" / "build.gradle.kts"

    wrapper_path = repo_root / "gradle" / "wrapper" / "gradle-wrapper.properties"
    gradle_props_path = repo_root / "gradle.properties"

    settings_text = read_text(settings_path)
    root_build_text = read_text(root_build_path)
    app_build_text = read_text(app_build_path)
    wrapper_text = read_text(wrapper_path)
    gradle_props_text = read_text(gradle_props_path)

    ext_values = {
        key: value
        for key, value in re.findall(r"ext\.([A-Za-z_][A-Za-z0-9_]*)\s*=\s*([^\r\n]+)", root_build_text + "\n" + app_build_text)
    }

    modules = collect_modules(settings_text)
    missing_modules = []
    for module in modules:
        module_dir = repo_root / module.replace(":", "/").lstrip("/")
        if not module_dir.exists():
            missing_modules.append(module)

    wrapper_url = first_match(r"distributionUrl=(.+)", wrapper_text)
    agp_version = first_match(r"com\.android\.tools\.build:gradle:([^\s'\"]+)", root_build_text)
    kotlin_plugin = first_match(r"org\.jetbrains\.kotlin[:.]kotlin-gradle-plugin:([^\s'\"]+)", root_build_text + "\n" + app_build_text)
    jvmargs = first_match(r"org\.gradle\.jvmargs=(.+)", gradle_props_text)
    compile_sdk = first_match(r"compileSdkVersion\s+([^\s]+)", app_build_text)
    build_tools = first_match(r"buildToolsVersion\s+['\"]([^'\"]+)['\"]", app_build_text)
    min_sdk = resolve_gradle_value(last_match(r"minSdkVersion\s+([^\s]+)", app_build_text), ext_values)
    target_sdk = resolve_gradle_value(last_match(r"targetSdkVersion\s+([^\s]+)", app_build_text), ext_values)

    plugin_markers = []
    if "apply plugin: 'io.fabric'" in app_build_text or 'apply plugin: "io.fabric"' in app_build_text:
        plugin_markers.append("io.fabric")
    if "crashlytics" in app_build_text.lower():
        plugin_markers.append("crashlytics-dependency")

    repo_markers = []
    if "jcenter()" in root_build_text or "jcenter()" in app_build_text:
        repo_markers.append("jcenter")
    if "jitpack.io" in root_build_text.lower() or "jitpack.io" in app_build_text.lower():
        repo_markers.append("jitpack")
    if "maven.fabric.io" in root_build_text or "maven.fabric.io" in app_build_text:
        repo_markers.append("fabric-maven")
    if "raw.github.com" in app_build_text.lower():
        repo_markers.append("raw-github-maven")

    dependency_markers = []
    for token in [
        "useLibrary 'org.apache.http.legacy'",
        'useLibrary "org.apache.http.legacy"',
        "com.google.android.gms:play-services:4.2.42",
        "com.android.support:appcompat-v7:22",
        "com.android.support:support-v13:22",
        "io.socket:socket.io-client:0.8.3",
        "org.apache.sshd:sshd-core:0.6.0",
    ]:
        if token in app_build_text:
            dependency_markers.append(token)

    jitpack_candidates = sorted(set(re.findall(r"com\.github\.[A-Za-z0-9_.-]+:[A-Za-z0-9_.-]+:[A-Za-z0-9_.+-]+", app_build_text)))

    has_java, has_kotlin = list_source_extensions(repo_root)

    print(f"repo_root: {repo_root}")
    print(f"settings_file: {settings_path.name if settings_path.exists() else 'missing'}")
    print(f"root_build_file: {root_build_path.name if root_build_path.exists() else 'missing'}")
    print(f"app_build_file: {app_build_path.relative_to(repo_root) if app_build_path.exists() else 'missing'}")
    print("")
    print("modules:")
    if modules:
        for module in modules:
            print(f"- {module}")
    else:
        print("- none detected")
    print("missing_module_dirs:")
    if missing_modules:
        for module in missing_modules:
            print(f"- {module}")
    else:
        print("- none")
    print("")
    print(f"gradle_wrapper: {wrapper_url or 'unknown'}")
    print(f"agp_version: {agp_version or 'not found'}")
    print(f"kotlin_plugin_version: {kotlin_plugin or 'not found'}")
    print(f"source_mix: java={str(has_java).lower()} kotlin={str(has_kotlin).lower()}")
    print(f"org.gradle.jvmargs: {jvmargs or 'not found'}")
    print("")
    print("android_config:")
    print(f"- compileSdkVersion: {compile_sdk or 'not found'}")
    print(f"- buildToolsVersion: {build_tools or 'not found'}")
    print(f"- minSdkVersion: {min_sdk or 'not found'}")
    print(f"- targetSdkVersion: {target_sdk or 'not found'}")
    print("")
    print("plugin_markers:")
    if plugin_markers:
        for marker in plugin_markers:
            print(f"- {marker}")
    else:
        print("- none")
    print("repository_markers:")
    if repo_markers:
        for marker in repo_markers:
            print(f"- {marker}")
    else:
        print("- none")
    print("dependency_markers:")
    if dependency_markers:
        for marker in dependency_markers:
            print(f"- {marker}")
    else:
        print("- none")
    print("jitpack_candidates:")
    if jitpack_candidates:
        for candidate in jitpack_candidates:
            print(f"- {candidate}")
    else:
        print("- none")

    hints = []
    if jvmargs and "MaxPermSize" in jvmargs:
        hints.append("MaxPermSize is present; expect JDK 17+ daemon startup failures.")
    if jitpack_candidates and "jitpack" not in repo_markers:
        hints.append("GitHub-style dependencies are present without jitpack.io; expect unresolved artifacts.")

    if hints:
        print("")
        print("diagnostic_hints:")
        for hint in hints:
            print(f"- {hint}")

    return 0


if __name__ == "__main__":
    raise SystemExit(main())
