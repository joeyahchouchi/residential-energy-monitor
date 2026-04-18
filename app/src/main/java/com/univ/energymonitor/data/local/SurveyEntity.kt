package com.univ.energymonitor.data.local

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

/**
 * Represents a completed survey with its calculated energy report.
 *
 * The full SurveyData object and EnergyReport are stored as JSON strings
 * (serialized with kotlinx.serialization) in single TEXT columns.
 * This avoids creating 15+ related tables while keeping all the data.
 */
@Entity(
    tableName = "surveys",
    foreignKeys = [
        ForeignKey(
            entity = UserEntity::class,
            parentColumns = ["username"],
            childColumns = ["ownerUsername"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [Index("ownerUsername")]
)
data class SurveyEntity(
    @PrimaryKey(autoGenerate = true)
    val id: Long = 0,

    val ownerUsername: String,            // which user created this survey
    val houseName: String,                // copied from houseInfo for quick listing
    val location: String,                 // copied from houseInfo for quick listing
    val createdAt: Long = System.currentTimeMillis(),

    // Full SurveyData serialized as JSON
    val surveyDataJson: String,

    // Full EnergyReport serialized as JSON (null if not yet calculated)
    val reportJson: String? = null,

    // Quick-access KPIs (denormalized for fast dashboard queries)
    val totalYearlyKwh: Double = 0.0,
    val totalYearlyCostUsd: Double = 0.0,
    val totalYearlyCo2Kg: Double = 0.0
)