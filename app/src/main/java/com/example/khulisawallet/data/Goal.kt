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
            onDelete = ForeignKey.CASCADE
        )
    ]
)
data class Goal(
    @PrimaryKey(autoGenerate = true) val id: Int = 0,
    val userId: Int,
    val name: String,
    val description: String? = null,
    val targetAmount: Double,
    val currentAmount: Double = 0.0,
    // Minimum threshold (e.g. must save at least R500)
    val minGoal: Double? = null,
    // Maximum threshold (e.g. don't spend more than R2000)
    val maxGoal: Double? = null,
    val deadline: Long? = null,
    val status: GoalStatus = GoalStatus.ACTIVE,
    val colorHex: String = "#2ECC71",
    val iconName: String = "ic_goal",
    val createdAt: Long = System.currentTimeMillis(),
    val updatedAt: Long? = null
)

enum class GoalStatus {
    ACTIVE,
    COMPLETED,
    CANCELLED
}