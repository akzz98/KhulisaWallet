package com.example.khulisawallet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "expenses",
    indices = [
        Index(value = ["categoryId"]),
        Index(value = ["userId"])
    ],
    foreignKeys = [
        ForeignKey(
            entity = Category::class,
            parentColumns = ["id"],
            childColumns = ["categoryId"],
            // Prevent deleting a category that has expenses
            onDelete = ForeignKey.RESTRICT
        ),
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            // Delete expenses if user is deleted
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Expense(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    // Links to User.id
    val userId: Int,
    // Links to Category.id
    val categoryId: Int,
    val title: String,
    val amount: Double,
    // EXPENSE or INCOME (mirrors category type)
    val type: CategoryType,
    val date: Long = System.currentTimeMillis(),
    val note: String? = null,
    // For photo receipt feature
    val imagePath: String? = null,
    val isRecurring: Boolean = false,
    // e.g. 30 for monthly
    val recurringIntervalDays: Int? = null,
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)