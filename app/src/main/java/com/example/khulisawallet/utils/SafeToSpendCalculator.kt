package com.example.khulisawallet.utils

import java.util.Calendar

object SafeToSpendCalculator {

    /**
     * Calculates how much the user can safely spend today.
     *
     * Formula: (monthlyIncome - goalCommitments - monthlySpent) / daysLeftInMonth
     *
     * @param monthlyIncome      Total income logged this month
     * @param goalCommitments    Sum of active goal maxGoal values (budget caps)
     * @param monthlySpent       Total expenses logged this month
     * @param daysLeftInMonth    Days remaining in the current month (including today)
     */
    fun calculate(
        monthlyIncome: Double,
        goalCommitments: Double,
        monthlySpent: Double,
        daysLeftInMonth: Int = getDaysLeftInMonth()
    ): SafeToSpendResult {
        val available = monthlyIncome - goalCommitments - monthlySpent
        val days = if (daysLeftInMonth < 1) 1 else daysLeftInMonth

        return when {
            monthlyIncome <= 0.0 -> SafeToSpendResult(
                dailyAmount = 0.0,
                status = SpendStatus.NO_INCOME,
                message = "Log your income to see your daily budget"
            )
            available <= 0.0 -> SafeToSpendResult(
                dailyAmount = 0.0,
                status = SpendStatus.TIGHT,
                message = "Budget tight — little left this month"
            )
            else -> {
                val daily = available / days
                SafeToSpendResult(
                    dailyAmount = daily,
                    status = if (daily > 0) SpendStatus.HEALTHY else SpendStatus.TIGHT,
                    message = "R %.2f safe to spend today".format(daily)
                )
            }
        }
    }

    fun getDaysLeftInMonth(): Int {
        val cal = Calendar.getInstance()
        val totalDays = cal.getActualMaximum(Calendar.DAY_OF_MONTH)
        val today = cal.get(Calendar.DAY_OF_MONTH)
        return totalDays - today + 1 // include today
    }

    fun getMonthDateRange(): Pair<Long, Long> {
        val cal = Calendar.getInstance()
        cal.set(Calendar.DAY_OF_MONTH, 1)
        cal.set(Calendar.HOUR_OF_DAY, 0)
        cal.set(Calendar.MINUTE, 0)
        cal.set(Calendar.SECOND, 0)
        cal.set(Calendar.MILLISECOND, 0)
        val start = cal.timeInMillis

        cal.set(Calendar.DAY_OF_MONTH, cal.getActualMaximum(Calendar.DAY_OF_MONTH))
        cal.set(Calendar.HOUR_OF_DAY, 23)
        cal.set(Calendar.MINUTE, 59)
        cal.set(Calendar.SECOND, 59)
        val end = cal.timeInMillis

        return Pair(start, end)
    }
}

data class SafeToSpendResult(
    val dailyAmount: Double,
    val status: SpendStatus,
    val message: String
)

enum class SpendStatus { HEALTHY, TIGHT, NO_INCOME }
