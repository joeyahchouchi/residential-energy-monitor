package com.univ.energymonitor.data.local

import androidx.room.Entity
import androidx.room.PrimaryKey

/**
 * Represents a user account in the local database.
 * Username is the primary key (lowercase, unique).
 *
 * Note: password is currently stored as plaintext.
 * Hashing will be added in Phase 4 — at that point this field
 * will be renamed to passwordHash.
 */
@Entity(tableName = "users")
data class UserEntity(
    @PrimaryKey
    val username: String,          // must be stored lowercase (enforced in UserRepository)
    val password: String,          // plaintext for now; will become passwordHash in Phase 4
    val fullName: String = "",
    val email: String = "",
    val createdAt: Long = System.currentTimeMillis()
)