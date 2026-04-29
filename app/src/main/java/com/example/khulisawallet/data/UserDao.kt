package com.example.khulisawallet.data

import androidx.lifecycle.LiveData
import androidx.room.*

@Dao
interface UserDao {

    // --- CREATE The User ---
    // Terminate if email already exists
    @Insert(onConflict = OnConflictStrategy.ABORT)
    // Returns the new row ID
    suspend fun insertUser(user: User): Long

    // --- READ User ---
    @Query("SELECT * FROM users WHERE id = :userId")
    suspend fun getUserById(userId: Int): User?

    @Query("SELECT * FROM users WHERE email = :email LIMIT 1")
    suspend fun getUserByEmail(email: String): User?

    @Query("SELECT * FROM users WHERE email = :email AND passwordHash = :passwordHash LIMIT 1")
    suspend fun login(email: String, passwordHash: String): User?

    @Query("SELECT * FROM users WHERE isActive = 1")
    // LiveData for reactive UI updates
    fun getAllActiveUsers(): LiveData<List<User>>

    // --- UPDATE User ---
    @Update
    suspend fun updateUser(user: User)

    @Query("UPDATE users SET lastLogin = :timestamp WHERE id = :userId")
    suspend fun updateLastLogin(userId: Int, timestamp: Long = System.currentTimeMillis())

    @Query("UPDATE users SET profileImagePath = :path WHERE id = :userId")
    suspend fun updateProfileImage(userId: Int, path: String)

    @Query("UPDATE users SET isActive = 0 WHERE id = :userId")
    // Soft delete (keeps data intact)
    suspend fun deactivateUser(userId: Int)

    // --- DELETE User ---
    @Delete
    // Hard delete when needed
    suspend fun deleteUser(user: User)
}