package com.example.khulisawallet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.UserRepository
import com.example.khulisawallet.databinding.ActivityLoginBinding
import com.example.khulisawallet.viewmodel.UserViewModel
import com.example.khulisawallet.viewmodel.UserViewModelFactory
import java.security.MessageDigest

class LoginActivity : AppCompatActivity() {

    private lateinit var binding: ActivityLoginBinding

    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            UserRepository(AppDatabase.getDatabase(this).userDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityLoginBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe current user and save to SharedPreferences
        viewModel.currentUser.observe(this) { user ->
            user?.let {
                // After successful login: save user's ID and first name to SharedPreferences
                val prefs = getSharedPreferences("khulisa_prefs", MODE_PRIVATE)
                prefs.edit()
                    .putInt("user_id", user.id)
                    .putString("user_first_name", user.firstName)
                    .apply()

                Toast.makeText(this, "Welcome back!", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, MainActivity::class.java))
                finish()
            }
        }

        // Observe login result
        viewModel.userOpResult.observe(this) { result ->
            result ?: return@observe
            if (result.isFailure) {
                Toast.makeText(this, result.exceptionOrNull()?.message ?: "Login failed", Toast.LENGTH_SHORT).show()
            }
            viewModel.clearResult()
        }

        // Login button
        binding.btnLogin.setOnClickListener {
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            viewModel.login(email, hashPassword(password))
        }

        // Navigate to Sign Up
        binding.tvGoToSignUp.setOnClickListener {
            startActivity(Intent(this, SignUpActivity::class.java))
        }
    }

    // SHA-256 password hashing
    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}