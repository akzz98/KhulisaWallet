package com.example.khulisawallet.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface GoalDao {

    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insertGoal(goal: Goal): Long

    // --- READ ---
    @Query("SELECT * FROM goals WHERE userId = :userId ORDER BY createdAt DESC")
    fun getAllGoalsByUser(userId: Int): LiveData<List<Goal>>

    @Query("SELECT * FROM goals WHERE userId = :userId AND status = :status ORDER BY deadline ASC")
    fun getGoalsByStatus(userId: Int, status: String): LiveData<List<Goal>>

    @Query("SELECT * FROM goals WHERE id = :id")
    suspend fun getGoalById(id: Int): Goal?

    // Goals where currentAmount is below minGoal (under-saving alert)
    @Query("""
        SELECT * FROM goals 
        WHERE userId = :userId 
        AND status = 'ACTIVE' 
        AND minGoal IS NOT NULL 
        AND currentAmount < minGoal
        ORDER BY createdAt DESC
    """)
    fun getGoalsBelowMinimum(userId: Int): LiveData<List<Goal>>

    // Goals where currentAmount has exceeded maxGoal (overspending alert)
    @Query("""
        SELECT * FROM goals 
        WHERE userId = :userId 
        AND status = 'ACTIVE' 
        AND maxGoal IS NOT NULL 
        AND currentAmount > maxGoal
        ORDER BY createdAt DESC
    """)
    fun getGoalsExceedingMaximum(userId: Int): LiveData<List<Goal>>

    // Goals approaching deadline (within next 7 days)
    @Query("""
        SELECT * FROM goals 
        WHERE userId = :userId 
        AND status = 'ACTIVE' 
        AND deadline IS NOT NULL 
        AND deadline BETWEEN :now AND :sevenDaysFromNow
        ORDER BY deadline ASC
    """)
    fun getUpcomingDeadlineGoals(userId: Int, now: Long, sevenDaysFromNow: Long): LiveData<List<Goal>>

    // Goals nearly complete (>= 80% funded)
    @Query("""
        SELECT * FROM goals 
        WHERE userId = :userId 
        AND status = 'ACTIVE' 
        AND (currentAmount / targetAmount) >= 0.8
        ORDER BY (currentAmount / targetAmount) DESC
    """)
    fun getNearlyCompleteGoals(userId: Int): LiveData<List<Goal>>

    @Query("SELECT SUM(targetAmount) FROM goals WHERE userId = :userId AND status = 'ACTIVE'")
    fun getTotalTargetAmount(userId: Int): LiveData<Double?>

    @Query("SELECT SUM(currentAmount) FROM goals WHERE userId = :userId AND status = 'ACTIVE'")
    fun getTotalSavedAmount(userId: Int): LiveData<Double?>

    // --- UPDATE ---
    @Update
    suspend fun updateGoal(goal: Goal)

    @Query("""
        UPDATE goals 
        SET currentAmount = currentAmount + :amount, 
            updatedAt = :timestamp,
            status = CASE 
                WHEN (currentAmount + :amount) >= targetAmount THEN 'COMPLETED' 
                ELSE status 
            END
        WHERE id = :goalId
    """)
    suspend fun contributeToGoal(
        goalId: Int,
        amount: Double,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("UPDATE goals SET status = :status, updatedAt = :timestamp WHERE id = :goalId")
    suspend fun updateGoalStatus(
        goalId: Int,
        status: String,
        timestamp: Long = System.currentTimeMillis()
    )

    @Query("""
        UPDATE goals 
        SET minGoal = :minGoal, maxGoal = :maxGoal, updatedAt = :timestamp 
        WHERE id = :goalId
    """)
    suspend fun updateGoalThresholds(
        goalId: Int,
        minGoal: Double?,
        maxGoal: Double?,
        timestamp: Long = System.currentTimeMillis()
    )

    // --- DELETE ---
    @Delete
    suspend fun deleteGoal(goal: Goal)

    @Query("DELETE FROM goals WHERE userId = :userId")
    suspend fun deleteAllUserGoals(userId: Int)
}