# Tracker

Android app (Kotlin + Jetpack Compose + Room).

## Requirements

- **JDK 17** (AGP 8.13 requires JDK 17 or newer).
- **Android SDK** with:
  - Platform `android-36` (`compileSdk`/`targetSdk` = 36)
  - Build Tools `35.0.0` (AGP 8.13 default)
- Gradle is provided by the wrapper (`gradle/wrapper`, Gradle 8.13); no local Gradle install is needed.
- If your default `java` is newer than Java 23 (Gradle 8.13 does not run on it), point `JAVA_HOME` at a JDK 17 install for the build.

Point Gradle at the SDK with either the `ANDROID_HOME` environment variable:

```bash
export ANDROID_HOME="$HOME/Android/Sdk"
export PATH="$ANDROID_HOME/platform-tools:$PATH"
```

or a `local.properties` file at the repo root (git-ignored):

```properties
sdk.dir=/home/you/Android/Sdk
```

You only need the SDK packages above; the build downloads its Java/Kotlin dependencies from Google Maven and Maven Central.

## Build the debug APK

```bash
./gradlew assembleDebug
```

The APK is written to:

```
app/build/outputs/apk/debug/app-debug.apk
```

Install it on a connected device or emulator with:

```bash
adb install -r app/build/outputs/apk/debug/app-debug.apk
```

## Signing

The debug build is signed with the auto-generated Android debug keystore, so it needs **no signing secrets**. Release signing is not configured in this repository and is out of scope for the current build setup.

## CI

`.github/workflows/build-apk.yml` builds the debug APK on every push to `master`, on pull requests, and on `v*` tags. Each run uploads the APK as the `app-debug` artifact; tag pushes also attach it to the corresponding GitHub Release.
