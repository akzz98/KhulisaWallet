package com.example.khulisawallet

import android.graphics.Color
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.*
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.khulisawallet.data.*
import com.example.khulisawallet.utils.applySystemBarInsets
import com.example.khulisawallet.viewmodel.*
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.android.material.textfield.TextInputEditText

class ManageCategoriesActivity : AppCompatActivity() {

    private lateinit var categoryViewModel: CategoryViewModel
    private lateinit var adapter: CategoryManageAdapter
    private var userId: Int = -1

    private val colorOptions = listOf(
        "#FF6B6B", "#4ECDC4", "#45B7D1", "#96CEB4",
        "#FFEAA7", "#DDA0DD", "#98D8C8", "#F39C12",
        "#2ECC71", "#9B59B6", "#E91E63", "#607D8B"
    )
    private var selectedColor = colorOptions[0]

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_manage_categories)
        applySystemBarInsets()

        val prefs = getSharedPreferences("khulisa_prefs", 0)
        userId = prefs.getInt("user_id", -1)
        if (userId == -1) { finish(); return }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbar)
        setSupportActionBar(toolbar)
        toolbar.setNavigationOnClickListener { finish() }

        val db = AppDatabase.getDatabase(this)
        categoryViewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(CategoryRepository(db.categoryDao()))
        )[CategoryViewModel::class.java]
        categoryViewModel.setUser(userId)

        adapter = CategoryManageAdapter(
            onEdit = { category -> showAddEditDialog(category) },
            onDelete = { category -> confirmDelete(category) }
        )
        val rv = findViewById<RecyclerView>(R.id.rv_categories)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val tvEmpty = findViewById<TextView>(R.id.tv_empty_categories)

        categoryViewModel.allCategories.observe(this) { categories ->
            adapter.submitList(categories)
            tvEmpty.visibility = if (categories.isEmpty()) View.VISIBLE else View.GONE
        }

        findViewById<FloatingActionButton>(R.id.fab_add_category).setOnClickListener {
            showAddEditDialog(null)
        }
    }

    private fun showAddEditDialog(existing: Category?) {
        val dialogView = LayoutInflater.from(this)
            .inflate(R.layout.dialog_add_edit_category, null)

        val etName = dialogView.findViewById<TextInputEditText>(R.id.et_category_name)
        val rbExpense = dialogView.findViewById<RadioButton>(R.id.rb_expense)
        val rbIncome = dialogView.findViewById<RadioButton>(R.id.rb_income)
        val llColors = dialogView.findViewById<LinearLayout>(R.id.ll_color_picker)

        if (existing != null) {
            etName.setText(existing.name)
            selectedColor = existing.colorHex
            if (existing.type == CategoryType.INCOME) rbIncome.isChecked = true
            else rbExpense.isChecked = true
        } else {
            rbExpense.isChecked = true
            selectedColor = colorOptions[0]
        }

        buildColorPicker(llColors, existing?.colorHex)

        val title = if (existing == null) "Add Category" else "Edit Category"
        AlertDialog.Builder(this)
            .setTitle(title)
            .setView(dialogView)
            .setPositiveButton(if (existing == null) "Add" else "Save") { _, _ ->
                val name = etName.text.toString().trim()
                if (name.isEmpty()) {
                    Toast.makeText(this, "Please enter a category name", Toast.LENGTH_SHORT).show()
                    return@setPositiveButton
                }
                val type = if (rbIncome.isChecked) CategoryType.INCOME else CategoryType.EXPENSE

                if (existing == null) {
                    categoryViewModel.addCategory(
                        name = name,
                        iconName = "ic_other",
                        colorHex = selectedColor,
                        type = type
                    )
                    Toast.makeText(this, "Category added!", Toast.LENGTH_SHORT).show()
                } else {
                    categoryViewModel.updateCategoryDetails(
                        id = existing.id,
                        name = name,
                        iconName = existing.iconName,
                        colorHex = selectedColor
                    )
                    Toast.makeText(this, "Category updated!", Toast.LENGTH_SHORT).show()
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun buildColorPicker(container: LinearLayout, preSelected: String?) {
        container.removeAllViews()
        val size = resources.getDimensionPixelSize(android.R.dimen.app_icon_size) / 2
        val margin = 8

        colorOptions.forEach { hex ->
            val circle = View(this).apply {
                layoutParams = LinearLayout.LayoutParams(size, size).also {
                    it.setMargins(margin, margin, margin, margin)
                }
                background = resources.getDrawable(R.drawable.circle_shape, null)
                background.setTint(Color.parseColor(hex))
                alpha = if (hex == (preSelected ?: colorOptions[0])) 1f else 0.4f
                setOnClickListener {
                    selectedColor = hex
                    for (i in 0 until container.childCount) {
                        container.getChildAt(i).alpha = 0.4f
                    }
                    alpha = 1f
                }
            }
            container.addView(circle)
        }
    }

    private fun confirmDelete(category: Category) {
        val message = if (category.isDefault)
            "\"${category.name}\" is a default category. It will be hidden but not permanently deleted. Continue?"
        else
            "Delete \"${category.name}\"? This cannot be undone."

        AlertDialog.Builder(this)
            .setTitle("Delete Category")
            .setMessage(message)
            .setPositiveButton("Delete") { _, _ ->
                if (category.isDefault) {
                    categoryViewModel.softDeleteCategory(category.id)
                } else {
                    categoryViewModel.deleteCategory(category)
                }
                Toast.makeText(this, "\"${category.name}\" removed", Toast.LENGTH_SHORT).show()
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}
