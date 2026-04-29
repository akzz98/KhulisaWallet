package com.example.khulisawallet.data

import androidx.lifecycle.LiveData

class CategoryRepository(private val categoryDao: CategoryDao) {

    val allActiveCategories: LiveData<List<Category>> = categoryDao.getAllActiveCategories()
    val defaultCategories: LiveData<List<Category>> = categoryDao.getDefaultCategories()
    val userCategories: LiveData<List<Category>> = categoryDao.getUserCategories()
    val parentCategories: LiveData<List<Category>> = categoryDao.getParentCategories()

    fun getCategoriesByType(type: CategoryType): LiveData<List<Category>> {
        return categoryDao.getCategoriesByType(type.name)
    }

    fun getSubCategories(parentId: Int): LiveData<List<Category>> {
        return categoryDao.getSubCategories(parentId)
    }

    suspend fun getCategoryById(id: Int): Category? {
        return categoryDao.getCategoryById(id)
    }

    suspend fun insertCategory(category: Category): Result<Long> {
        return try {
            val id = categoryDao.insertCategory(category)
            Result.success(id)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun preloadDefaults() {
        val defaults = listOf(
            // EXPENSE categories
            Category(name = "Food & Groceries",  iconName = "ic_food",       colorHex = "#FF6B6B", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Transport",          iconName = "ic_transport",  colorHex = "#4ECDC4", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Housing",            iconName = "ic_home",       colorHex = "#45B7D1", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Entertainment",      iconName = "ic_entertain",  colorHex = "#96CEB4", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Healthcare",         iconName = "ic_health",     colorHex = "#FFEAA7", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Education",          iconName = "ic_education",  colorHex = "#DDA0DD", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Savings",            iconName = "ic_savings",    colorHex = "#98D8C8", type = CategoryType.EXPENSE, isDefault = true),
            Category(name = "Other",              iconName = "ic_other",      colorHex = "#B0B0B0", type = CategoryType.EXPENSE, isDefault = true),
            // INCOME categories
            Category(name = "Salary",             iconName = "ic_salary",     colorHex = "#2ECC71", type = CategoryType.INCOME, isDefault = true),
            Category(name = "Freelance",          iconName = "ic_freelance",  colorHex = "#F39C12", type = CategoryType.INCOME, isDefault = true),
            Category(name = "Investment",         iconName = "ic_invest",     colorHex = "#9B59B6", type = CategoryType.INCOME, isDefault = true),
            Category(name = "Gift",               iconName = "ic_gift",       colorHex = "#E91E63", type = CategoryType.INCOME, isDefault = true),
            Category(name = "Other Income",       iconName = "ic_other",      colorHex = "#607D8B", type = CategoryType.INCOME, isDefault = true)
        )
        categoryDao.insertAll(defaults)
    }

    suspend fun updateCategory(category: Category): Result<Unit> {
        return try {
            categoryDao.updateCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateCategoryDetails(
        id: Int,
        name: String,
        iconName: String,
        colorHex: String
    ): Result<Unit> {
        return try {
            categoryDao.updateCategoryDetails(id, name, iconName, colorHex)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun softDeleteCategory(id: Int): Result<Unit> {
        return try {
            categoryDao.softDeleteCategory(id)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(category: Category): Result<Unit> {
        return try {
            categoryDao.deleteCategory(category)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}