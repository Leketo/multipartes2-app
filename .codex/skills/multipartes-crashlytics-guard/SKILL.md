---
name: multipartes-crashlytics-guard
description: Preserve the legacy Fabric/Crashlytics startup guard in `multipartes2-app`. Use when touching `LoginActivity`, `Main`, `CrashlyticsHelper`, `app/build.gradle`, or when investigating `The Crashlytics build ID is missing` / `UnmetDependencyException` during app startup.
---

# Multipartes Crashlytics Guard

## Goal

Keep the app launch path working even when Fabric tooling is absent from local debug builds.

## What happened

- `app/build.gradle` only applies `io.fabric` for release-oriented tasks.
- That leaves debug builds without the generated Crashlytics build ID.
- The app used to call `Fabric.with(this, new Crashlytics())` directly in `LoginActivity` and `Main`.
- On launch, Fabric threw `UnmetDependencyException` with `The Crashlytics build ID is missing`, crashing `LoginActivity` before the UI loaded.

Read [references/crashlytics-build-id.md](references/crashlytics-build-id.md) if you need the full incident summary and exact rationale.

## Required guardrails

- Do not reintroduce direct `Fabric.with(...)` calls inside activities for this repo.
- Centralize Crashlytics access through `py.multipartesapp.utils.CrashlyticsHelper`.
- Keep debug startup safe by short-circuiting initialization when `BuildConfig.DEBUG` is `true`.
- Keep release initialization best-effort: if Fabric runtime is present but misconfigured, log and continue instead of crashing the login flow.
- Route user-identification calls through the helper as well; do not assume Crashlytics is initialized.

## Files to check first

- `app/src/main/java/py/multipartesapp/utils/CrashlyticsHelper.java`
- `app/src/main/java/py/multipartesapp/activities/LoginActivity.java`
- `app/src/main/java/py/multipartesapp/activities/Main.java`
- `app/build.gradle`

## Validation

- Reproduce only if needed; the expected failure signature is `UnmetDependencyException` with `The Crashlytics build ID is missing`.
- Run `.\gradlew.bat :app:assembleDebug` only with JDK 8 for meaningful validation in this repo.
- If the build fails under JDK 11+ or 21+, report the environment blocker first; that is separate from the Crashlytics fix.
- Smoke test the launch flow: open `LoginActivity`, verify no fatal crash on startup, and verify navigation to `Main` still works with an existing session or valid login.