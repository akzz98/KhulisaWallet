package com.example.khulisawallet.viewmodel

import androidx.lifecycle.*
import com.example.khulisawallet.data.Goal
import com.example.khulisawallet.data.GoalRepository
import com.example.khulisawallet.data.GoalStatus
import kotlinx.coroutines.launch

class GoalViewModel(private val repository: GoalRepository) : ViewModel() {

    private val _userId = MutableLiveData<Int>()

    val allGoals: LiveData<List<Goal>> = _userId.switchMap {
        repository.getAllGoalsByUser(it)
    }

    val activeGoals: LiveData<List<Goal>> = _userId.switchMap {
        repository.getActiveGoals(it)
    }

    val completedGoals: LiveData<List<Goal>> = _userId.switchMap {
        repository.getCompletedGoals(it)
    }

    val upcomingDeadlineGoals: LiveData<List<Goal>> = _userId.switchMap {
        repository.getUpcomingDeadlineGoals(it)
    }

    val nearlyCompleteGoals: LiveData<List<Goal>> = _userId.switchMap {
        repository.getNearlyCompleteGoals(it)
    }

    val totalTargetAmount: LiveData<Double?> = _userId.switchMap {
        repository.getTotalTargetAmount(it)
    }

    val totalSavedAmount: LiveData<List<Goal>> = _userId.switchMap {
        repository.getAllGoalsByUser(it)
    }

    private val _goalOpResult = MutableLiveData<Result<Any>?>()
    val goalOpResult: LiveData<Result<Any>?> = _goalOpResult

    fun setUser(userId: Int) {
        _userId.value = userId
    }

    fun addGoal(
        name: String,
        targetAmount: Double,
        description: String? = null,
        deadline: Long? = null,
        colorHex: String = "#2ECC71",
        iconName: String = "ic_goal"
    ) {
        val userId = _userId.value ?: return
        viewModelScope.launch {
            val goal = Goal(
                userId = userId,
                name = name,
                description = description,
                targetAmount = targetAmount,
                deadline = deadline,
                colorHex = colorHex,
                iconName = iconName
            )
            val result = repository.insertGoal(goal)
            _goalOpResult.postValue(result as Result<Any>)
        }
    }

    fun contributeToGoal(goalId: Int, amount: Double) {
        viewModelScope.launch {
            val result = repository.contributeToGoal(goalId, amount)
            _goalOpResult.postValue(result as Result<Any>)
        }
    }

    fun updateGoal(goal: Goal) {
        viewModelScope.launch {
            val result = repository.updateGoal(goal)
            _goalOpResult.postValue(result as Result<Any>)
        }
    }

    fun cancelGoal(goalId: Int) {
        viewModelScope.launch {
            val result = repository.updateGoalStatus(goalId, GoalStatus.CANCELLED)
            _goalOpResult.postValue(result as Result<Any>)
        }
    }

    fun deleteGoal(goal: Goal) {
        viewModelScope.launch {
            val result = repository.deleteGoal(goal)
            _goalOpResult.postValue(result as Result<Any>)
        }
    }

    // Helper: calculate progress percentage
    fun getProgressPercent(goal: Goal): Int {
        if (goal.targetAmount == 0.0) return 0
        return ((goal.currentAmount / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
    }

    fun clearResult() {
        _goalOpResult.value = null
    }
}