# Khulisa Wallet 🌿
> **ST10263456 - PROG7313 Final POE Submission**

## 📌 Project Overview
Khulisa Wallet (*IsiZulu for "to grow"*) is a data-driven personal finance application built for South African users. It goes beyond simple expense tracking by introducing **proactive daily budgeting** through Safe-to-Spend logic and **habit-forming gamification** via the Khulisa Growth Streak system.

<img width="374" height="486" alt="image" src="https://github.com/user-attachments/assets/e742e816-d700-4773-a56d-eb69fb77e43c" />

The app is **offline-first**: all data is stored locally in Room for fast performance, then synced to **Firebase Firestore** when the user logs in or opens the app.

---

## ✨ Part 3: Innovative Features (My Own Features)

### 1. 💰 Safe-to-Spend Dashboard
Rather than only showing a total balance, Khulisa Wallet calculates how much the user can realistically spend **today**, based on what is left this month after commitments and spending.
<img width="398" height="206" alt="Screenshot 2026-06-03 220529" src="https://github.com/user-attachments/assets/5f22f030-5c4b-47ac-8050-380aa5267a19" />

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

<img width="411" height="290" alt="Screenshot 2026-06-03 221559" src="https://github.com/user-attachments/assets/fdbf7536-e72c-4dfe-9ef2-b7aec5f78050" />

---

### 2. 🌱 Khulisa Growth Streak & Ranks
To encourage financial discipline, the app tracks consecutive **calendar days** of activity. Logging an expense updates the streak and syncs it to Firebase.

<img width="437" height="387" alt="Screenshot 2026-06-03 221648" src="https://github.com/user-attachments/assets/b5ffd634-778f-4c3b-9bb6-03b0aa24d52c" />


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

<img width="436" height="200" alt="Screenshot 2026-06-03 221721" src="https://github.com/user-attachments/assets/08368109-22ba-430c-a84d-b0410eb16bd4" />

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
 
  <img width="392" height="866" alt="Screenshot 2026-06-03 221910" src="https://github.com/user-attachments/assets/0c003343-5adc-489f-9e3f-f113a2ee0aa2" /> <img    width="399" height="884" alt="Screenshot 2026-06-03 222038" src="https://github.com/user-attachments/assets/bf7fd886-8ecd-4e2c-9d44-1b2fee50afb7" />


### Income & expense tracking
- Add income or expense with category, date, notes
- Optional **receipt photo** (camera + storage)
- Categorised history with date-range filtering
- Expenses linked to goals by **category**
  
  <img width="412" height="809" alt="Screenshot 2026-06-03 222215" src="https://github.com/user-attachments/assets/e59999af-cac9-4f1b-850a-6e10e9ff72ec" /> <img    width="399" height="884" alt="Screenshot 2026-06-03 222134" src="https://github.com/user-attachments/assets/c68bf2d9-3710-4efe-bc1e-0d0e2913bbd6" />

### Goal management (category budgets)
- Goals tied to a category; progress from **real expenses** in that category
- **Min**, **Max**, and **Target** thresholds
- Status: *On Track*, *Below Min*, *Exceeded*
- Summary: Total goals, On Track, Needs Attention
- FAB opens **Add Goal** on Goals tab; hidden on Profile
  
  <img width="409" height="652" alt="image" src="https://github.com/user-attachments/assets/bd58e258-ab43-4d8b-a30b-cd476422f28f" /> <img width="421" height="164" alt="Screenshot 2026-06-03 222155" src="https://github.com/user-attachments/assets/9278afd5-5c2a-4a63-9ae9-d5596686c8c8" />


### Categories
- Default categories preloaded (Food, Transport, Salary, etc.)
- **Manage Categories** screen — add, edit, soft-delete custom categories
  
  <img width="399" height="878" alt="image" src="https://github.com/user-attachments/assets/e3f28243-a9d1-4c71-90be-4d502f92538c" />


### Profile
- User info, stats (transactions, goals, categories)
- Growth streak card (current rank + longest streak)
- Change password, manage categories, logout
  
  <img width="399" height="881" alt="Screenshot 2026-06-03 222441" src="https://github.com/user-attachments/assets/08e22b2c-ed95-4525-9f27-dd77ffbb194e" />


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

