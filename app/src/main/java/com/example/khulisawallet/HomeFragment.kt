package com.example.khulisawallet

import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.Category
import com.example.khulisawallet.data.CategoryRepository
import com.example.khulisawallet.data.CategoryType
import com.example.khulisawallet.data.Expense
import com.example.khulisawallet.data.ExpenseRepository
import com.example.khulisawallet.data.ExpenseWithCategory
import com.example.khulisawallet.data.GoalRepository
import com.example.khulisawallet.data.UserRepository
import com.example.khulisawallet.utils.SafeToSpendCalculator
import com.example.khulisawallet.utils.SpendStatus
import com.example.khulisawallet.utils.StreakManager
import com.example.khulisawallet.viewmodel.CategoryViewModel
import com.example.khulisawallet.viewmodel.CategoryViewModelFactory
import com.example.khulisawallet.viewmodel.ExpenseViewModel
import com.example.khulisawallet.viewmodel.ExpenseViewModelFactory
import com.example.khulisawallet.viewmodel.GoalViewModel
import com.example.khulisawallet.viewmodel.GoalViewModelFactory
import com.example.khulisawallet.viewmodel.UserViewModel
import com.example.khulisawallet.viewmodel.UserViewModelFactory
import com.github.mikephil.charting.animation.Easing
import com.github.mikephil.charting.charts.PieChart
import com.github.mikephil.charting.data.PieData
import com.github.mikephil.charting.data.PieDataSet
import com.github.mikephil.charting.data.PieEntry
import com.github.mikephil.charting.utils.ColorTemplate
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var goalViewModel: GoalViewModel
    private lateinit var userViewModel: UserViewModel
    private lateinit var expenseAdapter: ExpenseAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // --- Get logged-in userId from SharedPreferences ---
        val prefs = requireContext().getSharedPreferences("khulisa_prefs", 0)
        val userId = prefs.getInt("user_id", -1)
        if (userId == -1) return  // Safety check

        // --- Setup ViewModels ---
        val db = AppDatabase.getDatabase(requireContext())

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(ExpenseRepository(db.expenseDao(), db.goalDao(), db.userDao()))
        )[ExpenseViewModel::class.java]

        categoryViewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(CategoryRepository(db.categoryDao()))
        )[CategoryViewModel::class.java]

        goalViewModel = ViewModelProvider(
            this,
            GoalViewModelFactory(
                GoalRepository(db.goalDao(), db.expenseDao(), db.categoryDao()),
                ExpenseRepository(db.expenseDao(), db.goalDao(), db.userDao()),
                CategoryRepository(db.categoryDao())
            )
        )[GoalViewModel::class.java]

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(UserRepository(db.userDao()))
        )[UserViewModel::class.java]

        expenseViewModel.setUser(userId)
        goalViewModel.setUser(userId)

        val tvStreakCount = view.findViewById<TextView>(R.id.tv_streak_count)
        val tvStreakBadge = view.findViewById<TextView>(R.id.tv_streak_badge)

        userViewModel.getUserById(userId).observe(viewLifecycleOwner) { user ->
            user ?: return@observe
            tvStreakCount.text = StreakManager.formatStreakCount(user.currentStreak)
            tvStreakBadge.text = StreakManager.formatRankLabel(user.currentStreak)
        }

        // --- Greeting & Date ---
        val userName = prefs.getString("user_first_name", "User") ?: "User"
        view.findViewById<TextView>(R.id.tv_greeting).text = "Hello, $userName 👋"
        val dateStr = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        view.findViewById<TextView>(R.id.tv_date).text = dateStr

        val pieChart = view.findViewById<PieChart>(R.id.spendingPieChart)
        setupPieChart(pieChart)

        var latestExpenses: List<ExpenseWithCategory> = emptyList()
        var latestCategories: List<Category> = emptyList()

        fun refreshChart() {
            val expenseOnly = latestExpenses
                .map { it.expense }
                .filter { it.type == CategoryType.EXPENSE }
            if (expenseOnly.isNotEmpty() && latestCategories.isNotEmpty()) {
                updateChartData(pieChart, expenseOnly, latestCategories)
            }
        }

        categoryViewModel.allCategories.observe(viewLifecycleOwner) { categories ->
            latestCategories = categories
            refreshChart()
        }

        // --- RecyclerView Setup ---
        expenseAdapter = ExpenseAdapter()
        val rv = view.findViewById<RecyclerView>(R.id.rv_recent_expenses)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = expenseAdapter

        // --- Observe Recent Expenses (last 5) ---
        val tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            latestExpenses = expenses
            refreshChart()

            val recent = expenses.take(5)
            expenseAdapter.submitList(recent)
            tvEmpty.visibility = if (expenses.isEmpty()) View.VISIBLE else View.GONE
            rv.visibility = if (expenses.isEmpty()) View.GONE else View.VISIBLE
        }

        // --- Observe Totals ---
        val tvBalance = view.findViewById<TextView>(R.id.tv_balance)
        val tvIncome = view.findViewById<TextView>(R.id.tv_total_income)
        val tvExpense = view.findViewById<TextView>(R.id.tv_total_expense)

        var totalIncome = 0.0
        var totalExpense = 0.0

        expenseViewModel.totalIncome.observe(viewLifecycleOwner) { income ->
            totalIncome = income ?: 0.0
            tvIncome.text = "R %.2f".format(totalIncome)
            tvBalance.text = "R %.2f".format(totalIncome - totalExpense)
        }

        expenseViewModel.totalExpenses.observe(viewLifecycleOwner) { expense ->
            totalExpense = expense ?: 0.0
            tvExpense.text = "R %.2f".format(totalExpense)
            tvBalance.text = "R %.2f".format(totalIncome - totalExpense)
        }

        // --- Safe-to-Spend ---
        val tvSafeAmount = view.findViewById<TextView>(R.id.tv_safe_amount)
        val tvSafeMessage = view.findViewById<TextView>(R.id.tv_safe_message)
        val tvMonthlyIncome = view.findViewById<TextView>(R.id.tv_monthly_income)
        val tvGoalCommitments = view.findViewById<TextView>(R.id.tv_goal_commitments)
        val tvDaysRemaining = view.findViewById<TextView>(R.id.tv_days_remaining)
        val tvDaysLeft = view.findViewById<TextView>(R.id.tv_days_left)
        val cardSafeToSpend = view.findViewById<MaterialCardView>(R.id.card_safe_to_spend)

        val daysLeft = SafeToSpendCalculator.getDaysLeftInMonth()
        tvDaysLeft.text = "$daysLeft days left"
        tvDaysRemaining.text = "$daysLeft"

        var safeIncome = 0.0
        var safeGoalMax = 0.0

        fun refreshSafeToSpend() {
            val result = SafeToSpendCalculator.calculate(safeIncome, safeGoalMax, daysLeft)
            tvSafeAmount.text = "R %.2f".format(result.dailyAmount)
            tvSafeMessage.text = result.message
            tvMonthlyIncome.text = "R %.2f".format(safeIncome)
            tvGoalCommitments.text = "R %.2f".format(safeGoalMax)

            val bgColor = when (result.status) {
                SpendStatus.HEALTHY -> requireContext().getColor(R.color.colorPrimary)
                SpendStatus.TIGHT -> requireContext().getColor(android.R.color.holo_red_dark)
                SpendStatus.NO_INCOME -> requireContext().getColor(android.R.color.darker_gray)
            }
            cardSafeToSpend.setCardBackgroundColor(bgColor)
        }

        val (monthStart, monthEnd) = SafeToSpendCalculator.getMonthDateRange()
        expenseViewModel.getExpensesByDateRange(monthStart, monthEnd)
            .observe(viewLifecycleOwner) { expenses ->
                safeIncome = expenses
                    .filter { it.expense.type == CategoryType.INCOME }
                    .sumOf { it.expense.amount }
                refreshSafeToSpend()
            }

        goalViewModel.activeGoals.observe(viewLifecycleOwner) { goals ->
            safeGoalMax = goals.sumOf { it.maxGoal ?: 0.0 }
            refreshSafeToSpend()
        }

        // --- Observe Goal Alerts ---
        val cardAlert = view.findViewById<CardView>(R.id.card_goal_alert)
        val tvAlert = view.findViewById<TextView>(R.id.tv_goal_alert)

        goalViewModel.goalsBelowMinimum.observe(viewLifecycleOwner) { belowMin ->
            goalViewModel.goalsExceedingMaximum.observe(viewLifecycleOwner) { aboveMax ->
                val alerts = mutableListOf<String>()
                if (belowMin.isNotEmpty()) alerts.add("${belowMin.size} goal(s) below minimum target")
                if (aboveMax.isNotEmpty()) alerts.add("${aboveMax.size} goal(s) exceeding maximum limit")
                if (alerts.isNotEmpty()) {
                    cardAlert.visibility = View.VISIBLE
                    tvAlert.text = alerts.joinToString(" • ")
                } else {
                    cardAlert.visibility = View.GONE
                }
            }
        }

        // --- See All → navigate to History tab ---
        view.findViewById<TextView>(R.id.tv_see_all).setOnClickListener {
            requireActivity().findViewById<com.google.android.material.bottomnavigation.BottomNavigationView>(
                R.id.bottom_nav
            ).selectedItemId = R.id.navigation_history
        }
    }

    private fun setupPieChart(pieChart: PieChart) {
        pieChart.setUsePercentValues(true)
        pieChart.description.isEnabled = false
        pieChart.setExtraOffsets(5f, 10f, 5f, 5f)
        pieChart.dragDecelerationFrictionCoef = 0.95f
        pieChart.isDrawHoleEnabled = true
        pieChart.setHoleColor(android.graphics.Color.WHITE)
        pieChart.setTransparentCircleRadius(61f)
        pieChart.holeRadius = 58f
        pieChart.centerText = "Spending"
        pieChart.setCenterTextSize(20f)
        pieChart.animateY(1400, Easing.EaseInOutQuad)
    }

    private fun updateChartData(
        pieChart: PieChart,
        expenses: List<Expense>,
        categories: List<Category>
    ) {
        val entries = ArrayList<PieEntry>()

        val spendingByCategory = expenses.groupBy { it.categoryId }
            .mapValues { entry -> entry.value.sumOf { it.amount } }

        spendingByCategory.forEach { (catId, total) ->
            val categoryName = categories.find { it.id == catId }?.name ?: "Unknown"
            entries.add(PieEntry(total.toFloat(), categoryName))
        }

        val dataSet = PieDataSet(entries, "Categories")
        dataSet.sliceSpace = 3f
        dataSet.selectionShift = 5f
        dataSet.colors = ColorTemplate.MATERIAL_COLORS.toMutableList()

        val data = PieData(dataSet)
        data.setValueTextSize(12f)
        data.setValueTextColor(android.graphics.Color.WHITE)

        pieChart.data = data
        pieChart.invalidate()
    }
}