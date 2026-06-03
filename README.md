# Khulisa Wallet 🌿
> **ST10263456 - PROG7313 Final POE Submission**

## 📌 Project Overview
Khulisa Wallet (*IsiZulu for "to grow"*) is a data-driven personal finance application built for South African users. It goes beyond simple expense tracking by introducing **proactive daily budgeting** through Safe-to-Spend logic and **habit-forming gamification** via the Khulisa Growth Streak system.

The app is **offline-first**: all data is stored locally in Room for fast performance, then synced to **Firebase Firestore** when the user logs in or opens the app.

---

## ✨ Part 3: Innovative Features (My Own Features)

### 1. 💰 Safe-to-Spend Dashboard
Rather than only showing a total balance, Khulisa Wallet calculates how much the user can realistically spend **today**, based on what is left this month after commitments and spending.

**Formula:**
```
(Income This Month − Budget Caps − Spent This Month) ÷ Days Left in Month
```

| Input | Source |
|-------|--------|
| **Income this month** | Sum of `INCOME` transactions dated in the current calendar month |
| **Budget caps** | Sum of `maxGoal` on all active goals (goals without a max are excluded) |
| **Spent this month** | Sum of `EXPENSE` transactions dated in the current calendar month |
| **Days left** | Days from today through end of month (today included) |

**Status indicators:**
- 🟢 **Green (Healthy):** Money remains after income, caps, and spending — shows daily safe amount.
- 🔴 **Red (Tight):** Nothing meaningful left this month (caps + spending ≥ income).
- ⚫ **Grey (No income):** No income logged for the current month.

The home screen also separates **Total Balance** (green/red by sign), **Income** (green card), and **Expenses** (red card) for quick scanning.

---

### 2. 🌱 Khulisa Growth Streak & Ranks
To encourage financial discipline, the app tracks consecutive **calendar days** of activity. Logging an expense updates the streak and syncs it to Firebase.

**How it works:**
- First activity → streak starts at **1**
- Activity on the **next calendar day** → streak **+1**
- Activity on the **same day** → streak **unchanged**
- **Gap of 2+ days** → streak **resets to 1**

**Ranks (badges):**

| Streak | Rank |
|--------|------|
| 0–2 days | Getting Started |
| 3–6 days | Active Seed |
| 7–13 days | Fresh Sprout |
| 14–29 days | Strong Sapling |
| 30+ days | Financial Forest (Tree) |

**UX:** A welcome dialog shows streak status when the app opens; full details (including longest streak) live on the **Profile** screen.

---

## 🛠 Technical Implementation

### Architecture
- **Pattern:** MVVM — `Dao` → `Repository` → `ViewModel` → UI (Fragments / Activities)
- **Local DB:** Room (SQLite), offline-first
- **Cloud sync:** Firebase **Firestore** — push on save; pull from cloud on **login** and **splash** (`FirebaseSyncRepository`)
- **Charts:** MPAndroidChart pie chart on Home (hidden until the user has expenses)
- **Navigation:** Bottom navigation (Home, History, Goals, Profile) + contextual FAB

### Data flow
```
User action → Room (source of truth for UI) → Firestore (backup / restore)
App open / login → Firestore → Room (categories, expenses, goals, streak profile)
```

### Professional Quality Assurance
- **CI/CD:** GitHub Actions (`.github/workflows/android_ci.yml`) runs on every push/PR to `main`:
  - `./gradlew build`
  - `./gradlew test`
  - Uploads debug APK artifact
- **Unit testing:** **33 unit tests** across:
  - `StreakManagerTest` — streak calculation & badge logic (13 tests)
  - `SafeToSpendCalculatorTest` — Safe-to-Spend formula & status (11 tests)
  - `BudgetLogicTest` — balance, goal thresholds, date helpers (8 tests)
  - `ExampleUnitTest` — sanity check (1 test)

---

## 🚀 Key Features Recap

### Authentication & session
- Splash → Login / Sign Up
- Email + password (SHA-256 hash), session in `SharedPreferences`
- Splash validates that stored `user_id` still exists in Room (handles DB resets)

### Home dashboard
- Greeting, date, balance / income / expense summary cards
- Safe-to-Spend card with monthly breakdown
- Spending pie chart (shown only when expenses exist)
- Goal alert banner (below min / above max)
- Recent transactions + “See all” → History tab

### Income & expense tracking
- Add income or expense with category, date, notes
- Optional **receipt photo** (camera + storage)
- Categorised history with date-range filtering
- Expenses linked to goals by **category**

### Goal management (category budgets)
- Goals tied to a category; progress from **real expenses** in that category
- **Min**, **Max**, and **Target** thresholds
- Status: *On Track*, *Below Min*, *Exceeded*
- Summary: Total goals, On Track, Needs Attention
- FAB opens **Add Goal** on Goals tab; hidden on Profile

### Categories
- Default categories preloaded (Food, Transport, Salary, etc.)
- **Manage Categories** screen — add, edit, soft-delete custom categories

### Profile
- User info, stats (transactions, goals, categories)
- Growth streak card (current rank + longest streak)
- Change password, manage categories, logout

---

## 📱 Installation & Setup

1. Clone the repository:
   ```bash
   git clone https://github.com/akzz98/KhulisaWallet.git
   ```
2. Open in **Android Studio** (Ladybug or newer recommended).
3. Add your Firebase **`google-services.json`** to the `/app` directory  
   (package name: `com.example.khulisawallet`).
4. Sync Gradle and run on a **physical device or emulator (API 34+)**.

### Firebase setup
- Enable **Firestore** in the Firebase console.
- Data is stored under: `users/{userId}/categories|expenses|goals`
- **Note:** Login is local (Room). Cloud restore works for the same device/session; full cross-device account login would require Firebase Authentication (future enhancement).

### Run tests locally
```bash
./gradlew test
```

---

## 📂 Project Structure (high level)
```
app/src/main/java/com/example/khulisawallet/
├── SplashActivity, LoginActivity, SignUpActivity, MainActivity
├── HomeFragment, HistoryFragment, GoalsFragment, ProfileFragment
├── AddExpenseActivity, ManageCategoriesActivity
├── data/          # Room entities, DAOs, repositories
├── viewmodel/     # MVVM ViewModels + factories
└── utils/         # SafeToSpendCalculator, StreakManager, SystemBarUtils, …

app/src/test/java/com/example/khulisawallet/
├── StreakManagerTest.kt
├── SafeToSpendCalculatorTest.kt
└── BudgetLogicTest.kt
```

---

## 🧑‍💻 Author
**Student Name:** Alton Muganda 
**Student Number:** ST10263456  
**Module:** PROG7313
**Institution:** Emeris Sandton

