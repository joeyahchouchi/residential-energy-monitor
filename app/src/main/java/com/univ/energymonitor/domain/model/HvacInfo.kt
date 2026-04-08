package com.univ.energymonitor.domain.model

data class HvacInfo(
    val numberOfAcUnits: String,
    val acUnits: List<AcUnitInfo> = emptyList(),
    val heatingSystemType: String,
    val numberOfHeatingUnits: String,
    val heatingDailyUsageHours: String,
    val numberOfHeatingAcUnits: String = "",
    val heatingAcUnits: List<AcUnitInfo> = emptyList(),
    val waterHeaterType: String,
    val waterHeaterPowerKw: String,
    val waterHeaterDailyHours: String,
    val waterTankSizeLiters: String
)