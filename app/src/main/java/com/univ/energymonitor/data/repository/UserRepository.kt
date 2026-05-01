package com.univ.energymonitor.data.repository

import com.univ.energymonitor.data.local.UserDao
import com.univ.energymonitor.data.local.UserEntity

/**
 * Single source of truth for user account operations.
 * Normalizes usernames to lowercase before any DB operation.
 */
class UserRepository(private val dao: UserDao) {

    /**
     * Create a new user account.
     * Returns true if successful, false if username already taken.
     */
    suspend fun createUser(
        username: String,
        password: String,
        fullName: String = "",
        email: String = ""
    ): Boolean {
        val normalized = username.trim().lowercase()
        if (dao.exists(normalized)) return false
        dao.insert(
            UserEntity(
                username = normalized,
                password = password,
                fullName = fullName.trim(),
                email = email.trim().lowercase()
            )
        )
        return true
    }

    /**
     * Attempt login. Returns the UserEntity if credentials match, null otherwise.
     */
    suspend fun login(username: String, password: String): UserEntity? {
        val normalized = username.trim().lowercase()
        val user = dao.findByUsername(normalized)
        return if (user != null && user.password == password) user else null
    }

    /**
     * Check if a username is already taken.
     */
    suspend fun isUsernameTaken(username: String): Boolean {
        return dao.exists(username.trim().lowercase())
    }

    /**
     * Seed default users if the database is empty.
     * Called once on first app launch.
     */
    suspend fun seedDefaultUsers() {
        if (dao.count() == 0) {
            val defaults = listOf(
                UserEntity(username = "admin", password = "admin123", fullName = "Administrator"),
                UserEntity(username = "engineer", password = "leb2024", fullName = "Engineer"),
                UserEntity(username = "surveyor", password = "survey1", fullName = "Surveyor")
            )
            defaults.forEach { user ->
                try {
                    dao.insert(user)
                } catch (_: Exception) {
                    // Ignore if already exists
                }
            }
        }
    }
}