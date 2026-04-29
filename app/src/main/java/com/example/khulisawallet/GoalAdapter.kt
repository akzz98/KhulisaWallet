package com.example.khulisawallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.khulisawallet.data.Goal

class GoalAdapter : ListAdapter<GoalWithSpent, GoalAdapter.GoalViewHolder>(DiffCallback()) {

    inner class GoalViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val tvName: TextView = itemView.findViewById(R.id.tv_goal_name)
        val tvCategory: TextView = itemView.findViewById(R.id.tv_goal_category)
        val tvStatus: TextView = itemView.findViewById(R.id.tv_goal_status)
        val progressBar: ProgressBar = itemView.findViewById(R.id.progress_goal)
        val tvMin: TextView = itemView.findViewById(R.id.tv_goal_min)
        val tvCurrent: TextView = itemView.findViewById(R.id.tv_goal_current)
        val tvMax: TextView = itemView.findViewById(R.id.tv_goal_max)
        val tvAlert: TextView = itemView.findViewById(R.id.tv_goal_alert)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): GoalViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_goal, parent, false)
        return GoalViewHolder(view)
    }

    override fun onBindViewHolder(holder: GoalViewHolder, position: Int) {
        val item = getItem(position)
        val goal = item.goal
        val spent = item.amountSpent

        holder.tvName.text = goal.name
        holder.tvCategory.text = item.categoryName
        holder.tvMin.text = "Min: R %.2f".format(goal.minGoal ?: 0.0)
        holder.tvMax.text = "Max: R %.2f".format(goal.maxGoal ?: 0.0)
        holder.tvCurrent.text = "R %.2f spent".format(spent)

        // Progress: spent vs target
        val target = goal.targetAmount
        val progress = if (target > 0) ((spent / target) * 100).toInt().coerceIn(0, 100) else 0
        holder.progressBar.progress = progress

        // Status logic
        val minGoal = goal.minGoal ?: 0.0
        val maxGoal = goal.maxGoal ?: Double.MAX_VALUE
        when {
            spent < minGoal -> {
                // Below minimum — warn
                holder.tvStatus.text = "Below Min"
                holder.tvStatus.background.setTint(Color.parseColor("#F39C12"))
                holder.progressBar.progressTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#F39C12"))
                holder.tvAlert.visibility = View.VISIBLE
                holder.tvAlert.text = "⚠ Spending is below your minimum target of R %.2f".format(minGoal)
            }
            spent > maxGoal -> {
                // Exceeded maximum — danger
                holder.tvStatus.text = "Exceeded"
                holder.tvStatus.background.setTint(Color.parseColor("#E74C3C"))
                holder.progressBar.progressTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#E74C3C"))
                holder.tvAlert.visibility = View.VISIBLE
                holder.tvAlert.text = "🚨 Spending exceeded your maximum limit of R %.2f".format(maxGoal)
            }
            else -> {
                // On track
                holder.tvStatus.text = "On Track ✓"
                holder.tvStatus.background.setTint(Color.parseColor("#2ECC71"))
                holder.progressBar.progressTintList =
                    android.content.res.ColorStateList.valueOf(Color.parseColor("#2ECC71"))
                holder.tvAlert.visibility = View.GONE
            }
        }
    }

    class DiffCallback : DiffUtil.ItemCallback<GoalWithSpent>() {
        override fun areItemsTheSame(old: GoalWithSpent, new: GoalWithSpent) =
            old.goal.id == new.goal.id
        override fun areContentsTheSame(old: GoalWithSpent, new: GoalWithSpent) =
            old == new
    }
}

data class GoalWithSpent(
    val goal: Goal,
    val categoryName: String,
    val amountSpent: Double
)