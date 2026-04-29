package com.example.khulisawallet.data

import androidx.lifecycle.LiveData

class GoalRepository(private val goalDao: GoalDao) {

    fun getAllGoalsByUser(userId: Int): LiveData<List<Goal>> =
        goalDao.getAllGoalsByUser(userId)

    fun getActiveGoals(userId: Int): LiveData<List<Goal>> =
        goalDao.getGoalsByStatus(userId, GoalStatus.ACTIVE.name)

    fun getCompletedGoals(userId: Int): LiveData<List<Goal>> =
        goalDao.getGoalsByStatus(userId, GoalStatus.COMPLETED.name)

    fun getGoalsBelowMinimum(userId: Int): LiveData<List<Goal>> =
        goalDao.getGoalsBelowMinimum(userId)

    fun getGoalsExceedingMaximum(userId: Int): LiveData<List<Goal>> =
        goalDao.getGoalsExceedingMaximum(userId)

    fun getUpcomingDeadlineGoals(userId: Int): LiveData<List<Goal>> {
        val now = System.currentTimeMillis()
        val sevenDaysFromNow = now + (7 * 24 * 60 * 60 * 1000L)
        return goalDao.getUpcomingDeadlineGoals(userId, now, sevenDaysFromNow)
    }

    fun getNearlyCompleteGoals(userId: Int): LiveData<List<Goal>> =
        goalDao.getNearlyCompleteGoals(userId)

    fun getTotalTargetAmount(userId: Int): LiveData<Double?> =
        goalDao.getTotalTargetAmount(userId)

    fun getTotalSavedAmount(userId: Int): LiveData<Double?> =
        goalDao.getTotalSavedAmount(userId)

    suspend fun getGoalById(id: Int): Goal? =
        goalDao.getGoalById(id)

    suspend fun insertGoal(goal: Goal): Result<Long> {
        return try {
            val id = goalDao.insertGoal(goal)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGoal(goal: Goal): Result<Unit> {
        return try {
            goalDao.updateGoal(goal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun contributeToGoal(goalId: Int, amount: Double): Result<Unit> {
        return try {
            goalDao.contributeToGoal(goalId, amount)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGoalStatus(goalId: Int, status: GoalStatus): Result<Unit> {
        return try {
            goalDao.updateGoalStatus(goalId, status.name)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateGoalThresholds(goalId: Int, minGoal: Double?, maxGoal: Double?): Result<Unit> {
        return try {
            goalDao.updateGoalThresholds(goalId, minGoal, maxGoal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteGoal(goal: Goal): Result<Unit> {
        return try {
            goalDao.deleteGoal(goal)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}