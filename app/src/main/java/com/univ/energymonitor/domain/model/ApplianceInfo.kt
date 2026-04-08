package com.univ.energymonitor.domain.model

data class ApplianceInfo(
    val appliances: List<ApplianceItem> = emptyList(),
    val customAppliances: List<ApplianceItem> = emptyList()
)