package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class IndirectLampInfo(
    val roomName: String = "",
    val lengthMeters: String = "",
    val powerWatts: String = "",
    val dailyUsageHours: String = ""
)