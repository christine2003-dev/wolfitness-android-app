# WolFitness Android App Documentation

## Overview

WolFitness is a native Android fitness application written in Java. It guides a user through onboarding, account creation, profile setup, goal selection, workout browsing, workout timing, completion tracking, profile review, BMI display, and motivational notifications.

The app uses Firebase Authentication for login/signup and Cloud Firestore for user profiles, preferences, workouts, exercises, stats, and completed workout records. It also uses Android WorkManager and local notifications to send motivational reminders.

## Technology Stack

- Language: Java
- UI: Android XML layouts with AppCompat, Material Components, ConstraintLayout, and Edge-to-Edge APIs on some screens
- Build system: Gradle Kotlin DSL
- Minimum SDK: 24
- Target SDK: 35
- Firebase:
    - Firebase Authentication
    - Cloud Firestore
    - Firebase Storage dependency included
    - Firebase Analytics dependency included
    - Firebase Realtime Database dependency included
- Background work: AndroidX WorkManager
- Image loading: Picasso

## Project Structure

```text
app/src/main/java/com/example/wolfitness/
  activities/       Activity classes, one main screen per file
  models/           Firestore and app data models
  repositories/     Firebase access wrappers for user, goals, stats, preferences
  utils/            Firebase helper, notification helper, work scheduler
  workers/          WorkManager worker for motivational notifications
  network/          Placeholder API service/client classes
  adapters/         Placeholder adapter class

app/src/main/res/
  layout/           XML layouts for all screens and list items
  drawable/         Icons, gradients, workout images, onboarding assets
  raw/              Workout completion sound
  values/           Strings, colors, themes, fonts
```

## App Architecture

The app follows a simple Activity-based architecture:

- Each screen is implemented as a separate `AppCompatActivity`.
- XML layout files define the screen UI.
- Navigation is handled through explicit `Intent`s.
- Firebase calls are made asynchronously with success/failure listeners.
- Some Firebase logic is wrapped in repositories, mainly `UserRepository`.
- Some screens still access Firebase directly, especially profile registration, goals, home data loading, and workout screens.
- `SharedPreferences` is used as a fallback/cache for legacy profile data and local notification history.

This is not a strict MVVM architecture. There are no ViewModels, LiveData, Room database, or dependency injection. The code is closer to a layered Activity + Repository pattern, with business logic mostly inside Activity classes.

## Main Components

### Activities

`SplashActivity`
- Launcher activity.
- Disables night mode.
- Creates the notification channel.
- Requests Android 13+ notification permission.
- Schedules motivational notifications.
- Navigates to onboarding when the user taps Get Started.

`Onboarding1Activity`
- First onboarding screen.
- Navigates to `Onboarding2Activity`.

`Onboarding2Activity`
- Second onboarding screen.
- Navigates to `SignupActivity`.

`SignupActivity`
- Creates a Firebase Auth account with email/password.
- Collects name, phone, email, and password.
- Validates email, password length, and phone digits.
- Creates a `users/{uid}` Firestore document.
- Creates a user preferences document.
- Marks the user as profile incomplete.
- Navigates to `RegisterActivity`.

`LoginActivity`
- Logs in through Firebase Auth.
- Supports password visibility toggling.
- Sends password reset emails.
- Checks Firestore user profile completeness.
- Navigates to `RegisterActivity` if the profile is incomplete.
- Navigates to `HomeActivity` if the profile is complete.

`RegisterActivity`
- Collects or edits profile details: gender, date of birth, weight, and height.
- Requires an authenticated Firebase user.
- Saves profile data to `users/{uid}`.
- Calculates and stores age.
- Saves a BMI/stat snapshot in `user_stats`.
- Also writes legacy values to `SharedPreferences`.
- In normal registration mode, navigates to `GoalSelectionActivity`.
- In edit mode, returns to the previous screen after saving.

`GoalSelectionActivity`
- Lets the user choose one goal:
    - Improve Shape
    - Lean & Tone
    - Lose a Fat
- Saves the selected goal to the current user document in Firestore.
- Navigates to `SuccessRegisterActivity`.

`SuccessRegisterActivity`
- Shows the registration success/welcome screen.
- Loads the user name from intent, Firestore, or fallback value.
- Navigates to `HomeActivity`.

`HomeActivity`
- Main home screen after login/profile setup.
- Requires an authenticated Firebase user.
- Loads the user's name from Firestore, with `SharedPreferences` fallback.
- Shows workout cards/sections from the layout.
- Provides bottom navigation to home, workout details, and profile.
- Opens `NotificationActivity` from the notification icon.
- Opens the external WolFitness shop URL: `https://wolfitness-frontend.vercel.app/`.
- "View More" workout buttons open `WorkoutDetails1Activity`.

`WorkoutDetails1Activity`
- Loads a workout from Firestore collection `workouts`.
- Uses `WORKOUT_ID` intent extra, defaulting to `fullbody_workout_1`.
- Loads exercises from Firestore collection `exercises`, filtered by `workoutId`.
- Orders exercises by `setNumber` and `orderInSet`.
- Displays exercise groups by set.
- Loads remote images with Picasso or local drawable fallbacks.
- Starts `WorkoutTimerActivity` for a full workout or selected exercise.
- Allows changing displayed difficulty locally through a dialog.

`WorkoutTimerActivity`
- Shows workout/exercise details.
- Loads either:
    - Workout data from `workouts/{workoutId}`, or
    - Exercise data from `exercises/{exerciseId}`.
- Provides manual timer inputs for hours, minutes, seconds.
- Auto-fills timer values when an exercise duration is formatted like `mm:ss` or `hh:mm:ss`.
- Supports start, pause, and resume.
- Plays `res/raw/completion_sound.mp3` and vibrates on completion.
- Saves completion data to Firestore collection `completed_workouts`.
- Opens YouTube videos for known exercise names.
- Navigates to `WorkoutCompletionActivity` when the timer finishes.

`WorkoutCompletionActivity`
- Shows a congratulation screen.
- Uses Firebase user display name or email prefix as a fallback name.
- Navigates back to `HomeActivity`.

`ProfileActivity`
- Requires an authenticated Firebase user.
- Loads user profile from Firestore through `UserRepository`.
- Displays name, goal, height, weight, age, BMI value, and BMI category.
- Calculates BMI locally.
- Loads notification preference.
- Lets user toggle popup notifications and saves preference to Firestore and `SharedPreferences`.
- Opens `RegisterActivity` in edit mode for profile updates.
- Dynamically adds a logout option.
- Clears local preferences and returns to `LoginActivity` on logout.
- Some profile menu rows currently only show Toast messages.

`NotificationActivity`
- Displays locally saved notification history.
- Reads JSON notification objects from `SharedPreferences` named `notifications`.
- Shows each message with relative time.

`ProgressActivity`, `SettingsActivity`, `ChallengeDetailActivity`, `CompletionActivity`
- Layout-backed placeholder screens with minimal logic.

### Models

`User`
- Represents a user profile stored in `users/{uid}`.
- Fields include name, email, phone, gender, date of birth, weight, height, goal, age, profile image URL, notification flag, creation time, and last login time.
- Includes Firestore conversion helpers and BMI helpers.

`UserPreferences`
- Represents notification, reminder, unit, and theme preferences.
- Defaults:
    - notifications enabled
    - workout reminders enabled
    - reminder time `18:00`
    - weight unit `kg`
    - height unit `cm`
    - theme `system`

`UserStats`
- Represents a historical body-stat record.
- Stores user ID, weight, height, BMI, and recorded timestamp.

`Workout`
- Represents a Firestore workout.
- Fields include name, description, difficulty, total exercise count, duration, calories burned, cover image URL, and number of sets.

`Exercise`
- Represents a Firestore exercise.
- Fields include name, description, difficulty, duration, repetitions, image URL, workout ID, set number, and order within set.

`FitnessGoal`
- Represents a selectable fitness goal with name, description, image URL, and recommended workout type.

`AuthState`
- In-memory helper for logged-out, logged-in, profile-incomplete, and error states.

`Challenge` and `CompletedChallenge`
- Currently empty placeholder models.

### Repositories

`UserRepository`
- Wraps Firebase Auth and Firestore user operations.
- Handles:
    - register
    - login
    - password reset
    - sign out
    - get current Firebase user
    - create/update user documents
    - update last login time
    - save/get/update user preferences
    - add user stats
    - hold an in-memory `AuthState`

`GoalRepository`
- Provides basic Firestore operations for `goals`.
- Can fetch all goals, fetch by name, and create a goal.
- The current goal selection screen mostly uses direct Firestore calls instead of this repository.

`UserStatsRepository`
- Provides Firestore operations for historical stats.
- Uses collection name `userStats`.

`UserPreferencesRepository`
- Provides Firestore operations for preferences.
- Uses collection name `userPreferences`.

## Firebase Usage

### Authentication

Firebase Authentication is used for:

- Email/password signup in `SignupActivity`
- Email/password login in `LoginActivity`
- Password reset emails in `LoginActivity`
- Current user checks in protected screens
- Logout in `ProfileActivity`

Protected screens check `FirebaseAuth.getCurrentUser()` or `UserRepository.getCurrentUser()`. If there is no current user, the app redirects to `LoginActivity`.

### Firestore Collections

The code uses these collections:

`users`
- Main user profile collection.
- Document ID is the Firebase Auth UID.
- Written during signup and profile registration.
- Updated with profile details, goal, age, timestamps, and notification state.

Expected fields:

```text
name
email
phone
gender
dateOfBirth
weight
height
goal
age
profileImageUrl
notificationsEnabled
createdAt
lastLoginAt
updatedAt
```

`user_preferences`
- Used by `UserRepository`.
- Document ID is the Firebase Auth UID.
- Created during signup.

Expected fields:

```text
notificationsEnabled
workoutReminders
reminderTime
weightUnit
heightUnit
theme
```

`userPreferences`
- Used by `UserPreferencesRepository`.
- This is a second preferences collection name with different casing/underscore style.
- The current app primarily uses `UserRepository`, so `user_preferences` is the active path in signup/profile flows.

`user_stats`
- Used by `RegisterActivity` and `UserRepository`.
- Stores profile/body-stat snapshots after profile registration.

Expected fields:

```text
userId
weight
height
bmi
recordedAt
```

`userStats`
- Used by `UserStatsRepository`.
- This is a second stats collection name.
- The active registration flow writes to `user_stats`, not `userStats`.

`goals`
- Used by `GoalRepository`.
- Goal selection currently stores the selected goal on the user document instead of reading this collection.

`workouts`
- Used by `WorkoutDetails1Activity` and `WorkoutTimerActivity`.
- Documents represent workout plans.

Expected fields:

```text
name
description
difficulty
totalExercises
duration
caloriesBurn
coverImageUrl
numberOfSets
```

`exercises`
- Used by `WorkoutDetails1Activity` and `WorkoutTimerActivity`.
- Documents are linked to workouts by `workoutId`.

Expected fields:

```text
name
description
difficulty
duration
repetitions
imageUrl
workoutId
setNumber
orderInSet
```

`completed_workouts`
- Written by `WorkoutTimerActivity` after the timer finishes.
- Stores completed workout/exercise history.

Expected fields:

```text
userName
exerciseName
defaultDuration
userDuration
timestamp
workoutId
exerciseId
```

Note: `completed_workouts` currently stores `userName`, but does not store the Firebase UID. Adding `userId` would make per-user history queries safer and easier.

### Firebase Storage, Analytics, and Realtime Database

Dependencies are included for Firebase Storage, Analytics, and Realtime Database. In the current Java source, the active app logic uses Firebase Auth and Firestore. Storage, Analytics, and Realtime Database are not actively used by the inspected code.

## Notification System

Notifications use:

- `NotificationHelper`
- `WorkScheduler`
- `MotivationalWorker`
- AndroidX WorkManager
- Android notification channel `motivational_channel`

Flow:

1. `SplashActivity` creates the notification channel.
2. On Android 13+, it requests `POST_NOTIFICATIONS`.
3. If permission is granted, `WorkScheduler.scheduleDailyMotivation()` schedules three one-time notification jobs between 8 AM and 8 PM.
4. `SplashActivity` also enqueues six short-delay test notifications at 10, 40, 70, 100, 130, and 160 seconds.
5. `MotivationalWorker` chooses a random message and calls `NotificationHelper.showNotification()`.
6. `NotificationHelper` posts the system notification and saves the message/time into local `SharedPreferences`.
7. `NotificationActivity` reads that local history and displays it in the app.

Important behavior: scheduled daily notifications are `OneTimeWorkRequest`s. They do not automatically repeat every day unless the app schedules them again.

## User Workflow

### First-Time User

1. App launches into `SplashActivity`.
2. User grants or denies notification permission.
3. User taps Get Started.
4. User moves through `Onboarding1Activity` and `Onboarding2Activity`.
5. User opens `SignupActivity`.
6. User enters name, phone, email, and password.
7. App creates Firebase Auth account.
8. App creates Firestore user and preference documents.
9. User completes profile in `RegisterActivity`.
10. App saves gender, date of birth, weight, height, age, and stats.
11. User selects a goal in `GoalSelectionActivity`.
12. App saves goal to Firestore.
13. User sees `SuccessRegisterActivity`.
14. User enters `HomeActivity`.

### Returning User

1. User opens `LoginActivity`.
2. User logs in with email/password.
3. App checks Firestore profile completeness.
4. If profile is incomplete, app opens `RegisterActivity`.
5. If profile is complete, app opens `HomeActivity`.

### Workout Flow

1. User starts from `HomeActivity`.
2. User taps a workout/View More item.
3. `WorkoutDetails1Activity` loads workout and exercise data from Firestore.
4. User starts the workout or taps a specific exercise.
5. `WorkoutTimerActivity` loads workout/exercise details.
6. User sets or accepts timer values.
7. User starts timer.
8. Timer can be paused/resumed.
9. On finish, app plays sound, vibrates, saves completion to Firestore, and opens `WorkoutCompletionActivity`.
10. User returns to Home.

### Profile and Settings Flow

1. User opens `ProfileActivity` from bottom navigation.
2. App loads profile details from Firestore.
3. App calculates and displays BMI.
4. User can edit personal details through `RegisterActivity` edit mode.
5. User can toggle notification preference.
6. User can log out.

## Local Storage

`SharedPreferences` is used for:

- Legacy/fallback user profile values under `WoFitnessPrefs`
- Cached username
- Cached notification toggle state
- Local notification history under `notifications`

The main persisted source of truth for accounts and profiles is Firestore, but several screens fall back to local preferences when Firestore data is unavailable.

## External Links

The app opens these external URLs:

- WolFitness shop/frontend: `**https://wolfitness-frontend.vercel.app/**`
- Exercise tutorial videos on YouTube from `WorkoutTimerActivity`

## Permissions

Declared permissions:

```xml
INTERNET
VIBRATE
POST_NOTIFICATIONS
```

`POST_NOTIFICATIONS` is requested at runtime on Android 13 and above.

## Current Implementation Notes

- The architecture is mostly Activity-driven. Some Firebase operations are centralized in repositories, but several Activities still call Firebase directly.
- There are inconsistent Firestore collection names:
    - `user_preferences` and `userPreferences`
    - `user_stats` and `userStats`
- `completed_workouts` does not currently save the UID, which limits reliable per-user querying.
- `ProgressActivity`, `SettingsActivity`, `ChallengeDetailActivity`, `CompletionActivity`, `ChallengeCardAdapter`, `ApiService`, `RetrofitClient`, `Constants`, and `SharedPrefManager` are placeholders or mostly empty.
- `Challenge` and `CompletedChallenge` models are placeholders.
- Notification scheduling uses one-time work requests rather than periodic work.
- Some strings in source files show encoding artifacts, likely from character encoding mismatch.
- `Exercise` constructor accepts no `difficulty` parameter but assigns `this.difficulty = difficulty`, which refers to the field itself. Firestore deserialization still works through the empty constructor and setters.
- `UserStats.toMap()` does not include `recordedAt`; the comment expects `@ServerTimestamp`, but because `toMap()` is used, Firestore will not automatically add that field unless explicitly included elsewhere.

## How to Run

1. Open the project in Android Studio.
2. Ensure `app/google-services.json` exists.
3. Sync Gradle.
4. Confirm Firebase project settings match package `com.example.wolfitness`.
5. Run the `app` configuration on an emulator or Android device.

Command-line build:

```powershell
.\gradlew.bat assembleDebug
```

## Suggested Future Improvements

- Standardize Firestore collection names and migrate existing data if needed.
- Add `userId` to `completed_workouts`.
- Move direct Firebase calls from Activities into repositories.
- Add ViewModels to separate UI code from business/data logic.
- Replace one-time notification scheduling with periodic scheduling or rescheduling after completion.
- Implement the placeholder screens and classes or remove unused code.
- Add Firestore security rules matching the user-owned document structure.
- Add unit tests for BMI calculation, profile completeness, and timer duration parsing.
- Add instrumentation tests for signup/login/profile/workout workflows.
