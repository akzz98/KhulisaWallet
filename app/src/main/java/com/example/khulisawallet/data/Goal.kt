package com.example.khulisawallet.data

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "goals",
    indices = [Index(value = ["userId"])],
    foreignKeys = [
        ForeignKey(
            entity = User::class,
            parentColumns = ["id"],
            childColumns = ["userId"],
            onDelete = ForeignKey.CASCADE   // Delete goals if user is deleted
        )
    ]
)
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,                            // Links to User.id
    val name: String,
    val description: String? = null,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    val deadline: Long? = null,                 // Optional target date
    val status: GoalStatus = GoalStatus.ACTIVE,
    val colorHex: String = "#2ECC71",           // For UI display
    val iconName: String = "ic_goal",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}