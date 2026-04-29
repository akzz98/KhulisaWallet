package com.example.khulisawallet

import android.graphics.Color
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.khulisawallet.data.Category

data class CategoryTotal(
    val category: Category,
    val total: Double
)

class CategoryTotalAdapter(
    private var items: List<CategoryTotal> = emptyList()
) : RecyclerView.Adapter<CategoryTotalAdapter.ViewHolder>() {

    inner class ViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val colorDot: View = itemView.findViewById(R.id.view_cat_color)
        val tvName: TextView = itemView.findViewById(R.id.tv_cat_name)
        val tvTotal: TextView = itemView.findViewById(R.id.tv_cat_total)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_category_total, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val item = items[position]
        holder.tvName.text = item.category.name
        holder.tvTotal.text = "R %.2f".format(item.total)
        try {
            holder.colorDot.background.setTint(Color.parseColor(item.category.colorHex))
        } catch (e: Exception) {
            holder.colorDot.background.setTint(Color.parseColor("#2ECC71"))
        }
    }

    override fun getItemCount() = items.size

    fun submitList(newItems: List<CategoryTotal>) {
        items = newItems
        notifyDataSetChanged()
    }
}