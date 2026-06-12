---
name: android-gradle-diagnosis
description: Diagnose Android and Gradle build failures, especially in legacy Android repos. Use when Codex needs to inspect Gradle wrapper, AGP, JDK, SDK, module wiring, deprecated plugins or repositories, sandbox or filesystem blockers, configuration-time failures, and dependency-resolution failures before proposing fixes.
---

# Android Gradle Diagnosis

## Overview

Diagnose Android build failures by separating environment blockers from repo blockers.
Start with static inspection, then run a controlled build with an explicitly chosen JDK and local cache directories.

## Workflow

### 1. Inspect static configuration first

Read these files before changing anything:

- `settings.gradle` or `settings.gradle.kts`
- root `build.gradle` or `build.gradle.kts`
- module `build.gradle` files, especially `app`
- `gradle/wrapper/gradle-wrapper.properties`
- `gradle.properties`
- `local.properties` if present
- `AndroidManifest.xml` for the main app module

Look for:

- declared modules that do not exist on disk
- Gradle wrapper and AGP versions
- Kotlin plugin presence and any `.kt` or `.kts` files
- `org.gradle.jvmargs` flags that are invalid on newer JDKs
- `jcenter()`, Fabric, Crashlytics, raw GitHub Maven repos, or other obsolete infrastructure
- GitHub-style dependencies under `com.github.*`; these often require `https://jitpack.io`
- `useLibrary 'org.apache.http.legacy'`, old support libraries, or very old Play Services

### 2. Run the static collector

Run:

```powershell
python skills/android-gradle-diagnosis/scripts/collect_gradle_context.py .
```

Use that output to summarize:

- modules and missing module directories
- Gradle, AGP, Kotlin, and JVM settings
- legacy repository and plugin markers
- JitPack candidates and obvious dependency-resolution risks
- SDK levels and obvious compatibility risks

### 3. Attempt a controlled build

Prefer a compatible JDK before touching project files.

Rules of thumb:

- Gradle 5.x and AGP 3.5.x usually want JDK 8 or 11.
- For Gradle 5.4.1 and AGP 3.5.3 specifically, prefer JDK 11 as the safe modern runtime ceiling; JDK 17 may fail in Groovy before project evaluation.
- If `gradle.properties` contains `-XX:MaxPermSize`, expect JDK 17+ to fail before project evaluation.
- Treat early plugin failures as primary causes; downstream `compileSdkVersion is not specified` style errors may be cascades.
- If configuration succeeds and `com.github.*` dependencies fail to resolve, check for `jitpack.io` before chasing task-level fallout.
- If `jitpack.io` is present and a `com.github.*` artifact still 404s, verify the artifact URL directly before changing versions; some legacy coordinates are gone and may need a local replacement.
- If a legacy plugin such as Fabric blocks debug or sync but is only needed for release-era tasks, consider gating plugin application instead of forcing a full migration during diagnosis.

On Windows, start with a command like:

```powershell
$repo=(Resolve-Path .).Path
$env:JAVA_HOME='C:\Program Files\Java\jdk-11'
$env:Path="$env:JAVA_HOME\bin;$env:Path"
$env:GRADLE_USER_HOME=(Join-Path $repo '.gradle')
$env:ANDROID_PREFS_ROOT=(Join-Path $repo '.gradle')
$env:ANDROID_SDK_HOME=(Join-Path $repo '.gradle')
.\gradlew.bat :app:assembleDebug --stacktrace
```

If Android or Gradle tries to write under an unwritable sandbox home, classify that as an environment blocker first.

### 4. Classify failures before fixing

Bucket each failure into one of these categories:

- JDK mismatch: daemon or JVM options fail before project evaluation
- sandbox or filesystem issue: `.android`, `.gradle`, `.crashlytics`, or similar home-dir writes fail
- plugin bootstrap failure: Fabric, Crashlytics, old AGP, or third-party plugin fails during `apply`
- repository or dependency resolution failure: JCenter, missing JitPack, or obsolete Maven endpoints
- repo wiring issue: missing modules from `settings.gradle`
- SDK or manifest mismatch: compile SDK, target SDK, build tools, or manifest merge issues

Do not flatten them into one list. Preserve causal order.

### 5. Use the references selectively

- Read `references/common-failure-patterns.md` for recurring legacy Android blockers and how to interpret them.
- Read `references/multipartes2-app-findings.md` when working in this repo or one with similar symptoms.

## Reporting

When reporting findings:

- state the exact build command used
- state the JDK used
- separate the first hard blocker from secondary fallout
- cite the config files that explain each blocker
- say whether the failure is environmental, repo-local, or both

For this kind of repo, it is useful to report:

- `first blocker reached`
- `next blocker likely after that`
- `static risks seen even without a successful build`

