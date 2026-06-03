package com.example.khulisawallet.data

/**
 * Pulls cloud data from Firestore into the local Room database.
 * Write path remains unchanged: local save first, then push to Firebase.
 */
class FirebaseSyncRepository(
    private val db: AppDatabase,
    private val firebaseRepository: FirebaseRepository = FirebaseRepository(),
    private val categoryRepository: CategoryRepository = CategoryRepository(db.categoryDao())
) {

    data class SyncResult(
        val categories: Int,
        val expenses: Int,
        val goals: Int,
        val profileUpdated: Boolean
    )

    suspend fun syncUserData(userId: Int): Result<SyncResult> {
        return try {
            val categoryDao = db.categoryDao()
            val expenseDao = db.expenseDao()
            val goalDao = db.goalDao()
            val userDao = db.userDao()

            // 1. Categories — cloud first; fall back to local defaults if empty
            val remoteCategories = firebaseRepository.fetchCategories(userId).getOrThrow()
            if (remoteCategories.isNotEmpty()) {
                categoryDao.upsertAll(remoteCategories)
            } else {
                categoryRepository.preloadDefaults(userId)
            }

            // 2. Expenses — only rows whose category exists locally (FK safety)
            val remoteExpenses = firebaseRepository.fetchExpenses(userId).getOrThrow()
            val validExpenses = remoteExpenses.filter { expense ->
                categoryDao.getCategoryById(expense.categoryId) != null
            }
            if (validExpenses.isNotEmpty()) {
                expenseDao.upsertAllExpenses(validExpenses)
            }

            // 3. Goals
            val remoteGoals = firebaseRepository.fetchGoals(userId).getOrThrow()
            val validGoals = remoteGoals.filter { goal ->
                categoryDao.getCategoryById(goal.categoryId) != null
            }
            if (validGoals.isNotEmpty()) {
                goalDao.upsertAllGoals(validGoals)
            }

            // 4. User profile fields (streak + names from cloud)
            var profileUpdated = false
            val profile = firebaseRepository.fetchUserProfile(userId).getOrThrow()
            if (profile != null) {
                val localUser = userDao.getUserById(userId)
                if (localUser != null) {
                    userDao.updateUser(
                        localUser.copy(
                            firstName = profile.firstName ?: localUser.firstName,
                            lastName = profile.lastName ?: localUser.lastName,
                            currentStreak = profile.currentStreak,
                            lastActivityTimestamp = profile.lastActivityTimestamp,
                            longestStreak = profile.longestStreak
                        )
                    )
                    profileUpdated = true
                }
            }

            Result.success(
                SyncResult(
                    categories = if (remoteCategories.isNotEmpty()) remoteCategories.size else categoryDao.getCategoryCount(),
                    expenses = validExpenses.size,
                    goals = validGoals.size,
                    profileUpdated = profileUpdated
                )
            )
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
