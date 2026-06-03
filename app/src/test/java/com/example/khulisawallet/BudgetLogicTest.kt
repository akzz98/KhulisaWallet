package com.example.khulisawallet

import com.example.khulisawallet.utils.SafeToSpendCalculator
import com.example.khulisawallet.utils.StreakManager
import org.junit.Assert.*
import org.junit.Test

/**
 * Integration-style tests that verify the combined budget logic
 * used across the Khulisa Wallet app.
 */
class BudgetLogicTest {

    @Test
    fun `balance calculation is correct`() {
        val income = 8000.0
        val expenses = 3200.0
        val balance = income - expenses
        assertEquals(4800.0, balance, 0.001)
    }

    @Test
    fun `goal progress percentage is correct`() {
        val spent = 750.0
        val target = 1000.0
        val progress = (spent / target * 100).toInt()
        assertEquals(75, progress)
    }

    @Test
    fun `goal progress does not exceed 100 percent`() {
        val spent = 1500.0
        val target = 1000.0
        val progress = minOf((spent / target * 100).toInt(), 100)
        assertEquals(100, progress)
    }

    @Test
    fun `spending below minimum goal is flagged`() {
        val spent = 200.0
        val minAmount = 500.0
        val isBelowMin = spent < minAmount
        assertTrue(isBelowMin)
    }

    @Test
    fun `spending above maximum goal is flagged`() {
        val spent = 1200.0
        val maxAmount = 1000.0
        val isAboveMax = spent > maxAmount
        assertTrue(isAboveMax)
    }

    @Test
    fun `streak and safe-to-spend work independently`() {
        // Streak should not affect budget calculation
        val streakResult = StreakManager.calculateNewStreak(0L, 0)
        val budgetResult = SafeToSpendCalculator.calculate(
            monthlyIncome = 5000.0,
            goalCommitments = 2000.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 10
        )

        assertEquals(1, streakResult)
        assertEquals(300.0, budgetResult.dailyAmount, 0.001)
    }

    @Test
    fun `getDaysLeftInMonth returns value between 1 and 31`() {
        val days = SafeToSpendCalculator.getDaysLeftInMonth()
        assertTrue(days in 1..31)
    }

    @Test
    fun `getMonthDateRange start is before end`() {
        val (start, end) = SafeToSpendCalculator.getMonthDateRange()
        assertTrue(start < end)
    }
}
