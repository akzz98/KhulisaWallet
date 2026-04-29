package com.example.khulisawallet.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface ExpenseDao {

    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertExpense(expense: Expense): Long

    // --- READ (Basic) ---
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpensesByUser(userId: Int): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE id = :id")
    suspend fun getExpenseById(id: Int): Expense?

    @Query("SELECT * FROM expenses WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getExpensesByType(userId: Int, type: String): LiveData<List<Expense>>

    @Query("SELECT * FROM expenses WHERE userId = :userId AND categoryId = :categoryId ORDER BY date DESC")
    fun getExpensesByCategory(userId: Int, categoryId: Int): LiveData<List<Expense>>

    // --- READ (With Category) ---
    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId ORDER BY date DESC")
    fun getAllExpensesWithCategory(userId: Int): LiveData<List<ExpenseWithCategory>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId AND type = :type ORDER BY date DESC")
    fun getExpensesWithCategoryByType(userId: Int, type: String): LiveData<List<ExpenseWithCategory>>

    // --- READ (Date Range) ---
    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByDateRange(userId: Int, startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>>

    @Transaction
    @Query("SELECT * FROM expenses WHERE userId = :userId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate ORDER BY date DESC")
    fun getExpensesByCategoryAndDateRange(
        userId: Int,
        categoryId: Int,
        startDate: Long,
        endDate: Long
    ): LiveData<List<ExpenseWithCategory>>

    // --- READ (Summaries for Reporting) ---
    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND type = 'EXPENSE'")
    fun getTotalExpenses(userId: Int): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND type = 'INCOME'")
    fun getTotalIncome(userId: Int): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND type = 'EXPENSE' AND date BETWEEN :startDate AND :endDate")
    fun getTotalExpensesByDateRange(userId: Int, startDate: Long, endDate: Long): LiveData<Double?>

    @Query("SELECT SUM(amount) FROM expenses WHERE userId = :userId AND categoryId = :categoryId AND date BETWEEN :startDate AND :endDate")
    fun getTotalByCategoryAndDateRange(userId: Int, categoryId: Int, startDate: Long, endDate: Long): LiveData<Double?>

    @Query("SELECT * FROM expenses WHERE isRecurring = 1 AND userId = :userId")
    fun getRecurringExpenses(userId: Int): LiveData<List<Expense>>

    // --- UPDATE ---
    @Update
    suspend fun updateExpense(expense: Expense)

    @Query("UPDATE expenses SET imagePath = :path, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateReceiptImage(id: Int, path: String, timestamp: Long = System.currentTimeMillis())

    // --- DELETE ---
    @Delete
    suspend fun deleteExpense(expense: Expense)

    @Query("DELETE FROM expenses WHERE userId = :userId")
    suspend fun deleteAllUserExpenses(userId: Int)
}