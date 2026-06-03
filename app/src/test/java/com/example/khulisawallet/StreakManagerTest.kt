package com.example.khulisawallet

import com.example.khulisawallet.utils.StreakManager
import org.junit.Assert.*
import org.junit.Test
import java.util.Calendar

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

    @Test
    fun `activity on same day keeps streak unchanged`() {
        val todayTimestamp = daysAgo(0)
        val result = StreakManager.calculateNewStreak(
            lastActivityDate = todayTimestamp,
            currentStreak = 5
        )
        assertEquals(5, result)
    }

    @Test
    fun `activity on consecutive day increments streak`() {
        val yesterdayTimestamp = daysAgo(1)
        val result = StreakManager.calculateNewStreak(
            lastActivityDate = yesterdayTimestamp,
            currentStreak = 4
        )
        assertEquals(5, result)
    }

    @Test
    fun `missed day resets streak to 1`() {
        val twoDaysAgo = daysAgo(2)
        val result = StreakManager.calculateNewStreak(
            lastActivityDate = twoDaysAgo,
            currentStreak = 10
        )
        assertEquals(1, result)
    }

    @Test
    fun `long gap resets streak to 1`() {
        val longAgo = daysAgo(30)
        val result = StreakManager.calculateNewStreak(
            lastActivityDate = longAgo,
            currentStreak = 25
        )
        assertEquals(1, result)
    }

    @Test
    fun `badge title for 0 streak is Getting Started`() {
        assertEquals("Getting Started", StreakManager.getBadgeTitle(0))
    }

    @Test
    fun `badge title for 3 days is Active Seed`() {
        assertEquals("Active Seed", StreakManager.getBadgeTitle(3))
    }

    @Test
    fun `badge title for 7 days is Fresh Sprout`() {
        assertEquals("Fresh Sprout", StreakManager.getBadgeTitle(7))
    }

    @Test
    fun `badge title for 14 days is Strong Sapling`() {
        assertEquals("Strong Sapling", StreakManager.getBadgeTitle(14))
    }

    @Test
    fun `badge title for 30 days is Financial Forest`() {
        assertEquals("Financial Forest (Tree)", StreakManager.getBadgeTitle(30))
    }

    @Test
    fun `formatStreakCount for 0 returns start message`() {
        assertEquals("Start your streak today!", StreakManager.formatStreakCount(0))
    }

    @Test
    fun `formatStreakCount for 1 returns singular`() {
        assertEquals("1 Day Streak!", StreakManager.formatStreakCount(1))
    }

    @Test
    fun `formatStreakCount for 7 returns plural`() {
        assertEquals("7 Day Streak!", StreakManager.formatStreakCount(7))
    }
}
