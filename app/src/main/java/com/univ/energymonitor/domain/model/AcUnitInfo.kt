package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class AcUnitInfo(
    val roomName: String = "",
    val roomSizeM2: String = "",
    val acType: String = "",
    val capacityValue: String = "",
    val capacityUnit: String = "BTU/h",
    val copMethod: String = "",
    val cop: String = "",
    val acYear: String = "",
    val dailyUsageHours: String = "",
    val daysPerYear: String = ""
)