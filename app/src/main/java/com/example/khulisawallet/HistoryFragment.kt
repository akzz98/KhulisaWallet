package com.example.khulisawallet

import android.app.DatePickerDialog
import android.os.Bundle
import android.view.View
import android.widget.TextView
import androidx.cardview.widget.CardView
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.ExpenseRepository
import com.example.khulisawallet.data.CategoryRepository
import com.example.khulisawallet.viewmodel.ExpenseViewModel
import com.example.khulisawallet.viewmodel.ExpenseViewModelFactory
import com.example.khulisawallet.viewmodel.CategoryViewModel
import com.example.khulisawallet.viewmodel.CategoryViewModelFactory
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class HistoryFragment : Fragment(R.layout.fragment_history) {

    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var historyAdapter: ExpenseAdapter
    private lateinit var categoryTotalAdapter: CategoryTotalAdapter

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    // Default: current month
    private var fromDate: Long = getStartOfMonth()
    private var toDate: Long = System.currentTimeMillis()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("khulisa_prefs", 0)
        val userId = prefs.getInt("user_id", -1)
        if (userId == -1) return

        setupViewModels(userId)
        setupDateFields(view)
        setupRecyclerViews(view)
        setupFilterButton(view)

        // Load default (current month)
        applyFilter(view)
    }

    private fun setupViewModels(userId: Int) {
        val db = AppDatabase.getDatabase(requireContext())

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(ExpenseRepository(db.expenseDao()))
        )[ExpenseViewModel::class.java]
        expenseViewModel.setUser(userId)

        categoryViewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(CategoryRepository(db.categoryDao()))
        )[CategoryViewModel::class.java]
    }

    private fun setupDateFields(view: View) {
        val etFrom = view.findViewById<TextInputEditText>(R.id.et_date_from)
        val etTo = view.findViewById<TextInputEditText>(R.id.et_date_to)

        etFrom.setText(dateFormat.format(Date(fromDate)))
        etTo.setText(dateFormat.format(Date(toDate)))

        etFrom.setOnClickListener {
            showDatePicker(fromDate) { selected ->
                fromDate = selected
                etFrom.setText(dateFormat.format(Date(fromDate)))
            }
        }

        etTo.setOnClickListener {
            showDatePicker(toDate) { selected ->
                toDate = selected
                etTo.setText(dateFormat.format(Date(toDate)))
            }
        }
    }

    private fun setupRecyclerViews(view: View) {
        historyAdapter = ExpenseAdapter()
        view.findViewById<RecyclerView>(R.id.rv_history).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = historyAdapter
        }

        categoryTotalAdapter = CategoryTotalAdapter()
        view.findViewById<RecyclerView>(R.id.rv_category_totals).apply {
            layoutManager = LinearLayoutManager(requireContext())
            adapter = categoryTotalAdapter
        }
    }

    private fun setupFilterButton(view: View) {
        view.findViewById<MaterialButton>(R.id.btn_filter).setOnClickListener {
            applyFilter(view)
        }
    }

    private fun applyFilter(view: View) {
        val tvEmpty = view.findViewById<TextView>(R.id.tv_empty_history)
        val rvHistory = view.findViewById<RecyclerView>(R.id.rv_history)
        val cardTotals = view.findViewById<CardView>(R.id.card_totals)
        val tvPeriodIncome = view.findViewById<TextView>(R.id.tv_period_income)
        val tvPeriodExpense = view.findViewById<TextView>(R.id.tv_period_expense)

        // Set toDate to end of selected day
        val endOfDay = Calendar.getInstance().apply {
            timeInMillis = toDate
            set(Calendar.HOUR_OF_DAY, 23)
            set(Calendar.MINUTE, 59)
            set(Calendar.SECOND, 59)
        }.timeInMillis

        expenseViewModel.getExpensesByDateRange(fromDate, endOfDay)
            .observe(viewLifecycleOwner) { expenses ->

                historyAdapter.submitList(expenses)

                val isEmpty = expenses.isEmpty()
                tvEmpty.visibility = if (isEmpty) View.VISIBLE else View.GONE
                rvHistory.visibility = if (isEmpty) View.GONE else View.VISIBLE
                cardTotals.visibility = if (isEmpty) View.GONE else View.VISIBLE

                if (!isEmpty) {
                    // Period income/expense totals
                    val income = expenses.filter { it.expense.type.name == "INCOME" }
                        .sumOf { it.expense.amount }
                    val expense = expenses.filter { it.expense.type.name == "EXPENSE" }
                        .sumOf { it.expense.amount }

                    tvPeriodIncome.text = "Income: R %.2f".format(income)
                    tvPeriodExpense.text = "Expenses: R %.2f".format(expense)

                    // Category totals
                    categoryViewModel.allActiveCategories.observe(viewLifecycleOwner) { cats ->
                        val totals = cats.mapNotNull { cat ->
                            val total = expenses
                                .filter { it.expense.categoryId == cat.id }
                                .sumOf { it.expense.amount }
                            if (total > 0) CategoryTotal(cat, total) else null
                        }.sortedByDescending { it.total }

                        categoryTotalAdapter.submitList(totals)
                    }
                }
            }
    }

    private fun showDatePicker(current: Long, onSelected: (Long) -> Unit) {
        val cal = Calendar.getInstance().apply { timeInMillis = current }
        DatePickerDialog(
            requireContext(),
            { _, year, month, day ->
                val selected = Calendar.getInstance().apply {
                    set(year, month, day, 0, 0, 0)
                    set(Calendar.MILLISECOND, 0)
                }.timeInMillis
                onSelected(selected)
            },
            cal.get(Calendar.YEAR),
            cal.get(Calendar.MONTH),
            cal.get(Calendar.DAY_OF_MONTH)
        ).show()
    }

    private fun getStartOfMonth(): Long {
        return Calendar.getInstance().apply {
            set(Calendar.DAY_OF_MONTH, 1)
            set(Calendar.HOUR_OF_DAY, 0)
            set(Calendar.MINUTE, 0)
            set(Calendar.SECOND, 0)
            set(Calendar.MILLISECOND, 0)
        }.timeInMillis
    }
}