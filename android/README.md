# Android Project - Konnecta

This folder contains the native Android implementation using **Kotlin** and **Jetpack Compose**.

## Setup
1. Open this folder in **Android Studio**.
2. Create a `secrets.properties` file in the `android/` directory (it is ignored by Git).
3. Copy the following values from your root `.env.local`:
   ```properties
   SUPABASE_URL=https://your-project-id.supabase.co
   SUPABASE_ANON_KEY=your-anon-key
   ONESIGNAL_APP_ID=your-onesignal-app-id
   ```
4. Wait for Gradle to sync and build the project.

## Project Structure
- `app/src/main/kotlin/com/konnecta/app/`: Kotlin source files.
- `app/src/main/AndroidManifest.xml`: App configuration and permissions.
- `build.gradle.kts`: Build configuration.
