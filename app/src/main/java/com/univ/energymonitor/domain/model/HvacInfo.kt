package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class HvacInfo(
    // Cooling
    val numberOfAcUnits: String,
    val acUnits: List<AcUnitInfo> = emptyList(),

    // Heating
    val heatingSystemType: String,
    val numberOfHeatingAcUnits: String = "",
    val heatingAcUnits: List<AcUnitInfo> = emptyList(),
    val numberOfHeatingUnits: String = "",
    val heatingPowerKw: String = "",
    val heatingDailyUsageHours: String = "",
    val heatingDaysPerYear: String = "",
    val heatingGasKgPerYear: String = "",
    val heatingFuelLitersPerYear: String = "",

    // Water Heating
    val waterHeaterType: String,
    val waterTankSizeLiters: String = "",
    val waterTankInsulated: String = "",
    val waterHeaterPowerKw: String = "",
    val waterHeaterDailyHours: String = "",
    val waterHeaterDaysPerYear: String = "",
    val solarWaterBackupType: String = "",
    val solarWaterBackupHoursPerDay: String = "",
    val gasTankKgPerYear: String = "",
    val fuelLitersPerYear: String = ""
)