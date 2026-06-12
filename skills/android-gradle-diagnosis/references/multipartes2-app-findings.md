# multipartes2-app Findings

This reference captures the blockers, fixes, and verified outcomes found while diagnosing this repo.

## Static toolchain summary

- `settings.gradle` originally declared `:app` and `:socketchat`
- `:socketchat` was missing on disk and unused in the repo
- Gradle wrapper is `5.4.1`
- AGP is `3.5.3`
- Kotlin is not configured
- `app` uses Java 8 source and target compatibility
- `gradle.properties` originally included `-XX:MaxPermSize=512m`
- app dependencies included GitHub-style coordinates for `android-pdf-viewer` and `Android-SpinKit`
- app also applies the legacy `io.fabric` plugin and still uses old Support Library 22.x APIs

## Build outcome that is now verified

### Verified success

Command shape:

- use JDK 11
- use repo-local `GRADLE_USER_HOME`
- use repo-local `ANDROID_SDK_HOME`
- run `.\gradlew.bat :app:assembleDebug --no-daemon`

Result:

- `:app:assembleDebug` completed successfully

Interpretation:

- for this legacy stack, JDK 11 is a safe Gradle runtime
- the generated APK path is the normal AGP 3.5.x debug output under `app/build/outputs/apk/debug`

### Verified failure with JDK 17

Command shape:

- switch `JAVA_HOME` to JDK 17
- keep the same repo-local Gradle and Android homes
- run `.\gradlew.bat :app:assembleDebug --no-daemon`

Result:

- Gradle fails before project evaluation
- Groovy crashes with `Could not initialize class org.codehaus.groovy.vmplugin.v7.Java7`

Interpretation:

- do not move this repo to JDK 17 without upgrading Gradle and AGP
- keeping Java 8 bytecode for app compilation is fine, but the build runtime should stay on JDK 11 for now

## Fix sequence that unblocked the build

### 1. Modern-JDK daemon args

- removed `-XX:MaxPermSize=512m` from `gradle.properties`

Why:

- that flag is incompatible with modern JDK runtimes and was an early blocker in non-Java-8 environments

### 2. Repository wiring

- added `mavenCentral()`
- added `https://jitpack.io`
- kept legacy repos in place because the app still depends on them

Why:

- `Android-SpinKit` resolves once JitPack is present
- the repo still needs a mixed legacy repository set to compile

### 3. Missing module cleanup

- removed `:socketchat` from `settings.gradle`

Why:

- the module was missing on disk and no code references remained in the repo

### 4. Fabric plugin gating

- changed `app/build.gradle` so `io.fabric` is only applied for release or explicit Crashlytics-oriented tasks

Why:

- the legacy Fabric plugin is not needed for `assembleDebug`
- disabling it for debug and sync avoids environment-sensitive failures while preserving release-era behavior

### 5. Dead PDF dependency replacement

- `com.github.barteksc:android-pdf-viewer:2.8.2` still did not resolve even after adding JitPack
- replaced it with a local class at `com.github.barteksc.pdfviewer.PDFView`
- the replacement keeps the existing `fromUri(...).load()` call shape and renders PDFs with `PdfRenderer`

Why:

- the artifact is no longer reliably available from the configured repositories
- usage in this app only needed a minimal API surface, so a local replacement was lower risk than a broader UI rewrite

### 6. Support Library 22 compatibility cleanup

- replaced `android.support.annotation.RequiresApi` with framework `@TargetApi`
- replaced `ActivityCompat.checkSelfPermission(...)` with `context.checkSelfPermission(...)`

Why:

- Support Library 22 does not provide those newer support annotations and helper methods
- `minSdkVersion` is effectively 23 in this project, so framework permission checks are valid

## Remaining repo risks after a successful debug build

- `jcenter()` is still present
- Fabric Maven repo is still present
- a raw GitHub Maven repo is still present
- app dependencies include very old support libraries and Play Services
- `useLibrary 'org.apache.http.legacy'` is enabled
- many screens still use `ActionBarActivity`
- the repo includes Maps keys, Fabric key material, and a keystore file
- release builds that still require the Fabric plugin should be validated separately

## Practical guidance for future runs

1. Prefer JDK 11 for Gradle runtime in this repo
2. Do not enforce JDK 17 on Gradle 5.4.1 or AGP 3.5.3
3. If a `com.github.*` artifact still fails after adding JitPack, verify the artifact URL before assuming repository order is the only issue
4. If the missing artifact is dead and the app only uses a tiny API surface, a local replacement under the same package can be the lowest-risk recovery path
