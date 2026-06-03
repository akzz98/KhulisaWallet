package com.example.khulisawallet.data

import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions
import kotlinx.coroutines.tasks.await

class FirebaseRepository(
    private val firestore: FirebaseFirestore = FirebaseFirestore.getInstance()
) {

    suspend fun saveExpense(expense: Expense): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(expense.userId.toString())
                .collection("expenses")
                .document(expense.id.toString())
                .set(
                    mapOf(
                        "id" to expense.id,
                        "userId" to expense.userId,
                        "categoryId" to expense.categoryId,
                        "title" to expense.title,
                        "amount" to expense.amount,
                        "type" to expense.type.name,
                        "date" to expense.date,
                        "note" to expense.note,
                        "imagePath" to expense.imagePath,
                        "isRecurring" to expense.isRecurring,
                        "recurringIntervalDays" to expense.recurringIntervalDays,
                        "createdAt" to expense.createdAt,
                        "updatedAt" to expense.updatedAt
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveGoal(goal: Goal): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(goal.userId.toString())
                .collection("goals")
                .document(goal.id.toString())
                .set(
                    mapOf(
                        "id" to goal.id,
                        "userId" to goal.userId,
                        "categoryId" to goal.categoryId,
                        "name" to goal.name,
                        "description" to goal.description,
                        "targetAmount" to goal.targetAmount,
                        "currentAmount" to goal.currentAmount,
                        "minGoal" to goal.minGoal,
                        "maxGoal" to goal.maxGoal,
                        "deadline" to goal.deadline,
                        "status" to goal.status.name,
                        "colorHex" to goal.colorHex,
                        "iconName" to goal.iconName,
                        "createdAt" to goal.createdAt,
                        "updatedAt" to goal.updatedAt
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveCategory(userId: Int, category: Category): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId.toString())
                .collection("categories")
                .document(category.id.toString())
                .set(
                    mapOf(
                        "id" to category.id,
                        "name" to category.name,
                        "iconName" to category.iconName,
                        "colorHex" to category.colorHex,
                        "type" to category.type.name,
                        "parentCategoryId" to category.parentCategoryId,
                        "isDefault" to category.isDefault,
                        "isActive" to category.isActive,
                        "createdAt" to category.createdAt,
                        "updatedAt" to category.updatedAt
                    )
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun deleteCategory(userId: Int, categoryId: Int): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(userId.toString())
                .collection("categories")
                .document(categoryId.toString())
                .delete()
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun saveUser(user: User): Result<Unit> {
        return try {
            firestore.collection("users")
                .document(user.id.toString())
                .set(
                    mapOf(
                        "id" to user.id,
                        "firstName" to user.firstName,
                        "lastName" to user.lastName,
                        "email" to user.email,
                        "createdAt" to user.createdAt,
                        "lastLogin" to user.lastLogin,
                        "isActive" to user.isActive,
                        "currentStreak" to user.currentStreak,
                        "lastActivityTimestamp" to user.lastActivityTimestamp,
                        "longestStreak" to user.longestStreak
                    ),
                    SetOptions.merge()
                )
                .await()

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }
}
