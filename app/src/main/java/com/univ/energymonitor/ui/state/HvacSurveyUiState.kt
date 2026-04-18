package com.univ.energymonitor.ui.state

import com.univ.energymonitor.domain.model.AcUnitInfo

data class HvacSurveyUiState(
    // Cooling
    val numberOfAcUnits: String = "",
    val acUnits: List<AcUnitInfo> = emptyList(),

    // Heating
    val heatingSystemType: String = "",
    val numberOfHeatingAcUnits: String = "",
    val heatingAcUnits: List<AcUnitInfo> = emptyList(),
    val numberOfHeatingUnits: String = "",
    val heatingPowerKw: String = "",
    val heatingDailyUsageHours: String = "",
    val heatingDaysPerYear: String = "",
    val heatingGasKgPerYear: String = "",
    val heatingFuelLitersPerYear: String = "",

    // Water Heating
    val waterHeaterType: String = "",
    val waterTankSizeLiters: String = "",
    val waterTankInsulated: String = "",
    val waterHeaterPowerKw: String = "",
    val waterHeaterDailyHours: String = "",
    val waterHeaterDaysPerYear: String = "",
    val solarWaterBackupType: String = "",       // "None", "Electric", "Gas", "Diesel"
    val solarWaterBackupHoursPerDay: String = "",
    val gasTankKgPerYear: String = "",
    val fuelLitersPerYear: String = "",

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
    // Cooling validation
    val acCount = numberOfAcUnits.toIntOrNull() ?: -1
    if (acCount !in 0..20) return false

    for (unit in acUnits) {
        if (unit.roomName.isBlank()) return false
        val roomSize = unit.roomSizeM2.toDoubleOrNull() ?: -1.0
        if (roomSize !in 5.0..200.0) return false
        if (unit.acType.isBlank()) return false
        if (unit.capacityValue.isNotBlank()) {
            val capacity = unit.capacityValue.toDoubleOrNull() ?: -1.0
            if (capacity <= 0) return false
        }
        if (unit.copMethod.isBlank()) return false
        if (unit.copMethod == "I know the COP") {
            val cop = unit.cop.toDoubleOrNull() ?: -1.0
            if (cop !in 1.0..8.0) return false
        }
        if (unit.copMethod == "I know the AC year") {
            if (unit.acYear.isBlank()) return false
        }
        val hours = unit.dailyUsageHours.toDoubleOrNull() ?: -1.0
        if (hours !in 0.0..24.0) return false
        val days = unit.daysPerYear.toIntOrNull() ?: -1
        if (days !in 1..365) return false
    }

    // Heating validation
    if (heatingSystemType.isBlank()) return false
    when (heatingSystemType) {
        "AC" -> {
            val heatAcCount = numberOfHeatingAcUnits.toIntOrNull() ?: -1
            if (heatAcCount !in 1..20) return false
            for (unit in heatingAcUnits) {
                if (unit.roomName.isBlank()) return false
                val roomSize = unit.roomSizeM2.toDoubleOrNull() ?: -1.0
                if (roomSize !in 5.0..200.0) return false
                if (unit.acType.isBlank()) return false
                if (unit.capacityValue.isNotBlank()) {
                    val capacity = unit.capacityValue.toDoubleOrNull() ?: -1.0
                    if (capacity <= 0) return false
                }
                if (unit.copMethod.isBlank()) return false
                if (unit.copMethod == "I know the COP") {
                    val cop = unit.cop.toDoubleOrNull() ?: -1.0
                    if (cop !in 1.0..8.0) return false
                }
                if (unit.copMethod == "I know the AC year") {
                    if (unit.acYear.isBlank()) return false
                }
                val hours = unit.dailyUsageHours.toDoubleOrNull() ?: -1.0
                if (hours !in 0.0..24.0) return false
                val days = unit.daysPerYear.toIntOrNull() ?: -1
                if (days !in 1..365) return false
            }
        }
        "Electric Heater" -> {
            val units = numberOfHeatingUnits.toIntOrNull() ?: -1
            if (units !in 1..20) return false
            val power = heatingPowerKw.toDoubleOrNull() ?: -1.0
            if (power <= 0) return false
            val hrs = heatingDailyUsageHours.toDoubleOrNull() ?: -1.0
            if (hrs !in 0.0..24.0) return false
            val d = heatingDaysPerYear.toIntOrNull() ?: -1
            if (d !in 1..365) return false
        }
        "Gas Heater" -> {
            val kg = heatingGasKgPerYear.toDoubleOrNull() ?: -1.0
            if (kg <= 0) return false
        }
        "Diesel/Fuel Heater" -> {
            val liters = heatingFuelLitersPerYear.toDoubleOrNull() ?: -1.0
            if (liters <= 0) return false
        }
        "None" -> { /* no validation needed */ }
    }

    // Water heating validation
    if (waterHeaterType.isBlank()) return false
    when (waterHeaterType) {
        "Electrical Resistance" -> {
            if (waterTankSizeLiters.isBlank()) return false
            if (waterTankInsulated.isBlank()) return false
            val power = waterHeaterPowerKw.toDoubleOrNull() ?: -1.0
            if (power <= 0) return false
            val hrs = waterHeaterDailyHours.toDoubleOrNull() ?: -1.0
            if (hrs !in 0.0..24.0) return false
            val d = waterHeaterDaysPerYear.toIntOrNull() ?: -1
            if (d !in 1..365) return false
        }
        "Solar Heater" -> {
            if (waterTankSizeLiters.isBlank()) return false
            if (waterTankInsulated.isBlank()) return false
            if (solarWaterBackupType.isBlank()) return false
            if (solarWaterBackupType != "None") {
                val hrs = solarWaterBackupHoursPerDay.toDoubleOrNull() ?: -1.0
                if (hrs !in 0.0..24.0) return false
            }
        }
        "Gas Tank" -> {
            if (waterTankSizeLiters.isBlank()) return false
            if (waterTankInsulated.isBlank()) return false
            val kg = gasTankKgPerYear.toDoubleOrNull() ?: -1.0
            if (kg <= 0) return false
        }
        "Fuel Heating" -> {
            if (waterTankSizeLiters.isBlank()) return false
            if (waterTankInsulated.isBlank()) return false
            val liters = fuelLitersPerYear.toDoubleOrNull() ?: -1.0
            if (liters <= 0) return false
        }
        "None" -> { /* no validation needed */ }
    }

    return true
}