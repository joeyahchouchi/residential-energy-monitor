package com.univ.energymonitor.ui.state

import com.univ.energymonitor.domain.model.AcUnitInfo
import com.univ.energymonitor.domain.model.WaterHeaterInfo

data class HvacSurveyUiState(
    val numberOfAcUnits: String = "",
    val acUnits: List<AcUnitInfo> = emptyList(),

    val heatingSystemType: String = "",
    val heatedAreaM2: String = "",
    val numberOfHeatingAcUnits: String = "",
    val heatingAcUnits: List<AcUnitInfo> = emptyList(),
    val numberOfHeatingUnits: String = "",
    val heatingPowerKw: String = "",
    val heatingDailyUsageHours: String = "",
    val heatingDaysPerYear: String = "",
    val heatingGasKgPerYear: String = "",
    val heatingFuelLitersPerYear: String = "",

    val heatingEfficiencyMethod: String = "",
    val heatingEfficiencyPercent: String = "",
    val heatingInstallationYear: String = "",

    val numberOfWaterHeaters: String = "",
    val waterHeaters: List<WaterHeaterInfo> = emptyList(),

    val showErrors: Boolean = false
)

fun HvacSurveyUiState.withUpdatedAcCount(): HvacSurveyUiState {
    val count = numberOfAcUnits.toIntOrNull()?.coerceIn(0, 20) ?: 0
    return copy(
        acUnits = List(count) { index -> acUnits.getOrNull(index) ?: AcUnitInfo() }
    )
}

fun HvacSurveyUiState.withUpdatedHeatingAcCount(): HvacSurveyUiState {
    val count = numberOfHeatingAcUnits.toIntOrNull()?.coerceIn(0, 20) ?: 0
    return copy(
        heatingAcUnits = List(count) { index -> heatingAcUnits.getOrNull(index) ?: AcUnitInfo() }
    )
}

fun HvacSurveyUiState.withUpdatedWaterHeaterCount(): HvacSurveyUiState {
    val count = numberOfWaterHeaters.toIntOrNull()?.coerceIn(0, 10) ?: 0
    return copy(
        waterHeaters = List(count) { index -> waterHeaters.getOrNull(index) ?: WaterHeaterInfo() }
    )
}

private fun HvacSurveyUiState.hasValidHeatingEfficiency(): Boolean {
    return when (heatingEfficiencyMethod) {
        "I know the efficiency" -> {
            val efficiency = heatingEfficiencyPercent.toDoubleOrNull() ?: return false
            efficiency in 1.0..100.0
        }

        "I know the installation year" -> heatingInstallationYear.isNotBlank()

        "I don't know" -> true

        else -> false
    }
}

private fun HvacSurveyUiState.hasValidHeatedArea(): Boolean {
    val area = heatedAreaM2.toDoubleOrNull() ?: return false
    return area in 1.0..1000.0
}

fun HvacSurveyUiState.isValid(): Boolean {
    val acCount = numberOfAcUnits.toIntOrNull()
    if (acCount == null || acCount !in 0..20) return false

    if (acCount != acUnits.size) return false

    acUnits.forEach { unit ->
        if (unit.roomName.isBlank()) return false

        val roomSize = unit.roomSizeM2.toDoubleOrNull() ?: return false
        if (roomSize !in 5.0..200.0) return false

        if (unit.acType.isBlank()) return false
        if (unit.copMethod.isBlank()) return false

        if (unit.copMethod == "I know the COP") {
            val cop = unit.cop.toDoubleOrNull() ?: return false
            if (cop !in 1.0..8.0) return false
        }

        if (unit.copMethod == "I know the AC year" && unit.acYear.isBlank()) return false

        val hours = unit.dailyUsageHours.toDoubleOrNull() ?: return false
        if (hours !in 0.0..24.0) return false

        val days = unit.daysPerYear.toIntOrNull() ?: return false
        if (days !in 1..365) return false
    }

    if (heatingSystemType.isBlank()) return false

    when (heatingSystemType) {
        "AC" -> {
            val count = numberOfHeatingAcUnits.toIntOrNull() ?: return false
            if (count !in 1..20 || count != heatingAcUnits.size) return false

            heatingAcUnits.forEach { unit ->
                if (unit.roomName.isBlank()) return false

                val roomSize = unit.roomSizeM2.toDoubleOrNull() ?: return false
                if (roomSize !in 5.0..200.0) return false

                if (unit.acType.isBlank()) return false
                if (unit.copMethod.isBlank()) return false

                if (unit.copMethod == "I know the COP") {
                    val cop = unit.cop.toDoubleOrNull() ?: return false
                    if (cop !in 1.0..8.0) return false
                }

                if (unit.copMethod == "I know the AC year" && unit.acYear.isBlank()) return false

                val hours = unit.dailyUsageHours.toDoubleOrNull() ?: return false
                if (hours !in 0.0..24.0) return false

                val days = unit.daysPerYear.toIntOrNull() ?: return false
                if (days !in 1..365) return false
            }
        }

        "Electric Heater" -> {
            if (!hasValidHeatedArea()) return false

            val units = numberOfHeatingUnits.toIntOrNull() ?: return false
            if (units !in 1..20) return false

            val power = heatingPowerKw.toDoubleOrNull() ?: return false
            if (power <= 0) return false

            val hours = heatingDailyUsageHours.toDoubleOrNull() ?: return false
            if (hours !in 0.0..24.0) return false

            val days = heatingDaysPerYear.toIntOrNull() ?: return false
            if (days !in 1..365) return false

            if (!hasValidHeatingEfficiency()) return false
        }

        "Gas Heater" -> {
            if (!hasValidHeatedArea()) return false

            val gas = heatingGasKgPerYear.toDoubleOrNull() ?: return false
            if (gas <= 0) return false

            if (!hasValidHeatingEfficiency()) return false
        }

        "Diesel/Fuel Heater" -> {
            if (!hasValidHeatedArea()) return false

            val fuelTanks = heatingFuelLitersPerYear.toDoubleOrNull() ?: return false
            if (fuelTanks <= 0) return false

            if (!hasValidHeatingEfficiency()) return false
        }

        "None" -> Unit

        else -> return false
    }

    val waterCount = numberOfWaterHeaters.toIntOrNull() ?: return false
    if (waterCount !in 0..10 || waterCount != waterHeaters.size) return false

    waterHeaters.forEach { heater ->
        if (heater.type.isBlank()) return false

        if (heater.type != "None") {
            if (heater.tankSizeLiters.isBlank()) return false
            if (heater.tankInsulated.isBlank()) return false
        }

        when (heater.type) {
            "Electrical Resistance" -> {
                val power = heater.powerKw.toDoubleOrNull() ?: return false
                if (power <= 0) return false

                val hours = heater.dailyHours.toDoubleOrNull() ?: return false
                if (hours !in 0.0..24.0) return false

                val days = heater.daysPerYear.toIntOrNull() ?: return false
                if (days !in 1..365) return false
            }

            "Solar Heater" -> {
                val length = heater.solarPanelLengthMeters.toDoubleOrNull() ?: return false
                val width = heater.solarPanelWidthMeters.toDoubleOrNull() ?: return false

                if (length <= 0 || width <= 0) return false

                if (heater.solarBackupType.isBlank()) return false

                if (heater.solarBackupType != "None") {
                    val backupHours = heater.solarBackupHoursPerDay.toDoubleOrNull() ?: return false
                    if (backupHours !in 0.0..24.0) return false
                }
            }

            "Gas Tank" -> {
                val tanks = heater.gasTankCountPerYear.toIntOrNull() ?: return false
                if (tanks <= 0) return false

                val cost = heater.gasTankCostUsd.toDoubleOrNull() ?: return false
                if (cost <= 0) return false
            }

            "None" -> Unit

            else -> return false
        }
    }

    return true
}
