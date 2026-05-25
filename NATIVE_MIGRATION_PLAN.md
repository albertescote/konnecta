# Migration Plan: Konnecta to True Native (iOS & Android)

## Background & Motivation
Konnecta is currently a Progressive Web App (PWA) built with Next.js, React, Tailwind CSS, Supabase, and OneSignal. To achieve the absolute best performance, native platform gestures, and deep system integration, we will migrate the frontend to **True Native** applications. This means developing two separate applications: one in **Swift (SwiftUI)** for iOS and another in **Kotlin (Jetpack Compose)** for Android.

## Scope & Impact
- **Backend (No Change):** Supabase remains the single source of truth for the database, RLS policies, and authentication.
- **Frontend (Rewrite x2):** The Next.js frontend will be completely rewritten twice—once for iOS and once for Android.
- **Notifications (Migration):** OneSignal Web Push will be replaced with the native OneSignal SDKs for iOS and Android.

## Proposed Architecture
The architecture involves two distinct native clients interacting directly with the Supabase backend:

1. **Backend & Data Layer (Supabase)**
   - **Database:** Unchanged PostgreSQL with RLS.
   - **Auth:** Supabase Auth accessed via native SDKs (`supabase-swift` and `supabase-kt`). Deep linking (Universal Links for iOS, App Links for Android) will handle OAuth and Magic Links.
2. **iOS Native Client (Swift)**
   - **UI Framework:** **SwiftUI** for a modern, declarative UI that perfectly matches the iOS ecosystem while adhering to the "rounded-3xl" design language.
   - **Architecture/State:** MVVM (Model-View-ViewModel) or TCA (The Composable Architecture) to manage multi-tenant state and group scoping.
3. **Android Native Client (Kotlin)**
   - **UI Framework:** **Jetpack Compose** for building the UI declaratively, matching the design system on Android devices.
   - **Architecture/State:** MVVM with Kotlin Coroutines and StateFlow for managing the app state.
4. **External Services**
   - **Push Notifications:** OneSignal native SDKs.
   - **Weather:** Open-Meteo REST API consumed via `URLSession` (iOS) and `Retrofit` or `Ktor` (Android).
   - **Calendar:** Native EventKit (iOS) and Calendar Provider (Android).

## Required Components Map
The Next.js components will be translated into their respective native counterparts:

| Web Component | iOS (SwiftUI) | Android (Jetpack Compose) |
| :--- | :--- | :--- |
| `SwipeContainer` | `TabView` (PageTabViewStyle) | `HorizontalPager` |
| `PlansHub` / lists | `List` or `ScrollView` + `LazyVStack` | `LazyColumn` |
| Modals | `.sheet` or `.presentationDetents` | `ModalBottomSheet` |
| Forms | `Form`, `TextField`, `DatePicker` | `OutlinedTextField`, `DatePickerDialog` |
| Pull-to-Refresh | `.refreshable` | `PullRefreshIndicator` |

## Estimated Cost & Timeline
Developing two separate native applications significantly increases the timeline and cost compared to cross-platform solutions.

- **Estimated Timeline:** 12 to 16 Weeks (Assuming two Senior Developers, one iOS, one Android)
  - **Weeks 1-2:** Project scaffolding, Supabase SDK integration, Auth, and Deep Linking for both platforms.
  - **Weeks 3-6:** Core Domain implementation (Groups, Weekends, Voting, UI structure).
  - **Weeks 7-9:** Third-party integrations (OneSignal Native, Weather API, Native Calendar, Sharing).
  - **Weeks 10-12:** UI Polish, custom native animations, Dark Mode, and performance tuning.
  - **Weeks 13-16:** Extensive QA, Beta testing (TestFlight & Google Play Console), bug fixing, and Store submission.
- **Estimated Cost:** $40,000 – $70,000 USD (Assuming ~700-800 combined hours for two specialized developers).

## Phased Implementation Plan

### Phase 1: Foundation & Setup (Dual Track)
1. Initialize Xcode (iOS) and Android Studio (Android) projects.
2. Integrate `supabase-swift` and `supabase-kt`.
3. Configure Universal Links (iOS) and App Links (Android) for secure invite tokens.

### Phase 2: State & Core UI
1. Setup local state management (MVVM).
2. Build the main dashboard structure (Swiping between Weekend and Plans Hub views).
3. Implement the `WeekendSelector` and the `VotingSection`.

### Phase 3: Features & Integrations
1. Build the `ActivityBoard` and the activity creation flows.
2. Implement the `HallOfFame` and `AttendanceList`.
3. Integrate Open-Meteo for the `WeatherCard`.

### Phase 4: Native Capabilities
1. Integrate OneSignal native SDKs.
2. Implement native sharing (UIActivityViewController on iOS, Intent.ACTION_SEND on Android) for WhatsApp integration.
3. Add native "Add to Calendar" functionality.

### Phase 5: Release
1. Set up CI/CD workflows for both platforms (e.g., fastlane, GitHub Actions).
2. Generate all required App Store and Google Play assets.
3. Submit apps for review.

## Verification & Rollback
- **Verification:** Continuous internal testing via TestFlight (iOS) and Google Play Internal Testing (Android). Feature parity with the PWA is the primary acceptance criteria.
- **Rollback / Migration:** The Next.js PWA will remain operational. Users can migrate to the native apps seamlessly as the Supabase backend handles all state.
