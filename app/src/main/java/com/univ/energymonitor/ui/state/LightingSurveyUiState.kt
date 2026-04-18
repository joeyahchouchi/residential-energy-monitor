package com.univ.energymonitor.ui.state

import com.univ.energymonitor.domain.model.IndirectLampInfo
import com.univ.energymonitor.domain.model.LampInfo

data class LightingSurveyUiState(
    val numberOfDirectLamps: String = "",
    val numberOfDirectTypes: String = "",
    val directLampSamples: List<LampInfo> = emptyList(),
    val hasIndirectLighting: Boolean = false,
    val numberOfIndirectRooms: String = "",
    val indirectRooms: List<IndirectLampInfo> = emptyList(),
    val hasOutdoorLighting: Boolean = false,
    val numberOfOutdoorLamps: String = "",
    val outdoorLamps: List<LampInfo> = emptyList(),
    val showErrors: Boolean = false
) {
    fun withUpdatedDirectTypes(): LightingSurveyUiState {
        val count = numberOfDirectTypes.toIntOrNull() ?: 0
        val newList = List(count) { i -> directLampSamples.getOrElse(i) { LampInfo() } }
        return copy(directLampSamples = newList)
    }

    fun withUpdatedIndirectRooms(): LightingSurveyUiState {
        val count = numberOfIndirectRooms.toIntOrNull() ?: 0
        val newList = List(count) { i -> indirectRooms.getOrElse(i) { IndirectLampInfo() } }
        return copy(indirectRooms = newList)
    }

    fun withUpdatedOutdoorCount(): LightingSurveyUiState {
        val count = numberOfOutdoorLamps.toIntOrNull() ?: 0
        val newList = List(count) { i -> outdoorLamps.getOrElse(i) { LampInfo() } }
        return copy(outdoorLamps = newList)
    }
}

fun LightingSurveyUiState.isValid(): Boolean {
    val direct = numberOfDirectLamps.toIntOrNull() ?: -1
    if (direct < 0) return false

    if (direct > 0) {
        val types = numberOfDirectTypes.toIntOrNull() ?: -1
        if (types !in 1..10) return false
        for (lamp in directLampSamples) {
            if (lamp.roomName.isBlank()) return false
            if (lamp.bulbType.isBlank()) return false
            val p = lamp.powerWatts.toDoubleOrNull() ?: -1.0
            if (p <= 0) return false
            val h = lamp.dailyUsageHours.toDoubleOrNull() ?: -1.0
            if (h !in 0.0..24.0) return false
        }
    }

    if (hasIndirectLighting) {
        val rooms = numberOfIndirectRooms.toIntOrNull() ?: -1
        if (rooms !in 1..20) return false
        for (room in indirectRooms) {
            if (room.roomName.isBlank()) return false
            val len = room.lengthMeters.toDoubleOrNull() ?: -1.0
            if (len <= 0) return false
            val p = room.powerWatts.toDoubleOrNull() ?: -1.0
            if (p <= 0) return false
            val h = room.dailyUsageHours.toDoubleOrNull() ?: -1.0
            if (h !in 0.0..24.0) return false
        }
    }

    if (hasOutdoorLighting) {
        val outCount = numberOfOutdoorLamps.toIntOrNull() ?: -1
        if (outCount !in 1..50) return false
        for (lamp in outdoorLamps) {
            val p = lamp.powerWatts.toDoubleOrNull() ?: -1.0
            if (p <= 0) return false
            val h = lamp.dailyUsageHours.toDoubleOrNull() ?: -1.0
            if (h !in 0.0..24.0) return false
        }
    }
    return true
}