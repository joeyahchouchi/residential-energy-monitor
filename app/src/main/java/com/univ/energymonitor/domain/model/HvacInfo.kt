package com.univ.energymonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class WaterHeaterInfo(
    val type: String = "",
    val tankSizeLiters: String = "",
    val tankInsulated: String = "",
    val powerKw: String = "",
    val dailyHours: String = "",
    val daysPerYear: String = "",
    val solarBackupType: String = "",
    val solarBackupHoursPerDay: String = "",
    val solarPanelLengthMeters: String = "",
    val solarPanelWidthMeters: String = "",
    val gasTankCountPerYear: String = "",
    val gasTankCostUsd: String = ""
)

@Serializable
data class HvacInfo(
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
    val waterHeaters: List<WaterHeaterInfo> = emptyList()
)
