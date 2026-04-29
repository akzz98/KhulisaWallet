package com.example.khulisawallet.viewmodel

import androidx.lifecycle.*
import com.example.khulisawallet.data.*
import kotlinx.coroutines.launch

class ExpenseViewModel(private val repository: ExpenseRepository) : ViewModel() {

    private val _userId = MutableLiveData<Int>()

    // Auto-updates when userId changes
    val allExpenses: LiveData<List<ExpenseWithCategory>> = _userId.switchMap {
        repository.getAllExpensesWithCategory(it)
    }

    val totalExpenses: LiveData<Double?> = _userId.switchMap {
        repository.getTotalExpenses(it)
    }

    val totalIncome: LiveData<Double?> = _userId.switchMap {
        repository.getTotalIncome(it)
    }

    val recurringExpenses: LiveData<List<Expense>> = _userId.switchMap {
        repository.getRecurringExpenses(it)
    }

    private val _expenseOpResult = MutableLiveData<Result<Any>?>()
    val expenseOpResult: LiveData<Result<Any>?> = _expenseOpResult

    fun setUser(userId: Int) {
        _userId.value = userId
    }

    fun getExpensesByType(type: CategoryType): LiveData<List<ExpenseWithCategory>> {
        return repository.getExpensesWithCategoryByType(_userId.value ?: return MutableLiveData(), type)
    }

    fun getExpensesByDateRange(startDate: Long, endDate: Long): LiveData<List<ExpenseWithCategory>> {
        return repository.getExpensesByDateRange(_userId.value ?: return MutableLiveData(), startDate, endDate)
    }

    fun getTotalExpensesByDateRange(startDate: Long, endDate: Long): LiveData<Double?> {
        return repository.getTotalExpensesByDateRange(_userId.value ?: return MutableLiveData(), startDate, endDate)
    }

    fun addExpense(
        categoryId: Int,
        title: String,
        amount: Double,
        type: CategoryType,
        date: Long = System.currentTimeMillis(),
        note: String? = null,
        imagePath: String? = null,
        isRecurring: Boolean = false,
        recurringIntervalDays: Int? = null
    ) {
        val userId = _userId.value ?: return
        viewModelScope.launch {
            val expense = Expense(
                userId = userId,
                categoryId = categoryId,
                title = title,
                amount = amount,
                type = type,
                date = date,
                note = note,
                imagePath = imagePath,
                isRecurring = isRecurring,
                recurringIntervalDays = recurringIntervalDays
            )
            val result = repository.insertExpense(expense)
            _expenseOpResult.postValue(result as Result<Any>)
        }
    }

    fun updateExpense(expense: Expense) {
        viewModelScope.launch {
            val result = repository.updateExpense(expense)
            _expenseOpResult.postValue(result as Result<Any>)
        }
    }

    fun updateReceiptImage(id: Int, path: String) {
        viewModelScope.launch {
            val result = repository.updateReceiptImage(id, path)
            _expenseOpResult.postValue(result as Result<Any>)
        }
    }

    fun deleteExpense(expense: Expense) {
        viewModelScope.launch {
            val result = repository.deleteExpense(expense)
            _expenseOpResult.postValue(result as Result<Any>)
        }
    }

    fun clearResult() {
        _expenseOpResult.value = null
    }
}