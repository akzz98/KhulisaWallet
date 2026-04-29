package com.example.khulisawallet.viewmodel

import androidx.lifecycle.*
import com.example.khulisawallet.data.User
import com.example.khulisawallet.data.UserRepository
import kotlinx.coroutines.launch

class UserViewModel(private val repository: UserRepository) : ViewModel() {

    // LiveData to observe all active users
    val allActiveUsers: LiveData<List<User>> = repository.getAllActiveUsers()

    // State for Login/Registration results (nullable to allow clearing)
    private val _userOpResult = MutableLiveData<Result<Any>?>()
    val userOpResult: LiveData<Result<Any>?> = _userOpResult

    // State for the currently logged-in user
    private val _currentUser = MutableLiveData<User?>()
    val currentUser: LiveData<User?> = _currentUser

    /**
     * Register a new user
     */
    fun registerUser(firstName: String, lastName: String, email: String, passwordHash: String) {
        viewModelScope.launch {
            val newUser = User(
                firstName = firstName,
                lastName = lastName,
                email = email,
                passwordHash = passwordHash
            )
            val result = repository.registerUser(newUser)
            _userOpResult.postValue(result)
        }
    }

    /**
     * Login user
     */
    fun login(email: String, passwordHash: String) {
        viewModelScope.launch {
            val result = repository.login(email, passwordHash)
            if (result.isSuccess) {
                _currentUser.postValue(result.getOrNull())
            }
            _userOpResult.postValue(result as Result<Any>)
        }
    }

    /**
     * Update user profile
     */
    fun updateUser(user: User) {
        viewModelScope.launch {
            repository.updateUser(user)
        }
    }

    // Clear the operation result after the UI has handled it (to prevent re-triggering)
    fun clearResult() {
        _userOpResult.value = null
    }
}