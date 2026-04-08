package com.univ.energymonitor.domain.model

data class LightingInfo(
    val numberOfIndoorLamps: String,
    val indoorLamps: List<LampInfo> = emptyList(),
    val hasOutdoorLighting: Boolean,
    val numberOfOutdoorLamps: String = "",
    val outdoorLamps: List<LampInfo> = emptyList()
)