# Common Failure Patterns

Use this reference when a legacy Android repo fails before tasks even start.

## JDK mismatch before project evaluation

Symptoms:

- Gradle daemon fails before configuring projects
- `Unrecognized VM option 'MaxPermSize=...'`

Interpretation:

- The repo is carrying JVM flags from old Java 7 or 8 era tooling.
- Gradle 5.x plus AGP 3.x usually needs JDK 8 or 11, not 17+.

Action:

- retry with JDK 11 first
- inspect `gradle.properties` for `org.gradle.jvmargs`
- treat this as an environment blocker, not an application code bug

## Android home or prefs directory is not writable

Symptoms:

- `Cannot create directory ...\.android`
- AGP fails during plugin application or metrics initialization

Interpretation:

- The build is blocked by sandboxed or unwritable home-directory paths.
- This is common in CI, sandboxes, containers, or agent environments.

Action:

- redirect `GRADLE_USER_HOME`, `ANDROID_PREFS_ROOT`, and `ANDROID_SDK_HOME` into the repo or another writable path
- rerun the same command before changing project files

## Fabric or Crashlytics plugin bootstrap failure

Symptoms:

- `Failed to apply plugin [id 'io.fabric']`
- `Crashlytics data directory ... could not be created`
- configuration stops before task graph creation

Interpretation:

- The old Fabric plugin is brittle and may assume writable user-home paths.
- It can also fail for repo resolution or toolchain reasons.

Action:

- classify it as a primary blocker
- do not trust follow-up Android DSL errors until the plugin failure is removed or bypassed
- check for `maven.fabric.io`, old Fabric classpaths, and old Crashlytics dependencies

## Cascading Android DSL errors

Symptoms:

- `compileSdkVersion is not specified`
- other Android extension errors after an earlier plugin exception

Interpretation:

- Often a secondary error caused by a plugin failing before the Android block finishes configuring.

Action:

- report the first plugin failure as the main blocker
- mention the DSL error as likely fallout

## Obsolete repositories and dependencies

Markers:

- `jcenter()`
- `maven.fabric.io`
- raw GitHub Maven endpoints
- Support Library 22.x
- very old `com.google.android.gms:play-services`
- Apache Http legacy library

Interpretation:

- Even if the build reaches dependency resolution, it may fail later due to unavailable artifacts or incompatible metadata.

Action:

- separate `configuration-time blockers reached now` from `resolution blockers likely next`
- call out deprecated infrastructure explicitly in the report

## GitHub or JitPack artifacts not resolving

Symptoms:

- dependencies under `com.github.*` fail with `Could not find ...`
- Gradle searched JCenter, Google, or other repos, but not `https://jitpack.io`

Interpretation:

- The dependency coordinates are GitHub-style artifacts that normally resolve via JitPack.
- The repo may be stale if it predates a migration away from JCenter but never added JitPack.

Action:

- check for `com.github.*` coordinates in module dependencies
- check whether `jitpack.io` is present in repositories
- report missing JitPack as the primary dependency-resolution blocker if the build already gets past configuration

## Missing modules in settings

Symptoms:

- `settings.gradle` includes modules that do not exist on disk

Interpretation:

- The checkout is incomplete or the settings file is stale.

Action:

- report the missing module as a repo-local blocker that will surface once earlier blockers are removed
