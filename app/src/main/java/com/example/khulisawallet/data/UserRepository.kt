package com.example.khulisawallet.data

import androidx.lifecycle.LiveData

class UserRepository(private val userDao: UserDao) {

    // --- CREATE ---
    suspend fun registerUser(user: User): Result<Long> {
        return try {
            val id = userDao.insertUser(user)
            Result.success(id)
        } catch (e: Exception) {
            // Catches duplicate email exception from the abort strategy
            Result.failure(e)
        }
    }

    // --- READ ---
    suspend fun getUserById(userId: Int): User? {
        return userDao.getUserById(userId)
    }

    fun getUserByIdLiveData(userId: Int): LiveData<User?> {
        return userDao.getUserByIdLiveData(userId)
    }

    suspend fun getUserByIdOnce(id: Int): User? {
        return userDao.getUserById(id)
    }

    suspend fun getUserByEmail(email: String): User? {
        return userDao.getUserByEmail(email)
    }

    suspend fun login(email: String, passwordHash: String): Result<User> {
        return try {
            val user = userDao.login(email, passwordHash)
            if (user != null) {
                // Update the last login timestamp
                userDao.updateLastLogin(user.id)
                Result.success(user)
            } else {
                Result.failure(Exception("Invalid email or password"))
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun getAllActiveUsers(): LiveData<List<User>> {
        return userDao.getAllActiveUsers()
    }

    // --- UPDATE ---
    suspend fun updateUser(user: User): Result<Unit> {
        return try {
            userDao.updateUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updateProfileImage(userId: Int, path: String): Result<Unit> {
        return try {
            userDao.updateProfileImage(userId, path)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun updatePassword(userId: Int, newPassword: String): Result<Unit> {
        return try {
            userDao.updatePassword(userId, newPassword)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deactivateUser(userId: Int): Result<Unit> {
        return try {
            userDao.deactivateUser(userId)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    // --- DELETE ---
    suspend fun deleteUser(user: User): Result<Unit> {
        return try {
            userDao.deleteUser(user)
            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}