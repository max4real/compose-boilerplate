# Build & Run APKs

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