# Alphabet Launcher 🚀

**Alphabet Launcher** is a lightweight, aesthetic, and blazing-fast Android launcher designed for minimalists. It combines a custom A-Z side index navigation system with a stylish torn-paper widget aesthetic, circular quick dock, and real-time app search.

---

## ✨ Features

- **🅰️-🆉 Interactive A-Z Alphabet Bar**:
  - Drag along the right edge of the screen to quickly jump to applications by letter.
  - Features smooth Gaussian curve bending, circular letter bubble indicator, active letter highlighting, and haptic vibration feedback.
- **🎨 Aesthetic Torn-Paper Widget**:
  - **Dynamic Month & Day**: Displays uppercase month (`+ IT'S JULY`) and bold day of the week (`THURSDAY`).
  - **Time-of-Day Tag**: Automatically updates pill badge (`MORNING`, `AFTERNOON`, `EVENING`, `NIGHT`).
  - **Date & Clock**: Large date number (`27`) and continuous live clock (`🕒 09:38 AM`).
- **⭕ Circular Quick Launch Dock**:
  - Vertical list of translucent circular app icon badges positioned on the bottom left for instant access to your daily apps.
- **🔍 Instant App Search**:
  - Built-in search bar that live-filters all installed applications in real-time across all letters.
- **📌 Customizable Favourites**:
  - Long-press any app row or quick dock icon to pin/unpin it or open **App Info** in system settings.
  - Favorites persist across restarts via `SharedPreferences`.
- **🌙 Theme & Edge-to-Edge Ready**:
  - Supports system dark and light modes with proper status/navigation bar window insets handling.

---

## 📱 Screenshots

| Home Screen & Widget | A-Z Navigation Drawer | App Search |
| :---: | :---: | :---: |
| *(Torn-paper widget & quick dock)* | *(Drag along A-Z bar)* | *(Instant app filtering)* |

---

## 🛠️ Tech Stack & Requirements

- **Language**: Java
- **Minimum SDK**: API Level 26 (Android 8.0 Oreo)
- **Target SDK**: API Level 35+ / 37
- **Build System**: Gradle with Version Catalog (`gradle/libs.versions.toml`)
- **Key APIs**:
  - Custom 2D Canvas Drawing (`Canvas`, `Paint`, `ValueAnimator`)
  - `PackageManager` & `ResolveInfo` for app discovery
  - `WindowInsetsCompat` for Edge-to-Edge support
  - Android Haptic Feedback (`Vibrator`, `VibrationEffect`)

---

## 🚀 Getting Started

### Prerequisites
- Android Studio Ladybug (or newer)
- JDK 11 or higher
- Android SDK Platform 34/35/37

### Installation & Build

1. **Clone the Repository**:
   ```bash
   git clone https://github.com/your-username/AlphabetLauncher.git
   cd AlphabetLauncher
   ```

2. **Open in Android Studio**:
   - Open Android Studio and select **Open**.
   - Navigate to the `AlphabetLauncher` folder.

3. **Build & Run**:
   - Connect your Android device or start an emulator.
   - Click **Run** (`Shift + F10`) or run via Gradle CLI:
     ```bash
     ./gradlew assembleDebug
     ```

---

## 📂 Project Structure

```
AlphabetLauncher/
├── app/
│   ├── src/main/
│   │   ├── java/com/example/alphabetlauncher/
│   │   │   ├── AlphabetBarView.java   # Custom View for A-Z sidebar & bending animations
│   │   │   ├── AppInfo.java           # Data model for installed applications
│   │   │   ├── AppRepository.java     # PackageManager helper to query & group apps
│   │   │   └── MainActivity.java      # Main activity, widget UI & drawer overlay logic
│   │   ├── res/
│   │   │   ├── drawable/
│   │   │   │   └── cat_wallpaper.jpg  # Background wallpaper asset
│   │   │   ├── values/
│   │   │   │   ├── colors.xml
│   │   │   │   └── styles.xml
│   │   └── AndroidManifest.xml
│   └── build.gradle
├── gradle/libs.versions.toml
├── build.gradle
└── settings.gradle
```

---

## 🎮 How to Use

1. **Home Screen**:
   - View your date, time, time-of-day tag, and circular quick dock on the left side.
2. **A-Z Sidebar Navigation**:
   - Drag your finger along the right edge of the screen to slide through letters **A to Z**.
   - Lift your finger to view apps matching the selected letter.
3. **Search & All Apps**:
   - Tap the star (**★**) icon at the top of the A-Z bar to open the search bar and browse all apps.
4. **Customizing Quick Dock**:
   - Long-press any app icon or row to **Pin to Quick Dock** / **Unpin from Quick Dock** or open **App Info**.

---


