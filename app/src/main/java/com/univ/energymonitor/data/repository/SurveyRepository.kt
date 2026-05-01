package com.univ.energymonitor.data.repository

import com.univ.energymonitor.data.local.SurveyDao
import com.univ.energymonitor.data.local.SurveyEntity
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.SurveyData
import kotlinx.coroutines.flow.Flow
import kotlinx.serialization.json.Json

/**
 * Single source of truth for survey data.
 * Handles serialization of SurveyData/EnergyReport to JSON for storage.
 */
class SurveyRepository(private val dao: SurveyDao) {

    private val json = Json {
        ignoreUnknownKeys = true
        prettyPrint = false
    }

    /**
     * Save a completed survey with its energy report.
     * Returns the generated survey ID.
     */
    suspend fun saveSurvey(
        username: String,
        surveyData: SurveyData,
        report: EnergyReport
    ): Long {
        val entity = SurveyEntity(
            ownerUsername = username.trim().lowercase(),
            houseName = surveyData.houseInfo?.houseName ?: "Unnamed",
            location = surveyData.houseInfo?.location ?: "",
            surveyDataJson = json.encodeToString(SurveyData.serializer(), surveyData),
            reportJson = json.encodeToString(EnergyReport.serializer(), report),
            totalYearlyKwh = report.totalYearlyKwh,
            totalYearlyCostUsd = report.totalYearlyCostUsd,
            totalYearlyCo2Kg = report.totalYearlyCo2Kg
        )
        return dao.insert(entity)
    }

    /**
     * Update an existing survey.
     */
    suspend fun updateSurvey(
        id: Long,
        username: String,
        surveyData: SurveyData,
        report: EnergyReport
    ) {
        val entity = SurveyEntity(
            id = id,
            ownerUsername = username.trim().lowercase(),
            houseName = surveyData.houseInfo?.houseName ?: "Unnamed",
            location = surveyData.houseInfo?.location ?: "",
            surveyDataJson = json.encodeToString(SurveyData.serializer(), surveyData),
            reportJson = json.encodeToString(EnergyReport.serializer(), report),
            totalYearlyKwh = report.totalYearlyKwh,
            totalYearlyCostUsd = report.totalYearlyCostUsd,
            totalYearlyCo2Kg = report.totalYearlyCo2Kg
        )
        dao.update(entity)
    }

    /**
     * Load a survey by ID and deserialize its data.
     * Returns a Pair of (SurveyData, EnergyReport?) or null if not found.
     */
    suspend fun loadSurvey(id: Long): Pair<SurveyData, EnergyReport?>? {
        val entity = dao.findById(id) ?: return null
        val surveyData = json.decodeFromString(SurveyData.serializer(), entity.surveyDataJson)
        val report = entity.reportJson?.let {
            json.decodeFromString(EnergyReport.serializer(), it)
        }
        return Pair(surveyData, report)
    }

    /**
     * Delete a survey by ID.
     */
    suspend fun deleteSurvey(id: Long) {
        val entity = dao.findById(id)
        if (entity != null) dao.delete(entity)
    }

    /**
     * Observe all surveys for a user (newest first).
     * Returns Flow of SurveyEntity list for the "Past Surveys" screen.
     */
    fun observeSurveys(username: String): Flow<List<SurveyEntity>> {
        return dao.observeByUser(username.trim().lowercase())
    }

    // ─── Dashboard KPI Flows ────────────────────────────────────────

    /**
     * Number of surveys submitted by this user.
     */
    fun observeSurveyCount(username: String): Flow<Int> {
        return dao.observeCountByUser(username.trim().lowercase())
    }

    /**
     * Average monthly kWh across all surveys for this user.
     */
    fun observeAvgMonthlyKwh(username: String): Flow<Double?> {
        return dao.observeAvgMonthlyKwh(username.trim().lowercase())
    }

    /**
     * Total yearly CO₂ across all surveys for this user.
     */
    fun observeTotalCo2(username: String): Flow<Double> {
        return dao.observeTotalCo2(username.trim().lowercase())
    }
}