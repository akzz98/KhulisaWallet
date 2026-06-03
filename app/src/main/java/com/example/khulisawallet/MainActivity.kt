package com.example.khulisawallet

import android.view.View
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.NavController
import androidx.navigation.findNavController
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupWithNavController
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.CategoryRepository
import com.example.khulisawallet.utils.applySystemBarInsets
import com.example.khulisawallet.utils.StreakManager
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    private var streakDialogShown = false

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)
        applySystemBarInsets()

        streakDialogShown = savedInstanceState?.getBoolean(KEY_STREAK_DIALOG_SHOWN) == true

        val navView: BottomNavigationView = findViewById(R.id.bottom_nav)
        val navController = findNavController(R.id.nav_host_fragment)

        val db = AppDatabase.getDatabase(this)
        val categoryRepository = CategoryRepository(db.categoryDao())
        val userId = getSharedPreferences("khulisa_prefs", MODE_PRIVATE).getInt("user_id", -1)
        lifecycleScope.launch {
            categoryRepository.preloadDefaults(userId.takeIf { it > 0 })
        }

        navView.setupWithNavController(navController)

        val fab: FloatingActionButton = findViewById(R.id.fab_add_expense)
        setupFab(navController, fab)

        if (!streakDialogShown) {
            showStreakWelcomeDialog(userId)
        }
    }

    override fun onSaveInstanceState(outState: Bundle) {
        super.onSaveInstanceState(outState)
        outState.putBoolean(KEY_STREAK_DIALOG_SHOWN, streakDialogShown)
    }

    private fun setupFab(navController: NavController, fab: FloatingActionButton) {
        navController.addOnDestinationChangedListener { _, destination, _ ->
            configureFab(fab, destination.id)
        }
        configureFab(fab, navController.currentDestination?.id ?: R.id.navigation_home)
    }

    private fun configureFab(fab: FloatingActionButton, destinationId: Int) {
        when (destinationId) {
            R.id.navigation_profile -> {
                fab.visibility = View.GONE
            }
            R.id.navigation_goals -> {
                fab.visibility = View.VISIBLE
                fab.contentDescription = "Add Goal"
                fab.setOnClickListener {
                    val navHost = supportFragmentManager
                        .findFragmentById(R.id.nav_host_fragment) as? NavHostFragment
                    (navHost?.childFragmentManager?.primaryNavigationFragment as? GoalsFragment)
                        ?.openAddGoalDialog()
                }
            }
            else -> {
                fab.visibility = View.VISIBLE
                fab.contentDescription = "Add Transaction"
                fab.setOnClickListener {
                    startActivity(Intent(this, AddExpenseActivity::class.java))
                }
            }
        }
    }

    private fun showStreakWelcomeDialog(userId: Int) {
        if (userId <= 0) return

        lifecycleScope.launch {
            val user = AppDatabase.getDatabase(this@MainActivity).userDao().getUserById(userId)
                ?: return@launch
            if (isFinishing || streakDialogShown) return@launch

            streakDialogShown = true
            val longestLabel = if (user.longestStreak == 1) "day" else "days"
            AlertDialog.Builder(this@MainActivity)
                .setTitle("🔥 Khulisa Growth Streak")
                .setMessage(
                    "${StreakManager.formatStreakCount(user.currentStreak)}\n\n" +
                        "${StreakManager.formatRankLabel(user.currentStreak)}\n\n" +
                        "Longest streak: ${user.longestStreak} $longestLabel"
                )
                .setPositiveButton("Let's go!", null)
                .show()
        }
    }

    companion object {
        private const val KEY_STREAK_DIALOG_SHOWN = "streak_dialog_shown"
    }
}