package com.example.khulisawallet

import android.content.Intent
import android.os.Bundle
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.AppCompatActivity
import com.example.khulisawallet.data.AppDatabase
import com.example.khulisawallet.data.UserRepository
import com.example.khulisawallet.databinding.ActivitySignupBinding
import com.example.khulisawallet.viewmodel.UserViewModel
import com.example.khulisawallet.viewmodel.UserViewModelFactory
import java.security.MessageDigest

class SignUpActivity : AppCompatActivity() {

    private lateinit var binding: ActivitySignupBinding

    private val viewModel: UserViewModel by viewModels {
        UserViewModelFactory(
            UserRepository(AppDatabase.getDatabase(this).userDao())
        )
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivitySignupBinding.inflate(layoutInflater)
        setContentView(binding.root)

        // Observe registration result
        viewModel.userOpResult.observe(this) { result ->
            result ?: return@observe
            if (result.isSuccess) {
                Toast.makeText(this, "Account created! Please login.", Toast.LENGTH_SHORT).show()
                startActivity(Intent(this, LoginActivity::class.java))
                finish()
            } else {
                val msg = result.exceptionOrNull()?.message ?: "Registration failed"
                val friendlyMsg = if (msg.contains("UNIQUE")) "Email already registered" else msg
                Toast.makeText(this, friendlyMsg, Toast.LENGTH_SHORT).show()
            }
            viewModel.clearResult()
        }

        // Sign Up button
        binding.btnSignUp.setOnClickListener {
            val firstName = binding.etFirstName.text.toString().trim()
            val lastName = binding.etLastName.text.toString().trim()
            val email = binding.etEmail.text.toString().trim()
            val password = binding.etPassword.text.toString()
            val confirmPassword = binding.etConfirmPassword.text.toString()

            // Validation
            when {
                firstName.isEmpty() || lastName.isEmpty() || email.isEmpty() || password.isEmpty() ->
                    Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
                password != confirmPassword ->
                    Toast.makeText(this, "Passwords do not match", Toast.LENGTH_SHORT).show()
                password.length < 6 ->
                    Toast.makeText(this, "Password must be at least 6 characters", Toast.LENGTH_SHORT).show()
                else -> viewModel.registerUser(firstName, lastName, email, hashPassword(password))
            }
        }

        // Back to Login
        binding.tvGoToLogin.setOnClickListener {
            finish()
        }
    }

    private fun hashPassword(password: String): String {
        val digest = MessageDigest.getInstance("SHA-256")
        val hashBytes = digest.digest(password.toByteArray())
        return hashBytes.joinToString("") { "%02x".format(it) }
    }
}