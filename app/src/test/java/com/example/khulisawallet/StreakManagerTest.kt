package com.example.khulisawallet

import com.example.khulisawallet.utils.StreakManager
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar
import java.util.concurrent.TimeUnit

class StreakManagerTest {

    // Helper: get a timestamp for N days ago (midnight)
    private fun daysAgo(days: Int): Long {
        val cal = Calendar.getInstance().apply {
            add(Calendar.DAY_OF_MONTH, -days)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }
        return cal.timeInMillis
    }

    @Test
    fun `first ever activity returns streak of 1`() {
        val result = StreakManager.calculateNewStreak(
            lastActivityDate = 0L,
            currentStreak = 0
        )
        assertEquals(1, result)
    }
}