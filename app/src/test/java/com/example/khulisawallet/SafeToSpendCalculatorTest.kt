package com.example.khulisawallet

import com.example.khulisawallet.utils.SafeToSpendCalculator
import com.example.khulisawallet.utils.SpendStatus
import org.junit.Assert.*
import org.junit.Test

class SafeToSpendCalculatorTest {

    @Test
    fun `no income returns NO_INCOME status`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 0.0,
            goalCommitments = 0.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 15
        )
        assertEquals(SpendStatus.NO_INCOME, result.status)
        assertEquals(0.0, result.dailyAmount, 0.001)
    }

    @Test
    fun `income less than goal commitments returns TIGHT status`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 1000.0,
            goalCommitments = 1500.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 15
        )
        assertEquals(SpendStatus.TIGHT, result.status)
        assertEquals(0.0, result.dailyAmount, 0.001)
    }

    @Test
    fun `income equal to goal commitments returns TIGHT status`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 1000.0,
            goalCommitments = 1000.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 15
        )
        assertEquals(SpendStatus.TIGHT, result.status)
    }

    @Test
    fun `healthy budget calculates correct daily amount`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 5000.0,
            goalCommitments = 2000.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 10
        )
        // (5000 - 2000 - 0) / 10 = 300.0
        assertEquals(SpendStatus.HEALTHY, result.status)
        assertEquals(300.0, result.dailyAmount, 0.001)
    }

    @Test
    fun `monthly spending reduces daily safe amount`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 5000.0,
            goalCommitments = 2000.0,
            monthlySpent = 1000.0,
            daysLeftInMonth = 10
        )
        // (5000 - 2000 - 1000) / 10 = 200.0
        assertEquals(SpendStatus.HEALTHY, result.status)
        assertEquals(200.0, result.dailyAmount, 0.001)
    }

    @Test
    fun `spending exceeds remaining budget returns TIGHT status`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 5000.0,
            goalCommitments = 2000.0,
            monthlySpent = 3500.0,
            daysLeftInMonth = 10
        )
        assertEquals(SpendStatus.TIGHT, result.status)
        assertEquals(0.0, result.dailyAmount, 0.001)
    }

    @Test
    fun `single day left calculates full remaining amount`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 3000.0,
            goalCommitments = 1000.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 1
        )
        // (3000 - 1000 - 0) / 1 = 2000.0
        assertEquals(2000.0, result.dailyAmount, 0.001)
    }

    @Test
    fun `zero days left defaults to 1 day to avoid division by zero`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 3000.0,
            goalCommitments = 1000.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 0
        )
        // Should not crash, defaults to 1 day
        assertEquals(2000.0, result.dailyAmount, 0.001)
    }

    @Test
    fun `no goal commitments uses full income minus spending`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 6000.0,
            goalCommitments = 0.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 20
        )
        // 6000 / 20 = 300.0
        assertEquals(300.0, result.dailyAmount, 0.001)
        assertEquals(SpendStatus.HEALTHY, result.status)
    }

    @Test
    fun `message contains R and amount when healthy`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 5000.0,
            goalCommitments = 2000.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 10
        )
        assertTrue(result.message.contains("R"))
        assertTrue(result.message.contains("300.00"))
    }

    @Test
    fun `tight message warns user`() {
        val result = SafeToSpendCalculator.calculate(
            monthlyIncome = 500.0,
            goalCommitments = 1000.0,
            monthlySpent = 0.0,
            daysLeftInMonth = 10
        )
        assertTrue(result.message.contains("tight") || result.message.contains("avoid"))
    }
}
