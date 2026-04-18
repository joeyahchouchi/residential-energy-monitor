package com.univ.energymonitor.data.local

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `users` table.
 * All suspend functions run off the main thread automatically (via Room + coroutines).
 */
@Dao
interface UserDao {

    /**
     * Insert a new user. Fails if username already exists (ABORT strategy).
     * Use this for account creation so duplicates are caught as errors.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(user: UserEntity)

    /**
     * Look up a user by username (exact match).
     * Returns null if not found.
     */
    @Query("SELECT * FROM users WHERE username = :username LIMIT 1")
    suspend fun findByUsername(username: String): UserEntity?

    /**
     * Returns true if a user with this username exists.
     * Used to check uniqueness during account creation.
     */
    @Query("SELECT EXISTS(SELECT 1 FROM users WHERE username = :username)")
    suspend fun exists(username: String): Boolean

    /**
     * Reactive stream of all usernames (for debugging / admin views).
     */
    @Query("SELECT username FROM users ORDER BY createdAt DESC")
    fun observeAllUsernames(): Flow<List<String>>

    /**
     * Count total registered users.
     */
    @Query("SELECT COUNT(*) FROM users")
    suspend fun count(): Int
}