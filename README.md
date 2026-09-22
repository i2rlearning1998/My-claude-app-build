# MotionForge

An original Android video/motion-graphics editor, built in the spirit of apps like Alight
Motion (import clips, arrange them on a timeline, adjust and export) but with its own name,
branding, and implementation — not a clone of any commercial app's code, assets, or UI.

## Why not a literal "Alight Motion" clone

Reproducing Alight Motion's name, logo, or UI/asset files would be copyright/trademark
infringement, and its full feature set (a custom C++ rendering engine, vector shape
animation, motion tracking, etc.) represents years of engineering that isn't realistic to
fully replicate in one pass. This project instead implements a solid, original core video
editor in the same category, designed to be extended.

## Feature set (MVP)

- Project list (create / delete), persisted locally with Room
- Import video and photo clips via the system Photo Picker (no storage permissions needed)
- Horizontal timeline: reorder, delete, and trim clips (in/out points)
- Per-clip preview (ExoPlayer for video, native bitmap for images)
- Per-clip color adjustments: brightness, contrast, saturation
- Per-clip text overlay (position: top / center / bottom)
- Export the full timeline to a single MP4 via Media3 Transformer, then share it

## Tech stack

- Kotlin + Jetpack Compose (Material 3), MVVM with `ViewModel` + `StateFlow`
- Navigation Compose for Home → Editor → Export
- Room (+ kotlinx.serialization for the clip list) for project persistence
- Media3 (ExoPlayer for preview, Media3 Transformer/effects for export)
- Gradle version catalog (`gradle/libs.versions.toml`)

## Project layout

```
app/src/main/java/com/motionforge/editor/
  data/       Room entities, DAO, repository, domain models (Project, Clip)
  editing/    MediaProbe, frame-size probing, Transformer/effects pipeline (export)
  ui/home/    Project list screen
  ui/editor/  Timeline, preview player, filter/overlay panel
  ui/export/  Export progress + share screen
  ui/navigation/ NavHost wiring the three screens together
```

## Building

The code was written in a network-restricted sandbox that couldn't reach Google's Maven repo,
so it couldn't be compiled there. It has since been built successfully by the
`.github/workflows/build-apk.yml` GitHub Actions workflow (`./gradlew assembleDebug` on a
GitHub-hosted runner) — see the Actions tab for the latest run and its `motionforge-debug-apk`
artifact.

To build locally:

1. Open the project root in Android Studio (Koala/2024.1+ recommended) and let it sync — it
   will download the Android SDK components and the Gradle/AndroidX/Media3 dependencies
   listed in `gradle/libs.versions.toml`.
2. Run on a device/emulator running Android 8.0 (API 26) or newer.

Or from the command line once you have an Android SDK installed and `ANDROID_HOME` set:

```
./gradlew assembleDebug
```

## Suggested next steps

- Live filter/overlay preview (currently filters/overlay only apply at export; preview shows
  trim only)
- Audio track import and volume/mute per clip
- Keyframe-based property animation (position/scale/rotation/opacity over time) — the
  feature closest to Alight Motion's signature workflow, and the biggest follow-up
- Drag-to-reorder timeline (currently move-left/move-right buttons)
- Unit tests for the data layer and `ClipEffects` mapping
