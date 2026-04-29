package com.example.khulisawallet

import android.app.AlertDialog
import android.os.Bundle
import android.view.View
import android.widget.*
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.khulisawallet.data.*
import com.example.khulisawallet.viewmodel.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText

class GoalsFragment : Fragment(R.layout.fragment_goals) {

    private lateinit var goalViewModel: GoalViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var goalAdapter: GoalAdapter

    private var categories: List<Category> = emptyList()
    private var userId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("khulisa_prefs", 0)
        userId = prefs.getInt("user_id", -1)
        if (userId == -1) return

        setupViewModels()
        setupRecyclerView(view)
        observeGoals(view)

        view.findViewById<MaterialButton>(R.id.btn_add_goal).setOnClickListener {
            showAddGoalDialog()
        }
    }

    private fun setupViewModels() {
        val db = AppDatabase.getDatabase(requireContext())

        goalViewModel = ViewModelProvider(
            this,
            GoalViewModelFactory(GoalRepository(db.goalDao()))
        )[GoalViewModel::class.java]
        goalViewModel.setUser(userId)

        categoryViewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(CategoryRepository(db.categoryDao()))
        )[CategoryViewModel::class.java]

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(ExpenseRepository(db.expenseDao()))
        )[ExpenseViewModel::class.java]
        expenseViewModel.setUser(userId)
    }

    private fun setupRecyclerView(view: View) {
        goalAdapter = GoalAdapter()
        view.findViewById<RecyclerView>(R.id.rv_goals).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = goalAdapter
        }
    }

    private fun observeGoals(view: View) {
        val tvEmpty = view.findViewById<TextView>(R.id.tv_empty_goals)
        val rvGoals = view.findViewById<RecyclerView>(R.id.rv_goals)
        val tvTotal = view.findViewById<TextView>(R.id.tv_total_goals)
        val tvOnTrack = view.findViewById<TextView>(R.id.tv_on_track)
        val tvNeedsAttention = view.findViewById<TextView>(R.id.tv_needs_attention)

        // Observe all goals
        goalViewModel.allGoals.observe(viewLifecycleOwner) { goals ->
            val goalsWithSpent = goals.map { goal ->
                GoalWithSpent(goal, goal.name, 0.0)
            }

            goalAdapter.submitList(goalsWithSpent)

            val isEmpty = goalsWithSpent.isEmpty()
            tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
            rvGoals.visibility = if (isEmpty) View.GONE else View.VISIBLE

            // Summary counts
            val minGoalVal = 0.0
            val maxGoalVal = Double.MAX_VALUE
            val onTrack = goalsWithSpent.count {
                val min = it.goal.minGoal ?: minGoalVal
                val max = it.goal.maxGoal ?: maxGoalVal
                it.amountSpent >= min && it.amountSpent <= max
            }
            val needsAttention = goalsWithSpent.count {
                val min = it.goal.minGoal ?: minGoalVal
                val max = it.goal.maxGoal ?: maxGoalVal
                it.amountSpent < min || it.amountSpent > max
            }

            tvTotal.text = goalsWithSpent.size.toString()
            tvOnTrack.text = onTrack.toString()
            tvNeedsAttention.text = needsAttention.toString()
        }
    }

    private fun showAddGoalDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_add_goal, null)
        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_goal_name)
        val etMin = dialogView.findViewById<TextInputEditText>(R.id.et_goal_min)
        val etMax = dialogView.findViewById<TextInputEditText>(R.id.et_goal_max)
        val etTarget = dialogView.findViewById<TextInputEditText>(R.id.et_goal_target)

        AlertDialog.Builder(requireContext())
            .setTitle("Add New Goal")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val name = etName.text.toString().trim()
                val min = etMin.text.toString().toDoubleOrNull()
                val max = etMax.text.toString().toDoubleOrNull()
                val target = etTarget.text.toString().toDoubleOrNull()

                if (name.isEmpty() || target == null) {
                    Toast.makeText(requireContext(), "Please fill name and target amount", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                if (min != null && max != null && min > max) {
                    Toast.makeText(requireContext(), "Minimum cannot exceed Maximum", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }

                goalViewModel.addGoal(
                    name = name,
                    targetAmount = target,
                    minGoal = min,
                    maxGoal = max
                )

                Toast.makeText(requireContext(), "Goal added!", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}