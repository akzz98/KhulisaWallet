package com.example.khulisawallet

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import androidx.navigation.findNavController
import androidx.navigation.ui.setupWithNavController
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.CategoryRepository
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton
import kotlinx.coroutines.launch

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val navView: BottomNavigationView = findViewById(R.id.bottom_nav)
        val navController = findNavController(R.id.nav_host_fragment)

        val db = AppDatabase.getDatabase(this)
        val categoryRepository = CategoryRepository(db.categoryDao())
        lifecycleScope.launch {
            categoryRepository.preloadDefaults()
        }

        navView.setupWithNavController(navController)

        val fab: FloatingActionButton = findViewById(R.id.fab_add_expense)
        fab.setOnClickListener {
            startActivity(Intent(this, AddExpenseActivity::class.java))
        }
    }
}