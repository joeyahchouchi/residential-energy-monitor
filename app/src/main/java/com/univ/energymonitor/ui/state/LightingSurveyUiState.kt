package com.univ.energymonitor.ui.state

import com.univ.energymonitor.domain.model.LampInfo

data class LightingSurveyUiState(
    val numberOfIndoorLamps: String = "",
    val indoorLamps: List<LampInfo> = emptyList(),
    val hasOutdoorLighting: Boolean = false,
    val numberOfOutdoorLamps: String = "",
    val outdoorLamps: List<LampInfo> = emptyList(),
    val showErrors: Boolean = false
) {
    fun withUpdatedIndoorCount(): LightingSurveyUiState {
        val count = numberOfIndoorLamps.toIntOrNull() ?: 0
        val newList = List(count) { index ->
            indoorLamps.getOrElse(index) { LampInfo() }
        }
        return copy(indoorLamps = newList)
    }

    fun withUpdatedOutdoorCount(): LightingSurveyUiState {
        val count = numberOfOutdoorLamps.toIntOrNull() ?: 0
        val newList = List(count) { index ->
            outdoorLamps.getOrElse(index) { LampInfo() }
        }
        return copy(outdoorLamps = newList)
    }
}

fun LightingSurveyUiState.isValid(): Boolean {
    val indoorCount = numberOfIndoorLamps.toIntOrNull() ?: -1
    if (indoorCount !in 1..100) return false

    for (lamp in indoorLamps) {
        if (lamp.roomName.isBlank()) return false
        if (lamp.bulbType.isBlank()) return false
        val power = lamp.powerWatts.toDoubleOrNull() ?: -1.0
        if (power <= 0) return false
        val hours = lamp.dailyUsageHours.toDoubleOrNull() ?: -1.0
        if (hours !in 0.0..24.0) return false
    }

    if (hasOutdoorLighting) {
        val outdoorCount = numberOfOutdoorLamps.toIntOrNull() ?: -1
        if (outdoorCount !in 1..50) return false

        for (lamp in outdoorLamps) {
            if (lamp.bulbType.isBlank()) return false
            val power = lamp.powerWatts.toDoubleOrNull() ?: -1.0
            if (power <= 0) return false
            val hours = lamp.dailyUsageHours.toDoubleOrNull() ?: -1.0
            if (hours !in 0.0..24.0) return false
        }
    }

    return true
}