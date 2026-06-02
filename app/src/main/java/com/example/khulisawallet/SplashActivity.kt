package com.example.khulisawallet

import android.annotation.SuppressLint
import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.lifecycleScope
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.CategoryRepository
import com.example.khulisawallet.databinding.ActivitySplashBinding
import kotlinx.coroutines.async
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

@SuppressLint("CustomSplashScreen")
class SplashActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySplashBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySplashBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Preload default categories on first launch, show splash for 2.5 seconds, then route
        lifecycleScope.launch {
            val categoryRepository = CategoryRepository(
                AppDatabase.getDatabase(this@SplashActivity).categoryDao()
            )
            val preload = async { categoryRepository.preloadDefaults() }
            delay(2500L)
            preload.await()

            val sharedPref = getSharedPreferences("khulisa_prefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", -1)
            val db = AppDatabase.getDatabase(this@SplashActivity)
            val userExists = userId != -1 && db.userDao().getUserById(userId) != null

            // Prefs can outlive a destructive DB migration — clear stale session
            if (userId != -1 && !userExists) {
                sharedPref.edit().clear().apply()
            }

            if (userExists) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}