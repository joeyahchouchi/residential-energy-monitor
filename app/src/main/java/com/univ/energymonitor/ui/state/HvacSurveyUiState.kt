package com.univ.energymonitor.ui.state

import com.univ.energymonitor.domain.model.AcUnitInfo

data class HvacSurveyUiState(
    val numberOfAcUnits: String = "",
    val acUnits: List<AcUnitInfo> = emptyList(),
    val heatingSystemType: String = "",
    val numberOfHeatingUnits: String = "",
    val heatingDailyUsageHours: String = "",
    val numberOfHeatingAcUnits: String = "",
    val heatingAcUnits: List<AcUnitInfo> = emptyList(),
    val waterHeaterType: String = "",
    val waterHeaterPowerKw: String = "",
    val waterHeaterDailyHours: String = "",
    val waterTankSizeLiters: String = "",
    val showErrors: Boolean = false
) {
    fun withUpdatedAcCount(): HvacSurveyUiState {
        val count = numberOfAcUnits.toIntOrNull() ?: 0
        val newList = List(count) { index ->
            acUnits.getOrElse(index) { AcUnitInfo() }
        }
        return copy(acUnits = newList)
    }

    fun withUpdatedHeatingAcCount(): HvacSurveyUiState {
        val count = numberOfHeatingAcUnits.toIntOrNull() ?: 0
        val newList = List(count) { index ->
            heatingAcUnits.getOrElse(index) { AcUnitInfo() }
        }
        return copy(heatingAcUnits = newList)
    }
}

fun HvacSurveyUiState.isValid(): Boolean {
    val acCount = numberOfAcUnits.toIntOrNull() ?: -1
    if (acCount !in 0..20) return false

    for (unit in acUnits) {
        if (unit.roomName.isBlank()) return false
        val roomSize = unit.roomSizeM2.toDoubleOrNull() ?: -1.0
        if (roomSize !in 5.0..200.0) return false
        val capacity = unit.capacityValue.toDoubleOrNull() ?: -1.0
        if (capacity <= 0) return false
        if (unit.knowsCop) {
            val cop = unit.cop.toDoubleOrNull() ?: -1.0
            if (cop !in 1.0..8.0) return false
        } else {
            if (unit.acYear.isBlank()) return false
        }
        val hours = unit.dailyUsageHours.toDoubleOrNull() ?: -1.0
        if (hours !in 0.0..24.0) return false
        val days = unit.daysPerYear.toIntOrNull() ?: -1
        if (days !in 1..365) return false
    }

    if (heatingSystemType.isBlank()) return false
    if (heatingSystemType == "AC") {
        val heatAcCount = numberOfHeatingAcUnits.toIntOrNull() ?: -1
        if (heatAcCount !in 1..20) return false
        for (unit in heatingAcUnits) {
            if (unit.roomName.isBlank()) return false
            val roomSize = unit.roomSizeM2.toDoubleOrNull() ?: -1.0
            if (roomSize !in 5.0..200.0) return false
            val capacity = unit.capacityValue.toDoubleOrNull() ?: -1.0
            if (capacity <= 0) return false
            if (unit.knowsCop) {
                val cop = unit.cop.toDoubleOrNull() ?: -1.0
                if (cop !in 1.0..8.0) return false
            } else {
                if (unit.acYear.isBlank()) return false
            }
            val hours = unit.dailyUsageHours.toDoubleOrNull() ?: -1.0
            if (hours !in 0.0..24.0) return false
            val days = unit.daysPerYear.toIntOrNull() ?: -1
            if (days !in 1..365) return false
        }
    } else if (heatingSystemType != "None") {
        val heatingUnits = numberOfHeatingUnits.toIntOrNull() ?: -1
        if (heatingUnits !in 1..20) return false
        val heatingHours = heatingDailyUsageHours.toDoubleOrNull() ?: -1.0
        if (heatingHours !in 0.0..24.0) return false
    }

    if (waterHeaterType.isBlank()) return false
    if (waterHeaterType != "None") {
        val whPower = waterHeaterPowerKw.toDoubleOrNull() ?: -1.0
        if (whPower <= 0) return false
        val whHours = waterHeaterDailyHours.toDoubleOrNull() ?: -1.0
        if (whHours !in 0.0..24.0) return false
        val tank = waterTankSizeLiters.toIntOrNull() ?: -1
        if (tank !in 1..500) return false
    }

    return true
}