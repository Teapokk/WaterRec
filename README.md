# WaterRec

**WaterRec** — Premium Screen Recorder for Android

A professional-grade screen recording application for Android with Kotlin, featuring programmatic UI, MediaProjection recorder, and a floating bubble overlay.

## Features

- 📱 **Floating Bubble Control**: Tap the bubble to expand a menu with recording controls
- 🎬 **Professional Recording**: Built on MediaProjectionManager and MediaRecorder
- 🎨 **Programmatic UI**: All screens built with Kotlin (no layout XML files)
- 🎯 **Premium Theme**: Modern dark theme with gradient UI elements
- ⚙️ **Advanced Settings**:
  - Optional watermark support
  - Screen tap visualization
  - Keyboard input recording
  - App UI hiding (FLAG_SECURE)
- 🔔 **Foreground Service**: Stable background recording with persistent notification
- 🚀 **CI/CD Ready**: GitHub Actions workflow for automated APK builds

## Technical Stack

- **Language**: Kotlin
- **Minimum SDK**: 26 (Android 8.0)
- **Target SDK**: 34 (Android 14)
- **Build System**: Gradle KTS
- **Permissions**:
  - `SYSTEM_ALERT_WINDOW` — Floating bubble overlay
  - `FOREGROUND_SERVICE` — Background recording
  - `FOREGROUND_SERVICE_MEDIA_PROJECTION` — MediaProjection service
  - `RECORD_AUDIO` — Audio capture
  - `POST_NOTIFICATIONS` — Notification display
  - `WRITE_EXTERNAL_STORAGE` (up to API 28) — Video file output

## Architecture

### Core Components

1. **MainActivity** (`MainActivity.kt`)
   - Settings hub with toggle switches
   - Watermark, taps, keyboard, and UI hiding options
   - Launches FloatingBubbleService on startup

2. **FloatingBubbleService** (`FloatingBubbleService.kt`)
   - WindowManager-based floating overlay
   - Black-to-blue gradient bubble (120x120 dp)
   - Expandable menu with recording controls
   - Draggable and clickable

3. **RecorderService** (`RecorderService.kt`)
   - MediaProjectionManager integration
   - MediaRecorder with H.264 video + AAC audio
   - Foreground service with persistent notification
   - Handles start, pause, and stop actions

### Resource Files (XML)

- **colors.xml**: Premium dark theme palette (black #000000, dark-blue #0A192F)
- **strings.xml**: All UI strings, settings labels, notifications
- **themes.xml**: Global Material theme definition
- **AndroidManifest.xml**: Permissions, services, activities

## Building & Running

### Prerequisites

- Android Studio Arctic Fox or later
- JDK 17+
- Gradle 8.0+

### Local Build

```bash
# Clone the repository
git clone https://github.com/Teapokk/WaterRec.git
cd WaterRec

# Build debug APK
./gradlew assembleDebug

# APK output: app/build/outputs/apk/debug/app-debug.apk
```

### Via GitHub Actions

Push to `main` branch or create a pull request. The workflow will automatically:
1. Set up JDK 17
2. Build the APK
3. Upload as a GitHub Artifact (30-day retention)

## Project Structure

```
WaterRec/
├── .github/
│   └── workflows/
│       └── build.yml                    # GitHub Actions CI/CD
├── app/
│   ├── build.gradle.kts                 # App-level Gradle config
│   ├── proguard-rules.pro              # ProGuard/R8 configuration
│   └── src/main/
│       ├── AndroidManifest.xml          # App manifest with permissions
│       ├── kotlin/com/waterrec/recorder/
│       │   ├── MainActivity.kt          # Settings activity
│       │   ├── ui/
│       │   │   └── FloatingBubbleService.kt
│       │   └── recorder/
│       │       └── RecorderService.kt   # Recording engine
│       └── res/
│           ├── values/
│           │   ├── colors.xml           # Theme colors
│           │   ├── strings.xml          # UI strings
│           │   ├── themes.xml           # Material theme
│           │   └── attrs.xml            # Custom attributes
│           └── xml/
│               ├── data_extraction_rules.xml
│               └── backup_descriptor.xml
├── build.gradle.kts                     # Project-level Gradle config
├── settings.gradle.kts                  # Gradle settings
├── gradle.properties                    # Gradle properties
├── .gitignore                           # Git ignore rules
├── .gitattributes                       # Git attributes
└── README.md                            # This file
```

## Permissions Explained

| Permission | Purpose | Scope |
|---|---|---|
| `SYSTEM_ALERT_WINDOW` | Draw floating bubble overlay | API 26+ |
| `FOREGROUND_SERVICE` | Run recording as foreground service | API 26+ |
| `FOREGROUND_SERVICE_MEDIA_PROJECTION` | MediaProjection service type | API 31+ |
| `RECORD_AUDIO` | Capture system/app audio | API 26+ |
| `POST_NOTIFICATIONS` | Display persistent notification | API 33+ |
| `WRITE_EXTERNAL_STORAGE` | Save recordings to storage | API 26-28 only |

## Programmatic UI Pattern

All UI is built programmatically using Android's standard views:

```kotlin
val layout = LinearLayout(this).apply {
    orientation = LinearLayout.VERTICAL
    setBackgroundColor(resources.getColor(R.color.color_surface, null))
}
```

**Benefits**:
- No XML parsing overhead
- Dynamic theme switching
- Reduced APK size
- Runtime flexibility

## Development Workflow

1. **Feature branches**: `feature/name` or `setup/*`
2. **Pull requests** to `main`
3. **CI/CD validation**: GitHub Actions builds APK on every push
4. **Testing**: Manual testing on Android 8.0+ devices

## Known Limitations

- MediaProjection requires user permission each session
- Recording quality depends on device hardware
- Audio capture requires `RECORD_AUDIO` permission
- Watermark rendering not yet implemented (UI ready)

## Future Enhancements

- [ ] Video codec selection (H.264, H.265)
- [ ] Custom watermark rendering
- [ ] Tap indicator visualization
- [ ] Keyboard input overlay
- [ ] Video trimming & editing
- [ ] Cloud storage integration
- [ ] Dark/Light theme toggle

## License

MIT License — See LICENSE file for details

## Support

For issues, questions, or feature requests, please open a GitHub issue.

---

**Built with ❤️ in Kotlin**
