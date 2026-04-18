package com.univ.energymonitor.data.local

import androidx.room.TypeConverter
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.SurveyData
import kotlinx.serialization.json.Json

/**
 * Type converters that let Room store complex Kotlin objects as JSON strings.
 * We need this because Room only knows how to store primitives (String, Int, Long, etc.).
 *
 * By serializing SurveyData and EnergyReport to JSON, we can save/load the entire
 * object tree with a single column instead of 15+ related tables.
 */
object Converters {

    // Configure the JSON serializer
    // - ignoreUnknownKeys: forward compatibility (new fields won't crash old data)
    // - prettyPrint: false for smaller storage
    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    // ─── SurveyData ⇄ String ───────────────────────────────────────────────
    @TypeConverter
    @JvmStatic
    fun fromSurveyData(data: SurveyData): String = json.encodeToString(SurveyData.serializer(), data)

    @TypeConverter
    @JvmStatic
    fun toSurveyData(value: String): SurveyData = json.decodeFromString(SurveyData.serializer(), value)

    // ─── EnergyReport? ⇄ String? ───────────────────────────────────────────
    @TypeConverter
    @JvmStatic
    fun fromEnergyReport(report: EnergyReport?): String? =
        report?.let { json.encodeToString(EnergyReport.serializer(), it) }

    @TypeConverter
    @JvmStatic
    fun toEnergyReport(value: String?): EnergyReport? =
        value?.let { json.decodeFromString(EnergyReport.serializer(), it) }
}