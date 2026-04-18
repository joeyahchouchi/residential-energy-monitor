package com.univ.energymonitor.domain.model
import kotlinx.serialization.Serializable
@Serializable
data class ApplianceInfo(
    val appliances: List<ApplianceItem> = emptyList(),
    val customAppliances: List<ApplianceItem> = emptyList()
)