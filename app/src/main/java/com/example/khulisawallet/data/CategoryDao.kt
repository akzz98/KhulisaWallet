package com.example.khulisawallet.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface CategoryDao {

    // --- CREATE ---
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCategory(category: Category): Long

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertAll(categories: List<Category>)

    // --- READ ---
    @Query("SELECT * FROM categories WHERE isActive = 1 ORDER BY name ASC")
    fun getAllActiveCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE type = :type AND isActive = 1 ORDER BY name ASC")
    fun getCategoriesByType(type: String): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE parentCategoryId IS NULL AND isActive = 1 ORDER BY name ASC")
    fun getParentCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE parentCategoryId = :parentId AND isActive = 1 ORDER BY name ASC")
    fun getSubCategories(parentId: Int): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE id = :id")
    suspend fun getCategoryById(id: Int): Category?

    @Query("SELECT * FROM categories WHERE isDefault = 1 AND isActive = 1")
    fun getDefaultCategories(): LiveData<List<Category>>

    @Query("SELECT * FROM categories WHERE isDefault = 0 AND isActive = 1")
    fun getUserCategories(): LiveData<List<Category>>

    // --- UPDATE ---
    @Update
    suspend fun updateCategory(category: Category)

    @Query("UPDATE categories SET isActive = 0, updatedAt = :timestamp WHERE id = :id")
    suspend fun softDeleteCategory(id: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE categories SET name = :name, iconName = :iconName, colorHex = :colorHex, updatedAt = :timestamp WHERE id = :id")
    suspend fun updateCategoryDetails(
        id: Int,
        name: String,
        iconName: String,
        colorHex: String,
        timestamp: Long = System.currentTimeMillis()
    )

    // --- DELETE ---
    @Delete
    suspend fun deleteCategory(category: Category)

    @Query("DELETE FROM categories WHERE isDefault = 0")
    suspend fun deleteAllUserCategories()
}