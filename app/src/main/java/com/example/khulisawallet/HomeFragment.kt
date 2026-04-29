package com.example.khulisawallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import androidx.cardview.widget.CardView
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.ExpenseRepository
import com.example.khulisawallet.data.GoalRepository
import com.example.khulisawallet.viewmodel.ExpenseViewModel
import com.example.khulisawallet.viewmodel.ExpenseViewModelFactory
import com.example.khulisawallet.viewmodel.GoalViewModel
import com.example.khulisawallet.viewmodel.GoalViewModelFactory
import java.text.SimpleDateFormat
import java.util.*

class HomeFragment : Fragment(R.layout.fragment_home) {

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var goalViewModel: GoalViewModel
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
            ExpenseViewModelFactory(ExpenseRepository(db.expenseDao()))
        )[ExpenseViewModel::class.java]

        goalViewModel = ViewModelProvider(
            this,
            GoalViewModelFactory(GoalRepository(db.goalDao()))
        )[GoalViewModel::class.java]

        expenseViewModel.setUser(userId)
        goalViewModel.setUser(userId)

        // --- Greeting & Date ---
        val userName = prefs.getString("user_first_name", "User") ?: "User"
        view.findViewById<TextView>(R.id.tv_greeting).text = "Hello, $userName 👋"
        val dateStr = SimpleDateFormat("EEEE, dd MMM yyyy", Locale.getDefault()).format(Date())
        view.findViewById<TextView>(R.id.tv_date).text = dateStr

        // --- RecyclerView Setup ---
        expenseAdapter = ExpenseAdapter()
        val rv = view.findViewById<RecyclerView>(R.id.rv_recent_expenses)
        rv.layoutManager = LinearLayoutManager(requireContext())
        rv.adapter = expenseAdapter

        // --- Observe Recent Expenses (last 5) ---
        val tvEmpty = view.findViewById<TextView>(R.id.tv_empty)
        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
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
}