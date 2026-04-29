package com.example.khulisawallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.khulisawallet.data.ExpenseWithCategory
import java.text.SimpleDateFormat
import java.util.*

class ExpenseAdapter : ListAdapter<ExpenseWithCategory, ExpenseAdapter.ExpenseViewHolder>(DiffCallback()) {

    private val dateFormat = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())

    inner class ExpenseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvTitle: TextView = itemView.findViewById(R.id.tv_expense_title)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_expense_category)
        val tvAmount: TextView = itemView.findViewById(R.id.tv_expense_amount)
        val tvDate: TextView = itemView.findViewById(R.id.tv_expense_date)
        val viewCategoryColor: View = itemView.findViewById(R.id.view_category_color)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ExpenseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_expense, parent, false)
        return ExpenseViewHolder(view)
    }

    override fun onBindViewHolder(holder: ExpenseViewHolder, position: Int) {
        val item = getItem(position)
        val expense = item.expense
        val category = item.category

        holder.tvTitle.text = expense.title
        holder.tvCategory.text = category.name
        holder.tvDate.text = dateFormat.format(Date(expense.date))

        // Color the dot with the category color
        try {
            holder.viewCategoryColor.background.setTint(Color.parseColor(category.colorHex))
        } catch (e: Exception) {
            holder.viewCategoryColor.background.setTint(Color.parseColor("#2ECC71"))
        }

        // Show amount with color: green for income, red for expense
        val isIncome = expense.type.name == "INCOME"
        holder.tvAmount.text = if (isIncome) "+ R %.2f".format(expense.amount)
        else "- R %.2f".format(expense.amount)
        holder.tvAmount.setTextColor(
            if (isIncome) Color.parseColor("#2ECC71")
            else Color.parseColor("#E74C3C")
        )
    }

    class DiffCallback : DiffUtil.ItemCallback<ExpenseWithCategory>() {
        override fun areItemsTheSame(old: ExpenseWithCategory, new: ExpenseWithCategory) =
            old.expense.id == new.expense.id
        override fun areContentsTheSame(old: ExpenseWithCategory, new: ExpenseWithCategory) =
            old == new
    }
}