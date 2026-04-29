package com.example.khulisawallet

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.ExpenseRepository
import com.example.khulisawallet.data.CategoryRepository
import com.example.khulisawallet.data.GoalRepository
import com.example.khulisawallet.data.UserRepository
import com.example.khulisawallet.viewmodel.*
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import java.text.SimpleDateFormat
import java.util.*

class ProfileFragment : Fragment(R.layout.fragment_profile) {

    private lateinit var userViewModel: UserViewModel
    private lateinit var expenseViewModel: ExpenseViewModel
    private lateinit var goalViewModel: GoalViewModel
    private lateinit var categoryViewModel: CategoryViewModel

    private var userId: Int = -1

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        val prefs = requireContext().getSharedPreferences("khulisa_prefs", 0)
        userId = prefs.getInt("user_id", -1)
        if (userId == -1) return

        setupViewModels()
        loadUserInfo(view)
        loadStats(view)
        setupButtons(view)
    }

    private fun setupViewModels() {
        val db = AppDatabase.getDatabase(requireContext())

        userViewModel = ViewModelProvider(
            this,
            UserViewModelFactory(UserRepository(db.userDao()))
        )[UserViewModel::class.java]

        expenseViewModel = ViewModelProvider(
            this,
            ExpenseViewModelFactory(ExpenseRepository(db.expenseDao()))
        )[ExpenseViewModel::class.java]
        expenseViewModel.setUser(userId)

        goalViewModel = ViewModelProvider(
            this,
            GoalViewModelFactory(GoalRepository(db.goalDao()))
        )[GoalViewModel::class.java]
        goalViewModel.setUser(userId)

        categoryViewModel = ViewModelProvider(
            this,
            CategoryViewModelFactory(CategoryRepository(db.categoryDao()))
        )[CategoryViewModel::class.java]
        categoryViewModel.setUser(userId)
    }

    private fun loadUserInfo(view: View) {
        userViewModel.getUserById(userId).observe(viewLifecycleOwner) { user ->
            user ?: return@observe

            val fullName = "${user.firstName} ${user.lastName}"
            val initials = "${user.firstName.firstOrNull() ?: ""}${user.lastName.firstOrNull() ?: ""}"
                .uppercase()

            view.findViewById<TextView>(R.id.tv_avatar).text = initials
            view.findViewById<TextView>(R.id.tv_profile_name).text = fullName
            view.findViewById<TextView>(R.id.tv_profile_email).text = user.email
            view.findViewById<TextView>(R.id.tv_first_name).text = user.firstName
            view.findViewById<TextView>(R.id.tv_last_name).text = user.lastName
            view.findViewById<TextView>(R.id.tv_email).text = user.email

            val dateStr = SimpleDateFormat("dd MMM yyyy", Locale.getDefault())
                .format(Date(user.createdAt))
            view.findViewById<TextView>(R.id.tv_member_since).text = dateStr
        }
    }

    private fun loadStats(view: View) {
        expenseViewModel.allExpenses.observe(viewLifecycleOwner) { expenses ->
            view.findViewById<TextView>(R.id.tv_stat_transactions).text =
                expenses.size.toString()
        }

        goalViewModel.allGoals.observe(viewLifecycleOwner) { goals ->
            view.findViewById<TextView>(R.id.tv_stat_goals).text =
                goals.size.toString()
        }

        categoryViewModel.allCategories.observe(viewLifecycleOwner) { cats ->
            view.findViewById<TextView>(R.id.tv_stat_categories).text =
                cats.size.toString()
        }
    }

    private fun setupButtons(view: View) {

        // Manage Categories — placeholder toast for now
        view.findViewById<LinearLayout>(R.id.btn_manage_categories).setOnClickListener {
            Toast.makeText(requireContext(), "Manage Categories coming soon!", Toast.LENGTH_SHORT).show()
        }

        // Change Password Dialog
        view.findViewById<LinearLayout>(R.id.btn_change_password).setOnClickListener {
            showChangePasswordDialog()
        }

        // Logout
        view.findViewById<MaterialButton>(R.id.btn_logout).setOnClickListener {
            showLogoutConfirmation()
        }
    }

    private fun showChangePasswordDialog() {
        val dialogView = layoutInflater.inflate(R.layout.dialog_change_password, null)

        AlertDialog.Builder(requireContext())
            .setTitle("Change Password")
            .setView(dialogView)
            .setPositiveButton("Update") { _, _ ->
                val current = dialogView.findViewById<TextInputEditText>(R.id.et_current_password)
                    .text.toString()
                val newPass = dialogView.findViewById<TextInputEditText>(R.id.et_new_password)
                    .text.toString()
                val confirm = dialogView.findViewById<TextInputEditText>(R.id.et_confirm_password)
                    .text.toString()

                when {
                    current.isEmpty() || newPass.isEmpty() || confirm.isEmpty() ->
                        Toast.makeText(requireContext(), "Please fill all fields", Toast.LENGTH_SHORT).show()
                    newPass != confirm ->
                        Toast.makeText(requireContext(), "Passwords do not match", Toast.LENGTH_SHORT).show()
                    newPass.length < 6 ->
                        Toast.makeText(requireContext(), "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                    else -> {
                        userViewModel.changePassword(userId, current, newPass)
                        userViewModel.userOpResult.observe(viewLifecycleOwner) { result ->
                            result?.let {
                                if (it.isSuccess) {
                                    Toast.makeText(requireContext(), "Password updated!", Toast.LENGTH_SHORT).show()
                                } else {
                                    Toast.makeText(requireContext(), "Current password is incorrect", Toast.LENGTH_SHORT).show()
                                }
                                userViewModel.clearResult()
                            }
                        }
                    }
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showLogoutConfirmation() {
        AlertDialog.Builder(requireContext())
            .setTitle("Log Out")
            .setMessage("Are you sure you want to log out?")
            .setPositiveButton("Log Out") { _, _ ->
                // Clear SharedPreferences
                requireContext()
                    .getSharedPreferences("khulisa_prefs", 0)
                    .edit().clear().apply()

                // Navigate back to Login
                val intent = Intent(requireContext(), LoginActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                startActivity(intent)
            }
            .setNegativeButton("Cancel", null)
            .show()
    }
}