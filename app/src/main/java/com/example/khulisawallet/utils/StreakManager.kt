package com.example.khulisawallet.utils

import java.util.*
import java.util.concurrent.TimeUnit

object StreakManager {

    fun calculateNewStreak(lastActivityDate: Long, currentStreak: Int): Int {
        if (lastActivityDate == 0L) return 1 // First ever activity

        val now = Calendar.getInstance().apply {
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val last = Calendar.getInstance().apply {
            timeInMillis = lastActivityDate
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis

        val diff = now - last
        val days = TimeUnit.MILLISECONDS.toDays(diff)

        return when {
            days == 0L -> currentStreak // Already active today, streak stays the same
            days == 1L -> currentStreak + 1 // Consecutive day!
            else -> 1 // Streak broken, restart at 1
        }
    }

    fun getBadgeTitle(streak: Int): String {
        return when {
            streak >= 30 -> "Financial Forest (Tree)"
            streak >= 14 -> "Strong Sapling"
            streak >= 7 -> "Fresh Sprout"
            streak >= 3 -> "Active Seed"
            else -> "Getting Started"
        }
    }
}
