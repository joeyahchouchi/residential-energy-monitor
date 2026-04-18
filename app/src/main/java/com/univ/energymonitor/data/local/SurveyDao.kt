package com.univ.energymonitor.data.local

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import kotlinx.coroutines.flow.Flow

/**
 * Data Access Object for the `surveys` table.
 *
 * Surveys are stored with their full SurveyData and EnergyReport serialized as JSON.
 * Quick-access KPI fields (totalYearlyKwh, etc.) are denormalized for fast dashboard
 * queries without having to deserialize the entire JSON blob.
 */
@Dao
interface SurveyDao {

    /**
     * Insert a new survey. Returns the auto-generated row ID.
     */
    @Insert(onConflict = OnConflictStrategy.ABORT)
    suspend fun insert(survey: SurveyEntity): Long

    /**
     * Update an existing survey (e.g. when user edits or recalculates).
     */
    @Update
    suspend fun update(survey: SurveyEntity)

    /**
     * Delete a survey by its entity (uses primary key).
     */
    @Delete
    suspend fun delete(survey: SurveyEntity)

    /**
     * Get a single survey by ID (null if not found).
     */
    @Query("SELECT * FROM surveys WHERE id = :id LIMIT 1")
    suspend fun findById(id: Long): SurveyEntity?

    /**
     * Reactive stream of all surveys for a given user, newest first.
     * Used in the "Past Surveys" list.
     */
    @Query("SELECT * FROM surveys WHERE ownerUsername = :username ORDER BY createdAt DESC")
    fun observeByUser(username: String): Flow<List<SurveyEntity>>

    /**
     * Count how many surveys a user has submitted.
     * Used for the dashboard KPI card.
     */
    @Query("SELECT COUNT(*) FROM surveys WHERE ownerUsername = :username")
    fun observeCountByUser(username: String): Flow<Int>

    /**
     * Sum of monthly kWh across all of this user's surveys, divided by count.
     * Returns null if user has no surveys yet.
     */
    @Query("""
        SELECT AVG(totalYearlyKwh / 12.0)
        FROM surveys
        WHERE ownerUsername = :username
    """)
    fun observeAvgMonthlyKwh(username: String): Flow<Double?>

    /**
     * Sum of yearly CO₂ across all of this user's surveys.
     * Used for the dashboard KPI.
     */
    @Query("""
        SELECT COALESCE(SUM(totalYearlyCo2Kg), 0.0)
        FROM surveys
        WHERE ownerUsername = :username
    """)
    fun observeTotalCo2(username: String): Flow<Double>
}