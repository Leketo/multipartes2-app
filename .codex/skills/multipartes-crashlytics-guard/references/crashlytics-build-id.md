# Crashlytics build ID incident

## Symptom

Startup crashed in `py.multipartesapp.activities.LoginActivity` with:

- `io.fabric.sdk.android.services.concurrency.UnmetDependencyException`
- `The Crashlytics build ID is missing`

The exception happened during `Fabric.with(this, new Crashlytics())` inside `onCreate`.

## Root cause

- This repo is legacy Android and keeps the Fabric Gradle plugin disabled for normal debug work.
- `app/build.gradle` enables `io.fabric` only for release or Crashlytics-oriented tasks.
- Without that tooling step, debug builds do not get the Crashlytics build ID expected by the runtime SDK.
- Direct initialization from activities made the whole app startup depend on that missing build artifact.

## Fix applied

- Added `app/src/main/java/py/multipartesapp/utils/CrashlyticsHelper.java`.
- Moved initialization from `LoginActivity` and `Main` into that helper.
- Short-circuited initialization in debug with `BuildConfig.DEBUG`.
- Wrapped runtime initialization in `try/catch` so a misconfigured Crashlytics setup does not block login or main-screen startup.
- Routed user logging through the helper instead of direct `Crashlytics.setUser...` calls.

## Why this shape

- It matches the existing build intent: debug should work without full legacy Fabric tooling.
- It keeps release capable of reporting crashes when properly configured.
- It avoids turning telemetry misconfiguration into a user-facing blocker.

## Related environment note

Repository validation still depends on JDK 8. `Gradle 5.4.1 / AGP 3.5.3` failed under JDK 21 during local verification, which is an independent environment issue.
