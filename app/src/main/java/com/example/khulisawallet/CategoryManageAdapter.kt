package com.example.khulisawallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.khulisawallet.data.Category

class CategoryManageAdapter(
    private val onEdit: (Category) -> Unit,
    private val onDelete: (Category) -> Unit
) : ListAdapter<Category, CategoryManageAdapter.ViewHolder>(DiffCallback()) {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorDot: View = itemView.findViewById(R.id.view_color_dot)
        val tvName: TextView = itemView.findViewById(R.id.tv_category_name)
        val tvType: TextView = itemView.findViewById(R.id.tv_category_type)
        val tvDefault: TextView = itemView.findViewById(R.id.tv_default_badge)
        val btnEdit: ImageButton = itemView.findViewById(R.id.btn_edit_category)
        val btnDelete: ImageButton = itemView.findViewById(R.id.btn_delete_category)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_manage, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val category = getItem(position)

        holder.tvName.text = category.name
        holder.tvType.text = category.type.name.lowercase().replaceFirstChar { it.uppercase() }

        try {
            holder.colorDot.background.setTint(Color.parseColor(category.colorHex))
        } catch (e: Exception) {
            holder.colorDot.background.setTint(Color.GRAY)
        }

        holder.tvDefault.visibility = if (category.isDefault) View.VISIBLE else View.GONE

        holder.btnEdit.setOnClickListener { onEdit(category) }
        holder.btnDelete.setOnClickListener { onDelete(category) }
    }

    class DiffCallback : DiffUtil.ItemCallback<Category>() {
        override fun areItemsTheSame(oldItem: Category, newItem: Category) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: Category, newItem: Category) = oldItem == newItem
    }
}
