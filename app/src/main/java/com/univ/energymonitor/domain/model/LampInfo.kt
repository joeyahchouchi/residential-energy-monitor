package com.univ.energymonitor.domain.model

data class LampInfo(
    val roomName: String = "",
    val bulbType: String = "",
    val powerWatts: String = "",
    val dailyUsageHours: String = "",
    val isDimmable: Boolean = false
)