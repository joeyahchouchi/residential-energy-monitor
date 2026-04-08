package com.univ.energymonitor.domain.model

data class ApplianceItem(
    val name: String,
    val exists: Boolean = false,
    val powerWatts: String = "",
    val dailyUsageHours: String = ""
)