# Build & Run APKs

## CI/CD for Firebase App Distribution

```
./gradlew assembleDevRelease appDistributionUploadDevRelease
```

## Profile APK:

```
./gradlew assembleDevProfile
```

Out Put
app/build/outputs/apk/dev/profile/app-dev-profile.apk

```
./gradlew assembleProdProfile
```

Out Put
app/build/outputs/apk/prod/profile/app-prod-profile.apk

## Release AAB for Play Store:

```
./gradlew bundleProdRelease
```

Output:
app/build/outputs/bundle/prodRelease/app-prod-release.aab

## Install Profile

```
./gradlew installDevProfile
./gradlew installProdProfile
```

## SHA KEYS

```
./gradlew signingReport
```

----------------------------------------------------------------------------------------

## Build Flavors (environment)

Two product flavors under the `environment` dimension: `dev` and `prod`.

- dev: applicationId `com.xsphere.roomchatandroid.dev`, app name "Room Chat Dev", icon
  `ic_launcher_v2_dev` / `ic_launcher_v2_dev_round`
- prod: applicationId `com.xsphere.roomchatandroid`, app name "Room Chat", icon `ic_launcher_v2` /
  `ic_launcher_v2_round`

Combine with build type + flavor, e.g.:
./gradlew assembleDevRelease
./gradlew assembleProdRelease
./gradlew assembleDevProfile
./gradlew assembleProdProfile

## Firebase (per flavor)

Each flavor has its own Firebase project and its own `google-services.json`:

- prod: `app/src/prod/google-services.json` -> Firebase project `roomchatnative` (package
  `com.xsphere.roomchatandroid`)
- dev: `app/src/dev/google-services.json` -> Firebase project `roomchatnativedev` (package
  `com.xsphere.roomchatandroid.dev`)

Crashlytics has been tested and confirmed working on both flavors/projects.

## Base URLs / Env config

Base URLs, media URL, and MQTT host are no longer hardcoded in
`AppEnvironment.kt`. They come from `BuildConfig` fields, which are populated
per flavor in `app/build.gradle.kts` from `env.properties` (repo root, not
committed as secrets in code):

DEV_API_BASE_URL / DEV_MEDIA_BASE_URL / DEV_MQTT_HOST
PROD_API_BASE_URL / PROD_MEDIA_BASE_URL / PROD_MQTT_HOST

`AppEnvironment.kt` just exposes `BuildConfig.API_BASE_URL`,
`BuildConfig.MEDIA_BASE_URL`, `BuildConfig.MQTT_HOST` — update values in
`env.properties`, not in code.
