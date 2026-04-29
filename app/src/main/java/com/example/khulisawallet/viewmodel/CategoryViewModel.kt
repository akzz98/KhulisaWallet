package com.example.khulisawallet.viewmodel

import androidx.lifecycle.*
import com.example.khulisawallet.data.Category
import com.example.khulisawallet.data.CategoryRepository
import com.example.khulisawallet.data.CategoryType
import kotlinx.coroutines.launch

class CategoryViewModel(private val repository: CategoryRepository) : ViewModel() {

    private val _userId = MutableLiveData<Int>()

    fun setUser(userId: Int) {
        _userId.value = userId
    }

    val allCategories: LiveData<List<Category>> = repository.allActiveCategories
    val allActiveCategories: LiveData<List<Category>> = repository.allActiveCategories
    val defaultCategories: LiveData<List<Category>> = repository.defaultCategories
    val userCategories: LiveData<List<Category>> = repository.userCategories
    val parentCategories: LiveData<List<Category>> = repository.parentCategories

    private val _categoryOpResult = MutableLiveData<Result<Any>?>()
    val categoryOpResult: LiveData<Result<Any>?> = _categoryOpResult

    fun getExpenseCategories(): LiveData<List<Category>> =
        repository.getCategoriesByType(CategoryType.EXPENSE)

    fun getIncomeCategories(): LiveData<List<Category>> =
        repository.getCategoriesByType(CategoryType.INCOME)

    fun getSubCategories(parentId: Int): LiveData<List<Category>> =
        repository.getSubCategories(parentId)

    fun addCategory(
        name: String,
        iconName: String,
        colorHex: String,
        type: CategoryType,
        parentCategoryId: Int? = null
    ) {
        viewModelScope.launch {
            val category = Category(
                name = name,
                iconName = iconName,
                colorHex = colorHex,
                type = type,
                parentCategoryId = parentCategoryId,
                isDefault = false
            )
            val result = repository.insertCategory(category)
            _categoryOpResult.postValue(result as Result<Any>)
        }
    }

    fun preloadDefaultCategories() {
        viewModelScope.launch {
            repository.preloadDefaults()
        }
    }

    fun updateCategoryDetails(id: Int, name: String, iconName: String, colorHex: String) {
        viewModelScope.launch {
            val result = repository.updateCategoryDetails(id, name, iconName, colorHex)
            _categoryOpResult.postValue(result as Result<Any>)
        }
    }

    fun softDeleteCategory(id: Int) {
        viewModelScope.launch {
            val result = repository.softDeleteCategory(id)
            _categoryOpResult.postValue(result as Result<Any>)
        }
    }

    fun deleteCategory(category: Category) {
        viewModelScope.launch {
            repository.deleteCategory(category)
        }
    }

    fun clearResult() {
        _categoryOpResult.value = null
    }
}