package com.example.khulisawallet.data

import androidx.lifecycle.LiveData
import com.example.khulisawallet.utils.StreakManager

class ExpenseRepository(
    private val expenseDao: ExpenseDao,
    private val goalDao: GoalDao,
    private val userDao: UserDao,
    private val firebaseRepository: FirebaseRepository = FirebaseRepository()
) {

    fun getAllExpensesByUser(userId: Int): LiveData<List<Expense>> =
        expenseDao.getAllExpensesByUser(userId)

    fun getAllExpensesWithCategory(userId: Int): LiveData<List<ExpenseWithCategory>> =
        expenseDao.getAllExpensesWithCategory(userId)

    fun getExpensesByType(userId: Int, type: CategoryType): LiveData<List<Expense>> =
        expenseDao.getExpensesByType(userId, type.name)

    fun getExpensesWithCategoryByType(userId: Int, type: CategoryType): LiveData<List<ExpenseWithCategory>> =
        expenseDao.getExpensesWithCategoryByType(userId, type.name)

    fun getExpensesByDateRange(userId: Int, startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>> =
        expenseDao.getExpensesByDateRange(userId, startDate, endDate)

    fun getExpensesByCategoryAndDateRange(
        userId: Int,
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): LiveData<List<ExpenseWithCategory>> =
        expenseDao.getExpensesByCategoryAndDateRange(userId, categoryId, startDate, endDate)

    fun getTotalExpenses(userId: Int): LiveData<Double?> =
        expenseDao.getTotalExpenses(userId)

    fun getTotalIncome(userId: Int): LiveData<Double?> =
        expenseDao.getTotalIncome(userId)

    fun getTotalExpensesByDateRange(userId: Int, startDate: Long, endDate: Long): LiveData<Double?> =
        expenseDao.getTotalExpensesByDateRange(userId, startDate, endDate)

    fun getIncomeByDateRange(userId: Int, startDate: Long, endDate: Long): LiveData<Double> =
        expenseDao.getIncomeByDateRange(userId, startDate, endDate)

    fun getTotalByCategoryAndDateRange(
        userId: Int,
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): LiveData<Double?> =
        expenseDao.getTotalByCategoryAndDateRange(userId, categoryId, startDate, endDate)

    fun getSumForCategory(userId: Int, categoryId: Int): LiveData<Double?> =
        expenseDao.getSumForCategory(userId, categoryId)

    fun getSumForCategorySince(
        userId: Int,
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): LiveData<Double?> =
        expenseDao.getSumForCategorySince(userId, categoryId, startDate, endDate)

    fun getRecurringExpenses(userId: Int): LiveData<List<Expense>> =
        expenseDao.getRecurringExpenses(userId)

    suspend fun insertExpense(expense: Expense): Result<Long> {
        return try {
            val id = expenseDao.insertExpense(expense)
            val savedExpense = expense.copy(id = id.toInt())

            if (savedExpense.type == CategoryType.EXPENSE) {
                syncExpenseToGoals(savedExpense)
            }

            firebaseRepository.saveExpense(savedExpense)

            val user = userDao.getUserById(savedExpense.userId)
            if (user != null) {
                val newStreak = StreakManager.calculateNewStreak(
                    user.lastActivityTimestamp,
                    user.currentStreak
                )
                userDao.updateStreak(savedExpense.userId, newStreak, System.currentTimeMillis())

                val updatedUser = userDao.getUserById(savedExpense.userId)
                if (updatedUser != null) {
                    firebaseRepository.saveUser(updatedUser)
                }
            }

            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private suspend fun syncExpenseToGoals(expense: Expense) {
        val goals = goalDao.getActiveGoalsByCategory(expense.userId, expense.categoryId)
        for (goal in goals) {
            if (expense.date >= goal.createdAt &&
                (goal.deadline == null || expense.date <= goal.deadline)
            ) {
                goalDao.addSpendingToGoal(goal.id, expense.amount)
                goalDao.getGoalById(goal.id)?.let { updatedGoal ->
                    firebaseRepository.saveGoal(updatedGoal)
                }
            }
        }
    }

    suspend fun updateExpense(expense: Expense): Result<Unit> {
        return try {
            expenseDao.updateExpense(expense)
            firebaseRepository.saveExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateReceiptImage(id: Int, path: String): Result<Unit> {
        return try {
            expenseDao.updateReceiptImage(id, path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteExpense(expense: Expense): Result<Unit> {
        return try {
            expenseDao.deleteExpense(expense)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun getExpenseById(id: Int): Expense? =
        expenseDao.getExpenseById(id)
}