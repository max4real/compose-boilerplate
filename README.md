# compose-boilerplate

Reusable Android Jetpack Compose + Kotlin boilerplate for quickly starting new projects.
It carries over the foundation layer proven in a production app — networking, DI, auth
token refresh, a design-token theme system, and a set of common widgets — so a new
project doesn't need to rebuild any of that from scratch.

## Stack

- Kotlin 2.2.10, AGP 9.4.0 (classic Kotlin Android plugin + variant API — see note below)
- Jetpack Compose (BOM `2026.02.01`) + Navigation Compose
- Hilt `2.60.1` for DI, via KSP (not kapt)
- Retrofit + Gson, with an OkHttp auth-token-refresh interceptor/authenticator chain
- Coil for image loading
- CameraX + Media3 Transformer for the camera-capture/media-compression widgets

## Project structure

```
app/src/main/java/com/max4real/compose_boilerplate/
├── MainActivity.kt              # @AndroidEntryPoint, hosts AppNavHost
├── di/
│   ├── BoilerplateApplication.kt  # @HiltAndroidApp
│   ├── GlobalModule.kt            # Retrofit/OkHttp/Coil/TokenManager providers
│   └── refresh/                   # Auth interceptor, 401/403 handling, token refresh
├── navigation/
│   └── AppNavHost.kt             # Route registry + NavHost — add screens here
├── shared/
│   ├── config/AppEnvironment.kt  # Reads BuildConfig fields sourced from env.properties
│   ├── devices/                  # Device id/model info (sent on refresh calls)
│   ├── managers/                 # TokenManager (encrypted prefs), ThemePreferenceManager
│   ├── model/                    # ApiResponse<T>, Either, AuthTokenResponse, failures
│   ├── network/                  # Connectivity check
│   ├── util/                     # Haptics, share/clipboard, media compression, spoiler
│   │                               markup, mention regex, link handling, etc.
│   └── widget/                   # Reusable Compose widgets — see below
├── template/                     # Empty per-feature package layout — copy this folder
│                                  # (rename + fill in) when adding a new feature module
└── ui/theme/                     # AppColors design tokens, Material3 theme, typography
```

### `shared/widget/` highlights

- `mediapicker/` — bottom-sheet gallery/file picker (permissions, paging, albums)
- `cameracapture/` — CameraX capture screen used by the media picker
- `spoiler/` — tap-to-reveal spoiler text (pairs with `SpoilerMarkup` in `shared/util`)
- `ohteepee/` — OTP/PIN input boxes
- Misc: `AppDialog`, `CustomBottomSheetPlate`, `CustomAvatar`, `CustomSwitch`, `Shimmer`,
  `TopGradient`/`BotGradient`, `AuthButton`, `VerifiedBadge`, and a few more small ones.

### `template/` — feature module convention

New features are expected to follow one folder per feature, each shaped like:

```
<feature>/
├── data/{api,model,repo}
├── di/
└── ui/{screen,viewmodel,widget}
```

`template/` is that layout with nothing in it yet — copy it, rename the package, and
fill in the files instead of inventing a new structure per feature.

## Getting started

1. **Rename the package** from `com.max4real.compose_boilerplate` to your app's package
   (Android Studio's "Refactor > Rename" on the package works fine here), and update
   `namespace`/`applicationId` in `app/build.gradle.kts`.
2. **Set up `env.properties`**: copy `env.properties.example` to `env.properties` (already
   gitignored) and fill in `API_BASE_URL` / `MEDIA_BASE_URL`. These feed
   `BuildConfig.API_BASE_URL` / `MEDIA_BASE_URL` via `AppEnvironment`.
3. **Wire your first screen**: add a route in `navigation/AppNavHost.kt` and replace the
   `HomePlaceholder` composable.
4. Build: `./gradlew :app:assembleDebug`.

## Notes / known trade-offs

- **AGP 9's "built-in Kotlin" / new DSL is disabled** (`android.builtInKotlin=false`,
  `android.newDsl=false` in `gradle.properties`). The Hilt Gradle plugin and KSP aren't
  compatible with AGP 9's new variant API yet, so this project opts back into the
  classic Kotlin Android plugin + legacy variant API. Revisit this once Hilt/KSP catch up.
- **No Room/database layer** is included — add one per project as needed; several ported
  widgets (e.g. the media picker's "recent files") intentionally use in-memory state
  instead of a persisted store to avoid pulling in a database dependency by default.
- **No multi-account session handling** — `TokenRefreshService` only manages a single
  live token pair. The source app's per-account session coordination was dropped as
  out of scope for a starter template.
- Ported from an existing chat app; token/color names were generalized where already
  reused elsewhere (e.g. `AppColors`, `CustomColor.accentBlue`), but this is a starting
  point, not a finished design system — expect to add tokens as screens need them.
