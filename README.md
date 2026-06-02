# Khulisa Wallet (PROG7313)

Khulisa Wallet is an Android personal finance app built for **PROG7313**. It helps users track spending by category, set **spending goals** (min/max/target), and view progress with alerts when spending is **below minimum** or **exceeds maximum**.

## Key Features

### Authentication & Session
- Splash screen → Login / Sign Up
- User session stored with `SharedPreferences` (e.g., `user_id`)

### Categories
- Create and manage categories (per user)
- Categories are used to classify expenses and goals

### Expenses
- Log expenses linked to a category and date
- (Optional/extendable) receipt photo support if implemented in your Expense flow

### Goals (Budget Targets)
- Add goals with:
  - `minAmount` (minimum target)
  - `maxAmount` (maximum limit)
  - `targetAmount` (used to calculate progress %)
- Goal list shows:
  - Progress bar (spent vs target)
  - Status badge (“On Track”, “Below Min”, “Exceeded”)
  - Alert messages for min/max thresholds
- Summary counts: Total Goals, On Track, Needs Attention

### Profile
- Displays logged-in user info (name, email, member since)
- Simple stats (transactions, goals, categories)
- Logout (clears preferences and returns to Login)

## Tech Stack / Architecture
- **Language:** Kotlin
- **UI:** Activities + Fragments, Material Components, RecyclerView
- **Architecture:** MVVM
  - `Dao` → `Repository` → `ViewModel` → UI
- **Database:** Room
- **Annotation processing:** KSP (Room compiler via `ksp(...)`)

## Project Structure (Typical)
app/src/main/java/com/example/khulisawallet/
├── SplashActivity.kt
├── LoginActivity.kt
├── SignUpActivity.kt
├── MainActivity.kt (hosts fragments / navigation)
├── GoalsFragment.kt
├── ProfileFragment.kt
├── adapters/
│ └── GoalAdapter.kt
├── data/
│ ├── AppDatabase.kt
│ ├── User.kt / UserDao.kt / UserRepository.kt
│ ├── Category.kt / CategoryDao.kt / CategoryRepository.kt
│ ├── Expense.kt / ExpenseDao.kt / ExpenseRepository.kt
│ └── Goal.kt / GoalDao.kt / GoalRepository.kt
└── viewmodel/
├── UserViewModel.kt (+ Factory)
├── CategoryViewModel.kt (+ Factory)
├── ExpenseViewModel.kt (+ Factory)
└── GoalViewModel.kt (+ Factory)

app/src/main/res/layout/
├── activity_splash.xml
├── activity_login.xml
├── activity_signup.xml
├── fragment_goals.xml
├── fragment_profile.xml
├── item_goal.xml
├── dialog_add_goal.xml
└── dialog_change_password.xml

app/src/main/res/drawable/
├── badge_background.xml
└── circle_avatar_background.xml


## Setup & Run (Android Studio)
1. Open the project in Android Studio.
2. Let Gradle sync finish.
3. Ensure Room + KSP dependencies are correctly set in Gradle.
4. Run the app on an emulator or physical device.

### Notes for running
- The launcher activity should be set to `SplashActivity` in `AndroidManifest.xml`.
- The app expects a valid logged-in session (`user_id` stored in `SharedPreferences`) to access fragments like Goals/Profile.

## Database Notes
- Room database version must be incremented when entities change.
- During development you may use destructive migration, but for final submission it’s better to add proper migrations if required.

## How Goals Progress Works (Logic Summary)
For each goal:
- `spent` is computed from expenses in the same category (expense-only entries).
- Progress % is based on `spent / targetAmount` (clamped 0–100).
- Alerts:
  - `spent < minAmount` → “Below Min”
  - `spent > maxAmount` → “Exceeded”
  - otherwise → “On Track”

## Author
- Student Number: **ST10263456**
- Module: **PROG7313**
- Project: **Khulisa Wallet**


Youtube video link:
https://youtu.be/FNxKHVaSb1c
