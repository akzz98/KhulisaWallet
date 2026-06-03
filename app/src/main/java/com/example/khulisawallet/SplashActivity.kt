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
import com.example.khulisawallet.utils.applySystemBarInsets
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
        applySystemBarInsets()

        // Preload default categories on first launch, show splash for 2.5 seconds, then route
        lifecycleScope.launch {
            val sharedPref = getSharedPreferences("khulisa_prefs", Context.MODE_PRIVATE)
            val userId = sharedPref.getInt("user_id", -1)
            val db = AppDatabase.getDatabase(this@SplashActivity)
            val userExists = userId != -1 && db.userDao().getUserById(userId) != null

            if (userId != -1 && !userExists) {
                sharedPref.edit().clear().apply()
            }

            val categoryRepository = CategoryRepository(db.categoryDao())
            val syncUserId = if (userExists) userId else null
            val preload = async { categoryRepository.preloadDefaults(syncUserId) }
            delay(2500L)
            preload.await()

            if (userExists) {
                startActivity(Intent(this@SplashActivity, MainActivity::class.java))
            } else {
                startActivity(Intent(this@SplashActivity, LoginActivity::class.java))
            }
            finish()
        }
    }
}