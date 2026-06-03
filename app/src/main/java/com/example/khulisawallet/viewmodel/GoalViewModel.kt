package com.example.khulisawallet.viewmodel

import androidx.lifecycle.*
import com.example.khulisawallet.GoalWithSpent
import com.example.khulisawallet.data.*
import kotlinx.coroutines.launch

class GoalViewModel(
    private val repository: GoalRepository,
    private val expenseRepository: ExpenseRepository,
    private val categoryRepository: CategoryRepository
) : ViewModel() {

    private val _userId = MutableLiveData<Int>()
    private val _goalsWithSpending = MediatorLiveData<List<GoalWithSpent>>()
    val goalsWithSpending: LiveData<List<GoalWithSpent>> = _goalsWithSpending

    private var goalsSource: LiveData<List<Goal>>? = null
    private var expensesSource: LiveData<List<ExpenseWithCategory>>? = null
    private var categoriesSource: LiveData<List<Category>>? = null

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

    val totalMaxGoalAmount: LiveData<Double> = _userId.switchMap {
        repository.getTotalMaxGoalAmount(it)
    }

    val totalSavedAmount: LiveData<Double?> = _userId.switchMap {
        repository.getTotalSavedAmount(it)
    }

    val goalsBelowMinimum: LiveData<List<Goal>> = _userId.switchMap {
        repository.getGoalsBelowMinimum(it)
    }

    val goalsExceedingMaximum: LiveData<List<Goal>> = _userId.switchMap {
        repository.getGoalsExceedingMaximum(it)
    }

    private val _goalOpResult = MutableLiveData<Result<Any>?>()
    val goalOpResult: LiveData<Result<Any>?> = _goalOpResult

    fun setUser(userId: Int) {
        _userId.value = userId
        setupGoalsWithSpending(userId)
    }

    private fun setupGoalsWithSpending(userId: Int) {
        goalsSource?.let { _goalsWithSpending.removeSource(it) }
        expensesSource?.let { _goalsWithSpending.removeSource(it) }
        categoriesSource?.let { _goalsWithSpending.removeSource(it) }

        var goals: List<Goal> = emptyList()
        var expenses: List<ExpenseWithCategory> = emptyList()
        var categories: List<Category> = emptyList()

        fun recompute() {
            val now = System.currentTimeMillis()
            val result = goals.map { goal ->
                val endDate = goal.deadline ?: now
                val spent = expenses
                    .filter { item ->
                        item.expense.categoryId == goal.categoryId &&
                            item.expense.type == CategoryType.EXPENSE &&
                            item.expense.date >= goal.createdAt &&
                            item.expense.date <= endDate
                    }
                    .sumOf { it.expense.amount }
                val categoryName = categories.find { it.id == goal.categoryId }?.name ?: "Unknown"
                GoalWithSpent(goal, categoryName, spent)
            }
            _goalsWithSpending.value = result
        }

        goalsSource = repository.getAllGoalsByUser(userId).also { source ->
            _goalsWithSpending.addSource(source) {
                goals = it
                recompute()
            }
        }
        expensesSource = expenseRepository.getAllExpensesWithCategory(userId).also { source ->
            _goalsWithSpending.addSource(source) {
                expenses = it
                recompute()
            }
        }
        categoriesSource = categoryRepository.allActiveCategories.also { source ->
            _goalsWithSpending.addSource(source) {
                categories = it
                recompute()
            }
        }
    }

    fun getSpendingForCategory(categoryId: Int): LiveData<Double?> {
        val userId = _userId.value ?: return MutableLiveData()
        return repository.getSumForCategory(userId, categoryId)
    }

    fun addGoal(
        name: String,
        categoryId: Int,
        targetAmount: Double,
        description: String? = null,
        minGoal: Double? = null,
        maxGoal: Double? = null,
        deadline: Long? = null,
        colorHex: String = "#2ECC71",
        iconName: String = "ic_goal"
    ) {
        val userId = _userId.value ?: return
        viewModelScope.launch {
            val goal = Goal(
                userId = userId,
                categoryId = categoryId,
                name = name,
                description = description,
                targetAmount = targetAmount,
                minGoal = minGoal,
                maxGoal = maxGoal,
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

    fun updateGoalThresholds(goalId: Int, minGoal: Double?, maxGoal: Double?) {
        viewModelScope.launch {
            val result = repository.updateGoalThresholds(goalId, minGoal, maxGoal)
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

    fun getProgressPercent(goal: Goal): Int {
        if (goal.targetAmount == 0.0) return 0
        return ((goal.currentAmount / goal.targetAmount) * 100).toInt().coerceIn(0, 100)
    }

    fun isBelowMinGoal(goal: Goal): Boolean {
        return goal.minGoal != null && goal.currentAmount < goal.minGoal
    }

    fun isExceedingMaxGoal(goal: Goal): Boolean {
        return goal.maxGoal != null && goal.currentAmount > goal.maxGoal
    }

    fun clearResult() {
        _goalOpResult.value = null
    }
}
