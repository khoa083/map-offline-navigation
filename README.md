<h1 align="center">Kblack: Offline Map Navigation (MVP)</h1>

<p align="center">
  <img align="center" src="app/src/main/res/mipmap-xxxhdpi/ic_launcher.png" alt="NerdCalci Icon" width="192" height="192">
</p>

<p align="center">
  <strong>Kblack: Offline Map Navigation is an offline navigation app for Android that utilizes the GraphHopper library for routing algorithms and MapLibre for map rendering.</strong>
</p>

<!-- <p align="center">
  <a href="README.md">Việt Nam</a> | <a href="README.en.md">English</a>
   🟢 Done
</p> -->

<div align="center">
  <!-- <a href="https://github.com/khoa083/map-offline-navigation/">
    <img src="https://gitlab.com/IzzyOnDroid/repo/-/raw/master/assets/IzzyOnDroid.png" height="80" alt="Get it at IzzyOnDroid">
  </a> -->
  <a href="https://github.com/khoa083/map-offline-navigation/">
    <img src="https://github.com/machiav3lli/oandbackupx/raw/034b226cea5c1b30eb4f6a6f313e4dadcbb0ece4/badge_github.png" alt="Get it on GitHub" height="80">
  </a>
</div>

<p align="center">
  <strong>⚠️ Currently only supports Vietnam.</strong>
  Want your region? <a href="https://github.com/khoa083/map-offline-navigation/issues">Open an issue →</a>
</p>

## Screenshots

<div align="center">
  <img src="https://github.com/khoa083/mapdata/blob/main/screen/1_map.png?raw=true" width="170" vertical-align="middle"/>
  <img src="https://github.com/khoa083/mapdata/blob/main/screen/2_map.png?raw=true" width="170" vertical-align="middle"/>
  <img src="https://github.com/khoa083/mapdata/blob/main/screen/3_map.png?raw=true" width="170" vertical-align="middle"/>
  <img src="https://github.com/khoa083/mapdata/blob/main/screen/4_map.png?raw=true" width="170" vertical-align="middle"/>
  <img src="https://github.com/khoa083/mapdata/blob/main/screen/5_map.png?raw=true" width="170" vertical-align="middle"/>
  <img src="https://github.com/khoa083/mapdata/blob/main/screen/screen-20260421-162624-1776763553324.gif?raw=true" width="200"/>
</div>

## Features

* View maps offline without an internet connection.
* Supports rotate, zoom, and 2D/3D compass mode toggles.
* Find routes using the GraphHopper.
* Fully offline navigation — no internet required after map download.
* Auto-recalculate route when off track (> 30m deviation).
* Resume interrupted map downloads — no re-download needed.
* Supports car, motorcycle profiles.
  
## Project Status

| Feature | Status |
| :--- | :---: |
| **Map Theme** | 🟡 In Progress |
| **Voice Guidance** | 🟡 In Progress |
| **Speed Limits** | 🔴 Planned |
| **Lock Screen Navigation** | 🔴 Planned |
| **POI Search** | 🔴 Planned |

## Credits

* [GraphHopper](https://www.graphhopper.com/) for the routing engine.
* [MapLibre](https://maplibre.org/) for the map rendering library.
* [OpenStreetMap](https://www.openstreetmap.org/) contributors for the map data.
* This project is referenced from and inspired by [PocketMaps](https://github.com/junjunguo/PocketMaps) by [junjunguo](https://github.com/junjunguo). It has been modernized and completely rewritten using **Clean Architecture**, **MVVM**, **Kotlin**, and **Jetpack Compose**.

## Architecture

The project is built with **Clean Architecture** and follows the **MVVM (Model-View-ViewModel)** pattern to ensure scalability, maintainability, and ease of testing.

* **UI Components**:
    * **Jetpack Compose** (Declarative UI)
    * **Material Design 3**
* **Dependency Injection**: Hilt
* **Asynchronous Processing**: Coroutines & Flow
* **Navigation**: Navigation Compose
* **Local Data**: Room Database & DataStore (Preferences)
* **Remote Data**: Retrofit 2 + OkHttp + Moshi (JSON)
* **Routing Engine**: GraphHopper
* **Map Rendering**: MapLibre Compose
* **Other Libraries**:
    * **WorkManager**: Background tasks processing
    * **Timber**: Advanced logging
    * **Chucker**: In-app HTTP inspection (Debug)
    * **LeakCanary**: Memory leak detection
    * **Konfetti**: Particle system for celebratory effects
    * **Commonmark**: Markdown rendering support

## Build

### Requirements

* [Android Studio](https://developer.android.com/studio)
* Java 21 or higher

### Steps

1. Clone the repository:
   ```bash
   git clone https://github.com/khoa083/map-offline-navigation.git
   cd map-offline-navigation 
   ```
2. Open the project in Android Studio

3. Sync Gradle and run the app on a device or emulator

### Running tests

Run all unit tests:

```bash
./gradlew :app:testDebugUnitTest
```

> **Note:** After execution, the HTML report can be found at:  
> `app/build/reports/tests/testDebugUnitTest/index.html`
<img width="500" alt="image" src="https://github.com/user-attachments/assets/8f607d65-8b50-494c-bcdb-a061008649d4" />

Run Jacoco:

```bash
./gradlew :app:jacocoTestReportAll
```

> **Note:** After execution, the HTML report can be found at:  
> `app/build/reports/jacoco/all/html/index.html`


## Contributing
Pull requests and issue reports are welcome. Help us improve Kblack: Offline Map Navigation!

## License
This project is licensed under the MIT License - see the [LICENSE](LICENSE) file for details.
