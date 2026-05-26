# KONNECTA - Android App

Nativa Android application for the KONNECTA project, built with Kotlin and Jetpack Compose. This app brings the full experience of the KONNECTA PWA to a native mobile environment with enhanced performance and smooth interactions.

## 🚀 Tech Stack

- **Language:** Kotlin
- **UI Framework:** Jetpack Compose (Modern, declarative UI)
- **Backend:** [Supabase](https://supabase.com/) (Authentication, PostgREST, Storage)
- **Push Notifications:** [OneSignal](https://onesignal.com/)
- **Image Loading:** [Coil](https://coil-kt.github.io/coil/)
- **Dependency Management:** Gradle (Kotlin DSL)
- **Architecture:** MVVM (Model-View-ViewModel)

## ✨ Key Features

- **Dashboard:** Interactive weekend view with quick status voting (Sí, No, Potser) and detailed comments.
- **Activity Management:** Propose new plans, edit existing ones, join activities, and even add a "+1" for guests.
- **Weather Prediction:** Real-time weekend weather forecast with a detailed 3-day breakdown.
- **Hall of Fame:** Complete group rankings based on historical attendance.
- **Group Management:** Create new groups, switch between them, and manage members/roles (Admin only).
- **Group Invites:** Generate and share invite links with tokens that expire every 48 hours.
- **Profile Management:** Edit your display name and upload a profile picture directly to Supabase Storage.
- **Native Experience:** Dark/Light mode support (persistent), haptic feedback, and gesture-driven navigation.

## 📂 Project Structure

```text
android/app/src/main/kotlin/com/konnecta/app/
├── data/
│   ├── model/       # Data classes and UI state models
│   └── remote/      # Supabase services (Auth, Activity, Group, etc.)
├── ui/
│   ├── components/  # Reusable UI widgets and BottomSheets
│   ├── screens/     # Full-screen Composables (Dashboard, Login, etc.)
│   └── theme/       # Material3 theme, colors, and typography
├── utils/           # Date formatting, sharing, and calendar utilities
├── KonnectaApplication.kt # App entry point & initialization
└── MainActivity.kt        # Main navigation and state orchestration
```

## 🛠 Setup & Development

### Prerequisites
- Android Studio (Latest stable version)
- JDK 17 or higher
- Android SDK 26+ (Targeting 34)

### Configuration
1. Create a `secrets.properties` file in the `android/` root directory (copy from `secrets.properties.example`).
2. Add your Supabase and OneSignal credentials:
   ```properties
   SUPABASE_URL=your_supabase_url
   SUPABASE_ANON_KEY=your_supabase_anon_key
   ONESIGNAL_APP_ID=your_onesignal_app_id
   BASE_URL=https://your-custom-domain
   ```

### Running the app
- Connect an Android device or start an emulator.
- Click **Run 'app'** in Android Studio or use the CLI:
  ```bash
  ./gradlew assembleDebug
  ```

## 🔐 Permissions
- **Internet:** Required for backend synchronization.
- **Post Notifications:** Required for receiving OneSignal push alerts.
- **Read External Storage:** Required for profile picture uploads.
- 
---
Built with ❤️ by the KONNECTA team.
