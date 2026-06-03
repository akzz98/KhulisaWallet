package com.example.khulisawallet.data

import com.google.firebase.firestore.DocumentSnapshot

private fun DocumentSnapshot.number(key: String): Double? =
    (get(key) as? Number)?.toDouble()

private fun DocumentSnapshot.longValue(key: String): Long? =
    when (val value = get(key)) {
        is Long -> value
        is Int -> value.toLong()
        is Double -> value.toLong()
        else -> null
    }

private fun DocumentSnapshot.intValue(key: String): Int? =
    when (val value = get(key)) {
        is Long -> value.toInt()
        is Int -> value
        is Double -> value.toInt()
        else -> null
    }

fun DocumentSnapshot.toCategory(): Category? {
    val id = intValue("id") ?: return null
    val name = getString("name") ?: return null
    val iconName = getString("iconName") ?: "ic_other"
    val colorHex = getString("colorHex") ?: "#B0B0B0"
    val typeName = getString("type") ?: return null

    return try {
        Category(
            id = id,
            name = name,
            iconName = iconName,
            colorHex = colorHex,
            type = CategoryType.valueOf(typeName),
            parentCategoryId = intValue("parentCategoryId"),
            isDefault = getBoolean("isDefault") ?: false,
            isActive = getBoolean("isActive") ?: true,
            createdAt = longValue("createdAt") ?: System.currentTimeMillis(),
            updatedAt = longValue("updatedAt")
        )
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun DocumentSnapshot.toExpense(): Expense? {
    val id = intValue("id") ?: return null
    val userId = intValue("userId") ?: return null
    val categoryId = intValue("categoryId") ?: return null
    val title = getString("title") ?: return null
    val amount = number("amount") ?: return null
    val typeName = getString("type") ?: return null

    return try {
        Expense(
            id = id,
            userId = userId,
            categoryId = categoryId,
            title = title,
            amount = amount,
            type = CategoryType.valueOf(typeName),
            date = longValue("date") ?: System.currentTimeMillis(),
            note = getString("note"),
            imagePath = getString("imagePath"),
            isRecurring = getBoolean("isRecurring") ?: false,
            recurringIntervalDays = intValue("recurringIntervalDays"),
            createdAt = longValue("createdAt") ?: System.currentTimeMillis(),
            updatedAt = longValue("updatedAt")
        )
    } catch (_: IllegalArgumentException) {
        null
    }
}

fun DocumentSnapshot.toGoal(): Goal? {
    val id = intValue("id") ?: return null
    val userId = intValue("userId") ?: return null
    val categoryId = intValue("categoryId") ?: return null
    val name = getString("name") ?: return null
    val targetAmount = number("targetAmount") ?: return null
    val statusName = getString("status") ?: GoalStatus.ACTIVE.name

    return try {
        Goal(
            id = id,
            userId = userId,
            categoryId = categoryId,
            name = name,
            description = getString("description"),
            targetAmount = targetAmount,
            currentAmount = number("currentAmount") ?: 0.0,
            minGoal = number("minGoal"),
            maxGoal = number("maxGoal"),
            deadline = longValue("deadline"),
            status = GoalStatus.valueOf(statusName),
            colorHex = getString("colorHex") ?: "#2ECC71",
            iconName = getString("iconName") ?: "ic_goal",
            createdAt = longValue("createdAt") ?: System.currentTimeMillis(),
            updatedAt = longValue("updatedAt")
        )
    } catch (_: IllegalArgumentException) {
        null
    }
}

data class FirebaseUserProfile(
    val firstName: String?,
    val lastName: String?,
    val email: String?,
    val currentStreak: Int,
    val lastActivityTimestamp: Long,
    val longestStreak: Int
)

fun DocumentSnapshot.toUserProfile(): FirebaseUserProfile? {
    return FirebaseUserProfile(
        firstName = getString("firstName"),
        lastName = getString("lastName"),
        email = getString("email"),
        currentStreak = intValue("currentStreak") ?: 0,
        lastActivityTimestamp = longValue("lastActivityTimestamp") ?: 0L,
        longestStreak = intValue("longestStreak") ?: 0
    )
}
