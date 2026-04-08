package com.univ.energymonitor.domain.model

data class AcUnitInfo(
    val roomName: String = "",
    val roomSizeM2: String = "",
    val knowsCop: Boolean = true,
    val cop: String = "",
    val acYear: String = "",
    val capacityValue: String = "",
    val capacityUnit: String = "kW",
    val dailyUsageHours: String = "",
    val daysPerYear: String = ""
)