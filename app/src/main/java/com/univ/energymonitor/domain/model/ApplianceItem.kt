package com.univ.energymonitor.domain.model

import kotlinx.serialization.Serializable

@Serializable
data class ApplianceItem(
    val name: String,
    val exists: Boolean = false,
    val powerWatts: String = "",
    val dailyUsageHours: String = "",
    val efficiencyLabel: String = "",
    val purchaseYear: String = "",
    val userPickedUnknown: Boolean = false
)