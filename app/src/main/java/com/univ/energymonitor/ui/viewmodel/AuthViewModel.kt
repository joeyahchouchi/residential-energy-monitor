package com.univ.energymonitor.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.univ.energymonitor.EnergyMonitorApp
import com.univ.energymonitor.data.repository.UserRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Handles authentication: login, account creation, session state.
 * Uses Room via UserRepository instead of in-memory Map.
 */
class AuthViewModel(application: Application) : AndroidViewModel(application) {

    private val userRepository: UserRepository =
        (application as EnergyMonitorApp).container.userRepository

    // Current logged-in username (empty = not logged in)
    private val _loggedUser = MutableStateFlow("")
    val loggedUser: StateFlow<String> = _loggedUser

    // Login result feedback
    private val _loginError = MutableStateFlow("")
    val loginError: StateFlow<String> = _loginError

    // Account creation result
    private val _createAccountResult = MutableStateFlow<Boolean?>(null)
    val createAccountResult: StateFlow<Boolean?> = _createAccountResult

    init {
        // Seed default users on first launch
        viewModelScope.launch {
            userRepository.seedDefaultUsers()
        }
    }

    /**
     * Attempt login with username and password.
     * Updates loggedUser on success, loginError on failure.
     */
    fun login(username: String, password: String) {
        _loginError.value = ""
        viewModelScope.launch {
            val user = userRepository.login(username, password)
            if (user != null) {
                _loggedUser.value = user.username
            } else {
                _loginError.value = "Invalid username or password"
            }
        }
    }

    /**
     * Create a new account.
     * Updates createAccountResult: true = success, false = username taken.
     */
    fun createAccount(
        username: String,
        password: String,
        fullName: String = "",
        email: String = ""
    ) {
        viewModelScope.launch {
            val success = userRepository.createUser(
                username = username,
                password = password,
                fullName = fullName,
                email = email
            )
            _createAccountResult.value = success
        }
    }

    /**
     * Reset create account result (after showing feedback to user).
     */
    fun resetCreateAccountResult() {
        _createAccountResult.value = null
    }

    /**
     * Log out the current user.
     */
    fun logout() {
        _loggedUser.value = ""
    }

    /**
     * Check if a username is already taken (for real-time validation).
     */
    suspend fun isUsernameTaken(username: String): Boolean {
        return userRepository.isUsernameTaken(username)
    }
}