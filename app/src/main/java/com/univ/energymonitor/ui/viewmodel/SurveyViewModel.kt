package com.univ.energymonitor.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.univ.energymonitor.EnergyMonitorApp
import com.univ.energymonitor.data.local.SurveyEntity
import com.univ.energymonitor.data.repository.SurveyRepository
import com.univ.energymonitor.domain.model.EnergyReport
import com.univ.energymonitor.domain.model.SurveyData
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.launch

/**
 * Manages survey persistence: saving, loading, and dashboard KPIs.
 * Bridges the UI (MainActivity) with Room via SurveyRepository.
 */
class SurveyViewModel(application: Application) : AndroidViewModel(application) {

    private val surveyRepository: SurveyRepository =
        (application as EnergyMonitorApp).container.surveyRepository

    // Last saved survey ID (for feedback after submission)
    private val _lastSavedId = MutableStateFlow<Long?>(null)
    val lastSavedId: StateFlow<Long?> = _lastSavedId

    /**
     * Save a completed survey to the database.
     */
    fun saveSurvey(username: String, surveyData: SurveyData, report: EnergyReport) {
        viewModelScope.launch {
            val id = surveyRepository.saveSurvey(username, surveyData, report)
            _lastSavedId.value = id
        }
    }

    /**
     * Update an existing survey.
     */
    fun updateSurvey(id: Long, username: String, surveyData: SurveyData, report: EnergyReport) {
        viewModelScope.launch {
            surveyRepository.updateSurvey(id, username, surveyData, report)
        }
    }

    /**
     * Load a survey by ID. Returns (SurveyData, EnergyReport?) or null.
     */
    suspend fun loadSurvey(id: Long): Pair<SurveyData, EnergyReport?>? {
        return surveyRepository.loadSurvey(id)
    }

    /**
     * Delete a survey by ID.
     */
    fun deleteSurvey(id: Long) {
        viewModelScope.launch {
            surveyRepository.deleteSurvey(id)
        }
    }

    // ─── Dashboard KPI Flows ────────────────────────────────────────

    /**
     * Observe all past surveys for a user (for "Past Surveys" list).
     */
    fun observeSurveys(username: String): Flow<List<SurveyEntity>> {
        return surveyRepository.observeSurveys(username)
    }

    /**
     * Observe survey count for dashboard KPI.
     */
    fun observeSurveyCount(username: String): Flow<Int> {
        return surveyRepository.observeSurveyCount(username)
    }

    /**
     * Observe average monthly kWh for dashboard KPI.
     */
    fun observeAvgMonthlyKwh(username: String): Flow<Double?> {
        return surveyRepository.observeAvgMonthlyKwh(username)
    }

    /**
     * Observe total CO₂ for dashboard KPI.
     */
    fun observeTotalCo2(username: String): Flow<Double> {
        return surveyRepository.observeTotalCo2(username)
    }
}