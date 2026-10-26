# BudgetBoss

<img src="app/src/main/ic_launcher-playstore.png" alt="BudgetBoss icon" width="120" align="right">

An Android budgeting app for tracking spending against monthly goals. Users record expenses in custom categories, attach receipt photos, and follow their progress through charts, smart alerts and achievements. It is built with Kotlin and Jetpack Compose, stores data locally with Room, and syncs it to Cloud Firestore, with receipts in Supabase Storage.

Built by a team of four for PROG7313 at Emeris Durban North (2026).

**Demo video:** [Watch on YouTube](https://youtu.be/fsY_WB32ysc)

## Features

- **Accounts:** registration and sign-in with Firebase Authentication.
- **Categories and expenses:** custom categories with colours, icons and optional limits; expenses with date, time range, description and category, validated on entry.
- **Receipt photos:** capture or pick a receipt image, preview it, and store it in Supabase Storage.
- **History and totals:** filter expenses by date range and category, sort them, and see totals per category.
- **Budget goals:** minimum and maximum monthly goals, remaining budget and projected month-end spending.
- **Charts and dashboard:** daily spending charts with category segments, goal progress and overspending highlights.
- **Smart alerts:** configurable thresholds that warn before the monthly maximum is exceeded.
- **Recurring expenses:** reusable templates that generate scheduled entries.
- **Gamification:** badges, logging streaks, XP and levels.

## Tech stack

| Area | Technologies |
| --- | --- |
| App | Kotlin, Jetpack Compose, Material 3, Navigation Compose, ViewModel |
| Data | Room (local), Cloud Firestore (sync), Supabase Storage (receipts) |
| Auth | Firebase Authentication |
| Build | Gradle (Kotlin DSL), KSP; Android 7.0+ (API 24), targets API 36 |
| Testing and CI | JUnit, GitHub Actions |

## Architecture

- **UI:** Compose screens grouped by feature (auth, expenses, management, insights, settings) inside a shared app shell with central navigation.
- **State:** a ViewModel exposes UI state built by a dedicated state factory.
- **Data:** a repository coordinates the Room database, Firestore sync and Supabase receipt storage, keeping persistence and networking out of the UI.

## Getting started

### Prerequisites

- Android Studio with Android SDK 36 and JDK 17
- A Firebase project with email/password authentication and Firestore
- A Supabase project with a storage bucket for receipts

### Run locally

1. Open the repository in Android Studio and let Gradle sync.
2. Add your Firebase config for the package `com.example.budgetboss` at `app/google-services.json`.
3. Copy `local.properties.example` to `local.properties` and fill in your Supabase URL, anon key and receipt bucket. Never put a Supabase service-role key in the app.
4. Build and run on an emulator or device:

```bash
./gradlew :app:assembleDebug
```

`google-services.json` and `local.properties` are ignored by Git.

## Testing

```bash
./gradlew :app:testDebugUnitTest
```

Unit tests cover currency and date formatting, password validation, default categories and goals, dashboard warning and progress calculations, streaks and achievement state. The GitHub Actions workflow builds the debug APK and runs the unit tests; it is set to run manually.

## Project structure

```text
app/src/main/java/com/example/budgetboss/
  feature/       Screens: auth, expenses, management, insights, settings
  data/          Room entities and DAO, Firestore and Supabase data sources, repository
  presentation/  ViewModel and UI state
  navigation/    App shell, routes and navigation host
  ui/            Shared components and theme
```

## Team

| Member | Contribution |
| --- | --- |
| Khayden Chetty | Firebase authentication, login and registration, authenticated navigation shell |
| Rivaan Kawulesar | Receipt photos, Supabase storage, filtered history |
| Stephane Nsengiyumva | Categories, expense entry, validation and Firestore sync |
| Zenwyn Naicker | Goals, totals, charts, dashboard progress, smart features and achievements |

Testing, documentation, integration and review were shared across the team.

## Acknowledgements

Libraries, documentation and adapted code patterns are credited in [CODE_ATTRIBUTION.txt](CODE_ATTRIBUTION.txt).
